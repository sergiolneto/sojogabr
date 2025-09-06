package com.br.sojogabr.api.dto;

public record LoginResponse(String token, UserResponse user) {
}