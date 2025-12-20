package dev.mcoldibelli.biked.controller;

import dev.mcoldibelli.biked.dto.request.TelemetryDataRequest;
import dev.mcoldibelli.biked.exception.WorkoutNotFoundException;
import dev.mcoldibelli.biked.exception.WorkoutNotInProgressException;
import dev.mcoldibelli.biked.model.WorkoutStatus;
import dev.mcoldibelli.biked.repository.WorkoutRepository;
import dev.mcoldibelli.biked.service.TelemetryProducer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/telemetry")
@RequiredArgsConstructor
@Tag(name = "Telemetry", description = "Receives data from ESP32")
public class TelemetryController {

  private final TelemetryProducer telemetryProducer;
  private final WorkoutRepository workoutRepository;

  @PostMapping
  @Operation(summary = "Sends telemetry", description = "Receives data about cadence, speed and heart rate")
  public ResponseEntity<Void> receive(@Valid @RequestBody TelemetryDataRequest data) {
    var workout = workoutRepository.findById(data.workoutId())
        .orElseThrow(() -> new WorkoutNotFoundException(data.workoutId()));

    if (workout.getStatus() != WorkoutStatus.IN_PROGRESS) {
      throw new WorkoutNotInProgressException(data.workoutId());
    }

    telemetryProducer.send(data);
    return ResponseEntity.accepted().build();
  }
}