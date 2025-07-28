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
            if (almostEqualZero(x)) x = 0;

            y = (magnitude * Math.sin(direction));
            if (almostEqualZero(y)) y = 0;

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
    private static boolean almostEqualZero(double a) {
        return Math.abs(a) < 1.0E-8;
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
        if (almostEqualZero(x)) x = 0;

        y = (this.magnitude * Math.sin(direction));
        if (almostEqualZero(y)) y = 0;
    }

    public double getDirection() {
        return direction;
    }

    public void setDirection(double direction) {
        this.direction = direction;

        x = (magnitude * Math.cos(this.direction));
        if (almostEqualZero(x)) x = 0;

        y = (magnitude * Math.sin(this.direction));
        if (almostEqualZero(y)) y = 0;
    }
}
