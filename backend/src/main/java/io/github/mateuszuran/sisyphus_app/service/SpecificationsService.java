package io.github.mateuszuran.sisyphus_app.service;

import io.github.mateuszuran.sisyphus_app.model.Specification;
import reactor.core.publisher.Mono;

public interface SpecificationsService {

    Mono<Specification> saveSpecification(String url, String appId);

}
