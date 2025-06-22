package br.com.projetonotificador.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

import br.com.projetonotificador.model.Compromisso;
import br.com.projetonotificador.model.GerenciadorCompromissos;

public class AddCompromissoController {

    @FXML
    private TextField txtTitulo;

    @FXML
    private TextArea txtDescricao;

    @FXML
    private DatePicker datePicker;

    private Stage dialogStage;
    private GerenciadorCompromissos gerenciador;

    public AddCompromissoController() {
        this.gerenciador = new GerenciadorCompromissos();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @FXML
    private void onSalvarClick() {
        String titulo = txtTitulo.getText();
        String descricao = txtDescricao.getText();
        LocalDate data = datePicker.getValue();

        if (titulo == null || titulo.trim().isEmpty() || data == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro de Validação");
            alert.setHeaderText(null);
            alert.setContentText("O título e a data são campos obrigatórios!");
            alert.showAndWait();
            return;
        }

        Compromisso novo = new Compromisso(titulo, descricao, data);
        List<Compromisso> todos = gerenciador.carregarCompromissos();
        todos.add(novo);
        gerenciador.salvarCompromissos(todos);

        dialogStage.close();
    }
}