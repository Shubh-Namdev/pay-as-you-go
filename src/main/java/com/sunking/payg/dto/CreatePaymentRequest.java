package com.sunking.payg.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePaymentRequest {
    private String idempotencyKey;
    private Long customerId;
    private Long deviceId;
    private BigDecimal amount;
}