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
import java.net.URL;

public class GerenciadorCompromissos {
    private static final String NOME_PASTA_APP = "NotificadorCompromissos";
    private static final String NOME_PASTA_DEV_DATA = "data"; // Pasta para dados de dev
    private static final String ARQUIVO_ATIVOS = "compromissos.json";
    private static final String ARQUIVO_CONCLUIDOS = "compromissos_concluidos.json";
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .setPrettyPrinting()
            .create();

    // Método para obter o diretório de dados do aplicativo
    private Path getAppDataDirectory() {
        Path dir;
        // Verifica se o app está rodando de um JAR ou de arquivos de classe soltos (IDE)
        URL resource = GerenciadorCompromissos.class.getResource("GerenciadorCompromissos.class");
        if (resource != null && resource.getProtocol().equals("file")) {
            // MODO DE DESENVOLVIMENTO: Salva na pasta 'data' local do projeto
            dir = Paths.get(NOME_PASTA_DEV_DATA);
        } else {
            // MODO DE PRODUÇÃO: Salva na pasta AppData do usuário
            String appDataPath = System.getenv("LOCALAPPDATA");
            // Se a variável de ambiente não existir, usa o diretório home como alternativa
            if (appDataPath == null || appDataPath.isEmpty()) {
                appDataPath = System.getProperty("user.home");
            }
            dir = Paths.get(appDataPath, NOME_PASTA_APP);
        }
        // Cria o diretório se ele não existir
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return dir;
    }

    private List<Compromisso> carregarLista(String nomeArquivo) {
        Path caminhoDoArquivo = getAppDataDirectory().resolve(nomeArquivo);
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
        // Usa o novo método para obter o caminho completo
        Path caminhoDoArquivo = getAppDataDirectory().resolve(nomeArquivo);
        try (FileWriter writer = new FileWriter(caminhoDoArquivo.toFile())) {
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

    /**
     * Marca uma instância específica de um compromisso recorrente como concluída.
     * @param compromissoPai O compromisso original.
     * @param dataDaInstancia A data da ocorrência a ser concluída.
     */
    public void concluirInstancia(Compromisso compromissoPai, LocalDate dataDaInstancia) {
        List<Compromisso> ativos = carregarCompromissos();
        // Encontra o compromisso original na lista pelo ID
        ativos.stream()
              .filter(c -> c.getId().equals(compromissoPai.getId()))
              .findFirst()
              .ifPresent(c -> {
                  c.adicionarDataConcluida(dataDaInstancia);
                  salvarCompromissos(ativos);
              });
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

    /**
     * Reativa uma instância específica de um compromisso recorrente, removendo-a da lista de datas concluídas.
     * @param compromissoPai O compromisso original.
     * @param dataDaInstancia A data da ocorrência a ser reativada.
     */
    public void reativarInstancia(Compromisso compromissoPai, LocalDate dataDaInstancia) {
        List<Compromisso> ativos = carregarCompromissos();
        ativos.stream()
              .filter(c -> c.getId().equals(compromissoPai.getId()))
              .findFirst()
              .ifPresent(c -> {
                  if (c.getDatasConcluidas() != null) {
                      c.getDatasConcluidas().remove(dataDaInstancia);
                  }
                  salvarCompromissos(ativos);
              });
    }
}