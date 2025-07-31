package zeroxfc.nullpo.custom.libs;

public class MathHelper {
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
}
