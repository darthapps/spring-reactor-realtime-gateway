package com.icon.practise.paymentgateway.realtime.service;

import com.icon.practise.paymentgateway.realtime.model.RiskLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


class RiskServiceTest {

    private static final String HIGH_RISK_USER = "HR_user_1111";
    private static final String LOW_RISK_USER = "LR_user_1111";
    private static final String MEDIUM_RISK_USER = "MR_user_1111";

    private RiskService testee;

    @BeforeEach
    public void setup() {
        testee = new RiskService();
    }

    @Test
    public void shouldReturnMediumRisk() {
        var publisher = testee.checkRisk(MEDIUM_RISK_USER).as(StepVerifier::create);
        publisher
                .expectSubscription()
                .consumeNextWith(result -> assertThat(result).isEqualTo(RiskLevel.MEDIUM))
                .verifyComplete();
    }

    @Test
    public void shouldReturnLowRisk() {
        var publisher = testee.checkRisk(LOW_RISK_USER).as(StepVerifier::create);
        publisher
                .expectSubscription()
                .consumeNextWith(result -> assertThat(result).isEqualTo(RiskLevel.LOW))
                .verifyComplete();
    }

    @Test
    public void shouldReturnHighRisk() {
        var publisher = testee.checkRisk(HIGH_RISK_USER).as(StepVerifier::create);
        publisher
                .expectSubscription()
                .consumeNextWith(result -> assertThat(result).isEqualTo(RiskLevel.HIGH))
                .verifyComplete();
    }

}
