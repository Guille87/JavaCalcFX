package io.guillermoamadodiaz.javacalcfx.ui;

import io.guillermoamadodiaz.javacalcfx.calc.Calculator;
import io.guillermoamadodiaz.javacalcfx.i18n.Messages;

/**
 * Step-by-step explanation of the percentage of an amount: substitutes the data
 * into {@code x = (p / 100) · c}.
 *
 * <p>It only fills fixed templates with the numbers; it is deterministic and
 * covered by tests.
 */
public final class PercentageSteps {

    private PercentageSteps() {}

    /** @throws IllegalArgumentException if a value is not a finite number */
    public static String explain(double percentage, double amount) {
        double x = Calculator.percentageOf(percentage, amount); // validates the data

        StringBuilder sb = new StringBuilder();
        sb.append(Messages.get("porcentaje.pasos.intro")).append('\n');
        sb.append("x = (p / 100) · c\n");
        sb.append("x = (%s / 100) · %s\n".formatted(n(percentage), n(amount)));
        sb.append("x = %s · %s\n".formatted(n(percentage / 100.0), n(amount)));
        sb.append("x = %s".formatted(n(x)));
        return sb.toString();
    }

    private static String n(double value) {
        return Format.number(value);
    }
}
