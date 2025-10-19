package com.br.sojogabr.api.dto;

import java.util.List;

public record CriarCampeonatoRequest(String nome, String esporte, List<String> timeIds) {
}
