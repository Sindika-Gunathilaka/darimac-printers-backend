package com.example.printingApp.service;

import com.example.printingApp.model.AuditLog;
import com.example.printingApp.model.PrintJob;
import com.example.printingApp.model.User;
import com.example.printingApp.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private DigitalPrintRepository digitalPrintRepository;
    
    @Autowired
    private SublimationPrintRepository sublimationPrintRepository;
    
    @Autowired
    private DuploPrintRepository duploPrintRepository;
    
    @Autowired
    private OffsetPrintRepository offsetPrintRepository;
    
    @Autowired
    private OtherPrintRepository otherPrintRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(String entityType, Long entityId, AuditLog.AuditAction action, 
                         Object oldEntity, Object newEntity, String userId, 
                         HttpServletRequest request) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setAction(action);
            auditLog.setUserId(userId);
            auditLog.setTimestamp(LocalDateTime.now());

            if (request != null) {
                auditLog.setIpAddress(getClientIpAddress(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
            }

            // Convert entities to JSON strings
            if (oldEntity != null) {
                auditLog.setOldValues(objectMapper.writeValueAsString(oldEntity));
            }
            if (newEntity != null) {
                auditLog.setNewValues(objectMapper.writeValueAsString(newEntity));
            }

            // Generate changes summary
            auditLog.setChanges(generateChangesSummary(action, oldEntity, newEntity));

            // Populate print job information if entity is a print job
            Object entityToCheck = newEntity != null ? newEntity : oldEntity;
            if (entityToCheck != null) {
                populatePrintJobInfo(auditLog, entityToCheck);
            }

            auditLogRepository.save(auditLog);
        } catch (JsonProcessingException e) {
            // Log the error but don't fail the main operation
            System.err.println("Failed to serialize entity for audit log: " + e.getMessage());
        } catch (Exception e) {
            // Log the error but don't fail the main operation
            System.err.println("Failed to create audit log: " + e.getMessage());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(String entityType, Long entityId, AuditLog.AuditAction action, 
                         Object oldEntity, Object newEntity, String userId) {
        logAction(entityType, entityId, action, oldEntity, newEntity, userId, null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(String entityType, Long entityId, AuditLog.AuditAction action, 
                         Object entity, String userId) {
        switch (action) {
            case CREATE:
                logAction(entityType, entityId, action, null, entity, userId, null);
                break;
            case DELETE:
                logAction(entityType, entityId, action, entity, null, userId, null);
                break;
            default:
                logAction(entityType, entityId, action, entity, entity, userId, null);
                break;
        }
    }

    private String generateChangesSummary(AuditLog.AuditAction action, Object oldEntity, Object newEntity) {
        switch (action) {
            case CREATE:
                return "Entity created";
            case DELETE:
                return "Entity deleted";
            case UPDATE:
                return "Entity updated";
            case PAYMENT_RECORDED:
                return "Payment recorded for print job";
            default:
                return "Action performed: " + action.name();
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        } else {
            // X-Forwarded-For can contain multiple IPs, get the first one
            return xForwardedForHeader.split(",")[0].trim();
        }
    }

    private void populateUserName(AuditLog auditLog) {
        if (auditLog.getUserId() != null) {
            try {
                // Check if userId is numeric (user ID) or text (username)
                if (auditLog.getUserId().matches("\\d+")) {
                    // userId is numeric, treat as user ID
                    Long userId = Long.parseLong(auditLog.getUserId());
                    userService.getUserById(userId).ifPresent(user -> 
                        auditLog.setUserName(user.getFullName())
                    );
                } else {
                    // userId is text, treat as username
                    userService.getUserByUsername(auditLog.getUserId()).ifPresent(user -> 
                        auditLog.setUserName(user.getFullName())
                    );
                }
            } catch (Exception e) {
                // If lookup fails, keep userName null
            }
        }
    }

    private void populateUserNames(List<AuditLog> auditLogs) {
        auditLogs.forEach(this::populateUserName);
    }

    private void populateUserNames(Page<AuditLog> auditLogs) {
        auditLogs.getContent().forEach(this::populateUserName);
    }

    private void populatePrintJobInfo(AuditLog auditLog, Object entity) {
        if (entity instanceof PrintJob) {
            PrintJob printJob = (PrintJob) entity;
            auditLog.setPrintJobId(printJob.getId());
            if (printJob.getCustomer() != null) {
                auditLog.setCustomerName(printJob.getCustomer().getName());
            } else if (printJob.getCustomerName() != null) {
                auditLog.setCustomerName(printJob.getCustomerName());
            }
        }
    }

    private void populatePrintJobInfoFromAuditLog(AuditLog auditLog) {
        if (isPrintJobEntity(auditLog.getEntityType())) {
            try {
                PrintJob printJob = fetchPrintJobByTypeAndId(auditLog.getEntityType(), auditLog.getEntityId());
                if (printJob != null) {
                    auditLog.setPrintJobId(printJob.getId());
                    if (printJob.getCustomer() != null) {
                        auditLog.setCustomerName(printJob.getCustomer().getName());
                    } else if (printJob.getCustomerName() != null) {
                        auditLog.setCustomerName(printJob.getCustomerName());
                    }
                }
            } catch (Exception e) {
                // If lookup fails, keep fields null
            }
        }
    }

    private boolean isPrintJobEntity(String entityType) {
        return "DigitalPrint".equals(entityType) || 
               "SublimationPrint".equals(entityType) || 
               "DuploPrint".equals(entityType) || 
               "OffsetPrint".equals(entityType) || 
               "OtherPrint".equals(entityType);
    }

    private PrintJob fetchPrintJobByTypeAndId(String entityType, Long entityId) {
        try {
            switch (entityType) {
                case "DigitalPrint":
                    return digitalPrintRepository.findById(entityId).orElse(null);
                case "SublimationPrint":
                    return sublimationPrintRepository.findById(entityId).orElse(null);
                case "DuploPrint":
                    return duploPrintRepository.findById(entityId).orElse(null);
                case "OffsetPrint":
                    return offsetPrintRepository.findById(entityId).orElse(null);
                case "OtherPrint":
                    return otherPrintRepository.findById(entityId).orElse(null);
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private void populateAllAuditLogInfo(List<AuditLog> auditLogs) {
        auditLogs.forEach(auditLog -> {
            populateUserName(auditLog);
            populatePrintJobInfoFromAuditLog(auditLog);
        });
    }

    private void populateAllAuditLogInfo(Page<AuditLog> auditLogs) {
        auditLogs.getContent().forEach(auditLog -> {
            populateUserName(auditLog);
            populatePrintJobInfoFromAuditLog(auditLog);
        });
    }

    // Query methods
    public List<AuditLog> getAuditLogsForEntity(String entityType, Long entityId) {
        List<AuditLog> auditLogs = auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId);
        populateAllAuditLogInfo(auditLogs);
        return auditLogs;
    }

    public List<AuditLog> getAuditLogsByEntityType(String entityType) {
        List<AuditLog> auditLogs = auditLogRepository.findByEntityTypeOrderByTimestampDesc(entityType);
        populateAllAuditLogInfo(auditLogs);
        return auditLogs;
    }

    public List<AuditLog> getAuditLogsByUser(String userId) {
        List<AuditLog> auditLogs = auditLogRepository.findByUserIdOrderByTimestampDesc(userId);
        populateAllAuditLogInfo(auditLogs);
        return auditLogs;
    }

    public List<AuditLog> getAuditLogsByAction(AuditLog.AuditAction action) {
        List<AuditLog> auditLogs = auditLogRepository.findByActionOrderByTimestampDesc(action);
        populateAllAuditLogInfo(auditLogs);
        return auditLogs;
    }

    public List<AuditLog> getAuditLogsBetween(LocalDateTime startDate, LocalDateTime endDate) {
        List<AuditLog> auditLogs = auditLogRepository.findByTimestampBetween(startDate, endDate);
        populateAllAuditLogInfo(auditLogs);
        return auditLogs;
    }

    // Paginated methods with filtering
    public Page<AuditLog> getAuditLogsWithFilters(String entityType, String userId, 
                                                  AuditLog.AuditAction action,
                                                  LocalDateTime startDate, LocalDateTime endDate,
                                                  Pageable pageable) {
        Page<AuditLog> auditLogs = auditLogRepository.findAuditLogsWithFilters(entityType, userId, action, startDate, endDate, pageable);
        populateAllAuditLogInfo(auditLogs);
        return auditLogs;
    }

    public Page<AuditLog> getAllAuditLogs(Pageable pageable) {
        Page<AuditLog> auditLogs = auditLogRepository.findAllByOrderByTimestampDesc(pageable);
        populateAllAuditLogInfo(auditLogs);
        return auditLogs;
    }
}