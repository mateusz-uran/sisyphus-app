package io.github.mateuszuran.sisyphus_app.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mateuszuran.sisyphus_app.SisyphusAppApplication;
import io.github.mateuszuran.sisyphus_app.dto.SpecificationDTO;
import io.github.mateuszuran.sisyphus_app.model.Specification;
import io.github.mateuszuran.sisyphus_app.repository.SpecificationRepository;
import io.github.mateuszuran.sisyphus_app.service.ApplicationsServiceImpl;
import io.github.mateuszuran.sisyphus_app.service.SpecificationsServiceImpl;
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
public class SpecificationIntegrationTest {
    private static MockWebServer mockBackEnd;
    private SpecificationsServiceImpl workSpecificationsServiceImpl;
    private ApplicationsServiceImpl appService;
    private SpecificationRepository repository;
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

        appService = mock(ApplicationsServiceImpl.class);
        repository = mock(SpecificationRepository.class);

        workSpecificationsServiceImpl = new SpecificationsServiceImpl(repository, webClientBuilder, appService);
        objectMapper = new ObjectMapper();
    }


    @Test
    void givenUrlAndAppId_whenNotExistsAndCanScrap_thenReturnSavedSpecJob() throws Exception {
        // Prepare mock responses
        SpecificationDTO mockDTO = SpecificationDTO.builder()
                .company_name("company")
                .requirements_expected(List.of("req1", "req2"))
                .technologies_expected(List.of("tech1", "tech2"))
                .build();

        Specification mockSpec = Specification.builder()
                .companyName("company")
                .requirements(List.of("req1", "req2"))
                .technologies(List.of("tech1", "tech2"))
                .build();

        mockBackEnd.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(mockDTO))
                .addHeader("Content-Type", "application/json"));

        when(appService.checkSpecInsideApplicationReactive(anyString(), any())).thenReturn(Mono.just(false));
        when(repository.save(any())).thenReturn(mockSpec);
        when(appService.updateApplicationSpecificationsReactive(anyString(), any())).thenReturn(Mono.empty());

        // Call the method
        Mono<Specification> result = workSpecificationsServiceImpl.saveSpecification("fake_job_url", "1234");

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
        Mono<Specification> result = workSpecificationsServiceImpl.saveSpecification("testUrl", "testAppId");

        // Verify error
        StepVerifier.create(result)
                .expectErrorMatches(error -> error instanceof RuntimeException && error.getMessage().equals("Empty scraper response"))
                .verify();
    }

    @Test
    void givenUrlAndId_whenScrapeReturnAndSpecAlreadyExists_thenReturnError() throws Exception {
        SpecificationDTO mockDTO = SpecificationDTO.builder()
                .company_name("company")
                .requirements_expected(List.of("req1", "req2"))
                .technologies_expected(List.of("tech1", "tech2"))
                .build();

        mockBackEnd.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(mockDTO))
                .addHeader("Content-Type", "application/json"));

        when(appService.checkSpecInsideApplicationReactive(anyString(), any())).thenReturn(Mono.just(true));

        // Call the method
        Mono<Specification> result = workSpecificationsServiceImpl.saveSpecification("testUrl", "testAppId");

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
        Mono<Specification> result = workSpecificationsServiceImpl.saveSpecification("testUrl", "testAppId");

        // Verify error
        StepVerifier.create(result)
                .expectErrorMatches(error -> error instanceof IllegalStateException && error.getMessage().equals("Unexpected response type: Invalid JSON"))
                .verify();
    }
}
