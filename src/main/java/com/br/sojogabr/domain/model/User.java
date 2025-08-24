package com.br.sojogabr.domain.model;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class User {

    private String id;
    private String nome;
    private String endereco;
    private String celular;
    private String instagram;
    private List<String> esportes = new ArrayList<>();
    private String posicao;
    private boolean capitao;
    private String username;
    private String password;


    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }
}