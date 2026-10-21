package ua.kpi.comsys.streams;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TelemetryStreamAnalyzer Test Suite")
class TelemetryStreamAnalyzerTest {

    private TelemetryStreamAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new TelemetryStreamAnalyzer();
    }

    private Stream<SensorMetric> createInfiniteStream(String deviceId, String metricType) {
        AtomicLong counter = new AtomicLong(0);
        return Stream.generate(() -> {
            long idx = counter.incrementAndGet();
            return new SensorMetric(
                    deviceId,
                    metricType,
                    idx * 1.0,
                    Instant.ofEpochMilli(idx * 1000)
            );
        });
    }

    @Nested
    @DisplayName("SensorMetric Domain Validation Tests")
    class SensorMetricValidationTests {

        @ParameterizedTest(name = "Invalid deviceId: \"{0}\"")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t"})
        void testInvalidDeviceIdThrows(String id) {
            assertThrows(IllegalArgumentException.class, () ->
                    new SensorMetric(id, "temp", 25.0, Instant.now())
            );
        }

        @ParameterizedTest(name = "Invalid metricType: \"{0}\"")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\n"})
        void testInvalidMetricTypeThrows(String type) {
            assertThrows(IllegalArgumentException.class, () ->
                    new SensorMetric("DEV-1", type, 25.0, Instant.now())
            );
        }

        @Test
        @DisplayName("Null timestamp throws IllegalArgumentException")
        void testNullTimestampThrows() {
            assertThrows(IllegalArgumentException.class, () ->
                    new SensorMetric("DEV-1", "temp", 25.0, null)
            );
        }

        @Test
        @DisplayName("Valid SensorMetric creates successfully")
        void testValidCreation() {
            Instant now = Instant.now();
            SensorMetric metric = new SensorMetric("DEV-1", "temp", 22.5, now);
            assertEquals("DEV-1", metric.deviceId());
            assertEquals("temp", metric.metricType());
            assertEquals(22.5, metric.value());
            assertEquals(now, metric.timestamp());
        }
    }

    @Nested
    @DisplayName("findFirstAnomaly with Short-Circuiting & Infinite Streams")
    class FindFirstAnomalyTests {

        @Test
        @Timeout(value = 2, unit = TimeUnit.SECONDS)
        @DisplayName("Finds first anomaly in an infinite stream without hanging")
        void testFindFirstAnomalyInfiniteStream() {
            Stream<SensorMetric> infinite = createInfiniteStream("IOT-001", "temperature");

            Optional<SensorMetric> result = analyzer.findFirstAnomaly(infinite, "IOT-001", 50.0);

            assertTrue(result.isPresent());
            result.ifPresent(m -> {
                assertEquals("IOT-001", m.deviceId());
                assertEquals(51.0, m.value());
            });
        }

        @Test
        @DisplayName("Returns Optional.empty when no anomaly matches in finite stream")
        void testNotFoundReturnsEmptyOptional() {
            List<SensorMetric> metrics = List.of(
                    new SensorMetric("IOT-001", "temp", 10.0, Instant.now()),
                    new SensorMetric("IOT-001", "temp", 20.0, Instant.now()),
                    new SensorMetric("OTHER-DEV", "temp", 100.0, Instant.now())
            );

            Optional<SensorMetric> result = analyzer.findFirstAnomaly(metrics.stream(), "IOT-001", 50.0);

            assertTrue(result.isEmpty());
            SensorMetric fallback = result.orElse(null);
            assertNull(fallback);
        }

        @Test
        @DisplayName("Validates null stream parameter")
        void testNullStreamThrows() {
            assertThrows(IllegalArgumentException.class, () ->
                    analyzer.findFirstAnomaly(null, "DEV-1", 10.0)
            );
        }

        @ParameterizedTest(name = "Invalid targetDeviceId: \"{0}\"")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t"})
        void testInvalidDeviceIdThrows(String devId) {
            Stream<SensorMetric> stream = Stream.empty();
            assertThrows(IllegalArgumentException.class, () ->
                    analyzer.findFirstAnomaly(stream, devId, 10.0)
            );
        }
    }

    @Nested
    @DisplayName("streamActiveMetrics with dropWhile & Infinite Streams")
    class StreamActiveMetricsTests {

        @Test
        @Timeout(value = 2, unit = TimeUnit.SECONDS)
        @DisplayName("Drops normal values and takes N active events from infinite stream")
        void testDropWhileAndTakeActiveInfinite() {
            Stream<SensorMetric> infinite = createInfiniteStream("DEV-1", "temp");
            List<SensorMetric> active = analyzer.streamActiveMetrics(infinite, 5.0, 3);

            assertEquals(3, active.size());
            assertEquals(List.of(6.0, 7.0, 8.0), active.stream().map(SensorMetric::value).toList());
        }

        @Test
        @DisplayName("Returns empty list if finite stream has only normal values")
        void testAllNormalFiniteStream() {
            List<SensorMetric> metrics = List.of(
                    new SensorMetric("D1", "T", 1.0, Instant.now()),
                    new SensorMetric("D1", "T", 2.0, Instant.now())
            );

            List<SensorMetric> active = analyzer.streamActiveMetrics(metrics.stream(), 5.0, 10);
            assertTrue(active.isEmpty());
        }

        @Test
        @DisplayName("Validates null stream and non-positive maxCount")
        void testArgumentValidation() {
            assertThrows(IllegalArgumentException.class, () ->
                    analyzer.streamActiveMetrics(null, 5.0, 5)
            );

            Stream<SensorMetric> stream = Stream.empty();
            assertThrows(IllegalArgumentException.class, () ->
                    analyzer.streamActiveMetrics(stream, 5.0, 0)
            );
            assertThrows(IllegalArgumentException.class, () ->
                    analyzer.streamActiveMetrics(stream, 5.0, -1)
            );
        }
    }

    @Nested
    @DisplayName("Custom Stream Gatherer: calculateMovingAverages")
    class MovingAveragesGathererTests {

        @Test
        @Timeout(value = 2, unit = TimeUnit.SECONDS)
        @DisplayName("Calculates moving averages over infinite stream with custom Gatherer")
        void testMovingAverageInfiniteStream() {
            AtomicLong step = new AtomicLong(0);
            Stream<SensorMetric> infinite = Stream.generate(() -> {
                long n = step.incrementAndGet();
                return new SensorMetric("DEV-IOT", "humidity", n * 10.0, Instant.now());
            });

            List<Double> averages = analyzer.calculateMovingAverages(infinite, "humidity", 3, 5);

            assertEquals(5, averages.size());
            assertEquals(10.0, averages.get(0), 1e-6);
            assertEquals(15.0, averages.get(1), 1e-6);
            assertEquals(20.0, averages.get(2), 1e-6);
            assertEquals(30.0, averages.get(3), 1e-6);
            assertEquals(40.0, averages.get(4), 1e-6);
        }

        @Test
        @DisplayName("Filters out other metric types during Gatherer processing")
        void testIgnoresOtherMetricTypes() {
            List<SensorMetric> metrics = List.of(
                    new SensorMetric("DEV", "temperature", 10.0, Instant.now()),
                    new SensorMetric("DEV", "pressure", 999.0, Instant.now()),
                    new SensorMetric("DEV", "temperature", 20.0, Instant.now()),
                    new SensorMetric("DEV", "voltage", 220.0, Instant.now()),
                    new SensorMetric("DEV", "temperature", 30.0, Instant.now())
            );

            List<Double> averages = analyzer.calculateMovingAverages(metrics.stream(), "temperature", 2, 10);
            assertEquals(List.of(10.0, 15.0, 25.0), averages);
        }

        @Test
        @DisplayName("Validates calculateMovingAverages arguments")
        void testValidation() {
            Stream<SensorMetric> stream = Stream.empty();

            assertThrows(IllegalArgumentException.class, () ->
                    analyzer.calculateMovingAverages(null, "temp", 3, 5)
            );
            assertThrows(IllegalArgumentException.class, () ->
                    analyzer.calculateMovingAverages(stream, null, 3, 5)
            );
            assertThrows(IllegalArgumentException.class, () ->
                    analyzer.calculateMovingAverages(stream, " ", 3, 5)
            );
            assertThrows(IllegalArgumentException.class, () ->
                    analyzer.calculateMovingAverages(stream, "temp", 0, 5)
            );
            assertThrows(IllegalArgumentException.class, () ->
                    analyzer.calculateMovingAverages(stream, "temp", 3, 0)
            );
        }
    }
}
