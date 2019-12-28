package zeroxfc.nullpo.custom.modes;

import mu.nu.nullpo.game.component.BGMStatus;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.component.SpeedParam;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.util.CustomProperties;
import mu.nu.nullpo.util.GeneralUtil;

public class RollTraining extends MarathonModeBase {
	private static final int SPEED_TAP = 0,
	                         SPEED_TI = 1,
	                         SPEED_SETTING_COUNT = 2;

	private static final SpeedParam[] SPEED_SETTINGS = {
		new SpeedParam(), new SpeedParam()
	};

	/** Note: M-roll uses the lock flash values. */
	private static final int FADING_FRAMES = 300;

	static {
		// TAP settings
		SPEED_SETTINGS[0].gravity = -1;
		SPEED_SETTINGS[0].are = 14;
		SPEED_SETTINGS[0].areLine = 8;
		SPEED_SETTINGS[0].lockDelay = 17;
		SPEED_SETTINGS[0].lineDelay = 6;
		SPEED_SETTINGS[0].das = 8;

		// TI settings
		SPEED_SETTINGS[1].gravity = -1;
		SPEED_SETTINGS[1].are = 6;
		SPEED_SETTINGS[1].areLine = 6;
		SPEED_SETTINGS[1].lockDelay = 15;
		SPEED_SETTINGS[1].lineDelay = 6;
		SPEED_SETTINGS[1].das = 8;
	}

	/** Number of entries in rankings */
	public static final int RANKING_MAX = 10;

	/** Number of ranking types */
	public static final int RANKING_TYPE = 4;

	/** Number of game types */
	public static final int GAMETYPE_MAX = 4;

	private static final double[][] GRADE_INCREASES = {
		new double[] {
			0.04, 0.08, 0.12, 0.26
		},
		new double[] {
			0.1, 0.2, 0.3, 1.0
		}
	};

	private static final double[] CLEAR_GRADE_BONUS = {
		0.5, 1.6
	};

	private static final int[] TIME_LIMITS = {
		3694, 3238
	};

	/** Rankings' scores */
	public double[][] rankingGrade;

	/** Rankings' line counts */
	public int[][] rankingLines;

	/** Rankings' times */
	public int[][] rankingTime;

	private boolean useMRoll;
	private int usedSpeed;
	private boolean endless;
	private double tiGrade;
	private double tapGrade;
	private int lastGrade;
	private int timer;

	/*
	 * Mode name
	 */
	@Override
	public String getName() {
		return "ROLL TRAINING";
	}

	/*
	 * Initialization
	 */
	@Override
	public void playerInit(GameEngine engine, int playerID) {
		owner = engine.owner;
		receiver = engine.owner.receiver;
		scgettime = 0;
		lastevent = EVENT_NONE;
		lastb2b = false;
		lastpiece = 0;
		bgmlv = 0;

		useMRoll = true;
		usedSpeed = SPEED_TAP;
		endless = false;
		tiGrade = 0.0;
		tapGrade = 0.0;
		timer = 0;
		lastGrade = 0;

		rankingRank = -1;
		rankingGrade = new double[RANKING_TYPE][RANKING_MAX];
		rankingLines = new int[RANKING_TYPE][RANKING_MAX];
		rankingTime = new int[RANKING_TYPE][RANKING_MAX];

		enableB2B = true;
		enableCombo = false;
		enableTSpin = false;
		tspinEnableEZ = false;
		big = false;

		netPlayerInit(engine, playerID);

		if(!owner.replayMode) {
			loadSetting(owner.modeConfig);
			loadRanking(owner.modeConfig, engine.ruleopt.strRuleName);
			version = CURRENT_VERSION;
		} else {
			loadSetting(owner.replayProp);
			if((version == 0) && (owner.replayProp.getProperty("rollTraining.endless", false))) goaltype = 2;

			// NET: Load name
			netPlayerName = engine.owner.replayProp.getProperty(playerID + ".net.netPlayerName", "");
		}

		engine.owner.backgroundStatus.bg = startlevel;
		engine.framecolor = usedSpeed == SPEED_TAP ? GameEngine.FRAME_COLOR_GRAY : GameEngine.FRAME_COLOR_BLUE;
	}

