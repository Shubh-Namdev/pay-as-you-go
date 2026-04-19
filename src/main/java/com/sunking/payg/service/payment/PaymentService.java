package com.sunking.payg.service.payment;

import com.sunking.payg.dto.CreatePaymentRequest;
import com.sunking.payg.dto.PaymentResponse;

public interface PaymentService {
    PaymentResponse createPayment(CreatePaymentRequest request, Long customerId);
    void updatePaymentStatus(Long paymentId, String status, String txnId);
    public PaymentResponse getPaymentStatus(Long customerId, Long deviceId);
}