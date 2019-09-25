package zeroxfc.nullpo.custom.libs;

import mu.nu.nullpo.game.component.*;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class FieldScatter {
	/** Gravity */
	private static final DoubleVector GRAVITY = new DoubleVector(0, 1d / 6d, false);

	/** Blocks to draw */
	private ArrayList<PhysicsObject> blocks;

	/** Lifetime */
	private int lifeTime;

	/**
	 * Makes a new field explosion. Destroys blocks by default.
	 * @param receiver  Renderer to draw with.
	 * @param engine    Current GameEngine.
	 * @param playerID  Current player ID.
	 */
	public FieldScatter(EventReceiver receiver, GameEngine engine, int playerID) {
		this(receiver, engine, playerID, true);
	}

	/**
	 * Makes a new field explosion.
	 * @param receiver     Renderer to draw with.
	 * @param engine       Current GameEngine.
	 * @param playerID     Current player ID.
	 * @param clearBlocks  Clear the blocks in the field?
	 */
	public FieldScatter(EventReceiver receiver, GameEngine engine, int playerID, boolean clearBlocks) {
		// Direction randomiser
		Random rdm = new Random(engine.randSeed + engine.statistics.time);
		lifeTime = 0;

		Block eBlock = new Block(0, engine.getSkin());
		blocks = new ArrayList<>();
		for (int i = -1 * engine.field.getHiddenHeight(); i < engine.field.getHeight(); i++) {
			for (int j = 0; j < engine.field.getWidth(); j++) {
				// Generate object array.
				Block blk = engine.field.getBlock(j, i);
				if (blk.color > 0) {
					blocks.add(new PhysicsObject(

							new DoubleVector(
									receiver.getFieldDisplayPositionX(engine, playerID) + 4 + j * 16,
									receiver.getFieldDisplayPositionY(engine, playerID) + 52 + i * 16,
									false
							),
							new DoubleVector(
									rdm.nextDouble() * 8, rdm.nextDouble() * Math.PI * 2, true
							),
							-1, 1, 1, PhysicsObject.ANCHOR_POINT_TL, blk.color
					));

					if (clearBlocks) receiver.blockBreak(engine, playerID, j, i, blk);

					if (clearBlocks) blk.copy(eBlock);
				}
			}
		}
	}

	/**
	 * Makes a new field explosion. Destroys specfic blocks.
	 * @param receiver             Renderer to draw with.
	 * @param engine               Current GameEngine.
	 * @param playerID             Current player ID.
	 * @param fieldBlockLocations  Blocck locations.
	 */
	public FieldScatter(EventReceiver receiver, GameEngine engine, int playerID, ArrayList<int[]> fieldBlockLocations) {
		/** Direction randomiser */
		Random rdm = new Random(engine.randSeed + engine.statistics.time);
		lifeTime = 0;

		Block eBlock = new Block(0, engine.getSkin());
		blocks = new ArrayList<>();
		for (int[] loc : fieldBlockLocations) {
			int j = loc[0];
			int i = loc[1];

			// Generate object array.
			Block blk = engine.field.getBlock(j, i);
			if (blk.color > 0) {
				blocks.add(new PhysicsObject(

						new DoubleVector(
								receiver.getFieldDisplayPositionX(engine, playerID) + 4 + j * 16,
								receiver.getFieldDisplayPositionY(engine, playerID) + 52 + i * 16,
								false
						),
						new DoubleVector(
								rdm.nextDouble() * 8, rdm.nextDouble() * Math.PI * 2, true
						),
						-1, 1, 1, PhysicsObject.ANCHOR_POINT_TL, blk.color
				));

				receiver.blockBreak(engine, playerID, j, i, blk);

				blk.copy(eBlock);
			}
		}
	}

	/**
	 * Makes a new field explosion. Destroys specfic blocks.
	 * @param receiver             Renderer to draw with.
	 * @param engine               Current GameEngine.
	 * @param playerID             Current player ID.
	 * @param fieldBlockLocations  Blocck locations.
	 */
	public FieldScatter(EventReceiver receiver, GameEngine engine, int playerID, int[][] fieldBlockLocations) {
		this(receiver, engine, playerID, (ArrayList<int[]>)Arrays.asList(fieldBlockLocations));
	}

	/**
	 * Updates the life cycle of the explosion.
	 */
	public void update() {
		if (shouldNull()) return;
		for (PhysicsObject pho /* NOTE: Delicious */ : blocks) {
//			if ((pho.position.getX() <= -16 || pho.position.getX() > 640)) continue;
//			if ((pho.position.getY() > 460 && Math.abs(pho.velocity.getMagnitude()) < 0.0001)) continue;

			pho.move();

			if (pho.position.getY() > 465) {
				PhysicsObject.reflectVelocityWithRestitution(pho.velocity, true, 0.75);
				while (pho.position.getY() > 465) pho.move();
			}
			pho.velocity = DoubleVector.add(pho.velocity, GRAVITY);
		}

		blocks.removeIf(block -> (block.position.getX() <= -16 || block.position.getX() > 640));
		blocks.removeIf(block -> (block.position.getY() > 460 && Math.abs(block.velocity.getMagnitude()) < 0.0001));

		lifeTime++;
	}

	/**
	 * Draws the explosion.
	 * @param receiver  Renderer to draw explosion with.
	 * @param engine    Current GameEngine.
	 * @param playerID  Current playerID.
	 */
	public void draw(EventReceiver receiver, GameEngine engine, int playerID) {
		if (shouldNull()) return;
		for (PhysicsObject pho : blocks) {
//			if ((pho.position.getX() <= -16 || pho.position.getX() > 640)) continue;
//			if ((pho.position.getY() > 460 && Math.abs(pho.velocity.getMagnitude()) < 0.0001)) continue;

			pho.draw(receiver, engine, playerID);
		}
	}

	/**
	 * Checks if the object's lifetime has expired.
	 * @return Expiry status
	 */
	public boolean shouldNull() {
		return lifeTime >= 600;
	}
}