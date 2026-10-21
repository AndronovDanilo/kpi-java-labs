package ua.kpi.comsys.streams;

import java.time.Instant;

public record SensorMetric(String deviceId, String metricType, double value, Instant timestamp) {
    public SensorMetric {
        if (deviceId == null || deviceId.isBlank()) {
            throw new IllegalArgumentException("deviceId cannot be null or blank");
        }
        if (metricType == null || metricType.isBlank()) {
            throw new IllegalArgumentException("metricType cannot be null or blank");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("timestamp cannot be null");
        }
    }
}
