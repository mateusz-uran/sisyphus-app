package io.github.mateuszuran.sisyphus_app.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mateuszuran.sisyphus_app.dto.WorkApplicationDTO;
import io.github.mateuszuran.sisyphus_app.model.WorkApplications;
import io.github.mateuszuran.sisyphus_app.model.WorkSpecification;
import io.github.mateuszuran.sisyphus_app.service.WorkApplicationsServiceImpl;
import io.github.mateuszuran.sisyphus_app.service.WorkSpecificationsServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
public class WorkApplicationsController {
    private final WorkApplicationsServiceImpl service;
    private final WorkSpecificationsServiceImpl specService;

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

    @PostMapping("/spec/{applicationId}")
    public Mono<WorkSpecification> scrape( @PathVariable String applicationId, @RequestBody Map<String, String> requestBody) {
        return specService.saveSpecification(requestBody.get("url"), applicationId);
    }
}
