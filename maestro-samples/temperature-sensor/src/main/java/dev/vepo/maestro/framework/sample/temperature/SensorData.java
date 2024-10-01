package dev.vepo.maestro.framework.sample.temperature;

import java.time.Instant;
import java.util.Objects;

public class SensorData {
    private final String sensorId;
    private final double value;
    private final Instant timestamp;

    public SensorData(String sensorId, double value, Instant timestamp) {
        this.sensorId = sensorId;
        this.value = value;
        this.timestamp = timestamp;
    }

    public String getSensorId() {
        return sensorId;
    }

    public double getValue() {
        return value;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sensorId, timestamp, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        SensorData other = (SensorData) obj;
        return Objects.equals(sensorId, other.sensorId) &&
                Objects.equals(timestamp, other.timestamp) &&
                Double.compare(value, other.value) == 0;
    }

    @Override
    public String toString() {
        return String.format("SensorData[sensorId=%s, value=%.2f, timestamp=%d]",
                             sensorId, value, timestamp.toEpochMilli());
    }
}