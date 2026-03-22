package com.sunking.payg.service.integration;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PaymentGatewayServiceImpl implements PaymentGatewayService {

    private final RestTemplate restTemplate;
    private static final String GATEWAY_URL = "http://localhost:7070/mock-payment/initiate";


    @Override
    public void initiatePayment(Long paymentId, Long customerId, Long deviceId, BigDecimal amount) {

        Map<String, Object> request = new HashMap<>();
        request.put("paymentId", paymentId);
        request.put("customerId", customerId);
        request.put("deviceId", deviceId);
        request.put("amount", amount);

        System.out.println("Routing payment request to payment gateway: "+request);

        // Testing retry mechanism
        // if (paymentId == 1) throw new RuntimeException("problem at gateway side");
        restTemplate.postForObject(GATEWAY_URL, request, Void.class);
    }

    @Override
    public boolean checkStatus(Long paymentId) {
        return new Random().nextBoolean();
    }
}