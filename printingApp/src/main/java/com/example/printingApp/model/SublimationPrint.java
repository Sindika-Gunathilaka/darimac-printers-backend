package com.example.printingApp.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Entity
@Table(name = "sublimation_prints")
@Data
public class SublimationPrint extends PrintJob {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SublimationType sublimationType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer profitPercentage = 20;

    @Column(precision = 10, scale = 2)
    private BigDecimal otherExpenses = BigDecimal.ZERO;

    @Column(length = 500)
    private String otherExpensesDescription;

    // Calculated fields
    @Column(precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalProfit;

    @OneToMany(mappedBy = "sublimationPrint", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "sublimation-print-expenses")
    private List<PrintExpense> expenses = new ArrayList<>();

    @PrePersist
    @PreUpdate
    public void calculateTotalAmount() {
        // Initialize values if null
        if (this.quantity == null) this.quantity = 1;
        if (this.unitPrice == null) this.unitPrice = BigDecimal.ZERO;
        if (this.profitPercentage == null) this.profitPercentage = 20;
        if (this.otherExpenses == null) this.otherExpenses = BigDecimal.ZERO;
        // Calculate base cost (unit price * quantity)
        BigDecimal baseCost = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));

        // Calculate total expenses from the expenses list
        BigDecimal totalExpensesList = getTotalExpenses();

        // Calculate subtotal (base cost + other expenses + expenses list)
        this.subtotal = baseCost.add(this.otherExpenses).add(totalExpensesList);

        // Calculate profit
        BigDecimal profitRate = BigDecimal.valueOf(this.profitPercentage).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        this.totalProfit = this.subtotal.multiply(profitRate);

        // Set total amount
        this.totalAmount = this.subtotal.add(this.totalProfit);
        this.expensesCost = this.subtotal;

        // Update payment status
        updatePaymentStatus();
    }

    // Helper method to add an expense
    public void addExpense(PrintExpense expense) {
        expenses.add(expense);
    }

    // Helper method to remove an expense
    public void removeExpense(PrintExpense expense) {
        expenses.remove(expense);
    }

    // Convenience method to get the total expenses from the expenses list
    public BigDecimal getTotalExpenses() {
        if (expenses == null || expenses.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return expenses.stream()
                .map(PrintExpense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Convenience method to get base cost
    public BigDecimal getBaseCost() {
        if (this.unitPrice == null || this.quantity == null) {
            return BigDecimal.ZERO;
        }
        return this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
    }
}
