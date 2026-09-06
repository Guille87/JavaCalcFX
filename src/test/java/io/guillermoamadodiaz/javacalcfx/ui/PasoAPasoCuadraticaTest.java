package io.guillermoamadodiaz.javacalcfx.ui;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.guillermoamadodiaz.javacalcfx.i18n.Textos;
import java.util.Locale;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PasoAPasoCuadraticaTest {

    @BeforeAll
    static void enEspanol() {
        Textos.usarIdioma(Locale.forLanguageTag("es"));
    }

    @Test
    void empieza_por_la_formula_y_la_sustitucion() {
        String d = PasoAPasoCuadratica.desarrollo(4, -4, 1); // el ejemplo de la imagen
        assertTrue(d.startsWith("x = (-b ± √(b² - 4ac)) / 2a"), d);
        assertTrue(d.contains("x = (-(-4) ± √((-4)² - 4·(4)·(1))) / (2·(4))"), d);
        assertTrue(d.contains("√(16 - 16)"), d);
        assertTrue(d.contains("√0"), d);
    }

    @Test
    void raiz_doble_termina_en_una_sola_solucion() {
        String d = PasoAPasoCuadratica.desarrollo(1, -4, 4); // (x - 2)² : x = 4 / 2 = 2
        assertTrue(d.contains("x = 4 / 2 = 2"), d);
        assertTrue(!d.contains("x₁"), "la raíz doble no debe partir en dos ramas: " + d);
    }

    @Test
    void dos_raices_reales_muestran_ambas_ramas() {
        String d = PasoAPasoCuadratica.desarrollo(1, -5, 6); // 2 y 3
        assertTrue(d.contains("x₁ = ("), d);
        assertTrue(d.contains("x₂ = ("), d);
    }

    @Test
    void discriminante_negativo_avisa_y_pasa_a_complejos() {
        String d = PasoAPasoCuadratica.desarrollo(7, -3, 1);
        assertTrue(d.contains("en los reales no hay solución"), d);
        assertTrue(d.contains(" i"), d);
    }

    @Test
    void a_cero_lanza() {
        assertThrows(IllegalArgumentException.class, () -> PasoAPasoCuadratica.desarrollo(0, 2, 1));
    }
}
