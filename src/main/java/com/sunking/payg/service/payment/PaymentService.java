package com.sunking.payg.service.payment;

import com.sunking.payg.dto.CreatePaymentRequest;

public interface PaymentService {
    Long createPayment(CreatePaymentRequest request);
}