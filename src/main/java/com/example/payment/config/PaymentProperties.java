package com.example.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashSet;
import java.util.Set;

@Data
@ConfigurationProperties(prefix = "payment")
public class PaymentProperties {
    private Set<String> supportedCurrencies = new HashSet<>();
}