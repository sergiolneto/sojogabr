package com.br.sojogabr.domain.service;

import com.br.sojogabr.domain.model.campeonato.StatusCampeonato;
import com.br.sojogabr.domain.model.campeonato.dynamo.CampeonatoItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class CampeonatoService {

    private final DynamoDbTable<CampeonatoItem> campeonatoTable;
    private final DynamoDbEnhancedClient enhancedClient;

    @Autowired
    public CampeonatoService(DynamoDbTable<CampeonatoItem> campeonatoTable, DynamoDbEnhancedClient enhancedClient) {
        this.campeonatoTable = campeonatoTable;
        this.enhancedClient = enhancedClient;
    }

    /**
     * Cria um novo campeonato e todos os itens relacionados (links de times, classificação inicial).
     * Usa uma transação para garantir que tudo seja criado ou nada seja.
     */
    public CampeonatoItem criarCampeonato(String nome, String esporte, List<String> timeIds) {
        // Validações (número mínimo de times, etc.)
        if (timeIds.size() < 2) {
            throw new IllegalArgumentException("São necessários pelo menos 2 times.");
        }

        String champId = UUID.randomUUID().toString();
        String champPk = "CHAMP#" + champId;

        // 1. Cria o item principal do campeonato
        CampeonatoItem campeonato = new CampeonatoItem();
        campeonato.setPk(champPk);
        campeonato.setSk("METADATA");
        campeonato.setNome(nome);
        campeonato.setEsporte(esporte);
        campeonato.setDataInicio(LocalDate.now());
        campeonato.setStatus(StatusCampeonato.PLANEJADO);

        // Prepara uma transação para gravar tudo de uma vez
        TransactWriteItemsEnhancedRequest.Builder transaction = TransactWriteItemsEnhancedRequest.builder();
        transaction.addPutItem(campeonatoTable, campeonato);

        // 2. Para cada time, cria o item de link e o item de classificação inicial
        for (String timeId : timeIds) {
            String teamPk = "TEAM#" + timeId;

            // Item de link (CHAMP#... -> TEAM#...)
            CampeonatoItem timeLink = new CampeonatoItem();
            timeLink.setPk(champPk);
            timeLink.setSk(teamPk);
            transaction.addPutItem(campeonatoTable, timeLink);

            // Item de classificação (CHAMP#... -> STANDINGS#...)
            CampeonatoItem classificacao = new CampeonatoItem();
            classificacao.setPk(champPk);
            classificacao.setSk("STANDINGS#" + timeId);
            classificacao.setPontos(0); // Zera as estatísticas
            // ... setar outros campos para 0
            transaction.addPutItem(campeonatoTable, classificacao);
        }

        // 3. Executa a transação
        enhancedClient.transactWriteItems(transaction.build());

        return campeonato;
    }
    // ... outros métodos para gerar partidas, registrar resultados, etc.
}
