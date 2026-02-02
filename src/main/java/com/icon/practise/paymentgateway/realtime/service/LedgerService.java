package com.icon.practise.paymentgateway.realtime.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
@Slf4j
public class LedgerService {

    public Mono<Boolean> hasSufficientFunds(final String userId, final BigDecimal amount) {
        log.info("userId {} and amount {} are being checked for sufficient funds", userId, amount);
        if (userId.startsWith("HR")) {
            log.info("There is no sufficient balance");
            return Mono.just(amount.compareTo(BigDecimal.valueOf(1000)) <= 0);
        }
        if (userId.startsWith("LR")) {
            return Mono.just(true);
        }
        return  amount.compareTo(BigDecimal.valueOf(1000)) < 0 ? Mono.just(false) : Mono.just(true);
    }
}
