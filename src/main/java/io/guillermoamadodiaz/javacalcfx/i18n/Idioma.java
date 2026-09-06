package io.guillermoamadodiaz.javacalcfx.i18n;

import java.util.Locale;

/** Idiomas que ofrece la aplicación en el selector de la interfaz. */
public enum Idioma {

    ESPANOL(Locale.forLanguageTag("es"), "Español"),
    INGLES(Locale.ENGLISH, "English");

    private final Locale locale;
    private final String etiqueta;

    Idioma(Locale locale, String etiqueta) {
        this.locale = locale;
        this.etiqueta = etiqueta;
    }

    public Locale locale() {
        return locale;
    }

    /** Nombre del idioma en su propia lengua, para mostrarlo en el selector. */
    public String etiqueta() {
        return etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }

    /** El idioma cuya lengua coincide con {@code locale}; {@link #ESPANOL} si ninguno. */
    public static Idioma desde(Locale locale) {
        for (Idioma idioma : values()) {
            if (idioma.locale.getLanguage().equals(locale.getLanguage())) {
                return idioma;
            }
        }
        return ESPANOL;
    }
}
