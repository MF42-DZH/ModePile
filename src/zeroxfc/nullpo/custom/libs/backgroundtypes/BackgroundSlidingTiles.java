package zeroxfc.nullpo.custom.libs.backgroundtypes;

import java.util.Random;
import mu.nu.nullpo.game.play.GameEngine;
import zeroxfc.nullpo.custom.libs.CustomResourceHolder;
import zeroxfc.nullpo.custom.libs.types.ObjectAlignment;
import zeroxfc.nullpo.custom.libs.types.ImageChunk;

public class BackgroundSlidingTiles extends AnimatedBackgroundHook {
    private static final int DIRECTION_UP = 0;
    private static final int DIRECTION_RIGHT = 1;
    private static final int DIRECTION_DOWN = 1;
    private static final int DIRECTION_LEFT = 0;
    private static final int DIRECTIONS = 2;

    private ImageChunk[][] gridChunks;
    private int[][] colours;
    private Integer skin;
    private Integer size;
    private Integer colour;
    private Float darkness;
    private boolean custom;
    private Random directionRandomiser;
    private int direction;
    private boolean horizontal;
    private int currentMovement;
    private int width, height;
    private boolean move;

    {
        ID = AnimatedBackgroundHook.ANIMATION_SLIDING_TILES;
        setImageName("localSkin");

        horizontal = true;
        move = false;
        currentMovement = 0;
    }

    public BackgroundSlidingTiles(int skin, Random directionRandomiser, Integer colour, int size, float darkness) {
        custom = false;
        this.skin = skin;

        if (skin < 0 || skin >= CustomResourceHolder.getNumberLoadedBlockSkins()) skin = 0;

        this.colour = colour;
        this.size = size;
        this.darkness = darkness;
        this.directionRandomiser = directionRandomiser;

        setup();

        log.debug("Non-custom sliding tiles background created (Skin: " + skin + ").");
    }

    public BackgroundSlidingTiles(int skin, long seed, Integer colour, int size, float darkness) {
        custom = false;
        this.skin = skin;

        if (skin < 0 || skin >= CustomResourceHolder.getNumberLoadedBlockSkins()) skin = 0;

        this.colour = colour;
        this.size = size;
        this.darkness = darkness;
        this.directionRandomiser = new Random(seed);

        setup();

        log.debug("Non-custom sliding tiles background created (Skin: " + skin + ").");
    }

    public BackgroundSlidingTiles(String filePath, Random directionRandomiser) {
        custom = true;
        this.directionRandomiser = directionRandomiser;

        customHolder = new CustomResourceHolder();
        customHolder.loadImage(filePath, imageName);

        setup();

        log.debug("Custom sliding tiles background created (File Path: " + filePath + ").");
    }

    private void setup() {
        if (customHolder == null) customHolder = new CustomResourceHolder();
        customHolder.loadImage("res/graphics/blank_black_24b.png", "blackBG");

        direction = directionRandomiser.nextInt(DIRECTIONS);
        if (custom) {
            int[] dim = customHolder.getImageDimensions(imageName);

            width = dim[0];
            height = dim[1];

            int sw = 640 / dim[0];
            if (sw * dim[0] < 640) sw++;
            sw += 2;

            int sh = 480 / dim[1];
            if (sh * dim[1] < 480) sh++;
            sh += 2;

            gridChunks = new ImageChunk[sh][sw];

            for (int y = 0; y < gridChunks.length; y++) {
                for (int x = 0; x < gridChunks[y].length; x++) {
                    gridChunks[y][x] = new ImageChunk(ObjectAlignment.TOP_LEFT, new int[] { (x - 1) * dim[0], (y - 1) * dim[1] }, new int[] { 0, 0 }, new int[] { dim[0], dim[1] }, new float[] { 1f, 1f });
                }
            }
        } else {
            int s = 16;
            if (size < 0) s = 8;
            if (size > 0) s = 32;

            width = s;
            height = s;

            gridChunks = new ImageChunk[(480 / s) + 2][(640 / s) + 2];
            colours = new int[(480 / s) + 2][(640 / s) + 2];

            for (int y = 0; y < gridChunks.length; y++) {
                for (int x = 0; x < gridChunks[y].length; x++) {
                    gridChunks[y][x] = new ImageChunk(ObjectAlignment.TOP_LEFT, new int[] { (x - 1) * s, (y - 1) * s }, new int[] { 0, 0 }, new int[] { s, s }, new float[] { 1f, 1f });

                    if (colour != null) {
                        colours[y][x] = colour;
                    } else {
                        colours[y][x] = directionRandomiser.nextInt(8) + 1;
                    }
                }
            }
        }
    }

