package com.example.printingApp.controller;

import com.example.printingApp.model.AuditLog;
import com.example.printingApp.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<Page<AuditLog>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) AuditLog.AuditAction action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> auditLogs;
        
        // If any filter is provided, use filtered search
        if (entityType != null || userId != null || action != null || startDate != null || endDate != null) {
            auditLogs = auditLogService.getAuditLogsWithFilters(entityType, userId, action, startDate, endDate, pageable);
        } else {
            // Otherwise get all audit logs
            auditLogs = auditLogService.getAllAuditLogs(pageable);
        }
        
        return ResponseEntity.ok(auditLogs);
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<List<AuditLog>> getAuditLogsForEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId) {
        List<AuditLog> auditLogs = auditLogService.getAuditLogsForEntity(entityType, entityId);
        return ResponseEntity.ok(auditLogs);
    }

    @GetMapping("/entity-type/{entityType}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByEntityType(@PathVariable String entityType) {
        List<AuditLog> auditLogs = auditLogService.getAuditLogsByEntityType(entityType);
        return ResponseEntity.ok(auditLogs);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByUser(@PathVariable String userId) {
        List<AuditLog> auditLogs = auditLogService.getAuditLogsByUser(userId);
        return ResponseEntity.ok(auditLogs);
    }

    @GetMapping("/action/{action}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByAction(@PathVariable AuditLog.AuditAction action) {
        List<AuditLog> auditLogs = auditLogService.getAuditLogsByAction(action);
        return ResponseEntity.ok(auditLogs);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<AuditLog>> getAuditLogsBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<AuditLog> auditLogs = auditLogService.getAuditLogsBetween(startDate, endDate);
        return ResponseEntity.ok(auditLogs);
    }

    @GetMapping("/print-job/{printJobId}")
    public ResponseEntity<List<AuditLog>> getPrintJobAuditLogs(@PathVariable Long printJobId) {
        // Get audit logs for all print job types for the given ID
        List<AuditLog> auditLogs = auditLogService.getAuditLogsForEntity("PrintJob", printJobId);
        
        // Also check specific print job types
        auditLogs.addAll(auditLogService.getAuditLogsForEntity("DigitalPrint", printJobId));
        auditLogs.addAll(auditLogService.getAuditLogsForEntity("OffsetPrint", printJobId));
        auditLogs.addAll(auditLogService.getAuditLogsForEntity("DuploPrint", printJobId));
        auditLogs.addAll(auditLogService.getAuditLogsForEntity("SublimationPrint", printJobId));
        auditLogs.addAll(auditLogService.getAuditLogsForEntity("OtherPrint", printJobId));
        
        // Sort by timestamp descending
        auditLogs.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        
        return ResponseEntity.ok(auditLogs);
    }
}