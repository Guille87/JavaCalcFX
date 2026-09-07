package io.guillermoamadodiaz.javacalcfx.ui;

import io.guillermoamadodiaz.javacalcfx.calc.CalculationError;
import io.guillermoamadodiaz.javacalcfx.i18n.Textos;
import java.util.List;
import java.util.concurrent.CancellationException;

/**
 * Traduce una excepción surgida durante un cálculo a un mensaje para el usuario.
 *
 * <p>Función pura y sin JavaFX, para poder cubrirla con tests unitarios en vez de
 * dejar la lógica enterrada en un manejador de eventos. Es aquí donde la clave
 * que trae {@link CalculationError} se convierte en texto traducido.
 */
public final class MensajesDeError {

    private MensajesDeError() {}

    public static String describir(Throwable ex) {
        if (ex instanceof CancellationException) {
            return ""; // el cálculo se canceló: no hay nada que mostrar
        }
        if (ex instanceof NumberFormatException) {
            return Textos.get("error.numeros.invalidos");
        }
        if (ex instanceof CalculationError error) {
            return Textos.get("error.prefijo", Textos.get(error.key(), traducirArgumentos(error.arguments())));
        }
        if (ex instanceof IllegalArgumentException || ex instanceof ArithmeticException) {
            return Textos.get("error.prefijo", ex.getMessage());
        }
        return Textos.get("error.inesperado", String.valueOf(ex));
    }

    /** Traduce los argumentos que son a su vez claves de texto ({@link CalculationError.Name}). */
    private static Object[] traducirArgumentos(List<Object> argumentos) {
        return argumentos.stream()
                .map(arg -> arg instanceof CalculationError.Name nombre ? Textos.get(nombre.key()) : arg)
                .toArray();
    }
}
