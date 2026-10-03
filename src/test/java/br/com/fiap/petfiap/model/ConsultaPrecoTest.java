package br.com.fiap.petfiap.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Testes unitarios do model: sem banco, sem Spring (Aula 15).
public class ConsultaPrecoTest {

    private ConsultaVeterinaria consultaComPorte(String porte) {
        return new ConsultaVeterinaria(1, "Mimi", porte, "Bruno", LocalDateTime.of(2026, 10, 1, 14, 0));
    }

    @Test
    public void deveCustar150ReaisQuandoQualquerPorte() {
        // Act + Assert: preco fixo, o porte nao muda o valor
        assertEquals(150.0, consultaComPorte("PEQUENO").calcularPreco(), 0.001);
        assertEquals(150.0, consultaComPorte("MEDIO").calcularPreco(), 0.001);
        assertEquals(150.0, consultaComPorte("GRANDE").calcularPreco(), 0.001);
    }
}
