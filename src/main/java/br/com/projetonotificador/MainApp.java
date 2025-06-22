package br.com.projetonotificador;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

public class MainApp extends Application {

    private GerenciadorCompromissos gerenciador;
    private ObservableList<Compromisso> compromissosVisiveis;
    private ListView<Compromisso> listView;

    @Override
    public void start(Stage primaryStage) {
        gerenciador = new GerenciadorCompromissos();
        compromissosVisiveis = FXCollections.observableArrayList();

        primaryStage.setTitle("Alerta de Compromissos");

        // Layout principal
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Botão para adicionar novo compromisso
        Button btnAdicionar = new Button("Adicionar Compromisso");
        btnAdicionar.setOnAction(e -> exibirTelaAdicionarCompromisso(primaryStage));
        
        VBox topContainer = new VBox(btnAdicionar);
        topContainer.setAlignment(Pos.CENTER);
        topContainer.setPadding(new Insets(10));
        root.setTop(topContainer);

        // Lista de compromissos
        listView = new ListView<>(compromissosVisiveis);
        listView.setPlaceholder(new Label("Nenhum compromisso foi adicionado ainda."));
        root.setCenter(listView);

        // Carregar os dados iniciais
        atualizarListaCompromissos();

        Scene scene = new Scene(root, 700, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void exibirTelaAdicionarCompromisso(Stage owner) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(owner);
        dialogStage.setTitle("Adicionar Novo Compromisso");

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER_LEFT);

        // Campos do formulário
        TextField txtTitulo = new TextField();
        txtTitulo.setPromptText("Título do compromisso");

        TextArea txtDescricao = new TextArea();
        txtDescricao.setPromptText("Descrição detalhada");

        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Data do compromisso");

        Button btnSalvar = new Button("Salvar");
        btnSalvar.setOnAction(e -> {
            String titulo = txtTitulo.getText();
            String descricao = txtDescricao.getText();
            LocalDate data = datePicker.getValue();

            if (titulo.isEmpty() || data == null) {
                // Opcional: Adicionar um alerta para o usuário
                System.out.println("Título e data são obrigatórios!");
                return;
            }

            Compromisso novo = new Compromisso(titulo, descricao, data);
            List<Compromisso> todos = gerenciador.carregarCompromissos();
            todos.add(novo);
            gerenciador.salvarCompromissos(todos);

            atualizarListaCompromissos();
            dialogStage.close();
        });

        vbox.getChildren().addAll(
            new Label("Título:"), txtTitulo,
            new Label("Descrição:"), txtDescricao,
            new Label("Data:"), datePicker,
            btnSalvar
        );

        Scene dialogScene = new Scene(vbox, 400, 350);
        dialogStage.setScene(dialogScene);
        dialogStage.showAndWait();
    }

    private void atualizarListaCompromissos() {
        List<Compromisso> todos = gerenciador.carregarCompromissos();
        compromissosVisiveis.setAll(todos);
    }

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--verificar")) {
            // Modo silencioso: apenas verifica e notifica
            System.out.println("Modo de verificação ativado...");
            Notificador notificador = new Notificador();
            notificador.verificarEAlertar();
            System.exit(0); // Fecha o programa após verificar
        } else {
            // Modo normal: abre a interface gráfica (JavaFX)
            launch(args);
        }
    }
}