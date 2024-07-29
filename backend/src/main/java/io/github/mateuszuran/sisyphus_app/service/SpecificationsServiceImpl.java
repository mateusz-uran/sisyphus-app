package io.github.mateuszuran.sisyphus_app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mateuszuran.sisyphus_app.dto.SpecificationDTO;
import io.github.mateuszuran.sisyphus_app.exception.ScraperException;
import io.github.mateuszuran.sisyphus_app.model.Specification;
import io.github.mateuszuran.sisyphus_app.repository.SpecificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpecificationsServiceImpl implements SpecificationsService {
    private final SpecificationRepository repository;
    private final WebClient.Builder webClient;
    private final ApplicationsServiceImpl appService;

    @Value("${scraper.api}")
    private String scrapeApiUrl;

    private Mono<SpecificationDTO> scrapeJob(String url) {
        return webClient.build().post()
                .uri(scrapeApiUrl)
                .header("Content-Type", "application/json")
                .bodyValue(url)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(response -> {
                    try {
                        SpecificationDTO workSpec = new ObjectMapper().readValue(response, SpecificationDTO.class);
                        return Mono.just(workSpec);
                    } catch (JsonProcessingException e) {
                        return Mono.error(new ScraperException("Unexpected response type: " + response, HttpStatus.BAD_REQUEST));
                    }
                })
                .onErrorMap(throwable -> {
                    if (throwable instanceof WebClientRequestException) {
                        return new ScraperException("Failed to connect to the Scraper API", HttpStatus.SERVICE_UNAVAILABLE);
                    }
                    return throwable;
                });
    }


    @Override
    public Mono<Specification> saveSpecification(String url, String appId) {
        return scrapeJob(url)
                .flatMap(response -> {
                    if (checkResponse(response)) {
                        return Mono.error(new RuntimeException("Empty scraper response"));
                    }
                    var mappedResponse = mapDTOtoSpec(response);

                    return appService.checkSpecInsideApplicationReactive(appId, mappedResponse)
                            .flatMap(exists -> {
                                if (exists) {
                                    return Mono.error(new RuntimeException("This specification already exists"));
                                }

                                return Mono.fromCallable(() -> repository.save(mappedResponse))
                                        .flatMap(savedSpec ->
                                                appService.updateApplicationSpecificationsReactive(appId, savedSpec)
                                                        .thenReturn(savedSpec)
                                        );
                            });

                })
                .doOnError(error -> {
                    log.error("Scraper API call failed: {}", error.getMessage());
                    if (!(error instanceof ScraperException)) {
                        throw new ScraperException(error.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                });
    }

    private boolean checkResponse(SpecificationDTO response) {
        return (response.company_name() == null || response.company_name().isEmpty()) ||
                (response.requirements_expected() == null || response.requirements_expected().isEmpty()) ||
                (response.technologies_expected() == null || response.technologies_expected().isEmpty());
    }

    private Specification mapDTOtoSpec(SpecificationDTO dto) {
        return Specification.builder()
                .companyName(dto.company_name())
                .requirements(dto.requirements_expected())
                .technologies(dto.technologies_expected())
                .build();
    }
}
