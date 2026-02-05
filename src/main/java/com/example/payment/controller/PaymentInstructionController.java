package com.example.payment.controller;

import com.example.payment.dto.PaymentInstructionRequest;
import com.example.payment.dto.PaymentInstructionResponse;
import com.example.payment.service.PaymentInstructionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/payment-instructions")
@RequiredArgsConstructor
public class PaymentInstructionController {

    private final PaymentInstructionService paymentInstructionService;

    @PostMapping
    public ResponseEntity<PaymentInstructionResponse> submit(@Valid @RequestBody PaymentInstructionRequest request) {
        var result = paymentInstructionService.submit(request);

        if (result.isCreated()) {
            return ResponseEntity
                    .created(URI.create("/payment-instructions/" + result.getResponse().getInstructionId()))
                    .body(result.getResponse());
        }
        return ResponseEntity.ok(result.getResponse());
    }

    @GetMapping("/{instructionId}")
    public ResponseEntity<PaymentInstructionResponse> getStatus(@PathVariable String instructionId) {
        return ResponseEntity.ok(paymentInstructionService.getById(instructionId));
    }
}
