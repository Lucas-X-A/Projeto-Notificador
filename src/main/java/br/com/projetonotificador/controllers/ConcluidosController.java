package br.com.projetonotificador.controllers;

import br.com.projetonotificador.model.Compromisso;
import br.com.projetonotificador.model.CompromissoInstancia;
import br.com.projetonotificador.model.GerenciadorCompromissos;
import br.com.projetonotificador.model.TipoRecorrencia;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ConcluidosController {

    @FXML
    private ListView<CompromissoInstancia> listViewConcluidos;

    private MainController mainController;
    private GerenciadorCompromissos gerenciador;
    private ObservableList<CompromissoInstancia> compromissosConcluidosVisiveis;
    
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

        listViewConcluidos.setCellFactory(listView -> new ListCell<CompromissoInstancia>() {
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
                    CompromissoInstancia instancia = getItem();
                    if (instancia != null) {
                        Compromisso pai = instancia.getCompromissoPai();
                        if (pai.isConcluido()) { // É um compromisso totalmente concluído
                            gerenciador.reativarCompromisso(pai);
                        } else { // É uma instância de um compromisso recorrente
                            gerenciador.reativarInstancia(pai, instancia.getDataDaInstancia());
                        }
                        atualizarListaConcluidos();
                    }
                });

                removerButton.setOnAction(event -> {
                    CompromissoInstancia instancia = getItem();
                    // Adiciona uma verificação para garantir que só removemos compromissos totalmente concluídos
                    if (instancia != null && instancia.getCompromissoPai().isConcluido()) {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Confirmar Remoção");
                        alert.setHeaderText("Remover permanentemente o compromisso?");

                        Text boldText = new Text("Esta ação não pode ser desfeita.\n\n");
                        boldText.setStyle("-fx-font-weight: bold;");
                        Text regularText = new Text("Título: " + instancia.getTitulo());
                        TextFlow textFlow = new TextFlow(boldText, regularText);
                        
                        alert.getDialogPane().setContent(textFlow);

                        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icone_app.png")));
                        
                        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                            List<Compromisso> concluidos = gerenciador.carregarCompromissosConcluidos();
                            concluidos.removeIf(c -> c.getId().equals(instancia.getCompromissoPai().getId()));
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
            protected void updateItem(CompromissoInstancia instancia, boolean empty) {
                super.updateItem(instancia, empty);
                if (empty || instancia == null) {
                    setGraphic(null);
                    setStyle("");
                } else {
                    // Popula os dados
                    String dataFormatada = instancia.getDataDaInstancia().format(formatter);
                    titleLabel.setText(dataFormatada + " - " + instancia.getTitulo());
                    detailsLabel.setText("Detalhes: " + instancia.getDescricao());

                    // O botão de remover só é visível para compromissos que foram totalmente concluídos
                    removerButton.setVisible(instancia.getCompromissoPai().isConcluido());

                    // Estilo para indicar que está concluído
                    titleLabel.setStyle("-fx-strikethrough: true; -fx-text-fill: gray;");
                    detailsLabel.setStyle("-fx-text-fill: gray;");

                    // Mostra/esconde a descrição baseando-se na seleção
                    boolean isSelected = getListView().getSelectionModel().getSelectedItem() == instancia;
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
        List<CompromissoInstancia> todasAsInstanciasConcluidas = new ArrayList<>();

        // 1. Carrega os compromissos que foram totalmente concluídos
        List<Compromisso> concluidosTotalmente = gerenciador.carregarCompromissosConcluidos();
        for (Compromisso c : concluidosTotalmente) {
            todasAsInstanciasConcluidas.add(new CompromissoInstancia(c, c.getData()));
        }

        // 2. Carrega os compromissos ativos para encontrar instâncias concluídas de tarefas recorrentes
        List<Compromisso> ativos = gerenciador.carregarCompromissos();
        for (Compromisso c : ativos) {
            if (c.getRecorrencia() != TipoRecorrencia.NAO_RECORRENTE && c.getDatasConcluidas() != null) {
                for (LocalDate dataConcluida : c.getDatasConcluidas()) {
                    todasAsInstanciasConcluidas.add(new CompromissoInstancia(c, dataConcluida));
                }
            }
        }

        // 3. Ordena a lista combinada por data
        todasAsInstanciasConcluidas.sort(Comparator.comparing(CompromissoInstancia::getDataDaInstancia));

        // 4. Atualiza a UI
        compromissosConcluidosVisiveis.setAll(todasAsInstanciasConcluidas);
    }
}