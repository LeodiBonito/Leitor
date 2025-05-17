package com.example.leitor;

public class Evento {
    private String id;
    private String nome;
    private String dataInicio;
    private String dataTermino;
    private String endereco;
    private String descricao;

    // Construtor vazio (obrigatório para Firebase)
    public Evento() {}

    // Construtor
    public Evento(String nome, String dataInicio, String dataTermino, String endereco, String descricao) {
        this.nome = nome;
        this.dataInicio = dataInicio;
        this.dataTermino = dataTermino;
        this.endereco = endereco;
        this.descricao = descricao;
    }

    // Getters e Setters (obrigatórios)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    // ... (implemente para todos os campos)

    public String getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(String dataInicio) {
        this.dataInicio = dataInicio;
    }

    public String getDataTermino() {
        return dataTermino;
    }

    public void setDataTermino(String dataTermino) {
        this.dataTermino = dataTermino;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}