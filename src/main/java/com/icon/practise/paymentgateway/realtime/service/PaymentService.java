package com.icon.practise.paymentgateway.realtime.service;

import com.icon.practise.paymentgateway.realtime.exception.InsufficientFundsException;
import com.icon.practise.paymentgateway.realtime.exception.RiskCheckException;
import com.icon.practise.paymentgateway.realtime.exception.UserNotFoundException;
import com.icon.practise.paymentgateway.realtime.mapper.PaymentMapper;
import com.icon.practise.paymentgateway.realtime.model.PaymentRequest;
import com.icon.practise.paymentgateway.realtime.model.PaymentResponse;
import com.icon.practise.paymentgateway.realtime.model.RiskLevel;
import com.icon.practise.paymentgateway.realtime.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

import java.math.BigDecimal;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final UserService userService;
    private final LedgerService ledgerService;
    private final RiskService riskService;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    public Mono<PaymentResponse> processPayment(final PaymentRequest paymentRequest) {
        log.info("processPayment request {}", paymentRequest);
        return userService.userExists(paymentRequest.userId())
                .filter(exists -> exists)
                .switchIfEmpty(Mono.error(new UserNotFoundException(paymentRequest.userId())))
                .doOnNext(exists -> log.info("user {} exists {}", paymentRequest.userId(), exists))
                .flatMap(exists -> {
                    final var riskCheck = getResilientRiskCheck(paymentRequest.userId());
                    final var fundsCheck = getResilientFundsCheck(paymentRequest.userId(), paymentRequest.amount());
                    return Mono.zip(riskCheck, fundsCheck)
                            .filter(tuple -> tuple.getT1() != RiskLevel.HIGH)
                            .switchIfEmpty(Mono.error(new RiskCheckException(paymentRequest.userId())))
                            .filter(Tuple2::getT2)
                            .switchIfEmpty(Mono.error(new InsufficientFundsException(paymentRequest.userId())))
                            .flatMap(tuple -> {
                                final var payment = paymentMapper.mapToPayment(paymentRequest);
                                log.info("payment is {}", payment);
                                return paymentRepository.save(payment)
                                        .map(paymentMapper::success);
                            });
                })
                .onErrorResume(UserNotFoundException.class, e -> Mono.just(paymentMapper.userNotFound(paymentRequest.userId())))
                .onErrorResume(RiskCheckException.class, e -> Mono.just(paymentMapper.highRisk()))
                .onErrorResume(InsufficientFundsException.class, e -> Mono.just(paymentMapper.insufficientFunds()))
                .doOnNext(payment -> log.info("Payment {} processed successfully.", payment));
    }

    private Mono<RiskLevel> getResilientRiskCheck(final String userId) {
        return riskService.checkRisk(userId)
                .subscribeOn(Schedulers.boundedElastic())
                .timeout(Duration.ofMillis(500))
                .onErrorReturn(RiskLevel.HIGH)
                .retry(1);
    }

    private Mono<Boolean> getResilientFundsCheck(final String userId, final BigDecimal amount) {
        return ledgerService.hasSufficientFunds(userId, amount)
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorReturn(false);
    }

}
