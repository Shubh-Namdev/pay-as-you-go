package com.sunking.payg.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.sunking.payg.enums.PaymentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
@AllArgsConstructor
public class PaymentResponse {
    
    private Long id;
    private Long customerId;
    private Long deviceId;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private PaymentStatus status;
    private String externalTxnId;
}
