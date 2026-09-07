package io.guillermoamadodiaz.javacalcfx.ui;

import io.guillermoamadodiaz.javacalcfx.calc.Calculator;
import io.guillermoamadodiaz.javacalcfx.i18n.Messages;

/**
 * Step-by-step explanation of the direct rule of three: sets up the proportion
 * {@code a → b, c → x} and solves {@code x = (c · b) / a} by substituting the
 * data.
 *
 * <p>It only fills fixed templates with the numbers; it is deterministic and
 * covered by tests.
 */
public final class RuleOfThreeSteps {

    private RuleOfThreeSteps() {}

    /** @throws IllegalArgumentException if {@code a} is 0 or a value is not finite */
    public static String explain(double a, double b, double c) {
        double x = Calculator.ruleOfThree(a, b, c); // validates a, b, c

        StringBuilder sb = new StringBuilder();
        sb.append(Messages.get("ruleofthree.steps.intro")).append('\n');
        sb.append("%s → %s\n".formatted(n(a), n(b)));
        sb.append("%s → x\n\n".formatted(n(c)));
        sb.append("x = (c · b) / a\n");
        sb.append("x = (%s · %s) / %s\n".formatted(n(c), n(b), n(a)));
        sb.append("x = %s / %s\n".formatted(n(c * b), n(a)));
        sb.append("x = %s".formatted(n(x)));
        return sb.toString();
    }

    private static String n(double value) {
        return Format.number(value);
    }
}
