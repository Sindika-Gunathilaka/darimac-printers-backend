package com.example.printingApp.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Entity
@Table(name = "offset_prints")
@Data
public class OffsetPrint extends PrintJob {
    private String jobType;
    private Integer quantity;
    private BigDecimal supplierJobAmount;
    private Integer profitPercentage;

    @OneToMany(mappedBy = "offsetPrint", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "offset-print-expenses")
    private List<PrintExpense> expenses = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    @JsonBackReference(value = "supplier-offsetprints")
    private Supplier supplier;

    @Transient
    private String supplierName;

    // Modified setter for customer that also sets the name
    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
        if (supplier != null) {
            this.supplierName = supplier.getName();
        }
    }

    @PrePersist
    @PreUpdate
    public void calculateTotalCost() {

        // Calculate total expenses from the expenses list
        BigDecimal totalExpenses = expenses.stream()
                .map(PrintExpense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Base cost = supplier job amount + additional expenses
        this.expensesCost = supplierJobAmount.add(totalExpenses);

        // Total amount = cost + profit
        this.totalAmount = getTotalAmount();

        // Update payment status
        updatePaymentStatus();
    }

    // Helper method to add an expense
    public void addExpense(PrintExpense expense) {
        expenses.add(expense);
        expense.setOffsetPrint(this);
    }

    // Helper method to remove an expense
    public void removeExpense(PrintExpense expense) {
        expenses.remove(expense);
        expense.setOffsetPrint(null);
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
