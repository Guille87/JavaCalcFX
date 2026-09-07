package io.guillermoamadodiaz.javacalcfx.calc;

import java.math.BigInteger;
import java.util.concurrent.CancellationException;

/**
 * Pure mathematical logic of the application.
 *
 * <p>This package depends on nothing else (not JavaFX, not even the message
 * texts): it takes primitive values, validates their preconditions and returns
 * results, or throws {@link CalculationError} carrying the message <em>key</em>,
 * which the UI translates. That way every calculation is verifiable with unit
 * tests without any setup.
 */
public final class Calculator {

    /** Cap for {@link #factorial(int)} so a huge computation can't hang the app. */
    public static final int MAX_FACTORIAL = 100_000;

    /** Minimum and maximum grade accepted by {@link #mean(double...)}. */
    public static final double MIN_GRADE = 0.0;

    public static final double MAX_GRADE = 10.0;

    private Calculator() {}

    /**
     * Solves a right triangle from its two legs.
     *
     * @param legA length of leg A (&gt; 0)
     * @param legB length of leg B (&gt; 0)
     * @throws CalculationError if a leg is not a finite positive number
     */
    public static Triangle solveRightTriangle(double legA, double legB) {
        requirePositiveFinite(legA, "calc.name.legA");
        requirePositiveFinite(legB, "calc.name.legB");

        double hypotenuse = Math.hypot(legA, legB); // stable against overflow
        double area = (legA * legB) / 2.0;
        double perimeter = legA + legB + hypotenuse;
        double alpha = Math.toDegrees(Math.atan2(legB, legA));
        double beta = Math.toDegrees(Math.atan2(legA, legB));
        return new Triangle(legA, legB, hypotenuse, area, perimeter, alpha, beta);
    }

    /**
     * Total surface area (lateral surface + two bases) of a right cylinder.
     *
     * @param radius base radius (&ge; 0)
     * @param height cylinder height (&ge; 0)
     */
    public static double cylinderArea(double radius, double height) {
        requireNonNegativeFinite(radius, "calc.name.radius");
        requireNonNegativeFinite(height, "calc.name.height");
        return 2 * Math.PI * radius * (radius + height);
    }

    /**
     * Whether a year of the proleptic Gregorian calendar is a leap year.
     *
     * @throws CalculationError if {@code year <= 0}
     */
    public static boolean isLeapYear(int year) {
        if (year <= 0) {
            throw new CalculationError("calc.year.not.positive");
        }
        return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
    }

    /**
     * Factorial of a non-negative integer.
     *
     * @throws CalculationError if {@code n < 0} or {@code n > }{@link #MAX_FACTORIAL}
     */
    public static BigInteger factorial(int n) {
        if (n < 0) {
            throw new CalculationError("calc.factorial.negative");
        }
        if (n > MAX_FACTORIAL) {
            throw new CalculationError("calc.factorial.large", String.valueOf(MAX_FACTORIAL));
        }
        BigInteger result = BigInteger.ONE;
        for (int i = 2; i <= n; i++) {
            // Lets whoever runs this on a separate thread (Task) abort it.
            if (Thread.currentThread().isInterrupted()) {
                throw new CancellationException();
            }
            result = result.multiply(BigInteger.valueOf(i));
        }
        return result;
    }

    /**
     * Whether {@code a} is a multiple of {@code b}. By mathematical convention every
     * integer is a multiple of 0 only if it is itself 0, and 0 is a multiple of any integer.
     */
    public static boolean isMultiple(long a, long b) {
        if (b == 0) {
            return a == 0;
        }
        return a % b == 0;
    }

    /** Greatest common divisor of {@code |a|} and {@code |b|} (Euclid's algorithm). {@code gcd(0, 0) = 0}. */
    public static long gcd(long a, long b) {
        long x = absOrThrow(a);
        long y = absOrThrow(b);
        while (y != 0) {
            long remainder = x % y;
            x = y;
            y = remainder;
        }
        return x;
    }

    /**
     * Least common multiple of {@code |a|} and {@code |b|}. {@code lcm(0, x) = 0}.
     *
     * @throws CalculationError if the result does not fit in a {@code long}
     */
    public static long lcm(long a, long b) {
        if (a == 0 || b == 0) {
            return 0;
        }
        try {
            return Math.absExact(Math.multiplyExact(a / gcd(a, b), b));
        } catch (ArithmeticException overflow) {
            throw new CalculationError("calc.lcm.large");
        }
    }

    private static long absOrThrow(long value) {
        try {
            return Math.absExact(value);
        } catch (ArithmeticException overflow) {
            throw new CalculationError("calc.integer.large");
        }
    }

    /**
     * Analyzes the primality of {@code n} by trial division up to √n. For a
     * composite number it returns its smallest proper divisor; for a prime or for
     * {@code n < 2} the divisor is 0.
     *
     * <p>The loop checks {@link Thread#isInterrupted()} so a big number can be
     * aborted when navigating to another screen.
     */
    public static Primality analyzePrimality(long n) {
        if (n < 2) {
            return new Primality(false, 0);
        }
        if (n < 4) {
            return new Primality(true, 0); // 2 and 3
        }
        if (n % 2 == 0) {
            return new Primality(false, 2);
        }
        for (long i = 3; i <= n / i; i += 2) {
            if (Thread.currentThread().isInterrupted()) {
                throw new CancellationException();
            }
            if (n % i == 0) {
                return new Primality(false, i);
            }
        }
        return new Primality(true, 0);
    }

