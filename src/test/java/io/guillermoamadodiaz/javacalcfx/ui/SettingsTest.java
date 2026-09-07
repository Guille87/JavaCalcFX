package io.guillermoamadodiaz.javacalcfx.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.prefs.Preferences;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class SettingsTest {

    @AfterEach
    void restoreDefaults() {
        try {
            Preferences.userNodeForPackage(Settings.class).node("settings").clear();
        } catch (Exception ignored) {
            // no persistence available in the test environment
        }
    }

    @Test
    void remember_last_calculator_defaults_to_false() {
        assertFalse(Settings.rememberLastCalculator());
    }

    @Test
    void remember_last_calculator_can_be_turned_on_and_off() {
        Settings.setRememberLastCalculator(true);
        assertTrue(Settings.rememberLastCalculator());

        Settings.setRememberLastCalculator(false);
        assertFalse(Settings.rememberLastCalculator());
    }

    @Test
    void remember_window_defaults_to_true() {
        assertTrue(Settings.rememberWindow());
    }

    @Test
    void remember_window_can_be_turned_off_and_on() {
        Settings.setRememberWindow(false);
        assertFalse(Settings.rememberWindow());

        Settings.setRememberWindow(true);
        assertTrue(Settings.rememberWindow());
    }

    @Test
    void history_defaults_enabled_with_max_25_and_no_clear_on_exit() {
        assertTrue(Settings.historyEnabled());
        assertEquals(25, Settings.historyMax());
        assertFalse(Settings.clearHistoryOnExit());
    }

    @Test
    void history_options_can_be_changed() {
        Settings.setHistoryEnabled(false);
        Settings.setHistoryMax(50);
        Settings.setClearHistoryOnExit(true);

        assertFalse(Settings.historyEnabled());
        assertEquals(50, Settings.historyMax());
        assertTrue(Settings.clearHistoryOnExit());
    }

    @Test
    void restore_defaults_puts_every_option_back() {
        Settings.setRememberLastCalculator(true);
        Settings.setRememberWindow(false);
        Settings.setHistoryEnabled(false);
        Settings.setHistoryMax(100);
        Settings.setClearHistoryOnExit(true);

        Settings.restoreDefaults();

        assertFalse(Settings.rememberLastCalculator());
        assertTrue(Settings.rememberWindow());
        assertTrue(Settings.historyEnabled());
        assertEquals(25, Settings.historyMax());
        assertFalse(Settings.clearHistoryOnExit());
    }
}
