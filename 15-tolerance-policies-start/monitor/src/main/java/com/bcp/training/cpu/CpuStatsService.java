package com.bcp.training.cpu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class CpuStatsService {

    private static final Logger log = LoggerFactory.getLogger(CpuStatsService.class);
    private int callCount = 0;

    public CpuStats getCpuStats() {
        List<Double> series = getCpuUsageTimeSeries();
        double mean = calculateMean(series);
        double standardDeviation = calculateStandardDeviation(series);
        return new CpuStats(series, mean, standardDeviation);
    }

    private List<Double> getCpuUsageTimeSeries() {
        callCount++;
        List<Double> series = IntStream.range(1, 10)
                .mapToDouble(this::getCpuUsageAtTimePoint)
                .boxed()
                .collect(Collectors.toList());
        simulateMissingValues(series);
        return series;
    }

    private double getCpuUsageAtTimePoint(int point) {
        return Math.random();
    }

    private void simulateMissingValues(List<Double> series) {
        if (callCount % 3 == 0) {
            series.set(1, null);
            series.set(3, null);
            series.set(5, null);
            log.warn("Cpu usage data in request #{} contains null values", callCount);
        }
    }

    private double calculateMean(List<Double> series) {
        double sum = series.stream().mapToDouble(Double::doubleValue).sum();
        return sum / series.size();
    }

    private double calculateStandardDeviation(List<Double> series) {
        double average = calculateMean(series);
        double deviations = series.stream()
                .mapToDouble(x -> Math.pow(x - average, 2))
                .sum();
        return Math.sqrt(deviations / series.size());
    }
}
