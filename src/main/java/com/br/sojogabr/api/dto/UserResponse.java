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
    private String nome;
    private String endereco;
    private String celular;
    private String email;
    private String insta;
    private List<String> esportes;
    private String time;
    private String posicao;
    private boolean capitao;



    public static UserResponse fromEntity(User user) {
        UserResponse dto = new UserResponse();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setCelular(user.getCelular());
        dto.setEmail(user.getEmail());
        dto.setNome(user.getNome());
        dto.setEndereco(user.getEndereco());
        dto.setInsta(user.getInsta());
        dto.setEsportes(user.getEsportes());
        dto.setPosicao(user.getPosicao());
        dto.setCapitao(user.isCapitao());
        dto.setTime(user.getTime());
        return dto;
    }
}