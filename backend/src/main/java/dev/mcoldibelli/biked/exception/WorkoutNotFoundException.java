package dev.mcoldibelli.biked.exception;

import java.util.UUID;

public class WorkoutNotFoundException extends RuntimeException {

  public WorkoutNotFoundException(UUID id) {
    super("Workout not found with id: " + id);
  }
}
