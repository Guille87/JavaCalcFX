package io.guillermoamadodiaz.javacalcfx.calc;

import java.util.List;

/**
 * Error de dominio de {@link Calculadora}: la entrada incumple una precondición.
 *
 * <p>Lleva la <em>clave</em> del mensaje y sus argumentos, no el texto ya
 * traducido: así el paquete {@code calc} no depende de la capa de textos. Es la
 * interfaz quien lo traduce (ver {@code ui/MensajesDeError}). Extiende
 * {@link IllegalArgumentException} por compatibilidad con quien capture ese tipo.
 */
public final class ErrorDeCalculo extends IllegalArgumentException {

    private final transient List<Object> argumentos;

    /**
     * @param clave clave del mensaje en el catálogo de textos
     * @param argumentos valores para sustituir en {@code {0}}, {@code {1}}…; un
     *     argumento de tipo {@link Nombre} se traduce antes de sustituirlo
     */
    public ErrorDeCalculo(String clave, Object... argumentos) {
        super(clave);
        this.argumentos = List.of(argumentos);
    }

    /** Clave del mensaje en el catálogo de textos. */
    public String clave() {
        return getMessage();
    }

    /** Argumentos a sustituir en el mensaje. */
    public List<Object> argumentos() {
        return argumentos;
    }

    /**
     * Un argumento que es a su vez una clave de texto —el nombre de un dato,
     * p. ej. «el radio»— y que la capa de textos debe traducir antes de
     * sustituirlo en el mensaje.
     */
    public record Nombre(String clave) {}
}
