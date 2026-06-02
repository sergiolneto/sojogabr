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
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
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
    }

    private User createUser(String username, String nome) {
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setPk("USER#" + username);
        user.setSk("METADATA");
        user.setUsername(username);
        user.setPassword("password");
        user.setNome(nome);
        user.setStatus(User.UserStatus.ACTIVE);
        user.setRole(User.UserRole.USER);
        return user;
    }

    @Test
    @DisplayName("Deve salvar um usuario e permitir busca por username")
    void save_shouldSaveUserAndAllowFindByUsername() {
        String username = "testuser-" + UUID.randomUUID();
        User user = createUser(username, "Test User");

        userRepository.save(user);

        Optional<User> foundUserOpt = userRepository.findByUsername(username);

        assertThat(foundUserOpt).isPresent();
        User foundUser = foundUserOpt.get();
        assertThat(foundUser.getId()).isEqualTo(user.getId());
        assertThat(foundUser.getUsername()).isEqualTo(username);
        assertThat(foundUser.getNome()).isEqualTo("Test User");

        Key key = Key.builder().partitionValue(user.getPk()).sortValue(user.getSk()).build();
        User userFromDb = userTable.getItem(key);
        assertThat(userFromDb).isNotNull();
        assertThat(userFromDb.getUsername()).isEqualTo(username);
    }

    @Test
    @DisplayName("Deve encontrar usuario pelo ID (PK)")
    void findById_shouldReturnUser() {
        String username = "testuser2-" + UUID.randomUUID();
        User user = createUser(username, "Test User 2");
        userRepository.save(user);

        Optional<User> foundUserOpt = userRepository.findById(user.getPk());

        assertThat(foundUserOpt).isPresent();
        assertThat(foundUserOpt.get().getUsername()).isEqualTo(username);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio para username nao existente")
    void findByUsername_shouldReturnEmptyOptionalForNonExistingUser() {
        Optional<User> foundUserOpt = userRepository.findByUsername("nonexistent-" + UUID.randomUUID());

        assertThat(foundUserOpt).isNotPresent();
    }

    @Test
    @DisplayName("Deve listar usuarios pendentes usando paginacao por status")
    void findByStatusPaginated_shouldReturnPendingUsers() {
        User firstUser = createUser("pending-1-" + UUID.randomUUID(), "Pending User 1");
        firstUser.setStatus(User.UserStatus.PENDING_APPROVAL);
        User secondUser = createUser("pending-2-" + UUID.randomUUID(), "Pending User 2");
        secondUser.setStatus(User.UserStatus.PENDING_APPROVAL);

        userRepository.save(firstUser);
        userRepository.save(secondUser);

        Page<User> firstPage = userRepository.findByStatusPaginated(User.UserStatus.PENDING_APPROVAL, 1, null);

        assertThat(firstPage.items()).hasSize(1);
        assertThat(firstPage.lastEvaluatedKey()).isNotEmpty();

        Page<User> secondPage = userRepository.findByStatusPaginated(
                User.UserStatus.PENDING_APPROVAL, 10, firstPage.lastEvaluatedKey());

        assertThat(Stream.concat(firstPage.items().stream(), secondPage.items().stream()).toList())
                .extracting(User::getUsername)
                .contains(firstUser.getUsername(), secondUser.getUsername());
    }
}
