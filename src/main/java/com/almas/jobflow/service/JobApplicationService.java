package com.almas.jobflow.service;

import com.almas.jobflow.dto.JobApplicationRequest;
import com.almas.jobflow.dto.JobApplicationResponse;
import com.almas.jobflow.exception.ResourceNotFoundException;
import com.almas.jobflow.model.ApplicationStatus;
import com.almas.jobflow.model.JobApplication;
import com.almas.jobflow.model.User;
import com.almas.jobflow.repository.JobApplicationRepository;
import com.almas.jobflow.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.almas.jobflow.dto.StatsResponse;
import java.util.EnumMap;
import java.util.Map;

@Service
public class JobApplicationService {

    private final JobApplicationRepository repository;
    private final UserRepository userRepository;

    public JobApplicationService(JobApplicationRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    public JobApplicationResponse create(String ownerEmail, JobApplicationRequest request) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        JobApplication application = new JobApplication();
        application.setOwner(owner);
        application.setStatus(ApplicationStatus.APPLIED);
        applyRequest(application, request);
        return JobApplicationResponse.from(repository.save(application));
    }

    public Page<JobApplicationResponse> getAll(String ownerEmail, ApplicationStatus status, Pageable pageable) {
        Page<JobApplication> page = (status == null)
                ? repository.findByOwnerEmail(ownerEmail, pageable)
                : repository.findByOwnerEmailAndStatus(ownerEmail, status, pageable);
        return page.map(JobApplicationResponse::from);
    }

    public JobApplicationResponse getById(String ownerEmail, Long id) {
        return JobApplicationResponse.from(findOrThrow(ownerEmail, id));
    }

    public JobApplicationResponse update(String ownerEmail, Long id, JobApplicationRequest request) {
        JobApplication application = findOrThrow(ownerEmail, id);
        applyRequest(application, request);
        return JobApplicationResponse.from(repository.save(application));
    }

    public void delete(String ownerEmail, Long id) {
        repository.delete(findOrThrow(ownerEmail, id));
    }

    private JobApplication findOrThrow(String ownerEmail, Long id) {
        return repository.findByIdAndOwnerEmail(id, ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + id));
    }

    private void applyRequest(JobApplication application, JobApplicationRequest request) {
        application.setCompanyName(request.companyName());
        application.setJobTitle(request.jobTitle());
        application.setJobUrl(request.jobUrl());
        if (request.status() != null) {
            application.setStatus(request.status());
        }
        application.setAppliedDate(request.appliedDate());
        application.setInterviewDate(request.interviewDate());
        application.setNotes(request.notes());
    }

    public StatsResponse getStats(String ownerEmail) {
        Map<ApplicationStatus, Long> byStatus = new EnumMap<>(ApplicationStatus.class);
        for (ApplicationStatus status : ApplicationStatus.values()) {
            byStatus.put(status, 0L);
        }
        for (JobApplicationRepository.StatusCount row : repository.countByStatusForOwner(ownerEmail)) {
            byStatus.put(row.getStatus(), row.getTotal());
        }

        long total = byStatus.values().stream().mapToLong(Long::longValue).sum();
        long offers = byStatus.get(ApplicationStatus.OFFER);
        long interviews = byStatus.get(ApplicationStatus.INTERVIEW) + offers;

        return new StatsResponse(total, byStatus, rate(interviews, total), rate(offers, total));
    }

    private double rate(long part, long total) {
        if (total == 0) {
            return 0.0;
        }
        return Math.round(part * 1000.0 / total) / 1000.0;
    }
}