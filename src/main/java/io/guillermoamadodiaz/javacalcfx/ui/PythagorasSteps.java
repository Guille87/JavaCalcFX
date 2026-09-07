package io.guillermoamadodiaz.javacalcfx.ui;

import io.guillermoamadodiaz.javacalcfx.calc.Calculator;
import io.guillermoamadodiaz.javacalcfx.calc.Calculator.Triangle;
import io.guillermoamadodiaz.javacalcfx.i18n.Messages;

/**
 * Step-by-step explanation of the right triangle, in linear notation: applies the
 * Pythagorean theorem to the hypotenuse and substitutes the data into the
 * formulas for the area, the perimeter and the acute angles.
 *
 * <p>It only fills fixed templates with the numbers; it is deterministic and
 * covered by tests.
 */
public final class PythagorasSteps {

    private PythagorasSteps() {}

    /** @throws IllegalArgumentException if a leg is not a finite, positive number */
    public static String explain(double a, double b) {
        Triangle t = Calculator.solveRightTriangle(a, b); // validates a and b

        StringBuilder sb = new StringBuilder();
        sb.append(Messages.get("pythagoras.steps.hypotenuse")).append('\n');
        sb.append("h = √(a² + b²)\n");
        sb.append("h = √((%s)² + (%s)²)\n".formatted(n(a), n(b)));
        sb.append("h = √(%s + %s)\n".formatted(n(a * a), n(b * b)));
        sb.append("h = √%s\n".formatted(n(a * a + b * b)));
        sb.append("h = %s\n\n".formatted(n(t.hypotenuse())));

        sb.append(Messages.get("pythagoras.steps.area")).append('\n');
        sb.append("A = (a · b) / 2 = (%s · %s) / 2 = %s\n\n".formatted(n(a), n(b), n(t.area())));

        sb.append(Messages.get("pythagoras.steps.perimeter")).append('\n');
        sb.append("P = a + b + h = %s + %s + %s = %s\n\n".formatted(n(a), n(b), n(t.hypotenuse()), n(t.perimeter())));

        sb.append(Messages.get("pythagoras.steps.angles")).append('\n');
        sb.append("α = arctan(b / a) = arctan(%s / %s) ≅ %s°\n".formatted(n(b), n(a), n(t.angleAlpha())));
        sb.append("β = 90° − α ≅ %s°".formatted(n(t.angleBeta())));
        return sb.toString();
    }

    private static String n(double value) {
        return Format.number(value);
    }
}
