package br.com.projetonotificador.model;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class Compromisso {
    private UUID id;
    private String titulo;
    private String descricao;
    private LocalDate data;
    private boolean concluido;
    private TipoRecorrencia recorrencia;
    private LocalDate dataFimRecorrencia;
    private Set<LocalDate> datasConcluidas;

    // Construtor
    public Compromisso(String titulo, String descricao, LocalDate data) {
        this.id = UUID.randomUUID();
        this.titulo = titulo;
        this.descricao = descricao;
        this.data = data;
        this.concluido = false;
        this.recorrencia = TipoRecorrencia.NAO_RECORRENTE;
        this.datasConcluidas = new HashSet<>();
    }

    // Getters
    public UUID getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public LocalDate getData() { return data; }
    public boolean isConcluido() { return concluido; }
    public TipoRecorrencia getRecorrencia() { return recorrencia; }
    public LocalDate getDataFimRecorrencia() { return dataFimRecorrencia; }
    public Set<LocalDate> getDatasConcluidas() { return datasConcluidas; }
    // Setters
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setData(LocalDate data) { this.data = data; }
    public void setConcluido(boolean concluido) { this.concluido = concluido; }
    public void setRecorrencia(TipoRecorrencia recorrencia) { this.recorrencia = recorrencia; }
    public void setDataFimRecorrencia(LocalDate dataFimRecorrencia) { this.dataFimRecorrencia = dataFimRecorrencia; }

    // Métodos para gerenciar instâncias concluídas
    public void adicionarDataConcluida(LocalDate data) {
        if (this.datasConcluidas == null) {
            this.datasConcluidas = new HashSet<>();
        }
        this.datasConcluidas.add(data);
    }

    /**
     * Verifica se um compromisso recorrente teve todas as suas instâncias concluídas.
     * @return true se todas as instâncias foram concluídas, false caso contrário.
     */
    public boolean isTotalmenteConcluido() {
        if (recorrencia == null || recorrencia == TipoRecorrencia.NAO_RECORRENTE) {
            return this.concluido; // Para não recorrentes, usa o campo 'concluido'
        }

        if (datasConcluidas == null || dataFimRecorrencia == null) {
            return false;
        }

        int totalOcorrencias = 0;
        LocalDate dataIteracao = this.data;
        while (!dataIteracao.isAfter(dataFimRecorrencia)) {
            totalOcorrencias++;
            if (recorrencia == TipoRecorrencia.DIARIO) {
                dataIteracao = dataIteracao.plusDays(1);
            } else if (recorrencia == TipoRecorrencia.SEMANAL) {
                dataIteracao = dataIteracao.plusWeeks(1);
            } else if (recorrencia == TipoRecorrencia.MENSAL) {
                dataIteracao = dataIteracao.plusMonths(1);
            } else {
                break; // Tipo de recorrência desconhecido
            }
        }

        return datasConcluidas.size() >= totalOcorrencias;
    }

    @Override
    public String toString() {
        return "Compromisso{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", data=" + data +
                ", recorrencia=" + recorrencia +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Compromisso that = (Compromisso) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

