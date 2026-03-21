package com.sunking.payg.kafka.consumer;

import com.sunking.payg.entity.Device;
import com.sunking.payg.entity.DeviceAssignment;
import com.sunking.payg.entity.Payment;
import com.sunking.payg.enums.DeviceStatus;
import com.sunking.payg.enums.PaymentStatus;
import com.sunking.payg.kafka.event.PaymentEvent;
import com.sunking.payg.repository.AssignmentRepository;
import com.sunking.payg.repository.DeviceRepository;
import com.sunking.payg.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PaymentConsumer {

    private final PaymentRepository paymentRepository;
    private final AssignmentRepository assignmentRepository;
    private final DeviceRepository deviceRepository;
    private final RedisTemplate<String, Object> redisTemplate;


    @KafkaListener(topics = "payment-events", groupId = "payment-group")
    public void consume(PaymentEvent event) {
        System.out.println("Received event: " + event);

        Payment payment = paymentRepository.findById(event.getPaymentId())
                .orElseThrow();

        // Idempotency check
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return;
        }

        // Simulate external API success
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setExternalTxnId("TXN_" + System.currentTimeMillis());
        paymentRepository.save(payment);

        DeviceAssignment assignment = assignmentRepository.findByDeviceId(event.getDeviceId())
                .orElseThrow();

        Device device = deviceRepository.findById(event.getDeviceId()).orElseThrow();

        // Update balance
        assignment.setRemainingBalance(
                assignment.getRemainingBalance().subtract(event.getAmount())
        );

        assignment.setLastPaymentDate(LocalDateTime.now());

        // Update next due date
        if (device.getPaymentPlanType().name().equals("DAILY")) {
            assignment.setNextDueDate(LocalDateTime.now().plusMinutes(3));
            // assignment.setNextDueDate(LocalDateTime.now().plusDays(1));
        } else {
            assignment.setNextDueDate(LocalDateTime.now().plusWeeks(1));
        }

        // Unlock device
        assignment.setStatus(DeviceStatus.ACTIVE);

        assignmentRepository.save(assignment);

        // ✅ Update Redis cache
        String key = "device:" + assignment.getDeviceId() + ":status";
        redisTemplate.opsForValue().set(key, assignment.getStatus().name());
    }
}