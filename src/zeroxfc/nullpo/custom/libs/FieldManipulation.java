/*
 * This library class was created by 0xFC963F18DC21 / Shots243
 * It is part of an extension library for the game NullpoMino (copyright 2010)
 *
 * Herewith shall the term "Library Creator" be given to 0xFC963F18DC21.
 * Herewith shall the term "Game Creator" be given to the original creator of NullpoMino.
 *
 * THIS LIBRARY AND MODE PACK WAS NOT MADE IN ASSOCIATION WITH THE GAME CREATOR.
 *
 * Repository: https://github.com/Shots243/ModePile
 *
 * When using this library in a mode / library pack of your own, the following
 * conditions must be satisfied:
 *     - This license must remain visible at the top of the document, unmodified.
 *     - You are allowed to use this library for any modding purpose.
 *         - If this is the case, the Library Creator must be credited somewhere.
 *             - Source comments only are fine, but in a README is recommended.
 *     - Modification of this library is allowed, but only in the condition that a
 *       pull request is made to merge the changes to the repository.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package zeroxfc.nullpo.custom.libs;

import java.lang.reflect.*;
import java.util.*;

import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import org.apache.log4j.Logger;

public class FieldManipulation {
	/** debug */
	private static final Logger log = Logger.getLogger(FieldManipulation.class);

	/**
	 * Check for split line clears. To be executed in gamemode's onLineClear.<br />
	 * Credit goes to MandL27
	 * @param field Field to check
	 * @return <code>true</code> if line clear is split. <code>false</code> otherwise.
	 */
	public static boolean checkLineForSplit(Field field) {
/*		if (field.lastLinesCleared == null){
 *			field.lastLinesCleared = new ArrayList<Block[]>();
 *		}
 *		field.lastLinesCleared.clear();
 *
 *		boolean previousFlag = false;
 *		boolean atLeastOne = false;
 *
 *		Block[] row = new Block[field.getWidth()];
 *
 *		for(int i = (field.getHiddenHeight() * -1); i < field.getHeightWithoutHurryupFloor(); i++) {
 *			boolean flag = true;
 *
 *			for(int j = 0; j < field.getWidth(); j++) {
 *				row[j] = new Block(field.getBlock(j, i));
 *				if((field.getBlockEmpty(j, i)) || (field.getBlock(j, i).getAttribute(Block.BLOCK_ATTRIBUTE_WALL))) {
 *					flag = false;
 *					break;
 *				}
 *			}
 *
 *			field.setLineFlag(i, flag);
 *
 *			if (flag != previousFlag && atLeastOne && flag) {
 *				return true;
 *			}
 *
 *			if(flag) {
 *				atLeastOne = true;
 *			}
 *
 *			previousFlag = flag;
  		} */
		int regions = 0;
		int lines = 0;

		for (int i = field.getHiddenHeight() * -1; i < field.getHeight(); i++) {
			if (field.getLineFlag(i)) lines++;
			if (lines > 0) regions++;
		}

		return regions > lines;
	}

	/**
	 * Checks if a line needs to be cleared. Does not flag line.
	 * @param field Field to use
	 * @param rownum y-value of row
	 * @return <code>true</code> if line is clearable. <code>false</code> otherwise.
	 */
	public static boolean checkSingleLineNoFlag(Field field, int rownum) {
		if (field.lastLinesCleared == null){
			field.lastLinesCleared = new ArrayList<Block[]>();
		}
		field.lastLinesCleared.clear();

		Block[] row = new Block[field.getWidth()];

		boolean flag = true;

		for(int j = 0; j < field.getWidth(); j++) {
			row[j] = new Block(field.getBlock(j, rownum));
			if((field.getBlockEmpty(j, rownum)) || (field.getBlock(j, rownum).getAttribute(Block.BLOCK_ATTRIBUTE_WALL))) {
				flag = false;
				break;
			}
		}

		field.setLineFlag(rownum, flag);

		return flag;
	}
	
	/**
	 * Line clear check
	 * @return 消えるLinescount
	 */
	public static int eraseFlaggedLine(Field field) {
		int lines = 0;

		if (field.lastLinesCleared == null){
			field.lastLinesCleared = new ArrayList<Block[]>();
		}
		field.lastLinesCleared.clear();

		Block[] row = new Block[field.getWidth()];

		for(int i = (field.getHiddenHeight() * -1); i < field.getHeightWithoutHurryupFloor(); i++) {
			boolean flag = field.getLineFlag(i);

			if(flag) {
				lines++;
				field.lastLinesCleared.add(row);

				for(int j = 0; j < field.getWidth(); j++) {
					field.getBlock(j, i).setAttribute(Block.BLOCK_ATTRIBUTE_ERASE, true);
				}
			}
		}

		return lines;
	}

	/**
	 * Fixed flip field method (180° Field item).
	 * @param field Field to flip
	 */
	public static void flipVerticalFix(Field field) {
		Block temp1, temp2;
		
		Field field2 = new Field();
		field2.copy(field);
		for (int yMin = field.getHighestBlockY() - field.getHiddenHeight(), yMax = (field.getHiddenHeight() + field.getHeight())-1; yMin < yMax; yMin++, yMax--)
		{
			for (int x = 0; x < field.getWidth(); x++) {
				temp1 = field2.getBlock(x, yMin);
				temp2 = field2.getBlock(x, yMax);
				field.setBlock(x, yMin, temp2);
				field.setBlock(x, yMax, temp1);
			}
		}
	}

	/**
	 * Remove an amount of hurry-up lines.
	 * @param lines Amount of lines to remove
	 */
	public static void removeHurryUpLines(Field field, int lines) {
		try {
			// This will complain about an unchecked cast but nothing should go wrong.
			Class<Field> fld = (Class<Field>)field.getClass();
			java.lang.reflect.Field localField;
			localField = fld.getDeclaredField("hurryupFloorLines");
			localField.setAccessible(true);

			// sum = new amount of lines, difference = how much to push down
			int sum = field.getHurryupFloorLines() - lines;
			if (sum < 0) sum = 0;

			int difference = field.getHurryupFloorLines() - sum;

			localField.set(field, sum);  // Put new number in
			field.cutLine(field.getHeight(), difference);  // Push down the field to simulate the removal
		} catch (Exception e) {
			// Do nothing.
		}
	}

	/**
	 * Negates all the blocks in the field up to the current stack height.
	 * @param field Field to negate
	 */
	public static void negaFieldFix(Field field) {
		for (int y = field.getHighestBlockY(); y < field.getHeight(); y++)
			for (int x = 0; x < field.getWidth(); x++)
			{
				if (field.getBlockEmpty(x, y))
					field.garbageDropPlace(x, y, false, 0); // TODO: Set color
				else
					field.setBlockColor(x, y, Block.BLOCK_COLOR_NONE);
			}
	}

	/**
	 * Mirrors the current field. Essentially the same as horizontal flip.
	 * @param field
	 */
	public static void mirrorFix(Field field) {
		Block temp;

		for (int y = field.getHighestBlockY(); y < field.getHeight(); y--)
			for (int xMin = 0, xMax = field.getWidth()-1; xMin < xMax; xMin++, xMax--)
			{
				temp = field.getBlock(xMin, y);
				field.setBlock(xMin, y, field.getBlock(xMax, y));
				field.setBlock(xMax, y, temp);
			}
	}

	/**
	 * Fixed delete upper half of field method.
	 * @param field Field to trim.
	 */
	public static void delUpperFix(Field field) {
		int rows = (int)Math.round((field.getHeight() - field.getHighestBlockY()) / 2d);
		// I think this rounds up.
		int g = field.getHighestBlockY();
		for (int y = 0; y < rows; y++)
			field.delLine(g+y);
	}

	/**
	 * Deletes blocks with any of the colours in the given array.
	 * @param field Field to erase colours from
	 * @param colours Colours to erase
	 * @return int[]; Amount of blocks of each colour erased
	 */
	public static int[] delColours(Field field, int[] colours) {
		int[] results = new int[colours.length];

		for (int i = 0; i < colours.length; i++) {
			results[i] = delColour(field, colours[i]);
		}

		return results;
	}

	/**
	 * Deletes all blocks of a certain colour on a field.
	 * @param field Field to erase colour from
	 * @param colour Colour to erase
	 * @return int; Amount of blocks erased
	 */
	public static int delColour(Field field, int colour) {
		int erased = 0;
		for (int y = (-1 * field.getHiddenHeight()); y < field.getHeight(); y++) {
			for (int x = 0; x < field.getWidth(); x++) {
				int c = field.getBlockColor(x, y);
				if (c == colour) {
					field.getBlock(x, y).color = Block.BLOCK_COLOR_NONE;
					erased++;
				}
			}
		}
		return erased;
	}

	/**
	 * Erases a mino in every filled line on a field.
	 * @param field Field to shoot
	 * @param random Random instance to use
	 */
	public static void shotgunField(Field field, Random random) {
		for (int y = field.getHiddenHeight() * -1; y < field.getHeight(); y++) {
			boolean allEmpty = true;
			ArrayList<Integer> spaces = new ArrayList<>();

			for (int x = 0; x < field.getWidth(); x++) {
				Block blk = field.getBlock(x, y);
				if (blk != null) {
					if (blk.color > Block.BLOCK_COLOR_NONE) {
						allEmpty = false;
						spaces.add(x);
					}
				}
			}

			if (!allEmpty) {
				while (true) {
					int x = spaces.get(random.nextInt(spaces.size()));
					Block blk = field.getBlock(x, y);
					if (blk != null) {
						if (blk.color > Block.BLOCK_COLOR_NONE) {
							blk.color = Block.BLOCK_COLOR_NONE;
							break;
						}
					}
				}
			}
		}
	}

	/**
	 * Erases a mino in every filled line on a field.
	 * @param receiver Renderer to use
	 * @param engine Current GameEngine
	 * @param playerID Current player ID
	 * @param field Field to shoot
	 * @param random Random instance to use
	 */
	public static void shotgunField(EventReceiver receiver, GameEngine engine, int playerID, Field field, Random random) {
		for (int y = field.getHiddenHeight() * -1; y < field.getHeight(); y++) {
			boolean allEmpty = true;
			ArrayList<Integer> spaces = new ArrayList<>();

			for (int x = 0; x < field.getWidth(); x++) {
				Block blk = field.getBlock(x, y);
				if (blk != null) {
					if (blk.color > Block.BLOCK_COLOR_NONE) {
						allEmpty = false;
						spaces.add(x);
					}
				}
			}

			if (!allEmpty) {
				while (true) {
					int x = spaces.get(random.nextInt(spaces.size()));
					Block blk = field.getBlock(x, y);
					if (blk != null) {
						if (blk.color > Block.BLOCK_COLOR_NONE) {
							receiver.blockBreak(engine, playerID, x, y,blk);
							blk.color = Block.BLOCK_COLOR_NONE;
							break;
						}
					}
				}
			}
		}
	}

	/**
	 * <p>Randomises the column order in a field.</p>
	 * Requires a random seed. For consistency, try <code>(engine.randSeed + engine.statistics.time)</code>.
	 * @param field Field to randomise columns
	 * @param seed Random seed
	 */
	public static void shuffleColumns(Field field, long seed) {
		final ArrayRandomiser ar = new ArrayRandomiser(seed);
		shuffleColumns(field, ar);
	}

	/**
	 * <p>Randomises the column order in a field.</p>
	 * Requires a pre-instantiated <code>ArrayRandomiser</code> instance.
	 * @param field Field to randomise columnds
	 * @param arrayRandomiser ArrayRandomiser instance
	 */
	public static void shuffleColumns(Field field, ArrayRandomiser arrayRandomiser) {
		int[] columns = new int[field.getWidth()];
		for (int i = 0; i < columns.length; i++) columns[i] = i;

		columns = arrayRandomiser.permute(columns);

		Field nf = new Field(field);
		for (int i = 0; i < columns.length; i++) {
			for (int y = (-1 * nf.getHiddenHeight()); y < nf.getHeight(); y++) {
				nf.getBlock(i, y).copy(field.getBlock(columns[i], y));
			}
		}

		field.copy(nf);
	}

	/**
	 * <p>Randomises the row order in a field.</p>
	 * Requires a random seed. For consistency, try <code>(engine.randSeed + engine.statistics.time)</code>.
	 * @param field Field to randomise columns
	 * @param seed Random seed
	 * @param highestRow Highest row randomised (0 = top visible), recommended / default is >= 2 (inclusive)
	 * @param lowestRow Lowest row randomised (inclusive)
	 */
	public static void shuffleRows(Field field, long seed, Integer highestRow, Integer lowestRow) {
		final ArrayRandomiser ar = new ArrayRandomiser(seed);
		shuffleRows(field, ar, highestRow, lowestRow);
	}

	/**
	 * <p>Randomises the row order in a field.</p>
	 * Requires a pre-instantiated <code>ArrayRandomiser</code> instance.
	 * @param field Field to randomise rows
	 * @param arrayRandomiser ArrayRandomiser instance
	 * @param highestRow Highest row randomised (0 = top visible), recommended / default is >= 2 (inclusive)
	 * @param lowestRow Lowest row randomised (inclusive)
	 */
	public static void shuffleRows(Field field, ArrayRandomiser arrayRandomiser, Integer highestRow, Integer lowestRow) {
		if (highestRow == null) highestRow = 2;
		if (lowestRow == null) lowestRow = field.getHeight() - 1;
		if (highestRow < (field.getHiddenHeight() * -1) || highestRow >= field.getHiddenHeight()) return;

		int[] rows = new int[lowestRow - highestRow + 1];
		for (int i = 0; i < rows.length; i++) {
			rows[i] = highestRow + i;
		}

		int[] oldRows = rows.clone();
		rows = arrayRandomiser.permute(rows);

		Field nf = new Field(field);

		for (int i = 0; i < rows.length; i++) {
			for (int x = 0; x < nf.getWidth(); x++) {
				nf.getBlock(x, oldRows[i]).copy(field.getBlock(x, rows[i]));
			}
		}

		field.copy(nf);
	}

	/**
	 * Erases a mino in every filled line on a field. Displays the blockbreak effect.
	 * @param receiver EventReceiver instance to display blockbreak effect on.
	 * @param engine GameEngine of field
	 * @param playerID Player ID (1P = 0)
	 * @param random Random instance to use
	 */
	public static void shotgunField(EventReceiver receiver, GameEngine engine, int playerID, Random random) {
		for (int y = engine.field.getHiddenHeight() * -1; y < engine.field.getHeight(); y++) {
			boolean allEmpty = true;
			ArrayList<Integer> spaces = new ArrayList<>();

			for (int x = 0; x < engine.field.getWidth(); x++) {
				Block blk = engine.field.getBlock(x, y);
				if (blk != null) {
					if (blk.color > Block.BLOCK_COLOR_NONE) {
						allEmpty = false;
						spaces.add(x);
					}
				}
			}

			if (!allEmpty) {
				while (true) {
					int x = spaces.get(random.nextInt(spaces.size()));
					Block blk = engine.field.getBlock(x, y);
					if (blk != null) {
						if (blk.color > Block.BLOCK_COLOR_NONE) {
							receiver.blockBreak(engine, playerID, x, y, blk);
							blk.color = Block.BLOCK_COLOR_NONE;
							break;
						}
					}
				}
			}
		}
	}

	/**
	 * Compares two fields and calculates the percentage maps between them. Useful for "build shape modes".
	 * This overload uses non-exact, non-colourmatch matching.
	 * @param a A field
	 * @param b A field to compare field <code>a</code> with.
	 * @return A double that denotes the proportion of match between the fields (0 <= value <= 1).
	 */
	public static double fieldCompare(Field a, Field b) {
		return fieldCompare(a, b, false, false);
	}

	/**
	 * <p>Compares two fields and calculates the percentage maps between them. Useful for "build shape modes".</p>
	 * <p></p>
	 * <p>NOTE: BOTH FIELDS MUST HAVE THE SAME DIMENSIONS IF USING <code>exact</code> MATCHING MODE!</p>
	 * <p>NOTE: If using non-<code>exact</code> matching, <code>Field b</code> is used as the comparator where the percentage reflects how much of <code>a</code> is the same as <code>b</code>.</p>
	 *
	 * @param a A field
	 * @param b A field to compare field <code>a</code> with.
	 * @param exact Exact matching (also takes into account absolute block positions)?
	 * @param colourMatch Exact colour matching (halves match value of non-colour matches)?
	 * @return A double that denotes the proportion of match between the fields (0 <= value <= 1).
	 */
	public static double fieldCompare(Field a, Field b, boolean exact, boolean colourMatch) {
		if (exact) {
			/*
			 * This path attempts to match fields exactly.
			 * This means both fields must have the same dimensions.
			 */
			if (a.getWidth() != b.getWidth()) return 0d;
			if ((a.getHiddenHeight() + a.getHiddenHeight()) != (b.getHiddenHeight() + b.getHiddenHeight())) return 0d;

			final int total = 2 * getNumberOfBlocks(b);
			int current = 0;

			for (int y = (-1 * a.getHiddenHeight()); y < a.getHeight(); y++) {
				for (int x = 0; x < a.getWidth(); x++) {
					Block blkA = a.getBlock(x, y);
					Block blkB = b.getBlock(x, y);

					if (blkA != null && blkB != null) {
						if (colourMatch) {
							if (!blkA.isEmpty() || !blkB.isEmpty()) {
								if (!blkA.isEmpty() && blkB.isEmpty()) {
									current -= 6;
								} else if (!blkA.isEmpty() && !blkB.isEmpty()) {
									if (blkA.color == blkB.color) {
										current += 2;
									} else {
										current += 1;
									}
								}
							}
						} else {
							if (!blkA.isEmpty() || !blkB.isEmpty()) {
								if (!blkA.isEmpty() && blkB.isEmpty()) {
									current -= 6;
								} else if (!blkA.isEmpty() && !blkB.isEmpty()) {
									current += 2;
								}
							}
						}
					}
				}
			}

			double res = (double)current / (double)total;
			if (res < 0) res = 0;
			return res;
		} else {
			double finalResult = 0;

			int areaA = 0, areaB = 0;
			int[] topLeftA, topLeftB;

			// Stage 1: area filled
			for (int y = (-1 * a.getHiddenHeight()); y < a.getHeight(); y++) {
				for (int x = 0; x < a.getWidth(); x++) {
					Block blk = a.getBlock(x, y);
					if (blk.color != Block.BLOCK_COLOR_NONE) areaA++;
				}
			}

			for (int y = (-1 * b.getHiddenHeight()); y < b.getHeight(); y++) {
				for (int x = 0; x < b.getWidth(); x++) {
					Block blk = b.getBlock(x, y);
					if (blk.color != Block.BLOCK_COLOR_NONE) areaB++;
				}
			}

			// Stage 2: blocks
			final int[][] resA = getOpposingCornerCoords(a);
			topLeftA = resA[0];
			// bottomRightA = resA[1];

			final int[][] resB = getOpposingCornerCoords(b);
			topLeftB = resB[0];
			// bottomRightB = resB[1];

			final Integer[] bboxSizeA = getOpposingCornerBoxSize(a), bboxSizeB = getOpposingCornerBoxSize(b);

			if (bboxSizeA[0] != null && bboxSizeA[1] != null && bboxSizeB[0] != null && bboxSizeB[1] != null) {
				// log.debug(String.format("%d %d | %d %d", bboxSizeA[0], bboxSizeA[1], bboxSizeB[0], bboxSizeB[1]));

				final int aA = bboxSizeA[0] * bboxSizeA[1], aB = bboxSizeB[0] * bboxSizeB[1];
				int total = 0;
				// int excess = 0;

				if (bboxSizeA[0].equals(bboxSizeB[0]) && bboxSizeA[1].equals(bboxSizeB[1])) {
					for (int y = 0; y < bboxSizeB[1]; y++) {
						for (int x = 0; x < bboxSizeB[0]; x++) {
							Block blkA = a.getBlock(topLeftA[0] + x, topLeftA[1] + y);
							Block blkB = b.getBlock(topLeftB[0] + x, topLeftB[1] + y);

							if (blkA != null && blkB != null) {
								if (colourMatch) {
									if (blkA.isEmpty() && blkB.isEmpty()) {
										total += 2;
									} else {
										if (!blkA.isEmpty() && blkB.isEmpty()) {
											total -= 6;
										} else if (!blkA.isEmpty() && !blkB.isEmpty()) {
											if (blkA.color == blkB.color) {
												total += 2;
											} else {
												total += 1;
											}
										}
									}
								} else {
									if (blkA.isEmpty() && blkB.isEmpty()) {
										total += 2;
										// log.debug("(" + x + ", " + y + ") " + "EMPTY MATCH");
									} else {
										if (!blkA.isEmpty() && blkB.isEmpty()) {
											total -= 6;
											// log.debug("(" + x + ", " + y + ") " + "EXCESS IN A");
										} else if (!blkA.isEmpty() && !blkB.isEmpty()) {
											total += 2;
											// log.debug("(" + x + ", " + y + ") " + "FULL MATCH");
										}//  else {
											//total -= 1;
											// log.debug("(" + x + ", " + y + ") " + "UNFILLED");
										// }
									}
								}
							}
						}
					}

					double res3 = (double)total / (double)(2 * aB);
					if (res3 < 0) res3 = 0;

					// log.debug(String.format("TOTAL: %d, MAX: %d, PERCENT: %.2f", total, 2 * aB, res3 * 100));
					return res3;
				} else {
					// Use the lowest common multiple + nearest neighbour scaling check method.
					final int lcmWidth = lcm(bboxSizeA[0], bboxSizeB[0]), lcmHeight = lcm(bboxSizeA[1], bboxSizeB[1]);

					final int multiplierWidthA = lcmWidth / bboxSizeA[0];
					final int multiplierWidthB = lcmWidth / bboxSizeB[0];
					final int multiplierHeightA = lcmHeight / bboxSizeA[1];
					final int multiplierHeightB = lcmHeight / bboxSizeB[1];

					final int maxArea = lcmHeight * lcmWidth * 2;

					double closenessAverageH = ((double)bboxSizeA[0] / bboxSizeB[0]);
					if (closenessAverageH > 1) closenessAverageH = 1 - (closenessAverageH - 1);
					if (closenessAverageH < 0) closenessAverageH = 0;

					double closenessAverageV = ((double)bboxSizeA[1] / bboxSizeB[1]);
					if (closenessAverageV > 1) closenessAverageV = 1 - (closenessAverageV - 1);
					if (closenessAverageV < 0) closenessAverageV = 0;

					final double closenessAverage = ((closenessAverageH + closenessAverageV) / 2);

					//StringBuilder matchArr = new StringBuilder("MATCH ARRAY:\n");

					for (int y = 0; y < lcmHeight; y++) {
						for (int x = 0; x < lcmWidth; x++) {
							int v1 = a.getBlock(topLeftA[0] + (x / multiplierWidthA), topLeftA[1] + (y / multiplierHeightA)).color;
							int v2 = b.getBlock(topLeftB[0] + (x / multiplierWidthB), topLeftB[1] + (y / multiplierHeightB)).color;

							if (colourMatch) {
								if (v1 <= 0 && v2 <= 0) {
									total += 2;
									//matchArr.append(" 2");
								} else {
									if (v1 > 0 && v2 <= 0) {
										total -= 6;
										//matchArr.append("-8");
									} else if (v1 > 0 && v2 > 0) {
										if (v1 == v2) {
											total += 2;
											//matchArr.append(" 2");
										} else {
											total += 1;
											//matchArr.append(" 1");
										}
									} // else {
										// total -= 1;
										//matchArr.append("-4");
									// }
								}
							} else {
								if (v1 <= 0 && v2 <= 0) {
									total += 2;
									//matchArr.append(" 2");
								} else {
									if (v1 > 0 && v2 <= 0) {
										total -= 6;
										//matchArr.append("-8");
									} else if (v1 > 0 && v2 > 0) {
										total += 2;
										//matchArr.append(" 2");
									} // else {
										// total -= 1;
										//matchArr.append("-4");
									// }
								}
							}
						}
						//matchArr.append("\n");
					}

//					StringBuilder j = new StringBuilder("FIELD A:\n");
//					for (int y = 0; y < lcmHeight; y++) {
//						for (int x = 0; x < lcmWidth; x++) {
//							j.append(a.getBlock(topLeftA[0] + (x / multiplierWidthA), topLeftA[1] + (y / multiplierHeightA)).color);
//						}
//						j.append("\n");
//					}
//
//					StringBuilder k = new StringBuilder("FIELD B:\n");
//					for (int y = 0; y < lcmHeight; y++) {
//						for (int x = 0; x < lcmWidth; x++) {
//							k.append(b.getBlock(topLeftB[0] + (x / multiplierWidthB), topLeftB[1] + (y / multiplierHeightB)).color);
//						}
//						k.append("\n");
//					}
//
//					log.debug(j.toString());
//					log.debug(k.toString());
//					log.debug(matchArr.toString());

					double res3 = ((double)total / (double)maxArea) * closenessAverage;
					if (res3 < 0) res3 = 0;

					// log.debug(String.format("TOTAL: %d, MAX: %d, CLOSENESS: %.2f, PERCENT: %.2f", total, maxArea, closenessAverage, res3 * 100));
					return res3;
				}
			}

			return finalResult;
		}
	}

	/*
	 * NOTE: Here's hoping this doesn't slow the game down by much.
	 *
	 * Euler's method?
	 */

	// Recursive method to return gcd of a and b
	private static int gcd(int a, int b) {
		if (a == 0) return b;
		else return gcd(b % a, a);
	}

	// Method to return LCM of two numbers
	private static int lcm(int a, int b) {
		return (a * b) / gcd(a, b);
	}

	/**
	 * Gets the full height of a field, including hidden height.
	 * @param field Field to get full height of
	 * @return int; Full height
	 */
	public static int getFullHeight(Field field) {
		return field.getHiddenHeight() + field.getHeight();
	}

	/**
	 * Gets the number of non-empty blocks inside the field.
	 * @param field Field to check
	 * @return Number of blocks inside (including in hidden height)
	 */
	public static int getNumberOfBlocks(Field field) {
		int result = 0;
		for (int y = (-1 * field.getHiddenHeight()); y < field.getHeight(); y++) {
			for (int x = 0; x < field.getWidth(); x++) {
				Block blk = field.getBlock(x, y);
				if (!blk.isEmpty()) result++;
			}
		}
		return result;
	}

	/**
	 * Gets the number of empty blocks inside the field.
	 * @param field Field to check
	 * @return Number of empty spaces inside (including in hidden height)
	 */
	public static int getNumberOfEmptySpaces(Field field) {
		int result = 0;
		for (int y = (-1 * field.getHiddenHeight()); y < field.getHeight(); y++) {
			for (int x = 0; x < field.getWidth(); x++) {
				Block blk = field.getBlock(x, y);
				if (blk.isEmpty()) result++;
			}
		}
		return result;
	}

	/**
	 * Gets the coordinates of the top-left and the bottom-right of the smallest bounding square that covers all blocks in the field.
	 * @param field Field to check
	 * @return int[2][2] result: result[0] = top left, result[1] = bottom right. result[i][0] = x, result[i][1] = y.
	 */
	public static int[][] getOpposingCornerCoords(Field field) {
		int[] topLeft = new int[2];
		int[] bottomRight = new int[2];

		topLeft[0] = getLeftmostColumn(field);
		topLeft[1] = field.getHighestBlockY();
		bottomRight[0] = getRightmostColumn(field);
		bottomRight[1] = getBottommostRow(field);

		return new int[][] { topLeft, bottomRight };
	}

	/**
	 * Gets the size of the smallest bounding box that covers all blocks in the field.
	 * @param field Field to check
	 * @return int[] results: results[0] = x, results[1] = y.
	 */
	public static Integer[] getOpposingCornerBoxSize(Field field) {
		int[][] bbox = getOpposingCornerCoords(field);
		Integer i = bbox[1][0] - bbox[0][0] + 1;
		Integer j = bbox[1][1] - bbox[0][1] + 1;

		if (i <= 0) i = null;
		if (j <= 0) j = null;
		return new Integer[] { i, j };
	}

	/**
	 * Gets the x coordinate of the left-most filled column a field.
	 * @param field Field to check
	 * @return int; x coordinate
	 */
	public static int getLeftmostColumn(Field field) {
		for (int x = 0; x < field.getWidth(); x++) {
			for (int y = (-1 * field.getHiddenHeight()); y < field.getHeight(); y++) {
				Block blk = field.getBlock(x, y);
				if (blk != null) {
					if (!blk.isEmpty()) return x;
				}
			}
		}
		return field.getWidth() - 1;
	}

	/**
	 * Gets the x coordinate of the right-most filled column a field.
	 * @param field Field to check
	 * @return int; x coordinate
	 */
	public static int getRightmostColumn(Field field) {
		for (int x = field.getWidth() - 1; x >= 0; x--) {
			for (int y = (-1 * field.getHiddenHeight()); y < field.getHeight(); y++) {
				Block blk = field.getBlock(x, y);
				if (blk != null) {
					if (!blk.isEmpty()) return x;
				}
			}
		}
		return 0;
	}

	/**
	 * Gets the y coordinate of the bottom-most filled row in a field.
	 * @param field Field to check
	 * @return int; y coordinate
	 */
	public static int getBottommostRow(Field field) {
		for (int y = field.getHeight() - 1; y >= (-1 * field.getHiddenHeight()); y--) {
			for (int x = 0; x < field.getWidth(); x++) {
				Block blk = field.getBlock(x, y);
				if (blk != null) {
					if (!blk.isEmpty()) return y;
				}
			}
		}
		return field.getHiddenHeight() * -1;
	}
}
