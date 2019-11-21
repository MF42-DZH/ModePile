package zeroxfc.nullpo.custom.modes;

import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.util.CustomProperties;

import java.util.Random;

public class Constantris extends MarathonModeBase {
	private static final int DIFFICULTIES = 4;

	private static final int DIFFICULTY_EASY = 0,
	                         DIFFICULTY_NORMAL = 1,
	                         DIFFICULTY_HARD = 2,
	                         DIFFICULTY_VERY_HARD = 3;

	private static final int[] GOAL_LEVELS = { 10, 20, 40, 80 };
	private static final int[] STARTING_SPARE_TIME = { 60, 40, 20, 10 };
	private static final int[] PENALTY_MULTIPLIER = { 1, 1, 2, 4 };
	private static final int BASE_TOP_OUT_PENALTY = 5;

	private int difficulty;
	private int currentLineTarget;
	private int spareTime;
	private Random localRandom;

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

		engine.owner.backgroundStatus.bg = startlevel;
		engine.framecolor = GameEngine.FRAME_COLOR_GREEN;
	}

	@Override
	public boolean onGameOver(GameEngine engine, int playerID) {
		int penalty = BASE_TOP_OUT_PENALTY * PENALTY_MULTIPLIER[difficulty];

		if (spareTime > penalty && engine.ending == 0) {
			engine.lives++;
			spareTime -= penalty;
		}
		else spareTime = 0;

		return false;
	}

	@Override
	public void onLast(GameEngine engine, int playerID) {
		if (spareTime == 0) {
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
		startlevel = prop.getProperty("constantris.startlevel", 0);
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
	}

	/**
	 * Save settings to property file
	 * @param prop Property file
	 */
	private void saveSetting(CustomProperties prop) {
		prop.setProperty("constantris.startlevel", startlevel);
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
		return (int)(timeLimit * multiplier);
	}

	// endregion
}
