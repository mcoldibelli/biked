package dev.mcoldibelli.biked.controller;

import dev.mcoldibelli.biked.dto.request.CreateUserRequest;
import dev.mcoldibelli.biked.dto.request.LoginRequest;
import dev.mcoldibelli.biked.dto.response.TokenResponse;
import dev.mcoldibelli.biked.dto.response.UserResponse;
import dev.mcoldibelli.biked.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register and Login endpoints")
public class AuthController {

  private final AuthService authService;

  @PostMapping("/register")
  @Operation(summary = "Register user", description = "Create a new user account")
  public ResponseEntity<UserResponse> register(@Valid @RequestBody CreateUserRequest request) {
    var user = authService.register(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(user);
  }

  @PostMapping("/login")
  @Operation(summary = "Sign in", description = "Authenticate and return a JWT")
  public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
    var token = authService.login(request);
    return ResponseEntity.ok(token);
  }
}
