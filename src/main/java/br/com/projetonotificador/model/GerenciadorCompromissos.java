package br.com.projetonotificador.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

public class GerenciadorCompromissos {
    private static final String ARQUIVO_ATIVOS = "compromissos.json";
    private static final String ARQUIVO_CONCLUIDOS = "compromissos_concluidos.json";
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .setPrettyPrinting()
            .create();

    private List<Compromisso> carregarLista(String nomeArquivo) {
        Path caminhoDoArquivo = Paths.get(nomeArquivo);
        try {
            if (!Files.exists(caminhoDoArquivo) || Files.size(caminhoDoArquivo) == 0) {
                return new ArrayList<>();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        try (FileReader reader = new FileReader(caminhoDoArquivo.toFile())) {
            Type tipoLista = new TypeToken<ArrayList<Compromisso>>() {}.getType();
            List<Compromisso> compromissos = gson.fromJson(reader, tipoLista);
            return compromissos == null ? new ArrayList<>() : compromissos;
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void salvarLista(List<Compromisso> compromissos, String nomeArquivo) {
        try (FileWriter writer = new FileWriter(nomeArquivo)) {
            gson.toJson(compromissos, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Compromisso> carregarCompromissos() {
        return carregarLista(ARQUIVO_ATIVOS);
    }

    public void salvarCompromissos(List<Compromisso> compromissos) {
        salvarLista(compromissos, ARQUIVO_ATIVOS);
    }

    public List<Compromisso> carregarCompromissosConcluidos() {
        return carregarLista(ARQUIVO_CONCLUIDOS);
    }

    public void salvarCompromissosConcluidos(List<Compromisso> compromissos) {
        salvarLista(compromissos, ARQUIVO_CONCLUIDOS);
    }

    public void concluirCompromisso(Compromisso compromissoParaConcluir) {
        List<Compromisso> ativos = carregarCompromissos();
        List<Compromisso> concluidos = carregarCompromissosConcluidos();

        // Remove o compromisso da lista de ativos procurando pelo ID único.
        boolean removido = ativos.removeIf(c -> c.getId().equals(compromissoParaConcluir.getId()));

        if (removido) {
            compromissoParaConcluir.setConcluido(true);
            concluidos.add(compromissoParaConcluir);
            salvarCompromissos(ativos);
            salvarCompromissosConcluidos(concluidos);
        }
    }

    public void reativarCompromisso(Compromisso compromissoParaReativar) {
        List<Compromisso> ativos = carregarCompromissos();
        List<Compromisso> concluidos = carregarCompromissosConcluidos();

        // Remove o compromisso da lista de concluídos procurando pelo ID único.
        boolean removido = concluidos.removeIf(c -> c.getId().equals(compromissoParaReativar.getId()));

        if (removido) {
            compromissoParaReativar.setConcluido(false);
            ativos.add(compromissoParaReativar);
            salvarCompromissos(ativos);
            salvarCompromissosConcluidos(concluidos);
        }
    }
}