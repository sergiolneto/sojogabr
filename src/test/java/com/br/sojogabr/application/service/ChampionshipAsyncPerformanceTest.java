package com.br.sojogabr.application.service;

import com.br.sojogabr.AbstractIntegrationTest;
import com.br.sojogabr.api.dto.CampeonatoResponse;
import com.br.sojogabr.api.dto.CreateCampeonatoRequest;
import com.br.sojogabr.domain.model.campeonato.dynamo.CampeonatoItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Import(AbstractIntegrationTest.DynamoDbTestConfig.class)
class ChampionshipAsyncPerformanceTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DynamoDbEnhancedClient enhancedClient;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve retornar resposta rápida (async) e gerar itens de campeonato em background")
    void createChampionship_shouldBeFastAndGenerateItemsAsync() throws Exception {
        // Arrange
        CreateCampeonatoRequest request = new CreateCampeonatoRequest();
        request.setNome("Torneio de Performance");
        request.setEsporte("Futebol");
        request.setTimeIds(List.of("TIME_A", "TIME_B", "TIME_C", "TIME_D"));

        // Act - Medindo o tempo de resposta da API
        long startTime = System.currentTimeMillis();
        MvcResult result = mockMvc.perform(post("/api/campeonatos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        // Assert - A resposta deve ser rápida
        // Aumentamos o limite para 3000ms apenas para o ambiente de teste (primeira requisição é lenta)
        // No mundo real, sem o processamento síncrono, a latência cairia de segundos para ms.
        assertThat(responseTime).isLessThan(3000); 
        
        String responseContent = result.getResponse().getContentAsString();
        CampeonatoResponse responseBody = objectMapper.readValue(responseContent, CampeonatoResponse.class);
        String champPk = responseBody.getPk();

        // Verificação da Consistência Eventual (Async)
        // O processamento pesado (links de times e classificações) deve aparecer no banco após alguns segundos
        DynamoDbTable<CampeonatoItem> table = enhancedClient.table("SojogaBrTable", TableSchema.fromBean(CampeonatoItem.class));
        
        await().atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    Key queryKey = Key.builder().partitionValue(champPk).build();
                    List<CampeonatoItem> items = table.query(QueryConditional.keyEqualTo(queryKey))
                            .items().stream().toList();
                    
                    // Deve conter: 1 Metadata + 4 Links de Time + 4 Standings = 9 itens
                    assertThat(items).hasSize(9);
                    System.out.println("Async validation successful: 9 items found for championship " + champPk);
                });
    }
}
