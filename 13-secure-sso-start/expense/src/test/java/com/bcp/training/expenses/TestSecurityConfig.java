package com.bcp.training.expenses;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Provides a mock JwtDecoder for tests so the app does not call Keycloak.
 * The decoded JWT carries roles "read" and "modify" for expense endpoints.
 */
@TestConfiguration
public class TestSecurityConfig {

    private static final Jwt MOCK_JWT = Jwt.withTokenValue("test-token")
            .header("alg", "none")
            .claim("sub", "test-user")
            .claim("realm_access", Map.of("roles", List.of("read", "modify")))
            .claim("resource_access", Map.of("backend-service", Map.of("roles", List.of("read", "modify"))))
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();

    @Bean
    @Primary
    JwtDecoder jwtDecoder() {
        return token -> MOCK_JWT;
    }
}
