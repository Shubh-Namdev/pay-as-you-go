package com.sunking.payg.service.integration;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.sunking.payg.config.GatewayConfig;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PaymentGatewayServiceImpl implements PaymentGatewayService {

    @Autowired
    private GatewayConfig gatewayConfig;

    private final RestTemplate restTemplate;

    // String url = gatewayConfig.getUrl();
    // private final String GATEWAY_URL = url+"/mock-payment/initiate";


    @Override
    public void initiatePayment(Long paymentId, Long customerId, Long deviceId, BigDecimal amount) {

        Map<String, Object> request = new HashMap<>();
        request.put("paymentId", paymentId);
        request.put("customerId", customerId);
        request.put("deviceId", deviceId);
        request.put("amount", amount);

        // Testing retry mechanism
        // if (paymentId == 1) throw new RuntimeException("problem at gateway side");
        String gatewayURL = gatewayConfig.getUrl()+"/mock-payment/initiate";
        System.out.println("Routing payment request to "+gatewayURL+ " with request details : " +request);
       
        restTemplate.postForObject(gatewayURL, request, Void.class);
    }

    @Override
    public boolean checkStatus(Long paymentId) {
        return new Random().nextBoolean();
    }
}