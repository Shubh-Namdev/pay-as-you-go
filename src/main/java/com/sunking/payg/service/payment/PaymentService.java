package com.sunking.payg.service.payment;

import com.sunking.payg.dto.CreatePaymentRequest;

public interface PaymentService {
    String createPayment(CreatePaymentRequest request, Long customerId);
    void updatePaymentStatus(Long paymentId, String status, String txnId);
}