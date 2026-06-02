package com.br.sojogabr.domain.service;

import com.br.sojogabr.domain.event.ChampionshipCreatedEvent;
import com.br.sojogabr.domain.model.campeonato.StatusCampeonato;
import com.br.sojogabr.domain.model.campeonato.dynamo.CampeonatoItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class CampeonatoService {

    private final DynamoDbTable<CampeonatoItem> campeonatoTable;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public CampeonatoService(DynamoDbTable<CampeonatoItem> campeonatoTable, ApplicationEventPublisher eventPublisher) {
        this.campeonatoTable = campeonatoTable;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Cria um novo campeonato (apenas metadados).
     * O restante da geração (links de times, classificações) ocorre de forma assíncrona.
     */
    public CampeonatoItem criarCampeonato(String nome, String esporte, List<String> timeIds) {
        if (timeIds.size() < 2) {
            throw new IllegalArgumentException("São necessários pelo menos 2 times.");
        }

        String champId = UUID.randomUUID().toString();
        String champPk = "CHAMP#" + champId;

        CampeonatoItem campeonato = new CampeonatoItem();
        campeonato.setPk(champPk);
        campeonato.setSk("METADATA");
        campeonato.setNome(nome);
        campeonato.setEsporte(esporte);
        campeonato.setDataInicio(LocalDate.now());
        campeonato.setStatus(StatusCampeonato.PLANEJADO);

        // Salva apenas o item principal síncronamente
        campeonatoTable.putItem(campeonato);

        // Dispara o evento para processamento pesado em background
        eventPublisher.publishEvent(new ChampionshipCreatedEvent(champPk, nome, esporte, timeIds));

        return campeonato;
    }
}
