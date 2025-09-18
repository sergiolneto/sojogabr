package com.br.sojogabr.api.controller;

import com.br.sojogabr.api.dto.UserResponse;
import com.br.sojogabr.application.port.in.UserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserUseCase userUseCase;

    /**
     * Lista todos os usuários que estão aguardando aprovação.
     * Acessível por ADMINs e POWER_USERs.
     */
    @GetMapping("/users/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'POWER_USER')")
    public ResponseEntity<List<UserResponse>> listPendingUsers() {
        List<UserResponse> pendingUsers = userUseCase.findPendingUsers().stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(pendingUsers);
    }

    /**
     * Aprova um usuário, mudando seu status para ACTIVE.
     * Acessível por ADMINs e POWER_USERs.
     */
    @PostMapping("/users/{username}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'POWER_USER')")
    public ResponseEntity<UserResponse> approveUser(@PathVariable String username) {
        UserResponse updatedUser = UserResponse.fromEntity(userUseCase.approveUser(username));
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Promove um usuário para a role POWER_USER.
     * Acessível apenas por ADMINs.
     */
    @PostMapping("/users/{username}/promote")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> promoteUser(@PathVariable String username) {
        UserResponse updatedUser = UserResponse.fromEntity(userUseCase.promoteUser(username));
        return ResponseEntity.ok(updatedUser);
    }
}
