package com.br.sojogabr.domain.model.campeonato.dynamo;

import com.br.sojogabr.domain.model.campeonato.StatusPartida;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.time.LocalDateTime;

/**
 * Representa 2 tipos de itens na tabela DynamoDB:
 * 1. A Partida em si (PK = CHAMP#<champId>, SK = MATCH#<matchId>)
 * 2. Um Evento da Partida (PK = MATCH#<matchId>, SK = EVENT#<eventId>)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class PartidaItem {

    @Getter(onMethod_ = @DynamoDbPartitionKey)
    private String pk;

    @Getter(onMethod_ = @DynamoDbSortKey)
    private String sk;

    // Atributos para SK=MATCH#...
    private String timeCasaId; // Formato: TEAM#<teamId>
    private String timeVisitanteId; // Formato: TEAM#<teamId>
    private LocalDateTime dataHora;
    private Integer placarCasa;
    private Integer placarVisitante;
    private StatusPartida status;

}