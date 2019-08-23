package zeroxfc.nullpo.custom.libs;

public class MathHelper {
	/**
	 * Modulo operator that functions similarly to Python's % operator.
	 * @param value Number
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
	 * @param value Number
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
	 * @param value Value to clamp
	 * @param min Min value
	 * @param max Max value
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
	 * @param value Value to clamp
	 * @param min Min value
	 * @param max Max value
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
	 * @param value Value to clamp
	 * @param min Min value
	 * @param max Max value
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
	 * @param value Value to clamp
	 * @param min Min value
	 * @param max Max value
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
	 * Gets the greatest common divisor between two integers.<br />
	 * Recursive function.
	 * @param a int
	 * @param b int
	 * @return GCD of the two integers
	 */
	public static int gcd(int a, int b) {
		if (a == 0)
			return b;
		return gcd(b % a, a);
	}

	/**
	 * Gets the lowest common multiple between two integers.<br />
	 * Calls <code>gcd(a, b)</code>, a recursive function.
	 * @param a int
	 * @param b int
	 * @return LCM of the two integers
	 */
	public static int lcm(int a, int b) {
		return (a * b) / gcd(a, b);
	}
}
