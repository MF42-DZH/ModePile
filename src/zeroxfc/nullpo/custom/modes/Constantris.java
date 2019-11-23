package zeroxfc.nullpo.custom.modes;

import mu.nu.nullpo.game.component.BGMStatus;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.util.CustomProperties;
import zeroxfc.nullpo.custom.libs.ShakingText;
import zeroxfc.nullpo.custom.libs.SoundLoader;

import java.util.Random;

public class Constantris extends MarathonModeBase {
	private static final int DIFFICULTIES = 4;

	private static final int DIFFICULTY_EASY = 0,
	                         DIFFICULTY_NORMAL = 1,
	                         DIFFICULTY_HARD = 2,
	                         DIFFICULTY_VERY_HARD = 3;

	private static final int[] GOAL_LINES = {
		5, 5, 5, 5, 5, 10, 10, 10, 10, 10,
		15, 15, 15, 15, 15, 20, 20, 20, 20, 20,
		30, 30, 30, 30, 30, 40, 40, 40, 40, 40,
		50, 50, 50, 50, 50, 60, 60, 60, 60, 60,
		75, 75, 75, 75, 75, 90, 90, 90, 90, 90,
		105, 105, 105, 105, 105, 120, 120, 120, 120, 120,
		140, 140, 140, 140, 140, 160, 160, 160, 160, 160,
		180, 180, 180, 180, 180, 200, 200, 200, 200, 200
	};

	private static final int[] GOAL_LEVELS = { 20, 20, 40, 80 };
	private static final int[] STARTING_SPARE_TIME = { 60, 40, 20, 10 };
	private static final int[] STALLING_LIMITS = { 150, 300 };
	private static final int[] PENALTY_MULTIPLIER = { 1, 1, 2, 4 };
	private static final int BASE_PENALTY = 5;

	private static final int RESTRICTION_NONE = 0,
	                         RESTRICTION_NO_HARD_DROP = 1,
	                         RESTRICTION_NO_STALLING = 2;

	private int difficulty;
	private int currentLineTarget;
	private int currentTimeTarget;
	private int spareTime;
	private int restriction;
	private Random localRandom;
	private ShakingText shakingText;

	// region Mode Block

	/**
	 * Returns the mode name.
	 * @return <code>String</code>
	 */
	@Override
	public String getName() {
		return "CONSTANTRIS";
	}

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
		currentLineTarget = 0;
		currentTimeTarget = 0;
		spareTime = 0;
		restriction = RESTRICTION_NONE;

		rankingRank = -1;
		rankingScore = new int[RANKING_TYPE][RANKING_MAX];
		rankingLines = new int[RANKING_TYPE][RANKING_MAX];
		rankingTime = new int[RANKING_TYPE][RANKING_MAX];

		netPlayerInit(engine, playerID);

		if(owner.replayMode == false) {
			loadSetting(owner.modeConfig);
			loadRanking(owner.modeConfig, engine.ruleopt.strRuleName);
			version = CURRENT_VERSION;
		} else {
			loadSetting(owner.replayProp);
			if((version == 0) && (owner.replayProp.getProperty("constantris.endless", false) == true)) goaltype = 2;

			// NET: Load name
			netPlayerName = engine.owner.replayProp.getProperty(playerID + ".net.netPlayerName", "");
		}

		SoundLoader.importSound("res/se/zeroxfc/timereduce.wav", "timereduce");

