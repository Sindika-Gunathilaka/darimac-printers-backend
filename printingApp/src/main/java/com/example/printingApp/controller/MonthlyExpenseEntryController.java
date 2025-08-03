package com.example.printingApp.controller;

import com.example.printingApp.model.MonthlyExpenseEntry;
import com.example.printingApp.service.MonthlyExpenseEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/monthly-expense-entries")
public class MonthlyExpenseEntryController {

    @Autowired
    private MonthlyExpenseEntryService monthlyExpenseEntryService;

    @GetMapping
    public ResponseEntity<List<MonthlyExpenseEntry>> getAllEntries() {
        List<MonthlyExpenseEntry> entries = monthlyExpenseEntryService.getAllEntries();
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MonthlyExpenseEntry> getEntryById(@PathVariable Long id) {
        return monthlyExpenseEntryService.getEntryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/month/{year}/{month}")
    public ResponseEntity<List<MonthlyExpenseEntry>> getEntriesForMonth(@PathVariable int year, @PathVariable int month) {
        List<MonthlyExpenseEntry> entries = monthlyExpenseEntryService.getEntriesForMonth(year, month);
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/unpaid")
    public ResponseEntity<List<MonthlyExpenseEntry>> getUnpaidEntries() {
        List<MonthlyExpenseEntry> entries = monthlyExpenseEntryService.getUnpaidEntries();
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<MonthlyExpenseEntry>> getOverdueEntries() {
        List<MonthlyExpenseEntry> entries = monthlyExpenseEntryService.getOverdueEntries();
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/total/{year}/{month}")
    public ResponseEntity<BigDecimal> getTotalExpensesForMonth(@PathVariable int year, @PathVariable int month) {
        BigDecimal total = monthlyExpenseEntryService.getTotalExpensesForMonth(year, month);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/paid-total/{year}/{month}")
    public ResponseEntity<BigDecimal> getTotalPaidExpensesForMonth(@PathVariable int year, @PathVariable int month) {
        BigDecimal total = monthlyExpenseEntryService.getTotalPaidExpensesForMonth(year, month);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/unpaid-total/{year}/{month}")
    public ResponseEntity<BigDecimal> getTotalUnpaidExpensesForMonth(@PathVariable int year, @PathVariable int month) {
        BigDecimal total = monthlyExpenseEntryService.getTotalUnpaidExpensesForMonth(year, month);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/year-range/{startYear}/{endYear}")
    public ResponseEntity<List<MonthlyExpenseEntry>> getEntriesForYearRange(@PathVariable int startYear, @PathVariable int endYear) {
        List<MonthlyExpenseEntry> entries = monthlyExpenseEntryService.getEntriesForYearRange(startYear, endYear);
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/summary/{year}/{month}")
    public ResponseEntity<MonthlyExpenseSummary> getMonthlySummary(@PathVariable int year, @PathVariable int month) {
        try {
            BigDecimal totalExpenses = monthlyExpenseEntryService.getTotalExpensesForMonth(year, month);
            BigDecimal totalPaid = monthlyExpenseEntryService.getTotalPaidExpensesForMonth(year, month);
            BigDecimal totalUnpaid = monthlyExpenseEntryService.getTotalUnpaidExpensesForMonth(year, month);
            List<MonthlyExpenseEntry> entries = monthlyExpenseEntryService.getEntriesForMonth(year, month);

            MonthlyExpenseSummary summary = new MonthlyExpenseSummary(
                    year, month, totalExpenses, totalPaid, totalUnpaid, entries.size(),
                    (int) entries.stream().filter(e -> e.getPaymentStatus() == MonthlyExpenseEntry.PaymentStatus.PAID).count(),
                    (int) entries.stream().filter(e -> e.getPaymentStatus() == MonthlyExpenseEntry.PaymentStatus.UNPAID).count(),
                    (int) entries.stream().filter(e -> e.getPaymentStatus() == MonthlyExpenseEntry.PaymentStatus.OVERDUE).count()
            );

            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<MonthlyExpenseEntry> createEntry(@RequestBody MonthlyExpenseEntry entry) {
        try {
            MonthlyExpenseEntry savedEntry = monthlyExpenseEntryService.saveEntry(entry);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedEntry);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<MonthlyExpenseEntry> updateEntry(@PathVariable Long id, @RequestBody MonthlyExpenseEntry entry) {
        try {
            MonthlyExpenseEntry updatedEntry = monthlyExpenseEntryService.updateEntry(id, entry);
            return ResponseEntity.ok(updatedEntry);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntry(@PathVariable Long id) {
        try {
            monthlyExpenseEntryService.deleteEntry(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/mark-paid")
    public ResponseEntity<MonthlyExpenseEntry> markAsPaid(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate paymentDate) {
        try {
            LocalDate actualPaymentDate = paymentDate != null ? paymentDate : LocalDate.now();
            MonthlyExpenseEntry updatedEntry = monthlyExpenseEntryService.markAsPaid(id, actualPaymentDate);
            return ResponseEntity.ok(updatedEntry);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}/mark-unpaid")
    public ResponseEntity<MonthlyExpenseEntry> markAsUnpaid(@PathVariable Long id) {
        try {
            MonthlyExpenseEntry updatedEntry = monthlyExpenseEntryService.markAsUnpaid(id);
            return ResponseEntity.ok(updatedEntry);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // Inner class for summary response
    public static class MonthlyExpenseSummary {
        private int year;
        private int month;
        private BigDecimal totalExpenses;
        private BigDecimal totalPaid;
        private BigDecimal totalUnpaid;
        private int totalEntries;
        private int paidEntries;
        private int unpaidEntries;
        private int overdueEntries;

        public MonthlyExpenseSummary(int year, int month, BigDecimal totalExpenses,
                                     BigDecimal totalPaid, BigDecimal totalUnpaid,
                                     int totalEntries, int paidEntries, int unpaidEntries, int overdueEntries) {
            this.year = year;
            this.month = month;
            this.totalExpenses = totalExpenses;
            this.totalPaid = totalPaid;
            this.totalUnpaid = totalUnpaid;
            this.totalEntries = totalEntries;
            this.paidEntries = paidEntries;
            this.unpaidEntries = unpaidEntries;
            this.overdueEntries = overdueEntries;
        }

        // Getters
        public int getYear() { return year; }
        public int getMonth() { return month; }
        public BigDecimal getTotalExpenses() { return totalExpenses; }
        public BigDecimal getTotalPaid() { return totalPaid; }
        public BigDecimal getTotalUnpaid() { return totalUnpaid; }
        public int getTotalEntries() { return totalEntries; }
        public int getPaidEntries() { return paidEntries; }
        public int getUnpaidEntries() { return unpaidEntries; }
        public int getOverdueEntries() { return overdueEntries; }

        // Setters
        public void setYear(int year) { this.year = year; }
        public void setMonth(int month) { this.month = month; }
        public void setTotalExpenses(BigDecimal totalExpenses) { this.totalExpenses = totalExpenses; }
        public void setTotalPaid(BigDecimal totalPaid) { this.totalPaid = totalPaid; }
        public void setTotalUnpaid(BigDecimal totalUnpaid) { this.totalUnpaid = totalUnpaid; }
        public void setTotalEntries(int totalEntries) { this.totalEntries = totalEntries; }
        public void setPaidEntries(int paidEntries) { this.paidEntries = paidEntries; }
        public void setUnpaidEntries(int unpaidEntries) { this.unpaidEntries = unpaidEntries; }
        public void setOverdueEntries(int overdueEntries) { this.overdueEntries = overdueEntries; }
    }
}