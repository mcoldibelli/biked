package dev.mcoldibelli.biked.controller;

import dev.mcoldibelli.biked.dto.request.CreateUserRequest;
import dev.mcoldibelli.biked.dto.request.UpdateUserRequest;
import dev.mcoldibelli.biked.dto.response.UserResponse;
import dev.mcoldibelli.biked.service.UserService;
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
public class UserController {

  private final UserService userService;

  @GetMapping("/me")
  public ResponseEntity<UserResponse> getCurrentUser() {
    var userId = (UUID) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();

    return ResponseEntity.ok(userService.findById(userId));
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserResponse> getById(@PathVariable UUID id) {
    return ResponseEntity.ok(userService.findById(id));
  }

  @GetMapping
  public ResponseEntity<Page<UserResponse>> getAll(Pageable pageable) {
    return ResponseEntity.ok(userService.findAll(pageable));
  }

  @PostMapping
  public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
    var created = userService.create(request);
    var location = URI.create("/api/v1/users/" + created.id());
    return ResponseEntity.created(location).body(created);
  }

  @PutMapping("/{id}")
  public ResponseEntity<UserResponse> update(@PathVariable UUID id,
      @Valid @RequestBody UpdateUserRequest request) {
    return ResponseEntity.ok(userService.update(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    userService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
