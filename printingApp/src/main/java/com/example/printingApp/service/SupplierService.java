package com.example.printingApp.service;

import com.example.printingApp.model.Customer;
import com.example.printingApp.model.Supplier;
import com.example.printingApp.repository.CustomerRepository;
import com.example.printingApp.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupplierService {
    @Autowired
    private SupplierRepository supplierRepository;

    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

}
