package dev.mcoldibelli.biked.controller;

import dev.mcoldibelli.biked.dto.request.TelemetryDataRequest;
import dev.mcoldibelli.biked.service.TelemetryProducer;
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
public class TelemetryController {

  private final TelemetryProducer telemetryProducer;

  @PostMapping
  public ResponseEntity<Void> receive(@Valid @RequestBody TelemetryDataRequest data) {
    telemetryProducer.send(data);
    return ResponseEntity.accepted().build();
  }
}
