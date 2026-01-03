package zeroxfc.nullpo.custom.modes.objects.seasons;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Random;
import mu.nu.nullpo.game.event.EventReceiver;
import zeroxfc.nullpo.custom.libs.PrimitiveDrawingHook;
import zeroxfc.nullpo.custom.libs.types.ColourMixer;

public class FireAndSnow {
    private final Deque<Particle> particles = new LinkedList<>();
    private final Random random;

    public FireAndSnow(long seed) {
        random = new Random(seed);
    }

    public void update() {
        particles.removeIf(Particle::update);
    }

    public void addFire(int num, boolean winter) {
        for (int i = 0; i < num; ++i) {
            particles.add(
                new FireParticle(
                    random,
                    random.nextInt(640), 480,
                    random.nextInt(5) - 2, -(random.nextInt(5) + 2),
                    random.nextInt(12) + 4,
                    winter
                )
            );
        }
    }

    public void addSnow(int num) {
        for (int i = 0; i < num; ++i) {
            particles.add(
                new SnowParticle(
                    random,
                    random.nextInt(640), 0,
                    random.nextInt(5) - 2, random.nextInt(5) + 2,
                    random.nextInt(12) + 4
                )
            );
        }
    }

    public void draw(PrimitiveDrawingHook drawing, EventReceiver receiver) {
        for (Particle p : particles) p.draw(drawing, receiver);
    }

    // Particle instance class.
    private static abstract class Particle {
        protected int x;
        protected int y;

        protected final int velocityX;
        protected final int velocityY;

        protected final int size;
        protected final ColourMixer colour;

        public Particle(int x, int y, int velocityX, int velocityY, int size) {
            this.x = x;
            this.y = y;

            this.velocityX = velocityX;
            this.velocityY = velocityY;

            this.size = size;
            this.colour = ColourMixer.rgb(0, 0, 0);
        }

        abstract boolean update();
        abstract void draw(PrimitiveDrawingHook drawing, EventReceiver receiver);
    }

    private static class FireParticle extends Particle {
        public FireParticle(Random random, int x, int y, int velocityX, int velocityY, int size, boolean winter) {
            super(x, y, velocityX, velocityY, size);

            if (winter) {
                colour
                    .setHueAngle((random.nextDouble() * 60.0) + 180.0)
                    .setSaturation(0.75 + (random.nextDouble() * 0.25))
                    .setValue(1.0);
            } else {
                colour
                    .setHue(random.nextDouble() * 0.066666666666)
                    .setSaturation(0.8 + (random.nextDouble() * 0.2))
                    .setValue(1.0);
            }
        }

        public boolean update() {
            x += velocityX;
            y += velocityY;

            return y < -size;
        }

        // Hardcodes the drawing limit of 480y because idc at this point lmao.
        public void draw(PrimitiveDrawingHook drawing, EventReceiver receiver) {
            final float yRatio = (float) Math.sqrt(y / 480.0);
            final int drawSize = (int) Math.ceil(size * yRatio);

            drawing.drawRectangle(
                receiver,
                x - (drawSize / 2), y - (drawSize / 2),
                drawSize, drawSize,
                colour.getRed8(), colour.getGreen8(), colour.getBlue8(), (int) Math.ceil(255.0 * yRatio),
                true
            );
        }
    }

    private static class SnowParticle extends Particle {
        public SnowParticle(Random random, int x, int y, int velocityX, int velocityY, int size) {
            super(x, y, velocityX, velocityY, size);

            colour
                .setHueAngle(0)
                .setSaturation(0)
                .setValue(0.75 + (0.25 * random.nextDouble()));
        }

        public boolean update() {
            x += velocityX;
            y += velocityY;

            return y > (640 + size);
        }

        // Hardcodes the drawing limit of 480y because idc at this point lmao.
        public void draw(PrimitiveDrawingHook drawing, EventReceiver receiver) {
            final float yRatio = 1f - (y / 480f);
            final int drawSize = (int) Math.ceil(size * yRatio);

            drawing.drawRectangle(
                receiver,
                x - (drawSize / 2), y - (drawSize / 2),
                drawSize, drawSize,
                colour.getRed8(), colour.getGreen8(), colour.getBlue8(), (int) Math.ceil(255.0 * yRatio),
                true
            );
        }
    }
}
