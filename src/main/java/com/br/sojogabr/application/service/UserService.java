package com.br.sojogabr.application.service;

import com.br.sojogabr.api.dto.PaginatedResponse;
import com.br.sojogabr.api.dto.RegisterRequest;
import com.br.sojogabr.api.exception.UserAlreadyExistsException;
import com.br.sojogabr.application.port.in.UserUseCase;
import com.br.sojogabr.domain.model.User;
import com.br.sojogabr.domain.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserUseCase, UserDetailsService {

    private static final int DEFAULT_PAGE_LIMIT = 10;
    private static final int MAX_PAGE_LIMIT = 100;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado: " + username));
    }

    @Override
    public Optional<User> findByUsernameOptional(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado: " + username));
    }

    @Override
    public User registerUser(RegisterRequest request) {
        userRepository.findByUsername(request.username()).ifPresent(user -> {
            throw new UserAlreadyExistsException("Username '" + request.username() + "' ja esta em uso.");
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

    @Override
    public List<User> findPendingUsers() {
        return userRepository.findByStatus(User.UserStatus.PENDING_APPROVAL);
    }

    @Override
    public PaginatedResponse<User> findPendingUsersPaginated(Integer limit, String lastKey) {
        int pageLimit = normalizeLimit(limit);
        Map<String, AttributeValue> exclusiveStartKey = decodeLastKey(lastKey);

        Page<User> page = userRepository.findByStatusPaginated(
                User.UserStatus.PENDING_APPROVAL, pageLimit, exclusiveStartKey);

        return new PaginatedResponse<>(page.items(), encodeLastKey(page.lastEvaluatedKey()));
    }

    @Override
    public User approveUser(String username) {
        User userToApprove = findByUsername(username);
        userToApprove.setStatus(User.UserStatus.ACTIVE);
        return userRepository.save(userToApprove);
    }

    @Override
    public User promoteUser(String username) {
        User userToPromote = findByUsername(username);
        userToPromote.setRole(User.UserRole.POWER_USER);
        return userRepository.save(userToPromote);
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_PAGE_LIMIT;
        }
        return Math.max(1, Math.min(limit, MAX_PAGE_LIMIT));
    }

    private Map<String, AttributeValue> decodeLastKey(String lastKey) {
        if (lastKey == null || lastKey.isBlank()) {
            return null;
        }

        try {
            byte[] decoded = Base64.getUrlDecoder().decode(lastKey);
            Map<String, String> stringKey = objectMapper.readValue(
                    new String(decoded, StandardCharsets.UTF_8),
                    new TypeReference<>() {
                    });

            Map<String, AttributeValue> attributeKey = new HashMap<>();
            stringKey.forEach((key, value) -> attributeKey.put(key, AttributeValue.fromS(value)));
            return attributeKey;
        } catch (IllegalArgumentException | JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid pagination token.", e);
        }
    }

    private String encodeLastKey(Map<String, AttributeValue> lastEvaluatedKey) {
        if (lastEvaluatedKey == null || lastEvaluatedKey.isEmpty()) {
            return null;
        }

        Map<String, String> stringKey = new HashMap<>();
        lastEvaluatedKey.forEach((key, value) -> stringKey.put(key, value.s()));

        try {
            String json = objectMapper.writeValueAsString(stringKey);
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to encode pagination token.", e);
        }
    }
}
