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

import java.time.Duration;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.DYNAMODB;

// A anotação @Testcontainers foi removida daqui e será colocada nas classes de teste concretas.
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
            return DynamoDbClient.builder()
                    .endpointOverride(localStack.getEndpointOverride(DYNAMODB))
                    .credentialsProvider(
                            StaticCredentialsProvider.create(
                                    AwsBasicCredentials.create(localStack.getAccessKey(), localStack.getSecretKey())
                            )
                    )
                    .region(Region.of(localStack.getRegion()))
                    .build();
        }

        @Bean
        public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
            return DynamoDbEnhancedClient.builder()
                    .dynamoDbClient(dynamoDbClient)
                    .build();
        }
    }
}
