package io.guillermoamadodiaz.javacalcfx.ui;

import java.util.concurrent.CancellationException;

/**
 * Traduce una excepción surgida durante un cálculo a un mensaje para el usuario.
 *
 * <p>Función pura y sin JavaFX, para poder cubrirla con tests unitarios en vez de
 * dejar la lógica enterrada en un manejador de eventos.
 */
public final class MensajesDeError {

    private MensajesDeError() {
    }

    public static String describir(Throwable ex) {
        if (ex instanceof CancellationException) {
            return ""; // el cálculo se canceló: no hay nada que mostrar
        }
        if (ex instanceof NumberFormatException) {
            return "Por favor ingresa números válidos en todos los campos.";
        }
        if (ex instanceof IllegalArgumentException || ex instanceof ArithmeticException) {
            return "Error: " + ex.getMessage();
        }
        return "Error inesperado: " + ex;
    }
}
