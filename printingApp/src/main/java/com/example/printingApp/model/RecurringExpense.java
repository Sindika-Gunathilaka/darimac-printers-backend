package com.example.printingApp.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Entity
@Table(name = "recurring_expenses")
@Data
public class RecurringExpense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecurringExpenseCategory category;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseFrequency frequency;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Boolean autoGenerate = true;

    @Column(length = 500)
    private String description;

    private LocalDate nextDueDate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "recurringExpense", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "recurring-expense-entries")
    private List<MonthlyExpenseEntry> monthlyEntries = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        calculateNextDueDate();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateNextDueDate();
    }

    private void calculateNextDueDate() {
        if (startDate == null) return;

        LocalDate currentDate = LocalDate.now();
        LocalDate calculatedDate = startDate;

        // Find the next due date based on frequency
        while (calculatedDate.isBefore(currentDate) || calculatedDate.isEqual(currentDate)) {
            switch (frequency) {
                case MONTHLY:
                    calculatedDate = calculatedDate.plusMonths(1);
                    break;
                case QUARTERLY:
                    calculatedDate = calculatedDate.plusMonths(3);
                    break;
                case HALF_YEARLY:
                    calculatedDate = calculatedDate.plusMonths(6);
                    break;
                case YEARLY:
                    calculatedDate = calculatedDate.plusYears(1);
                    break;
            }
        }

        // Check if end date has passed
        if (endDate != null && calculatedDate.isAfter(endDate)) {
            this.nextDueDate = null;
            this.isActive = false;
        } else {
            this.nextDueDate = calculatedDate;
        }
    }

    // Helper method to add a monthly entry
    public void addMonthlyEntry(MonthlyExpenseEntry entry) {
        monthlyEntries.add(entry);
        entry.setRecurringExpense(this);
    }

    // Helper method to remove a monthly entry
    public void removeMonthlyEntry(MonthlyExpenseEntry entry) {
        monthlyEntries.remove(entry);
        entry.setRecurringExpense(null);
    }

    // Check if this expense should be generated for the given month/year
    public boolean isDueForMonth(int year, int month) {
        if (!isActive) return false;

        LocalDate targetDate = LocalDate.of(year, month, 1);
        LocalDate expenseStartDate = startDate.withDayOfMonth(1); // First day of start month

        // If the target month is before the expense starts
        if (targetDate.isBefore(expenseStartDate)) {
            return false;
        }

        // If the expense has ended and target month is after end date
        if (endDate != null && targetDate.isAfter(endDate.withDayOfMonth(1))) {
            return false;
        }

        // Check based on frequency
        switch (frequency) {
            case MONTHLY:
                // Generate for every month from start date onwards
                return !targetDate.isBefore(expenseStartDate);

            case QUARTERLY:
                // Generate every 3 months from start date
                long monthsBetween = java.time.Period.between(expenseStartDate, targetDate).toTotalMonths();
                return monthsBetween % 3 == 0;

            case HALF_YEARLY:
                // Generate every 6 months from start date
                monthsBetween = java.time.Period.between(expenseStartDate, targetDate).toTotalMonths();
                return monthsBetween % 6 == 0;

            case YEARLY:
                // Generate yearly on the same month as start date
                return targetDate.getMonth() == expenseStartDate.getMonth() &&
                        !targetDate.isBefore(expenseStartDate);

            default:
                return false;
        }
    }
}