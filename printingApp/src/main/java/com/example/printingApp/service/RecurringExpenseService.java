package com.example.printingApp.service;

import com.example.printingApp.model.*;
import com.example.printingApp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class RecurringExpenseService {

    @Autowired
    private RecurringExpenseRepository recurringExpenseRepository;

    @Autowired
    private MonthlyExpenseEntryRepository monthlyExpenseEntryRepository;

    public List<RecurringExpense> getAllRecurringExpenses() {
        return recurringExpenseRepository.findAll();
    }

    public List<RecurringExpense> getActiveRecurringExpenses() {
        return recurringExpenseRepository.findByIsActiveTrueOrderByNextDueDateAsc();
    }

    public Optional<RecurringExpense> getRecurringExpenseById(Long id) {
        return recurringExpenseRepository.findById(id);
    }

    public List<RecurringExpense> getRecurringExpensesByCategory(RecurringExpenseCategory category) {
        return recurringExpenseRepository.findByCategory(category);
    }

    @Transactional
    public RecurringExpense saveRecurringExpense(RecurringExpense recurringExpense) {
        return recurringExpenseRepository.save(recurringExpense);
    }

    @Transactional
    public RecurringExpense updateRecurringExpense(Long id, RecurringExpense recurringExpense) {
        return getRecurringExpenseById(id)
                .map(existingExpense -> {
                    recurringExpense.setId(id);
                    recurringExpense.setCreatedAt(existingExpense.getCreatedAt());
                    return recurringExpenseRepository.save(recurringExpense);
                })
                .orElseThrow(() -> new RuntimeException("Recurring expense not found with id: " + id));
    }

    @Transactional
    public void deleteRecurringExpense(Long id) {
        recurringExpenseRepository.deleteById(id);
    }

    @Transactional
    public void toggleActive(Long id) {
        getRecurringExpenseById(id).ifPresent(expense -> {
            expense.setIsActive(!expense.getIsActive());
            recurringExpenseRepository.save(expense);
        });
    }

    // Generate monthly entries for expenses that are due
    @Transactional
    public void generateMonthlyEntries(int year, int month) {
        List<RecurringExpense> activeExpenses = getActiveRecurringExpenses();

        for (RecurringExpense expense : activeExpenses) {
            if (expense.getAutoGenerate() && expense.isDueForMonth(year, month)) {
                // Check if entry already exists
                Optional<MonthlyExpenseEntry> existingEntry = monthlyExpenseEntryRepository
                        .findByRecurringExpenseAndYearAndMonth(expense, year, month);

                if (existingEntry.isEmpty()) {
                    MonthlyExpenseEntry entry = new MonthlyExpenseEntry();
                    entry.setRecurringExpense(expense);
                    entry.setYear(year);
                    entry.setMonth(month);
                    entry.setAmount(expense.getAmount());
                    entry.setPaymentStatus(MonthlyExpenseEntry.PaymentStatus.UNPAID);

                    monthlyExpenseEntryRepository.save(entry);
                }
            }
        }
    }

    // Auto-generate entries for current month
    @Transactional
    public void autoGenerateCurrentMonth() {
        LocalDate now = LocalDate.now();
        generateMonthlyEntries(now.getYear(), now.getMonthValue());
    }

    // Get expenses due in the next N days
    public List<RecurringExpense> getExpensesDueSoon(int days) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(days);
        return recurringExpenseRepository.findDueExpenses(startDate, endDate);
    }

    // Get total monthly budget (sum of all active recurring expenses)
    public BigDecimal getMonthlyBudget() {
        List<RecurringExpense> activeExpenses = getActiveRecurringExpenses();
        return activeExpenses.stream()
                .filter(expense -> expense.getFrequency() == ExpenseFrequency.MONTHLY)
                .map(RecurringExpense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Get total expenses for a specific month/year
    public BigDecimal getTotalExpensesForMonth(int year, int month) {
        return monthlyExpenseEntryRepository.getTotalExpensesForMonth(year, month);
    }

    // Get breakdown by category for a month
    public List<MonthlyExpenseEntry> getMonthlyExpensesByCategory(int year, int month) {
        return monthlyExpenseEntryRepository.findByYearAndMonth(year, month);
    }
}