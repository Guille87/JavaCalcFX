package io.guillermoamadodiaz.javacalcfx.ui;

import io.guillermoamadodiaz.javacalcfx.calc.Calculator;
import io.guillermoamadodiaz.javacalcfx.calc.Calculator.QuadraticEquation;
import io.guillermoamadodiaz.javacalcfx.i18n.Messages;

/**
 * Desarrollo paso a paso de la fórmula cuadrática, en notación lineal.
 *
 * <p>Solo sustituye números en plantillas fijas y usa la fórmula clásica
 * {@code (-b ± √Δ) / 2a} para <em>mostrar</em> el procedimiento (la calculadora
 * usa una variante más estable para el resultado final; para entradas normales
 * coinciden). Es determinista y está cubierto por tests.
 */
public final class PasoAPasoCuadratica {

    private PasoAPasoCuadratica() {}

    /** @throws IllegalArgumentException si {@code a == 0} o algún coeficiente no es finito */
    public static String desarrollo(double a, double b, double c) {
        QuadraticEquation ecuacion = Calculator.solveQuadratic(a, b, c); // valida a, b, c
        double disc = ecuacion.discriminant();
        double dosA = 2 * a;

        StringBuilder sb = new StringBuilder();
        sb.append("x = (-b ± √(b² - 4ac)) / 2a\n");
        sb.append("x = (-(%s) ± √((%s)² - 4·(%s)·(%s))) / (2·(%s))\n".formatted(n(b), n(b), n(a), n(c), n(a)));
        sb.append("x = (%s ± √(%s - %s)) / %s\n".formatted(n(-b), n(b * b), n(4 * a * c), n(dosA)));
        sb.append("x = (%s ± √%s) / %s\n".formatted(n(-b), n(disc), n(dosA)));

        if (disc < 0) {
            double re = -b / dosA;
            double im = Math.sqrt(-disc) / dosA;
            sb.append(Messages.get("cuadratica.pasos.sin.reales")).append('\n');
            sb.append("x = %s / %s ± (√%s / %s)·i\n".formatted(n(-b), n(dosA), n(-disc), n(dosA)));
            sb.append("x₁ = %s + %s i\n".formatted(n(re), n(im)));
            sb.append("x₂ = %s - %s i".formatted(n(re), n(im)));
            return sb.toString();
        }

        double raiz = Math.sqrt(disc);
        sb.append("x = (%s ± %s) / %s\n".formatted(n(-b), n(raiz), n(dosA)));
        if (ecuacion.hasDoubleRoot()) {
            sb.append("x = %s / %s = %s".formatted(n(-b), n(dosA), n(-b / dosA)));
        } else {
            sb.append("x₁ = (%s + %s) / %s = %s\n".formatted(n(-b), n(raiz), n(dosA), n((-b + raiz) / dosA)));
            sb.append("x₂ = (%s - %s) / %s = %s".formatted(n(-b), n(raiz), n(dosA), n((-b - raiz) / dosA)));
        }
        return sb.toString();
    }

    private static String n(double valor) {
        return Format.number(valor);
    }
}