		owner.backgroundStatus.bg = 0;
		engine.framecolor = GameEngine.FRAME_COLOR_YELLOW;
	}

	/*
	 * Called for initialization during "Ready" screen
	 */
	@Override
	public void startGame(GameEngine engine, int playerID) {
		localRandom = new Random(engine.randSeed + 1L);
		shakingText = new ShakingText(new Random(engine.randSeed + 2L));

		engine.statistics.level = 0;
		engine.statistics.levelDispAdd = 1;
		engine.b2bEnable = enableB2B;
		if(enableCombo == true) {
			engine.comboType = GameEngine.COMBO_TYPE_NORMAL;
		} else {
			engine.comboType = GameEngine.COMBO_TYPE_DISABLE;
		}
		engine.big = big;

		if(version >= 2) {
			engine.tspinAllowKick = enableTSpinKick;
			if(tspinEnableType == 0) {
				engine.tspinEnable = false;
			} else if(tspinEnableType == 1) {
				engine.tspinEnable = true;
			} else {
				engine.tspinEnable = true;
				engine.useAllSpinBonus = true;
			}
		} else {
			engine.tspinEnable = enableTSpin;
		}

		engine.spinCheckType = spinCheckType;
		engine.tspinEnableEZ = tspinEnableEZ;

		setSpeed(engine);

		if(netIsWatch) {
			owner.bgmStatus.bgm = BGMStatus.BGM_NOTHING;
		}
	}

	/**
	 * Set the gravity rate
	 * @param engine GameEngine
	 */
	public void setSpeed(GameEngine engine) {
		int lv = engine.statistics.level / 4;

		if(lv < 0) lv = 0;
		if(lv >= tableGravity.length) lv = tableGravity.length - 1;

		engine.speed.gravity = tableGravity[lv];
		engine.speed.denominator = tableDenominator[lv];

		currentLineTarget = engine.statistics.lines + GOAL_LINES[engine.statistics.level];
		currentTimeTarget = (engine.statistics.time - (engine.statistics.time % 60)) + getTimeLimit(GOAL_LINES[engine.statistics.level]);
	}

	@Override
	public boolean onGameOver(GameEngine engine, int playerID) {
		int penalty = BASE_PENALTY * PENALTY_MULTIPLIER[difficulty];

		if (spareTime > penalty && engine.ending == 0) {
			engine.lives++;
			spareTime -= penalty;
		}
		else spareTime = 0;

		return false;
	}

	@Override
	public void onLast(GameEngine engine, int playerID) {
		if (engine.statistics.time >= currentTimeTarget + 60) {
			if (engine.statistics.time % 60 == 0) {
				--spareTime;
				engine.playSE("timereduce");
			}
		}

		if (spareTime == 0 && engine.gameStarted) {
			engine.stat = GameEngine.STAT_GAMEOVER;
			engine.gameEnded();
		}

		scgettime++;
	}

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
		tspinEnableType = prop.getProperty("constantris.tspinEnableType", 1);
		enableTSpin = prop.getProperty("constantris.enableTSpin", true);
		enableTSpinKick = prop.getProperty("constantris.enableTSpinKick", true);
		spinCheckType = prop.getProperty("constantris.spinCheckType", 0);
		tspinEnableEZ = prop.getProperty("constantris.tspinEnableEZ", false);
		enableB2B = prop.getProperty("constantris.enableB2B", true);
		enableCombo = prop.getProperty("constantris.enableCombo", true);
		goaltype = prop.getProperty("constantris.gametype", 0);
		big = prop.getProperty("constantris.big", false);
		version = prop.getProperty("constantris.version", 0);
		difficulty = prop.getProperty("constantris.difficulty", DIFFICULTY_EASY);
	}

	/**
	 * Save settings to property file
	 * @param prop Property file
	 */
	private void saveSetting(CustomProperties prop) {
		prop.setProperty("constantris.tspinEnableType", tspinEnableType);
		prop.setProperty("constantris.enableTSpin", enableTSpin);
		prop.setProperty("constantris.enableTSpinKick", enableTSpinKick);
		prop.setProperty("constantris.spinCheckType", spinCheckType);
		prop.setProperty("constantris.tspinEnableEZ", tspinEnableEZ);
		prop.setProperty("constantris.enableB2B", enableB2B);
		prop.setProperty("constantris.enableCombo", enableCombo);
		prop.setProperty("constantris.gametype", goaltype);
		prop.setProperty("constantris.big", big);
		prop.setProperty("constantris.version", version);
		prop.setProperty("constantris.difficulty", difficulty);
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
				rankingScore[j][i] = prop.getProperty("constantris.ranking." + ruleName + "." + j + ".score." + i, 0);
				rankingLines[j][i] = prop.getProperty("constantris.ranking." + ruleName + "." + j + ".lines." + i, 0);
				rankingTime[j][i] = prop.getProperty("constantris.ranking." + ruleName + "." + j + ".time." + i, 0);
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
				prop.setProperty("constantris.ranking." + ruleName + "." + j + ".score." + i, rankingScore[j][i]);
				prop.setProperty("constantris.ranking." + ruleName + "." + j + ".lines." + i, rankingLines[j][i]);
				prop.setProperty("constantris.ranking." + ruleName + "." + j + ".time." + i, rankingTime[j][i]);
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

	// endregion

	// region Misc Block

	private int getTimeLimit(int nextLineGoal) {
		int base = localRandom.nextInt(16) + 20;
		int timeLimit = 60 * base;

		switch (difficulty) {
			case DIFFICULTY_EASY:
				timeLimit += 60 * 5;
				break;
			case DIFFICULTY_HARD:
				timeLimit -= 60 * 5;
				break;
			case DIFFICULTY_VERY_HARD:
				timeLimit -= 60 * 10;
				break;
			default:
				break;
		}

		double multiplier = nextLineGoal / 10d;

		if (restriction == RESTRICTION_NO_HARD_DROP) timeLimit *= 2;

		return (int)(timeLimit * multiplier);
	}

	// endregion
}
