package io.guillermoamadodiaz.javacalcfx.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.guillermoamadodiaz.javacalcfx.calc.CalculationError;
import io.guillermoamadodiaz.javacalcfx.i18n.Messages;
import java.util.Locale;
import java.util.concurrent.CancellationException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MensajesDeErrorTest {

    @BeforeAll
    static void fijarIdioma() {
        Messages.useLocale(Locale.forLanguageTag("es"));
    }

    @AfterAll
    static void restaurarIdioma() {
        Messages.useLocale(Locale.getDefault());
    }

    @Test
    void cancelacion_no_produce_mensaje() {
        assertEquals("", MensajesDeError.describir(new CancellationException()));
    }

    @Test
    void numero_invalido_da_mensaje_generico() {
        assertEquals(
                "Por favor ingresa números válidos en todos los campos.",
                MensajesDeError.describir(new NumberFormatException("For input string: \"x\"")));
    }

    @Test
    void argumento_ilegal_conserva_el_mensaje_de_dominio() {
        assertEquals(
                "Error: El radio no puede ser negativo.",
                MensajesDeError.describir(new IllegalArgumentException("El radio no puede ser negativo.")));
    }

    @Test
    void error_de_calculo_se_traduce_por_su_clave() {
        assertEquals(
                "Error: El año debe ser mayor que cero.",
                MensajesDeError.describir(new CalculationError("calc.year.not.positive")));
    }

    @Test
    void error_de_calculo_traduce_tambien_el_nombre_del_dato() {
        assertEquals(
                "Error: El radio no puede ser negativo.",
                MensajesDeError.describir(
                        new CalculationError("calc.value.negative", new CalculationError.Name("calc.name.radius"))));
    }

    @Test
    void error_de_calculo_con_argumento_literal() {
        assertEquals(
                "Error: El número es demasiado grande (máximo 100000).",
                MensajesDeError.describir(new CalculationError("calc.factorial.large", "100000")));
    }

    @Test
    void aritmetica_se_trata_como_error_de_dominio() {
        assertTrue(
                MensajesDeError.describir(new ArithmeticException("/ by zero")).startsWith("Error: "));
    }

    @Test
    void excepcion_inesperada_se_marca_como_tal() {
        assertTrue(MensajesDeError.describir(new IllegalStateException("boom")).startsWith("Error inesperado: "));
    }
}
