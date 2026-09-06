package io.guillermoamadodiaz.javacalcfx.ui;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.guillermoamadodiaz.javacalcfx.i18n.Textos;
import java.util.Locale;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PasoAPasoPitagorasTest {

    @BeforeAll
    static void enEspanol() {
        Textos.usarIdioma(Locale.forLanguageTag("es"));
    }

    @Test
    void terna_3_4_5_sustituye_y_resuelve() {
        String d = PasoAPasoPitagoras.desarrollo(3, 4);
        assertTrue(d.startsWith("Hipotenusa (teorema de Pitágoras):"), d);
        assertTrue(d.contains("h = √((3)² + (4)²)"), d);
        assertTrue(d.contains("h = √(9 + 16)"), d);
        assertTrue(d.contains("h = √25"), d);
        assertTrue(d.contains("h = 5"), d);
    }

    @Test
    void incluye_area_perimetro_y_angulos() {
        String d = PasoAPasoPitagoras.desarrollo(3, 4);
        assertTrue(d.contains("A = (a · b) / 2 = (3 · 4) / 2 = 6"), d);
        assertTrue(d.contains("P = a + b + h = 3 + 4 + 5 = 12"), d);
        assertTrue(d.contains("α = arctan(b / a) = arctan(4 / 3)"), d);
        assertTrue(d.contains("β = 90° − α"), d);
    }

    @Test
    void rechaza_catetos_no_positivos() {
        assertThrows(IllegalArgumentException.class, () -> PasoAPasoPitagoras.desarrollo(0, 4));
        assertThrows(IllegalArgumentException.class, () -> PasoAPasoPitagoras.desarrollo(3, -1));
    }
}
