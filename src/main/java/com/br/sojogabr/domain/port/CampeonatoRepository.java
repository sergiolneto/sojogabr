package com.br.sojogabr.domain.port;

import com.br.sojogabr.domain.model.campeonato.dynamo.CampeonatoItem;

import java.util.List;

/**
 * Interface (Port) que define as operações de persistência para Campeonatos,
 * seguindo a Onion Architecture. A camada de Aplicação depende desta interface.
 */
public interface CampeonatoRepository {
    CampeonatoItem save(String nome, String esporte, List<String> timeIds);
}