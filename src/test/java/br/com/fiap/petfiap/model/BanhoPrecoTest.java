package br.com.fiap.petfiap.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Testes unitarios do model: sem banco, sem Spring (Aula 15).
public class BanhoPrecoTest {

    private Banho banhoComPorte(String porte) {
        return new Banho(1, "Rex", porte, "Ana", LocalDateTime.of(2026, 10, 1, 10, 0));
    }

    @Test
    public void deveCobrarPrecoCrescenteQuandoPorteAumenta() {
        // Act
        double pequeno = banhoComPorte("PEQUENO").calcularPreco();
        double medio = banhoComPorte("MEDIO").calcularPreco();
        double grande = banhoComPorte("GRANDE").calcularPreco();

        // Assert: PEQUENO 60, MEDIO 80, GRANDE 100
        assertEquals(60.0, pequeno, 0.001);
        assertEquals(80.0, medio, 0.001);
        assertEquals(100.0, grande, 0.001);
    }
}
