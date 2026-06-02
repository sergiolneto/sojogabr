package com.br.sojogabr.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class User implements UserDetails {

    public static final String PK = "pk";
    public static final String SK = "sk";
    public static final String USERNAME = "username";
    public static final String STATUS = "status";
    public static final String NOME = "nome";
    public static final String EMAIL = "email";
    public static final String ID = "id";
    public static final String ESPORTES = "esportes";

    // Enums movidos para dentro da classe e tornados públicos para acesso externo
    public enum UserRole {
        USER,
        POWER_USER,
        ADMIN
    }

    public enum UserStatus {
        PENDING_APPROVAL,
        ACTIVE,
        INACTIVE
    }

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

    private UserRole role;
    private UserStatus status;

    @DynamoDbPartitionKey
    public String getPk() {
        return pk;
    }

    @DynamoDbSortKey
    public String getSk() {
        return sk;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "username-index")
    public String getUsername() {
        return username;
    }

    // Adiciona um GSI no campo 'status' para permitir buscas eficientes por status.
    @DynamoDbSecondaryPartitionKey(indexNames = "status-index")
    public UserStatus getStatus() {
        return status;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.role == null) {
            return List.of();
        }
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
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
        return this.status == UserStatus.ACTIVE;
    }

    @Override
    public String getPassword() {
        return password;
    }


}