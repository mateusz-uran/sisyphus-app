package io.github.mateuszuran.sisyphus_app.service;

import io.github.mateuszuran.sisyphus_app.dto.WorkApplicationDTO;
import io.github.mateuszuran.sisyphus_app.model.WorkApplications;
import io.github.mateuszuran.sisyphus_app.model.WorkSpecification;
import reactor.core.publisher.Mono;

import java.util.List;

public interface WorkApplicationsService {
    void createWorkApplication(List<WorkApplicationDTO> application, String workGroupId);

    void deleteWorkApplication(String applicationId);

    WorkApplications updateApplicationStatus(String status, String applicationId);

    WorkApplications updateWorkApplicationUrl(String applicationId, String applicationUrl);

    Mono<WorkApplications> updateWorkApplicationSpecificationsReactive(String applicationId, WorkSpecification specification);

    Mono<Boolean> checkWorkSpecInsideApplicationReactive(String applicationId, WorkSpecification spec);
}
