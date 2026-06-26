package com.xe.ratealerts.controller;

import com.xe.ratealerts.dto.AlertResponse;
import com.xe.ratealerts.dto.CreateAlertRequest;
import com.xe.ratealerts.service.AlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Replaces AlertsStubController.
 * Delegates all logic to AlertService — controller stays thin.
 */
@RestController
@RequestMapping("/api/alerts")
public class AlertsController {

    private final AlertService alertService;

    public AlertsController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public List<AlertResponse> list() {
        return alertService.listAll();
    }

    @PostMapping
    public ResponseEntity<AlertResponse> create(@RequestBody CreateAlertRequest request) {
        AlertResponse response = alertService.create(request);
        return ResponseEntity
                .created(URI.create("/api/alerts/" + response.id()))
                .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        boolean deleted = alertService.delete(id);
        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}