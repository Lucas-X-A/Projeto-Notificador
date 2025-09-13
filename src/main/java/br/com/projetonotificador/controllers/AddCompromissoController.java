package br.com.projetonotificador.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

import br.com.projetonotificador.model.Compromisso;
import br.com.projetonotificador.model.GerenciadorCompromissos;
import br.com.projetonotificador.model.TipoRecorrencia;

public class AddCompromissoController {

    @FXML
    private TextField txtTitulo;
    @FXML
    private TextArea txtDescricao;
    @FXML
    private DatePicker datePicker;
    @FXML
    private ComboBox<TipoRecorrencia> comboRecorrencia;
    @FXML
    private Label lblDataFim;
    @FXML
    private DatePicker datePickerFim;

    private Stage dialogStage;
    private GerenciadorCompromissos gerenciador;
    private Compromisso compromissoParaEditar;

    @FXML
    public void initialize() {
        gerenciador = new GerenciadorCompromissos();
        compromissoParaEditar = null;

        // Configura o ComboBox de recorrência
        comboRecorrencia.getItems().setAll(TipoRecorrencia.values());
        comboRecorrencia.setValue(TipoRecorrencia.NAO_RECORRENTE);

        // Esconde os campos de data final por padrão e garante que não ocupem espaço
        lblDataFim.setVisible(false);
        datePickerFim.setVisible(false);
        lblDataFim.setManaged(false);
        datePickerFim.setManaged(false);

        // Adiciona um listener para mostrar/esconder a data final
        comboRecorrencia.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isRecorrente = newVal != null && newVal != TipoRecorrencia.NAO_RECORRENTE;
            lblDataFim.setVisible(isRecorrente);
            datePickerFim.setVisible(isRecorrente);
            lblDataFim.setManaged(isRecorrente);
            datePickerFim.setManaged(isRecorrente);
        });
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
        // Impede que a janela de diálogo seja redimensionada ou maximizada.
        this.dialogStage.setResizable(false);

    }

    public void setCompromissoParaEditar(Compromisso compromisso) {
        this.compromissoParaEditar = compromisso;
        txtTitulo.setText(compromisso.getTitulo());
        txtDescricao.setText(compromisso.getDescricao());
        datePicker.setValue(compromisso.getData());

        // Popula os campos de recorrência, permitindo a edição
        comboRecorrencia.setValue(compromisso.getRecorrencia());
        if (compromisso.getRecorrencia() != TipoRecorrencia.NAO_RECORRENTE) {
            datePickerFim.setValue(compromisso.getDataFimRecorrencia());
        }
    }

    @FXML
    private void onSalvarClick() {
        String titulo = txtTitulo.getText();
        String descricao = txtDescricao.getText();
        LocalDate data = datePicker.getValue();
        TipoRecorrencia recorrencia = comboRecorrencia.getValue();
        LocalDate dataFim = datePickerFim.getValue();

        if (titulo == null || titulo.trim().isEmpty() || data == null) {
            showAlert("O título e a data de início são campos obrigatórios!");
            return;
        }

        if (recorrencia != TipoRecorrencia.NAO_RECORRENTE) {
            if (dataFim == null) {
                showAlert("A data final é obrigatória para compromissos recorrentes!");
                return;
            }
            if (dataFim.isBefore(data)) {
                showAlert("A data final da recorrência não pode ser anterior à data de início!");
                return;
            }
        }

        List<Compromisso> todos = gerenciador.carregarCompromissos();

        if (compromissoParaEditar == null) { // Modo de Adição
            Compromisso novo = new Compromisso(titulo, descricao, data);
            novo.setRecorrencia(recorrencia);
            if (recorrencia != TipoRecorrencia.NAO_RECORRENTE) {
                novo.setDataFimRecorrencia(dataFim);
            }
            todos.add(novo);
        } else { // Modo de Edição
            int index = todos.indexOf(this.compromissoParaEditar);
            if (index != -1) {
                Compromisso aAtualizar = todos.get(index);
                aAtualizar.setTitulo(titulo);
                aAtualizar.setDescricao(descricao);
                aAtualizar.setData(data);
                // Atualiza os dados de recorrência
                aAtualizar.setRecorrencia(recorrencia);
                if (recorrencia != TipoRecorrencia.NAO_RECORRENTE) {
                    aAtualizar.setDataFimRecorrencia(dataFim);
                } else {
                    aAtualizar.setDataFimRecorrencia(null); // Limpa a data final se não for mais recorrente
                }
            } else {
                System.err.println("Erro: Compromisso a ser editado não encontrado na lista.");
                return;
            }
        }

        gerenciador.salvarCompromissos(todos);
        dialogStage.close();
    }

    @FXML
    private void onCancelarClick() {
        dialogStage.close();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro de Validação");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}