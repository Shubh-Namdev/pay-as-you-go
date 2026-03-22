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

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(name = "external_txn_id")
    private String externalTxnId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}