package dev.mcoldibelli.biked.repository;

import dev.mcoldibelli.biked.model.User;
import dev.mcoldibelli.biked.model.Workout;
import dev.mcoldibelli.biked.model.WorkoutStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutRepository extends JpaRepository<Workout, UUID> {

  Page<Workout> findByUserId(UUID userId, Pageable pageable);

  Optional<Workout> findByIdAndUserId(UUID id, UUID userId);

  Optional<Workout> findByUserIdAndStatus(UUID userId, WorkoutStatus status);

  UUID user(User user);
}
