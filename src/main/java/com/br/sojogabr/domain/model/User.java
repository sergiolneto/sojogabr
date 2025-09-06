package com.br.sojogabr.domain.model;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class User implements UserDetails {

    private String id;
    // Os setters de pk e sk são necessários para o DynamoDbEnhancedClient
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

    /**
     * Retorna as autorizações concedidas ao usuário.
     * Para este exemplo, retornamos uma lista vazia. Em um cenário real,
     * você poderia mapear roles (ex: "ROLE_ADMIN", "ROLE_USER") aqui.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}