package com.br.sojogabr.domain.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.RestController;

@Setter
@Getter
@RestController
public class LoginRequest {
    private String username;
    private String password;

}
