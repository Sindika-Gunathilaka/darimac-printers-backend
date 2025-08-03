package com.example.printingApp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String entityType; // e.g., "PrintJob", "DigitalPrint", "OffsetPrint"

    @Column(nullable = false)
    private Long entityId; // ID of the entity being audited

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AuditAction action;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private String userId; // User who performed the action
    
    @Transient
    private String userName; // Name of the user who performed the action
    
    @Transient
    private Long printJobId; // Print job ID for print job related entities
    
    @Transient
    private String customerName; // Customer name for print job related entities

    @Column(columnDefinition = "TEXT")
    private String oldValues; // JSON string of previous values

    @Column(columnDefinition = "TEXT")
    private String newValues; // JSON string of new values

    @Column(columnDefinition = "TEXT")
    private String changes; // Summary of what changed

    private String ipAddress;
    private String userAgent;

    public enum AuditAction {
        CREATE, UPDATE, DELETE, PAYMENT_RECORDED
    }

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}