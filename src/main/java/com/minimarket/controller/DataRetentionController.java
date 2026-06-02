package com.minimarket.controller;

import com.minimarket.security.constants.SecurityExpressions;
import com.minimarket.security.retention.DataRetentionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/retention")
public class DataRetentionController {

    private final DataRetentionService dataRetentionService;

    public DataRetentionController(DataRetentionService dataRetentionService) {
        this.dataRetentionService = dataRetentionService;
    }

    @PostMapping("/run")
    @PreAuthorize(SecurityExpressions.SOLO_GERENTE)
    public ResponseEntity<Map<String, Object>> runRetention() {
        int count = dataRetentionService.anonymizeInactiveUsers();
        return ResponseEntity.ok(Map.of(
                "message", "Proceso de retención ejecutado",
                "anonymizedCount", count));
    }
}
