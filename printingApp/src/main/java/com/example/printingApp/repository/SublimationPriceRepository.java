package com.example.printingApp.repository;

import com.example.printingApp.model.SublimationPrice;
import com.example.printingApp.model.SublimationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SublimationPriceRepository extends JpaRepository<SublimationPrice, Long> {

    // Find active prices only
    List<SublimationPrice> findByIsActiveTrue();

    // Find by sublimation type and active status
    Optional<SublimationPrice> findBySublimationTypeAndIsActiveTrue(SublimationType sublimationType);

    // Find all prices for a specific type (including inactive)
    List<SublimationPrice> findBySublimationType(SublimationType sublimationType);

    // Get latest price for each type
    @Query("SELECT sp FROM SublimationPrice sp WHERE sp.isActive = true AND sp.id IN " +
            "(SELECT MAX(sp2.id) FROM SublimationPrice sp2 WHERE sp2.sublimationType = sp.sublimationType AND sp2.isActive = true GROUP BY sp2.sublimationType)")
    List<SublimationPrice> findLatestActivePrices();
}