package dev.mcoldibelli.biked.dto.request;

import java.util.UUID;

public record TelemetryDataRequest(
    UUID workoutId,
    Double cadence,
    Double speed,
    Integer heartRate,
    Long timestamp
) {

}
