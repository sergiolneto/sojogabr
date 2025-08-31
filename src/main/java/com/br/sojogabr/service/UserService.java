package com.br.sojogabr.service;

import com.br.sojogabr.domain.model.User;
import com.br.sojogabr.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(User user) {
        // Garante que o usuário tenha um ID único antes de ser salvo.
        if (user.getId() == null || user.getId().isEmpty()) {
            user.setId(UUID.randomUUID().toString());
        }

        // Criptografa a senha antes de qualquer outra operação
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Log 1: Dados que serão enviados para o banco (sem a senha!)
        logger.info("Iniciando processo de cadastro para o usuário: '{}'. " +
                    "Dados a serem salvos: [Nome: {}, Email: {}, Insta: {}, Esportes: {}]",
                    user.getUsername(), user.getNome(),  user.getEmail(),
                    user.getInsta(), user.getEsportes());

        User savedUser = userRepository.save(user);

        // Log 2: Confirmação com a resposta do banco (o ID é a melhor confirmação)
        logger.info("Usuário '{}' cadastrado com sucesso no banco de dados. ID gerado: {}",
                savedUser.getUsername(), savedUser.getId());

        return savedUser;
    }
}