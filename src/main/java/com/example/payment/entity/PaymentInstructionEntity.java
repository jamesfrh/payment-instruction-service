package com.example.payment.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "payment_instruction")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
//For JPA entities, I prefer explicit equality semantics and to avoid Lombok-generated equals/hashCode pulling in lazy fields.”
public class PaymentInstructionEntity {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "instruction_id", length = 64, nullable = false)
    private String instructionId;

    @Column(name = "source_system", nullable = false)
    private String sourceSystem;

    @Column(name = "payer_account", nullable = false)
    private String payerAccount;

    @Column(name = "payee_account", nullable = false)
    private String payeeAccount;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "requested_execution_date", nullable = false)
    private LocalDate requestedExecutionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
