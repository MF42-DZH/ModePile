package zeroxfc.nullpo.custom.libs;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.NavigableMap;
import java.util.TreeMap;
import org.apache.log4j.Logger;
import zeroxfc.nullpo.custom.libs.types.tuples.Pair;

public class MathHelper {
    private static final Logger log = Logger.getLogger(MathHelper.class);

    // This is a static class.
    private MathHelper() {}

    /** Get the max of an arbitrary number of {@code int}s. */
    public static int max(int a, int... ints) {
        for (int i : ints) a = Math.max(a, i);
        return a;
    }

    /** Get the max of an arbitrary number of {@code long}s. */
    public static long max(long a, long... longs) {
        for (long i : longs) a = Math.max(a, i);
        return a;
    }

    /** Get the max of an arbitrary number of {@code float}s. */
    public static float max(float a, float... floats) {
        for (float i : floats) a = Math.max(a, i);
        return a;
    }

    /** Get the max of an arbitrary number of {@code double}s. */
    public static double max(double a, double... doubles) {
        for (double i : doubles) a = Math.max(a, i);
        return a;
    }

    /** Get the min of an arbitrary number of {@code int}s. */
    public static int min(int a, int... ints) {
        for (int i : ints) a = Math.min(a, i);
        return a;
    }

    /** Get the min of an arbitrary number of {@code long}s. */
    public static long min(long a, long... longs) {
        for (long i : longs) a = Math.min(a, i);
        return a;
    }

    /** Get the min of an arbitrary number of {@code float}s. */
    public static float min(float a, float... floats) {
        for (float i : floats) a = Math.min(a, i);
        return a;
    }

    /** Get the min of an arbitrary number of {@code double}s. */
    public static double min(double a, double... doubles) {
        for (double i : doubles) a = Math.min(a, i);
        return a;
    }

    /**
     * Modulo operator that functions similarly to Python's % operator.
     *
     * @param value   Number
     * @param divisor Divisor
     * @return Remainder after division
     */
    public static int pythonModulo(int value, int divisor) {
        int dividend = value % divisor;
        if (dividend < 0) dividend = divisor + dividend;
        return dividend;
    }

    /**
     * Modulo operator that functions similarly to Python's % operator.
     *
     * @param value   Number
     * @param divisor Divisor
     * @return Remainder after division
     */
    public static long pythonModulo(long value, long divisor) {
        long dividend = value % divisor;
        if (dividend < 0) dividend = divisor + dividend;
        return dividend;
    }

    /**
     * Modulo operator that functions similarly to Python's % operator.
     *
     * @param value   Number
     * @param divisor Divisor
     * @return Remainder after division
     */
    public static float pythonModulo(float value, float divisor) {
        float dividend = value % divisor;
        if (dividend < 0) dividend = divisor + dividend;
        return dividend;
    }

    /**
     * Modulo operator that functions similarly to Python's % operator.
     *
     * @param value   Number
     * @param divisor Divisor
     * @return Remainder after division
     */
    public static double pythonModulo(double value, double divisor) {
        double dividend = value % divisor;
        if (dividend < 0) dividend = divisor + dividend;
        return dividend;
    }

    /**
     * Clamps a value to within a range.
     *
     * @param value Value to clamp
     * @param min   Min value
     * @param max   Max value
     * @return Clamped value
     */
    public static int clamp(int value, int min, int max) {
        if (min <= value && value <= max) {
            return value;
        } else if (value < min) {
            return min;
        } else {
            return max;
        }
    }

    /**
     * Clamps a value to within a range.
     *
     * @param value Value to clamp
     * @param min   Min value
     * @param max   Max value
     * @return Clamped value
     */
    public static long clamp(long value, long min, long max) {
        if (min <= value && value <= max) {
            return value;
        } else if (value < min) {
            return min;
        } else {
            return max;
        }
    }

    /**
     * Clamps a value to within a range.
     *
     * @param value Value to clamp
     * @param min   Min value
     * @param max   Max value
     * @return Clamped value
     */
    public static float clamp(float value, float min, float max) {
        if (min <= value && value <= max) {
            return value;
        } else if (value < min) {
            return min;
        } else {
            return max;
        }
    }

    /**
     * Clamps a value to within a range.
     *
     * @param value Value to clamp
     * @param min   Min value
     * @param max   Max value
     * @return Clamped value
     */
    public static double clamp(double value, double min, double max) {
        if (min <= value && value <= max) {
            return value;
        } else if (value < min) {
            return min;
        } else {
            return max;
        }
    }

