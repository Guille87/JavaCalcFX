package io.guillermoamadodiaz.javacalcfx.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;
import org.junit.jupiter.api.Test;

class IdiomaTest {

    @Test
    void desde_reconoce_espanol_e_ingles_ignorando_el_pais() {
        assertEquals(Idioma.ESPANOL, Idioma.desde(Locale.forLanguageTag("es-ES")));
        assertEquals(Idioma.ESPANOL, Idioma.desde(Locale.forLanguageTag("es-AR")));
        assertEquals(Idioma.INGLES, Idioma.desde(Locale.US));
        assertEquals(Idioma.INGLES, Idioma.desde(Locale.UK));
    }

    @Test
    void desde_cae_en_espanol_para_un_idioma_no_soportado() {
        assertEquals(Idioma.ESPANOL, Idioma.desde(Locale.forLanguageTag("de")));
        assertEquals(Idioma.ESPANOL, Idioma.desde(Locale.FRANCE));
    }

    @Test
    void la_etiqueta_esta_en_la_propia_lengua() {
        assertEquals("Español", Idioma.ESPANOL.etiqueta());
        assertEquals("English", Idioma.INGLES.etiqueta());
    }
}
