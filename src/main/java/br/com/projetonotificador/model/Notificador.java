package br.com.projetonotificador.model;

import java.awt.AWTException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

public class Notificador {

    private static Notificador instance; // A única instância da classe
    private final GerenciadorCompromissos gerenciador;
    private TrayIcon trayIcon; 
    private boolean isInfoWindowShowing = false;
    private LocalDate ultimaDataNotificada; // Data da última notificação exibida

    // Construtor privado para impedir a criação de novas instâncias
    private Notificador() {
        this.gerenciador = new GerenciadorCompromissos();
    }

    public static synchronized Notificador getInstance() {
        if (instance == null) {
            instance = new Notificador();
        }
        return instance;
    }

    public boolean isTrayIconActive() {
        return this.trayIcon != null;
    }

    public boolean verificarEAlertar() {
        List<Compromisso> compromissosBase = gerenciador.carregarCompromissos();
        List<CompromissoInstancia> instanciasDeHoje = new ArrayList<>();
        LocalDate hoje = LocalDate.now();

        for (Compromisso c : compromissosBase) {
            if (c.isConcluido()) continue;

            // Lógica para compromissos não recorrentes
            if (c.getRecorrencia() == null || c.getRecorrencia() == TipoRecorrencia.NAO_RECORRENTE) {
                if (c.getData().equals(hoje)) {
                    instanciasDeHoje.add(new CompromissoInstancia(c, c.getData()));
                }
            }
            // Lógica para compromissos recorrentes
            else {
                LocalDate dataFim = c.getDataFimRecorrencia();
                if (dataFim == null || hoje.isAfter(dataFim)) continue;

                LocalDate dataIteracao = c.getData();
                while (!dataIteracao.isAfter(dataFim)) {
                    // Se a data de iteração já passou de hoje, podemos parar de verificar este compromisso
                    if (dataIteracao.isAfter(hoje)) {
                        break; 
                    }
                    
                    // Verifica se a iteração é hoje e se esta data específica não foi concluída
                    if (dataIteracao.equals(hoje) && (c.getDatasConcluidas() == null || !c.getDatasConcluidas().contains(dataIteracao))) {
                        instanciasDeHoje.add(new CompromissoInstancia(c, dataIteracao));
                        break; // Encontrou a ocorrência de hoje, pode ir para o próximo compromisso
                    }

                    // Avança para a próxima data de recorrência
                    if (c.getRecorrencia() == TipoRecorrencia.SEMANAL) {
                        dataIteracao = dataIteracao.plusWeeks(1);
                    } else if (c.getRecorrencia() == TipoRecorrencia.MENSAL) {
                        dataIteracao = dataIteracao.plusMonths(1);
                    } else {
                        break; 
                    }
                }
            } 
        }

        if (!instanciasDeHoje.isEmpty()) {
            criarIconeNaBandeja(instanciasDeHoje);
            return true;
        }
        
        return false;
    }

    private void criarIconeNaBandeja(List<CompromissoInstancia> instancias) {
        if (!SystemTray.isSupported()) {
            System.err.println("System tray não é suportado.");
            return;
        }

        try {
            // Se o ícone não existe, cria e adiciona à bandeja.
            if (trayIcon == null) {
                SystemTray tray = SystemTray.getSystemTray();
                Image image = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/images/icone_app.png"));
                trayIcon = new TrayIcon(image, "Alerta de Compromissos");
                trayIcon.setImageAutoSize(true);
                tray.add(trayIcon);
            }

            // Remove todos os listeners antigos para evitar comportamento desatualizado.
            for (java.awt.event.ActionListener al : trayIcon.getActionListeners()) {
                trayIcon.removeActionListener(al);
            }
            for (java.awt.event.MouseListener ml : trayIcon.getMouseListeners()) {
                trayIcon.removeMouseListener(ml);
            }

            // Adiciona os novos listeners com a lista de instâncias atualizada.
            trayIcon.addActionListener(e -> exibirDetalhes(instancias));
            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
                        exibirDetalhes(instancias);
                    }
                }
            });

            LocalDate hoje = LocalDate.now();
            if (ultimaDataNotificada == null || !ultimaDataNotificada.equals(hoje)) {
                // Exibe a notificação inicial (o balão)
                String tituloNotificacao = "Você tem " + instancias.size() + " compromissos hoje.";
                if (instancias.size() == 1) {
                    tituloNotificacao = "Compromisso para Hoje: " + instancias.get(0).getTitulo();
                }
            
                trayIcon.displayMessage(
                    tituloNotificacao,
                "Clique no ícone para ver os detalhes.",
                    TrayIcon.MessageType.INFO
                );

                ultimaDataNotificada = hoje; // Atualiza a data da última notificação
            } 
            
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    private synchronized void exibirDetalhes(List<CompromissoInstancia> instancias) {
        // Se a janela já estiver aberta, não faz nada.
        if (isInfoWindowShowing) {
            return;
        }

        try {
            isInfoWindowShowing = true; // Ativa a trava

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            // Constrói uma única string HTML com todos os compromissos do dia
            String htmlContent = instancias.stream()
                .map(inst -> String.format(
                    "<p><b>Título:</b> %s<br>" +
                    "<b>Data:</b> %s<br>" +
                    "<b>Descrição:</b> %s</p>",
                    inst.getTitulo(),
                    inst.getDataDaInstancia().format(formatter),
                    inst.getDescricao()
                ))
                .collect(Collectors.joining("<hr>")); // Usa uma linha horizontal como separador

            // Envolve o conteúdo com as tags <html> para que o JOptionPane o interprete corretamente
            String mensagemFinal = "<html><body style='width: 300px;'>" + htmlContent + "</body></html>";

            // Carrega o ícone original
            ImageIcon originalIcon = new ImageIcon(getClass().getResource("/images/icone_app.png"));
            int iconSize = 48; // Tamanho desejado

            // Cria uma nova imagem em buffer com suporte a transparência
            BufferedImage resizedImage = new BufferedImage(iconSize, iconSize, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = resizedImage.createGraphics();

            // Aplica dicas de renderização para alta qualidade de interpolação
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            
            // Desenha a imagem original na nova imagem redimensionada
            g2d.drawImage(originalIcon.getImage(), 0, 0, iconSize, iconSize, null);
            g2d.dispose(); // Libera os recursos gráficos

            ImageIcon finalIcon = new ImageIcon(resizedImage);

            JOptionPane.showMessageDialog(null, mensagemFinal, "Compromissos de Hoje", JOptionPane.PLAIN_MESSAGE, finalIcon);
        
        } finally {
            isInfoWindowShowing = false; // Libera a trava quando a janela é fechada
        }
    }
}