package com.example.printingApp.repository;

import com.example.printingApp.model.SublimationPrint;
import com.example.printingApp.model.SublimationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SublimationPrintRepository extends JpaRepository<SublimationPrint, Long> {

    // Find by customer ID
    List<SublimationPrint> findByCustomerId(Long customerId);

    // Find by sublimation type
    List<SublimationPrint> findBySublimationType(SublimationType sublimationType);

    // Find by customer and sublimation type
    List<SublimationPrint> findByCustomerIdAndSublimationType(Long customerId, SublimationType sublimationType);
}