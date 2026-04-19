package com.br.sojogabr.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CreateCampeonatoRequest {

    @NotBlank(message = "Nome do campeonato é obrigatório")
    private String nome;

    @NotBlank(message = "Esporte é obrigatório")
    private String esporte;

    @NotEmpty(message = "Deve haver pelo menos 2 times")
    @Size(min = 2, message = "Deve haver pelo menos 2 times")
    private List<String> timeIds;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEsporte() {
        return esporte;
    }

    public void setEsporte(String esporte) {
        this.esporte = esporte;
    }

    public List<String> getTimeIds() {
        return timeIds;
    }

    public void setTimeIds(List<String> timeIds) {
        this.timeIds = timeIds;
    }
}
