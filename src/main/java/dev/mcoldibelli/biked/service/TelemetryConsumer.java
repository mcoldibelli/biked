package dev.mcoldibelli.biked.service;

import dev.mcoldibelli.biked.config.RabbitMQConfig;
import dev.mcoldibelli.biked.dto.request.TelemetryDataRequest;
import dev.mcoldibelli.biked.model.DataPoint;
import dev.mcoldibelli.biked.repository.DataPointRepository;
import dev.mcoldibelli.biked.repository.WorkoutRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelemetryConsumer {

  private final DataPointRepository dataPointRepository;
  private final WorkoutRepository workoutRepository;

  @RabbitListener(queues = RabbitMQConfig.TELEMETRY_QUEUE)
  @Transactional
  public void consume(TelemetryDataRequest data) {
    log.info("Received telemetry - Workout: {}, Cadence: {}, Speed: {}",
        data.workoutId(), data.cadence(), data.speed());

    var workout = workoutRepository.getReferenceById(data.workoutId());

    var dataPoint = DataPoint.builder()
        .workout(workout)
        .cadence(data.cadence())
        .speed(data.speed())
        .heartRate(data.heartRate())
        .recordedAt(Instant.ofEpochMilli(data.timestamp()))
        .build();

    dataPointRepository.save(dataPoint);
    log.debug("DataPoint saved for workout: {}", data.workoutId());
  }
}
