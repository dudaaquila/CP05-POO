package br.com.fiap.petfiap.service;

import br.com.fiap.petfiap.model.Banho;
import br.com.fiap.petfiap.repository.AtendimentoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

// Teste unitario da agenda: NAO sobe o Spring e NAO conecta no Oracle (Aula 15).
@ExtendWith(MockitoExtension.class)
public class AgendaServiceDataPassadoTest {

    @Mock
    private AtendimentoRepository repository;

    @InjectMocks
    private AgendaService service;

    @Test
    public void deveRecusarAgendamentoQuandoDataHoraNoPassado() {
        // Arrange
        Banho noPassado = new Banho(1, "Rex", "PEQUENO", "Ana", LocalDateTime.now().minusDays(1));

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> service.agendar(noPassado));

        // O banco nem e consultado nem acionado
        verify(repository, never()).findByPetNome(anyString());
        verify(repository, never()).save(any());
    }
}
