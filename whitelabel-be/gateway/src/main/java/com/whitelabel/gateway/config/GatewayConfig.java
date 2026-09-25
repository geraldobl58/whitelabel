package com.whitelabel.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder
                .routes()
                .route("product", r -> r
                        .path("/api/v1/products/**")
                        .uri("lb://product"))
                .route("order", r -> r
                        .path("/api/v1/orders/**")
                        .uri("lb://order"))
                .route("stock", r -> r
                        .path("/api/v1/stocks/**")
                        .uri("lb://stock"))
                .build();
    }
}
