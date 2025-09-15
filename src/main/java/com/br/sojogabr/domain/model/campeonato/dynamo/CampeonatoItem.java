package com.br.sojogabr.domain.model.campeonato.dynamo;

import com.br.sojogabr.domain.model.campeonato.StatusCampeonato;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.time.LocalDate;

/**
 * Representa 3 tipos de itens na tabela DynamoDB:
 * 1. Metadados do Campeonato (SK = METADATA)
 * 2. Link para um Time (SK = TEAM#<teamId>)
 * 3. Classificação de um Time (SK = STANDINGS#<teamId>)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class CampeonatoItem {

    @Getter(onMethod_ = @DynamoDbPartitionKey)
    private String pk; // Formato: CHAMP#<champId>

    @Getter(onMethod_ = @DynamoDbSortKey)
    private String sk; // Formato: METADATA, TEAM#<teamId>, ou STANDINGS#<teamId>

    // Atributos para SK=METADATA
    private String nome;
    private String esporte;
    private LocalDate dataInicio;
    private StatusCampeonato status;

    // Atributos para SK=STANDINGS#<teamId>
    private int pontos, jogos, vitorias, empates, derrotas, golsPro, golsContra, saldoGols;
}