package io.github.mateuszuran.sisyphus_app.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mateuszuran.sisyphus_app.AbstractIntegrationTest;
import io.github.mateuszuran.sisyphus_app.SisyphusAppApplication;
import io.github.mateuszuran.sisyphus_app.model.ApplicationStatus;
import io.github.mateuszuran.sisyphus_app.model.Applications;
import io.github.mateuszuran.sisyphus_app.model.Specification;
import io.github.mateuszuran.sisyphus_app.model.WorkGroup;
import io.github.mateuszuran.sisyphus_app.repository.ApplicationsRepository;
import io.github.mateuszuran.sisyphus_app.repository.GroupRepository;
import io.github.mateuszuran.sisyphus_app.repository.SpecificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = SisyphusAppApplication.class)
@AutoConfigureMockMvc
public class ApplicationsIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private SpecificationRepository specificationRepository;

    @Autowired
    private ApplicationsRepository applicationsRepository;


    @BeforeEach
    public void setUp() throws Exception {
        applicationsRepository.deleteAll();
        groupRepository.deleteAll();
        specificationRepository.deleteAll();
    }

    @Test
    void givenWorkGroupId_whenGetAllApplications_thenReturnListOfApplications() throws Exception {
        //given
        WorkGroup group = WorkGroup.builder().creationTime("today").build();
        WorkGroup savedWorkGroup = groupRepository.save(group);

        Applications app1 = Applications.builder().workUrl("url1").appliedDate("12-08-2024").build();
        Applications app2 = Applications.builder().workUrl("url2").appliedDate("13-08-2024").build();
        Applications app3 = Applications.builder().workUrl("url3").appliedDate("14-08-2024").build();
        List<Applications> appList = List.of(app1, app2, app3);
        var savedApplications = applicationsRepository.saveAll(appList);

        savedWorkGroup.setApplications(savedApplications);
        var updatedGroup = groupRepository.save(savedWorkGroup);

        //when + then
        mockMvc.perform(get("/applications/all/" + updatedGroup.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].workUrl").value("url3"))
                .andExpect(jsonPath("$.[1].workUrl").value("url2"));

    }

    @Test
    void givenWorkGroupId_whenGetAllApplicationsAndSortWithInvalidDate_thenReturnBadRequest() throws Exception {
        WorkGroup group = WorkGroup.builder().creationTime("today").build();
        WorkGroup savedWorkGroup = groupRepository.save(group);

        Applications app1 = Applications.builder().workUrl("url1").appliedDate("invalid-date").build();
        Applications app2 = Applications.builder().workUrl("url2").appliedDate("13-08-2024").build();
        List<Applications> appList = List.of(app1, app2);
        var savedApplications = applicationsRepository.saveAll(appList);

        savedWorkGroup.setApplications(savedApplications);
        var updatedGroup = groupRepository.save(savedWorkGroup);

        //when + then
        mockMvc.perform(get("/applications/all/" + updatedGroup.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Cannot sort applications list because of time parsing"));
    }

    @Test
    void givenListOfApplicationsAndWorkGroupId_whenSave_thenReturnStatusCreated() throws Exception {
        //given
        WorkGroup group = WorkGroup.builder().creationTime("tomorrow").build();
        var savedGroup = groupRepository.save(group);

        Applications work = Applications.builder().workUrl("work_url").build();

        //when
        mockMvc.perform(post("/applications/save/" + savedGroup.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(work))))
                .andExpect(status().isCreated());

        //then
        var groupResult = groupRepository.findById(savedGroup.getId()).orElseThrow();
        Assertions.assertNotNull(groupResult);
        Assertions.assertFalse(groupResult.getApplications().isEmpty());

        var savedWork = groupResult.getApplications()
                .stream()
                .filter(g -> g.getWorkUrl().equalsIgnoreCase(work.getWorkUrl()))
                .findFirst();

        Assertions.assertTrue(savedWork.isPresent());
        Assertions.assertEquals(savedWork.get().getStatus(), ApplicationStatus.SENT);
        Assertions.assertEquals(groupResult.getSent(), 1);
    }

    @Test
    void givenEmptyList_whenSave_thenThrowException() throws Exception {
        //given
        WorkGroup group = WorkGroup.builder().creationTime("tomorrow").build();
        var savedGroup = groupRepository.save(group);

        //when
        mockMvc.perform(post("/applications/save/" + savedGroup.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of())))
                .andExpect(status().isForbidden())
                .andDo(print())
                .andExpect(jsonPath("$.detail").value("Applications list cannot be empty"));
    }

    @Test
    void givenApplication_whenDelete_thenReturnStatus() throws Exception {
        //given
        Specification spec1 = Specification.builder().companyName("company1").build();
        Specification spec2 = Specification.builder().companyName("company2").build();
        specificationRepository.saveAll(List.of(spec1, spec2));
        Applications work1 = Applications.builder().workUrl("work_url1").status(ApplicationStatus.SENT).specification(spec1).build();
        Applications work2 = Applications.builder().workUrl("work_url2").status(ApplicationStatus.IN_PROGRESS).specification(spec2).build();
        applicationsRepository.saveAll(List.of(work1, work2));

        WorkGroup group = WorkGroup.builder()
                .cvData(null)
                .creationTime("today")
                .sent(15)
                .rejected(4)
                .inProgress(12)
                .applications(new ArrayList<>())
                .build();
        group.getApplications().addAll(List.of(work1, work2));
        groupRepository.save(group);

        //when
        mockMvc.perform(delete("/applications/delete/" + work1.getId()))
                .andExpect(status().isOk());

        //then
        Assertions.assertNotNull(applicationsRepository.findById(work1.getId()));

        var editedGroup = groupRepository.findById(group.getId()).orElseThrow();
        Assertions.assertEquals(14, editedGroup.getSent());
    }

    @Test
    void givenApplicationIdAndStatus_whenUpdateStatus_thenReturnApplicationAndUpdateWorkGroupCounters() throws Exception {
        //given
        String newStatus = "sent";
        Applications work1 = Applications.builder().workUrl("work_url1").status(ApplicationStatus.REJECTED).build();
        applicationsRepository.save(work1);

        WorkGroup group = WorkGroup.builder()
                .cvData(null)
                .creationTime("today")
                .sent(15)
                .rejected(4)
                .inProgress(12)
                .applications(new ArrayList<>())
                .build();
        group.getApplications().add(work1);
        var savedGroup = groupRepository.save(group);

        //when
        mockMvc.perform(patch("/applications/update/" + work1.getId() + "/" + newStatus))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(newStatus.toUpperCase()));

        //then
        var updatedGroup = groupRepository.findById(savedGroup.getId()).orElseThrow();
        Assertions.assertNotNull(updatedGroup);
        Assertions.assertEquals(3, updatedGroup.getRejected());
        Assertions.assertEquals(16, updatedGroup.getSent());
    }

    @Test
    void givenApplicationIdAndStatus_whenUpdateStatusToHired_thenReturnApplicationAndCheckWorkGroupHiredState() throws Exception {
        //given
        String newStatus = "hired";
        Applications work1 = Applications.builder().workUrl("work_url1").status(ApplicationStatus.IN_PROGRESS).build();
        applicationsRepository.save(work1);

        WorkGroup group = WorkGroup.builder()
                .cvData(null)
                .creationTime("today")
                .sent(15)
                .rejected(4)
                .inProgress(12)
                .isHired(false)
                .applications(new ArrayList<>())
                .build();
        group.getApplications().add(work1);
        var savedGroup = groupRepository.save(group);

        //when
        mockMvc.perform(patch("/applications/update/" + work1.getId() + "/" + newStatus))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(newStatus.toUpperCase()));

        //then
        var updatedGroup = groupRepository.findById(savedGroup.getId()).orElseThrow();
        Assertions.assertNotNull(updatedGroup);
        Assertions.assertEquals(updatedGroup.getRejected(), 4);
        Assertions.assertEquals(updatedGroup.getSent(), 15);
        Assertions.assertTrue(updatedGroup.isHired());
    }

    @Test
    void givenApplicationIdAndStatus_whenUpdateStatusToInProgressFromHired_thenReturnApplicationAndCheckWorkGroupHiredState() throws Exception {
        //given
        String newStatus = "in_progress";
        Applications work1 = Applications.builder().workUrl("work_url1").status(ApplicationStatus.HIRED).build();
        applicationsRepository.save(work1);

        WorkGroup group = WorkGroup.builder()
                .cvData(null)
                .creationTime("today")
                .rejected(4)
                .inProgress(12)
                .isHired(true)
                .applications(new ArrayList<>())
                .build();
        group.getApplications().add(work1);
        var savedGroup = groupRepository.save(group);

        //when
        mockMvc.perform(patch("/applications/update/" + work1.getId() + "/" + newStatus))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(newStatus.toUpperCase()));

        //then
        var updatedGroup = groupRepository.findById(savedGroup.getId()).orElseThrow();
        Assertions.assertNotNull(updatedGroup);
        Assertions.assertEquals(updatedGroup.getRejected(), 4);
        Assertions.assertFalse(updatedGroup.isHired());
    }
}