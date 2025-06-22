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
    private Compromisso compromissoParaEditar;

    public AddCompromissoController() {
        this.gerenciador = new GerenciadorCompromissos();
        this.compromissoParaEditar = null;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setCompromissoParaEditar(Compromisso compromisso) {
        this.compromissoParaEditar = compromisso;
        txtTitulo.setText(compromisso.getTitulo());
        txtDescricao.setText(compromisso.getDescricao());
        datePicker.setValue(compromisso.getData());
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

        List<Compromisso> todos = gerenciador.carregarCompromissos();

        if (compromissoParaEditar == null) { // Modo de Adição
            Compromisso novo = new Compromisso(titulo, descricao, data);
            todos.add(novo);
        } else { // Modo de Edição
            int index = todos.indexOf(this.compromissoParaEditar);
            if (index != -1) {
                Compromisso aAtualizar = todos.get(index);
                aAtualizar.setTitulo(titulo);
                aAtualizar.setDescricao(descricao);
                aAtualizar.setData(data);
            } else {
                // Opcional: Tratar erro se o compromisso não for encontrado
                System.err.println("Erro: Compromisso a ser editado não encontrado na lista.");
                return;
            }
        }

        gerenciador.salvarCompromissos(todos);
        dialogStage.close();
    }
}