package dev.mcoldibelli.biked.service;

import dev.mcoldibelli.biked.dto.response.DataPointResponse;
import dev.mcoldibelli.biked.model.DataPoint;
import dev.mcoldibelli.biked.repository.DataPointRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DataPointService {

  private final DataPointRepository dataPointRepository;

  @Transactional(readOnly = true)
  public List<DataPointResponse> findByWorkoutId(UUID workoutId) {
    return dataPointRepository.findByWorkoutIdOrderByRecordedAtAsc(workoutId)
        .stream()
        .map(this::toResponse)
        .toList();
  }

  private DataPointResponse toResponse(DataPoint dataPoint) {
    return new DataPointResponse(
        dataPoint.getId(),
        dataPoint.getCadence(),
        dataPoint.getSpeed(),
        dataPoint.getHeartRate(),
        dataPoint.getRecordedAt()
    );
  }
}
