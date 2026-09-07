package io.guillermoamadodiaz.javacalcfx.ui;

import java.util.prefs.Preferences;

/**
 * User-configurable options, persisted in their own {@code java.util.prefs}
 * subnode. Each option is a plain getter/setter with a sensible default, so the
 * settings screen and the rest of the UI read the same source of truth.
 */
public final class Settings {

    private static final Preferences PREFS =
            Preferences.userNodeForPackage(Settings.class).node("settings");

    private static final String REMEMBER_LAST_CALCULATOR = "remember-last-calculator";
    private static final String REMEMBER_WINDOW = "remember-window";

    private Settings() {}

    /** Whether the app reopens the last calculator on start (default: {@code false}). */
    public static boolean rememberLastCalculator() {
        return PREFS.getBoolean(REMEMBER_LAST_CALCULATOR, false);
    }

    public static void setRememberLastCalculator(boolean value) {
        PREFS.putBoolean(REMEMBER_LAST_CALCULATOR, value);
    }

    /** Whether the window's size and position are remembered between sessions (default: {@code true}). */
    public static boolean rememberWindow() {
        return PREFS.getBoolean(REMEMBER_WINDOW, true);
    }

    public static void setRememberWindow(boolean value) {
        PREFS.putBoolean(REMEMBER_WINDOW, value);
    }
}
