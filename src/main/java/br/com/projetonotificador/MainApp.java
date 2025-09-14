package br.com.projetonotificador;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import atlantafx.base.theme.CupertinoLight;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

import br.com.projetonotificador.model.Notificador;
import br.com.projetonotificador.model.UpdateChecker;

public class MainApp extends Application {

    private static final int SINGLE_INSTANCE_PORT = 61357;
    private static final String SINGLE_INSTANCE_COMMAND = "SHOW_WINDOW";
    private Stage primaryStage;
    private static MainApp instance;
    private static ServerSocket serverSocket;
    private static boolean showUIOnStart = false;

    @Override
    public void start(Stage primaryStage) throws IOException {
        instance = this;
        this.primaryStage = primaryStage;
        boolean notificou = Notificador.getInstance().verificarEAlertar();

        // A aplicação só se encerra se não notificou E se a UI não foi explicitamente solicitada.
        if (!notificou && !showUIOnStart) {
            System.out.println("Nenhum compromisso para hoje. Encerrando aplicação silenciosamente.");
            Platform.exit(); // Encerra a aplicação JavaFX
            return; 
        }

        // Se chegou até aqui, é porque há compromissos. O app deve continuar rodando.
        Platform.setImplicitExit(false); // Impede que o app feche ao fechar a janela
        primaryStage.setOnCloseRequest(event -> primaryStage.hide()); // Esconde a janela

        Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet());
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/main-view.fxml"));
        Parent root = loader.load();
        
        primaryStage.setTitle("Alerta de Compromissos");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icone_app.png")));
        primaryStage.setScene(new Scene(root));

        if (showUIOnStart) {
            primaryStage.show();
        }
        
        // Faz verificação de atualização em segundo plano
        UpdateChecker.check();
    }

    private void showStage() {
        if (primaryStage != null) {
            Platform.runLater(() -> {
                primaryStage.show();
                primaryStage.toFront();
            });
        }
    }

    // Sobrescreve o método stop() para o cleanup
    @Override
    public void stop() throws Exception {
        System.out.println("Encerrando a aplicação e liberando recursos...");
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
        super.stop();
    }
    
    public static void main(String[] args) {
        showUIOnStart = !Arrays.asList(args).contains("--verificar");
        try {
            serverSocket = new ServerSocket(SINGLE_INSTANCE_PORT);
            // Se chegou aqui, é a primeira instância ("servidor").
            
            Thread listenerThread = new Thread(() -> {
                while (serverSocket != null && !serverSocket.isClosed()) {
                    try (Socket clientSocket = serverSocket.accept();
                         BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                        String command = in.readLine();
                        if (SINGLE_INSTANCE_COMMAND.equals(command)) {
                            if (instance != null) {
                                instance.showStage();
                            }
                        }
                    } catch (IOException e) {
                        if (serverSocket.isClosed()) {
                            System.out.println("Thread de escuta encerrada pois o socket foi fechado.");
                            break;
                        }
                        e.printStackTrace();
                    }
                }
            });

            listenerThread.setDaemon(true);
            listenerThread.start();
            launch(args);

        } catch (BindException e) {
            System.out.println("Instância já em execução. Enviando comando para mostrar a janela.");
            try (Socket clientSocket = new Socket("localhost", SINGLE_INSTANCE_PORT);
                 PrintWriter out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true)) {
                out.println(SINGLE_INSTANCE_COMMAND);
                System.exit(0);
            } catch (IOException ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}