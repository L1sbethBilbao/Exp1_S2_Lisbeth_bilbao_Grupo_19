package com.minimarket.controller;

import com.minimarket.entity.AuditLog;
import com.minimarket.repository.AuditLogRepository;
import com.minimarket.security.constants.SecurityExpressions;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    public AuditLogController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    @PreAuthorize(SecurityExpressions.SOLO_GERENTE)
    public List<AuditLog> listarAuditLogs() {
        return auditLogRepository.findByResourceOrderByTimestampDesc("/api/usuarios");
    }
}
