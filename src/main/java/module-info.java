module br.com.projetonotificador {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires com.google.gson;
    requires java.desktop;

    opens br.com.projetonotificador.controllers to javafx.fxml;
    opens br.com.projetonotificador.model to com.google.gson;
    exports br.com.projetonotificador;
}