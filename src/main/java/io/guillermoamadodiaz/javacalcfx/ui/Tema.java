package io.guillermoamadodiaz.javacalcfx.ui;

import java.util.prefs.Preferences;
import javafx.scene.Scene;

/**
 * Tema claro/oscuro de la aplicación.
 *
 * <p>El modo oscuro se activa añadiendo la clase de estilo {@link #CLASE_OSCURO}
 * a la raíz de la escena; los colores están en {@code styles.css}. La preferencia
 * se persiste con {@link Preferences} (en un subnodo propio para que no la borre
 * la limpieza de {@link WindowState} en los tests).
 */
public final class Tema {

    /** Clase de estilo que activa el modo oscuro (ver {@code styles.css}). */
    public static final String CLASE_OSCURO = "tema-oscuro";

    private static final Preferences PREFS =
            Preferences.userNodeForPackage(Tema.class).node("tema");
    private static final String CLAVE = "oscuro";

    private Tema() {}

    /** {@code true} si el modo oscuro está activo (persistido; por defecto claro). */
    public static boolean esOscuro() {
        return PREFS.getBoolean(CLAVE, false);
    }

    /** Cambia el tema y lo persiste. Devuelve el nuevo estado ({@code true} = oscuro). */
    public static boolean alternar() {
        boolean oscuro = !esOscuro();
        PREFS.putBoolean(CLAVE, oscuro);
        return oscuro;
    }

    /** Aplica el tema actual a la escena. Idempotente; ignora {@code null}. */
    public static void aplicarA(Scene escena) {
        if (escena == null || escena.getRoot() == null) {
            return;
        }
        var clases = escena.getRoot().getStyleClass();
        clases.remove(CLASE_OSCURO);
        if (esOscuro()) {
            clases.add(CLASE_OSCURO);
        }
    }
}