    /**
     * Gets the greatest common divisor between two integers.
     *
     * @param a int
     * @param b int
     * @return GCD of the two integers
     */
    public static int gcd(int a, int b) {
        int temp;

        while (a != 0) {
            temp = a;
            a = b % temp;
            b = temp;
        }

        return b;
    }

    /**
     * Gets the lowest common multiple between two integers.
     *
     * @param a int
     * @param b int
     * @return LCM of the two integers
     */
    public static int lcm(int a, int b) {
        return (a * b) / gcd(a, b);
    }

    /**
     * Is almost equal to.
     *
     * @param a   Value
     * @param b   Value
     * @param eps Exclusive maximum difference
     * @return Is the difference <= eps?
     */
    public static boolean almostEqual(double a, double b, double eps) {
        return Math.abs(a - b) < eps;
    }

    /**
     * Checks if a coordinate is within a certain radius.
     *
     * @param x      X-coordinate of circle's centre.
     * @param y      Y-coordinate of circle's centre.
     * @param xTest  X-coordinate of test square.
     * @param yTest  Y-coordinate of test square.
     * @param radius The testing radius
     * @return The result of the check. true: within. false: not within.
     */
    public static boolean isCoordWithinRadius(int x, int y, int xTest, int yTest, double radius) {
        int dX = xTest - x;
        int dY = yTest - y;

        double distance = (double) (dX * dX) + (dY * dY);
        return (distance <= radius * radius);
    }

    /**
     * Gets the direct distance between two coordinate points.
     *
     * @param x0 X-coordinate of the first point
     * @param y0 Y-coordinate of the first point
     * @param x1 X-coordinate of the second point
     * @param y1 Y-coordinate of the second point
     * @return Direct distance between the two points.
     */
    public static double distanceBetween(int x0, int y0, int x1, int y1) {
        int dX, dY;
        dX = x1 - x0;
        dY = y1 - y0;

        return Math.sqrt((dX * dX) + (dY * dY));
    }

    /** Gets the number of digits after the decimal point of a {@link java.math.BigDecimal}. */
    public static int decimalPlaces(BigDecimal num) {
        return Math.max(0, num.stripTrailingZeros().scale());
    }

    private static int nonDecimalDigits(BigDecimal num) {
        final BigDecimal stripped = num.stripTrailingZeros();
        return stripped.precision() - stripped.scale();
    }

    private static int log10_approx_lower(BigDecimal num) {
        return nonDecimalDigits(num) - 1;
    }

    private static int log10_approx_upper(BigDecimal num) {
        return nonDecimalDigits(num);
    }

    /** Gets the greatest common divisor of two {@link java.math.BigInteger}s. */
    public static BigInteger gcd(BigInteger a, BigInteger b) {
        BigInteger temp;

        while (a.compareTo(BigInteger.ZERO) != 0) {
            temp = a;
            a = b.mod(temp);
            b = temp;
        }

        return b;
    }

    /** Makes a best approximation for the improper fraction represented by <code>num</code>. */
    public static Pair<BigInteger, BigInteger> asImproperFraction(BigDecimal num) {
        num = num.stripTrailingZeros();
        final int n = decimalPlaces(num);

        final boolean negative = num.compareTo(BigDecimal.ZERO) < 0;
        final BigInteger numeratorRaw = num.movePointRight(n).toBigIntegerExact().abs();
        final BigInteger denominatorRaw = BigInteger.TEN.pow(n);
        final BigInteger div = gcd(numeratorRaw, denominatorRaw);

        if (negative) return Pair.of(numeratorRaw.divide(div).negate(), denominatorRaw.divide(div));
        return Pair.of(numeratorRaw.divide(div), denominatorRaw.divide(div));
    }

    /** Performs a logarithmic-time (ish) iterative exponent of a {@link java.math.BigDecimal} base with a {@link java.math.BigInteger} exponent. */
    public static BigDecimal fastExp(BigDecimal base, BigInteger exp) {
        assert exp.compareTo(BigInteger.ZERO) >= 0;

        BigDecimal result = BigDecimal.ONE;

        while (exp.compareTo(BigInteger.ZERO) > 0) {
            if (exp.mod(new BigInteger("2")).equals(BigInteger.ONE)) result = result.multiply(base);

            base = base.multiply(base).stripTrailingZeros();
            exp = exp.divide(new BigInteger("2"));
        }

        return result;
    }

    private static final NavigableMap<BigInteger, NavigableMap<BigInteger, BigInteger>> PASCAL_CACHE;
    static {
        PASCAL_CACHE = new TreeMap<>();
        final NavigableMap<BigInteger, BigInteger> zeroRow = new TreeMap<>();
        zeroRow.put(BigInteger.ZERO, BigInteger.ONE);

        PASCAL_CACHE.put(BigInteger.ZERO, zeroRow);
    }

