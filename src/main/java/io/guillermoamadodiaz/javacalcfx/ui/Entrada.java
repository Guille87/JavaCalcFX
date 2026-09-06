package io.guillermoamadodiaz.javacalcfx.ui;

/**
 * Conversión del texto de los campos a valores numéricos.
 *
 * <p>Punto único de parseo: si en el futuro se añade un {@code TextFormatter} o
 * mensajes de error más específicos, se cambia aquí. Lanza
 * {@link NumberFormatException}, que {@link MensajesDeError} traduce.
 */
public final class Entrada {

    private Entrada() {}

    public static double doble(String texto) {
        return Double.parseDouble(texto);
    }

    public static int entero(String texto) {
        return Integer.parseInt(texto);
    }

    public static long largo(String texto) {
        return Long.parseLong(texto);
    }
}
