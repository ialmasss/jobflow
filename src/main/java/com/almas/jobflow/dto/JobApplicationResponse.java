package com.almas.jobflow.dto;

import com.almas.jobflow.model.ApplicationStatus;
import com.almas.jobflow.model.JobApplication;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record JobApplicationResponse(
        Long id,
        String companyName,
        String jobTitle,
        String jobUrl,
        ApplicationStatus status,
        LocalDate appliedDate,
        LocalDateTime interviewDate,
        String notes
) {

    public static JobApplicationResponse from(JobApplication application) {
        return new JobApplicationResponse(
                application.getId(),
                application.getCompanyName(),
                application.getJobTitle(),
                application.getJobUrl(),
                application.getStatus(),
                application.getAppliedDate(),
                application.getInterviewDate(),
                application.getNotes()
        );
    }
}