package io.guillermoamadodiaz.javacalcfx.ui;

import io.guillermoamadodiaz.javacalcfx.calc.Calculator;
import io.guillermoamadodiaz.javacalcfx.i18n.Messages;

/**
 * Desarrollo paso a paso del porcentaje de una cantidad: sustituye los datos en
 * {@code x = (p / 100) · c}.
 *
 * <p>Solo rellena plantillas fijas con los números; es determinista y está
 * cubierto por tests.
 */
public final class PasoAPasoPorcentaje {

    private PasoAPasoPorcentaje() {}

    /** @throws IllegalArgumentException si algún dato no es un número finito */
    public static String desarrollo(double porcentaje, double cantidad) {
        double x = Calculator.percentageOf(porcentaje, cantidad); // valida los datos

        StringBuilder sb = new StringBuilder();
        sb.append(Messages.get("porcentaje.pasos.intro")).append('\n');
        sb.append("x = (p / 100) · c\n");
        sb.append("x = (%s / 100) · %s\n".formatted(n(porcentaje), n(cantidad)));
        sb.append("x = %s · %s\n".formatted(n(porcentaje / 100.0), n(cantidad)));
        sb.append("x = %s".formatted(n(x)));
        return sb.toString();
    }

    private static String n(double valor) {
        return Format.number(valor);
    }
}
