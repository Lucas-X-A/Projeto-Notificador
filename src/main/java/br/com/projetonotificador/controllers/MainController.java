package br.com.projetonotificador.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

import br.com.projetonotificador.model.Compromisso;
import br.com.projetonotificador.model.GerenciadorCompromissos;

public class MainController {

    @FXML
    private ListView<Compromisso> listViewCompromissos;

    private GerenciadorCompromissos gerenciador;
    private ObservableList<Compromisso> compromissosVisiveis;

    @FXML
    public void initialize() {
        gerenciador = new GerenciadorCompromissos();
        compromissosVisiveis = FXCollections.observableArrayList();
        listViewCompromissos.setItems(compromissosVisiveis);
        atualizarListaCompromissos();
    }

    @FXML
    private void onAdicionarCompromissoClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/add-compromisso-view.fxml"));
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Adicionar Novo Compromisso");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(listViewCompromissos.getScene().getWindow());
            Scene scene = new Scene(loader.load());
            dialogStage.setScene(scene);

            // Passa o palco para o controlador para que ele possa se fechar
            AddCompromissoController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            // Atualiza a lista após o diálogo ser fechado
            atualizarListaCompromissos();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void atualizarListaCompromissos() {
        List<Compromisso> todos = gerenciador.carregarCompromissos();
        compromissosVisiveis.setAll(todos);
    }
}
