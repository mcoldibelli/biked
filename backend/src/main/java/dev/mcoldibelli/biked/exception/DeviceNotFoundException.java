package dev.mcoldibelli.biked.exception;

import java.util.UUID;

public class DeviceNotFoundException extends RuntimeException {

  public DeviceNotFoundException(UUID id) {
    super("Device not found with id: " + id);
  }

  public DeviceNotFoundException(String macAddress) {
    super("Device not found with MAC: " + macAddress);
  }
}
