package br.com.projetonotificador;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

import br.com.projetonotificador.model.Notificador;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/main-view.fxml"));
        Parent root = loader.load();
        
        primaryStage.setTitle("Alerta de Compromissos");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icone_app.png")));
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--verificar")) {
            // Modo silencioso: apenas verifica e notifica
            System.out.println("Modo de verificação ativado...");
            Notificador notificador = new Notificador();
            boolean notificou = notificador.verificarEAlertar();
            
            // Se não notificou, o programa pode fechar.
            if (!notificou) {
                System.exit(0);
            }
        } else {
            // Modo normal: abre a interface gráfica (JavaFX)
            launch(args);
        }
    }
}