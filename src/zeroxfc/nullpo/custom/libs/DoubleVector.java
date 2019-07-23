package zeroxfc.nullpo.custom.libs;

public class DoubleVector {
	// Fields: X, Y, DIR, MAG
	private double x, y, magnitude, direction;
	
	/**
	 * Generate a double-container vector for handling directions.
	 * @param v1 X or Magnitude
	 * @param v2 Y or Direction
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
	 * Negates DoubleVector a; inverts its direction by pi radians while preserving magnitude.
	 */
	public static DoubleVector neg(DoubleVector a) {
		return new DoubleVector(- a.getX(), - a.getY(), false);
	}
	
	/*
	 * Misc Methods
	 */
	
	/**
	 * Gives a zero vector. For use in blank initialisations.
	 * @return A zero vector.
	 */
	public static DoubleVector zero() {
		return new DoubleVector(0, 0, false);
	}
	
	/**
	 * Gives a unit vector in a given direction.
	 * @param direction Angle of vector.
	 * @return Unit vector at angle 'direction'.
	 */
	public static DoubleVector unitVector(double direction) {
		return new DoubleVector(1, direction, true);
	}
	
	/**
	 * Gets the distance between two vector tails.
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
	 * @param a Vector 1.
	 * @param b Vector 2.
	 * @return DoubleVector that treats a as origin and b as end.
	 */
	public static double directionBetween(DoubleVector a, DoubleVector b) {
		DoubleVector pointerVector = DoubleVector.sub(b, a);
		return pointerVector.getDirection();
	}
	
	/*
	 * Getters / Setters
	 */
	
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

	// Fuzzy equals.
	private static boolean almostEqual(double a, double b, double eps){
	    return Math.abs(a - b) < eps;
	}
}
