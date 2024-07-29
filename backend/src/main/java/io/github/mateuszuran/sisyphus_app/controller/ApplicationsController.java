package io.github.mateuszuran.sisyphus_app.controller;

import io.github.mateuszuran.sisyphus_app.dto.ApplicationDTO;
import io.github.mateuszuran.sisyphus_app.model.Applications;
import io.github.mateuszuran.sisyphus_app.model.Specification;
import io.github.mateuszuran.sisyphus_app.service.ApplicationsServiceImpl;
import io.github.mateuszuran.sisyphus_app.service.SpecificationsServiceImpl;
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
public class ApplicationsController {
    private final ApplicationsServiceImpl service;
    private final SpecificationsServiceImpl specService;

    @GetMapping("/all/{workGroupId}")
    public ResponseEntity<List<Applications>> getAllApplicationsByGroup(@PathVariable String workGroupId) {
        return ResponseEntity.ok().body(service.getAllApplicationsByWorkGroupId(workGroupId));
    }

    @PostMapping("/save/{workGroupId}")
    public ResponseEntity<String> addApplications(@RequestBody List<ApplicationDTO> applications, @PathVariable String workGroupId) {
        service.createApplications(applications, workGroupId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping("/delete/{applicationId}")
    public ResponseEntity<String> deleteSingleApplication(@PathVariable String applicationId) {
        service.deleteApplication(applicationId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PatchMapping("/update/{applicationId}/{status}")
    public ResponseEntity<Applications> updateApplicationStatus(@PathVariable String applicationId, @PathVariable String status) {
        return ResponseEntity.ok().body(service.updateApplicationStatus(applicationId, status));
    }

    @PostMapping("/spec/{applicationId}")
    public Mono<Specification> scrapeJob(@PathVariable String applicationId, @RequestBody Map<String, String> requestBody) {
        return specService.saveSpecification(requestBody.get("url"), applicationId);
    }
}
