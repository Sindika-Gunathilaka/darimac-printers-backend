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
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/digital-prints")
public class DigitalPrintController {
    @Autowired
    private DigitalPrintService digitalPrintService;

    @GetMapping
    public List<DigitalPrint> getAllDigitalPrints() {
        return digitalPrintService.getAllDigitalPrints();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DigitalPrint> getDigitalPrintById(@PathVariable Long id) {
        return digitalPrintService.getDigitalPrintById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}")
    public List<DigitalPrint> getDigitalPrintsByCustomerId(@PathVariable Long customerId) {
        return digitalPrintService.getDigitalPrintsByCustomerId(customerId);
    }

    @PostMapping
    public ResponseEntity<DigitalPrint> createDigitalPrint(@RequestBody DigitalPrint digitalPrint, HttpServletRequest request) {
        String userId = getCurrentUserId();
        DigitalPrint saved = digitalPrintService.saveDigitalPrint(digitalPrint, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DigitalPrint> updateDigitalPrint(@PathVariable Long id, @RequestBody DigitalPrint digitalPrint, HttpServletRequest request) {
        return digitalPrintService.getDigitalPrintById(id)
                .map(existingPrint -> {
                    digitalPrint.setId(id);
                    String userId = getCurrentUserId();
                    return ResponseEntity.ok(digitalPrintService.saveDigitalPrint(digitalPrint, userId, request));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDigitalPrint(@PathVariable Long id, HttpServletRequest request) {
        return digitalPrintService.getDigitalPrintById(id)
                .map(existingPrint -> {
                    String userId = getCurrentUserId();
                    digitalPrintService.deleteDigitalPrint(id, userId, request);
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

    @GetMapping("/materials")
    public List<PrintMaterialType> getAllMaterials() {
        return Arrays.asList(PrintMaterialType.values());
    }

    @GetMapping("/qualities")
    public List<PrintQualityType> getAllQualities() {
        return Arrays.asList(PrintQualityType.values());
    }}