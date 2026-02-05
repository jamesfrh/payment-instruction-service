package com.example.payment.service;

import com.example.payment.dto.PaymentInstructionRequest;
import com.example.payment.dto.PaymentInstructionResponse;
import com.example.payment.entity.PaymentEventType;
import com.example.payment.entity.PaymentInstructionEntity;
import com.example.payment.entity.PaymentInstructionEventEntity;
import com.example.payment.entity.PaymentStatus;
import com.example.payment.exception.BadRequestException;
import com.example.payment.exception.ConflictException;
import com.example.payment.exception.NotFoundException;
import com.example.payment.repository.PaymentInstructionEventRepository;
import com.example.payment.repository.PaymentInstructionRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class PaymentInstructionService {

    private static final Set<String> SUPPORTED_CURRENCIES = Set.of("SGD", "MYR", "USD");

    private final PaymentInstructionRepository instructionRepo;
    private final PaymentInstructionEventRepository eventRepo;

    @Transactional
    public SubmitResult submit(PaymentInstructionRequest req) {

        // 1) Idempotency check
        var existingOpt = instructionRepo.findById(req.getInstructionId());
        if (existingOpt.isPresent()) {
            var existing = existingOpt.get();

            if (isSamePayload(existing, req)) {
                return new SubmitResult(toResponse(existing), false);
            }

            writeEvent(existing.getInstructionId(),
                    PaymentEventType.DUPLICATE_CONFLICT,
                    existing.getStatus(),
                    existing.getStatus(),
                    "InstructionId exists with different payload");
            throw new ConflictException("InstructionId already exists with different payload"); //throw 409
        }

        //Business validation before save >400
        validateBusinessRulesOrThrow(req);

        //Persist new valid instruction
        Instant now = Instant.now();
        PaymentInstructionEntity entity = new PaymentInstructionEntity();
        entity.setInstructionId(req.getInstructionId());
        entity.setSourceSystem(req.getSourceSystem());
        entity.setPayerAccount(req.getPayerAccount());
        entity.setPayeeAccount(req.getPayeeAccount());
        entity.setAmount(req.getAmount());
        entity.setCurrency(req.getCurrency());
        entity.setRequestedExecutionDate(req.getRequestedExecutionDate());

        entity.setStatus(PaymentStatus.VALIDATED);
        entity.setFailureReason(null);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        instructionRepo.save(entity);
        writeEvent(entity.getInstructionId(), PaymentEventType.CREATED, null, PaymentStatus.VALIDATED, "Instruction created (validated)");

        return new SubmitResult(toResponse(entity), true); //201
    }

    private static PaymentInstructionEntity getPaymentInstructionEntity(PaymentInstructionRequest req, Instant now) {
        PaymentInstructionEntity entity = new PaymentInstructionEntity();
        entity.setInstructionId(req.getInstructionId());
        entity.setSourceSystem(req.getSourceSystem());
        entity.setPayerAccount(req.getPayerAccount());
        entity.setPayeeAccount(req.getPayeeAccount());
        entity.setAmount(req.getAmount());
        entity.setCurrency(req.getCurrency());
        entity.setRequestedExecutionDate(req.getRequestedExecutionDate());
        entity.setStatus(PaymentStatus.RECEIVED);
        entity.setFailureReason(null);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return entity;
    }

    @Transactional(readOnly = true)
    public PaymentInstructionResponse getById(String instructionId) {
        var entity = instructionRepo.findById(instructionId)
                .orElseThrow(() -> new NotFoundException("Instruction not found: " + instructionId));
        return toResponse(entity);
    }

    private void transition(PaymentInstructionEntity entity, PaymentStatus newStatus, String failureReason) {
        entity.setStatus(newStatus);
        entity.setFailureReason(failureReason);
        entity.setUpdatedAt(Instant.now());
    }


//    Return null when valid; otherwise return a reason for REJECTED.
    private void validateBusinessRulesOrThrow(PaymentInstructionRequest req) {
        if (Objects.equals(req.getPayerAccount(), req.getPayeeAccount())) {
            throw new BadRequestException("payerAccount and payeeAccount must be different");
        }

        LocalDate today = LocalDate.now();
        if (req.getRequestedExecutionDate().isBefore(today)) {
            throw new BadRequestException("requestedExecutionDate must be today or later");
        }

        if (!SUPPORTED_CURRENCIES.contains(req.getCurrency())) {
            throw new BadRequestException("Unsupported currency: " + req.getCurrency());
        }
    }

    private boolean isSamePayload(PaymentInstructionEntity e, PaymentInstructionRequest r) {
        return Objects.equals(e.getSourceSystem(), r.getSourceSystem())
                && Objects.equals(e.getPayerAccount(), r.getPayerAccount())
                && Objects.equals(e.getPayeeAccount(), r.getPayeeAccount())
                && e.getAmount().compareTo(r.getAmount()) == 0
                && Objects.equals(e.getCurrency(), r.getCurrency())
                && Objects.equals(e.getRequestedExecutionDate(), r.getRequestedExecutionDate());
    }

    private void writeEvent(String instructionId,
                            PaymentEventType type,
                            PaymentStatus oldStatus,
                            PaymentStatus newStatus,
                            String details) {
        PaymentInstructionEventEntity ev = new PaymentInstructionEventEntity();
        ev.setInstructionId(instructionId);
        ev.setEventType(type);
        ev.setOldStatus(oldStatus);
        ev.setNewStatus(newStatus);
        ev.setEventTime(Instant.now());
        ev.setDetails(details);
        eventRepo.save(ev);
    }

    private PaymentInstructionResponse toResponse(PaymentInstructionEntity e) {
        return PaymentInstructionResponse.builder()
                .instructionId(e.getInstructionId())
                .sourceSystem(e.getSourceSystem())
                .payerAccount(e.getPayerAccount())
                .payeeAccount(e.getPayeeAccount())
                .amount(e.getAmount())
                .currency(e.getCurrency())
                .requestedExecutionDate(e.getRequestedExecutionDate())
                .status(e.getStatus())
                .failureReason(e.getFailureReason())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    @Getter
    @AllArgsConstructor
    public static class SubmitResult {
        private final PaymentInstructionResponse response;
        private final boolean created;
    }
}
