package com.example.printingApp.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Entity
@Table(name = "duplo_prints")
@Data
public class DuploPrint extends PrintJob {
    private Integer quantity;
    private String paperSize;
    private Integer copies;
    private BigDecimal baseCost;
    private BigDecimal otherExpenses;
    private String otherExpensesDescription;
    private Integer profitPercentage;

    @OneToMany(mappedBy = "duploPrint", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "duplo-print-expenses")
    private List<PrintExpense> expenses = new ArrayList<>();

    @PrePersist
    @PreUpdate
    public void calculateTotalAmount() {
        // Initialize values if null
        if (this.baseCost == null) this.baseCost = BigDecimal.ZERO;
        if (this.otherExpenses == null) this.otherExpenses = BigDecimal.ZERO;
        if (this.profitPercentage == null) this.profitPercentage = 0;

        // Calculate total expenses from the expenses list
        BigDecimal totalExpensesList = getTotalExpenses();

        // Calculate subtotal (base cost + other expenses + expenses list)
        BigDecimal subtotal = this.baseCost
                .add(this.otherExpenses)
                .add(totalExpensesList);

        // Calculate profit
        BigDecimal profit = subtotal.multiply(
                BigDecimal.valueOf(this.profitPercentage).divide(BigDecimal.valueOf(100))
        );

        // Set total amount
        this.totalAmount = subtotal.add(profit);
        this.expensesCost = subtotal;

        // Update payment status
        updatePaymentStatus();
    }

    // Helper method to add an expense
    public void addExpense(PrintExpense expense) {
        expenses.add(expense);
        expense.setDuploPrint(this);
    }

    // Helper method to remove an expense
    public void removeExpense(PrintExpense expense) {
        expenses.remove(expense);
        expense.setDuploPrint(null);
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
}