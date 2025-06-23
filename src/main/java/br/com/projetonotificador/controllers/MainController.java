package br.com.projetonotificador.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
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

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private HBox topButtonsBox;

    private GerenciadorCompromissos gerenciador;
    private ObservableList<Compromisso> compromissosVisiveis;

    @FXML
    public void initialize() {
        gerenciador = new GerenciadorCompromissos();
        compromissosVisiveis = FXCollections.observableArrayList();
        listViewCompromissos.setItems(compromissosVisiveis);

        listViewCompromissos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            // Se a seleção mudou, atualiza a lista
            if (oldSelection != newSelection) {
                listViewCompromissos.refresh();
            }
        });

        // Configura a CellFactory
        listViewCompromissos.setCellFactory(listView -> new ListCell<Compromisso>() {
            private final VBox vbox = new VBox();
            private final HBox hbox = new HBox();
            private final Label titleLabel = new Label();
            private final Label detailsLabel = new Label();
            private final Pane pane = new Pane();
            private final Button editButton = new Button("Editar");
            private final Button concluirButton = new Button("Concluir");
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            {
                // Configuração do layout da célula
                HBox.setHgrow(pane, Priority.ALWAYS);
                hbox.getChildren().addAll(titleLabel, pane, editButton, concluirButton);
                hbox.setAlignment(Pos.CENTER);
                hbox.setSpacing(10);

                detailsLabel.setWrapText(true);
                detailsLabel.setStyle("-fx-padding: 5 0 0 0;"); // Espaçamento acima da descrição

                vbox.getChildren().addAll(hbox, detailsLabel);

                // Lógica para o botão "Concluir"
                concluirButton.setOnAction(event -> {
                    Compromisso compromisso = getItem();
                    if (compromisso != null) {
                        // Exibe uma mensagem de confirmação
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Confirmar Conclusão");
                        alert.setHeaderText("Concluir o compromisso?");
                        alert.setContentText("Você tem certeza que deseja marcar '" + compromisso.getTitulo() + "' como concluído?");
                        
                        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icone_app.png")));

                        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                            gerenciador.concluirCompromisso(compromisso);
                            atualizarListaCompromissos(); // Atualiza a lista principal
                        }
                    }
                });

                
                // Lógica para expandir/recolher ao clicar
                this.setOnMouseClicked(event -> {
                    if (isEmpty() || getItem() == null) return;

                    // Se clicou no item já selecionado, limpa a seleção (recolhe)
                    if (getListView().getSelectionModel().getSelectedItem() == getItem()) {
                        getListView().getSelectionModel().clearSelection();
                    } else { // Senão, seleciona o novo item (expande)
                        getListView().getSelectionModel().select(getItem());
                    }
                });

                editButton.setOnMouseClicked(mouseEvent -> mouseEvent.consume());
                concluirButton.setOnMouseClicked(mouseEvent -> mouseEvent.consume());

                editButton.setOnAction(event -> {
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
                    setGraphic(null);
                    setStyle("");
                } else {
                    // Popula os dados
                    String dataFormatada = compromisso.getData().format(formatter);
                    titleLabel.setText(dataFormatada + " - " + compromisso.getTitulo());
                    detailsLabel.setText("Detalhes: " + compromisso.getDescricao());

                    // Define as cores do texto
                    titleLabel.setTextFill(Color.BLACK);
                    detailsLabel.setTextFill(Color.BLACK);

                    // Mostra/esconde os botões
                    // Apenas compromissos futuros e não concluídos podem ser editados.
                    boolean podeEditar = !compromisso.isConcluido() && !compromisso.getData().isBefore(LocalDate.now());
                    editButton.setVisible(podeEditar);

                    // Qualquer compromisso não concluído pode ser marcado como tal, mesmo que já tenha passado.
                    boolean podeConcluir = !compromisso.isConcluido();
                    concluirButton.setVisible(podeConcluir);

                    // Mostra/esconde a descrição baseando-se na seleção
                    boolean isSelected = getListView().getSelectionModel().getSelectedItem() == compromisso;
                    detailsLabel.setVisible(isSelected);
                    detailsLabel.setManaged(isSelected);

                    // Define um fundo customizado com uma borda inferior para separação
                    String borderStyle = "-fx-border-width: 0 0 1 0; -fx-border-color: #e0e0e0;";

                    // Define um fundo customizado (sem a cor do texto)
                    if (getIndex() % 2 == 0) {
                        setStyle("-fx-background-color: #fafafa;" + borderStyle); // Cor para linhas pares
                    } else {
                        setStyle("-fx-background-color: #ffffff;" + borderStyle); // Cor para linhas ímpares
                    }

                    setGraphic(vbox);
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

    @FXML
    private void onVerConcluidosClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/concluidos-view.fxml"));
            BorderPane concluidosView = loader.load(); // Carrega a view dos concluídos

            // Passa uma referência deste MainController para o ConcluidosController
            ConcluidosController controller = loader.getController();
            controller.setMainController(this);

            // Substitui o centro da tela principal pela nova view
            mainBorderPane.setCenter(concluidosView);
            topButtonsBox.setVisible(false); // Esconde os botões do topo

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void mostrarCompromissosAtivos() {
        mainBorderPane.setCenter(listViewCompromissos); 
        topButtonsBox.setVisible(true); 
        atualizarListaCompromissos(); 
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
        todos.removeIf(Compromisso::isConcluido);
        todos.sort(Comparator.comparing(Compromisso::getData));
        compromissosVisiveis.setAll(todos);
    }

}
