package io.guillermoamadodiaz.javacalcfx.ui;

import io.guillermoamadodiaz.javacalcfx.calc.Calculator;
import io.guillermoamadodiaz.javacalcfx.calc.Calculator.Triangle;
import io.guillermoamadodiaz.javacalcfx.i18n.Messages;

/**
 * Desarrollo paso a paso del triángulo rectángulo, en notación lineal: aplica el
 * teorema de Pitágoras a la hipotenusa y sustituye los datos en las fórmulas del
 * área, el perímetro y los ángulos agudos.
 *
 * <p>Solo rellena plantillas fijas con los números; es determinista y está
 * cubierto por tests.
 */
public final class PasoAPasoPitagoras {

    private PasoAPasoPitagoras() {}

    /** @throws IllegalArgumentException si algún cateto no es un número finito y positivo */
    public static String desarrollo(double a, double b) {
        Triangle t = Calculator.solveRightTriangle(a, b); // valida a y b

        StringBuilder sb = new StringBuilder();
        sb.append(Messages.get("pitagoras.pasos.hipotenusa")).append('\n');
        sb.append("h = √(a² + b²)\n");
        sb.append("h = √((%s)² + (%s)²)\n".formatted(n(a), n(b)));
        sb.append("h = √(%s + %s)\n".formatted(n(a * a), n(b * b)));
        sb.append("h = √%s\n".formatted(n(a * a + b * b)));
        sb.append("h = %s\n\n".formatted(n(t.hypotenuse())));

        sb.append(Messages.get("pitagoras.pasos.area")).append('\n');
        sb.append("A = (a · b) / 2 = (%s · %s) / 2 = %s\n\n".formatted(n(a), n(b), n(t.area())));

        sb.append(Messages.get("pitagoras.pasos.perimetro")).append('\n');
        sb.append("P = a + b + h = %s + %s + %s = %s\n\n".formatted(n(a), n(b), n(t.hypotenuse()), n(t.perimeter())));

        sb.append(Messages.get("pitagoras.pasos.angulos")).append('\n');
        sb.append("α = arctan(b / a) = arctan(%s / %s) ≅ %s°\n".formatted(n(b), n(a), n(t.angleAlpha())));
        sb.append("β = 90° − α ≅ %s°".formatted(n(t.angleBeta())));
        return sb.toString();
    }

    private static String n(double valor) {
        return Format.number(valor);
    }
}
