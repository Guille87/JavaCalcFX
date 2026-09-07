package io.guillermoamadodiaz.javacalcfx.ui;

import io.guillermoamadodiaz.javacalcfx.calc.Calculator;
import io.guillermoamadodiaz.javacalcfx.calc.Calculator.QuadraticEquation;
import io.guillermoamadodiaz.javacalcfx.i18n.Messages;

/**
 * Step-by-step explanation of the quadratic formula, in linear notation.
 *
 * <p>It only substitutes numbers into fixed templates and uses the classic
 * formula {@code (-b ± √Δ) / 2a} to <em>show</em> the procedure (the calculator
 * uses a more stable variant for the final result; for normal input they match).
 * It is deterministic and covered by tests.
 */
public final class QuadraticSteps {

    private QuadraticSteps() {}

    /** @throws IllegalArgumentException if {@code a == 0} or a coefficient is not finite */
    public static String explain(double a, double b, double c) {
        QuadraticEquation equation = Calculator.solveQuadratic(a, b, c); // validates a, b, c
        double disc = equation.discriminant();
        double twoA = 2 * a;

        StringBuilder sb = new StringBuilder();
        sb.append("x = (-b ± √(b² - 4ac)) / 2a\n");
        sb.append("x = (-(%s) ± √((%s)² - 4·(%s)·(%s))) / (2·(%s))\n".formatted(n(b), n(b), n(a), n(c), n(a)));
        sb.append("x = (%s ± √(%s - %s)) / %s\n".formatted(n(-b), n(b * b), n(4 * a * c), n(twoA)));
        sb.append("x = (%s ± √%s) / %s\n".formatted(n(-b), n(disc), n(twoA)));

        if (disc < 0) {
            double re = -b / twoA;
            double im = Math.sqrt(-disc) / twoA;
            sb.append(Messages.get("quadratic.steps.no.real")).append('\n');
            sb.append("x = %s / %s ± (√%s / %s)·i\n".formatted(n(-b), n(twoA), n(-disc), n(twoA)));
            sb.append("x₁ = %s + %s i\n".formatted(n(re), n(im)));
            sb.append("x₂ = %s - %s i".formatted(n(re), n(im)));
            return sb.toString();
        }

        double root = Math.sqrt(disc);
        sb.append("x = (%s ± %s) / %s\n".formatted(n(-b), n(root), n(twoA)));
        if (equation.hasDoubleRoot()) {
            sb.append("x = %s / %s = %s".formatted(n(-b), n(twoA), n(-b / twoA)));
        } else {
            sb.append("x₁ = (%s + %s) / %s = %s\n".formatted(n(-b), n(root), n(twoA), n((-b + root) / twoA)));
            sb.append("x₂ = (%s - %s) / %s = %s".formatted(n(-b), n(root), n(twoA), n((-b - root) / twoA)));
        }
        return sb.toString();
    }

    private static String n(double value) {
        return Format.number(value);
    }
}
