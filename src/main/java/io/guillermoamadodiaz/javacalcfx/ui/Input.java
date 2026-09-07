package io.guillermoamadodiaz.javacalcfx.ui;

/**
 * Converts field text into numeric values.
 *
 * <p>Single parsing seam: if a {@code TextFormatter} or more specific error
 * messages are added later, they change here. Throws
 * {@link NumberFormatException}, which {@link ErrorMessages} translates.
 */
public final class Input {

    private Input() {}

    public static double asDouble(String text) {
        return Double.parseDouble(text);
    }

    public static int asInt(String text) {
        return Integer.parseInt(text);
    }

    public static long asLong(String text) {
        return Long.parseLong(text);
    }
}