    /**
     * Converts an integer to binary, octal, decimal and hexadecimal. The input
     * base is inferred from the prefix: {@code 0b} binary, {@code 0o} octal,
     * {@code 0x} hexadecimal; no prefix, decimal. A sign is allowed.
     *
     * @throws CalculationError if the text is not a valid integer
     */
    public static BaseConversion convertBase(String text) {
        String trimmed = text == null ? "" : text.trim();
        boolean negative = trimmed.startsWith("-");
        String body = negative ? trimmed.substring(1) : trimmed;

        int base = 10;
        if (body.length() > 2 && body.charAt(0) == '0') {
            switch (Character.toLowerCase(body.charAt(1))) {
                case 'b' -> base = 2;
                case 'o' -> base = 8;
                case 'x' -> base = 16;
                default -> {}
            }
            if (base != 10) {
                body = body.substring(2);
            }
        }

        long value;
        try {
            value = Long.parseLong(body, base);
        } catch (NumberFormatException e) {
            throw new CalculationError("calc.base.invalid.number");
        }
        if (negative) {
            value = -value;
        }
        return new BaseConversion(
                Long.toString(value, 2),
                Long.toString(value, 8),
                Long.toString(value, 10),
                Long.toString(value, 16).toUpperCase());
    }

    /**
     * {@code percentage}% of {@code amount}.
     *
     * @throws CalculationError if an argument is not finite
     */
    public static double percentageOf(double percentage, double amount) {
        requireFinite(percentage, "calc.name.percentage");
        requireFinite(amount, "calc.name.amount");
        return percentage / 100.0 * amount;
    }

    /**
     * Direct rule of three: if {@code a} corresponds to {@code b}, then {@code c}
     * corresponds to {@code c · b / a}.
     *
     * @throws CalculationError if {@code a} is 0 or an argument is not finite
     */
    public static double ruleOfThree(double a, double b, double c) {
        requireFinite(a, "calc.name.a");
        requireFinite(b, "calc.name.b");
        requireFinite(c, "calc.name.c");
        if (a == 0.0) {
            throw new CalculationError("calc.ruleofthree.a.zero");
        }
        return c * b / a;
    }

    /**
     * Body mass index: {@code BMI = weight / height²}, with weight in kilograms
     * and height in meters. Returns the value and its category per the WHO ranges
     * for adults.
     *
     * @throws CalculationError if weight or height is not a finite positive number,
     *     or if the result is not finite
     */
    public static BodyMassIndex bmi(double weight, double height) {
        requirePositiveFinite(weight, "calc.name.weight");
        requirePositiveFinite(height, "calc.name.height");
        double value = weight / (height * height);
        if (!Double.isFinite(value)) {
            throw new CalculationError("calc.bmi.undefined");
        }
        return new BodyMassIndex(value, BmiCategory.of(value));
    }

    /**
     * Arithmetic mean of a non-empty set of grades, each in the range
     * {@code [MIN_GRADE, MAX_GRADE]}.
     *
     * @throws CalculationError if no grade is given or one falls outside the range
     */
    public static double mean(double... grades) {
        if (grades == null || grades.length == 0) {
            throw new CalculationError("calc.grades.empty");
        }
        double sum = 0;
        for (double grade : grades) {
            if (!Double.isFinite(grade) || grade < MIN_GRADE || grade > MAX_GRADE) {
                throw new CalculationError(
                        "calc.grade.range", String.valueOf((int) MIN_GRADE), String.valueOf((int) MAX_GRADE));
            }
            sum += grade;
        }
        return sum / grades.length;
    }

    /** A student passes with a mean of 5 or more. */
    public static boolean isPassing(double mean) {
        return mean >= 5.0;
    }

    /**
     * Solves the quadratic equation {@code a·x² + b·x + c = 0}.
     *
     * <p>Uses a numerically stable formula (avoiding the catastrophic cancellation
     * of {@code (-b ± √Δ) / 2a} when {@code b²} dominates {@code 4ac}).
     *
     * @throws CalculationError if {@code a} is 0 (it would not be quadratic) or if
     *     a coefficient is not finite
     */
    public static QuadraticEquation solveQuadratic(double a, double b, double c) {
        requireFinite(a, "calc.name.coefA");
        requireFinite(b, "calc.name.coefB");
        requireFinite(c, "calc.name.coefC");
        if (a == 0.0) {
            throw new CalculationError("calc.quadratic.not.quadratic");
        }

        double discriminant = b * b - 4 * a * c;
        if (discriminant == 0.0) {
            Root doubleRoot = new Root(-b / (2 * a), 0);
            return new QuadraticEquation(discriminant, doubleRoot, doubleRoot);
        }
        if (discriminant > 0) {
            double sqrtDelta = Math.sqrt(discriminant);
            double q = -0.5 * (b + Math.copySign(sqrtDelta, b));
            return new QuadraticEquation(discriminant, new Root(q / a, 0), new Root(c / q, 0));
        }
        double realPart = -b / (2 * a);
        double imaginaryPart = Math.sqrt(-discriminant) / (2 * a);
        return new QuadraticEquation(
                discriminant, new Root(realPart, imaginaryPart), new Root(realPart, -imaginaryPart));
    }

