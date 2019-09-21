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

import org.apache.log4j.Logger;

import java.util.Arrays;

public class Interpolation {
	/** Debug Logger */
	private static final Logger log = Logger.getLogger(Interpolation.class);

	/**
	 * Linear interpolation between two <code>int</code> values.
	 * @param v0 Start point
	 * @param v1 End point
	 * @param lerpVal Proportion of point travelled (0 = start, 1 = end)
	 * @return Interpolated value as <code>int</code>
	 */
	public static int lerp(int v0, int v1, double lerpVal) {
		return (int)((1.0 - lerpVal) * v0) + (int)(lerpVal * v1);
	}

	/**
	 * Linear interpolation between two <code>double</code> values.
	 * @param v0 Start point
	 * @param v1 End point
	 * @param lerpVal Proportion of point travelled (0 = start, 1 = end)
	 * @return Interpolated value as <code>double</code>
	 */
	public static double lerp(double v0, double v1, double lerpVal) {
		return ((1.0 - lerpVal) * v0) + (lerpVal * v1);
	}

	/**
	 * Linear interpolation between two <code>long</code> values.
	 * @param v0 Start point
	 * @param v1 End point
	 * @param lerpVal Proportion of point travelled (0 = start, 1 = end)
	 * @return Interpolated value as <code>long</code>
	 */
	public static long lerp(long v0, long v1, double lerpVal) {
		return (long)((1.0 - lerpVal) * v0) + (long)(lerpVal * v1);
	}

	/**
	 * Linear interpolation between two <code>float</code> values.
	 * @param v0 Start point
	 * @param v1 End point
	 * @param lerpVal Proportion of point travelled (0 = start, 1 = end)
	 * @return Interpolated value as <code>float</code>
	 */
	public static float lerp(float v0, float v1, double lerpVal) {
		return (float)((1.0 - lerpVal) * v0) + (float)(lerpVal * v1);
	}

	/**
	 * One dimensional Bézier interpolation
	 * @param points Control points for the interpolation (first = start point, last = end point)
	 * @param t Proportion of point travelled (0 = start, 1 = end)
	 * @return Interpolated value as <code>double</code>
	 */
	public static double bezier1DInterp(double[] points, double t) {
		if (points.length == 1) {
			return points[0];  // Only one point, return it.
		} else {
			double[] np1 = new double[points.length - 1], np2 = new double[points.length - 1];
			for (int i = 0; i < points.length - 1; i++) {
				np1[i] = points[i];      // Get all points except last
				np2[i] = points[i + 1];  // Get all points except first
			}
			double nv1 = bezier1DInterp(np1, t), nv2 = bezier1DInterp(np2, t);  // Recursive call
			double nt = 1d - t;  // Inverse value
			return (nt * nv1 + t * nv2);
		}
	}

	/**
	 * Two dimensional Bézier interpolation, requires a value pair within each
	 * @param points Control points for the interpolation (first = start point, last = end point)
	 * @param t Proportion of point travelled (0 = start, 1 = end)
	 * @return Interpolated value as <code>double[]</code>
	 */
	public static double[] bezier2DInterp(double[][] points, double t) {
		if (points.length == 1) {
			return points[0];
		} else {
			double[][] np1 = new double[points.length - 1][], np2 = new double[points.length - 1][];
			for (int i = 0; i < points.length - 1; i++) {
				np1[i] = points[i];
				np2[i] = points[i + 1];
			}
			double[] nv1 = bezier2DInterp(np1, t), nv2 = bezier2DInterp(np2, t);
			double nt = 1d - t;
			return new double[] { nt * nv1[0] + t * nv2[0], nt * nv1[1] + t * nv2[1] };
		}
	}

	/**
	 * N-dimensional Bézier interpolation, requires a value set within each.<br />
	 * Each value set must contain the same number of values.
	 * @param points Control points for the interpolation (first = start point, last = end point)
	 * @param t Proportion of point travelled (0 = start, 1 = end)
	 * @return Interpolated value as <code>double[]</code>
	 */
	public static double[] bezierNDInterp(double[][] points, double t) {
		if (points.length == 1) {
			return points[0];
		} else {
			double[][] np1 = new double[points.length - 1][], np2 = new double[points.length - 1][];
			for (int i = 0; i < points.length - 1; i++) {
				np1[i] = points[i];
				np2[i] = points[i + 1];
			}
			double[] nv1 = bezierNDInterp(np1, t), nv2 = bezierNDInterp(np2, t);
			double nt = 1d - t;

			double[] result = new double[nv1.length];
			for (int i = 0; i < result.length; i++) {
				result[i] = nt * nv1[i] + t * nv2[i];
			}

			return result;
		}
	}

	/**
	 * Smooth curve interpolation of two <code>double</code> values.
	 * @param v0 Start point
	 * @param v1 End point
	 * @param denominator Step ease scale (denominator > 2 where smaller = closer to linear)
	 * @param interpVal Proportion of point travelled (0 = start, 1 = end)
	 * @return Interpolated value as <code>double</code>
	 */
	public static double smoothStep(double v0, double v1, double denominator, double interpVal) {
		if (denominator <= 2) denominator = 6;
		double diff = v1 - v0;
		double p1 = diff * (1d / denominator);
		double p2 = diff - p1;

		// log.debug(Arrays.toString(new double[] { v0, v0 + p1, v0 + p2, v1 }));
		// log.debug(Arrays.toString(new double[] { p1, p2}));
		// log.debug(lerpVal);

		return bezier1DInterp(new double[] { v0, v0 + p1, v0 + p2, v1 }, interpVal);
	}

	/**
	 * Smooth curve interpolation of two <code>double</code> values.
	 * @param v0 Start point
	 * @param v1 End point
	 * @param interpVal Proportion of point travelled (0 = start, 1 = end)
	 * @return Interpolated value as <code>double</code>
	 */
	public static double smoothStep(double v0, double v1, double interpVal) {
		return smoothStep(v0, v1, 6, interpVal);
	}

	/**
	 * Sine interpolation of two <code>double</code> values.
	 * @param v0 Start point
	 * @param v1 End point
	 * @param interpVal Proportion of point travelled (0 = start, 1 = end)
	 * @return Interpolated value as <code>double</code>
	 */
	public static double sineStep(double v0, double v1, double interpVal) {
		final double OFFSET = Math.PI / 4d;
		final double t = (Math.sin((-1d * OFFSET) + interpVal * OFFSET) + 1d) / 2d;

		return (1.0 - t) * v0 + v1 * t;
	}
}
