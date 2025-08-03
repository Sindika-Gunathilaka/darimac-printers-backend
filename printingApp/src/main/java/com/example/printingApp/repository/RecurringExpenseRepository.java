package com.example.printingApp.repository;

import com.example.printingApp.model.RecurringExpense;
import com.example.printingApp.model.RecurringExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface RecurringExpenseRepository extends JpaRepository<RecurringExpense, Long> {

    // Find active recurring expenses
    List<RecurringExpense> findByIsActiveTrueOrderByNextDueDateAsc();

    // Find by category
    List<RecurringExpense> findByCategory(RecurringExpenseCategory category);

    // Find by category and active status
    List<RecurringExpense> findByCategoryAndIsActiveTrue(RecurringExpenseCategory category);

    // Find expenses due within a date range
    @Query("SELECT re FROM RecurringExpense re WHERE re.isActive = true AND re.nextDueDate BETWEEN :startDate AND :endDate")
    List<RecurringExpense> findDueExpenses(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Find expenses with auto-generation enabled that are due
    @Query("SELECT re FROM RecurringExpense re WHERE re.isActive = true AND re.autoGenerate = true AND re.nextDueDate <= :currentDate")
    List<RecurringExpense> findAutoGenerationDue(@Param("currentDate") LocalDate currentDate);

    // Find expenses ending soon (for notifications)
    @Query("SELECT re FROM RecurringExpense re WHERE re.isActive = true AND re.endDate IS NOT NULL AND re.endDate BETWEEN :startDate AND :endDate")
    List<RecurringExpense> findEndingSoon(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}