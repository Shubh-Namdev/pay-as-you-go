package com.sunking.payg.controller.gateway;

import lombok.Data;

@Data
public class PaymentGatewayResponse {
    private String status;
    private String transactionId;

    public PaymentGatewayResponse(String status, String transactionId) {
        this.status = status;
        this.transactionId = transactionId;
    }
}
