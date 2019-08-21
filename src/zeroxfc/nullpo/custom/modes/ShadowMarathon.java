package zeroxfc.nullpo.custom.modes;

import mu.nu.nullpo.game.component.*;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.util.CustomProperties;
import mu.nu.nullpo.util.GeneralUtil;
import org.apache.log4j.Logger;
import zeroxfc.nullpo.custom.libs.FieldManipulation;
import zeroxfc.nullpo.custom.libs.GameTextUtilities;
import zeroxfc.nullpo.custom.libs.Interpolation;
import zeroxfc.nullpo.custom.libs.RendererExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class ShadowMarathon extends MarathonModeBase {
	/** debug */
	private static final Logger log = Logger.getLogger(ShadowMarathon.class);

	/** Line counts when game ending occurs */
	private static final int[] tableGameClearLines = { 1, 2, 3, 4, 5, 10, 25, 50, 100, 150, 200, 999, -1 };

	/** Number of ranking types */
	private static final int RANKING_TYPE = 13;

	/** Number of game types */
	private static final int GAMETYPE_MAX = 13;

	/** Piece names for printing */
	private static final String[] SHAPE_NAMES = {
			"I-PIECE",
			"L-PIECE",
			"O-PIECE",
			"Z-PIECE",
			"T-PIECE",
			"J-PIECE",
			"S-PIECE"
	};

	private static final int[] PIECE_COLOURS = {
			EventReceiver.COLOR_CYAN,
			EventReceiver.COLOR_ORANGE,
			EventReceiver.COLOR_YELLOW,
			EventReceiver.COLOR_RED,
			EventReceiver.COLOR_PURPLE,
			EventReceiver.COLOR_DARKBLUE,
			EventReceiver.COLOR_GREEN
	};

	// region Shapes

	// TODO: Fields of different tetromino shapes.
	private static final int[][][] SHAPES = {
			// region 4x

			new int[][] {
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }
			},
			new int[][] {
					new int[] { 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1 }
			},  // 4x I-PIECE
			new int[][] {
					new int[] { 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 }
			},
			new int[][] {
					new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }
			},
			new int[][] {
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 }
			},
			new int[][] {
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1 }
			},  // 4x L-PIECE
			new int[][] {
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 }
			},  // 4x O-PIECE
			new int[][] {
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1 }
			},
			new int[][] {
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 0, 0, 0, 0 }
			},  // 4x Z-PIECE
			new int[][] {
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0 }
			},
			new int[][] {
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }
			},
			new int[][] {
					new int[] { 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 0, 0, 0, 0 }
			},
			new int[][] {
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1 }
			},  // 4x T-PIECE
			new int[][] {
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1 }
			},
			new int[][] {
					new int[] { 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }
			},
			new int[][] {
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 0, 0, 0, 0 }
			},
			new int[][] {
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 }
			},  // 4x J-PIECE
			new int[][] {
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0 }
			},
			new int[][] {
					new int[] { 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 1, 1, 1, 1 }
			},  // 4x S-PIECE

			// endregion 4x

			// region 3x

			new int[][] {
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }
			},
			new int[][] {
					new int[] { 1, 1, 1 },
					new int[] { 1, 1, 1 },
					new int[] { 1, 1, 1 },
					new int[] { 1, 1, 1 },
					new int[] { 1, 1, 1 },
					new int[] { 1, 1, 1 },
					new int[] { 1, 1, 1 },
					new int[] { 1, 1, 1 },
					new int[] { 1, 1, 1 },
					new int[] { 1, 1, 1 },
					new int[] { 1, 1, 1 },
					new int[] { 1, 1, 1 }
			},  // 3x I-PIECE
			new int[][] {
					new int[] { 1, 1, 1, 0, 0, 0 },
					new int[] { 1, 1, 1, 0, 0, 0 },
					new int[] { 1, 1, 1, 0, 0, 0 },
					new int[] { 1, 1, 1, 0, 0, 0 },
					new int[] { 1, 1, 1, 0, 0, 0 },
					new int[] { 1, 1, 1, 0, 0, 0 },
					new int[] { 1, 1, 1, 0, 0, 0 },
					new int[] { 1, 1, 1, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1 }
			},
			new int[][] {
					new int[] { 0, 0, 0, 0, 0, 0, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 0, 0, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 0, 0, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1 }
			},
			new int[][] {
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 0, 0, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 0, 0, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 0, 0, 0, 0, 0, 0 }
			},
			new int[][] {
					new int[] { 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 1, 1, 1 },
					new int[] { 0, 0, 0, 1, 1, 1 },
					new int[] { 0, 0, 0, 1, 1, 1 },
					new int[] { 0, 0, 0, 1, 1, 1 },
					new int[] { 0, 0, 0, 1, 1, 1 },
					new int[] { 0, 0, 0, 1, 1, 1 }
			},  // 3x L-PIECE
			new int[][] {
					new int[] { 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1 }
			},  // 3x O-PIECE
			new int[][] {
					new int[] { 1, 1, 1, 1, 1, 1, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 1, 1, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 1, 1, 0, 0, 0 },
					new int[] { 0, 0, 0, 1, 1, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 1, 1, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 1, 1, 1, 1, 1, 1 },
			},
			new int[][] {
					new int[] { 0, 0, 0, 1, 1, 1 },
					new int[] { 0, 0, 0, 1, 1, 1 },
					new int[] { 0, 0, 0, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 0, 0, 0 },
					new int[] { 1, 1, 1, 0, 0, 0 },
					new int[] { 1, 1, 1, 0, 0, 0 }
			},  // 3x Z-PIECE
			new int[][] {
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 1, 1, 1, 0, 0, 0 },
					new int[] { 0, 0, 0, 1, 1, 1, 0, 0, 0 },
					new int[] { 0, 0, 0, 1, 1, 1, 0, 0, 0 },
			},
			new int[][] {
					new int[] { 0, 0, 0, 1, 1, 1, 0, 0, 0 },
					new int[] { 0, 0, 0, 1, 1, 1, 0, 0, 0 },
					new int[] { 0, 0, 0, 1, 1, 1, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1 }
			},
			new int[][] {
					new int[] { 1, 1, 1, 0, 0, 0 },
					new int[] { 1, 1, 1, 0, 0, 0 },
					new int[] { 1, 1, 1, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 0, 0, 0 },
					new int[] { 1, 1, 1, 0, 0, 0 },
					new int[] { 1, 1, 1, 0, 0, 0 }
			},
			new int[][] {
					new int[] { 0, 0, 0, 1, 1, 1 },
					new int[] { 0, 0, 0, 1, 1, 1 },
					new int[] { 0, 0, 0, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 1, 1, 1 },
					new int[] { 0, 0, 0, 1, 1, 1 },
					new int[] { 0, 0, 0, 1, 1, 1 }
			},  // 3x T-PIECE
			new int[][] {
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 0, 0, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 0, 0, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 0, 0, 1, 1, 1 }
			},
			new int[][] {
					new int[] { 1, 1, 1, 0, 0, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 0, 0, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 0, 0, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1 }
			},
			new int[][] {
					new int[] { 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 0, 0, 0 },
					new int[] { 1, 1, 1, 0, 0, 0 },
					new int[] { 1, 1, 1, 0, 0, 0 },
					new int[] { 1, 1, 1, 0, 0, 0 },
					new int[] { 1, 1, 1, 0, 0, 0 },
					new int[] { 1, 1, 1, 0, 0, 0 }
			},
			new int[][] {
					new int[] { 0, 0, 0, 1, 1, 1 },
					new int[] { 0, 0, 0, 1, 1, 1 },
					new int[] { 0, 0, 0, 1, 1, 1 },
					new int[] { 0, 0, 0, 1, 1, 1 },
					new int[] { 0, 0, 0, 1, 1, 1 },
					new int[] { 0, 0, 0, 1, 1, 1 },
					new int[] { 0, 0, 0, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1 }
			},  // 3x J-PIECE
			new int[][] {
					new int[] { 0, 0, 0, 1, 1, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 1, 1, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 1, 1, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 1, 1, 0, 0, 0 }
			},
			new int[][] {
					new int[] { 1, 1, 1, 0, 0, 0 },
					new int[] { 1, 1, 1, 0, 0, 0 },
					new int[] { 1, 1, 1, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 1, 1, 1 },
					new int[] { 0, 0, 0, 1, 1, 1 },
					new int[] { 0, 0, 0, 1, 1, 1 }
			},   // 3x S-PIECE

			// endregion 3x

			// region 2x
			new int[][] {
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 }
			},
			new int[][] {
					new int[] { 1, 1 },
					new int[] { 1, 1 },
					new int[] { 1, 1 },
					new int[] { 1, 1 },
					new int[] { 1, 1 },
					new int[] { 1, 1 },
					new int[] { 1, 1 },
					new int[] { 1, 1 },
			},  // I-PIECE
			new int[][] {
					new int[] { 1, 1, 0, 0, 0, 0 },
					new int[] { 1, 1, 0, 0, 0, 0 },
					new int[] { 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1 }
			},
			new int[][] {
					new int[] { 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1 },
					new int[] { 1, 1, 0, 0 },
					new int[] { 1, 1, 0, 0 },
					new int[] { 1, 1, 0, 0 },
					new int[] { 1, 1, 0, 0 }
			},
			new int[][] {
					new int[] { 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1 },
					new int[] { 0, 0, 0, 0, 1, 1 },
					new int[] { 0, 0, 0, 0, 1, 1 }
			},
			new int[][] {
					new int[] { 0, 0, 1, 1 },
					new int[] { 0, 0, 1, 1 },
					new int[] { 0, 0, 1, 1 },
					new int[] { 0, 0, 1, 1 },
					new int[] { 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1 }
			},  // J-PIECE
			new int[][] {
					new int[] { 0, 0, 0, 0, 1, 1 },
					new int[] { 0, 0, 0, 0, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1 }
			},
			new int[][] {
					new int[] { 1, 1, 0, 0 },
					new int[] { 1, 1, 0, 0 },
					new int[] { 1, 1, 0, 0 },
					new int[] { 1, 1, 0, 0 },
					new int[] { 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1 }
			},
			new int[][] {
					new int[] { 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 0, 0, 0, 0 },
					new int[] { 1, 1, 0, 0, 0, 0 }
			},
			new int[][] {
					new int[] { 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1 },
					new int[] { 0, 0, 1, 1 },
					new int[] { 0, 0, 1, 1 },
					new int[] { 0, 0, 1, 1 },
					new int[] { 0, 0, 1, 1 }
			},  // L-PIECE
			new int[][] {
					new int[] { 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1 }
			},  // O-PIECE
			new int[][] {
					new int[] { 0, 0, 1, 1, 1, 1 },
					new int[] { 0, 0, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 0, 0 },
					new int[] { 1, 1, 1, 1, 0, 0 }
			},
			new int[][] {
					new int[] { 1, 1, 0, 0 },
					new int[] { 1, 1, 0, 0 },
					new int[] { 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1 },
					new int[] { 0, 0, 1, 1 },
					new int[] { 0, 0, 1, 1 }
			},  // S-PIECE
			new int[][] {
					new int[] { 0, 0, 1, 1, 0, 0 },
					new int[] { 0, 0, 1, 1, 0, 0 },
					new int[] { 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1 }
			},
			new int[][] {
					new int[] { 1, 1, 0, 0 },
					new int[] { 1, 1, 0, 0 },
					new int[] { 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1 },
					new int[] { 1, 1, 0, 0 },
					new int[] { 1, 1, 0, 0 }
			},
			new int[][] {
					new int[] { 1, 1, 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1, 1, 1 },
					new int[] { 0, 0, 1, 1, 0, 0 },
					new int[] { 0, 0, 1, 1, 0, 0 }
			},
			new int[][] {
					new int[] { 0, 0, 1, 1 },
					new int[] { 0, 0, 1, 1 },
					new int[] { 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1 },
					new int[] { 0, 0, 1, 1 },
					new int[] { 0, 0, 1, 1 }
			},  // T-PIECE
			new int[][] {
					new int[] { 1, 1, 1, 1, 0, 0 },
					new int[] { 1, 1, 1, 1, 0, 0 },
					new int[] { 0, 0, 1, 1, 1, 1 },
					new int[] { 0, 0, 1, 1, 1, 1 }
			},
			new int[][] {
					new int[] { 0, 0, 1, 1 },
					new int[] { 0, 0, 1, 1 },
					new int[] { 1, 1, 1, 1 },
					new int[] { 1, 1, 1, 1 },
					new int[] { 1, 1, 0, 0 },
					new int[] { 1, 1, 0, 0 }
			}   // Z-PIECE

			// endregion 2x
	};

	private static final ArrayList<Field> SHAPE_FIELDS = new ArrayList<>();
	private static final int[] SHAPE_TO_PIECE_ID = {
			0, 0, 1, 1, 1, 1, 2, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 6, 6,
			0, 0, 1, 1, 1, 1, 2, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 6, 6,
			0, 0, 5, 5, 5, 5, 1, 1, 1, 1, 2, 6, 6, 4, 4, 4, 4, 3, 3
	};
	private static final int[] SHAPE_TO_MULTIPLIER = {
			27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27,
			1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
			8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8
	};

	// Static initialiser for shape fields.
	// NOTE: in-game during calcScore, if the shadow field is being used, calculate the match % for all fields then specify the most likely.
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

	// endregion Shapes

	/**
	 * Shadow Field = field for building pieces in.
	 * Main Field = field that main marathon game happens in.
	 */
	private Field shadowField, mainField;

	/** Is the current field the shadow field or the main field? (Used for disabling/enabling scoring). */
	private boolean onShadow;

	/** The match confidence of the field to each piece. */
	private ArrayList<Double> matchConfidences;

	/** Current piece set given by rule. */
	private ArrayList<Piece> pieceSet;
	private ArrayList<Integer> pieceDirections;
	private ArrayList<Boolean> pieceRandomDirection;

	/**
	 * Should the game increment next pieces and override the current piece?
	 * -- NOTE: OVERRIDE onMove() --
	 */
	private boolean capLines;

	/**
	 * Pattern matches give different scores on closeness.
	 * The following formula will be used:
	 *
	 * multiplierScore = matchConfidence / 0.9
	 */
	private double multiplierScore;

	/**
	 * The history of previously-built pieces. 4-long.
	 * If a piece was in here when a new one was built, reduce score multiplier by 50% and reduce all line clears down to 1.
	 * Oh, and write these down too on the screen so the player knows which pieces they've done.
	 */
	private ArrayList<Integer> lastIDs;
	private ArrayList<Double> lastMatchPercentages;

	private Piece fallPieceDraw;
	private double[] fallPieceLoc;
	private double[] fallPieceVel;

	private static final double V_MAX = 16;
	private static final double GRAVITY = 9.80665 / 10d;

	private int currentMaxIndex;
	private int currentPieceID;
	private double currentMaxMatchValue;
	private Random directionRandom;
	private int lerpTime, lastScore;

	@Override
	public String getName() {
		return "SHADOW MARATHON";
	}

	@Override
	public void playerInit(GameEngine engine, int playerID) {
		owner = engine.owner;
		receiver = engine.owner.receiver;
		lastscore = 0;
		scgettime = 0;
		lerpTime = 0;
		lastScore = 0;
		lastevent = EVENT_NONE;
		lastb2b = false;
		lastcombo = 0;
		lastpiece = 0;
		bgmlv = 0;

		matchConfidences = new ArrayList<>();
		lastIDs = new ArrayList<>();
		lastMatchPercentages = new ArrayList<>();
		
		pieceDirections = new ArrayList<>();
		pieceRandomDirection = new ArrayList<>();
		
		pieceSet = new ArrayList<>();

		rankingRank = -1;
		rankingScore = new int[RANKING_TYPE][RANKING_MAX];
		rankingLines = new int[RANKING_TYPE][RANKING_MAX];
		rankingTime = new int[RANKING_TYPE][RANKING_MAX];

		netPlayerInit(engine, playerID);

		if(!owner.replayMode) {
			loadSetting(owner.modeConfig);
			loadRanking(owner.modeConfig, engine.ruleopt.strRuleName);
			version = CURRENT_VERSION;
		} else {
			loadSetting(owner.replayProp);
			if((version == 0) && owner.replayProp.getProperty("shadowMarathon.endless", false)) goaltype = 2;

			// NET: Load name
			netPlayerName = engine.owner.replayProp.getProperty(playerID + ".net.netPlayerName", "");
		}

		engine.owner.backgroundStatus.bg = startlevel;
		engine.framecolor = GameEngine.FRAME_COLOR_GREEN;
	}

	/*
	 * Called at settings screen
	 */
	@Override
	public boolean onSetting(GameEngine engine, int playerID) {
		// NET: Net Ranking
		if(netIsNetRankingDisplayMode) {
			netOnUpdateNetPlayRanking(engine, goaltype);
		}
		// Menu
		else if(engine.owner.replayMode == false) {
			// Configuration changes
			int change = updateCursor(engine, 8, playerID);

			if(change != 0) {
				engine.playSE("change");

				switch(engine.statc[2]) {
					case 0:
						startlevel += change;
						if(tableGameClearLines[goaltype] >= 0) {
							if(startlevel < 0) startlevel = (tableGameClearLines[goaltype] - 1) / 10;
							if(startlevel > (tableGameClearLines[goaltype] - 1) / 10) startlevel = 0;
						} else {
							if(startlevel < 0) startlevel = 19;
							if(startlevel > 19) startlevel = 0;
						}
						engine.owner.backgroundStatus.bg = startlevel;
						break;
					case 1:
						//enableTSpin = !enableTSpin;
						tspinEnableType += change;
						if(tspinEnableType < 0) tspinEnableType = 2;
						if(tspinEnableType > 2) tspinEnableType = 0;
						break;
					case 2:
						enableTSpinKick = !enableTSpinKick;
						break;
					case 3:
						spinCheckType += change;
						if(spinCheckType < 0) spinCheckType = 1;
						if(spinCheckType > 1) spinCheckType = 0;
						break;
					case 4:
						tspinEnableEZ = !tspinEnableEZ;
						break;
					case 5:
						enableB2B = !enableB2B;
						break;
					case 6:
						enableCombo = !enableCombo;
						break;
					case 7:
						goaltype += change;
						if(goaltype < 0) goaltype = GAMETYPE_MAX - 1;
						if(goaltype > GAMETYPE_MAX - 1) goaltype = 0;

						if((startlevel > (tableGameClearLines[goaltype] - 1) / 10) && (tableGameClearLines[goaltype] >= 0)) {
							startlevel = (tableGameClearLines[goaltype] - 1) / 10;
							engine.owner.backgroundStatus.bg = startlevel;
						}
						break;
					case 8:
						big = !big;
						break;
				}

				// NET: Signal options change
				if(netIsNetPlay && (netNumSpectators > 0)) {
					netSendOptions(engine);
				}
			}

			// Confirm
			if(engine.ctrl.isPush(Controller.BUTTON_A) && (engine.statc[3] >= 5)) {
				engine.playSE("decide");
				saveSetting(owner.modeConfig);
				receiver.saveModeConfig(owner.modeConfig);

				// NET: Signal start of the game
				if(netIsNetPlay) netLobby.netPlayerClient.send("start1p\n");

				return false;
			}

			// Cancel
			if(engine.ctrl.isPush(Controller.BUTTON_B) && !netIsNetPlay) {
				engine.quitflag = true;
			}

			// NET: Netplay Ranking
			if(engine.ctrl.isPush(Controller.BUTTON_D) && netIsNetPlay && startlevel == 0 && !big &&
					engine.ai == null) {
				netEnterNetPlayRankingScreen(engine, playerID, goaltype);
			}

			engine.statc[3]++;
		}
		// Replay
		else {
			engine.statc[3]++;
			engine.statc[2] = -1;

			if(engine.statc[3] >= 60) {
				return false;
			}
		}

		return true;
	}

	/*
	 * Render the settings screen
	 */
	@Override
	public void renderSetting(GameEngine engine, int playerID) {
		if(netIsNetRankingDisplayMode) {
			// NET: Netplay Ranking
			netOnRenderNetPlayRanking(engine, playerID, receiver);
		} else {
			String strTSpinEnable = "";
			if(version >= 2) {
				if(tspinEnableType == 0) strTSpinEnable = "OFF";
				if(tspinEnableType == 1) strTSpinEnable = "T-ONLY";
				if(tspinEnableType == 2) strTSpinEnable = "ALL";
			} else {
				strTSpinEnable = GeneralUtil.getONorOFF(enableTSpin);
			}
			drawMenu(engine, playerID, receiver, 0, EventReceiver.COLOR_BLUE, 0,
					"LEVEL", String.valueOf(startlevel + 1),
					"SPIN BONUS", strTSpinEnable,
					"EZ SPIN", GeneralUtil.getONorOFF(enableTSpinKick),
					"SPIN TYPE", (spinCheckType == 0) ? "4POINT" : "IMMOBILE",
					"EZIMMOBILE", GeneralUtil.getONorOFF(tspinEnableEZ),
					"B2B", GeneralUtil.getONorOFF(enableB2B),
					"COMBO",  GeneralUtil.getONorOFF(enableCombo),
					"GOAL",  (tableGameClearLines[goaltype] < 0) ? "ENDLESS" : tableGameClearLines[goaltype] + (tableGameClearLines[goaltype] > 1 ? " LINES" : " LINE"),
					"BIG", GeneralUtil.getONorOFF(big));
		}
	}

	// Currently this is set to generate a fresh set of pieces.
	@Override
	public boolean onReady(GameEngine engine, int playerID) {
		// Generates a fresh set of pieces from the current rule.
		if (engine.statc[0] == 0) {
			fallPieceDraw = null;
			fallPieceLoc = null;
			fallPieceVel = null;

			lerpTime = 120;
			lastScore = 0;

			directionRandom = new Random(engine.randSeed);
			pieceSet.clear();
			pieceDirections.clear();
			pieceRandomDirection.clear();

			for (int i = 0; i <= 6; i++) {
				pieceSet.add(new Piece(i));
				pieceSet.get(i).direction = engine.ruleopt.pieceDefaultDirection[pieceSet.get(i).id];
				if(pieceSet.get(i).direction >= Piece.DIRECTION_COUNT) {
					pieceRandomDirection.add(true);
					pieceSet.get(i).direction = directionRandom.nextInt(Piece.DIRECTION_COUNT);
					pieceDirections.add(pieceSet.get(i).direction);
				} else {
					pieceRandomDirection.add(false);
					pieceDirections.add(pieceSet.get(i).direction);
				}
				pieceSet.get(i).connectBlocks = engine.connectBlocks;
				pieceSet.get(i).setColor(engine.ruleopt.pieceColor[pieceSet.get(i).id]);
				pieceSet.get(i).setSkin(engine.getSkin());
				pieceSet.get(i).updateConnectData();
				pieceSet.get(i).setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
				pieceSet.get(i).setAttribute(Block.BLOCK_ATTRIBUTE_BONE, engine.bone);
			}

			onShadow = true;
			capLines = false;
			multiplierScore = 1d;
			matchConfidences.clear();

			lastIDs.clear();
			lastMatchPercentages.clear();

			for (int i = 0; i < 4; i++) {
				lastIDs.add(-1);
				lastMatchPercentages.add(0d);
			}
		} else if (engine.statc[0] == 1) {
			mainField = new Field(engine.field);
			shadowField = new Field(engine.field);
			parseMatches(engine);
		}

		return false;
	}

	private void switchField(GameEngine engine) {
		if (onShadow) {
			onShadow = false;
			shadowField = new Field(engine.field.getWidth(), engine.field.getHeight(), engine.field.getHiddenHeight(), engine.field.ceiling);
			engine.field = new Field(mainField);
		} else {
			onShadow = true;
			mainField = new Field(engine.field);
			engine.field = new Field(shadowField);
		}

		engine.combo = 0;
		lastevent = EVENT_NONE;
		scgettime = 120;
	}

	@Override
	public boolean onMove(GameEngine engine, int playerID) {
		// 横溜めInitialization
		int moveDirection = engine.getMoveDirection();

		if((engine.statc[0] > 0) || (engine.ruleopt.dasInMoveFirstFrame)) {
			if(engine.dasDirection != moveDirection) {
				engine.dasDirection = moveDirection;
				if(!(engine.dasDirection == 0 && engine.ruleopt.dasStoreChargeOnNeutral)){
					engine.dasCount = 0;
				}
			}
		}

		// 出現時の処理
		if(engine.statc[0] == 0) {
			if((engine.statc[1] == 0) && (engine.initialHoldFlag == false) && onShadow) {
				// 通常出現
				engine.nowPieceObject = engine.getNextObjectCopy(engine.nextPieceCount);
				engine.nextPieceCount++;
				if(engine.nextPieceCount < 0) engine.nextPieceCount = 0;
				engine.holdDisable = false;
			} else if (onShadow) {
				// ホールド出現
				if(engine.initialHoldFlag) {
					// 先行ホールド
					if(engine.holdPieceObject == null) {
						// 1回目
						engine.holdPieceObject = engine.getNextObjectCopy(engine.nextPieceCount);
						engine.holdPieceObject.applyOffsetArray(engine.ruleopt.pieceOffsetX[engine.holdPieceObject.id], engine.ruleopt.pieceOffsetY[engine.holdPieceObject.id]);
						engine.nextPieceCount++;
						if(engine.nextPieceCount < 0) engine.nextPieceCount = 0;

						if(engine.bone == true) engine.getNextObject(engine.nextPieceCount + engine.ruleopt.nextDisplay - 1).setAttribute(Block.BLOCK_ATTRIBUTE_BONE, true);

						engine.nowPieceObject = engine.getNextObjectCopy(engine.nextPieceCount);
						engine.nextPieceCount++;
						if(engine.nextPieceCount < 0) engine.nextPieceCount = 0;
					} else {
						// 2回目以降
						Piece pieceTemp = engine.holdPieceObject;
						engine.holdPieceObject = engine.getNextObjectCopy(engine.nextPieceCount);
						engine.holdPieceObject.applyOffsetArray(engine.ruleopt.pieceOffsetX[engine.holdPieceObject.id], engine.ruleopt.pieceOffsetY[engine.holdPieceObject.id]);
						engine.nowPieceObject = pieceTemp;
						engine.nextPieceCount++;
						if(engine.nextPieceCount < 0) engine.nextPieceCount = 0;
					}
				} else {
					// 通常ホールド
					if(engine.holdPieceObject == null) {
						// 1回目
						engine.nowPieceObject.big = false;
						engine.holdPieceObject = engine.nowPieceObject;
						engine.nowPieceObject = engine.getNextObjectCopy(engine.nextPieceCount);
						engine.nextPieceCount++;
						if(engine.nextPieceCount < 0) engine.nextPieceCount = 0;
					} else {
						// 2回目以降
						engine.nowPieceObject.big = false;
						Piece pieceTemp = engine.holdPieceObject;
						engine.holdPieceObject = engine.nowPieceObject;
						engine.nowPieceObject = pieceTemp;
					}
				}

				// Directionを戻す
				if((engine.ruleopt.holdResetDirection) && (engine.ruleopt.pieceDefaultDirection[engine.holdPieceObject.id] < Piece.DIRECTION_COUNT)) {
					engine.holdPieceObject.direction = engine.ruleopt.pieceDefaultDirection[engine.holdPieceObject.id];
					engine.holdPieceObject.updateConnectData();
				}

				// 使用した count+1
				engine.holdUsedCount++;
				engine.statistics.totalHoldUsed++;

				// ホールド無効化
				engine.initialHoldFlag = false;
				engine.holdDisable = true;
			}

			if (!onShadow) {
				engine.nowPieceObject = new Piece(pieceSet.get(currentPieceID));
				engine.nowPieceObject.offsetApplied = false;
				engine.holdDisable = true;
			}

			engine.playSE("piece" + engine.getNextObject(engine.nextPieceCount).id);

			if(engine.nowPieceObject.offsetApplied == false)
				engine.nowPieceObject.applyOffsetArray(engine.ruleopt.pieceOffsetX[engine.nowPieceObject.id], engine.ruleopt.pieceOffsetY[engine.nowPieceObject.id]);

			engine.nowPieceObject.big = engine.big;

			// 出現位置 (横）
			engine.nowPieceX = engine.getSpawnPosX(engine.field, engine.nowPieceObject);

			// 出現位置 (縦）
			engine.nowPieceY = engine.getSpawnPosY(engine.nowPieceObject);

			engine.nowPieceBottomY = engine.nowPieceObject.getBottom(engine.nowPieceX, engine.nowPieceY, engine.field);
			engine.nowPieceColorOverride = -1;

			if(engine.itemRollRollEnable) engine.nowPieceColorOverride = Block.BLOCK_COLOR_GRAY;

			// 先行rotation
			if(engine.versionMajor < 7.5f) engine.initialRotate(); //XXX: Weird active time IRS
			//if( (getARE() != 0) && ((getARELine() != 0) || (version < 6.3f)) ) initialRotate();

			if((engine.speed.gravity > engine.speed.denominator) && (engine.speed.denominator > 0))
				engine.gcount = engine.speed.gravity % engine.speed.denominator;
			else
				engine.gcount = 0;

			engine.lockDelayNow = 0;
			engine.dasSpeedCount = engine.getDASDelay();
			engine.dasRepeat = false;
			engine.dasInstant = false;
			engine.extendedMoveCount = 0;
			engine.extendedRotateCount = 0;
			engine.softdropFall = 0;
			engine.harddropFall = 0;
			engine.manualLock = false;
			engine.nowPieceMoveCount = 0;
			engine.nowPieceRotateCount = 0;
			engine.nowPieceRotateFailCount = 0;
			engine.nowWallkickCount = 0;
			engine.nowUpwardWallkickCount = 0;
			engine.lineClearing = 0;
			engine.lastmove = GameEngine.LASTMOVE_NONE;
			engine.kickused = false;
			engine.tspin = false;
			engine.tspinmini = false;
			engine.tspinez = false;

			engine.getNextObject(engine.nextPieceCount + engine.ruleopt.nextDisplay - 1).setAttribute(Block.BLOCK_ATTRIBUTE_BONE, engine.bone);

			if(engine.ending == 0) engine.timerActive = true;

			if((engine.ai != null) && (!engine.owner.replayMode || engine.owner.replayRerecord)) engine.ai.newPiece(engine, playerID);
		}

		engine.checkDropContinuousUse();

		boolean softdropUsed = false; // この frame にSoft dropを使ったらtrue
		int softdropFallNow = 0; // この frame のSoft dropで落下した段count

		boolean updown = false; // Up下同時押し flag
		if(engine.ctrl.isPress(engine.getUp()) && engine.ctrl.isPress(engine.getDown())) updown = true;

		if(!engine.dasInstant) {

			// ホールド
			if(engine.ctrl.isPush(Controller.BUTTON_D) || engine.initialHoldFlag) {
				if(engine.isHoldOK() && onShadow) {
					engine.statc[0] = 0;
					engine.statc[1] = 1;
					if(!engine.initialHoldFlag) engine.playSE("hold");
					engine.initialHoldContinuousUse = true;
					engine.initialHoldFlag = false;
					engine.holdDisable = true;
					engine.initialRotate(); //Hold swap triggered IRS
					engine.statMove();
					return true;
				} else if((engine.statc[0] > 0) && (!engine.initialHoldFlag)) {
					engine.playSE("holdfail");
				}
			}

			// rotation
			boolean onGroundBeforeRotate = engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY + 1, engine.field);
			int move = 0;
			boolean rotated = false;

			if(engine.initialRotateDirection != 0) {
				move = engine.initialRotateDirection;
				engine.initialRotateLastDirection = engine.initialRotateDirection;
				engine.initialRotateContinuousUse = true;
				engine.playSE("initialrotate");
			} else if((engine.statc[0] > 0) || (engine.ruleopt.moveFirstFrame == true)) {
				if((engine.itemRollRollEnable) && (engine.replayTimer % engine.itemRollRollInterval == 0)) move = 1;	// Roll Roll

				//  button input
				if(engine.ctrl.isPush(Controller.BUTTON_A) || engine.ctrl.isPush(Controller.BUTTON_C)) move = -1;
				else if(engine.ctrl.isPush(Controller.BUTTON_B)) move = 1;
				else if(engine.ctrl.isPush(Controller.BUTTON_E)) move = 2;

				if(move != 0) {
					engine.initialRotateLastDirection = move;
					engine.initialRotateContinuousUse = true;
				}
			}

			if((engine.ruleopt.rotateButtonAllowDouble == false) && (move == 2)) move = -1;
			if((engine.ruleopt.rotateButtonAllowReverse == false) && (move == 1)) move = -1;
			if(engine.isRotateButtonDefaultRight() && (move != 2)) move = move * -1;

			if(move != 0) {
				// Direction after rotationを決める
				int rt = engine.getRotateDirection(move);

				// rotationできるか判定
				if(engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY, rt, engine.field) == false)
				{
					// Wallkickなしでrotationできるとき
					rotated = true;
					engine.kickused = false;
					engine.nowPieceObject.direction = rt;
					engine.nowPieceObject.updateConnectData();
				} else if( (engine.ruleopt.rotateWallkick == true) &&
						(engine.wallkick != null) &&
						((engine.initialRotateDirection == 0) || (engine.ruleopt.rotateInitialWallkick == true)) &&
						((engine.ruleopt.lockresetLimitOver != RuleOptions.LOCKRESET_LIMIT_OVER_NOWALLKICK) || (engine.isRotateCountExceed() == false)) )
				{
					// Wallkickを試みる
					boolean allowUpward = (engine.ruleopt.rotateMaxUpwardWallkick < 0) || (engine.nowUpwardWallkickCount < engine.ruleopt.rotateMaxUpwardWallkick);
					WallkickResult kick = engine.wallkick.executeWallkick(engine.nowPieceX, engine.nowPieceY, move, engine.nowPieceObject.direction, rt,
							allowUpward, engine.nowPieceObject, engine.field, engine.ctrl);

					if(kick != null) {
						rotated = true;
						engine.kickused = true;
						engine.nowWallkickCount++;
						if(kick.isUpward()) engine.nowUpwardWallkickCount++;
						engine.nowPieceObject.direction = kick.direction;
						engine.nowPieceObject.updateConnectData();
						engine.nowPieceX += kick.offsetX;
						engine.nowPieceY += kick.offsetY;

						if(engine.ruleopt.lockresetWallkick && !engine.isRotateCountExceed()) {
							engine.lockDelayNow = 0;
							engine.nowPieceObject.setDarkness(0f);
						}
					}
				}

				// Domino Quick Turn
				if(!rotated && engine.dominoQuickTurn && (engine.nowPieceObject.id == Piece.PIECE_I2) && (engine.nowPieceRotateFailCount >= 1)) {
					rt = engine.getRotateDirection(2);
					rotated = true;
					engine.nowPieceObject.direction = rt;
					engine.nowPieceObject.updateConnectData();
					engine.nowPieceRotateFailCount = 0;

					if(engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY, rt, engine.field) == true) {
						engine.nowPieceY--;
					} else if(onGroundBeforeRotate) {
						engine.nowPieceY++;
					}
				}

				if(rotated == true) {
					// rotation成功
					engine.nowPieceBottomY = engine.nowPieceObject.getBottom(engine.nowPieceX, engine.nowPieceY, engine.field);

					if((engine.ruleopt.lockresetRotate == true) && (engine.isRotateCountExceed() == false)) {
						engine.lockDelayNow = 0;
						engine.nowPieceObject.setDarkness(0f);
					}

					if(onGroundBeforeRotate) {
						engine.extendedRotateCount++;
						engine.lastmove = GameEngine.LASTMOVE_ROTATE_GROUND;
					} else {
						engine.lastmove = GameEngine.LASTMOVE_ROTATE_AIR;
					}

					if(engine.initialRotateDirection == 0) {
						engine.playSE("rotate");
					}

					engine.nowPieceRotateCount++;
					if((engine.ending == 0) || (engine.staffrollEnableStatistics)) engine.statistics.totalPieceRotate++;
				} else {
					// rotation失敗
					engine.playSE("rotfail");
					engine.nowPieceRotateFailCount++;
				}
			}
			engine.initialRotateDirection = 0;

			// game over check
			if((engine.statc[0] == 0) && (engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY, engine.field) == true)) {
				// Blockの出現位置を上にずらすことができる場合はそうする
				for(int i = 0; i < engine.ruleopt.pieceEnterMaxDistanceY; i++) {
					if(engine.nowPieceObject.big) engine.nowPieceY -= 2;
					else engine.nowPieceY--;

					if(engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY, engine.field) == false) {
						engine.nowPieceBottomY = engine.nowPieceObject.getBottom(engine.nowPieceX, engine.nowPieceY, engine.field);
						break;
					}
				}

				// 死亡
				if(engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY, engine.field) == true) {
					engine.nowPieceObject.placeToField(engine.nowPieceX, engine.nowPieceY, engine.field);
					engine.nowPieceObject = null;

					if (engine.statistics.level >= 2000) {
						engine.stat = GameEngine.STAT_EXCELLENT;
					} else {
						engine.stat = GameEngine.STAT_GAMEOVER;
					}
					if((engine.ending == 2) && (engine.staffrollNoDeath)) engine.stat = GameEngine.STAT_NOTHING;
					engine.resetStatc();
					return true;
				}
			}

		}

		int move = 0;
		boolean sidemoveflag = false;	// この frame に横移動したらtrue

		if((engine.statc[0] > 0) || (engine.ruleopt.moveFirstFrame == true)) {
			// 横移動
			boolean onGroundBeforeMove = engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY + 1, engine.field);

			move = moveDirection;

			if (engine.statc[0] == 0 && engine.delayCancel) {
				if (engine.delayCancelMoveLeft) move = -1;
				if (engine.delayCancelMoveRight) move = 1;
				engine.dasCount = 0;
				// delayCancel = false;
				engine.delayCancelMoveLeft = false;
				engine.delayCancelMoveRight = false;
			} else if (engine.statc[0] == 1 && engine.delayCancel && (engine.dasCount < engine.getDAS())) {
				move = 0;
				engine.delayCancel = false;
			}

			if(move != 0) sidemoveflag = true;

			if(engine.big && engine.bigmove) move *= 2;

			if((move != 0) && (engine.dasCount == 0)) engine.shiftLock = 0;

			if( (move != 0) && ((engine.dasCount == 0) || (engine.dasCount >= engine.getDAS())) ) {
				engine.shiftLock &= engine.ctrl.getButtonBit();

				if(engine.shiftLock == 0) {
					if( (engine.dasSpeedCount >= engine.getDASDelay()) || (engine.dasCount == 0) ) {
						if(engine.dasCount > 0) engine.dasSpeedCount = 1;

						if(engine.nowPieceObject.checkCollision(engine.nowPieceX + move, engine.nowPieceY, engine.field) == false) {
							engine.nowPieceX += move;

							if((engine.getDASDelay() == 0) && (engine.dasCount > 0) && (engine.nowPieceObject.checkCollision(engine.nowPieceX + move, engine.nowPieceY, engine.field) == false)) {
								if(!engine.dasInstant) engine.playSE("move");
								engine.dasRepeat = true;
								engine.dasInstant = true;
							}

							//log.debug("Successful movement: move="+move);

							if((engine.ruleopt.lockresetMove == true) && (engine.isMoveCountExceed() == false)) {
								engine.lockDelayNow = 0;
								engine.nowPieceObject.setDarkness(0f);
							}

							engine.nowPieceMoveCount++;
							if((engine.ending == 0) || (engine.staffrollEnableStatistics)) engine.statistics.totalPieceMove++;
							engine.nowPieceBottomY = engine.nowPieceObject.getBottom(engine.nowPieceX, engine.nowPieceY, engine.field);

							if(onGroundBeforeMove) {
								engine.extendedMoveCount++;
								engine.lastmove = GameEngine.LASTMOVE_SLIDE_GROUND;
							} else {
								engine.lastmove = GameEngine.LASTMOVE_SLIDE_AIR;
							}

							if(!engine.dasInstant) engine.playSE("move");

						} else if (engine.ruleopt.dasChargeOnBlockedMove) {
							engine.dasCount = engine.getDAS();
							engine.dasSpeedCount = engine.getDASDelay();
						}
					} else {
						engine.dasSpeedCount++;
					}
				}
			}

			// Hard drop
			if( (engine.ctrl.isPress(engine.getUp()) == true) &&
					(engine.harddropContinuousUse == false) &&
					(engine.ruleopt.harddropEnable == true) &&
					((engine.isDiagonalMoveEnabled() == true) || (sidemoveflag == false)) &&
					((engine.ruleopt.moveUpAndDown == true) || (updown == false)) &&
					(engine.nowPieceY < engine.nowPieceBottomY) )
			{
				engine.harddropFall += engine.nowPieceBottomY - engine.nowPieceY;

				if(engine.nowPieceY != engine.nowPieceBottomY) {
					engine.nowPieceY = engine.nowPieceBottomY;
					engine.playSE("harddrop");
				}

				if(engine.owner.mode != null) engine.owner.mode.afterHardDropFall(engine, playerID, engine.harddropFall);
				engine.owner.receiver.afterHardDropFall(engine, playerID, engine.harddropFall);

				engine.lastmove = GameEngine.LASTMOVE_FALL_SELF;
				if(engine.ruleopt.lockresetFall == true) {
					engine.lockDelayNow = 0;
					engine.nowPieceObject.setDarkness(0f);
					engine.extendedMoveCount = 0;
					engine.extendedRotateCount = 0;
				}
			}

			if(!engine.ruleopt.softdropGravitySpeedLimit || (engine.ruleopt.softdropSpeed < 1.0f)) {
				// Old Soft Drop codes
				if( (engine.ctrl.isPress(engine.getDown()) == true) &&
						(engine.softdropContinuousUse == false) &&
						(engine.ruleopt.softdropEnable == true) &&
						((engine.isDiagonalMoveEnabled() == true) || (sidemoveflag == false)) &&
						((engine.ruleopt.moveUpAndDown == true) || (updown == false)) )
				{
					if((engine.ruleopt.softdropMultiplyNativeSpeed == true) || (engine.speed.denominator <= 0))
						engine.gcount += (int)(engine.speed.gravity * engine.ruleopt.softdropSpeed);
					else
						engine.gcount += (int)(engine.speed.denominator * engine.ruleopt.softdropSpeed);

					softdropUsed = true;
				}
			} else {
				// New Soft Drop codes
				if( engine.ctrl.isPress(engine.getDown()) && !engine.softdropContinuousUse &&
						engine.ruleopt.softdropEnable && (engine.isDiagonalMoveEnabled() || !sidemoveflag) &&
						(engine.ruleopt.moveUpAndDown || !updown) &&
						(engine.ruleopt.softdropMultiplyNativeSpeed || (engine.speed.gravity < (int)(engine.speed.denominator * engine.ruleopt.softdropSpeed))) )
				{
					if((engine.ruleopt.softdropMultiplyNativeSpeed == true) || (engine.speed.denominator <= 0)) {
						// gcount += (int)(speed.gravity * ruleopt.softdropSpeed);
						engine.gcount = (int)(engine.speed.gravity * engine.ruleopt.softdropSpeed);
					} else {
						// gcount += (int)(speed.denominator * ruleopt.softdropSpeed);
						engine.gcount = (int)(engine.speed.denominator * engine.ruleopt.softdropSpeed);
					}

					softdropUsed = true;
				} else {
					// 落下
					// This prevents soft drop from adding to the gravity speed.
					engine.gcount += engine.speed.gravity;
				}
			}

			if((engine.ending == 0) || (engine.staffrollEnableStatistics)) engine.statistics.totalPieceActiveTime++;
		}

		if(!engine.ruleopt.softdropGravitySpeedLimit || (engine.ruleopt.softdropSpeed < 1.0f))
			engine.gcount += engine.speed.gravity;	// Part of Old Soft Drop

		while((engine.gcount >= engine.speed.denominator) || (engine.speed.gravity < 0)) {
			if(engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY + 1, engine.field) == false) {
				if(engine.speed.gravity >= 0) engine.gcount -= engine.speed.denominator;
				engine.nowPieceY++;

				if(engine.ruleopt.lockresetFall == true) {
					engine.lockDelayNow = 0;
					engine.nowPieceObject.setDarkness(0f);
				}

				if((engine.lastmove != GameEngine.LASTMOVE_ROTATE_GROUND) && (engine.lastmove != GameEngine.LASTMOVE_SLIDE_GROUND) && (engine.lastmove != GameEngine.LASTMOVE_FALL_SELF)) {
					engine.extendedMoveCount = 0;
					engine.extendedRotateCount = 0;
				}

				if(softdropUsed == true) {
					engine.lastmove = GameEngine.LASTMOVE_FALL_SELF;
					engine.softdropFall++;
					softdropFallNow++;
					engine.playSE("softdrop");
				} else {
					engine.lastmove = GameEngine.LASTMOVE_FALL_AUTO;
				}
			} else {
				break;
			}
		}

		if(softdropFallNow > 0) {
			if(engine.owner.mode != null) engine.owner.mode.afterSoftDropFall(engine, playerID, softdropFallNow);
			engine.owner.receiver.afterSoftDropFall(engine, playerID, softdropFallNow);
		}

		// 接地と固定
		if( (engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY + 1, engine.field) == true) &&
				((engine.statc[0] > 0) || (engine.ruleopt.moveFirstFrame == true)) )
		{
			if((engine.lockDelayNow == 0) && (engine.getLockDelay() > 0))
				engine.playSE("step");

			if(engine.lockDelayNow < engine.getLockDelay())
				engine.lockDelayNow++;

			if((engine.getLockDelay() >= 99) && (engine.lockDelayNow > 98))
				engine.lockDelayNow = 98;

			if(engine.lockDelayNow < engine.getLockDelay()) {
				if(engine.lockDelayNow >= engine.getLockDelay() - 1)
					engine.nowPieceObject.setDarkness(0.5f);
				else
					engine.nowPieceObject.setDarkness((engine.lockDelayNow * 7 / engine.getLockDelay()) * 0.05f);
			}

			if(engine.getLockDelay() != 0)
				engine.gcount = engine.speed.gravity;

			// trueになると即固定
			boolean instantlock = false;

			// Hard drop固定
			if( (engine.ctrl.isPress(engine.getUp()) == true) &&
					(engine.harddropContinuousUse == false) &&
					(engine.ruleopt.harddropEnable == true) &&
					((engine.isDiagonalMoveEnabled() == true) || (sidemoveflag == false)) &&
					((engine.ruleopt.moveUpAndDown == true) || (updown == false)) &&
					(engine.ruleopt.harddropLock == true) )
			{
				engine.harddropContinuousUse = true;
				engine.manualLock = true;
				instantlock = true;
			}

			// Soft drop固定
			if( (engine.ctrl.isPress(engine.getDown()) == true) &&
					(engine.softdropContinuousUse == false) &&
					(engine.ruleopt.softdropEnable == true) &&
					((engine.isDiagonalMoveEnabled() == true) || (sidemoveflag == false)) &&
					((engine.ruleopt.moveUpAndDown == true) || (updown == false)) &&
					(engine.ruleopt.softdropLock == true) )
			{
				engine.softdropContinuousUse = true;
				engine.manualLock = true;
				instantlock = true;
			}

			// 接地状態でソフドドロップ固定
			if( (engine.ctrl.isPush(engine.getDown()) == true) &&
					(engine.ruleopt.softdropEnable == true) &&
					((engine.isDiagonalMoveEnabled() == true) || (sidemoveflag == false)) &&
					((engine.ruleopt.moveUpAndDown == true) || (updown == false)) &&
					(engine.ruleopt.softdropSurfaceLock == true) )
			{
				engine.softdropContinuousUse = true;
				engine.manualLock = true;
				instantlock = true;
			}

			if((engine.manualLock == true) && (engine.ruleopt.shiftLockEnable)) {
				// bit 1 and 2 are button_up and button_down currently
				engine.shiftLock = engine.ctrl.getButtonBit() & 3;
			}

			// 移動＆rotationcount制限超過
			if( (engine.ruleopt.lockresetLimitOver == RuleOptions.LOCKRESET_LIMIT_OVER_INSTANT) && (engine.isMoveCountExceed() || engine.isRotateCountExceed()) ) {
				instantlock = true;
			}

			// 接地即固定
			if( (engine.getLockDelay() == 0) && ((engine.gcount >= engine.speed.denominator) || (engine.speed.gravity < 0)) ) {
				instantlock = true;
			}

			// 固定
			if( ((engine.lockDelayNow >= engine.getLockDelay()) && (engine.getLockDelay() > 0)) || (instantlock == true) ) {
				if(engine.ruleopt.lockflash > 0) engine.nowPieceObject.setDarkness(-0.8f);

				/*if((lastmove == LASTMOVE_ROTATE_GROUND) && (tspinEnable == true)) {

					tspinmini = false;

					// T-Spin Mini判定

					if(!useAllSpinBonus) {
						if(spinCheckType == SPINTYPE_4POINT) {
							if(tspinminiType == TSPINMINI_TYPE_ROTATECHECK) {
								if(nowPieceObject.checkCollision(nowPieceX, nowPieceY, getRotateDirection(-1), field) &&
								   nowPieceObject.checkCollision(nowPieceX, nowPieceY, getRotateDirection( 1), field))
									tspinmini = true;
							} else if(tspinminiType == TSPINMINI_TYPE_WALLKICKFLAG) {
								tspinmini = kickused;
							}
						} else if(spinCheckType == SPINTYPE_IMMOBILE) {
							Field copyField = new Field(field);
							nowPieceObject.placeToField(nowPieceX, nowPieceY, copyField);
							if((copyField.checkLineNoFlag() == 1) && (kickused == true)) tspinmini = true;
						}
					}
				}*/

				// T-Spin判定
				if((engine.lastmove == GameEngine.LASTMOVE_ROTATE_GROUND) && (engine.tspinEnable == true)) {
					if(engine.useAllSpinBonus)
						engine.setAllSpin(engine.nowPieceX, engine.nowPieceY, engine.nowPieceObject, engine.field);
					else
						engine.setTSpin(engine.nowPieceX, engine.nowPieceY, engine.nowPieceObject, engine.field);
				}

				engine.nowPieceObject.setAttribute(Block.BLOCK_ATTRIBUTE_SELFPLACED, true);

				boolean partialLockOut = engine.nowPieceObject.isPartialLockOut(engine.nowPieceX, engine.nowPieceY, engine.field);
				boolean put = engine.nowPieceObject.placeToField(engine.nowPieceX, engine.nowPieceY, engine.field);

				engine.playSE("lock");

				engine.holdDisable = false;

				if((engine.ending == 0) || (engine.staffrollEnableStatistics)) engine.statistics.totalPieceLocked++;

				if (engine.clearMode == GameEngine.CLEAR_LINE)
					engine.lineClearing = engine.field.checkLineNoFlag();
				else if (engine.clearMode == GameEngine.CLEAR_COLOR)
					engine.lineClearing = engine.field.checkColor(engine.colorClearSize, false, engine.garbageColorClear, engine.gemSameColor, engine.ignoreHidden);
				else if (engine.clearMode == GameEngine.CLEAR_LINE_COLOR)
					engine.lineClearing = engine.field.checkLineColor(engine.colorClearSize, false, engine.lineColorDiagonals, engine.gemSameColor);
				else if (engine.clearMode == GameEngine.CLEAR_GEM_COLOR)
					engine.lineClearing = engine.field.gemColorCheck(engine.colorClearSize, false,engine. garbageColorClear, engine.ignoreHidden);
				engine.chain = 0;
				engine.lineGravityTotalLines = 0;

				if(engine.lineClearing == 0) {
					engine.combo = 0;

					if(engine.tspin) {
						engine.playSE("tspin0");

						if((engine.ending == 0) || (engine.staffrollEnableStatistics)) {
							if(engine.tspinmini) engine.statistics.totalTSpinZeroMini++;
							else engine.statistics.totalTSpinZero++;
						}
					}

					if(engine.owner.mode != null) engine.owner.mode.calcScore(engine, playerID, engine.lineClearing);
					engine.owner.receiver.calcScore(engine, playerID, engine.lineClearing);
				}

				if(engine.owner.mode != null) engine.owner.mode.pieceLocked(engine, playerID, engine.lineClearing);
				engine.owner.receiver.pieceLocked(engine, playerID, engine.lineClearing);

				engine.dasRepeat = false;
				engine.dasInstant = false;

				// Next 処理を決める(Mode 側でステータスを弄っている場合は何もしない)
				if((engine.stat == GameEngine.STAT_MOVE) || (engine.versionMajor <= 6.3f)) {
					engine.resetStatc();

					if((engine.ending == 1) && (engine.versionMajor >= 6.6f) && (engine.versionMinorOld >= 0.1f)) {
						// Ending
						engine.stat = GameEngine.STAT_ENDINGSTART;
					} else if( (!put && engine.ruleopt.fieldLockoutDeath) || (partialLockOut && engine.ruleopt.fieldPartialLockoutDeath) ) {
						// 画面外に置いて死亡
						engine.stat = GameEngine.STAT_GAMEOVER;
						if((engine.ending == 2) && (engine.staffrollNoDeath)) engine.stat = GameEngine.STAT_NOTHING;
					} else if ((engine.lineGravityType == GameEngine.LINE_GRAVITY_CASCADE || engine.lineGravityType == GameEngine.LINE_GRAVITY_CASCADE_SLOW)
							&& !engine.connectBlocks) {
						engine.stat = GameEngine.STAT_LINECLEAR;
						engine.statc[0] = engine.getLineDelay();
						engine.statLineClear();
					} else if( (engine.lineClearing > 0) && ((engine.ruleopt.lockflash <= 0) || (!engine.ruleopt.lockflashBeforeLineClear)) ) {
						// Line clear
						engine.stat = GameEngine.STAT_LINECLEAR;
						engine.statLineClear();
					} else if( ((engine.getARE() > 0) || (engine.lagARE) || (engine.ruleopt.lockflashBeforeLineClear)) &&
							(engine.ruleopt.lockflash > 0) && (engine.ruleopt.lockflashOnlyFrame) )
					{
						// AREあり (光あり）
						engine.stat = GameEngine.STAT_LOCKFLASH;
					} else if((engine.getARE() > 0) || (engine.lagARE)) {
						// AREあり (光なし）
						engine.statc[1] = engine.getARE();
						engine.stat = GameEngine.STAT_ARE;
					} else if(engine.interruptItemNumber != GameEngine.INTERRUPTITEM_NONE) {
						// 中断効果のあるアイテム処理
						engine.nowPieceObject = null;
						engine.interruptItemPreviousStat = GameEngine.STAT_MOVE;
						engine.stat = GameEngine.STAT_INTERRUPTITEM;
					} else {
						// AREなし
						engine.stat = GameEngine.STAT_MOVE;
						if(engine.ruleopt.moveFirstFrame == false) engine.statMove();
					}
				}
				return true;
			}
		}

		// 横溜め
		if((engine.statc[0] > 0) || (engine.ruleopt.dasInMoveFirstFrame)) {
			if( (moveDirection != 0) && (moveDirection == engine.dasDirection) && ((engine.dasCount < engine.getDAS()) || (engine.getDAS() <= 0)) ) {
				engine.dasCount++;
			}
		}

		engine.statc[0]++;
		return true;
	}

	@Override
	public void pieceLocked(GameEngine engine, int playerID, int lines) {
		if (!onShadow && lines == 0) switchField(engine);
	}

	@Override
	public boolean lineClearEnd(GameEngine engine, int playerID) {
		if(engine.lineGravityType == GameEngine.LINE_GRAVITY_NATIVE) engine.field.downFloatingBlocks();
		engine.playSE("linefall");

		engine.field.lineColorsCleared = null;

		if((engine.stat == GameEngine.STAT_LINECLEAR) || (engine.versionMajor <= 6.3f)) {
			engine.resetStatc();
			if(engine.ending == 1) {
				// Ending
				engine.stat = GameEngine.STAT_ENDINGSTART;
			} else if((engine.getARELine() > 0) || (engine.lagARE)) {
				// AREあり
				engine.statc[0] = 0;
				engine.statc[1] = engine.getARELine();
				engine.statc[2] = 1;
				engine.stat = GameEngine.STAT_ARE;
			} else if(engine.interruptItemNumber != GameEngine.INTERRUPTITEM_NONE) {
				// 中断効果のあるアイテム処理
				engine.nowPieceObject = null;
				engine.interruptItemPreviousStat = GameEngine.STAT_MOVE;
				engine.stat = GameEngine.STAT_INTERRUPTITEM;
			} else {
				// AREなし
				engine.nowPieceObject = null;
				if(engine.versionMajor < 7.5f) engine.initialRotate(); //XXX: Weird IRS thing on lines cleared but no ARE
				engine.stat = GameEngine.STAT_MOVE;
			}
		}

		if (onShadow) parseMatches(engine);
		// log.debug(String.format("ID %d: INDEX %d", currentPieceID, currentMaxIndex));

		if (!onShadow) switchField(engine);
		return true;
	}

	/*
	 * Called after every frame
	 */
	@Override
	public void onLast(GameEngine engine, int playerID) {
		scgettime++;
		lerpTime++;

		if (directionRandom != null && engine.gameActive) {
			for (int i = 0; i < pieceRandomDirection.size(); i++) {
				if (pieceRandomDirection.get(i)) {
					pieceDirections.set(i, directionRandom.nextInt(Piece.DIRECTION_COUNT));
					pieceSet.get(i).direction = pieceDirections.get(i);
				}
			}
		}

		if (engine.gameActive && (engine.stat == GameEngine.STAT_MOVE || engine.stat == GameEngine.STAT_ARE)) {
			if (engine.ctrl.isPush(Controller.BUTTON_F) && onShadow && currentMaxMatchValue >= 0.75) {
				fallPieceDraw = new Piece(engine.nowPieceObject);

				int bX = receiver.getFieldDisplayPositionX(engine, playerID) + 4;
				int bY = receiver.getFieldDisplayPositionY(engine, playerID) + 52;

				fallPieceLoc = new double[] { bX + engine.nowPieceX * 16, bY + engine.nowPieceY * 16 };
				fallPieceVel = new double[] { ((directionRandom.nextDouble() - 0.5) * 2 * 8), (directionRandom.nextDouble() * -V_MAX) };

				engine.resetStatc();
				engine.stat = GameEngine.STAT_MOVE;
				engine.nextPieceCount--;
				multiplierScore = currentMaxMatchValue / 0.9;
				multiplierScore *= SHAPE_TO_MULTIPLIER[currentMaxIndex];
				if (lastIDs.contains(currentPieceID)) {
					multiplierScore /= 8;
					capLines = true;
				} else {
					capLines = false;
				}
				switchField(engine);

				lastIDs.remove(0);
				lastIDs.add(currentPieceID);

				lastMatchPercentages.remove(0);
				lastMatchPercentages.add(currentMaxMatchValue);
				engine.playSE("hold");
			} else if (engine.ctrl.isPush(Controller.BUTTON_F)) {
				engine.playSE("holdfail");
			}
		}

		if (engine.gameActive) {
			if (onShadow) {
				engine.framecolor = GameEngine.FRAME_COLOR_GRAY;
			} else {
				engine.framecolor = GameEngine.FRAME_COLOR_GREEN;
			}
		} else {
			engine.framecolor = GameEngine.FRAME_COLOR_GREEN;
		}

		if (fallPieceDraw != null) {
			if (fallPieceLoc[1] > 540) {
				fallPieceDraw = null;
				fallPieceLoc = null;
				fallPieceVel = null;
			} else {
				fallPieceLoc[0] += fallPieceVel[0];
				fallPieceLoc[1] += fallPieceVel[1];

				fallPieceVel[1] += GRAVITY;
			}
		}
	}

	/*
	 * Calculate score
	 */
	@Override
	public void calcScore(GameEngine engine, int playerID, int lines) {
		if (onShadow) parseMatches(engine);
		// log.debug(String.format("ID %d: INDEX %d", currentPieceID, currentMaxIndex));

		// Line clear bonus
		int pts = 0;

		if(engine.tspin) {
			// T-Spin 0 lines
			if((lines == 0) && (!engine.tspinez)) {
				if(engine.tspinmini) {
					pts += 100 * (engine.statistics.level + 1);
					lastevent = EVENT_TSPIN_ZERO_MINI;
				} else {
					pts += 400 * (engine.statistics.level + 1);
					lastevent = EVENT_TSPIN_ZERO;
				}
			}
			// Immobile EZ Spin
			else if(engine.tspinez && (lines > 0)) {
				if(engine.b2b) {
					pts += 180 * (engine.statistics.level + 1);
				} else {
					pts += 120 * (engine.statistics.level + 1);
				}
				lastevent = EVENT_TSPIN_EZ;
			}
			// T-Spin 1 line
			else if(lines == 1) {
				if(engine.tspinmini) {
					if(engine.b2b) {
						pts += 300 * (engine.statistics.level + 1);
					} else {
						pts += 200 * (engine.statistics.level + 1);
					}
					lastevent = EVENT_TSPIN_SINGLE_MINI;
				} else {
					if(engine.b2b) {
						pts += 1200 * (engine.statistics.level + 1);
					} else {
						pts += 800 * (engine.statistics.level + 1);
					}
					lastevent = EVENT_TSPIN_SINGLE;
				}
			}
			// T-Spin 2 lines
			else if(lines == 2) {
				if(engine.tspinmini && engine.useAllSpinBonus) {
					if(engine.b2b) {
						pts += 600 * (engine.statistics.level + 1);
					} else {
						pts += 400 * (engine.statistics.level + 1);
					}
					lastevent = EVENT_TSPIN_DOUBLE_MINI;
				} else {
					if(engine.b2b) {
						pts += 1800 * (engine.statistics.level + 1);
					} else {
						pts += 1200 * (engine.statistics.level + 1);
					}
					lastevent = EVENT_TSPIN_DOUBLE;
				}
			}
			// T-Spin 3 lines
			else if(lines >= 3) {
				if(engine.b2b) {
					pts += 2400 * (engine.statistics.level + 1);
				} else {
					pts += 1600 * (engine.statistics.level + 1);
				}
				lastevent = EVENT_TSPIN_TRIPLE;
			}
		} else {
			if(lines == 1) {
				pts += 100 * (engine.statistics.level + 1); // 1列
				lastevent = EVENT_SINGLE;
			} else if(lines == 2) {
				pts += 300 * (engine.statistics.level + 1); // 2列
				lastevent = EVENT_DOUBLE;
			} else if(lines == 3) {
				pts += 500 * (engine.statistics.level + 1); // 3列
				lastevent = EVENT_TRIPLE;
			} else if(lines >= 4) {
				// 4 lines
				if(engine.b2b) {
					pts += 1200 * (engine.statistics.level + 1);
				} else {
					pts += 800 * (engine.statistics.level + 1);
				}
				lastevent = EVENT_FOUR;
			}
		}

		lastb2b = engine.b2b;

		// Combo
		if((enableCombo) && (engine.combo >= 1) && (lines >= 1)) {
			pts += ((engine.combo - 1) * 50) * (engine.statistics.level + 1);
			lastcombo = engine.combo;
		}

		// All clear
		if((lines >= 1) && (engine.field.isEmpty())) {
			engine.playSE("bravo");
			pts += 1800 * (engine.statistics.level + 1);
		}

		// Add to score
		if(pts > 0) {
			pts *= multiplierScore;
			lastscore = pts;
			lastpiece = engine.nowPieceObject.id;
			scgettime = 0;
			if (!onShadow) {
				lastScore = engine.statistics.score;
				if(lines >= 1) engine.statistics.scoreFromLineClear += pts;
				else engine.statistics.scoreFromOtherBonus += pts;
				engine.statistics.score += pts;
				lerpTime = 0;
			} else {
				lastscore = 0;
			}
		}

		if (onShadow) {
			engine.statistics.lines -= lines;
		} else {
			if (lines > 0 && capLines) {
				engine.statistics.lines -= (lines - 1);
			}
		}

		// BGM fade-out effects and BGM changes
		if(tableBGMChange[bgmlv] != -1) {
			if(engine.statistics.lines >= tableBGMChange[bgmlv] - 5) owner.bgmStatus.fadesw = true;

			if( (engine.statistics.lines >= tableBGMChange[bgmlv]) &&
					((engine.statistics.lines < tableGameClearLines[goaltype]) || (tableGameClearLines[goaltype] < 0)) )
			{
				bgmlv++;
				owner.bgmStatus.bgm = bgmlv;
				owner.bgmStatus.fadesw = false;
			}
		}

		// Meter
		engine.meterValue = ((engine.statistics.lines % 10) * receiver.getMeterMax(engine)) / 9;
		engine.meterColor = GameEngine.METER_COLOR_GREEN;
		if(engine.statistics.lines % 10 >= 4) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
		if(engine.statistics.lines % 10 >= 6) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
		if(engine.statistics.lines % 10 >= 8) engine.meterColor = GameEngine.METER_COLOR_RED;

		if((engine.statistics.lines >= tableGameClearLines[goaltype]) && (tableGameClearLines[goaltype] >= 0)) {
			// Ending
			engine.ending = 1;
			engine.gameEnded();
		} else if((engine.statistics.lines >= (engine.statistics.level + 1) * 10) && (engine.statistics.level < 19)) {
			// Level up
			engine.statistics.level++;

			owner.backgroundStatus.fadesw = true;
			owner.backgroundStatus.fadecount = 0;
			owner.backgroundStatus.fadebg = engine.statistics.level;

			setSpeed(engine);
			engine.playSE("levelup");
		}
	}

	/*
	 * Soft drop
	 */
	@Override
	public void afterSoftDropFall(GameEngine engine, int playerID, int fall) {
		if (!onShadow) {
			engine.statistics.scoreFromSoftDrop += fall;
			engine.statistics.score += fall;
		}
	}

	/*
	 * Hard drop
	 */
	@Override
	public void afterHardDropFall(GameEngine engine, int playerID, int fall) {
		if (!onShadow) {
			engine.statistics.scoreFromHardDrop += fall * 2;
			engine.statistics.score += fall * 2;
		}
	}

	/*
	 * Render score
	 */
	@Override
	public void renderLast(GameEngine engine, int playerID) {
		if(owner.menuOnly) return;

		receiver.drawScoreFont(engine, playerID, 0, 0, getName(), EventReceiver.COLOR_GREEN);

		if(tableGameClearLines[goaltype] == -1) {
			receiver.drawScoreFont(engine, playerID, 0, 1, "(ENDLESS GAME)", EventReceiver.COLOR_GREEN);
		} else {
			receiver.drawScoreFont(engine, playerID, 0, 1, "(" + tableGameClearLines[goaltype] + " " + (tableGameClearLines[goaltype] > 1 ? "LINES" : "LINE") + " GAME)", EventReceiver.COLOR_GREEN);
		}

		if( (engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (owner.replayMode == false)) ) {
			if((owner.replayMode == false) && (big == false) && (engine.ai == null)) {
				float scale = (receiver.getNextDisplayType() == 2) ? 0.5f : 1.0f;
				int topY = (receiver.getNextDisplayType() == 2) ? 6 : 4;
				receiver.drawScoreFont(engine, playerID, 3, topY-1, "SCORE  LINE TIME", EventReceiver.COLOR_BLUE, scale);

				for(int i = 0; i < RANKING_MAX; i++) {
					receiver.drawScoreFont(engine, playerID,  0, topY+i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW, scale);
					receiver.drawScoreFont(engine, playerID,  3, topY+i, String.valueOf(rankingScore[goaltype][i]), (i == rankingRank), scale);
					receiver.drawScoreFont(engine, playerID, 10, topY+i, String.valueOf(rankingLines[goaltype][i]), (i == rankingRank), scale);
					receiver.drawScoreFont(engine, playerID, 15, topY+i, GeneralUtil.getTime(rankingTime[goaltype][i]), (i == rankingRank), scale);
				}
			}
		} else {
			receiver.drawScoreFont(engine, playerID, 0, 3, "SCORE", EventReceiver.COLOR_BLUE);
			String strScore;
			if((lastscore == 0) || (lerpTime >= 120)) {
				strScore = String.valueOf(engine.statistics.score);
			} else {
				strScore = String.valueOf(Interpolation.lerp(lastScore,engine.statistics.score, (double)lerpTime / 120d)) + "(+" + String.valueOf(lastscore) + ")";
			}
			receiver.drawScoreFont(engine, playerID, 0, 4, strScore);

			receiver.drawScoreFont(engine, playerID, 0, 6, "LINE", EventReceiver.COLOR_BLUE);
			if((engine.statistics.level >= 19) && (tableGameClearLines[goaltype] < 0))
				receiver.drawScoreFont(engine, playerID, 0, 7, engine.statistics.lines + "");
			else {
				int k = ((engine.statistics.level + 1) * 10);
				if (k > tableGameClearLines[goaltype]) k = tableGameClearLines[goaltype];
				receiver.drawScoreFont(engine, playerID, 0, 7, engine.statistics.lines + "/" + k);
			}

			receiver.drawScoreFont(engine, playerID, 0, 9, "LEVEL", EventReceiver.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 10, String.valueOf(engine.statistics.level + 1));

			receiver.drawScoreFont(engine, playerID, 0, 12, "TIME", EventReceiver.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 13, GeneralUtil.getTime(engine.statistics.time));

			if (engine.field != null) {
				receiver.drawScoreFont(engine, playerID, 0, 15, "BEST MATCH", EventReceiver.COLOR_BLUE);
				if (engine.field.isEmpty() && onShadow) {
					receiver.drawScoreFont(engine, playerID, 0, 16, "EMPTY FIELD", (engine.statistics.time / 4) % 2 == 1);
				} else {
					if (onShadow) receiver.drawScoreFont(engine, playerID, 0, 16, SHAPE_NAMES[currentPieceID] + String.format(" [%.2f", currentMaxMatchValue * 100) + "%]", PIECE_COLOURS[currentPieceID]);
					else GameTextUtilities.drawRainbowScoreString(receiver, engine, playerID, 0, 16, "ON REAL FIELD", GameTextUtilities.RAINBOW_ORDER[(engine.statistics.time / 2) % GameTextUtilities.RAINBOW_COLOURS], 1f);
				}
			}

			receiver.drawScoreFont(engine, playerID, 0, 18, "RECENT", EventReceiver.COLOR_BLUE);
			for (int i = 0; i < lastIDs.size(); i++) {
				if (lastIDs.get(i) != -1) receiver.drawScoreFont(engine, playerID, 0, 22 - i, SHAPE_NAMES[lastIDs.get(i)] + String.format(" [%.2f", lastMatchPercentages.get(i) * 100) + "%]", PIECE_COLOURS[lastIDs.get(i)]);
			}

			if((lastevent != EVENT_NONE) && (scgettime < 120)) {
				String strPieceName = Piece.getPieceName(lastpiece);

				switch(lastevent) {
					case EVENT_SINGLE:
						receiver.drawMenuFont(engine, playerID, 2, 21, "SINGLE", EventReceiver.COLOR_DARKBLUE);
						break;
					case EVENT_DOUBLE:
						receiver.drawMenuFont(engine, playerID, 2, 21, "DOUBLE", EventReceiver.COLOR_BLUE);
						break;
					case EVENT_TRIPLE:
						receiver.drawMenuFont(engine, playerID, 2, 21, "TRIPLE", EventReceiver.COLOR_GREEN);
						break;
					case EVENT_FOUR:
						if(lastb2b) receiver.drawMenuFont(engine, playerID, 3, 21, "FOUR", EventReceiver.COLOR_RED);
						else receiver.drawMenuFont(engine, playerID, 3, 21, "FOUR", EventReceiver.COLOR_ORANGE);
						break;
					case EVENT_TSPIN_ZERO_MINI:
						receiver.drawMenuFont(engine, playerID, 2, 21, strPieceName + "-SPIN", EventReceiver.COLOR_PURPLE);
						break;
					case EVENT_TSPIN_ZERO:
						receiver.drawMenuFont(engine, playerID, 2, 21, strPieceName + "-SPIN", EventReceiver.COLOR_PINK);
						break;
					case EVENT_TSPIN_SINGLE_MINI:
						if(lastb2b) receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-MINI-S", EventReceiver.COLOR_RED);
						else receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-MINI-S", EventReceiver.COLOR_ORANGE);
						break;
					case EVENT_TSPIN_SINGLE:
						if(lastb2b) receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-SINGLE", EventReceiver.COLOR_RED);
						else receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-SINGLE", EventReceiver.COLOR_ORANGE);
						break;
					case EVENT_TSPIN_DOUBLE_MINI:
						if(lastb2b) receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-MINI-D", EventReceiver.COLOR_RED);
						else receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-MINI-D", EventReceiver.COLOR_ORANGE);
						break;
					case EVENT_TSPIN_DOUBLE:
						if(lastb2b) receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-DOUBLE", EventReceiver.COLOR_RED);
						else receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-DOUBLE", EventReceiver.COLOR_ORANGE);
						break;
					case EVENT_TSPIN_TRIPLE:
						if(lastb2b) receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-TRIPLE", EventReceiver.COLOR_RED);
						else receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-TRIPLE", EventReceiver.COLOR_ORANGE);
						break;
					case EVENT_TSPIN_EZ:
						if(lastb2b) receiver.drawMenuFont(engine, playerID, 3, 21, "EZ-" + strPieceName, EventReceiver.COLOR_RED);
						else receiver.drawMenuFont(engine, playerID, 3, 21, "EZ-" + strPieceName, EventReceiver.COLOR_ORANGE);
						break;
				}

				if((lastcombo >= 2) && (lastevent != EVENT_TSPIN_ZERO_MINI) && (lastevent != EVENT_TSPIN_ZERO))
					receiver.drawMenuFont(engine, playerID, 2, 22, (lastcombo - 1) + "COMBO", EventReceiver.COLOR_CYAN);
			}

			if (fallPieceDraw != null) {
				RendererExtension.drawPiece(receiver, (int)fallPieceLoc[0] + 2, (int)fallPieceLoc[1] + 2, fallPieceDraw, 1f, (1f / 3f));
				RendererExtension.drawPiece(receiver, (int)fallPieceLoc[0], (int)fallPieceLoc[1], fallPieceDraw, 1f, 0f);
			}
		}

		// NET: Number of spectators
		netDrawSpectatorsCount(engine, 0, 18);
		// NET: All number of players
		if(playerID == getPlayers() - 1) {
			netDrawAllPlayersCount(engine);
			netDrawGameRate(engine);
		}
		// NET: Player name (It may also appear in offline replay)
		netDrawPlayerName(engine);
	}

	/*
	 * Render results screen
	 */
	@Override
	public void renderResult(GameEngine engine, int playerID) {
		drawResultStats(engine, playerID, receiver, 0, EventReceiver.COLOR_BLUE,
				STAT_SCORE, STAT_LINES, STAT_LEVEL, STAT_TIME);
		drawResultRank(engine, playerID, receiver, 8, EventReceiver.COLOR_BLUE, rankingRank);
		drawResultNetRank(engine, playerID, receiver, 10, EventReceiver.COLOR_BLUE, netRankingRank[0]);
		drawResultNetRankDaily(engine, playerID, receiver, 12, EventReceiver.COLOR_BLUE, netRankingRank[1]);

		if(netIsPB) {
			receiver.drawMenuFont(engine, playerID, 2, 21, "NEW PB", EventReceiver.COLOR_ORANGE);
		}

		if(netIsNetPlay && (netReplaySendStatus == 1)) {
			receiver.drawMenuFont(engine, playerID, 0, 22, "SENDING...", EventReceiver.COLOR_PINK);
		} else if(netIsNetPlay && !netIsWatch && (netReplaySendStatus == 2)) {
			receiver.drawMenuFont(engine, playerID, 1, 22, "A: RETRY", EventReceiver.COLOR_RED);
		}
	}


	/**
	 * Parse matches and set closest match values.
	 * @param engine
	 */
	private void parseMatches(GameEngine engine) {
		matchConfidences.clear();
		int index = 0;
		for (Field field : SHAPE_FIELDS) {
			// log.debug("INDEX " + index + ":");
			matchConfidences.add(FieldManipulation.fieldCompare(engine.field, field));
			index++;
		}

		currentMaxMatchValue = Collections.max(matchConfidences);
		currentMaxIndex = matchConfidences.indexOf(currentMaxMatchValue);
		currentPieceID = SHAPE_TO_PIECE_ID[currentMaxIndex];
	}

	// region Replay and Score

	/*
	 * Called when saving replay
	 */
	@Override
	public void saveReplay(GameEngine engine, int playerID, CustomProperties prop) {
		saveSetting(prop);

		// NET: Save name
		if((netPlayerName != null) && (netPlayerName.length() > 0)) {
			prop.setProperty(playerID + ".net.netPlayerName", netPlayerName);
		}

		// Update rankings
		if((owner.replayMode == false) && (big == false) && (engine.ai == null)) {
			updateRanking(engine.statistics.score, engine.statistics.lines, engine.statistics.time, goaltype);

			if(rankingRank != -1) {
				saveRanking(owner.modeConfig, engine.ruleopt.strRuleName);
				receiver.saveModeConfig(owner.modeConfig);
			}
		}
	}

	/**
	 * Load settings from property file
	 * @param prop Property file
	 */
	private void loadSetting(CustomProperties prop) {
		startlevel = prop.getProperty("shadowMarathon.startlevel", 0);
		tspinEnableType = prop.getProperty("shadowMarathon.tspinEnableType", 1);
		enableTSpin = prop.getProperty("shadowMarathon.enableTSpin", true);
		enableTSpinKick = prop.getProperty("shadowMarathon.enableTSpinKick", true);
		spinCheckType = prop.getProperty("shadowMarathon.spinCheckType", 0);
		tspinEnableEZ = prop.getProperty("shadowMarathon.tspinEnableEZ", false);
		enableB2B = prop.getProperty("shadowMarathon.enableB2B", true);
		enableCombo = prop.getProperty("shadowMarathon.enableCombo", true);
		goaltype = prop.getProperty("shadowMarathon.gametype", 0);
		big = prop.getProperty("shadowMarathon.big", false);
		version = prop.getProperty("shadowMarathon.version", 0);
	}

	/**
	 * Save settings to property file
	 * @param prop Property file
	 */
	private void saveSetting(CustomProperties prop) {
		prop.setProperty("shadowMarathon.startlevel", startlevel);
		prop.setProperty("shadowMarathon.tspinEnableType", tspinEnableType);
		prop.setProperty("shadowMarathon.enableTSpin", enableTSpin);
		prop.setProperty("shadowMarathon.enableTSpinKick", enableTSpinKick);
		prop.setProperty("shadowMarathon.spinCheckType", spinCheckType);
		prop.setProperty("shadowMarathon.tspinEnableEZ", tspinEnableEZ);
		prop.setProperty("shadowMarathon.enableB2B", enableB2B);
		prop.setProperty("shadowMarathon.enableCombo", enableCombo);
		prop.setProperty("shadowMarathon.gametype", goaltype);
		prop.setProperty("shadowMarathon.big", big);
		prop.setProperty("shadowMarathon.version", version);
	}

	/**
	 * Read rankings from property file
	 * @param prop Property file
	 * @param ruleName Rule name
	 */
	@Override
	protected void loadRanking(CustomProperties prop, String ruleName) {
		for(int i = 0; i < RANKING_MAX; i++) {
			for(int j = 0; j < GAMETYPE_MAX; j++) {
				rankingScore[j][i] = prop.getProperty("shadowMarathon.ranking." + ruleName + "." + j + ".score." + i, 0);
				rankingLines[j][i] = prop.getProperty("shadowMarathon.ranking." + ruleName + "." + j + ".lines." + i, 0);
				rankingTime[j][i] = prop.getProperty("shadowMarathon.ranking." + ruleName + "." + j + ".time." + i, 0);
			}
		}
	}

	/**
	 * Save rankings to property file
	 * @param prop Property file
	 * @param ruleName Rule name
	 */
	private void saveRanking(CustomProperties prop, String ruleName) {
		for(int i = 0; i < RANKING_MAX; i++) {
			for(int j = 0; j < GAMETYPE_MAX; j++) {
				prop.setProperty("shadowMarathon.ranking." + ruleName + "." + j + ".score." + i, rankingScore[j][i]);
				prop.setProperty("shadowMarathon.ranking." + ruleName + "." + j + ".lines." + i, rankingLines[j][i]);
				prop.setProperty("shadowMarathon.ranking." + ruleName + "." + j + ".time." + i, rankingTime[j][i]);
			}
		}
	}

	/**
	 * Update rankings
	 * @param sc Score
	 * @param li Lines
	 * @param time Time
	 */
	private void updateRanking(int sc, int li, int time, int type) {
		rankingRank = checkRanking(sc, li, time, type);

		if(rankingRank != -1) {
			// Shift down ranking entries
			for(int i = RANKING_MAX - 1; i > rankingRank; i--) {
				rankingScore[type][i] = rankingScore[type][i - 1];
				rankingLines[type][i] = rankingLines[type][i - 1];
				rankingTime[type][i] = rankingTime[type][i - 1];
			}

			// Add new data
			rankingScore[type][rankingRank] = sc;
			rankingLines[type][rankingRank] = li;
			rankingTime[type][rankingRank] = time;
		}
	}

	/**
	 * Calculate ranking position
	 * @param sc Score
	 * @param li Lines
	 * @param time Time
	 * @return Position (-1 if unranked)
	 */
	private int checkRanking(int sc, int li, int time, int type) {
		for(int i = 0; i < RANKING_MAX; i++) {
			if(sc > rankingScore[type][i]) {
				return i;
			} else if((sc == rankingScore[type][i]) && (li > rankingLines[type][i])) {
				return i;
			} else if((sc == rankingScore[type][i]) && (li == rankingLines[type][i]) && (time < rankingTime[type][i])) {
				return i;
			}
		}

		return -1;
	}

	// endregion Replay and Score
}
