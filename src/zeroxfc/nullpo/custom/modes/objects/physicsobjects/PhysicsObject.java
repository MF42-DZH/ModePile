package zeroxfc.nullpo.custom.modes.objects.physicsobjects;

import zeroxfc.nullpo.custom.libs.*;

public abstract class PhysicsObject {
	public static final int ANCHOR_POINT_TL = 0,
	                        ANCHOR_POINT_TM = 1,
	                        ANCHOR_POINT_TR = 2,
	                        ANCHOR_POINT_ML = 3,
	                        ANCHOR_POINT_MM = 4,
	                        ANCHOR_POINT_MR = 5,
	                        ANCHOR_POINT_LL = 6,
	                        ANCHOR_POINT_LM = 7,
	                        ANCHOR_POINT_LR = 8;

	public static final int PROPERTY_STATIC = 1,
	                        PROPERTY_COLLISION = 2,
	                        PROPERTY_DESTRUCTIBLE = 4;

	public int collisionsToDestroy = -1;
	public DoubleVector position, velocity;
	public int blockSizeX, blockSizeY;

	/**
	 * Gets the object's physics bounding box.
	 * @return Collision bounding box.
	 */
	public double[][] getBoundingBox() {
		return new double[][] { { 0, 0 }, { 0, 0 } };
	}

	public void move() {
		position = DoubleVector.add(position, velocity);
	}

	/**
	 * Checks if two PhysicsObject instances are intersecting each other.
	 * @param a First PhysicsObject instance
	 * @param b Sirst PhysicsObject instance
	 * @return Boolean that says if the instances are intersecting.
	 */
	public static boolean checkCollision(PhysicsObject a, PhysicsObject b) {
		double[][] bboxA = a.getBoundingBox();
		double aMinX = bboxA[0][0];
		double aMinY = bboxA[0][1];
		double aMaxX = bboxA[1][0];
		double aMaxY = bboxA[1][1];

		double[][] bboxB = b.getBoundingBox();
		double bMinX = bboxB[0][0];
		double bMinY = bboxB[0][1];
		double bMaxX = bboxB[1][0];
		double bMaxY = bboxB[1][1];

		boolean intersection = false;

		if ( (aMaxX <= bMinX) || (bMaxX <= aMinX) || (aMaxY <= bMinY) || (bMaxY <= aMinY) || (aMinX <= bMaxX) || (bMinX <= aMaxX) || (aMinY <= bMaxY) || (bMinY <= aMaxY) ) {
			intersection = true;
		}

		return intersection;
	}
}
