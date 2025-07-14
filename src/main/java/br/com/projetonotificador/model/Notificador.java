package br.com.projetonotificador.model;

import java.awt.AWTException;
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

    private final GerenciadorCompromissos gerenciador;
    private static TrayIcon trayIcon; // Ícone estático para persistir
    private static boolean isInfoWindowShowing = false;


    public Notificador() {
        this.gerenciador = new GerenciadorCompromissos();
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

        // Se o ícone já existe, não faz nada.
        if (trayIcon != null) {
            return;
        }

        try {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/images/icone_app.png"));
            trayIcon = new TrayIcon(image, "Alerta de Compromissos");
            trayIcon.setImageAutoSize(true);

            // Ação ao clicar na notificação
            trayIcon.addActionListener(e -> exibirDetalhes(instancias));

            // Ação ao clicar no ícone na bandeja do sistema
            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) { 
                    if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
                        exibirDetalhes(instancias);
                    }
                }
            });

            tray.add(trayIcon);
        
            // Exibe a notificação inicial (o balão)
            String tituloNotificacao = "Você tem " + instancias.size() + " compromisso(s) hoje.";
            if (instancias.size() == 1) {
                tituloNotificacao = "Compromisso para Hoje: " + instancias.get(0).getTitulo();
            }
            
            trayIcon.displayMessage(
                tituloNotificacao,
                "Clique no ícone para ver os detalhes.",
                TrayIcon.MessageType.INFO
            );

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
            
            // Constrói uma única string com todos os compromissos do dia
            String mensagemDetalhada = instancias.stream()
                .map(inst -> String.format(
                    "Título: %s\nData: %s\nDescrição: %s",
                    inst.getTitulo(),
                    inst.getDataDaInstancia().format(formatter),
                    inst.getDescricao()
                ))
                .collect(Collectors.joining("\n\n---\n\n"));

            ImageIcon originalIcon = new ImageIcon(getClass().getResource("/images/icone_app.png"));
            Image resizedImage = originalIcon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
            ImageIcon finalIcon = new ImageIcon(resizedImage);

            JOptionPane.showMessageDialog(null, mensagemDetalhada, "Compromissos de Hoje", JOptionPane.PLAIN_MESSAGE, finalIcon);
        
        } finally {
            isInfoWindowShowing = false; // Libera a trava quando a janela é fechada
        }
    }
}