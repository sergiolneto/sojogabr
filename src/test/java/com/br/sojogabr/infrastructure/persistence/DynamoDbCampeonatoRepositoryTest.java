package com.br.sojogabr.infrastructure.persistence;

import com.br.sojogabr.domain.model.campeonato.dynamo.CampeonatoItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.DYNAMODB;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class DynamoDbCampeonatoRepositoryTest {

    @Container
    static LocalStackContainer localStack =
            new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.2.0"))
                    .withServices(DYNAMODB);

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.aws.dynamodb.endpoint", () -> localStack.getEndpointOverride(DYNAMODB).toString());
        registry.add("spring.cloud.aws.credentials.access-key", localStack::getAccessKey);
        registry.add("spring.cloud.aws.credentials.secret-key", localStack::getSecretKey);
        registry.add("spring.cloud.aws.region.static", localStack::getRegion);
    }

    @Autowired
    private DynamoDbCampeonatoRepository campeonatoRepository;

    @Autowired
    private DynamoDbEnhancedClient enhancedClient;

    private DynamoDbTable<CampeonatoItem> campeonatoTable;

    @BeforeEach
    void setUp() {
        String tableName = "sojoga-test-table";
        campeonatoTable = enhancedClient.table(tableName, TableSchema.fromBean(CampeonatoItem.class));
        try {
            campeonatoTable.deleteTable();
        } catch (Exception e) {
            // Ignora caso a tabela não exista na primeira execução
        }
        campeonatoTable.createTable();
    }

    @Test
    @DisplayName("Deve salvar um campeonato com seus times e classificações em uma transação")
    void save_shouldCreateAllChampionshipItemsInTransaction() {
        // Arrange
        String nome = "Brasileirão 2024";
        String esporte = "Futebol";
        List<String> timeIds = List.of("FLA", "PAL");

        // Act
        CampeonatoItem resultado = campeonatoRepository.save(nome, esporte, timeIds);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getPk()).startsWith("CHAMP#");
        assertThat(resultado.getSk()).isEqualTo("METADATA");
        assertThat(resultado.getNome()).isEqualTo(nome);

        // Verifica diretamente no DynamoDB se todos os itens foram criados
        Key queryKey = Key.builder().partitionValue(resultado.getPk()).build();
        List<CampeonatoItem> items = campeonatoTable.query(QueryConditional.keyEqualTo(queryKey)).items().stream().toList();

        // Deve haver 1 (METADATA) + 2 (TEAM#) + 2 (STANDINGS#) = 5 itens
        assertThat(items).hasSize(5);

        // Verifica o item de metadados
        Optional<CampeonatoItem> metadataItem = items.stream().filter(i -> i.getSk().equals("METADATA")).findFirst();
        assertThat(metadataItem).isPresent();
        assertThat(metadataItem.get().getNome()).isEqualTo(nome);
        assertThat(metadataItem.get().getEsporte()).isEqualTo(esporte);

        // Verifica os links dos times
        Optional<CampeonatoItem> time1Link = items.stream().filter(i -> i.getSk().equals("TEAM#FLA")).findFirst();
        assertThat(time1Link).isPresent();

        Optional<CampeonatoItem> time2Link = items.stream().filter(i -> i.getSk().equals("TEAM#PAL")).findFirst();
        assertThat(time2Link).isPresent();

        // Verifica as classificações iniciais
        Optional<CampeonatoItem> time1Standings = items.stream().filter(i -> i.getSk().equals("STANDINGS#FLA")).findFirst();
        assertThat(time1Standings).isPresent();
        assertThat(time1Standings.get().getPontos()).isZero();
        assertThat(time1Standings.get().getJogos()).isZero();

        Optional<CampeonatoItem> time2Standings = items.stream().filter(i -> i.getSk().equals("STANDINGS#PAL")).findFirst();
        assertThat(time2Standings).isPresent();
        assertThat(time2Standings.get().getPontos()).isZero();
    }
}