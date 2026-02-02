package com.icon.practise.paymentgateway.realtime.exception;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String userId) {
        super("User " + userId + " has insufficient funds");
    }

}
