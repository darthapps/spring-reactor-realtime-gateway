package com.icon.practise.paymentgateway.realtime.service;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Duration;

import static com.icon.practise.paymentgateway.realtime.service.UserService.NOT_FOUND_USER_ID;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


class UserServiceTest {

    private UserService testee;

    @BeforeEach
    public void setup() {
        testee = new UserService();
    }

    @Test
    public void shouldReturnUserNotFound() {
        StepVerifier.withVirtualTime(() ->
                        testee.userExists(NOT_FOUND_USER_ID))
                .expectSubscription()
                .expectNoEvent(Duration.ofMillis(1000))
                .consumeNextWith(actual -> assertThat(actual).isFalse())
                .verifyComplete();
    }

    @Test
    public void shouldReturnUserFound() {
        var publisher = testee.userExists("HR_user_" + Faker.instance().number().digits(5))
                .as(StepVerifier::create);
        publisher.expectSubscription()
                .consumeNextWith(result -> assertThat(result).isTrue())
                .verifyComplete();
    }

}
