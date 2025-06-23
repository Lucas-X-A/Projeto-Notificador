package br.com.projetonotificador.model;

import java.time.LocalDate;

/**
 * Representa uma única ocorrência (instância) de um compromisso.
 * Se o compromisso não for recorrente, a data da instância será a mesma data do compromisso.
 */
public class CompromissoInstancia {
    private final Compromisso compromissoPai;
    private final LocalDate dataDaInstancia;

    public CompromissoInstancia(Compromisso compromissoPai, LocalDate dataDaInstancia) {
        this.compromissoPai = compromissoPai;
        this.dataDaInstancia = dataDaInstancia;
    }

    public Compromisso getCompromissoPai() {
        return compromissoPai;
    }

    public LocalDate getDataDaInstancia() {
        return dataDaInstancia;
    }

    // Métodos "delegados" para facilitar o acesso aos dados na interface
    public String getTitulo() { return compromissoPai.getTitulo(); }
    public String getDescricao() { return compromissoPai.getDescricao(); }
}