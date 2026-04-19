package com.br.sojogabr.api.dto;

import com.br.sojogabr.domain.model.campeonato.dynamo.CampeonatoItem;

import java.time.LocalDate;

public class CampeonatoResponse {

    private String pk;
    private String sk;
    private String nome;
    private String esporte;
    private LocalDate dataInicio;
    private String status;

    public static CampeonatoResponse fromEntity(CampeonatoItem item) {
        CampeonatoResponse response = new CampeonatoResponse();
        response.setPk(item.getPk());
        response.setSk(item.getSk());
        response.setNome(item.getNome());
        response.setEsporte(item.getEsporte());
        response.setDataInicio(item.getDataInicio());
        response.setStatus(item.getStatus().toString());
        return response;
    }

    // Getters and setters
    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    public String getSk() {
        return sk;
    }

    public void setSk(String sk) {
        this.sk = sk;
    }

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

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
