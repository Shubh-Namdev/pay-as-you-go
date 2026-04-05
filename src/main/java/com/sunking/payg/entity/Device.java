package com.sunking.payg.entity;

import com.sunking.payg.enums.PaymentPlanType;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "devices",
    indexes = {
        @Index(name = "idx_device_serial", columnList = "serial_number", unique = true)
    }
)
@Data
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String serialNumber;

    private BigDecimal totalCost;

    @Enumerated(EnumType.STRING)
    private PaymentPlanType paymentPlanType;

    private BigDecimal paymentAmount;

    private String imageUrl;

    private LocalDateTime createdAt = LocalDateTime.now();

    private boolean isAvailable;
}