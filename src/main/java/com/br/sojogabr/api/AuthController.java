package com.br.sojogabr.api;

import com.br.sojogabr.domain.model.LoginRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class AuthController {

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest loginRequest) {
        // Aqui você implementaria a lógica de autenticação
        // Por exemplo, verificar no banco de dados se o usuário e senha são válidos
        if ("usuario".equals(loginRequest.getUsername()) && "senha123".equals(loginRequest.getPassword())) {
            return "Login bem-sucedido!";
        } else {
            return "Usuário ou senha inválidos.";
        }
    }
}