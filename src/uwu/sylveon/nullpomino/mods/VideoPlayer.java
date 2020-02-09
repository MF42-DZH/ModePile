/*
    Copyright (c) 2010, NullNoname
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:

        * Redistributions of source code must retain the above copyright
          notice, this list of conditions and the following disclaimer.
        * Redistributions in binary form must reproduce the above copyright
          notice, this list of conditions and the following disclaimer in the
          documentation and/or other materials provided with the distribution.
        * Neither the name of NullNoname nor the names of its
          contributors may be used to endorse or promote products derived from
          this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.
*/
package uwu.sylveon.nullpomino.mods;

import mu.nu.nullpo.game.component.BGMStatus;
import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.net.NetUtil;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.subsystem.mode.NetDummyMode;
import mu.nu.nullpo.util.CustomProperties;
import mu.nu.nullpo.util.GeneralUtil;
import uwu.sylveon.nullpomino.mods.lib.ByteArrayToBitArray;

/**
 * - Video Player -
 * by ry00001 with help from 0xFC963F18DC21
 */
public class VideoPlayer extends NetDummyMode {
	/** Current version */
	public static final int CURRENT_VERSION = 2;

	/** Fall velocity table (numerators) */
	public static final int[] tableGravity = { 1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1,  1, 465, 731, 1280, 1707,  -1,  -1,  -1};

	/** Fall velocity table (denominators) */
	public static final int[] tableDenominator = {63, 50, 39, 30, 22, 16, 12,  8,  6,  4,  3,  2,  1, 256, 256,  256,  256, 256, 256, 256};

	/** Line counts when BGM changes occur */
	public static final int[] tableBGMChange = {50, 100, 150, 200, -1};

	/** Line counts when game ending occurs */
	public static final int[] tableGameClearLines = {150, 200, -1};

	/** Number of entries in rankings */
	public static final int RANKING_MAX = 10;

	/** Number of ranking types */
	public static final int RANKING_TYPE = 3;

	/** Number of game types */
	public static final int GAMETYPE_MAX = 3;

	/** Most recent scoring event type constants */
	public static final int EVENT_NONE = 0,
							 EVENT_SINGLE = 1,
							 EVENT_DOUBLE = 2,
							 EVENT_TRIPLE = 3,
							 EVENT_FOUR = 4,
							 EVENT_TSPIN_ZERO_MINI = 5,
							 EVENT_TSPIN_ZERO = 6,
							 EVENT_TSPIN_SINGLE_MINI = 7,
							 EVENT_TSPIN_SINGLE = 8,
							 EVENT_TSPIN_DOUBLE_MINI = 9,
							 EVENT_TSPIN_DOUBLE = 10,
							 EVENT_TSPIN_TRIPLE = 11,
							 EVENT_TSPIN_EZ = 12;

	/** Drawing and event handling EventReceiver */
	public EventReceiver receiver;

	/** Most recent increase in score */
	public int lastscore;

	/** Time to display the most recent increase in score */
	public int scgettime;

	/** Most recent scoring event type */
	public int lastevent;

	/** True if most recent scoring event is a B2B */
	public boolean lastb2b;

	/** Combo count for most recent scoring event */
	public int lastcombo;

	/** Piece ID for most recent scoring event */
	public int lastpiece;

	/** Current BGM */
	public int bgmlv;

	/** Level at start time */
	public int startlevel;

	/** Flag for types of T-Spins allowed (0=none, 1=normal, 2=all spin) */
	public int tspinEnableType;

	/** Old flag for allowing T-Spins */
	public boolean enableTSpin;

	/** Flag for enabling wallkick T-Spins */
	public boolean enableTSpinKick;

	/** Spin check type (4Point or Immobile) */
	public int spinCheckType;

	/** Immobile EZ spin */
	public boolean tspinEnableEZ;

	/** Flag for enabling B2B */
	public boolean enableB2B;

	/** Flag for enabling combos */
	public boolean enableCombo;

	/** Game type */
	public int goaltype;

	/** Big */
	public boolean big;

	/** Version */
	public int version;

	/** Current round's ranking rank */
	public int rankingRank;

