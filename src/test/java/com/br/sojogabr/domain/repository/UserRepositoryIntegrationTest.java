package com.br.sojogabr.domain.repository;

import com.br.sojogabr.domain.model.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class UserRepositoryIntegrationTest {

    // Define a imagem e os serviços que o Testcontainers deve usar
    private static final DockerImageName LOCALSTACK_IMAGE = DockerImageName.parse("localstack/localstack:3.5.0");

    @Container
    static LocalStackContainer localStack = new LocalStackContainer(LOCALSTACK_IMAGE)
            .withServices(LocalStackContainer.Service.DYNAMODB);

    @Autowired
    private UserRepository userRepository;

    // Configura dinamicamente a aplicação para se conectar ao container do LocalStack
    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.aws.dynamodb.endpoint", () -> localStack.getEndpointOverride(LocalStackContainer.Service.DYNAMODB));
        registry.add("spring.cloud.aws.credentials.access-key", localStack::getAccessKey);
        registry.add("spring.cloud.aws.credentials.secret-key", localStack::getSecretKey);
        registry.add("spring.cloud.aws.region.static", localStack::getRegion);
    }

    // Antes de todos os testes, executa o script para criar a tabela dentro do container
    @BeforeAll
    static void setup() throws IOException, InterruptedException {
        // Garante que o AWS CLI esteja disponível para o container executar o script
        localStack.execInContainer("pip", "install", "awscli");

        // Executa o script de criação da tabela
        localStack.execInContainer("awslocal", "dynamodb", "create-table",
                "--table-name", "Usuario",
                "--attribute-definitions", "AttributeName=id,AttributeType=S",
                "--key-schema", "AttributeName=id,KeyType=HASH",
                "--provisioned-throughput", "ReadCapacityUnits=5,WriteCapacityUnits=5"
        );
    }

    /**
     * Limpa a tabela antes de cada teste para garantir o isolamento.
     * Isso evita que um teste interfira no resultado do outro.
     */
    @BeforeEach
    void cleanUpTable() {
        // A operação scan() lê todos os itens, e então deletamos um por um.
        userRepository.findAll().forEach(user -> userRepository.deleteById(user.getId()));
    }

    @Test
    void shouldSaveUserAndFindById() {
        // Arrange: Cria um novo usuário
		User newUser = createTestUser(UUID.randomUUID().toString(), "Test User", List.of("Futebol", "Vôlei"));

        // Act: Salva o usuário
        userRepository.save(newUser);

        // Assert: Busca o usuário e verifica se os dados estão corretos
		Optional<User> foundUserOpt = userRepository.findById(newUser.getId());
        assertThat(foundUserOpt).isPresent();
        User foundUser = foundUserOpt.get();
		assertThat(foundUser.getId()).isEqualTo(newUser.getId());
        assertThat(foundUser.getNome()).isEqualTo("Test User");
        assertThat(foundUser.getEsportes()).containsExactly("Futebol", "Vôlei");
    }

    @Test
    void shouldFindAllUsers() {
        // Arrange: Cria e salva dois usuários
		userRepository.save(createTestUser(UUID.randomUUID().toString(), "User One", null));
		userRepository.save(createTestUser(UUID.randomUUID().toString(), "User Two", null));

        // Act: Busca todos os usuários
        List<User> users = userRepository.findAll();

        // Assert: Verifica se a lista contém pelo menos os dois usuários criados
        assertThat(users).hasSize(2);
		assertThat(users).extracting(User::getNome).containsExactlyInAnyOrder("User One", "User Two");
    }

    @Test
    void shouldDeleteUser() {
        // Arrange: Cria e salva um usuário
		User userToDelete = createTestUser(UUID.randomUUID().toString(), "User to Delete", null);
        userRepository.save(userToDelete);

        // Garante que o usuário foi salvo
		assertThat(userRepository.findById(userToDelete.getId())).isPresent();

        // Act: Deleta o usuário
		userRepository.deleteById(userToDelete.getId());

        // Assert: Verifica se o usuário não é mais encontrado
		assertThat(userRepository.findById(userToDelete.getId())).isNotPresent();
	}

	/**
	 * Método auxiliar para reduzir a duplicação de código na criação de usuários para testes.
	 */
	private User createTestUser(String id, String name, List<String> sports) {
		User user = new User();
		user.setId(id);
		user.setNome(name);
		if (sports != null) {
			user.setEsportes(sports);
		}
		return user;
    }
}