package com.example.printingApp.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Table(name = "monthly_expense_entries")
@Data
public class MonthlyExpenseEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurring_expense_id", nullable = false)
    @JsonBackReference(value = "recurring-expense-entries")
    private RecurringExpense recurringExpense;

    @Column(name = "`year`", nullable = false)
    private Integer year;

    @Column(name = "`month`", nullable = false)
    private Integer month;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    private LocalDate paymentDate;

    @Column(length = 500)
    private String notes;

    private LocalDate dueDate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum PaymentStatus {
        UNPAID, PAID, OVERDUE
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        // Set due date to the last day of the month
        if (year != null && month != null) {
            dueDate = LocalDate.of(year, month, 1).withDayOfMonth(
                    LocalDate.of(year, month, 1).lengthOfMonth()
            );
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();

        // Update payment status based on due date
        if (paymentStatus == PaymentStatus.UNPAID && dueDate != null && dueDate.isBefore(LocalDate.now())) {
            paymentStatus = PaymentStatus.OVERDUE;
        }
    }

    // Helper method to get month name
    public String getMonthName() {
        if (month == null) return "";

        String[] months = {
                "", "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };

        return months[month];
    }

    // Helper method to get period display
    public String getPeriodDisplay() {
        return getMonthName() + " " + year;
    }
}