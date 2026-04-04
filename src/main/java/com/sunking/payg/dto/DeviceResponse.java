package com.sunking.payg.dto;

import java.math.BigDecimal;

import com.sunking.payg.enums.PaymentPlanType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeviceResponse {

    private Long id;
    private String serialNumber;
    private BigDecimal totalCost;
    private PaymentPlanType paymentPlanType;
    private BigDecimal paymentAmount;
    private String imageUrl;
}
