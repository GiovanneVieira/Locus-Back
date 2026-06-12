package com.project.locusapi.service.metrics;

import com.project.locusapi.constant.metrics.CriticalFailureType;
import com.project.locusapi.constant.metrics.LoginDeviceType;
import com.project.locusapi.constant.metrics.UserMetricEventType;
import com.project.locusapi.event.metrics.LoginFailedEvent;
import com.project.locusapi.event.metrics.LoginSucceededEvent;
import com.project.locusapi.event.metrics.OtpValidationFailedEvent;
import com.project.locusapi.event.metrics.RentalCreatedEvent;
import com.project.locusapi.event.metrics.RentalStatusChangedEvent;
import com.project.locusapi.event.metrics.S3UploadTrackedEvent;
import com.project.locusapi.event.metrics.UserActivatedEvent;
import com.project.locusapi.event.metrics.UserRegisteredEvent;
import com.project.locusapi.model.metrics.CriticalFailureMetric;
import com.project.locusapi.model.metrics.LoginAccessMetric;
import com.project.locusapi.model.metrics.RentalMetricEvent;
import com.project.locusapi.model.metrics.StorageUploadMetric;
import com.project.locusapi.model.metrics.UserMetricEvent;
import com.project.locusapi.repository.metrics.CriticalFailureMetricRepository;
import com.project.locusapi.repository.metrics.LoginAccessMetricRepository;
import com.project.locusapi.repository.metrics.RentalMetricEventRepository;
import com.project.locusapi.repository.metrics.StorageUploadMetricRepository;
import com.project.locusapi.repository.metrics.UserMetricEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class MetricsListener {

    private final UserMetricEventRepository userMetricEventRepository;
    private final LoginAccessMetricRepository loginAccessMetricRepository;
    private final RentalMetricEventRepository rentalMetricEventRepository;
    private final StorageUploadMetricRepository storageUploadMetricRepository;
    private final CriticalFailureMetricRepository criticalFailureMetricRepository;

    @Async("applicationTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onUserRegistered(UserRegisteredEvent event) {
        userMetricEventRepository.save(UserMetricEvent.builder()
                .userId(event.userId())
                .email(event.email())
                .eventType(UserMetricEventType.REGISTERED)
                .enabled(event.enabled())
                .authProvider(event.authProvider())
                .occurredAt(event.occurredAt())
                .build());
    }

    @Async("applicationTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onUserActivated(UserActivatedEvent event) {
        userMetricEventRepository.save(UserMetricEvent.builder()
                .userId(event.userId())
                .email(event.email())
                .eventType(UserMetricEventType.ACTIVATED)
                .enabled(true)
                .occurredAt(event.occurredAt())
                .build());
    }

    @Async("applicationTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onLoginSucceeded(LoginSucceededEvent event) {
        var userAgent = analyzeUserAgent(event.userAgent());
        loginAccessMetricRepository.save(LoginAccessMetric.builder()
                .userId(event.userId())
                .email(event.email())
                .success(true)
                .rawUserAgent(event.userAgent())
                .deviceType(userAgent.deviceType())
                .operatingSystem(userAgent.operatingSystem())
                .occurredAt(event.occurredAt())
                .build());
    }

    @Async("applicationTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onLoginFailed(LoginFailedEvent event) {
        var userAgent = analyzeUserAgent(event.userAgent());
        loginAccessMetricRepository.save(LoginAccessMetric.builder()
                .email(event.email())
                .success(false)
                .reason(event.reason())
                .rawUserAgent(event.userAgent())
                .deviceType(userAgent.deviceType())
                .operatingSystem(userAgent.operatingSystem())
                .occurredAt(event.occurredAt())
                .build());

        criticalFailureMetricRepository.save(CriticalFailureMetric.builder()
                .email(event.email())
                .failureType(CriticalFailureType.LOGIN_INVALID)
                .reason(event.reason())
                .occurredAt(event.occurredAt())
                .build());
    }

    @Async("applicationTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onOtpValidationFailed(OtpValidationFailedEvent event) {
        criticalFailureMetricRepository.save(CriticalFailureMetric.builder()
                .email(event.email())
                .failureType(event.failureType())
                .reason(event.reason())
                .occurredAt(event.occurredAt())
                .build());
    }

    @Async("applicationTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onRentalCreated(RentalCreatedEvent event) {
        rentalMetricEventRepository.save(RentalMetricEvent.builder()
                .rentalId(event.rentalId())
                .renterId(event.renterId())
                .rentableAddressId(event.rentableAddressId())
                .eventType("CREATED")
                .status(event.status())
                .occurredAt(event.occurredAt())
                .build());
    }

    @Async("applicationTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onRentalStatusChanged(RentalStatusChangedEvent event) {
        rentalMetricEventRepository.save(RentalMetricEvent.builder()
                .rentalId(event.rentalId())
                .eventType("STATUS_CHANGED")
                .previousStatus(event.previousStatus())
                .status(event.newStatus())
                .occurredAt(event.occurredAt())
                .build());
    }

    @Async("applicationTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onS3UploadTracked(S3UploadTrackedEvent event) {
        storageUploadMetricRepository.save(StorageUploadMetric.builder()
                .imageId(event.imageId())
                .hostId(event.hostId())
                .fileSize(event.fileSize())
                .contentType(event.contentType())
                .occurredAt(event.occurredAt())
                .build());
    }

    private UserAgentAnalysis analyzeUserAgent(String rawUserAgent) {
        if (rawUserAgent == null || rawUserAgent.isBlank()) {
            return new UserAgentAnalysis(LoginDeviceType.UNKNOWN, "Unknown");
        }

        String userAgent = rawUserAgent.toLowerCase();
        LoginDeviceType deviceType = (userAgent.contains("mobile") || userAgent.contains("android") || userAgent.contains("iphone"))
                ? LoginDeviceType.MOBILE
                : LoginDeviceType.WEB;

        String operatingSystem;
        if (userAgent.contains("hyperos")) {
            operatingSystem = "Android/HyperOS";
        } else if (userAgent.contains("android")) {
            operatingSystem = "Android";
        } else if (userAgent.contains("iphone") || userAgent.contains("ipad") || userAgent.contains("ios")) {
            operatingSystem = "iOS";
        } else if (userAgent.contains("windows")) {
            operatingSystem = "Windows";
        } else if (userAgent.contains("linux")) {
            operatingSystem = "Linux";
        } else if (userAgent.contains("mac os") || userAgent.contains("macintosh")) {
            operatingSystem = "macOS";
        } else {
            operatingSystem = "Unknown";
        }

        return new UserAgentAnalysis(deviceType, operatingSystem);
    }

    private record UserAgentAnalysis(LoginDeviceType deviceType, String operatingSystem) {
    }
}
