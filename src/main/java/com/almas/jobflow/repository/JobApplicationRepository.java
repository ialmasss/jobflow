package com.almas.jobflow.repository;

import com.almas.jobflow.model.ApplicationStatus;
import com.almas.jobflow.model.JobApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    @Query("select a.status as status, count(a) as total from JobApplication a "
            + "where a.owner.email = :email group by a.status")
    List<StatusCount> countByStatusForOwner(@Param("email") String email);

    interface StatusCount {
        ApplicationStatus getStatus();

        Long getTotal();
    }

    Page<JobApplication> findByOwnerEmail(String email, Pageable pageable);

    Page<JobApplication> findByOwnerEmailAndStatus(String email, ApplicationStatus status, Pageable pageable);

    Optional<JobApplication> findByIdAndOwnerEmail(Long id, String email);
}