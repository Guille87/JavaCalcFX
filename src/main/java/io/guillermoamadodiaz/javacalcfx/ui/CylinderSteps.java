package io.guillermoamadodiaz.javacalcfx.ui;

import io.guillermoamadodiaz.javacalcfx.calc.Calculator;
import io.guillermoamadodiaz.javacalcfx.i18n.Messages;

/**
 * Step-by-step explanation of a cylinder's total surface area, in linear
 * notation: substitutes the radius and height into {@code A = 2·π·r·(r + h)} and
 * breaks down the contribution of the two bases ({@code 2·π·r²}) and of the
 * lateral surface ({@code 2·π·r·h}).
 *
 * <p>It only fills fixed templates with the numbers; it is deterministic and
 * covered by tests.
 */
public final class CylinderSteps {

    private CylinderSteps() {}

    /** @throws IllegalArgumentException if the radius or height are not finite and &ge; 0 */
    public static String explain(double radius, double height) {
        double area = Calculator.cylinderArea(radius, height); // validates radius and height
        double bases = 2 * Math.PI * radius * radius;
        double lateral = 2 * Math.PI * radius * height;

        StringBuilder sb = new StringBuilder();
        sb.append(Messages.get("cylinder.steps.formula")).append('\n');
        sb.append("A = 2·π·r·(r + h)\n");
        sb.append("A = 2·π·(%s)·((%s) + (%s))\n".formatted(n(radius), n(radius), n(height)));
        sb.append("A = 2·π·(%s)·(%s)\n".formatted(n(radius), n(radius + height)));
        sb.append("A ≅ %s\n\n".formatted(n(area)));

        sb.append(Messages.get("cylinder.steps.breakdown")).append('\n');
        sb.append("bases   = 2·π·(%s)² ≅ %s\n".formatted(n(radius), n(bases)));
        sb.append("lateral = 2·π·(%s)·(%s) ≅ %s".formatted(n(radius), n(height), n(lateral)));
        return sb.toString();
    }

    private static String n(double value) {
        return Format.number(value);
    }
}
