package io.guillermoamadodiaz.javacalcfx.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

/**
 * Historial de los últimos cálculos: el título de la pantalla y el resultado que
 * se mostró. El más reciente va primero y se conservan como mucho {@link #MAXIMO}.
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
    private static final String SEPARADOR_CAMPO = "\u001f"; // US: entre título y resultado

    private static final List<Entrada> entradas = new ArrayList<>(cargar());

    private Historial() {}

    /** Un cálculo del historial. */
    public record Entrada(String titulo, String resultado) {}

    /** Los cálculos guardados, del más reciente al más antiguo. */
    public static synchronized List<Entrada> reciente() {
        return List.copyOf(entradas);
    }

    /** Registra un cálculo (lo pone el primero) y lo persiste. */
    public static synchronized void registrar(String titulo, String resultado) {
        entradas.add(0, new Entrada(titulo, recortar(resultado)));
        while (entradas.size() > MAXIMO) {
            entradas.remove(entradas.size() - 1);
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
                    if (campos.length == 2) {
                        lista.add(new Entrada(campos[0], campos[1]));
                    }
                }
            }
        } catch (RuntimeException ignorado) {
            // preferencias no disponibles o valor corrupto: se empieza sin historial
        }
        return lista;
    }

    private static void guardar() {
        try {
            PREFS.put(
                    CLAVE,
                    entradas.stream()
                            .map(e -> e.titulo() + SEPARADOR_CAMPO + e.resultado())
                            .collect(Collectors.joining(SEPARADOR_ENTRADA)));
        } catch (RuntimeException ignorado) {
            // si no se puede persistir (o no cabe), el historial vale para esta sesión
        }
    }
}
