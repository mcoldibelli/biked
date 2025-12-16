package dev.mcoldibelli.biked.controller;

import dev.mcoldibelli.biked.dto.request.CreateUserRequest;
import dev.mcoldibelli.biked.dto.request.UpdateUserRequest;
import dev.mcoldibelli.biked.dto.response.UserResponse;
import dev.mcoldibelli.biked.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Users management")
@SecurityRequirement(name = "Bearer Token")
public class UserController {

  private final UserService userService;

  @GetMapping("/me")
  @Operation(summary = "Current user", description = "Returns data from authenticated user")
  public ResponseEntity<UserResponse> getCurrentUser() {
    var userId = (UUID) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();

    return ResponseEntity.ok(userService.findById(userId));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Search for ID", description = "Returns a user by ID")
  public ResponseEntity<UserResponse> getById(@PathVariable UUID id) {
    return ResponseEntity.ok(userService.findById(id));
  }

  @GetMapping
  @Operation(summary = "List all users", description = "List all users with pagination")
  public ResponseEntity<Page<UserResponse>> getAll(Pageable pageable) {
    return ResponseEntity.ok(userService.findAll(pageable));
  }

  @PostMapping
  @Operation(summary = "Create user", description = "Create a new user")
  public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
    var created = userService.create(request);
    var location = URI.create("/api/v1/users/" + created.id());
    return ResponseEntity.created(location).body(created);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Updates an user", description = "Updates data from user")
  public ResponseEntity<UserResponse> update(@PathVariable UUID id,
      @Valid @RequestBody UpdateUserRequest request) {
    return ResponseEntity.ok(userService.update(id, request));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Deletes user", description = "Remove user")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    userService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
