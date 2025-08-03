package com.example.printingApp.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "other_prints")
@Data
public class OtherPrint extends PrintJob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    private LocalDate printDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalCost;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 500)
    private String customerRemark; // Customer name and any remarks

    @Column(precision = 12, scale = 2)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal balance;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum PaymentStatus {
        PAID,
        UNPAID,
        PARTIALLY_PAID,
        OVERDUE
    }

    // Helper method to calculate profit
    public BigDecimal getProfit() {
        if (totalAmount != null && totalCost != null) {
            return totalAmount.subtract(totalCost);
        }
        return BigDecimal.ZERO;
    }

    // Helper method to get profit percentage
    public BigDecimal getProfitPercentage() {
        if (totalCost != null && totalCost.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal profit = getProfit();
            return profit.divide(totalCost, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        return BigDecimal.ZERO;
    }
}
