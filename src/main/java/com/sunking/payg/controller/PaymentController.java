package com.sunking.payg.controller;

import com.sunking.payg.dto.CreatePaymentRequest;
import com.sunking.payg.entity.Payment;
import com.sunking.payg.enums.PaymentStatus;
import com.sunking.payg.repository.PaymentRepository;
import com.sunking.payg.service.payment.PaymentService;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;

    @PostMapping
    public String createPayment(@RequestBody CreatePaymentRequest request) {
        String paymentStatus = paymentService.createPayment(request);
        return paymentStatus;
    }

    @PostMapping("/callback")
    public void handleCallback(@RequestBody Map<String, Object> request) {

        System.out.println("Received payment status from Gateway : "+request);
        Long paymentId = Long.valueOf(request.get("paymentId").toString());
        String status = request.get("status").toString();
        String txnId = request.get("transactionId").toString();

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow();

        if (!payment.getStatus().equals(PaymentStatus.PENDING)) return;

        paymentService.updatePaymentStatus(paymentId, status, txnId);
    }
}