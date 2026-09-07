package io.guillermoamadodiaz.javacalcfx.ui;

import io.guillermoamadodiaz.javacalcfx.calc.CalculationError;
import io.guillermoamadodiaz.javacalcfx.i18n.Messages;
import java.util.List;
import java.util.concurrent.CancellationException;

/**
 * Translates an exception raised during a calculation into a message for the user.
 *
 * <p>A pure, JavaFX-free function, so it can be covered by unit tests instead of
 * leaving the logic buried in an event handler. This is where the key carried by
 * {@link CalculationError} becomes translated text.
 */
public final class ErrorMessages {

    private ErrorMessages() {}

    public static String describe(Throwable ex) {
        if (ex instanceof CancellationException) {
            return ""; // the calculation was cancelled: nothing to show
        }
        if (ex instanceof NumberFormatException) {
            return Messages.get("error.invalid.numbers");
        }
        if (ex instanceof CalculationError error) {
            return Messages.get("error.prefix", Messages.get(error.key(), translateArguments(error.arguments())));
        }
        if (ex instanceof IllegalArgumentException || ex instanceof ArithmeticException) {
            return Messages.get("error.prefix", ex.getMessage());
        }
        return Messages.get("error.unexpected", String.valueOf(ex));
    }

    /** Translates the arguments that are themselves text keys ({@link CalculationError.Name}). */
    private static Object[] translateArguments(List<Object> arguments) {
        return arguments.stream()
                .map(arg -> arg instanceof CalculationError.Name name ? Messages.get(name.key()) : arg)
                .toArray();
    }
}
