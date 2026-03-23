package com.sunking.payg.entity;

import com.sunking.payg.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments",
    indexes = {
        @Index(name = "idx_payment_idempotency", columnList = "idempotency_key", unique = true),

        @Index(name = "idx_payment_customer_device_status_created",
               columnList = "customer_id, device_id, status, created_at"),

        @Index(name = "idx_payment_status_created",
               columnList = "status, created_at"),

        @Index(name = "idx_payment_status",
               columnList = "status")
    }
)
@Data
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String idempotencyKey;

    private Long customerId;
    private Long deviceId;

    private BigDecimal amount;

    private LocalDateTime paymentDate;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String externalTxnId;

    private LocalDateTime createdAt = LocalDateTime.now();
}