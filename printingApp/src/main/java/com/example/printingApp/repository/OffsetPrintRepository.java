package com.example.printingApp.repository;

import com.example.printingApp.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OffsetPrintRepository extends JpaRepository<OffsetPrint, Long> {
    List<OffsetPrint> findByCustomerId(Long customerId);
}
