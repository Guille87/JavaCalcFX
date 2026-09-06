package io.guillermoamadodiaz.javacalcfx.ui;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

/**
 * Restringe un {@link TextField} a texto numérico <em>mientras</em> se teclea.
 *
 * <p>Dos variantes: {@link Tipo#ENTERO} (signo opcional y dígitos) y
 * {@link Tipo#DECIMAL} (además, un único separador decimal '.'). Ambas dejan pasar
 * estados intermedios ("", "-", y "." o "-." en el caso decimal) para no bloquear
 * la escritura; la validación de dominio y el parseo definitivo siguen en
 * {@code Calculadora} y {@link Entrada}. La comprobación ({@link #esValido}) es
 * pura y está cubierta por tests.
 */
public final class FiltroNumerico {

    /** Clase de número que admite un campo. */
    public enum Tipo {
        ENTERO(Pattern.compile("-?\\d*")),
        DECIMAL(Pattern.compile("-?\\d*\\.?\\d*"));

        private final Pattern patron;

        Tipo(Pattern patron) {
            this.patron = patron;
        }
    }

    private FiltroNumerico() {}

    /** {@code true} si {@code texto} es un prefijo válido de un número del tipo dado. */
    public static boolean esValido(String texto, Tipo tipo) {
        return tipo.patron.matcher(texto).matches();
    }

    /** Instala el filtro en el campo. */
    public static void aplicarA(TextField campo, Tipo tipo) {
        UnaryOperator<TextFormatter.Change> filtro =
                cambio -> esValido(cambio.getControlNewText(), tipo) ? cambio : null;
        campo.setTextFormatter(new TextFormatter<>(filtro));
    }
}
