package com.bcp.training;

import com.bcp.training.cpu.CpuPredictionService;
import com.bcp.training.cpu.CpuStats;
import com.bcp.training.cpu.CpuStatsService;
import com.bcp.training.status.StatusService;
import com.bcp.training.sysinfo.Info;
import com.bcp.training.sysinfo.InfoService;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class MonitorResource {

    private final InfoService infoService;
    private final StatusService statusService;
    private final CpuStatsService cpuStatsService;
    private final CpuPredictionService cpuPredictionService;

    public MonitorResource(InfoService infoService, StatusService statusService,
                           CpuStatsService cpuStatsService, CpuPredictionService cpuPredictionService) {
        this.infoService = infoService;
        this.statusService = statusService;
        this.cpuStatsService = cpuStatsService;
        this.cpuPredictionService = cpuPredictionService;
    }

    @GetMapping("/hello")
    public String hello() {
        return "Cloud instance monitoring app";
    }

    @GetMapping("/info")
    public Info getSystemInfo() {
        return infoService.getInfo();
    }

    @GetMapping("/status")
    public String getWeatherConditions() {
        return statusService.getStatus();
    }

    @GetMapping("/cpu/stats")
    public CpuStats getCpuStats() {
        return cpuStatsService.getCpuStats();
    }

    @GetMapping("/cpu/predict")
    public ResponseEntity<?> predictCpuLoad() {
        try {
            return ResponseEntity.ok(cpuPredictionService.predictSystemLoad());
        } catch (CallNotPermittedException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Prediction service is not available at the moment");
        }
    }
}
