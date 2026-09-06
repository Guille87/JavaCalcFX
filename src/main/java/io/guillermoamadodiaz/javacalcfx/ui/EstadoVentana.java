package io.guillermoamadodiaz.javacalcfx.ui;

import java.util.prefs.Preferences;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Guarda y restaura el tamaño y la posición de la ventana entre sesiones
 * ({@link java.util.prefs}). Descarta valores por debajo del mínimo o que
 * dejarían la ventana fuera de toda pantalla (p. ej. tras desconectar un monitor).
 */
public final class EstadoVentana {

    private static final Preferences PREFS = Preferences.userNodeForPackage(EstadoVentana.class);
    private static final double NADA = Double.NaN;

    private EstadoVentana() {}

    /** Aplica el estado guardado, si lo hay y es válido. Llamar antes de {@code show()}. */
    public static void restaurar(Stage ventana) {
        double w = PREFS.getDouble("win.w", NADA);
        double h = PREFS.getDouble("win.h", NADA);
        if (tamanoValido(w, h, ventana.getMinWidth(), ventana.getMinHeight())) {
            ventana.setWidth(w);
            ventana.setHeight(h);
        }

        double x = PREFS.getDouble("win.x", NADA);
        double y = PREFS.getDouble("win.y", NADA);
        if (puntoVisible(x, y)) {
            ventana.setX(x);
            ventana.setY(y);
        }

        if (PREFS.getBoolean("win.max", false)) {
            ventana.setMaximized(true);
        }
    }

    /** Empieza a persistir los cambios de tamaño/posición. Llamar después de {@code show()}. */
    public static void vigilar(Stage ventana) {
        Runnable guardar = () -> guardarSiNoMaximizada(ventana);
        ventana.widthProperty().addListener((o, a, b) -> guardar.run());
        ventana.heightProperty().addListener((o, a, b) -> guardar.run());
        ventana.xProperty().addListener((o, a, b) -> guardar.run());
        ventana.yProperty().addListener((o, a, b) -> guardar.run());
        ventana.maximizedProperty().addListener((o, a, b) -> PREFS.putBoolean("win.max", b));
    }

    private static void guardarSiNoMaximizada(Stage ventana) {
        if (ventana.isMaximized() || ventana.isIconified()) {
            return; // conservar el tamaño "normal", no el de maximizado/minimizado
        }
        PREFS.putDouble("win.w", ventana.getWidth());
        PREFS.putDouble("win.h", ventana.getHeight());
        PREFS.putDouble("win.x", ventana.getX());
        PREFS.putDouble("win.y", ventana.getY());
    }

    /** El tamaño guardado es un número finito y no menor que el mínimo de la ventana. */
    static boolean tamanoValido(double w, double h, double minW, double minH) {
        return Double.isFinite(w) && Double.isFinite(h) && w >= minW && h >= minH;
    }

    /** Hay alguna pantalla que contiene el punto (metido un poco hacia dentro por los bordes). */
    static boolean puntoVisible(double x, double y) {
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            return false;
        }
        for (Screen pantalla : Screen.getScreens()) {
            Rectangle2D area = pantalla.getVisualBounds();
            if (area.contains(x + 20, y + 20)) {
                return true;
            }
        }
        return false;
    }
}
