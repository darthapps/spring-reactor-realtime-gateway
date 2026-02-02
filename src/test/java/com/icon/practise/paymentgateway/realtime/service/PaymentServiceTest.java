package com.icon.practise.paymentgateway.realtime.service;

import com.github.javafaker.Faker;
import com.icon.practise.paymentgateway.realtime.mapper.PaymentMapper;
import com.icon.practise.paymentgateway.realtime.model.PaymentRequest;
import com.icon.practise.paymentgateway.realtime.model.PaymentResponse;
import com.icon.practise.paymentgateway.realtime.model.RiskLevel;
import com.icon.practise.paymentgateway.realtime.repository.Payment;
import com.icon.practise.paymentgateway.realtime.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static com.icon.practise.paymentgateway.realtime.model.PaymentStatus.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    private static final Faker faker = Faker.instance();

    @Mock
    private UserService userService;

    @Mock
    private LedgerService ledgerService;

    @Mock
    private RiskService riskService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentService testee;

    @Test
    public void shouldRejectPaymentDueToUserNotFound() {
        final var userId = "HR_" + faker.number().digits(5);
        final var paymentRequest = PaymentRequest
                .builder()
                .amount(BigDecimal.valueOf(15000))
                .targetAccount(faker.number().digits(10))
                .userId(userId)
                .build();
        final var paymentResponse = PaymentResponse
                .builder()
                .status(USER_NOT_FOUND)
                .message("User " + userId + " does not exist")
                .build();

        when(userService.userExists(userId))
                .thenReturn(Mono.just(false));
        when(paymentMapper.userNotFound(userId))
                .thenReturn(paymentResponse);

        var publisher = testee.processPayment(paymentRequest).as(StepVerifier::create);
        publisher.consumeNextWith(result -> {
            assertThat(result.status()).isEqualTo(USER_NOT_FOUND);
            assertThat(result.message()).isEqualTo("User " + userId + " does not exist");
            assertThat(result.txId()).isNull();
        }).verifyComplete();
        verify(userService).userExists(userId);
        verify(paymentMapper).userNotFound(userId);
        verifyNoInteractions(riskService, ledgerService);

    }

    @Test
    public void shouldRejectPaymentDueToHighRisk() {
        final var userId = "HR_" + faker.number().digits(5);
        final var paymentRequest = PaymentRequest
                .builder()
                .amount(BigDecimal.valueOf(15000))
                .targetAccount(faker.number().digits(10))
                .userId(userId)
                .build();
        final var paymentResponse = PaymentResponse
                .builder()
                .status(RISK_REJECTED)
                .message("Rejected due to high risk")
                .build();

        when(paymentMapper.highRisk())
                .thenReturn(paymentResponse);
        when(userService.userExists(userId))
                .thenReturn(Mono.just(true));
        when(riskService.checkRisk(any()))
                .thenReturn(Mono.just(RiskLevel.HIGH));
        when(ledgerService.hasSufficientFunds(userId, paymentRequest.amount()))
                .thenReturn(Mono.just(true));

        var publisher = testee.processPayment(paymentRequest).as(StepVerifier::create);

        publisher.consumeNextWith(result -> {
            assertThat(result.status()).isEqualTo(RISK_REJECTED);
            assertThat(result.txId()).isNull();
            assertThat(result.message()).isEqualTo("Rejected due to high risk");
        }).verifyComplete();

        verify(paymentMapper).highRisk();
        verify(riskService).checkRisk(paymentRequest.userId());
        verify(userService).userExists(paymentRequest.userId());
        verify(ledgerService).hasSufficientFunds(userId, paymentRequest.amount());
    }

    @Test
    public void shouldRejectPaymentDueToInsufficientFunds() {
        final var userId = "MR_" + faker.number().digits(5);
        final var paymentRequest = PaymentRequest
                .builder()
                .amount(BigDecimal.valueOf(15000))
                .targetAccount(faker.number().digits(10))
                .userId(userId)
                .build();

        final var paymentResponse = PaymentResponse
                .builder()
                .status(FUNDS_REJECTED)
                .message("Rejected due to insufficient funds")
                .build();

        when(paymentMapper.insufficientFunds())
                .thenReturn(paymentResponse);
        when(userService.userExists(userId))
                .thenReturn(Mono.just(true));
        when(riskService.checkRisk(any()))
                .thenReturn(Mono.just(RiskLevel.MEDIUM));
        when(ledgerService.hasSufficientFunds(userId, paymentRequest.amount()))
                .thenReturn(Mono.just(false));

        var publisher = testee.processPayment(paymentRequest).as(StepVerifier::create);

        publisher.consumeNextWith(result -> {
            assertThat(result.status()).isEqualTo(FUNDS_REJECTED);
            assertThat(result.txId()).isNull();
            assertThat(result.message()).isEqualTo("Rejected due to insufficient funds");
        }).verifyComplete();

        verify(paymentMapper).insufficientFunds();
        verify(paymentMapper).insufficientFunds();
        verify(riskService).checkRisk(paymentRequest.userId());
        verify(userService).userExists(paymentRequest.userId());
        verify(ledgerService).hasSufficientFunds(userId, paymentRequest.amount());
    }


    @Test
    public void shouldProcessPaymentSuccessfully() {
        final var userId = "MR_" + faker.number().digits(5);
        final var txId = UUID.randomUUID().toString();
        final var paymentRequest = PaymentRequest
                .builder()
                .amount(BigDecimal.valueOf(15000))
                .targetAccount(faker.number().digits(10))
                .userId(userId)
                .build();
        final var paymentResponse = PaymentResponse
                .builder()
                .status(SUCCESS)
                .txId(txId)
                .build();
        final var payment = Payment
                .builder()
                .amount(paymentRequest.amount())
                .id(txId)
                .userId(paymentRequest.userId())
                .build();

        when(paymentMapper.success(eq(payment)))
                .thenReturn(paymentResponse);
        when(userService.userExists(userId))
                .thenReturn(Mono.just(true));
        when(riskService.checkRisk(any()))
                .thenReturn(Mono.just(RiskLevel.MEDIUM));
        when(ledgerService.hasSufficientFunds(userId, paymentRequest.amount()))
                .thenReturn(Mono.just(true));
        when(paymentRepository.save(any()))
                .thenReturn(Mono.just(payment));

        var publisher = testee.processPayment(paymentRequest).as(StepVerifier::create);

        publisher.consumeNextWith(result -> {
            assertThat(result.status()).isEqualTo(SUCCESS);
            assertThat(result.txId()).isEqualTo(txId);
        }).verifyComplete();

        verify(paymentMapper).success(payment);
        verify(riskService).checkRisk(paymentRequest.userId());
        verify(userService).userExists(paymentRequest.userId());
        verify(ledgerService).hasSufficientFunds(userId, paymentRequest.amount());
    }

}
