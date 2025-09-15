package br.com.projetonotificador.model;

public enum TipoRecorrencia {
    NAO_RECORRENTE("Não Recorrente"),
    DIARIO("Diário"),
    SEMANAL("Semanal"),
    MENSAL("Mensal");

    private final String displayName;

    TipoRecorrencia(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}