package com.br.sojogabr.api.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.br.sojogabr.api.dto.CriarCampeonatoRequest;
import com.br.sojogabr.domain.model.campeonato.dynamo.CampeonatoItem;
import com.br.sojogabr.domain.usecase.CriarCampeonatoUseCase;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/campeonatos")
@RequiredArgsConstructor
public class CampeonatoController {

    private final CriarCampeonatoUseCase criarCampeonatoUseCase;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','POWER_USER')")
    public ResponseEntity<CampeonatoItem> criar(@RequestBody CriarCampeonatoRequest request) {
        CampeonatoItem created = criarCampeonatoUseCase.execute(request.nome(), request.esporte(), request.timeIds());
        URI location = URI.create(String.format("/api/campeonatos/%s", created.getPk()));
        return ResponseEntity.created(location).body(created);
    }
}
