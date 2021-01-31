package zeroxfc.nullpo.custom.libs.backgroundtypes;

import mu.nu.nullpo.game.play.GameEngine;
import zeroxfc.nullpo.custom.libs.ResourceHolderCustomAssetExtension;

public class BackgroundImageSequenceAnim extends AnimatedBackgroundHook {
    private final int frameTime;
    private final int frameCount;
    private final boolean pingPong;
    private int currentTick;
    private int currentFrame;
    private boolean forward;

    {
        ID = AnimatedBackgroundHook.ANIMATION_IMAGE_SEQUENCE_ANIM;
    }

    public BackgroundImageSequenceAnim(String[] filePaths, int frameTime, boolean pingPong) {
        customHolder = new ResourceHolderCustomAssetExtension();

        for (int i = 0; i < filePaths.length; i++) {
            customHolder.loadImage(filePaths[i], "frame" + i);

            int[] dim = customHolder.getImageDimensions("frame" + i);
            if (dim[0] != 640 || dim[1] != 480)
                log.warn("Image at " + filePaths[i] + " is not 640x480. It may not render correctly.");
        }

        this.frameTime = frameTime;
        this.pingPong = pingPong;
        this.frameCount = filePaths.length;

        setup();

        log.debug("Sequence frame animation background created (Frames: " + filePaths.length + ").");
    }

    private void setup() {
        forward = true;
        currentFrame = 0;
        currentTick = 0;
    }


    /**
     * Performs an update tick on the background. Advisably used in onLast.
     */
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

    /**
     * Resets the background to its base state.
     */
    @Override
    public void reset() {
        setup();
    }

    /**
     * Draws the background to the game screen.
     *
     * @param engine   Current GameEngine instance
     * @param playerID Current player ID (1P = 0)
     */
    @Override
    public void draw(GameEngine engine, int playerID) {
        String i = "frame" + currentFrame;
        customHolder.drawImage(engine, i, 0, 0, 640, 480, 0, 0, 640, 480, 255, 255, 255, 255, 0);
    }

    /**
     * Change BG to one of the default ones.
     *
     * @param bg New BG number
     */
    @Override
    public void setBG(int bg) {
        log.warn("Image Sequence animation backgrounds do not support this operation. Please create a new instance.");
    }

    /**
     * Change BG to a custom BG using its file path.
     *
     * @param filePath File path of new background
     */
    @Override
    public void setBG(String filePath) {
        log.warn("Image Sequence animation backgrounds do not support this operation. Please create a new instance.");
    }

    /**
     * Allows the hot-swapping of pre-loaded BGs from a storage instance of a <code>ResourceHolderCustomAssetExtension</code>.
     *
     * @param holder Storage instance
     * @param name   Image name
     */
    @Override
    public void setBGFromHolder(ResourceHolderCustomAssetExtension holder, String name) {
        log.warn("Image Sequence animation backgrounds do not support this operation. Please create a new instance.");
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
