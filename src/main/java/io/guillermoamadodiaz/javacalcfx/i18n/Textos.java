package io.guillermoamadodiaz.javacalcfx.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.prefs.Preferences;

/**
 * Acceso central a los textos traducibles de la aplicación.
 *
 * <p>El idioma inicial es el que se guardó en la última sesión y, si no hay
 * ninguno, el del sistema ({@link Locale#getDefault()}). {@link #seleccionar}
 * lo cambia y lo recuerda. Los ficheros {@code messages*.properties} se leen
 * directamente (sin {@link java.util.ResourceBundle}, cuya búsqueda depende del
 * locale por defecto): {@code messages.properties} es español y sirve de base;
 * cada idioma añade su fichero encima. Sin dependencias de JavaFX.
 */
public final class Textos {

    private static final String RUTA = "/io/guillermoamadodiaz/javacalcfx/i18n/";
    private static final String CLAVE_IDIOMA = "idioma";

    private static Idioma idioma = idiomaInicial();
    private static Properties textos = cargar(idioma);

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

    private static Properties leer(String fichero) {
        Properties p = new Properties();
        try (InputStream in = Textos.class.getResourceAsStream(RUTA + fichero)) {
            if (in == null) {
                throw new MissingResourceException(
                        "No se encuentra " + fichero, Textos.class.getName(), fichero);
            }
            try (Reader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                p.load(r);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return p;
    }

    private static Properties cargar(Idioma idioma) {
        Properties base = leer("messages.properties"); // español, siempre presente
        if (idioma == Idioma.ESPANOL) {
            return base;
        }
        Properties p = new Properties(base); // lo que falte, se toma del español
        p.putAll(leer("messages_" + idioma.locale().getLanguage() + ".properties"));
        return p;
    }

    /** Idioma activo. */
    public static Idioma idioma() {
        return idioma;
    }

    /** Cambia el idioma y lo recuerda para próximos arranques. */
    public static void seleccionar(Idioma nuevo) {
        idioma = nuevo;
        textos = cargar(nuevo);
        try {
            preferencias().put(CLAVE_IDIOMA, nuevo.name());
        } catch (RuntimeException ignorado) {
            // si no se puede persistir, el cambio vale al menos para esta sesión
        }
    }

    /** Cambia el idioma sin persistirlo. Lo usan los tests. */
    public static void usarIdioma(Locale locale) {
        idioma = Idioma.desde(locale);
        textos = cargar(idioma);
    }

    /** Texto asociado a {@code clave}. */
    public static String get(String clave) {
        String valor = textos.getProperty(clave);
        if (valor == null) {
            throw new MissingResourceException(
                    "Falta la clave " + clave, Textos.class.getName(), clave);
        }
        return valor;
    }

    /** Texto de {@code clave} con {@code {0}}, {@code {1}}… sustituidos por {@code args}. */
    public static String get(String clave, Object... args) {
        return MessageFormat.format(get(clave), args);
    }
}
