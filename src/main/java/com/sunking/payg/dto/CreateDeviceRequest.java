package com.sunking.payg.dto;

import com.sunking.payg.enums.PaymentPlanType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateDeviceRequest {
    private String serialNumber;
    private BigDecimal totalCost;
    private PaymentPlanType paymentPlanType;
    private BigDecimal paymentAmount;
    private String imageUrl;
}