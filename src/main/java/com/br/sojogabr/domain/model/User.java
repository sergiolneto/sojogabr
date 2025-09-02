package com.br.sojogabr.domain.model;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class User {

    private String id;
    private String pk; // Partition Key (e.g., USER#<username>)
    private String sk; // Sort Key (e.g., METADATA)

    private String nome;
    private String email;
    private String endereco;
    private String celular;
    private String insta;
    private List<String> esportes = new ArrayList<>();
    private String time;
    private String posicao;
    private boolean capitao;
    private String username;
    private String password;

    @DynamoDbPartitionKey
    public String getPk() {
        return pk;
    }

    @DynamoDbSortKey
    public String getSk() {
        return sk;
    }

    // O GSI (Global Secondary Index) permite buscar um usuário pelo username
    // sem saber o PK completo, se necessário.
    @DynamoDbSecondaryPartitionKey(indexNames = "username-index")
    public String getUsername() {
        return username;
    }

    // Os setters de pk e sk são necessários para o DynamoDbEnhancedClient
    public void setPk(String pk) { this.pk = pk; }
    public void setSk(String sk) { this.sk = sk; }
}