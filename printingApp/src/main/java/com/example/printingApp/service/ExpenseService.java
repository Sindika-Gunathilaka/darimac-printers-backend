package com.example.printingApp.service;

import com.example.printingApp.model.Expense;
import com.example.printingApp.model.Expense.PaymentStatus;
import com.example.printingApp.model.Expense.ExpenseType;
import com.example.printingApp.model.Supplier;
import com.example.printingApp.repository.ExpenseRepository;
import com.example.printingApp.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    public Optional<Expense> getExpenseById(Long id) {
        return expenseRepository.findById(id);
    }

    public List<Expense> getExpensesBySupplier(Long supplierId) {
        return expenseRepository.findBySupplierId(supplierId);
    }

    public List<Expense> getExpensesByPaymentStatus(PaymentStatus paymentStatus) {
        return expenseRepository.findByPaymentStatus(paymentStatus);
    }

    public List<Expense> getExpensesByType(ExpenseType expenseType) {
        return expenseRepository.findByExpenseType(expenseType);
    }

    public List<Expense> getExpensesByDateRange(LocalDate startDate, LocalDate endDate) {
        return expenseRepository.findByExpenseDateBetween(startDate, endDate);
    }

    public List<Expense> getExpensesByGrnNumber(String grnNumber) {
        return expenseRepository.findByGrnNumber(grnNumber);
    }

    public List<Expense> searchExpenses(
            String grnNumber,
            String supplierName,
            ExpenseType expenseType,
            PaymentStatus paymentStatus,
            LocalDate startDate,
            LocalDate endDate) {

        return expenseRepository.searchExpenses(
                grnNumber, supplierName, expenseType, paymentStatus, startDate, endDate);
    }

    @Transactional
    public Expense saveExpense(Expense expense) {
        // Generate expense number if not present
        if (expense.getExpenseNumber() == null || expense.getExpenseNumber().isEmpty()) {
            String prefix = "EXP";
            String timestamp = String.valueOf(System.currentTimeMillis()).substring(6);
            expense.setExpenseNumber(prefix + timestamp);
        }

        // Handle supplier relationship
        if (expense.getSupplier() != null && expense.getSupplier().getId() != null) {
            Supplier supplier = supplierRepository.findById(expense.getSupplier().getId())
                    .orElseThrow(() -> new RuntimeException("Supplier not found"));
            expense.setSupplier(supplier);
        }

        // Set default dates if not provided
        if (expense.getExpenseDate() == null) {
            expense.setExpenseDate(LocalDate.now());
        }

        // If payment status is PAID but payment date is null, set it to today
        if (PaymentStatus.PAID.equals(expense.getPaymentStatus()) && expense.getPaymentDate() == null) {
            expense.setPaymentDate(LocalDate.now());
        }

        return expenseRepository.save(expense);
    }

    @Transactional
    public void deleteExpense(Long id) {
        expenseRepository.deleteById(id);
    }

    @Transactional
    public Expense updatePaymentStatus(Long id, PaymentStatus paymentStatus, LocalDate paymentDate) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        expense.setPaymentStatus(paymentStatus);

        if (PaymentStatus.PAID.equals(paymentStatus)) {
            expense.setPaymentDate(paymentDate != null ? paymentDate : LocalDate.now());
        } else if (PaymentStatus.UNPAID.equals(paymentStatus)) {
            expense.setPaymentDate(null);
        }

        return expenseRepository.save(expense);
    }
}
