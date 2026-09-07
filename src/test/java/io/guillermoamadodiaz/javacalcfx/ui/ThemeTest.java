package io.guillermoamadodiaz.javacalcfx.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ThemeTest {

    private boolean initial;

    @BeforeEach
    void rememberState() {
        initial = Theme.isDark();
    }

    @AfterEach
    void restoreState() {
        if (Theme.isDark() != initial) {
            Theme.toggle();
        }
    }

    @Test
    void toggle_changes_the_state_and_persists_it() {
        boolean now = Theme.toggle();
        assertEquals(!initial, now);
        assertEquals(!initial, Theme.isDark());
    }

    @Test
    void two_changes_return_to_the_starting_point() {
        Theme.toggle();
        Theme.toggle();
        assertEquals(initial, Theme.isDark());
    }
}
