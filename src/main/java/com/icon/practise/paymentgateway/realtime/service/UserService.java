package com.icon.practise.paymentgateway.realtime.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@Slf4j
public class UserService {

    public static final String NOT_FOUND_USER_ID = "HR_99999";

    public Mono<Boolean> userExists(final String userId) {
        return userId.equals(NOT_FOUND_USER_ID) ? Mono.just(false).delayElement(Duration.ofMillis(1000))
                : Mono.just(true).delayElement(Duration.ofMillis(1000));
    }
}

