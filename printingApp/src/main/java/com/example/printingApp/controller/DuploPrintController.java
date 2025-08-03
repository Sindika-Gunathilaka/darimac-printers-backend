package com.example.printingApp.controller;

import com.example.printingApp.model.DuploPrint;
import com.example.printingApp.service.DuploPrintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

@RestController
@RequestMapping("/api/duplo-prints")
public class DuploPrintController {

    @Autowired
    private DuploPrintService duploPrintService;

    @GetMapping
    public ResponseEntity<List<DuploPrint>> getAllDuploPrints() {
        List<DuploPrint> duploPrints = duploPrintService.getAllDuploPrints();
        return ResponseEntity.ok(duploPrints);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DuploPrint> getDuploPrintById(@PathVariable Long id) {
        return duploPrintService.getDuploPrintById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<DuploPrint>> getDuploPrintsByCustomerId(@PathVariable Long customerId) {
        List<DuploPrint> duploPrints = duploPrintService.getDuploPrintsByCustomerId(customerId);
        return ResponseEntity.ok(duploPrints);
    }

    @PostMapping
    public ResponseEntity<DuploPrint> createDuploPrint(@RequestBody DuploPrint duploPrint, HttpServletRequest request) {
        try {
            String userId = getCurrentUserId();
            DuploPrint savedDuploPrint = duploPrintService.saveDuploPrint(duploPrint, userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDuploPrint);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<DuploPrint> updateDuploPrint(@PathVariable Long id, @RequestBody DuploPrint duploPrint, HttpServletRequest request) {
        try {
            String userId = getCurrentUserId();
            DuploPrint updatedDuploPrint = duploPrintService.updateDuploPrint(id, duploPrint, userId, request);
            return ResponseEntity.ok(updatedDuploPrint);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDuploPrint(@PathVariable Long id, HttpServletRequest request) {
        try {
            String userId = getCurrentUserId();
            duploPrintService.deleteDuploPrint(id, userId, request);
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