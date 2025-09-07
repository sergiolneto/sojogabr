package com.br.sojogabr.infrastructure.configuration;

import com.br.sojogabr.domain.model.User;
import com.br.sojogabr.domain.model.campeonato.dynamo.CampeonatoItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.EnhancedGlobalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.model.ProjectionType;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;

@Configuration
@Profile("dev") // Garante que este inicializador rode apenas no perfil de desenvolvimento
@Order(1) // Garante que este bean seja executado antes de outros ApplicationRunner
public class DynamoDbInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DynamoDbInitializer.class);
    private final DynamoDbTable<User> userTable;
    private final DynamoDbTable<CampeonatoItem> campeonatoTable;

    public DynamoDbInitializer(DynamoDbTable<User> userTable, DynamoDbTable<CampeonatoItem> campeonatoTable) {
        this.userTable = userTable;
        this.campeonatoTable = campeonatoTable;
    }

    @Override
    public void run(String... args) {
        createUserTable();
        createCampeonatoTable();
    }

    private void createUserTable() {
        try {
            logger.info("Verificando e criando a tabela '{}' no DynamoDB (Usuário)...", userTable.tableName());
            EnhancedGlobalSecondaryIndex gsi = EnhancedGlobalSecondaryIndex.builder()
                    .indexName("username-index")
                    .projection(p -> p.projectionType(ProjectionType.ALL))
                    .build();
            userTable.createTable(r -> r.globalSecondaryIndices(gsi));
            logger.info("Tabela '{}' e GSI 'username-index' criados com sucesso.", userTable.tableName());
        } catch (ResourceInUseException e) {
            logger.info("A tabela Usuário '{}' já existe. Nenhuma ação necessária.", userTable.tableName());
        }
    }

    private void createCampeonatoTable() {
        try {
            logger.info("Verificando e criando a tabela '{}' no DynamoDB (LocalStack)...", campeonatoTable.tableName());
            campeonatoTable.createTable();
            logger.info("Tabela '{}' criada com sucesso.", campeonatoTable.tableName());
        } catch (ResourceInUseException e) {
            logger.info("A tabela '{}' já existe. Nenhuma ação necessária.", campeonatoTable.tableName());
        }
    }
}