package zeroxfc.nullpo.custom.modes;

import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Field;

import java.util.ArrayList;

public class DrawMode extends MarathonModeBase {
	/** This mode always has 0G gravity */
	private static final int GRAVITY_CONSTANT = 0;

	/**
	 * Time limit for placements per level:
	 * Score starts decreasing when time is expired at a rate of
	 * 8 * (level + 1) / frame until next placement
	 */
	private static final int[] tablePieceTimeLimit = { 1800,  1680,  1620,  1560,  1500,  1440,  1380,  1320,  1260,  1200,  1140,  1080,  1020, 960, 900, 840, 780,  720,  660,  600 };

	/** Line counts when game ending occurs */
	private static final int[] tableGameClearLines = { 50, 100, 150, 200, -1 };

	/** Number of entries in rankings */
	private static final int RANKING_MAX = 10;

	/** Number of ranking types */
	private static final int RANKING_TYPE = 5;

	/** Number of game types */
	private static final int GAMETYPE_MAX = 5;

	/** Maximum drawable squares in draw mode */
	private static final int MAX_SQUARES = 4;

	// region SHAPES
	// Aww s#/t! Here we go again...

	private static final int[][][] SHAPES = {
			//region Shape Outlines
			new int[][] {
					new int[] { 1, 1, 1, 1 }
			},
			new int[][] {
					new int[] { 1 },
					new int[] { 1 },
					new int[] { 1 },
					new int[] { 1 }
			},  // I-PIECE
			new int[][] {
					new int[] { 0, 0, 1 },
					new int[] { 1, 1, 1 },
			},
			new int[][] {
					new int[] { 1, 0 },
					new int[] { 1, 0 },
					new int[] { 1, 1 }
			},
			new int[][] {
					new int[] { 1, 1, 1 },
					new int[] { 1, 0, 0 },
			},
			new int[][] {
					new int[] { 1, 1 },
					new int[] { 0, 1 },
					new int[] { 0, 1 }
			},  // L-PIECE
			new int[][] {
					new int[] { 1, 1 },
					new int[] { 1, 1 }
			},  // O-PIECE
			new int[][] {
					new int[] { 1, 1, 0 },
					new int[] { 0, 1, 1 },
			},
			new int[][] {
					new int[] { 0, 1 },
					new int[] { 1, 1 },
					new int[] { 1, 0 }
			},  // Z-PIECE
			new int[][] {
					new int[] { 0, 1, 0 },
					new int[] { 1, 1, 1 },
			},
			new int[][] {
					new int[] { 1, 0 },
					new int[] { 1, 1 },
					new int[] { 1, 0 }
			},
			new int[][] {
					new int[] { 1, 1, 1 },
					new int[] { 0, 1, 0 },
			},
			new int[][] {
					new int[] { 0, 1 },
					new int[] { 1, 1 },
					new int[] { 0, 1 }
			},  // T-PIECE
			new int[][] {
					new int[] { 1, 0, 0 },
					new int[] { 1, 1, 1 },
			},
			new int[][] {
					new int[] { 1, 1 },
					new int[] { 1, 0 },
					new int[] { 1, 0 }
			},
			new int[][] {
					new int[] { 1, 1, 1 },
					new int[] { 0, 0, 1 },
			},
			new int[][] {
					new int[] { 0, 1 },
					new int[] { 0, 1 },
					new int[] { 1, 1 }
			},  // J-PIECE
			new int[][] {
					new int[] { 0, 1, 1 },
					new int[] { 1, 1, 0 },
			},
			new int[][] {
					new int[] { 1, 0 },
					new int[] { 1, 1 },
					new int[] { 0, 1 }
			}   // S-PIECE
			//endregion
	};

	private static final ArrayList<Field> SHAPE_FIELDS = new ArrayList<>();

	private static final int[] SHAPE_TO_PIECE_ID = {
			0, 0, 1, 1, 1, 1, 2, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 6, 6
	};

	private static final int[] SHAPE_TO_PIECE_DIRECTION = {
			0, 1, 0, 1, 2, 3, 0, 2, 1, 0, 1, 2, 3, 0, 1, 2, 3, 2, 3
	};

	static {
		for (int i = 0; i < SHAPES.length; i++) {
			int[][] shape = SHAPES[i];
			int dimX = shape[0].length;
			int dimY = shape.length;

			SHAPE_FIELDS.add(new Field(dimX, dimY, 0));

			for (int y = 0; y < dimY; y++) {
				for (int x = 0; x < dimX; x++) {
					SHAPE_FIELDS.get(i).getBlock(x, y).copy(new Block(shape[y][x], 0));
				}
			}
		}
	}

	// endregion SHAPES

	/** The match confidence of the field to each piece. */
	private ArrayList<Double> matchConfidences;

	/** Mode Name */
	@Override
	public String getName() {
		return "DRAW MODE";
	}
}
