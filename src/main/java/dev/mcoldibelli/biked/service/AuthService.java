package dev.mcoldibelli.biked.service;

import dev.mcoldibelli.biked.dto.request.CreateUserRequest;
import dev.mcoldibelli.biked.dto.request.LoginRequest;
import dev.mcoldibelli.biked.dto.response.TokenResponse;
import dev.mcoldibelli.biked.dto.response.UserResponse;
import dev.mcoldibelli.biked.exception.InvalidCredentialsException;
import dev.mcoldibelli.biked.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

  private final UserRepository userRepository;
  private final UserService userService;
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public UserResponse register(CreateUserRequest request) {
    return userService.create(request);
  }

  @Transactional(readOnly = true)
  public TokenResponse login(LoginRequest request) {
    var user = userRepository.findByEmail(request.email())
        .orElseThrow(InvalidCredentialsException::new);

    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new InvalidCredentialsException();
    }

    var token = jwtService.generateToken(user.getId(), user.getEmail());
    log.info("User logged in: {}", user.getEmail());

    return new TokenResponse(token);
  }
}
