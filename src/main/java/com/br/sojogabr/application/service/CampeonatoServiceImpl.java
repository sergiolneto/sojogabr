package com.br.sojogabr.application.service;

import com.br.sojogabr.application.port.in.CampeonatoUseCase;
import com.br.sojogabr.domain.model.campeonato.dynamo.CampeonatoItem;
import com.br.sojogabr.domain.usecase.CriarCampeonatoUseCase;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CampeonatoServiceImpl implements CampeonatoUseCase {

    private final CriarCampeonatoUseCase criarCampeonatoUseCase;

    public CampeonatoServiceImpl(CriarCampeonatoUseCase criarCampeonatoUseCase) {
        this.criarCampeonatoUseCase = criarCampeonatoUseCase;
    }

    @Override
    public CampeonatoItem criarCampeonato(String nome, String esporte, List<String> timeIds) {
        return criarCampeonatoUseCase.execute(nome, esporte, timeIds);
    }
}
