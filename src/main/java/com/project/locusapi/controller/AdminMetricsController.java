package com.project.locusapi.controller;

import com.project.locusapi.constant.metrics.MetricGranularity;
import com.project.locusapi.dto.admin.metrics.AdminMetricsOverviewDTO;
import com.project.locusapi.dto.admin.metrics.CriticalFailureMetricResponseDTO;
import com.project.locusapi.dto.admin.metrics.LoginAccessMetricResponseDTO;
import com.project.locusapi.service.metrics.AdminMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin/metrics")
@RequiredArgsConstructor
public class AdminMetricsController {

    private final AdminMetricsService adminMetricsService;

    @GetMapping
    public ResponseEntity<AdminMetricsOverviewDTO> overview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(required = false, defaultValue = "day") String granularity
    ) {
        return ResponseEntity.ok(adminMetricsService.getOverview(start, end, MetricGranularity.fromParam(granularity)));
    }

    @GetMapping("/access-logs")
    public ResponseEntity<Page<LoginAccessMetricResponseDTO>> accessLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            Pageable pageable
    ) {
        return ResponseEntity.ok(adminMetricsService.getAccessLogs(start, end, pageable));
    }

    @GetMapping("/critical-failures")
    public ResponseEntity<Page<CriticalFailureMetricResponseDTO>> criticalFailures(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            Pageable pageable
    ) {
        return ResponseEntity.ok(adminMetricsService.getCriticalFailures(start, end, pageable));
    }
}
