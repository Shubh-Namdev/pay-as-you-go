package com.sunking.payg.service.scheduler;

import com.sunking.payg.entity.Payment;
import com.sunking.payg.enums.PaymentStatus;
import com.sunking.payg.repository.PaymentRepository;
import com.sunking.payg.service.integration.PaymentGatewayService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PaymentScheduler {

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayService paymentGatewayService;

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
            }

            paymentRepository.saveAll(result.getContent()); // batch update

            page++;

        } while (!result.isLast());
    }


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

                boolean success = paymentGatewayService.checkStatus(payment.getId());

                if (success) {
                    payment.setStatus(PaymentStatus.SUCCESS);
                }
            }

            paymentRepository.saveAll(result.getContent()); // batch update

            page++;

        } while (!result.isLast());
    }
}



// @Scheduled(fixedRate = 60000) // every 1 min
    // public void markStuckPaymentsAsFailed() {

    //     List<Payment> stuckPayments =
    //             paymentRepository.findByStatusAndCreatedAtBefore(
    //                     PaymentStatus.PENDING,
    //                     LocalDateTime.now().minusMinutes(5)
    //             );

    //     for (Payment payment : stuckPayments) {
    //         payment.setStatus(PaymentStatus.FAILED);
    //         paymentRepository.save(payment);

    //         System.out.println("Marked as FAILED (timeout): " + payment.getId());
    //     }
    // }


// @Scheduled(fixedRate = 300000) // every 5 min
//     public void reconcilePayments() {

//         List<Payment> pendingPayments =
//                 paymentRepository.findByStatus(PaymentStatus.PENDING);

//         for (Payment payment : pendingPayments) {

//             // call gateway status API (mock for now)
//             boolean success = paymentGatewayService.checkStatus(payment.getId());

//             if (success) {
//                 payment.setStatus(PaymentStatus.SUCCESS);
//                 paymentRepository.save(payment);

//                 System.out.println("Reconciled SUCCESS: " + payment.getId());
//             }

//             System.out.println("Payment was not completed");
//         }
//     }