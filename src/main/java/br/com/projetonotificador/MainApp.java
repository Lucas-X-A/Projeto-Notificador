package br.com.projetonotificador;

import static javafx.application.Application.launch;

public class MainApp
{
    public static void main(String[] args) {
        if ((args.length > 0) && args[0].equals("--verificar")) {
            // Modo silencioso: apenas verifica e notifica
            Notificador notificador = new Notificador();
            notificador.verificarEAlertar();
            System.exit(0); // Fecha o programa após verificar
        } else {
            // Modo normal: abre a interface gráfica (JavaFX)
            launch(args); // Método padrão para iniciar uma app JavaFX
        }
    }
}
