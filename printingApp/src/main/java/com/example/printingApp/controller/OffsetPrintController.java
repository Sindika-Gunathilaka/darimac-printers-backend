package com.example.printingApp.controller;

import com.example.printingApp.model.OffsetPrint;
import com.example.printingApp.model.Supplier;
import com.example.printingApp.service.OffsetPrintService;
import com.example.printingApp.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/offset-prints")
public class OffsetPrintController {
    @Autowired
    private OffsetPrintService offsetPrintService;


    @GetMapping
    public List<Map<String, Object>> getAllOffsetPrints() {
        List<OffsetPrint> offsetPrints = offsetPrintService.getAllOffsetPrints();
        return offsetPrints.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OffsetPrint> getOffsetPrintById(@PathVariable Long id) {
        return offsetPrintService.getOffsetPrintById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}")
    public List<Map<String, Object>> getOffsetPrintsByCustomerId(@PathVariable Long customerId) {
        List<OffsetPrint> offsetPrints = offsetPrintService.getOffsetPrintsByCustomerId(customerId);
        return offsetPrints.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<OffsetPrint> createOffsetPrint(@RequestBody OffsetPrint offsetPrint, HttpServletRequest request) {
        String userId = getCurrentUserId();
        OffsetPrint saved = offsetPrintService.saveOffsetPrint(offsetPrint, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OffsetPrint> updateOffsetPrint(@PathVariable Long id, @RequestBody OffsetPrint offsetPrint, HttpServletRequest request) {
        return offsetPrintService.getOffsetPrintById(id)
                .map(existingPrint -> {
                    offsetPrint.setId(id);
                    String userId = getCurrentUserId();
                    return ResponseEntity.ok(offsetPrintService.saveOffsetPrint(offsetPrint, userId, request));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffsetPrint(@PathVariable Long id, HttpServletRequest request) {
        return offsetPrintService.getOffsetPrintById(id)
                .map(existingPrint -> {
                    String userId = getCurrentUserId();
                    offsetPrintService.deleteOffsetPrint(id, userId, request);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() instanceof Long) {
            return String.valueOf(authentication.getDetails());
        }
        return authentication != null ? authentication.getName() : "anonymous";
    }

    private Map<String, Object> convertToDto(OffsetPrint offsetPrint) {
        Map<String, Object> dto = new java.util.HashMap<>();
        dto.put("id", offsetPrint.getId());
        dto.put("jobName", offsetPrint.getJobName());
        dto.put("jobDescription", offsetPrint.getJobDescription());
        dto.put("printType", offsetPrint.getPrintType());

        // Customer info
        if (offsetPrint.getCustomer() != null) {
            dto.put("customerId", offsetPrint.getCustomer().getId());
            dto.put("customerName", offsetPrint.getCustomer().getName());
        } else {
            dto.put("customerName", "N/A");
        }

        // Supplier info
        if (offsetPrint.getSupplier() != null) {
            dto.put("supplierId", offsetPrint.getSupplier().getId());
            dto.put("supplierName", offsetPrint.getSupplier().getName());
        } else {
            dto.put("supplierName", "N/A");
        }

        dto.put("jobType", offsetPrint.getJobType());
        dto.put("quantity", offsetPrint.getQuantity());
        dto.put("supplierJobAmount", offsetPrint.getSupplierJobAmount());
        dto.put("profitPercentage", offsetPrint.getProfitPercentage());
        dto.put("totalAmount", offsetPrint.getTotalAmount());
        dto.put("paymentStatus", offsetPrint.getPaymentStatus());

        return dto;
    }
}