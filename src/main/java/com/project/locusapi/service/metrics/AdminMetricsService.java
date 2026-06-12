package com.project.locusapi.service.metrics;

import com.project.locusapi.constant.RentalStatus;
import com.project.locusapi.constant.metrics.MetricGranularity;
import com.project.locusapi.constant.metrics.UserMetricEventType;
import com.project.locusapi.dto.admin.metrics.AccessPlatformMetricsDTO;
import com.project.locusapi.dto.admin.metrics.AdminMetricsOverviewDTO;
import com.project.locusapi.dto.admin.metrics.CriticalFailureMetricResponseDTO;
import com.project.locusapi.dto.admin.metrics.CriticalFailureMetricsDTO;
import com.project.locusapi.dto.admin.metrics.LoginAccessMetricResponseDTO;
import com.project.locusapi.dto.admin.metrics.MetricCountDTO;
import com.project.locusapi.dto.admin.metrics.RentalConversionMetricsDTO;
import com.project.locusapi.dto.admin.metrics.StorageUploadMetricsDTO;
import com.project.locusapi.dto.admin.metrics.TimeBucketMetricDTO;
import com.project.locusapi.dto.admin.metrics.UserAcquisitionMetricsDTO;
import com.project.locusapi.model.metrics.CriticalFailureMetric;
import com.project.locusapi.model.metrics.LoginAccessMetric;
import com.project.locusapi.repository.metrics.CriticalFailureMetricRepository;
import com.project.locusapi.repository.metrics.LoginAccessMetricRepository;
import com.project.locusapi.repository.metrics.RentalMetricEventRepository;
import com.project.locusapi.repository.metrics.StorageUploadMetricRepository;
import com.project.locusapi.repository.metrics.UserMetricEventRepository;
import com.project.locusapi.repository.metrics.projection.MetricCountProjection;
import com.project.locusapi.repository.metrics.projection.TimeBucketMetricProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminMetricsService {

    private static final String RENTAL_CREATED_EVENT = "CREATED";
    private static final String RENTAL_STATUS_CHANGED_EVENT = "STATUS_CHANGED";

    private final UserMetricEventRepository userMetricEventRepository;
    private final LoginAccessMetricRepository loginAccessMetricRepository;
    private final RentalMetricEventRepository rentalMetricEventRepository;
    private final StorageUploadMetricRepository storageUploadMetricRepository;
    private final CriticalFailureMetricRepository criticalFailureMetricRepository;

    @Transactional(readOnly = true)
    public AdminMetricsOverviewDTO getOverview(LocalDateTime start, LocalDateTime end, MetricGranularity granularity) {
        LocalDateTime resolvedEnd = end != null ? end : LocalDateTime.now();
        LocalDateTime resolvedStart = start != null ? start : resolvedEnd.minusDays(30);

        if (resolvedEnd.isBefore(resolvedStart)) {
            throw new IllegalArgumentException("A data final não pode ser anterior à data inicial.");
        }

        var users = getUserAcquisitionMetrics(resolvedStart, resolvedEnd, granularity);
        var access = getAccessPlatformMetrics(resolvedStart, resolvedEnd);
        var rentals = getRentalConversionMetrics(resolvedStart, resolvedEnd);
        var uploads = getStorageUploadMetrics(resolvedStart, resolvedEnd);
        var failures = getCriticalFailureMetrics(resolvedStart, resolvedEnd);

        return new AdminMetricsOverviewDTO(resolvedStart, resolvedEnd, users, access, rentals, uploads, failures);
    }

    @Transactional(readOnly = true)
    public Page<LoginAccessMetricResponseDTO> getAccessLogs(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        LocalDateTime resolvedEnd = end != null ? end : LocalDateTime.now();
        LocalDateTime resolvedStart = start != null ? start : resolvedEnd.minusDays(30);
        return loginAccessMetricRepository.findByOccurredAtBetween(resolvedStart, resolvedEnd, pageable)
                .map(this::toLoginAccessResponse);
    }

    @Transactional(readOnly = true)
    public Page<CriticalFailureMetricResponseDTO> getCriticalFailures(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        LocalDateTime resolvedEnd = end != null ? end : LocalDateTime.now();
        LocalDateTime resolvedStart = start != null ? start : resolvedEnd.minusDays(30);
        return criticalFailureMetricRepository.findByOccurredAtBetween(resolvedStart, resolvedEnd, pageable)
                .map(this::toCriticalFailureResponse);
    }

    private UserAcquisitionMetricsDTO getUserAcquisitionMetrics(LocalDateTime start, LocalDateTime end, MetricGranularity granularity) {
        LocalDateTime now = LocalDateTime.now();
        long daily = userMetricEventRepository.countByEventTypeAndOccurredAtGreaterThanEqual(UserMetricEventType.REGISTERED, now.minusDays(1));
        long weekly = userMetricEventRepository.countByEventTypeAndOccurredAtGreaterThanEqual(UserMetricEventType.REGISTERED, now.minusWeeks(1));
        long monthly = userMetricEventRepository.countByEventTypeAndOccurredAtGreaterThanEqual(UserMetricEventType.REGISTERED, now.minusMonths(1));
        long activated = userMetricEventRepository.countByEventTypeAndOccurredAtBetween(UserMetricEventType.ACTIVATED, start, end);
        List<TimeBucketMetricDTO> series = userMetricEventRepository
                .countByBucket(UserMetricEventType.REGISTERED.name(), granularity.getDateTruncValue(), start, end)
                .stream()
                .map(this::toTimeBucketMetric)
                .toList();

        return new UserAcquisitionMetricsDTO(daily, weekly, monthly, activated, series);
    }

    private AccessPlatformMetricsDTO getAccessPlatformMetrics(LocalDateTime start, LocalDateTime end) {
        long successfulLogins = loginAccessMetricRepository.countBySuccessAndOccurredAtBetween(true, start, end);
        long failedLogins = loginAccessMetricRepository.countBySuccessAndOccurredAtBetween(false, start, end);
        return new AccessPlatformMetricsDTO(
                successfulLogins,
                failedLogins,
                toMetricCounts(loginAccessMetricRepository.countSuccessfulLoginsByDevice(start, end)),
                toMetricCounts(loginAccessMetricRepository.countSuccessfulLoginsByOperatingSystem(start, end))
        );
    }

    private RentalConversionMetricsDTO getRentalConversionMetrics(LocalDateTime start, LocalDateTime end) {
        long created = rentalMetricEventRepository.countByEventTypeAndOccurredAtBetween(RENTAL_CREATED_EVENT, start, end);
        long confirmed = rentalMetricEventRepository.countByEventTypeAndStatusAndOccurredAtBetween(
                RENTAL_STATUS_CHANGED_EVENT,
                RentalStatus.CONFIRMED,
                start,
                end
        );
        double conversionRate = created == 0 ? 0 : (double) confirmed / created;
        return new RentalConversionMetricsDTO(created, confirmed, conversionRate);
    }

    private StorageUploadMetricsDTO getStorageUploadMetrics(LocalDateTime start, LocalDateTime end) {
        long count = storageUploadMetricRepository.countByOccurredAtBetween(start, end);
        long totalBytes = storageUploadMetricRepository.sumFileSizeBetween(start, end);
        double averageBytes = storageUploadMetricRepository.averageFileSizeBetween(start, end);
        return new StorageUploadMetricsDTO(count, totalBytes, averageBytes);
    }

    private CriticalFailureMetricsDTO getCriticalFailureMetrics(LocalDateTime start, LocalDateTime end) {
        long total = criticalFailureMetricRepository.countByOccurredAtBetween(start, end);
        return new CriticalFailureMetricsDTO(total, toMetricCounts(criticalFailureMetricRepository.countByFailureType(start, end)));
    }

    private List<MetricCountDTO> toMetricCounts(List<MetricCountProjection> projections) {
        return projections.stream()
                .map(projection -> new MetricCountDTO(projection.getLabel(), projection.getTotal()))
                .toList();
    }

    private TimeBucketMetricDTO toTimeBucketMetric(TimeBucketMetricProjection projection) {
        return new TimeBucketMetricDTO(projection.getBucket(), projection.getTotal());
    }

    private LoginAccessMetricResponseDTO toLoginAccessResponse(LoginAccessMetric metric) {
        return new LoginAccessMetricResponseDTO(
                metric.getId(),
                metric.getUserId(),
                metric.getEmail(),
                metric.isSuccess(),
                metric.getReason(),
                metric.getDeviceType(),
                metric.getOperatingSystem(),
                metric.getOccurredAt()
        );
    }

    private CriticalFailureMetricResponseDTO toCriticalFailureResponse(CriticalFailureMetric metric) {
        return new CriticalFailureMetricResponseDTO(
                metric.getId(),
                metric.getEmail(),
                metric.getFailureType(),
                metric.getReason(),
                metric.getOccurredAt()
        );
    }
}
