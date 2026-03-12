package com.bcp.training.cpu;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CpuPredictionService {

    private static final Logger log = LoggerFactory.getLogger(CpuPredictionService.class);
    private int callCount = 0;
    private long lastCallTime = 0;

    @CircuitBreaker(name = "cpuPredict")
    public Double predictSystemLoad() {
        callCount++;
        crashPossibly();
        return Math.random();
    }

    private void crashPossibly() {
        long currentTime = System.currentTimeMillis();
        long gapMillis = currentTime - lastCallTime;
        lastCallTime = currentTime;

        if (gapMillis < 2000) {
            log.error("Prediction #{} has failed", callCount);
            throw new RuntimeException("Prediction service not available due to high load");
        }
        log.info("Prediction #{} has succeeded", callCount);
    }
}
