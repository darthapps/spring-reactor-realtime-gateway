package com.icon.practise.paymentgateway.realtime.mapper;

import com.icon.practise.paymentgateway.realtime.model.PaymentRequest;
import com.icon.practise.paymentgateway.realtime.model.PaymentResponse;
import com.icon.practise.paymentgateway.realtime.model.PaymentStatus;
import com.icon.practise.paymentgateway.realtime.repository.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.icon.practise.paymentgateway.realtime.model.PaymentStatus.SUCCESS;

@Slf4j
@Component
public class PaymentMapper {

    public PaymentResponse success(final Payment payment) {
        return PaymentResponse.builder()
                .status(PaymentStatus.SUCCESS)
                .txId(payment.getId())
                .build();
    }

    public PaymentResponse highRisk() {
        return PaymentResponse.builder()
                .status(PaymentStatus.RISK_REJECTED)
                .message("Rejected due to high risk")
                .build();
    }

    public PaymentResponse insufficientFunds() {
        return PaymentResponse.builder()
                .status(PaymentStatus.FUNDS_REJECTED)
                .message("Rejected due to insufficient funds")
                .build();
    }

    public Payment mapToPayment(final PaymentRequest paymentRequest) {
        return Payment.builder()
                .status(SUCCESS.name())
                .userId(paymentRequest.userId())
                .amount(paymentRequest.amount())
                .id(UUID.randomUUID().toString())
                .build();
    }

    public PaymentResponse userNotFound(final String userId) {
        return PaymentResponse.builder()
                .status(PaymentStatus.USER_NOT_FOUND)
                .message("User " + userId + " does not exist")
                .build();
    }
}
