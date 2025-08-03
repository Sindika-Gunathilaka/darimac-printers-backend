package com.example.printingApp.service;

import com.example.printingApp.model.SublimationPrice;
import com.example.printingApp.model.SublimationType;
import com.example.printingApp.repository.SublimationPriceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SublimationPriceService {

    @Autowired
    private SublimationPriceRepository sublimationPriceRepository;

    public List<SublimationPrice> getAllActivePrices() {
        return sublimationPriceRepository.findByIsActiveTrue();
    }

    public List<SublimationPrice> getAllPrices() {
        return sublimationPriceRepository.findAll();
    }

    public Optional<SublimationPrice> getPriceById(Long id) {
        return sublimationPriceRepository.findById(id);
    }

    public Optional<SublimationPrice> getActivePriceByType(SublimationType sublimationType) {
        return sublimationPriceRepository.findBySublimationTypeAndIsActiveTrue(sublimationType);
    }

    public List<SublimationPrice> getLatestActivePrices() {
        return sublimationPriceRepository.findLatestActivePrices();
    }

    @Transactional
    public SublimationPrice savePrice(SublimationPrice price) {
        // When saving a new price, optionally deactivate old prices of the same type
        if (price.getId() == null && price.getIsActive()) {
            // Deactivate existing active prices for this type
            Optional<SublimationPrice> existingPrice =
                    sublimationPriceRepository.findBySublimationTypeAndIsActiveTrue(price.getSublimationType());

            if (existingPrice.isPresent()) {
                SublimationPrice existing = existingPrice.get();
                existing.setIsActive(false);
                sublimationPriceRepository.save(existing);
            }
        }

        return sublimationPriceRepository.save(price);
    }

    @Transactional
    public SublimationPrice updatePrice(Long id, SublimationPrice price) {
        return getPriceById(id)
                .map(existingPrice -> {
                    price.setId(id);
                    price.setCreatedAt(existingPrice.getCreatedAt()); // Preserve creation date
                    return sublimationPriceRepository.save(price);
                })
                .orElseThrow(() -> new RuntimeException("Sublimation price not found with id: " + id));
    }

    @Transactional
    public void deletePrice(Long id) {
        sublimationPriceRepository.deleteById(id);
    }

    @Transactional
    public void deactivatePrice(Long id) {
        getPriceById(id).ifPresent(price -> {
            price.setIsActive(false);
            sublimationPriceRepository.save(price);
        });
    }

    @Transactional
    public void activatePrice(Long id) {
        getPriceById(id).ifPresent(price -> {
            // First deactivate any existing active price for this type
            Optional<SublimationPrice> existingActivePrice =
                    sublimationPriceRepository.findBySublimationTypeAndIsActiveTrue(price.getSublimationType());

            if (existingActivePrice.isPresent()) {
                SublimationPrice existing = existingActivePrice.get();
                existing.setIsActive(false);
                sublimationPriceRepository.save(existing);
            }

            // Then activate this price
            price.setIsActive(true);
            sublimationPriceRepository.save(price);
        });
    }
}