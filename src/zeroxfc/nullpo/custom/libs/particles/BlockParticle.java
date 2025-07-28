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

    private RendererExtension rendererExtension;

    /**
     * Creates a block particle.
     *
     * @param block       Block to import parameters from.
     * @param position    Starting location.
     * @param maxVelocity Highest absolute speed.
     * @param timeToLive  Frames to live.
     * @param randomiser  Randomiser used for setting speed and direction.
     */
    public BlockParticle(Block block, RendererExtension rendererExtension, DoubleVector position, double maxVelocity, int timeToLive, boolean flash, Random randomiser) {
        objectTexture = new Block();
        objectTexture.copy(block);
        this.position = position;
        this.rendererExtension = rendererExtension;

        double speed = randomiser.nextDouble() * maxVelocity;
        double angle = randomiser.nextDouble() * Math.PI * 2;
        velocity = new DoubleVector(speed, angle, true);
        isFlashing = flash;

        maxLifetime = timeToLive;
        currentLifetime = 0;
        size = 1;
    }

    public BlockParticle(Block block, RendererExtension rendererExtension, DoubleVector position, DoubleVector velocity, int timeToLive) {
        objectTexture = new Block();
        objectTexture.copy(block);
        this.rendererExtension = rendererExtension;

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
                rendererExtension.drawScaledBlock(receiver, (int) position.getX() + (int) (((engine.displaysize == 0) ? 2 : 4) * size), (int) position.getY() + ((engine.displaysize == 0) ? 2 : 4),
                    objectTexture.color, objectTexture.skin,
                    objectTexture.getAttribute(Block.BLOCK_ATTRIBUTE_BONE), 0.5f, 1f,
                    ((engine.displaysize == 0) ? 1f : 2f) * size, 0);
            }
            rendererExtension.drawScaledBlock(receiver, (int) position.getX(), (int) position.getY(),
                objectTexture.color, objectTexture.skin,
                objectTexture.getAttribute(Block.BLOCK_ATTRIBUTE_BONE), (isFlashing && ((currentLifetime / 2) % 2 == 0)) ? -0.8f : 0f, (animType == BlockParticleCollection.ANIMATION_DTET) ? 0.667f : 1f,
                ((engine.displaysize == 0) ? 1f : 2f) * size, 0);
        }
    }

    public int getMaxLifetime() {
        return maxLifetime;
    }

    public int getCurrentLifetime() {
        return currentLifetime;
    }
}
