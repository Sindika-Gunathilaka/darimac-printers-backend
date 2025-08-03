package com.example.printingApp.controller;

import com.example.printingApp.model.*;
import com.example.printingApp.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/print-jobs")
public class PrintJobController {
    @Autowired
    private PrintJobService printJobService;

    @GetMapping
    public List<PrintJob> getAllPrintJobs() {
        List<PrintJob> printJobs = printJobService.getAllPrintJobs();

        // Set customer name for each job
        printJobs.forEach(job -> {
            if (job.getCustomer() != null) {
                job.setCustomerName(job.getCustomer().getName());
            }
        });

        return printJobs;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrintJob> getPrintJobById(@PathVariable Long id) {
        return printJobService.getPrintJobById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}")
    public List<PrintJob> getPrintJobsByCustomerId(@PathVariable Long customerId) {
        return printJobService.getPrintJobsByCustomerId(customerId);
    }

    @GetMapping("/status/{status}")
    public List<PrintJob> getPrintJobsByStatus(@PathVariable PrintJob.PaymentStatus status) {
        return printJobService.getPrintJobsByPaymentStatus(status);
    }

    @PostMapping("/{id}/payments")
    public ResponseEntity<Payment> recordPayment(@PathVariable Long id, @RequestBody Payment payment, HttpServletRequest request) {
        try {
            String userId = getCurrentUserId();
            Payment savedPayment = printJobService.recordPayment(id, payment, userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedPayment);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() instanceof Long) {
            return String.valueOf(authentication.getDetails());
        }
        return authentication != null ? authentication.getName() : "anonymous";
    }
}