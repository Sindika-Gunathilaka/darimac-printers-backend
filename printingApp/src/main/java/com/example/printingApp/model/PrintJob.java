package com.example.printingApp.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Table(name = "print_jobs")
@Data
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class PrintJob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String jobNumber;
    private String jobName;
    private String jobDescription;

    @Enumerated(EnumType.STRING)
    private PrintType printType;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    @JsonBackReference(value = "customer-printjobs")
    private Customer customer;

    @Transient
    private String customerName;

    // Modified setter for customer that also sets the name
    public void setCustomer(Customer customer) {
        this.customer = customer;
        if (customer != null) {
            this.customerName = customer.getName();
        }
    }

    protected BigDecimal expensesCost;
    protected BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum PrintType {
        DIGITAL, OFFSET, DUPLO, OTHER, SUBLIMATION
    }

    public enum PaymentStatus {
        UNPAID, PARTIALLY_PAID, PAID
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void updatePaymentStatus() {
        if (amountPaid.compareTo(BigDecimal.ZERO) == 0) {
            this.paymentStatus = PaymentStatus.UNPAID;
        } else if (amountPaid.compareTo(totalAmount) < 0) {
            this.paymentStatus = PaymentStatus.PARTIALLY_PAID;
        } else {
            this.paymentStatus = PaymentStatus.PAID;
        }

        this.balance = totalAmount.subtract(amountPaid);
    }
}
