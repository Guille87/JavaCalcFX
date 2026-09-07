package io.guillermoamadodiaz.javacalcfx.ui;

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
}
