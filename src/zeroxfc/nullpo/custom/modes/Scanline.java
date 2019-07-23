package zeroxfc.nullpo.custom.modes;

import zeroxfc.nullpo.custom.libs.FieldExtension;
import zeroxfc.nullpo.custom.libs.Interpolation;
import zeroxfc.nullpo.custom.libs.ResourceHolderCustomAssetExtension;
import zeroxfc.nullpo.custom.libs.SoundLoader;

import mu.nu.nullpo.game.component.BGMStatus;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.util.CustomProperties;
import mu.nu.nullpo.util.GeneralUtil;

public class Scanline extends MarathonModeBase {
	// Scan grace max
	private static final int[] SCAN_GRACE_LIMITS = { 600, 1200, 1800 };
	
	// Scan setting names
	private static final String[] SCAN_GRACE_LIMIT_NAMES = { "SHORT", "NORMAL", "LONG" };
	
	// Line clear names
	private static final String[] LINE_CLEAR_NAMES = {
		"SINGLE",       "DOUBLE",        "TRIPLE",       "TETRIS",        "PENTRIS",
		"HEXATRIS",     "HEPTATRIS",     "OCTRIS",       "NONATRIS",      "DECATRIS",
		"HENDECATRIS",  "DODECATRIS",    "TRIDECATRIS",  "TETRADECATRIS", "PENTADECATRIS",
		"HEXADECATRIS", "HEPTADECATRIS", "OCTADECATRIS", "NONADECATRIS",  "ICOSATRIS",
		"HENICOSATRIS", "DOICOSATRIS",   "TRIAICOSATRIS"
	};
	
	// Max scan speeds
	private static final int MAX_SCANSPEEDS = 3;
	
	// Scanner advance speed
	private static final int SCANNER_ADVANCE_DELAY = 4;
	
	// Scan speed setting
	private int scanSpeed;
	
	// Current scan timer
	private int scanTimer;
	
	/** Rankings' scores */
	public int[][][] rankingScore;

	/** Rankings' line counts */
	public int[][][] rankingLines;

	/** Rankings' times */
	public int[][][] rankingTime;
	
	// Lines cleared on the last scan
	private int lastClearAmount;
	
	// Locations of found lines
	private boolean[] lineLocations;
	
	// Scanner location
	private int scannerLocation;
	
	// Scanner advancement timer
	private int scannerAdvanceTimer;
	
	// Has game been interrupted
	private boolean interrupted;
	
	// Score before increase
	private int scoreBeforeIncrease;
	
	// Custom Scanner Graphic
	private ResourceHolderCustomAssetExtension customHolder;
	
	private int sx, sy;
	
	@Override
	public String getName() {
		return "SCANLINE";
	}
	
	/*
	 * TODO:
	 * 
	 * - Override line clear method so that it only occurs when the scan time expires. (Possibly use FieldExtension.)
	 * - Find out how to interrupt and halt game-play and resume it later. STAT_CUSTOM!!!
	 * - Import the custom sound set into the game in playerInit().
	 */
	
