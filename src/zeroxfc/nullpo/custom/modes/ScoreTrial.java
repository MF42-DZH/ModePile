package zeroxfc.nullpo.custom.modes;

import java.util.ArrayList;
import java.util.Random;

import mu.nu.nullpo.game.component.BGMStatus;
import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.util.CustomProperties;
import mu.nu.nullpo.util.GeneralUtil;
import zeroxfc.nullpo.custom.libs.*;
import zeroxfc.nullpo.custom.libs.particles.BlockParticleCollection;

public class ScoreTrial extends MarathonModeBase {
	// Speed tables
	private static final int[] GRAVITY_TABLE = { 8, 16, 24, 32, 40, 48, 56, 64, 72, 80,
												 96, 104, 112, 128, 144, 152, 160, 192, 224, 256, 64,
												 80, 96, 112, 128, 144, 160, 192, 224, 224, 256,
												 256, 384, 512, 512, 768, 768, 1024, 1024, 1280, 512,
												 512, 768, 1024, 1280, 1536, 1792, 2048, 2304, 2560, -1 };
	private static final int[] ARE_TABLE =     { 15, 15, 15, 15, 14, 14,
			                                     13, 12, 11, 10, 9,
			                                     8, 7, 6, 5, 15 };
	private static final int[] LOCK_TABLE =    { 30, 29, 28, 27, 26, 25,
			                                     24, 23, 22, 21, 20,
			                                     19, 18, 17, 17, 30 };
	
	// Levels for speed changes
	private static final int[] LEVEL_ARE_LOCK_CHANGE =  { 60, 70, 80, 90, 100, 
														  110, 120, 130, 140, 150,
														  160, 170, 180, 190, 200, 10000 };
	
	// Timer constants
	private static final int STARTING_TIMER = 7200;
	private static final int HARD_50_TIMER = 16200;
	private static final int NORMAL_30_TIMER = 10800;
	private static final int LEVEL_TIMEBONUS = 900;
	private static final int TIMER_MAX = 18000;
	
	// Max difficulties
	private static final int MAX_DIFFICULTIES = 3;
	
	// Difficulty names
	private static final String[] DIFFICULTY_NAMES = {
		"NORMAL",
		"HARD",
		"ADVANCE"
	};
	
	// Starting lives (4 here = 5 in play).
	// - N.B. when timer runs out, simply make the lifecount 0 before triggering game over.
	private static final int STARTING_LIVES = 4;
	
	// BG changes every 5 levels until 50, then at 200.
	// Level changes every 8 lines until lv 50, then every line clear increases it by 1 until 200.
	
	// 6f LD for NORMAL/HARD
	// 4'30" LV50 Time Limit for HARD
	// 3'00" LV30 Time Limit for NORMAL
	
	// Line clear scores.
	private static final int[] LINECLEAR_SCORES = { 40, 100, 300, 1200 };
	
	// Ingame Timer
	private int mainTimer;
	
	// Ranking stuff
	private int[][] rankingScore;
	private int[][] rankingLines;
	private int[][] rankingTime;
	
	// Lives added/removed
	private int lifeOffset;
	
	// Score before increase;
	private int scoreBeforeIncrease;
	
	// ANIMATION TYPE
	private int lineClearAnimType;
	
	// Difficulty selector
	private int difficultySelected;
	
	// should use timer?
	private boolean shouldUseTimer;
	
	// Local randomiser
	private Random localRandom;
	
	// Goal line
	private int goalLine;
	
	// Lives started with
	private int livesStartedWith;
	
	// Particle stuff
	private BlockParticleCollection blockParticles;
	
	// Flying congratulations text
	private FlyInOutText congratulationsText;
	
	// Combo Text
	private FlyInOutText comboTextAward;
	private FlyInOutText comboTextNumber;

	/** The good hard drop effect */
	private ArrayList<int[]> pCoordList;
	private Piece cPiece;
	
	private boolean o;
	private int l;

	private ProfileProperties playerProperties;
	private static final int headerColour = EventReceiver.COLOR_PINK;
	private boolean showPlayerStats;
	private String PLAYER_NAME;
	private int rankingRankPlayer;
	private int[][] rankingScorePlayer;
	private int[][] rankingLinesPlayer;
	private int[][] rankingTimePlayer;

	// Mode name
	public String getName() {
		return "SCORE TRIAL";
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
		lifeOffset = 0;
		o = false;
		l = 0;
		scoreBeforeIncrease = 0;
		lineClearAnimType = 0;
		difficultySelected = 0;
		shouldUseTimer = false;
		goalLine = 0;

		pCoordList = new ArrayList<>();
		cPiece = null;
		
		congratulationsText = null;
		comboTextAward = null;
		comboTextNumber = null;
		
		mainTimer = 0;
		livesStartedWith = 0;

		rankingRank = -1;
		rankingScore = new int[MAX_DIFFICULTIES][RANKING_MAX];
		rankingLines = new int[MAX_DIFFICULTIES][RANKING_MAX];
		rankingTime = new int[MAX_DIFFICULTIES][RANKING_MAX];

		if (playerProperties == null) {
			playerProperties = new ProfileProperties(headerColour);

			showPlayerStats = false;
		}

		rankingRankPlayer = -1;
		rankingScorePlayer = new int[MAX_DIFFICULTIES][RANKING_MAX];
		rankingLinesPlayer = new int[MAX_DIFFICULTIES][RANKING_MAX];
		rankingTimePlayer = new int[MAX_DIFFICULTIES][RANKING_MAX];

		netPlayerInit(engine, playerID);

		if(owner.replayMode == false) {
			loadSetting(owner.modeConfig);
			loadRanking(owner.modeConfig, engine.ruleopt.strRuleName);

			if (playerProperties.isLoggedIn()) {
				loadSettingPlayer(playerProperties);
				loadRankingPlayer(playerProperties, engine.ruleopt.strRuleName);
			}

			PLAYER_NAME = "";
			version = CURRENT_VERSION;
		} else {
			loadSetting(owner.replayProp);
			if((version == 0) && (owner.replayProp.getProperty("scoretrial.endless", false) == true)) goaltype = 2;

			PLAYER_NAME = owner.replayProp.getProperty("scoretrial.playerName", "");

			// NET: Load name
			netPlayerName = engine.owner.replayProp.getProperty(playerID + ".net.netPlayerName", "");
		}

		engine.owner.backgroundStatus.bg = startlevel;
		engine.framecolor = GameEngine.FRAME_COLOR_GRAY;
		engine.tspinEnable = false;
		engine.comboType = GameEngine.COMBO_TYPE_NORMAL;
		engine.b2b = false;
		
		engine.statistics.levelDispAdd = 0;
		
		engine.speed.das = 8;
		engine.speed.denominator = 256;
		engine.speed.are = 15;
		engine.speed.areLine = 15;
		engine.speed.lineDelay = 0;
	}
	
