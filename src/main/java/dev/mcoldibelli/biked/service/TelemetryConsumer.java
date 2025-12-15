package dev.mcoldibelli.biked.service;

import dev.mcoldibelli.biked.config.RabbitMQConfig;
import dev.mcoldibelli.biked.dto.request.TelemetryDataRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelemetryConsumer {

  @RabbitListener(queues = RabbitMQConfig.TELEMETRY_QUEUE)
  public void consume(TelemetryDataRequest data) {
    log.info("Received telemetry - Workout: {}, Cadence: {}, Speed: {}",
        data.workoutId(), data.cadence(), data.speed());

  }
}
