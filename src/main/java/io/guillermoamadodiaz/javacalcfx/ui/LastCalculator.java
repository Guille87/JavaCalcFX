package io.guillermoamadodiaz.javacalcfx.ui;

import java.util.Optional;
import java.util.prefs.Preferences;

/**
 * Remembers which calculator was open last, to return to it on startup.
 *
 * <p>Stores the calculator's key in its own {@link Preferences} subnode. Going
 * back to the menu {@link #forget() forgets} it, so the app opens on the last
 * <em>screen</em> (a calculator if it was closed on one, the menu if it was
 * closed on the menu).
 */
public final class LastCalculator {

    private static final Preferences PREFS =
            Preferences.userNodeForPackage(LastCalculator.class).node("last-calculator");
    private static final String KEY = "key";

    private LastCalculator() {}

    /** Remembers the calculator {@code key}, unless the user turned that setting off. */
    public static void remember(String key) {
        if (!Settings.rememberLastCalculator()) {
            return;
        }
        try {
            PREFS.put(KEY, key);
        } catch (RuntimeException ignored) {
            // no preferences available: nothing to remember
        }
    }

    /** The key of the last calculator opened, if one is remembered. */
    public static Optional<String> remembered() {
        try {
            return Optional.ofNullable(PREFS.get(KEY, null));
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }

    /** Forgets the remembered calculator (called when going back to the menu). */
    public static void forget() {
        try {
            PREFS.remove(KEY);
        } catch (RuntimeException ignored) {
            // no preferences available: nothing to forget
        }
    }
}
