package com.bcp.training.status;

import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class StatusService {

    private static final Logger log = LoggerFactory.getLogger(StatusService.class);
    private int callCount = 0;

    @Retry(name = "status")
    public String getStatus() {
        callCount++;
        delayPossibly();
        return "Running";
    }

    private void delayPossibly() {
        long start = System.currentTimeMillis();
        if (callCount % 5 != 0) {
            log.warn("Request #{} is taking too long...", callCount);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                log.warn("Request #{} has been interrupted after {} milliseconds", callCount, System.currentTimeMillis() - start);
                Thread.currentThread().interrupt();
                return;
            }
        }
        log.info("Request #{} completed in {} milliseconds", callCount, System.currentTimeMillis() - start);
    }
}
