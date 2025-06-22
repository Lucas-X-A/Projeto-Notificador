package br.com.projetonotificador.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
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

        // Configura a CellFactory para personalizar a exibição de cada item
        listViewCompromissos.setCellFactory(listView -> new ListCell<Compromisso>() {
            private final HBox hbox = new HBox();
            private final Label label = new Label();
            private final Pane pane = new Pane();
            private final Button button = new Button("Editar");
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            {
                HBox.setHgrow(pane, Priority.ALWAYS);
                hbox.getChildren().addAll(label, pane, button);
                hbox.setAlignment(Pos.CENTER);

                button.setOnAction(event -> {
                    Compromisso compromisso = getItem();
                    if (compromisso != null) {
                        abrirJanelaEdicao(compromisso);
                    }
                });
            }

            @Override
            protected void updateItem(Compromisso compromisso, boolean empty) {
                super.updateItem(compromisso, empty);

                if (empty || compromisso == null) {
                    setText(null);
                    setGraphic(null); // Limpa o conteúdo gráfico da célula
                } else {
                    // Define o texto da Label dentro do HBox
                    String dataFormatada = compromisso.getData().format(formatter);
                    label.setText(dataFormatada + " - " + compromisso.getTitulo());

                    // Define a visibilidade do botão com base nas condições
                    boolean podeEditar = !compromisso.isConcluido() && !compromisso.getData().isBefore(LocalDate.now());
                    button.setVisible(podeEditar);

                    // Define o HBox como o conteúdo gráfico da célula
                    setGraphic(hbox);
                    setText(null); // Limpa o texto padrão da célula para evitar duplicidade
                }
            }
        });

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
            dialogStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icone_app.png")));
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

    private void abrirJanelaEdicao(Compromisso compromisso) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/add-compromisso-view.fxml"));
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Editar Compromisso");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(listViewCompromissos.getScene().getWindow());
            dialogStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icone_app.png")));
            Scene scene = new Scene(loader.load());
            dialogStage.setScene(scene);

            AddCompromissoController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setCompromissoParaEditar(compromisso);

            dialogStage.showAndWait();

            atualizarListaCompromissos();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void atualizarListaCompromissos() {
        List<Compromisso> todos = gerenciador.carregarCompromissos();
        todos.sort(Comparator.comparing(Compromisso::getData));
        compromissosVisiveis.setAll(todos);
    }

}
