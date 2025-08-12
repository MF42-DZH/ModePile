package zeroxfc.nullpo.custom.libs.backgroundtypes;

import mu.nu.nullpo.game.play.GameEngine;
import zeroxfc.nullpo.custom.libs.CustomResourceHolder;
import zeroxfc.nullpo.custom.libs.types.ObjectAlignment;
import zeroxfc.nullpo.custom.libs.types.ImageChunk;
import zeroxfc.nullpo.custom.libs.types.tuples.FloatPair;
import zeroxfc.nullpo.custom.libs.types.tuples.IntPair;

public class BackgroundFrameAnim extends AnimatedBackgroundHook {
    public static final int SEQUENCE_LINEAR_HORIZONTAL = 0;
    public static final int SEQUENCE_LINEAR_VERTICAL = 1;
    public static final int SEQUENCE_GRID_HFTV = 2;
    public static final int SEQUENCE_GRID_VFTH = 3;
    private final int type;
    private ImageChunk[] chunkSequence;
    // private CustomResourceHolder customHolder;
    private int frameTime;
    private int currentTick;
    private int frameCount;
    private int currentFrame;
    private boolean pingPong, forward;

    {
        ID = AnimatedBackgroundHook.ANIMATION_FRAME_ANIM;
        setImageName("localBG");
    }

    public BackgroundFrameAnim(String filePath, int type, int frameTime, boolean pingPong) {
        customHolder = new CustomResourceHolder();
        customHolder.loadImage(filePath, imageName);

        this.type = type;
        this.frameTime = frameTime;
        this.pingPong = pingPong;

        setup();

        log.debug("Type " + type + " frame animation background created (File Path: " + filePath + ").");
    }

    private void setup() {
        forward = true;
        currentFrame = 0;
        currentTick = 0;

        switch (type) {
            case SEQUENCE_LINEAR_HORIZONTAL:
                int[] hDim = customHolder.getImageDimensions(imageName);
                int hAmount = hDim[0] / 640;

                chunkSequence = new ImageChunk[hAmount];
                for (int i = 0; i < hAmount; i++) {
                    chunkSequence[i] = new ImageChunk(
                        ObjectAlignment.TOP_LEFT,
                        IntPair.of(0, 0),
                        IntPair.of(i * 640, 0),
                        IntPair.of(640, 480),
                        FloatPair.of(1f, 1f)
                    );
                }

                frameCount = hAmount;
                break;
            case SEQUENCE_LINEAR_VERTICAL:
                int[] vDim = customHolder.getImageDimensions(imageName);
                int vAmount = vDim[1] / 480;

                chunkSequence = new ImageChunk[vAmount];
                for (int i = 0; i < vAmount; i++) {
                    chunkSequence[i] = new ImageChunk(
                        ObjectAlignment.TOP_LEFT,
                        IntPair.of(0, 0),
                        IntPair.of(0, i * 480),
                        IntPair.of(640, 480),
                        FloatPair.of(1f, 1f)
                    );
                }

                frameCount = vAmount;
                break;
            case SEQUENCE_GRID_HFTV:
                int[] gDim1 = customHolder.getImageDimensions(imageName);
                int hCells1 = gDim1[0] / 640;
                int vCells1 = gDim1[1] / 480;

                chunkSequence = new ImageChunk[vCells1 * hCells1];
                for (int y = 0; y < vCells1; y++) {
                    for (int x = 0; x < hCells1; x++) {
                        int chunk = (y * vCells1) + hCells1;
                        chunkSequence[chunk] = new ImageChunk(
                            ObjectAlignment.TOP_LEFT,
                            IntPair.of(0, 0),
                            IntPair.of(640 * x, 480 * x),
                            IntPair.of(640, 480),
                            FloatPair.of(1f, 1f)
                        );
                    }
                }

                frameCount = hCells1 * vCells1;
                break;
            case SEQUENCE_GRID_VFTH:
                int[] gDim2 = customHolder.getImageDimensions(imageName);
                int hCells2 = gDim2[0] / 640;
                int vCells2 = gDim2[1] / 480;

                chunkSequence = new ImageChunk[vCells2 * hCells2];
                for (int x = 0; x < hCells2; x++) {
                    for (int y = 0; y < vCells2; y++) {
                        int chunk = (y * hCells2) + vCells2;
                        chunkSequence[chunk] = new ImageChunk(
                            ObjectAlignment.TOP_LEFT,
                            IntPair.of(0, 0),
                            IntPair.of(640 * x, 480 * x),
                            IntPair.of(640, 480),
                            FloatPair.of(1f, 1f)
                        );
                    }
                }

                frameCount = hCells2 * vCells2;
                break;
            default:
                break;
        }
    }

    public void setSpeed(int frameTime) {
        this.frameTime = frameTime;
        reset();
    }

    public void setPingPong(boolean pingPong) {
        this.pingPong = pingPong;
        reset();
    }

    @Override
    public void update() {
        currentTick++;
        if (currentTick >= frameTime) {
            currentTick = 0;

            if (pingPong) {
                if (forward) currentFrame++;
                else currentFrame--;

                if (currentFrame >= frameCount) {
                    currentFrame -= 2;
                    forward = false;
                } else if (currentFrame < 0) {
                    currentFrame++;
                    forward = true;
                }
            } else {
                currentFrame = (currentFrame + 1) % frameCount;
            }
        }
    }

    @Override
    public void reset() {
        forward = true;
        currentFrame = 0;
        currentTick = 0;
    }

    @Override
    public void draw(GameEngine engine, int playerID) {
        customHolder.drawOffsetImage(engine, imageName, chunkSequence[currentFrame], 255, 255, 255, 255);
    }

    @Override
    public void setBG(int bg) {
        log.warn("Frame animation backgrounds do not support in-game backgrounds.");
    }

    @Override
    public void setBG(String filePath) {
        customHolder.loadImage(filePath, imageName);
        log.debug("Custom frame animation background modified (New File Path: " + filePath + ").");
        setup();
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
        log.debug("Custom frame animation background modified (New Image Reference: " + name + ").");
        setup();
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
