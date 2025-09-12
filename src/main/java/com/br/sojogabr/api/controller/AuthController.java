package com.br.sojogabr.api.controller;

import com.br.sojogabr.api.dto.LoginRequest;
import com.br.sojogabr.api.dto.LoginResponse;
import com.br.sojogabr.api.dto.UserResponse;
import com.br.sojogabr.application.port.out.TokenProvider;
import com.br.sojogabr.domain.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;

    public AuthController(AuthenticationManager authenticationManager, TokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        User user = (User) authentication.getPrincipal();
        String token = tokenProvider.generateToken(user);
        UserResponse userResponse = UserResponse.fromEntity(user);

        return ResponseEntity.ok(new LoginResponse(token, userResponse));
    }
}