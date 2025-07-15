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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import br.com.projetonotificador.model.Compromisso;
import br.com.projetonotificador.model.CompromissoInstancia;
import br.com.projetonotificador.model.GerenciadorCompromissos;
import br.com.projetonotificador.model.TipoRecorrencia;

public class MainController {

    @FXML
    private ListView<CompromissoInstancia> listViewCompromissos;

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private HBox topButtonsBox;

    private GerenciadorCompromissos gerenciador;
    private ObservableList<CompromissoInstancia> compromissosVisiveis;

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
        listViewCompromissos.setCellFactory(listView -> new ListCell<CompromissoInstancia>() {
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

                titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

                detailsLabel.setWrapText(true);
                detailsLabel.setStyle("-fx-font-size: 14px; -fx-padding: 1 0 0 0;"); 

                vbox.getChildren().addAll(hbox, detailsLabel);

                // Lógica para o botão "Concluir"
                concluirButton.setOnAction(event -> {
                    CompromissoInstancia instancia = getItem();
                    if (instancia != null) {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Confirmar Conclusão");
                        alert.setHeaderText("Concluir o compromisso?");
                        alert.setContentText("Você tem certeza que deseja marcar '" + instancia.getTitulo() + "' como concluído?");
                        
                        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icone_app.png")));

                        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                            if (instancia.getCompromissoPai().getRecorrencia() == TipoRecorrencia.NAO_RECORRENTE) {
                                gerenciador.concluirCompromisso(instancia.getCompromissoPai());
                            } else {
                                gerenciador.concluirInstancia(instancia.getCompromissoPai(), instancia.getDataDaInstancia());
                            }
                            atualizarListaCompromissos();
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
                    CompromissoInstancia instancia = getItem();
                    if (instancia != null) {
                        abrirJanelaEdicao(instancia.getCompromissoPai());
                    }
                });
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

                    // Define as cores do texto
                    titleLabel.setTextFill(Color.BLACK);
                    detailsLabel.setTextFill(Color.BLACK);

                    // Mostra/esconde os botões
                    // Apenas compromissos futuros e não concluídos podem ser editados.
                    boolean podeEditar = !instancia.getDataDaInstancia().isBefore(LocalDate.now());
                    editButton.setVisible(podeEditar);
                    concluirButton.setVisible(true); // O botão de concluir é sempre visível para instâncias ativas

                    // Mostra/esconde a descrição baseando-se na seleção
                    boolean isSelected = getListView().getSelectionModel().getSelectedItem() == instancia;
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
        abrirJanelaEdicao(null); // Passa null para indicar que é um novo compromisso
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

    private void abrirJanelaEdicao(Compromisso compromisso) { // Agora aceita um compromisso nulo (para adição)
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/add-compromisso-view.fxml"));
            Stage dialogStage = new Stage();
            dialogStage.setTitle(compromisso == null ? "Adicionar Novo Compromisso" : "Editar Compromisso");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(listViewCompromissos.getScene().getWindow());
            dialogStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icone_app.png")));
            Scene scene = new Scene(loader.load());
            dialogStage.setScene(scene);

            AddCompromissoController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            if (compromisso != null) { // Se for edição, passa o objeto
                controller.setCompromissoParaEditar(compromisso);
            }

            dialogStage.showAndWait();

            atualizarListaCompromissos();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void atualizarListaCompromissos() {
        List<Compromisso> compromissosBase = gerenciador.carregarCompromissos();
        List<CompromissoInstancia> instanciasAtivas = new ArrayList<>();
        LocalDate hoje = LocalDate.now();
        LocalDate limiteGeracao = hoje.plusMonths(3); // Gera ocorrências para até 3 meses no futuro

        for (Compromisso c : compromissosBase) {
            if (c.isConcluido()) continue;

            if (c.getRecorrencia() == null || c.getRecorrencia() == TipoRecorrencia.NAO_RECORRENTE) {
                if (!c.getData().isBefore(hoje)) {
                    instanciasAtivas.add(new CompromissoInstancia(c, c.getData()));
                }
            } else {
                LocalDate dataFim = c.getDataFimRecorrencia();
                if (dataFim == null) continue;

                LocalDate dataIteracao = c.getData();
                while (!dataIteracao.isAfter(dataFim) && !dataIteracao.isAfter(limiteGeracao)) {
                    // Adiciona a instância apenas se ela não for anterior a hoje e não estiver na lista de concluídas
                    if (!dataIteracao.isBefore(hoje) && (c.getDatasConcluidas() == null || !c.getDatasConcluidas().contains(dataIteracao))) {
                        instanciasAtivas.add(new CompromissoInstancia(c, dataIteracao));
                    }

                    if (c.getRecorrencia() == TipoRecorrencia.SEMANAL) {
                        dataIteracao = dataIteracao.plusWeeks(1);
                    } else if (c.getRecorrencia() == TipoRecorrencia.MENSAL) {
                        dataIteracao = dataIteracao.plusMonths(1);
                    } else {
                        break; // Segurança para evitar loop infinito
                    }
                }
            }
        }

        instanciasAtivas.sort(Comparator.comparing(CompromissoInstancia::getDataDaInstancia));
        compromissosVisiveis.setAll(instanciasAtivas);
    }

}
