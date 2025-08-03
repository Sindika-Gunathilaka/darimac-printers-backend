package com.example.printingApp.repository;

import com.example.printingApp.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PrintJobRepository extends JpaRepository<PrintJob, Long> {
    List<PrintJob> findByCustomerId(Long customerId);
    List<PrintJob> findByPaymentStatus(PrintJob.PaymentStatus status);
}
