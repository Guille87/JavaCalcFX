package io.guillermoamadodiaz.javacalcfx.ui;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.guillermoamadodiaz.javacalcfx.i18n.Messages;
import java.util.Locale;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ProportionsStepsTest {

    @BeforeAll
    static void inSpanish() {
        Messages.useLocale(Locale.forLanguageTag("es"));
    }

    @Nested
    class RuleOfThree {

        @Test
        void sets_up_the_proportion_and_solves() {
            String d = RuleOfThreeSteps.explain(3, 6, 5); // 3 → 6, 5 → 10
            assertTrue(d.startsWith("Regla de tres directa:"), d);
            assertTrue(d.contains("3 → 6"), d);
            assertTrue(d.contains("5 → x"), d);
            assertTrue(d.contains("x = (5 · 6) / 3"), d);
            assertTrue(d.contains("x = 30 / 3"), d);
            assertTrue(d.contains("x = 10"), d);
        }

        @Test
        void rejects_a_zero() {
            assertThrows(IllegalArgumentException.class, () -> RuleOfThreeSteps.explain(0, 6, 5));
        }
    }

    @Nested
    class Percentage {

        @Test
        void substitutes_into_the_formula() {
            String d = PercentageSteps.explain(15, 200); // 15% of 200 = 30
            assertTrue(d.startsWith("El porcentaje de la cantidad:"), d);
            assertTrue(d.contains("x = (15 / 100) · 200"), d);
            assertTrue(d.contains("x = 0.15 · 200"), d);
            assertTrue(d.contains("x = 30"), d);
        }

        @Test
        void rejects_non_finite_data() {
            assertThrows(IllegalArgumentException.class, () -> PercentageSteps.explain(Double.NaN, 200));
        }
    }
}
