package com.icon.practise.paymentgateway.realtime.service;


import com.icon.practise.paymentgateway.realtime.model.RiskLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@Slf4j
public class RiskService {

    public Mono<RiskLevel> checkRisk(String userId) {

        if (userId.startsWith("HR")) {
            return Mono.just(RiskLevel.HIGH)
                    .delayElement(Duration.ofSeconds(2));
        }
        if (userId.startsWith("MR")) {
            return Mono.just(RiskLevel.MEDIUM)
                    .delayElement(Duration.ofSeconds(2));
        }
        if (userId.startsWith("LR")) {
            return Mono.just(RiskLevel.LOW)
                    .delayElement(Duration.ofMillis(100));
        }
        return Mono.empty();
    }
}
