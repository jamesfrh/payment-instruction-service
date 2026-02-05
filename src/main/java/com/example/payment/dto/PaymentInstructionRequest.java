package com.example.payment.dto;


import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentInstructionRequest {
    @NotBlank
    @Size(max = 64)
    private String instructionId;

    @NotBlank
    @Size(max = 64)
    private String sourceSystem;

    @NotBlank
    @Size(max = 64)
    private String payerAccount;

    @NotBlank
    @Size(max = 64)
    private String payeeAccount;

    @NotNull
    @DecimalMin(value = "0.0001")
    private BigDecimal amount;

    @NotBlank
    @Pattern(regexp = "^[A-Z]{3}$", message = "currency must be a 3 letter ISO code")
    private String currency;

    @NotNull
    private LocalDate requestedExecutionDate;
}
