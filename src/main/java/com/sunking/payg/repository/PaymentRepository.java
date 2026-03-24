package com.sunking.payg.repository;

import com.sunking.payg.entity.Payment;
import com.sunking.payg.enums.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

        Optional<Payment> findByIdempotencyKey(String idempotencyKey);

        Optional<Payment> findTopByCustomerIdAndDeviceIdAndStatusOrderByCreatedAtDesc(
                                Long customerId,
                                Long deviceId,
                                PaymentStatus status
                        );

        Optional<Payment> findByCustomerIdAndDeviceId(Long customerId, Long deviceId);
        
        Page<Payment> findByStatusAndCreatedAtBefore(
                        PaymentStatus status,
                        LocalDateTime time,
                        Pageable pageable
                );

        Page<Payment> findByStatus(PaymentStatus status, Pageable pageable );
}