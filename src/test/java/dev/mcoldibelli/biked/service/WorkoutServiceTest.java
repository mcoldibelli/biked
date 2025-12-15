package dev.mcoldibelli.biked.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.mcoldibelli.biked.dto.request.FinishWorkoutRequest;
import dev.mcoldibelli.biked.exception.WorkoutNotFoundException;
import dev.mcoldibelli.biked.model.User;
import dev.mcoldibelli.biked.model.Workout;
import dev.mcoldibelli.biked.model.WorkoutStatus;
import dev.mcoldibelli.biked.repository.DataPointRepository;
import dev.mcoldibelli.biked.repository.UserRepository;
import dev.mcoldibelli.biked.repository.WorkoutRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
@DisplayName("Workout Service")
class WorkoutServiceTest {

  @Mock
  private WorkoutRepository workoutRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private DataPointRepository dataPointRepository;

  @InjectMocks
  private WorkoutService workoutService;

  private UUID userId;
  private UUID workoutId;
  private User user;
  private Workout workout;

  @BeforeEach
  void setup() {
    userId = UUID.randomUUID();
    workoutId = UUID.randomUUID();

    user = User.builder()
        .id(userId)
        .email("test@gmail.com")
        .name("Test User")
        .build();

    workout = Workout.builder()
        .id(workoutId)
        .user(user)
        .status(WorkoutStatus.IN_PROGRESS)
        .startedAt(Instant.now())
        .createdAt(Instant.now())
        .build();
  }

  @Test
  @DisplayName("Should start workout successfully")
  void shouldStartWorkout() {
    // Arrange
    when(userRepository.getReferenceById(userId)).thenReturn(user);
    when(workoutRepository.save(any(Workout.class))).thenReturn(workout);

    // Act
    var result = workoutService.start(userId);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(workoutId);
    assertThat(result.status()).isEqualTo(WorkoutStatus.IN_PROGRESS);
    verify(workoutRepository).save(any(Workout.class));
  }

  @Test
  @DisplayName("Should finish workout successfully")
  void shouldFinishWorkout() {
    // Arrange
    var request = new FinishWorkoutRequest(85.5, 120.0, 25.3, 35.0, 15000.0, 450);
    when(workoutRepository.findByIdAndUserId(workoutId, userId)).thenReturn(Optional.of(workout));
    when(workoutRepository.save(any(Workout.class))).thenReturn(workout);

    when(dataPointRepository.findAvgCadenceByWorkoutId(workoutId)).thenReturn(85.5);
    when(dataPointRepository.findMaxCadenceByWorkoutId(workoutId)).thenReturn(120.0);
    when(dataPointRepository.findAvgSpeedByWorkoutId(workoutId)).thenReturn(25.3);
    when(dataPointRepository.findMaxSpeedByWorkoutId(workoutId)).thenReturn(35.0);

    // Act
    var result = workoutService.finish(workoutId, userId, request);

    // Assert
    assertThat(result).isNotNull();
    verify(workoutRepository).findByIdAndUserId(workoutId, userId);
    verify(workoutRepository).save(any(Workout.class));
    verify(dataPointRepository).findAvgCadenceByWorkoutId(workoutId);
  }

  @Test
  @DisplayName("Should throw exception when finishing non-existing workout")
  void shouldThrowWhenFinishingNonExistentWorkout() {
    // Arrange
    var request = new FinishWorkoutRequest(85.2, 120.0, 25.3, 35.0, 15000.0, 450);
    when(workoutRepository.findByIdAndUserId(workoutId, userId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> workoutService.finish(workoutId, userId, request))
        .isInstanceOf(WorkoutNotFoundException.class);

    verify(workoutRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should find workout by ID")
  void shouldFindWorkoutById() {
    // Arrange
    when(workoutRepository.findByIdAndUserId(workoutId, userId)).thenReturn(Optional.of(workout));

    // Act
    var result = workoutService.findById(workoutId, userId);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(workoutId);
  }

  @Test
  @DisplayName("Should throw exception when workout not found")
  void shouldThrowWhenWorkoutNotFound() {
    // Arrange
    when(workoutRepository.findByIdAndUserId(workoutId, userId)).thenReturn(Optional.empty());

    // Act & Arrange
    assertThatThrownBy(() -> workoutService.findById(workoutId, userId))
        .isInstanceOf(WorkoutNotFoundException.class);
  }

  @Test
  @DisplayName("Should find all workouts by user")
  void shouldFindAllByUser() {
    // Arrange
    var pageable = PageRequest.of(0, 10);
    var page = new PageImpl<>(List.of(workout));
    when(workoutRepository.findByUserId(userId, pageable)).thenReturn(page);

    // Act
    var result = workoutService.findAllByUser(userId, pageable);

    // Arrange
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).id()).isEqualTo(workoutId);
  }
}
