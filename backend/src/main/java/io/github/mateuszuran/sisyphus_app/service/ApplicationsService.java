package io.github.mateuszuran.sisyphus_app.service;

import io.github.mateuszuran.sisyphus_app.dto.ApplicationDTO;
import io.github.mateuszuran.sisyphus_app.model.Applications;
import io.github.mateuszuran.sisyphus_app.model.Specification;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ApplicationsService {
    void createApplications(List<ApplicationDTO> application, String workGroupId);

    void deleteApplication(String applicationId);

    Applications updateApplicationStatus(String status, String applicationId);

    Applications updateApplicationUrl(String applicationId, String applicationUrl);

    Mono<Applications> updateApplicationSpecificationsReactive(String applicationId, Specification specification);

    Mono<Boolean> checkSpecInsideApplicationReactive(String applicationId, Specification spec);
}
