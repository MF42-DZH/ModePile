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

public class DoubleVector {
    // Fields: X, Y, DIR, MAG
    private double x, y, magnitude, direction;

    /**
     * Generate a double-container vector for handling directions.
     *
     * @param v1   X or Magnitude
     * @param v2   Y or Direction
     * @param mode Use (X, Y) if false, use (Mag., Dir.) otherwise.
     */
    public DoubleVector(double v1, double v2, boolean mode) {
        if (mode) {
            // MAGNITUDE AND DIRECTION
            magnitude = Math.abs(v1);
            direction = v2;

            x = (magnitude * Math.cos(direction));
            if (almostEqual(x, 0, 1E-8)) x = 0;

            y = (magnitude * Math.sin(direction));
            if (almostEqual(y, 0, 1E-8)) y = 0;

        } else {
            // CARTESIAN
            x = v1;
            y = v2;

            magnitude = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
            direction = Math.atan2(y, x);

            if (direction < 0) direction += (2.0 * Math.PI);
        }
    }

    /**
     * Gives a zero vector. For use in blank initialisations.
     */
    public DoubleVector() {
        this(0, 0, false);
    }

    /**
     * Adds two DoubleVector objects; a and b together.
     */
    public static DoubleVector add(DoubleVector a, DoubleVector b) {
        return new DoubleVector(a.getX() + b.getX(), a.getY() + b.getY(), false);
    }

    /**
     * Subtracts DoubleVector b from a.
     */
    public static DoubleVector sub(DoubleVector a, DoubleVector b) {
        return new DoubleVector(a.getX() - b.getX(), a.getY() - b.getY(), false);
    }

    /**
     * Multiples the magnitude of a by b.
     */
    public static DoubleVector mul(DoubleVector a, double b) {
        return new DoubleVector(a.getMagnitude() * b, a.getDirection(), true);
    }

    /**
     * Divides the magnitude of a by b.
     */
    public static DoubleVector div(DoubleVector a, double b) {
        return new DoubleVector(a.getMagnitude() / b, a.getDirection(), true);
    }

    /**
     * Negates DoubleVector a; inverts its direction by pi radians while preserving magnitude.
     */
    public static DoubleVector neg(DoubleVector a) {
        return new DoubleVector(-a.getX(), -a.getY(), false);
    }

    /**
     * Gives a zero vector. For use in blank initialisations.
     *
     * @return A zero vector.
     */
    public static DoubleVector zero() {
        return new DoubleVector(0, 0, false);
    }

    /**
     * Gives a unit vector in a given direction.
     *
     * @param direction Angle of vector.
     * @return Unit vector at angle 'direction'.
     */
    public static DoubleVector unitVector(double direction) {
        return new DoubleVector(1, direction, true);
    }

    /**
     * Gets the distance between two vector tails.
     *
     * @param a Vector 1
     * @param b Vector 2
     * @return A double denoting distance.
     */
    public static double distanceBetween(DoubleVector a, DoubleVector b) {
        double xDiff = Math.abs(a.getX() - b.getX());
        double yDiff = Math.abs(a.getY() - b.getY());

        return Math.sqrt(Math.pow(xDiff, 2) + Math.pow(yDiff, 2));
    }

    /**
     * Returns a vector that represents a vector originating at a and ending at b.
     * (Starts at a, points to and ends at b.)
     *
     * @param a Vector 1.
     * @param b Vector 2.
     * @return DoubleVector that treats a as origin and b as end.
     */
    public static double directionBetween(DoubleVector a, DoubleVector b) {
        DoubleVector pointerVector = DoubleVector.sub(b, a);
        return pointerVector.getDirection();
    }

    // Fuzzy equals.
    private static boolean almostEqual(double a, double b, double eps) {
        return Math.abs(a - b) < eps;
    }

    /*
     * Misc Methods
     */

    /**
     * Adds DoubleVector e to this vector.
     */
    public void add(DoubleVector e) {
        setX(getX() + e.getX());
        setY(getY() + e.getY());
    }

    /**
     * Subtracts DoubleVector e from this vector.
     */
    public void sub(DoubleVector e) {
        setX(getX() - e.getX());
        setY(getY() - e.getY());
    }

    /**
     * Multiplies the magnitude of this vector by e.
     */
    public void mul(double e) {
        setMagnitude(getMagnitude() * e);
    }

    /**
     * Divides the magnitude of this vector by e.
     */
    public void div(double e) {
        setMagnitude(getMagnitude() / e);
    }

    /*
     * Getters / Setters
     */

    /**
     * Rotates this vector by PI radians.
     */
    public void neg(double e) {
        setX(-getX());
        setY(-getY());
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;

        magnitude = Math.sqrt(Math.pow(this.x, 2) + Math.pow(y, 2));
        direction = Math.atan2(y, this.x);

        if (direction < 0) direction += (2.0 * Math.PI);
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;

        magnitude = Math.sqrt(Math.pow(x, 2) + Math.pow(this.y, 2));
        direction = Math.atan2(this.y, x);

        if (direction < 0) direction += (2.0 * Math.PI);
    }

    public double getMagnitude() {
        return magnitude;
    }

    public void setMagnitude(double magnitude) {
        this.magnitude = Math.abs(magnitude);

        x = (this.magnitude * Math.cos(direction));
        if (almostEqual(x, 0, 1E-8)) x = 0;

        y = (this.magnitude * Math.sin(direction));
        if (almostEqual(y, 0, 1E-8)) y = 0;
    }

    public double getDirection() {
        return direction;
    }

    public void setDirection(double direction) {
        this.direction = direction;

        x = (magnitude * Math.cos(this.direction));
        if (almostEqual(x, 0, 1E-8)) x = 0;

        y = (magnitude * Math.sin(this.direction));
        if (almostEqual(y, 0, 1E-8)) y = 0;
    }
}
