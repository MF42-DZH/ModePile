package zeroxfc.nullpo.custom.modes.objects.gemswap;

import java.util.ArrayList;
import java.util.Random;

import org.apache.log4j.Logger;

import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.play.GameEngine;

public class GemField {
	private static Logger log = Logger.getLogger(GemField.class);
	
	/** Gem IDs */
	public static final int GEMID_EMPTY = 0,
							GEMID_NORMAL = 1,
							GEMID_POWER = 2,
							GEMID_STAR = 3,
							GEMID_HYPERCUBE = 4,
							GEMID_SUPERNOVA = 5;
	
	public static final int ATTRIBUTE_RECENTSWAP = 0,
			                ATTRIBUTE_RECENTFALL = 1,
			                ATTRIBUTE_MATCHEDHORIZONTAL = 2,
			                ATTRIBUTE_MATCHEDVERTICAL = 3,
			                ATTRIBUTE_SPECIAL = 4,
			                ATTRIBUTE_ACTIONCONDUCTED = 5,
			                ATTRIBUTE_DESTROY = 6;
	
	public static final int[] GEM_COLOURS = {
		Block.BLOCK_COLOR_RED,
		Block.BLOCK_COLOR_ORANGE,
		Block.BLOCK_COLOR_YELLOW,
		Block.BLOCK_COLOR_GREEN,
		Block.BLOCK_COLOR_BLUE,
		Block.BLOCK_COLOR_PURPLE,
		Block.BLOCK_COLOR_GRAY
	};
	
	public static final int MAX_GEM_COLOURS = 7;

	private static final double randomCoefficient = (2.0 / 3.0);
	
	private int fieldWidth, fieldHeight;
	private Gem[][] fieldContents;
	private int[][] fieldHorizontalMatchValues;
	private int[][] fieldVerticalMatchValues;
	private Random randEngine;
	private long randSeed;

	public GemField(int width, int height, long seed) {
		fieldContents = new Gem[height][width];
		fieldHorizontalMatchValues = new int[height][width];
		fieldVerticalMatchValues = new int[height][width];
		
		fieldWidth = width;
		fieldHeight = height;
		
		randSeed = seed;
		randEngine = new Random(seed);
		
		for (int i = 0; i < fieldContents.length; i++) {
			for (int j = 0; j < fieldContents[i].length; j++) {
				fieldContents[i][j] = new EmptyGem(j, i);
			}
		}
	}
	
	public GemField(GemField field) {
		fieldWidth = field.getWidth();
		fieldHeight = field.getHeight();
		randSeed = field.getRandSeed();
		randEngine = field.getRandom();
		fieldHorizontalMatchValues = field.getFieldHorizontalMatchValues();
		fieldVerticalMatchValues = field.getFieldVerticalMatchValues();
		
		fieldContents = new Gem[fieldHeight][fieldWidth];
		for (int y = 0; y < fieldHeight; y++) {
			for (int x = 0; x < fieldWidth; x++) {
				fieldContents[y][x] = field.getCell(x, y).getSelf();
			}
		}
	}
	
	public void resetMatchArrays() {
		fieldHorizontalMatchValues = new int[fieldHeight][fieldWidth];
		fieldVerticalMatchValues = new int[fieldHeight][fieldWidth];
	}

	public void generateField() {
		for (int y = 0; y < fieldHeight; y++) {
			for (int x = 0; x < fieldWidth / 2; x++) {
				int x2 = (x * 2) + (y % 2);
				
				fieldContents[y][x2] = new NormalGem(x2, y, GEM_COLOURS[randEngine.nextInt(MAX_GEM_COLOURS)]);
			}
		}
		
		for (int y = 0; y < fieldHeight; y++) {
			for (int x = 0; x < fieldWidth / 2; x++) {
				int x2 = (x * 2) + (1 - (y % 2));
				int[][] loc = getSurroundingLocations(x2, y);
				int[] colors = new int[getSurrounding(x2, y)];
				
				int i = 0;
				for (int[] is : loc) {
					colors[i] = fieldContents[is[1]][is[0]].getColour();
					i++;
				}
				
				double res = randEngine.nextDouble();

				if (res < randomCoefficient) {
					fieldContents[y][x2] = new NormalGem(x2, y, GEM_COLOURS[randEngine.nextInt(GEM_COLOURS.length)]);
				} else {
					fieldContents[y][x2] = new NormalGem(x2, y, colors[randEngine.nextInt(colors.length)]);
				}
			}
		}
	}
	
	public int getSurrounding(int x, int y) {
		int amt = 0;
		int[][] testLocations = {
			{ 0, -1 }, { 1, 0 }, { 0, 1 }, { -1, 0 }
		};
		
		for (int[] is : testLocations) {
			int tX = x + is[0];
			int tY = y + is[1];
			
			if (tX >= 0 && tX < fieldWidth && tY >= 0 && tY < fieldHeight) {
				amt++;
			}
		}
		
		return amt;
	}
	
