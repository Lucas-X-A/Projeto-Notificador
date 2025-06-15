package br.com.projetonotificador;

import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.Toolkit;
import java.awt.Image;
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
                exibirNotificacao(c.getDescricao());
            }
        }
    }

    private void exibirNotificacao(String descricao) {
        try {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().createImage("icon.png"); // Adicione um ícone ao seu projeto
            TrayIcon trayIcon = new TrayIcon(image, "Alerta de Compromisso");
            trayIcon.setImageAutoSize(true);
            tray.add(trayIcon);
            trayIcon.displayMessage("Lembrete de Compromisso!", descricao, TrayIcon.MessageType.INFO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
