package com.sunking.payg.controller.gateway;

import lombok.Data;

@Data
public class PaymentGatewayRequest {
    private Long customerId;
    private Long deviceId;
    private String amount;
}