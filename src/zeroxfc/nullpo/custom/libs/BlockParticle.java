package zeroxfc.nullpo.custom.libs;

import java.util.Random;

import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;

public class BlockParticle {
	// Block for use in texture.
	private Block objectTexture;
		
	// Position
	private DoubleVector position;
	
	// Velocity
	private DoubleVector velocity;
	
	// Lifetime
	private int maxLifetime;
		
	// Current time alive
	private int currentLifetime;
	
	// Flash
	private boolean isFlashing;
	
	/**
	 * Creates a block particle.
	 * @param block Block to import parameters from.
	 * @param position Starting location.
	 * @param maxVelocity Highest absolute speed.
	 * @param timeToLive Frames to live.
	 * @param randomiser Randomiser used for setting speed and direction.
	 */
	public BlockParticle(Block block, DoubleVector position, double maxVelocity, int timeToLive, boolean flash, Random randomiser) {
		objectTexture = new Block();
		objectTexture.copy(block);
		this.position = position;
		
		double speed = randomiser.nextDouble() * maxVelocity;
		double angle = randomiser.nextDouble() * Math.PI * 2;
		velocity = new DoubleVector(speed, angle, true);
		isFlashing = flash;
		
		maxLifetime = timeToLive;
		currentLifetime = 0;
	}
	
	public BlockParticle(Block block, DoubleVector position, DoubleVector velocity, int timeToLive) {
		objectTexture = new Block();
		objectTexture.copy(block);
		
		this.position = position;
		this.velocity = velocity;
		
		maxLifetime = timeToLive;
		currentLifetime = 0;
	}
	
	/**
	 * Update position and lifetime data.
	 */
	public void update(int animType) {
		position = DoubleVector.add(position, velocity);

		switch (animType) {
		case BlockParticleCollection.ANIMATION_DTET:
			if (currentLifetime < (maxLifetime * 0.75)) {
				velocity.setDirection(velocity.getDirection() + (Math.PI / maxLifetime));
			}
			break;
		case BlockParticleCollection.ANIMATION_TGM:
			velocity = DoubleVector.add(velocity, new DoubleVector(0, 0.980665 / 2.25, false));
			break;
		}
		
		currentLifetime++;
	}
	
	/**
	 * Draw the block particle.
	 * @param receiver EventReceiver doing the drawing.
	 */
	public void draw(GameEngine engine, EventReceiver receiver, int playerID, int animType) {
		if (engine.displaysize != -1) {			
			if (animType == BlockParticleCollection.ANIMATION_TGM) {
				receiver.drawSingleBlock(engine, playerID,
		                (int)position.getX() + ((engine.displaysize == 0) ? 2 : 4), (int)position.getY() + ((engine.displaysize == 0) ? 2 : 4),
		                objectTexture.color, objectTexture.skin,
		                objectTexture.getAttribute(Block.BLOCK_ATTRIBUTE_BONE), 0.5f, 1f,
		                (engine.displaysize == 0) ? 1f : 2f);
			}
			receiver.drawSingleBlock(engine, playerID,
	                (int)position.getX(), (int)position.getY(),
	                objectTexture.color, objectTexture.skin,
	                objectTexture.getAttribute(Block.BLOCK_ATTRIBUTE_BONE), (isFlashing && ((currentLifetime / 2) % 2 == 0)) ? -0.8f : 0f, (animType == BlockParticleCollection.ANIMATION_DTET) ? 0.667f : 1f,
	                (engine.displaysize == 0) ? 1f : 2f);
		}
	}

	public int getMaxLifetime() {
		return maxLifetime;
	}

	public int getCurrentLifetime() {
		return currentLifetime;
	}
}
