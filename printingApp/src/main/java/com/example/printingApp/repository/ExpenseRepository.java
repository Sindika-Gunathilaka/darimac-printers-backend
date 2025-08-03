package com.example.printingApp.repository;

import com.example.printingApp.model.Expense;
import com.example.printingApp.model.Expense.PaymentStatus;
import com.example.printingApp.model.Expense.ExpenseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findBySupplierId(Long supplierId);

    List<Expense> findByPaymentStatus(PaymentStatus paymentStatus);

    List<Expense> findByExpenseType(ExpenseType expenseType);

    List<Expense> findByExpenseDateBetween(LocalDate startDate, LocalDate endDate);

    List<Expense> findByGrnNumber(String grnNumber);

    @Query("SELECT e FROM Expense e WHERE " +
            "(:grnNumber IS NULL OR e.grnNumber LIKE %:grnNumber%) AND " +
            "(:supplierName IS NULL OR e.supplier.name LIKE %:supplierName%) AND " +
            "(:expenseType IS NULL OR e.expenseType = :expenseType) AND " +
            "(:paymentStatus IS NULL OR e.paymentStatus = :paymentStatus) AND " +
            "(:startDate IS NULL OR e.expenseDate >= :startDate) AND " +
            "(:endDate IS NULL OR e.expenseDate <= :endDate)")
    List<Expense> searchExpenses(
            @Param("grnNumber") String grnNumber,
            @Param("supplierName") String supplierName,
            @Param("expenseType") ExpenseType expenseType,
            @Param("paymentStatus") PaymentStatus paymentStatus,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
