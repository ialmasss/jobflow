package com.almas.jobflow.dto;

import com.almas.jobflow.model.ApplicationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record JobApplicationRequest(
        @NotBlank String companyName,
        @NotBlank String jobTitle,
        String jobUrl,
        ApplicationStatus status,
        @NotNull LocalDate appliedDate,
        LocalDateTime interviewDate,
        @Size(max = 2000) String notes
) {
}