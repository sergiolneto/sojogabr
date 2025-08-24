package com.br.sojogabr.api;

import com.br.sojogabr.api.dto.LoginRequest;
import com.br.sojogabr.domain.model.User;
import com.br.sojogabr.domain.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final UserRepository userRepository;

    // 1. Injeção de dependência via construtor (prática recomendada)
    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
            // 2. Lógica de login mais robusta e com retorno padronizado
            // Em um app real, a senha seria criptografada (ex: com BCrypt)
            return userRepository.findById(loginRequest.getUsername()) // Assumindo que o DTO terá um username
                    .filter(user -> "senha123".equals(loginRequest.getPassword())) // Simulação de verificação de senha
                    .map(ResponseEntity::ok) // Se o usuário for encontrado e a senha bater, retorna 200 OK com o usuário
                    .orElse(ResponseEntity.status(401).build()); // Caso contrário, retorna 401 Unauthorized
        }

    @PostMapping("/users")
    public ResponseEntity<User> saveUser(@RequestBody User user, UriComponentsBuilder uriBuilder) {
        if (user.getId() == null || user.getId().isEmpty()) {
            user.setId(UUID.randomUUID().toString());
        }
        User savedUser = userRepository.save(user);

        URI location = uriBuilder.path("/api/users/{id}").buildAndExpand(savedUser.getId()).toUri();
        return ResponseEntity.created(location).body(savedUser);

    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id){
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());

    }
}