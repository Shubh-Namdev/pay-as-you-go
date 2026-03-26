package com.sunking.payg.entity;

import com.sunking.payg.enums.DeviceStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "device_assignments",
    indexes = {
        @Index(name = "idx_assignment_device", columnList = "device_id"),
        @Index(name = "idx_assignment_due_date", columnList = "next_due_date")
    }
)
@Data
public class DeviceAssignment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    @Enumerated(EnumType.STRING)
    private DeviceStatus status;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "last_payment_date")
    private LocalDateTime lastPaymentDate;

    @Column(name = "next_due_date")
    private LocalDateTime nextDueDate;

    @Column(name = "remaining_balance", precision = 10, scale = 2)
    private BigDecimal remainingBalance;

    @PrePersist
    public void prePersist() {
        this.assignedAt = LocalDateTime.now();
    }
}