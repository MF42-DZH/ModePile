package zeroxfc.nullpo.custom.libs.particles;

import java.util.Random;
import mu.nu.nullpo.game.component.Block;
import zeroxfc.nullpo.custom.libs.CustomResourceHolder;
import zeroxfc.nullpo.custom.libs.DoubleVector;
import zeroxfc.nullpo.custom.libs.Interpolation;
import zeroxfc.nullpo.custom.libs.MathHelper;

public class LandingParticles extends ParticleEmitterBase<LandingParticles.Parameters> {
    /**
     * Default max velocity
     */
    public static final double DEF_MAX_VEL = 1.25d;

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
        public final double upChance;

        public Parameters(int minX, int maxX, int startY, int yVar, int red, int green, int blue, int alpha, int variance, double maxVel, double upChance) {
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
            this.upChance = upChance;
        }

        public Parameters(int minX, int maxX, int startY, int yVar, int red, int green, int blue, int alpha, int variance, double upChance) {
            this(minX, maxX, startY, yVar, red, green, blue, alpha, variance, DEF_MAX_VEL, upChance);
        }
    }

    /** Add a number of particles based on a block. */
    public void addNumber(int num, int minX, int maxX, int startY, int yVar, Block block) {
        int red, green, blue;
        switch (block.color) {
            case Block.BLOCK_COLOR_RED:
            case Block.BLOCK_COLOR_GEM_RED:
                red = 255;
                green = 32;
                blue = 32;
                break;
            case Block.BLOCK_COLOR_ORANGE:
            case Block.BLOCK_COLOR_GEM_ORANGE:
                red = 255;
                green = 128;
                blue = 0;
                break;
            case Block.BLOCK_COLOR_SQUARE_GOLD_1:
            case Block.BLOCK_COLOR_SQUARE_GOLD_2:
            case Block.BLOCK_COLOR_SQUARE_GOLD_3:
            case Block.BLOCK_COLOR_SQUARE_GOLD_4:
            case Block.BLOCK_COLOR_SQUARE_GOLD_5:
            case Block.BLOCK_COLOR_SQUARE_GOLD_6:
            case Block.BLOCK_COLOR_SQUARE_GOLD_7:
            case Block.BLOCK_COLOR_SQUARE_GOLD_8:
            case Block.BLOCK_COLOR_SQUARE_GOLD_9:
            case Block.BLOCK_COLOR_YELLOW:
            case Block.BLOCK_COLOR_GEM_YELLOW:
                red = 255;
                green = 255;
                blue = 0;
                break;
            case Block.BLOCK_COLOR_GREEN:
            case Block.BLOCK_COLOR_GEM_GREEN:
                red = 32;
                green = 255;
                blue = 32;
                break;
            case Block.BLOCK_COLOR_CYAN:
            case Block.BLOCK_COLOR_GEM_CYAN:
                red = 0;
                green = 255;
                blue = 255;
                break;
            case Block.BLOCK_COLOR_BLUE:
            case Block.BLOCK_COLOR_GEM_BLUE:
                red = 32;
                green = 32;
                blue = 255;
                break;
            case Block.BLOCK_COLOR_PURPLE:
            case Block.BLOCK_COLOR_GEM_PURPLE:
                red = 128;
                green = 32;
                blue = 255;
                break;
            default:
                red = 255;
                green = 255;
                blue = 255;
                break;
        }

        int alpha = (block.color == Block.BLOCK_COLOR_INVALID || block.color == Block.BLOCK_COLOR_NONE) ? 0 : 255;
        int variance = (block.color == Block.BLOCK_COLOR_RAINBOW || block.color == Block.BLOCK_COLOR_GEM_RAINBOW) ? 255 : 16;

        addNumber(num, new Parameters(minX, maxX, startY, yVar, red, green, blue, alpha, variance, 1d / 3d));
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
        int minX, maxX, startY, yVar, red, green, blue, alpha, variance;
        double maxVel, upChance;

        try {
            minX = params.minX;
            maxX = params.maxX;
            startY = params.startY;
            yVar = params.yVar;
            red = params.red;
            green = params.green;
            blue = params.blue;
            alpha = params.alpha;
            variance = params.variance;

            maxVel = params.maxVel;
            upChance = params.upChance;

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

                DoubleVector p = new DoubleVector(
                    Interpolation.lerp(minX, maxX, randomiser.nextDouble()),
                    Interpolation.lerp(startY - yVar, startY + yVar, randomiser.nextDouble()),
                    false
                );

                DoubleVector v = new DoubleVector(
                    Interpolation.lerp(-maxVel / 2, maxVel / 2, randomiser.nextDouble()),
                    Interpolation.lerp(0, maxVel, randomiser.nextDouble()) * (randomiser.nextDouble() < upChance ? -0.5 : 1),
                    false
                );

                Particle particle = new Particle(
                    Particle.ParticleShape.Rectangle,
                    Interpolation.lerp(DEF_MIN_LIFE, DEF_MAX_LIFE, randomiser.nextDouble()),
                    p,
                    v,
                    new DoubleVector(0, 9.80665d / 60, false),
                    2, 2,
                    ured, ugreen, ublue, ualpha,
                    (int) (ured / 1.5), (int) (ugreen / 1.5), (int) (ublue / 1.5), 64
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
