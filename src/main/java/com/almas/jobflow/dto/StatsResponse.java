package com.almas.jobflow.dto;

import com.almas.jobflow.model.ApplicationStatus;

import java.util.Map;

public record StatsResponse(
        long total,
        Map<ApplicationStatus, Long> byStatus,
        double interviewRate,
        double offerRate
) {
}