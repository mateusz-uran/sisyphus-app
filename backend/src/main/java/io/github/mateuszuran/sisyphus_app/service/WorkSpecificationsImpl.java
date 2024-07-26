package io.github.mateuszuran.sisyphus_app.service;

import io.github.mateuszuran.sisyphus_app.dto.WorkSpecificationDTO;
import io.github.mateuszuran.sisyphus_app.model.WorkSpecification;
import io.github.mateuszuran.sisyphus_app.repository.WorkSpecificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkSpecificationsImpl implements WorkSpecificationsService {
    private final WorkSpecificationRepository repository;
    private final WebClient.Builder webClient;
    private final WorkApplicationsServiceImpl appService;

    // replace with variable in application.properties
    private String scrapeApiUrl = "http://127.0.0.1:5858/scrape";

    private Mono<WorkSpecificationDTO> scraperJob(String url) {
        return webClient.build().post()
                .uri(scrapeApiUrl)
                .header("Content-Type", "application/json")
                .bodyValue(url)
                .retrieve()
                .bodyToMono(WorkSpecificationDTO.class);
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
        return response.company_name().isEmpty() ||
                response.requirements_expected().isEmpty() ||
                response.technologies_expected().isEmpty();
    }

    private WorkSpecification mapWorkSpecDTOtoWorkSpec(WorkSpecificationDTO dto) {
        return WorkSpecification.builder()
                .companyName(dto.company_name())
                .requirements(dto.requirements_expected())
                .technologies(dto.technologies_expected())
                .build();
    }
}
