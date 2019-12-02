package zeroxfc.nullpo.custom.modes;

import mu.nu.nullpo.game.component.BGMStatus;
import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.game.subsystem.mode.DummyMode;
import mu.nu.nullpo.util.CustomProperties;
import mu.nu.nullpo.util.GeneralUtil;
import zeroxfc.nullpo.custom.libs.*;
import zeroxfc.nullpo.custom.modes.objects.twothousandandfortyeight.*;

public class TwoThousandAndFortyEight extends DummyMode {
	// A classic hit.
	// Now to figure out how to actually render something meaningful...
	// Probably using texts of different sizes.

	private static final int CUSTOMSTATE_IDLE = 0,
							 CUSTOMSTATE_CONTROL = 1,
							 CUSTOMSTATE_MOVE = 2;

	private static final int MAX_RANKING = 10;
	private static final int MAX_GAMETYPE = 2;

	private static final int FIELD_DIMENSION = 4;

	private GameManager owner;
	private EventReceiver receiver;
	private NumberGrid mainGrid;
	private boolean endless;
	private int bgm;
	private int bg;
	private int rankingRank;
	private int[][] rankingTile;
	private int[][] rankingScore;
	private int localState;
	private int lastMove;
	private ValueWrapper score;
	private int moves;
	private int lastScoreTime;
	private int lastScore;

	private ProfileProperties playerProperties;
	private static final int headerColour = EventReceiver.COLOR_CYAN;
	private boolean showPlayerStats;
	private String PLAYER_NAME;
	private int rankingRankPlayer;
	private int[][] rankingScorePlayer;
	private int[][] rankingTilePlayer;
	
	@Override
	public String getName() {
		return "2048";
	}

	@Override
	public void playerInit(GameEngine engine, int playerID) {
		owner = engine.owner;
		receiver = engine.owner.receiver;

		lastMove = -1;
		bgm = -1;
		bg = 0;
		endless = false;
		mainGrid = null;
		localState = CUSTOMSTATE_IDLE;
		lastScoreTime = 0;
		lastScore = 0;

		rankingTile = new int[MAX_GAMETYPE][MAX_RANKING];
		rankingScore = new int[MAX_GAMETYPE][MAX_RANKING];
		rankingRank = -1;

		if (playerProperties == null) {
			playerProperties = new ProfileProperties(headerColour);

			showPlayerStats = false;
		}

		rankingRankPlayer = -1;
		rankingScorePlayer = new int[MAX_GAMETYPE][MAX_RANKING];
		rankingTilePlayer = new int[MAX_GAMETYPE][MAX_RANKING];

		engine.framecolor = GameEngine.FRAME_COLOR_YELLOW;
		engine.blockOutlineType = GameEngine.BLOCK_OUTLINE_NONE;

		if (!owner.replayMode) {
			loadSetting(owner.modeConfig);
			loadRanking(owner.modeConfig);

			if (playerProperties.isLoggedIn()) {
				loadSettingPlayer(playerProperties);
				loadRankingPlayer(playerProperties);
			}

			PLAYER_NAME = "";
		} else {
			loadSetting(owner.replayProp);
			PLAYER_NAME = owner.replayProp.getProperty("2048.playerName", "");
		}

		engine.owner.backgroundStatus.bg = bg;
	}

