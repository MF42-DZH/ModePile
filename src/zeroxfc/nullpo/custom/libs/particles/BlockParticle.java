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
package zeroxfc.nullpo.custom.libs.particles;

import java.util.Random;
import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import zeroxfc.nullpo.custom.libs.DoubleVector;
import zeroxfc.nullpo.custom.libs.RendererExtension;

public class BlockParticle {
    // Lifetime
    private final int maxLifetime;
    // Block for use in texture.
    private final Block objectTexture;
    // Position
    private DoubleVector position;
    // Velocity
    private DoubleVector velocity;
    // Size
    private float size;
    // Current time alive
    private int currentLifetime;

    // Flash
    private boolean isFlashing;

    /**
     * Creates a block particle.
     *
     * @param block       Block to import parameters from.
     * @param position    Starting location.
     * @param maxVelocity Highest absolute speed.
     * @param timeToLive  Frames to live.
     * @param randomiser  Randomiser used for setting speed and direction.
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
        size = 1;
    }

    public BlockParticle(Block block, DoubleVector position, DoubleVector velocity, int timeToLive) {
        objectTexture = new Block();
        objectTexture.copy(block);

        this.position = position;
        this.velocity = velocity;

        maxLifetime = timeToLive;
        currentLifetime = 0;
        size = 1;
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
                size += (1f / 60f);
                break;
        }

        currentLifetime++;
    }

    /**
     * Draw the block particle.
     *
     * @param receiver EventReceiver doing the drawing.
     */
    public void draw(GameEngine engine, EventReceiver receiver, int playerID, int animType) {
        if (engine.displaysize != -1) {
            if (animType == BlockParticleCollection.ANIMATION_TGM) {
                RendererExtension.drawScaledBlock(receiver, (int) position.getX() + (int) (((engine.displaysize == 0) ? 2 : 4) * size), (int) position.getY() + ((engine.displaysize == 0) ? 2 : 4),
                    objectTexture.color, objectTexture.skin,
                    objectTexture.getAttribute(Block.BLOCK_ATTRIBUTE_BONE), 0.5f, 1f,
                    ((engine.displaysize == 0) ? 1f : 2f) * size, 0);
//				receiver.drawSingleBlock(engine, playerID,
//		                (int)position.getX() + ((engine.displaysize == 0) ? 2 : 4), (int)position.getY() + ((engine.displaysize == 0) ? 2 : 4),
//		                objectTexture.color, objectTexture.skin,
//		                objectTexture.getAttribute(Block.BLOCK_ATTRIBUTE_BONE), 0.5f, 1f,
//		                (engine.displaysize == 0) ? 1f : 2f);
            }
            RendererExtension.drawScaledBlock(receiver, (int) position.getX(), (int) position.getY(),
                objectTexture.color, objectTexture.skin,
                objectTexture.getAttribute(Block.BLOCK_ATTRIBUTE_BONE), (isFlashing && ((currentLifetime / 2) % 2 == 0)) ? -0.8f : 0f, (animType == BlockParticleCollection.ANIMATION_DTET) ? 0.667f : 1f,
                ((engine.displaysize == 0) ? 1f : 2f) * size, 0);
//			receiver.drawSingleBlock(engine, playerID,
//	                (int)position.getX(), (int)position.getY(),
//	                objectTexture.color, objectTexture.skin,
//	                objectTexture.getAttribute(Block.BLOCK_ATTRIBUTE_BONE), (isFlashing && ((currentLifetime / 2) % 2 == 0)) ? -0.8f : 0f, (animType == BlockParticleCollection.ANIMATION_DTET) ? 0.667f : 1f,
//	                (engine.displaysize == 0) ? 1f : 2f);
        }
    }

    public int getMaxLifetime() {
        return maxLifetime;
    }

    public int getCurrentLifetime() {
        return currentLifetime;
    }
}
