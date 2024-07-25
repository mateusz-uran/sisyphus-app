package io.github.mateuszuran.sisyphus_app.controller;

import io.github.mateuszuran.sisyphus_app.dto.WorkApplicationDTO;
import io.github.mateuszuran.sisyphus_app.dto.WorkSpecificationDTO;
import io.github.mateuszuran.sisyphus_app.model.WorkApplications;
import io.github.mateuszuran.sisyphus_app.model.WorkSpecification;
import io.github.mateuszuran.sisyphus_app.service.WorkApplicationsServiceImpl;
import io.github.mateuszuran.sisyphus_app.service.WorkSpecificationsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
public class WorkApplicationsController {
    private final WorkApplicationsServiceImpl service;
    private final WorkSpecificationsImpl specService;

    @GetMapping("/all/{workGroupId}")
    public ResponseEntity<List<WorkApplications>> getAllWorkApplicationsByGroup(@PathVariable String workGroupId) {
        return ResponseEntity.ok().body(service.getAllApplicationsByWorkGroupId(workGroupId));
    }

    @PostMapping("/save/{workGroupId}")
    public ResponseEntity<String> addWorkApp(@RequestBody List<WorkApplicationDTO> applications, @PathVariable String workGroupId) {
        service.createWorkApplication(applications, workGroupId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping("/delete/{applicationId}")
    public ResponseEntity<String> deleteSingleWorkApplication(@PathVariable String applicationId) {
        service.deleteWorkApplication(applicationId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("/update/{applicationId}/{status}")
    public ResponseEntity<WorkApplications> updateWorkStatus(@PathVariable String applicationId, @PathVariable String status) {
        return ResponseEntity.ok().body(service.updateApplicationStatus(applicationId, status));
    }

    @PostMapping("/spec/{applicationId}/{type}")
    public Mono<WorkSpecification> scrape(@PathVariable String type, @PathVariable String applicationId, @RequestBody String url) {
        return specService.saveSpecification(type, url, applicationId);
    }
}
