package io.guillermoamadodiaz.javacalcfx.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LastCalculatorTest {

    @BeforeEach
    void enableRemembering() {
        Settings.setRememberLastCalculator(true); // remember(...) is a no-op when this is off
    }

    @AfterEach
    void cleanUp() {
        LastCalculator.forget();
    }

    @Test
    void with_nothing_remembered_returns_empty() {
        LastCalculator.forget();
        assertTrue(LastCalculator.remembered().isEmpty());
    }

    @Test
    void remembers_the_last_key() {
        LastCalculator.remember("factorial");
        assertEquals("factorial", LastCalculator.remembered().orElseThrow());

        LastCalculator.remember("quadratic");
        assertEquals("quadratic", LastCalculator.remembered().orElseThrow());
    }

    @Test
    void forgetting_clears_what_was_remembered() {
        LastCalculator.remember("bmi");
        LastCalculator.forget();
        assertTrue(LastCalculator.remembered().isEmpty());
    }
}
