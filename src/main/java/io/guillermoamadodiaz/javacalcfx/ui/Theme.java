package io.guillermoamadodiaz.javacalcfx.ui;

import java.util.prefs.Preferences;
import javafx.scene.Scene;

/**
 * The application's light/dark theme.
 *
 * <p>Dark mode is turned on by adding the {@link #DARK_CLASS} style class to the
 * scene root; the colors live in {@code styles.css}. The preference is persisted
 * with {@link Preferences} (in its own subnode so the {@link WindowState} cleanup
 * in the tests does not wipe it).
 */
public final class Theme {

    /** Style class that turns on dark mode (see {@code styles.css}). */
    public static final String DARK_CLASS = "dark-theme";

    private static final Preferences PREFS =
            Preferences.userNodeForPackage(Theme.class).node("theme");
    private static final String KEY = "dark";

    private Theme() {}

    /** {@code true} if dark mode is active (persisted; light by default). */
    public static boolean isDark() {
        return PREFS.getBoolean(KEY, false);
    }

    /** Switches the theme and persists it. Returns the new state ({@code true} = dark). */
    public static boolean toggle() {
        boolean dark = !isDark();
        PREFS.putBoolean(KEY, dark);
        return dark;
    }

    /** Sets dark mode on or off and persists it. */
    public static void setDark(boolean dark) {
        PREFS.putBoolean(KEY, dark);
    }

    /** Applies the current theme to the scene. Idempotent; ignores {@code null}. */
    public static void applyTo(Scene scene) {
        if (scene == null || scene.getRoot() == null) {
            return;
        }
        var classes = scene.getRoot().getStyleClass();
        classes.remove(DARK_CLASS);
        if (isDark()) {
            classes.add(DARK_CLASS);
        }
    }
}
