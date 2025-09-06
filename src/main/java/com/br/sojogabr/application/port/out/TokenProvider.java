package com.br.sojogabr.application.port.out;

import com.br.sojogabr.domain.model.User;
import org.springframework.security.core.userdetails.UserDetails;

public interface TokenProvider {

    String generateToken(User user);

    boolean isTokenValid(String token, UserDetails userDetails);

    String extractUsername(String token);
}