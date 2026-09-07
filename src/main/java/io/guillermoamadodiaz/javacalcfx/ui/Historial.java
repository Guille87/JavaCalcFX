package io.guillermoamadodiaz.javacalcfx.ui;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

/**
 * Historial de los últimos cálculos: el título de la pantalla, el resultado que
 * se mostró y cuándo. El más reciente va primero y se conservan como mucho
 * {@link #MAXIMO}. Repetir el mismo cálculo (mismo título y resultado) no añade
 * una entrada nueva: solo actualiza la hora de la que ya está arriba.
 *
 * <p>Se guarda entre sesiones con {@link Preferences} en un subnodo propio, como
 * una sola cadena (entradas separadas por {@code RS}, campos por {@code US}). El
 * resultado se recorta a {@link #MAX_RESULTADO} caracteres para no desbordar el
 * límite de tamaño de las preferencias con, p. ej., un factorial enorme.
 */
public final class Historial {

    /** Número máximo de cálculos que se recuerdan. */
    public static final int MAXIMO = 25;

    /** Longitud máxima del resultado que se guarda por entrada. */
    public static final int MAX_RESULTADO = 300;

    private static final Preferences PREFS =
            Preferences.userNodeForPackage(Historial.class).node("historial");
    private static final String CLAVE = "entradas";
    private static final String SEPARADOR_ENTRADA = "\u001e"; // RS: entre entradas
    private static final String SEPARADOR_CAMPO = "\u001f"; // US: entre campos

    private static final List<Entrada> entradas = new ArrayList<>(cargar());

    private Historial() {}

    /**
     * Un cálculo del historial. {@code momento} puede ser {@code null} en entradas
     * guardadas por versiones anteriores.
     */
    public record Entrada(String titulo, String resultado, Instant momento) {}

    /** Los cálculos guardados, del más reciente al más antiguo. */
    public static synchronized List<Entrada> reciente() {
        return List.copyOf(entradas);
    }

    /** Registra un cálculo (lo pone el primero) y lo persiste. */
    public static synchronized void registrar(String titulo, String resultado) {
        Entrada nueva = new Entrada(titulo, recortar(resultado), Instant.now());
        if (!entradas.isEmpty()
                && entradas.get(0).titulo().equals(nueva.titulo())
                && entradas.get(0).resultado().equals(nueva.resultado())) {
            entradas.set(0, nueva); // mismo cálculo repetido: solo se actualiza la hora
        } else {
            entradas.add(0, nueva);
            while (entradas.size() > MAXIMO) {
                entradas.remove(entradas.size() - 1);
            }
        }
        guardar();
    }

    /** Vacía el historial. */
    public static synchronized void limpiar() {
        entradas.clear();
        guardar();
    }

    private static String recortar(String texto) {
        return texto.length() <= MAX_RESULTADO ? texto : texto.substring(0, MAX_RESULTADO) + "…";
    }

    private static List<Entrada> cargar() {
        List<Entrada> lista = new ArrayList<>();
        try {
            String crudo = PREFS.get(CLAVE, "");
            if (!crudo.isEmpty()) {
                for (String trozo : crudo.split(SEPARADOR_ENTRADA, -1)) {
                    String[] campos = trozo.split(SEPARADOR_CAMPO, -1);
                    if (campos.length >= 2) {
                        lista.add(new Entrada(campos[0], campos[1], momentoDe(campos)));
                    }
                }
            }
        } catch (RuntimeException ignorado) {
            // preferencias no disponibles o valor corrupto: se empieza sin historial
        }
        return lista;
    }

    private static Instant momentoDe(String[] campos) {
        if (campos.length < 3 || campos[2].isEmpty()) {
            return null; // entrada de una versión anterior, sin fecha
        }
        try {
            return Instant.ofEpochMilli(Long.parseLong(campos[2]));
        } catch (NumberFormatException corrupto) {
            return null;
        }
    }

    private static void guardar() {
        try {
            PREFS.put(
                    CLAVE, entradas.stream().map(Historial::serializar).collect(Collectors.joining(SEPARADOR_ENTRADA)));
        } catch (RuntimeException ignorado) {
            // si no se puede persistir (o no cabe), el historial vale para esta sesión
        }
    }

    private static String serializar(Entrada entrada) {
        String millis =
                entrada.momento() == null ? "" : Long.toString(entrada.momento().toEpochMilli());
        return entrada.titulo() + SEPARADOR_CAMPO + entrada.resultado() + SEPARADOR_CAMPO + millis;
    }
}
