package io.guillermoamadodiaz.javacalcfx.ui;

import io.guillermoamadodiaz.javacalcfx.calc.Calculadora;
import io.guillermoamadodiaz.javacalcfx.i18n.Textos;

/**
 * Desarrollo paso a paso de la regla de tres directa: plantea la proporción
 * {@code a → b, c → x} y despeja {@code x = (c · b) / a} sustituyendo los datos.
 *
 * <p>Solo rellena plantillas fijas con los números; es determinista y está
 * cubierto por tests.
 */
public final class PasoAPasoReglaDeTres {

    private PasoAPasoReglaDeTres() {}

    /** @throws IllegalArgumentException si {@code a} es 0 o algún dato no es finito */
    public static String desarrollo(double a, double b, double c) {
        double x = Calculadora.reglaDeTres(a, b, c); // valida a, b, c

        StringBuilder sb = new StringBuilder();
        sb.append(Textos.get("regladetres.pasos.intro")).append('\n');
        sb.append("%s → %s\n".formatted(n(a), n(b)));
        sb.append("%s → x\n\n".formatted(n(c)));
        sb.append("x = (c · b) / a\n");
        sb.append("x = (%s · %s) / %s\n".formatted(n(c), n(b), n(a)));
        sb.append("x = %s / %s\n".formatted(n(c * b), n(a)));
        sb.append("x = %s".formatted(n(x)));
        return sb.toString();
    }

    private static String n(double valor) {
        return Formato.numero(valor);
    }
}