	public int[][] getSurroundingLocations(int x, int y) {
		int[][] loc = new int[getSurrounding(x, y)][];
		
		int[][] testLocations = {
			{ 0, -1 }, { 1, 0 }, { 0, 1 }, { -1, 0 }
		};
		
		int i = 0;
		for (int[] is : testLocations) {
			int tX = x + is[0];
			int tY = y + is[1];
			
			if (tX >= 0 && tX < fieldWidth && tY >= 0 && tY < fieldHeight) {
				loc[i] = new int[] { tX, tY };
				
				i++;
			}
		}
		
		return loc;
	}
	
	public int possibleMatches() {
		int patternMatches = 0;

		int[][][] testLocationPairs = {
			{
				{-2, 0},
				{-3, 0}
			},
			{
				{ 2, 0},
				{ 3, 0}
			},
			{
				{ 0,-2},
				{ 0,-3}
			},
			{
				{ 0, 2},
				{ 0, 3}
			},
			{
				{-1,-1},
				{-2,-1}
			},
			{
				{ 1,-1},
				{ 2,-1}
			},
			{
				{-1, 1},
				{-2, 1}
			},
			{
				{ 1, 1},
				{ 2, 1}
			},
			{
				{ 1,-1},
				{ 1,-2}
			},
			{
				{ 1, 1},
				{ 1, 2}
			},
			{
				{-1, 1},
				{-1, 2}
			},
			{
				{-1,-1},
				{-1,-2}
			},
			{
				{-1,-1},
				{ 1,-1}
			},
			{
				{ 1,-1},
				{ 1, 1}
			},
			{
				{-1,-1},
				{-1, 1}
			},
			{
				{-1, 1},
				{ 1, 1}
			}
		};
		
		for (int y = 0; y < fieldHeight; y++) {
			for (int x = 0; x < fieldWidth; x++) {
				if (fieldContents[y][x].getID() == GEMID_HYPERCUBE) {
					patternMatches += getSurrounding(x, y);
				} else {
					int colour = fieldContents[y][x].getColour();

					for (int[][] coordPairSet : testLocationPairs) {
						boolean match = true;

						for (int[] coordPair : coordPairSet) {
							int tX = x + coordPair[0];
							int tY = y + coordPair[1];

							if (tX >= 0 && tX < fieldWidth && tY >= 0 && tY < fieldHeight) {
								if (colour != fieldContents[tY][tX].getColour()) {
									match = false;
									break;
								}
							} else {
								match = false;
								break;
							}
						}

						if (match) patternMatches++;
					}
				}
			}
		}

		return patternMatches;
	}
	
	/**
	 * Sets all match status (both horizontal and vertical) to a state.
	 * Useful for hypercube annihilation.
	 * @param status New match status.
	 */
	public void setAllMatchStatus(boolean status) {
		for (int y = 0; y < fieldHeight; y++) {
			for (int x = 0; x < fieldWidth / 2; x++) {
				fieldContents[y][x].setMatchedHorizontal(status);
				fieldContents[y][x].setMatchedVertical(status);
			}
		}
	}

