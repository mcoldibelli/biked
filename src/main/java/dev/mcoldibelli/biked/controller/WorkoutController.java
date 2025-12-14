package dev.mcoldibelli.biked.controller;

import static org.springframework.http.HttpStatus.CREATED;

import dev.mcoldibelli.biked.dto.request.FinishWorkoutRequest;
import dev.mcoldibelli.biked.dto.response.WorkoutResponse;
import dev.mcoldibelli.biked.service.WorkoutService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/workouts")
@RequiredArgsConstructor
public class WorkoutController {

  private final WorkoutService workoutService;

  @PostMapping
  public ResponseEntity<WorkoutResponse> start() {
    var userId = getCurrentUserId();
    var workout = workoutService.start(userId);
    return ResponseEntity.status(CREATED).body(workout);
  }

  @PutMapping("/{id}/finish")
  public ResponseEntity<WorkoutResponse> finish(
      @PathVariable UUID id,
      @Valid @RequestBody FinishWorkoutRequest request) {
    var userId = getCurrentUserId();
    var workout = workoutService.finish(id, userId, request);
    return ResponseEntity.ok(workout);
  }

  @GetMapping("/{id}")
  public ResponseEntity<WorkoutResponse> getById(@PathVariable UUID id) {
    var userId = getCurrentUserId();
    return ResponseEntity.ok(workoutService.findById(id, userId));
  }

  @GetMapping
  public ResponseEntity<Page<WorkoutResponse>> getAll(Pageable pageable) {
    var userId = getCurrentUserId();
    return ResponseEntity.ok(workoutService.findAllByUser(userId, pageable));
  }

  private UUID getCurrentUserId() {
    return (UUID) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();
  }
}
