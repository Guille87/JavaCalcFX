package io.guillermoamadodiaz.javacalcfx.ui;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

/**
 * Restricts a {@link TextField} to numeric text <em>while</em> typing.
 *
 * <p>Two variants: {@link Type#INTEGER} (optional sign and digits) and
 * {@link Type#DECIMAL} (plus a single '.' decimal separator). Both let
 * intermediate states through ("", "-", and "." or "-." for the decimal case) so
 * typing is not blocked; domain validation and final parsing still happen in
 * {@code Calculator} and {@link Input}. The check ({@link #isValid}) is pure and
 * covered by tests.
 */
public final class NumericFilter {

    /** The class of number a field accepts. */
    public enum Type {
        INTEGER(Pattern.compile("-?\\d*")),
        DECIMAL(Pattern.compile("-?\\d*\\.?\\d*"));

        private final Pattern pattern;

        Type(Pattern pattern) {
            this.pattern = pattern;
        }
    }

    private NumericFilter() {}

    /** {@code true} if {@code text} is a valid prefix of a number of the given type. */
    public static boolean isValid(String text, Type type) {
        return type.pattern.matcher(text).matches();
    }

    /** Installs the filter on the field. */
    public static void applyTo(TextField field, Type type) {
        UnaryOperator<TextFormatter.Change> filter =
                change -> isValid(change.getControlNewText(), type) ? change : null;
        field.setTextFormatter(new TextFormatter<>(filter));
    }
}
