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
 *     - Only binaries of this library are allowed to be distributed unless at the
 *       permission of the Library Creator.
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
