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
public class DuploPrintService {
    @Autowired
    private DuploPrintRepository duploPrintRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AuditLogService auditLogService;

    public List<DuploPrint> getAllDuploPrints() {
        return duploPrintRepository.findAll();
    }

    public Optional<DuploPrint> getDuploPrintById(Long id) {
        return duploPrintRepository.findById(id);
    }

    public List<DuploPrint> getDuploPrintsByCustomerId(Long customerId) {
        return duploPrintRepository.findByCustomerId(customerId);
    }

    @Transactional
    public DuploPrint saveDuploPrint(DuploPrint duploPrint) {
        return saveDuploPrint(duploPrint, null, null);
    }

    @Transactional
    public DuploPrint saveDuploPrint(DuploPrint duploPrint, String userId, HttpServletRequest request) {
        boolean isUpdate = duploPrint.getId() != null;
        DuploPrint oldEntity = null;
        
        if (isUpdate) {
            oldEntity = duploPrintRepository.findById(duploPrint.getId()).orElse(null);
        }
        // Ensure print type is set
        duploPrint.setPrintType(PrintJob.PrintType.DUPLO);

        // If amountPaid is null, initialize it
        if (duploPrint.getAmountPaid() == null) {
            duploPrint.setAmountPaid(BigDecimal.ZERO);
        }

        // Handle customer relationship
        if (duploPrint.getCustomer() != null && duploPrint.getCustomer().getId() != null) {
            Customer customer = customerRepository.findById(duploPrint.getCustomer().getId())
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            duploPrint.setCustomer(customer);
        }

        // Handle expenses if provided from frontend
        if (duploPrint.getExpenses() != null) {
            // Set the duplo print reference on each expense
            duploPrint.getExpenses().forEach(expense -> expense.setDuploPrint(duploPrint));
        }

        // Initialize default values
        if (duploPrint.getCopies() == null) {
            duploPrint.setCopies(1);
        }
        if (duploPrint.getProfitPercentage() == null) {
            duploPrint.setProfitPercentage(20);
        }
        if (duploPrint.getOtherExpenses() == null) {
            duploPrint.setOtherExpenses(BigDecimal.ZERO);
        }

        DuploPrint savedEntity = duploPrintRepository.save(duploPrint);

        // Log audit action
        AuditLog.AuditAction action = isUpdate ? AuditLog.AuditAction.UPDATE : AuditLog.AuditAction.CREATE;
        auditLogService.logAction("DuploPrint", savedEntity.getId(), action, oldEntity, savedEntity, userId, request);

        return savedEntity;
    }

    @Transactional
    public void deleteDuploPrint(Long id) {
        deleteDuploPrint(id, null, null);
    }

    @Transactional
    public void deleteDuploPrint(Long id, String userId, HttpServletRequest request) {
        DuploPrint duploPrint = duploPrintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("DuploPrint not found"));
        
        duploPrintRepository.deleteById(id);
        
        // Log audit action
        auditLogService.logAction("DuploPrint", id, AuditLog.AuditAction.DELETE, duploPrint, null, userId, request);
    }

    @Transactional
    public DuploPrint updateDuploPrint(Long id, DuploPrint duploPrint) {
        return updateDuploPrint(id, duploPrint, null, null);
    }

    @Transactional
    public DuploPrint updateDuploPrint(Long id, DuploPrint duploPrint, String userId, HttpServletRequest request) {
        return getDuploPrintById(id)
                .map(existingPrint -> {
                    duploPrint.setId(id);
                    duploPrint.setCreatedAt(existingPrint.getCreatedAt()); // Preserve creation date
                    return saveDuploPrint(duploPrint, userId, request);
                })
                .orElseThrow(() -> new RuntimeException("Duplo print not found with id: " + id));
    }
}