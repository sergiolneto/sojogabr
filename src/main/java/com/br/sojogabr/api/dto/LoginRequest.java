package com.br.sojogabr.api.dto;

import lombok.Getter;
import lombok.Setter;

// Usamos DTOs (Data Transfer Objects) para desacoplar a API da entidade de domínio.
@Getter
@Setter
public class LoginRequest {
    private String username;
    private String password;
}