package dev.mcoldibelli.biked.service;

import dev.mcoldibelli.biked.config.RabbitMQConfig;
import dev.mcoldibelli.biked.dto.request.TelemetryDataRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelemetryProducer {

  private final RabbitTemplate rabbitTemplate;

  public void send(TelemetryDataRequest data) {
    log.debug("Sending telemetry data for workout: {}", data.workoutId());
    rabbitTemplate.convertAndSend(
        RabbitMQConfig.TELEMETRY_EXCHANGE,
        RabbitMQConfig.TELEMETRY_ROUTING_KEY,
        data
    );
  }
}
