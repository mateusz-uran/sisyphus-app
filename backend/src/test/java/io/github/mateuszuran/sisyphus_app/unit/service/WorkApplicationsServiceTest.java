package io.github.mateuszuran.sisyphus_app.unit.service;

import io.github.mateuszuran.sisyphus_app.dto.WorkApplicationDTO;
import io.github.mateuszuran.sisyphus_app.model.ApplicationStatus;
import io.github.mateuszuran.sisyphus_app.model.WorkApplications;
import io.github.mateuszuran.sisyphus_app.model.WorkSpecification;
import io.github.mateuszuran.sisyphus_app.repository.WorkApplicationsRepository;
import io.github.mateuszuran.sisyphus_app.service.WorkApplicationsServiceImpl;
import io.github.mateuszuran.sisyphus_app.service.WorkGroupServiceImpl;
import io.github.mateuszuran.sisyphus_app.util.TimeUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkApplicationsServiceTest {

    @Mock
    TimeUtil util;
    @Mock
    WorkGroupServiceImpl groupService;
    @Mock
    WorkApplicationsRepository repository;

    @InjectMocks
    WorkApplicationsServiceImpl serviceImpl;

    @Test
    public void givenWorkApplicationAndWorkGroupId_whenAdd_thenSaveWorkApplication() {
        //given
        String workGroupId = "123";
        WorkApplications application1 = WorkApplications.builder().workUrl("work1").build();
        WorkApplicationDTO applicationDTO = WorkApplicationDTO.builder().workUrl("work1").build();

        //when
        serviceImpl.createWorkApplication(List.of(applicationDTO), workGroupId);

        //then
        verify(repository).saveAll(assertArg(arg -> {
            var savedWorkApplication = arg.iterator().next();
            assertEquals(savedWorkApplication.getWorkUrl(), "work1");
        }));
    }

    @Test
    public void givenWorkApplicationId_whenDelete_thenDoNothing() {
        //given
        String workApplicationId = "1234";
        var workApplication = WorkApplications.builder().build();
        when(repository.findById(workApplicationId)).thenReturn(Optional.of(workApplication));

        //when
        serviceImpl.deleteWorkApplication(workApplicationId);

        //then
        verify(repository).delete(workApplication);
    }

    @Test
    public void givenApplicationIdAndStatus_whenUpdate_thenReturnUpdatedApplication() {
        //given
        String workApplicationId = "1234";
        var newStatus = "REJECTED";
        WorkApplications work = WorkApplications.builder().workUrl("work1").status(ApplicationStatus.IN_PROGRESS).build();
        when(repository.findById(workApplicationId)).thenReturn(Optional.of(work));

        WorkApplications updatedWork = WorkApplications.builder().workUrl("work1").status(ApplicationStatus.valueOf(newStatus)).build();
        when(repository.save(any(WorkApplications.class))).thenReturn(updatedWork);

        //when
        var result = serviceImpl.updateApplicationStatus(workApplicationId, newStatus);

        //then
        assertThat(result).isEqualTo(updatedWork);
    }

    @Test
    public void givenApplicationIdAndStatus_whenNotFound_thenThrow() {
        //given
        String workApplicationId = "1234";
        //when
        assertThrows(IllegalArgumentException.class, () -> serviceImpl.updateApplicationStatus("SEND", null));
        verify(repository, never()).findById(workApplicationId);
        verify(repository, never()).save(any());
    }

    @Test
    public void givenWorkApplicationIdAndUrl_whenUpdate_thenReturnUpdatedWorkApplication() {
        //given
        String workId = "1234";
        String newWorkUrl = "new_url";
        WorkApplications oldWork = WorkApplications.builder().workUrl("old_url").build();
        when(repository.findById(workId)).thenReturn(Optional.of(oldWork));
        when(repository.save(any(WorkApplications.class))).thenReturn(WorkApplications.builder().workUrl(newWorkUrl).build());

        //when
        var updatedWork = serviceImpl.updateWorkApplicationUrl(workId, newWorkUrl);

        //then
        assertThat(updatedWork.getWorkUrl()).isEqualTo(newWorkUrl);
    }

    @Test
    public void givenApplicationId_whenGet_thenReturnObject() {
        //given
        String workId = "1234";
        WorkApplications work = WorkApplications.builder().workUrl("url").build();
        when(repository.findById(workId)).thenReturn(Optional.of(work));

        //when
        var result = serviceImpl.getSingleApplicationReactive(workId);

        //then
        StepVerifier.create(result)
                .expectNextMatches(app -> app.getWorkUrl().equalsIgnoreCase("url"))
                .verifyComplete();
    }

    @Test
    public void givenApplicationId_whenGet_thenThrow() {
        //given
        String workId = "1234";
        when(repository.findById(workId)).thenReturn(Optional.empty());

        //when
        var result = serviceImpl.getSingleApplicationReactive(workId);

        //then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().equals("Work application with given id does not exist."))
                .verify();
    }

    @Test
    public void givenWorkSpecAndAppId_whenCheck_thenReturnTrue() {
        //given
        String workId = "1234";
        WorkSpecification spec = WorkSpecification.builder()
                .companyName("company")
                .requirements(List.of("do1", "do2"))
                .technologies(List.of("tech1", "tech2"))
                .build();
        WorkApplications work = WorkApplications.builder().workUrl("url").specification(spec).build();
        when(repository.findById(workId)).thenReturn(Optional.of(work));

        //when
        var result = serviceImpl.checkWorkSpecInsideApplicationReactive(workId, spec);

        //then
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    public void givenWorkSpecAndId_whenCheck_thenReturnFalse() {
        //given
        String workId = "1234";
        WorkSpecification spec1 = WorkSpecification.builder()
                .companyName("company1")
                .requirements(List.of("do1", "do2"))
                .technologies(List.of("tech1", "tech2"))
                .build();
        WorkSpecification spec2 = WorkSpecification.builder()
                .companyName("company2")
                .requirements(List.of("do1", "do2"))
                .technologies(List.of("tech1", "tech2"))
                .build();
        WorkApplications work = WorkApplications.builder().workUrl("url").specification(spec2).build();
        when(repository.findById(workId)).thenReturn(Optional.of(work));

        //when
        var result = serviceImpl.checkWorkSpecInsideApplicationReactive(workId, spec1);

        //then
        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    public void givenAppIdAndSpecObject_whenSpecNotExists_thenReturnCreatedSpecObject() {
        //given
        String workId = "1234";
        WorkSpecification spec1 = WorkSpecification.builder()
                .companyName("company1")
                .requirements(List.of("do1", "do2"))
                .technologies(List.of("tech1", "tech2"))
                .build();
        WorkSpecification spec2 = WorkSpecification.builder()
                .companyName("company2")
                .requirements(List.of("do1", "do2"))
                .technologies(List.of("tech1", "tech2"))
                .build();
        WorkApplications work = WorkApplications.builder().workUrl("url").specification(spec2).build();
        when(repository.findById(workId)).thenReturn(Optional.of(work));
        when(repository.save(work)).thenAnswer(invocation -> {
            WorkApplications app = invocation.getArgument(0);
            app.setSpecification(spec2);
            return app;
        });

        //when
        var result = serviceImpl.updateWorkApplicationSpecificationsReactive(workId, spec1);

        //then
        StepVerifier.create(result)
                .expectNextMatches(app -> app.getSpecification().getCompanyName().equalsIgnoreCase("company2"))
                .verifyComplete();
    }

    @Test
    public void givenAppIdAndSpecObject_whenSpecEmpty_thenThrowException() {
        //when
        Mono<WorkApplications> result = serviceImpl.updateWorkApplicationSpecificationsReactive("1234", null);

        //then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof IllegalStateException &&
                        throwable.getMessage().equals("Specification is empty"))
                .verify();
    }
}
