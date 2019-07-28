package zeroxfc.nullpo.custom.modes;

import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.util.CustomProperties;

public class SingleDeathMarathon extends MarathonModeBase {
	/*
	 * Mode name
	 */
	@Override
	public String getName() {
		return "SINGLE DEATH MARATHON";
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
			if((version == 0) && (owner.replayProp.getProperty("singledeath.endless", false) == true)) goaltype = 2;

			// NET: Load name
			netPlayerName = engine.owner.replayProp.getProperty(playerID + ".net.netPlayerName", "");
		}

		engine.owner.backgroundStatus.bg = startlevel;
		engine.framecolor = GameEngine.FRAME_COLOR_GREEN;
	}

	private void loadSetting(CustomProperties prop) {
		startlevel = prop.getProperty("singledeath.startlevel", 0);
		tspinEnableType = prop.getProperty("singledeath.tspinEnableType", 1);
		enableTSpin = prop.getProperty("singledeath.enableTSpin", true);
		enableTSpinKick = prop.getProperty("singledeath.enableTSpinKick", true);
		spinCheckType = prop.getProperty("singledeath.spinCheckType", 0);
		tspinEnableEZ = prop.getProperty("singledeath.tspinEnableEZ", false);
		enableB2B = prop.getProperty("singledeath.enableB2B", true);
		enableCombo = prop.getProperty("singledeath.enableCombo", true);
		goaltype = prop.getProperty("singledeath.gametype", 0);
		// big = prop.getProperty("singledeath.big", false);
		version = prop.getProperty("singledeath.version", 0);
	}

	/**
	 * Save settings to property file
	 * @param prop Property file
	 */
	private void saveSetting(CustomProperties prop) {
		prop.setProperty("singledeath.startlevel", startlevel);
		prop.setProperty("singledeath.tspinEnableType", tspinEnableType);
		prop.setProperty("singledeath.enableTSpin", enableTSpin);
		prop.setProperty("singledeath.enableTSpinKick", enableTSpinKick);
		prop.setProperty("singledeath.spinCheckType", spinCheckType);
		prop.setProperty("singledeath.tspinEnableEZ", tspinEnableEZ);
		prop.setProperty("singledeath.enableB2B", enableB2B);
		prop.setProperty("singledeath.enableCombo", enableCombo);
		prop.setProperty("singledeath.gametype", goaltype);
		// prop.setProperty("singledeath.big", big);
		prop.setProperty("singledeath.version", version);
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
				rankingScore[j][i] = prop.getProperty("singledeath.ranking." + ruleName + "." + j + ".score." + i, 0);
				rankingLines[j][i] = prop.getProperty("singledeath.ranking." + ruleName + "." + j + ".lines." + i, 0);
				rankingTime[j][i] = prop.getProperty("singledeath.ranking." + ruleName + "." + j + ".time." + i, 0);
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
				prop.setProperty("singledeath.ranking." + ruleName + "." + j + ".score." + i, rankingScore[j][i]);
				prop.setProperty("singledeath.ranking." + ruleName + "." + j + ".lines." + i, rankingLines[j][i]);
				prop.setProperty("singledeath.ranking." + ruleName + "." + j + ".time." + i, rankingTime[j][i]);
			}
		}
	}
}
