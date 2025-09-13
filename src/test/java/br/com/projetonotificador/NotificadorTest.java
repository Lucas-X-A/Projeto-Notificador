package br.com.projetonotificador;

import br.com.projetonotificador.model.Compromisso;
import br.com.projetonotificador.model.GerenciadorCompromissos;
import br.com.projetonotificador.model.Notificador;
import br.com.projetonotificador.model.TipoRecorrencia;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.TrayIcon;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificadorTest {

    // Mock para a dependência externa que lê arquivos.
    // Não queremos que nosso teste dependa do sistema de arquivos real.
    @Mock
    private GerenciadorCompromissos gerenciadorCompromissosMock;

    // Mock para o TrayIcon, pois não podemos (e não queremos) criar um ícone real no teste.
    @Mock
    private TrayIcon trayIconMock;

    // @Spy em um Singleton é complexo. Vamos usar reflexão para injetar os mocks.
    private Notificador notificador;

    @BeforeEach
    void setUp() throws Exception {
        // Obtém a instância Singleton do Notificador
        notificador = Notificador.getInstance();

        // Usamos "reflexão" para injetar nosso Gerenciador de Compromissos mockado
        // dentro da instância Singleton do Notificador. Isso nos dá controle total.
        Field gerenciadorField = Notificador.class.getDeclaredField("gerenciador");
        gerenciadorField.setAccessible(true);
        gerenciadorField.set(notificador, gerenciadorCompromissosMock);

        // Também injetamos nosso TrayIcon mockado para podermos verificar suas chamadas.
        Field trayIconField = Notificador.class.getDeclaredField("trayIcon");
        trayIconField.setAccessible(true);
        trayIconField.set(notificador, trayIconMock);
        
        // Resetamos a data da última notificação antes de cada teste para garantir isolamento.
        Field ultimaDataNotificadaField = Notificador.class.getDeclaredField("ultimaDataNotificada");
        ultimaDataNotificadaField.setAccessible(true);
        ultimaDataNotificadaField.set(notificador, null);

        // Ensina o mock a retornar um array vazio em vez de null para evitar NullPointerException.
        when(trayIconMock.getActionListeners()).thenReturn(new java.awt.event.ActionListener[0]);
        when(trayIconMock.getMouseListeners()).thenReturn(new java.awt.event.MouseListener[0]);
    }

    @Test
    @DisplayName("Deve exibir notificação na primeira chamada do dia, mas não na segunda")
    void testNotificacaoApenasUmaVezPorDia() {
        // --- ARRANGE (Preparação) ---

        // 1. Cria um compromisso de teste para o dia de hoje.
        Compromisso compromissoDeHoje = new Compromisso(
            "Teste de Notificação", 
            "Descrição de teste",
            LocalDate.now()
        );

        // 2. Usa os métodos 'set' para os campos restantes.
        compromissoDeHoje.setRecorrencia(TipoRecorrencia.NAO_RECORRENTE);

        List<Compromisso> listaDeCompromissos = new ArrayList<>();
        listaDeCompromissos.add(compromissoDeHoje);

        // 2. Configura o Mockito: Quando o notificador chamar "carregarCompromissos",
        //    retorne nossa lista de teste em vez de ler o arquivo real.
        when(gerenciadorCompromissosMock.carregarCompromissos()).thenReturn(listaDeCompromissos);

        // --- ACT & ASSERT (Ação e Verificação) - Primeira Chamada ---

        System.out.println("Executando a primeira verificação do dia...");
        notificador.verificarEAlertar();

        // 3. VERIFICAÇÃO: Confirma que o método displayMessage() foi chamado EXATAMENTE 1 VEZ.
        //    Isso prova que a notificação foi exibida.
        verify(trayIconMock, times(1)).displayMessage(anyString(), anyString(), any(TrayIcon.MessageType.class));
        System.out.println("Verificado: Notificação exibida na primeira chamada.");

        // --- ACT & ASSERT (Ação e Verificação) - Segunda Chamada ---

        System.out.println("\nExecutando a segunda verificação no mesmo dia...");
        notificador.verificarEAlertar();

        // 4. VERIFICAÇÃO FINAL: Confirma que o método displayMessage() AINDA foi chamado apenas 1 vez no total.
        //    Se a lógica estivesse errada, o total seria 2. Isso prova que a notificação NÃO foi reexibida.
        verify(trayIconMock, times(1)).displayMessage(anyString(), anyString(), any(TrayIcon.MessageType.class));
        System.out.println("Verificado: Notificação NÃO foi exibida na segunda chamada. Teste passou!");
    }
}