	/*
	 * Initialization
	 */
	@Override
	public void playerInit(GameEngine engine, int playerID) {
		SoundLoader.loadSoundset(SoundLoader.LOADTYPE_SCANNER);
		
		owner = engine.owner;
		receiver = engine.owner.receiver;
		lastscore = 0;
		scgettime = 0;
		lastevent = EVENT_NONE;
		lastb2b = false;
		lastcombo = 0;
		lastpiece = 0;
		bgmlv = 0;
		
		scanSpeed = 0;
		scanTimer = 0;
		lastClearAmount = 0;
		scannerLocation = 0;
		scannerAdvanceTimer = 0;
		interrupted = false;
		scoreBeforeIncrease = 0;
		
		rankingRank = -1;
		rankingScore = new int[MAX_SCANSPEEDS][RANKING_TYPE][RANKING_MAX];
		rankingLines = new int[MAX_SCANSPEEDS][RANKING_TYPE][RANKING_MAX];
		rankingTime = new int[MAX_SCANSPEEDS][RANKING_TYPE][RANKING_MAX];

		netPlayerInit(engine, playerID);

		if(owner.replayMode == false) {
			loadSetting(owner.modeConfig);
			loadRanking(owner.modeConfig, engine.ruleopt.strRuleName);
			version = CURRENT_VERSION;
		} else {
			loadSetting(owner.replayProp);
			if((version == 0) && (owner.replayProp.getProperty("scanline.endless", false) == true)) goaltype = 2;

			// NET: Load name
			netPlayerName = engine.owner.replayProp.getProperty(playerID + ".net.netPlayerName", "");
		}

		engine.owner.backgroundStatus.bg = startlevel;
		engine.framecolor = GameEngine.FRAME_COLOR_BLUE;
		
		engine.tspinEnable = false;
		engine.lineGravityType = GameEngine.LINE_GRAVITY_CASCADE;
		engine.comboType = GameEngine.COMBO_TYPE_DISABLE;
		
		customHolder = new ResourceHolderCustomAssetExtension(engine);
		customHolder.loadImage("res/graphics/scanner.png", "scanner");
		
		sx = 0;
		sy = 0;
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
			drawMenu(engine, playerID, receiver, 0, EventReceiver.COLOR_BLUE, 0,
					"LEVEL", String.valueOf(startlevel + 1),
					"B2B", GeneralUtil.getONorOFF(enableB2B),
					"GOAL",  (goaltype == 2) ? "ENDLESS" : tableGameClearLines[goaltype] + " LINES",
					"BIG", GeneralUtil.getONorOFF(big),
					"SCAN SPEED", SCAN_GRACE_LIMIT_NAMES[scanSpeed]);
		}
	}
	
	/*
	 * Called for initialization during "Ready" screen
	 */
	@Override
	public void startGame(GameEngine engine, int playerID) {
		engine.statistics.level = startlevel;
		engine.statistics.levelDispAdd = 1;
		
		sx = 4 + receiver.getFieldDisplayPositionX(engine, playerID);
		sy = 52 + receiver.getFieldDisplayPositionY(engine, playerID) + (16 * engine.field.getHeight());
		
		engine.b2bEnable = enableB2B;
		engine.big = big;
		interrupted = false;
		scoreBeforeIncrease = 0;
		
		scanTimer = 0;
		scannerAdvanceTimer = 0;
		lastClearAmount = 0;
		lineLocations = new boolean[engine.field.getHeight() + engine.field.getHiddenHeight()];
		setLLFalse();
		scannerLocation = engine.field.getHeight() - 1;

		setSpeed(engine);

		if(netIsWatch) {
			owner.bgmStatus.bgm = BGMStatus.BGM_NOTHING;
		}
	}
	
	/*
	 * Render score
	 */
	@Override
	public void renderLast(GameEngine engine, int playerID) {
		if(owner.menuOnly) return;

		receiver.drawScoreFont(engine, playerID, 0, 0, getName() + " " + SCAN_GRACE_LIMIT_NAMES[scanSpeed] + " SCAN", EventReceiver.COLOR_BLUE);

		if(tableGameClearLines[goaltype] == -1) {
			receiver.drawScoreFont(engine, playerID, 0, 1, "(ENDLESS GAME)", EventReceiver.COLOR_BLUE);
		} else {
			receiver.drawScoreFont(engine, playerID, 0, 1, "(" + tableGameClearLines[goaltype] + " LINES GAME)", EventReceiver.COLOR_BLUE);
		}

		if( (engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (owner.replayMode == false)) ) {
			if((owner.replayMode == false) && (big == false) && (engine.ai == null)) {
				float scale = (receiver.getNextDisplayType() == 2) ? 0.5f : 1.0f;
				int topY = (receiver.getNextDisplayType() == 2) ? 6 : 4;
				receiver.drawScoreFont(engine, playerID, 3, topY-1, "SCORE  LINE TIME", EventReceiver.COLOR_BLUE, scale);

				for(int i = 0; i < RANKING_MAX; i++) {
					receiver.drawScoreFont(engine, playerID,  0, topY+i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW, scale);
					String s = String.valueOf(rankingScore[scanSpeed][goaltype][i]);
					receiver.drawScoreFont(engine, playerID, (s.length() > 6) ? 6 : 3, (s.length() > 6) ? (topY+i) * 2 : (topY+i), s, (i == rankingRank), (s.length() > 6) ? scale * 0.5f : scale);
					receiver.drawScoreFont(engine, playerID, 10, topY+i, String.valueOf(rankingLines[scanSpeed][goaltype][i]), (i == rankingRank), scale);
					receiver.drawScoreFont(engine, playerID, 15, topY+i, GeneralUtil.getTime(rankingTime[scanSpeed][goaltype][i]), (i == rankingRank), scale);
				}
			}
		} else {
			receiver.drawScoreFont(engine, playerID, 0, 3, "SCORE", EventReceiver.COLOR_BLUE);
			String strScore;
			if((lastscore == 0) || (scgettime >= 120)) {
				strScore = String.valueOf(engine.statistics.score);
			} else {
				strScore = String.valueOf(Interpolation.lerp(scoreBeforeIncrease, engine.statistics.score, (scgettime / 120.0)) + "(+" + String.valueOf(lastscore) + ")");
			}
			receiver.drawScoreFont(engine, playerID, 0, 4, strScore);

			receiver.drawScoreFont(engine, playerID, 0, 6, "LINE", EventReceiver.COLOR_BLUE);
			if((engine.statistics.level >= 19) && (tableGameClearLines[goaltype] < 0))
				receiver.drawScoreFont(engine, playerID, 0, 7, engine.statistics.lines + "");
			else
				receiver.drawScoreFont(engine, playerID, 0, 7, engine.statistics.lines + "/" + ((engine.statistics.level + 1) * 10));

			receiver.drawScoreFont(engine, playerID, 0, 9, "LEVEL", EventReceiver.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 10, String.valueOf(engine.statistics.level + 1));

			receiver.drawScoreFont(engine, playerID, 0, 12, "TIME", EventReceiver.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 13, GeneralUtil.getTime(engine.statistics.time));
			
			int remain = SCAN_GRACE_LIMITS[scanSpeed] - scanTimer;
			if (remain < 0) remain = 0;
			
			receiver.drawScoreFont(engine, playerID, 0, 15, "UNTIL NEXT SCAN", EventReceiver.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 16, GeneralUtil.getTime(remain), (remain <= 300));

			if((lastClearAmount > 0) && (scgettime < 120)) {
				int d = lastClearAmount - 1;
				String h;
				if (d < 23) {
					h = LINE_CLEAR_NAMES[d];
				}
				else {
					h = String.valueOf(lastClearAmount) + "-TRIS";
				}
				int offset = 2 - ((h.length() - 6) / 2);
				if (offset < -2) offset = -2;
				receiver.drawMenuFont(engine, playerID, offset, 21, h, (lastClearAmount < 4) ? EventReceiver.COLOR_DARKBLUE : (lastb2b) ? EventReceiver.COLOR_RED : EventReceiver.COLOR_ORANGE);
			}
			
			if (engine.timerActive && engine.stat == GameEngine.STAT_CUSTOM) {
				int scanLength = engine.field.getHeight() * (SCANNER_ADVANCE_DELAY + scanSpeed);
				int locY = sy - (int)((16 * engine.field.getHeight()) * ((float)engine.statc[0] / scanLength)) - 16;
				
				for (int i = 0; i < lineLocations.length; i++) {
					if (lineLocations[i]) receiver.drawMenuFont(engine, playerID, 0, i-3, new String(new char[engine.field.getWidth()]).replace("\0", "e"), EventReceiver.COLOR_RED);
				}
				
				for (int i = 0; i < engine.field.getWidth(); i++) {
					int srcX = 16;
					if (i == 0) srcX = 0;
					if (i == engine.field.getWidth() - 1) srcX = 32;
					customHolder.drawImage(engine, "scanner", sx + (i * 16), locY, srcX, 0, 16, 16, 255, 255, 255, scannerLocation >= 0 ? 255 : 128, 1.0f);
				}
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
	
	@Override
	public void onFirst(GameEngine engine, int playerID) {
		if ((engine.stat == GameEngine.STAT_MOVE || engine.stat == GameEngine.STAT_ARE) && engine.timerActive) {
			scanTimer++;
		}
		
		// Meter
		double scv = scanTimer;
		if (scv > SCAN_GRACE_LIMITS[scanSpeed]) scv = SCAN_GRACE_LIMITS[scanSpeed];
		double h = (scv / (double)SCAN_GRACE_LIMITS[scanSpeed]);
		engine.meterValue = (int)(h * receiver.getMeterMax(engine));
		engine.meterColor = GameEngine.METER_COLOR_GREEN;
		if(h >= 0.25) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
		if(h >= 0.5) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
		if(h >= 0.75) engine.meterColor = GameEngine.METER_COLOR_RED;
	}
	
	/*
	 * Called after every frame
	 */
	@Override
	public void onLast(GameEngine engine, int playerID) {
		scgettime++;
	}
	
	@Override
	public boolean onMove(GameEngine engine, int playerID) {
		interrupted = false;
		return false;
	}
	
	private void setLLFalse() {
		for (int i = 0; i < lineLocations.length; i++) {
			lineLocations[i] = false;
		}
	}
	
	@Override
	public boolean onCustom(GameEngine engine, int playerID) {
		if (engine.timerActive) {
			interrupted = true;
			
			if (scannerAdvanceTimer == 0) {
				boolean flag = FieldExtension.checkSingleLineNoFlag(engine.field, scannerLocation);
				lineLocations[scannerLocation + engine.field.getHiddenHeight()] = flag;
				if (flag) {
					engine.playSE("linescanned");
				}
			}
			
			scannerAdvanceTimer++;
			if (scannerAdvanceTimer >= (SCANNER_ADVANCE_DELAY + scanSpeed)) {
				scannerAdvanceTimer = 0;
				
				if (scannerLocation > (engine.field.getHiddenHeight() * -1)) {
					scannerLocation--;
					engine.playSE("linescannermove");
				} else {
					lineLocations = new boolean[engine.field.getHeight() + engine.field.getHiddenHeight()];
					scannerLocation = engine.field.getHeight() - 1;
					setLLFalse();
					scanTimer = 0;
					
					int lines = engine.field.checkLineNoFlag();
					engine.resetStatc();
					if (lines > 0) {
						engine.stat = GameEngine.STAT_LINECLEAR;
						return true;
					}
					else {
						engine.stat = GameEngine.STAT_ARE;
						return true;
					}
				}
			}
		}
		
		engine.statc[0]++;
		
		return true;
	}
	
	@Override
	public void pieceLocked(GameEngine engine, int playerID, int lines) {
		if (scanTimer >= SCAN_GRACE_LIMITS[scanSpeed] && lines == 0) {
			engine.stat = GameEngine.STAT_LINECLEAR;
		}
	}
	
	// Where the magic happens
	@Override
	public boolean onLineClear(GameEngine engine, int playerID) {		
		if (scanTimer >= SCAN_GRACE_LIMITS[scanSpeed]) {
			engine.stat = GameEngine.STAT_CUSTOM;
			engine.resetStatc();
			// engine.statCustom();
			return true;
		} else if (!interrupted) {
			engine.stat = GameEngine.STAT_ARE;
			engine.resetStatc();
			// engine.statARE();
			return true;
		} else {
			return false;
		}
	}
	
	/*
	 * Calculate score
	 */
	@Override
	public void calcScore(GameEngine engine, int playerID, int lines) {
		// Line clear bonus
		int pts = getLineScore(lines) * (engine.statistics.level + 1);
		lastClearAmount = lines;
		
		if (enableB2B) {
			lastb2b = engine.b2b;
			if (lines > 0 && lines < 4) {
				if (lastb2b) engine.playSE("b2b_end");
				lastb2b = false;
			}
			
			if (lastb2b) pts *= 1.5;
		}

		// All clear
		if((lines >= 1) && (engine.field.isEmpty())) {
			engine.playSE("bravo");
			pts += 1800 * (engine.statistics.level + 1);
		}
		
		if (lines >= 5) {
			engine.playSE("erase4");
		}

		// Add to score
		if(pts > 0) {
			lastscore = pts;
			lastpiece = engine.nowPieceObject.id;
			scgettime = 0;
			scoreBeforeIncrease = engine.statistics.score;
			if(lines >= 1) engine.statistics.scoreFromLineClear += pts;
			else engine.statistics.scoreFromOtherBonus += pts;
			engine.statistics.score += pts;
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
		//engine.meterValue = ((engine.statistics.lines % 10) * receiver.getMeterMax(engine)) / 9;
		//engine.meterColor = GameEngine.METER_COLOR_GREEN;
		//if(engine.statistics.lines % 10 >= 4) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
		//if(engine.statistics.lines % 10 >= 6) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
		//if(engine.statistics.lines % 10 >= 8) engine.meterColor = GameEngine.METER_COLOR_RED;

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
			int change = updateCursor(engine, 4, playerID);

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
					enableB2B = !enableB2B;
					break;
				case 2:
					goaltype += change;
					if(goaltype < 0) goaltype = GAMETYPE_MAX - 1;
					if(goaltype > GAMETYPE_MAX - 1) goaltype = 0;

					if((startlevel > (tableGameClearLines[goaltype] - 1) / 10) && (tableGameClearLines[goaltype] >= 0)) {
						startlevel = (tableGameClearLines[goaltype] - 1) / 10;
						engine.owner.backgroundStatus.bg = startlevel;
					}
					break;
				case 3:
					big = !big;
					break;
				case 4:
					scanSpeed += change;
					if(scanSpeed < 0) scanSpeed = MAX_SCANSPEEDS - 1;
					if(scanSpeed > MAX_SCANSPEEDS - 1) scanSpeed = 0;
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
				rankingScore[scanSpeed][type][i] = rankingScore[scanSpeed][type][i - 1];
				rankingLines[scanSpeed][type][i] = rankingLines[scanSpeed][type][i - 1];
				rankingTime[scanSpeed][type][i] = rankingTime[scanSpeed][type][i - 1];
			}

			// Add new data
			rankingScore[scanSpeed][type][rankingRank] = sc;
			rankingLines[scanSpeed][type][rankingRank] = li;
			rankingTime[scanSpeed][type][rankingRank] = time;
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
			if(sc > rankingScore[scanSpeed][type][i]) {
				return i;
			} else if((sc == rankingScore[scanSpeed][type][i]) && (li > rankingLines[scanSpeed][type][i])) {
				return i;
			} else if((sc == rankingScore[scanSpeed][type][i]) && (li == rankingLines[scanSpeed][type][i]) && (time < rankingTime[scanSpeed][type][i])) {
				return i;
			}
		}

		return -1;
	}
	
	/**
	 * Load settings from property file
	 * @param prop Property file
	 */
	private void loadSetting(CustomProperties prop) {
		startlevel = prop.getProperty("scanline.startlevel", 0);
		tspinEnableType = prop.getProperty("scanline.tspinEnableType", 1);
		enableB2B = prop.getProperty("scanline.enableB2B", true);
		goaltype = prop.getProperty("scanline.gametype", 0);
		big = prop.getProperty("scanline.big", false);
		version = prop.getProperty("scanline.version", 0);
		scanSpeed = prop.getProperty("scanline.speed", 0);
	}

	/**
	 * Save settings to property file
	 * @param prop Property file
	 */
	private void saveSetting(CustomProperties prop) {
		prop.setProperty("scanline.startlevel", startlevel);
		prop.setProperty("scanline.tspinEnableType", tspinEnableType);
		prop.setProperty("scanline.enableB2B", enableB2B);
		prop.setProperty("scanline.gametype", goaltype);
		prop.setProperty("scanline.big", big);
		prop.setProperty("scanline.version", version);
		prop.setProperty("scanline.speed", scanSpeed);
	}

	/**
	 * Read rankings from property file
	 * @param prop Property file
	 * @param ruleName Rule name
	 */
	@Override
	protected void loadRanking(CustomProperties prop, String ruleName) {
		for (int h = 0; h < MAX_SCANSPEEDS; h++) {
			for(int i = 0; i < RANKING_MAX; i++) {
				for(int j = 0; j < GAMETYPE_MAX; j++) {
					rankingScore[h][j][i] = prop.getProperty("scanline.ranking." + ruleName + "." + h + "." + j + ".score." + i, 0);
					rankingLines[h][j][i] = prop.getProperty("scanline.ranking." + ruleName + "." + h + "." + j + ".lines." + i, 0);
					rankingTime[h][j][i] = prop.getProperty("scanline.ranking." + ruleName + "." + h + "." + j + ".time." + i, 0);
				}
			}
		}
	}

	/**
	 * Save rankings to property file
	 * @param prop Property file
	 * @param ruleName Rule name
	 */
	private void saveRanking(CustomProperties prop, String ruleName) {
		for (int h = 0; h < MAX_SCANSPEEDS; h++) {
			for(int i = 0; i < RANKING_MAX; i++) {
				for(int j = 0; j < GAMETYPE_MAX; j++) {
					prop.setProperty("scanline.ranking." + ruleName + "." + h + "." + j + ".score." + i, rankingScore[h][j][i]);
					prop.setProperty("scanline.ranking." + ruleName + "." + h + "." + j + ".lines." + i, rankingLines[h][j][i]);
					prop.setProperty("scanline.ranking." + ruleName + "." + h + "." + j + ".time." + i, rankingTime[h][j][i]);
				}
			}
		}
	}
		
	/*
	 * Gets score given lines scanned.
	 * Follows the 100, 300, 500, 800... pattern.
	 * 
	 * NOTE: int truncating is usefulAF ~0xFC
	 * 
	 * @param lines Number of Lines
	 */
	private int getLineScore(int lines) {
		int x = 1;
		if (lines == 0) x = 0;
		
		if (lines >= 2) {
			for (int i = 2; i <= lines; i++) {
				x += (i / 2) + 1;
			}
		}
		
		return x * 100;
	}
}