	/**
	 * Set the gravity rate
	 * @param engine GameEngine
	 */
	public void setSpeed(GameEngine engine) {
		engine.speed.copy(SPEED_SETTINGS[usedSpeed]);
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
		else if(!engine.owner.replayMode) {
			// Configuration changes
			int change = updateCursor(engine, 3, playerID);

			if(change != 0) {
				engine.playSE("change");

				switch(engine.statc[2]) {
					case 0:
						usedSpeed += change;
						if(usedSpeed >= SPEED_SETTING_COUNT) usedSpeed = 0;
						else if (usedSpeed < 0) usedSpeed = SPEED_SETTING_COUNT - 1;
						engine.framecolor = usedSpeed == SPEED_TAP ? GameEngine.FRAME_COLOR_GRAY : GameEngine.FRAME_COLOR_BLUE;
						break;
					case 1:
						useMRoll = !useMRoll;
						break;
					case 2:
						endless = !endless;
						break;
					case 3:
						startlevel += change;
						if (startlevel > 19) startlevel = 0;
						else if (startlevel < 0) startlevel = 19;
						engine.owner.backgroundStatus.bg = startlevel;
						break;
					default:
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

			return engine.statc[3] < 60;
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
			drawMenu(engine, playerID, receiver, 0, EventReceiver.COLOR_BLUE, 0,
					"TYPE", usedSpeed == SPEED_TAP ? "TAP" : "TI",
					"M-ROLL", GeneralUtil.getONorOFF(useMRoll),
					"ENDLESS", GeneralUtil.getONorOFF(endless),
					"BACKGROUND", String.valueOf(startlevel));
		}
	}

	/*
	 * Called for initialization during "Ready" screen
	 */
	@Override
	public void startGame(GameEngine engine, int playerID) {
		engine.statistics.level = 0;
		engine.b2bEnable = enableB2B;
		if(enableCombo) {
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
		timer = TIME_LIMITS[usedSpeed];
		engine.blockHidden = useMRoll ? engine.ruleopt.lockflash : FADING_FRAMES;
		engine.blockHiddenAnim = !useMRoll;
		engine.blockOutlineType = useMRoll ? GameEngine.BLOCK_OUTLINE_NORMAL : GameEngine.BLOCK_OUTLINE_NONE;
		owner.bgmStatus.bgm = BGMStatus.BGM_ENDING1;

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

		receiver.drawScoreFont(engine, playerID, 0, 0, getName(), EventReceiver.COLOR_RED);

		StringBuilder sb = new StringBuilder("(");
		if (endless) sb.append("ENDLESS ");

		if (usedSpeed == SPEED_TAP) sb.append("TAP ");
		else sb.append("TI ");

		if (useMRoll) sb.append("M-");
		else sb.append("FADING ");

		sb.append("ROLL)");

		receiver.drawScoreFont(engine, playerID, 0, 1, sb.toString(), EventReceiver.COLOR_RED);

		if( (engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (!owner.replayMode)) ) {
			if((!owner.replayMode) && (!big) && (engine.ai == null)) {
				float scale = (receiver.getNextDisplayType() == 2) ? 0.5f : 1.0f;
				int topY = (receiver.getNextDisplayType() == 2) ? 6 : 4;
				receiver.drawScoreFont(engine, playerID, 3, topY-1, "SCORE  LINE TIME", EventReceiver.COLOR_BLUE, scale);

				for(int i = 0; i < RANKING_MAX; i++) {
					receiver.drawScoreFont(engine, playerID,  0, topY+i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW, scale);

					int color;
					if (rankingRank == i) color = EventReceiver.COLOR_RED;
					else {
						if (usedSpeed == SPEED_TAP) {
							color = ((!useMRoll && rankingTime[endless ? usedSpeed + 2 : usedSpeed][i] >= TIME_LIMITS[0]) || rankingLines[endless ? usedSpeed + 2 : usedSpeed][i] >= 32) ? EventReceiver.COLOR_ORANGE : EventReceiver.COLOR_GREEN;
						} else {
							color = rankingTime[endless ? usedSpeed + 2 : usedSpeed][i] >= TIME_LIMITS[1] ? EventReceiver.COLOR_ORANGE : EventReceiver.COLOR_GREEN;
						}
					}

					String gText;
					if (usedSpeed == SPEED_TAP) {
						gText = !useMRoll ? "S9" : (rankingGrade[endless ? usedSpeed + 2 : usedSpeed][i] >= 1.0 ? "GM" : "M");
					} else {
						gText = "+" + String.format("%.2f", rankingGrade[endless ? usedSpeed + 2 : usedSpeed][i]);
					}

					receiver.drawScoreFont(engine, playerID,  3, topY+i, gText, color, scale);
					receiver.drawScoreFont(engine, playerID, 10, topY+i, String.valueOf(rankingLines[endless ? usedSpeed + 2 : usedSpeed][i]), (i == rankingRank), scale);
					receiver.drawScoreFont(engine, playerID, 15, topY+i, GeneralUtil.getTime(rankingTime[endless ? usedSpeed + 2 : usedSpeed][i]), (i == rankingRank), scale);
				}
			}
		} else {
			receiver.drawScoreFont(engine, playerID, 0, 3, usedSpeed == SPEED_TAP ? "GRADE" : "BONUS", EventReceiver.COLOR_BLUE);
			String grade;
			int gc;

			if (usedSpeed == SPEED_TAP) {
				grade = !useMRoll ? "S9" : (tapGrade >= 1.0 ? "GM" : "M");
				gc = ((!useMRoll && engine.statistics.time >= TIME_LIMITS[0]) || engine.statistics.lines >= 32) ? EventReceiver.COLOR_ORANGE : EventReceiver.COLOR_GREEN;
			} else {
				grade = "+" + String.format("%.2f", tiGrade);
				gc = engine.statistics.time >= TIME_LIMITS[1] ? EventReceiver.COLOR_ORANGE : EventReceiver.COLOR_GREEN;
			}

			receiver.drawScoreFont(engine, playerID, 0, 4, grade, gc);

			receiver.drawScoreFont(engine, playerID, 0, 6, "LINE", EventReceiver.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 7, engine.statistics.lines + "");

			receiver.drawScoreFont(engine, playerID, 0, 9, "TIME", EventReceiver.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 10, GeneralUtil.getTime(engine.statistics.time));

			if (!endless) {
				receiver.drawScoreFont(engine, playerID, 0, 12, "REMAINING", EventReceiver.COLOR_YELLOW);
				receiver.drawScoreFont(engine, playerID, 0, 13, GeneralUtil.getTime(Math.max(timer, 0)), timer <= 600 && (timer / 2 % 2 == 0));
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
		lastGrade = usedSpeed == SPEED_TAP ? (int)tapGrade : (int)tiGrade;
	}

	/*
	 * Called after every frame
	 */
	@Override
	public void onLast(GameEngine engine, int playerID) {
		++scgettime;

		if (engine.timerActive) {
			if (timer == 0) {
				if (usedSpeed == SPEED_TI) tiGrade += CLEAR_GRADE_BONUS[useMRoll ? 1 : 0];
				else {
					tapGrade += 1.0;
				}

				if (!endless) {
					engine.stat = GameEngine.STAT_EXCELLENT;
					engine.resetStatc();
					engine.resetFieldVisible();
					engine.gameEnded();
				}
			}
			if (timer > -1) --timer;
			if (!endless && timer <= 600 && timer > 0 && timer % 60 == 0) engine.playSE("countdown");
		}

		int cg = usedSpeed == SPEED_TAP ? (int)tapGrade : (int)tiGrade;
		if (cg > lastGrade) engine.playSE("gradeup");

		// Meter
		int lt = timer;
		if (lt < 0) lt = 0;
		double factor = ((double) lt / (double) TIME_LIMITS[usedSpeed]);
		if (!endless) engine.meterValue = (int)(factor * receiver.getMeterMax(engine));
		else engine.meterValue = receiver.getMeterMax(engine);
		engine.meterColor = GameEngine.METER_COLOR_GREEN;
		if(factor >= 0.25 && !endless) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
		if(factor >= 0.5 && !endless) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
		if(factor >= 0.75 && !endless) engine.meterColor = GameEngine.METER_COLOR_RED;
	}

	/*
	 * Calculate score
	 */
	@Override
	public void calcScore(GameEngine engine, int playerID, int lines) {
		if(lines == 1) {
			if (usedSpeed == SPEED_TI) tiGrade += GRADE_INCREASES[useMRoll ? 1 : 0][lines - 1]; // 1列
			lastevent = EVENT_SINGLE;
		} else if(lines == 2) {
			if (usedSpeed == SPEED_TI) tiGrade += GRADE_INCREASES[useMRoll ? 1 : 0][lines - 1]; // 2列
			lastevent = EVENT_DOUBLE;
		} else if(lines == 3) {
			if (usedSpeed == SPEED_TI) tiGrade += GRADE_INCREASES[useMRoll ? 1 : 0][lines - 1]; // 3列
			lastevent = EVENT_TRIPLE;
		} else if(lines >= 4) {
			// 4 lines
			if (usedSpeed == SPEED_TI) tiGrade += GRADE_INCREASES[useMRoll ? 1 : 0][lines - 1];
			lastevent = EVENT_FOUR;
		}

		lastb2b = engine.b2b;

		// All clear
		if((lines >= 1) && (engine.field.isEmpty())) {
			engine.playSE("bravo");
		}

		// Add to score
		if(lines > 0) {
			lastpiece = engine.nowPieceObject.id;
			scgettime = 0;
		}
	}

	/*
	 * Soft drop
	 */
	@Override
	public void afterSoftDropFall(GameEngine engine, int playerID, int fall) {
		// NOTHING
	}

	/*
	 * Hard drop
	 */
	@Override
	public void afterHardDropFall(GameEngine engine, int playerID, int fall) {
		// NOTHING ...yet
	}

	/*
	 * Render results screen
	 */
	@Override
	public void renderResult(GameEngine engine, int playerID) {
		String grade;
		int gc;

		if (usedSpeed == SPEED_TAP) {
			grade = tapGrade >= 1.0 ? "GM" : "M";
			gc = ((!useMRoll && engine.statistics.time >= TIME_LIMITS[0]) || engine.statistics.lines >= 32) ? EventReceiver.COLOR_ORANGE : EventReceiver.COLOR_GREEN;
		} else {
			grade = "+" + String.format("%.2f", tiGrade);
			gc = engine.statistics.time >= TIME_LIMITS[1] ? EventReceiver.COLOR_ORANGE : EventReceiver.COLOR_GREEN;
		}

		receiver.drawMenuFont(engine, playerID, 0, 0, usedSpeed == SPEED_TAP ? "GRADE" : "BONUS", EventReceiver.COLOR_BLUE);
		receiver.drawMenuFont(engine, playerID, 0, 1, String.format("%10s", grade), gc);

		drawResultStats(engine, playerID, receiver, 2, EventReceiver.COLOR_BLUE,
				STAT_LINES, STAT_TIME, STAT_LPM);
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
		if((!owner.replayMode) && (!big) && (engine.ai == null)) {
			updateRanking(usedSpeed == SPEED_TAP ? tapGrade : tiGrade, engine.statistics.lines, engine.statistics.time, endless ? usedSpeed + 2 : usedSpeed);

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
		startlevel = prop.getProperty("rollTraining.startlevel", startlevel);
		usedSpeed = prop.getProperty("rollTraining.usedSpeed", usedSpeed);
		useMRoll = prop.getProperty("rollTraining.useMRoll", useMRoll);
		endless = prop.getProperty("rollTraining.endlessMode", endless);
		version = prop.getProperty("rollTraining.version", 0);
	}

	/**
	 * Save settings to property file
	 * @param prop Property file
	 */
	private void saveSetting(CustomProperties prop) {
		prop.setProperty("rollTraining.startlevel", startlevel);
		prop.setProperty("rollTraining.usedSpeed", usedSpeed);
		prop.setProperty("rollTraining.useMRoll", useMRoll);
		prop.setProperty("rollTraining.endlessMode", endless);
		prop.setProperty("rollTraining.version", version);
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
				rankingGrade[j][i] = prop.getProperty("rollTraining.ranking." + ruleName + "." + j + ".grade." + i, 0.0);
				rankingLines[j][i] = prop.getProperty("rollTraining.ranking." + ruleName + "." + j + ".lines." + i, 0);
				rankingTime[j][i] = prop.getProperty("rollTraining.ranking." + ruleName + "." + j + ".time." + i, 0);
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
				prop.setProperty("rollTraining.ranking." + ruleName + "." + j + ".grade." + i, rankingGrade[j][i]);
				prop.setProperty("rollTraining.ranking." + ruleName + "." + j + ".lines." + i, rankingLines[j][i]);
				prop.setProperty("rollTraining.ranking." + ruleName + "." + j + ".time." + i, rankingTime[j][i]);
			}
		}
	}

	/**
	 * Update rankings
	 * @param sc Score
	 * @param li Lines
	 * @param time Time
	 */
	private void updateRanking(double sc, int li, int time, int type) {
		rankingRank = checkRanking(sc, li, time, type);

		if(rankingRank != -1) {
			// Shift down ranking entries
			for(int i = RANKING_MAX - 1; i > rankingRank; i--) {
				rankingGrade[type][i] = rankingGrade[type][i - 1];
				rankingLines[type][i] = rankingLines[type][i - 1];
				rankingTime[type][i] = rankingTime[type][i - 1];
			}

			// Add new data
			rankingGrade[type][rankingRank] = sc;
			rankingLines[type][rankingRank] = li;
			rankingTime[type][rankingRank] = time;
		}
	}

	private int getClear(int type, int time, int lines) {
		if (!useMRoll || (type & 1) == 1) return (time >= TIME_LIMITS[type & 1]) ? 1 : 0;
		else return lines >= 32 ? 1 : 0;
	}

	/**
	 * Calculate ranking position
	 * @param sc Score
	 * @param li Lines
	 * @param time Time
	 * @return Position (-1 if unranked)
	 */
	private int checkRanking(double sc, int li, int time, int type) {
		for(int i = 0; i < RANKING_MAX; i++) {
			if (getClear(type, time, li) > getClear(type, rankingTime[type][i], rankingLines[type][i])) {
				return i;
			} else if(getClear(type, time, li) == getClear(type, rankingTime[type][i], rankingLines[type][i]) && sc > rankingGrade[type][i]) {
				return i;
			} else if(getClear(type, time, li) == getClear(type, rankingTime[type][i], rankingLines[type][i]) && (sc == rankingGrade[type][i]) && (time > rankingTime[type][i])) {
				return i;
			} else if(getClear(type, time, li) == getClear(type, rankingTime[type][i], rankingLines[type][i]) && (sc == rankingGrade[type][i]) && (time == rankingTime[type][i]) && (li > rankingLines[type][i])) {
				return i;
			}
		}

		return -1;
	}
}
