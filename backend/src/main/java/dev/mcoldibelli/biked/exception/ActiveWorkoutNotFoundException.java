package dev.mcoldibelli.biked.exception;

public class ActiveWorkoutNotFoundException extends RuntimeException {

  public ActiveWorkoutNotFoundException(String macAddress) {
    super("No active workout found for device: " + macAddress);
  }
}
