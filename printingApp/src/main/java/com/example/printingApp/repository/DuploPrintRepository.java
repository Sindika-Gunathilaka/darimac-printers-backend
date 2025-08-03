package com.example.printingApp.repository;

import com.example.printingApp.model.DuploPrint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DuploPrintRepository extends JpaRepository<DuploPrint, Long> {
    List<DuploPrint> findByCustomerId(Long customerId);
}