	public void matchCheck() {
		// 1: Vertical match check.
		for (int y = 0; y < fieldHeight; y++) {
			for (int x = 0; x < fieldWidth; x++) {
				if ( fieldContents[y][x].getID() == GEMID_HYPERCUBE || fieldContents[y][x].getMatchedVertical() ) {
					continue;
				} else {
					ArrayList<int[]> coordList = new ArrayList<int[]>();
					coordList.add( new int[] { x, y } );

					int colour = fieldContents[y][x].getColour();

					int yOffset = -1;
					while (y + yOffset >= 0) {
						if ( fieldContents[y + yOffset][x].getColour() == colour ) {
							coordList.add( new int[] { x, y + yOffset } );
						} else {
							break;
						}

						yOffset--;
					}

					yOffset = 1;
					while (y + yOffset < fieldHeight) {
						if ( fieldContents[y + yOffset][x].getColour() == colour ) {
							coordList.add( new int[] { x, y + yOffset } );
						} else {
							break;
						}

						yOffset++;
					}

					if (coordList.size() >= 3) {
						for (int[] is : coordList) {
							int x2 = is[0];
							int y2 = is[1];
							
							log.debug(coordList.size() + "-MATCH AT " + x2 + ", "+ y2);

							fieldContents[y2][x2].setMatchedVertical(true);
							fieldContents[y2][x2].setDestroy(true);
							fieldVerticalMatchValues[y2][x2] = coordList.size();
						}
					}
				}
			}
		}

		// 2: Horizontal match check.
		for (int y = 0; y < fieldHeight; y++) {
			for (int x = 0; x < fieldWidth; x++) {
				if ( fieldContents[y][x].getID() == GEMID_HYPERCUBE || fieldContents[y][x].getMatchedHorizontal() ) {
					continue;
				} else {
					ArrayList<int[]> coordList = new ArrayList<int[]>();
					coordList.add( new int[] { x, y } );

					int colour = fieldContents[y][x].getColour();

					int xOffset = -1;
					while (x + xOffset >= 0) {
						if ( fieldContents[y][x + xOffset].getColour() == colour ) {
							coordList.add( new int[] { x + xOffset, y } );
						} else {
							break;
						}

						xOffset--;
					}

					xOffset = 1;
					while (x + xOffset < fieldWidth) {
						if ( fieldContents[y][x + xOffset].getColour() == colour ) {
							coordList.add( new int[] { x + xOffset, y } );
						} else {
							break;
						}

						xOffset++;
					}

					if (coordList.size() >= 3) {
						for (int[] is : coordList) {
							int x2 = is[0];
							int y2 = is[1];

							log.debug(coordList.size() + "-MATCH AT " + x2 + ", "+ y2);
							
							fieldContents[y2][x2].setMatchedHorizontal(true);
							fieldContents[y2][x2].setDestroy(true);
							fieldHorizontalMatchValues[y2][x2] = coordList.size();
						}
					}
				}
			}
		}
		
		for (int y = 0; y < fieldHeight; y++) {
			String debugString = "";
			
			for (int x = 0; x < fieldWidth; x++) {
				debugString += fieldHorizontalMatchValues[y][x] + " ";
			}
			log.debug(debugString);
		}
		
		for (int y = 0; y < fieldHeight; y++) {
			String debugString = "";
			
			for (int x = 0; x < fieldWidth; x++) {
				debugString += fieldVerticalMatchValues[y][x] + " ";
			}
			log.debug(debugString);
		}
	}
	
	public void createGemsOnTopRow() {
		for (int x = 0; x < fieldWidth; x++) {
			if ( fieldContents[0][x].getID() == GEMID_EMPTY ) {
				double res = randEngine.nextDouble();

				if (res < randomCoefficient) {
					fieldContents[0][x] = new NormalGem(x, 0, GEM_COLOURS[randEngine.nextInt(GEM_COLOURS.length)]);
				} else {
					int[][] loc = getSurroundingLocations(x, 0);
					int[] colors = new int[getSurrounding(x, 0)];
					
					int i = 0;
					for (int[] is : loc) {
						colors[i] = fieldContents[is[1]][is[0]].getColour();
						i++;
					}
					
					int c = Block.BLOCK_COLOR_NONE;
					do {
						c = colors[randEngine.nextInt(colors.length)];
					} while (c == Block.BLOCK_COLOR_NONE);
					
					fieldContents[0][x] = new NormalGem(x, 0, c);
				}
			}
		}
	}

	public void resetFallStatus() {
		for (int y = 0; y < fieldHeight; y++) {
			for (int x = 0; x < fieldWidth; x++) {
				fieldContents[y][x].setRecentFall(false);
			}
		}
	}

	public void resetSwapStatus() {
		for (int y = 0; y < fieldHeight; y++) {
			for (int x = 0; x < fieldWidth; x++) {
				fieldContents[y][x].setRecentSwap(false);
			}
		}
	}
	
	public void setCellAttribute(int x, int y, int attribute, boolean state) {
		if (x < 0 || y < 0 || y >= fieldHeight || x >= fieldWidth) {
			switch (attribute) {
			case ATTRIBUTE_RECENTSWAP:
				fieldContents[y][x].setRecentSwap(state);
				break;
			case ATTRIBUTE_RECENTFALL:
				fieldContents[y][x].setRecentFall(state);
				break;
			case ATTRIBUTE_MATCHEDHORIZONTAL:
				fieldContents[y][x].setMatchedHorizontal(state);
				break;
			case ATTRIBUTE_MATCHEDVERTICAL:
				fieldContents[y][x].setMatchedVertical(state);
				break;
			case ATTRIBUTE_ACTIONCONDUCTED:
				fieldContents[y][x].setActionConducted(state);
				break;
			case ATTRIBUTE_DESTROY:
				fieldContents[y][x].setDestroy(state);
				break;
			default:
				break;
			}
		} else {
			return;
		}
		
	}
	
