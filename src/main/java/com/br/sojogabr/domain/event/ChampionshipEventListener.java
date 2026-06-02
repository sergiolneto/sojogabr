package com.br.sojogabr.domain.event;

import com.br.sojogabr.domain.model.campeonato.dynamo.CampeonatoItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChampionshipEventListener {

    private final DynamoDbTable<CampeonatoItem> campeonatoTable;
    private final DynamoDbEnhancedClient enhancedClient;

    @Async
    @EventListener
    public void handleChampionshipCreated(ChampionshipCreatedEvent event) {
        log.info("Processing async generation for championship: {}", event.champPk());

        try {
            TransactWriteItemsEnhancedRequest.Builder transaction = TransactWriteItemsEnhancedRequest.builder();

            for (String timeId : event.timeIds()) {
                String teamPk = "TEAM#" + timeId;

                // Item de link (CHAMP#... -> TEAM#...)
                CampeonatoItem timeLink = new CampeonatoItem();
                timeLink.setPk(event.champPk());
                timeLink.setSk(teamPk);
                transaction.addPutItem(campeonatoTable, timeLink);

                // Item de classificação (CHAMP#... -> STANDINGS#...)
                CampeonatoItem classificacao = new CampeonatoItem();
                classificacao.setPk(event.champPk());
                classificacao.setSk("STANDINGS#" + timeId);
                classificacao.setPontos(0);
                transaction.addPutItem(campeonatoTable, classificacao);
            }

            enhancedClient.transactWriteItems(transaction.build());
            log.info("Async generation completed for championship: {}", event.champPk());
        } catch (Exception e) {
            log.error("Error in async generation for championship: {}", event.champPk(), e);
        }
    }
}
