package com.bcp.training.conference.session;


import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("serviceIsReady")
public class ReadinessIndicator implements HealthIndicator {

    @Override
    public Health health() {
        return Health.up().withDetail("message", "Service is ready").build();
    }
}
