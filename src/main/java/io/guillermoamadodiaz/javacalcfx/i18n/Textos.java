package io.guillermoamadodiaz.javacalcfx.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

/**
 * Acceso central a los textos traducibles de la aplicación.
 *
 * <p>El idioma inicial es el que se guardó en la última sesión y, si no hay
 * ninguno, el del sistema ({@link Locale#getDefault()}). {@link #seleccionar}
 * lo cambia y lo recuerda. Si un idioma no tiene traducción se usa
 * {@code messages.properties} (español). Sin dependencias de JavaFX, para que
 * tanto la capa de dominio como la de interfaz puedan usarlo.
 */
public final class Textos {

    private static final String BUNDLE = "io.guillermoamadodiaz.javacalcfx.i18n.messages";
    private static final String CLAVE_IDIOMA = "idioma";

    private static Idioma idioma = idiomaInicial();
    private static ResourceBundle bundle = cargar(idioma.locale());

    private Textos() {
    }

    private static Preferences preferencias() {
        return Preferences.userNodeForPackage(Textos.class);
    }

    private static Idioma idiomaInicial() {
        try {
            String guardado = preferencias().get(CLAVE_IDIOMA, null);
            if (guardado != null) {
                return Idioma.valueOf(guardado);
            }
        } catch (RuntimeException ignorado) {
            // preferencias no disponibles o valor corrupto: usar el idioma del sistema
        }
        return Idioma.desde(Locale.getDefault());
    }

    private static ResourceBundle cargar(Locale locale) {
        return ResourceBundle.getBundle(BUNDLE, locale);
    }

    /** Idioma activo. */
    public static Idioma idioma() {
        return idioma;
    }

    /** Cambia el idioma y lo recuerda para próximos arranques. */
    public static void seleccionar(Idioma nuevo) {
        idioma = nuevo;
        bundle = cargar(nuevo.locale());
        try {
            preferencias().put(CLAVE_IDIOMA, nuevo.name());
        } catch (RuntimeException ignorado) {
            // si no se puede persistir, el cambio vale al menos para esta sesión
        }
    }

    /** Cambia solo el bundle a un locale arbitrario, sin persistir. Lo usan los tests. */
    public static void usarIdioma(Locale locale) {
        idioma = Idioma.desde(locale);
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
