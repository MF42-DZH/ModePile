package zeroxfc.nullpo.custom.modes.objects.minesweeper;

import java.util.Random;

public class GameGrid {
	public static final int STATE_SAFE = 0,
			 				STATE_MINE = 1,
			 				STATE_ALREADY_OPEN = 2;
	
	private int length;
	private int height;
	
	private int squares;
	private int mines;
	private float minePercent;
	private Random randomizer;
	
	public GridSpace[][] contents;
	
	public GameGrid() {
		this(10, 10, 0.1f, 0);
	}
	
	public GameGrid(int sizeLength, int sizeHeight, float minePercentArg, long randseed) {
		randomizer = new Random(randseed);
		length = sizeLength;
		height = sizeHeight;
		squares = length * height;
		minePercent = minePercentArg;
		mines = (int)((minePercent / 100f) * squares);
		
		contents = new GridSpace[height][length];
		
		for (int y = 0; y < contents.length; y++) {
			for (int x = 0; x < contents[y].length; x++) {
				contents[y][x] = new GridSpace(false);
			}
		}
	}
	
	public void generateMines(int excludeX, int excludeY) {
		for (int i = 0; i < mines; i++)
        {
            int TestX, TestY;
            int RollCount = 0;

            do
            {
                TestX = randomizer.nextInt(length);
                TestY = randomizer.nextInt(height);
                RollCount++;
            } while (getSurroundingMines(TestX, TestY) >= 3 && RollCount < 6);

            if (!contents[TestY][TestX].isMine && !(TestY == excludeY && TestX == excludeX))
            {
                contents[TestY][TestX].isMine = true;
            }
            else
            {
                i--;
            }
        }

        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < length; x++)
            {
                if (!contents[y][x].isMine)
                {
                	contents[y][x].surroundingMines = getSurroundingMines(x, y);
                }
            }
        }
	}
	
	public int getSurroundingMines(int x, int y) {
		int mine = 0;
		int[][] testLocations = { { -1, -1 }, { 0, -1 }, { 1, -1 },
                { -1, 0 }, { 1, 0 },
                { -1, 1 }, { 0, 1 }, { 1, 1 }};
		
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
                { -1, 1 }, { 0, 1 }, { 1, 1 }};
		
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
                { -1, 1 }, { 0, 1 }, { 1, 1 }};
		
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
		for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < length; x++)
            {
                if (contents[y][x].isMine)
                {
                	contents[y][x].uncovered = true;
                }
            }
        }
	}
	
	public void uncoverNonMines() {
		for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < length; x++)
            {
                if (!contents[y][x].isMine)
                {
                	contents[y][x].uncovered = true;
                }
            }
        }
	}
	
	public void flagAllCovered() {
		for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < length; x++)
            {
                if (!contents[y][x].uncovered)
                {
                	contents[y][x].flagged = true;
                	contents[y][x].question = false;
                }
            }
        }
	}
	
	public int uncoverAt(int x, int y) {
		if (!contents[y][x].flagged && !contents[y][x].uncovered && !contents[y][x].question) {
			contents[y][x].uncovered = true;
			
			if (contents[y][x].isMine) {
				return STATE_MINE;
			} else {
				if (contents[y][x].surroundingMines == 0) {
					int[][] testLocations = { { -1, -1 }, { 0, -1 }, { 1, -1 },
			                  { -1, 0 }, { 1, 0 },
			                  { -1, 1 }, { 0, 1 }, { 1, 1 }};
	
					for (int[] loc : testLocations) {
						int px = x + loc[0];
						int py = y + loc[1];
						
						if (px < 0 || px >= length) continue;
						if (py < 0 || py >= height) continue;
						
						if (!contents[py][px].uncovered) uncoverAt(px, py);
					}
				}
				
				return STATE_SAFE;
			}
		}
		
		return STATE_ALREADY_OPEN;
	}
	
	public int cycleState(int x, int y) {
		if (!contents[y][x].uncovered) {
			if (!contents[y][x].flagged && !contents[y][x].question) {
				contents[y][x].flagged = true;
				contents[y][x].question = false;
				return 0;
			} else if (contents[y][x].flagged && !contents[y][x].question) {
				contents[y][x].flagged = false;
				contents[y][x].question = true;
				return 0;
			} else if (!contents[y][x].flagged && contents[y][x].question) {
				contents[y][x].flagged = false;
				contents[y][x].question = false;
				return 0;
			}
		}
		return 1;
	}
	
	public GridSpace getSquareAt(int x, int y) {
		return contents[y][x];
	}
	
	public int getCoveredSquares() {
		int amt = 0;
		for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < length; x++)
            {
                if (!contents[y][x].uncovered)
                {
                	amt++;
                }
            }
        }
		return amt;
	}
	
	public int getFlaggedSquares() {
		int amt = 0;
		for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < length; x++)
            {
                if (contents[y][x].flagged)
                {
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
}
