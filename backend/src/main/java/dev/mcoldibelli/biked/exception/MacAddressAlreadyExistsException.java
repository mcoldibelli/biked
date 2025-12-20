package dev.mcoldibelli.biked.exception;

public class MacAddressAlreadyExistsException extends RuntimeException {

  public MacAddressAlreadyExistsException(String macAddress) {
    super("MAC Address (" + macAddress + ") already exists");
  }
}
