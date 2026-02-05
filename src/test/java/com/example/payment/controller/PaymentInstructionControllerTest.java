package com.example.payment.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;


@SpringBootTest
@AutoConfigureMockMvc
public class PaymentInstructionControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    private Map<String, Object> validRequest(String instructionId) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("instructionId", instructionId);
        body.put("sourceSystem", "CRM");
        body.put("payerAccount", "ACC-1");
        body.put("payeeAccount", "ACC-2");
        body.put("amount", new BigDecimal("100.50"));
        body.put("currency", "SGD");
        body.put("requestedExecutionDate", LocalDate.now().plusDays(1).toString());
        return body;
    }


    @Test
    void post_valid_creates201() throws Exception {
        var body = validRequest("INS-201");

        mockMvc.perform(post("/payment-instructions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/payment-instructions/INS-201")))
                .andExpect(jsonPath("$.instructionId").value("INS-201"))
                .andExpect(jsonPath("$.status").value("VALIDATED"))
                .andExpect(jsonPath("$.failureReason").doesNotExist())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void post_duplicate_samePayload_returns200() throws Exception {
        var body = validRequest("INS-DUP-OK");

        // First submit -> 201
        mockMvc.perform(post("/payment-instructions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());

        // Same payload -> 200
        mockMvc.perform(post("/payment-instructions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.instructionId").value("INS-DUP-OK"))
                .andExpect(jsonPath("$.status").value("VALIDATED"));
    }

    @Test
    void post_duplicate_differentPayload_returns409() throws Exception {
        var body = validRequest("INS-DUP-CONFLICT");

        mockMvc.perform(post("/payment-instructions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());

        // Change amount, keep same instructionId
        body.put("amount", new BigDecimal("999.99"));

        mockMvc.perform(post("/payment-instructions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"))
                .andExpect(jsonPath("$.message", containsString("different payload")));
    }

    @Test
    void post_payerEqualsPayee_returns400() throws Exception {
        var body = validRequest("INS-BAD-400");
        body.put("payeeAccount", "ACC-1"); // same as payer

        mockMvc.perform(post("/payment-instructions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(anyOf(is("BAD_REQUEST"), is("VALIDATION_ERROR"))))
                .andExpect(jsonPath("$.message", containsString("must be different")));
    }

    @Test
    void get_missing_returns404() throws Exception {
        mockMvc.perform(get("/payment-instructions/INS-NOT-FOUND"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void get_existing_returns200() throws Exception {
        var body = validRequest("INS-GET-200");

        mockMvc.perform(post("/payment-instructions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/payment-instructions/INS-GET-200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.instructionId").value("INS-GET-200"))
                .andExpect(jsonPath("$.status").value("VALIDATED"));
    }
}
