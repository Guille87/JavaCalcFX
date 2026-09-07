package io.guillermoamadodiaz.javacalcfx.i18n;

import java.util.Locale;

/** Languages offered in the interface's language selector. */
public enum Language {
    SPANISH(Locale.forLanguageTag("es"), "Español"),
    ENGLISH(Locale.ENGLISH, "English");

    private final Locale locale;
    private final String label;

    Language(Locale locale, String label) {
        this.locale = locale;
        this.label = label;
    }

    public Locale locale() {
        return locale;
    }

    /** The language's name in its own tongue, for the selector. */
    public String label() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }

    /** The language whose tongue matches {@code locale}; {@link #SPANISH} if none. */
    public static Language from(Locale locale) {
        for (Language language : values()) {
            if (language.locale.getLanguage().equals(locale.getLanguage())) {
                return language;
            }
        }
        return SPANISH;
    }
}
