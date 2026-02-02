package com.icon.practise.paymentgateway.realtime.config;

import com.icon.practise.paymentgateway.realtime.repository.PaymentRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@Configuration
@EnableReactiveMongoRepositories(basePackageClasses = PaymentRepository.class)
@EnableReactiveMongoAuditing
public class DatabaseConfig {
}
