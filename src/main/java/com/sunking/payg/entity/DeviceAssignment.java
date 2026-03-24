package com.sunking.payg.entity;

import com.sunking.payg.enums.DeviceStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "device_assignments",
    indexes = {
        @Index(name = "idx_assignment_device",
               columnList = "device_id"),

        @Index(name = "idx_assignment_due_date",
               columnList = "next_due_date")
    }
)
@Data
public class DeviceAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long customerId;
    private Long deviceId;

    @Enumerated(EnumType.STRING)
    private DeviceStatus status;

    private LocalDateTime assignedAt;
    private LocalDateTime lastPaymentDate;
    private LocalDateTime nextDueDate;

    private BigDecimal remainingBalance;
}