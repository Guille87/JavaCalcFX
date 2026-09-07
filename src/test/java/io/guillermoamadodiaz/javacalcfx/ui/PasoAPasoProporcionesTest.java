package io.guillermoamadodiaz.javacalcfx.ui;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.guillermoamadodiaz.javacalcfx.i18n.Messages;
import java.util.Locale;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PasoAPasoProporcionesTest {

    @BeforeAll
    static void enEspanol() {
        Messages.useLocale(Locale.forLanguageTag("es"));
    }

    @Nested
    class ReglaDeTres {

        @Test
        void plantea_la_proporcion_y_despeja() {
            String d = PasoAPasoReglaDeTres.desarrollo(3, 6, 5); // 3 → 6, 5 → 10
            assertTrue(d.startsWith("Regla de tres directa:"), d);
            assertTrue(d.contains("3 → 6"), d);
            assertTrue(d.contains("5 → x"), d);
            assertTrue(d.contains("x = (5 · 6) / 3"), d);
            assertTrue(d.contains("x = 30 / 3"), d);
            assertTrue(d.contains("x = 10"), d);
        }

        @Test
        void rechaza_a_cero() {
            assertThrows(IllegalArgumentException.class, () -> PasoAPasoReglaDeTres.desarrollo(0, 6, 5));
        }
    }

    @Nested
    class Porcentaje {

        @Test
        void sustituye_en_la_formula() {
            String d = PasoAPasoPorcentaje.desarrollo(15, 200); // 15 % de 200 = 30
            assertTrue(d.startsWith("El porcentaje de la cantidad:"), d);
            assertTrue(d.contains("x = (15 / 100) · 200"), d);
            assertTrue(d.contains("x = 0.15 · 200"), d);
            assertTrue(d.contains("x = 30"), d);
        }

        @Test
        void rechaza_datos_no_finitos() {
            assertThrows(IllegalArgumentException.class, () -> PasoAPasoPorcentaje.desarrollo(Double.NaN, 200));
        }
    }
}
