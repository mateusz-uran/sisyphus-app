package io.github.mateuszuran.sisyphus_app.service;

import io.github.mateuszuran.sisyphus_app.dto.WorkGroupDTO;
import io.github.mateuszuran.sisyphus_app.exception.ServiceException;
import io.github.mateuszuran.sisyphus_app.model.Applications;
import io.github.mateuszuran.sisyphus_app.model.WorkGroup;
import io.github.mateuszuran.sisyphus_app.repository.GroupRepository;
import io.github.mateuszuran.sisyphus_app.util.TimeUtil;
import io.github.mateuszuran.sisyphus_app.util.WorkGroupComparator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Comparator;
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
        var applications = getWorkGroup(workGroupId).getApplications();

        applications.sort(Comparator.comparing((Applications app) -> {
            try {
                return utility.convertCreationTime(app.getAppliedDate());
            } catch (ParseException e) {
                log.info("Parse exception: " + e.getMessage());
                throw new ServiceException("Cannot sort applications list because of time parsing", HttpStatus.BAD_REQUEST);
            }
        }, Comparator.nullsLast(Timestamp::compareTo)).reversed());

        return applications;
    }

    @Override
    public void updateGroupCounters(Applications work, String newStatus) {
        WorkGroup group = findGroupByGivenApplication(work);
        group.incrementCounter(newStatus);
        group.decrementCounter(work.getStatus().name());
        repository.save(group);
    }

    @Override
    public void updateGroupCounters(Applications work) {
        WorkGroup group = findGroupByGivenApplication(work);
        group.decrementCounter(work.getStatus().name());
        repository.save(group);
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
        allGroups.sort(new WorkGroupComparator(utility));
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
