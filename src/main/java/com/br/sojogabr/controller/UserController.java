package com.br.sojogabr.controller;

import com.br.sojogabr.api.dto.UserResponse;
import com.br.sojogabr.domain.model.User;
import com.br.sojogabr.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);
        // Converte a entidade para o DTO de resposta antes de enviar ao cliente.
        logger.info("Convertendo o DTO");
        return new ResponseEntity<>(UserResponse.fromEntity(createdUser), HttpStatus.CREATED);
    }
}