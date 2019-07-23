package zeroxfc.nullpo.custom.libs;

import java.util.Random;

import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;

public class BlockParticleCollection {
	// Block particles in collection
	private BlockParticle[] collectionBlockParticles;
	
	// Animation type
	private int animationType;
	
	// Animation types:
	public static final int ANIMATION_DTET = 0,
							 ANIMATION_TGM = 1;
	
	// Number of anim types
	public static final int ANIMATION_TYPES = 2;
	
	/**
	 * Creates a new collection of particles.
	 * @param length Maximum particle count.
	 */
	public BlockParticleCollection(int length, int animType) {
		collectionBlockParticles = new BlockParticle[length];
		animationType = animType;
		for (int i = 0; i < collectionBlockParticles.length; i++) {
			collectionBlockParticles[i] = null;
		}
	}
	
	public void update() {
		for (int i = 0; i < collectionBlockParticles.length; i++) {
			if (collectionBlockParticles[i] != null) {
				collectionBlockParticles[i].update(animationType);
				
				if (collectionBlockParticles[i].getCurrentLifetime() >= collectionBlockParticles[i].getMaxLifetime()) {
					collectionBlockParticles[i] = null;
				}
			}
		}
	}
	
	public void drawAll(GameEngine engine, EventReceiver receiver, int playerID) {
		for (int i = 0; i < collectionBlockParticles.length; i++) {
			if (collectionBlockParticles[i] != null) {
				collectionBlockParticles[i].draw(engine, receiver, playerID, animationType);
			}
		}
	}
	
	public void addBlock(GameEngine engine, EventReceiver receiver, int playerID, Block block, int x, int y, double maxVelocity, int timeToLive, boolean isFlashing, Random randomiser) {
		int i = 0;
		try {
			while (collectionBlockParticles[i] != null) i++;
		} catch (IndexOutOfBoundsException e) {
			return;  // Do not add block if full.
		}
		
		Block k = new Block();
		k.copy(block);
		
		int v1 = receiver.getFieldDisplayPositionX(engine, playerID) + (engine.displaysize == 0 ? 4 : 8) + (x * (engine.displaysize == 0 ? 16 : 32));
		int v2 = receiver.getFieldDisplayPositionY(engine, playerID) + (engine.displaysize == 0 ? 52 : 104) + (y * (engine.displaysize == 0 ? 16 : 32));
		DoubleVector position = new DoubleVector(v1, v2, false);
		
		timeToLive += (randomiser.nextInt(5) - 2);
		
		collectionBlockParticles[i] = new BlockParticle(k, position, maxVelocity, timeToLive, isFlashing, randomiser);
	}
	
	public void addBlock(GameEngine engine, EventReceiver receiver, int playerID, Block block, int x, int y, int maxX, int yMod, int maxYMod, int timeToLive) {
		int i = 0;
		try {
			while (collectionBlockParticles[i] != null) i++;
		} catch (IndexOutOfBoundsException e) {
			return;  // Do not add block if full.
		}
		
		Block k = new Block();
		k.copy(block);
		
		int v1 = receiver.getFieldDisplayPositionX(engine, playerID) + (engine.displaysize == 0 ? 4 : 8) + (x * (engine.displaysize == 0 ? 16 : 32));
		int v2 = receiver.getFieldDisplayPositionY(engine, playerID) + (engine.displaysize == 0 ? 52 : 104) + (y * (engine.displaysize == 0 ? 16 : 32));
		DoubleVector position = new DoubleVector(v1, v2, false);
		
		double xU = (x - (maxX / 2));
		if (maxX % 2 == 0) xU += 0.5;
		double mod = (1.0 / 3.0) * xU;
		DoubleVector velocity = new DoubleVector(mod, -3.2 * (0.5 + (0.5 * ((double)(maxYMod - yMod) / maxYMod))), false);
		
		collectionBlockParticles[i] = new BlockParticle(k, position, velocity, timeToLive);
	}
	
	public int getCount() {
		int g = 0;
		for (int i = 0; i < collectionBlockParticles.length; i++) {
			if (collectionBlockParticles[i] != null) g++;
		}
		return g;
	}
}
