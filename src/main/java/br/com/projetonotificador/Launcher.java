package br.com.projetonotificador;

import com.formdev.flatlaf.FlatLightLaf;

/**
 * Classe lançadora para contornar problemas com o JavaFX no empacotamento.
 */
public class Launcher {
    public static void main(String[] args) {
        FlatLightLaf.setup(); // Configura o Look and Feel da FlatLaf antes de chamar o MainApp
        MainApp.main(args);
    }
}
