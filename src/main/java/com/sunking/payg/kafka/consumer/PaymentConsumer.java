package com.sunking.payg.kafka.consumer;

import com.sunking.payg.entity.Payment;
import com.sunking.payg.enums.PaymentStatus;
import com.sunking.payg.exceptions.ResourceNotFoundException;
import com.sunking.payg.kafka.event.PaymentEvent;
import com.sunking.payg.repository.PaymentRepository;
import com.sunking.payg.service.integration.PaymentGatewayService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;


@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentConsumer {

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayService paymentGatewayService;

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 2000),
            dltTopicSuffix = "-dlt"
    )
    @KafkaListener(topics = "payment-events", groupId = "payment-group")
    public void consume(PaymentEvent event) {

        log.info("Received payment event: paymentId={}, customerId={}, deviceId={}",
                event.getPaymentId(), event.getCustomerId(), event.getDeviceId());

        Payment payment = paymentRepository.findById(event.getPaymentId())
                .orElseThrow(() -> {
                    log.error("Payment not found for paymentId={}", event.getPaymentId());
                    return new ResourceNotFoundException(
                            "Payment not found with id " + event.getPaymentId());
                });

        // Idempotency
        if (payment.getStatus() != PaymentStatus.PENDING) {
            log.warn("Skipping paymentId={} as status is {}",
                    payment.getId(), payment.getStatus());
            return;
        }

        try {
            // CALL EXTERNAL GATEWAY (INITIATE ONLY)
            log.info("Initiating payment with gateway for paymentId={}", payment.getId());

            paymentGatewayService.initiatePayment(
                    payment.getId(),
                    payment.getCustomerId(),
                    payment.getDeviceId(),
                    payment.getAmount()
            );

            log.info("Payment initiation request sent successfully for paymentId={}", payment.getId());

        }catch (Exception ex) {
            log.error("Gateway call failed for paymentId={}, will retry", payment.getId(), ex);
            throw ex; 
        }
    }


    @KafkaListener(topics = "payment-events-dlt", groupId = "payment-group")
    public void handleDLT(PaymentEvent event) {

        log.error("Message moved to DLT after retries exhausted: paymentId={}", event.getPaymentId());

        try {
            Payment payment = paymentRepository.findById(event.getPaymentId())
                    .orElseThrow(() -> {
                        log.error("Payment not found in DLT for paymentId={}", event.getPaymentId());
                        return new ResourceNotFoundException(
                                "Payment not found with id " + event.getPaymentId());
                    });

            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);

            log.error("Marked paymentId={} as FAILED after retries", payment.getId());

        } catch (Exception ex) {
            // DO NOT throw → avoid infinite loop
            log.error("Failed to process DLT event for paymentId={}", event.getPaymentId(), ex);
        }
    }
    
}

