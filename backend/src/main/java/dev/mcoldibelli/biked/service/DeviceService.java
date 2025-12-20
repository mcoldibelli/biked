package dev.mcoldibelli.biked.service;

import dev.mcoldibelli.biked.dto.request.CreateDeviceRequest;
import dev.mcoldibelli.biked.dto.response.DeviceResponse;
import dev.mcoldibelli.biked.exception.DeviceNotFoundException;
import dev.mcoldibelli.biked.exception.MacAddressAlreadyExistsException;
import dev.mcoldibelli.biked.exception.UserNotFoundException;
import dev.mcoldibelli.biked.model.Device;
import dev.mcoldibelli.biked.repository.DeviceRepository;
import dev.mcoldibelli.biked.repository.UserRepository;
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
public class DeviceService {

  private final DeviceRepository deviceRepository;

  private final UserRepository userRepository;

  @Transactional
  public DeviceResponse register(UUID userId, CreateDeviceRequest request) {
    log.info("Registering device with MAC: {}", request.macAddress());

    if (deviceRepository.existsByMacAddress(request.macAddress())) {
      throw new MacAddressAlreadyExistsException(request.macAddress());
    }

    var user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));

    var device = Device.builder()
        .user(user)
        .macAddress(request.macAddress())
        .name(request.name())
        .build();

    var saved = deviceRepository.save(device);
    log.info("Device registered with MAC: {}", request.macAddress());

    return toResponse(saved);
  }

  @Transactional(readOnly = true)
  public DeviceResponse findByMacAddress(String macAddress) {
    return deviceRepository.findByMacAddress(macAddress)
        .map(this::toResponse)
        .orElseThrow(() -> new DeviceNotFoundException(macAddress));
  }

  @Transactional(readOnly = true)
  public Page<DeviceResponse> findAllByUserId(UUID userId, Pageable pageable) {
    return deviceRepository.findAllByUserId(userId, pageable)
        .map(this::toResponse);
  }


  private DeviceResponse toResponse(Device device) {
    return new DeviceResponse(
        device.getId(),
        device.getMacAddress(),
        device.getName(),
        device.getCreatedAt()
    );
  }
}
