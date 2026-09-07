package io.guillermoamadodiaz.javacalcfx.calc;

/**
 * Unit conversions to SI units (kilograms and meters), so the same calculation
 * logic can be reused with data in different systems.
 *
 * <p>Pure functions, no validation: the caller passes the result to the
 * calculator, which already checks its preconditions.
 */
public final class Conversions {

    private static final double POUND_IN_KILOS = 0.453_592_37;
    private static final double INCH_IN_METERS = 0.025_4;
    private static final int INCHES_PER_FOOT = 12;

    private Conversions() {}

    /** Pounds to kilograms. */
    public static double poundsToKilos(double pounds) {
        return pounds * POUND_IN_KILOS;
    }

    /** Kilograms to pounds. */
    public static double kilosToPounds(double kilos) {
        return kilos / POUND_IN_KILOS;
    }

    /** Centimeters to meters. */
    public static double cmToMeters(double centimeters) {
        return centimeters / 100.0;
    }

    /** Meters to centimeters. */
    public static double metersToCm(double meters) {
        return meters * 100.0;
    }

    /** Feet and inches (e.g. {@code 5}, {@code 9}) to meters. */
    public static double feetInchesToMeters(double feet, double inches) {
        return (feet * INCHES_PER_FOOT + inches) * INCH_IN_METERS;
    }

    /**
     * Meters to {@code [feet, inches]}, with inches in {@code [0, 12)} and possibly
     * fractional (e.g. {@code 1.75 m → [5, 8.9…]}).
     */
    public static double[] metersToFeetInches(double meters) {
        double totalInches = meters / INCH_IN_METERS;
        double feet = Math.floor(totalInches / INCHES_PER_FOOT);
        return new double[] {feet, totalInches - feet * INCHES_PER_FOOT};
    }
}
