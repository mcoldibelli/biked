package dev.mcoldibelli.biked.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
    @NotNull @Email String email,
    @NotNull @Size(min = 2, max = 100) String name,
    @NotNull @Size(min = 8) String password
) {

}
