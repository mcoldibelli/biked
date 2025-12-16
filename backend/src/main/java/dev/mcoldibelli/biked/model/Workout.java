package dev.mcoldibelli.biked.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "workouts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Workout {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private WorkoutStatus status;

  @Column(name = "started_at", nullable = false)
  private Instant startedAt;

  @Column(name = "finished_at")
  private Instant finishedAt;

  @Column(name = "duration_seconds")
  private Integer durationSeconds;

  @Column(name = "avg_cadence")
  private Double avgCadence;

  @Column(name = "max_cadence")
  private Double maxCadence;

  @Column(name = "avg_speed")
  private Double avgSpeed;

  @Column(name = "max_speed")
  private Double maxSpeed;

  @Column(name = "distance_meters")
  private Double distanceMeters;

  @Column(name = "calories_burned")
  private Integer caloriesBurned;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = Instant.now();
    if (startedAt == null) {
      startedAt = Instant.now();
    }

    if (status == null) {
      status = WorkoutStatus.IN_PROGRESS;
    }
  }
}
