package zeroxfc.nullpo.custom.libs.backgroundtypes;

import java.util.Random;
import mu.nu.nullpo.game.play.GameEngine;
import zeroxfc.nullpo.custom.libs.CustomResourceHolder;
import zeroxfc.nullpo.custom.libs.types.ObjectAlignment;
import zeroxfc.nullpo.custom.libs.types.ImageChunk;
import zeroxfc.nullpo.custom.libs.types.tuples.FloatPair;
import zeroxfc.nullpo.custom.libs.types.tuples.IntPair;

public class BackgroundFakeScanlines extends AnimatedBackgroundHook {
    private static final int AMT = 480 / 2;
    private static final int PERIOD = 480;  // Frames
    private static final float BASE_LUMINANCE_OFFSET = 0.25f;

    private Random colourRandom;
    private ImageChunk[] chunks;
    private int phase;
    private float phaseMult;

    {
        ID = AnimatedBackgroundHook.ANIMATION_FAKE_SCANLINES;
        setImageName("localBG");
    }

    public BackgroundFakeScanlines(int bgNumber) {
        if (bgNumber < 0 || bgNumber > 19) bgNumber = 0;

        customHolder = new CustomResourceHolder();
        customHolder.loadImage("res/graphics/back" + bgNumber + ".png", imageName);

        setup();

        log.debug("Non-custom fake scanline background (" + bgNumber + ") created.");
    }

    public BackgroundFakeScanlines(String filePath) {
        customHolder = new CustomResourceHolder();
        customHolder.loadImage(filePath, imageName);

        setup();

        log.debug("Custom fake scanline background created (File Path: " + filePath + ").");
    }

    @Override
    public void setBG(int bg) {
        customHolder.loadImage("res/graphics/back" + bg + ".png", imageName);
        log.debug("Non-custom horizontal bars background modified (New BG: " + bg + ").");
    }

    @Override
    public void setBG(String filePath) {
        customHolder.loadImage(filePath, imageName);
        log.debug("Custom horizontal bars background modified (New File Path: " + filePath + ").");
    }

    /**
     * Allows the hot-swapping of pre-loaded BGs from a storage instance of a <code>CustomResourceHolder</code>.
     *
     * @param holder Storage instance
     * @param name   Image name
     */
    @Override
    public void setBGFromHolder(CustomResourceHolder holder, String name) {
        customHolder.putImageAt(holder.getImageAt(name), imageName);
        log.debug("Custom horizontal bars background modified (New Image Reference: " + name + ").");
    }

    private void setup() {
        colourRandom = new Random();

        // Generate chunks
        chunks = new ImageChunk[AMT];
        for (int i = 0; i < chunks.length; i++) {
            chunks[i] = new ImageChunk(
                ObjectAlignment.TOP_LEFT,
                IntPair.of(0, ((480 / AMT) * i) + ((480 / AMT) / 2)),
                IntPair.of(0, (480 / AMT) * i),
                IntPair.of(640, (480 / AMT)),
                FloatPair.of(1f, 1f)
            );
        }

        phase = 0;
        phaseMult = 1f;
    }

    public void updatePhaseMult(float phaseMult) {
        this.phaseMult = phaseMult;
    }

    @Override
    public void update() {
        if (colourRandom == null) return;
        for (ImageChunk chunk : chunks) {
            float newScale = (float) (0.01f * colourRandom.nextDouble()) + 0.995f;
            chunk.setScale(FloatPair.of(newScale, 1f));
        }

        phase = (phase + 1) % PERIOD;
    }

    @Override
    public void reset() {
        phase = 0;
        update();
    }

    @Override
    public void draw(GameEngine engine, int playerID) {
        for (int id = 0; id < chunks.length; id++) {
            float col = 1f - BASE_LUMINANCE_OFFSET;
            if ((id & 2) == 0) col -= BASE_LUMINANCE_OFFSET;

            final int usedPhase = (int) (phase * phaseMult);
            if (usedPhase >= PERIOD / 2 && (id == usedPhase - (PERIOD / 2) || id == 1 + usedPhase - (PERIOD / 2) || id == -1 + usedPhase - (PERIOD / 2))) {
                col += BASE_LUMINANCE_OFFSET;
            }

            // Randomness offset
            col -= (float) (0.025 * colourRandom.nextDouble());
            int colour = (int) (255 * col);

            customHolder.drawOffsetImage(engine, imageName, chunks[id], colour, colour, colour, 255);
        }
    }

    /**
     * This last one is important. In the case that any of the child types are used, it allows identification.
     * The identification can be used to allow casting during operations.
     *
     * @return Identification number of child class.
     */
    @Override
    public int getID() {
        return ID;
    }
}
