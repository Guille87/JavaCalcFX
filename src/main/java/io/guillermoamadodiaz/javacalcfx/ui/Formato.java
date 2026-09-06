package io.guillermoamadodiaz.javacalcfx.ui;

import java.math.BigInteger;
import java.text.DecimalFormat;

/**
 * Formateo de resultados para mostrar en pantalla.
 *
 * <p>Sin dependencias de JavaFX: recibe valores numéricos y devuelve texto listo
 * para una etiqueta. {@link DecimalFormat} no es seguro entre hilos, así que cada
 * llamada crea su propia instancia (el coste es irrelevante: una por pulsación).
 */
public final class Formato {

    private static final String PATRON_DECIMAL = "#,##0.####";

    private Formato() {
    }

    /** Número con separador de miles y hasta 4 decimales (sin ceros finales). */
    public static String numero(double valor) {
        return new DecimalFormat(PATRON_DECIMAL).format(valor);
    }

    /** Número con exactamente dos decimales (p. ej. una nota media). */
    public static String dosDecimales(double valor) {
        return String.format("%.2f", valor);
    }

    /** Entero grande con separador de miles. */
    public static String enteroGrande(BigInteger valor) {
        return String.format("%,d", valor);
    }
}
