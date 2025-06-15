package br.com.projetonotificador;

import java.time.LocalDate;

public class Compromisso {
    private String descricao;
    private LocalDate data;
    private boolean concluido;

    // Construtor
    public Compromisso(String descricao, LocalDate data) {
        this.descricao = descricao;
        this.data = data;
        this.concluido = false; // Todo novo compromisso começa como não concluído
    }

    // Getters e Setters
    public String getDescricao() { return descricao; }
    public LocalDate getData() { return data; }
    public boolean isConcluido() { return concluido; }
    public void setConcluido(boolean concluido) { this.concluido = concluido; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setData(LocalDate data) { this.data = data; }

    @Override
    public String toString() {
        // Usado para exibição simples em listas
        return data.toString() + " - " + descricao + (concluido ? " (Concluído)" : "");
    }
}

