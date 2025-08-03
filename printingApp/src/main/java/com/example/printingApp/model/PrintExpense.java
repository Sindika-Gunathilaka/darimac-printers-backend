package com.example.printingApp.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Data;

@Entity
@Table(name = "print_expenses")
@Data
public class PrintExpense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;
    private BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "digital_print_id")
    @JsonBackReference(value = "digital-print-expenses")
    private DigitalPrint digitalPrint;

    @ManyToOne
    @JoinColumn(name = "offset_print_id")
    @JsonBackReference(value = "offset-print-expenses")
    private OffsetPrint offsetPrint;

    @ManyToOne
    @JoinColumn(name = "duplo_print_id")
    @JsonBackReference(value = "duplo-print-expenses")
    private DuploPrint duploPrint;

    @ManyToOne
    @JoinColumn(name = "sublimation_print_id")
    @JsonBackReference(value = "sublimation-print-expenses")
    private SublimationPrint sublimationPrint;
}