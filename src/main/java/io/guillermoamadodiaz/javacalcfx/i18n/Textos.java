package io.guillermoamadodiaz.javacalcfx.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Acceso central a los textos traducibles de la aplicación.
 *
 * <p>Carga el {@link ResourceBundle} {@code messages} para el idioma del sistema
 * ({@link Locale#getDefault()}). Si no hay traducción para ese idioma se usa
 * {@code messages.properties} (español). Sin dependencias de JavaFX, para que
 * tanto la capa de dominio como la de interfaz puedan usarlo.
 */
public final class Textos {

    private static final String BUNDLE = "io.guillermoamadodiaz.javacalcfx.i18n.messages";

    private static ResourceBundle bundle = cargar(Locale.getDefault());

    private Textos() {
    }

    private static ResourceBundle cargar(Locale locale) {
        return ResourceBundle.getBundle(BUNDLE, locale);
    }

    /** Cambia el idioma en tiempo de ejecución (lo usan los tests). */
    public static void usarIdioma(Locale locale) {
        bundle = cargar(locale);
    }

    /** Texto asociado a {@code clave}. */
    public static String get(String clave) {
        return bundle.getString(clave);
    }

    /** Texto de {@code clave} con {@code {0}}, {@code {1}}… sustituidos por {@code args}. */
    public static String get(String clave, Object... args) {
        return MessageFormat.format(bundle.getString(clave), args);
    }
}
