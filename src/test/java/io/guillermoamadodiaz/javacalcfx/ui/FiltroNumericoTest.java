package io.guillermoamadodiaz.javacalcfx.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.guillermoamadodiaz.javacalcfx.ui.FiltroNumerico.Tipo;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FiltroNumericoTest {

    @Nested
    class Decimal {

        @ParameterizedTest
        @ValueSource(strings = {"", "-", ".", "-.", "3", "3.5", "-42", "-0.001", "1000"})
        void acepta(String texto) {
            assertTrue(FiltroNumerico.esValido(texto, Tipo.DECIMAL), texto);
        }

        @ParameterizedTest
        @ValueSource(strings = {"abc", "3a", "3.5.1", "1,5", "--3", "3-", "3 4", " 3"})
        void rechaza(String texto) {
            assertFalse(FiltroNumerico.esValido(texto, Tipo.DECIMAL), texto);
        }
    }

    @Nested
    class Entero {

        @ParameterizedTest
        @ValueSource(strings = {"", "-", "0", "42", "-42", "1000"})
        void acepta(String texto) {
            assertTrue(FiltroNumerico.esValido(texto, Tipo.ENTERO), texto);
        }

        @ParameterizedTest
        @ValueSource(strings = {".", "3.5", "-.", "3.", "abc", "--3", "3-", "1,5"})
        void rechaza_decimales_y_basura(String texto) {
            assertFalse(FiltroNumerico.esValido(texto, Tipo.ENTERO), texto);
        }
    }
}
