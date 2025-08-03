package com.example.printingApp.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Entity
@Table(name = "loans")
@Data
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference(value = "user-loans")
    private User user;

    @Column(nullable = false, length = 200)
    private String loanName;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal principalAmount;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate; // Annual interest rate as percentage

    @Column(nullable = false)
    private Integer loanTermMonths; // Total loan term in months

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlyPayment;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status = LoanStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanType loanType;

    @Column(length = 500)
    private String description;

    @Column(length = 100)
    private String lender; // Bank or institution name

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "loan-payments")
    private List<LoanPayment> payments = new ArrayList<>();

    public enum LoanStatus {
        ACTIVE, COMPLETED, DEFAULTED, SUSPENDED
    }

    public enum LoanType {
        PERSONAL, HOME, VEHICLE, BUSINESS, EDUCATION, OTHER
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        calculateMonthlyPayment();
        calculateEndDate();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Calculate monthly payment using loan formula
    private void calculateMonthlyPayment() {
        if (principalAmount != null && interestRate != null && loanTermMonths != null) {
            double principal = principalAmount.doubleValue();
            double monthlyRate = interestRate.doubleValue() / 100 / 12; // Convert annual % to monthly decimal
            int months = loanTermMonths;

            if (monthlyRate == 0) {
                // Simple division if no interest
                monthlyPayment = principalAmount.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
            } else {
                // EMI formula: P * [r(1+r)^n] / [(1+r)^n - 1]
                double emi = principal * (monthlyRate * Math.pow(1 + monthlyRate, months)) /
                        (Math.pow(1 + monthlyRate, months) - 1);
                monthlyPayment = BigDecimal.valueOf(emi).setScale(2, RoundingMode.HALF_UP);
            }
        }
    }

    // Calculate end date based on start date and term
    private void calculateEndDate() {
        if (startDate != null && loanTermMonths != null) {
            endDate = startDate.plusMonths(loanTermMonths);
        }
    }

    // Business logic methods
    public BigDecimal getTotalAmountToPay() {
        return monthlyPayment.multiply(BigDecimal.valueOf(loanTermMonths));
    }

    public BigDecimal getTotalInterest() {
        return getTotalAmountToPay().subtract(principalAmount);
    }

    public BigDecimal getPaidAmount() {
        return payments.stream()
                .filter(p -> p.getPaymentStatus() == LoanPayment.PaymentStatus.PAID)
                .map(LoanPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getOutstandingBalance() {
        return getTotalAmountToPay().subtract(getPaidAmount());
    }

    public Integer getCompletedPayments() {
        return (int) payments.stream()
                .filter(p -> p.getPaymentStatus() == LoanPayment.PaymentStatus.PAID)
                .count();
    }

    public Integer getRemainingPayments() {
        return loanTermMonths - getCompletedPayments();
    }

    public LocalDate getNextPaymentDate() {
        if (startDate == null) return null;

        int completedPayments = getCompletedPayments();
        if (completedPayments >= loanTermMonths) {
            return null; // Loan completed
        }

        return startDate.plusMonths(completedPayments + 1);
    }

    public Double getCompletionPercentage() {
        if (loanTermMonths == 0) return 0.0;
        return (getCompletedPayments().doubleValue() / loanTermMonths) * 100;
    }

    public Boolean isOverdue() {
        LocalDate nextPayment = getNextPaymentDate();
        return nextPayment != null && nextPayment.isBefore(LocalDate.now());
    }

    public Integer getMonthsRemaining() {
        LocalDate nextPayment = getNextPaymentDate();
        if (nextPayment == null) return 0;

        Period period = Period.between(LocalDate.now(), endDate);
        return period.getYears() * 12 + period.getMonths();
    }

    // Helper methods
    public void addPayment(LoanPayment payment) {
        payments.add(payment);
        payment.setLoan(this);
    }

    public void removePayment(LoanPayment payment) {
        payments.remove(payment);
        payment.setLoan(null);
    }
}