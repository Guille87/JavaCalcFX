package io.guillermoamadodiaz.javacalcfx.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;
import java.util.concurrent.CancellationException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.guillermoamadodiaz.javacalcfx.i18n.Textos;

class MensajesDeErrorTest {

    @BeforeAll
    static void fijarIdioma() {
        Textos.usarIdioma(Locale.forLanguageTag("es"));
    }

    @AfterAll
    static void restaurarIdioma() {
        Textos.usarIdioma(Locale.getDefault());
    }

    @Test
    void cancelacion_no_produce_mensaje() {
        assertEquals("", MensajesDeError.describir(new CancellationException()));
    }

    @Test
    void numero_invalido_da_mensaje_generico() {
        assertEquals("Por favor ingresa números válidos en todos los campos.",
                MensajesDeError.describir(new NumberFormatException("For input string: \"x\"")));
    }

    @Test
    void argumento_ilegal_conserva_el_mensaje_de_dominio() {
        assertEquals("Error: El radio no puede ser negativo.",
                MensajesDeError.describir(new IllegalArgumentException("El radio no puede ser negativo.")));
    }

    @Test
    void aritmetica_se_trata_como_error_de_dominio() {
        assertTrue(MensajesDeError.describir(new ArithmeticException("/ by zero")).startsWith("Error: "));
    }

    @Test
    void excepcion_inesperada_se_marca_como_tal() {
        assertTrue(MensajesDeError.describir(new IllegalStateException("boom")).startsWith("Error inesperado: "));
    }
}
