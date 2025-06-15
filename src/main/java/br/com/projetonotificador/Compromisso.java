package br.com.projetonotificador;

import java.time.LocalDate;

public class Compromisso {
    private String titulo;
    private String descricao;
    private LocalDate data;
    private boolean concluido;

    // Construtor
    public Compromisso(String titulo, String descricao, LocalDate data) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.data = data;
        this.concluido = false; // Todo novo compromisso começa como não concluído
    }

    // Getters e Setters
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public LocalDate getData() { return data; }
    public boolean isConcluido() { return concluido; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setData(LocalDate data) { this.data = data; }
    public void setConcluido(boolean concluido) { this.concluido = concluido; }

    @Override
    public String toString() {
        // Usado para exibição simples em listas
        return data.toString() + " - " + titulo + " : " + descricao + (concluido ? " (Concluído)" : "");
    }
}

