package com.sunking.payg.controller;

import com.sunking.payg.dto.CreatePaymentRequest;
import com.sunking.payg.service.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public String createPayment(@RequestBody CreatePaymentRequest request) {
        Long paymentId = paymentService.createPayment(request);
        return "Payment initiated with ID: " + paymentId;
    }
}