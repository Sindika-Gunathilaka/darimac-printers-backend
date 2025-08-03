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
public class OffsetPrintService {
    @Autowired
    private OffsetPrintRepository offsetPrintRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private AuditLogService auditLogService;

    public List<OffsetPrint> getAllOffsetPrints() {
        return offsetPrintRepository.findAll();
    }

    public Optional<OffsetPrint> getOffsetPrintById(Long id) {
        return offsetPrintRepository.findById(id);
    }

    public List<OffsetPrint> getOffsetPrintsByCustomerId(Long customerId) {
        return offsetPrintRepository.findByCustomerId(customerId);
    }

    @Transactional
    public OffsetPrint saveOffsetPrint(OffsetPrint offsetPrint) {
        return saveOffsetPrint(offsetPrint, null, null);
    }

    @Transactional
    public OffsetPrint saveOffsetPrint(OffsetPrint offsetPrint, String userId, HttpServletRequest request) {
        boolean isUpdate = offsetPrint.getId() != null;
        OffsetPrint oldEntity = null;
        
        if (isUpdate) {
            oldEntity = offsetPrintRepository.findById(offsetPrint.getId()).orElse(null);
        }

        // Ensure print type is set
        offsetPrint.setPrintType(PrintJob.PrintType.OFFSET);

        // If amountPaid is null, initialize it
        if (offsetPrint.getAmountPaid() == null) {
            offsetPrint.setAmountPaid(BigDecimal.ZERO);
        }

        // Handle customer relationship
        if (offsetPrint.getCustomer() != null && offsetPrint.getCustomer().getId() != null) {
            Customer customer = customerRepository.findById(offsetPrint.getCustomer().getId())
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            offsetPrint.setCustomer(customer);
        }

        // Handle supplier relationship
        if (offsetPrint.getSupplier() != null && offsetPrint.getSupplier().getId() != null) {
            Supplier supplier = supplierRepository.findById(offsetPrint.getSupplier().getId())
                    .orElseThrow(() -> new RuntimeException("Supplier not found"));
            offsetPrint.setSupplier(supplier);
        }

        // Handle expenses if provided from frontend
        if (offsetPrint.getExpenses() != null) {
            // Set the offset print reference on each expense
            offsetPrint.getExpenses().forEach(expense -> {
                expense.setOffsetPrint(offsetPrint);
                expense.setDigitalPrint(null); // Ensure it's not linked to a digital print
            });
        }

        OffsetPrint savedEntity = offsetPrintRepository.save(offsetPrint);

        // Log audit action
        AuditLog.AuditAction action = isUpdate ? AuditLog.AuditAction.UPDATE : AuditLog.AuditAction.CREATE;
        auditLogService.logAction("OffsetPrint", savedEntity.getId(), action, oldEntity, savedEntity, userId, request);

        return savedEntity;
    }

    @Transactional
    public void deleteOffsetPrint(Long id, String userId, HttpServletRequest request) {
        OffsetPrint offsetPrint = offsetPrintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OffsetPrint not found"));
        
        offsetPrintRepository.deleteById(id);
        
        // Log audit action
        auditLogService.logAction("OffsetPrint", id, AuditLog.AuditAction.DELETE, offsetPrint, null, userId, request);
    }
}