package com.example.payment.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class ApiError {
    private String code;
    private String message;
    private Instant timestamp;
}
