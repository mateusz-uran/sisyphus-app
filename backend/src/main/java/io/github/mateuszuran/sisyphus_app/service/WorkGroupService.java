package io.github.mateuszuran.sisyphus_app.service;

import io.github.mateuszuran.sisyphus_app.model.Applications;
import io.github.mateuszuran.sisyphus_app.model.WorkGroup;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface WorkGroupService {
    void createNewWorkGroup(MultipartFile cvFile) throws IOException;

    WorkGroup getWorkGroup(String workGroupId);

    WorkGroup updateWorkGroupWithApplications(List<Applications> applications, String workGroupId);

    List<WorkGroup> getAllGroups();

    void deleteSingleGroup(String workGroupId);

    List<Applications> getAllApplicationsFromWorkGroup(String workGroupId);

    void updateGroupCounters(Applications work, String newStatus);
    void updateGroupCounters(Applications work);
}
