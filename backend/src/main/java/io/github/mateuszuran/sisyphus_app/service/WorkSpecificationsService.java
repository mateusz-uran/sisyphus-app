package io.github.mateuszuran.sisyphus_app.service;

import io.github.mateuszuran.sisyphus_app.model.WorkSpecification;
import reactor.core.publisher.Mono;

public interface WorkSpecificationsService {

    Mono<WorkSpecification> saveSpecification(String url, String appId);

}
