package dev.mcoldibelli.biked.dto.request;

import jakarta.validation.constraints.Min;

public record FinishedWorkoutRequest(
    @Min(0) Double avgCadence,
    @Min(0) Double maxCadence,
    @Min(0) Double avgSpeed,
    @Min(0) Double maxSpeed,
    @Min(0) Double distanceMeters,
    @Min(0) Integer caloriesBurned
) {

}
