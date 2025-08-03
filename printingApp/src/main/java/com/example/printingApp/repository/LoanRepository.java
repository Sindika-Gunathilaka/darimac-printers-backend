package com.example.printingApp.repository;

import com.example.printingApp.model.Loan;
import com.example.printingApp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    // Only basic Spring Data JPA methods
    List<Loan> findByUserOrderByCreatedAtDesc(User user);
    List<Loan> findByUserAndStatus(User user, Loan.LoanStatus status);
    List<Loan> findByStatus(Loan.LoanStatus status);
    List<Loan> findByLoanType(Loan.LoanType loanType);

    // ONE simple custom query to test
    @Query("SELECT l FROM Loan l WHERE l.user.id = :userId")
    List<Loan> getUserLoans(@Param("userId") Long userId);
}