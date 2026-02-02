package com.icon.practise.paymentgateway.realtime.model;

import lombok.Builder;

import java.math.BigDecimal;

@Builder(toBuilder = true)
public record PaymentRequest(
        String userId,
        BigDecimal amount,
        String targetAccount
) {
}
