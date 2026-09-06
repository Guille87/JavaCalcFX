package io.guillermoamadodiaz.javacalcfx.ui;

import java.util.Optional;
import java.util.prefs.Preferences;

/**
 * Recuerda cuál fue la última calculadora abierta para volver a ella al arrancar.
 *
 * <p>Guarda la clave de la calculadora en un subnodo propio de
 * {@link Preferences}. Al volver al menú se {@link #olvidar() olvida}, de modo
 * que la app abre en la última <em>pantalla</em> (una calculadora si se cerró en
 * una, el menú si se cerró en el menú).
 */
public final class UltimaCalculadora {

    private static final Preferences PREFS =
            Preferences.userNodeForPackage(UltimaCalculadora.class).node("ultima-calculadora");
    private static final String CLAVE = "clave";

    private UltimaCalculadora() {}

    /** Recuerda la calculadora {@code clave}. */
    public static void recordar(String clave) {
        try {
            PREFS.put(CLAVE, clave);
        } catch (RuntimeException ignorado) {
            // sin preferencias disponibles: nada que recordar
        }
    }

    /** La clave de la última calculadora abierta, si hay alguna recordada. */
    public static Optional<String> recordada() {
        try {
            return Optional.ofNullable(PREFS.get(CLAVE, null));
        } catch (RuntimeException ignorado) {
            return Optional.empty();
        }
    }

    /** Olvida la calculadora recordada (se llama al volver al menú). */
    public static void olvidar() {
        try {
            PREFS.remove(CLAVE);
        } catch (RuntimeException ignorado) {
            // sin preferencias disponibles: nada que olvidar
        }
    }
}
