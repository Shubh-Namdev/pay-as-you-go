package com.sunking.payg.service.integration;

import java.math.BigDecimal;

public interface PaymentGatewayService {
    void initiatePayment(Long paymentId, Long customerId, Long deviceId, BigDecimal amount);
    boolean checkStatus(Long paymentId);
}