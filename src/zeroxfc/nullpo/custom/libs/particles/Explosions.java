package zeroxfc.nullpo.custom.libs.particles;

import java.util.Random;
import zeroxfc.nullpo.custom.libs.CustomResourceHolder;
import zeroxfc.nullpo.custom.libs.DoubleVector;
import zeroxfc.nullpo.custom.libs.Interpolation;
import zeroxfc.nullpo.custom.libs.types.ColourMixer;

// TODO: explosions look really strange. fix that.
public class Explosions extends ParticleEmitterBase<Explosions.Charge> {
    // Explosion parameters.
    public static class Charge {
        public final int centreX;         // Epicentre X
        public final int centreY;         // Epicentre Y
        public final int minLife;         // Minimum lifetime (of explosion fire; smoke lasts longer)
        public final int maxLife;         // Maximum lifetime
        public final double radius;       // Self-explanatory
        public final double speedMult;    // Explosion travel speed
        public final double temperature;  // Lower = Redder, Higher = Whiter [0, 1]
        public final double smokeDensity; // Higher = More Smoke [0, 1]

        public Charge(int centreX, int centreY, int minLife, int maxLife, double radius, double speedMult, double temperature, double smokeDensity) {
            assert (temperature >= 0d && temperature <= 1d);
            assert (smokeDensity >= 0d && smokeDensity <= 1d);
            assert (minLife > 0 && maxLife >= minLife);

            this.centreX = centreX;
            this.centreY = centreY;
            this.minLife = minLife;
            this.maxLife = maxLife;
            this.radius = radius;
            this.speedMult = speedMult;
            this.temperature = temperature;
            this.smokeDensity = smokeDensity;
        }
    }

    // Explosion particles are unique in that they behave differently depending on their
    // component, and their size does change as their lifetime progresses.
    private enum Component {
        FLAME, SMOKE
    }

    private static class ExplosionParticle extends Particle {
        private final Component component;
        private final int startSizeX;
        private final int startSizeY;

        public ExplosionParticle(
            Component component, int maxLifeTime,
            DoubleVector position, DoubleVector velocity, DoubleVector acceleration,
            int sizeX, int sizeY,
            int red, int green, int blue, int alpha,
            int redEnd, int greenEnd, int blueEnd, int alphaEnd
        ) {
            super(ParticleShape.RECTANGLE, maxLifeTime, position, velocity, acceleration, sizeX, sizeY, red, green, blue, alpha, redEnd, greenEnd, blueEnd, alphaEnd);

            this.startSizeX = sizeX;
            this.startSizeY = sizeY;
            this.component = component;
        }

        @Override
        public boolean update() {
            final boolean ret = super.update();

            if (component == Component.FLAME) {
                setSizeX((int) Math.ceil(startSizeX * 1.5 * getLifetimeProportion()));
                setSizeY((int) Math.ceil(startSizeY * 1.5 * getLifetimeProportion()));

                final double up = 1 - getLifetimeProportion();
                ua = (int) (alpha * Math.sqrt(up));

                velocity.mul(29d / 30d);
            } else if (component == Component.SMOKE) {
                setSizeX((int) Math.ceil(startSizeX * (1 + getLifetimeProportion() / 2)));
                setSizeY((int) Math.ceil(startSizeY * (1 + getLifetimeProportion() / 2)));

                final double up = 1 - getLifetimeProportion();
                ua = (int) (alpha * Math.sqrt(up));

                velocity.mul(59d / 60d);
            }

            return ret;
        }
    }

    private final Random randomizer;

    public Explosions(CustomResourceHolder customResourceHolder) {
        this(customResourceHolder, new Random());
    }

    public Explosions(CustomResourceHolder customResourceHolder, long seed) {
        this(customResourceHolder, new Random(seed));
    }

    public Explosions(CustomResourceHolder customGraphics, Random randomizer) {
        super(customGraphics);
        this.randomizer = randomizer;
    }

    @Override
    public void addNumber(int num, Charge params) {
        for (int i = 0; i < (int) Math.ceil(num * (1 + params.smokeDensity)); ++i) {
            final Component component = i < num ? Component.FLAME : Component.SMOKE;

            final DoubleVector origin = new DoubleVector(
                Interpolation.lerp(0, params.radius, randomizer.nextDouble()),
                Interpolation.lerp(0, 2 * Math.PI, randomizer.nextDouble()),
                true
            );

            final DoubleVector velocity = new DoubleVector(origin.getMagnitude(), origin.getDirection(), true);
            velocity.div(4);
            velocity.mul(params.speedMult);

            origin.add(new DoubleVector(params.centreX, params.centreY, false));

            final ColourMixer colourStart = component == Component.FLAME
                ? (ColourMixer.hsvViaAngle(((randomizer.nextDouble() / 2) + 0.5) * params.temperature * 60.0, 1 - params.temperature, 1.0))
                : (ColourMixer.hsl(0, 0, randomizer.nextDouble() / 2));

            final ColourMixer colourEnd = component == Component.FLAME
                ? (ColourMixer.hslViaAngle(60, 1.0, 2d / 3d))
                : (ColourMixer.hsl(0, 0, 1.0));

            int lifetime = randomizer.nextInt(params.maxLife - params.minLife + 1) + params.minLife;
            if (component == Component.SMOKE) lifetime = (int) Math.ceil(lifetime * 1.5);

            final int startSize = 4 + (int) Math.ceil(randomizer.nextDouble() * 4);

            particles.add(
                new ExplosionParticle(
                    component, lifetime,
                    origin, velocity, DoubleVector.zero(),
                    startSize, startSize,
                    colourStart.getRed8(), colourStart.getGreen8(), colourStart.getBlue8(), component == Component.SMOKE ? 160 : 255,
                    colourEnd.getRed8(), colourEnd.getGreen8(), colourEnd.getBlue8(), component == Component.SMOKE ? 160 : 255
                )
            );
        }
    }
}
