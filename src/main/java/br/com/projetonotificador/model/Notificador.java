package br.com.projetonotificador.model;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane; 

public class Notificador {

    private final GerenciadorCompromissos gerenciador;

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
                    // Otimização: se a data de iteração já passou de hoje, podemos parar de verificar este compromisso
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
                        break; // Segurança
                    }
                }
            } 
        }

        // Exibe uma notificação para cada instância encontrada para hoje
        for (CompromissoInstancia instancia : instanciasDeHoje) {
            exibirNotificacao(instancia);
        }
        
        return !instanciasDeHoje.isEmpty();
    }

    private void exibirNotificacao(CompromissoInstancia compromisso) {
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
                    compromisso.getDataDaInstancia().format(formatter),
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