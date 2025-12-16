package dev.mcoldibelli.biked.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.mcoldibelli.biked.dto.request.CreateUserRequest;
import dev.mcoldibelli.biked.dto.request.UpdateUserRequest;
import dev.mcoldibelli.biked.dto.response.UserResponse;
import dev.mcoldibelli.biked.exception.UserNotFoundException;
import dev.mcoldibelli.biked.model.User;
import dev.mcoldibelli.biked.repository.UserRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService")
class UserServiceTest {

  @InjectMocks
  private UserService userService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  private User user;
  private UUID userId;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    user = User.builder()
        .id(userId)
        .email("marcelo@email.com")
        .name("Marcelo Coldibelli")
        .password("hashedPassword")
        .createdAt(Instant.now())
        .build();
  }

  @Test
  @DisplayName("Should find user by ID successfully")
  void shouldFindUserById() {
    // Arrange
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    // Act
    UserResponse result = userService.findById(userId);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(userId);
    assertThat(result.email()).isEqualTo("marcelo@email.com");
    assertThat(result.name()).isEqualTo("Marcelo Coldibelli");
    verify(userRepository).findById(userId);
  }

  @Test
  @DisplayName("Should throw exception when user not found")
  void shouldThrowExceptionWhenUserNotFound() {
    // Arrange
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> userService.findById(userId))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining(userId.toString());

    verify(userRepository).findById(userId);
  }

  @Test
  @DisplayName("Should create user successfully")
  void shouldCreateUser() {
    // Arrange
    var request = new CreateUserRequest(
        "augusto@email.com",
        "Augusto Coldibelli",
        "password123"
    );

    when(passwordEncoder.encode(request.password())).thenReturn("hashed-password");
    when(userRepository.save(any(User.class))).thenReturn(user);

    // Act
    UserResponse result = userService.create(request);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.email()).isEqualTo("marcelo@email.com");
    verify(passwordEncoder).encode(request.password());
    verify(userRepository).save(any(User.class));
  }

  @Test
  @DisplayName("Should update user successfully")
  void shouldUpdateUser() {
    // Arrange
    var request = new UpdateUserRequest("Updated Name");
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenReturn(user);

    // Act
    UserResponse result = userService.update(userId, request);

    // Assert
    assertThat(result).isNotNull();
    verify(userRepository).findById(userId);
    verify(userRepository).save(any(User.class));
  }

  @Test
  @DisplayName("Should throw exception when updating non-existing user")
  void shouldThrowExceptionWhenUpdatingNonExistingUser() {
    // Arrange
    var request = new UpdateUserRequest("Updated Name");
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> userService.update(userId, request))
        .isInstanceOf(UserNotFoundException.class);

    verify(userRepository).findById(userId);
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should delete user successfully")
  void shouldDeleteUser() {
    // Arrange
    when(userRepository.existsById(userId)).thenReturn(true);

    // Act
    userService.delete(userId);

    // Assert
    verify(userRepository).existsById(userId);
    verify(userRepository).deleteById(userId);
  }

  @Test
  @DisplayName("Should throw exception when deleting non-existent user")
  void shouldThrowExceptionWhenDeletingNonExistentUser() {
    // Arrange
    when(userRepository.existsById(userId)).thenReturn(false);

    // Act & Assert
    assertThatThrownBy(() -> userService.delete(userId))
        .isInstanceOf(UserNotFoundException.class);

    verify(userRepository).existsById(userId);
    verify(userRepository, never()).deleteById(any());
  }

}
