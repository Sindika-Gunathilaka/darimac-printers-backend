package com.example.printingApp.controller;

import com.example.printingApp.model.*;
import com.example.printingApp.service.RecurringExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/recurring-expenses")
public class RecurringExpenseController {

    @Autowired
    private RecurringExpenseService recurringExpenseService;

    @GetMapping
    public ResponseEntity<List<RecurringExpense>> getAllRecurringExpenses() {
        List<RecurringExpense> expenses = recurringExpenseService.getAllRecurringExpenses();
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/active")
    public ResponseEntity<List<RecurringExpense>> getActiveRecurringExpenses() {
        List<RecurringExpense> expenses = recurringExpenseService.getActiveRecurringExpenses();
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecurringExpense> getRecurringExpenseById(@PathVariable Long id) {
        return recurringExpenseService.getRecurringExpenseById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<RecurringExpense>> getRecurringExpensesByCategory(@PathVariable RecurringExpenseCategory category) {
        List<RecurringExpense> expenses = recurringExpenseService.getRecurringExpensesByCategory(category);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<RecurringExpenseCategory>> getAllCategories() {
        return ResponseEntity.ok(Arrays.asList(RecurringExpenseCategory.values()));
    }

    @GetMapping("/frequencies")
    public ResponseEntity<List<ExpenseFrequency>> getAllFrequencies() {
        return ResponseEntity.ok(Arrays.asList(ExpenseFrequency.values()));
    }

    @GetMapping("/due-soon")
    public ResponseEntity<List<RecurringExpense>> getExpensesDueSoon(@RequestParam(defaultValue = "30") int days) {
        List<RecurringExpense> expenses = recurringExpenseService.getExpensesDueSoon(days);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/monthly-budget")
    public ResponseEntity<BigDecimal> getMonthlyBudget() {
        BigDecimal budget = recurringExpenseService.getMonthlyBudget();
        return ResponseEntity.ok(budget);
    }

    @GetMapping("/total/{year}/{month}")
    public ResponseEntity<BigDecimal> getTotalExpensesForMonth(@PathVariable int year, @PathVariable int month) {
        BigDecimal total = recurringExpenseService.getTotalExpensesForMonth(year, month);
        return ResponseEntity.ok(total);
    }

    @PostMapping
    public ResponseEntity<RecurringExpense> createRecurringExpense(@RequestBody RecurringExpense recurringExpense) {
        try {
            RecurringExpense savedExpense = recurringExpenseService.saveRecurringExpense(recurringExpense);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedExpense);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecurringExpense> updateRecurringExpense(@PathVariable Long id, @RequestBody RecurringExpense recurringExpense) {
        try {
            RecurringExpense updatedExpense = recurringExpenseService.updateRecurringExpense(id, recurringExpense);
            return ResponseEntity.ok(updatedExpense);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecurringExpense(@PathVariable Long id) {
        try {
            recurringExpenseService.deleteRecurringExpense(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/toggle-active")
    public ResponseEntity<Void> toggleActive(@PathVariable Long id) {
        try {
            recurringExpenseService.toggleActive(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/generate/{year}/{month}")
    public ResponseEntity<Void> generateMonthlyEntries(@PathVariable int year, @PathVariable int month) {
        try {
            recurringExpenseService.generateMonthlyEntries(year, month);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/auto-generate")
    public ResponseEntity<Void> autoGenerateCurrentMonth() {
        try {
            recurringExpenseService.autoGenerateCurrentMonth();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}