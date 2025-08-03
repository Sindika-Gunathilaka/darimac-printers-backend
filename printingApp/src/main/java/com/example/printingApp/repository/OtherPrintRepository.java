package com.example.printingApp.repository;


import com.example.printingApp.model.OtherPrint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface OtherPrintRepository extends JpaRepository<OtherPrint, Long> {

    // Find by payment status
    List<OtherPrint> findByPaymentStatusOrderByPrintDateDesc(OtherPrint.PaymentStatus paymentStatus);

    // Find by date range
    List<OtherPrint> findByPrintDateBetweenOrderByPrintDateDesc(LocalDate startDate, LocalDate endDate);

    // Find by customer remark (search)
    List<OtherPrint> findByCustomerRemarkContainingIgnoreCaseOrderByPrintDateDesc(String customerRemark);

    // All ordered by date
    List<OtherPrint> findAllByOrderByPrintDateDesc();

    // Summary queries
    @Query("SELECT COUNT(o) FROM OtherPrint o")
    Long getTotalCount();

    @Query("SELECT COUNT(o) FROM OtherPrint o WHERE o.paymentStatus = :status")
    Long getCountByStatus(@Param("status") OtherPrint.PaymentStatus status);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM OtherPrint o")
    BigDecimal getTotalRevenue();

    @Query("SELECT COALESCE(SUM(o.totalCost), 0) FROM OtherPrint o")
    BigDecimal getTotalCost();

    @Query("SELECT COALESCE(SUM(o.amountPaid), 0) FROM OtherPrint o")
    BigDecimal getTotalAmountPaid();

    @Query("SELECT COALESCE(SUM(o.balance), 0) FROM OtherPrint o WHERE o.paymentStatus != 'PAID'")
    BigDecimal getTotalOutstanding();
}

