package com.br.sojogabr.application.port.in;

import com.br.sojogabr.api.dto.RegisterRequest;
import com.br.sojogabr.domain.model.User;

import java.util.Optional;

public interface UserUseCase {
    User findByUsername(String username);
    Optional<User> findByUsernameOptional(String username);
    User registerUser(RegisterRequest request);
}