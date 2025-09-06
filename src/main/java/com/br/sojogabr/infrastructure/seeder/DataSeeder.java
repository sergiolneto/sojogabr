package com.br.sojogabr.infrastructure.seeder;

import com.br.sojogabr.domain.model.User;
import com.br.sojogabr.domain.service.CampeonatoService;
import com.br.sojogabr.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Esta classe é executada na inicialização da aplicação para popular o banco de dados
 * com dados iniciais, caso ele esteja vazio.
 * A anotação @Profile("dev") garante que ela só será executada quando o perfil 'dev'
 * estiver ativo, evitando que rode em produção.
 */
@Component
@Profile("dev") // Garante que este seeder não rode em produção
@Order(2) // Garante que este bean seja executado depois dos inicializadores
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CampeonatoService campeonatoService;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder, CampeonatoService campeonatoService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.campeonatoService = campeonatoService;
    }

    @Override
    public void run(String... args) throws Exception {
        // Verifica se já existem usuários para não popular o banco novamente
        if (userRepository.findByUsername("admin").isEmpty()) {
            logger.info("Banco de dados vazio. Populando com dados iniciais...");
            createUsers();
            createChampionships();
            logger.info("Povoamento do banco de dados concluído.");
        } else {
            logger.info("O banco de dados já contém dados. O seeder não será executado.");
        }
    }

    private void createUsers() {
        // Usuário 1 - Administrador
        User admin = new User();
        admin.setUsername("admin");
        admin.setNome("Administrador do Sistema");
        admin.setEmail("admin@sojoga.br");
        // A senha 'admin' será codificada
        // O campo 'id' também precisa ser preenchido, pois ele é provavelmente a chave de partição real
        // na entidade User, e não pode ser nulo.
        admin.setId(UUID.randomUUID().toString());
        admin.setPassword(passwordEncoder.encode("admin"));
        admin.setPk("USER#admin"); // Chave de Partição
        admin.setSk("METADATA");   // Chave de Classificação
        admin.setEsportes(List.of("Todos"));

        // Usuário 2 - Jogador Comum
        User player1 = new User();
        player1.setUsername("leomessi");
        player1.setNome("Lionel Messi");
        player1.setEmail("messi@sojoga.br");
        player1.setId(UUID.randomUUID().toString());
        player1.setPassword(passwordEncoder.encode("senha123"));
        player1.setPk("USER#leomessi");
        player1.setSk("METADATA");
        player1.setEsportes(List.of("Futebol", "Futevôlei"));
        player1.setPosicao("Atacante");
        player1.setTime("Inter Miami");

        // Salva os usuários no DynamoDB
        userRepository.save(admin);
        userRepository.save(player1);

        // Você pode adicionar a criação de outros itens aqui:
        // - Times
        // - Campeonatos de exemplo (usando o CriarCampeonatoUseCase)
        // - Partidas
    }

    private void createChampionships() {
        logger.info("Criando campeonatos de exemplo...");

        // Mock de IDs de times para o campeonato. Em um cenário real,
        // eles viriam de um repositório de times.
        List<String> teamIds = List.of(UUID.randomUUID().toString(), UUID.randomUUID().toString());

        campeonatoService.criarCampeonato(
                "Copa de Verão Sojoga",
                "Futebol",
                teamIds
        );
        logger.info("Campeonato 'Copa de Verão Sojoga' criado.");
    }
}