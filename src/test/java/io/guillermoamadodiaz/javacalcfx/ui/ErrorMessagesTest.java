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

class ErrorMessagesTest {

    @BeforeAll
    static void pinLanguage() {
        Messages.useLocale(Locale.forLanguageTag("es"));
    }

    @AfterAll
    static void restoreLanguage() {
        Messages.useLocale(Locale.getDefault());
    }

    @Test
    void cancellation_produces_no_message() {
        assertEquals("", ErrorMessages.describe(new CancellationException()));
    }

    @Test
    void an_invalid_number_gives_a_generic_message() {
        assertEquals(
                "Por favor ingresa números válidos en todos los campos.",
                ErrorMessages.describe(new NumberFormatException("For input string: \"x\"")));
    }

    @Test
    void an_illegal_argument_keeps_the_domain_message() {
        assertEquals(
                "Error: El radio no puede ser negativo.",
                ErrorMessages.describe(new IllegalArgumentException("El radio no puede ser negativo.")));
    }

    @Test
    void a_calculation_error_is_translated_by_its_key() {
        assertEquals(
                "Error: El año debe ser mayor que cero.",
                ErrorMessages.describe(new CalculationError("calc.year.not.positive")));
    }

    @Test
    void a_calculation_error_also_translates_the_value_name() {
        assertEquals(
                "Error: El radio no puede ser negativo.",
                ErrorMessages.describe(
                        new CalculationError("calc.value.negative", new CalculationError.Name("calc.name.radius"))));
    }

    @Test
    void a_calculation_error_with_a_literal_argument() {
        assertEquals(
                "Error: El número es demasiado grande (máximo 100000).",
                ErrorMessages.describe(new CalculationError("calc.factorial.large", "100000")));
    }

    @Test
    void arithmetic_is_treated_as_a_domain_error() {
        assertTrue(ErrorMessages.describe(new ArithmeticException("/ by zero")).startsWith("Error: "));
    }

    @Test
    void an_unexpected_exception_is_marked_as_such() {
        assertTrue(ErrorMessages.describe(new IllegalStateException("boom")).startsWith("Error inesperado: "));
    }
}
