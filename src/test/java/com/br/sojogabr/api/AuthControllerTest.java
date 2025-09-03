package com.br.sojogabr.api;

import com.br.sojogabr.api.dto.LoginRequest;
import com.br.sojogabr.config.SecurityConfig;
import com.br.sojogabr.domain.model.User;
import com.br.sojogabr.domain.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId("testuser"); // Assuming username is the ID for login
        user.setUsername("testuser");        
        user.setPassword("some-hashed-password");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("senha123");
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar status 200 OK ao logar com credenciais válidas")
    void login_whenCredentialsAreValid_shouldReturnOk() throws Exception {
        // 1. Mock do método correto: findByUsername
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        // 2. Mock do PasswordEncoder para simular uma senha válida
        when(passwordEncoder.matches("senha123", "some-hashed-password")).thenReturn(true);

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @DisplayName("Deve retornar status 401 Unauthorized com senha inválida")
    void login_whenPasswordIsInvalid_shouldReturnUnauthorized() throws Exception {
        loginRequest.setPassword("wrongpassword");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        // Simula que a senha não bate
        when(passwordEncoder.matches("wrongpassword", "some-hashed-password")).thenReturn(false);

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve retornar status 401 Unauthorized para usuário inexistente")
    void login_whenUserNotFound_shouldReturnUnauthorized() throws Exception {
        loginRequest.setUsername("nonexistentuser");
        when(userRepository.findByUsername("nonexistentuser")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar status 201 Created ao salvar um novo usuário")
    void saveUser_whenUserIsValid_shouldReturnCreated() throws Exception {
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("password");

        // Mock da codificação da senha
        when(passwordEncoder.encode("password")).thenReturn("encoded-password");

        // Mock the behavior of the repository save method
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            // The controller should have set an ID if it was null/empty
            if (userToSave.getId() == null || userToSave.getId().isEmpty()) {
                userToSave.setId(UUID.randomUUID().toString());
            }
            return userToSave;
        });

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    @DisplayName("Deve retornar status 200 OK e o usuário ao buscar por ID existente")
    void getUserById_whenUserExists_shouldReturnOk() throws Exception {
        String userId = UUID.randomUUID().toString();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.username").value(user.getUsername()));
    }

    @Test
    @DisplayName("Deve retornar status 404 Not Found ao buscar por ID inexistente")
    void getUserById_whenUserDoesNotExist_shouldReturnNotFound() throws Exception {
        String nonExistentId = UUID.randomUUID().toString();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}