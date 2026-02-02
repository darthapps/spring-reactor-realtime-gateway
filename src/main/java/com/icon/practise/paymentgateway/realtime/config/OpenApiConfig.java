package com.icon.practise.paymentgateway.realtime.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Real-Time Payment Gateway API")
                        .version("1.0")
                        .description("API for processing real-time payments with risk and ledger validation."));
    }
}
