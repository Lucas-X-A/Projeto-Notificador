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
    private static final String ARQUIVO_DB = "compromissos.json";
    private Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .setPrettyPrinting() // Deixa o JSON formatado e legível
            .create();

    public List<Compromisso> carregarCompromissos() {
        Path caminhoDoArquivo = Paths.get(ARQUIVO_DB);
        // Retorna uma lista vazia se o arquivo não existe ou se ele está vazio.
        try {
            if (!Files.exists(caminhoDoArquivo) || Files.size(caminhoDoArquivo) == 0) {
                return new ArrayList<>();
            }
        } catch (IOException e) {
            // Se houver um erro ao verificar o tamanho, trate como se estivesse vazio
            e.printStackTrace();
            return new ArrayList<>();
        }

        try (FileReader reader = new FileReader(caminhoDoArquivo.toFile())) {
            Type tipoLista = new TypeToken<ArrayList<Compromisso>>() {}.getType();
            List<Compromisso> compromissos = gson.fromJson(reader, tipoLista);

            // Se o arquivo contiver apenas "null", também retorna uma lista vazia
            return compromissos == null ? new ArrayList<>() : compromissos;
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void salvarCompromissos(List<Compromisso> compromissos) {
        try (FileWriter writer = new FileWriter(ARQUIVO_DB)) {
            gson.toJson(compromissos, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
