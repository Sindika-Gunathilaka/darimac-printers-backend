package com.example.printingApp.repository;

import com.example.printingApp.model.MonthlyExpenseEntry;
import com.example.printingApp.model.RecurringExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlyExpenseEntryRepository extends JpaRepository<MonthlyExpenseEntry, Long> {

    // Find entries for a specific month and year
    List<MonthlyExpenseEntry> findByYearAndMonth(Integer year, Integer month);

    // Find entries for a specific recurring expense
    List<MonthlyExpenseEntry> findByRecurringExpenseOrderByYearDescMonthDesc(RecurringExpense recurringExpense);

    // Find specific entry for recurring expense and month/year
    Optional<MonthlyExpenseEntry> findByRecurringExpenseAndYearAndMonth(RecurringExpense recurringExpense, Integer year, Integer month);

    // Find entries by payment status
    List<MonthlyExpenseEntry> findByPaymentStatus(MonthlyExpenseEntry.PaymentStatus paymentStatus);

    // Find unpaid entries for a specific month/year
    List<MonthlyExpenseEntry> findByYearAndMonthAndPaymentStatus(Integer year, Integer month, MonthlyExpenseEntry.PaymentStatus paymentStatus);

    // Calculate total expenses for a month/year
    @Query("SELECT COALESCE(SUM(mee.amount), 0) FROM MonthlyExpenseEntry mee WHERE mee.year = :year AND mee.month = :month")
    BigDecimal getTotalExpensesForMonth(@Param("year") Integer year, @Param("month") Integer month);

    // Calculate total paid expenses for a month/year
    @Query("SELECT COALESCE(SUM(mee.amount), 0) FROM MonthlyExpenseEntry mee WHERE mee.year = :year AND mee.month = :month AND mee.paymentStatus = 'PAID'")
    BigDecimal getTotalPaidExpensesForMonth(@Param("year") Integer year, @Param("month") Integer month);

    // Calculate total unpaid expenses for a month/year
    @Query("SELECT COALESCE(SUM(mee.amount), 0) FROM MonthlyExpenseEntry mee WHERE mee.year = :year AND mee.month = :month AND mee.paymentStatus IN ('UNPAID', 'OVERDUE')")
    BigDecimal getTotalUnpaidExpensesForMonth(@Param("year") Integer year, @Param("month") Integer month);

    // Find entries by year range
    List<MonthlyExpenseEntry> findByYearBetweenOrderByYearDescMonthDesc(Integer startYear, Integer endYear);

    // Find overdue entries
    @Query("SELECT mee FROM MonthlyExpenseEntry mee WHERE mee.paymentStatus = 'OVERDUE' ORDER BY mee.dueDate ASC")
    List<MonthlyExpenseEntry> findOverdueEntries();
}