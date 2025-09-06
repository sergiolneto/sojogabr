package com.br.sojogabr.api.controller;

import com.br.sojogabr.api.dto.LoginRequest;
import com.br.sojogabr.api.dto.LoginResponse;
import com.br.sojogabr.api.dto.RegisterRequest;
import com.br.sojogabr.api.dto.UserResponse;
import com.br.sojogabr.application.port.in.UserUseCase;
import com.br.sojogabr.application.port.out.TokenProvider;
import com.br.sojogabr.domain.model.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserUseCase userUseCase;
    private final TokenProvider tokenProvider;

    public AuthController(AuthenticationManager authenticationManager, UserUseCase userUseCase, TokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userUseCase = userUseCase;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                // Acessa os campos do record diretamente, sem o "get"
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        User user = userUseCase.findByUsername(request.username());
        String token = tokenProvider.generateToken(user);
        UserResponse userResponse = UserResponse.fromEntity(user);

        return ResponseEntity.ok(new LoginResponse(token, userResponse));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        User newUser = userUseCase.registerUser(request);
        return ResponseEntity.status(201).body(UserResponse.fromEntity(newUser));
    }
}