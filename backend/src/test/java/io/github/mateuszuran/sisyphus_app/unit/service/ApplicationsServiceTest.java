package io.github.mateuszuran.sisyphus_app.unit.service;

import io.github.mateuszuran.sisyphus_app.dto.ApplicationDTO;
import io.github.mateuszuran.sisyphus_app.model.ApplicationStatus;
import io.github.mateuszuran.sisyphus_app.model.Applications;
import io.github.mateuszuran.sisyphus_app.model.Specification;
import io.github.mateuszuran.sisyphus_app.repository.ApplicationsRepository;
import io.github.mateuszuran.sisyphus_app.repository.SpecificationRepository;
import io.github.mateuszuran.sisyphus_app.service.ApplicationsServiceImpl;
import io.github.mateuszuran.sisyphus_app.service.SpecificationsServiceImpl;
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
public class ApplicationsServiceTest {

    @Mock
    TimeUtil util;
    @Mock
    WorkGroupServiceImpl groupService;
    @Mock
    ApplicationsRepository repository;
    @Mock
    SpecificationRepository specRepo;

    @InjectMocks
    ApplicationsServiceImpl serviceImpl;

    @Test
    public void givenApplicationAndWorkGroupId_whenAdd_thenSaveApplication() {
        //given
        String workGroupId = "123";
        Applications application1 = Applications.builder().workUrl("work1").build();
        ApplicationDTO applicationDTO = ApplicationDTO.builder().workUrl("work1").build();

        //when
        serviceImpl.createApplications(List.of(applicationDTO), workGroupId);

        //then
        verify(repository).saveAll(assertArg(arg -> {
            var savedWorkApplication = arg.iterator().next();
            assertEquals(savedWorkApplication.getWorkUrl(), "work1");
        }));
    }

    @Test
    public void givenApplicationId_whenDelete_thenDoNothing() {
        //given
        String workApplicationId = "1234";
        var workApplication = Applications.builder().specification(Specification.builder().build()).build();
        when(repository.findById(workApplicationId)).thenReturn(Optional.of(workApplication));

        //when
        serviceImpl.deleteApplication(workApplicationId);

        //then
        verify(repository).delete(workApplication);
    }

    @Test
    public void givenApplicationIdAndStatus_whenUpdate_thenReturnUpdatedApplication() {
        //given
        String workApplicationId = "1234";
        var newStatus = "REJECTED";
        Applications work = Applications.builder().workUrl("work1").status(ApplicationStatus.IN_PROGRESS).build();
        when(repository.findById(workApplicationId)).thenReturn(Optional.of(work));

        Applications updatedWork = Applications.builder().workUrl("work1").status(ApplicationStatus.valueOf(newStatus)).build();
        when(repository.save(any(Applications.class))).thenReturn(updatedWork);

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
    public void givenApplicationIdAndUrl_whenUpdate_thenReturnUpdatedApplication() {
        //given
        String workId = "1234";
        String newWorkUrl = "new_url";
        Applications oldWork = Applications.builder().workUrl("old_url").build();
        when(repository.findById(workId)).thenReturn(Optional.of(oldWork));
        when(repository.save(any(Applications.class))).thenReturn(Applications.builder().workUrl(newWorkUrl).build());

        //when
        var updatedWork = serviceImpl.updateApplicationUrl(workId, newWorkUrl);

        //then
        assertThat(updatedWork.getWorkUrl()).isEqualTo(newWorkUrl);
    }

    @Test
    public void givenApplicationId_whenGet_thenReturnObject() {
        //given
        String workId = "1234";
        Applications work = Applications.builder().workUrl("url").build();
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
    public void givenSpecAndAppId_whenCheck_thenReturnTrue() {
        //given
        String workId = "1234";
        Specification spec = Specification.builder()
                .companyName("company")
                .requirements(List.of("do1", "do2"))
                .technologies(List.of("tech1", "tech2"))
                .build();
        Applications work = Applications.builder().workUrl("url").specification(spec).build();
        when(repository.findById(workId)).thenReturn(Optional.of(work));

        //when
        var result = serviceImpl.checkSpecInsideApplicationReactive(workId, spec);

        //then
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    public void givenSpecAndId_whenCheck_thenReturnFalse() {
        //given
        String workId = "1234";
        Specification spec1 = Specification.builder()
                .companyName("company1")
                .requirements(List.of("do1", "do2"))
                .technologies(List.of("tech1", "tech2"))
                .build();
        Specification spec2 = Specification.builder()
                .companyName("company2")
                .requirements(List.of("do1", "do2"))
                .technologies(List.of("tech1", "tech2"))
                .build();
        Applications work = Applications.builder().workUrl("url").specification(spec2).build();
        when(repository.findById(workId)).thenReturn(Optional.of(work));

        //when
        var result = serviceImpl.checkSpecInsideApplicationReactive(workId, spec1);

        //then
        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    public void givenAppIdAndSpecObject_whenSpecNotExists_thenReturnCreatedSpecObject() {
        //given
        String workId = "1234";
        Specification spec1 = Specification.builder()
                .companyName("company1")
                .requirements(List.of("do1", "do2"))
                .technologies(List.of("tech1", "tech2"))
                .build();
        Specification spec2 = Specification.builder()
                .companyName("company2")
                .requirements(List.of("do1", "do2"))
                .technologies(List.of("tech1", "tech2"))
                .build();
        Applications work = Applications.builder().workUrl("url").specification(spec2).build();
        when(repository.findById(workId)).thenReturn(Optional.of(work));
        when(repository.save(work)).thenAnswer(invocation -> {
            Applications app = invocation.getArgument(0);
            app.setSpecification(spec2);
            return app;
        });

        //when
        var result = serviceImpl.updateApplicationSpecificationsReactive(workId, spec1);

        //then
        StepVerifier.create(result)
                .expectNextMatches(app -> app.getSpecification().getCompanyName().equalsIgnoreCase("company2"))
                .verifyComplete();
    }

    @Test
    public void givenAppIdAndSpecObject_whenSpecEmpty_thenThrowException() {
        //when
        Mono<Applications> result = serviceImpl.updateApplicationSpecificationsReactive("1234", null);

        //then
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof IllegalStateException &&
                        throwable.getMessage().equals("Specification is empty"))
                .verify();
    }
}
