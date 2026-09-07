package io.guillermoamadodiaz.javacalcfx.calc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ConversionsTest {

    private static final double EPS = 1e-9;

    @Test
    void pounds_to_kilos() {
        assertEquals(0.45359237, Conversions.poundsToKilos(1), EPS);
        assertEquals(70.0, Conversions.poundsToKilos(154.323583529), 1e-6);
    }

    @Test
    void centimeters_to_meters() {
        assertEquals(1.75, Conversions.cmToMeters(175), EPS);
        assertEquals(0.0, Conversions.cmToMeters(0), EPS);
    }

    @Test
    void feet_and_inches_to_meters() {
        assertEquals(1.8288, Conversions.feetInchesToMeters(6, 0), EPS); // 72 in
        assertEquals(1.7526, Conversions.feetInchesToMeters(5, 9), EPS); // 69 in
        assertEquals(0.0254, Conversions.feetInchesToMeters(0, 1), EPS);
    }

    @Test
    void the_inverse_conversions_round_trip() {
        assertEquals(154.32358, Conversions.kilosToPounds(70), 1e-5);
        assertEquals(180.0, Conversions.metersToCm(1.8), EPS);

        double[] ftIn = Conversions.metersToFeetInches(1.8288); // exactly 6'0"
        assertEquals(6.0, ftIn[0], EPS);
        assertEquals(0.0, ftIn[1], 1e-9);

        double[] other = Conversions.metersToFeetInches(1.7526); // 5'9"
        assertEquals(5.0, other[0], EPS);
        assertEquals(9.0, other[1], 1e-6);
    }
}
