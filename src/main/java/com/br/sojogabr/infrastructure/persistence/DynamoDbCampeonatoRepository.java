package com.br.sojogabr.infrastructure.persistence;

import com.br.sojogabr.domain.model.campeonato.StatusCampeonato;
import com.br.sojogabr.domain.model.campeonato.dynamo.CampeonatoItem;
import com.br.sojogabr.domain.port.CampeonatoRepository;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public class DynamoDbCampeonatoRepository implements CampeonatoRepository {

    private final DynamoDbTable<CampeonatoItem> campeonatoTable;
    private final DynamoDbEnhancedClient enhancedClient;

    public DynamoDbCampeonatoRepository(DynamoDbTable<CampeonatoItem> campeonatoTable,
                                        DynamoDbEnhancedClient enhancedClient) {
        this.campeonatoTable = campeonatoTable;
        this.enhancedClient = enhancedClient;
    }

    @Override
    public CampeonatoItem save(String nome, String esporte, List<String> timeIds) {
        String champPk = "CHAMP#" + UUID.randomUUID();
        CampeonatoItem campeonato = createCampeonatoMetadata(champPk, nome, esporte);

        TransactWriteItemsEnhancedRequest.Builder transaction = TransactWriteItemsEnhancedRequest.builder();
        transaction.addPutItem(campeonatoTable, campeonato);

        for (String timeId : timeIds) {
            transaction.addPutItem(campeonatoTable, createTeamLinkItem(champPk, timeId));
            transaction.addPutItem(campeonatoTable, createStandingsItem(champPk, timeId));
        }

        enhancedClient.transactWriteItems(transaction.build());

        return campeonato;
    }

    private CampeonatoItem createCampeonatoMetadata(String pk, String nome, String esporte) {
        CampeonatoItem campeonato = new CampeonatoItem();
        campeonato.setPk(pk);
        campeonato.setSk("METADATA");
        campeonato.setNome(nome);
        campeonato.setEsporte(esporte);
        campeonato.setDataInicio(LocalDate.now());
        campeonato.setStatus(StatusCampeonato.PLANEJADO);
        return campeonato;
    }

    private CampeonatoItem createTeamLinkItem(String pk, String timeId) {
        CampeonatoItem timeLink = new CampeonatoItem();
        timeLink.setPk(pk);
        timeLink.setSk("TEAM#" + timeId);
        return timeLink;
    }

    private CampeonatoItem createStandingsItem(String pk, String timeId) {
        CampeonatoItem classificacao = new CampeonatoItem();
        classificacao.setPk(pk);
        classificacao.setSk("STANDINGS#" + timeId);
        // Inicializa os pontos e outras estatísticas como 0
        classificacao.setPontos(0);
        return classificacao;
    }
}