	@Override
	public boolean onSetting(GameEngine engine, int playerID) {
		if (mainGrid != null) mainGrid = null;

		// Menu
		if(engine.owner.replayMode == false) {
			// Configuration changes
			int change = updateCursor(engine, 2, playerID);

			if(change != 0) {
				engine.playSE("change");

				switch(engine.statc[2]) {
					case 0:
						endless = !endless;
						break;
					case 1:
						bgm += change;
						if (bgm > 15) bgm = -1;
						if (bgm < -1) bgm = 15;
						break;
					case 2:
						bg += change;
						if (bg > 19) bg = 0;
						if (bg < 0) bg = 19;

						engine.owner.backgroundStatus.bg = bg;
						break;
				}
			}

			engine.owner.backgroundStatus.bg = bg;

			// Confirm
			if(engine.ctrl.isPush(Controller.BUTTON_A) && (engine.statc[3] >= 5)) {
				engine.playSE("decide");
				saveSetting(owner.modeConfig);
				receiver.saveModeConfig(owner.modeConfig);
				return false;
			}

			// Cancel
			if(engine.ctrl.isPush(Controller.BUTTON_B)) {
				engine.quitflag = true;
			}

			// New acc
			if(engine.ctrl.isPush(Controller.BUTTON_E) && engine.ai == null) {
				playerProperties = new ProfileProperties(headerColour);
				engine.playSE("decide");

				engine.stat = GameEngine.STAT_CUSTOM;
				engine.resetStatc();
				return true;
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

	@Override
	public void renderSetting(GameEngine engine, int playerID) {
		drawMenu(engine, playerID, receiver, 0, EventReceiver.COLOR_BLUE, 0,
				"ENDLESS", GeneralUtil.getONorOFF(endless),
				"BGM", (bgm >= 0) ? String.valueOf(bgm) : "DISABLED",
				"BG", String.valueOf(bg));
	}

	@Override
	public boolean onReady(GameEngine engine, int playerID) {
		// 横溜め
		if(engine.ruleopt.dasInReady && engine.gameActive) engine.padRepeat();
		else if(engine.ruleopt.dasRedirectInDelay) { engine.dasRedirect(); }

		// Initialization
		if(engine.statc[0] == 0) {
			// fieldInitialization
			mainGrid = new NumberGrid(FIELD_DIMENSION, FIELD_DIMENSION, engine.randSeed);

			engine.ruleopt.fieldWidth = FIELD_DIMENSION;
			engine.ruleopt.fieldHeight = FIELD_DIMENSION;
			engine.ruleopt.fieldHiddenHeight = 0;
			engine.displaysize = 1;

			engine.ruleopt.nextDisplay = 0;
			engine.ruleopt.holdEnable = false;

			engine.fieldWidth = engine.ruleopt.fieldWidth;
			engine.fieldHeight = engine.ruleopt.fieldHeight;
			engine.fieldHiddenHeight = engine.ruleopt.fieldHiddenHeight;
			engine.field = new Field(engine.fieldWidth, engine.fieldHeight, engine.fieldHiddenHeight, true);

			lastScoreTime = 120;
			lastScore = 0;

			if(!engine.readyDone) {
				//  button input状態リセット
				engine.ctrl.reset();
				// ゲーム中 flagON
				engine.gameActive = true;
				engine.gameStarted = true;
				engine.isInGame = true;
			}

			rankingRank = -1;
		}

		// READY音
		if(engine.statc[0] == engine.readyStart) engine.playSE("ready");

		// GO音
		if(engine.statc[0] == engine.goStart) engine.playSE("go");

		// 開始
		if(engine.statc[0] >= engine.goEnd) {
			if(!engine.readyDone) engine.owner.bgmStatus.bgm = -1;
			if(engine.owner.mode != null) engine.owner.mode.startGame(engine, playerID);
			engine.owner.receiver.startGame(engine, playerID);
			engine.stat = GameEngine.STAT_CUSTOM;
			localState = CUSTOMSTATE_CONTROL;
			engine.timerActive = true;
			engine.resetStatc();
			if(!engine.readyDone) {
				engine.startTime = System.nanoTime();
				//startTime = System.nanoTime()/1000000L;
			}
			engine.readyDone = true;
			return true;
		}

		engine.statc[0]++;

		return true;
	}

	@Override
	public void startGame(GameEngine engine, int playerID) {
		engine.owner.bgmStatus.bgm = bgm;
	}

	/*
	 * Called after every frame
	 */
	@Override
	public void onLast(GameEngine engine, int playerID) {
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
		if(engine.lives <= 0) {
			// もう復活できないとき
			if(engine.statc[0] == 0) {
				engine.gameEnded();
				engine.blockShowOutlineOnly = false;
				if(owner.getPlayers() < 2) owner.bgmStatus.bgm = BGMStatus.BGM_NOTHING;

				if(engine.field.isEmpty()) {
					engine.statc[0] = engine.field.getHeight() + 1;
				} else {
					engine.resetFieldVisible();
				}
			}

			if(engine.statc[0] < engine.field.getHeight() + 1) {
				for(int i = 0; i < engine.field.getWidth(); i++) {
					if(engine.field.getBlockColor(i, engine.field.getHeight() - engine.statc[0]) != Block.BLOCK_COLOR_NONE) {
						Block blk = engine.field.getBlock(i, engine.field.getHeight() - engine.statc[0]);

						if(blk != null) {
							if (blk.color > Block.BLOCK_COLOR_NONE) {
								if(!blk.getAttribute(Block.BLOCK_ATTRIBUTE_GARBAGE)) {
									blk.color = Block.BLOCK_COLOR_GRAY;
									blk.setAttribute(Block.BLOCK_ATTRIBUTE_GARBAGE, true);
								}
								blk.darkness = 0.3f;
								blk.elapsedFrames = -1;
							}
						}
					}
				}
				engine.statc[0]++;
			} else if(engine.statc[0] == engine.field.getHeight() + 1) {
				engine.playSE("gameover");
				engine.statc[0]++;
			} else if(engine.statc[0] < engine.field.getHeight() + 1 + 180) {
				if((engine.statc[0] >= engine.field.getHeight() + 1 + 60) && (engine.ctrl.isPush(Controller.BUTTON_A))) {
					engine.statc[0] = engine.field.getHeight() + 1 + 180;
				}

				engine.statc[0]++;
			} else {
				if(!owner.replayMode || owner.replayRerecord) owner.saveReplay();

				for(int i = 0; i < owner.getPlayers(); i++) {
					if((i == playerID) || (engine.gameoverAll)) {
						if(owner.engine[i].field != null) {
							owner.engine[i].field.reset();
						}
						owner.engine[i].resetStatc();
						owner.engine[i].stat = GameEngine.STAT_RESULT;
					}
				}
			}
		} else {
			// 復活できるとき
			if(engine.statc[0] == 0) {
				engine.blockShowOutlineOnly = false;
				engine.playSE("died");

				engine.resetFieldVisible();

				for(int i = (engine.field.getHiddenHeight() * -1); i < engine.field.getHeight(); i++) {
					for(int j = 0; j < engine.field.getWidth(); j++) {
						if(engine.field.getBlockColor(j, i) != Block.BLOCK_COLOR_NONE) {
							engine.field.setBlockColor(j, i, Block.BLOCK_COLOR_GRAY);
						}
					}
				}

				engine.statc[0] = 1;
			}

			if(!engine.field.isEmpty()) {
				engine.field.pushDown();
			} else if(engine.statc[1] < engine.getARE()) {
				engine.statc[1]++;
			} else {
				engine.lives--;
				engine.resetStatc();
				engine.stat = GameEngine.STAT_CUSTOM;
			}
		}
		return true;
	}

	@Override
	public boolean onCustom(GameEngine engine, int playerID) {
		if (engine.gameActive) {
			boolean updateTimer = false;

			engine.rainbowAnimate = true;

			switch (localState) {
			    case CUSTOMSTATE_CONTROL:
			        updateTimer = statControl(engine, playerID);
			        break;
				case CUSTOMSTATE_MOVE:
					updateTimer = statMove(engine, playerID);
					break;
			    default:
			        break;
			}

			if(lastScoreTime < 120) lastScoreTime++;

			if (updateTimer) engine.statc[0]++;
		} else {
			showPlayerStats = false;

			engine.isInGame = true;

			boolean s = playerProperties.loginScreen.updateScreen(engine, playerID);
			if (playerProperties.isLoggedIn()) {
				loadRankingPlayer(playerProperties);
				loadSettingPlayer(playerProperties);
			}

			if (engine.stat == GameEngine.STAT_SETTING) engine.isInGame = false;
		}
		return true;
	}

	private boolean statControl(GameEngine engine, int playerID) {
		lastMove = -1;

		if (!mainGrid.movesPossible()) {

			if (mainGrid.getMaxSquare() < 2048) {
				engine.resetStatc();
				engine.gameEnded();
				localState = CUSTOMSTATE_IDLE;
				engine.stat = GameEngine.STAT_GAMEOVER;
			} else {
				engine.resetStatc();
				engine.gameEnded();
				engine.ending = 1;

				localState = CUSTOMSTATE_IDLE;
				engine.stat = GameEngine.STAT_EXCELLENT;
			}

			engine.rainbowAnimate = false;

			return false;
		}

		if (engine.ctrl.isPush(Controller.BUTTON_LEFT) && !engine.ctrl.isPush(Controller.BUTTON_RIGHT) &&
			!engine.ctrl.isPush(Controller.BUTTON_UP) && !engine.ctrl.isPush(Controller.BUTTON_DOWN)) {
			lastMove = NumberGrid.DIRECTION_LEFT;

			localState = CUSTOMSTATE_MOVE;
			engine.resetStatc();
			return false;
		} else if (!engine.ctrl.isPush(Controller.BUTTON_LEFT) && engine.ctrl.isPush(Controller.BUTTON_RIGHT) &&
				   !engine.ctrl.isPush(Controller.BUTTON_UP) && !engine.ctrl.isPush(Controller.BUTTON_DOWN)) {
			lastMove = NumberGrid.DIRECTION_RIGHT;

			localState = CUSTOMSTATE_MOVE;
			engine.resetStatc();
			return false;
		} else if (!engine.ctrl.isPush(Controller.BUTTON_LEFT) && !engine.ctrl.isPush(Controller.BUTTON_RIGHT) &&
				   engine.ctrl.isPush(Controller.BUTTON_UP) && !engine.ctrl.isPush(Controller.BUTTON_DOWN)) {
			lastMove = NumberGrid.DIRECTION_UP;

			localState = CUSTOMSTATE_MOVE;
			engine.resetStatc();
			return false;
		} else if (!engine.ctrl.isPush(Controller.BUTTON_LEFT) && !engine.ctrl.isPush(Controller.BUTTON_RIGHT) &&
				   !engine.ctrl.isPush(Controller.BUTTON_UP) && engine.ctrl.isPush(Controller.BUTTON_DOWN)) {
			lastMove = NumberGrid.DIRECTION_DOWN;

			localState = CUSTOMSTATE_MOVE;
			engine.resetStatc();
			return false;
		}


	    return false;
	}

	private boolean statMove(GameEngine engine, int playerID) {
		if (engine.statc[0] == 0) {
			moves = 0;
			score = new ValueWrapper(0);

			for (int y = 0; y < FIELD_DIMENSION; y++) {
				for (int x = 0; x < FIELD_DIMENSION; x++) {
					mainGrid.grid[y][x].setMerged(false);
				}
			}
		}

		if (engine.statc[0] % 3 == 0) {
			boolean moved = mainGrid.moveSquares(engine, receiver, playerID, lastMove, score);
			if (moved) moves++;

			if (!moved) {
				if (score.valueInt > 0) {
					lastScoreTime = 0;
					lastScore = engine.statistics.score;
					engine.statistics.score += score.valueInt;
				}

				if (moves > 0) mainGrid.createSquare();

				localState = CUSTOMSTATE_CONTROL;
				engine.resetStatc();
				return false;
			}
		}

		return true;
	}

	private void updateEngineGrid(GameEngine engine) {
		Block[][] blockGrid = mainGrid.getDrawGrid(engine);

		for (int y = 0; y < FIELD_DIMENSION; y++) {
			for (int x = 0; x < FIELD_DIMENSION; x++) {
				if (engine.field.getBlock(x, y).blockToChar() != blockGrid[y][x].blockToChar()) {
					engine.field.getBlock(x, y).copy(blockGrid[y][x]);
				}
			}
		}
	}

	@Override
	public void renderLast(GameEngine engine, int playerID) {
		if(owner.menuOnly) return;

		int base = -2;

		receiver.drawScoreFont(engine, playerID, base, 0, getName(), EventReceiver.COLOR_BLUE);
		receiver.drawScoreFont(engine, playerID, base, 1, "(" + (endless ? "ENDLESS" : "NORMAL") + " GAME)", EventReceiver.COLOR_BLUE);

		if( (engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (owner.replayMode == false)) ) {
			if (!owner.replayMode) {
				float scale = (receiver.getNextDisplayType() == 2) ? 0.5f : 1.0f;
				int topY = (receiver.getNextDisplayType() == 2) ? 6 : 4;
				receiver.drawScoreFont(engine, playerID, base + 3, topY-1, "MAXTILE SCORE", EventReceiver.COLOR_BLUE, scale);

				if (showPlayerStats) {
					for(int i = 0; i < MAX_RANKING; i++) {
						receiver.drawScoreFont(engine, playerID,  base, topY+i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW, scale);
						receiver.drawScoreFont(engine, playerID,  base + 3, topY+i, String.valueOf(rankingTilePlayer[endless ? 1 : 0][i]), (i == rankingRankPlayer), scale);
						receiver.drawScoreFont(engine, playerID,  base + 11, topY+i, String.valueOf(rankingScorePlayer[endless ? 1 : 0][i]), (i == rankingRankPlayer), scale);
					}

					receiver.drawScoreFont(engine, playerID, base, topY + MAX_RANKING + 1, "PLAYER SCORES", EventReceiver.COLOR_BLUE);
					receiver.drawScoreFont(engine, playerID, base, topY + MAX_RANKING + 2, playerProperties.getNameDisplay(), EventReceiver.COLOR_WHITE, 2f);

					receiver.drawScoreFont(engine, playerID, base, topY + MAX_RANKING + 5, "F:SWITCH RANK SCREEN", EventReceiver.COLOR_GREEN);
				} else {
					for(int i = 0; i < MAX_RANKING; i++) {
						receiver.drawScoreFont(engine, playerID,  base, topY+i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW, scale);
						receiver.drawScoreFont(engine, playerID,  base + 3, topY+i, String.valueOf(rankingTile[endless ? 1 : 0][i]), (i == rankingRank), scale);
						receiver.drawScoreFont(engine, playerID,  base + 11, topY+i, String.valueOf(rankingScore[endless ? 1 : 0][i]), (i == rankingRank), scale);
					}

					receiver.drawScoreFont(engine, playerID, base, topY + MAX_RANKING + 1, "LOCAL SCORES", EventReceiver.COLOR_BLUE);
					if (!playerProperties.isLoggedIn()) receiver.drawScoreFont(engine, playerID, base, topY + MAX_RANKING + 2, "(NOT LOGGED IN)\n(E:LOG IN)");
					if (playerProperties.isLoggedIn()) receiver.drawScoreFont(engine, playerID, base, topY + MAX_RANKING + 5, "F:SWITCH RANK SCREEN", EventReceiver.COLOR_GREEN);

				}
			}
		} else if (engine.stat == GameEngine.STAT_CUSTOM && !engine.gameActive) {
			playerProperties.loginScreen.renderScreen(receiver, engine, playerID);
		} else {
			receiver.drawScoreFont(engine, playerID, base, 3, "SCORE", EventReceiver.COLOR_BLUE);

			int sc;
			if (lastScoreTime <= 8) {
				sc = (int) Interpolation.sineStep(lastScore, engine.statistics.score, (double)lastScoreTime / 8.0);
			} else {
				sc = engine.statistics.score;
			}

			if (lastScoreTime < 120) {
				receiver.drawScoreFont(engine, playerID, base, 4, sc + "(+" + (engine.statistics.score - lastScore) + ")");
			} else {
				receiver.drawScoreFont(engine, playerID, base, 4, String.valueOf(sc));
			}

			receiver.drawScoreFont(engine, playerID, base, 6, "TIME", EventReceiver.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, base, 7, GeneralUtil.getTime(engine.statistics.time));

			if (playerProperties.isLoggedIn() || PLAYER_NAME.length() > 0) {
				receiver.drawScoreFont(engine, playerID, base, 12, "PLAYER", EventReceiver.COLOR_BLUE);
				receiver.drawScoreFont(engine, playerID, base, 13, owner.replayMode ? PLAYER_NAME : playerProperties.getNameDisplay(), EventReceiver.COLOR_WHITE, 2f);
			}

			if (mainGrid != null) {
				receiver.drawScoreFont(engine, playerID, base, 9, "MAX TILE", EventReceiver.COLOR_BLUE);
				receiver.drawScoreFont(engine, playerID, base, 10, String.valueOf(mainGrid.getMaxSquare()) + "/2048");

				if (engine.stat != GameEngine.STAT_GAMEOVER) updateEngineGrid(engine);

				double proportion = (double)(log2nlz(mainGrid.getMaxSquare()) - 1) / 10.0;
				if (proportion > 1.0) proportion = 1.0;
				engine.meterValue = (int)(proportion * receiver.getMeterMax(engine));
				engine.meterColor = GameEngine.METER_COLOR_RED;
				if(mainGrid.getMaxSquare() >= 64) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
				if(mainGrid.getMaxSquare() >= 256) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
				if(mainGrid.getMaxSquare() >= 1024) engine.meterColor = GameEngine.METER_COLOR_GREEN;

				if (engine.stat != GameEngine.STAT_RESULT) {
					for (int y = 0; y < FIELD_DIMENSION; y++) {
						for (int x = 0; x < FIELD_DIMENSION; x++) {
							int value = mainGrid.getCell(x, y).getValue();

							if (value > 0) {
								String str = String.valueOf(value);
								int length = str.length();

								float scale = 2f / length;
								if (scale > 1f) scale = 1f;

								int offsetY = (int)( (32 - (16 * scale)) / 2 );
								int offsetX = 0;
								if (length < 2) offsetX = 8;

								int lX = receiver.getFieldDisplayPositionX(engine, playerID) + 4 + (x * 32);
								int lY = receiver.getFieldDisplayPositionY(engine, playerID) + 52 + (y * 32);

								if (value >= 2048) {
									receiver.drawDirectFont(engine, playerID, lX + offsetX, lY + offsetY, str, EventReceiver.COLOR_ORANGE, scale);
								} else if (value >= 256) {
									receiver.drawDirectFont(engine, playerID, lX + offsetX, lY + offsetY, str, EventReceiver.COLOR_YELLOW, scale);
								} else {
									receiver.drawDirectFont(engine, playerID, lX + offsetX, lY + offsetY,str, EventReceiver.COLOR_WHITE, scale);
								}
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void renderResult(GameEngine engine, int playerID) {
		receiver.drawMenuFont(engine, playerID, 0, 0, "MAX TILE", EventReceiver.COLOR_BLUE);
		receiver.drawMenuFont(engine, playerID, 0, 1, String.format("%8s", String.valueOf(mainGrid.getMaxSquare())));

		receiver.drawMenuFont(engine, playerID, 0, 2, "SCORE", EventReceiver.COLOR_BLUE);
		receiver.drawMenuFont(engine, playerID, 0, 3, String.format("%8s", String.valueOf(engine.statistics.score)));

		receiver.drawMenuFont(engine, playerID, 0, 4, "TIME", EventReceiver.COLOR_BLUE);
		receiver.drawMenuFont(engine, playerID, 0, 5, String.format("%8s", GeneralUtil.getTime(engine.statistics.time)));
	}

	/*
	 * RANK AND SETTINGS
	 */

	/**
	 * Read rankings from property file
	 * @param prop Property file
	 */
	protected void loadRanking(CustomProperties prop) {
		for(int i = 0; i < MAX_RANKING; i++) {
			for(int j = 0; j < MAX_GAMETYPE; j++) {
				rankingScore[j][i] = prop.getProperty("2048.ranking." + j + ".score." + i, 0);
				rankingTile[j][i] = prop.getProperty("2048.ranking." + j + ".maxTile." + i, 0);
			}
		}
	}

	/**
	 * Save rankings to property file
	 * @param prop Property file
	 */
	private void saveRanking(CustomProperties prop) {
		for(int i = 0; i < MAX_RANKING; i++) {
			for(int j = 0; j < MAX_GAMETYPE; j++) {
				prop.setProperty("2048.ranking." + j + ".score." + i, rankingScore[j][i]);
				prop.setProperty("2048.ranking." + j + ".maxTile." + i, rankingTile[j][i]);
			}
		}
	}

	/**
	 * Read rankings from property file
	 * @param prop Property file
	 */
	protected void loadRankingPlayer(ProfileProperties prop) {
		if (!prop.isLoggedIn()) return;
		for(int i = 0; i < MAX_RANKING; i++) {
			for(int j = 0; j < MAX_GAMETYPE; j++) {
				rankingScorePlayer[j][i] = prop.getProperty("2048.ranking." + j + ".score." + i, 0);
				rankingTilePlayer[j][i] = prop.getProperty("2048.ranking." + j + ".maxTile." + i, 0);
			}
		}
	}

	/**
	 * Save rankings to property file
	 * @param prop Property file
	 */
	private void saveRankingPlayer(ProfileProperties prop) {
		if (!prop.isLoggedIn()) return;
		for(int i = 0; i < MAX_RANKING; i++) {
			for(int j = 0; j < MAX_GAMETYPE; j++) {
				prop.setProperty("2048.ranking." + j + ".score." + i, rankingScorePlayer[j][i]);
				prop.setProperty("2048.ranking." + j + ".maxTile." + i, rankingTilePlayer[j][i]);
			}
		}
	}

	private void updateRanking(int sc, int type, int tile) {
		rankingRank = checkRanking(tile, type, sc);

		if(rankingRank != -1) {
			// Shift down ranking entries
			for(int i = MAX_RANKING - 1; i > rankingRank; i--) {
				rankingScore[type][i] = rankingScore[type][i - 1];
				rankingTile[type][i] = rankingTile[type][i - 1];
			}

			// Add new data
			rankingScore[type][rankingRank] = sc;
			rankingTile[type][rankingRank] = tile;
		}

		if (playerProperties.isLoggedIn()) {
			rankingRankPlayer = checkRankingPlayer(tile, type, sc);

			if(rankingRankPlayer != -1) {
				// Shift down ranking entries
				for(int i = MAX_RANKING - 1; i > rankingRankPlayer; i--) {
					rankingScorePlayer[type][i] = rankingScorePlayer[type][i - 1];
					rankingTilePlayer[type][i] = rankingTilePlayer[type][i - 1];
				}

				// Add new data
				rankingScorePlayer[type][rankingRankPlayer] = sc;
				rankingTilePlayer[type][rankingRankPlayer] = tile;
			}
		}
	}

	private int checkRanking(int tile, int type, int sc) {
		for(int i = 0; i < MAX_RANKING; i++) {
			if(tile > rankingTile[type][i]) {
				return i;
			} else if (tile == rankingTile[type][i] && sc > rankingScore[type][i]) {
				return i;
			}
		}

		return -1;
	}

	private int checkRankingPlayer(int tile, int type, int sc) {
		for(int i = 0; i < MAX_RANKING; i++) {
			if(tile > rankingTilePlayer[type][i]) {
				return i;
			} else if (tile == rankingTilePlayer[type][i] && sc > rankingScorePlayer[type][i]) {
				return i;
			}
		}

		return -1;
	}

	/*
	 * Called when saving replay
	 */
	@Override
	public void saveReplay(GameEngine engine, int playerID, CustomProperties prop) {
		saveSetting(prop);

		if((!owner.replayMode)) {
			updateRanking(engine.statistics.score, endless ? 1 : 0, mainGrid.getMaxSquare());

			if (playerProperties.isLoggedIn()) {
				prop.setProperty("2048.playerName", playerProperties.getNameDisplay());
			}

			if(rankingRank != -1) {
				saveRanking(owner.modeConfig);
				receiver.saveModeConfig(owner.modeConfig);
			}

			if(rankingRankPlayer != -1 && playerProperties.isLoggedIn()) {
				saveRankingPlayer(playerProperties);
				playerProperties.saveProfileConfig();
			}
		}
	}

	/**
	 * Load settings from property file
	 * @param prop Property file
	 */
	private void loadSetting(CustomProperties prop) {
		endless = prop.getProperty("2048.endless", false);
		bgm = prop.getProperty("2048.bgm", 0);
		bg = prop.getProperty("2048.bg", 0);
	}

	/**
	 * Save settings to property file
	 * @param prop Property file
	 */
	private void saveSetting(CustomProperties prop) {
		prop.setProperty("2048.endless", endless);
		prop.setProperty("2048.bgm", bgm);
		prop.setProperty("2048.bg", bg);
	}

	/**
	 * Load settings from property file
	 * @param prop Property file
	 */
	private void loadSettingPlayer(ProfileProperties prop) {
		if (!prop.isLoggedIn()) return;
		endless = prop.getProperty("2048.endless", false);
		bgm = prop.getProperty("2048.bgm", 0);
		bg = prop.getProperty("2048.bg", 0);
	}

	/**
	 * Save settings to property file
	 * @param prop Property file
	 */
	private void saveSettingPlayer(ProfileProperties prop) {
		if (!prop.isLoggedIn()) return;
		prop.setProperty("2048.endless", endless);
		prop.setProperty("2048.bgm", bgm);
		prop.setProperty("2048.bg", bg);
	}

	private static int log2nlz(int bits)
	{
		if( bits == 0 )
			return 0; // or throw exception
		return 31 - Integer.numberOfLeadingZeros(bits);
	}
}