    /**
     * Power {@code base}<sup>{@code exponent}</sup>.
     *
     * @throws CalculationError if an argument is not finite or the result is not a
     *     finite real number (0 to a negative power, negative base with a
     *     non-integer exponent, overflow…)
     */
    public static double power(double base, double exponent) {
        requireFinite(base, "calc.name.base");
        requireFinite(exponent, "calc.name.exponent");
        double result = Math.pow(base, exponent);
        if (!Double.isFinite(result)) {
            throw new CalculationError("calc.power.undefined");
        }
        return result;
    }

    /**
     * Root of index {@code index} of {@code radicand}. Allows odd indices of a
     * negative radicand ({@code nthRoot(-8, 3) == -2}).
     *
     * @param index whole number &ge; 2
     * @throws CalculationError if the radicand is not finite, the index is not a
     *     whole number &ge; 2, or an even-index root of a negative is requested
     */
    public static double nthRoot(double radicand, double index) {
        requireFinite(radicand, "calc.name.radicand");
        if (!Double.isFinite(index) || index < 2 || index != Math.rint(index)) {
            throw new CalculationError("calc.root.index");
        }
        int n = (int) index;
        if (radicand < 0) {
            if (n % 2 == 0) {
                throw new CalculationError("calc.root.even.of.negative");
            }
            return -positiveRoot(-radicand, n);
        }
        return positiveRoot(radicand, n);
    }

    /** {@code n}-th root of {@code x >= 0}, refined with Newton so exact roots come out exact. */
    private static double positiveRoot(double x, int n) {
        if (x == 0.0) {
            return 0.0;
        }
        double r = Math.pow(x, 1.0 / n);
        for (int i = 0; i < 3; i++) {
            double rPowN1 = Math.pow(r, n - 1);
            if (rPowN1 == 0.0 || !Double.isFinite(rPowN1)) {
                break;
            }
            r -= (rPowN1 * r - x) / (n * rPowN1);
        }
        return r;
    }

    private static void requireFinite(double value, String nameKey) {
        if (!Double.isFinite(value)) {
            throw new CalculationError("calc.value.not.finite", new CalculationError.Name(nameKey));
        }
    }

    private static void requirePositiveFinite(double value, String nameKey) {
        if (!Double.isFinite(value) || value <= 0) {
            throw new CalculationError("calc.value.not.positive", new CalculationError.Name(nameKey));
        }
    }

    private static void requireNonNegativeFinite(double value, String nameKey) {
        if (!Double.isFinite(value) || value < 0) {
            throw new CalculationError("calc.value.negative", new CalculationError.Name(nameKey));
        }
    }

    /**
     * Immutable result of {@link #solveRightTriangle(double, double)}. The angles
     * are in degrees.
     */
    public record Triangle(
            double legA,
            double legB,
            double hypotenuse,
            double area,
            double perimeter,
            double angleAlpha,
            double angleBeta) {}

    /** A root: {@code real + imaginary·i}. If {@link #isReal()}, {@code imaginary == 0}. */
    public record Root(double real, double imaginary) {
        public boolean isReal() {
            return imaginary == 0.0;
        }
    }

    /** Solution of {@link #solveQuadratic(double, double, double)}. */
    public record QuadraticEquation(double discriminant, Root x1, Root x2) {
        public boolean hasRealRoots() {
            return discriminant >= 0.0;
        }

        public boolean hasDoubleRoot() {
            return discriminant == 0.0;
        }
    }

    /**
     * Result of {@link #analyzePrimality(long)}: whether the number is prime and,
     * when it is composite, its smallest proper divisor ({@code > 1}).
     */
    public record Primality(boolean prime, long smallestProperDivisor) {
        /** {@code true} if the number is composite (has a proper divisor greater than 1). */
        public boolean isComposite() {
            return smallestProperDivisor > 1;
        }
    }

    /** An integer shown in the four usual bases. */
    public record BaseConversion(String binary, String octal, String decimal, String hex) {}

    /** Weight category by BMI (WHO ranges for adults). */
    public enum BmiCategory {
        UNDERWEIGHT,
        NORMAL,
        OVERWEIGHT,
        OBESITY;

        /** Classifies a BMI ({@code >= 0}) into its category. */
        static BmiCategory of(double bmi) {
            if (bmi < 18.5) {
                return UNDERWEIGHT;
            }
            if (bmi < 25.0) {
                return NORMAL;
            }
            if (bmi < 30.0) {
                return OVERWEIGHT;
            }
            return OBESITY;
        }
    }

    /** Result of {@link #bmi(double, double)}: the value and its category. */
    public record BodyMassIndex(double value, BmiCategory category) {}
}
