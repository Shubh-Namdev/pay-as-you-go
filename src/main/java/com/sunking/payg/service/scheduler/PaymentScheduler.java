package com.sunking.payg.service.scheduler;

import com.sunking.payg.entity.Payment;
import com.sunking.payg.enums.PaymentStatus;
import com.sunking.payg.repository.PaymentRepository;
import com.sunking.payg.service.integration.PaymentGatewayService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentScheduler {

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayService paymentGatewayService;

    // Mark stuck payments as FAILED
    @Scheduled(fixedRate = 60000)
    public void markStuckPaymentsAsFailed() {

        int page = 0;
        int size = 100;

        Page<Payment> result;

        do {
            result = paymentRepository.findByStatusAndCreatedAtBefore(
                    PaymentStatus.PENDING,
                    LocalDateTime.now().minusMinutes(5),
                    PageRequest.of(page, size)
            );

            for (Payment payment : result.getContent()) {
                payment.setStatus(PaymentStatus.FAILED);
                log.info("marked payment as FAILED with id {}", payment.getExternalTxnId());
            }

            paymentRepository.saveAll(result.getContent()); // batch update

            page++;

        } while (!result.isLast());
    }


    // Reconcile with external gateway
    @Scheduled(fixedRate = 300000)
    public void reconcilePayments() {

        int page = 0;
        int size = 100;

        Page<Payment> result;

        do {
            result = paymentRepository.findByStatus(
                    PaymentStatus.PENDING,
                    PageRequest.of(page, size)
            );

            for (Payment payment : result.getContent()) {

                try{
                    boolean success = paymentGatewayService.checkStatus(payment.getId());

                    if (success) 
                        payment.setStatus(PaymentStatus.SUCCESS);
                    log.info("Payment {} reconciled SUCCESS", payment.getId());
                }catch (Exception ex) {
                    log.error("Error while reconciling payment {}", payment.getId(), ex);
                }
            }

            paymentRepository.saveAll(result.getContent()); // batch update

            page++;

        } while (!result.isLast());
    }
}



