package dev.mcoldibelli.biked.exception;

import java.util.UUID;

public class WorkoutNotInProgressException extends RuntimeException {

  public WorkoutNotInProgressException(UUID workoutId) {
    super("Workout is not in progress: " + workoutId);
  }
}