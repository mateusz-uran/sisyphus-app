package io.github.mateuszuran.sisyphus_app.unit.service;

import io.github.mateuszuran.sisyphus_app.model.ApplicationStatus;
import io.github.mateuszuran.sisyphus_app.model.Applications;
import io.github.mateuszuran.sisyphus_app.model.WorkGroup;
import io.github.mateuszuran.sisyphus_app.repository.GroupRepository;
import io.github.mateuszuran.sisyphus_app.service.WorkGroupServiceImpl;
import io.github.mateuszuran.sisyphus_app.util.TimeUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkGroupServiceTest {

    @Mock
    TimeUtil util;
    @Mock
    GroupRepository repository;
    @InjectMocks
    WorkGroupServiceImpl serviceImpl;


    @Test
    public void givenCvUrl_whenCreateNewWorkGroup_thenCreatePlainWorkGroup() {
        //given
        String filename = "test-cv.pdf";
        byte[] fileContent = "Mock file content".getBytes();
        MockMultipartFile mockFile = new MockMultipartFile("file", filename, "application/pdf", fileContent);

        var time = util.formatCreationTime();
        //when
        serviceImpl.createNewWorkGroup(mockFile);
        //then
        verify(repository).save(assertArg(arg -> {
            byte[] cvUrlBytes = arg.getCvData().getData();
            var creationTime = arg.getCreationTime();
            assertArrayEquals(cvUrlBytes, fileContent);
            assertEquals(creationTime, time);
            assertEquals(filename, arg.getCvFileName());
        }));
    }

    @Test
    public void givenCvUrl_whenCreateNewWorkGroup_thenThrowException() {
        String filename = "test-cv.pdf";
        byte[] fileContent = "Mock file content".getBytes();
        MockMultipartFile mockFile = new MockMultipartFile("file", filename, "application/pdf", fileContent);
        //when
        serviceImpl.createNewWorkGroup(mockFile);
        //then
        doThrow(new RuntimeException("Failed to read file")).when(repository).save(any(WorkGroup.class));

        assertThrows(RuntimeException.class, () -> serviceImpl.createNewWorkGroup(mockFile));
    }

    @Test
    public void givenWorkGroupId_whenGet_thenReturnWorkGroupObject() {
        //given
        String workGroupId = "12345";
        WorkGroup group = WorkGroup.builder()
                .id(workGroupId).build();
        when(repository.findById(workGroupId)).thenReturn(Optional.of(group));
        //when
        var result = serviceImpl.getWorkGroup(workGroupId);
        //then
        assertNotNull(result);
        assertEquals(workGroupId, result.getId());
    }

    @Test
    public void givenApplicationsAndWorkGroupId_whenUpdate_thenAddWorkGroupToApplications() {
        //given
        String workGroupId = "123";
        WorkGroup group = WorkGroup.builder().id(workGroupId).applications(new ArrayList<>()).build();
        when(repository.findById(workGroupId)).thenReturn(Optional.of(group));

        Applications application1 = Applications.builder().workUrl("work1").build();
        Applications application2 = Applications.builder().workUrl("work2").build();
        Applications application3 = Applications.builder().workUrl("work3").build();
        var applicationsList = List.of(application1, application2, application3);

        int applicationSize = applicationsList.size();
        WorkGroup expectedGroup = WorkGroup.builder()
                .id(workGroupId)
                .sent(applicationSize)
                .inProgress(0)
                .applications(applicationsList).build();
        when(repository.save(group)).thenReturn(expectedGroup);

        //when
        var updatedGroup = serviceImpl.updateWorkGroupWithApplications(applicationsList, workGroupId);

        //then
        assertNotNull(updatedGroup.getApplications());
        assertThat(updatedGroup.getApplications())
                .hasSize(3)
                .extracting(Applications::getWorkUrl)
                .containsExactlyInAnyOrder("work1", "work2", "work3");
        assertThat(updatedGroup.getSent()).isEqualTo(applicationsList.size());
        assertThat(updatedGroup.getSent()).isEqualTo(3);
        assertThat(updatedGroup.getInProgress()).isEqualTo(0);

    }

    @Test
    public void givenApplicationEmptyList_whenUpdate_thenThrow() {
        //given
        String workGroupId = "123";

        //when + then
        assertThrows(IllegalStateException.class, () -> serviceImpl.updateWorkGroupWithApplications(null, workGroupId));
        verify(repository, never()).findById(workGroupId);
        verify(repository, never()).save(any());

    }

    @Test
    public void givenNothing_whenGet_thenReturnListOfWorKGroups() {
        //given
        WorkGroup group1 = WorkGroup.builder().creationTime("date1").build();
        WorkGroup group2 = WorkGroup.builder().creationTime("date2").build();
        WorkGroup group3 = WorkGroup.builder().creationTime("date3").build();
        var groupList = List.of(group1, group2, group3);
        when(repository.findAll()).thenReturn(groupList);
        //when
        var returnedList = serviceImpl.getAllGroups();
        //then
        assertThat(returnedList)
                .hasSize(groupList.size())
                .extracting(WorkGroup::getCreationTime)
                .containsExactly("date1", "date2", "date3");

    }

    @Test
    public void givenWorkGroupId_whenExists_thenDeleteWorkGroup() {
        //given
        String workGroupId = "123";
        WorkGroup groupToDelete = WorkGroup.builder().build();
        when(repository.findById(workGroupId)).thenReturn(Optional.of(groupToDelete));

        //when
        serviceImpl.deleteSingleGroup(workGroupId);

        //then
        verify(repository).delete(any(WorkGroup.class));
    }

    @Test
    public void givenWorkGroupId_whenGetAllApplications_thenReturnListOf() {
        //given
        String workGroupId = "123";
        WorkGroup group = WorkGroup.builder().id(workGroupId).applications(new ArrayList<>()).build();

        Applications application1 = Applications.builder().workUrl("work1").appliedDate("12-08-2024").build();
        Applications application2 = Applications.builder().workUrl("work2").appliedDate("13-08-2024").build();
        Applications application3 = Applications.builder().workUrl("work3").appliedDate("14-08-2024").build();
        var applicationsList = List.of(application1, application2, application3);
        group.getApplications().addAll(applicationsList);
        when(repository.findById(workGroupId)).thenReturn(Optional.of(group));

        //when
        var workApplications = serviceImpl.getAllApplicationsFromWorkGroup(workGroupId);

        //then
        assertThat(workApplications)
                .hasSize(applicationsList.size())
                .extracting(Applications::getWorkUrl)
                .containsExactly("work1", "work2", "work3");
    }

    @Test
    void givenApplicationAndStatus_whenChangeDeniedToOther_thenDecrementCounter() {
        //given
        Applications application = Applications.builder().id("1234").status(ApplicationStatus.REJECTED).build();
        WorkGroup group = WorkGroup.builder().sent(5).inProgress(3).rejected(2).applications(List.of(application)).build();

        when(repository.findAll()).thenReturn(List.of(group));

        ArgumentCaptor<WorkGroup> groupCaptor = ArgumentCaptor.forClass(WorkGroup.class);

        // when
        serviceImpl.updateGroupWhenApplicationUpdate(application, ApplicationStatus.IN_PROGRESS.name(), application.getStatus().name());

        // then
        verify(repository).findAll();
        verify(repository).save(groupCaptor.capture());

        WorkGroup capturedGroup = groupCaptor.getValue();
        assertNotNull(capturedGroup);

        assertEquals(1, capturedGroup.getRejected());
    }

    @Test
    void givenApplicationAndStatus_whenChangeOtherToDenied_thenIncrementCounter() {
        //given
        Applications application = Applications.builder().id("1234").status(ApplicationStatus.SENT).build();
        WorkGroup group = WorkGroup.builder().sent(5).inProgress(3).rejected(2).applications(List.of(application)).build();

        when(repository.findAll()).thenReturn(List.of(group));

        ArgumentCaptor<WorkGroup> groupCaptor = ArgumentCaptor.forClass(WorkGroup.class);

        // when
        serviceImpl.updateGroupWhenApplicationUpdate(application, ApplicationStatus.REJECTED.name(), application.getStatus().name());

        // then
        verify(repository).findAll();
        verify(repository).save(groupCaptor.capture());

        WorkGroup capturedGroup = groupCaptor.getValue();
        assertNotNull(capturedGroup);

        assertEquals(3, capturedGroup.getRejected());
    }

    @Test
    void givenApplicationAndStatus_whenWorkGroupNotFound_thenThrowException() {
        //given
        Applications application = Applications.builder().status(ApplicationStatus.SENT).build();

        //when + then
        assertThrows(IllegalArgumentException.class, () -> serviceImpl.updateGroupWhenApplicationUpdate(application, ApplicationStatus.REJECTED.name(), application.getStatus().name()));
        verify(repository, never()).save(any());
    }

    @Test
    void givenApplication_whenWorkStatusIsNotDeniedAndDelete_thenUpdateWorkGroupCounters() {
        //given
        Applications application = Applications.builder().id("1234").status(ApplicationStatus.SENT).build();
        WorkGroup group = WorkGroup.builder().sent(5).inProgress(3).rejected(2).applications(new ArrayList<>(List.of(application))).build();

        when(repository.findAll()).thenReturn(List.of(group));

        ArgumentCaptor<WorkGroup> groupCaptor = ArgumentCaptor.forClass(WorkGroup.class);
        //when
        serviceImpl.updateGroupWhenApplicationDelete(application);

        //then
        verify(repository).findAll();
        verify(repository).save(groupCaptor.capture());

        WorkGroup capturedGroup = groupCaptor.getValue();
        assertNotNull(capturedGroup);
        assertEquals(4, capturedGroup.getSent());
        assertEquals(3, capturedGroup.getInProgress());
    }

    @Test
    void givenApplication_whenWorkStatusDeniedAndDelete_thenUpdateWorkGroupOneCounter() {
        //given
        Applications application = Applications.builder().id("1234").status(ApplicationStatus.REJECTED).build();
        WorkGroup group = WorkGroup.builder().sent(5).inProgress(3).rejected(2).applications(new ArrayList<>(List.of(application))).build();

        when(repository.findAll()).thenReturn(List.of(group));

        ArgumentCaptor<WorkGroup> groupCaptor = ArgumentCaptor.forClass(WorkGroup.class);
        //when
        serviceImpl.updateGroupWhenApplicationDelete(application);

        //then
        verify(repository).findAll();
        verify(repository).save(groupCaptor.capture());

        WorkGroup capturedGroup = groupCaptor.getValue();
        assertNotNull(capturedGroup);
        assertEquals(1, capturedGroup.getRejected());
    }

    @Test
    void givenApplication_whenStatusIsHired_thenToggleWorkGroup() {
        //given
        Applications application = Applications.builder().id("1234").status(ApplicationStatus.HIRED).build();
        WorkGroup group = WorkGroup.builder().sent(5).inProgress(3).rejected(2).applications(List.of(application)).build();

        when(repository.findAll()).thenReturn(List.of(group));

        ArgumentCaptor<WorkGroup> groupCaptor = ArgumentCaptor.forClass(WorkGroup.class);
        //when
        serviceImpl.updateGroupWhenApplicationUpdate(application, "hired", "in_progress");

        //then
        verify(repository).findAll();
        verify(repository).save(groupCaptor.capture());

        WorkGroup capturedGroup = groupCaptor.getValue();
        assertNotNull(capturedGroup);
        assertTrue(capturedGroup.isHired());
    }
}