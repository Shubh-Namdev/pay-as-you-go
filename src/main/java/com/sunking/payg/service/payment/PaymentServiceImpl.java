package com.sunking.payg.service.payment;

import com.sunking.payg.dto.CreatePaymentRequest;
import com.sunking.payg.entity.Device;
import com.sunking.payg.entity.DeviceAssignment;
import com.sunking.payg.entity.Payment;
import com.sunking.payg.enums.DeviceStatus;
import com.sunking.payg.enums.PaymentStatus;
import com.sunking.payg.kafka.event.PaymentEvent;
import com.sunking.payg.kafka.producer.PaymentProducer;
import com.sunking.payg.repository.AssignmentRepository;
import com.sunking.payg.repository.DeviceRepository;
import com.sunking.payg.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final AssignmentRepository assignmentRepository;
    private final DeviceRepository deviceRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PaymentProducer paymentProducer;

    @Override
    public String createPayment(CreatePaymentRequest request) {

        // ✅ 1. Idempotency check
        Optional<Payment> existing = paymentRepository
                .findByIdempotencyKey(request.getIdempotencyKey());
        if (existing.isPresent()) return "duplicate payment found with id " +existing.get().getId();


        // ✅ 2. Check if already processing
        Optional<Payment> pending = paymentRepository
                .findTopByCustomerIdAndDeviceIdAndStatusOrderByCreatedAtDesc(
                        request.getCustomerId(),
                        request.getDeviceId(),
                        PaymentStatus.PENDING
                );

        if (pending.isPresent()) return "payment is already in progress with payment id" +pending.orElseThrow().getId();
        

        // ✅ 3. Create new payment
        Payment payment = new Payment();
        payment.setCustomerId(request.getCustomerId());
        payment.setDeviceId(request.getDeviceId());
        payment.setAmount(request.getAmount());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setIdempotencyKey(request.getIdempotencyKey());

        try {
            Payment saved = paymentRepository.save(payment);
            
            PaymentEvent event = new PaymentEvent(
                    saved.getId(),
                    saved.getCustomerId(),
                    saved.getDeviceId(),
                    saved.getAmount()
            );

            paymentProducer.publish(event);

            return "payment initiated with payment id" +saved.getId();
            
        }catch (DataIntegrityViolationException ex) {
            return "payment is already in progress with payment id " +paymentRepository.findByCustomerIdAndDeviceId(
                        request.getCustomerId(), request.getDeviceId()
                    ).orElseThrow().getId();
        }

    }

    @Override
    public void updatePaymentStatus(Long paymentId, String status, String txnId) {

        System.out.println("Updating payment status to Database with : "+paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow();
        
        if ("SUCCESS".equals(status)) {

            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setExternalTxnId(txnId);
            paymentRepository.save(payment);

            DeviceAssignment assignment = assignmentRepository
                    .findByDeviceId(payment.getDeviceId())
                    .orElseThrow();

            Device device = deviceRepository.findById(payment.getDeviceId()).orElseThrow();

            assignment.setRemainingBalance(
                    assignment.getRemainingBalance().subtract(payment.getAmount())
            );

            assignment.setLastPaymentDate(LocalDateTime.now());

            if (device.getPaymentPlanType().name().equals("DAILY")) {
                assignment.setNextDueDate(LocalDateTime.now().plusMinutes(5));
                // assignment.setNextDueDate(LocalDateTime.now().plusDays(1));
            } else {
                assignment.setNextDueDate(LocalDateTime.now().plusWeeks(1));
            }

            assignment.setStatus(DeviceStatus.ACTIVE);
            assignmentRepository.save(assignment);

            redisTemplate.opsForValue().set(
                    "device:" + assignment.getDeviceId() + ":status",
                    DeviceStatus.ACTIVE.name()
            );

            // TBD
            // Send notification to customer for payment confirmation from payg side

        } else {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
        }
    }
}
