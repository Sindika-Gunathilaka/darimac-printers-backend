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
public class DigitalPrintService {
    @Autowired
    private DigitalPrintRepository digitalPrintRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private AuditLogService auditLogService;

    public List<DigitalPrint> getAllDigitalPrints() {
        return digitalPrintRepository.findAll();
    }

    public Optional<DigitalPrint> getDigitalPrintById(Long id) {
        return digitalPrintRepository.findById(id);
    }

    public List<DigitalPrint> getDigitalPrintsByCustomerId(Long customerId) {
        return digitalPrintRepository.findByCustomerId(customerId);
    }

    @Transactional
    public DigitalPrint saveDigitalPrint(DigitalPrint digitalPrint) {
        return saveDigitalPrint(digitalPrint, null, null);
    }

    @Transactional
    public DigitalPrint saveDigitalPrint(DigitalPrint digitalPrint, String userId, HttpServletRequest request) {
        boolean isUpdate = digitalPrint.getId() != null;
        DigitalPrint oldEntity = null;
        
        if (isUpdate) {
            oldEntity = digitalPrintRepository.findById(digitalPrint.getId()).orElse(null);
        }

        // Ensure print type is set
        digitalPrint.setPrintType(PrintJob.PrintType.DIGITAL);

        // If amountPaid is null, initialize it
        if (digitalPrint.getAmountPaid() == null) {
            digitalPrint.setAmountPaid(BigDecimal.ZERO);
        }

        // Handle customer relationship
        if (digitalPrint.getCustomer() != null && digitalPrint.getCustomer().getId() != null) {
            Customer customer = customerRepository.findById(digitalPrint.getCustomer().getId())
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            digitalPrint.setCustomer(customer);
        }

        // Handle expenses if provided from frontend
        if (digitalPrint.getExpenses() != null) {
            // Set the digital print reference on each expense
            digitalPrint.getExpenses().forEach(expense -> expense.setDigitalPrint(digitalPrint));
        }

        DigitalPrint savedEntity = digitalPrintRepository.save(digitalPrint);

        // Log audit action
        AuditLog.AuditAction action = isUpdate ? AuditLog.AuditAction.UPDATE : AuditLog.AuditAction.CREATE;
        auditLogService.logAction("DigitalPrint", savedEntity.getId(), action, oldEntity, savedEntity, userId, request);

        return savedEntity;
    }

    @Transactional
    public void deleteDigitalPrint(Long id, String userId, HttpServletRequest request) {
        DigitalPrint digitalPrint = digitalPrintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("DigitalPrint not found"));
        
        digitalPrintRepository.deleteById(id);
        
        // Log audit action
        auditLogService.logAction("DigitalPrint", id, AuditLog.AuditAction.DELETE, digitalPrint, null, userId, request);
    }
}
