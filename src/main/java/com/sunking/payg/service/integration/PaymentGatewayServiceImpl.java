package com.sunking.payg.service.integration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import com.sunking.payg.config.GatewayConfig;
import com.sunking.payg.exceptions.BusinessException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentGatewayServiceImpl implements PaymentGatewayService {

    private final GatewayConfig gatewayConfig;
    private final RestTemplate restTemplate;

    @Override
    public void initiatePayment(Long paymentId, Long customerId, Long deviceId, BigDecimal amount) {

        String gatewayURL = gatewayConfig.getUrl() + "/mock-payment/initiate";

        Map<String, Object> request = new HashMap<>();
        request.put("paymentId", paymentId);
        request.put("customerId", customerId);
        request.put("deviceId", deviceId);
        request.put("amount", amount);

        log.info("Initiating payment to gateway: paymentId={}, customerId={}, deviceId={}, amount={}",
                paymentId, customerId, deviceId, amount);

        try {
            restTemplate.postForObject(gatewayURL, request, Void.class);
            log.info("Payment request sent successfully to gateway for paymentId={}", paymentId);

        } catch (RestClientException ex) {

            log.error("Failed to call payment gateway for paymentId={}, url={}",
                    paymentId, gatewayURL, ex);

            // propagate for retry (Kafka will retry)
            throw new BusinessException("Payment gateway call failed");
        }
    }

    @Override
    public boolean checkStatus(Long paymentId) {

        // Mock behavior
        boolean status = new Random().nextBoolean();

        log.debug("Checking payment status from gateway: paymentId={}, result={}",
                paymentId, status);

        return status;
    }
}