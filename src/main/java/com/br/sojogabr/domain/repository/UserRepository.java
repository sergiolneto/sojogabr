package com.br.sojogabr.domain.repository;

import com.br.sojogabr.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class UserRepository {

    private final DynamoDbTable<User> userTable;
    private final DynamoDbIndex<User> usernameIndex;
    private final DynamoDbIndex<User> statusIndex; // Índice para o campo status
    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);

    public UserRepository(DynamoDbTable<User> userTable) {
        this.userTable = userTable;
        this.usernameIndex = this.userTable.index("username-index");
        // Inicializa o índice para o status
        this.statusIndex = this.userTable.index("status-index");
    }

    public User save(User user) {
        logger.info("Executando operação PutItem no DynamoDB para o usuário: '{}'", user.getUsername());
        userTable.putItem(user);
        return user;
    }

    public Optional<User> findById(String pk) {
        Key key = Key.builder()
                .partitionValue(pk)
                .sortValue("METADATA")
                .build();
        return Optional.ofNullable(userTable.getItem(key));
    }

    public Optional<User> findByUsername(String username){
        QueryConditional queryConditional =  QueryConditional.keyEqualTo(K -> K.partitionValue(username));
        return this.usernameIndex.query(queryConditional).stream()
                .flatMap(page -> page.items().stream())
                .findFirst();
    }

    /**
     * Encontra todos os usuários com um determinado status usando o GSI 'status-index'.
     */
    public List<User> findByStatus(User.UserStatus status) {
        Page<User> firstPage = findByStatusPaginated(status, 100, null);
        return firstPage.items();
    }

    /**
     * Busca paginada por status.
     */
    public Page<User> findByStatusPaginated(User.UserStatus status, Integer limit, Map<String, AttributeValue> exclusiveStartKey) {
        QueryConditional queryConditional = QueryConditional.keyEqualTo(k -> k.partitionValue(status.name()));
        
        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .limit(limit)
                .exclusiveStartKey(exclusiveStartKey)
                .build();

        return this.statusIndex.query(request).iterator().next();
    }
}
