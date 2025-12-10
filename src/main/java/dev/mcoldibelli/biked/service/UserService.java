package dev.mcoldibelli.biked.service;

import dev.mcoldibelli.biked.dto.request.CreateUserRequest;
import dev.mcoldibelli.biked.dto.response.UserResponse;
import dev.mcoldibelli.biked.exception.UserNotFoundException;
import dev.mcoldibelli.biked.model.User;
import dev.mcoldibelli.biked.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public UserResponse findById(UUID id) {
    return userRepository.findById(id)
        .map(this::toResponse)
        .orElseThrow(() -> new UserNotFoundException(id));
  }

  @Transactional(readOnly = true)
  public UserResponse create(CreateUserRequest request) {
    log.info("Creating user with email: {}", request.email());

    var user = User.builder()
        .email(request.email())
        .name(request.name())
        .password(request.password())
        .build();

    var saved = userRepository.save(user);
    log.info("User created with id: {}", saved.getId());

    return toResponse(saved);
  }

  private UserResponse toResponse(User user) {
    return new UserResponse(
        user.getId(),
        user.getEmail(),
        user.getName(),
        user.getCreatedAt()
    );
  }
}
