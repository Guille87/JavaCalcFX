package io.guillermoamadodiaz.javacalcfx.ui;

import java.util.prefs.BackingStoreException;
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
    private static final String HISTORY_ENABLED = "history-enabled";
    private static final String HISTORY_MAX = "history-max";
    private static final String CLEAR_HISTORY_ON_EXIT = "clear-history-on-exit";

    /** Default number of history entries; also one of the values the selector offers. */
    public static final int DEFAULT_HISTORY_MAX = 25;

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

    /** Whether calculations are recorded in the history (default: {@code true}). */
    public static boolean historyEnabled() {
        return PREFS.getBoolean(HISTORY_ENABLED, true);
    }

    public static void setHistoryEnabled(boolean value) {
        PREFS.putBoolean(HISTORY_ENABLED, value);
    }

    /** How many history entries are kept (default: {@link #DEFAULT_HISTORY_MAX}). */
    public static int historyMax() {
        return PREFS.getInt(HISTORY_MAX, DEFAULT_HISTORY_MAX);
    }

    public static void setHistoryMax(int value) {
        PREFS.putInt(HISTORY_MAX, value);
    }

    /** Whether the history is wiped when the app closes (default: {@code false}). */
    public static boolean clearHistoryOnExit() {
        return PREFS.getBoolean(CLEAR_HISTORY_ON_EXIT, false);
    }

    public static void setClearHistoryOnExit(boolean value) {
        PREFS.putBoolean(CLEAR_HISTORY_ON_EXIT, value);
    }

    /** Clears every stored option, so each getter falls back to its default. */
    public static void restoreDefaults() {
        try {
            PREFS.clear();
        } catch (BackingStoreException | RuntimeException ignored) {
            // no persistence available: nothing to clear
        }
    }
}
