package com.example.printingApp.repository;

import com.example.printingApp.model.Loan;
import com.example.printingApp.model.LoanPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanPaymentRepository extends JpaRepository<LoanPayment, Long> {

    // Only basic Spring Data JPA methods (no custom queries initially)
    List<LoanPayment> findByLoanOrderByPaymentNumberAsc(Loan loan);
    List<LoanPayment> findByLoanIdOrderByPaymentNumberAsc(Long loanId);
    List<LoanPayment> findByPaymentStatusOrderByDueDateAsc(LoanPayment.PaymentStatus status);
    List<LoanPayment> findByLoanAndPaymentStatus(Loan loan, LoanPayment.PaymentStatus status);
    Optional<LoanPayment> findByTransactionReference(String transactionReference);

    // ONE simple custom query to test
    @Query("SELECT p FROM LoanPayment p JOIN p.loan l WHERE l.user.id = :userId")
    List<LoanPayment> getUserPayments(@Param("userId") Long userId);
}