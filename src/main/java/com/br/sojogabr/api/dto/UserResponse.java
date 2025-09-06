package com.br.sojogabr.api.dto;

import com.br.sojogabr.domain.model.User;

import java.util.List;

public record UserResponse(
        String id,
        String username,
        String nome,
        String email,
        List<String> esportes
) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getNome(),
                user.getEmail(),
                user.getEsportes()
        );
    }
}