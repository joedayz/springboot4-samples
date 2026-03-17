package com.bcp.training;


import com.bcp.training.service.StateService;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class AppLivenessHealthIndicator implements HealthIndicator {

    private static final String HEALTH_CHECK_NAME = "Liveness";
    private final StateService applicationState;

    public AppLivenessHealthIndicator(StateService applicationState) {
        this.applicationState = applicationState;
    }

    @Override
    public Health health() {
        return applicationState.isAlive()
                ? Health.up().withDetail(HEALTH_CHECK_NAME, "up").build()
                : Health.down().withDetail(HEALTH_CHECK_NAME, "down").build();
    }
}
