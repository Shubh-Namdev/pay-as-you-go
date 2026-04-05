package com.sunking.payg.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.sunking.payg.enums.DeviceStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeviceStatusResponse {

    private Long deviceId;
    private DeviceStatus status;
    private String message;
    private LocalDateTime assignedAt;
    private LocalDateTime lastPaymentDate;
    private LocalDateTime nextDueDate;
    private BigDecimal remainingBalance;
}