    /**
     * Get a value from Pascal's triangle.
     *
     * @param row Row, zero-indexed. [0, inf)
     * @param column Column, zero-indexed. [0, row]
     *
     * @return Value at that position.
     */
    public static BigInteger pascal(BigInteger row, BigInteger column) {
        if (column.compareTo(BigInteger.ZERO) < 0 || column.compareTo(row) > 0) return BigInteger.ZERO;

        final NavigableMap<BigInteger, BigInteger> rowData = PASCAL_CACHE.computeIfAbsent(
            row,
            r -> {
                final NavigableMap<BigInteger, BigInteger> data = new TreeMap<>();
                data.put(BigInteger.ZERO, BigInteger.ONE);
                data.put(r, BigInteger.ONE);

                for (BigInteger c = BigInteger.ONE; c.compareTo(r.divide(BigInteger.valueOf(2))) <= 0; c = c.add(BigInteger.ONE)) {
                    final BigInteger value = data
                        .get(c.subtract(BigInteger.ONE))
                        .multiply(r.add(BigInteger.ONE).subtract(c))
                        .divide(c);

                    data.put(c, value);
                    data.put(r.subtract(c), value);
                }

                return data;
            }
        );

        return rowData.get(column);
    }

    /**
     * Iterative arbitrary precision power for both base and exponent.
     * Method derived from <a href="https://doi.org/10.48550/arXiv.0908.3030">this paper</a>.
     *
     * @param base Number to raise by power
     * @param exp Power
     * @param precision Decimal digit precision
     * @param maxIterations Maximum number of iterations (higher = more accurate)
     *
     * @return Approximation of <code>base ^ exp</code> to <code>precision</code> dp.
     */
    public static BigDecimal bigPow(BigDecimal base, BigDecimal exp, int precision, int maxIterations) {
        assert base.compareTo(BigDecimal.ZERO) >= 0;

        if (base.equals(BigDecimal.ZERO)) return BigDecimal.ZERO;
        if (exp.equals(BigDecimal.ZERO)) return BigDecimal.ONE;
        if (exp.equals(BigDecimal.ONE)) return base;

        final Pair<BigInteger, BigInteger> rawExpFrac = asImproperFraction(exp);

        final boolean isReciprocal = rawExpFrac.valL.compareTo(BigInteger.ZERO) < 0;
        final Pair<BigInteger, BigInteger> expFrac = Pair.of(rawExpFrac.valL.abs(), rawExpFrac.valR);

        // Root - Denominator
        BigDecimal upper = BigDecimal.TEN.pow(
            BigDecimal
                .valueOf(log10_approx_upper(base))
                .divide(new BigDecimal(expFrac.valR), precision, RoundingMode.UP)
                .setScale(0, RoundingMode.UP)
                .toBigInteger()
                .intValue() // I can't believe we have to do this l m a o
        );
        BigDecimal lower = BigDecimal.TEN.pow(
            BigDecimal
                .valueOf(log10_approx_lower(base))
                .divide(new BigDecimal(expFrac.valR), precision, RoundingMode.DOWN)
                .setScale(0, RoundingMode.DOWN)
                .toBigInteger()
                .intValue()
        );
        BigDecimal result = upper.add(lower).divide(BigDecimal.valueOf(2), precision, RoundingMode.HALF_UP);

        for (int i = 0; i < maxIterations && !upper.equals(lower); ++i) {
            final BigDecimal test = upper.add(lower).divide(BigDecimal.valueOf(2), precision, RoundingMode.HALF_UP);
            final int cmp = fastExp(test, expFrac.valR).compareTo(base);

            result = test;

            if (cmp < 0) {
                if (lower.equals(test)) break;
                lower = test;
            } else if (cmp > 0) {
                if (upper.equals(test)) break;
                upper = test;
            } else {
                break;
            }

            if (i == maxIterations - 1) {
                log.warn("Maximum iterations reached for bigPow(precision = " + precision + ", maxIterations = " + maxIterations + ")");
            }
        }

        // Exp - Numerator
        result = fastExp(result, expFrac.valL).stripTrailingZeros().setScale(precision, RoundingMode.HALF_UP);

        if (isReciprocal) return BigDecimal.ONE.divide(result, precision, RoundingMode.HALF_UP).stripTrailingZeros();
        else return result.setScale(precision, RoundingMode.HALF_UP);
    }
}
