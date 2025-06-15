package br.com.projetonotificador;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class GerenciadorCompromissos {
    private static final String ARQUIVO_DB = "compromissos.json";
    private Gson gson = new Gson();

    public List<Compromisso> carregarCompromissos() {
        if (!Files.exists(Paths.get(ARQUIVO_DB))) {
            return new ArrayList<>(); // Retorna lista vazia se o arquivo não existe
        }
        try (FileReader reader = new FileReader(ARQUIVO_DB)) {
            Type tipoLista = new TypeToken<ArrayList<Compromisso>>() {}.getType();
            return gson.fromJson(reader, tipoLista);
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
