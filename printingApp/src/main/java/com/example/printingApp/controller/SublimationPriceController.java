package com.example.printingApp.controller;

import com.example.printingApp.model.SublimationPrice;
import com.example.printingApp.model.SublimationType;
import com.example.printingApp.service.SublimationPriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sublimation-prices")
public class SublimationPriceController {

    @Autowired
    private SublimationPriceService sublimationPriceService;

    @GetMapping
    public ResponseEntity<List<SublimationPrice>> getAllPrices() {
        List<SublimationPrice> prices = sublimationPriceService.getAllPrices();
        return ResponseEntity.ok(prices);
    }

    @GetMapping("/active")
    public ResponseEntity<List<SublimationPrice>> getAllActivePrices() {
        List<SublimationPrice> prices = sublimationPriceService.getAllActivePrices();
        return ResponseEntity.ok(prices);
    }

    @GetMapping("/latest")
    public ResponseEntity<List<SublimationPrice>> getLatestActivePrices() {
        List<SublimationPrice> prices = sublimationPriceService.getLatestActivePrices();
        return ResponseEntity.ok(prices);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SublimationPrice> getPriceById(@PathVariable Long id) {
        return sublimationPriceService.getPriceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/type/{sublimationType}")
    public ResponseEntity<SublimationPrice> getActivePriceByType(@PathVariable SublimationType sublimationType) {
        return sublimationPriceService.getActivePriceByType(sublimationType)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/types")
    public ResponseEntity<List<SublimationType>> getAllSublimationTypes() {
        return ResponseEntity.ok(Arrays.asList(SublimationType.values()));
    }

    @GetMapping("/current-prices")
    public ResponseEntity<Map<SublimationType, BigDecimal>> getCurrentPrices() {
        List<SublimationPrice> activePrices = sublimationPriceService.getAllActivePrices();
        Map<SublimationType, BigDecimal> priceMap = activePrices.stream()
                .collect(Collectors.toMap(
                        SublimationPrice::getSublimationType,
                        SublimationPrice::getUnitPrice
                ));
        return ResponseEntity.ok(priceMap);
    }

    @PostMapping
    public ResponseEntity<SublimationPrice> createPrice(@RequestBody SublimationPrice price) {
        try {
            SublimationPrice savedPrice = sublimationPriceService.savePrice(price);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedPrice);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<SublimationPrice> updatePrice(@PathVariable Long id, @RequestBody SublimationPrice price) {
        try {
            SublimationPrice updatedPrice = sublimationPriceService.updatePrice(id, price);
            return ResponseEntity.ok(updatedPrice);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePrice(@PathVariable Long id) {
        try {
            sublimationPriceService.deletePrice(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivatePrice(@PathVariable Long id) {
        try {
            sublimationPriceService.deactivatePrice(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<Void> activatePrice(@PathVariable Long id) {
        try {
            sublimationPriceService.activatePrice(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}