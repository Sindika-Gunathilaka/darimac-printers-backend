package com.example.printingApp.controller;

import com.example.printingApp.model.SublimationPrint;
import com.example.printingApp.model.SublimationType;
import com.example.printingApp.service.SublimationPrintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/sublimation-prints")
public class SublimationPrintController {

    @Autowired
    private SublimationPrintService sublimationPrintService;

    @GetMapping
    public ResponseEntity<List<SublimationPrint>> getAllSublimationPrints() {
        List<SublimationPrint> prints = sublimationPrintService.getAllSublimationPrints();
        return ResponseEntity.ok(prints);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SublimationPrint> getSublimationPrintById(@PathVariable Long id) {
        return sublimationPrintService.getSublimationPrintById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<SublimationPrint>> getSublimationPrintsByCustomerId(@PathVariable Long customerId) {
        List<SublimationPrint> prints = sublimationPrintService.getSublimationPrintsByCustomerId(customerId);
        return ResponseEntity.ok(prints);
    }

    @GetMapping("/type/{sublimationType}")
    public ResponseEntity<List<SublimationPrint>> getSublimationPrintsBySublimationType(@PathVariable SublimationType sublimationType) {
        List<SublimationPrint> prints = sublimationPrintService.getSublimationPrintsBySublimationType(sublimationType);
        return ResponseEntity.ok(prints);
    }

    @GetMapping("/types")
    public ResponseEntity<List<SublimationType>> getAllSublimationTypes() {
        return ResponseEntity.ok(Arrays.asList(SublimationType.values()));
    }

    @GetMapping("/current-price/{sublimationType}")
    public ResponseEntity<BigDecimal> getCurrentUnitPrice(@PathVariable SublimationType sublimationType) {
        BigDecimal unitPrice = sublimationPrintService.getCurrentUnitPrice(sublimationType);
        return ResponseEntity.ok(unitPrice);
    }

    @PostMapping
    public ResponseEntity<SublimationPrint> createSublimationPrint(@RequestBody SublimationPrint sublimationPrint, HttpServletRequest request) {
        try {
            String userId = getCurrentUserId();
            SublimationPrint savedPrint = sublimationPrintService.saveSublimationPrint(sublimationPrint, userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedPrint);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<SublimationPrint> updateSublimationPrint(@PathVariable Long id, @RequestBody SublimationPrint sublimationPrint, HttpServletRequest request) {
        try {
            String userId = getCurrentUserId();
            SublimationPrint updatedPrint = sublimationPrintService.updateSublimationPrint(id, sublimationPrint, userId, request);
            return ResponseEntity.ok(updatedPrint);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSublimationPrint(@PathVariable Long id, HttpServletRequest request) {
        try {
            String userId = getCurrentUserId();
            sublimationPrintService.deleteSublimationPrint(id, userId, request);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
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