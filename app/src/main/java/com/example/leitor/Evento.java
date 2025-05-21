package com.example.leitor;

public class Evento {
    private String id;
    private String nome;
    private String dataInicio;
    private String dataTermino;
    private String endereco;
    private String descricao;
    private String qrCodeBase64;

    // Construtor vazio obrigatório para o Firebase
    public Evento() {}

    public Evento(String id, String nome, String dataInicio, String dataTermino, String endereco, String descricao, String qrCodeBase64) {
        this.id = id;
        this.nome = nome;
        this.dataInicio = dataInicio;
        this.dataTermino = dataTermino;
        this.endereco = endereco;
        this.descricao = descricao;
        this.qrCodeBase64 = qrCodeBase64;
    }




    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDataInicio() { return dataInicio; }
    public void setDataInicio(String dataInicio) { this.dataInicio = dataInicio; }
    public String getDataTermino() { return dataTermino; }
    public void setDataTermino(String dataTermino) { this.dataTermino = dataTermino; }
    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getQrCodeBase64() { return qrCodeBase64; }
    public void setQrCodeBase64(String qrCodeBase64) { this.qrCodeBase64 = qrCodeBase64; }
}