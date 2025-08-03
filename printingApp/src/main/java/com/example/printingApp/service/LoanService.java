package com.example.printingApp.service;

import com.example.printingApp.model.Loan;
import com.example.printingApp.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class LoanService {

    @Autowired
    private LoanRepository loanRepository;

    // Basic CRUD operations only
    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    public Optional<Loan> getLoanById(Long id) {
        return loanRepository.findById(id);
    }

    public List<Loan> getLoansByUserId(Long userId) {
        return loanRepository.getUserLoans(userId);
    }

    public List<Loan> getLoansByStatus(Loan.LoanStatus status) {
        return loanRepository.findByStatus(status);
    }

    @Transactional
    public Loan createLoan(Loan loan) {
        return loanRepository.save(loan);
    }

    @Transactional
    public Loan updateLoan(Long id, Loan loan) {
        return getLoanById(id)
                .map(existingLoan -> {
                    loan.setId(id);
                    loan.setCreatedAt(existingLoan.getCreatedAt());
                    return loanRepository.save(loan);
                })
                .orElseThrow(() -> new RuntimeException("Loan not found with id: " + id));
    }

    @Transactional
    public void deleteLoan(Long id) {
        loanRepository.deleteById(id);
    }
}