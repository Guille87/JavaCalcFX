package io.guillermoamadodiaz.javacalcfx.calc;

import java.util.List;

/**
 * Domain error of {@link Calculator}: the input violates a precondition.
 *
 * <p>It carries the message <em>key</em> and its arguments, not the already
 * translated text: that way the {@code calc} package does not depend on the
 * message layer. The UI translates it (see {@code ui/ErrorMessages}). Extends
 * {@link IllegalArgumentException} for compatibility with callers that catch
 * that type.
 */
public final class CalculationError extends IllegalArgumentException {

    private final transient List<Object> arguments;

    /**
     * @param key message key in the text catalog
     * @param arguments values to substitute into {@code {0}}, {@code {1}}…; an
     *     argument of type {@link Name} is translated before being substituted
     */
    public CalculationError(String key, Object... arguments) {
        super(key);
        this.arguments = List.of(arguments);
    }

    /** Message key in the text catalog. */
    public String key() {
        return getMessage();
    }

    /** Arguments to substitute into the message. */
    public List<Object> arguments() {
        return arguments;
    }

    /**
     * An argument that is itself a text key — the name of a value, e.g. "the
     * radius" — which the text layer must translate before substituting it into
     * the message.
     */
    public record Name(String key) {}
}
