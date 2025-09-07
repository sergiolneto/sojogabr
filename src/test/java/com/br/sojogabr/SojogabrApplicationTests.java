package com.br.sojogabr;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers // Adicionada para ativar o @DynamicPropertySource
@Import(AbstractIntegrationTest.DynamoDbTestConfig.class)
class SojogabrApplicationTests extends AbstractIntegrationTest {

}
