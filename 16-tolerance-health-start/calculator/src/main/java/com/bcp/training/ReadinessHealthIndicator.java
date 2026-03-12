package com.bcp.training;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class ReadinessHealthIndicator implements HealthIndicator {

    private static final String HEALTH_CHECK_NAME = "Readiness";
    private int counter = 0;

    @Override
    public Health health() {
        return ++counter >= 10
                ? Health.up().withDetail(HEALTH_CHECK_NAME, "up").build()
                : Health.down().withDetail(HEALTH_CHECK_NAME, "down").build();
    }
}
