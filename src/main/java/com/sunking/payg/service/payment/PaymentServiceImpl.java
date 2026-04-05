package com.sunking.payg.service.payment;

import com.sunking.payg.dto.CreatePaymentRequest;
import com.sunking.payg.dto.PaymentResponse;
import com.sunking.payg.entity.Device;
import com.sunking.payg.entity.DeviceAssignment;
import com.sunking.payg.entity.Payment;
import com.sunking.payg.enums.DeviceStatus;
import com.sunking.payg.enums.PaymentStatus;
import com.sunking.payg.exceptions.BusinessException;
import com.sunking.payg.exceptions.ResourceNotFoundException;
import com.sunking.payg.kafka.event.PaymentEvent;
import com.sunking.payg.kafka.producer.PaymentProducer;
import com.sunking.payg.repository.AssignmentRepository;
import com.sunking.payg.repository.DeviceRepository;
import com.sunking.payg.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final AssignmentRepository assignmentRepository;
    private final DeviceRepository deviceRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PaymentProducer paymentProducer;

    @Override
    public PaymentResponse createPayment(CreatePaymentRequest request, Long customerId) {

        log.info("Creating payment for customerId={}, deviceId={}, amount={}",
                customerId, request.getDeviceId(), request.getAmount());

        // Idempotency check
        Optional<Payment> existing = paymentRepository
                .findByIdempotencyKey(request.getIdempotencyKey());
        if (existing.isPresent()) {
             log.warn("Duplicate payment request detected for idempotencyKey={}, paymentId={}",
                    request.getIdempotencyKey(), existing.get().getId());
            return mapToResponse(existing.get());
        }

        // Check if already processing
        Optional<Payment> pending = paymentRepository
                .findTopByCustomerIdAndDeviceIdAndStatusOrderByCreatedAtDesc(
                        customerId,
                        request.getDeviceId(),
                        PaymentStatus.PENDING
                );

        if (pending.isPresent()) {
            log.warn("Payment already in progress for customerId={}, deviceId={}, paymentId={}",
                    customerId, request.getDeviceId(), pending.get().getId());
            return mapToResponse(pending.get());
        }

        // Create new payment
        Payment payment = new Payment();
        payment.setCustomerId(customerId);
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
            log.info("Payment event published to Kafka for paymentId={}", saved.getId());

            return mapToResponse(saved);
            
        }catch (DataIntegrityViolationException ex) {
            log.warn("Race condition detected for idempotencyKey={}, customerId={}, deviceId={}",
                    request.getIdempotencyKey(), customerId, request.getDeviceId());

            Payment existingPayment = paymentRepository
                    .findByCustomerIdAndDeviceId(customerId, request.getDeviceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Payment not found after duplicate detection"));
                    

            return mapToResponse(existingPayment);
        }
    }

    @Override
    public void updatePaymentStatus(Long paymentId, String status, String txnId) {

        log.info("Updating payment status for paymentId={}, status={}", paymentId, status);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> {
                    log.error("Payment not found for paymentId={}", paymentId);
                    return new ResourceNotFoundException("Payment not found with id " + paymentId);
                });
        
        try {
            if ("SUCCESS".equals(status)) {

                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setExternalTxnId(txnId);
                paymentRepository.save(payment);
                log.info("Payment marked SUCCESS for paymentId={}, txnId={}", paymentId, txnId);

                DeviceAssignment assignment = assignmentRepository
                        .findByDeviceId(payment.getDeviceId())
                        .orElseThrow(() -> {
                            log.error("Assignment not found for deviceId={}", payment.getDeviceId());
                            return new ResourceNotFoundException("Assignment not found");
                        });

                Device device = deviceRepository.findById(payment.getDeviceId())
                        .orElseThrow(() -> {
                            log.error("Device not found for deviceId={}", payment.getDeviceId());
                            return new ResourceNotFoundException("Device not found");
                        });

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
                log.info("Device unlocked for deviceId={}, customerId={}",
                        assignment.getDeviceId(), assignment.getCustomerId());

                
                // update redis
                redisTemplate.opsForValue().set(
                        "device:" + assignment.getDeviceId() + ":status",
                        DeviceStatus.ACTIVE.name()
                );

                // TBD
                // Send notification to customer for payment confirmation from payg side

            } else {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
                log.warn("Payment marked FAILED for paymentId={}", paymentId);
            }
        }catch (Exception ex) {

            log.error("Error while updating payment status for paymentId={}", paymentId, ex);

            throw new BusinessException("Failed to update payment status");
        }
    }


    @Override
    public PaymentResponse getPaymentStatus(Long customerId, Long deviceId) {

        Payment payment = paymentRepository.findByCustomerIdAndDeviceId(
            customerId, deviceId).orElseThrow(
                () -> new ResourceNotFoundException("Payment not found with customer id " + customerId + "and device id " + deviceId)
            );

        return mapToResponse(payment);
                            
    }

    private PaymentResponse mapToResponse(Payment payment) {

        return PaymentResponse.builder()
                    .id(payment.getId())
                    .customerId(payment.getCustomerId())
                    .deviceId(payment.getDeviceId())
                    .amount(payment.getAmount())
                    .paymentDate(payment.getPaymentDate())
                    .status(payment.getStatus())
                    .externalTxnId(payment.getExternalTxnId())
                    .build();
    }
}
