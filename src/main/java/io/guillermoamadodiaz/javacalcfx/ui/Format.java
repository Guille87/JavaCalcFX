package io.guillermoamadodiaz.javacalcfx.ui;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Formats results for on-screen display.
 *
 * <p>No JavaFX dependencies: it takes numeric values and returns text ready for a
 * label. {@link DecimalFormat} is not thread-safe, so every call builds its own
 * instance (the cost is irrelevant: one per keystroke).
 *
 * <p>Numbers are always formatted with {@link Locale#ROOT} (dot decimal, comma
 * thousands), consistent with the input: the numeric filter only accepts the dot.
 * That way the result does not depend on the system language.
 */
public final class Format {

    private static final String DECIMAL_PATTERN = "#,##0.####";
    private static final DecimalFormatSymbols SYMBOLS = DecimalFormatSymbols.getInstance(Locale.ROOT);

    private Format() {}

    /** Number with thousands separator and up to 4 decimals (no trailing zeros). */
    public static String number(double value) {
        return new DecimalFormat(DECIMAL_PATTERN, SYMBOLS).format(normalizeZero(value));
    }

    /** Number with exactly two decimals (e.g. an average grade). */
    public static String twoDecimals(double value) {
        return String.format(Locale.ROOT, "%.2f", normalizeZero(value));
    }

    /** Turns -0.0 into 0.0 so "-0" is never shown. */
    private static double normalizeZero(double value) {
        return value + 0.0;
    }

    /** Integer with thousands separator. */
    public static String integer(long value) {
        return String.format(Locale.ROOT, "%,d", value);
    }

    /** Big integer with thousands separator. */
    public static String bigInteger(BigInteger value) {
        return String.format(Locale.ROOT, "%,d", value);
    }
}
