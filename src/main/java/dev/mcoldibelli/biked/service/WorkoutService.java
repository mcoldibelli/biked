package dev.mcoldibelli.biked.service;

import dev.mcoldibelli.biked.dto.request.FinishedWorkoutRequest;
import dev.mcoldibelli.biked.dto.response.WorkoutResponse;
import dev.mcoldibelli.biked.exception.WorkoutNotFoundException;
import dev.mcoldibelli.biked.model.Workout;
import dev.mcoldibelli.biked.model.WorkoutStatus;
import dev.mcoldibelli.biked.repository.UserRepository;
import dev.mcoldibelli.biked.repository.WorkoutRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkoutService {

  private final WorkoutRepository workoutRepository;
  private final UserRepository userRepository;

  public WorkoutResponse start(UUID userId) {
    log.info("Starting workout for user: {}", userId);

    var user = userRepository.getReferenceById(userId);

    var workout = Workout.builder()
        .user(user)
        .status(WorkoutStatus.IN_PROGRESS)
        .startedAt(Instant.now())
        .build();

    var saved = workoutRepository.save(workout);
    log.info("Workout started with id: {}", saved.getId());

    return toResponse(saved);
  }

  @Transactional
  public WorkoutResponse finish(UUID workoutId, UUID userId, FinishedWorkoutRequest request) {
    log.info("Finishing workout: {}", workoutId);

    var workout = workoutRepository.findByIdAndUserId(workoutId, userId)
        .orElseThrow(() -> new WorkoutNotFoundException(workoutId));

    var finishedAt = Instant.now();
    var duration = Duration.between(workout.getStartedAt(), finishedAt);

    workout.setStatus(WorkoutStatus.COMPLETED);
    workout.setFinishedAt(finishedAt);
    workout.setDurationSeconds((int) duration.getSeconds());
    workout.setAvgCadence(request.avgCadence());
    workout.setMaxCadence(request.maxCadence());
    workout.setAvgSpeed(request.avgSpeed());
    workout.setMaxSpeed(request.maxSpeed());
    workout.setDistanceMeters(request.distanceMeters());
    workout.setCaloriesBurned(request.caloriesBurned());

    var saved = workoutRepository.save(workout);
    log.info("Workout finished: {}", saved.getId());

    return toResponse(saved);
  }

  @Transactional(readOnly = true)
  public WorkoutResponse findById(UUID workoutId, UUID userId) {
    return workoutRepository.findByIdAndUserId(workoutId, userId)
        .map(this::toResponse)
        .orElseThrow(() -> new WorkoutNotFoundException(workoutId));
  }

  @Transactional(readOnly = true)
  public Page<WorkoutResponse> findAllByUser(UUID userId, Pageable pageable) {
    return workoutRepository.findByUserId(userId, pageable)
        .map(this::toResponse);
  }

  private WorkoutResponse toResponse(Workout workout) {
    return new WorkoutResponse(
        workout.getId(),
        workout.getStatus(),
        workout.getStartedAt(),
        workout.getFinishedAt(),
        workout.getDurationSeconds(),
        workout.getAvgCadence(),
        workout.getMaxCadence(),
        workout.getAvgSpeed(),
        workout.getMaxSpeed(),
        workout.getDistanceMeters(),
        workout.getCaloriesBurned(),
        workout.getCreatedAt()
    );
  }
}
