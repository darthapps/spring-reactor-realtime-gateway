package com.icon.practise.paymentgateway.realtime.controller;

import com.github.javafaker.Faker;
import com.icon.practise.paymentgateway.realtime.model.PaymentRequest;
import com.icon.practise.paymentgateway.realtime.model.PaymentResponse;
import com.icon.practise.paymentgateway.realtime.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

import static com.icon.practise.paymentgateway.realtime.model.PaymentStatus.SUCCESS;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = PaymentController.class)
class PaymentControllerTest {


    private final Faker faker = Faker.instance();

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private PaymentService paymentService;

    @Test
    public void shouldProcessPaymentSuccessfully() {

        final var request = PaymentRequest
                .builder()
                .amount(BigDecimal.valueOf(100))
                .targetAccount(faker.number().digits(10))
                .build();

        final var response = PaymentResponse
                .builder()
                .txId(UUID.randomUUID().toString())
                .status(SUCCESS)
                .build();

        when(paymentService.processPayment(request))
                .thenReturn(Mono.just(response));

        webTestClient.post()
                .uri("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(PaymentResponse.class)
                .isEqualTo(response);
    }

}