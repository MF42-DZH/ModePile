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
 *     - Only binaries of this library are allowed to be distributed unless at the
 *       permission of the Library Creator.
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
