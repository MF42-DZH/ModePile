package zeroxfc.nullpo.custom.libs.particles;

import java.util.Random;
import zeroxfc.nullpo.custom.libs.CustomResourceHolder;
import zeroxfc.nullpo.custom.libs.DoubleVector;
import zeroxfc.nullpo.custom.libs.Interpolation;

public class SurfaceSparks extends ParticleEmitterBase<SurfaceSparks.Parameters> {
    private final Random randomiser;

    public SurfaceSparks(CustomResourceHolder customGraphics) {
        this(customGraphics, new Random());
    }

    public SurfaceSparks(CustomResourceHolder customGraphics, long seed) {
        super(customGraphics);
        this.randomiser = new Random(seed);
    }

    public SurfaceSparks(CustomResourceHolder customGraphics, Random randomiser) {
        super(customGraphics);
        this.randomiser = randomiser;
    }

    public static class Parameters {
        public final int minX;
        public final int maxX;
        public final int startY;
        public final int direction;

        public Parameters(int minX, int maxX, int startY, int direction) {
            assert direction == 1 || direction == -1;

            this.minX = minX;
            this.maxX = maxX;
            this.startY = startY;
            this.direction = direction;
        }
    }

    @Override
    public void addNumber(int num, Parameters params) {
        int ured, ugreen, ublue;

        for (int i = 0; i < num; ++i) {
            ured = randomiser.nextInt(16) + 240;
            ugreen = randomiser.nextInt(33) + 112;
            ublue = randomiser.nextInt(32);

            final DoubleVector p = new DoubleVector(
                Interpolation.lerp(params.minX, params.maxX, randomiser.nextDouble()),
                Interpolation.lerp(params.startY - 1, params.startY + 1, randomiser.nextDouble()),
                false
            );

            final DoubleVector v = new DoubleVector(
                Interpolation.lerp(1.5 * params.direction, 3 * params.direction, randomiser.nextDouble()),
                0.0d,
                false
            );

            final Particle particle = new Particle(
                Particle.ParticleShape.Rectangle,
                Interpolation.lerp(9, 15, randomiser.nextDouble()),
                p,
                v,
                new DoubleVector(0, 9.80665d / 60, false),
                4, 2,
                ured, ugreen, ublue, 255,
                255, 255, 255, 64
            );

            particles.add(particle);
        }
    }
}
