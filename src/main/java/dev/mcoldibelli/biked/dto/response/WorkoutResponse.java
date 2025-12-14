package dev.mcoldibelli.biked.dto.response;

import dev.mcoldibelli.biked.model.WorkoutStatus;
import java.time.Instant;
import java.util.UUID;

public record WorkoutResponse(
    UUID id,
    WorkoutStatus status,
    Instant startedAt,
    Instant finishedAt,
    Integer durationSeconds,
    Double avgCadence,
    Double maxCadence,
    Double avgSpeed,
    Double maxSpeed,
    Double distanceMeters,
    Integer caloriesBurned,
    Instant createdAt
) {

}
