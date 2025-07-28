package zeroxfc.nullpo.custom.libs.particles;

import java.util.Arrays;
import java.util.Random;
import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import zeroxfc.nullpo.custom.libs.DoubleVector;
import zeroxfc.nullpo.custom.libs.RendererExtension;

public class BlockParticleCollection {
    // Animation types:
    public static final int ANIMATION_DTET = 0,
        ANIMATION_TGM = 1;
    // Number of anim types
    public static final int ANIMATION_TYPES = 2;
    // Block particles in collection
    private final BlockParticle[] collectionBlockParticles;
    // Animation type
    private final int animationType;

    private RendererExtension rendererExtension;

    /**
     * Creates a new collection of particles.
     *
     * @param length Maximum particle count.
     */
    public BlockParticleCollection(RendererExtension rendererExtension, int length, int animType) {
        collectionBlockParticles = new BlockParticle[length];
        animationType = animType;
        Arrays.fill(collectionBlockParticles, null);

        this.rendererExtension = rendererExtension;
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
        for (BlockParticle collectionBlockParticle : collectionBlockParticles) {
            if (collectionBlockParticle != null) {
                collectionBlockParticle.draw(engine, receiver, playerID, animationType);
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

        collectionBlockParticles[i] = new BlockParticle(k, rendererExtension, position, maxVelocity, timeToLive, isFlashing, randomiser);
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

        double xU = (x - (maxX / 2d));
        if (maxX % 2 == 0) xU += 0.5;
        double mod = (1.0 / 3.0) * xU;
        DoubleVector velocity = new DoubleVector(mod * 1.1d, -4.8 * (0.5 + (0.5 * ((double) (maxYMod - yMod) / maxYMod))), false);

        collectionBlockParticles[i] = new BlockParticle(k, rendererExtension, position, velocity, timeToLive);
    }

    public int getCount() {
        int g = 0;
        for (BlockParticle collectionBlockParticle : collectionBlockParticles) {
            if (collectionBlockParticle != null) g++;
        }
        return g;
    }
}
