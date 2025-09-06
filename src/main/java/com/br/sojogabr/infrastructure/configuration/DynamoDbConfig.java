package com.br.sojogabr.infrastructure.configuration;

import com.br.sojogabr.domain.model.User;
import com.br.sojogabr.domain.model.campeonato.dynamo.CampeonatoItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Configuration
public class DynamoDbConfig {

    @Bean
    public DynamoDbTable<User> userTable(DynamoDbEnhancedClient enhancedClient,
                                         @Value("${aws.dynamodb.user-tableName}") String tableName) {
        return enhancedClient.table(tableName, TableSchema.fromBean(User.class));
    }

    @Bean
    public DynamoDbTable<CampeonatoItem> campeonatoTable(DynamoDbEnhancedClient enhancedClient,
                                                         @Value("${aws.dynamodb.campeonato-tableName}") String tableName) {
        return enhancedClient.table(tableName, TableSchema.fromBean(CampeonatoItem.class));
    }

}