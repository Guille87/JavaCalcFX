package io.guillermoamadodiaz.javacalcfx.ui;

import java.util.prefs.Preferences;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Saves and restores the window's size and position between sessions
 * ({@link java.util.prefs}), while {@link Settings#rememberWindow()} is on.
 * Discards values below the minimum or that would leave the window off every
 * screen (e.g. after unplugging a monitor).
 */
public final class WindowState {

    private static final Preferences PREFS = Preferences.userNodeForPackage(WindowState.class);
    private static final double NONE = Double.NaN;

    private WindowState() {}

    /** Applies the saved state, if remembered, present and valid. Call before {@code show()}. */
    public static void restore(Stage window) {
        if (!Settings.rememberWindow()) {
            return;
        }
        double w = PREFS.getDouble("win.w", NONE);
        double h = PREFS.getDouble("win.h", NONE);
        if (isValidSize(w, h, window.getMinWidth(), window.getMinHeight())) {
            window.setWidth(w);
            window.setHeight(h);
        }

        double x = PREFS.getDouble("win.x", NONE);
        double y = PREFS.getDouble("win.y", NONE);
        if (isVisiblePoint(x, y)) {
            window.setX(x);
            window.setY(y);
        }

        if (PREFS.getBoolean("win.max", false)) {
            window.setMaximized(true);
        }
    }

    /** Starts persisting size/position changes. Call after {@code show()}. */
    public static void watch(Stage window) {
        Runnable save = () -> saveIfNotMaximized(window);
        window.widthProperty().addListener((o, a, b) -> save.run());
        window.heightProperty().addListener((o, a, b) -> save.run());
        window.xProperty().addListener((o, a, b) -> save.run());
        window.yProperty().addListener((o, a, b) -> save.run());
        window.maximizedProperty().addListener((o, a, b) -> {
            if (Settings.rememberWindow()) {
                PREFS.putBoolean("win.max", b);
            }
        });
    }

    /** Forgets the saved geometry and returns {@code window} to its default size, centered. */
    public static void reset(Stage window) {
        PREFS.remove("win.w");
        PREFS.remove("win.h");
        PREFS.remove("win.x");
        PREFS.remove("win.y");
        PREFS.remove("win.max");
        window.setMaximized(false);
        window.setWidth(window.getMinWidth());
        window.setHeight(window.getMinHeight());
        window.centerOnScreen();
    }

    private static void saveIfNotMaximized(Stage window) {
        if (!Settings.rememberWindow() || window.isMaximized() || window.isIconified()) {
            return; // not remembering, or keeping the "normal" size (not maximized/minimized)
        }
        PREFS.putDouble("win.w", window.getWidth());
        PREFS.putDouble("win.h", window.getHeight());
        PREFS.putDouble("win.x", window.getX());
        PREFS.putDouble("win.y", window.getY());
    }

    /** The saved size is a finite number and not smaller than the window's minimum. */
    static boolean isValidSize(double w, double h, double minW, double minH) {
        return Double.isFinite(w) && Double.isFinite(h) && w >= minW && h >= minH;
    }

    /** Some screen contains the point (nudged inward a little to allow for borders). */
    static boolean isVisiblePoint(double x, double y) {
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            return false;
        }
        for (Screen screen : Screen.getScreens()) {
            Rectangle2D area = screen.getVisualBounds();
            if (area.contains(x + 20, y + 20)) {
                return true;
            }
        }
        return false;
    }
}
