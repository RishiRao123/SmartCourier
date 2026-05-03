package org.raoamigos.apigateway.config;

import org.raoamigos.apigateway.filter.AuthenticationFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    private final AuthenticationFilter authenticationFilter;

    public GatewayConfig(AuthenticationFilter authenticationFilter) {
        this.authenticationFilter = authenticationFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Explicitly add a route for auth-admin that applies the authentication filter
                .route("auth-admin-route", r -> r.path("/auth-admin/**")
                        .filters(f -> f.filter(authenticationFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://AUTH-SERVICE"))
                // Also add a route for /auth/profile to ensure it's protected
                .route("auth-profile-route", r -> r.path("/auth/profile/**", "/auth/users/**")
                        .filters(f -> f.filter(authenticationFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://AUTH-SERVICE"))
                .build();
    }
}
