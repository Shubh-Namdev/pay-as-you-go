package com.sunking.payg.service.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.sunking.payg.dto.CreatePaymentRequest;
import com.sunking.payg.dto.PaymentResponse;
import com.sunking.payg.entity.Device;
import com.sunking.payg.entity.DeviceAssignment;
import com.sunking.payg.entity.Payment;
import com.sunking.payg.enums.DeviceStatus;
import com.sunking.payg.enums.PaymentPlanType;
import com.sunking.payg.enums.PaymentStatus;
import com.sunking.payg.kafka.event.PaymentEvent;
import com.sunking.payg.kafka.producer.PaymentProducer;
import com.sunking.payg.repository.AssignmentRepository;
import com.sunking.payg.repository.DeviceRepository;
import com.sunking.payg.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private PaymentProducer paymentProducer;

    @InjectMocks
    private PaymentServiceImpl paymentService;


    @Test
    void shouldCreatePaymentSuccessfully() {

        // Arrange
        CreatePaymentRequest paymentRequest = new CreatePaymentRequest();
        paymentRequest.setIdempotencyKey("SGHJ1463HGF6");
        paymentRequest.setDeviceId(1L);
        paymentRequest.setAmount(BigDecimal.valueOf(500.00));

        Long customerId = 1L;

        when(paymentRepository.findByIdempotencyKey(paymentRequest.getIdempotencyKey()))
            .thenReturn(Optional.empty());
        
        when(paymentRepository.findTopByCustomerIdAndDeviceIdAndStatusOrderByCreatedAtDesc(
            customerId, paymentRequest.getDeviceId(), PaymentStatus.PENDING))
            .thenReturn(Optional.empty());

        Payment payment = new Payment();
        payment.setId(1L);
        payment.setCustomerId(customerId);
        payment.setDeviceId(paymentRequest.getDeviceId());
        payment.setAmount(paymentRequest.getAmount());
        payment.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.save(any(Payment.class)))
            .thenReturn(payment);


        // Act
        PaymentResponse paymentResponse = paymentService.createPayment(paymentRequest, customerId);

        // Assert
        assertEquals(1L, paymentResponse.getId());
        assertEquals(PaymentStatus.PENDING, paymentResponse.getStatus());

        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(paymentProducer, times(1)).publish(any(PaymentEvent.class));

    }


    @Test
    void shouldReturnExistingPayment_whenIdempotencyKeyExists() {
        CreatePaymentRequest paymentRequest = new CreatePaymentRequest();
        paymentRequest.setIdempotencyKey("HGD4262");
        paymentRequest.setDeviceId(1L);
        paymentRequest.setAmount(BigDecimal.valueOf(345.56));

        Long customerId = 1L;

        Payment payment = new Payment();
        payment.setId(3L);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setCustomerId(customerId);
        payment.setDeviceId(paymentRequest.getDeviceId());
        payment.setAmount(paymentRequest.getAmount());
        payment.setCreatedAt(LocalDateTime.now());

        // Assert
        when(paymentRepository.findByIdempotencyKey(paymentRequest.getIdempotencyKey()))
            .thenReturn(Optional.of(payment));

        // Act
        PaymentResponse paymentResponse = paymentService.createPayment(paymentRequest, customerId);

        // Assert
        assertNotNull(paymentResponse);
        assertEquals(3L, paymentResponse.getId());

        verify(paymentRepository, times(1))
                .findByIdempotencyKey(anyString());

        verify(paymentRepository, never()).save(any(Payment.class));
        verify(paymentProducer, never()).publish(any(PaymentEvent.class));

    }


    @Test
    void shouldReturnExistingPayment_whenPaymentAlreadyInProgress() {
        CreatePaymentRequest paymentRequest = new CreatePaymentRequest();
        paymentRequest.setIdempotencyKey("HGD4262");
        paymentRequest.setDeviceId(1L);
        paymentRequest.setAmount(BigDecimal.valueOf(345.56));

        Long customerId = 1L;

        Payment payment = new Payment();
        payment.setId(3L);
        payment.setIdempotencyKey(paymentRequest.getIdempotencyKey());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCustomerId(customerId);
        payment.setDeviceId(paymentRequest.getDeviceId());
    

        when(paymentRepository.findByIdempotencyKey(paymentRequest.getIdempotencyKey()))
            .thenReturn(Optional.empty());

        when(paymentRepository.findTopByCustomerIdAndDeviceIdAndStatusOrderByCreatedAtDesc(customerId, paymentRequest.getDeviceId(), PaymentStatus.PENDING))
            .thenReturn(Optional.of(payment));

        // Act
        PaymentResponse paymentResponse = paymentService.createPayment(paymentRequest, customerId);

        // Assert
        assertNotNull(paymentResponse);
        assertEquals(3L, paymentResponse.getId());
        assertEquals(customerId, paymentResponse.getCustomerId());
        assertEquals(paymentRequest.getDeviceId(), paymentResponse.getDeviceId());
        assertEquals(PaymentStatus.PENDING, paymentResponse.getStatus());

        verify(paymentRepository, times(1))
            .findByIdempotencyKey(paymentRequest.getIdempotencyKey());
        verify(paymentRepository, times(1))
            .findTopByCustomerIdAndDeviceIdAndStatusOrderByCreatedAtDesc(customerId, paymentRequest.getDeviceId(), PaymentStatus.PENDING);

        verify(paymentRepository, never()).save(any(Payment.class));
        verify(paymentProducer, never()).publish(any(PaymentEvent.class));

    }


    @Test
    void shouldThrowException_whenDuplicateInsertionOccurs() {
        // Arrange
        CreatePaymentRequest paymentRequest = new CreatePaymentRequest();
        paymentRequest.setIdempotencyKey("SGHJ1463HGF6");
        paymentRequest.setDeviceId(1L);
        paymentRequest.setAmount(BigDecimal.valueOf(500.00));

        Long customerId = 1L;

        when(paymentRepository.findByIdempotencyKey(paymentRequest.getIdempotencyKey()))
            .thenReturn(Optional.empty());
        
        when(paymentRepository.findTopByCustomerIdAndDeviceIdAndStatusOrderByCreatedAtDesc(
            customerId, paymentRequest.getDeviceId(), PaymentStatus.PENDING))
            .thenReturn(Optional.empty());

        Payment payment = new Payment();
        payment.setId(1L);
        payment.setCustomerId(customerId);
        payment.setIdempotencyKey(paymentRequest.getIdempotencyKey());
        payment.setDeviceId(paymentRequest.getDeviceId());
        payment.setAmount(paymentRequest.getAmount());
        payment.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.findByCustomerIdAndDeviceId(customerId, paymentRequest.getDeviceId()))
            .thenReturn(Optional.of(payment));

        when(paymentRepository.save(any(Payment.class)))
            .thenThrow(new DataIntegrityViolationException("Duplicate payment"));


        // Act
        PaymentResponse paymentResponse = paymentService.createPayment(paymentRequest, customerId);

        // Assert 
        assertEquals(1L, paymentResponse.getId());
        assertEquals(customerId, paymentResponse.getCustomerId());
        assertEquals(paymentRequest.getDeviceId(), paymentResponse.getDeviceId());
        assertEquals(PaymentStatus.PENDING, paymentResponse.getStatus());


        verify(paymentRepository, times(1))
            .findByIdempotencyKey(paymentRequest.getIdempotencyKey());
        verify(paymentRepository, times(1))
            .findTopByCustomerIdAndDeviceIdAndStatusOrderByCreatedAtDesc(customerId, paymentRequest.getDeviceId(), PaymentStatus.PENDING);
        verify(paymentRepository, never())
            .save(payment);
        verify(paymentRepository, times(1))
            .findByCustomerIdAndDeviceId(customerId, paymentRequest.getDeviceId());

    }


    @Test
    void shouldUpdatePaymentStatusSuccessfully() {

        Long paymentId = 1L;
        String paymentStatus = "SUCCESS";
        String txnId = "GJK574HBGF7BNHKk8";

        Payment payment = new Payment();
        payment.setId(1L);
        payment.setDeviceId(1L);
        payment.setAmount(BigDecimal.valueOf(100.00));
        payment.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.findById(paymentId))
            .thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment))
            .thenReturn(payment);

        DeviceAssignment deviceAssignment = new DeviceAssignment();
        deviceAssignment.setRemainingBalance(BigDecimal.valueOf(500.00));
        deviceAssignment.setDeviceId(payment.getDeviceId());

        Device device = new Device();
        device.setPaymentPlanType(PaymentPlanType.DAILY);

        when(assignmentRepository.findByDeviceId(payment.getDeviceId()))
            .thenReturn(Optional.of(deviceAssignment));
        when(deviceRepository.findById(payment.getDeviceId()))
            .thenReturn(Optional.of(device));
        when(assignmentRepository.save(deviceAssignment))
            .thenReturn(deviceAssignment);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doNothing().when(valueOperations).set("device:1:status", DeviceStatus.ACTIVE.name());

        // Act 
        paymentService.updatePaymentStatus(paymentId, paymentStatus, txnId);

        verify(paymentRepository, times(1))
              .findById(paymentId);
        verify(paymentRepository, times(1))
              .save(payment);
        verify(assignmentRepository, times(1))
              .findByDeviceId(payment.getDeviceId());
        verify(deviceRepository, times(1))
              .findById(payment.getDeviceId());
        verify(assignmentRepository, times(1))
              .save(deviceAssignment);
        
    }
}
