package dev.mcoldibelli.biked.service;

import dev.mcoldibelli.biked.dto.request.CreateUserRequest;
import dev.mcoldibelli.biked.dto.request.UpdateUserRequest;
import dev.mcoldibelli.biked.dto.response.UserResponse;
import dev.mcoldibelli.biked.exception.EmailAlreadyExistsException;
import dev.mcoldibelli.biked.exception.UserNotFoundException;
import dev.mcoldibelli.biked.model.User;
import dev.mcoldibelli.biked.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional(readOnly = true)
  public UserResponse findById(UUID id) {
    return userRepository.findById(id)
        .map(this::toResponse)
        .orElseThrow(() -> new UserNotFoundException(id));
  }

  @Transactional(readOnly = true)
  public Page<UserResponse> findAll(Pageable pageable) {
    return userRepository.findAll(pageable)
        .map(this::toResponse);
  }

  @Transactional
  public UserResponse create(CreateUserRequest request) {
    log.info("Creating user with email: {}", request.email());

    if (userRepository.existsByEmail(request.email())) {
      throw new EmailAlreadyExistsException(request.email());
    }

    var user = User.builder()
        .email(request.email())
        .name(request.name())
        .password(passwordEncoder.encode(request.password()))
        .build();

    var saved = userRepository.save(user);
    log.info("User created with id: {}", saved.getId());

    return toResponse(saved);
  }

  @Transactional
  public UserResponse update(UUID id, UpdateUserRequest request) {
    var user = userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(id));

    if (request.name() != null) {
      user.setName(request.name());
    }

    if (request.weightKg() != null) {
      user.setWeightKg(request.weightKg());
    }

    var saved = userRepository.save(user);
    log.info("User updated with id: {}", saved.getId());

    return toResponse(saved);
  }

  @Transactional
  public void delete(UUID id) {
    if (!userRepository.existsById(id)) {
      throw new UserNotFoundException(id);
    }

    userRepository.deleteById(id);
    log.info("User deleted with id: {}", id);
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
