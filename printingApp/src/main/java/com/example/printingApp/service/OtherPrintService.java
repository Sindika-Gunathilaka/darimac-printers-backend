package com.example.printingApp.service;

import com.example.printingApp.model.*;
import com.example.printingApp.repository.OtherPrintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@Service
public class OtherPrintService {

    @Autowired
    private OtherPrintRepository otherPrintRepository;

    @Autowired
    private AuditLogService auditLogService;

    // Basic CRUD operations
    public List<OtherPrint> getAllOtherPrints() {
        return otherPrintRepository.findAllByOrderByPrintDateDesc();
    }

    public Optional<OtherPrint> getOtherPrintById(Long id) {
        return otherPrintRepository.findById(id);
    }

    public List<OtherPrint> getOtherPrintsByStatus(OtherPrint.PaymentStatus status) {
        return otherPrintRepository.findByPaymentStatusOrderByPrintDateDesc(status);
    }

    public List<OtherPrint> getOtherPrintsByDateRange(LocalDate startDate, LocalDate endDate) {
        return otherPrintRepository.findByPrintDateBetweenOrderByPrintDateDesc(startDate, endDate);
    }

    public List<OtherPrint> searchByCustomerRemark(String customerRemark) {
        return otherPrintRepository.findByCustomerRemarkContainingIgnoreCaseOrderByPrintDateDesc(customerRemark);
    }

    @Transactional
    public OtherPrint createOtherPrint(OtherPrint otherPrint) {
        return createOtherPrint(otherPrint, null, null);
    }

    @Transactional
    public OtherPrint createOtherPrint(OtherPrint otherPrint, String userId, HttpServletRequest request) {
        OtherPrint savedEntity = otherPrintRepository.save(otherPrint);

        // Log audit action
        auditLogService.logAction("OtherPrint", savedEntity.getId(), AuditLog.AuditAction.CREATE, null, savedEntity, userId, request);

        return savedEntity;
    }

    @Transactional
    public OtherPrint updateOtherPrint(Long id, OtherPrint otherPrint) {
        return updateOtherPrint(id, otherPrint, null, null);
    }

    @Transactional
    public OtherPrint updateOtherPrint(Long id, OtherPrint otherPrint, String userId, HttpServletRequest request) {
        return getOtherPrintById(id)
                .map(existingPrint -> {
                    otherPrint.setId(id);
                    otherPrint.setCreatedAt(existingPrint.getCreatedAt());
                    OtherPrint savedEntity = otherPrintRepository.save(otherPrint);
                    
                    // Log audit action
                    auditLogService.logAction("OtherPrint", savedEntity.getId(), AuditLog.AuditAction.UPDATE, existingPrint, savedEntity, userId, request);
                    
                    return savedEntity;
                })
                .orElseThrow(() -> new RuntimeException("Other Print not found with id: " + id));
    }

    @Transactional
    public void deleteOtherPrint(Long id) {
        deleteOtherPrint(id, null, null);
    }

    @Transactional
    public void deleteOtherPrint(Long id, String userId, HttpServletRequest request) {
        OtherPrint otherPrint = otherPrintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OtherPrint not found"));
        
        otherPrintRepository.deleteById(id);
        
        // Log audit action
        auditLogService.logAction("OtherPrint", id, AuditLog.AuditAction.DELETE, otherPrint, null, userId, request);
    }

    @Transactional
    public OtherPrint updatePayment(Long id, BigDecimal amountPaid) {
        return updatePayment(id, amountPaid, null, null);
    }

    @Transactional
    public OtherPrint updatePayment(Long id, BigDecimal amountPaid, String userId, HttpServletRequest request) {
        return getOtherPrintById(id)
                .map(existingPrint -> {
                    OtherPrint otherPrint = existingPrint;
                    otherPrint.setAmountPaid(amountPaid);
                    OtherPrint savedEntity = otherPrintRepository.save(otherPrint);
                    
                    // Log audit action
                    auditLogService.logAction("OtherPrint", savedEntity.getId(), AuditLog.AuditAction.UPDATE, existingPrint, savedEntity, userId, request);
                    
                    return savedEntity;
                })
                .orElseThrow(() -> new RuntimeException("Other Print not found with id: " + id));
    }

    // Summary/Statistics methods
    public Map<String, Object> getSummaryStatistics() {
        Map<String, Object> summary = new HashMap<>();

        summary.put("totalCount", otherPrintRepository.getTotalCount());
        summary.put("paidCount", otherPrintRepository.getCountByStatus(OtherPrint.PaymentStatus.PAID));
        summary.put("unpaidCount", otherPrintRepository.getCountByStatus(OtherPrint.PaymentStatus.UNPAID));
        summary.put("partiallyPaidCount", otherPrintRepository.getCountByStatus(OtherPrint.PaymentStatus.PARTIALLY_PAID));
        summary.put("overdueCount", otherPrintRepository.getCountByStatus(OtherPrint.PaymentStatus.OVERDUE));

        summary.put("totalRevenue", otherPrintRepository.getTotalRevenue());
        summary.put("totalCost", otherPrintRepository.getTotalCost());
        summary.put("totalAmountPaid", otherPrintRepository.getTotalAmountPaid());
        summary.put("totalOutstanding", otherPrintRepository.getTotalOutstanding());

        BigDecimal totalRevenue = (BigDecimal) summary.get("totalRevenue");
        BigDecimal totalCost = (BigDecimal) summary.get("totalCost");
        summary.put("totalProfit", totalRevenue.subtract(totalCost));

        return summary;
    }

    // Get payment status options for frontend
    public OtherPrint.PaymentStatus[] getPaymentStatuses() {
        return OtherPrint.PaymentStatus.values();
    }
}
