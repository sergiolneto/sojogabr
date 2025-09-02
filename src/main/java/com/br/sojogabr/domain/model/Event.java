package com.br.sojogabr.domain.model;

import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.time.LocalDateTime;

@Data
@DynamoDbBean
public class Event {

    private String id;
    private String pk; // Ex: EVENT#<eventId>
    private String sk; // Ex: METADATA
    private String title; // Ex: "Futebol de Quinta" ou "Campeonato de Vôlei de Praia"
    private String description; // Detalhes sobre o evento
    private String sport; // Ex: "Futebol", "Vôlei"
    private LocalDateTime eventDate; // Data e hora do evento
    private String location; // Ex: "Quadra do Parque Ibirapuera"
    private String imageUrl; // URL da imagem do post do Instagram
    private EventType eventType; // Para diferenciar JOGO de CAMPEONATO

    @DynamoDbPartitionKey
    public String getPk() {
        return pk;
    }

    @DynamoDbSortKey
    public String getSk() {
        return sk;
    }

    public enum EventType {
        JOGO,
        CAMPEONATO
    }
}
