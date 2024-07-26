package io.github.mateuszuran.sisyphus_app.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mateuszuran.sisyphus_app.SisyphusAppApplication;
import io.github.mateuszuran.sisyphus_app.dto.WorkSpecificationDTO;
import io.github.mateuszuran.sisyphus_app.model.WorkSpecification;
import io.github.mateuszuran.sisyphus_app.repository.WorkSpecificationRepository;
import io.github.mateuszuran.sisyphus_app.service.WorkApplicationsServiceImpl;
import io.github.mateuszuran.sisyphus_app.service.WorkSpecificationsImpl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = SisyphusAppApplication.class)
@AutoConfigureMockMvc
public class WorkSpecificationIntegrationTest {
    private static MockWebServer mockBackEnd;
    private WorkSpecificationsImpl workSpecificationsImpl;
    private WorkApplicationsServiceImpl appService;
    private WorkSpecificationRepository repository;
    private ObjectMapper objectMapper;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @BeforeEach
    void initialize() {
        WebClient.Builder webClientBuilder = mock(WebClient.Builder.class);
        WebClient webClient = WebClient.create(mockBackEnd.url("/").toString());
        when(webClientBuilder.build()).thenReturn(webClient);

        appService = mock(WorkApplicationsServiceImpl.class);
        repository = mock(WorkSpecificationRepository.class);

        workSpecificationsImpl = new WorkSpecificationsImpl(repository, webClientBuilder, appService);
        objectMapper = new ObjectMapper();
    }


    @Test
    void givenUrlAndAppId_whenNotExistsAndCanScrap_thenReturnSavedSpecJob() throws Exception {
        // Prepare mock responses
        WorkSpecificationDTO mockDTO = WorkSpecificationDTO.builder()
                .company_name("company")
                .requirements_expected(List.of("req1", "req2"))
                .technologies_expected(List.of("tech1", "tech2"))
                .build();

        WorkSpecification mockSpec = WorkSpecification.builder()
                .companyName("company")
                .requirements(List.of("req1", "req2"))
                .technologies(List.of("tech1", "tech2"))
                .build();

        mockBackEnd.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(mockDTO))
                .addHeader("Content-Type", "application/json"));

        when(appService.checkWorkSpecInsideApplicationReactive(anyString(), any())).thenReturn(Mono.just(false));
        when(repository.save(any())).thenReturn(mockSpec);
        when(appService.updateWorkApplicationSpecificationsReactive(anyString(), any())).thenReturn(Mono.empty());

        // Call the method
        Mono<WorkSpecification> result = workSpecificationsImpl.saveSpecification("fake_job_url", "1234");

        // Verify the result
        StepVerifier.create(result)
                .expectNextMatches(spec -> spec.getCompanyName().equals("company"))
                .verifyComplete();
    }

    @Test
    void givenUrlAndId_whenScrapeEmpty_thenThrowError() {
        mockBackEnd.enqueue(new MockResponse()
                .setBody("{}")
                .addHeader("Content-Type", "application/json"));

        // Call the method
        Mono<WorkSpecification> result = workSpecificationsImpl.saveSpecification("testUrl", "testAppId");

        // Verify error
        StepVerifier.create(result)
                .expectErrorMatches(error -> error instanceof RuntimeException && error.getMessage().equals("Empty scraper response"))
                .verify();
    }

    @Test
    void givenUrlAndId_whenScrapeReturnAndSpecAlreadyExists_thenReturnError() throws Exception {
        WorkSpecificationDTO mockDTO = WorkSpecificationDTO.builder()
                .company_name("company")
                .requirements_expected(List.of("req1", "req2"))
                .technologies_expected(List.of("tech1", "tech2"))
                .build();

        mockBackEnd.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(mockDTO))
                .addHeader("Content-Type", "application/json"));

        when(appService.checkWorkSpecInsideApplicationReactive(anyString(), any())).thenReturn(Mono.just(true));

        // Call the method
        Mono<WorkSpecification> result = workSpecificationsImpl.saveSpecification("testUrl", "testAppId");

        // Verify error
        StepVerifier.create(result)
                .expectErrorMatches(error -> error instanceof RuntimeException && error.getMessage().equals("This specification already exists"))
                .verify();
    }

    @Test
    void givenUrlAndId_whenScraperFail_thenReturnError() {
        // Prepare mock responses
        mockBackEnd.enqueue(new MockResponse()
                .setBody("Invalid JSON")
                .addHeader("Content-Type", "application/json"));

        // Call the method
        Mono<WorkSpecification> result = workSpecificationsImpl.saveSpecification("testUrl", "testAppId");

        // Verify error
        StepVerifier.create(result)
                .expectErrorMatches(error -> error instanceof IllegalStateException && error.getMessage().equals("Unexpected response type: Invalid JSON"))
                .verify();
    }
}
