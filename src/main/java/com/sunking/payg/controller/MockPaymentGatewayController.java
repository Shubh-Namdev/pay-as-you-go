package com.sunking.payg.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/mock-payment")
@RequiredArgsConstructor
public class MockPaymentGatewayController {

    private final RestTemplate restTemplate;
    private final Random random = new Random();

    @PostMapping("/initiate")
    public void initiate(@RequestBody Map<String, Object> request) {

        Long paymentId = Long.valueOf(request.get("paymentId").toString());

        // simulate async callback
        new Thread(() -> {
            try {
                System.out.println("Gateway processing the payment : "+paymentId);
                Thread.sleep(3000); // user action delay

                boolean success = random.nextBoolean();

                Map<String, Object> callback = new HashMap<>();
                callback.put("paymentId", paymentId);
                callback.put("status", success ? "SUCCESS" : "FAILED");
                callback.put("transactionId", "TXN_" + System.currentTimeMillis());

                System.out.println("Gateway sending payment confirmation to PAYG : "+paymentId);
                restTemplate.postForObject(
                        "http://localhost:7070/payments/callback",
                        callback,
                        Void.class
                );

            } catch (Exception ignored) {}
        }).start();
    }
}