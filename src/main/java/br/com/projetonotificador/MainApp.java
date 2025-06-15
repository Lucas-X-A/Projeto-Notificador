package br.com.projetonotificador;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    // 2. Adicionado o metodo start() obrigatório
    @Override
    public void start(Stage primaryStage) {
        // É AQUI que você vai construir a sua janela principal.
        // Por enquanto, vamos colocar um texto simples.
        Label label = new Label("Bem-vindo ao Alerta de Compromissos!");
        StackPane root = new StackPane(label);
        Scene scene = new Scene(root, 640, 480);

        primaryStage.setTitle("Alerta de Compromissos");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--verificar")) {
            // Modo silencioso: apenas verifica e notifica
            System.out.println("Modo de verificação ativado...");
            Notificador notificador = new Notificador();
            notificador.verificarEAlertar();
            System.exit(0); // Fecha o programa após verificar
        } else {
            // Modo normal: abre a interface gráfica (JavaFX)
            launch(args); // Este método agora funcionará corretamente
        }
    }
}