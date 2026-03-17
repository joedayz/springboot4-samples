package com.bcp.training.conference.session;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("serviceIsAlive")
public class LivenessIndicator implements HealthIndicator {

    @Override
    public Health health() {
        return Health.up().withDetail("message", "Service is alive").build();
    }
}
