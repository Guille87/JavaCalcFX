package io.guillermoamadodiaz.javacalcfx.ui;

import io.guillermoamadodiaz.javacalcfx.calc.ErrorDeCalculo;
import io.guillermoamadodiaz.javacalcfx.i18n.Textos;
import java.util.List;
import java.util.concurrent.CancellationException;

/**
 * Traduce una excepción surgida durante un cálculo a un mensaje para el usuario.
 *
 * <p>Función pura y sin JavaFX, para poder cubrirla con tests unitarios en vez de
 * dejar la lógica enterrada en un manejador de eventos. Es aquí donde la clave
 * que trae {@link ErrorDeCalculo} se convierte en texto traducido.
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
        if (ex instanceof ErrorDeCalculo error) {
            return Textos.get("error.prefijo", Textos.get(error.clave(), traducirArgumentos(error.argumentos())));
        }
        if (ex instanceof IllegalArgumentException || ex instanceof ArithmeticException) {
            return Textos.get("error.prefijo", ex.getMessage());
        }
        return Textos.get("error.inesperado", String.valueOf(ex));
    }

    /** Traduce los argumentos que son a su vez claves de texto ({@link ErrorDeCalculo.Nombre}). */
    private static Object[] traducirArgumentos(List<Object> argumentos) {
        return argumentos.stream()
                .map(arg -> arg instanceof ErrorDeCalculo.Nombre nombre ? Textos.get(nombre.clave()) : arg)
                .toArray();
    }
}
