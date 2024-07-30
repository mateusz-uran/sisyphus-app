package io.github.mateuszuran.sisyphus_app.service;

import io.github.mateuszuran.sisyphus_app.dto.ApplicationDTO;
import io.github.mateuszuran.sisyphus_app.model.ApplicationStatus;
import io.github.mateuszuran.sisyphus_app.model.Applications;
import io.github.mateuszuran.sisyphus_app.model.Specification;
import io.github.mateuszuran.sisyphus_app.repository.ApplicationsRepository;
import io.github.mateuszuran.sisyphus_app.repository.SpecificationRepository;
import io.github.mateuszuran.sisyphus_app.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationsServiceImpl implements ApplicationsService {

    private final ApplicationsRepository repository;
    private final SpecificationRepository specRepository;
    private final WorkGroupServiceImpl groupServiceImpl;
    private final TimeUtil timeUtil;

    @Override
    public void createApplications(List<ApplicationDTO> applications, String workGroupId) {
        String creationTime = timeUtil.formatCreationTime();

        // TODO: check if applications list is empty, if so throw exception or something

        var workApplicationList = applications
                .stream()
                .map(work -> Applications.builder()
                        .workUrl(work.workUrl())
                        .appliedDate(creationTime)
                        .status(ApplicationStatus.SENT)
                        .build())
                .toList();

        repository.saveAll(workApplicationList);
        groupServiceImpl.updateWorkGroupWithApplications(workApplicationList, workGroupId);
    }


    @Override
    public void deleteApplication(String applicationId) {
        var applicationToDelete = getSingleApplication(applicationId);
        groupServiceImpl.updateGroupWhenApplicationDelete(applicationToDelete);
        specRepository.deleteById(applicationToDelete.getSpecification().getId());
        repository.delete(applicationToDelete);
    }

    @Override
    public Applications updateApplicationStatus(String applicationId, String newStatus) {
        var workToUpdate = getSingleApplication(applicationId);
        String oldStatus = workToUpdate.getStatus().name();

        if (workToUpdate.getStatus().equals(ApplicationStatus.getByUpperCaseStatus(newStatus))) {
            // TODO: 07.06.2024 add custom exception
            log.info("Status are equal, cant update.");
            return null;
        }

        workToUpdate.setStatus(ApplicationStatus.getByUpperCaseStatus(newStatus));
        var savedWork = repository.save(workToUpdate);

        groupServiceImpl.updateGroupWhenApplicationUpdate(savedWork, workToUpdate.getStatus().name(), oldStatus);

        return savedWork;
    }

    @Override
    public Applications updateApplicationUrl(String applicationId, String applicationUrl) {
        var workToUpdate = repository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Work application with given ID not found."));
        workToUpdate.setWorkUrl(applicationUrl);
        return repository.save(workToUpdate);
    }

    @Override
    public Mono<Applications> updateApplicationSpecificationsReactive(String applicationId, Specification specification) {
        if (specification == null) {
            return Mono.error(new IllegalStateException("Specification is empty"));
        }
        return getSingleApplicationReactive(applicationId)
                .flatMap(application -> {
                    application.setSpecification(specification);
                    return Mono.fromCallable(() -> repository.save(application));
                });
    }

    @Override
    public Mono<Boolean> checkSpecInsideApplicationReactive(String applicationId, Specification spec) {
        return getSingleApplicationReactive(applicationId)
                .map(application -> {
                    Specification existingSpec = application.getSpecification();
                    return existingSpec != null &&
                            Objects.equals(existingSpec.getCompanyName(), spec.getCompanyName()) &&
                            Objects.equals(existingSpec.getRequirements(), spec.getRequirements()) &&
                            Objects.equals(existingSpec.getTechnologies(), spec.getTechnologies());
                });
    }

    public List<Applications> getAllApplicationsByWorkGroupId(String workGroupId) {
        return groupServiceImpl.getAllApplicationsFromWorkGroup(workGroupId);
    }

    public Applications getSingleApplication(String applicationId) {
        return repository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Work application with given id no exists."));
    }

    public Mono<Applications> getSingleApplicationReactive(String applicationId) {
        return Mono.fromCallable(() -> repository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Work application with given id does not exist.")));
    }
}
