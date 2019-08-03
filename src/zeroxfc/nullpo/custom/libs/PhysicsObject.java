package zeroxfc.nullpo.custom.libs;

import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import org.apache.log4j.Logger;

/**
 * PhysicsObject
 *
 * Gravity-less square physics objects.
 * Has settable anchor points for more convenient location handling.
 * Has a bounding box collision system.
 */
public class PhysicsObject implements Cloneable {
	private static Logger log = Logger.getLogger(PhysicsObject.class);

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
		final int baseSize = 16;

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
	 * Get x-coordinate of top-left corner of bounding box.
	 * @return x-coordinate
	 */
	public double getMinX() {
		return getBoundingBox()[0][0];
	}

	/**
	 * Get y-coordinate of top-left corner of bounding box.
	 * @return y-coordinate
	 */
	public double getMinY() {
		return getBoundingBox()[0][1];
	}

	/**
	 * Get x-coordinate of bottom-right corner of bounding box.
	 * @return x-coordinate
	 */
	public double getMaxX() {
		return getBoundingBox()[1][0];
	}

	/**
	 * Get y-coordinate of bottom-right corner of bounding box.
	 * @return y-coordinate
	 */
	public double getMaxY() {
		return getBoundingBox()[1][1];
	}

	/**
	 * Draw instance blocks to engine.
	 * @param receiver Block renderer.
	 * @param engine Current GameEngine.
	 * @param playerID Current player ID.
	 */
	public void draw(EventReceiver receiver, GameEngine engine, int playerID) {
		int size = 16;
		for (int y = 0; y < blockSizeY; y++) {
			for (int x = 0; x < blockSizeX; x++) {
				receiver.drawSingleBlock(engine, playerID, (int)getMinX() + (x * size), (int)getMinY() + (y * size), colour, engine.getSkin(), false, 0f,1f,1f);
			}
		}
	}

	/**
	 * Do one movement tick.
	 */
	public void move() {
		if (!PROPERTY_Static) position = DoubleVector.add(position, velocity);
	}

	/**
	 * Do one movement tick with a custom velocity.
	 */
	public void move(DoubleVector velocity) {
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

		if (aMaxX >= bMinX && aMaxX <= bMaxX && aMaxY >= bMinY && aMaxY <= bMaxY) {
			intersection = true;
		} else if (aMinX >= bMinX && aMinX <= bMaxX && aMinY >= bMinY && aMinY <= bMaxY) {
			intersection = true;
		} else if (bMaxX >= aMinX && bMaxX <= aMaxX && bMaxY >= aMinY && bMaxY <= aMaxY) {
			intersection = true;
		} else if (bMinX >= aMinX && bMinX <= aMaxX && bMinY >= aMinY && bMinY <= aMaxY) {
			intersection = true;
		}

		return intersection;
	}

	/**
	 * Conducts a flat-surface reflection.
	 * @param vector Velocity vector to modify.
	 * @param vertical <code>true</code> if using a vertical mirror line. <code>false</code> if using a horizontal mirror line.
	 */
	public static void reflectVelocity(DoubleVector vector, boolean vertical) {
		if (vertical) {
			vector.setY(vector.getY() * -1);
		} else {
			vector.setX(vector.getX() * -1);
		}
	}

	/**
	 * Clones the current PhysicsObject instance.
	 * @return A complete clone of the current PhysicsObject instance.
	 */
	public PhysicsObject clone() {
		PhysicsObject clone;
		try {
			clone = (PhysicsObject) super.clone();

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
		} catch (Exception e) {
			// Do nothing, but log error.
			log.error(e.getMessage());
			return null;
		}
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
