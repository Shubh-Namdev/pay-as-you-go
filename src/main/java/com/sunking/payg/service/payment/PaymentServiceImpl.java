package com.sunking.payg.service.payment;

import com.sunking.payg.dto.CreatePaymentRequest;
import com.sunking.payg.entity.Payment;
import com.sunking.payg.enums.PaymentStatus;
import com.sunking.payg.kafka.event.PaymentEvent;
import com.sunking.payg.kafka.producer.PaymentProducer;
import com.sunking.payg.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentProducer paymentProducer;

    @Override
    public Long createPayment(CreatePaymentRequest request) {

        Payment payment = new Payment();
        payment.setCustomerId(request.getCustomerId());
        payment.setDeviceId(request.getDeviceId());
        payment.setAmount(request.getAmount());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentDate(LocalDateTime.now());

        Payment saved = paymentRepository.save(payment);

        PaymentEvent event = new PaymentEvent(
                saved.getId(),
                saved.getCustomerId(),
                saved.getDeviceId(),
                saved.getAmount()
        );

        paymentProducer.publish(event);

        return saved.getId();
    }
}