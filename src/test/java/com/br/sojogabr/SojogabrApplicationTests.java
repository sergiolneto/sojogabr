package com.br.sojogabr;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers // Adicionada para ativar o @DynamicPropertySource
@Import(AbstractIntegrationTest.DynamoDbTestConfig.class)
class SojogabrApplicationTests extends AbstractIntegrationTest {

	@Test
	void contextLoads() {
		// Este teste agora iniciará um container LocalStack automaticamente,
		// configurará o Spring para usá-lo e o destruirá no final.
	}

}
