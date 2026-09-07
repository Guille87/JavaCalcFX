package io.guillermoamadodiaz.javacalcfx.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WindowStateTest {

    @Test
    void accepts_a_size_not_smaller_than_the_minimum() {
        assertTrue(WindowState.isValidSize(800, 600, 655, 490));
        assertTrue(WindowState.isValidSize(655, 490, 655, 490));
    }

    @Test
    void rejects_sizes_below_the_minimum_or_not_finite() {
        assertFalse(WindowState.isValidSize(400, 600, 655, 490));
        assertFalse(WindowState.isValidSize(800, 300, 655, 490));
        assertFalse(WindowState.isValidSize(Double.NaN, 600, 655, 490));
        assertFalse(WindowState.isValidSize(800, Double.POSITIVE_INFINITY, 655, 490));
    }

    @Test
    void a_non_finite_point_is_never_visible() {
        assertFalse(WindowState.isVisiblePoint(Double.NaN, 0));
        assertFalse(WindowState.isVisiblePoint(0, Double.NEGATIVE_INFINITY));
    }
}
