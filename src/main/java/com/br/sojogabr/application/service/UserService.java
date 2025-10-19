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

import java.util.List;
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

        newUser.setRole(User.UserRole.USER);
        newUser.setStatus(User.UserStatus.PENDING_APPROVAL);

        return userRepository.save(newUser);
    }

    // --- Implementação dos novos métodos de administração ---

    @Override
    public List<User> findPendingUsers() {
        return userRepository.findByStatus(User.UserStatus.PENDING_APPROVAL);
    }

    @Override
    public User approveUser(String username) {
        User userToApprove = findByUsername(username); // Reusa o métodos que já lança exceção se não encontrar
        userToApprove.setStatus(User.UserStatus.ACTIVE);
        return userRepository.save(userToApprove);
    }

    @Override
    public User promoteUser(String username) {
        User userToPromote = findByUsername(username);
        userToPromote.setRole(User.UserRole.POWER_USER);
        return userRepository.save(userToPromote);
    }
}