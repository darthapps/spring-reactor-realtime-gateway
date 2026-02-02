package com.icon.practise.paymentgateway.realtime.service;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class LedgerServiceTest {

    private LedgerService testee;

    private static final Faker faker = Faker.instance();

    @BeforeEach
    public void setup() {
        testee = new LedgerService();
    }

    @Test
    public void shouldReturnInsufficientFunds() {
        final var userId = "HR_" + faker.number().digits(5);
        var publisher = testee.hasSufficientFunds(userId, BigDecimal.valueOf(5000)).as(StepVerifier::create);
        publisher.consumeNextWith(response -> {
            assertThat(response).isFalse();
        }).verifyComplete();
    }

    @Test
    public void shouldReturnSufficientFundsForHighRiskUser() {
        final var userId = "HR_" + faker.number().digits(5);
        var publisher = testee.hasSufficientFunds(userId, BigDecimal.valueOf(100)).as(StepVerifier::create);
        publisher.consumeNextWith(response -> {
            assertThat(response).isTrue();
        }).verifyComplete();
    }

    @Test
    public void shouldReturnSufficientFundsForLowRiskUser() {
        final var userId = "LR_" + faker.number().digits(5);
        var publisher = testee.hasSufficientFunds(userId, BigDecimal.valueOf(100)).as(StepVerifier::create);
        publisher.consumeNextWith(response -> {
            assertThat(response).isTrue();
        }).verifyComplete();
    }

}