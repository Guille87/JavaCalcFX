package io.guillermoamadodiaz.javacalcfx.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;
import org.junit.jupiter.api.Test;

class LanguageTest {

    @Test
    void from_recognizes_spanish_and_english_ignoring_the_country() {
        assertEquals(Language.SPANISH, Language.from(Locale.forLanguageTag("es-ES")));
        assertEquals(Language.SPANISH, Language.from(Locale.forLanguageTag("es-AR")));
        assertEquals(Language.ENGLISH, Language.from(Locale.US));
        assertEquals(Language.ENGLISH, Language.from(Locale.UK));
    }

    @Test
    void from_falls_back_to_spanish_for_an_unsupported_language() {
        assertEquals(Language.SPANISH, Language.from(Locale.forLanguageTag("de")));
        assertEquals(Language.SPANISH, Language.from(Locale.FRANCE));
    }

    @Test
    void the_label_is_in_the_languages_own_tongue() {
        assertEquals("Español", Language.SPANISH.label());
        assertEquals("English", Language.ENGLISH.label());
    }
}
