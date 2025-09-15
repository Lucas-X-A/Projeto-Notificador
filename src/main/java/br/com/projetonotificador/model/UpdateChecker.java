package br.com.projetonotificador.model;

import org.json.JSONObject; // Importa a classe para manipular JSON

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.awt.Desktop; // Usado para abrir o navegador
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

/**
 * Classe responsável por verificar se há uma nova versão do aplicativo no GitHub.
 */
public class UpdateChecker {
    
    private static final String GITHUB_USER = "Lucas-X-A";
    private static final String GITHUB_REPO = "Projeto-Notificador";
    
    // Esta é a versão ATUAL da sua aplicação. Você deve atualizar esta linha a cada nova release.
    private static final String CURRENT_VERSION = "2.1.3"; 

    // URL da API do GitHub para obter a última release
    private static final String GITHUB_API_URL = "https://api.github.com/repos/" + GITHUB_USER + "/" + GITHUB_REPO + "/releases/latest";

    /**
     * Inicia a verificação de atualização em uma nova thread para não bloquear a interface do usuário.
     */
    public static void check() {
        new Thread(() -> {
            try {
                // Cria um cliente HTTP moderno
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(GITHUB_API_URL))
                        .header("Accept", "application/vnd.github.v3+json") // Header recomendado pela API do GitHub
                        .build();

                // Envia a requisição e obtém a resposta como uma String
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                // Analisa a resposta JSON
                JSONObject jsonResponse = new JSONObject(response.body());
                String latestVersion = jsonResponse.getString("tag_name"); // Pega a tag, ex: "v2.0.1"
                
                // Remove o 'v' inicial da tag, se existir, para comparar
                String cleanLatestVersion = latestVersion.startsWith("v") ? latestVersion.substring(1) : latestVersion;

                // Compara as versões
                if (isNewerVersion(cleanLatestVersion, CURRENT_VERSION)) {
                    // Se houver uma versão mais nova, mostra um diálogo de alerta na thread da UI do JavaFX
                    String downloadUrl = jsonResponse.getString("html_url"); // Pega a URL da página da release
                    Platform.runLater(() -> showUpdateDialog(cleanLatestVersion, downloadUrl));
                }

            } catch (Exception e) {
                // Em caso de erro (ex: sem internet), simplesmente ignora e não notifica o usuário.
                // System.err.println("Erro ao verificar atualização: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Compara duas strings de versão (ex: "2.1.0" vs "2.0.0").
     * @return true se a 'newVersion' for mais recente que a 'currentVersion'.
     */
    private static boolean isNewerVersion(String newVersion, String currentVersion) {
        String[] newParts = newVersion.split("\\.");
        String[] currentParts = currentVersion.split("\\.");
        int length = Math.max(newParts.length, currentParts.length);
        for (int i = 0; i < length; i++) {
            int newPart = i < newParts.length ? Integer.parseInt(newParts[i]) : 0;
            int currentPart = i < currentParts.length ? Integer.parseInt(currentParts[i]) : 0;
            if (newPart > currentPart) {
                return true;
            }
            if (newPart < currentPart) {
                return false;
            }
        }
        return false;
    }

    /**
     * Exibe um diálogo de alerta do JavaFX informando sobre a nova versão.
     * @param newVersion A nova versão disponível.
     * @param downloadUrl O link para a página de download no GitHub.
     */
    private static void showUpdateDialog(String newVersion, String downloadUrl) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Atualização Disponível");
        alert.setHeaderText("Uma nova versão do Notificador de Compromissos está disponível!");
        alert.setContentText("Você está usando a versão " + CURRENT_VERSION + ". A versão " + newVersion + " já pode ser baixada.");

        // Adiciona o ícone personalizado à barra de título da janela.
        alert.setGraphic(null);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(UpdateChecker.class.getResourceAsStream("/images/icone_app.png")));

        // Procura pelo painel do cabeçalho para estilizar sua borda inferior.
        final javafx.scene.Node headerPanel = alert.getDialogPane().lookup(".header-panel");
        if (headerPanel != null) {
            // Define a cor da borda inferior como um cinza claro e discreto.
            headerPanel.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0; -fx-padding: 10;");
        }

        // Cria tipos de botão customizados.
        ButtonType buttonDownload = new ButtonType("Baixar Agora", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonCancel = new ButtonType("Depois", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonDownload, buttonCancel);

        // Pega os nós dos botões para estilização.
        final Button downloadBtnNode = (Button) alert.getDialogPane().lookupButton(buttonDownload);
        final Button cancelBtnNode = (Button) alert.getDialogPane().lookupButton(buttonCancel);

        // Estiliza os botões para destacar a ação principal e secundária.
        // Verde para a ação principal "Baixar"
        downloadBtnNode.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;");
        // Cinza para a ação secundária "Depois"
        cancelBtnNode.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: black; -fx-font-weight: bold;");
        
        // Define o botão de download como o padrão (acionado com a tecla Enter).
        downloadBtnNode.setDefaultButton(true);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == buttonDownload) {
            try {
                // Tenta abrir o navegador padrão do usuário na página de download
                Desktop.getDesktop().browse(new URI(downloadUrl));
            } catch (Exception e) {
                // System.err.println("Erro ao abrir o navegador: " + e.getMessage());
            }
        }
    }
}