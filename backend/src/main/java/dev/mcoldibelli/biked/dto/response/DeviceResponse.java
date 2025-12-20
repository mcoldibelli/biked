package dev.mcoldibelli.biked.dto.response;

import java.time.Instant;
import java.util.UUID;

public record DeviceResponse(
    UUID id,
    String macAddress,
    String name,
    Instant createdAt
) {

}
