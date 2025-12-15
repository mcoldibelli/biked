package dev.mcoldibelli.biked.repository;

import dev.mcoldibelli.biked.model.DataPoint;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DataPointRepository extends JpaRepository<DataPoint, UUID> {

  List<DataPoint> findByWorkoutIdOrderByRecordedAtAsc(UUID workoutId);

  @Query("""
      SELECT AVG(d.cadence) FROM DataPoint d
            WHERE d.workout.id = :workoutId
      """
  )
  Double findAvgCadenceByWorkoutId(@Param("workoutId") UUID workoutId);

  @Query("""
      SELECT MAX(d.cadence) FROM DataPoint d
            WHERE d.workout.id = :workoutId
      """
  )
  Double findMaxCadenceByWorkoutId(@Param("workoutId") UUID workoutId);

  @Query("""
      SELECT AVG(d.speed) FROM DataPoint d
      WHERE d.workout.id = :workoutId
      """)
  Double findAvgSpeedByWorkoutId(@Param("workoutId") UUID workoutId);

  @Query("""
      SELECT MAX(d.speed) FROM DataPoint d
      WHERE d.workout.id = :workoutId
      """)
  Double findMaxSpeedByWorkoutId(@Param("workoutId") UUID workoutId);
}