    @Override
    public void update() {
        if (move) {
            move = false;

            if (horizontal) {
                for (int y = 0; y < gridChunks.length; y++) {
                    for (int x = 0; x < gridChunks[y].length; x++) {
                        int[] locOld = gridChunks[y][x].getAnchorLocation();
                        int yMod = Math.abs(locOld[1] / width);
                        int dir = (direction + yMod) % DIRECTIONS;
                        int xNew;
                        switch (dir) {
                            case DIRECTION_LEFT:
                                xNew = locOld[0] - 1;
                                if (xNew <= (width * -2)) xNew = (gridChunks[0].length - 2) * width;
                                gridChunks[y][x].setAnchorLocation(new int[] { xNew, locOld[1] });
                                break;
                            case DIRECTION_RIGHT:
                                xNew = locOld[0] + 1;
                                if (xNew >= (gridChunks[0].length - 1) * width) xNew = width * -1;
                                gridChunks[y][x].setAnchorLocation(new int[] { xNew, locOld[1] });
                                break;
                            default:
                                break;
                        }
                    }
                }

                currentMovement++;
            } else {
                for (int x = 0; x < gridChunks[0].length; x++) {
                    for (int y = 0; y < gridChunks.length; y++) {
                        int[] locOld = gridChunks[y][x].getAnchorLocation();
                        int xMod = Math.abs(locOld[0] / width);
                        int dir2 = (direction + xMod) % DIRECTIONS;
                        int yNew;
                        switch (dir2) {
                            case DIRECTION_UP:
                                yNew = locOld[1] - 1;
                                if (yNew <= (height * -2)) yNew = (gridChunks.length - 2) * height;
                                gridChunks[y][x].setAnchorLocation(new int[] { locOld[0], yNew });
                                break;
                            case DIRECTION_DOWN:
                                yNew = locOld[1] + 1;
                                if (yNew >= (gridChunks.length - 1) * height) yNew = height * -1;
                                gridChunks[y][x].setAnchorLocation(new int[] { locOld[0], yNew });
                                break;
                            default:
                                break;
                        }
                    }
                }

                currentMovement++;
            }

            if (horizontal) {
                if (currentMovement >= width) {
                    currentMovement = 0;
                    direction = directionRandomiser.nextInt(DIRECTIONS);
                    horizontal = false;
                }
            } else {
                if (currentMovement >= height) {
                    currentMovement = 0;
                    direction = directionRandomiser.nextInt(DIRECTIONS);
                    horizontal = true;
                }
            }
        } else {
            move = true;
        }
    }

    private void swap(int x, int y, int x2, int y2) {
        ImageChunk ic = gridChunks[y][x];
        gridChunks[y][x] = gridChunks[y2][x2];
        gridChunks[y][x] = ic;
    }

    @Override
    public void reset() {
        setup();
        horizontal = true;
        move = false;
        currentMovement = 0;
    }

    @Override
    public void draw(GameEngine engine, int playerID) {
        customHolder.drawImage(engine, "blackBG", 0, 0);
        for (int y = 0; y < gridChunks.length; y++) {
            for (int x = 0; x < gridChunks[y].length; x++) {
                final ImageChunk chunk = gridChunks[y][x];
                final int[] pos = chunk.getDrawLocation();

                if (custom) {
                    customHolder.drawOffsetImage(engine, imageName, chunk, 255, 255, 255, 255);
                } else {
                    float s = 1f;
                    if (size < 0) s = 0.5f;
                    if (size > 0) s = 2f;
                    engine.owner.receiver.drawSingleBlock(engine, playerID, pos[0], pos[1], colours[y][x], skin, false, darkness, 1f, s);
                }
            }
        }
    }

    public void modifyValues(Integer colour, Integer size, Float darkness) {
        if (custom) return;

        if (colour != null) this.colour = colour;
        if (size != null) this.size = size;
        if (darkness != null) this.darkness = darkness;

        if (colour != null || size != null) reset();
    }

    public void setSeed(long seed) {
        this.directionRandomiser = new Random(seed);
        reset();
    }

    /**
     * In this case, BG means block skin.
     *
     * @param bg Block skin to use.
     */
    @Override
    public void setBG(int bg) {
        custom = false;
        this.skin = bg;
        this.colour = null;
        this.size = 0;
        this.darkness = 0f;

        modifyValues(null, null, null);

        log.debug("Non-custom sliding tiles background modified (New Skin: " + bg + ").");
        log.warn("Please set up new values using modifyValues(...)");
    }

    @Override
    public void setBG(String filePath) {
        custom = true;
        customHolder.loadImage(filePath, imageName);

        reset();

        log.debug("Custom sliding tiles background modified (New File Path: " + filePath + ").");
    }

    /**
     * Allows the hot-swapping of pre-loaded BGs from a storage instance of a <code>CustomResourceHolder</code>.
     *
     * @param holder Storage instance
     * @param name   Image name
     */
    @Override
    public void setBGFromHolder(CustomResourceHolder holder, String name) {
        custom = true;
        customHolder.putImageAt(holder.getImageAt(name), imageName);

        reset();

        log.debug("Custom sliding tiles background modified (New Image Reference: " + name + ").");
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
