package com.gabriel.entities;

import java.util.Date;

public class DadosEvasao {
    private Integer lote;
    private Integer praca;
    private Integer sentido; // norte, sul, leste oeste
    private Date dataEvasao;
    private Integer horas;
    private Integer categoria; // veiculo + eixos
    private Integer tipoCampo; // isenção, valido, evasao
    private Integer quantidade;
    private Double valor;

    public DadosEvasao() {
    }

    public DadosEvasao(Integer lote, Integer praca, Integer sentido, Date dataEvasao, Integer horas, Integer categoria, Integer tipoCampo, Integer quantidade, Double valor) {
        this.lote = lote;
        this.praca = praca;
        this.sentido = sentido;
        this.dataEvasao = dataEvasao;
        this.horas = horas;
        this.categoria = categoria;
        this.tipoCampo = tipoCampo;
        this.quantidade = quantidade;
        this.valor = valor;
    }

    public Integer getLote() {
        return lote;
    }

    public void setLote(Integer lote) {
        this.lote = lote;
    }

    public Integer getPraca() {
        return praca;
    }

    public void setPraca(Integer praca) {
        this.praca = praca;
    }

    public Integer getSentido() {
        return sentido;
    }

    public void setSentido(Integer sentido) {
        this.sentido = sentido;
    }

    public Date getDataEvasao() {
        return dataEvasao;
    }

    public void setDataEvasao(Date dataEvasao) {
        this.dataEvasao = dataEvasao;
    }

    public Integer getHoras() {
        return horas;
    }

    public void setHoras(Integer horas) {
        this.horas = horas;
    }

    public Integer getCategoria() {
        return categoria;
    }

    public void setCategoria(Integer categoria) {
        this.categoria = categoria;
    }

    public Integer getTipoCampo() {
        return tipoCampo;
    }

    public void setTipoCampo(Integer tipoCampo) {
        this.tipoCampo = tipoCampo;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }
}
