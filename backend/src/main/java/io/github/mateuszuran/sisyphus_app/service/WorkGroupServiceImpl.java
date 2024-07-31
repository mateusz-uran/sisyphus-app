package io.github.mateuszuran.sisyphus_app.service;

import io.github.mateuszuran.sisyphus_app.dto.WorkGroupDTO;
import io.github.mateuszuran.sisyphus_app.model.Applications;
import io.github.mateuszuran.sisyphus_app.model.WorkGroup;
import io.github.mateuszuran.sisyphus_app.repository.GroupRepository;
import io.github.mateuszuran.sisyphus_app.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkGroupServiceImpl implements WorkGroupService {
    private final GroupRepository repository;
    private final TimeUtil utility;

    @Override
    public void createNewWorkGroup(MultipartFile file) {
        try {
            Binary cv = new Binary(BsonBinarySubType.BINARY, file.getBytes());
            String creationTime = utility.formatCreationTime();

            WorkGroup group = WorkGroup.builder()
                    .cvData(cv)
                    .cvFileName(file.getOriginalFilename())
                    .creationTime(creationTime)
                    .sent(0)
                    .rejected(0)
                    .inProgress(0)
                    .isHired(false)
                    .build();
            repository.save(group);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read file", e);
        }
    }

    @Override
    public WorkGroup getWorkGroup(String workGroupId) {
        return repository.findById(workGroupId)
                .orElseThrow(() -> new RuntimeException("Work group with given ID not found"));
    }

    public WorkGroup updateWorkGroupWithApplications(List<Applications> applications, String workGroupId) {
        if (applications == null) {
            throw new IllegalStateException("Applications list is empty");
        }
        WorkGroup groupToUpdate = getWorkGroup(workGroupId);
        groupToUpdate.getApplications().addAll(applications);
        var sendValue = groupToUpdate.getSent();

        if (sendValue != 0) {
            groupToUpdate.setSent(sendValue + applications.size());

        } else {
            groupToUpdate.setSent(applications.size());
        }

        return repository.save(groupToUpdate);
    }

    @Override
    public List<WorkGroup> getAllGroups() {
        return repository.findAll();
    }

    @Override
    public void deleteSingleGroup(String workGroupId) {
        var groupToDelete = getWorkGroup(workGroupId);
        repository.delete(groupToDelete);
    }

    @Override
    public List<Applications> getAllApplicationsFromWorkGroup(String workGroupId) {
        var groupToFind = getWorkGroup(workGroupId);
        return groupToFind.getApplications();
    }


    @Override
    public void updateGroupWhenApplicationUpdate(Applications work, String newStatus, String oldStatus) {
        var group = findGroupByGivenApplication(work);

        adjustOldStatusCount(oldStatus, group, false);
        adjustNewStatusCount(newStatus, group);

        repository.save(group);
    }

    @Override
    public void updateGroupWhenApplicationDelete(Applications work) {
        var group = findGroupByGivenApplication(work);

        adjustOldStatusCount(work.getStatus().name(), group, true);
        group.getApplications().remove(work);
        repository.save(group);
    }

    private void adjustOldStatusCount(String oldStatus, WorkGroup group, boolean isDeleting) {
        switch (oldStatus.toUpperCase()) {
            case "SENT" -> {
                if (isDeleting) group.setSent(Math.max(0, group.getSent() - 1));
            }
            case "IN_PROGRESS" -> group.setInProgress(Math.max(0, group.getInProgress() - 1));
            case "REJECTED" -> group.setRejected(Math.max(0, group.getRejected() - 1));
            case "HIRED" -> group.setHired(false);
            default -> throw new IllegalStateException("Unexpected value: " + oldStatus.toUpperCase());
        }
    }

    private void adjustNewStatusCount(String newStatus, WorkGroup group) {
        switch (newStatus.toUpperCase()) {
            case "SENT" -> group.setSent(group.getSent() + 1);
            case "IN_PROGRESS" -> group.setInProgress(group.getInProgress() + 1);
            case "REJECTED" -> group.setRejected(group.getRejected() + 1);
            case "HIRED" -> group.setHired(true);
            default -> throw new IllegalStateException("Unexpected value: " + newStatus.toUpperCase());
        }
    }

    private WorkGroup findGroupByGivenApplication(Applications work) {
        return repository.findAll()
                .stream()
                .filter(workGroup -> workGroup.getApplications() != null)
                .filter(workGroup -> workGroup.getApplications()
                        .stream()
                        .anyMatch(workApplications -> workApplications.getId().equals(work.getId())))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Work group not found"));
    }

    private String encodeBinaryCv(Binary groupCv) {
        return Base64.getEncoder().encodeToString(groupCv.getData());
    }

    public WorkGroupDTO getMappedSingleWorkGroup(String workGroupId) {
        WorkGroup group = getWorkGroup(workGroupId);
        return WorkGroupDTO.builder()
                .id(group.getId())
                .cvData(encodeBinaryCv(group.getCvData()))
                .cvFileName(group.getCvFileName())
                .creationTime(group.getCreationTime())
                .applied(group.getSent())
                .rejected(group.getRejected())
                .inProgress(group.getInProgress())
                .isHired(group.isHired())
                .build();
    }

    public List<WorkGroupDTO> getAllMappedWorkGroups() {
        var allGroups = getAllGroups();
        return allGroups
                .stream()
                .map(group ->
                        WorkGroupDTO.builder()
                                .id(group.getId())
                                .cvData(encodeBinaryCv(group.getCvData()))
                                .cvFileName(group.getCvFileName())
                                .creationTime(group.getCreationTime())
                                .applied(group.getSent())
                                .rejected(group.getRejected())
                                .inProgress(group.getInProgress())
                                .isHired(group.isHired())
                                .build())
                .toList();
    }
}
