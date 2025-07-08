package zeroxfc.nullpo.custom.modes.objects.minesweeper;

import java.util.Random;

public class GameGrid {
    private final int length;
    private final int height;
    private final int squares;
    private final int mines;
    private final float minePercent;
    private final Random randomizer;
    private GridSpace[][] contents;

    private int version;

    public GameGrid(int version) {
        this(10, 10, 0.1f, 0, version);
    }

    public GameGrid(int sizeLength, int sizeHeight, float minePercentArg, long randseed, int version) {
        randomizer = new Random(randseed);
        length = sizeLength;
        height = sizeHeight;
        squares = length * height;
        minePercent = minePercentArg;
        mines = (int) ((minePercent / 100f) * squares);
        this.version = version;

        contents = new GridSpace[height][length];

        for (int y = 0; y < contents.length; y++) {
            for (int x = 0; x < contents[y].length; x++) {
                contents[y][x] = new GridSpace(false);
            }
        }
    }

    public void generateMines(int excludeX, int excludeY) {
        for (int i = 0; i < mines; i++) {
            int testX, testY;

            if (version < 2) {
                int rollCount = 0;

                do {
                    testX = randomizer.nextInt(length);
                    testY = randomizer.nextInt(height);
                    rollCount++;
                } while (getSurroundingMines(testX, testY) >= 3 && rollCount < 6);
            } else {
                testX = randomizer.nextInt(length);
                testY = randomizer.nextInt(height);
            }

            if (!contents[testY][testX].isMine && !(testY == excludeY && testX == excludeX)) {
                contents[testY][testX].isMine = true;
            } else {
                i--;
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < length; x++) {
                if (!contents[y][x].isMine) {
                    contents[y][x].surroundingMines = getSurroundingMines(x, y);
                }
            }
        }
    }

    public int getSurroundingMines(int x, int y) {
        int mine = 0;
        int[][] testLocations = { { -1, -1 }, { 0, -1 }, { 1, -1 },
            { -1, 0 }, { 1, 0 },
            { -1, 1 }, { 0, 1 }, { 1, 1 } };

        for (int[] loc : testLocations) {
            int px = x + loc[0];
            int py = y + loc[1];

            if (px < 0 || px >= length) continue;
            if (py < 0 || py >= height) continue;

            if (contents[py][px].isMine) mine++;
        }

        return mine;
    }

    public int getSurroundingFlags(int x, int y) {
        int flag = 0;
        int[][] testLocations = { { -1, -1 }, { 0, -1 }, { 1, -1 },
            { -1, 0 }, { 1, 0 },
            { -1, 1 }, { 0, 1 }, { 1, 1 } };

        for (int[] loc : testLocations) {
            int px = x + loc[0];
            int py = y + loc[1];

            if (px < 0 || px >= length) continue;
            if (py < 0 || py >= height) continue;

            if (contents[py][px].flagged) flag++;
        }

        return flag;
    }

    public int getSurroundingCovered(int x, int y) {
        int flag = 0;
        int[][] testLocations = { { -1, -1 }, { 0, -1 }, { 1, -1 },
            { -1, 0 }, { 1, 0 },
            { -1, 1 }, { 0, 1 }, { 1, 1 } };

        for (int[] loc : testLocations) {
            int px = x + loc[0];
            int py = y + loc[1];

            if (px < 0 || px >= length) continue;
            if (py < 0 || py >= height) continue;

            if (!contents[py][px].uncovered) flag++;
        }

        return flag;
    }

    public void uncoverAllMines() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < length; x++) {
                if (contents[y][x].isMine) {
                    contents[y][x].uncovered = true;
                }
            }
        }
    }

    public void uncoverNonMines() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < length; x++) {
                if (!contents[y][x].isMine) {
                    contents[y][x].uncovered = true;
                }
            }
        }
    }

    public void flagAllCovered() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < length; x++) {
                if (!contents[y][x].uncovered) {
                    contents[y][x].flagged = true;
                    contents[y][x].question = false;
                }
            }
        }
    }

    public Square uncoverAt(int x, int y) {
        if (!contents[y][x].flagged && !contents[y][x].uncovered && !contents[y][x].question) {
            contents[y][x].uncovered = true;

            if (contents[y][x].isMine) {
                return Square.MINE;
            } else {
                if (contents[y][x].surroundingMines == 0) {
                    int[][] testLocations = { { -1, -1 }, { 0, -1 }, { 1, -1 },
                        { -1, 0 }, { 1, 0 },
                        { -1, 1 }, { 0, 1 }, { 1, 1 } };

                    for (int[] loc : testLocations) {
                        int px = x + loc[0];
                        int py = y + loc[1];

                        if (px < 0 || px >= length) continue;
                        if (py < 0 || py >= height) continue;

                        if (!contents[py][px].uncovered) uncoverAt(px, py);
                    }
                }

                return Square.SAFE;
            }
        }

        return Square.ALREADY_OPEN;
    }

    public Square cycleState(int x, int y) {
        if (!contents[y][x].uncovered) {
            if (!contents[y][x].flagged && !contents[y][x].question) {
                contents[y][x].flagged = true;
                contents[y][x].question = false;
                return Square.SAFE;
            } else if (contents[y][x].flagged && !contents[y][x].question) {
                contents[y][x].flagged = false;
                contents[y][x].question = true;
                return Square.SAFE;
            } else if (!contents[y][x].flagged && contents[y][x].question) {
                contents[y][x].flagged = false;
                contents[y][x].question = false;
                return Square.SAFE;
            }
        }

        return Square.MINE;
    }

    public GridSpace getSquareAt(int x, int y) {
        return contents[y][x];
    }

    public int getCoveredSquares() {
        int amt = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < length; x++) {
                if (!contents[y][x].uncovered) {
                    amt++;
                }
            }
        }
        return amt;
    }

    public int getFlaggedSquares() {
        int amt = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < length; x++) {
                if (contents[y][x].flagged) {
                    amt++;
                }
            }
        }
        return amt;
    }

    public int getSquares() {
        return squares;
    }

    public int getMines() {
        return mines;
    }

    public float getMinePercent() {
        return minePercent;
    }

    public int getLength() {
        return length;
    }

    public int getHeight() {
        return height;
    }

    // Representation of a square.
    public enum Square {
        SAFE, MINE, ALREADY_OPEN
    }
}
