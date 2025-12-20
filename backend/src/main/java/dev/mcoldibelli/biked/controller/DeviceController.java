package dev.mcoldibelli.biked.controller;

import static org.springframework.http.HttpStatus.CREATED;

import dev.mcoldibelli.biked.dto.request.CreateDeviceRequest;
import dev.mcoldibelli.biked.dto.response.DeviceResponse;
import dev.mcoldibelli.biked.dto.response.WorkoutResponse;
import dev.mcoldibelli.biked.service.DeviceService;
import dev.mcoldibelli.biked.service.WorkoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
@Tag(name = "Device", description = "Device management")
@SecurityRequirement(name = "Bearer Token")
public class DeviceController {

  private final DeviceService deviceService;
  private final WorkoutService workoutService;

  @PostMapping
  @Operation(summary = "Register device", description = "Register a new device")
  public ResponseEntity<DeviceResponse> register(
      @Valid @RequestBody CreateDeviceRequest request) {
    var userId = getCurrentUserId();
    var registeredDevice = deviceService.register(userId, request);

    return ResponseEntity.status(CREATED).body(registeredDevice);
  }

  @GetMapping
  @Operation(summary = "List devices", description = "List all devices from user")
  public ResponseEntity<Page<DeviceResponse>> getAll(Pageable pageable) {
    var userId = getCurrentUserId();
    return ResponseEntity.ok(deviceService.findAllByUserId(userId, pageable));
  }

  @GetMapping("/{macAddress}")
  @Operation(summary = "Search device by MAC", description = "Look up for device with given MAC Address")
  public ResponseEntity<DeviceResponse> getByMacAddress(@PathVariable String macAddress) {
    return ResponseEntity.ok(deviceService.findByMacAddress(macAddress));
  }


  @GetMapping("/{macAddress}/active-workout")
  @Operation(summary = "Get active workout", description = "Find active workout for device")
  public ResponseEntity<WorkoutResponse> getActiveByDeviceMacAddress(
      @PathVariable String macAddress) {
    return ResponseEntity.ok(workoutService.findActiveByDeviceMacAddress(macAddress));
  }

  private UUID getCurrentUserId() {
    return (UUID) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();
  }
}
