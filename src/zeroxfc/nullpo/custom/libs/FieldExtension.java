package zeroxfc.nullpo.custom.libs;

import java.util.ArrayList;

import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Field;

public class FieldExtension {
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

	static public void flipVerticalFix(Field field) {
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
	
	static public void delUpperFix(Field field) {
		int rows = ((field.getHeight() - field.getHighestBlockY()) / 2);
		//TODO: Check if this should round up or down.
		int g = field.getHighestBlockY();
		for (int y = 0; y < rows; y++)
			field.delLine(g+y);
	}
}
