package dev.mcoldibelli.biked.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.mcoldibelli.biked.config.JwtAuthenticationFilter;
import dev.mcoldibelli.biked.dto.request.CreateUserRequest;
import dev.mcoldibelli.biked.dto.request.UpdateUserRequest;
import dev.mcoldibelli.biked.dto.response.UserResponse;
import dev.mcoldibelli.biked.exception.GlobalExceptionHandler;
import dev.mcoldibelli.biked.exception.UserNotFoundException;
import dev.mcoldibelli.biked.service.JwtService;
import dev.mcoldibelli.biked.service.UserService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("UserController")
class UserControllerTest {

  private final UUID userId = UUID.randomUUID();

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private UserService userService;

  @MockitoBean
  private JwtService jwtService;

  @MockitoBean
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Test
  @DisplayName("POST /api/v1/users - should create user and return 201")
  void shouldCreateUser() throws Exception {
    // Arrange
    var request = new CreateUserRequest("test@gmail.com", "Test User", "password123");
    var response = new UserResponse(userId, "test@gmail.com", "Test User", Instant.now());

    when(userService.create(any(CreateUserRequest.class))).thenReturn(response);

    // Act && Assert
    mockMvc.perform(post("/api/v1/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.id").value(userId.toString()))
        .andExpect(jsonPath("$.email").value("test@gmail.com"))
        .andExpect(jsonPath("$.name").value("Test User"));

    verify(userService).create(any(CreateUserRequest.class));
  }

  @Test
  @DisplayName("POST /api/v1/users - should return 400 for invalid email")
  void shouldReturn400ForInvalidEmail() throws Exception {
    var request = new CreateUserRequest("invalid-email", "Test", "password123");

    mockMvc.perform(post("/api/v1/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect((status().isBadRequest()))
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

    verify(userService, never()).create(any());
  }

  @Test
  @DisplayName("POST /api/v1/users - should return 400 for short password")
  void shouldRetunr400ForShortPassword() throws Exception {
    var request = new CreateUserRequest("email@gmail.com", "Test", "123");

    mockMvc.perform(post("/api/v1/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

    verify(userService, never()).create(any());
  }

  @Test
  @DisplayName("GET /api/v1/users/{id} - should return user")
  void shouldReturnUser() throws Exception {
    var response = new UserResponse(userId, "test@email.com", "Test User", Instant.now());
    when(userService.findById(userId)).thenReturn(response);

    mockMvc.perform(get("/api/v1/users/{id}", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(response)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(userId.toString()));

    verify(userService).findById(userId);
  }

  @Test
  @DisplayName("GET /api/v1/users/{id} - should return 404 when not found")
  void shouldReturn404WhenNotFound() throws Exception {
    when(userService.findById(userId)).thenThrow(new UserNotFoundException(userId));

    mockMvc.perform(get("/api/v1/users/{id}", userId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("NOT_FOUND"));

    verify(userService).findById(userId);
  }

  @Test
  @DisplayName("PUT /api/v1/users/{id} - should update user")
  void shouldUpdateUser() throws Exception {
    var request = new UpdateUserRequest("Updated name");
    var response = new UserResponse(userId, "test@email.com", "Updated name", Instant.now());

    when(userService.update(userId, request)).thenReturn(response);

    mockMvc.perform(put("/api/v1/users/{id}", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Updated name"));

    verify(userService).update(eq(userId), any(UpdateUserRequest.class));
  }

  @Test
  @DisplayName("DELETE /api/v1/users/{id} - should return 204")
  void shouldDeleteUser() throws Exception {
    doNothing().when(userService).delete(userId);

    mockMvc.perform(delete("/api/v1/users/{id}", userId))
        .andExpect(status().isNoContent());

    verify(userService).delete(userId);
  }
}
