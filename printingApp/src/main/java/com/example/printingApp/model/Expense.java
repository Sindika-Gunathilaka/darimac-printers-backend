package com.example.printingApp.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * Model class for tracking expenses
 */
@Entity
@Table(name = "expenses")
@Data
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String expenseNumber; // Unique identifier for the expense

    private String description;

    @Enumerated(EnumType.STRING)
    private ExpenseType expenseType;

    private BigDecimal amount;

    private String grnNumber; // Goods Received Note number

    private LocalDate expenseDate;

    private String invoiceNumber;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    private LocalDate paymentDueDate;

    private LocalDate paymentDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum ExpenseType {
        MATERIAL,
        UTILITY,
        RENT,
        SALARY,
        EQUIPMENT,
        MAINTENANCE,
        TRANSPORT,
        MARKETING,
        TAX,
        OTHER
    }

    public enum PaymentStatus {
        PAID,
        UNPAID,
        PARTIALLY_PAID,
        OVERDUE
    }
}
