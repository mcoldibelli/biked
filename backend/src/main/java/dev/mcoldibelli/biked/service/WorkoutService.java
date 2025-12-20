package dev.mcoldibelli.biked.service;

import dev.mcoldibelli.biked.dto.request.FinishWorkoutRequest;
import dev.mcoldibelli.biked.dto.response.WorkoutResponse;
import dev.mcoldibelli.biked.exception.ActiveWorkoutNotFoundException;
import dev.mcoldibelli.biked.exception.WorkoutNotFoundException;
import dev.mcoldibelli.biked.model.Device;
import dev.mcoldibelli.biked.model.Workout;
import dev.mcoldibelli.biked.model.WorkoutStatus;
import dev.mcoldibelli.biked.repository.DataPointRepository;
import dev.mcoldibelli.biked.repository.DeviceRepository;
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
  private final DataPointRepository dataPointRepository;
  private final DeviceRepository deviceRepository;

  public WorkoutResponse start(UUID userId, UUID deviceId) {
    log.info("Starting workout for user: {}, at device: {}", userId, deviceId);

    var user = userRepository.getReferenceById(userId);

    Device device = null;
    if (deviceId != null) {
      device = deviceRepository.getReferenceById(deviceId);
    }

    var workout = Workout.builder()
        .user(user)
        .device(device)
        .status(WorkoutStatus.IN_PROGRESS)
        .startedAt(Instant.now())
        .build();

    var saved = workoutRepository.save(workout);
    log.info("Workout started with id: {}", saved.getId());

    return toResponse(saved);
  }

  @Transactional
  public WorkoutResponse finish(UUID workoutId, UUID userId, FinishWorkoutRequest request) {
    log.info("Finishing workout: {}", workoutId);

    var workout = workoutRepository.findByIdAndUserId(workoutId, userId)
        .orElseThrow(() -> new WorkoutNotFoundException(workoutId));

    if (workout.getStatus() != WorkoutStatus.IN_PROGRESS) {
      throw new IllegalStateException("Workout is not in progress");
    }

    workout.setStatus(WorkoutStatus.COMPLETED);
    workout.setFinishedAt(Instant.now());
    workout.setDurationSeconds(
        (int) Duration.between(workout.getStartedAt(), workout.getFinishedAt()).getSeconds());

    // Metrics from data points
    var avgCadence = dataPointRepository.findAvgCadenceByWorkoutId(workoutId);
    var maxCadence = dataPointRepository.findMaxCadenceByWorkoutId(workoutId);
    var avgSpeed = dataPointRepository.findAvgSpeedByWorkoutId(workoutId);
    var maxSpeed = dataPointRepository.findMaxSpeedByWorkoutId(workoutId);

    workout.setAvgCadence(avgCadence);
    workout.setMaxCadence(maxCadence);
    workout.setAvgSpeed(avgSpeed);
    workout.setMaxSpeed(maxSpeed);

    // Calcula distância: cada datapoint = 5s, speed em km/h
    // distância (m) = SUM(speed) × (5/3600) × 1000 = SUM(speed) × 1.39
    var sumSpeed = dataPointRepository.sumSpeedByWorkoutId(workoutId);
    if (sumSpeed != null) {
      workout.setDistanceMeters(sumSpeed * 1.39);
    }

    // Calcula calorias: MET dinâmico × peso × duração_horas
    var user = workout.getUser();
    if (user.getWeightKg() != null && workout.getDurationSeconds() != null) {
      double met = calculateMet(avgCadence);
      double durationHours = workout.getDurationSeconds() / 3600.0;
      double calories = met * user.getWeightKg() * durationHours;
      workout.setCaloriesBurned((int) Math.round(calories));
      log.info("Calories calculated: MET={}, weight={}, duration={}h, result={}",
          met, user.getWeightKg(), durationHours, calories);
    }

    // Valores manuais sobrescrevem os calculados (apenas se fornecidos)
    if (request != null && request.distanceMeters() != null) {
      workout.setDistanceMeters(request.distanceMeters());
    }
    if (request != null && request.caloriesBurned() != null) {
      workout.setCaloriesBurned(request.caloriesBurned());
    }

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

  @Transactional(readOnly = true)
  public WorkoutResponse findActiveByDeviceMacAddress(String macAddress) {
    return workoutRepository.findByDeviceMacAddressAndStatus(macAddress, WorkoutStatus.IN_PROGRESS)
        .map(this::toResponse)
        .orElseThrow(() -> new ActiveWorkoutNotFoundException(macAddress));
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

  // MET dinâmico baseado na cadência
  // Cadência baixa (<50): MET ~2.5 (leve)
  // Cadência média (50-70): MET ~4 (moderado)
  // Cadência alta (>70): MET ~6 (intenso)
  private double calculateMet(Double avgCadence) {
    if (avgCadence == null || avgCadence < 30) {
      return 2.0;
    }
    if (avgCadence < 50) {
      return 2.5;
    }
    if (avgCadence < 70) {
      return 4.0;
    }
    if (avgCadence < 90) {
      return 5.5;
    }
    return 7.0;
  }

}
