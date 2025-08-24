package com.br.sojogabr.api;

import com.br.sojogabr.api.dto.LoginRequest;
import com.br.sojogabr.domain.model.User;
import com.br.sojogabr.domain.repository.UserRepository;
import com.br.sojogabr.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    // 1. Injeção de dependência via construtor (prática recomendada)
    public AuthController(UserRepository userRepository, JwtService jwtService, JwtService jwtService1) {
        this.userRepository = userRepository;
        this.jwtService = jwtService1;
        JwtService jwtService2 = this.jwtService;

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
            // 2. Lógica de login mais robusta e com retorno padronizado
            // Em um app real, a senha seria criptografada (ex: com BCrypt)
        Optional<User> userOptional = userRepository.findById(loginRequest.getUsername());

        if (userOptional.isPresent() && "senha123".equals(loginRequest.getPassword())){
            org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(userOptional.get().getId(), "", new ArrayList<>());
            String token = jwtService.generateToken(userDetails);
            return ResponseEntity.ok(Map.of("token", token));
        } else {
            return ResponseEntity.status(401).body("Usuário ou senha inválidos");
        }
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