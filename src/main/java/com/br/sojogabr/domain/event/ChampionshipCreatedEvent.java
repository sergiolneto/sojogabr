package com.br.sojogabr.domain.event;

import java.util.List;

public record ChampionshipCreatedEvent(String champPk, String nome, String esporte, List<String> timeIds) {
}
