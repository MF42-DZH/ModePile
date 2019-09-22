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
package mandl.nullpo.custom.modes;

import java.util.*;

import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.util.CustomProperties;
import mu.nu.nullpo.util.GeneralUtil;
import zeroxfc.nullpo.custom.libs.ResourceHolderCustomAssetExtension;
import zeroxfc.nullpo.custom.libs.ScrollingMarqueeText;

/**
 * RETRO MANIA 2 mode (by MandL27; edited from the original Retro Mania)
 * Modifications by ry00001
 */
public class RetroMania2Mode extends mu.nu.nullpo.game.subsystem.mode.DummyMode {
	/** Current version */
	private static final int CURRENT_VERSION = 2;

	/** Poweron Pattern */
	private static final String STRING_POWERON_PATTERN =
		"4040050165233516506133350555213560141520224542633206134255165200333560031332022463366645230432611435"+
		"5335503262512313515002442203656664131543211220146344201325061401134610644005663441101532234006340505"+
		"4621441004021465010225623313311635326311133504346120621126223156523530636115044065300222245330252325"+
		"5563545455656660124120450663502223206465164461126135621055103645066644052535021110020361422122352566"+
		"1564343513043465103636404534525056551422631026052022163516150316500504641606133253660234134530365424"+
		"4124644510156225214120146050543513004022131140054341604166064441010614144404145451160041314635320626"+
		"0246251556635262420616451361336106153451563316660054255631510320566516465265421144640513424316315421"+
		"6644140264401653410103024436251016522052305506020020331200443440341001604426324366453255122653512056"+
		"4234334231212152312006153023444306242003331046140330636540231321265610510125435251421621035523001404"+
		"0335464640401464125332132315552404146634264364245513600336065666305002023203545052006445544450440460";

	/** Game feel */
	private boolean sega;
	private String[] FEEL_STRING = {"NULLPO", "SEGA"};

	/** Gravity table */
	private static final int tableDenominator[][] = {{24, 15, 10, 6, 20, 5, 5, 4, 3, 3, 2, 2, 2, 2, 2, 1, 24, 1},
													 {24, 15, 10, 6, 20, 5, 5, 4, 3, 3, 2, 2, 2, 2, 2, 1, 24, 1},
													 {24, 15, 10, 3, 20, 3, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 24, 1},
													 { 1,  1,  1, 1, 20, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 24, 1},
													 {-1, -1, -1,-1, 20,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1, 24, 1},
													 {-1, -1, -1,-1, -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1, -1,-1}};

	/** Lock delay table */
	private static final int tableLockDelay[][] = {{44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44, 44},
												   {44, 39, 34, 30, 39, 29, 29, 29, 28, 28, 28, 28, 28, 28, 28, 24, 44, 18},
												   {44, 39, 34, 30, 39, 29, 29, 29, 28, 28, 24, 24, 24, 20, 20, 19, 44, 18},
												   {24, 24, 24, 30, 39, 24, 24, 24, 24, 24, 24, 24, 24, 20, 20, 19, 44, 18},
											  	   {44, 39, 34, 30, 39, 29, 29, 29, 28, 28, 28, 28, 28, 28, 28, 24, 44, 18},
												   {24, 24, 24, 30, 39, 24, 24, 24, 24, 24, 24, 24, 24, 20, 20, 19, 44, 18}};

	/** ARE table */
	private static final int tableAre[][] = {{31, 28, 26, 26, 28, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 31, 17},
											 {31, 28, 26, 26, 28, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 31, 17},
											 {31, 28, 26, 26, 28, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 31, 17},
											 {26, 26, 26, 26, 28, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 31, 17},
											 {26, 26, 26, 26, 28, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 31, 17},
											 {26, 26, 26, 26, 28, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 31, 17}};

	/** Score multiplier table */
	private static final int tableMult[][] = {{1, 2, 3, 4, 5, 6,  6,  6,  8,  8, 10, 10, 10, 10, 10, 11, 12, 13},
											  {1, 2, 3, 4, 5, 6,  6,  6,  8,  8, 10, 10, 10, 10, 10, 11, 12, 13},
											  {1, 2, 3, 4, 5, 6,  7,  8,  9, 10, 11, 12, 13, 14, 15, 16, 20, 20},
											  {5, 5, 6, 6, 8, 8, 10, 10, 10, 10, 11, 12, 13, 14, 15, 16, 20, 20},
											  {5, 5, 6, 6, 8, 8, 10, 10, 10, 10, 11, 12, 13, 14, 15, 16, 20, 20},
											  {5, 5, 6, 6, 8, 8, 10, 10, 10, 10, 11, 12, 13, 14, 15, 16, 20, 20}};

	/** Slot multiplier table */
	private static final int tableSlot[] = {1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 5, 5, 10};

	/** Lines needed to level up */
	private static final int tableLines[] = {6, 6, 7, 9, 6, 9, 9, 9, 10, 10, 20, 16, 16, 16, 16};

