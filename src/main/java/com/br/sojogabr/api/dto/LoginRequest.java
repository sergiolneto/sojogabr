// src/main/java/com/br/sojogabr/api/dto/LoginRequest.java
package com.br.sojogabr.api.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}