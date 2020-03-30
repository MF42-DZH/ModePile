package zeroxfc.nullpo.custom.modes;

import mu.nu.nullpo.game.play.GameEngine;
import zeroxfc.nullpo.custom.libs.Interpolation;

public class Deltatris extends MarathonModeBase {
	/** Gravity denominator for G calculation */
	private static final int GRAVITY_DENOMINATOR = 256;

	/** Starting delays */
	private static final int START_ARE = 25;
	private static final int START_LINE_ARE = 25;
	private static final int START_LINE_DELAY = 40;
	private static final int START_DAS = 14;
	private static final int START_LOCK_DELAY = 30;
	private static final int START_GRAVITY = 4;

	/** Delays after reaching max piece */
	private static final int[] END_ARE = { 8, 6, 4 };
	private static final int[] END_LINE_ARE = { 5, 4, 3 };
	private static final int[] END_LINE_DELAY = { 9, 6, 3 };
	private static final int[] END_DAS = { 6, 6, 4 };
	private static final int[] END_LOCK_DELAY = { 17, 15, 8 };
	private static final int[] END_GRAVITY = { GRAVITY_DENOMINATOR * 40, GRAVITY_DENOMINATOR * 80, GRAVITY_DENOMINATOR * 120 };

	/** Pieces until max
	 *  Use interpolation to get the delays and gravity.
	 *
	 *  -- Planned --
	 *  GRAVITY, ARE, LINE ARE: Linear
	 *  DAS, LOCK DELAY, LINE DELAY: Ease-in-ease-out
	 */
	private static final int[] PIECES_MAX = { (int)(573 * 1.5), 573, 573 / 2 };

	/** Difficulties */
	private static final int DIFFICULTIES = 3;
	private static final int DIFFICULTY_EASY = 0;
	private static final int DIFFICULTY_NORMAL = 1;
	private static final int DIFFICULTY_HARD = 2;

	private int difficulty;

	/**
	 * Deltatris - How fast can you go in this ΔMAX-inspired gamemode?
	 * @return Mode name
	 */
	@Override
	public String getName() {
		return "DELTATRIS";
	}

	// help me i forgot how to make modes

	/*
	 * Initialization
	 */
	@Override
	public void playerInit(GameEngine engine, int playerID) {
		owner = engine.owner;
		receiver = engine.owner.receiver;
		lastscore = 0;
		scgettime = 0;
		lastevent = EVENT_NONE;
		lastb2b = false;
		lastcombo = 0;
		lastpiece = 0;
		bgmlv = 0;

		difficulty = 1;

		rankingRank = -1;
		rankingScore = new int[RANKING_TYPE][RANKING_MAX];
		rankingLines = new int[RANKING_TYPE][RANKING_MAX];
		rankingTime = new int[RANKING_TYPE][RANKING_MAX];

		netPlayerInit(engine, playerID);

		if(!owner.replayMode) {
			// loadSetting(owner.modeConfig);
			loadRanking(owner.modeConfig, engine.ruleopt.strRuleName);
			version = CURRENT_VERSION;
		} else {
			// loadSetting(owner.replayProp);
			if((version == 0) && (owner.replayProp.getProperty("deltatris.endless", false))) goaltype = 2;

			// NET: Load name
			netPlayerName = engine.owner.replayProp.getProperty(playerID + ".net.netPlayerName", "");
		}

		engine.owner.backgroundStatus.bg = startlevel;
		engine.framecolor = GameEngine.FRAME_COLOR_GRAY;
	}

	/**
	 * Set the overall game speed
	 * @param engine GameEngine
	 */
	public void setSpeed(GameEngine engine) {
		double percentage = engine.statistics.totalPieceLocked / (double)PIECES_MAX[difficulty];
		if (percentage > 1d) percentage = 1d;

		engine.speed.gravity = Interpolation.lerp(START_GRAVITY, END_GRAVITY[difficulty], percentage);
		engine.speed.are = (int) Math.ceil(Interpolation.lerp((double)START_ARE, END_ARE[difficulty], percentage));
		engine.speed.areLine = (int) Math.ceil(Interpolation.lerp((double)START_LINE_ARE, END_LINE_ARE[difficulty], percentage));
		engine.speed.lineDelay = (int) Math.ceil(Interpolation.smoothStep(START_LINE_DELAY, END_LINE_DELAY[difficulty], percentage));
		engine.speed.das = (int) Math.floor(Interpolation.smoothStep(START_DAS, END_DAS[difficulty], percentage));
		engine.speed.lockDelay = (int) Math.ceil(Interpolation.smoothStep(START_LOCK_DELAY, END_LOCK_DELAY[difficulty], percentage));
		engine.speed.denominator = GRAVITY_DENOMINATOR;
	}
}
