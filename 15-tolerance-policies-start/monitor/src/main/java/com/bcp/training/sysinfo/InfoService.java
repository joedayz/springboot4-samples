package com.bcp.training.sysinfo;

import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class InfoService {

    private static final Logger log = LoggerFactory.getLogger(InfoService.class);
    private int callCount = 0;

    @Retry(name = "info")
    public Info getInfo() {
        callCount++;
        crashPossibly();
        return new Info();
    }

    private void crashPossibly() {
        if (callCount % 5 == 0) {
            log.info("Request #{} has succeeded", callCount);
        } else {
            log.error("Request #{} has failed", callCount);
            throw new RuntimeException("InfoService failed due to unexpected error");
        }
    }
}
