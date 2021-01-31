package zeroxfc.nullpo.custom.modes.objects.twothousandandfortyeight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import zeroxfc.nullpo.custom.libs.ValueWrapper;

public class NumberGrid {
    // region Static Fields
    public static final int DIRECTION_UP = 0,
        DIRECTION_DOWN = 1,
        DIRECTION_LEFT = 2,
        DIRECTION_RIGHT = 3;

    private static final int[] COLOUR_VALUES = {
        Block.BLOCK_COLOR_NONE,
        Block.BLOCK_COLOR_ORANGE,
        Block.BLOCK_COLOR_YELLOW,
        Block.BLOCK_COLOR_RED,
        Block.BLOCK_COLOR_GREEN,
        Block.BLOCK_COLOR_CYAN,
        Block.BLOCK_COLOR_BLUE,
        Block.BLOCK_COLOR_PURPLE,
        Block.BLOCK_COLOR_GRAY,
        Block.BLOCK_COLOR_GRAY,
        Block.BLOCK_COLOR_GRAY,
        Block.BLOCK_COLOR_RAINBOW
    };

    private static final int[] COLOUR_NUMBERS = {
        0,
        2,
        4,
        8,
        16,
        32,
        64,
        128,
        256,
        512,
        1024,
        2048
    };

    private static final double TWO_CHANCE = 0.9;
    private final HashMap<Integer, Integer> colourAssign;
    private final int height;
    private final int width;
    private final Random localRandom;
    // endregion Static Fields
    public GridSpace2048[][] grid;

