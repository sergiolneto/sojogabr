package com.br.sojogabr.domain.repository;

import com.br.sojogabr.domain.model.User;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.DeleteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class UserRepository {

    private final DynamoDbTable<User> userTable;
    private final DynamoDbEnhancedClient enhancedClient;

    /**
     * O Spring injeta o DynamoDbEnhancedClient automaticamente.
     * Usamos ele para obter uma referência à nossa tabela "Usuario".
     */
    public UserRepository(DynamoDbEnhancedClient enhancedClient) {
        this.enhancedClient = enhancedClient;
        this.userTable = enhancedClient.table("Usuario", TableSchema.fromBean(User.class));
    }

    public User save(User user) {
        userTable.putItem(user);
        return user;
    }

    public Optional<User> findById(String id) {
        Key key = Key.builder().partitionValue(id).build();
        return Optional.ofNullable(userTable.getItem(key));
    }

    public List<User> findAll() {
        // Cuidado: a operação scan() lê a tabela inteira, pode ser custosa em produção.
        return userTable.scan().items().stream().collect(Collectors.toList());
    }

    public void deleteById(String id) {
        Key key = Key.builder().partitionValue(id).build();
        userTable.deleteItem(key);
    }

public void deleteAll() {
    WriteBatch.Builder<User> writeBatchBuilder = WriteBatch.builder(User.class).mappedTableResource(userTable);
    findAll().forEach(user -> {
        Key key = Key.builder().partitionValue(user.getId()).build();
        writeBatchBuilder.addDeleteItem(DeleteItemEnhancedRequest.builder().key(key).build());
    });

    enhancedClient.batchWriteItem(BatchWriteItemEnhancedRequest.builder().writeBatches(writeBatchBuilder.build()).build());
}
}
