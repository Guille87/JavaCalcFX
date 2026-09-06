package io.guillermoamadodiaz.javacalcfx.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class EntradaTest {

    @Test
    void parsea_valores_validos() {
        assertEquals(3.5, Entrada.doble("3.5"));
        assertEquals(42, Entrada.entero("42"));
        assertEquals(9_000_000_000L, Entrada.largo("9000000000"));
    }

    @Test
    void texto_no_numerico_lanza_NumberFormatException() {
        assertThrows(NumberFormatException.class, () -> Entrada.doble("abc"));
        assertThrows(NumberFormatException.class, () -> Entrada.entero(""));
        assertThrows(NumberFormatException.class, () -> Entrada.largo("1,5"));
    }

    @Test
    void entero_fuera_de_rango_lanza_NumberFormatException() {
        assertThrows(NumberFormatException.class, () -> Entrada.entero("3000000000"));
    }
}
