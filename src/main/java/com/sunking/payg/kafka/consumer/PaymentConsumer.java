package com.sunking.payg.kafka.consumer;

import com.sunking.payg.entity.Payment;
import com.sunking.payg.enums.PaymentStatus;
import com.sunking.payg.kafka.event.PaymentEvent;
import com.sunking.payg.repository.PaymentRepository;
import com.sunking.payg.service.integration.PaymentGatewayService;

import lombok.RequiredArgsConstructor;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;


@Component
@RequiredArgsConstructor
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

        System.out.println("Initiating payment with gateway: " + event);

        Payment payment = paymentRepository.findById(event.getPaymentId())
                .orElseThrow();

        // Idempotency
        if (payment.getStatus() != PaymentStatus.PENDING) {
            return;
        }

        try {
            // CALL EXTERNAL GATEWAY (INITIATE ONLY)
            paymentGatewayService.initiatePayment(
                    payment.getId(),
                    payment.getCustomerId(),
                    payment.getDeviceId(),
                    payment.getAmount()
            );

        }catch (Exception ex) {
            System.out.println("Gateway call failed, retrying...");
            throw ex; 
        }
    }


    @KafkaListener(topics = "payment-events-dlt", groupId = "payment-group")
    public void handleDLT(PaymentEvent event) {

        System.out.println("Moved to DLT: " + event);

        Payment payment = paymentRepository.findById(event.getPaymentId())
                .orElseThrow();

        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);
    }
    
}

















// @KafkaListener(topics = "payment-events", groupId = "payment-group")
//     public void consume(PaymentEvent event) {
//         System.out.println("Received event: " + event);

//         Payment payment = paymentRepository.findById(event.getPaymentId())
//                 .orElseThrow();

//         // Idempotency check
//         if (payment.getStatus() == PaymentStatus.SUCCESS) {
//             return;
//         }

//         // Simulate external API success
//         payment.setStatus(PaymentStatus.SUCCESS);
//         payment.setExternalTxnId("TXN_" + System.currentTimeMillis());
//         paymentRepository.save(payment);

//         DeviceAssignment assignment = assignmentRepository.findByDeviceId(event.getDeviceId())
//                 .orElseThrow();

//         Device device = deviceRepository.findById(event.getDeviceId()).orElseThrow();

//         // Update balance
//         assignment.setRemainingBalance(
//                 assignment.getRemainingBalance().subtract(event.getAmount())
//         );

//         assignment.setLastPaymentDate(LocalDateTime.now());

//         // Update next due date
//         if (device.getPaymentPlanType().name().equals("DAILY")) {
//             assignment.setNextDueDate(LocalDateTime.now().plusMinutes(3));
//             // assignment.setNextDueDate(LocalDateTime.now().plusDays(1));
//         } else {
//             assignment.setNextDueDate(LocalDateTime.now().plusWeeks(1));
//         }

//         // Unlock device
//         assignment.setStatus(DeviceStatus.ACTIVE);

//         assignmentRepository.save(assignment);

//         // ✅ Update Redis cache
//         String key = "device:" + assignment.getDeviceId() + ":status";
//         redisTemplate.opsForValue().set(key, assignment.getStatus().name());
//     }