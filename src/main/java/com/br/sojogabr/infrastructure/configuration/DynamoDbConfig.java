package com.br.sojogabr.infrastructure.configuration;

import com.br.sojogabr.domain.model.User;
import com.br.sojogabr.domain.model.campeonato.dynamo.CampeonatoItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

/**
 * Cria os beans DynamoDbTable necessários para o mapeamento das entidades.
 * Sem esses beans, o contêiner de DI do Spring não consegue satisfazer as
 * injeções por construtor presentes em UserRepository, DynamoDbCampeonatoRepository,
 * DynamoDbInitializer, CampeonatoService e ChampionshipEventListener.
 */
@Configuration
public class DynamoDbConfig {

    @Value("${aws.dynamodb.user-tableName:Usuario}")
    private String userTableName;

    @Value("${aws.dynamodb.campeonato-tableName:SojogaBrTable}")
    private String campeonatoTableName;

    @Bean
    public DynamoDbTable<User> userTable(DynamoDbEnhancedClient enhancedClient) {
        return enhancedClient.table(userTableName, TableSchema.fromBean(User.class));
    }

    @Bean
    public DynamoDbTable<CampeonatoItem> campeonatoTable(DynamoDbEnhancedClient enhancedClient) {
        return enhancedClient.table(campeonatoTableName, TableSchema.fromBean(CampeonatoItem.class));
    }
}
