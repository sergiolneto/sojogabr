package com.br.sojogabr.domain.usecase;

import com.br.sojogabr.domain.model.campeonato.dynamo.CampeonatoItem;
import com.br.sojogabr.domain.port.CampeonatoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CriarCampeonatoUseCase {

    private final CampeonatoRepository campeonatoRepository;

    @Autowired
    public CriarCampeonatoUseCase(CampeonatoRepository campeonatoRepository) {
        this.campeonatoRepository = campeonatoRepository;
    }

    /**
     * Caso de uso para criar um novo campeonato.
     */
    public CampeonatoItem execute(String nome, String esporte, List<String> timeIds) {
        // Validações (número mínimo de times, etc.)
        if (timeIds.size() < 2) {
            throw new IllegalArgumentException("São necessários pelo menos 2 times.");
        }

        // Delega a persistência para a implementação do repositório
        return campeonatoRepository.save(nome, esporte, timeIds);
    }
}
