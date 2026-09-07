package io.guillermoamadodiaz.javacalcfx.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.guillermoamadodiaz.javacalcfx.ui.NumericFilter.Type;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NumericFilterTest {

    @Nested
    class Decimal {

        @ParameterizedTest
        @ValueSource(strings = {"", "-", ".", "-.", "3", "3.5", "-42", "-0.001", "1000"})
        void accepts(String text) {
            assertTrue(NumericFilter.isValid(text, Type.DECIMAL), text);
        }

        @ParameterizedTest
        @ValueSource(strings = {"abc", "3a", "3.5.1", "1,5", "--3", "3-", "3 4", " 3"})
        void rejects(String text) {
            assertFalse(NumericFilter.isValid(text, Type.DECIMAL), text);
        }
    }

    @Nested
    class Integer {

        @ParameterizedTest
        @ValueSource(strings = {"", "-", "0", "42", "-42", "1000"})
        void accepts(String text) {
            assertTrue(NumericFilter.isValid(text, Type.INTEGER), text);
        }

        @ParameterizedTest
        @ValueSource(strings = {".", "3.5", "-.", "3.", "abc", "--3", "3-", "1,5"})
        void rejects_decimals_and_junk(String text) {
            assertFalse(NumericFilter.isValid(text, Type.INTEGER), text);
        }
    }
}
