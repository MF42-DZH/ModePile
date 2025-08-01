package zeroxfc.nullpo.custom.libs;

import org.apache.log4j.Logger;

public class Interpolation {
    /**
     * Debug Logger
     */
    private static final Logger log = Logger.getLogger(Interpolation.class);

    // This is a static class.
    private Interpolation() {}

    /**
     * Linear interpolation between two <code>int</code> values.
     *
     * @param v0      Start point
     * @param v1      End point
     * @param lerpVal Proportion of point travelled (0 = start, 1 = end)
     * @return Interpolated value as <code>int</code>
     */
    public static int lerp(int v0, int v1, double lerpVal) {
        return (int) ((1.0 - lerpVal) * v0) + (int) (lerpVal * v1);
    }

    /**
     * Linear interpolation between two <code>double</code> values.
     *
     * @param v0      Start point
     * @param v1      End point
     * @param lerpVal Proportion of point travelled (0 = start, 1 = end)
     * @return Interpolated value as <code>double</code>
     */
    public static double lerp(double v0, double v1, double lerpVal) {
        return ((1.0 - lerpVal) * v0) + (lerpVal * v1);
    }

    /**
     * Linear interpolation between two <code>long</code> values.
     *
     * @param v0      Start point
     * @param v1      End point
     * @param lerpVal Proportion of point travelled (0 = start, 1 = end)
     * @return Interpolated value as <code>long</code>
     */
    public static long lerp(long v0, long v1, double lerpVal) {
        return (long) ((1.0 - lerpVal) * v0) + (long) (lerpVal * v1);
    }

    /**
     * Linear interpolation between two <code>float</code> values.
     *
     * @param v0      Start point
     * @param v1      End point
     * @param lerpVal Proportion of point travelled (0 = start, 1 = end)
     * @return Interpolated value as <code>float</code>
     */
    public static float lerp(float v0, float v1, double lerpVal) {
        return (float) ((1.0 - lerpVal) * v0) + (float) (lerpVal * v1);
    }

    /**
     * One dimensional Bézier interpolation
     *
     * @param points Control points for the interpolation (first = start point, last = end point)
     * @param t      Proportion of point travelled (0 = start, 1 = end)
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
     *
     * @param points Control points for the interpolation (first = start point, last = end point)
     * @param t      Proportion of point travelled (0 = start, 1 = end)
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
     *
     * @param points Control points for the interpolation (first = start point, last = end point)
     * @param t      Proportion of point travelled (0 = start, 1 = end)
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
     *
     * @param v0          Start point
     * @param v1          End point
     * @param denominator Step ease scale (denominator > 2 where smaller = closer to linear)
     * @param interpVal   Proportion of point travelled (0 = start, 1 = end)
     * @return Interpolated value as <code>double</code>
     */
    public static double smoothStep(double v0, double v1, double denominator, double interpVal) {
        if (denominator <= 2) denominator = 6;
        double diff = v1 - v0;
        double p1 = diff * (1d / denominator);
        double p2 = diff - p1;

        return bezier1DInterp(new double[] { v0, v0 + p1, v0 + p2, v1 }, interpVal);
    }

    /**
     * Smooth curve interpolation of two <code>double</code> values.
     *
     * @param v0        Start point
     * @param v1        End point
     * @param interpVal Proportion of point travelled (0 = start, 1 = end)
     * @return Interpolated value as <code>double</code>
     */
    public static double smoothStep(double v0, double v1, double interpVal) {
        return smoothStep(v0, v1, 6, interpVal);
    }

    private static final double HALF_PI = Math.PI / 2d;

    /**
     * Sine interpolation of two <code>double</code> values.
     *
     * @param v0        Start point
     * @param v1        End point
     * @param interpVal Proportion of point travelled (0 = start, 1 = end)
     * @return Interpolated value as <code>double</code>
     */
    public static double sineStep(double v0, double v1, double interpVal) {
        final double t = (Math.sin((-HALF_PI) + (interpVal * HALF_PI * 2)) + 1d) / 2d;

        return (1.0 - t) * v0 + v1 * t;
    }

    /**
     * Tangent interpolation of two <code>double</code> values.
     *
     * @param v0        Start point
     * @param v1        End point
     * @param interpVal Proportion of point travelled (0 = start, 1 = end)
     * @return Interpolated value as <code>double</code>
     */
    public static double tanStep(double v0, double v1, double interpVal) {
        final double normIV = interpVal <= 0.5 ? interpVal : (1 - interpVal);
        final double t = Math.tan(normIV * HALF_PI);
        final double fitted = (t * t) / 2.0;

        return interpVal <= 0.5 ? Interpolation.lerp(v0, v1, fitted) : Interpolation.lerp(v0, v1, 1.0 - fitted);
    }

    /**
     * A class representing a class of time-based integer interpolators (for score displays, etc.).
     * Typically, these interpolators are monotonic (providing increasing values).
     */
    public abstract static class IntInterpolator {
        protected int scoreToDisplay;
        protected int targetScore;

