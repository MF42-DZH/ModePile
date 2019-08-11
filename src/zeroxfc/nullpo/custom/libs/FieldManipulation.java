package zeroxfc.nullpo.custom.libs;

import java.util.ArrayList;
import java.util.Random;

import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;

public class FieldManipulation {
	/**
	 * Check for split line clears. To be executed in gamemode's onLineClear.
	 * @param field Field to check
	 * @return <code>true</code> if line clear is split. <code>false</code> otherwise.
	 */
	public static boolean checkLineForSplit(Field field) {
		if (field.lastLinesCleared == null){
			field.lastLinesCleared = new ArrayList<Block[]>();
		}
		field.lastLinesCleared.clear();
		
		boolean previousFlag = false;
		boolean atLeastOne = false;

		Block[] row = new Block[field.getWidth()];

		for(int i = (field.getHiddenHeight() * -1); i < field.getHeightWithoutHurryupFloor(); i++) {
			boolean flag = true;

			for(int j = 0; j < field.getWidth(); j++) {
				row[j] = new Block(field.getBlock(j, i));
				if((field.getBlockEmpty(j, i) == true) || (field.getBlock(j, i).getAttribute(Block.BLOCK_ATTRIBUTE_WALL) == true)) {
					flag = false;
					break;
				}
			}

			field.setLineFlag(i, flag);
			
			if (flag != previousFlag && atLeastOne && flag) {
				return true;
			}
			
			if(flag) {
				atLeastOne = true;
			}
			
			previousFlag = flag;
		}

		return false;
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
			if((field.getBlockEmpty(j, rownum) == true) || (field.getBlock(j, rownum).getAttribute(Block.BLOCK_ATTRIBUTE_WALL) == true)) {
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
	 * Fixed delete upper half of field method.
	 * @param field Field to trim.
	 */
	public static void delUpperFix(Field field) {
		int rows = ((field.getHeight() - field.getHighestBlockY()) / 2);
		//TODO: Check if this should round up or down.
		int g = field.getHighestBlockY();
		for (int y = 0; y < rows; y++)
			field.delLine(g+y);
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
}
