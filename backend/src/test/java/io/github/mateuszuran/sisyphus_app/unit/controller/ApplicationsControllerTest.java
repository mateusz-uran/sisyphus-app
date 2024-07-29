package io.github.mateuszuran.sisyphus_app.unit.controller;

import io.github.mateuszuran.sisyphus_app.controller.ApplicationsController;
import io.github.mateuszuran.sisyphus_app.model.ApplicationStatus;
import io.github.mateuszuran.sisyphus_app.model.Applications;
import io.github.mateuszuran.sisyphus_app.model.Specification;
import io.github.mateuszuran.sisyphus_app.repository.ApplicationsRepository;
import io.github.mateuszuran.sisyphus_app.service.ApplicationsServiceImpl;
import io.github.mateuszuran.sisyphus_app.service.SpecificationsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicationsController.class)
@AutoConfigureMockMvc
class ApplicationsControllerTest {

    @Autowired
    WebTestClient webTestClient;
    @Autowired
    MockMvc mockMvc;
    @MockBean
    ApplicationsServiceImpl serviceImpl;
    @MockBean
    SpecificationsServiceImpl specServiceImpl;
    @MockBean
    ApplicationsRepository repository;

    List<Applications> works = new ArrayList<>();

    @BeforeEach
    void setUp() {
        works = List.of(
                Applications.builder().workUrl("url1").build(),
                Applications.builder().workUrl("url2").build(),
                Applications.builder().workUrl("url3").build());
    }

    @Test
    void givenWorkGroupId_whenGetAllApplications_thenReturnListOfApplications() throws Exception {
        //given
        String workGroupId = "1234";
<<<<<<< HEAD:backend/src/test/java/io/github/mateuszuran/sisyphus_app/unit/controller/WorkApplicationsControllerTest.java
        WorkApplications applications1 = WorkApplications.builder().workUrl("url1").status(ApplicationStatus.SENT).build();
        WorkApplications applications2 = WorkApplications.builder().workUrl("url2").status(ApplicationStatus.REJECTED).build();
        WorkApplications applications3 = WorkApplications.builder().workUrl("url3").status(ApplicationStatus.IN_PROGRESS).build();
        List<WorkApplications> expectedList = List.of(applications1, applications2, applications3);
=======
        Applications applications1 = Applications.builder().workUrl("url1").status(ApplicationStatus.SENT).build();
        Applications applications2 = Applications.builder().workUrl("url2").status(ApplicationStatus.REJECTED).build();
        Applications applications3 = Applications.builder().workUrl("url3").status(ApplicationStatus.IN_PROGRESS).build();
        List<Applications> expectedList = List.of(applications1, applications2, applications3);
>>>>>>> dev:backend/src/test/java/io/github/mateuszuran/sisyphus_app/unit/controller/ApplicationsControllerTest.java

        //when
        when(serviceImpl.getAllApplicationsByWorkGroupId(workGroupId)).thenReturn(expectedList);

        mockMvc.perform(get("/applications/all/" + workGroupId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].workUrl").value("url1"))
                .andExpect(jsonPath("$.[0].status").value("SENT"));
    }

    @Test
    void givenWorkGroupIdAndApplicationsList_whenPost_thenCreateNewApplications() throws Exception {
        //given
        var workJson = "[{\"workUrl\":\"url1\"}]";

        //when + then
        mockMvc.perform(post("/applications/save/1234")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(workJson))
                .andExpect(status().isCreated());
    }

    @Test
    void givenWorkGroupId_whenDelete_thenReturnStatus() throws Exception {
        mockMvc.perform(delete("/applications/delete/1234"))
                .andExpect(status().isOk());
    }

    @Test
    void givenNewStatusAndApplicationId_whenUpdate_thenReturnApplication() throws Exception {
        //given
        String applicationId = "1234";
        String status = "IN_PROGRESS";
        var newStatus = ApplicationStatus.IN_PROGRESS;
        var updatedWork = works.get(0);
        updatedWork.setStatus(newStatus);
        when(serviceImpl.updateApplicationStatus(applicationId, status)).thenReturn(updatedWork);

        //when + then
        mockMvc.perform(patch("/applications/update/{applicationId}/{status}", applicationId, status))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(newStatus.name()));
    }

    @Test
    void givenAppIdAndUrl_whenPost_thenReturnAppSpecObject() throws Exception {
        // Given
        String applicationId = "1234";
        String jobUrl = "{\"url\":\"https://job.pl\"}";
        Specification spec = Specification.builder()
                .id("12345")
                .companyName("company")
                .requirements(List.of("req1", "req2"))
                .technologies(List.of("tech1", "tech2"))
                .build();

        when(specServiceImpl.saveSpecification(anyString(), anyString())).thenReturn(Mono.just(spec));

        // When & Then
        webTestClient.post()
                .uri("/applications/spec/{applicationId}", applicationId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jobUrl)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.companyName").isEqualTo("company")
                .jsonPath("$.requirements[0]").isEqualTo("req1")
                .jsonPath("$.technologies[0]").isEqualTo("tech1");
    }
}