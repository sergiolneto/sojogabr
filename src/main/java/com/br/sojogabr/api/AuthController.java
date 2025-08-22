package com.br.sojogabr.api;

import com.br.sojogabr.api.dto.LoginRequest;
import com.br.sojogabr.domain.model.User;
import com.br.sojogabr.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public Object login(@RequestBody LoginRequest loginRequest) {
        User user = userRepository.findById("1").orElse(null);
        if (user != null && "senha123".equals(loginRequest.getPassword())) {
            return user;
        } else {
            return "Usuário ou senha inválidos.";
        }
    }

    @PostMapping("/users")
    public User saveUser(@RequestBody User user) {
        return userRepository.save(user);
    }
}