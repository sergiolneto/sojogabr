package com.br.sojogabr.api;

import com.br.sojogabr.api.dto.UserResponse;
import com.br.sojogabr.api.dto.LoginRequest;
import com.br.sojogabr.api.dto.UserLoginResponse;
import com.br.sojogabr.domain.model.User;
import com.br.sojogabr.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    // Dependências para o fluxo de autenticação padrão do Spring Security
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    // 1. Injeção de dependência via construtor (prática recomendada)
    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(@RequestBody LoginRequest loginRequest) {
        logger.info("Tentativa de login recebida para o usuário: '{}'", loginRequest.getUsername());

        // Delega a autenticação para o Spring Security.
        // Se as credenciais estiverem erradas, ele lançará uma exceção
        // que será capturada pelo GlobalExceptionHandler.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        UserLoginResponse response = new UserLoginResponse(userDetails.getUsername());

        logger.info("Usuário '{}' autenticado com sucesso.", response.getUsername());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id){
        return userRepository.findById(id)
                .map(UserResponse::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());

    }
}