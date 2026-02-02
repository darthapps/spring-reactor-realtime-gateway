package com.icon.practise.paymentgateway.realtime.repository;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "payments")
@Data
@Builder(toBuilder = true)
public class Payment {
    @Id
    private String id;
    private String userId;
    private BigDecimal amount;
    private String status; // ACCEPTED, REJECTED_RISK, REJECTED_FUNDS
    private LocalDateTime createdAt;
}