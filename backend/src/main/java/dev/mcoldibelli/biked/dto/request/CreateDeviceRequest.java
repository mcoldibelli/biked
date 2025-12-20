package dev.mcoldibelli.biked.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateDeviceRequest(
    @NotBlank String macAddress,
    String name
) {

}
