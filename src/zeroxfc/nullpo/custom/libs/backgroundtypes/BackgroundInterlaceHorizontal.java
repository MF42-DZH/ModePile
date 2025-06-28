package zeroxfc.nullpo.custom.libs.backgroundtypes;

import mu.nu.nullpo.game.play.GameEngine;
import zeroxfc.nullpo.custom.libs.CustomResourceHolder;

public class BackgroundInterlaceHorizontal extends AnimatedBackgroundHook {
    private static final int SCREEN_WIDTH = 640;
    private static final int SCREEN_HEIGHT = 480;
    private static final int DEFAULT_ROW_HEIGHT = 1;
    private static final int DEFAULT_TIMER_MAX = 30;
    private static final boolean LEFT_ODD_DEFAULT = true;
    private static final float BASE_SCALE = 1f;
    private static final float SCALE_VARIANCE = 0.1f;

    private ImageChunk[] chunks;
    private int pulseTimer, pulseTimerMax, rowHeight;
    private float baseScale, scaleVariance;
    private boolean leftOdd, reverse;

    {
        ID = AnimatedBackgroundHook.ANIMATION_INTERLACE_HORIZONTAL;
        customHolder = new CustomResourceHolder();
        setImageName("localBG");
    }

    public BackgroundInterlaceHorizontal(int bgNumber, Integer rowHeight, Integer pulseTimerFrames, Float pulseBaseScale, Float pulseScaleVariance, Boolean leftOdd, Boolean reverse) {
        if (bgNumber < 0 || bgNumber > 19) bgNumber = 0;

        customHolder.loadImage("res/graphics/back" + bgNumber + ".png", imageName);
        setup(rowHeight, pulseTimerFrames, pulseBaseScale, pulseScaleVariance, leftOdd, reverse);

        log.debug("Non-custom horizontal interlace background (" + bgNumber + ") created.");
    }

    public BackgroundInterlaceHorizontal(String filePath, Integer rowHeight, Integer pulseTimerFrames, Float pulseBaseScale, Float pulseScaleVariance, Boolean leftOdd, Boolean reverse) {
        customHolder.loadImage(filePath, imageName);
        setup(rowHeight, pulseTimerFrames, pulseBaseScale, pulseScaleVariance, leftOdd, reverse);

        log.debug("Custom horizontal interlace background created (File Path: " + filePath + ").");
    }

    private void setup(Integer rowHeight, Integer pulseTimerFrames, Float pulseBaseScale, Float pulseScaleVariance, Boolean leftOdd, Boolean reverse) {
        if (rowHeight == null) rowHeight = DEFAULT_ROW_HEIGHT;
        if (480 % rowHeight != 0) rowHeight = DEFAULT_ROW_HEIGHT;
        if (pulseTimerFrames == null) pulseTimerFrames = DEFAULT_TIMER_MAX;
        if (pulseBaseScale == null) pulseBaseScale = BASE_SCALE;
        if (pulseScaleVariance == null) pulseScaleVariance = SCALE_VARIANCE;
        if (leftOdd == null) leftOdd = LEFT_ODD_DEFAULT;
        if (reverse == null) reverse = false;

        this.chunks = new ImageChunk[SCREEN_HEIGHT / rowHeight];
        this.leftOdd = !leftOdd;
        this.pulseTimerMax = pulseTimerFrames;
        this.baseScale = pulseBaseScale;
        this.scaleVariance = pulseScaleVariance;
        this.pulseTimer = pulseTimerFrames;
        this.reverse = reverse;
        this.rowHeight = rowHeight;

        for (int i = 0; i < chunks.length; i++) {
            final boolean left = leftOdd && (i % 2 == 1);
            final int anchorType = left ? ImageChunk.ANCHOR_POINT_TR : ImageChunk.ANCHOR_POINT_TL;
            final int[] anchorLocation = new int[] { left ? SCREEN_WIDTH : 0, i * rowHeight };
            final int[] srcLocation = new int[] { 0, i * rowHeight };
            chunks[i] = new ImageChunk(anchorType, anchorLocation, srcLocation, new int[] { 640, rowHeight }, new float[] { baseScale, 1f });
        }
    }

    /**
     * Performs an update tick on the background. Advisably used in onLast.
     */
    @Override
    public void update() {
        pulseTimer++;
        if (pulseTimer >= pulseTimerMax) {
            pulseTimer = 0;
        }

        for (int i = 0; i < chunks.length; i++) {
            int j = i;
            if (reverse) j = chunks.length - 1 - i;

            int ppu = (pulseTimer + i) % pulseTimerMax;
            double s = Math.sin(Math.PI * ((double) ppu / pulseTimerMax));
            double scale = baseScale + (scaleVariance * s);
            if (scale < 1d) scale = 1d;
            chunks[j].setScale(new float[] { (float) scale, 1f });
        }
    }

    public void modifyValues(Integer pulseFrames, Float pulseBaseScale, Float pulseScaleVariance, Boolean leftOdd) {
        if (leftOdd != null) this.leftOdd = leftOdd;
        if (pulseFrames != null) pulseTimerMax = pulseFrames;
        if (pulseBaseScale != null) this.baseScale = pulseBaseScale;
        if (pulseScaleVariance != null) this.scaleVariance = pulseScaleVariance;

        if (pulseTimer > pulseTimerMax) pulseTimer = pulseTimerMax;
    }

    /**
     * Resets the background to its base state.
     */
    @Override
    public void reset() {
        pulseTimer = pulseTimerMax;
        update();
    }

    /**
     * Draws the background to the game screen.
     *
     * @param engine   Current GameEngine instance
     * @param playerID Current player ID (1P = 0)
     */
    @Override
    public void draw(GameEngine engine, int playerID) {
        for (ImageChunk i : chunks) {
            int[] pos = i.getDrawLocation();
            int[] ddim = i.getDrawDimensions();
            int[] sloc = i.getSourceLocation();
            int[] sdim = i.getSourceDimensions();
            customHolder.drawOffsetImage(engine, imageName, pos[0], pos[1], ddim[0], ddim[1], sloc[0], sloc[1], sdim[0], sdim[1], 255, 255, 255, 255);
        }
    }

    /**
     * Change BG to one of the default ones.
     *
     * @param bg New BG number
     */
    @Override
    public void setBG(int bg) {
        customHolder.loadImage("res/graphics/back" + bg + ".png", imageName);
        log.debug("Non-custom horizontal interlace background modified (New BG: " + bg + ").");
    }

    /**
     * Change BG to a custom BG using its file path.
     *
     * @param filePath File path of new background
     */
    @Override
    public void setBG(String filePath) {
        customHolder.loadImage(filePath, imageName);
        log.debug("Custom horizontal interlace background modified (New File Path: " + filePath + ").");
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
        log.debug("Custom horizontal interlace background modified (New Image Reference: " + name + ").");
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
