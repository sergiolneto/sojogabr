package com.br.sojogabr.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    @DisplayName("Deve criar um usuário com o construtor sem argumentos")
    void shouldCreateUserWithNoArgsConstructor() {
        // Arrange & Act
        User user = new User();

        // Assert
        assertThat(user).isNotNull();
        assertThat(user.getPk()).isNull();
        assertThat(user.getNome()).isNull();
    }

    @Test
    @DisplayName("Deve criar um usuário com o construtor com todos os argumentos")
    void shouldCreateUserWithAllArgsConstructor() {
        // Arrange
        List<String> esportes = List.of("Futebol", "Vôlei");

        // Act
        User user = new User(
                "123", "USER#johndoe", "METADATA", "John Doe", "john.doe@email.com",
                "Rua X, 123", "11999998888", "@johndoe", esportes,
                "Time A", "Atacante", true, "johndoe", "senha123",
                User.UserRole.USER, User.UserStatus.ACTIVE // Adiciona os novos campos
        );

        // Assert
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo("123");
        assertThat(user.getPk()).isEqualTo("USER#johndoe");
        assertThat(user.getSk()).isEqualTo("METADATA");
        assertThat(user.getNome()).isEqualTo("John Doe");
        assertThat(user.getEmail()).isEqualTo("john.doe@email.com");
        assertThat(user.getEsportes()).containsExactly("Futebol", "Vôlei");
        assertThat(user.getUsername()).isEqualTo("johndoe");
        assertThat(user.isCapitao()).isTrue();
        assertThat(user.getRole()).isEqualTo(User.UserRole.USER);
        assertThat(user.getStatus()).isEqualTo(User.UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("Deve retornar os valores corretos para as chaves do DynamoDB")
    void shouldReturnCorrectValuesForDynamoDbKeys() {
        // Arrange
        User user = new User();
        user.setPk("USER#testuser");
        user.setSk("METADATA");
        user.setUsername("testuser");

        // Act & Assert
        assertThat(user.getPk()).isEqualTo("USER#testuser");
        assertThat(user.getSk()).isEqualTo("METADATA");
        assertThat(user.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Deve permitir a modificação dos campos através dos setters")
    void shouldAllowFieldModificationViaSetters() {
        // Arrange
        User user = new User();

        // Act
        user.setNome("Jane Doe");
        user.setEmail("jane.doe@email.com");
        user.setCapitao(false);
        List<String> novosEsportes = List.of("Basquete");
        user.setEsportes(novosEsportes);
        user.setRole(User.UserRole.ADMIN);
        user.setStatus(User.UserStatus.INACTIVE);


        // Assert
        assertThat(user.getNome()).isEqualTo("Jane Doe");
        assertThat(user.getEmail()).isEqualTo("jane.doe@email.com");
        assertThat(user.isCapitao()).isFalse();
        assertThat(user.getEsportes()).isEqualTo(novosEsportes);
        assertThat(user.getRole()).isEqualTo(User.UserRole.ADMIN);
        assertThat(user.getStatus()).isEqualTo(User.UserStatus.INACTIVE);
    }
}