package com.br.sojogabr.domain.repository;

import com.br.sojogabr.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.Optional;

@Repository
public class UserRepository {

    private final DynamoDbTable<User> userTable;
    private final DynamoDbIndex<User> usernameIndex;
    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);

    /**
     * O Spring injeta o bean DynamoDbTable<User> que foi configurado em DynamoDbConfig.
     * Isso centraliza a configuração do nome da tabela.
     */
    public UserRepository(DynamoDbTable<User> userTable) {
        this.userTable = userTable;
        this.usernameIndex = this.userTable.index("username-index");
    }

    public User save(User user) {
        logger.info("Executando operação PutItem no DynamoDB para o usuário: '{}'", user.getUsername());
        userTable.putItem(user);
        return user;
    }

    public Optional<User> findById(String pk) {
        // A chave primária do usuário é composta pela PK (id) e a SK (fixa como "METADATA")
        Key key = Key.builder()
                .partitionValue(pk)
                .sortValue("METADATA") // Adiciona a chave de ordenação que estava faltando
                .build();
        return Optional.ofNullable(userTable.getItem(key));
    }

    public Optional<User> findByUsername(String username){
        QueryConditional queryConditional =  QueryConditional.keyEqualTo(K -> K.partitionValue(username));
        // A mesma lógica de paginação se aplica aqui.
        return this.usernameIndex.query(queryConditional).stream()
                .flatMap(page -> page.items().stream())
                .findFirst();
    }
}
