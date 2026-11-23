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
}