package com.icon.practise.paymentgateway.realtime.model;

import lombok.Builder;

@Builder(toBuilder = true)
public record PaymentResponse(
        String txId,
        PaymentStatus status,
        String message
) {
}
