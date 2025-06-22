package br.com.projetonotificador.model;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane; 

public class Notificador {

    private final GerenciadorCompromissos gerenciador;

    public Notificador() {
        this.gerenciador = new GerenciadorCompromissos();
    }

    public boolean verificarEAlertar() {
        List<Compromisso> compromissos = gerenciador.carregarCompromissos();
        LocalDate hoje = LocalDate.now();
        boolean notificacaoExibida = false; // Flag para rastrear se mostramos algo

        for (Compromisso c : compromissos) {
            if (c.getData().equals(hoje) && !c.isConcluido()) {
                exibirNotificacao(c);
                notificacaoExibida = true; // Marcamos que uma notificação foi exibida
            }
        }
        return notificacaoExibida; // Retornamos o resultado
    }

    private void exibirNotificacao(Compromisso compromisso) {
        if (!SystemTray.isSupported()) {
            System.err.println("System tray não é suportado.");
            return;
        }

        try {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/images/icone_app.png"));
            TrayIcon trayIcon = new TrayIcon(image, "Alerta de Compromisso");
            trayIcon.setImageAutoSize(true);

            // Remove listeners antigos para evitar múltiplas janelas
            for (ActionListener listener : trayIcon.getActionListeners()) {
                trayIcon.removeActionListener(listener);
            }

            // 1. Adiciona um "ouvinte de ação" para quando o balão for clicado
            trayIcon.addActionListener(e -> {
                // 2. Formata a data e cria a mensagem detalhada
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                String mensagemDetalhada = String.format(
                    "Título: %s\nData: %s\n\nDescrição:\n%s",
                    compromisso.getTitulo(),
                    compromisso.getData().format(formatter),
                    compromisso.getDescricao()
                );
                
                // Carrega a imagem original
                ImageIcon originalIcon = new ImageIcon(getClass().getResource("/images/icone_app.png"));
                // Redimensiona a imagem para um tamanho adequado (ex: 32x32 pixels)
                Image resizedImage = originalIcon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
                // Cria um novo ImageIcon com a imagem redimensionada
                ImageIcon finalIcon = new ImageIcon(resizedImage);

                // 3. Exibe uma janela de diálogo (JOptionPane) com o ícone redimensionado
                JOptionPane.showMessageDialog(null, mensagemDetalhada, "Detalhes do Compromisso", JOptionPane.PLAIN_MESSAGE, finalIcon);

                // 4. Remove o ícone da bandeja do sistema para limpeza
                tray.remove(trayIcon);
            });

            tray.add(trayIcon);

            // A mensagem inicial agora é um convite para clicar
            trayIcon.displayMessage(
                "Compromisso para Hoje: " + compromisso.getTitulo(),
                "Clique aqui para ver os detalhes.",
                TrayIcon.MessageType.INFO
            );

        } catch (AWTException e) {
            e.printStackTrace();
        }
    }
}