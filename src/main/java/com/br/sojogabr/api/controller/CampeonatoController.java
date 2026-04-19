package com.br.sojogabr.api.controller;

import com.br.sojogabr.api.dto.CampeonatoResponse;
import com.br.sojogabr.api.dto.CreateCampeonatoRequest;
import com.br.sojogabr.application.port.in.CampeonatoUseCase;
import com.br.sojogabr.domain.model.campeonato.dynamo.CampeonatoItem;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/campeonatos")
public class CampeonatoController {

    private final CampeonatoUseCase campeonatoUseCase;

    public CampeonatoController(CampeonatoUseCase campeonatoUseCase) {
        this.campeonatoUseCase = campeonatoUseCase;
    }

    @PostMapping
    public ResponseEntity<CampeonatoResponse> createCampeonato(@Valid @RequestBody CreateCampeonatoRequest request) {
        CampeonatoItem createdCampeonato = campeonatoUseCase.criarCampeonato(
                request.getNome(),
                request.getEsporte(),
                request.getTimeIds()
        );

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(extractIdFromPk(createdCampeonato.getPk()))
                .toUri();

        return ResponseEntity.created(location).body(CampeonatoResponse.fromEntity(createdCampeonato));
    }

    private String extractIdFromPk(String pk) {
        // Assuming pk is "CHAMP#<id>", extract <id>
        return pk.substring(6);
    }
}
