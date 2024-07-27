package io.github.mateuszuran.sisyphus_app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mateuszuran.sisyphus_app.dto.WorkSpecificationDTO;
import io.github.mateuszuran.sisyphus_app.model.WorkSpecification;
import io.github.mateuszuran.sisyphus_app.repository.WorkSpecificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkSpecificationsServiceImpl implements WorkSpecificationsService {
    private final WorkSpecificationRepository repository;
    private final WebClient.Builder webClient;
    private final WorkApplicationsServiceImpl appService;

    @Value("${scraper.api}")
    private String scrapeApiUrl;

    private Mono<WorkSpecificationDTO> scraperJob(String url) {
        return webClient.build().post()
                .uri(scrapeApiUrl)
                .header("Content-Type", "application/json")
                .bodyValue(url)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(response -> {
                    try {
                        WorkSpecificationDTO workSpec = new ObjectMapper().readValue(response, WorkSpecificationDTO.class);
                        return Mono.just(workSpec);
                    } catch (JsonProcessingException e) {
                        // TODO: create custom exception
                        return Mono.error(new IllegalStateException("Unexpected response type: " + response));
                    }
                });
    }


    // TODO: replace runtime with custom exceptions
    @Override
    public Mono<WorkSpecification> saveSpecification(String url, String appId) {
        return scraperJob(url)
                .flatMap(response -> {
                    if (checkResponse(response)) {
                        return Mono.error(new RuntimeException("Empty scraper response"));
                    }

                    var mappedResponse = mapWorkSpecDTOtoWorkSpec(response);

                    return appService.checkWorkSpecInsideApplicationReactive(appId, mappedResponse)
                            .flatMap(exists -> {
                                if (exists) {
                                    return Mono.error(new RuntimeException("This specification already exists"));
                                }

                                return Mono.fromCallable(() -> repository.save(mappedResponse))
                                        .flatMap(savedSpec ->
                                                appService.updateWorkApplicationSpecificationsReactive(appId, savedSpec)
                                                        .thenReturn(savedSpec)
                                        );
                            });

                })
                .doOnError(error -> log.error("Scraper API call failed: {}", error.getMessage()));
    }

    private boolean checkResponse(WorkSpecificationDTO response) {
        return (response.company_name() == null || response.company_name().isEmpty()) ||
                (response.requirements_expected() == null || response.requirements_expected().isEmpty()) ||
                (response.technologies_expected() == null || response.technologies_expected().isEmpty());
    }

    private WorkSpecification mapWorkSpecDTOtoWorkSpec(WorkSpecificationDTO dto) {
        return WorkSpecification.builder()
                .companyName(dto.company_name())
                .requirements(dto.requirements_expected())
                .technologies(dto.technologies_expected())
                .build();
    }
}
