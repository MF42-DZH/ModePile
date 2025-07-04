package zeroxfc.nullpo.custom.libs.particles;

import java.util.Arrays;
import java.util.Random;
import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import zeroxfc.nullpo.custom.libs.CustomResourceHolder;
import zeroxfc.nullpo.custom.libs.DoubleVector;
import zeroxfc.nullpo.custom.libs.Interpolation;
import zeroxfc.nullpo.custom.libs.MathHelper;

public class LandingParticles extends ParticleEmitterBase<LandingParticles.Parameters> implements BlockBasedEmitter {
    /**
     * Default max velocity
     */
    public static final double DEF_MAX_VEL = 2.5d;

    /**
     * Default min lifetime
     */
    public static final int DEF_MIN_LIFE = 15;

    /**
     * Default max lifetime
     */
    public static final int DEF_MAX_LIFE = 30;

    /**
     * Randomiser
     */
    private final Random randomiser;

    /**
     * Parameterless constructor. Uses time as the random seed.
     */
    public LandingParticles(CustomResourceHolder customGraphics) {
        this(customGraphics, new Random());
    }

    /**
     * Constructor that uses a fixed random seed.
     *
     * @param seed Random seed
     */
    public LandingParticles(CustomResourceHolder customGraphics, long seed) {
        super(customGraphics);
        randomiser = new Random(seed);
    }

    /**
     * Constructor that uses a fixed random object.
     *
     * @param random Random instance
     */
    public LandingParticles(CustomResourceHolder customGraphics, Random random) {
        super(customGraphics);
        randomiser = random;
    }

    // Parameter class for these particles.
    public static class Parameters {
        public final int minX;
        public final int maxX;
        public final int startY;
        public final int yVar;
        public final int red;
        public final int green;
        public final int blue;
        public final int alpha;
        public final int variance;
        public final double maxVel;

        public Parameters(int minX, int maxX, int startY, int yVar, int red, int green, int blue, int alpha, int variance, double maxVel) {
            this.minX = minX;
            this.maxX = maxX;
            this.startY = startY;
            this.yVar = yVar;
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
            this.variance = variance;
            this.maxVel = maxVel;
        }

        public Parameters(int minX, int maxX, int startY, int yVar, int red, int green, int blue, int alpha, int variance) {
            this(minX, maxX, startY, yVar, red, green, blue, alpha, variance, DEF_MAX_VEL);
        }
    }

    public void addNumber(GameEngine engine, EventReceiver receiver, int playerID, int num) {
        int baseX = (16 * engine.nowPieceX) + 4 + receiver.getFieldDisplayPositionX(engine, playerID);
        int baseY = (16 * engine.nowPieceY) + 52 + receiver.getFieldDisplayPositionY(engine, playerID);

        if (engine.nowPieceObject != null) {
            for (int i = 0; i < engine.nowPieceObject.getMaxBlock(); ++i) {
                if (checkLowestBlock(engine.nowPieceObject, i)) {
                    if (engine.nowPieceObject.big) {
                        addNumber(
                            num * 2,
                            baseX + 16 * engine.nowPieceObject.dataX[engine.nowPieceObject.direction][i],
                            baseX + 16 * (engine.nowPieceObject.dataX[engine.nowPieceObject.direction][i] + 2),
                            baseY + 16 * (engine.nowPieceObject.dataY[engine.nowPieceObject.direction][i] + 2),
                            2,
                            engine.nowPieceObject.block[i]
                        );
                    } else {
                        addNumber(
                            num,
                            baseX + 16 * engine.nowPieceObject.dataX[engine.nowPieceObject.direction][i],
                            baseX + 16 * (engine.nowPieceObject.dataX[engine.nowPieceObject.direction][i] + 1),
                            baseY + 16 * (engine.nowPieceObject.dataY[engine.nowPieceObject.direction][i] + 1),
                            2,
                            engine.nowPieceObject.block[i]
                        );
                    }
                }
            }
        }
    }

    /** Add a number of particles based on a block. */
    public void addNumber(int num, int minX, int maxX, int startY, int yVar, Block block) {
        final int[] rgb = getColorForBlock(block);

        final int alpha = (block.color == Block.BLOCK_COLOR_INVALID || block.color == Block.BLOCK_COLOR_NONE) ? 0 : 255;
        final int variance = (block.color == Block.BLOCK_COLOR_RAINBOW || block.color == Block.BLOCK_COLOR_GEM_RAINBOW) ? 255 : 16;

        addNumber(num, new Parameters(minX, maxX, startY, yVar, rgb[0], rgb[1], rgb[2], alpha, variance));
    }

    /**
     * Adds a number of landing particles.<br />
     * Parameters are min start x, max start x, start y, start y variance,
     * red, green, blue, alpha, variance (all <code>int</code> types),
     * maximum velocity, chance of upward movement (all <code>double</code> types)
     *
     * @param num    Number of particles
     * @param params Parameters to pass onto the particles
     */
    @Override
    public void addNumber(int num, Parameters params) {
        try {
            final int minX = params.minX;
            final int maxX = params.maxX;
            final int startY = params.startY;
            final int yVar = params.yVar;
            final int red = params.red;
            final int green = params.green;
            final int blue = params.blue;
            final int alpha = params.alpha;
            final int variance = params.variance;

            final double maxVel = params.maxVel;

            for (int i = 0; i < num; i++) {
                int ured, ugreen, ublue, ualpha;
                ured = red + (variance - randomiser.nextInt(2 * variance + 1));
                ugreen = green + (variance - randomiser.nextInt(2 * variance + 1));
                ublue = blue + (variance - randomiser.nextInt(2 * variance + 1));
                ualpha = alpha + (variance - randomiser.nextInt(2 * variance + 1));

                ured = MathHelper.clamp(ured, 0, 255);
                ugreen = MathHelper.clamp(ugreen, 0, 255);
                ublue = MathHelper.clamp(ublue, 0, 255);
                ualpha = MathHelper.clamp(ualpha, 0, 255);

                final DoubleVector p = new DoubleVector(
                    Interpolation.lerp(minX, maxX, (double) i / (num - 1)),
                    Interpolation.lerp(startY - yVar, startY + yVar, randomiser.nextDouble()),
                    false
                );

                final DoubleVector v = new DoubleVector(
                    0,
                    Interpolation.lerp(0, maxVel, randomiser.nextDouble()),
                    false
                );

                final Particle particle = new Particle(
                    Particle.ParticleShape.Rectangle,
                    Interpolation.lerp(DEF_MIN_LIFE, DEF_MAX_LIFE, randomiser.nextDouble()),
                    p,
                    v,
                    DoubleVector.zero(),
                    2, 2,
                    ured, ugreen, ublue, ualpha,
                    255, 255, 255, 32
                );

                particles.add(particle);
            }
        } catch (ClassCastException ce) {
            log.error("LandingParticles.addNumber: Invalid argument in params.", ce);
        } catch (Exception e) {
            log.error("LandingParticles.addNumber: Other exception occurred.", e);
        }
    }
}
