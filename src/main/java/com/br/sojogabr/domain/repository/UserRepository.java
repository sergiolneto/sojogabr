package com.br.sojogabr.domain.repository;

import com.br.sojogabr.domain.model.User;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class UserRepository {

    private final DynamoDbTable<User> userTable;
    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbIndex<User> usernameIndex;

    /**
     * O Spring injeta o DynamoDbEnhancedClient automaticamente.
     * Usamos ele para obter uma referência à nossa tabela "Usuario".
     */
    public UserRepository(DynamoDbEnhancedClient enhancedClient) {
        this.enhancedClient = enhancedClient;
        this.userTable = enhancedClient.table("Usuario", TableSchema.fromBean(User.class));
        this.usernameIndex = this.userTable.index("username-index");
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
        // A correção é fazer o stream das páginas e então "achatar" os itens de cada página em um único stream.
        return userTable.scan().stream()
                .flatMap(page -> page.items().stream())
                .collect(Collectors.toList());
    }

    public void deleteById(String id) {
        Key key = Key.builder().partitionValue(id).build();
        userTable.deleteItem(key);
    }

    public void deleteAll() {
        List<User> usersToDelete = findAll();
        if (usersToDelete.isEmpty()) {
            return;
        }
        final int BATCH_SIZE = 25;
        for (int i = 0; i < usersToDelete.size(); i += BATCH_SIZE) {
            List<User> batch = usersToDelete.subList(i, Math.min(i + BATCH_SIZE, usersToDelete.size()));

            WriteBatch.Builder<User> writeBatchBuilder = WriteBatch.builder(User.class)
                    .mappedTableResource(this.userTable);

            batch.forEach(user -> {
                Key key = Key.builder().partitionValue(user.getId()).build();
                writeBatchBuilder.addDeleteItem(r -> r.key(key));
            });

            // Execute the batch write operation for the current chunk.
            enhancedClient.batchWriteItem(r -> r.addWriteBatch(writeBatchBuilder.build()));
        }
        }

    public Optional<User> findByUsername(String username){
        QueryConditional queryConditional =  QueryConditional.keyEqualTo(K -> K.partitionValue(username));
        // A mesma lógica de paginação se aplica aqui.
        return this.usernameIndex.query(queryConditional).stream()
                .flatMap(page -> page.items().stream())
                .findFirst();
    }
}
