package ua.kpi.comsys.streams;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.stream.Gatherer;
import java.util.stream.Stream;

public class TelemetryStreamAnalyzer {

    public Optional<SensorMetric> findFirstAnomaly(Stream<SensorMetric> stream, String targetDeviceId, double criticalThreshold) {
        if (stream == null) {
            throw new IllegalArgumentException("stream cannot be null");
        }
        if (targetDeviceId == null || targetDeviceId.isBlank()) {
            throw new IllegalArgumentException("targetDeviceId cannot be null or blank");
        }

        return stream
                .filter(m -> targetDeviceId.equals(m.deviceId()) && m.value() > criticalThreshold)
                .findFirst();
    }

    public List<SensorMetric> streamActiveMetrics(Stream<SensorMetric> stream, double normalThreshold, int maxCount) {
        if (stream == null) {
            throw new IllegalArgumentException("stream cannot be null");
        }
        if (maxCount <= 0) {
            throw new IllegalArgumentException("maxCount must be greater than 0");
        }

        return stream
                .dropWhile(m -> m.value() <= normalThreshold)
                .limit(maxCount)
                .toList();
    }

    public List<Double> calculateMovingAverages(Stream<SensorMetric> stream, String metricType, int windowSize, int outputLimit) {
        if (stream == null) {
            throw new IllegalArgumentException("stream cannot be null");
        }
        if (metricType == null || metricType.isBlank()) {
            throw new IllegalArgumentException("metricType cannot be null or blank");
        }
        if (windowSize <= 0) {
            throw new IllegalArgumentException("windowSize must be greater than 0");
        }
        if (outputLimit <= 0) {
            throw new IllegalArgumentException("outputLimit must be greater than 0");
        }

        Gatherer<SensorMetric, ?, Double> movingAverageGatherer = createMovingAverageGatherer(metricType, windowSize);

        return stream
                .gather(movingAverageGatherer)
                .limit(outputLimit)
                .toList();
    }

    public static Gatherer<SensorMetric, ?, Double> createMovingAverageGatherer(String metricType, int windowSize) {
        class MovingAverageState {
            final Deque<Double> window = new ArrayDeque<>(windowSize);
            double runningSum = 0.0;

            double addAndComputeAverage(double value) {
                if (window.size() == windowSize) {
                    runningSum -= window.removeFirst();
                }
                window.addLast(value);
                runningSum += value;
                return runningSum / window.size();
            }
        }

        return Gatherer.ofSequential(
                MovingAverageState::new,
                Gatherer.Integrator.ofGreedy((state, metric, downstream) -> {
                    if (metricType.equals(metric.metricType())) {
                        double avg = state.addAndComputeAverage(metric.value());
                        return downstream.push(avg);
                    }
                    return true;
                })
        );
    }
}
