package com.example.payment.dto;

import com.example.payment.entity.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
public class PaymentInstructionResponse {
    private String instructionId;
    private String sourceSystem;
    private String payerAccount;
    private String payeeAccount;
    private BigDecimal amount;
    private String currency;
    private LocalDate requestedExecutionDate;

    private PaymentStatus status;
    private String failureReason;
    private Instant createdAt;
    private Instant updatedAt;
}
