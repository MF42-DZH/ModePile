/*
 * This library class was created by 0xFC963F18DC21 / Shots243
 * It is part of an extension library for the game NullpoMino (copyright 2010)
 *
 * Herewith shall the term "Library Creator" be given to 0xFC963F18DC21.
 * Herewith shall the term "Game Creator" be given to the original creator of NullpoMino.
 *
 * THIS LIBRARY AND MODE PACK WAS NOT MADE IN ASSOCIATION WITH THE GAME CREATOR.
 *
 * Repository: https://github.com/Shots243/ModePile
 *
 * When using this library in a mode / library pack of your own, the following
 * conditions must be satisfied:
 *     - This license must remain visible at the top of the document, unmodified.
 *     - You are allowed to use this library for any modding purpose.
 *         - If this is the case, the Library Creator must be credited somewhere.
 *             - Source comments only are fine, but in a README is recommended.
 *     - Modification of this library is allowed, but only in the condition that a
 *       pull request is made to merge the changes to the repository.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
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
		if (a == 0) return b;
		else return gcd(b % a, a);
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

	/**
	 * Is almost equal to.
	 * @param a Value
	 * @param b Value
	 * @param eps Exclusive maximum difference
	 * @return Is the difference <= eps?
	 */
	public static boolean almostEqual(double a, double b, double eps){
		return Math.abs(a - b) < eps;
	}

	/**
	 * Checks if a coordinate is within a certain radius.
	 * @param x X-coordinate of circle's centre.
	 * @param y Y-coordinate of circle's centre.
	 * @param xTest X-coordinate of test square.
	 * @param yTest Y-coordinate of test square.
	 * @param radius The testing radius
	 * @return The result of the check. true: within. false: not within.
	 */
	public boolean isCoordWithinRadius(int x, int y, int xTest, int yTest, double radius) {
		int dX = xTest - x;
		int dY = yTest - y;

		double distance = Math.sqrt((dX * dX) + (dY * dY));
		return (distance <= radius);
	}

	/**
	 * Gets the direct distance between two coordinate points.
	 * @param x0 X-coordinate of the first point
	 * @param y0 Y-coordinate of the first point
	 * @param x1 X-coordinate of the second point
	 * @param y1 Y-coordinate of the second point
	 * @return Direct distance between the two points.
	 */
	public double distanceBetween(int x0, int y0, int x1, int y1) {
		int dX, dY;
		dX = x1 - x0; dY = y1 - y0;
		return Math.sqrt((dX * dX) + (dY * dY));
	}
}
