package zeroxfc.nullpo.custom.libs.particles;

import java.util.Random;
import zeroxfc.nullpo.custom.libs.CustomResourceHolder;
import zeroxfc.nullpo.custom.libs.DoubleVector;
import zeroxfc.nullpo.custom.libs.Interpolation;
import zeroxfc.nullpo.custom.libs.MathHelper;

public class Fireworks extends ParticleEmitterBase<Object[]> {
    /**
     * Default max velocity
     */
    public static final double DEF_MAX_VEL = 3.2;
    /**
     * Default min lifetime
     */
    public static final int DEF_MIN_LIFE = 60;
    /**
     * Default max lifetime
     */
    public static final int DEF_MAX_LIFE = 120;
    /**
     * Gravity
     */
    private static final double GRAVITY = 2.4d / 30d;
    /**
     * Randomiser
     */
    private final Random randomiser;

    /**
     * Parameterless constructor. Uses time as the random seed.
     */
    public Fireworks(CustomResourceHolder customGraphics) {
        this(customGraphics, new Random());
    }

    /**
     * Constructor that uses a fixed random seed.
     *
     * @param seed Random seed
     */
    public Fireworks(CustomResourceHolder customGraphics, long seed) {
        super(customGraphics);
        randomiser = new Random(seed);
    }

    /**
     * Constructor that uses a fixed random object.
     *
     * @param random Random instance
     */
    public Fireworks(CustomResourceHolder customGraphics, Random random) {
        super(customGraphics);
        randomiser = random;
    }

    /**
     * Add some number of fireworks.
     * <code>params</code> are min start X location, max start X location, min start Y location, max start Y location,
     * red, green, blue, alpha, max colour variance (all <code>int</code> type),
     * max velocity (velocity is a <code>double</code>),
     * min lifetime, max lifetime (both <code>int</code>) in that order.
     *
     * @param num    Number of particles / particle groups.
     * @param params Parameters to pass onto the particles.
     */
    @Override
    public void addNumber(int num, Object[] params) {
        int minX, maxX, minY, maxY, red, green, blue, alpha, variance, minLifeTime, maxLifeTime;
        double maxVelocity;

        try {
            minX = (int) params[0];
            maxX = (int) params[1];
            minY = (int) params[2];
            maxY = (int) params[3];

            red = (int) params[4];
            green = (int) params[5];
            blue = (int) params[6];
            alpha = (int) params[7];
            variance = (int) params[8];

            maxVelocity = (double) params[9];

            minLifeTime = (int) params[10];
            maxLifeTime = (int) params[11];

            for (int i = 0; i < num; ++i) {
                DoubleVector origin = new DoubleVector(Interpolation.lerp(minX, maxX, randomiser.nextDouble()), Interpolation.lerp(minY, maxY, randomiser.nextDouble()), false);
                for (int j = 0; j < randomiser.nextInt(121) + 120; ++j) {
                    int ured, ugreen, ublue, ualpha;
                    ured = red + (variance - randomiser.nextInt(2 * variance + 1));
                    ugreen = green + (variance - randomiser.nextInt(2 * variance + 1));
                    ublue = blue + (variance - randomiser.nextInt(2 * variance + 1));
                    ualpha = alpha + (variance - randomiser.nextInt(2 * variance + 1));

                    ured = MathHelper.clamp(ured, 0, 255);
                    ugreen = MathHelper.clamp(ugreen, 0, 255);
                    ublue = MathHelper.clamp(ublue, 0, 255);
                    ualpha = MathHelper.clamp(ualpha, 0, 255);

                    int s = 1 + randomiser.nextInt(3);
                    DoubleVector v = new DoubleVector(2 * randomiser.nextDouble() * maxVelocity - maxVelocity, 2 * randomiser.nextDouble() * Math.PI, true);

                    Particle particle = new Particle(
                        Particle.ParticleShape.Rectangle,
                        Interpolation.lerp(minLifeTime, maxLifeTime, randomiser.nextDouble()),
                        origin,
                        v,
                        new DoubleVector(0, GRAVITY, false),
                        s, s,
                        ured, ugreen, ublue, ualpha,
                        (int) (ured / 1.5), (int) (ugreen / 1.5), (int) (ublue / 1.5), 64
                    );

                    particles.add(particle);

                    Particle particle2 = new Particle(
                        Particle.ParticleShape.Rectangle,
                        Interpolation.lerp(minLifeTime, maxLifeTime, randomiser.nextDouble()),
                        origin,
                        v,
                        new DoubleVector(0, GRAVITY, false),
                        1, 1,
                        Interpolation.lerp(ured, 255, 0.9),
                        Interpolation.lerp(ugreen, 255, 0.9),
                        Interpolation.lerp(ublue, 255, 0.9),
                        Interpolation.lerp(ualpha, 255, 0.9),
                        (int) (ured / 1.25), (int) (ugreen / 1.25), (int) (ublue / 1.25), 64
                    );

                    particles.add(particle2);
                }
            }
        } catch (ClassCastException ce) {
            log.error("Fireworks.addNumber: Invalid argument in params.", ce);
        } catch (Exception e) {
            log.error("Fireworks.addNumber: Other exception occurred.", e);
        }
    }
}
