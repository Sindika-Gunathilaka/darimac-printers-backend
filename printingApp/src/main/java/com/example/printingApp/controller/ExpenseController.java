package com.example.printingApp.controller;

import com.example.printingApp.model.Expense;
import com.example.printingApp.model.Expense.PaymentStatus;
import com.example.printingApp.model.Expense.ExpenseType;
import com.example.printingApp.model.Supplier;
import com.example.printingApp.service.ExpenseService;
import com.example.printingApp.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private SupplierService supplierService;

    @GetMapping
    public List<Expense> getAllExpenses() {
        return expenseService.getAllExpenses();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Expense> getExpenseById(@PathVariable Long id) {
        return expenseService.getExpenseById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/supplier/{supplierId}")
    public List<Expense> getExpensesBySupplier(@PathVariable Long supplierId) {
        return expenseService.getExpensesBySupplier(supplierId);
    }

    @GetMapping("/status/{status}")
    public List<Expense> getExpensesByPaymentStatus(@PathVariable String status) {
        try {
            PaymentStatus paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
            return expenseService.getExpensesByPaymentStatus(paymentStatus);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid payment status: " + status);
        }
    }

    @GetMapping("/type/{type}")
    public List<Expense> getExpensesByType(@PathVariable String type) {
        try {
            ExpenseType expenseType = ExpenseType.valueOf(type.toUpperCase());
            return expenseService.getExpensesByType(expenseType);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid expense type: " + type);
        }
    }

    @GetMapping("/date-range")
    public List<Expense> getExpensesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return expenseService.getExpensesByDateRange(startDate, endDate);
    }

    @GetMapping("/grn/{grnNumber}")
    public List<Expense> getExpensesByGrnNumber(@PathVariable String grnNumber) {
        return expenseService.getExpensesByGrnNumber(grnNumber);
    }

    @GetMapping("/search")
    public List<Expense> searchExpenses(
            @RequestParam(required = false) String grnNumber,
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) String expenseType,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        ExpenseType type = null;
        if (expenseType != null && !expenseType.isEmpty()) {
            try {
                type = ExpenseType.valueOf(expenseType.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid type, will be treated as null
            }
        }

        PaymentStatus status = null;
        if (paymentStatus != null && !paymentStatus.isEmpty()) {
            try {
                status = PaymentStatus.valueOf(paymentStatus.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid status, will be treated as null
            }
        }

        return expenseService.searchExpenses(
                grnNumber, supplierName, type, status, startDate, endDate);
    }

    @PostMapping
    public ResponseEntity<Expense> createExpense(@RequestBody Expense expense) {
        Expense saved = expenseService.saveExpense(expense);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Expense> updateExpense(@PathVariable Long id, @RequestBody Expense expense) {
        return expenseService.getExpenseById(id)
                .map(existingExpense -> {
                    expense.setId(id);
                    return ResponseEntity.ok(expenseService.saveExpense(expense));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/payment-status")
    public ResponseEntity<Expense> updatePaymentStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {

        try {
            String statusStr = (String) updates.get("paymentStatus");
            PaymentStatus status = PaymentStatus.valueOf(statusStr.toUpperCase());

            LocalDate paymentDate = null;
            if (updates.containsKey("paymentDate")) {
                paymentDate = LocalDate.parse((String) updates.get("paymentDate"));
            }

            Expense updated = expenseService.updatePaymentStatus(id, status, paymentDate);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        return expenseService.getExpenseById(id)
                .map(expense -> {
                    expenseService.deleteExpense(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/suppliers")
    public List<Supplier> getAllSuppliers() {
        return supplierService.getAllSuppliers();
    }

    @GetMapping("/expense-types")
    public List<String> getExpenseTypes() {
        return java.util.Arrays.stream(ExpenseType.values())
                .map(Enum::name)
                .toList();
    }

    @GetMapping("/payment-statuses")
    public List<String> getPaymentStatuses() {
        return java.util.Arrays.stream(PaymentStatus.values())
                .map(Enum::name)
                .toList();
    }
}
