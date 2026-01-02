package zeroxfc.nullpo.custom.modes.objects.seasons;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Random;
import mu.nu.nullpo.game.event.EventReceiver;
import zeroxfc.nullpo.custom.libs.PrimitiveDrawingHook;
import zeroxfc.nullpo.custom.libs.types.ColourMixer;

public class Fire {
    private final Deque<Particle> particles = new LinkedList<>();
    private final Random random;

    public Fire(long seed) {
        random = new Random(seed);
    }

    public void update() {
        particles.removeIf(Particle::update);
    }

    public void add(int num) {
        for (int i = 0; i < num; ++i) {
            particles.add(
                new Particle(
                    random,
                    random.nextInt(640), 480,
                    random.nextInt(5) - 2, -(random.nextInt(5) + 2),
                    random.nextInt(12) + 4
                )
            );
        }
    }

    public void draw(PrimitiveDrawingHook drawing, EventReceiver receiver) {
        for (Particle p : particles) p.draw(drawing, receiver);
    }

    // Particle instance class.
    private static class Particle {
        private int x;
        private int y;

        private final int velocityX;
        private final int velocityY;

        private final int size;
        private final ColourMixer colour;

        public Particle(Random random, int x, int y, int velocityX, int velocityY, int size) {
            this.x = x;
            this.y = y;

            this.velocityX = velocityX;
            this.velocityY = velocityY;

            this.size = size;

            colour = ColourMixer.rgb(0, 0, 0);
            colour
                .setHue(random.nextDouble() * 0.066666666666)
                .setSaturation(0.8 + (random.nextDouble() * 0.2))
                .setValue(1.0);
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
}
