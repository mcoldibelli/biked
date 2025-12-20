package dev.mcoldibelli.biked.dto.request;

import java.util.UUID;

public record CreateWorkoutRequest(
    UUID deviceId
) {

}
