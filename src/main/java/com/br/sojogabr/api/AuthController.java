package com.br.sojogabr.api;

import com.br.sojogabr.api.dto.LoginRequest;
import com.br.sojogabr.domain.model.User;
import com.br.sojogabr.domain.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final UserRepository userRepository;
    // Removido para simplificar, já que não estamos implementando a lógica de token JWT aqui.
    // private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    // 1. Injeção de dependência via construtor (prática recomendada)
    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        // NOTA: Este método assume que existe um `findByUsername` em seu UserRepository.
        // Isso provavelmente exigirá um Índice Secundário Global (GSI) no campo 'username' da sua tabela DynamoDB.
        Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());
    
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // 2. Compara a senha fornecida com a senha criptografada armazenada
            if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                // Em uma implementação real, aqui você geraria um token JWT.
                // Por simplicidade, retornamos uma confirmação com o nome de usuário.
                return ResponseEntity.ok(Map.of("username", user.getUsername()));
            }
        }
        // 3. Mensagem de erro genérica por segurança
        return ResponseEntity.status(401).body(Map.of("message", "Usuário ou senha inválidos"));
    }

    @PostMapping("/users")
    public ResponseEntity<User> saveUser(@RequestBody User user, UriComponentsBuilder uriBuilder) {
        if (user.getId() == null || user.getId().isEmpty()) {
            user.setId(UUID.randomUUID().toString());
        }
        // 4. Criptografa a senha antes de salvar o novo usuário
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);

        URI location = uriBuilder.path("/api/users/{id}").buildAndExpand(savedUser.getId()).toUri();

        // 5. NUNCA retorne a senha (mesmo que criptografada) na resposta da API
        savedUser.setPassword(null);

        return ResponseEntity.created(location).body(savedUser);

    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id){
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());

    }
}