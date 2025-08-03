package com.example.printingApp.repository;

import com.example.printingApp.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    List<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, Long entityId);
    
    List<AuditLog> findByEntityTypeOrderByTimestampDesc(String entityType);
    
    List<AuditLog> findByUserIdOrderByTimestampDesc(String userId);
    
    List<AuditLog> findByActionOrderByTimestampDesc(AuditLog.AuditAction action);
    
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :startDate AND :endDate ORDER BY a.timestamp DESC")
    List<AuditLog> findByTimestampBetween(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a FROM AuditLog a WHERE a.entityType = :entityType AND a.entityId = :entityId AND a.action = :action ORDER BY a.timestamp DESC")
    List<AuditLog> findByEntityAndAction(@Param("entityType") String entityType, 
                                        @Param("entityId") Long entityId, 
                                        @Param("action") AuditLog.AuditAction action);

    // Paginated queries with filtering
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:entityType IS NULL OR a.entityType = :entityType) AND " +
           "(:userId IS NULL OR a.userId = :userId) AND " +
           "(:action IS NULL OR a.action = :action) AND " +
           "(:startDate IS NULL OR a.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR a.timestamp <= :endDate) " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> findAuditLogsWithFilters(
            @Param("entityType") String entityType,
            @Param("userId") String userId,
            @Param("action") AuditLog.AuditAction action,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // Get all audit logs with pagination
    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);
}