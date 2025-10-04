package com.example.printingApp.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Entity
@Table(name = "customers")
@Data
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private String customerNumber;

    private String name;
    private String email;
    private String phone;
    private String address;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "customer-printjobs")
    private List<PrintJob> printJobs;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (customerNumber == null) {
            // Generate customer number: CUST + timestamp + random 4 digits
            customerNumber = "CUST" + System.currentTimeMillis() + String.format("%04d", (int)(Math.random() * 10000));
        }
    }
}
