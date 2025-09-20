package com.br.sojogabr.application.port.in;

import com.br.sojogabr.api.dto.RegisterRequest;
import com.br.sojogabr.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserUseCase {
    User findByUsername(String username);
    Optional<User> findByUsernameOptional(String username);
    User registerUser(RegisterRequest request);

    // --- Novos métodos para administração ---

    /**
     * Encontra todos os usuários que estão com o status PENDING_APPROVAL.
     * @return Uma lista de usuários pendentes.
     */
    List<User> findPendingUsers();

    /**
     * Aprova um usuário, mudando seu status para ACTIVE.
     * @param username O nome de usuário a ser aprovado.
     * @return O usuário atualizado.
     */
    User approveUser(String username);

    /**
     * Promove um usuário para a role POWER_USER.
     * @param username O nome de usuário a ser promovido.
     * @return O usuário atualizado.
     */
    User promoteUser(String username);
}