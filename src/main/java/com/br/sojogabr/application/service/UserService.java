package com.br.sojogabr.application.service;

import com.br.sojogabr.api.dto.RegisterRequest;
import com.br.sojogabr.api.exception.UserAlreadyExistsException;
import com.br.sojogabr.application.port.in.UserUseCase;
import com.br.sojogabr.domain.model.User;
import com.br.sojogabr.domain.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserUseCase, UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
    }

    @Override
    public Optional<User> findByUsernameOptional(String username){
        return userRepository.findByUsername(username);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
    }

    @Override
    public User registerUser(RegisterRequest request) {
        // Validação para evitar usuários duplicados
        userRepository.findByUsername(request.username()).ifPresent(user -> {
            throw new UserAlreadyExistsException("Username '" + request.username() + "' já está em uso.");
        });

        User newUser = new User();
        newUser.setUsername(request.username());
        newUser.setNome(request.nome());
        newUser.setEmail(request.email());
        newUser.setPassword(passwordEncoder.encode(request.password()));
        newUser.setEsportes(request.esportes());
        newUser.setId(UUID.randomUUID().toString());

        newUser.setPk("USER#" + newUser.getUsername());
        newUser.setSk("METADATA");
        
        return userRepository.save(newUser);
    }
}