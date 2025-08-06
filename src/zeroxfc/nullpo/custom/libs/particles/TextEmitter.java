package zeroxfc.nullpo.custom.libs.particles;

import java.util.LinkedList;
import java.util.List;
import mu.nu.nullpo.game.play.GameEngine;
import zeroxfc.nullpo.custom.libs.DoubleVector;
import zeroxfc.nullpo.custom.libs.GameTextUtilities;
import zeroxfc.nullpo.custom.libs.Interpolation;
import zeroxfc.nullpo.custom.libs.types.ObjectAlignment;

public class TextEmitter {
    private final List<Char> characters = new LinkedList<>();

    public void clear() {
        characters.clear();
    }

    public void addString(
        String str,
        DoubleVector position, DoubleVector velocity, DoubleVector acceleration,
        ObjectAlignment alignment,
        int life, int lifeOffset, int lifetime,
        int baseColour,
        float startScale, float endScale,
        int startRGB, int startA,
        int endRGB, int endA,
        boolean reverse
    ) {
        // This creates a centred string.
        final int offsetX = (int) (8 * startScale * str.length()) - (int) (16 * startScale);

        // 0 1 (2) 3 4 --- 0 1 (1.5) 2 3
        final double midIndex = str.length() / 2d;

        if (reverse) {
            for (int i = str.length() - 1; i >= 0; --i) {
                final DoubleVector augmentedPos = new DoubleVector(
                    position.getX() - offsetX + (16.0 * startScale * i),
                    position.getY(),
                    false
                );

                final DoubleVector augmentedVel = DoubleVector.add(
                    velocity,
                    new DoubleVector(
                        (i - midIndex) * (endScale / lifetime) * 16d,
                        0,
                        false
                    )
                );

                addCharacter(
                    str.charAt(i),
                    augmentedPos, augmentedVel, acceleration,
                    alignment,
                    life - ((str.length() - i - 1) * lifeOffset), lifetime,
                    baseColour,
                    startScale, endScale,
                    startRGB, startA,
                    endRGB, endA
                );
            }
        } else {
            for (int i = 0; i < str.length(); ++i) {
                final DoubleVector augmentedPos = new DoubleVector(
                    position.getX() - offsetX + (16.0 * startScale * i),
                    position.getY(),
                    false
                );

                final DoubleVector augmentedVel = DoubleVector.add(
                    velocity,
                    new DoubleVector(
                        (i - midIndex) * (endScale / lifetime) * 16d,
                        0,
                        false
                    )
                );

                addCharacter(
                    str.charAt(i),
                    augmentedPos, augmentedVel, acceleration,
                    alignment,
                    life - (i * lifeOffset), lifetime,
                    baseColour,
                    startScale, endScale,
                    startRGB, startA,
                    endRGB, endA
                );
            }
        }
    }

    public void addCharacter(
        char chr,
        DoubleVector position, DoubleVector velocity, DoubleVector acceleration,
        ObjectAlignment alignment,
        int life, int lifetime,
        int baseColour,
        float startScale, float endScale,
        int startRGB, int startA,
        int endRGB, int endA
    ) {
        characters.add(new Char(
            chr,
            position, velocity, acceleration,
            alignment,
            life, lifetime,
            baseColour,
            startScale, endScale,
            startRGB, startA,
            endRGB, endA
        ));
    }

    public void updateAll() {
        characters.removeIf(Char::update);
    }

    public void drawAll(GameEngine engine) {
        for (Char c : characters) c.draw(engine);
    }

    public static class Char {
        private final char chr;

        private DoubleVector position;
        private DoubleVector velocity;
        private DoubleVector acceleration;

        private final ObjectAlignment alignment;

        private int life;
        private final int lifetime;

        private final int baseColour; // Pick from EventReceiver.
        private final float startScale;
        private final float endScale;

        // I suggest using ColourMixer for this.
        private final int startR, endR;
        private final int startG, endG;
        private final int startB, endB;
        private final int startA, endA;

        public Char(
            char chr,
            DoubleVector position, DoubleVector velocity, DoubleVector acceleration,
            ObjectAlignment alignment,
            int life, int lifetime,
            int baseColour,
            float startScale, float endScale,
            int startRGB, int startA,
            int endRGB, int endA
        ) {
            this.chr = chr;

            this.position = position;
            this.velocity = velocity;
            this.acceleration = acceleration;
            this.alignment = alignment;
            this.life = life;
            this.lifetime = lifetime;

            this.baseColour = baseColour;
            this.startScale = startScale;
            this.endScale = endScale;

            this.startA = startA;
            this.endA = endA;

            this.startR = (startRGB >>> 16) & 0xFF;
            this.startG = (startRGB >>> 8) & 0xFF;
            this.startB = startRGB & 0xFF;

            this.endR = (endRGB >>> 16) & 0xFF;
            this.endG = (endRGB >>> 8) & 0xFF;
            this.endB = endRGB & 0xFF;
        }

        private int usedV(int v0, int v1) {
            return Interpolation.lerp(v0, v1, life / (double) lifetime);
        }

        private float usedScale() {
            return Interpolation.lerp(startScale, endScale, life / (double) lifetime);
        }

        public boolean update() {
            final boolean result = ++life > lifetime;

            if (life > 0) {
                velocity.add(acceleration);
                position.add(velocity);
            }

            return result;
        }

        public void draw(GameEngine engine) {
            if (life < 0 || life > lifetime) return;

            GameTextUtilities.drawAlignedText(
                engine,
                (int) position.getX(), (int) position.getY(),
                GameTextUtilities.Text.customMixColor(
                    String.valueOf(chr),
                    baseColour,
                    usedV(startR, endR), usedV(startG, endG), usedV(startB, endB), usedV(startA, endA),
                    usedScale()
                ),
                alignment
            );
        }
    }
}