        /** Reset all to zero. */
        public abstract void reset();

        /** Reset the time / speed of interpolation, without fully resetting all values to zero. */
        public abstract void resetIncrements();

        /** Perform an interpolation step. Usually run in a mode's {@code onLast} method. */
        public abstract void update();

        /** Set the new target score to reach. */
        public abstract void setTargetScore(int newTargetScore);

        /** Get the current display score. */
        public abstract int getScoreToDisplay();
    }

    /** An interpolator using fibonacci numbers. */
    public static class FibonacciInterpolator extends IntInterpolator {
        private int a;
        private int b;
        private int frame;

        private int incrementTick;
        private final double easeOutFactor;

        public FibonacciInterpolator() {
            this(12, 1d / 12d);
        }

        public FibonacciInterpolator(int incrementTick, double easeOutFactor) {
            assert incrementTick > 0;
            assert easeOutFactor > 0 && easeOutFactor <= 1;

            this.incrementTick = incrementTick;
            this.easeOutFactor = easeOutFactor;

            reset();
        }

        public int getIncrementTick() {
            return incrementTick;
        }

        public void setIncrementTick(int newIncrementTick) {
            incrementTick = newIncrementTick;
            frame = 0;
        }

        @Override
        public void reset() {
            scoreToDisplay = 0;
            targetScore = 0;

            resetIncrements();
        }

        @Override
        public void resetIncrements() {
            a = 1;
            b = 1;
            frame = 0;
        }

        @Override
        public void update() {
            ++frame;

            scoreToDisplay += Math.min(a, (int) Math.ceil((targetScore - scoreToDisplay) * easeOutFactor));
            if (scoreToDisplay >= targetScore || scoreToDisplay < 0) {
                resetIncrements();
            }

            if (frame >= incrementTick && b > 0) {
                final int tmp = a;
                a = b;
                b = tmp + b;

                frame = 0;
            }
        }

        @Override
        public void setTargetScore(int newTargetScore) {
            targetScore = newTargetScore;
        }

        @Override
        public int getScoreToDisplay() {
            return scoreToDisplay;
        }
    }

    /**
     * Classic GameHouse game-style rolling score helper class.
     * Formula provided by leikaisho, but some things have been ignored for a more general implementation.
     */
    public static class GGCE extends IntInterpolator {
        // The current increase in score.
        private int increase;

        // Current increase frame.
        private int frame;

        private final double gainRate;
        private final double easeOutFactor;
        private final boolean fullRate;

        /** Creates a new interpolator with the default values. */
        public GGCE() {
            this(1.2, 0.9, false);
        }

        /**
         * Creates a new interpolator with custom values.
         *
         * @param gainRate      Exponential score gain rate
         * @param easeOutFactor Ease-out factor (smaller = more ease-out)
         * @param fullRate      Run this at 60FPS?
         */
        public GGCE(double gainRate, double easeOutFactor, boolean fullRate) {
            reset();

            this.gainRate = gainRate;
            this.easeOutFactor = easeOutFactor;
            this.fullRate = fullRate;
        }

        private int usedFrame() {
            return fullRate ? frame : (frame >>> 1);
        }

        private int gainIncrease(int f) {
            int gain = 0;

            if (f <= 16) {
                gain = 1 + (f >= 6 ? 1 : 0) + (f >= 9 ? 1 : 0) + Math.max(0, f - 10) + (f == 16 ? 1 : 0);
            } else {
                gain = 10;

                for (int x = 17; x <= f; ++x) {
                    gain = Math.toIntExact(
                        MathHelper.clamp(
                            Math.round(gain * gainRate),
                            0, Integer.MAX_VALUE
                        )
                    );
                }
            }

            return Math.max(gain, 1);
        }

        /**
         * Reset the interpolator completely.
         */
        @Override
        public void reset() {
            scoreToDisplay = 0;
            targetScore = 0;

            resetIncrements();
        }

        /**
         * Set the next target score for the interpolator.
         *
         * @param newTargetScore New target score.
         */
        @Override
        public void setTargetScore(int newTargetScore) {
            this.targetScore = newTargetScore;
        }

        /**
         * Get the current score to display.
         *
         * @return The interpolated score to display.
         */
        @Override
        public int getScoreToDisplay() {
            return scoreToDisplay;
        }

        /**
         * Updates the interpolated score. Use this in {@code onLast}.
         */
        @Override
        public void update() {
            ++frame;

            if (frame % 2 == 0) {
                increase = Math.max(1, increase + gainIncrease(usedFrame()));
            }

            int addition = Math.min(increase, (int) Math.ceil((targetScore - scoreToDisplay) * easeOutFactor));
            if (fullRate) addition = (int) Math.round(addition / 2d);

            if (fullRate || (frame % 2 == 0)) {
                scoreToDisplay += addition;
            }

            if (scoreToDisplay >= targetScore || scoreToDisplay < 0) {
                resetIncrements();
            }
        }

        @Override
        public void resetIncrements() {
            frame = 0;
            increase = 0;
        }
    }
}
