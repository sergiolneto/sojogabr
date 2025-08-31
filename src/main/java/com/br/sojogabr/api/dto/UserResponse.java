package com.br.sojogabr.api.dto;

import com.br.sojogabr.domain.model.User;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserResponse {
    private String id;
    private String username;
    private String email;
    private String insta;
    private List<String> esportes;

    public static UserResponse fromEntity(User user) {
        UserResponse dto = new UserResponse();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setInsta(user.getInsta());
        dto.setEsportes(user.getEsportes());
        return dto;
    }
}