	public boolean getCellAttribute(int x, int y, int attribute) {
		Gem cell = getCell(x, y);
		if (cell != null) {
			switch (attribute) {
			case ATTRIBUTE_RECENTSWAP:
				return cell.getRecentSwap();
			case ATTRIBUTE_RECENTFALL:
				return cell.getRecentFall();
			case ATTRIBUTE_MATCHEDHORIZONTAL:
				return cell.getMatchedHorizontal();
			case ATTRIBUTE_MATCHEDVERTICAL:
				return cell.getMatchedVertical();
			case ATTRIBUTE_SPECIAL:
				return cell.getSpecial();
			case ATTRIBUTE_ACTIONCONDUCTED:
				return cell.getActionConducted();
			case ATTRIBUTE_DESTROY:
				return cell.getDestroy();
			default:
				return false;
			}
		} else {
			return false;
		}
	}
	
	public void destroyGems() {
		for (int y = 0; y < fieldHeight; y++) {
			for (int x = 0; x < fieldWidth; x++) {
				if (fieldContents[y][x].getDestroy()) {
					fieldContents[y][x] = new EmptyGem(x, y);
				}
			}
		}
	}
	
	public boolean dropAllGemsOnce() {
		// SHOULD RETURN true IF A GEM HAS DROPPED. OTHERWISE IT SHOULD BE FALSE.
		// USED A DO-WHILE LOOP.
		boolean dropped = false;

		for (int y = fieldHeight - 1; y > 0; y--) {
			for (int x = 0; x < fieldWidth; x++) {
				if (fieldContents[y][x].getID() == GEMID_EMPTY) {
					fieldContents[y][x] = fieldContents[y - 1][x];
					fieldContents[y][x].setRecentFall(true);
					fieldContents[y - 1][x] = new EmptyGem(x, y - 1);

					dropped = true;
				}
			}
		}
		
		refreshLocations();
		
		return dropped;
	}
	
	public void refreshLocations() {
		for (int y = 0; y < fieldHeight; y++) {
			for (int x = 0; x < fieldWidth; x++) {
				if (fieldContents[y][x] != null) fieldContents[y][x].setLocation(x, y);
			}
		}
	}

	public void checkForLanding(GameEngine engine) {
		for (int y = 0; y < fieldHeight; y++) {
			for (int x = 0; x < fieldWidth; x++) {
				if (getCell(x, y).getRecentFall()) {
					if(getCell(x, y + 1) != null) {
						if (getCell(x, y + 1).getID() != GEMID_EMPTY) engine.playSE("linefall");
					}
				}
			}
		}
	}
	
	public void swapGems(int x1, int y1, int x2, int y2) {
		Gem old = getCell(x1, y1).getSelf();
		
		fieldContents[y1][x1] = getCell(x2, y2).getSelf();
		fieldContents[y2][x2] = old;
		
		getCell(x1, y1).setRecentSwap(true);
		getCell(x2, y2).setRecentSwap(true);
		refreshLocations();
	}
	
	public boolean isMatchExist() {
		for (int y = 0; y < fieldHeight; y++) {
			for (int x = 0; x < fieldWidth; x++) {
				if (getCell(x, y).getMatchedHorizontal() || getCell(x, y).getMatchedVertical()) {
					return true;
				}
			}
		}
		return false;
	}
	
	public Gem[][] getContents() {
		return fieldContents;
	}
	
	public int getWidth() {
		return fieldWidth;
	}
	
	public int getHeight() {
		return fieldHeight;
	}
	
	public long getRandSeed() {
		return randSeed;
	}
	
	public Random getRandom() {
		return randEngine;
	}

	public Gem getCell(int x, int y) {
		if (x < 0 || y < 0 || y >= fieldHeight || x >= fieldWidth) {
			return null;
		} else {
			return fieldContents[y][x];
		}
	}
	
	public void setCell(Gem gem, int x, int y) {
		if (x < 0 || y < 0 || y >= fieldHeight || x >= fieldWidth) {
			fieldContents[y][x] = gem.getSelf();
		} else {
			return;
		}
	}
	
	public void setEmpty(int x, int y) {
		if (x < 0 || y < 0 || y >= fieldHeight || x >= fieldWidth) {
			fieldContents[y][x] = new EmptyGem(x, y);
		} else {
			return;
		}
	}

	public Field getFieldToDraw(GameEngine engine) {
		Field localField = new Field(fieldWidth, fieldHeight, 0);

		for (int y = 0; y < fieldHeight; y++) {
			for (int x = 0; x < fieldWidth; x++) {
				Block blk = new Block((getCell(x, y) != null) ? getCell(x, y).getColour() : Block.BLOCK_COLOR_NONE, engine.getSkin());
				localField.getBlock(x, y).copy(blk);
			}
		}
		
		localField.setAllAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
		localField.setAllAttribute(Block.BLOCK_ATTRIBUTE_OUTLINE, false);
		
		return localField;
	}
	
	public int[][] getFieldHorizontalMatchValues() {
		return fieldHorizontalMatchValues;
	}

	public int[][] getFieldVerticalMatchValues() {
		return fieldVerticalMatchValues;
	}
}