	/** Rankings' scores */
	public int[][] rankingScore;

	/** Rankings' line counts */
	public int[][] rankingLines;

	/** Rankings' times */
	public int[][] rankingTime;

	public int currentFrame = 0;
	public String path;
	public byte[] data;

	/*
	 * Mode name
	 */
	@Override
	public String getName() {
		return "VIDEO PLAYER";
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
			if((version == 0) && (owner.replayProp.getProperty("marathon.endless", false) == true)) goaltype = 2;

			// NET: Load name
			netPlayerName = engine.owner.replayProp.getProperty(playerID + ".net.netPlayerName", "");
		}

		engine.owner.backgroundStatus.bg = startlevel;
		engine.framecolor = GameEngine.FRAME_COLOR_GREEN;

		currentFrame = 0;
		path = ByteArrayToBitArray.getPath("graphics/video.bin");
		if (data == null) data = ByteArrayToBitArray.getFileBytes(path);
	}

	/**
	 * Set the gravity rate
	 * @param engine GameEngine
	 */
	public void setSpeed(GameEngine engine) {
		int lv = engine.statistics.level;

		if(lv < 0) lv = 0;
		if(lv >= tableGravity.length) lv = tableGravity.length - 1;

		engine.speed.gravity = tableGravity[lv];
		engine.speed.denominator = tableDenominator[lv];
	}

	/*
	 * Called at settings screen
	 */
	@Override
	public boolean onSetting(GameEngine engine, int playerID) {
		return false;
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
					"GOAL",  (goaltype == 2) ? "ENDLESS" : tableGameClearLines[goaltype] + " LINES",
					"BIG", GeneralUtil.getONorOFF(big));
		}
	}

	/*
	 * Called for initialization during "Ready" screen
	 */
	@Override
	public boolean onReady(GameEngine engine, int playerID) {
		engine.resetStatc();
		engine.gameActive = true;
		engine.gameStarted = true;
		engine.isInGame = true;
		engine.stat = GameEngine.STAT_CUSTOM;
		return true;
	}

	@Override
	public boolean onCustom(GameEngine engine, int playerID) {
		if (engine.statc[0] == 0) {
			owner.bgmStatus.bgm = 0;
			owner.bgmStatus.fadesw = false;
		}
		engine.statc[0]++;
		return true;
	}

	/*
	 * Render score
	 */
	@Override
	public void renderLast(GameEngine engine, int playerID) {
		if(owner.menuOnly) return;
		int skin = engine.getSkin();
		if (currentFrame > 7776) return;
		try {
			int place = 0;  // Out of 4800
			for (int i = 0; i < 600; i++) {
				int[] arr = ByteArrayToBitArray.convertByte(data[(currentFrame * 600) + i]);
				for (int b : arr) {
					int y = 8 * (place / 80);
					int x = 8 * (place % 80);

					// drawing calls go here.
					// check if b == 0 for black or b == 1 for white.
					receiver.drawSingleBlock(engine, playerID, x, y, Block.BLOCK_COLOR_GRAY, skin, false, (b == 0 ? 1.0f : 0.25f), 1.0f, 0.5f);

					place++;
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			engine.stat = GameEngine.STAT_GAMEOVER;
		}

		receiver.drawDirectFont(engine, playerID, 0, 0, String.format("%d", currentFrame));

	}

	/*
	 * Called after every frame
	 */
	@Override
	public void onLast(GameEngine engine, int playerID) {
		scgettime++;
		++currentFrame;
	}

	/*
	 * Calculate score
	 */
	@Override
	public void calcScore(GameEngine engine, int playerID, int lines) {
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
			lastscore = pts;
			lastpiece = engine.nowPieceObject.id;
			scgettime = 0;
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
		engine.statistics.scoreFromSoftDrop += fall;
		engine.statistics.score += fall;
	}

	/*
	 * Hard drop
	 */
	@Override
	public void afterHardDropFall(GameEngine engine, int playerID, int fall) {
		engine.statistics.scoreFromHardDrop += fall * 2;
		engine.statistics.score += fall * 2;
	}

	/*
	 * Render results screen
	 */
	@Override
	public void renderResult(GameEngine engine, int playerID) {
		drawResultStats(engine, playerID, receiver, 0, EventReceiver.COLOR_BLUE,
				STAT_SCORE, STAT_LINES, STAT_LEVEL, STAT_TIME, STAT_SPL, STAT_LPM);
		drawResultRank(engine, playerID, receiver, 12, EventReceiver.COLOR_BLUE, rankingRank);
		drawResultNetRank(engine, playerID, receiver, 14, EventReceiver.COLOR_BLUE, netRankingRank[0]);
		drawResultNetRankDaily(engine, playerID, receiver, 16, EventReceiver.COLOR_BLUE, netRankingRank[1]);

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
		startlevel = prop.getProperty("marathon.startlevel", 0);
		tspinEnableType = prop.getProperty("marathon.tspinEnableType", 1);
		enableTSpin = prop.getProperty("marathon.enableTSpin", true);
		enableTSpinKick = prop.getProperty("marathon.enableTSpinKick", true);
		spinCheckType = prop.getProperty("marathon.spinCheckType", 0);
		tspinEnableEZ = prop.getProperty("marathon.tspinEnableEZ", false);
		enableB2B = prop.getProperty("marathon.enableB2B", true);
		enableCombo = prop.getProperty("marathon.enableCombo", true);
		goaltype = prop.getProperty("marathon.gametype", 0);
		big = prop.getProperty("marathon.big", false);
		version = prop.getProperty("marathon.version", 0);
	}

	/**
	 * Save settings to property file
	 * @param prop Property file
	 */
	private void saveSetting(CustomProperties prop) {
		prop.setProperty("marathon.startlevel", startlevel);
		prop.setProperty("marathon.tspinEnableType", tspinEnableType);
		prop.setProperty("marathon.enableTSpin", enableTSpin);
		prop.setProperty("marathon.enableTSpinKick", enableTSpinKick);
		prop.setProperty("marathon.spinCheckType", spinCheckType);
		prop.setProperty("marathon.tspinEnableEZ", tspinEnableEZ);
		prop.setProperty("marathon.enableB2B", enableB2B);
		prop.setProperty("marathon.enableCombo", enableCombo);
		prop.setProperty("marathon.gametype", goaltype);
		prop.setProperty("marathon.big", big);
		prop.setProperty("marathon.version", version);
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
				rankingScore[j][i] = prop.getProperty("marathon.ranking." + ruleName + "." + j + ".score." + i, 0);
				rankingLines[j][i] = prop.getProperty("marathon.ranking." + ruleName + "." + j + ".lines." + i, 0);
				rankingTime[j][i] = prop.getProperty("marathon.ranking." + ruleName + "." + j + ".time." + i, 0);
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
				prop.setProperty("marathon.ranking." + ruleName + "." + j + ".score." + i, rankingScore[j][i]);
				prop.setProperty("marathon.ranking." + ruleName + "." + j + ".lines." + i, rankingLines[j][i]);
				prop.setProperty("marathon.ranking." + ruleName + "." + j + ".time." + i, rankingTime[j][i]);
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

	/**
	 * NET: Send various in-game stats (as well as goaltype)
	 * @param engine GameEngine
	 */
	@Override
	protected void netSendStats(GameEngine engine) {
		int bg = engine.owner.backgroundStatus.fadesw ? engine.owner.backgroundStatus.fadebg : engine.owner.backgroundStatus.bg;
		String msg = "game\tstats\t";
		msg += engine.statistics.score + "\t" + engine.statistics.lines + "\t" + engine.statistics.totalPieceLocked + "\t";
		msg += engine.statistics.time + "\t" + engine.statistics.level + "\t";
		msg += engine.statistics.lpm + "\t" + engine.statistics.spl + "\t" + goaltype + "\t";
		msg += engine.gameActive + "\t" + engine.timerActive + "\t";
		msg += lastscore + "\t" + scgettime + "\t" + lastevent + "\t" + lastb2b + "\t" + lastcombo + "\t" + lastpiece + "\t";
		msg += bg + "\n";
		netLobby.netPlayerClient.send(msg);
	}

	/**
	 * NET: Receive various in-game stats (as well as goaltype)
	 */
	@Override
	protected void netRecvStats(GameEngine engine, String[] message) {
		engine.statistics.score = Integer.parseInt(message[4]);
		engine.statistics.lines = Integer.parseInt(message[5]);
		engine.statistics.totalPieceLocked = Integer.parseInt(message[6]);
		engine.statistics.time = Integer.parseInt(message[7]);
		engine.statistics.level = Integer.parseInt(message[8]);
		engine.statistics.lpm = Float.parseFloat(message[9]);
		engine.statistics.spl = Double.parseDouble(message[10]);
		goaltype = Integer.parseInt(message[11]);
		engine.gameActive = Boolean.parseBoolean(message[12]);
		engine.timerActive = Boolean.parseBoolean(message[13]);
		lastscore = Integer.parseInt(message[14]);
		scgettime = Integer.parseInt(message[15]);
		lastevent = Integer.parseInt(message[16]);
		lastb2b = Boolean.parseBoolean(message[17]);
		lastcombo = Integer.parseInt(message[18]);
		lastpiece = Integer.parseInt(message[19]);
		engine.owner.backgroundStatus.bg = Integer.parseInt(message[20]);

		// Meter
		engine.meterValue = ((engine.statistics.lines % 10) * receiver.getMeterMax(engine)) / 9;
		engine.meterColor = GameEngine.METER_COLOR_GREEN;
		if(engine.statistics.lines % 10 >= 4) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
		if(engine.statistics.lines % 10 >= 6) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
		if(engine.statistics.lines % 10 >= 8) engine.meterColor = GameEngine.METER_COLOR_RED;
	}

	/**
	 * NET: Send end-of-game stats
	 * @param engine GameEngine
	 */
	@Override
	protected void netSendEndGameStats(GameEngine engine) {
		String subMsg = "";
		subMsg += "SCORE;" + engine.statistics.score + "\t";
		subMsg += "LINE;" + engine.statistics.lines + "\t";
		subMsg += "LEVEL;" + (engine.statistics.level + engine.statistics.levelDispAdd) + "\t";
		subMsg += "TIME;" + GeneralUtil.getTime(engine.statistics.time) + "\t";
		subMsg += "SCORE/LINE;" + engine.statistics.spl + "\t";
		subMsg += "LINE/MIN;" + engine.statistics.lpm + "\t";

		String msg = "gstat1p\t" + NetUtil.urlEncode(subMsg) + "\n";
		netLobby.netPlayerClient.send(msg);
	}

	/**
	 * NET: Send game options to all spectators
	 * @param engine GameEngine
	 */
	@Override
	protected void netSendOptions(GameEngine engine) {
		String msg = "game\toption\t";
		msg += startlevel + "\t" + tspinEnableType + "\t" + enableTSpinKick + "\t" + spinCheckType + "\t" + tspinEnableEZ + "\t";
		msg += enableB2B + "\t" + enableCombo + "\t" + goaltype + "\t" + big + "\n";
		netLobby.netPlayerClient.send(msg);
	}

	/**
	 * NET: Receive game options
	 */
	@Override
	protected void netRecvOptions(GameEngine engine, String[] message) {
		startlevel = Integer.parseInt(message[4]);
		tspinEnableType = Integer.parseInt(message[5]);
		enableTSpinKick = Boolean.parseBoolean(message[6]);
		spinCheckType = Integer.parseInt(message[7]);
		tspinEnableEZ = Boolean.parseBoolean(message[8]);
		enableB2B = Boolean.parseBoolean(message[9]);
		enableCombo = Boolean.parseBoolean(message[10]);
		goaltype = Integer.parseInt(message[11]);
		big = Boolean.parseBoolean(message[12]);
	}

	/**
	 * NET: Get goal type
	 */
	@Override
	protected int netGetGoalType() {
		return goaltype;
	}

	/**
	 * NET: It returns true when the current settings doesn't prevent leaderboard screen from showing.
	 */
	@Override
	protected boolean netIsNetRankingViewOK(GameEngine engine) {
		return ((startlevel == 0) && (!big) && (engine.ai == null));
	}
}
