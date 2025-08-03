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
public class SublimationPrintService {

    @Autowired
    private SublimationPrintRepository sublimationPrintRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private SublimationPriceRepository sublimationPriceRepository;

    @Autowired
    private AuditLogService auditLogService;

    public List<SublimationPrint> getAllSublimationPrints() {
        return sublimationPrintRepository.findAll();
    }

    public Optional<SublimationPrint> getSublimationPrintById(Long id) {
        return sublimationPrintRepository.findById(id);
    }

    public List<SublimationPrint> getSublimationPrintsByCustomerId(Long customerId) {
        return sublimationPrintRepository.findByCustomerId(customerId);
    }

    public List<SublimationPrint> getSublimationPrintsBySublimationType(SublimationType sublimationType) {
        return sublimationPrintRepository.findBySublimationType(sublimationType);
    }

    @Transactional
    public SublimationPrint saveSublimationPrint(SublimationPrint sublimationPrint) {
        return saveSublimationPrint(sublimationPrint, null, null);
    }

    @Transactional
    public SublimationPrint saveSublimationPrint(SublimationPrint sublimationPrint, String userId, HttpServletRequest request) {
        boolean isUpdate = sublimationPrint.getId() != null;
        SublimationPrint oldEntity = null;
        
        if (isUpdate) {
            oldEntity = sublimationPrintRepository.findById(sublimationPrint.getId()).orElse(null);
        }
        // Ensure print type is set
        sublimationPrint.setPrintType(PrintJob.PrintType.SUBLIMATION);

        // If amountPaid is null, initialize it
        if (sublimationPrint.getAmountPaid() == null) {
            sublimationPrint.setAmountPaid(BigDecimal.ZERO);
        }

        // Handle customer relationship
        if (sublimationPrint.getCustomer() != null && sublimationPrint.getCustomer().getId() != null) {
            Customer customer = customerRepository.findById(sublimationPrint.getCustomer().getId())
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            sublimationPrint.setCustomer(customer);
        }

        // Validate sublimation type and unit price
        if (sublimationPrint.getSublimationType() != null && sublimationPrint.getUnitPrice() != null) {
            // Optionally validate that the unit price matches current master data
            Optional<SublimationPrice> masterPrice = sublimationPriceRepository
                    .findBySublimationTypeAndIsActiveTrue(sublimationPrint.getSublimationType());

            // Note: We don't enforce the master price, just use it as reference
            // The user can override the price if needed
        }

        // Handle expenses if provided from frontend
        if (sublimationPrint.getExpenses() != null) {
            // Set the sublimation print reference on each expense
            sublimationPrint.getExpenses().forEach(expense -> expense.setSublimationPrint(sublimationPrint));
        }

        // Initialize default values
        if (sublimationPrint.getQuantity() == null) {
            sublimationPrint.setQuantity(1);
        }
        if (sublimationPrint.getProfitPercentage() == null) {
            sublimationPrint.setProfitPercentage(20);
        }
        if (sublimationPrint.getOtherExpenses() == null) {
            sublimationPrint.setOtherExpenses(BigDecimal.ZERO);
        }

        SublimationPrint savedEntity = sublimationPrintRepository.save(sublimationPrint);

        // Log audit action
        AuditLog.AuditAction action = isUpdate ? AuditLog.AuditAction.UPDATE : AuditLog.AuditAction.CREATE;
        auditLogService.logAction("SublimationPrint", savedEntity.getId(), action, oldEntity, savedEntity, userId, request);

        return savedEntity;
    }

    @Transactional
    public void deleteSublimationPrint(Long id) {
        deleteSublimationPrint(id, null, null);
    }

    @Transactional
    public void deleteSublimationPrint(Long id, String userId, HttpServletRequest request) {
        SublimationPrint sublimationPrint = sublimationPrintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SublimationPrint not found"));
        
        sublimationPrintRepository.deleteById(id);
        
        // Log audit action
        auditLogService.logAction("SublimationPrint", id, AuditLog.AuditAction.DELETE, sublimationPrint, null, userId, request);
    }

    @Transactional
    public SublimationPrint updateSublimationPrint(Long id, SublimationPrint sublimationPrint) {
        return updateSublimationPrint(id, sublimationPrint, null, null);
    }

    @Transactional
    public SublimationPrint updateSublimationPrint(Long id, SublimationPrint sublimationPrint, String userId, HttpServletRequest request) {
        return getSublimationPrintById(id)
                .map(existingPrint -> {
                    sublimationPrint.setId(id);
                    sublimationPrint.setCreatedAt(existingPrint.getCreatedAt()); // Preserve creation date
                    return saveSublimationPrint(sublimationPrint, userId, request);
                })
                .orElseThrow(() -> new RuntimeException("Sublimation print not found with id: " + id));
    }

    // Get current unit price for a sublimation type
    public BigDecimal getCurrentUnitPrice(SublimationType sublimationType) {
        return sublimationPriceRepository.findBySublimationTypeAndIsActiveTrue(sublimationType)
                .map(SublimationPrice::getUnitPrice)
                .orElse(BigDecimal.ZERO);
    }
}