	/** BGM fadeout level */
	private static final int[] tableBGMFadeout = {4, 9, 14, 15, -1};
	private static final int[] tableBGMFadeoutCustom = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, -1};

	/** BGM change level */
	private static final int[] tableBGMChange  = {5, 10, 15, 17, -1};
	private static final int[] tableBGMChangeCustom  = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, -1};

	/** Name of game types */
	private static final String[] GAMETYPE_NAME = {"V.EASY","EASY","NORMAL","HARD","ANOTHER","ANOTHER2"};

	/** Number of game type */
	private static final int GAMETYPE_MAX = 6;

	/** Most recent scoring event type constants */
	private static final int EVENT_NONE = 0,
			EVENT_SINGLE = 1,
			EVENT_DOUBLE = 2,
			EVENT_TRIPLE = 3,
			EVENT_FOUR = 4;

	/** Number of ranking records */
	private static final int RANKING_MAX = 10;

	/** Number of ranking types */
	private static final int RANKING_TYPE = 6;

	/** Max score */
	private static final int MAX_SCORE = 99999999;

	/** Max lines */
	private static final int MAX_LINES = 999;

	/** Max level */
	private static final int MAX_LEVEL = 17;

	/** Credits time */
	private static final int ROLLTIMELIMIT = 12000;

	/** GameManager object (Manages entire game status) */
	private GameManager owner;

	/** EventReceiver object (This receives many game events, can also be used for drawing the fonts.) */
	private EventReceiver receiver;

	private ResourceHolderCustomAssetExtension rhcae = new ResourceHolderCustomAssetExtension(18);

	/** Amount of points you just get from line clears */
	private int lastscore;

	/** Elapsed time from last line clear (lastscore is displayed to screen until this reaches to 120) */
	private int scgettime;

	/** Most recent scoring event type */
	private int lastevent;

	/** True if most recent scoring event is a split */
	private boolean lastsplit;

	/** Selected game type */
	private int gametype;

	/** Selected starting level */
	private int startlevel;

	/** Custom music? */
	private boolean customBgm = false;

	/** BGM level */
	private int bgmlv;

	/** Amount of lines cleared (It will be reset when the level increases) */
	private int linesAfterLastLevelUp;

	/** Last line clears */
	private String lineClearHistory;
	private boolean historyFirstMatch;
	private String lastLineHistory;
	private boolean lastLineMatch;
	private boolean waitingOnBoardClear;

	/** Roll timer */
	private int rolltime;

	private int thankyoutimer = 0;
	private static final int THANK_YOU_TIME_LIMIT = 600;
	private boolean drawThankYou = false;

	/** Special bonus elgibility */
	private boolean specialBonus;

	/** Version of this mode */
	private int version;

	/** Your place on leaderboard (-1: out of rank) */
	private int rankingRank;

	private int startLines = 0;

	/** Score records */
	private int[][] rankingScore;

	/** Line records */
	private int[][] rankingLines;

	/** Time records */
	private int[][] rankingTime;

	private String[] marqueeHeading = {"PORT BY", "BASED ON", "SPECIAL THANKS", "SEGA MODE/CUSTOM BGMS/CREDIT ROLL", "MUSIC FROM", ""};
	private String[] marqueeText = {"MANDL27/RY00001", "SEGA TETRIS (1999)", "OSHISAURE, 0XFC963F18DC21, LINEPIECE777", "RY00001", "SEGA TETRIS (1999)", "THANK YOU FOR PLAYING!"};

	private ScrollingMarqueeText marquee = new ScrollingMarqueeText(marqueeHeading, marqueeText, EventReceiver.COLOR_ORANGE, EventReceiver.COLOR_WHITE);

	/**
	 * Returns the name of this mode
	 */
	@Override
	public String getName() {
		return "RETRO MANIA 2";
	}

	@Override
	public boolean onGameOver(GameEngine engine, int playerID) {
		rhcae.stopBGM();
		engine.gameEnded();
		return false;
	}

	/**
	 * This function will be called when the game enters the main game screen.
	 */
	@Override
	public void playerInit(GameEngine engine, int playerID) {
		owner = engine.owner;
		receiver = engine.owner.receiver;
		lastscore = 0;
		scgettime = 0;
		lastevent = 0;
		lastsplit = false;
		linesAfterLastLevelUp = 0;
		rolltime = 0;
		thankyoutimer = 0;
		lineClearHistory = "";
		lastLineHistory = "";
		historyFirstMatch = false;
		specialBonus = true;
		drawThankYou = false;
		waitingOnBoardClear = false;

		rankingRank = -1;
		rankingScore = new int[RANKING_TYPE][RANKING_MAX];
		rankingLines = new int[RANKING_TYPE][RANKING_MAX];
		rankingTime = new int[RANKING_TYPE][RANKING_MAX];

		engine.tspinEnable = false;
		engine.b2bEnable = false;
		engine.comboType = GameEngine.COMBO_TYPE_DISABLE;
		engine.bighalf = false;
		engine.bigmove = false;
		engine.staffrollEnable = true;
		engine.staffrollEnableStatistics = true;
		engine.staffrollNoDeath = false;

		engine.speed.lineDelay = 47;
		engine.speed.das = 13;
		engine.framecolor = GameEngine.FRAME_COLOR_BLUE;

		if(owner.replayMode == false) {
			loadSetting(owner.modeConfig);
			loadRanking(owner.modeConfig, engine.ruleopt.strRuleName);
			version = CURRENT_VERSION;
		} else {
			loadSetting(owner.replayProp);
		}

		engine.owner.backgroundStatus.bg = startlevel;
	}

	/**
	 * Set BGM at start of game
	 * @param engine GameEngine
	 */
	private void setStartBgmlv(GameEngine engine) {
		bgmlv = customBgm ? 16 : 0;
		if (!customBgm) {
			while((tableBGMChange[bgmlv] != -1) && (engine.statistics.level >= tableBGMChange[bgmlv])) bgmlv++;
		} else {
			bgmlv = engine.statistics.level + 16;
		}
	}

	/**
	 * Set the gravity speed
	 * @param engine GameEngine object
	 */
	private void setSpeed(GameEngine engine) {
		int lv = engine.statistics.level;

		if(lv < 0) lv = 0;
		if(lv >= tableDenominator[0].length) lv = tableDenominator[0].length - 1;

		engine.speed.gravity = 1;
		engine.speed.denominator = (int) tableDenominator[gametype][lv];
		engine.speed.are = tableAre[gametype][lv];
		engine.speed.areLine = tableAre[gametype][lv] + 10;
		engine.speed.lockDelay = tableLockDelay[gametype][lv];
	}

	/**
	 * Main routine for game setup screen
	 */
	@Override
	public boolean onSetting(GameEngine engine, int playerID) {
		if(engine.owner.replayMode == false) {
			// Configuration changes
			int change = updateCursor(engine, 4);

			if(change != 0) {
				engine.playSE("change");

				switch(engine.statc[2]) {
					case 0:
						gametype += change;
						if(gametype < 0) gametype = GAMETYPE_MAX - 1;
						if(gametype > GAMETYPE_MAX - 1) gametype = 0;
						break;
					case 1:
						startlevel += change;
						if (startlevel < 0) startlevel = 15;
						if (startlevel > 15) startlevel = 0;
						engine.owner.backgroundStatus.bg = startlevel;
						break;
					case 2:
						startLines += (change * 10);
						if (startLines > 300) startLines = 300;
						if (startLines < 0) startLines = 0;
						break;
					case 3:
						customBgm = !customBgm;
						break;
					case 4:
						sega = !sega;
						break;
				}
			}

			// Check for A button, when pressed this will begin the game
			if(engine.ctrl.isPush(Controller.BUTTON_A) && (engine.statc[3] >= 5)) {
				engine.playSE("decide");
				saveSetting(owner.modeConfig);
				receiver.saveModeConfig(owner.modeConfig);
				return false;
			}

			// Check for B button, when pressed this will shutdown the game engine.
			if(engine.ctrl.isPush(Controller.BUTTON_B)) {
				engine.quitflag = true;
			}

			engine.statc[3]++;
		} else {
			engine.statc[3]++;
			engine.statc[2] = -1;

			if(engine.statc[3] >= 60) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Renders game setup screen
	 */
	@Override
	public void renderSetting(GameEngine engine, int playerID) {
		drawMenu(engine, playerID, receiver, 0, EventReceiver.COLOR_BLUE, 0,
				"DIFFICULTY", GAMETYPE_NAME[gametype],
				"LEVEL", String.valueOf(startlevel),
				"LINES", String.valueOf(startLines),
				"CUSTOM BGM", customBgm ? "ON" : "OFF",
				"GAME FEEL", sega ? "SEGA" : "NULLPO");
	}

	/**
	 * Ready
	 */
	public boolean onReady(GameEngine engine, int playerID) {
		if(engine.statc[0] == 0) {
			if (customBgm && rhcae.getAmountLoadedBGM() == 0) {
				// Load extra music, if it exists
				rhcae.loadNewBGMAppend("res/bgm/segatet/00.ogg", false, true);
				rhcae.loadNewBGMAppend("res/bgm/segatet/01.ogg", false, true);
				rhcae.loadNewBGMAppend("res/bgm/segatet/02.ogg", false, true);
				rhcae.loadNewBGMAppend("res/bgm/segatet/03.ogg", false, true);
				rhcae.loadNewBGMAppend("res/bgm/segatet/04.ogg", false, true);
				rhcae.loadNewBGMAppend("res/bgm/segatet/05.ogg", false, true);
				rhcae.loadNewBGMAppend("res/bgm/segatet/06.ogg", false, true);
				rhcae.loadNewBGMAppend("res/bgm/segatet/07.ogg", false, true);
				rhcae.loadNewBGMAppend("res/bgm/segatet/08.ogg", false, true);
				rhcae.loadNewBGMAppend("res/bgm/segatet/09.ogg", false, true);
				rhcae.loadNewBGMAppend("res/bgm/segatet/10.ogg", false, true);
				rhcae.loadNewBGMAppend("res/bgm/segatet/11.ogg", false, true);
				rhcae.loadNewBGMAppend("res/bgm/segatet/12.ogg", false, true);
				rhcae.loadNewBGMAppend("res/bgm/segatet/13.ogg", false, true);
				rhcae.loadNewBGMAppend("res/bgm/segatet/14.ogg", false, true);
				rhcae.loadNewBGMAppend("res/bgm/segatet/15.ogg", false, true);
				rhcae.loadNewBGMAppend("res/bgm/segatet/16.ogg", false, true);
				rhcae.loadNewBGMAppend("res/bgm/segatet/17.ogg", false, true);
			}
		}
		if (engine.statc[0] == 1) {
			engine.field = new Field(10,18,0,false);
		}
		return false;
	}

	/**
	 * This function will be called before the game actually begins (after Ready&Go screen disappears)
	 */
	@Override
	public void startGame(GameEngine engine, int playerID) {
		engine.statistics.level = startlevel;
		engine.statistics.lines = startLines;
		engine.statistics.levelDispAdd = 1;
		engine.ruleopt.lineFallAnim = true;

		setSpeed(engine);
		setStartBgmlv(engine);
		for (int i = 0; i < engine.statistics.level; i++) {
			engine.statistics.lines += tableLines[i];
		}
		if (engine.statistics.level == 15) {
		}
		owner.bgmStatus.bgm = bgmlv;
	}

	/**
	 * Renders HUD (leaderboard or game statistics)
	 */
	@Override
	public void renderLast(GameEngine engine, int playerID) {
		receiver.drawScoreFont(engine, playerID, 0, 0, "RETRO MANIA 2", EventReceiver.COLOR_GREEN);
		receiver.drawScoreFont(engine, playerID, 0, 1, "("+GAMETYPE_NAME[gametype]+" SPEED)", EventReceiver.COLOR_GREEN);

		if( (engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (owner.replayMode == false)) ) {
			// Leaderboard
			if((owner.replayMode == false) && (startlevel == 0) && (engine.ai == null)) {
				float scale = (receiver.getNextDisplayType() == 2) ? 0.5f : 1.0f;
				int topY = (receiver.getNextDisplayType() == 2) ? 6 : 4;
				receiver.drawScoreFont(engine, playerID, 3, topY-1, "SCORE   LINE TIME", EventReceiver.COLOR_BLUE, scale);

				for(int i = 0; i < RANKING_MAX; i++) {
					receiver.drawScoreFont(engine, playerID, 0, topY+i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW, scale);
					receiver.drawScoreFont(engine, playerID, 3, topY+i, String.valueOf(rankingScore[gametype][i]), (i == rankingRank), scale);
					receiver.drawScoreFont(engine, playerID, 11, topY+i, String.valueOf(rankingLines[gametype][i]), (i == rankingRank), scale);
					receiver.drawScoreFont(engine, playerID, 16, topY+i, GeneralUtil.getTime(rankingTime[gametype][i]), (i == rankingRank), scale);
				}
			}
		} else {
			// Game statistics
			receiver.drawScoreFont(engine, playerID, 0, 3, "SCORE", EventReceiver.COLOR_BLUE);
			String strScore;
			if((lastscore == 0) || (scgettime >= 120)) {
				strScore = String.valueOf(engine.statistics.score);
			} else {
				strScore = String.valueOf(engine.statistics.score) + "(+" + String.valueOf(lastscore) + ")";
			}
			receiver.drawScoreFont(engine, playerID, 0, 4, strScore);

			receiver.drawScoreFont(engine, playerID, 0, 6, "LINES", EventReceiver.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 7, String.valueOf(engine.statistics.lines)+"/300");
			if (engine.statistics.level < 15) {
				receiver.drawScoreFont(engine, playerID, 7, 7, "(" + linesAfterLastLevelUp + "/" + tableLines[engine.statistics.level] + ")");
			}
			if (scgettime < 120) {
				if (lastLineMatch && (scgettime % 4 <= 1)) {
					receiver.drawScoreFont(engine, playerID, 0, 8, lastLineHistory, EventReceiver.COLOR_RED);
				} else {
					receiver.drawScoreFont(engine, playerID, 0, 8, lastLineHistory, EventReceiver.COLOR_YELLOW);
				}
			} else {
				receiver.drawScoreFont(engine, playerID, 0, 8, lineClearHistory, EventReceiver.COLOR_YELLOW);
			}

			receiver.drawScoreFont(engine, playerID, 0, 10, "LEVEL", EventReceiver.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 11, String.valueOf(engine.statistics.level));

			receiver.drawScoreFont(engine, playerID, 0, 13, "TIME", EventReceiver.COLOR_BLUE);

			if((engine.gameActive) && (engine.ending == 2) && engine.statistics.level == 17) {
				int time = ROLLTIMELIMIT - rolltime;
				if(time < 0) time = 0;
				if (sega) {
					receiver.drawScoreFont(engine, playerID, 0, 14, GeneralUtil.getTime(time), EventReceiver.COLOR_ORANGE);
				} else {
					receiver.drawScoreFont(engine, playerID, 0, 14, GeneralUtil.getTime(engine.statistics.time));
					receiver.drawScoreFont(engine, playerID, 0, 16, "ROLL TIME", EventReceiver.COLOR_BLUE);
					receiver.drawScoreFont(engine, playerID, 0, 17, GeneralUtil.getTime(time));
				}

				final int tMax = 60 * 35;
				if (time <= tMax) {
					double prog = Math.abs(time - tMax) / (double)tMax;
					marquee.drawAtY(engine, receiver, playerID, 26, 1, prog);
				}
			} else {
				receiver.drawScoreFont(engine, playerID, 0, 14, GeneralUtil.getTime(engine.statistics.time));
			}

			if((lastevent != EVENT_NONE) && (scgettime < 120)) {

				switch (lastevent) {
					case EVENT_SINGLE:
						receiver.drawMenuFont(engine, playerID, 2, 19, "SINGLE", EventReceiver.COLOR_DARKBLUE);
						break;
					case EVENT_DOUBLE:
						if (lastsplit) receiver.drawMenuFont(engine, playerID, 2, 19, "SPLIT", EventReceiver.COLOR_CYAN);
						else receiver.drawMenuFont(engine, playerID, 2, 19, "DOUBLE", EventReceiver.COLOR_BLUE);
						break;
					case EVENT_TRIPLE:
						if (lastsplit) receiver.drawMenuFont(engine, playerID, 2, 19, "ONE/TWO", EventReceiver.COLOR_YELLOW);
						else receiver.drawMenuFont(engine, playerID, 2, 19, "TRIPLE", EventReceiver.COLOR_GREEN);
						break;
					case EVENT_FOUR:
						receiver.drawMenuFont(engine, playerID, 3, 19, "FOUR", EventReceiver.COLOR_ORANGE);
						break;
				}
			}

			if (drawThankYou) {
				receiver.drawMenuFont(engine, playerID, 0, 5, "CONGRATS!", EventReceiver.COLOR_ORANGE);
				receiver.drawMenuFont(engine, playerID, 0, 7, "THANK YOU", EventReceiver.COLOR_ORANGE);
				receiver.drawMenuFont(engine, playerID, 3, 9, "FOR", EventReceiver.COLOR_ORANGE);
				receiver.drawMenuFont(engine, playerID, 0, 11, "PLAYING!", EventReceiver.COLOR_ORANGE);
			}

			//receiver.drawScoreFont(engine, playerID, 0, 15, String.valueOf(linesAfterLastLevelUp));
			//receiver.drawScoreFont(engine, playerID, 0, 16, GeneralUtil.getTime(levelTime[Math.min(engine.statistics.level,15)] - levelTimer));
		}
	}

	/**
	 * This function will be called when the game timer updates
	 */
	@Override
	public void onLast(GameEngine engine, int playerID) {
		scgettime++;

		if (waitingOnBoardClear && engine.ending == 2) {
			waitingOnBoardClear = false;
			engine.holdPieceObject = null;
			setPowerOn(engine, playerID);
			if (sega) {
				engine.stat = GameEngine.STAT_CUSTOM;
				engine.resetStatc();
			}
		}

		if((engine.gameActive) && (engine.ending == 2) && engine.statistics.level == 17) {
			rolltime++;

			// Time meter
			int remainRollTime = ROLLTIMELIMIT - rolltime;
			engine.meterValue = (remainRollTime * 288) / ROLLTIMELIMIT;
			engine.meterColor = GameEngine.METER_COLOR_GREEN;
			if (remainRollTime <= 100 * 60) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
			if (remainRollTime <= 30 * 60) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
			if (remainRollTime <= 10 * 60) engine.meterColor = GameEngine.METER_COLOR_RED;

			// Background cycling
			if (rolltime >= 66 && rolltime < 9666 && (rolltime - 66) % 600 == 0) {
					owner.backgroundStatus.fadecount = 0;
					owner.backgroundStatus.fadebg = (rolltime - 66) / 600;
					owner.backgroundStatus.fadesw = (owner.backgroundStatus.fadebg != owner.backgroundStatus.bg);
			} else if (rolltime == 9666) {
				owner.backgroundStatus.fadecount = 0;
				owner.backgroundStatus.fadebg = 17;
				owner.backgroundStatus.fadesw = (owner.backgroundStatus.fadebg != owner.backgroundStatus.bg);
			}

			if (rolltime >= ROLLTIMELIMIT) {
				if (specialBonus) {
					engine.statistics.score += 10_000_000;
				}
				engine.gameEnded();
				engine.resetStatc();
				engine.stat = GameEngine.STAT_EXCELLENT;
			}
		}

		// Max-out score, lines, and level
		if(version >= 2) {
			if(engine.statistics.score > MAX_SCORE) engine.statistics.score = MAX_SCORE;
			if(engine.statistics.lines > MAX_LINES) engine.statistics.lines = MAX_LINES;
			if(engine.statistics.level > MAX_LEVEL) engine.statistics.level = MAX_LEVEL;
		}

		// Unload extra music, if necessary
		if (engine.quitflag && customBgm) {
			while (rhcae.getAmountLoadedBGM() > 0) rhcae.removeBGMFromEnd(true);
		}
	}

	/**
	 * Movement input
	 * Only used here for special bonus test
	 */
	@Override
	public boolean onMove(GameEngine engine, int playerID) {
		int move = 0;
		if(engine.ctrl.isPush(Controller.BUTTON_A) || engine.ctrl.isPush(Controller.BUTTON_C)) move = -1;
		else if(engine.ctrl.isPush(Controller.BUTTON_B)) move = 1;
		else if(engine.ctrl.isPush(Controller.BUTTON_E)) move = 2;
		if (move != 0) {
			int rt = engine.getRotateDirection(move);
			if (rt < 0 || rt == 2) {
				// counterclockwise, do nothing
			} else {
				specialBonus = false;
			}
		}
		return false;
	}

	/**
	 * CONGRATULATIONS!
	 * THANK YOU
	 * FOR
	 * PLAYING!
	 *
	 * @author ry00001
	 */
	@Override
	public boolean onCustom(GameEngine engine, int playerID) {
		if (engine.ending == 2) {
			thankyoutimer++;
			drawThankYou = true;
			if (thankyoutimer >= THANK_YOU_TIME_LIMIT) {
				drawThankYou = false;
				engine.stat = GameEngine.STAT_ARE;
			}
			return false;
		}
		return true;
	}

	/**
	 * Calculates line-clear score
	 * (This function will be called even if no lines are cleared)
	 */
	@Override
	public void calcScore(GameEngine engine, int playerID, int lines) {
		// Determines line-clear bonus
		int pts = 0;
		int mult = tableMult[gametype][engine.statistics.level];
		int region = 0;
		int rows = 0;
		for (int i = 0; (rows < lines) && (i < 18); i++) {
			if (engine.field.getLineFlag(i)) {
				rows++;
			}
			if (rows > 0) {
				region++;
			}
		}
		if (lines == 0) {
			lastevent = EVENT_NONE;
			lastsplit = false;
		} else if (lines == 1) {
			pts += 50 * mult; // Single
			lineClearHistory += "S";
			lastevent = EVENT_SINGLE;
		} else if (lines == 2) {
			if (region == lines) {
				pts += 200 * mult; // Double
			} else {
				pts += 300 * mult; // Split
				lastsplit = true;
			}
			lineClearHistory += "D";
			lastevent = EVENT_DOUBLE;
		} else if (lines == 3) {
			if (region == lines) {
				pts += 450 * mult; // Triple
			} else {
				pts += 550 * mult; // One/Two
				lastsplit = true;
			}
			lineClearHistory += "T";
			lastevent = EVENT_TRIPLE;
		} else if (lines >= 4) {
			pts += 1000 * mult; // Four
			lineClearHistory += "F";
			lastevent = EVENT_FOUR;
		}
		lastLineMatch = false;
		lastLineHistory = lineClearHistory.substring(0);

		// Slot bonus
		if (lineClearHistory.length() == 2 && lineClearHistory.charAt(0) == lineClearHistory.charAt(1) &&
				lineClearHistory.charAt(0) != 'S' && !historyFirstMatch) {
			engine.playSE("b2b_start");
			historyFirstMatch = true;
		}
		if (lineClearHistory.length() == 3) {
			if (lineClearHistory.charAt(0) == lineClearHistory.charAt(1) && lineClearHistory.charAt(1) == lineClearHistory.charAt(2)) {
				switch (lineClearHistory.charAt(0)) {
					case 'S':
						pts += 50 * tableSlot[engine.statistics.level];
						break;
					case 'D':
						pts += 10000 * tableSlot[engine.statistics.level];
						engine.playSE("b2b_continue");
						break;
					case 'T':
						pts += 50000 * tableSlot[engine.statistics.level];
						engine.playSE("b2b_continue");
						break;
					case 'F':
						pts += Math.min(100000 * tableSlot[engine.statistics.level], 999999);
						engine.playSE("b2b_continue");
						break;
				}
				lastLineMatch = true;
			}
			else if (historyFirstMatch){
				engine.playSE("b2b_end");
			}
			lineClearHistory = "";
			historyFirstMatch = false;
		}

		// Perfect clear bonus
		if (engine.field.isEmpty()) {
			engine.playSE("bravo");
			pts += 200000 * tableSlot[engine.statistics.level];
		}

		// Add score
		if (pts > 0) {
			lastscore = pts;
			scgettime = 0;
			engine.statistics.scoreFromLineClear += pts;
			engine.statistics.score += pts;
		}

		// Add lines
		linesAfterLastLevelUp += lines;

		// Level up
		if (((engine.statistics.level < 15) && (linesAfterLastLevelUp >= tableLines[engine.statistics.level])) ||
				(engine.statistics.level == 15 && engine.statistics.lines >= 300) ||
				(engine.statistics.level == 16 && linesAfterLastLevelUp > 0)) {
			engine.statistics.level++;

			if (engine.statistics.level != 17) {
				owner.backgroundStatus.fadecount = 0;
				owner.backgroundStatus.fadebg = (engine.statistics.level);
				if (owner.backgroundStatus.fadebg > 19) owner.backgroundStatus.fadebg = 19;
				owner.backgroundStatus.fadesw = (owner.backgroundStatus.fadebg != owner.backgroundStatus.bg);
			}

			linesAfterLastLevelUp = 0;
			setSpeed(engine);
			if (engine.statistics.level < 16) {
				engine.playSE("levelup");
			} else if (engine.statistics.level == 16) {
				// Start roll
				engine.ending = 1;
				waitingOnBoardClear = true;
			}
		}

		// Update the meter
		int remaining = 0;
		int meter = 0;
		if (engine.statistics.level < 15) {
			// Level 0-14
			meter = linesAfterLastLevelUp * 288 / tableLines[engine.statistics.level];
			remaining = tableLines[engine.statistics.level] - linesAfterLastLevelUp;
		} else if (engine.statistics.level == 15) {
			// Level 15
			meter = (engine.statistics.lines - 165) * 288 / 135;
			remaining = 300 - lines;
		}
		engine.meterValue = meter;
		engine.meterColor = GameEngine.METER_COLOR_GREEN;
		if ((meter > 144 && meter <= 230) || remaining == 3)
			engine.meterColor = GameEngine.METER_COLOR_YELLOW;
		if (meter > 230 || remaining == 2)
			engine.meterColor = GameEngine.METER_COLOR_ORANGE;
		if (remaining == 1)
			engine.meterColor = GameEngine.METER_COLOR_RED;

		int e = bgmlv;
		if (customBgm) {
			e -= 16;
		}

		// Fade/start music
		if((((customBgm ? tableBGMFadeoutCustom : tableBGMFadeout)[e] != -1) && (engine.statistics.level == (customBgm ? tableBGMFadeoutCustom : tableBGMFadeout)[e]) &&
				(remaining <= 5) && (remaining != 0)) || (engine.statistics.level == 15 && engine.statistics.lines >= 295)) {
			owner.bgmStatus.fadesw  = true;
		}
		if(((customBgm ? tableBGMChangeCustom : tableBGMChange)[e] != -1) && (engine.statistics.level >= (customBgm ? tableBGMChangeCustom : tableBGMChange)[e])) {
			bgmlv++;
			owner.bgmStatus.fadesw = false;
			owner.bgmStatus.bgm = bgmlv;
		}
	}

	private void setPowerOn(GameEngine engine, int playerID) {
		// Reset next position
		engine.nextPieceCount = 0;

		// Create new next queue
		engine.nextPieceArrayID = GeneralUtil.createNextPieceArrayFromNumberString(STRING_POWERON_PATTERN);
		engine.nextPieceArrayObject = new Piece[engine.nextPieceArrayID.length];

		for(int i = 0; i < engine.nextPieceArrayObject.length; i++) {
			engine.nextPieceArrayObject[i] = new Piece(engine.nextPieceArrayID[i]);
			engine.nextPieceArrayObject[i].direction = engine.ruleopt.pieceDefaultDirection[engine.nextPieceArrayObject[i].id];
			if(engine.nextPieceArrayObject[i].direction >= Piece.DIRECTION_COUNT) {
				engine.nextPieceArrayObject[i].direction = engine.random.nextInt(Piece.DIRECTION_COUNT);
			}
			engine.nextPieceArrayObject[i].connectBlocks = engine.connectBlocks;
			engine.nextPieceArrayObject[i].setColor(engine.ruleopt.pieceColor[engine.nextPieceArrayObject[i].id]);
			engine.nextPieceArrayObject[i].setSkin(engine.getSkin());
			engine.nextPieceArrayObject[i].updateConnectData();
			engine.nextPieceArrayObject[i].setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
			engine.nextPieceArrayObject[i].setAttribute(Block.BLOCK_ATTRIBUTE_BONE, engine.bone);
		}

		// Rule compatibility
		if (engine.randomBlockColor) {
			if (engine.blockColors.length < engine.numColors || engine.numColors < 1)
				engine.numColors = engine.blockColors.length;
			for (int i = 0; i < engine.nextPieceArrayObject.length; i++) {
				int size = engine.nextPieceArrayObject[i].getMaxBlock();
				int[] colors = new int[size];
				for (int j = 0; j < size; j++)
					colors[j] = engine.blockColors[engine.random.nextInt(engine.numColors)];
				engine.nextPieceArrayObject[i].setColor(colors);
				engine.nextPieceArrayObject[i].updateConnectData();
			}
		}
	}


	/**
	 * This function will be called when soft-drop is used
	 */
	@Override
	public void afterSoftDropFall(GameEngine engine, int playerID, int fall) {
		if((version >= 2) && (engine.speed.denominator == 1)) return;
		engine.statistics.scoreFromSoftDrop += fall;
		engine.statistics.score += fall;
	}

	/**
	 * This function will be called when hard-drop is used
	 */
	@Override
	public void afterHardDropFall(GameEngine engine, int playerID, int fall) {
		engine.statistics.scoreFromHardDrop += fall;
		engine.statistics.score += fall;
	}

	/**
	 * Renders game result screen
	 */
	@Override
	public void renderResult(GameEngine engine, int playerID) {
		receiver.drawMenuFont(engine, playerID,  0, 1, "PLAY DATA", EventReceiver.COLOR_ORANGE);

		drawResultStats(engine, playerID, receiver, 3, EventReceiver.COLOR_BLUE,
				STAT_SCORE, STAT_LINES, STAT_LEVEL, STAT_TIME);
		drawResultRank(engine, playerID, receiver, 11, EventReceiver.COLOR_BLUE, rankingRank);
	}

	/**
	 * This function will be called when the replay data is going to be saved
	 */
	@Override
	public void saveReplay(GameEngine engine, int playerID, CustomProperties prop) {
		saveSetting(prop);

		// Checks/Updates the ranking
		if((owner.replayMode == false) && (engine.ai == null)) {
			updateRanking(engine.statistics.score, engine.statistics.lines, engine.statistics.time, gametype);

			if(rankingRank != -1) {
				saveRanking(owner.modeConfig, engine.ruleopt.strRuleName);
				receiver.saveModeConfig(owner.modeConfig);
			}
		}
	}

	/**
	 * Load the settings
	 */
	private void loadSetting(CustomProperties prop) {
		startlevel = prop.getProperty("retromania2.startlevel", 0);
		gametype = prop.getProperty("retromania2.gametype", 0);
		version = prop.getProperty("retromania2.version", 0);
		sega = prop.getProperty("retromania2.sega", false);
		startLines = prop.getProperty("retromania2.lines", 0);
		customBgm = prop.getProperty("retromania2.bgm", false);
	}

	/**
	 * Save the settings
	 */
	private void saveSetting(CustomProperties prop) {
		prop.setProperty("retromania2.startlevel", startlevel);
		prop.setProperty("retromania2.gametype", gametype);
		prop.setProperty("retromania2.version", version);
		prop.setProperty("retromania2.sega", sega);
		prop.setProperty("retromania2.lines", startLines);
		prop.setProperty("retromania2.bgm", customBgm);
	}

	/**
	 * Load the ranking
	 */
	private void loadRanking(CustomProperties prop, String ruleName) {
		for(int i = 0; i < RANKING_MAX; i++) {
			for (int gametypeIndex = 0; gametypeIndex < RANKING_TYPE; gametypeIndex++)
			{
				rankingScore[gametypeIndex][i] = prop.getProperty("retromania2.ranking." + ruleName + "." + gametypeIndex + ".score." + i, 0);
				rankingLines[gametypeIndex][i] = prop.getProperty("retromania2.ranking." + ruleName + "." + gametypeIndex + ".lines." + i, 0);
				rankingTime[gametypeIndex][i] = prop.getProperty("retromania2.ranking." + ruleName + "." + gametypeIndex + ".time." + i, 0);

				if(rankingScore[gametypeIndex][i] > MAX_SCORE) rankingScore[gametypeIndex][i] = MAX_SCORE;
				if(rankingLines[gametypeIndex][i] > MAX_LINES) rankingLines[gametypeIndex][i] = MAX_LINES;
			}
		}
	}

	/**
	 * Save the ranking
	 */
	private void saveRanking(CustomProperties prop, String ruleName) {
		for(int i = 0; i < RANKING_MAX; i++) {
			for (int gametypeIndex = 0; gametypeIndex < RANKING_TYPE; gametypeIndex++)
			{
				prop.setProperty("retromania2.ranking." + ruleName + "." + gametypeIndex + ".score." + i, rankingScore[gametypeIndex][i]);
				prop.setProperty("retromania2.ranking." + ruleName + "." + gametypeIndex + ".lines." + i, rankingLines[gametypeIndex][i]);
				prop.setProperty("retromania2.ranking." + ruleName + "." + gametypeIndex + ".time." + i, rankingTime[gametypeIndex][i]);
			}
		}
	}

	/**
	 * Update the ranking
	 */
	private void updateRanking(int sc, int li, int time, int type) {
		rankingRank = checkRanking(sc, li, time, type);

		if(rankingRank != -1 && startlevel == 0) {
			// Shift the old records
			for(int i = RANKING_MAX - 1; i > rankingRank; i--) {
				rankingScore[type][i] = rankingScore[type][i - 1];
				rankingLines[type][i] = rankingLines[type][i - 1];
				rankingTime[type][i] = rankingTime[type][i - 1];
			}

			// Insert a new record
			rankingScore[type][rankingRank] = sc;
			rankingLines[type][rankingRank] = li;
			rankingTime[type][rankingRank] = time;
		}
	}

	/**
	 * This function will check the ranking and returns which place you are. (-1: Out of rank)
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
}
