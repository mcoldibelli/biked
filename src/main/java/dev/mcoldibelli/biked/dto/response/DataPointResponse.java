package dev.mcoldibelli.biked.dto.response;

import java.time.Instant;
import java.util.UUID;

public record DataPointResponse(
    UUID id,
    Double cadence,
    Double speed,
    Integer heartRate,
    Instant recordedAt
) {

}
