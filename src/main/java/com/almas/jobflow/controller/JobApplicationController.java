package com.almas.jobflow.controller;

import com.almas.jobflow.dto.JobApplicationRequest;
import com.almas.jobflow.dto.JobApplicationResponse;
import com.almas.jobflow.model.ApplicationStatus;
import com.almas.jobflow.service.JobApplicationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.almas.jobflow.dto.StatsResponse;

@RestController
@RequestMapping("/api/applications")
public class JobApplicationController {

    private final JobApplicationService service;

    public JobApplicationController(JobApplicationService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobApplicationResponse create(Authentication authentication,
                                         @Valid @RequestBody JobApplicationRequest request) {
        return service.create(authentication.getName(), request);
    }

    @GetMapping
    public PagedModel<JobApplicationResponse> getAll(
            Authentication authentication,
            @RequestParam(required = false) ApplicationStatus status,
            @PageableDefault(size = 10, sort = "appliedDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return new PagedModel<>(service.getAll(authentication.getName(), status, pageable));
    }

    @GetMapping("/stats")
    public StatsResponse getStats(Authentication authentication) {
        return service.getStats(authentication.getName());
    }

    @GetMapping("/{id}")
    public JobApplicationResponse getById(Authentication authentication, @PathVariable Long id) {
        return service.getById(authentication.getName(), id);
    }

    @PutMapping("/{id}")
    public JobApplicationResponse update(Authentication authentication,
                                         @PathVariable Long id,
                                         @Valid @RequestBody JobApplicationRequest request) {
        return service.update(authentication.getName(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication authentication, @PathVariable Long id) {
        service.delete(authentication.getName(), id);
    }
}