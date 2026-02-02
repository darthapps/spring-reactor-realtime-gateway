package com.icon.practise.paymentgateway.realtime.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@Configuration
public class PaymentGatewayConfig {

    @Autowired
    private ReactiveMongoTemplate mongoTemplate;
}
