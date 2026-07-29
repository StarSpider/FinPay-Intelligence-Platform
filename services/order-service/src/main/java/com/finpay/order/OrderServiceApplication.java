package com.finpay.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * FinPay Order Service — Main Entry Point
 *
 * Responsibilities:
 *  - Order lifecycle: CREATE → PAYMENT_PENDING → CONFIRMED / FAILED
 *  - Saga orchestration: order → payment → ledger
 *  - Optimistic locking via @Version on Order entity
 *  - HATEOAS REST API with pagination
 *  - JWT validation (OAuth2 Resource Server)
 *  - Deployed on EKS via Helm chart
 */
@SpringBootApplication
@EnableFeignClients(basePackages = "com.finpay.order.client")
@EnableJpaAuditing
@EnableAsync
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }

}