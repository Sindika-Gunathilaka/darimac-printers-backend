package com.example.printingApp.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Entity
@Table(name = "digital_prints")
@Data
public class DigitalPrint extends PrintJob {
    @Enumerated(EnumType.STRING)
    private PrintMaterialType material;

    @Enumerated(EnumType.STRING)
    private PrintQualityType quality;

    private BigDecimal squareFeet;

    @OneToMany(mappedBy = "digitalPrint", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "digital-print-expenses")
    private List<PrintExpense> expenses = new ArrayList<>();

    @PrePersist
    @PreUpdate
    public void calculateTotalAmount() {
        // Calculate total expenses from the expenses list
        BigDecimal totalExpenses = getTotalExpenses();


        // We're no longer tracking costs and profits separately
        this.expensesCost = getExpensesCost();
        this.totalAmount = getTotalAmount();

        // Update payment status
        updatePaymentStatus();
    }

    // Helper method to add an expense
    public void addExpense(PrintExpense expense) {
        expenses.add(expense);
        expense.setDigitalPrint(this);
    }

    // Helper method to remove an expense
    public void removeExpense(PrintExpense expense) {
        expenses.remove(expense);
        expense.setDigitalPrint(null);
    }

    // Convenience method to get the total expenses
    public BigDecimal getTotalExpenses() {
        if (expenses == null || expenses.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return expenses.stream()
                .map(PrintExpense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
