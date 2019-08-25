package zeroxfc.nullpo.custom.modes;

public class DrawMode extends MarathonModeBase {
	/** This mode always has 0G gravity */
	private static final int GRAVITY_CONSTANT = 0;

	/** Time limit per level */
	private static final int[] tablePieceTimeLimit = { 1800,  1680,  1620,  1560,  1500,  1440,  1380,  1320,  1260,  1200,  1140,  1080,  1020, 960, 900, 840, 780,  720,  660,  600 };

	/** Line counts when game ending occurs */
	private static final int[] tableGameClearLines = {50, 100, 150, 200, -1};

	/** Number of entries in rankings */
	private static final int RANKING_MAX = 10;

	/** Number of ranking types */
	private static final int RANKING_TYPE = 5;

	/** Number of game types */
	private static final int GAMETYPE_MAX = 5;

	/** Maximum drawable squares in draw mode */
	private static final int MAX_SQUARES = 4;
}
