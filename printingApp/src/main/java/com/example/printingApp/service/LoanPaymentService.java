package com.example.printingApp.service;

import com.example.printingApp.model.Loan;
import com.example.printingApp.model.LoanPayment;
import com.example.printingApp.repository.LoanPaymentRepository;
import com.example.printingApp.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class LoanPaymentService {

    @Autowired
    private LoanPaymentRepository loanPaymentRepository;

    @Autowired
    private LoanRepository loanRepository;

    // Basic CRUD operations only
    public List<LoanPayment> getAllPayments() {
        return loanPaymentRepository.findAll();
    }

    public Optional<LoanPayment> getPaymentById(Long id) {
        return loanPaymentRepository.findById(id);
    }

    public List<LoanPayment> getPaymentsByLoanId(Long loanId) {
        return loanPaymentRepository.findByLoanIdOrderByPaymentNumberAsc(loanId);
    }

    public List<LoanPayment> getPaymentsByUserId(Long userId) {
        return loanPaymentRepository.getUserPayments(userId);
    }

    @Transactional
    public LoanPayment createPayment(LoanPayment payment) {
        // Handle nested loan object from frontend
        if (payment.getLoan() != null && payment.getLoan().getId() != null) {
            Loan fullLoan = loanRepository.findById(payment.getLoan().getId())
                    .orElseThrow(() -> new RuntimeException("Loan not found with id: " + payment.getLoan().getId()));
            payment.setLoan(fullLoan);
        } else {
            throw new RuntimeException("Loan information is required");
        }

        return loanPaymentRepository.save(payment);
    }

    @Transactional
    public LoanPayment updatePayment(Long id, LoanPayment payment) {
        return getPaymentById(id)
                .map(existingPayment -> {
                    payment.setId(id);
                    payment.setCreatedAt(existingPayment.getCreatedAt());
                    return loanPaymentRepository.save(payment);
                })
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
    }

    @Transactional
    public void deletePayment(Long id) {
        loanPaymentRepository.deleteById(id);
    }

    @Transactional
    public LoanPayment markAsPaid(Long id, LocalDate paymentDate,
                                  LoanPayment.PaymentMethod paymentMethod,
                                  String transactionReference) {
        return getPaymentById(id)
                .map(payment -> {
                    payment.markAsPaid(paymentDate, paymentMethod, transactionReference);
                    return loanPaymentRepository.save(payment);
                })
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
    }
}