	/**
	 * Set the gravity rate
	 * @param engine GameEngine
	 */
	public void setSpeed(GameEngine engine) {
		int lv = engine.statistics.level;

		if(lv < 0) lv = 0;
		if(lv >= GRAVITY_TABLE.length) lv = GRAVITY_TABLE.length - 1;

		engine.speed.gravity = GRAVITY_TABLE[lv];
		if (engine.speed.gravity == -1) engine.speed.das = 6;
		
		if ((engine.statistics.level) >= 60) {
			int ldAreIndex = 0;
			while ((engine.statistics.level) >= LEVEL_ARE_LOCK_CHANGE[ldAreIndex]) ldAreIndex++;
			
			engine.speed.are = ARE_TABLE[ldAreIndex];
			engine.speed.areLine = ARE_TABLE[ldAreIndex];
			engine.speed.lockDelay = LOCK_TABLE[ldAreIndex];
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
			int change = updateCursor(engine, 3, playerID);

			if(change != 0) {
				engine.playSE("change");

				switch(engine.statc[2]) {
				case 0:
					difficultySelected += change;
					if (difficultySelected > MAX_DIFFICULTIES - 1) difficultySelected = 0;
					if (difficultySelected < 0) difficultySelected = MAX_DIFFICULTIES - 1;
					break;
				case 1:
					// if ((STARTING_LIVES + lifeOffset + change) >= 0 && (STARTING_LIVES + lifeOffset + change) <= 9) lifeOffset += change;
					lifeOffset += change;
					if ((STARTING_LIVES + lifeOffset) < 0) lifeOffset = 5;
					if ((STARTING_LIVES + lifeOffset) > 9) lifeOffset = -4;
					break;
				case 2:
					big = !big;
					break;
				case 3:
					lineClearAnimType += change;
					if (lineClearAnimType > BlockParticleCollection.ANIMATION_TYPES - 1) lineClearAnimType = 0;
					if (lineClearAnimType < 0) lineClearAnimType = BlockParticleCollection.ANIMATION_TYPES - 1;
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
				if (playerProperties.isLoggedIn()) {
					saveSettingPlayer(playerProperties);
					playerProperties.saveProfileConfig();
				} else {
					saveSetting(owner.modeConfig);
					receiver.saveModeConfig(owner.modeConfig);
				}

				// NET: Signal start of the game
				if(netIsNetPlay) netLobby.netPlayerClient.send("start1p\n");

				return false;
			}

			// Cancel
			if(engine.ctrl.isPush(Controller.BUTTON_B) && !netIsNetPlay) {
				engine.quitflag = true;
				playerProperties = new ProfileProperties(headerColour);
			}

			// New acc
			if(engine.ctrl.isPush(Controller.BUTTON_E) && engine.ai == null && !netIsNetPlay) {
				playerProperties = new ProfileProperties(headerColour);
				engine.playSE("decide");

				engine.stat = GameEngine.STAT_CUSTOM;
				engine.resetStatc();
				return true;
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
		String lc = "";
		switch (lineClearAnimType) {
		case 0:
			lc = "DTET";
			break;
		case 1:
			lc = "TGM";
			break;
		}
		if(netIsNetRankingDisplayMode) {
			// NET: Netplay Ranking
			netOnRenderNetPlayRanking(engine, playerID, receiver);
		} else {
			drawMenu(engine, playerID, receiver, 0, EventReceiver.COLOR_RED, 0,
					"DIFFICULTY", DIFFICULTY_NAMES[difficultySelected]);
			drawMenu(engine, playerID, receiver, 2, EventReceiver.COLOR_GREEN, 1,
					"LIVES", String.valueOf(STARTING_LIVES + lifeOffset + 1));
			drawMenu(engine, playerID, receiver, 4, EventReceiver.COLOR_BLUE, 2,
					"BIG", GeneralUtil.getONorOFF(big),
					"L.C. ANIM.", lc);
		}
	}
	
	@Override
	public boolean onReady(GameEngine engine, int playerID) {
		mainTimer = STARTING_TIMER;
		goalLine = 8;
		
		switch (difficultySelected) {
		case 0:
			mainTimer = NORMAL_30_TIMER;
			break;
		case 1:
			mainTimer = HARD_50_TIMER;
			break;
		case 2:
			mainTimer = STARTING_TIMER;
			break;
		}
		
		livesStartedWith = STARTING_LIVES + lifeOffset;
		engine.lives = STARTING_LIVES + lifeOffset;
		
		if (blockParticles != null) {
			blockParticles = null;
		}
		
		return false;
	}
	
	/*
	 * Called for initialization during "Ready" screen
	 */
	@Override
	public void startGame(GameEngine engine, int playerID) {
		engine.statistics.level = startlevel;
		engine.statistics.levelDispAdd = 0;
		
		switch (difficultySelected) {
		case 0:
			engine.ruleopt.areCancelHold = false;
			engine.ruleopt.areCancelMove = false;
			engine.ruleopt.areCancelRotate = false;
			
			
			engine.ruleopt.harddropEnable = false;
			engine.ruleopt.softdropSurfaceLock = true;
			engine.ruleopt.softdropEnable = true;
			
			
			shouldUseTimer = false;
			engine.speed.lineDelay = 8;
			break;
		case 1:
			engine.ruleopt.areCancelHold = false;
			engine.ruleopt.areCancelMove = false;
			engine.ruleopt.areCancelRotate = false;
			
			
			engine.ruleopt.harddropEnable = true;
			engine.ruleopt.softdropSurfaceLock = true;
			engine.ruleopt.softdropEnable = true;
			
			
			shouldUseTimer = false;
			engine.speed.lineDelay = 8;
			break;
		case 2:
			engine.ruleopt.areCancelHold = true;
			engine.ruleopt.areCancelMove = true;
			engine.ruleopt.areCancelRotate = true;
			
			
			engine.ruleopt.harddropEnable = true;
			engine.ruleopt.softdropSurfaceLock = true;
			engine.ruleopt.softdropEnable = true;
			
			
			shouldUseTimer = true;
			engine.speed.lineDelay = 0;
			break;
		}
		
		engine.ruleopt.rotateButtonAllowDouble = true;
		engine.ghost = false;
		engine.big = big;
		
		scoreBeforeIncrease = 0;
		engine.speed.das = 8;
		engine.speed.denominator = 256;
		engine.speed.are = 15;
		engine.speed.areLine = 15;
		engine.speed.lockDelay = 30;
		
		int total = (engine.field.getHeight() + engine.field.getHiddenHeight()) * engine.field.getWidth() * 2;
		blockParticles = new BlockParticleCollection(total, lineClearAnimType);
		
		localRandom = new Random(engine.randSeed);
		
		l = STARTING_LIVES + lifeOffset + 1;
		
		o = false;
		
		setSpeed(engine);
		
		owner.bgmStatus.bgm = difficultySelected;
		owner.bgmStatus.fadesw = false;

		if(netIsWatch) {
			owner.bgmStatus.bgm = BGMStatus.BGM_NOTHING;
		}
	}

	@Override
	public boolean onCustom(GameEngine engine, int playerID) {
		showPlayerStats = false;

		engine.isInGame = true;

		boolean s = playerProperties.loginScreen.updateScreen(engine, playerID);
		if (playerProperties.isLoggedIn()) {
			loadRankingPlayer(playerProperties, engine.ruleopt.strRuleName);
			loadSettingPlayer(playerProperties);
		}

		if (engine.stat == GameEngine.STAT_SETTING) engine.isInGame = false;

		return s;
	}
	
	/*
	 * Render score
	 */
	@Override
	public void renderLast(GameEngine engine, int playerID) {
		if(owner.menuOnly) return;

		receiver.drawScoreFont(engine, playerID, 0, 0, getName(), EventReceiver.COLOR_GREEN);
		receiver.drawScoreFont(engine, playerID, 0, 1, "(" + DIFFICULTY_NAMES[difficultySelected] + " TIER)", EventReceiver.COLOR_GREEN);

		if( (engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (owner.replayMode == false)) ) {
			if((owner.replayMode == false) && (big == false) && (engine.ai == null) && (lifeOffset == 0)) {
				float scale = (receiver.getNextDisplayType() == 2) ? 0.5f : 1.0f;
				int topY = (receiver.getNextDisplayType() == 2) ? 6 : 4;
				receiver.drawScoreFont(engine, playerID, 3, topY-1, "SCORE  LINE TIME", EventReceiver.COLOR_BLUE, scale);

				if (showPlayerStats) {
					for(int i = 0; i < RANKING_MAX; i++) {
						receiver.drawScoreFont(engine, playerID,  0, topY+i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW, scale);
						String s = String.valueOf(rankingScorePlayer[difficultySelected][i]);
						receiver.drawScoreFont(engine, playerID, (s.length() > 6 && receiver.getNextDisplayType() != 2) ? 6 : 3, (s.length() > 6 && receiver.getNextDisplayType() != 2) ? (topY+i) * 2 : (topY+i), s, (i == rankingRankPlayer), (s.length() > 6 && receiver.getNextDisplayType() != 2) ? scale * 0.5f : scale);
						receiver.drawScoreFont(engine, playerID, 10, topY+i, String.valueOf(rankingLinesPlayer[difficultySelected][i]), (i == rankingRankPlayer), scale);
						receiver.drawScoreFont(engine, playerID, 15, topY+i, GeneralUtil.getTime(rankingTimePlayer[difficultySelected][i]), (i == rankingRankPlayer), scale);
					}

					receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 1, "PLAYER SCORES", EventReceiver.COLOR_BLUE);
					receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 2, playerProperties.getNameDisplay(), EventReceiver.COLOR_WHITE, 2f);

					receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 5, "F:SWITCH RANK SCREEN", EventReceiver.COLOR_GREEN);
				} else {
					for(int i = 0; i < RANKING_MAX; i++) {
						receiver.drawScoreFont(engine, playerID,  0, topY+i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW, scale);
						String s = String.valueOf(rankingScore[difficultySelected][i]);
						receiver.drawScoreFont(engine, playerID, (s.length() > 6 && receiver.getNextDisplayType() != 2) ? 6 : 3, (s.length() > 6 && receiver.getNextDisplayType() != 2) ? (topY+i) * 2 : (topY+i), s, (i == rankingRank), (s.length() > 6 && receiver.getNextDisplayType() != 2) ? scale * 0.5f : scale);
						receiver.drawScoreFont(engine, playerID, 10, topY+i, String.valueOf(rankingLines[difficultySelected][i]), (i == rankingRank), scale);
						receiver.drawScoreFont(engine, playerID, 15, topY+i, GeneralUtil.getTime(rankingTime[difficultySelected][i]), (i == rankingRank), scale);
					}

					receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 1, "LOCAL SCORES", EventReceiver.COLOR_BLUE);
					if (!playerProperties.isLoggedIn()) receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 2, "(NOT LOGGED IN)\n(E:LOG IN)");
					if (playerProperties.isLoggedIn()) receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 5, "F:SWITCH RANK SCREEN", EventReceiver.COLOR_GREEN);
				}
			}
		} else if (engine.stat == GameEngine.STAT_CUSTOM) {
			playerProperties.loginScreen.renderScreen(receiver, engine, playerID);
		} else {
			receiver.drawScoreFont(engine, playerID, 0, 3, "SCORE", EventReceiver.COLOR_BLUE);
			String strScore;
			if((lastscore == 0) || (scgettime >= 120)) {
				strScore = String.valueOf(engine.statistics.score);
			} else {
				strScore = (int) Interpolation.sineStep(scoreBeforeIncrease, engine.statistics.score, ((double) scgettime / 120.0)) + "(+" + lastscore + ")";
			}
			receiver.drawScoreFont(engine, playerID, 0, 4, strScore);
			
			int lMax = 50;
			if (difficultySelected == 0) lMax = 30;
			
			receiver.drawScoreFont(engine, playerID, 0, 6, "LINE", EventReceiver.COLOR_BLUE);
			if(engine.statistics.level >= lMax)
				receiver.drawScoreFont(engine, playerID, 0, 7, engine.statistics.lines + "");
			else
				receiver.drawScoreFont(engine, playerID, 0, 7, engine.statistics.lines + "/" + String.valueOf(goalLine));
			
			if (!o) {
				l = engine.lives + 1;
			}
			if (engine.stat == GameEngine.STAT_GAMEOVER && !o && engine.lives == 0) l = 0;
			
			receiver.drawScoreFont(engine, playerID, 0, 9, "LIVES", EventReceiver.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 10, String.valueOf(l), l <= 1);
			
			receiver.drawScoreFont(engine, playerID, 0, 12, "LEVEL", EventReceiver.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 13, String.valueOf(engine.statistics.level));

			receiver.drawScoreFont(engine, playerID, 0, 15, "TIME", EventReceiver.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 16, GeneralUtil.getTime(engine.statistics.time));

			if (playerProperties.isLoggedIn() || PLAYER_NAME.length() > 0) {
				receiver.drawScoreFont(engine, playerID, 11, 6, "PLAYER", EventReceiver.COLOR_BLUE);
				receiver.drawScoreFont(engine, playerID, 11, 7, owner.replayMode ? PLAYER_NAME : playerProperties.getNameDisplay(), EventReceiver.COLOR_WHITE, 2f);
			}

			int baseX = receiver.getFieldDisplayPositionX(engine, playerID) + 4;
			int baseY = receiver.getFieldDisplayPositionY(engine, playerID) + 52;
			if (pCoordList.size() > 0 && cPiece != null) {
				for (int[] loc : pCoordList) {
					int cx = baseX + (16 * loc[0]);
					int cy = baseY + (16 * loc[1]);
					RendererExtension.drawScaledPiece(receiver, engine, playerID, cx, cy, cPiece, 1f, 0f);
				}
			}

			if (shouldUseTimer) {
				receiver.drawMenuFont(engine, playerID, 0, 21, "TIME LIMIT", EventReceiver.COLOR_RED);
				receiver.drawMenuFont(engine, playerID, 1, 22, GeneralUtil.getTime(mainTimer), (mainTimer <= 600 && ((mainTimer / 2) % 2 == 0)));
			}
			
			if (blockParticles != null) blockParticles.drawAll(engine, receiver, playerID);
			
			if (congratulationsText != null) {
				congratulationsText.draw(engine, receiver, playerID);
			}
			
			if (comboTextAward != null) {
				comboTextAward.draw(engine, receiver, playerID);
			}
			
			if (comboTextNumber != null) {
				comboTextNumber.draw(engine, receiver, playerID);
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
	 * Called after every frame
	 */
	@Override
	public void onLast(GameEngine engine, int playerID) {
		scgettime++;
		
		if (engine.gameStarted && engine.ending == 0) {
			if (engine.stat == GameEngine.STAT_ARE && difficultySelected == 2) {
				engine.dasCount = engine.speed.das;
			}
			
			if (shouldUseTimer && engine.stat != GameEngine.STAT_GAMEOVER) {
				mainTimer--;
				
				if((mainTimer <= 600) && (mainTimer % 60 == 0)) {
					receiver.playSE("countdown");
				}
			}
			
			// Meter - use as timer.
			int timerMax = TIMER_MAX;
			if (difficultySelected == 0) timerMax = NORMAL_30_TIMER;
			if (difficultySelected == 1) timerMax = HARD_50_TIMER;
			
			engine.meterValue = (int)((mainTimer / (double)timerMax) * receiver.getMeterMax(engine));
			engine.meterColor = GameEngine.METER_COLOR_GREEN;
			if(mainTimer > 3600 && mainTimer < 5400) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
			if(mainTimer <= 3600 && mainTimer > 1800) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
			if(mainTimer <= 1800) engine.meterColor = GameEngine.METER_COLOR_RED;
			
			if (mainTimer <= 0) {
				if (engine.statistics.level < 199 && difficultySelected == 2) {
					engine.lives = 0;
					engine.stat = GameEngine.STAT_GAMEOVER;
					engine.resetStatc();
					engine.gameEnded();
				} else {
					// Ending
					int baseBonus = engine.statistics.score;
					baseBonus *= (float)(engine.lives + 1) / (livesStartedWith + 1);
					
					engine.statistics.score += baseBonus;
					lastscore = baseBonus;
					o = true;
					l = engine.lives + 1;
					scgettime = 0;
					
					if (engine.lives == (STARTING_LIVES + lifeOffset)) {
						int destinationX = 192;
						int destinationY = 224;
						congratulationsText = new FlyInOutText("PERFECT!", destinationX, destinationY, 15, 90, 15, new int[] {EventReceiver.COLOR_GREEN, EventReceiver.COLOR_CYAN}, 2.0f, engine.randSeed + engine.statistics.time, true);
						engine.playSE("cool");
					}
					
					engine.lives = 0;
					engine.ending = 1;
					engine.resetStatc();
					engine.gameEnded();
					engine.stat = GameEngine.STAT_ENDINGSTART;
				}
			}
		}
		
		if (engine.gameStarted && blockParticles != null) {
			blockParticles.update();
		}
		
		if (engine.gameStarted && congratulationsText != null) {
			congratulationsText.update();
			if (congratulationsText.shouldPurge()) {
				congratulationsText = null;
			}
		}
		
		if (engine.gameStarted && comboTextAward != null) {
			comboTextAward.update();
			if (comboTextAward.shouldPurge()) {
				comboTextAward = null;
			}
		}
		
		if (engine.gameStarted && comboTextNumber != null) {
			comboTextNumber.update();
			if (comboTextNumber.shouldPurge()) {
				comboTextNumber = null;
			}
		}

		if( (engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (!owner.replayMode)) || engine.stat == GameEngine.STAT_CUSTOM ) {
			// Show rank
			if(engine.ctrl.isPush(Controller.BUTTON_F) && playerProperties.isLoggedIn() && engine.stat != GameEngine.STAT_CUSTOM) {
				showPlayerStats = !showPlayerStats;
				engine.playSE("change");
			}
		}

		if (engine.quitflag) {
			playerProperties = new ProfileProperties(headerColour);
		}
	}
	
	@Override
	public boolean onGameOver(GameEngine engine, int playerID) {
		if (engine.lives <= 0) {
			shouldUseTimer = false;
		}
		return false;
	}
	
	@Override
	public boolean onLineClear(GameEngine engine, int playerID) {
		//  event 発生
		owner.receiver.onLineClear(engine, playerID);

		engine.checkDropContinuousUse();

		// 横溜め
		if(engine.ruleopt.dasInLineClear) engine.padRepeat();
		else if(engine.ruleopt.dasRedirectInDelay) { engine.dasRedirect(); }

		// 最初の frame
		if(engine.statc[0] == 0) {
			// Line clear flagを設定
			if (engine.clearMode == GameEngine.CLEAR_LINE)
				engine.lineClearing = engine.field.checkLine();
			// Set color clear flags
			else if (engine.clearMode == GameEngine.CLEAR_COLOR)
				engine.lineClearing = engine.field.checkColor(engine.colorClearSize, true, engine.garbageColorClear, engine.gemSameColor, engine.ignoreHidden);
			// Set line color clear flags
			else if (engine.clearMode == GameEngine.CLEAR_LINE_COLOR)
				engine.lineClearing = engine.field.checkLineColor(engine.colorClearSize, true, engine.lineColorDiagonals, engine.gemSameColor);
			else if (engine.clearMode == GameEngine.CLEAR_GEM_COLOR)
				engine.lineClearing = engine.field.gemColorCheck(engine.colorClearSize, true, engine.garbageColorClear, engine.ignoreHidden);

			// Linescountを決める
			int li = engine.lineClearing;
			if(big && engine.bighalf)
				li >>= 1;
			//if(li > 4) li = 4;

			if(engine.tspin) {
				engine.playSE("tspin" + li);

				if((engine.ending == 0) || (engine.staffrollEnableStatistics)) {
					if((li == 1) && (engine.tspinmini))  engine.statistics.totalTSpinSingleMini++;
					if((li == 1) && (!engine.tspinmini)) engine.statistics.totalTSpinSingle++;
					if((li == 2) && (engine.tspinmini))  engine.statistics.totalTSpinDoubleMini++;
					if((li == 2) && (!engine.tspinmini)) engine.statistics.totalTSpinDouble++;
					if(li == 3) engine.statistics.totalTSpinTriple++;
				}
			} else {
				if (engine.clearMode == GameEngine.CLEAR_LINE)
				engine.playSE("erase" + li);

				if((engine.ending == 0) || (engine.staffrollEnableStatistics)) {
					if(li == 1) engine.statistics.totalSingle++;
					if(li == 2) engine.statistics.totalDouble++;
					if(li == 3) engine.statistics.totalTriple++;
					if(li == 4) engine.statistics.totalFour++;
				}
			}

			// B2B bonus
			if(engine.b2bEnable) {
				if((engine.tspin) || (li >= 4)) {
					engine.b2bcount++;

					if(engine.b2bcount == 1) {
						engine.playSE("b2b_start");
					} else {
						engine.b2b = true;
						engine.playSE("b2b_continue");

						if((engine.ending == 0) || (engine.staffrollEnableStatistics)) {
							if(li == 4) engine.statistics.totalB2BFour++;
							else engine.statistics.totalB2BTSpin++;
						}
					}
				} else if(engine.b2bcount != 0) {
					engine.b2b = false;
					engine.b2bcount = 0;
					engine.playSE("b2b_end");
				}
			}

			// Combo
			if((engine.comboType != GameEngine.COMBO_TYPE_DISABLE) && (engine.chain == 0)) {
				if( (engine.comboType == GameEngine.COMBO_TYPE_NORMAL) || ((engine.comboType == GameEngine.COMBO_TYPE_DOUBLE) && (li >= 2)) )
					engine.combo++;

				if(engine.combo >= 2) {
					int cmbse = engine.combo - 1;
					if(cmbse > 20) cmbse = 20;
					engine.playSE("combo" + cmbse);
				}

				if((engine.ending == 0) || (engine.staffrollEnableStatistics)) {
					if(engine.combo > engine.statistics.maxCombo) engine.statistics.maxCombo = engine.combo;
				}
			}

			engine.lineGravityTotalLines += engine.lineClearing;

			if((engine.ending == 0) || (engine.staffrollEnableStatistics)) engine.statistics.lines += li;

			if(engine.field.getHowManyGemClears() > 0) engine.playSE("gem");

			// Calculate score
			calcScore(engine, playerID, li);
			owner.receiver.calcScore(engine, playerID, li);

			// Blockを消す演出を出す (まだ実際には消えていない）
			if (engine.clearMode == GameEngine.CLEAR_LINE) {
				int cY = 0;
				for(int i = 0; i < engine.field.getHeight(); i++) {
					if(engine.field.getLineFlag(i)) {
						cY++;
						for(int j = 0; j < engine.field.getWidth(); j++) {
							Block blk = engine.field.getBlock(j, i);

							if(blk != null) {
								if (blockParticles != null) {
									switch (lineClearAnimType) {
									case 0:
										blockParticles.addBlock(engine, receiver, playerID, blk, j, i, 10, 90, (li >= 4), localRandom);
										break;
									case 1:
										blockParticles.addBlock(engine, receiver, playerID, blk, j, i, engine.field.getWidth(), cY, li, 120);
									default:
										break;
									}
								}
							}
						}
					}
				}
			} else if (engine.clearMode == GameEngine.CLEAR_LINE_COLOR || engine.clearMode == GameEngine.CLEAR_COLOR || engine.clearMode == GameEngine.CLEAR_GEM_COLOR)
				for(int i = 0; i < engine.field.getHeight(); i++) {
					for(int j = 0; j < engine.field.getWidth(); j++) {
						Block blk = engine.field.getBlock(j, i);
						if (blk == null)
							continue;
						if(blk.getAttribute(Block.BLOCK_ATTRIBUTE_ERASE)) {
							//blockParticles.addBlock(engine, receiver, playerID, blk, j, i, 4, 60, localRandom);
							if (engine.displaysize == 1)
							{
								//owner.receiver.blockBreak(engine, playerID, 2*j, 2*i, blk);
								//owner.receiver.blockBreak(engine, playerID, 2*j+1, 2*i, blk);
								//owner.receiver.blockBreak(engine, playerID, 2*j, 2*i+1, blk);
								//owner.receiver.blockBreak(engine, playerID, 2*j+1, 2*i+1, blk);
								
								switch (lineClearAnimType) {
								case 0:
									blockParticles.addBlock(engine, receiver, playerID, blk, 2*j, 2*i, 10, 90, false, localRandom);
									blockParticles.addBlock(engine, receiver, playerID, blk, 2*j+1, 2*i, 10, 90, false, localRandom);
									blockParticles.addBlock(engine, receiver, playerID, blk, 2*j, 2*i+1, 10, 90, false, localRandom);
									blockParticles.addBlock(engine, receiver, playerID, blk, 2*j+1, 2*i+1, 10, 90, false, localRandom);
									break;
								case 1:
									blockParticles.addBlock(engine, receiver, playerID, blk, 2*j, 2*i, engine.field.getWidth(), 1, 1, 120);
									blockParticles.addBlock(engine, receiver, playerID, blk, 2*j+1, 2*i, engine.field.getWidth(), 1, 1, 120);
									blockParticles.addBlock(engine, receiver, playerID, blk, 2*j, 2*i+1, engine.field.getWidth(), 1, 1, 120);
									blockParticles.addBlock(engine, receiver, playerID, blk, 2*j+1, 2*i+1, engine.field.getWidth(), 1, 1, 120);
								default:
									break;
								}
							}
							else
								//owner.receiver.blockBreak(engine, playerID, j, i, blk);
								switch (lineClearAnimType) {
								case 0:
									blockParticles.addBlock(engine, receiver, playerID, blk, j, i, 10, 90, false, localRandom);
									break;
								case 1:
									blockParticles.addBlock(engine, receiver, playerID, blk, j, i, engine.field.getWidth(), 1, 1, 120);
								default:
									break;
								}
						}
					}
				}

			// Blockを消す
			if (engine.clearMode == GameEngine.CLEAR_LINE)
				engine.field.clearLine();
			else if (engine.clearMode == GameEngine.CLEAR_COLOR)
				engine.field.clearColor(engine.colorClearSize, engine.garbageColorClear, engine.gemSameColor, engine.ignoreHidden);
			else if (engine.clearMode == GameEngine.CLEAR_LINE_COLOR)
				engine.field.clearLineColor(engine.colorClearSize, engine.lineColorDiagonals, engine.gemSameColor);
			else if (engine.clearMode == GameEngine.CLEAR_GEM_COLOR)
				engine.lineClearing = engine.field.gemClearColor(engine.colorClearSize, engine.garbageColorClear, engine.ignoreHidden);
		}

		// Linesを1段落とす
		if((engine.lineGravityType == GameEngine.LINE_GRAVITY_NATIVE) &&
		   (engine.getLineDelay() >= (engine.lineClearing - 1)) && (engine.statc[0] >= engine.getLineDelay() - (engine.lineClearing - 1)) && (engine.ruleopt.lineFallAnim))
		{
			engine.field.downFloatingBlocksSingleLine();
		}

		// Line delay cancel check
		engine.delayCancelMoveLeft = engine.ctrl.isPush(Controller.BUTTON_LEFT);
		engine.delayCancelMoveRight = engine.ctrl.isPush(Controller.BUTTON_RIGHT);

		boolean moveCancel = engine.ruleopt.lineCancelMove && (engine.ctrl.isPush(engine.getUp()) ||
				engine.ctrl.isPush(engine.getDown()) || engine.delayCancelMoveLeft || engine.delayCancelMoveRight);
		boolean rotateCancel = engine.ruleopt.lineCancelRotate && (engine.ctrl.isPush(Controller.BUTTON_A) ||
				engine.ctrl.isPush(Controller.BUTTON_B) || engine.ctrl.isPush(Controller.BUTTON_C) ||
				engine.ctrl.isPush(Controller.BUTTON_E));
		boolean holdCancel = engine.ruleopt.lineCancelHold && engine.ctrl.isPush(Controller.BUTTON_D);

		engine.delayCancel = moveCancel || rotateCancel || holdCancel;

		if( (engine.statc[0] < engine.getLineDelay()) && engine.delayCancel ) {
			engine.statc[0] = engine.getLineDelay();
		}

		// Next ステータス
		if(engine.statc[0] >= engine.getLineDelay()) {
			// Cascade
			if((engine.lineGravityType == GameEngine.LINE_GRAVITY_CASCADE || engine.lineGravityType == GameEngine.LINE_GRAVITY_CASCADE_SLOW)) {
				if (engine.statc[6] < engine.getCascadeDelay()) {
					engine.statc[6]++;
					return true;
				} else if(engine.field.doCascadeGravity(engine.lineGravityType)) {
					engine.statc[6] = 0;
					return true;
				} else if (engine.statc[6] < engine.getCascadeClearDelay()) {
					engine.statc[6]++;
					return true;
				} else if(((engine.clearMode == GameEngine.CLEAR_LINE) && engine.field.checkLineNoFlag() > 0) ||
						((engine.clearMode == GameEngine.CLEAR_COLOR) && engine.field.checkColor(engine.colorClearSize, false, engine.garbageColorClear, engine.gemSameColor, engine.ignoreHidden) > 0) ||
						((engine.clearMode == GameEngine.CLEAR_LINE_COLOR) && engine.field.checkLineColor(engine.colorClearSize, false, engine.lineColorDiagonals, engine.gemSameColor) > 0) ||
						((engine.clearMode == GameEngine.CLEAR_GEM_COLOR) && engine.field.gemColorCheck(engine.colorClearSize, false, engine.garbageColorClear, engine.ignoreHidden) > 0)) {
					engine.tspin = false;
					engine.tspinmini = false;
					engine.chain++;
					if(engine.chain > engine.statistics.maxChain) engine.statistics.maxChain = engine.chain;
					engine.statc[0] = 0;
					engine.statc[6] = 0;
					return true;
				}
			}

			boolean skip = false;
			skip = lineClearEnd(engine, playerID);
			owner.receiver.lineClearEnd(engine, playerID);

			if(!skip) {
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
			}

			return true;
		}

		engine.statc[0]++;
	return true;
	}
	
	/*
	 * Calculate score - PAIN
	 */
	@Override
	public void calcScore(GameEngine engine, int playerID, int lines) {
		// Line clear bonus
		int linesUsed = lines;
		if (linesUsed > 4) linesUsed = 4;
		
		if (lines > 0) {
			int pts = LINECLEAR_SCORES[linesUsed - 1];
			pts *= (engine.combo);
			pts *= (engine.statistics.level + 1);
			
			if (engine.combo >= 2) {
				int destinationX = receiver.getScoreDisplayPositionX(engine, playerID);
				int destinationY = receiver.getScoreDisplayPositionY(engine, playerID) + (20 * (engine.displaysize == 0 ? 16 : 32));
				
				int[] colors = new int[] {EventReceiver.COLOR_DARKBLUE};
				if (engine.combo >= 4) colors = new int[] {EventReceiver.COLOR_BLUE, EventReceiver.COLOR_DARKBLUE};
				if (engine.combo >= 8) colors = new int[] {EventReceiver.COLOR_CYAN, EventReceiver.COLOR_BLUE, EventReceiver.COLOR_DARKBLUE};
				if (engine.combo >= 11) colors = new int[] {EventReceiver.COLOR_YELLOW, EventReceiver.COLOR_ORANGE, EventReceiver.COLOR_RED};
				
				comboTextNumber = new FlyInOutText((engine.combo - 1) + " COMBO", destinationX, destinationY, 15, 60, 15, colors, 1.0f, engine.randSeed + engine.statistics.time, (engine.combo >= 8));
				if (engine.combo == 4) comboTextAward = new FlyInOutText("GOOD!", destinationX, destinationY + (engine.displaysize == 0 ? 16 : 32), 15, 60, 15, new int[] {EventReceiver.COLOR_YELLOW}, 1.0f, engine.randSeed + engine.statistics.time, (engine.combo >= 8));
				if (engine.combo == 8) comboTextAward = new FlyInOutText("AWESOME!", destinationX, destinationY + (engine.displaysize == 0 ? 16 : 32), 15, 60, 15, new int[] {EventReceiver.COLOR_GREEN}, 1.0f, engine.randSeed + engine.statistics.time, (engine.combo >= 8));
				if (engine.combo == 11) comboTextAward = new FlyInOutText("UNREAL!", destinationX, destinationY + (engine.displaysize == 0 ? 16 : 32), 15, 60, 15, new int[] {EventReceiver.COLOR_ORANGE, EventReceiver.COLOR_RED}, 1.0f, engine.randSeed + engine.statistics.time, (engine.combo >= 8));
			}
			
			// Add to score
			if(pts > 0) {
				scoreBeforeIncrease = engine.statistics.score;
				lastscore = pts;
				lastpiece = engine.nowPieceObject.id;
				scgettime = 0;
				if(lines >= 1) engine.statistics.scoreFromLineClear += pts;
				else engine.statistics.scoreFromOtherBonus += pts;
				engine.statistics.score += pts;
			}
		}
		
		if (engine.statistics.level >= 50 && engine.statistics.level < 200 && lines > 0 && difficultySelected == 2) {
			// Level up
			engine.statistics.level++;

			// owner.backgroundStatus.fadesw = true;
			// owner.backgroundStatus.fadecount = 0;
			// owner.backgroundStatus.fadebg = engine.statistics.level / 5;
			
			if (engine.statistics.level == 200) {
				owner.backgroundStatus.bg = 19;
				engine.playSE("endingstart");
				
				int destinationX = receiver.getScoreDisplayPositionX(engine, playerID);
				int destinationY = receiver.getScoreDisplayPositionY(engine, playerID) + (18 * (engine.displaysize == 0 ? 16 : 32));
				congratulationsText = new FlyInOutText("WELL DONE!", destinationX, destinationY, 30, 120, 30, new int[] {EventReceiver.COLOR_YELLOW, EventReceiver.COLOR_ORANGE, EventReceiver.COLOR_RED}, 1.0f, engine.randSeed + engine.statistics.time, false);
			}
			
			mainTimer += LEVEL_TIMEBONUS;
			if (mainTimer > TIMER_MAX) mainTimer = TIMER_MAX;

			setSpeed(engine);
			engine.playSE("levelup");
		} else if(engine.statistics.lines >= goalLine) {
			int lMax = 50;
			if (difficultySelected == 0) lMax = 30;
			
			if ((engine.statistics.level < lMax)) {
				// Level up
				engine.statistics.level++;
				
				switch (difficultySelected) {
				case 0:
					goalLine += (8 + ((engine.statistics.level / 10) * 2));
					break;
				case 1:
					goalLine += (8 + (engine.statistics.level / 10));
					break;
				case 2:
					goalLine += 8;
					break;
				}

				// owner.backgroundStatus.fadesw = true;
				// owner.backgroundStatus.fadecount = 0;
				owner.backgroundStatus.bg = (engine.statistics.level) / 5;
				if (engine.statistics.level == lMax && difficultySelected != 2) {
					owner.backgroundStatus.bg = 19;
					shouldUseTimer = true;
					engine.playSE("endingstart");
					
					int destinationX = receiver.getScoreDisplayPositionX(engine, playerID);
					int destinationY = receiver.getScoreDisplayPositionY(engine, playerID) + (18 * (engine.displaysize == 0 ? 16 : 32));
					congratulationsText = new FlyInOutText("WELL DONE!", destinationX, destinationY, 30, 120, 30, new int[] {EventReceiver.COLOR_YELLOW, EventReceiver.COLOR_ORANGE, EventReceiver.COLOR_RED}, 1.0f, engine.randSeed + engine.statistics.time, false);
				}
				
				if (difficultySelected == 2) {
					mainTimer += LEVEL_TIMEBONUS;
					if (mainTimer > TIMER_MAX) mainTimer = TIMER_MAX;
				}
				
				setSpeed(engine);
				engine.playSE("levelup");
			}
		}
	}
	
	/*
	 * Soft drop
	 */
	@Override
	public void afterSoftDropFall(GameEngine engine, int playerID, int fall) {
		engine.statistics.scoreFromSoftDrop += fall * (engine.statistics.level + 1);
		engine.statistics.score += fall * (engine.statistics.level + 1);
	}

	@Override
	public void onFirst(GameEngine engine, int playerID) {
		pCoordList.clear();
		cPiece = null;
	}

	/*
	 * Hard drop
	 */
	@Override
	public void afterHardDropFall(GameEngine engine, int playerID, int fall) {
		engine.statistics.scoreFromHardDrop += ((fall * 3) + 45) * (engine.statistics.level + 1);
		engine.statistics.score += ((fall * 3) + 45) * (engine.statistics.level + 1);

		int baseX = (16 * engine.nowPieceX) + 4 + receiver.getFieldDisplayPositionX(engine, playerID);
		int baseY = (16 * engine.nowPieceY) + 52 + receiver.getFieldDisplayPositionY(engine, playerID);
		cPiece = new Piece(engine.nowPieceObject);
		for (int i = 1; i <= fall; i++) {
			pCoordList.add(
					new int[] { engine.nowPieceX, engine.nowPieceY - i }
			);
		}
		for (int i = 0; i < cPiece.getMaxBlock(); i++) {
			if (!cPiece.big) {
				int x2 = baseX + (cPiece.dataX[cPiece.direction][i] * 16);
				int y2 = baseY + (cPiece.dataY[cPiece.direction][i] * 16);

				RendererExtension.addBlockBreakEffect(receiver, x2, y2, cPiece.block[i]);
			} else {
				int x2 = baseX + (cPiece.dataX[cPiece.direction][i] * 32);
				int y2 = baseY + (cPiece.dataY[cPiece.direction][i] * 32);

				RendererExtension.addBlockBreakEffect(receiver, x2, y2, cPiece.block[i]);
				RendererExtension.addBlockBreakEffect(receiver, x2+16, y2, cPiece.block[i]);
				RendererExtension.addBlockBreakEffect(receiver, x2, y2+16, cPiece.block[i]);
				RendererExtension.addBlockBreakEffect(receiver, x2+16, y2+16, cPiece.block[i]);
			}
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
		if((owner.replayMode == false) && (big == false) && (engine.ai == null) && (lifeOffset == 0)) {
			updateRanking(engine.statistics.score, engine.statistics.lines, engine.statistics.time, difficultySelected);

			if (playerProperties.isLoggedIn()) {
				prop.setProperty("scoretrial.playerName", playerProperties.getNameDisplay());
			}

			if(rankingRank != -1) {
				saveRanking(owner.modeConfig, engine.ruleopt.strRuleName);
				receiver.saveModeConfig(owner.modeConfig);
			}

			if(rankingRankPlayer != -1 && playerProperties.isLoggedIn()) {
				saveRankingPlayer(playerProperties, engine.ruleopt.strRuleName);
				playerProperties.saveProfileConfig();
			}
		}
	}

	/**
	 * Load settings from property file
	 * @param prop Property file
	 */
	private void loadSetting(CustomProperties prop) {
		startlevel = prop.getProperty("scoretrial.startlevel", 0);
		big = prop.getProperty("scoretrial.big", false);
		lifeOffset = prop.getProperty("scoretrial.extralives", 0);
		version = prop.getProperty("scoretrial.version", 0);
		lineClearAnimType = prop.getProperty("scoretrial.lcat", 0);
		difficultySelected = prop.getProperty("scoretrial.difficulty", 0);
	}

	/**
	 * Save settings to property file
	 * @param prop Property file
	 */
	private void saveSetting(CustomProperties prop) {
		prop.setProperty("scoretrial.startlevel", startlevel);
		prop.setProperty("scoretrial.big", big);
		prop.setProperty("scoretrial.extralives", lifeOffset);
		prop.setProperty("scoretrial.version", version);
		prop.setProperty("scoretrial.lcat", lineClearAnimType);
		prop.setProperty("scoretrial.difficulty", difficultySelected);
	}

	/**
	 * Load settings from property file
	 * @param prop Property file
	 */
	private void loadSettingPlayer(ProfileProperties prop) {
		if (!prop.isLoggedIn()) return;
		startlevel = prop.getProperty("scoretrial.startlevel", 0);
		big = prop.getProperty("scoretrial.big", false);
		lifeOffset = prop.getProperty("scoretrial.extralives", 0);
		lineClearAnimType = prop.getProperty("scoretrial.lcat", 0);
		difficultySelected = prop.getProperty("scoretrial.difficulty", 0);
	}

	/**
	 * Save settings to property file
	 * @param prop Property file
	 */
	private void saveSettingPlayer(ProfileProperties prop) {
		if (!prop.isLoggedIn()) return;
		prop.setProperty("scoretrial.startlevel", startlevel);
		prop.setProperty("scoretrial.big", big);
		prop.setProperty("scoretrial.extralives", lifeOffset);
		prop.setProperty("scoretrial.lcat", lineClearAnimType);
		prop.setProperty("scoretrial.difficulty", difficultySelected);
	}

	/**
	 * Read rankings from property file
	 * @param prop Property file
	 * @param ruleName Rule name
	 */
	@Override
	protected void loadRanking(CustomProperties prop, String ruleName) {
		for (int j = 0; j < MAX_DIFFICULTIES; j++) {
			for(int i = 0; i < RANKING_MAX; i++) {
				rankingScore[j][i] = prop.getProperty("scoretrial.ranking." + ruleName + "." + j + ".score." + i, 0);
				rankingLines[j][i] = prop.getProperty("scoretrial.ranking." + ruleName + "." + j + ".lines." + i, 0);
				rankingTime[j][i] = prop.getProperty("scoretrial.ranking." + ruleName + "." + j + ".time." + i, 0);
			}
		}
	}

	/**
	 * Save rankings to property file
	 * @param prop Property file
	 * @param ruleName Rule name
	 */
	private void saveRanking(CustomProperties prop, String ruleName) {
		for (int j = 0; j < MAX_DIFFICULTIES; j++) {
			for(int i = 0; i < RANKING_MAX; i++) {
				prop.setProperty("scoretrial.ranking." + ruleName + "." + j + ".score." + i, rankingScore[j][i]);
				prop.setProperty("scoretrial.ranking." + ruleName + "." + j + ".lines." + i, rankingLines[j][i]);
				prop.setProperty("scoretrial.ranking." + ruleName + "." + j + ".time." + i, rankingTime[j][i]);
			}
		}
	}

	/**
	 * Read rankings from property file
	 * @param prop Property file
	 * @param ruleName Rule name
	 */
	private void loadRankingPlayer(ProfileProperties prop, String ruleName) {
		if (!prop.isLoggedIn()) return;
		for (int j = 0; j < MAX_DIFFICULTIES; j++) {
			for(int i = 0; i < RANKING_MAX; i++) {
				rankingScorePlayer[j][i] = prop.getProperty("scoretrial.ranking." + ruleName + "." + j + ".score." + i, 0);
				rankingLinesPlayer[j][i] = prop.getProperty("scoretrial.ranking." + ruleName + "." + j + ".lines." + i, 0);
				rankingTimePlayer[j][i] = prop.getProperty("scoretrial.ranking." + ruleName + "." + j + ".time." + i, 0);
			}
		}
	}

	/**
	 * Save rankings to property file
	 * @param prop Property file
	 * @param ruleName Rule name
	 */
	private void saveRankingPlayer(ProfileProperties prop, String ruleName) {
		if (!prop.isLoggedIn()) return;
		for (int j = 0; j < MAX_DIFFICULTIES; j++) {
			for(int i = 0; i < RANKING_MAX; i++) {
				prop.setProperty("scoretrial.ranking." + ruleName + "." + j + ".score." + i, rankingScorePlayer[j][i]);
				prop.setProperty("scoretrial.ranking." + ruleName + "." + j + ".lines." + i, rankingLinesPlayer[j][i]);
				prop.setProperty("scoretrial.ranking." + ruleName + "." + j + ".time." + i, rankingTimePlayer[j][i]);
			}
		}
	}

	/**
	 * Update rankings
	 * @param sc Score
	 * @param li Lines
	 * @param time Time
	 */
	private void updateRanking(int sc, int li, int time, int diff) {
		rankingRank = checkRanking(sc, li, time, diff);

		if(rankingRank != -1) {
			// Shift down ranking entries
			for(int i = RANKING_MAX - 1; i > rankingRank; i--) {
				rankingScore[diff][i] = rankingScore[diff][i - 1];
				rankingLines[diff][i] = rankingLines[diff][i - 1];
				rankingTime[diff][i] = rankingTime[diff][i - 1];
			}

			// Add new data
			rankingScore[diff][rankingRank] = sc;
			rankingLines[diff][rankingRank] = li;
			rankingTime[diff][rankingRank] = time;
		}

		if (playerProperties.isLoggedIn()) {
			rankingRankPlayer = checkRankingPlayer(sc, li, time, diff);

			if(rankingRank != -1) {
				// Shift down ranking entries
				for(int i = RANKING_MAX - 1; i > rankingRankPlayer; i--) {
					rankingScorePlayer[diff][i] = rankingScorePlayer[diff][i - 1];
					rankingLinesPlayer[diff][i] = rankingLinesPlayer[diff][i - 1];
					rankingTimePlayer[diff][i] = rankingTimePlayer[diff][i - 1];
				}

				// Add new data
				rankingScorePlayer[diff][rankingRankPlayer] = sc;
				rankingLinesPlayer[diff][rankingRankPlayer] = li;
				rankingTimePlayer[diff][rankingRankPlayer] = time;
			}
		}
	}
	
	/**
	 * Calculate ranking position
	 * @param sc Score
	 * @param li Lines
	 * @param time Time
	 * @return Position (-1 if unranked)
	 */
	private int checkRanking(int sc, int li, int time, int diff) {
		for(int i = 0; i < RANKING_MAX; i++) {
			if(sc > rankingScore[diff][i]) {
				return i;
			} else if((sc == rankingScore[diff][i]) && (li > rankingLines[diff][i])) {
				return i;
			} else if((sc == rankingScore[diff][i]) && (li == rankingLines[diff][i]) && (time < rankingTime[diff][i])) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * Calculate ranking position
	 * @param sc Score
	 * @param li Lines
	 * @param time Time
	 * @return Position (-1 if unranked)
	 */
	private int checkRankingPlayer(int sc, int li, int time, int diff) {
		for(int i = 0; i < RANKING_MAX; i++) {
			if(sc > rankingScorePlayer[diff][i]) {
				return i;
			} else if((sc == rankingScorePlayer[diff][i]) && (li > rankingLinesPlayer[diff][i])) {
				return i;
			} else if((sc == rankingScorePlayer[diff][i]) && (li == rankingLinesPlayer[diff][i]) && (time < rankingTimePlayer[diff][i])) {
				return i;
			}
		}

		return -1;
	}
}
