package br.com.projetonotificador.model;

import java.awt.*;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class Notificador {

    public void verificarEAlertar() {
        GerenciadorCompromissos gerenciador = new GerenciadorCompromissos();
        List<Compromisso> todos = gerenciador.carregarCompromissos();

        List<Compromisso> compromissosDeHoje = todos.stream()
                .filter(c -> !c.isConcluido() && c.getData().equals(LocalDate.now()))
                .collect(Collectors.toList());

        if (!compromissosDeHoje.isEmpty() && SystemTray.isSupported()) {
            for (Compromisso c : compromissosDeHoje) {
                exibirNotificacao(c.getTitulo(), c.getDescricao());
            }
        }
    }

    private void exibirNotificacao(String titulo, String descricao) {
        try {
            SystemTray tray = SystemTray.getSystemTray();
            URL imageUrl = Notificador.class.getResource("/images/icone_app.png");
            if (imageUrl == null) {
                System.err.println("Arquivo de recurso não encontrado: /images/icone_app.png.");
                return;
            }
            Image image = Toolkit.getDefaultToolkit().createImage(imageUrl);
            TrayIcon trayIcon = new TrayIcon(image, "Alerta de Compromisso");
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip("Você tem um compromisso pra hoje: " + titulo + ": " + descricao);
            tray.add(trayIcon);
            trayIcon.displayMessage("Lembrete de Compromisso!", titulo, TrayIcon.MessageType.INFO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
