package com.br.sojogabr;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.Projection;
import software.amazon.awssdk.services.dynamodb.model.ProjectionType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

import java.time.Duration;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.DYNAMODB;

// A anotação @Testcontainers foi removida daqui e será colocada nas classes de teste concretas.
@SuppressWarnings("resource")
public abstract class AbstractIntegrationTest {

    // Padrão Singleton Container: O container é criado e iniciado apenas uma vez.
    protected static final LocalStackContainer localStack;

    static {
        localStack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.2.0"))
                .withServices(DYNAMODB)
                .waitingFor(Wait.forLogMessage(".*Ready.*", 1))
                .withStartupTimeout(Duration.ofMinutes(2));
        localStack.start();
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.aws.dynamodb.endpoint", () -> localStack.getEndpointOverride(DYNAMODB).toString());
        registry.add("spring.cloud.aws.credentials.access-key", localStack::getAccessKey);
        registry.add("spring.cloud.aws.credentials.secret-key", localStack::getSecretKey);
        registry.add("spring.cloud.aws.region.static", localStack::getRegion);
    }

    @TestConfiguration
    public static class DynamoDbTestConfig {

        @Bean
        public DynamoDbClient dynamoDbClient() {
            DynamoDbClient client = DynamoDbClient.builder()
                    .endpointOverride(localStack.getEndpointOverride(DYNAMODB))
                    .credentialsProvider(
                            StaticCredentialsProvider.create(
                                    AwsBasicCredentials.create(localStack.getAccessKey(), localStack.getSecretKey())
                            )
                    )
                    .region(Region.of(localStack.getRegion()))
                    .build();

            // Cria as tabelas necessárias para os testes de integração
            createTableIfAbsent(client, createUserTableRequest());
            createTableIfAbsent(client, createCampeonatoTableRequest());

            System.out.println("Tabelas 'Usuario' e 'SojogaBrTable' criadas para os testes.");

            return client;
        }

        private void createTableIfAbsent(DynamoDbClient client, CreateTableRequest request) {
            try {
                client.createTable(request);
            } catch (ResourceInUseException e) {
                System.out.println("Tabela '" + request.tableName() + "' ja existe para os testes.");
            }
        }

        @Bean
        public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
            return DynamoDbEnhancedClient.builder()
                    .dynamoDbClient(dynamoDbClient)
                    .build();
        }

        private CreateTableRequest createUserTableRequest() {
            return CreateTableRequest.builder()
                    .tableName("Usuario")
                    .keySchema(
                            KeySchemaElement.builder().attributeName("pk").keyType(KeyType.HASH).build(),
                            KeySchemaElement.builder().attributeName("sk").keyType(KeyType.RANGE).build()
                    )
                    .attributeDefinitions(
                            AttributeDefinition.builder().attributeName("pk").attributeType(ScalarAttributeType.S).build(),
                            AttributeDefinition.builder().attributeName("sk").attributeType(ScalarAttributeType.S).build(),
                            AttributeDefinition.builder().attributeName("username").attributeType(ScalarAttributeType.S).build(),
                            AttributeDefinition.builder().attributeName("status").attributeType(ScalarAttributeType.S).build()
                    )
                    .globalSecondaryIndexes(
                            GlobalSecondaryIndex.builder()
                                    .indexName("username-index")
                                    .keySchema(KeySchemaElement.builder().attributeName("username").keyType(KeyType.HASH).build())
                                    .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
                                    .provisionedThroughput(ProvisionedThroughput.builder().readCapacityUnits(5L).writeCapacityUnits(5L).build())
                                    .build(),
                            GlobalSecondaryIndex.builder()
                                    .indexName("status-index")
                                    .keySchema(KeySchemaElement.builder().attributeName("status").keyType(KeyType.HASH).build())
                                    .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
                                    .provisionedThroughput(ProvisionedThroughput.builder().readCapacityUnits(5L).writeCapacityUnits(5L).build())
                                    .build()
                    )
                    .provisionedThroughput(ProvisionedThroughput.builder().readCapacityUnits(5L).writeCapacityUnits(5L).build())
                    .build();
        }

        /**
         * Cria a definição da tabela 'SojogaBrTable' para o ambiente de testes.
         * IMPORTANTE: Verifique se os nomes dos atributos 'pk' e 'sk'
         * correspondem à sua entidade de Campeonato (CampeonatoItem).
         */
        private CreateTableRequest createCampeonatoTableRequest() {
            return CreateTableRequest.builder()
                    .tableName("SojogaBrTable")
                    .keySchema(
                            KeySchemaElement.builder().attributeName("pk").keyType(KeyType.HASH).build(), // Chave de Partição (PK)
                            KeySchemaElement.builder().attributeName("sk").keyType(KeyType.RANGE).build()      // Chave de Ordenação (SK)
                    )
                    .attributeDefinitions(
                            AttributeDefinition.builder().attributeName("pk").attributeType(ScalarAttributeType.S).build(),
                            AttributeDefinition.builder().attributeName("sk").attributeType(ScalarAttributeType.S).build()
                    )
                    .provisionedThroughput(ProvisionedThroughput.builder().readCapacityUnits(5L).writeCapacityUnits(5L).build())
                    .build();
        }
    }
}
