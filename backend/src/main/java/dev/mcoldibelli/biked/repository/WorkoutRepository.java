package dev.mcoldibelli.biked.repository;

import dev.mcoldibelli.biked.model.Workout;
import dev.mcoldibelli.biked.model.WorkoutStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WorkoutRepository extends JpaRepository<Workout, UUID> {

  Page<Workout> findByUserId(UUID userId, Pageable pageable);

  Optional<Workout> findByIdAndUserId(UUID id, UUID userId);

  Optional<Workout> findByUserIdAndStatus(UUID userId, WorkoutStatus status);

  Optional<Workout> findByDeviceIdAndStatus(UUID deviceId, WorkoutStatus status);

  @Query("SELECT w FROM Workout w JOIN w.device d WHERE d.macAddress = :macAddress AND w.status = :status")
  Optional<Workout> findByDeviceMacAddressAndStatus(String macAddress, WorkoutStatus status);
}
