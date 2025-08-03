package com.example.printingApp.controller;

import com.example.printingApp.model.OtherPrint;
import com.example.printingApp.service.OtherPrintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/other-prints")
public class OtherPrintController {

    @Autowired
    private OtherPrintService otherPrintService;

    @GetMapping
    public List<OtherPrint> getAllOtherPrints() {
        return otherPrintService.getAllOtherPrints();
    }

    @GetMapping("/{id}")
    public ResponseEntity<OtherPrint> getOtherPrintById(@PathVariable Long id) {
        return otherPrintService.getOtherPrintById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public List<OtherPrint> getOtherPrintsByStatus(@PathVariable OtherPrint.PaymentStatus status) {
        return otherPrintService.getOtherPrintsByStatus(status);
    }

    @GetMapping("/date-range")
    public List<OtherPrint> getOtherPrintsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return otherPrintService.getOtherPrintsByDateRange(startDate, endDate);
    }

    @GetMapping("/search")
    public List<OtherPrint> searchByCustomerRemark(@RequestParam String customerRemark) {
        return otherPrintService.searchByCustomerRemark(customerRemark);
    }

    @PostMapping
    public ResponseEntity<OtherPrint> createOtherPrint(@RequestBody OtherPrint otherPrint, HttpServletRequest request) {
        try {
            String userId = getCurrentUserId();
            OtherPrint savedPrint = otherPrintService.createOtherPrint(otherPrint, userId, request);
            return ResponseEntity.ok(savedPrint);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<OtherPrint> updateOtherPrint(@PathVariable Long id, @RequestBody OtherPrint otherPrint, HttpServletRequest request) {
        try {
            String userId = getCurrentUserId();
            OtherPrint updatedPrint = otherPrintService.updateOtherPrint(id, otherPrint, userId, request);
            return ResponseEntity.ok(updatedPrint);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOtherPrint(@PathVariable Long id, HttpServletRequest request) {
        try {
            String userId = getCurrentUserId();
            otherPrintService.deleteOtherPrint(id, userId, request);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/payment")
    public ResponseEntity<OtherPrint> updatePayment(@PathVariable Long id, @RequestParam BigDecimal amountPaid, HttpServletRequest request) {
        try {
            String userId = getCurrentUserId();
            OtherPrint updatedPrint = otherPrintService.updatePayment(id, amountPaid, userId, request);
            return ResponseEntity.ok(updatedPrint);
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

    @GetMapping("/summary")
    public Map<String, Object> getSummaryStatistics() {
        return otherPrintService.getSummaryStatistics();
    }

    @GetMapping("/payment-statuses")
    public List<OtherPrint.PaymentStatus> getPaymentStatuses() {
        return Arrays.asList(otherPrintService.getPaymentStatuses());
    }
}
