package io.guillermoamadodiaz.javacalcfx.ui;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.guillermoamadodiaz.javacalcfx.i18n.Messages;
import java.util.Locale;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class QuadraticStepsTest {

    @BeforeAll
    static void inSpanish() {
        Messages.useLocale(Locale.forLanguageTag("es"));
    }

    @Test
    void starts_with_the_formula_and_the_substitution() {
        String d = QuadraticSteps.explain(4, -4, 1); // the example from the image
        assertTrue(d.startsWith("x = (-b ± √(b² - 4ac)) / 2a"), d);
        assertTrue(d.contains("x = (-(-4) ± √((-4)² - 4·(4)·(1))) / (2·(4))"), d);
        assertTrue(d.contains("√(16 - 16)"), d);
        assertTrue(d.contains("√0"), d);
    }

    @Test
    void a_double_root_ends_in_a_single_solution() {
        String d = QuadraticSteps.explain(1, -4, 4); // (x - 2)² : x = 4 / 2 = 2
        assertTrue(d.contains("x = 4 / 2 = 2"), d);
        assertTrue(!d.contains("x₁"), "a double root must not split into two branches: " + d);
    }

    @Test
    void two_real_roots_show_both_branches() {
        String d = QuadraticSteps.explain(1, -5, 6); // 2 and 3
        assertTrue(d.contains("x₁ = ("), d);
        assertTrue(d.contains("x₂ = ("), d);
    }

    @Test
    void a_negative_discriminant_warns_and_moves_to_complex() {
        String d = QuadraticSteps.explain(7, -3, 1);
        assertTrue(d.contains("en los reales no hay solución"), d);
        assertTrue(d.contains(" i"), d);
    }

    @Test
    void a_zero_throws() {
        assertThrows(IllegalArgumentException.class, () -> QuadraticSteps.explain(0, 2, 1));
    }
}
