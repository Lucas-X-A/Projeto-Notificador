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

    // ================== TESTES ==================
    public static void main(String[] args) {
        System.out.println("--- INICIANDO TESTE DE ADIÇÃO DE COMPROMISSO ---");

        // 1. Cria uma instância do gerenciador
        GerenciadorCompromissos gerenciador = new GerenciadorCompromissos();

        // 2. Carrega a lista de compromissos existentes
        System.out.println("Carregando compromissos existentes...");
        List<Compromisso> listaDeCompromissos = gerenciador.carregarCompromissos();
        System.out.println("Encontrados " + listaDeCompromissos.size() + " compromissos.");

        // 3. Cria um novo compromisso (simulando o que o usuário digitaria na tela)
        System.out.println("Criando novo compromisso de teste...");
        Compromisso novoCompromisso1 = new Compromisso(
                "Teste: Reunião importante 1", "Comparecer à reunião teste 1",
                LocalDate.now() // Compromisso hoje
        );
        Compromisso novoCompromisso2 = new Compromisso(
                "Teste: Reunião importante 2", "Ir pra reunião teste 2",
                LocalDate.now().plusDays(5) // Compromisso para daqui a 5 dias
        );

        Compromisso novoCompromisso3 = new Compromisso(
                "Teste: Consulta médica", "Ir pra consulta médica de teste",
                LocalDate.now().plusDays(10) // Compromisso para daqui a 10 dias
        );

        // 4. Adiciona o novo compromisso à lista
        listaDeCompromissos.add(novoCompromisso1);
        listaDeCompromissos.add(novoCompromisso2);
        listaDeCompromissos.add(novoCompromisso3);
        System.out.println("Novos compromissos adicionados à lista.");

        // 5. Salva a lista inteira de volta no arquivo JSON
        System.out.println("Salvando a lista atualizada no arquivo 'compromissos.json'...");
        gerenciador.salvarCompromissos(listaDeCompromissos);

        // --- Parte 2: Testar o notificador ---
        System.out.println("\n--- Agora, testando a notificação ---");
        Notificador notificador = new Notificador();
        notificador.verificarEAlertar();

        System.out.println("--- TESTE CONCLUÍDO ---");
    }
    // ============================================
}
