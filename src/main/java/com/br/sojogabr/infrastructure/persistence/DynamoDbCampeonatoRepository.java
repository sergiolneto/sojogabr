package com.br.sojogabr.infrastructure.persistence;

import com.br.sojogabr.domain.model.campeonato.StatusCampeonato;
import com.br.sojogabr.domain.model.campeonato.dynamo.CampeonatoItem;
import com.br.sojogabr.domain.port.CampeonatoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public class DynamoDbCampeonatoRepository implements CampeonatoRepository {

    private final DynamoDbEnhancedClient enhancedClient;
    private DynamoDbTable<CampeonatoItem> campeonatoTable;

    @Value("${aws.dynamodb.tableName}")
    private String tableName;

    @Autowired
    public DynamoDbCampeonatoRepository(DynamoDbEnhancedClient enhancedClient) {
        this.enhancedClient = enhancedClient;
    }

    @PostConstruct
    private void init() {
        this.campeonatoTable = enhancedClient.table(tableName, TableSchema.fromBean(CampeonatoItem.class));
    }

    @Override
    public CampeonatoItem save(String nome, String esporte, List<String> timeIds) {
        String champId = UUID.randomUUID().toString();
        String champPk = "CHAMP#" + champId;

        CampeonatoItem campeonato = new CampeonatoItem();
        campeonato.setPk(champPk);
        campeonato.setSk("METADATA");
        campeonato.setNome(nome);
        campeonato.setEsporte(esporte);
        campeonato.setDataInicio(LocalDate.now());
        campeonato.setStatus(StatusCampeonato.PLANEJADO);

        TransactWriteItemsEnhancedRequest.Builder transaction = TransactWriteItemsEnhancedRequest.builder();
        transaction.addPutItem(campeonatoTable, campeonato);

        for (String timeId : timeIds) {
            CampeonatoItem timeLink = new CampeonatoItem(champPk, "TEAM#" + timeId, null, null, null, null, 0, 0, 0, 0, 0, 0, 0, 0);
            transaction.addPutItem(campeonatoTable, timeLink);

            CampeonatoItem classificacao = new CampeonatoItem(champPk, "STANDINGS#" + timeId, null, null, null, null, 0, 0, 0, 0, 0, 0, 0, 0);
            transaction.addPutItem(campeonatoTable, classificacao);
        }

        enhancedClient.transactWriteItems(transaction.build());

        return campeonato;
    }
}