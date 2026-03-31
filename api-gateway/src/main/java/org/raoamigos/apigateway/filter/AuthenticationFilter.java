package org.raoamigos.apigateway.filter;

import org.raoamigos.apigateway.util.JwtUtil;
import org.raoamigos.apigateway.util.RouteValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator validator;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {

            if (exchange.getRequest().getMethod().equals(HttpMethod.OPTIONS)) {
                return chain.filter(exchange);
            }

            if (validator.isSecured.test(exchange.getRequest())) {
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authorization header");
                }

                String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                }

                try {
                    jwtUtil.validateToken(authHeader);

                    String userId = jwtUtil.extractUserId(authHeader);
                    String role = jwtUtil.extractRole(authHeader);

                    if (exchange.getRequest().getURI().getPath().startsWith("/admin")) {
                        if (!"ROLE_ADMIN".equals(role)) {
                            System.out.println("Blocked: User " + userId + " tried to access Admin route with role " + role);
                            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: Admin privileges required");
                        }
                    }

                    exchange = exchange.mutate()
                            .request(exchange.getRequest().mutate()
                                    .header("X-User-Id", userId)
                                    .header("X-User-Role", role)
                                    .build())
                            .build();

                } catch (ResponseStatusException e) {
                    throw e;
                } catch (Exception e) {
                    System.out.println("Invalid access...!");
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access to application");
                }
            }
            return chain.filter(exchange);
        });
    }

    public static class Config {

    }
}