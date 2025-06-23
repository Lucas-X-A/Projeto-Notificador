package br.com.projetonotificador.controllers;

import br.com.projetonotificador.model.Compromisso;
import br.com.projetonotificador.model.GerenciadorCompromissos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public class ConcluidosController {

    @FXML
    private ListView<Compromisso> listViewConcluidos;

    private MainController mainController;
    private GerenciadorCompromissos gerenciador;
    private ObservableList<Compromisso> compromissosConcluidosVisiveis;

    @FXML
    public void initialize() {
        gerenciador = new GerenciadorCompromissos();
        compromissosConcluidosVisiveis = FXCollections.observableArrayList();
        listViewConcluidos.setItems(compromissosConcluidosVisiveis);

        // Listener para forçar a atualização da lista na mudança de seleção (essencial para expandir/recolher)
        listViewConcluidos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (oldSelection != newSelection) {
                listViewConcluidos.refresh();
            }
        });

        listViewConcluidos.setCellFactory(listView -> new ListCell<Compromisso>() {
            private final VBox vbox = new VBox();
            private final HBox hbox = new HBox();
            private final Label titleLabel = new Label();
            private final Label detailsLabel = new Label();
            private final Pane pane = new Pane();
            private final Button reativarButton = new Button("Reativar");
            private final Button removerButton = new Button("Remover");
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            {
                // Configuração do layout da célula
                HBox.setHgrow(pane, Priority.ALWAYS);
                hbox.getChildren().addAll(titleLabel, pane, reativarButton, removerButton);
                hbox.setAlignment(Pos.CENTER);
                hbox.setSpacing(10);

                detailsLabel.setWrapText(true);
                detailsLabel.setStyle("-fx-font-weight: bold; -fx-padding: 10 0 0 0;");

                vbox.getChildren().addAll(hbox, detailsLabel);

                // Lógica para expandir/recolher ao clicar
                this.setOnMouseClicked(event -> {
                    if (isEmpty() || getItem() == null) return;
                    if (getListView().getSelectionModel().getSelectedItem() == getItem()) {
                        getListView().getSelectionModel().clearSelection();
                    } else {
                        getListView().getSelectionModel().select(getItem());
                    }
                });

                reativarButton.setOnAction(event -> {
                    Compromisso compromisso = getItem();
                    if (compromisso != null) {
                        gerenciador.reativarCompromisso(compromisso);
                        atualizarListaConcluidos();
                    }
                });

                removerButton.setOnAction(event -> {
                    Compromisso compromisso = getItem();
                    if (compromisso != null) {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Confirmar Remoção");
                        alert.setHeaderText("Remover permanentemente o compromisso?");

                        Text boldText = new Text("Esta ação não pode ser desfeita.\n\n");
                        boldText.setStyle("-fx-font-weight: bold;");
                        Text regularText = new Text("Título: " + compromisso.getTitulo());
                        TextFlow textFlow = new TextFlow(boldText, regularText);
                        
                        alert.getDialogPane().setContent(textFlow);

                        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icone_app.png")));
                        
                        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                            List<Compromisso> concluidos = gerenciador.carregarCompromissosConcluidos();
                            concluidos.remove(compromisso);
                            gerenciador.salvarCompromissosConcluidos(concluidos);
                            atualizarListaConcluidos();
                        }
                    }
                });

                // Impede que o clique nos botões se propague para a célula
                reativarButton.setOnMouseClicked(mouseEvent -> mouseEvent.consume());
                removerButton.setOnMouseClicked(mouseEvent -> mouseEvent.consume());
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

                    // Estilo para indicar que está concluído
                    titleLabel.setStyle("-fx-strikethrough: true; -fx-text-fill: gray;");
                    detailsLabel.setStyle("-fx-text-fill: gray;");

                    // Mostra/esconde a descrição baseando-se na seleção
                    boolean isSelected = getListView().getSelectionModel().getSelectedItem() == compromisso;
                    detailsLabel.setVisible(isSelected);
                    detailsLabel.setManaged(isSelected);

                    // Define um fundo customizado com uma borda inferior para separação
                    String borderStyle = "-fx-border-width: 0 0 1 0; -fx-border-color: #e0e0e0;";
                    if (getIndex() % 2 == 0) {
                        setStyle("-fx-background-color: #fafafa; " + borderStyle);
                    } else {
                        setStyle("-fx-background-color: #ffffff; " + borderStyle);
                    }

                    setGraphic(vbox);
                }
            }
        });

        atualizarListaConcluidos();
    }

    /**
     * Recebe a instância do MainController para permitir a comunicação de volta.
     * @param mainController O controlador da tela principal.
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void onVoltarClick() {
        if (mainController != null) {
            mainController.mostrarCompromissosAtivos();
        }
    }

    private void atualizarListaConcluidos() {
        List<Compromisso> concluidos = gerenciador.carregarCompromissosConcluidos();
        concluidos.sort(Comparator.comparing(Compromisso::getData));
        compromissosConcluidosVisiveis.setAll(concluidos);
    }
}