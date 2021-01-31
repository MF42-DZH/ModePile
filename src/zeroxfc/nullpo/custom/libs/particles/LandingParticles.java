package zeroxfc.nullpo.custom.libs.particles;

import java.util.Random;
import zeroxfc.nullpo.custom.libs.DoubleVector;
import zeroxfc.nullpo.custom.libs.Interpolation;

public class LandingParticles extends ParticleEmitterBase {
    /**
     * Default max velocity
     */
    public static final double DEF_MAX_VEL = 1d;

    /**
     * Default min lifetime
     */
    public static final int DEF_MIN_LIFE = 40;

    /**
     * Default max lifetime
     */
    public static final int DEF_MAX_LIFE = 80;

    /**
     * Randomiser
     */
    private final Random randomiser;

    /**
     * Parameterless constructor. Uses time as the random seed.
     */
    public LandingParticles() {
        this(new Random());
    }

    /**
     * Constructor that uses a fixed random seed.
     *
     * @param seed Random seed
     */
    public LandingParticles(long seed) {
        randomiser = new Random(seed);
    }

    /**
     * Constructor that uses a fixed random object.
     *
     * @param random Random instance
     */
    public LandingParticles(Random random) {
        randomiser = random;
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
    public void addNumber(int num, Object[] params) {
        int minX, maxX, startY, yVar, red, green, blue, alpha, variance;
        double maxVel, upChance;

        try {
            minX = (int) params[0];
            maxX = (int) params[1];
            startY = (int) params[2];
            yVar = (int) params[3];
            red = (int) params[4];
            green = (int) params[5];
            blue = (int) params[6];
            alpha = (int) params[7];
            variance = (int) params[8];

            maxVel = (double) params[9];
            upChance = (double) params[10];

            for (int i = 0; i < num; i++) {
                int ured, ugreen, ublue, ualpha;
                ured = red + (int) (2 * randomiser.nextDouble() * variance - variance);
                ugreen = green + (int) (2 * randomiser.nextDouble() * variance - variance);
                ublue = blue + (int) (2 * randomiser.nextDouble() * variance - variance);
                ualpha = alpha + (int) (2 * randomiser.nextDouble() * variance - variance);

                DoubleVector p = new DoubleVector(
                    Interpolation.lerp(minX, maxX, randomiser.nextDouble()),
                    Interpolation.lerp(startY - yVar, startY + yVar, randomiser.nextDouble()),
                    false
                );

                DoubleVector v = new DoubleVector(
                    0,
                    Interpolation.lerp(0, maxVel, randomiser.nextDouble()) * (randomiser.nextDouble() < upChance ? -0.5 : 1),
                    false
                );

                Particle particle = new Particle(
                    Particle.ParticleShape.Rectangle,
                    Interpolation.lerp(DEF_MIN_LIFE, DEF_MAX_LIFE, randomiser.nextDouble()),
                    p,
                    v,
                    DoubleVector.zero(),
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
