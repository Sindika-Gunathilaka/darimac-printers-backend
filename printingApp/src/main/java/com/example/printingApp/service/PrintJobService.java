package com.example.printingApp.service;

import com.example.printingApp.model.*;
import com.example.printingApp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class PrintJobService {
    @Autowired
    private PrintJobRepository printJobRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private AuditLogService auditLogService;

    public List<PrintJob> getAllPrintJobs() {
        return printJobRepository.findAll();
    }

    public Optional<PrintJob> getPrintJobById(Long id) {
        return printJobRepository.findById(id);
    }

    public List<PrintJob> getPrintJobsByCustomerId(Long customerId) {
        return printJobRepository.findByCustomerId(customerId);
    }

    public List<PrintJob> getPrintJobsByPaymentStatus(PrintJob.PaymentStatus status) {
        return printJobRepository.findByPaymentStatus(status);
    }

    @Transactional
    public Payment recordPayment(Long printJobId, Payment payment) {
        return recordPayment(printJobId, payment, null, null);
    }

    @Transactional
    public Payment recordPayment(Long printJobId, Payment payment, String userId, HttpServletRequest request) {
        PrintJob printJob = printJobRepository.findById(printJobId)
                .orElseThrow(() -> new RuntimeException("Print job not found"));

        payment.setPrintJob(printJob);
        Payment savedPayment = paymentRepository.save(payment);

        // Update the total paid amount
        BigDecimal totalPaid = paymentRepository.findByPrintJobId(printJobId)
                .stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        printJob.setAmountPaid(totalPaid);
        printJob.updatePaymentStatus();
        printJobRepository.save(printJob);

        // Log audit action
        String entityType = printJob.getClass().getSimpleName();
        auditLogService.logAction(entityType, printJobId, AuditLog.AuditAction.PAYMENT_RECORDED, null, savedPayment, userId, request);

        return savedPayment;
    }
}

