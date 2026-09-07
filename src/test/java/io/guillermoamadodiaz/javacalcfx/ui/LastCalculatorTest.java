package io.guillermoamadodiaz.javacalcfx.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class LastCalculatorTest {

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

        LastCalculator.remember("cuadratica");
        assertEquals("cuadratica", LastCalculator.remembered().orElseThrow());
    }

    @Test
    void forgetting_clears_what_was_remembered() {
        LastCalculator.remember("imc");
        LastCalculator.forget();
        assertTrue(LastCalculator.remembered().isEmpty());
    }
}
