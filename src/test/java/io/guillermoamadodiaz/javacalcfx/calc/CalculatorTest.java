package io.guillermoamadodiaz.javacalcfx.calc;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.guillermoamadodiaz.javacalcfx.calc.Calculator.Triangle;
import java.math.BigInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class CalculatorTest {

    private static final double EPS = 1e-9;

    @Test
    @DisplayName("Domain errors carry the message key, not the text")
    void domain_errors_carry_the_message_key() {
        CalculationError e = assertThrows(CalculationError.class, () -> Calculator.isLeapYear(0));
        assertEquals("calc.year.not.positive", e.key());

        CalculationError withName = assertThrows(CalculationError.class, () -> Calculator.cylinderArea(-1, 2));
        assertEquals("calc.value.negative", withName.key());
        assertEquals(
                new CalculationError.Name("calc.name.radius"),
                withName.arguments().get(0));
    }

    @Nested
    @DisplayName("Right triangle")
    class RightTriangle {

        @Test
        void triple_3_4_5() {
            Triangle t = Calculator.solveRightTriangle(3, 4);
            assertEquals(5.0, t.hypotenuse(), EPS);
            assertEquals(6.0, t.area(), EPS);
            assertEquals(12.0, t.perimeter(), EPS);
            assertEquals(90.0, t.angleAlpha() + t.angleBeta(), EPS);
        }

        @Test
        void hypotenuse_does_not_overflow_with_huge_legs() {
            Triangle t = Calculator.solveRightTriangle(3e200, 4e200);
            assertEquals(5e200, t.hypotenuse(), 1e190);
        }

        @ParameterizedTest
        @CsvSource({"0,1", "-1,1", "1,0", "NaN,1"})
        void rejects_non_positive_legs(double a, double b) {
            assertThrows(IllegalArgumentException.class, () -> Calculator.solveRightTriangle(a, b));
        }
    }

    @Nested
    @DisplayName("Cylinder area")
    class CylinderArea {

        @Test
        void known_value() {
            // 2*pi*r*(r+h) with r=1, h=1 -> 4*pi
            assertEquals(4 * Math.PI, Calculator.cylinderArea(1, 1), EPS);
        }

        @Test
        void accepts_zero() {
            assertEquals(0.0, Calculator.cylinderArea(0, 5), EPS);
        }

        @Test
        void rejects_negatives() {
            assertThrows(IllegalArgumentException.class, () -> Calculator.cylinderArea(-1, 2));
            assertThrows(IllegalArgumentException.class, () -> Calculator.cylinderArea(2, -1));
        }
    }

    @Nested
    @DisplayName("Leap year")
    class LeapYear {

        @ParameterizedTest
        @ValueSource(ints = {2000, 2024, 1600, 2400})
        void leap_years(int year) {
            assertTrue(Calculator.isLeapYear(year));
        }

        @ParameterizedTest
        @ValueSource(ints = {1900, 2023, 2100, 1})
        void non_leap_years(int year) {
            assertFalse(Calculator.isLeapYear(year));
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -4})
        void rejects_non_positive_years(int year) {
            assertThrows(IllegalArgumentException.class, () -> Calculator.isLeapYear(year));
        }
    }

    @Nested
    @DisplayName("Factorial")
    class Factorial {

        @Test
        void base_cases() {
            assertEquals(BigInteger.ONE, Calculator.factorial(0));
            assertEquals(BigInteger.ONE, Calculator.factorial(1));
        }

        @Test
        void known_values() {
            assertEquals(BigInteger.valueOf(120), Calculator.factorial(5));
            assertEquals(new BigInteger("2432902008176640000"), Calculator.factorial(20));
        }

        @Test
        void rejects_negatives() {
            assertThrows(IllegalArgumentException.class, () -> Calculator.factorial(-1));
        }

        @Test
        void rejects_oversized() {
            assertThrows(IllegalArgumentException.class, () -> Calculator.factorial(Calculator.MAX_FACTORIAL + 1));
        }

        @Test
        void can_be_cancelled_via_interrupt() throws InterruptedException {
            var aborted = new java.util.concurrent.atomic.AtomicBoolean(false);
            Thread thread = new Thread(() -> {
                try {
                    Calculator.factorial(Calculator.MAX_FACTORIAL);
                } catch (java.util.concurrent.CancellationException e) {
                    aborted.set(true);
                }
            });
            thread.start();
            Thread.sleep(20);
            thread.interrupt();
            thread.join(5_000);
            assertTrue(aborted.get(), "factorial must abort when the thread is interrupted");
        }
    }

    @Nested
    @DisplayName("Multiples")
    class Multiples {

        @ParameterizedTest
        @CsvSource({"10,5", "0,7", "-9,3", "5,1"})
        void is_multiple(long a, long b) {
            assertTrue(Calculator.isMultiple(a, b));
        }

        @ParameterizedTest
        @CsvSource({"10,3", "7,0", "1,5"})
        void is_not_multiple(long a, long b) {
            assertFalse(Calculator.isMultiple(a, b));
        }

        @Test
        void zero_by_zero() {
            assertTrue(Calculator.isMultiple(0, 0));
        }

        @Test
        void does_not_throw_with_zero_divisor() {
            Calculator.isMultiple(5, 0); // used to cause ArithmeticException
        }
    }

    @Nested
    @DisplayName("Grades and passing")
    class Grades {

        @Test
        void mean_and_passing() {
            double mean = Calculator.mean(5, 5, 5, 5, 5);
            assertEquals(5.0, mean, EPS);
            assertTrue(Calculator.isPassing(mean));
        }

        @Test
        void failing() {
            double mean = Calculator.mean(4, 4, 4, 4, 4);
            assertFalse(Calculator.isPassing(mean));
        }

        @Test
        void mean_requires_data() {
            assertThrows(IllegalArgumentException.class, Calculator::mean);
        }

        @ParameterizedTest
        @CsvSource({"11", "-1", "10.5", "NaN"})
        void mean_rejects_grades_out_of_range(double grade) {
            assertThrows(IllegalArgumentException.class, () -> Calculator.mean(grade, 5, 5, 5, 5));
        }

        @Test
        void mean_accepts_the_range_endpoints() {
            assertEquals(5.0, Calculator.mean(0, 10, 0, 10, 5), EPS);
        }
    }

    @Nested
    @DisplayName("Quadratic equation")
    class QuadraticEquation {

        private double[] realRoots(double a, double b, double c) {
            var e = Calculator.solveQuadratic(a, b, c);
            double[] r = {e.x1().real(), e.x2().real()};
            java.util.Arrays.sort(r);
            return r;
        }

        @Test
        void two_distinct_real_roots() {
            var e = Calculator.solveQuadratic(1, -5, 6); // x²-5x+6 -> 2 and 3
            assertTrue(e.hasRealRoots());
            assertFalse(e.hasDoubleRoot());
            assertArrayEquals(new double[] {2, 3}, realRoots(1, -5, 6), EPS);
        }

        @Test
        void double_root() {
            var e = Calculator.solveQuadratic(1, -4, 4); // (x-2)²
            assertTrue(e.hasDoubleRoot());
            assertEquals(2.0, e.x1().real(), EPS);
            assertEquals(e.x1(), e.x2());
        }

        @Test
        void conjugate_complex_roots() {
            var e = Calculator.solveQuadratic(1, 0, 1); // x²+1 -> ±i
            assertFalse(e.hasRealRoots());
            assertEquals(0.0, e.x1().real(), EPS);
            assertEquals(1.0, Math.abs(e.x1().imaginary()), EPS);
            assertEquals(e.x1().real(), e.x2().real(), EPS);
            assertEquals(-e.x1().imaginary(), e.x2().imaginary(), EPS);
        }

        @Test
        void a_zero_is_not_quadratic() {
            assertThrows(IllegalArgumentException.class, () -> Calculator.solveQuadratic(0, 2, 1));
        }

        @Test
        void rejects_non_finite_coefficients() {
            assertThrows(IllegalArgumentException.class, () -> Calculator.solveQuadratic(1, Double.NaN, 1));
        }

        @Test
        void stable_formula_with_very_large_b() {
            // x² + 1e8·x + 1 = 0 : roots ≈ -1e8 and ≈ -1e-8. The naive formula loses
            // all the precision of the small root to catastrophic cancellation.
            double[] r = realRoots(1, 1e8, 1);
            assertEquals(-1e8, r[0], 1.0);
            assertEquals(-1e-8, r[1], 1e-12);
        }
    }

    @Nested
    @DisplayName("Power")
    class Power {

        @Test
        void known_values() {
            assertEquals(1024.0, Calculator.power(2, 10), EPS);
            assertEquals(0.125, Calculator.power(2, -3), EPS);
            assertEquals(1.0, Calculator.power(5, 0), EPS);
        }

        @Test
        void fractional_exponent_is_a_root() {
            assertEquals(3.0, Calculator.power(9, 0.5), EPS);
        }

        @ParameterizedTest
        @CsvSource({"-8,0.5", "0,-1", "10,400"}) // not real, 0^-1, overflow
        void non_real_or_infinite_results_are_rejected(double base, double exponent) {
            assertThrows(IllegalArgumentException.class, () -> Calculator.power(base, exponent));
        }

        @Test
        void rejects_non_finite_arguments() {
            assertThrows(IllegalArgumentException.class, () -> Calculator.power(Double.NaN, 2));
        }
    }

    @Nested
    @DisplayName("Nth root")
    class NthRoot {

        @Test
        void exact_roots() {
            assertEquals(3.0, Calculator.nthRoot(27, 3), 1e-12);
            assertEquals(2.0, Calculator.nthRoot(16, 4), 1e-12);
            assertEquals(5.0, Calculator.nthRoot(25, 2), 1e-12);
        }

        @Test
        void odd_index_of_negative_radicand() {
            assertEquals(-2.0, Calculator.nthRoot(-8, 3), 1e-12);
        }

        @Test
        void even_index_of_negative_radicand_has_no_real_value() {
            assertThrows(IllegalArgumentException.class, () -> Calculator.nthRoot(-4, 2));
        }

        @ParameterizedTest
        @CsvSource({"27,1", "27,0", "27,-3", "27,2.5", "27,NaN"})
        void index_must_be_a_whole_number_at_least_two(double radicand, double index) {
            assertThrows(IllegalArgumentException.class, () -> Calculator.nthRoot(radicand, index));
        }

        @Test
        void zero_radicand() {
            assertEquals(0.0, Calculator.nthRoot(0, 5), EPS);
        }
    }

    @Nested
    @DisplayName("GCD and LCM")
    class GcdAndLcm {

        @Test
        void gcd_known_values() {
            assertEquals(6, Calculator.gcd(12, 18));
            assertEquals(1, Calculator.gcd(7, 13));
            assertEquals(5, Calculator.gcd(-15, 10)); // sign is ignored
        }

        @Test
        void gcd_with_zero() {
            assertEquals(7, Calculator.gcd(0, 7));
            assertEquals(7, Calculator.gcd(7, 0));
            assertEquals(0, Calculator.gcd(0, 0));
        }

        @Test
        void lcm_known_values() {
            assertEquals(36, Calculator.lcm(12, 18));
            assertEquals(91, Calculator.lcm(7, 13));
            assertEquals(30, Calculator.lcm(-6, 10));
        }

        @Test
        void lcm_with_zero_is_zero() {
            assertEquals(0, Calculator.lcm(0, 5));
        }

        @Test
        void gcd_times_lcm_is_the_product_of_the_absolute_values() {
            assertEquals(12L * 18, Calculator.gcd(12, 18) * Calculator.lcm(12, 18));
        }

        @Test
        void lcm_that_overflows_a_long_is_rejected() {
            assertThrows(IllegalArgumentException.class, () -> Calculator.lcm(1_000_000_000_000L, 999_999_999_999L));
        }
    }

    @Nested
    @DisplayName("Primality")
    class Primes {

        @ParameterizedTest
        @ValueSource(longs = {2, 3, 5, 7, 13, 97, 7919, 1_000_000_007L})
        void primes(long n) {
            assertTrue(Calculator.analyzePrimality(n).prime());
        }

        @ParameterizedTest
        @ValueSource(longs = {4, 6, 9, 15, 100, 7917})
        void composites(long n) {
            var p = Calculator.analyzePrimality(n);
            assertFalse(p.prime());
            assertTrue(p.isComposite());
            assertEquals(0, n % p.smallestProperDivisor());
        }

        @Test
        void the_divisor_is_the_smallest() {
            assertEquals(2, Calculator.analyzePrimality(14).smallestProperDivisor());
            assertEquals(3, Calculator.analyzePrimality(15).smallestProperDivisor()); // 15 = 3·5
        }

        @ParameterizedTest
        @ValueSource(longs = {1, 0, -7})
        void less_than_two_is_neither_prime_nor_composite(long n) {
            var p = Calculator.analyzePrimality(n);
            assertFalse(p.prime());
            assertFalse(p.isComposite());
        }

        @Test
        void can_be_cancelled_via_interrupt() throws InterruptedException {
            var aborted = new java.util.concurrent.atomic.AtomicBoolean(false);
            Thread thread = new Thread(() -> {
                try {
                    Calculator.analyzePrimality(2_305_843_009_213_693_951L); // 2^61 - 1, Mersenne prime
                } catch (java.util.concurrent.CancellationException e) {
                    aborted.set(true);
                }
            });
            thread.start();
            Thread.sleep(30);
            thread.interrupt();
            thread.join(5_000);
            assertTrue(aborted.get(), "the check must abort when the thread is interrupted");
        }
    }

    @Nested
    @DisplayName("Base converter")
    class BaseConverter {

        @Test
        void decimal_to_the_four_bases() {
            var c = Calculator.convertBase("255");
            assertEquals("11111111", c.binary());
            assertEquals("377", c.octal());
            assertEquals("255", c.decimal());
            assertEquals("FF", c.hex());
        }

        @Test
        void recognizes_the_prefixes() {
            assertEquals("255", Calculator.convertBase("0xFF").decimal());
            assertEquals("255", Calculator.convertBase("0Xff").decimal());
            assertEquals("255", Calculator.convertBase("0b11111111").decimal());
            assertEquals("255", Calculator.convertBase("0o377").decimal());
        }

        @Test
        void accepts_a_sign() {
            var c = Calculator.convertBase("-0xFF");
            assertEquals("-255", c.decimal());
            assertEquals("-11111111", c.binary());
        }

        @Test
        void zero() {
            assertEquals("0", Calculator.convertBase("0").hex());
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "0xGG", "0b102", "abc", "12.5"})
        void invalid_text_is_rejected(String text) {
            assertThrows(IllegalArgumentException.class, () -> Calculator.convertBase(text));
        }
    }

    @Nested
    @DisplayName("Percentage and rule of three")
    class ProportionsAndPercentages {

        @Test
        void percentage_of_an_amount() {
            assertEquals(30.0, Calculator.percentageOf(15, 200), EPS);
            assertEquals(0.0, Calculator.percentageOf(0, 200), EPS);
            assertEquals(-10.0, Calculator.percentageOf(-5, 200), EPS);
        }

        @Test
        void direct_rule_of_three() {
            assertEquals(10.0, Calculator.ruleOfThree(3, 6, 5), EPS); // 3 kg -> 6 €, 5 kg -> 10 €
            assertEquals(30.0, Calculator.ruleOfThree(100, 15, 200), EPS); // 15 % of 200
        }

        @Test
        void rule_of_three_with_a_zero_is_rejected() {
            assertThrows(IllegalArgumentException.class, () -> Calculator.ruleOfThree(0, 5, 3));
        }

        @Test
        void rejects_non_finite_arguments() {
            assertThrows(IllegalArgumentException.class, () -> Calculator.percentageOf(Double.NaN, 100));
            assertThrows(IllegalArgumentException.class, () -> Calculator.ruleOfThree(1, Double.POSITIVE_INFINITY, 1));
        }
    }

    @Nested
    @DisplayName("Body mass index")
    class BodyMassIndex {

        @Test
        void value_and_formula() {
            assertEquals(22.86, Calculator.bmi(70, 1.75).value(), 0.01);
        }

        @ParameterizedTest
        @CsvSource({
            "50, 1.75, UNDERWEIGHT",
            "70, 1.75, NORMAL",
            "80, 1.75, OVERWEIGHT",
            "100, 1.75, OBESITY",
        })
        void classifies_by_the_who_ranges(double weight, double height, Calculator.BmiCategory expected) {
            assertEquals(expected, Calculator.bmi(weight, height).category());
        }

        @Test
        void rejects_non_positive_weight_or_height() {
            assertThrows(IllegalArgumentException.class, () -> Calculator.bmi(0, 1.75));
            assertThrows(IllegalArgumentException.class, () -> Calculator.bmi(70, -1));
            assertThrows(IllegalArgumentException.class, () -> Calculator.bmi(Double.NaN, 1.75));
        }
    }
}
