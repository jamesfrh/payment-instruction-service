package com.example.payment.repository;

import com.example.payment.entity.PaymentInstructionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentInstructionRepository extends JpaRepository<PaymentInstructionEntity, String> {
}
