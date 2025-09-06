package com.br.sojogabr.domain.repository;

import com.br.sojogabr.AbstractIntegrationTest;
import com.br.sojogabr.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.EnhancedGlobalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.model.ProjectionType;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers // Adicionada para ativar o @DynamicPropertySource
@Import(AbstractIntegrationTest.DynamoDbTestConfig.class)
class UserRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DynamoDbEnhancedClient enhancedClient;

    private DynamoDbTable<User> userTable;

    @BeforeEach
    void setUp() {
        userTable = enhancedClient.table("Usuario", TableSchema.fromBean(User.class));
        try {
            userTable.deleteTable();
        } catch (Exception e) {
            // Ignora caso a tabela não exista
        }

        EnhancedGlobalSecondaryIndex gsi = EnhancedGlobalSecondaryIndex.builder()
                .indexName("username-index")
                .projection(p -> p.projectionType(ProjectionType.ALL))
                .build();

        userTable.createTable(r -> r.globalSecondaryIndices(gsi));
    }

    private User createUser(String username, String nome) {
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setPk("USER#" + username);
        user.setSk("METADATA");
        user.setUsername(username);
        user.setPassword("password");
        user.setNome(nome);
        return user;
    }

    @Test
    @DisplayName("Deve salvar um usuário e permitir busca por username")
    void save_shouldSaveUserAndAllowFindByUsername() {
        // Arrange
        String username = "testuser";
        User user = createUser(username, "Test User");

        // Act
        userRepository.save(user);

        // Assert
        Optional<User> foundUserOpt = userRepository.findByUsername(username);

        assertThat(foundUserOpt).isPresent();
        User foundUser = foundUserOpt.get();
        assertThat(foundUser.getId()).isEqualTo(user.getId());
        assertThat(foundUser.getUsername()).isEqualTo(username);
        assertThat(foundUser.getNome()).isEqualTo("Test User");

        // Assert direto na tabela
        Key key = Key.builder().partitionValue(user.getPk()).sortValue(user.getSk()).build();
        User userFromDb = userTable.getItem(key);
        assertThat(userFromDb).isNotNull();
        assertThat(userFromDb.getUsername()).isEqualTo(username);
    }

    @Test
    @DisplayName("Deve encontrar usuário pelo ID (PK)")
    void findById_shouldReturnUser() {
        // Arrange
        String username = "testuser2";
        User user = createUser(username, "Test User 2");
        userRepository.save(user);

        // Act
        Optional<User> foundUserOpt = userRepository.findById(user.getPk());

        // Assert
        assertThat(foundUserOpt).isPresent();
        assertThat(foundUserOpt.get().getUsername()).isEqualTo(username);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio para username não existente")
    void findByUsername_shouldReturnEmptyOptionalForNonExistingUser() {
        // Act
        Optional<User> foundUserOpt = userRepository.findByUsername("nonexistent");

        // Assert
        assertThat(foundUserOpt).isNotPresent();
    }
}
