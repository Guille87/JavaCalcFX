package io.guillermoamadodiaz.javacalcfx.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;

class FormatTest {

    /** Strips everything that is not a digit, to compare without depending on the locale. */
    private static String digitsOnly(String s) {
        return s.replaceAll("\\D", "");
    }

    @Test
    void whole_number_without_decimals() {
        assertEquals("5", Format.number(5));
    }

    @Test
    void number_trims_to_four_decimals_without_trailing_zeros() {
        assertEquals("20711", digitsOnly(Format.number(2.07106781)));
    }

    @Test
    void number_groups_the_thousands() {
        String s = Format.number(12345);
        assertEquals("12345", digitsOnly(s));
        assertTrue(s.length() > 5, "must include a thousands separator: " + s);
    }

    @Test
    void the_format_does_not_depend_on_the_system_language() {
        // Dot decimal and comma thousands, always (consistent with the input).
        assertEquals("0.15", Format.number(0.15));
        assertEquals("1,234.5", Format.number(1234.5));
        assertEquals("7.50", Format.twoDecimals(7.5));
        assertEquals("1,000,000", Format.integer(1_000_000L));
    }

    @Test
    void two_decimals_always_two_places() {
        assertEquals("500", digitsOnly(Format.twoDecimals(5)));
    }

    @Test
    void negative_zero_is_shown_as_zero() {
        assertEquals("0", Format.number(-0.0));
        assertEquals("000", digitsOnly(Format.twoDecimals(-0.0)));
    }

    @Test
    void big_integer_with_thousands_separator() {
        String s = Format.bigInteger(new BigInteger("1000000"));
        assertEquals("1000000", digitsOnly(s));
        assertTrue(s.length() > 7, "must include thousands separators: " + s);
    }

    @Test
    void long_integer_with_thousands_separator() {
        String s = Format.integer(1_234_567L);
        assertEquals("1234567", digitsOnly(s));
        assertTrue(s.length() > 7, "must include thousands separators: " + s);
    }
}
