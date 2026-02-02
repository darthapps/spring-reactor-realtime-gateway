package com.icon.practise.paymentgateway.realtime.controller;


import com.icon.practise.paymentgateway.realtime.model.PaymentRequest;
import com.icon.practise.paymentgateway.realtime.model.PaymentResponse;
import com.icon.practise.paymentgateway.realtime.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public Mono<ResponseEntity<PaymentResponse>> process(@RequestBody PaymentRequest paymentRequest) {
        return paymentService.processPayment(paymentRequest)
                .map(result -> switch (result.status()) {
                    case SUCCESS -> ResponseEntity.ok().body(result);
                    case RISK_REJECTED -> ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
                    case USER_NOT_FOUND -> ResponseEntity.notFound().build();
                    case FUNDS_REJECTED -> ResponseEntity.unprocessableEntity().build();
                });
    }

}
