package dev.mcoldibelli.biked.controller;

import static org.springframework.http.HttpStatus.CREATED;

import dev.mcoldibelli.biked.dto.request.CreateWorkoutRequest;
import dev.mcoldibelli.biked.dto.request.FinishWorkoutRequest;
import dev.mcoldibelli.biked.dto.response.DataPointResponse;
import dev.mcoldibelli.biked.dto.response.WorkoutResponse;
import dev.mcoldibelli.biked.service.DataPointService;
import dev.mcoldibelli.biked.service.JwtService;
import dev.mcoldibelli.biked.service.WorkoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/workouts")
@RequiredArgsConstructor
@Tag(name = "Workouts", description = "Workouts management")
@SecurityRequirement(name = "Bearer Token")
public class WorkoutController {

  private final WorkoutService workoutService;
  private final JwtService jwtService;
  private final DataPointService dataPointService;

  @PostMapping
  @Operation(summary = "Start workout session", description = "Create a new workout in progress")
  public ResponseEntity<WorkoutResponse> start(
      @RequestBody(required = false) CreateWorkoutRequest request) {
    var userId = getCurrentUserId();
    var deviceId = request != null ? request.deviceId() : null;
    var workout = workoutService.start(userId, deviceId);
    return ResponseEntity.status(CREATED).body(workout);
  }

  @PutMapping("/{id}/finish")
  @Operation(summary = "Finishes workout", description = "Finishes workout session and calculate its metrics")
  public ResponseEntity<WorkoutResponse> finish(
      @PathVariable UUID id,
      @Valid @RequestBody FinishWorkoutRequest request) {
    var userId = getCurrentUserId();
    var workout = workoutService.finish(id, userId, request);
    return ResponseEntity.ok(workout);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Search workout", description = "Returns details from a given workout")
  public ResponseEntity<WorkoutResponse> getById(@PathVariable UUID id) {
    var userId = getCurrentUserId();
    return ResponseEntity.ok(workoutService.findById(id, userId));
  }

  @GetMapping
  @Operation(summary = "List workouts", description = "List all workouts from a user, using pagination")
  public ResponseEntity<Page<WorkoutResponse>> getAll(Pageable pageable) {
    var userId = getCurrentUserId();
    return ResponseEntity.ok(workoutService.findAllByUser(userId, pageable));
  }

  private UUID getCurrentUserId() {
    return (UUID) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();
  }

  @GetMapping("/{id}/datapoints")
  @Operation(summary = "List telemetry", description = "Return all telemetry points from workout")
  public ResponseEntity<List<DataPointResponse>> getDataPoints(
      @PathVariable UUID id,
      @RequestHeader("Authorization") String token
  ) {
    var userId = jwtService.extractUserId(token.replace("Bearer ", ""));
    workoutService.findById(id, userId);
    return ResponseEntity.ok(dataPointService.findByWorkoutId(id));
  }
}