    public NumberGrid(int width, int height, long randSeed) {
        this.height = height;
        this.width = width;
        this.grid = new GridSpace2048[height][width];
        this.colourAssign = new HashMap<>(11);
        this.localRandom = new Random(randSeed);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                this.grid[y][x] = new GridSpace2048(0);
            }
        }

        for (int i = 0; i < COLOUR_VALUES.length; i++) {
            colourAssign.put(COLOUR_NUMBERS[i], COLOUR_VALUES[i]);
        }

        createSquare();
        createSquare();
    }

    /**
     * Gets a cell. Returns null if index out of bounds.
     *
     * @param x x-coordinate of cell
     * @param y y-coordicate of cell
     * @return GridCell2048 instance in grid cell
     */
    public GridSpace2048 getCell(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return grid[y][x];
        } else {
            return null;
        }
    }

    public void setCell(GridSpace2048 space, int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            grid[y][x] = space.clone();
        }
    }

    public boolean movesPossible() {
        int[][] testLocations = {
            { 0, -1 }, { 0, 1 }, { -1, 0 }, { 1, 0 }
        };

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (getCell(x, y) != null) {
                    if (getCell(x, y).getValue() == 0) {
                        return true;  // If cell is 0, there is obviously a move.
                    } else {
                        for (int[] is : testLocations) {
                            int tX, tY;
                            tX = x + is[0];
                            tY = y + is[1];

                            if (getCell(tX, tY) != null) {
                                if (getCell(x, y).getValue() == getCell(tX, tY).getValue()) {
                                    return true;  // If an adjacent cell has an equal value, it must be a move.
                                }
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    public Block[][] getDrawGrid(GameEngine engine) {
        Block[][] localField = new Block[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int cellValue = getCell(x, y).getValue();
                int colour = colourAssign.getOrDefault(cellValue, Block.BLOCK_COLOR_RAINBOW);

                Block blk = new Block(colour, engine.getSkin(), Block.BLOCK_ATTRIBUTE_VISIBLE);
                blk.setAttribute(Block.BLOCK_ATTRIBUTE_OUTLINE, false);

                localField[y][x] = new Block(blk);
            }
        }

        return localField;
    }

    public boolean moveSquares(GameEngine engine, EventReceiver receiver, int playerID, int direction, ValueWrapper counter) {
        boolean moved = false;
        boolean merged = false;

        ArrayList<Integer[]> exclusion = new ArrayList<>();

        switch (direction) {
            case DIRECTION_UP:
                for (int y = 1; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int tY = y - 1;
                        if (getCell(x, y).getValue() != 0 && getCell(x, tY).getValue() == 0 && !exclusion.contains(new Integer[] { x, y })) {
                            setCell(getCell(x, y), x, tY);
                            setCell(new GridSpace2048(0), x, y);

                            moved = true;
                        } else if (getCell(x, y).getValue() != 0 && getCell(x, tY).isEqual(getCell(x, y)) && !getCell(x, y).getMerged() && !getCell(x, tY).getMerged()) {
                            setCell(new GridSpace2048(getCell(x, y).getValue() * 2, true), x, tY);
                            setCell(new GridSpace2048(0), x, y);

                            moved = true;
                            merged = true;
                            counter.valueInt += getCell(x, tY).getValue();
                            receiver.blockBreak(engine, playerID, x * 2, tY * 2, getBlock(getCell(x, tY).getValue()));
                            receiver.blockBreak(engine, playerID, x * 2 + 1, tY * 2, getBlock(getCell(x, tY).getValue()));
                            receiver.blockBreak(engine, playerID, x * 2, tY * 2 + 1, getBlock(getCell(x, tY).getValue()));
                            receiver.blockBreak(engine, playerID, x * 2 + 1, tY * 2 + 1, getBlock(getCell(x, tY).getValue()));
                            exclusion.add(new Integer[] { x, tY });
                        }
                    }
                }
                break;
            case DIRECTION_DOWN:
                for (int y = height - 2; y >= 0; y--) {
                    for (int x = 0; x < width; x++) {
                        int tY = y + 1;
                        if (getCell(x, y).getValue() != 0 && getCell(x, tY).getValue() == 0 && !exclusion.contains(new Integer[] { x, y })) {
                            setCell(getCell(x, y), x, tY);
                            setCell(new GridSpace2048(0), x, y);

                            moved = true;
                        } else if (getCell(x, y).getValue() != 0 && getCell(x, tY).isEqual(getCell(x, y)) && !getCell(x, y).getMerged() && !getCell(x, tY).getMerged()) {
                            setCell(new GridSpace2048(getCell(x, y).getValue() * 2, true), x, tY);
                            setCell(new GridSpace2048(0), x, y);

                            moved = true;
                            merged = true;
                            counter.valueInt += getCell(x, tY).getValue();
                            receiver.blockBreak(engine, playerID, x * 2, tY * 2, getBlock(getCell(x, tY).getValue()));
                            receiver.blockBreak(engine, playerID, x * 2 + 1, tY * 2, getBlock(getCell(x, tY).getValue()));
                            receiver.blockBreak(engine, playerID, x * 2, tY * 2 + 1, getBlock(getCell(x, tY).getValue()));
                            receiver.blockBreak(engine, playerID, x * 2 + 1, tY * 2 + 1, getBlock(getCell(x, tY).getValue()));
                            exclusion.add(new Integer[] { x, tY });
                        }
                    }
                }
                break;
            case DIRECTION_LEFT:
                for (int y = 0; y < height; y++) {
                    for (int x = 1; x < width; x++) {
                        int tX = x - 1;
                        if (getCell(x, y).getValue() != 0 && getCell(tX, y).getValue() == 0 && !exclusion.contains(new Integer[] { x, y })) {
                            setCell(getCell(x, y), tX, y);
                            setCell(new GridSpace2048(0), x, y);

                            moved = true;
                        } else if (getCell(x, y).getValue() != 0 && getCell(tX, y).isEqual(getCell(x, y)) && !getCell(x, y).getMerged() && !getCell(tX, y).getMerged()) {
                            setCell(new GridSpace2048(getCell(x, y).getValue() * 2, true), tX, y);
                            setCell(new GridSpace2048(0), x, y);

                            moved = true;
                            merged = true;
                            counter.valueInt += getCell(tX, y).getValue();
                            receiver.blockBreak(engine, playerID, tX * 2, y * 2, getBlock(getCell(tX, y).getValue()));
                            receiver.blockBreak(engine, playerID, tX + 1, y * 2, getBlock(getCell(tX, y).getValue()));
                            receiver.blockBreak(engine, playerID, tX * 2, y * 2 + 1, getBlock(getCell(tX, y).getValue()));
                            receiver.blockBreak(engine, playerID, tX * 2 + 1, y * 2 + 1, getBlock(getCell(tX, y).getValue()));
                            exclusion.add(new Integer[] { tX, y });
                        }
                    }
                }
                break;
            case DIRECTION_RIGHT:
                for (int y = 0; y < height; y++) {
                    for (int x = width - 2; x >= 0; x--) {
                        int tX = x + 1;
                        if (getCell(x, y).getValue() != 0 && getCell(tX, y).getValue() == 0 && !exclusion.contains(new Integer[] { x, y })) {
                            setCell(getCell(x, y), tX, y);
                            setCell(new GridSpace2048(0), x, y);

                            moved = true;
                        } else if (getCell(x, y).getValue() != 0 && getCell(tX, y).isEqual(getCell(x, y)) && !getCell(x, y).getMerged() && !getCell(tX, y).getMerged()) {
                            setCell(new GridSpace2048(getCell(x, y).getValue() * 2, true), tX, y);
                            setCell(new GridSpace2048(0), x, y);

                            moved = true;
                            merged = true;
                            counter.valueInt += getCell(tX, y).getValue();
                            receiver.blockBreak(engine, playerID, tX * 2, y * 2, getBlock(getCell(tX, y).getValue()));
                            receiver.blockBreak(engine, playerID, tX + 1, y * 2, getBlock(getCell(tX, y).getValue()));
                            receiver.blockBreak(engine, playerID, tX * 2, y * 2 + 1, getBlock(getCell(tX, y).getValue()));
                            receiver.blockBreak(engine, playerID, tX * 2 + 1, y * 2 + 1, getBlock(getCell(tX, y).getValue()));
                            exclusion.add(new Integer[] { tX, y });
                        }
                    }
                }
                break;
            default:
                break;
        }

        if (moved) engine.playSE("move");
        else if (!moved) engine.playSE("linefall");
        if (merged) engine.playSE("square_g");

        return moved;
    }

    public int getMaxSquare() {
        int max = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (getCell(x, y).getValue() > max) max = getCell(x, y).getValue();
            }
        }

        return max;
    }

    public void createSquare() {
        int x, y;

        ArrayList<int[]> locations = new ArrayList<>();

        for (int y2 = 0; y2 < height; y2++) {
            for (int x2 = 0; x2 < width; x2++) {
                if (getCell(x2, y2).getValue() == 0) locations.add(new int[] { x2, y2 });
            }
        }


        if (locations.size() > 0) {
            int[] is = locations.get(localRandom.nextInt(locations.size()));
            x = is[0];
            y = is[1];

            double coeff = localRandom.nextDouble();
            setCell(new GridSpace2048(coeff < TWO_CHANCE ? 2 : 4), x, y);
        }
    }

    private Block getBlock(int value) {
        return new Block(colourAssign.getOrDefault(value, Block.BLOCK_COLOR_RAINBOW));
    }
}
