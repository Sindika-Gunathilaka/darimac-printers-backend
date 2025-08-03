package com.example.printingApp.service;

import com.example.printingApp.model.MonthlyExpenseEntry;
import com.example.printingApp.repository.MonthlyExpenseEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MonthlyExpenseEntryService {

    @Autowired
    private MonthlyExpenseEntryRepository monthlyExpenseEntryRepository;

    public List<MonthlyExpenseEntry> getAllEntries() {
        return monthlyExpenseEntryRepository.findAll();
    }

    public Optional<MonthlyExpenseEntry> getEntryById(Long id) {
        return monthlyExpenseEntryRepository.findById(id);
    }

    public List<MonthlyExpenseEntry> getEntriesForMonth(int year, int month) {
        return monthlyExpenseEntryRepository.findByYearAndMonth(year, month);
    }

    public List<MonthlyExpenseEntry> getUnpaidEntries() {
        return monthlyExpenseEntryRepository.findByPaymentStatus(MonthlyExpenseEntry.PaymentStatus.UNPAID);
    }

    public List<MonthlyExpenseEntry> getOverdueEntries() {
        return monthlyExpenseEntryRepository.findOverdueEntries();
    }

    @Transactional
    public MonthlyExpenseEntry saveEntry(MonthlyExpenseEntry entry) {
        return monthlyExpenseEntryRepository.save(entry);
    }

    @Transactional
    public MonthlyExpenseEntry updateEntry(Long id, MonthlyExpenseEntry entry) {
        return getEntryById(id)
                .map(existingEntry -> {
                    entry.setId(id);
                    entry.setCreatedAt(existingEntry.getCreatedAt());
                    return monthlyExpenseEntryRepository.save(entry);
                })
                .orElseThrow(() -> new RuntimeException("Monthly expense entry not found with id: " + id));
    }

    @Transactional
    public void deleteEntry(Long id) {
        monthlyExpenseEntryRepository.deleteById(id);
    }

    @Transactional
    public MonthlyExpenseEntry markAsPaid(Long id, LocalDate paymentDate) {
        return getEntryById(id)
                .map(entry -> {
                    entry.setPaymentStatus(MonthlyExpenseEntry.PaymentStatus.PAID);
                    entry.setPaymentDate(paymentDate);
                    return monthlyExpenseEntryRepository.save(entry);
                })
                .orElseThrow(() -> new RuntimeException("Monthly expense entry not found with id: " + id));
    }

    @Transactional
    public MonthlyExpenseEntry markAsUnpaid(Long id) {
        return getEntryById(id)
                .map(entry -> {
                    entry.setPaymentStatus(MonthlyExpenseEntry.PaymentStatus.UNPAID);
                    entry.setPaymentDate(null);
                    return monthlyExpenseEntryRepository.save(entry);
                })
                .orElseThrow(() -> new RuntimeException("Monthly expense entry not found with id: " + id));
    }

    // Financial summary methods
    public BigDecimal getTotalExpensesForMonth(int year, int month) {
        return monthlyExpenseEntryRepository.getTotalExpensesForMonth(year, month);
    }

    public BigDecimal getTotalPaidExpensesForMonth(int year, int month) {
        return monthlyExpenseEntryRepository.getTotalPaidExpensesForMonth(year, month);
    }

    public BigDecimal getTotalUnpaidExpensesForMonth(int year, int month) {
        return monthlyExpenseEntryRepository.getTotalUnpaidExpensesForMonth(year, month);
    }

    // Get entries for a year range (for reporting)
    public List<MonthlyExpenseEntry> getEntriesForYearRange(int startYear, int endYear) {
        return monthlyExpenseEntryRepository.findByYearBetweenOrderByYearDescMonthDesc(startYear, endYear);
    }
}