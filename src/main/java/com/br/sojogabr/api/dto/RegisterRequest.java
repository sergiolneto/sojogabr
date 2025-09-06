package com.br.sojogabr.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RegisterRequest(
        @NotBlank(message = "O nome de usuário não pode ser vazio.")
        String username,

        @NotBlank(message = "A senha não pode ser vazia.")
        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres.")
        String password,

        @NotBlank(message = "O nome não pode ser vazio.")
        String nome,

        @NotBlank(message = "O email não pode ser vazio.")
        @Email(message = "O formato do email é inválido.")
        String email,

        List<String> esportes
) {}