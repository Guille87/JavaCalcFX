package io.guillermoamadodiaz.javacalcfx.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HistoryTest {

    @BeforeEach
    void startClean() {
        History.clear();
    }

    @AfterAll
    static void cleanUp() {
        History.clear();
    }

    @Test
    void the_most_recent_goes_first() {
        History.record("Factorial", "El factorial de 5 es 120");
        History.record("Año Bisiesto", "El año 2000 es bisiesto.");

        var recent = History.recent();
        assertEquals("Año Bisiesto", recent.get(0).title());
        assertEquals("Factorial", recent.get(1).title());
    }

    @Test
    void does_not_exceed_the_maximum_and_drops_the_oldest() {
        for (int i = 1; i <= History.MAX + 5; i++) {
            History.record("Cálculo " + i, "resultado " + i);
        }
        var recent = History.recent();
        assertEquals(History.MAX, recent.size());
        assertEquals("Cálculo " + (History.MAX + 5), recent.get(0).title());
        assertFalse(recent.stream().anyMatch(e -> e.title().equals("Cálculo 1")));
    }

    @Test
    void trims_a_result_that_is_too_long() {
        History.record("Factorial", "x".repeat(5_000));
        String saved = History.recent().get(0).result();
        assertTrue(saved.length() <= History.MAX_RESULT + 1, "length: " + saved.length());
        assertTrue(saved.endsWith("…"));
    }

    @Test
    void clearing_leaves_the_history_empty() {
        History.record("Factorial", "El factorial de 5 es 120");
        History.clear();
        assertTrue(History.recent().isEmpty());
    }

    @Test
    void repeating_the_same_calculation_does_not_duplicate_it() {
        History.record("IMC", "IMC: 22.86");
        History.record("IMC", "IMC: 22.86");
        History.record("IMC", "IMC: 22.86");

        assertEquals(1, History.recent().size(), "the same calculation repeated adds no entries");
    }

    @Test
    void a_different_calculation_is_added() {
        History.record("IMC", "IMC: 22.86");
        History.record("IMC", "IMC: 25.10");
        assertEquals(2, History.recent().size());
    }

    @Test
    void each_entry_carries_the_time() {
        Instant before = Instant.now();
        History.record("Factorial", "El factorial de 5 es 120");
        Instant timestamp = History.recent().get(0).timestamp();

        assertNotNull(timestamp);
        assertFalse(timestamp.isBefore(before.minusSeconds(1)));
        assertFalse(timestamp.isAfter(Instant.now().plusSeconds(1)));
    }
}
