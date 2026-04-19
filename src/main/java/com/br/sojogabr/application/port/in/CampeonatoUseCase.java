package com.br.sojogabr.application.port.in;

import com.br.sojogabr.domain.model.campeonato.dynamo.CampeonatoItem;

import java.util.List;

public interface CampeonatoUseCase {
    CampeonatoItem criarCampeonato(String nome, String esporte, List<String> timeIds);
}
