package io.guillermoamadodiaz.javacalcfx.ui;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Formateo de resultados para mostrar en pantalla.
 *
 * <p>Sin dependencias de JavaFX: recibe valores numéricos y devuelve texto listo
 * para una etiqueta. {@link DecimalFormat} no es seguro entre hilos, así que cada
 * llamada crea su propia instancia (el coste es irrelevante: una por pulsación).
 *
 * <p>Los números se formatean siempre con {@link Locale#ROOT} (punto decimal,
 * coma para los miles), coherente con la entrada: el filtro numérico solo admite
 * el punto. Así el resultado no depende del idioma del sistema.
 */
public final class Formato {

    private static final String PATRON_DECIMAL = "#,##0.####";
    private static final DecimalFormatSymbols SIMBOLOS = DecimalFormatSymbols.getInstance(Locale.ROOT);

    private Formato() {}

    /** Número con separador de miles y hasta 4 decimales (sin ceros finales). */
    public static String numero(double valor) {
        return new DecimalFormat(PATRON_DECIMAL, SIMBOLOS).format(normalizarCero(valor));
    }

    /** Número con exactamente dos decimales (p. ej. una nota media). */
    public static String dosDecimales(double valor) {
        return String.format(Locale.ROOT, "%.2f", normalizarCero(valor));
    }

    /** Convierte -0.0 en 0.0 para no mostrar «-0». */
    private static double normalizarCero(double valor) {
        return valor + 0.0;
    }

    /** Entero con separador de miles. */
    public static String entero(long valor) {
        return String.format(Locale.ROOT, "%,d", valor);
    }

    /** Entero grande con separador de miles. */
    public static String enteroGrande(BigInteger valor) {
        return String.format(Locale.ROOT, "%,d", valor);
    }
}
