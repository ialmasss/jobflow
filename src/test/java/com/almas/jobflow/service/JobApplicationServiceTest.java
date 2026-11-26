package com.almas.jobflow.service;

import com.almas.jobflow.dto.JobApplicationRequest;
import com.almas.jobflow.dto.JobApplicationResponse;
import com.almas.jobflow.dto.StatsResponse;
import com.almas.jobflow.exception.ResourceNotFoundException;
import com.almas.jobflow.model.ApplicationStatus;
import com.almas.jobflow.model.JobApplication;
import com.almas.jobflow.model.User;
import com.almas.jobflow.repository.JobApplicationRepository;
import com.almas.jobflow.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobApplicationServiceTest {

    @Mock
    private JobApplicationRepository repository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JobApplicationService service;

    @Test
    void getByIdReturnsApplicationOfItsOwner() {
        when(repository.findByIdAndOwnerEmail(1L, "alice@test.com"))
                .thenReturn(Optional.of(sampleApplication(1L)));

        JobApplicationResponse response = service.getById("alice@test.com", 1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.companyName()).isEqualTo("Kaspi");
    }

    @Test
    void getByIdThrowsNotFoundForAnotherUsersApplication() {
        when(repository.findByIdAndOwnerEmail(1L, "bob@test.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById("bob@test.com", 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createSetsOwnerAndDefaultStatus() {
        User alice = new User();
        alice.setEmail("alice@test.com");
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(alice));
        when(repository.save(any(JobApplication.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        JobApplicationRequest request = new JobApplicationRequest(
                "Kaspi", "Java Intern", null, null, LocalDate.of(2026, 11, 20), null, null);

        JobApplicationResponse response = service.create("alice@test.com", request);

        assertThat(response.status()).isEqualTo(ApplicationStatus.APPLIED);

        ArgumentCaptor<JobApplication> captor = ArgumentCaptor.forClass(JobApplication.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getOwner()).isSameAs(alice);
    }

    @Test
    void updateKeepsCurrentStatusWhenRequestHasNoStatus() {
        JobApplication existing = sampleApplication(1L);
        existing.setStatus(ApplicationStatus.INTERVIEW);
        when(repository.findByIdAndOwnerEmail(1L, "alice@test.com")).thenReturn(Optional.of(existing));
        when(repository.save(any(JobApplication.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        JobApplicationRequest request = new JobApplicationRequest(
                "Kaspi", "Java Junior", null, null, LocalDate.of(2026, 11, 20), null, null);

        JobApplicationResponse response = service.update("alice@test.com", 1L, request);

        assertThat(response.status()).isEqualTo(ApplicationStatus.INTERVIEW);
        assertThat(response.jobTitle()).isEqualTo("Java Junior");
    }

    @Test
    void deleteDoesNotTouchAnotherUsersApplication() {
        when(repository.findByIdAndOwnerEmail(1L, "bob@test.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete("bob@test.com", 1L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(repository, never()).delete(any(JobApplication.class));
    }

    @Test
    void getStatsCountsByStatusAndCalculatesRates() {
        when(repository.countByStatusForOwner("alice@test.com")).thenReturn(List.of(
                statusCount(ApplicationStatus.APPLIED, 6L),
                statusCount(ApplicationStatus.INTERVIEW, 2L),
                statusCount(ApplicationStatus.REJECTED, 1L),
                statusCount(ApplicationStatus.OFFER, 1L)));

        StatsResponse stats = service.getStats("alice@test.com");

        assertThat(stats.total()).isEqualTo(10L);
        assertThat(stats.interviewRate()).isEqualTo(0.3);
        assertThat(stats.offerRate()).isEqualTo(0.1);
    }

    @Test
    void getStatsForUserWithoutApplicationsReturnsZeros() {
        when(repository.countByStatusForOwner("new@test.com")).thenReturn(List.of());

        StatsResponse stats = service.getStats("new@test.com");

        assertThat(stats.total()).isZero();
        assertThat(stats.byStatus().values()).containsOnly(0L);
        assertThat(stats.interviewRate()).isZero();
    }

    private JobApplication sampleApplication(Long id) {
        JobApplication application = new JobApplication();
        application.setId(id);
        application.setCompanyName("Kaspi");
        application.setJobTitle("Java Intern");
        application.setStatus(ApplicationStatus.APPLIED);
        application.setAppliedDate(LocalDate.of(2026, 11, 20));
        return application;
    }

    private JobApplicationRepository.StatusCount statusCount(ApplicationStatus status, Long total) {
        return new JobApplicationRepository.StatusCount() {
            @Override
            public ApplicationStatus getStatus() {
                return status;
            }

            @Override
            public Long getTotal() {
                return total;
            }
        };
    }
}