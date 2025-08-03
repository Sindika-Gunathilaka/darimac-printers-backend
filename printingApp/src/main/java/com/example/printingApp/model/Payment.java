package com.example.printingApp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Entity
@Table(name = "payments")
@Data
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "print_job_id")
    private PrintJob printJob;

    private BigDecimal amount;
    private String paymentMethod;
    private String reference;

    @Enumerated(EnumType.STRING)
    private PaymentType type;

    public enum PaymentType {
        FULL, PARTIAL, INSTALLMENT
    }
}
