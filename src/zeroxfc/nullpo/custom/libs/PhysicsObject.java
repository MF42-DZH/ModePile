package zeroxfc.nullpo.custom.libs;

public class PhysicsObject {
	/**
	 * Anchor points for position.
	 *
	 * This selects where the position represents on the object.
	 *
	 * TL = top-left corner.
	 * TM = top-middle.
	 * TR = top-right corner.
	 * ML = middle-left.
	 * MM = centre.
	 * MR = middle-right.
	 * LL = bottom-left corner.
	 * LM = bottom-middle.
	 * LR = bottom-right corner.
	 */
	public static final int ANCHOR_POINT_TL = 0,
	                        ANCHOR_POINT_TM = 1,
	                        ANCHOR_POINT_TR = 2,
	                        ANCHOR_POINT_ML = 3,
	                        ANCHOR_POINT_MM = 4,
	                        ANCHOR_POINT_MR = 5,
	                        ANCHOR_POINT_LL = 6,
	                        ANCHOR_POINT_LM = 7,
	                        ANCHOR_POINT_LR = 8;

	/** Property identitifiers. Is set automatically during constructions but can be overridden by external methods.
	 *
	 * PROPERTY_Static is default true if the object cannot move.
	 * PROPERTY_Collision is default true if the object can collide with other objects.
	 * PROPERTY_Destructible is default true if the object can be destroyed.
	 */
	public boolean PROPERTY_Static, PROPERTY_Collision, PROPERTY_Destructible;

	/** Object "Health". Set to -1 to make indestructible. */
	public int collisionsToDestroy;

	public DoubleVector position, velocity;
	public int blockSizeX, blockSizeY, anchorPoint, colour;

	public PhysicsObject() {
		this(new DoubleVector(0, 0, false), new DoubleVector(0, 0, false), -1, 1, 1, 0, 1);
	}

	public PhysicsObject(DoubleVector position, DoubleVector velocity, int collisionsToDestroy, int blockSizeX, int blockSizeY, int anchorPoint, int colour) {
		this.position = position;
		this.velocity = velocity;
		this.collisionsToDestroy = collisionsToDestroy;
		this.blockSizeX = blockSizeX;
		this.blockSizeY = blockSizeY;
		this.anchorPoint = anchorPoint;
		this.colour = colour;

		PROPERTY_Static = velocity.getMagnitude() != 0;
		PROPERTY_Destructible = collisionsToDestroy > 0;
		PROPERTY_Collision = true;
	}

	/**
	 * Gets the object's physics bounding box. (Top-Left and Bottom-Right coordinate).
	 * Please override this.
	 * @return Collision bounding box.
	 */
	public double[][] getBoundingBox() {
		int baseSize = 16;

		int sizeX = blockSizeX * baseSize;
		int sizeY = blockSizeY * baseSize;

		double[] anchor = new double[] { 0 , 0 };
		switch (anchorPoint) {
			case ANCHOR_POINT_TL:
				anchor = new double[] { 0, 0 };
				break;
			case ANCHOR_POINT_TM:
				anchor = new double[] { sizeX / 2, 0 };
				break;
			case ANCHOR_POINT_TR:
				anchor = new double[] { sizeX - 1, 0 };
				break;
			case ANCHOR_POINT_ML:
				anchor = new double[] { 0, sizeY / 2 };
				break;
			case ANCHOR_POINT_MM:
				anchor = new double[] { sizeX / 2, sizeY / 2 };
				break;
			case ANCHOR_POINT_MR:
				anchor = new double[] { sizeX - 1, sizeY / 2 };
				break;
			case ANCHOR_POINT_LL:
				anchor = new double[] { 0, sizeY - 1 };
				break;
			case ANCHOR_POINT_LM:
				anchor = new double[] { sizeX / 2, sizeY - 1 };
				break;
			case ANCHOR_POINT_LR:
				anchor = new double[] { sizeX - 1, sizeY - 1 };
				break;
		}

		return new double[][] { { position.getX() - anchor[0], position.getY() - anchor[1] }, { position.getX() + sizeX - anchor[0], position.getY() + sizeY - anchor[1] } };
	}

	/**
	 * Do one movement tick.
	 */
	public void move() {
		if (!PROPERTY_Static) position = DoubleVector.add(position, velocity);
	}

	/**
	 * Checks if two PhysicsObject instances are intersecting each other.
	 * If either instance has collisions disabled,
	 * @param a First PhysicsObject instance
	 * @param b Sirst PhysicsObject instance
	 * @return Boolean that says if the instances are intersecting.
	 */
	public static boolean checkCollision(PhysicsObject a, PhysicsObject b) {
		if (!a.PROPERTY_Collision) return false;
		if (!b.PROPERTY_Collision) return false;

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

	/**
	 * Clones the current PhysicsObject instance.
	 * @return A complete clone of the current PhysicsObject instance.
	 * @throws CloneNotSupportedException If clone is not suported.
	 */
	public PhysicsObject clone() throws CloneNotSupportedException {
		PhysicsObject clone = (PhysicsObject) super.clone();

		clone.position = position;
		clone.velocity = velocity;
		clone.collisionsToDestroy = collisionsToDestroy;
		clone.blockSizeX = blockSizeX;
		clone.blockSizeY = blockSizeY;
		clone.anchorPoint = anchorPoint;
		clone.colour = colour;

		clone.PROPERTY_Static = PROPERTY_Static;
		clone.PROPERTY_Destructible = PROPERTY_Destructible;
		clone.PROPERTY_Collision = PROPERTY_Collision;

		return clone;
	}

	/**
	 * Copies another PhysicsObject instance's fields to this instance.
	 * @param object PhysicsObject to use fields from.
	 */
	public void copy(PhysicsObject object) {
		this.position = object.position;
		this.velocity = object.velocity;
		this.collisionsToDestroy = object.collisionsToDestroy;
		this.blockSizeX = object.blockSizeX;
		this.blockSizeY = object.blockSizeY;
		this.anchorPoint = object.anchorPoint;
		this.colour = object.colour;

		PROPERTY_Static = object.PROPERTY_Static;
		PROPERTY_Destructible = object.PROPERTY_Destructible;
		PROPERTY_Collision = object.PROPERTY_Collision;
	}
}
