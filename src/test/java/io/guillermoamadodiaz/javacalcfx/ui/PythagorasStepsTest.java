package io.guillermoamadodiaz.javacalcfx.ui;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.guillermoamadodiaz.javacalcfx.i18n.Messages;
import java.util.Locale;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PythagorasStepsTest {

    @BeforeAll
    static void inSpanish() {
        Messages.useLocale(Locale.forLanguageTag("es"));
    }

    @Test
    void triple_3_4_5_substitutes_and_solves() {
        String d = PythagorasSteps.explain(3, 4);
        assertTrue(d.startsWith("Hipotenusa (teorema de Pitágoras):"), d);
        assertTrue(d.contains("h = √((3)² + (4)²)"), d);
        assertTrue(d.contains("h = √(9 + 16)"), d);
        assertTrue(d.contains("h = √25"), d);
        assertTrue(d.contains("h = 5"), d);
    }

    @Test
    void includes_area_perimeter_and_angles() {
        String d = PythagorasSteps.explain(3, 4);
        assertTrue(d.contains("A = (a · b) / 2 = (3 · 4) / 2 = 6"), d);
        assertTrue(d.contains("P = a + b + h = 3 + 4 + 5 = 12"), d);
        assertTrue(d.contains("α = arctan(b / a) = arctan(4 / 3)"), d);
        assertTrue(d.contains("β = 90° − α"), d);
    }

    @Test
    void rejects_non_positive_legs() {
        assertThrows(IllegalArgumentException.class, () -> PythagorasSteps.explain(0, 4));
        assertThrows(IllegalArgumentException.class, () -> PythagorasSteps.explain(3, -1));
    }
}
