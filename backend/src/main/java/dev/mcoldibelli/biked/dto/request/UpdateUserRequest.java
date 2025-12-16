package dev.mcoldibelli.biked.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
    @Size(min = 2, max = 100) String name
) {

}
