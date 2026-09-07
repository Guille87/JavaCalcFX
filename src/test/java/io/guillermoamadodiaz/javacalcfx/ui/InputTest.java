package io.guillermoamadodiaz.javacalcfx.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class InputTest {

    @Test
    void parses_valid_values() {
        assertEquals(3.5, Input.asDouble("3.5"));
        assertEquals(42, Input.asInt("42"));
        assertEquals(9_000_000_000L, Input.asLong("9000000000"));
    }

    @Test
    void non_numeric_text_throws_NumberFormatException() {
        assertThrows(NumberFormatException.class, () -> Input.asDouble("abc"));
        assertThrows(NumberFormatException.class, () -> Input.asInt(""));
        assertThrows(NumberFormatException.class, () -> Input.asLong("1,5"));
    }

    @Test
    void out_of_range_int_throws_NumberFormatException() {
        assertThrows(NumberFormatException.class, () -> Input.asInt("3000000000"));
    }
}
