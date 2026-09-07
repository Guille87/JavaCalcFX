package io.guillermoamadodiaz.javacalcfx.ui;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.guillermoamadodiaz.javacalcfx.i18n.Messages;
import java.util.Locale;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PasoAPasoCilindroTest {

    @BeforeAll
    static void enEspanol() {
        Messages.useLocale(Locale.forLanguageTag("es"));
    }

    @Test
    void sustituye_radio_y_altura_en_la_formula() {
        String d = PasoAPasoCilindro.desarrollo(5, 3);
        assertTrue(d.startsWith("Área total = 2 bases + superficie lateral:"), d);
        assertTrue(d.contains("A = 2·π·(5)·((5) + (3))"), d);
        assertTrue(d.contains("A = 2·π·(5)·(8)"), d);
        assertTrue(d.contains("A ≅ 251"), d); // 2π·5·8 = 80π ≅ 251.33
    }

    @Test
    void desglosa_bases_y_lateral() {
        String d = PasoAPasoCilindro.desarrollo(5, 3);
        assertTrue(d.contains("bases   = 2·π·(5)² ≅ 157"), d); // 50π ≅ 157.08
        assertTrue(d.contains("lateral = 2·π·(5)·(3) ≅ 94"), d); // 30π ≅ 94.25
    }

    @Test
    void rechaza_valores_negativos() {
        assertThrows(IllegalArgumentException.class, () -> PasoAPasoCilindro.desarrollo(-1, 3));
    }
}
