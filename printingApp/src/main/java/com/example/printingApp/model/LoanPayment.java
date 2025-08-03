package com.example.printingApp.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Table(name = "loan_payments")
@Data
public class LoanPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    @JsonBackReference(value = "loan-payments")
    private Loan loan;

    @Column(nullable = false)
    private Integer paymentNumber; // 1, 2, 3... representing the installment number

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate dueDate;

    private LocalDate paidDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(length = 100)
    private String transactionReference;

    @Column(length = 500)
    private String notes;

    @Column(precision = 10, scale = 2)
    private BigDecimal lateFee = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal principalComponent; // Principal portion of this payment

    @Column(precision = 12, scale = 2)
    private BigDecimal interestComponent; // Interest portion of this payment

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum PaymentStatus {
        PAID,
        UNPAID,
        PARTIALLY_PAID,
        OVERDUE
    }

    public enum PaymentMethod {
        BANK_TRANSFER, CASH, CREDIT_CARD, DEBIT_CARD, ONLINE, CHECK, OTHER
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        // Only calculate components if we have sufficient data and avoid infinite calculations
        safeCalculatePaymentComponents();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();

        // Auto-update status based on due date
        if (paymentStatus == PaymentStatus.UNPAID && dueDate != null && dueDate.isBefore(LocalDate.now())) {
            paymentStatus = PaymentStatus.OVERDUE;
        }
    }

    // Safe calculation to avoid overflow errors
    private void safeCalculatePaymentComponents() {
        try {
            if (loan != null && amount != null && paymentNumber != null &&
                    loan.getInterestRate() != null && loan.getPrincipalAmount() != null) {

                // Validate input values to prevent overflow
                if (paymentNumber <= 0 || paymentNumber > 1000 || // reasonable payment number limit
                        amount.compareTo(BigDecimal.ZERO) <= 0 ||
                        loan.getInterestRate().compareTo(new BigDecimal("100")) > 0) { // max 100% interest rate
                    setDefaultComponents();
                    return;
                }

                calculatePaymentComponents();
            } else {
                setDefaultComponents();
            }
        } catch (Exception e) {
            // Log the error and set default values
            System.err.println("Error calculating payment components: " + e.getMessage());
            setDefaultComponents();
        }
    }

    // Set default components when calculation fails
    private void setDefaultComponents() {
        if (amount != null) {
            principalComponent = amount;
            interestComponent = BigDecimal.ZERO;
        } else {
            principalComponent = BigDecimal.ZERO;
            interestComponent = BigDecimal.ZERO;
        }
    }

    // Calculate principal and interest components of this payment
    private void calculatePaymentComponents() {
        if (loan == null || amount == null || paymentNumber == null) {
            setDefaultComponents();
            return;
        }

        BigDecimal monthlyRate = loan.getInterestRate().divide(BigDecimal.valueOf(100 * 12), 10, RoundingMode.HALF_UP);

        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            // No interest case
            principalComponent = amount;
            interestComponent = BigDecimal.ZERO;
            return;
        }

        try {
            // Calculate remaining balance at the time of this payment
            BigDecimal remainingBalance = loan.getPrincipalAmount();

            // Validate monthly payment exists
            if (loan.getMonthlyPayment() == null || loan.getMonthlyPayment().compareTo(BigDecimal.ZERO) <= 0) {
                setDefaultComponents();
                return;
            }

            // Calculate balance for previous payments (but limit iterations to prevent overflow)
            int maxIterations = Math.min(paymentNumber - 1, 360); // max 30 years

            for (int i = 1; i <= maxIterations; i++) {
                BigDecimal interestForMonth = remainingBalance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
                BigDecimal principalForMonth = loan.getMonthlyPayment().subtract(interestForMonth);

                // Prevent negative remaining balance
                if (principalForMonth.compareTo(remainingBalance) >= 0) {
                    remainingBalance = BigDecimal.ZERO;
                    break;
                }

                remainingBalance = remainingBalance.subtract(principalForMonth);

                // Safety check for extremely small balances
                if (remainingBalance.compareTo(new BigDecimal("0.01")) < 0) {
                    remainingBalance = BigDecimal.ZERO;
                    break;
                }
            }

            // Calculate components for this payment
            interestComponent = remainingBalance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            principalComponent = amount.subtract(interestComponent);

            // Ensure components don't exceed the payment amount
            if (principalComponent.compareTo(BigDecimal.ZERO) < 0) {
                principalComponent = BigDecimal.ZERO;
                interestComponent = amount;
            }

        } catch (ArithmeticException e) {
            System.err.println("Arithmetic error in payment calculation: " + e.getMessage());
            setDefaultComponents();
        }
    }

    // Business logic methods
    public Boolean isOverdue() {
        return paymentStatus == PaymentStatus.OVERDUE ||
                (paymentStatus == PaymentStatus.UNPAID && dueDate != null && dueDate.isBefore(LocalDate.now()));
    }

    public Integer getDaysOverdue() {
        if (!isOverdue()) return 0;
        return Math.toIntExact(java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now()));
    }

    public BigDecimal getTotalAmountDue() {
        return amount.add(lateFee != null ? lateFee : BigDecimal.ZERO);
    }

    // Helper method to get payment period display
    public String getPaymentPeriodDisplay() {
        if (dueDate == null) return "";
        return dueDate.getMonth().name() + " " + dueDate.getYear();
    }

    // Helper method to mark as paid
    public void markAsPaid(LocalDate paymentDate, PaymentMethod method, String reference) {
        this.paidDate = paymentDate;
        this.paymentStatus = PaymentStatus.PAID;
        this.paymentMethod = method;
        this.transactionReference = reference;
    }


}