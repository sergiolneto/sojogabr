package com.br.sojogabr.domain.model;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

import java.util.ArrayList;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class User {

    private String id;
    private String nome;
    private String email;
    private String endereco;
    private String celular;
    private String insta;
    private List<String> esportes = new ArrayList<>();
    private String posicao;
    private boolean capitao;
    private String username;
    private String password;


    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "username-index")
    public String getUsername() {
        return username;
    }
}