package com.example.printingApp.controller;

import com.example.printingApp.model.LoanPayment;
import com.example.printingApp.service.LoanPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/loan-payments")
public class LoanPaymentController {

    @Autowired
    private LoanPaymentService loanPaymentService;

    // Basic endpoints only
    @GetMapping
    public ResponseEntity<List<LoanPayment>> getAllPayments() {
        List<LoanPayment> payments = loanPaymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoanPayment> getPaymentById(@PathVariable Long id) {
        return loanPaymentService.getPaymentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/loan/{loanId}")
    public ResponseEntity<List<LoanPayment>> getLoanPayments(@PathVariable Long loanId) {
        List<LoanPayment> payments = loanPaymentService.getPaymentsByLoanId(loanId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LoanPayment>> getUserPayments(@PathVariable Long userId) {
        List<LoanPayment> payments = loanPaymentService.getPaymentsByUserId(userId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/payment-methods")
    public ResponseEntity<List<LoanPayment.PaymentMethod>> getPaymentMethods() {
        return ResponseEntity.ok(Arrays.asList(LoanPayment.PaymentMethod.values()));
    }

    @GetMapping("/payment-statuses")
    public ResponseEntity<List<LoanPayment.PaymentStatus>> getPaymentStatuses() {
        return ResponseEntity.ok(Arrays.asList(LoanPayment.PaymentStatus.values()));
    }

    @PostMapping
    public ResponseEntity<LoanPayment> createPayment(@RequestBody LoanPayment payment) {
        try {
            LoanPayment savedPayment = loanPaymentService.createPayment(payment);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedPayment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<LoanPayment> updatePayment(@PathVariable Long id, @RequestBody LoanPayment payment) {
        try {
            LoanPayment updatedPayment = loanPaymentService.updatePayment(id, payment);
            return ResponseEntity.ok(updatedPayment);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        try {
            loanPaymentService.deletePayment(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/mark-paid")
    public ResponseEntity<LoanPayment> markPaymentAsPaid(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate paymentDate,
            @RequestParam(required = false) LoanPayment.PaymentMethod paymentMethod,
            @RequestParam(required = false) String transactionReference) {
        try {
            LocalDate actualPaymentDate = paymentDate != null ? paymentDate : LocalDate.now();
            LoanPayment updatedPayment = loanPaymentService.markAsPaid(id, actualPaymentDate, paymentMethod, transactionReference);
            return ResponseEntity.ok(updatedPayment);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}