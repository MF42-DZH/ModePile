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

import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import org.apache.log4j.Logger;

import java.util.ArrayList;

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
	public int blockSizeX, blockSizeY, anchorPoint, colour, bounces;

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
		this.bounces = 0;

		PROPERTY_Static = velocity.getMagnitude() == 0;
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
	 * Do one movement tick, separated into multiple subticks in order to not merge into / pass through an object.
	 * @param subticks Amount of movement subticks (recommended >= 4)
	 * @param obstacles All obstacles.
	 * @return Did the object collide at all?
	 */
	public boolean move(int subticks, ArrayList<PhysicsObject> obstacles, boolean retract) {
		if (PROPERTY_Static) return false;
		DoubleVector v = DoubleVector.div(velocity, subticks);

		for (int i = 0; i < subticks; i++) {
			if (retract) position = DoubleVector.add(position, v);

			for (PhysicsObject obj : obstacles) {
				if (checkCollision(this, obj)) {
					position = DoubleVector.sub(position, v);
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Do one movement tick, separated into multiple subticks in order to not merge into / pass through an object.
	 * @param subticks Amount of movement subticks (recommended >= 4)
	 * @param obstacles All obstacles.
	 * @return Did the object collide at all?
	 */
	public boolean move(int subticks, PhysicsObject[] obstacles, boolean retract) {
		if (PROPERTY_Static) return false;
		DoubleVector v = DoubleVector.div(velocity, subticks);

		for (int i = 0; i < subticks; i++) {
			position = DoubleVector.add(position, v);

			for (PhysicsObject obj : obstacles) {
				if (checkCollision(this, obj)) {
					if (retract) position = DoubleVector.sub(position, v);
					return true;
				}
			}
		}

		return false;
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
	 * Conducts a flat-surface reflection.
	 * @param vector Velocity vector to modify.
	 * @param vertical <code>true</code> if using a vertical mirror line. <code>false</code> if using a horizontal mirror line.
	 * @param restitution The amount of "bounce". Use a value between 0 and 1.
	 */
	public static void reflectVelocityWithRestitution(DoubleVector vector, boolean vertical, double restitution) {
		if (vertical) {
			vector.setY(vector.getY() * -1);
		} else {
			vector.setX(vector.getX() * -1);
		}

		vector.setMagnitude(vector.getMagnitude() * restitution);
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
			log.error("Failed to create clone.", e);
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

		this.PROPERTY_Static = object.PROPERTY_Static;
		this.PROPERTY_Destructible = object.PROPERTY_Destructible;
		this.PROPERTY_Collision = object.PROPERTY_Collision;
	}
}
