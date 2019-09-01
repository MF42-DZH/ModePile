package zeroxfc.nullpo.custom.modes;

import mu.nu.nullpo.game.component.BGMStatus;
import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.util.CustomProperties;
import mu.nu.nullpo.util.GeneralUtil;
import zeroxfc.nullpo.custom.libs.*;
import zeroxfc.nullpo.custom.modes.objects.expressshipping.*;
import zeroxfc.nullpo.custom.modes.objects.gemswap.Effect;
import zeroxfc.nullpo.custom.modes.objects.gemswap.ScorePopup;

import java.util.ArrayList;
import java.util.Random;

public class ExpressShipping extends PuzzleGameEngine {
	// This is literally Puzzle Express.

	// private static Logger log = Logger.getLogger(ExpressShipping.class);

	/** Local states for onCustom */
	private static final int CUSTOMSTATE_IDLE = 0,
	                         CUSTOMSTATE_INGAME = 1,
	                         CUSTOMSTATE_RESULTS = 2;

	private static final int CONVEYOR_SPEED = 1;
	private static final int CONVEYOR_ANIMATION_FRAMES = 6;
	private static final int BOUNDING_BOX_PADDING = 4;
	private static final int SPAWN_TICK = 150;
	private static final double SPAWN_CHANCE = (2.0 / 3.0);

	private static final int POWERUP_NONE = 0,
	                         POWERUP_DESTROY_ONE = 1,
	                         POWERUP_DESTROY_TWO = 2,
	                         POWERUP_DESTROY_THREE = 3,
	                         POWERUP_ADD_ONE_MONO = 4,
	                         POWERUP_ADD_TWO_MONO = 5,
	                         POWERUP_ADD_THREE_MONO = 6,
	                         POWERUP_MULTIPLIER_TWO = 7,
	                         POWERUP_MULTIPLIER_THREE = 8,
	                         POWERUP_MULTIPLIER_FOUR = 9,
	                         POWERUP_FILLROW = 10;

	private static final int POWERUP_COUNT = 10;
	private static final double powerupChance = (1.0 / 80.0);
	private static final double powerExponent = 1.05;

	// Piece weight table.
	private static final int[][] PIECE_WEIGHTS = {
			{ 0, 3, 3, 3, 6, 0, 0, 0, 0, 4, 4 },
			{ 0, 2, 2, 2, 4, 0, 0, 0, 0, 4, 4 },
			{ 0, 3, 4, 4, 8, 0, 0, 0, 0, 6, 6 },
			{ 0, 5, 5, 5, 10, 3, 3, 0, 0, 10, 8 },
			{ 0, 6, 8, 8, 12, 5, 5, 2, 2, 12, 10 },
			{ 0, 10, 12, 12, 16, 8, 8, 5, 5, 14, 12 },
			{ 0, 10, 12, 12, 16, 8, 8, 5, 5, 14, 12, 2, 2, 1, 1, 2, 2 },
			{ 0, 12, 14, 14, 18, 10, 10, 8, 8, 16, 14, 3, 3, 2, 2, 3, 3 },
			{ 0, 16, 18, 18, 22, 14, 14, 12, 12, 20, 18, 10, 10, 6, 4, 8, 8 },
			{ 0, 16, 18, 18, 22, 16, 16, 14, 14, 20, 18, 12, 12, 8, 6, 10, 10, 6, 6 },
	};

	// Monomino start counters.
	private static final int[] MONOMINO_START_AMOUNTS = {
			10, 7, 7, 6, 6, 5, 5, 4, 4, 3
	};

	// Field quota per level
	private static final int[] LEVEL_FIELD_QUOTA = {
			1, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6, 6, 7, 7, 7, 8, 8, 8, 9, 9, 9, 10
	};

	// Level bonuses
	// Calculation: Fields Filled * Monominoes * Bonus. Minimum Base.
	private static final int[] LEVEL_END_BASE_BONUS = {
			3000, 3500, 4000, 5000, 6000, 7500, 9000, 11000, 13000, 15000, 17500, 20000, 23000, 26000, 29500, 33000, 37000, 41000, 45500,
			50000
	};

	private static final int NEW_MONOMINO_SCORE = 3000;

	private static final int RANKING_MAX = 10;

	private GameManager owner;
	private EventReceiver receiver;
	private MouseParser mouseControl;
	private Random boardRandomiser;
	private Random miscRandomiser;
	private Random powerupRandomiser;
	private ResourceHolderCustomAssetExtension customHolder;
	private WeightedRandomiser pieceRandomiser;
	private ArrayList<GamePiece> conveyorBelt;
	private ArrayList<Integer> history;
	private GamePiece selectedPiece;
	private GamePiece monominoConveyorBelt;
	private Effect[] scorePopups;
	private int monominoesLeft;
	private int fieldsLeft;
	private int localState;
	private int engineTick;
	private int conveyorFrame;
	private int bgm;
	private int bg;
	private int cargoReturnCooldown;
	private int endBonus;
	private int spawnTime;
	private int[] mouseCoords;
	private int spawnTries;
	private int scoreTowardsNewMonomino;
	private int[] rankingScore, rankingLevel;
	private int rankingRank;
	private int lastScore, scGetTime;

	private ProfileProperties playerProperties;
	private static final int headerColour = EventReceiver.COLOR_PINK;
	private boolean showPlayerStats;
	private int[] rankingScorePlayer, rankingLevelPlayer;
	private int rankingRankPlayer;

	@Override
	public String getName() {
		return "EXPRESS SHIPPING";
	}

	@Override
	public void playerInit(GameEngine engine, int playerID) {
		owner = engine.owner;
		receiver = engine.owner.receiver;
		mouseControl = new MouseParser();

		localState = CUSTOMSTATE_IDLE;
		conveyorBelt = new ArrayList<>();
		history = new ArrayList<>();
		selectedPiece = null;
		monominoConveyorBelt = null;
		engineTick = 0;
		conveyorFrame = 0;
		bg = 0;
		bgm = 0;
		spawnTime = 0;
		cargoReturnCooldown = 0;
		monominoesLeft = 0;
		spawnTries = 0;
		scoreTowardsNewMonomino = 0;
		scorePopups = new Effect[32];
		rankingRank = -1;
		rankingLevel = new int[RANKING_MAX];
		rankingScore = new int[RANKING_MAX];
		lastScore = 0;
		scGetTime = 0;

		if (playerProperties == null) {
			playerProperties = new ProfileProperties(headerColour);

			showPlayerStats = false;

			rankingRankPlayer = -1;
			rankingLevelPlayer = new int[RANKING_MAX];
			rankingScorePlayer = new int[RANKING_MAX];
		}

		customHolder = new ResourceHolderCustomAssetExtension(3);
		customHolder.loadImage("res/graphics/conveyor.png", "conveyor");
		customHolder.loadImage("res/graphics/conveyorreverse.png", "conveyorreverse");
		customHolder.loadImage("res/graphics/cargoreturn.png", "cargoreturn");

		loadSetting(owner.modeConfig);
		loadRanking(owner.modeConfig);

		if (playerProperties.isLoggedIn()) {
			loadSettingPlayer(playerProperties);
			loadRankingPlayer(playerProperties);
		}
	}

	private void resetHistory() {
		history.clear();

		history.add(-1);
		history.add(-1);
		history.add(-1);
		history.add(-1);
	}

	private void appendHistory(int e) {
		history.remove(0);
		history.add(e);
	}

	@Override
	public boolean onSetting(GameEngine engine, int playerID) {
		owner.backgroundStatus.bg = (bg == -1) ? 0 : bg;

		// Menu
		if(!engine.owner.replayMode) {
			// Configuration changes
			int change = updateCursor(engine, 1, playerID);

			if(change != 0) {
				engine.playSE("change");

				switch(engine.statc[2]) {
					case 0:
						bg += change;
						if (bg > 19) bg = -1;
						if (bg < -1) bg = 19;
						break;
					case 1:
						bgm += change;
						if (bgm > 15) bgm = -1;
						if (bgm < -1) bgm = 15;
						break;
				}
			}

			if (bg != -1) engine.owner.backgroundStatus.bg = bg;

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
				return false;
			}

			// Cancel
			if(engine.ctrl.isPush(Controller.BUTTON_B)) {
				engine.quitflag = true;
				playerProperties = new ProfileProperties(headerColour);
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

			if (engine.statc[3] >= 60) {
				return false;
			}
		}

		return true;
	}

	@Override
	public void renderSetting(GameEngine engine, int playerID) {
		drawMenu(engine, playerID, receiver, 0, EventReceiver.COLOR_BLUE, 0,
				"BG", (bg >= 0) ? String.valueOf(bg) : "AUTO",
				"BGM", (bgm >= 0) ? String.valueOf(bgm) : "DISABLED");
	}

	@Override
	public boolean onReady(GameEngine engine, int playerID) {
		// 横溜め
		if(engine.ruleopt.dasInReady && engine.gameActive) engine.padRepeat();
		else if(engine.ruleopt.dasRedirectInDelay) { engine.dasRedirect(); }

		// Initialization
		if(engine.statc[0] == 0) {
			// fieldInitialization
			scorePopups = new Effect[32];
			boardRandomiser = new Random(engine.randSeed);
			miscRandomiser = new Random(engine.randSeed + 1);
			pieceRandomiser = new WeightedRandomiser(PIECE_WEIGHTS[0], engine.randSeed + 2);
			powerupRandomiser = new Random(engine.randSeed + 3);
			engineTick = 0;
			conveyorFrame = 0;
			cargoReturnCooldown = 0;
			spawnTime = 0;
			spawnTries = 0;
			monominoesLeft = MONOMINO_START_AMOUNTS[0];
			fieldsLeft = LEVEL_FIELD_QUOTA[0];
			scoreTowardsNewMonomino = 0;
			lastScore = 0;
			scGetTime = 0;
			conveyorBelt.clear();
			resetHistory();

			engine.field = Boards.getBoard(boardRandomiser.nextInt(Boards.Boards.length), engine.getSkin());
			for (int y = 0; y < engine.field.getHeight(); y++) {
				engine.field.setLineFlag(y, false);
			}

			engine.framecolor = boardRandomiser.nextInt(8);

			engine.ruleopt.lockflash = 4;
			engine.ruleopt.nextDisplay = 0;
			engine.ruleopt.holdEnable = false;

			engine.ruleopt.fieldWidth = engine.field.getWidth();
			engine.ruleopt.fieldHeight = engine.field.getHeight();
			engine.ruleopt.fieldHiddenHeight = 0;
			engine.fieldWidth = engine.field.getWidth();
			engine.fieldHeight = engine.field.getHeight();
			engine.fieldHiddenHeight = 0;

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
			localState = CUSTOMSTATE_INGAME;
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
				// XXX: DO NOT SAVE REPLAY; MOUSE INCOMPATIBLE.
				// if(!owner.replayMode || owner.replayRerecord) owner.saveReplay();

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

	private void generateNewField(GameEngine engine) {
		engine.field = Boards.getBoard(boardRandomiser.nextInt(Boards.Boards.length), engine.getSkin());
		engine.framecolor = boardRandomiser.nextInt(8);

		engine.ruleopt.nextDisplay = 0;
		engine.ruleopt.holdEnable = false;

		engine.ruleopt.fieldWidth = engine.field.getWidth();
		engine.ruleopt.fieldHeight = engine.field.getHeight();
		engine.ruleopt.fieldHiddenHeight = 0;

		for (int y = 0; y < engine.field.getHeight(); y++) {
			engine.field.setLineFlag(y, false);
		}

		engine.fieldWidth = engine.field.getWidth();
		engine.fieldHeight = engine.field.getHeight();
		engine.fieldHiddenHeight = 0;
	}

	private void levelUp(GameEngine engine) {
		engineTick = 0;
		spawnTime = 0;
		cargoReturnCooldown = 0;
		scoreTowardsNewMonomino = 0;

		selectedPiece = null;
		monominoConveyorBelt = null;

		conveyorBelt.clear();
		resetHistory();
		engine.statistics.level++;

		int effectiveLevel = engine.statistics.level;
		if (effectiveLevel >= PIECE_WEIGHTS.length) effectiveLevel = PIECE_WEIGHTS.length - 1;
		pieceRandomiser.setWeights(PIECE_WEIGHTS[effectiveLevel]);

		effectiveLevel = engine.statistics.level;
		if (effectiveLevel >= MONOMINO_START_AMOUNTS.length) effectiveLevel = MONOMINO_START_AMOUNTS.length - 1;
		monominoesLeft = MONOMINO_START_AMOUNTS[effectiveLevel];

		effectiveLevel = engine.statistics.level;
		if (effectiveLevel >= LEVEL_FIELD_QUOTA.length) effectiveLevel = LEVEL_FIELD_QUOTA.length - 1;
		fieldsLeft = LEVEL_FIELD_QUOTA[effectiveLevel];

		if (bg == -1) owner.backgroundStatus.bg = (engine.statistics.level / 2) % 20;
		generateNewField(engine);
	}

	@Override
	public boolean onCustom(GameEngine engine, int playerID) {
		if (engine.gameActive) {
			boolean updateTime = false;

			conveyorFrame = (conveyorFrame + 1) % CONVEYOR_ANIMATION_FRAMES;

			mouseControl.update();
			mouseCoords = mouseControl.getMouseCoordinates();

			switch (localState) {
				case CUSTOMSTATE_INGAME:
					updateTime = statIngame(engine, playerID);
					break;
				case CUSTOMSTATE_RESULTS:
					updateTime = statResults(engine, playerID);
					break;
				default:
					break;
			}

			if (engine.stat == GameEngine.STAT_GAMEOVER) {
				updateRanking(engine.statistics.score, engine.statistics.level);
				if (rankingRank != -1) saveRanking(owner.modeConfig);
				if (rankingRank != -1) receiver.saveModeConfig(owner.modeConfig);
				if (rankingRankPlayer != -1 && playerProperties.isLoggedIn()) saveRankingPlayer(playerProperties);
				if (rankingRankPlayer != -1 && playerProperties.isLoggedIn()) playerProperties.saveProfileConfig();
			}

			if (updateTime) engine.statc[0]++;
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

	/*
	 * CARGO RETURN REGION:
	 * (49, 304) to (134, 350).
	 *
	 * FIELD STUFF:
	 * Standard field location parsing. Use getWidth() and getHeight().
	 */

	/*
	 * Now how to check conveyor collisions?
	 * Maybe use the bounding box in GamePiece.
	 * Hm...
	 *
	 * IDEA 1: make array of bounding boxes.
	 *
	 * !! THIS IS WORKING NOW !!
	 */

	private boolean statIngame(GameEngine engine, int playerID) {
		if (!engine.timerActive) engine.timerActive = true;
		if (endBonus != 0) endBonus = 0;
		if (cargoReturnCooldown > 0) cargoReturnCooldown--;

		// region CHECK CODE
		int effectiveLevel = engine.statistics.level;
		if (effectiveLevel >= LEVEL_FIELD_QUOTA.length) effectiveLevel = LEVEL_FIELD_QUOTA.length - 1;
		int fieldsFilled = LEVEL_FIELD_QUOTA[effectiveLevel];

		double proportion = (double)fieldsLeft / fieldsFilled;
		engine.meterValue = (int)(proportion * receiver.getMeterMax(engine));
		engine.meterColor = GameEngine.METER_COLOR_GREEN;
		if(proportion >= 0.25) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
		if(proportion >= 0.5) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
		if(proportion >= 0.75) engine.meterColor = GameEngine.METER_COLOR_RED;


		if (isFieldFull(engine)) {
			fieldsLeft--;
			if (fieldsLeft <= 0) {
				localState = CUSTOMSTATE_RESULTS;
				engine.resetStatc();
				return false;
			} else {
				engine.playSE("go");
				generateNewField(engine);
			}
		} else if (isFieldSingleHolesOnly(engine) && monominoesLeft <= 0 && selectedPiece == null) {
			if (engine.statistics.level >= LEVEL_FIELD_QUOTA.length - 1) {
				engine.stat = GameEngine.STAT_EXCELLENT;
				engine.ending = 1;
			} else {
				engine.stat = GameEngine.STAT_GAMEOVER;
				engine.playSE("died");
			}

			localState = CUSTOMSTATE_IDLE;
			engine.resetStatc();
			engine.gameEnded();

			selectedPiece = null;

			return false;
		}
		// endregion CHECK CODE

		// region SPAWNCODE
		spawnTime++;
		if (spawnTime >= SPAWN_TICK) {
			spawnTime = 0;

			double coeff = miscRandomiser.nextDouble();

			if (coeff < SPAWN_CHANCE) {
				if (!isPieceExistAtX(664)) {
					int v = -1;
					int rolls = 0;

					do {
						v = pieceRandomiser.nextInt();
						rolls++;
					} while (history.contains(v) && rolls < 4);

					appendHistory(v);

					conveyorBelt.add(PieceFactory.getPiece(v, -1, -1));
					conveyorBelt.get(conveyorBelt.size() - 1).setLocation(664, 324 - (int)(conveyorBelt.get(conveyorBelt.size() - 1).getConveyorYOffset() * 16));

					double powerCoeff = powerupRandomiser.nextDouble();
					double chance = powerupChance * Math.pow(powerExponent, engine.statistics.level);
					if (chance > 0.025) chance = 0.025;
					if (powerCoeff < chance) {
						int pwr = powerupRandomiser.nextInt(POWERUP_COUNT) + 1;
						conveyorBelt.get(conveyorBelt.size() - 1).setPowerup(pwr);
					}

					spawnTries = 0;
				} else {
					if (spawnTries < 3) {
						spawnTries++;
					} else {
						if (engine.statistics.level >= LEVEL_FIELD_QUOTA.length - 1) {
							engine.stat = GameEngine.STAT_EXCELLENT;
							engine.ending = 1;
						} else {
							engine.stat = GameEngine.STAT_GAMEOVER;
							engine.playSE("died");
						}

						localState = CUSTOMSTATE_IDLE;
						engine.resetStatc();
						engine.gameEnded();

						selectedPiece = null;

						return false;
					}
				}
			}
		}
		// endregion SPAWNCODE

		// region MOVECODE
		if (!conveyorBelt.isEmpty()) {
			for (int i = 0; i < conveyorBelt.size(); i++) {
				int tX = conveyorBelt.get(i).getConveyorBoundingBox()[0][0] - CONVEYOR_SPEED;  // Min X of piece.
				int x = conveyorBelt.get(i).getX();
				int y = conveyorBelt.get(i).getY();

				if (!isPieceExistAtX(tX, i) && tX >= 144 + BOUNDING_BOX_PADDING) {
					conveyorBelt.get(i).setLocation(x - CONVEYOR_SPEED, y);
				}
			}
		}

		// endregion MOVECODE

		// !!IMPORTANT: Move monominoes to x=12.

		// region MONOMINO SPAWNCODE
		if (monominoConveyorBelt == null && monominoesLeft > 0) {
			monominoConveyorBelt = PieceFactory.getPiece(0, -24, 324);
		}
		// endregion MONOMINO SPAWNCODE

		// region MONOMINO MOVECODE
		if (monominoConveyorBelt != null) {
			int x = monominoConveyorBelt.getX();
			int y = monominoConveyorBelt.getY();

			if (x + CONVEYOR_SPEED < 12) {
				monominoConveyorBelt.setLocation(x + CONVEYOR_SPEED, y);
			}
		}
		// endregion MONOMINO MOVECODE

		// region CLICK CODE
		if (selectedPiece == null) {
			if (mouseControl.getMouseClick(MouseParser.BUTTON_LEFT)) {
				if (monominoConveyorBelt != null && monominoesLeft > 0) {
					int[][][] cbbox = monominoConveyorBelt.getCursorBoundingBox();

					if (
							mouseCoords[0] >= cbbox[0][0][0] && mouseCoords[1] >= cbbox[0][0][1] && mouseCoords[0] < cbbox[0][1][0] && mouseCoords[1] < cbbox[0][1][1]
					) {
						selectedPiece = monominoConveyorBelt;
						engine.playSE("move");
						monominoConveyorBelt = null;
						monominoesLeft--;
					}
				}

				if (selectedPiece == null) {
					for (int i = 0; i < conveyorBelt.size(); i++) {
						int[][][] cbboxes = conveyorBelt.get(i).getCursorBoundingBox();
						for (int[][] bbox : cbboxes) {
							int minX, minY, maxX, maxY;
							minX = bbox[0][0];
							minY = bbox[0][1];
							maxX = bbox[1][0];
							maxY = bbox[1][1];

							if (mouseCoords[0] >= minX && mouseCoords[1] >= minY && mouseCoords[0] < maxX && mouseCoords[1] < maxY) {
								selectedPiece = conveyorBelt.get(i);
								engine.playSE("move");
								conveyorBelt.remove(i);
								break;
							}
						}

						if (selectedPiece != null) {
							break;
						}
					}
				}
			} else if (mouseControl.getMouseClick(MouseParser.BUTTON_RIGHT)) {
				if (monominoConveyorBelt != null) {
					int[] offset = monominoConveyorBelt.getCursorOffset();
					int fX = ((mouseCoords[0] - 4 - receiver.getFieldDisplayPositionX(engine, playerID)) / 16) - offset[0];
					int fY = ((mouseCoords[1] - 52 - receiver.getFieldDisplayPositionY(engine, playerID)) / 16) - offset[1];

					if (checkPieceFit(engine, monominoConveyorBelt, fX, fY)) {
						int lines = getFieldLines(engine, playerID);
						insertPiece(engine, monominoConveyorBelt, fX, fY);
						engine.playSE("lock");
						lastScore = engine.statistics.score;
						scGetTime = 0;
						engine.statistics.score += monominoConveyorBelt.getScore();
						addEffectToArray(new ScorePopup(monominoConveyorBelt.getScore(), mouseCoords, EventReceiver.COLOR_WHITE, (2f/3f)));
						scoreTowardsNewMonomino += monominoConveyorBelt.getScore();
						if (getFieldLines(engine, playerID) - lines > 0) engine.playSE("erase" + (getFieldLines(engine, playerID) - lines));
						monominoConveyorBelt = null;
						monominoesLeft--;
					} else {
						engine.playSE("holdfail");
					}
				}
			} else if (engine.ctrl.isPush(Controller.BUTTON_B)) {
				if (conveyorBelt.size() > 0) {
					selectedPiece = conveyorBelt.get(0);
					engine.playSE("move");
					conveyorBelt.remove(0);
				}
			} else if (engine.ctrl.isPush(Controller.BUTTON_A)) {
				if (conveyorBelt.size() > 0) {
					selectedPiece = conveyorBelt.get(conveyorBelt.size() - 1);
					engine.playSE("move");
					conveyorBelt.remove(conveyorBelt.size() - 1);
				}
			}
		} else {
			if (mouseControl.getMouseClick(MouseParser.BUTTON_RIGHT)) {
				selectedPiece.rotate();
				engine.playSE("rotate");
			}
			if (mouseControl.getMouseClick(MouseParser.BUTTON_LEFT)) {
				if (mouseCoords[0] >= 49 && mouseCoords[1] >= 304 && mouseCoords[0] <= 134 && mouseCoords[1] <= 350 && cargoReturnCooldown == 0) {
					engine.playSE("garbage");

					if (monominoConveyorBelt != null) {
						monominoConveyorBelt = null;
						monominoesLeft--;
						scoreTowardsNewMonomino = 0;
					}

					selectedPiece = null;
					cargoReturnCooldown = 120;
				} else {
					int[] offset = selectedPiece.getCursorOffset();
					int fX = ((mouseCoords[0] - 4 - receiver.getFieldDisplayPositionX(engine, playerID)) / 16) - offset[0];
					int fY = ((mouseCoords[1] - 52 - receiver.getFieldDisplayPositionY(engine, playerID)) / 16) - offset[1];

					if (checkPieceFit(engine, selectedPiece, fX, fY)) {
						int lines = getFieldLines(engine, playerID);

						insertPiece(engine, selectedPiece, fX, fY);
						engine.playSE("lock");

						int lineDiff = getFieldLines(engine, playerID) - lines;
						if (lineDiff == 0) lineDiff = 1;

						int score = lineDiff * selectedPiece.getScore();

						// region POWER ACTIVE
						int powerup = selectedPiece.getPowerup();
						switch (powerup) {
							case POWERUP_DESTROY_ONE:
								for (int i = 0; i < 1; i++) {
									if (conveyorBelt.size() > 0) conveyorBelt.remove(0);
								}
								engine.playSE("garbage");
								break;
							case POWERUP_DESTROY_TWO:
								for (int i = 0; i < 2; i++) {
									if (conveyorBelt.size() > 0) conveyorBelt.remove(0);
								}
								engine.playSE("garbage");
								break;
							case POWERUP_DESTROY_THREE:
								for (int i = 0; i < 3; i++) {
									if (conveyorBelt.size() > 0) conveyorBelt.remove(0);
								}
								engine.playSE("garbage");
								break;
							case POWERUP_ADD_ONE_MONO:
								monominoesLeft += 1;
								engine.playSE("square_g");
								break;
							case POWERUP_ADD_TWO_MONO:
								monominoesLeft += 2;
								engine.playSE("square_g");
								break;
							case POWERUP_ADD_THREE_MONO:
								monominoesLeft += 3;
								engine.playSE("square_g");
								break;
							case POWERUP_MULTIPLIER_TWO:
								score *= 2;
								engine.playSE("tspin0");
								break;
							case POWERUP_MULTIPLIER_THREE:
								score *= 3;
								engine.playSE("tspin0");
								break;
							case POWERUP_MULTIPLIER_FOUR:
								score *= 4;
								engine.playSE("tspin0");
								break;
							case POWERUP_FILLROW:
								int y = fY + (selectedPiece.getContents().length / 2);
								if (y < engine.field.getHeight()) {
									for (int x = 0; x < engine.field.getWidth(); x++) {
										Block blk = engine.field.getBlock(x, y);
										if (blk.color == Block.BLOCK_COLOR_NONE) {
											blk.color = Block.BLOCK_COLOR_GRAY;
										}
									}
								}
								score /= lineDiff;
								engine.playSE("linefall");
								break;
							default:
								break;
						}
						// endregion POWER ACTIVE

						if (getFieldLines(engine, playerID) - lines > 0) engine.playSE("erase" + (getFieldLines(engine, playerID) - lines));

						lastScore = engine.statistics.score;
						scGetTime = 0;
						engine.statistics.score += score;
						addEffectToArray(new ScorePopup(score, mouseCoords, (lineDiff == 1) ? EventReceiver.COLOR_WHITE : EventReceiver.COLOR_BLUE, (lineDiff == 1) ? (2f/3f) : 1));
						scoreTowardsNewMonomino += score;

						selectedPiece = null;
					} else {
						engine.playSE("holdfail");
					}
				}
			} else if (engine.ctrl.isPush(Controller.BUTTON_D) && cargoReturnCooldown == 0) {
				engine.playSE("garbage");

				if (monominoConveyorBelt != null) {
					monominoConveyorBelt = null;
					monominoesLeft--;
					scoreTowardsNewMonomino = 0;
				}

				selectedPiece = null;
				cargoReturnCooldown = 120;
			}
		}

		if (scoreTowardsNewMonomino >= NEW_MONOMINO_SCORE) {
			scoreTowardsNewMonomino = 0;
			engine.playSE("square_g");
			monominoesLeft++;
		}
		// endregion CLICK CODE

		return false;
	}

	private boolean isPieceExistAtX(int x) {
		if (!conveyorBelt.isEmpty()) {
			int[][][] boundingBoxes = new int[conveyorBelt.size()][][];

			int i = 0;
			for (GamePiece piece : conveyorBelt) {
				boundingBoxes[i] = piece.getConveyorBoundingBox();
				i++;
			}

			for (int[][] bbox : boundingBoxes) {
				int minX = bbox[0][0] - BOUNDING_BOX_PADDING;
				int maxX = bbox[1][0] + BOUNDING_BOX_PADDING;

				if (minX <= x && x <= maxX) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean isPieceExistAtX(int x, int indexExclusion) {
		if (conveyorBelt.size() > 1) {
			ArrayList<int[][]> boundingBoxes = new ArrayList<>();

			int i = 0;
			for (GamePiece piece : conveyorBelt) {
				if (i != indexExclusion) boundingBoxes.add(piece.getConveyorBoundingBox());
				i++;
			}

			for (int[][] bbox : boundingBoxes) {
				int minX = bbox[0][0] - BOUNDING_BOX_PADDING;
				int maxX = bbox[1][0] + BOUNDING_BOX_PADDING;

				if (minX <= x && x <= maxX) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean checkPieceFit(GameEngine engine, GamePiece piece, int x, int y) {
		int width = engine.field.getWidth();
		int height = engine.field.getHeight();

		for (int y2 = 0; y2 < piece.getContents().length; y2++) {
			for (int x2 = 0; x2 < piece.getContents()[0].length; x2++) {
				int tX = x + x2;
				int tY = y + y2;

				if (piece.getContents()[y2][x2] != 0) {
					if (tX < 0 || tX >= width) {
						return false;
					}
					if (tY < 0 || tY >= height) {
						return false;
					}

					Block blk = engine.field.getBlock(tX, tY);
					if (blk.color != Block.BLOCK_COLOR_NONE) {
						return false;
					}
				}
			}
		}

		return true;
	}

	private void insertPiece(GameEngine engine, GamePiece piece, int x, int y) {
		for (int y2 = 0; y2 < piece.getContents().length; y2++) {
			for (int x2 = 0; x2 < piece.getContents()[0].length; x2++) {
				int tX = x + x2;
				int tY = y + y2;

				if (piece.getContents()[y2][x2] != 0) {
					Block blk = new Block(piece.getColour(), engine.getSkin());
					blk.setAttribute(Block.BLOCK_ATTRIBUTE_OUTLINE, true);
					blk.setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);

					engine.field.getBlock(tX, tY).copy(blk);
				}
			}
		}
	}

	private int getFieldLines(GameEngine engine, int playerID) {
		int lines = 0;

		for (int y = 0; y < engine.field.getHeight(); y++) {
			boolean line = true;

			for (int x = 0; x < engine.field.getWidth(); x++) {
				Block blk = engine.field.getBlock(x, y);
				if (blk.color == Block.BLOCK_COLOR_NONE) {
					line = false;
					break;
				}
			}

			if (line) {
				if (!engine.field.getLineFlag(y)) {
					engine.field.setLineFlag(y, true);

					for (int x = 0; x < engine.field.getWidth(); x++) {
						Block blk = engine.field.getBlock(x, y);
						if (blk.color != Block.BLOCK_COLOR_NONE) {
							receiver.blockBreak(engine, playerID, x, y, blk);
							blk.elapsedFrames = 0;
						}
					}
				}

				lines++;
			}
		}

		return lines;
	}

	private boolean isFieldFull(GameEngine engine) {
		for (int y = 0; y < engine.field.getHeight(); y++) {
			for (int x = 0; x < engine.field.getWidth(); x++) {
				Block blk = engine.field.getBlock(x, y);
				if (blk.color == Block.BLOCK_COLOR_NONE) {
					return false;
				}
			}
		}

		return true;
	}

	private boolean isFieldSingleHolesOnly(GameEngine engine) {
		int[][] testLocations = {
				{ 0, -1 }, { 1, 0 }, { 0, 1 }, { -1, 0 }
		};

		for (int y = 0; y < engine.field.getHeight(); y++) {
			for (int x = 0; x < engine.field.getWidth(); x++) {
				Block blk = engine.field.getBlock(x, y);
				if (blk.color == Block.BLOCK_COLOR_NONE) {
					for (int[] loc : testLocations) {
						int tX = x + loc[0];
						int tY = y + loc[1];

						if(tX >= 0 && tX < engine.field.getWidth() && tY >= 0 && tY < engine.field.getHeight()) {
							Block blk2 = engine.field.getBlock(tX, tY);
							if (blk2.color == Block.BLOCK_COLOR_NONE) {
								return false;
							}
						}
					}
				}
			}
		}

		return true;
	}

	private boolean statResults(GameEngine engine, int playerID) {
		if (engine.timerActive) engine.timerActive = false;

		if (engine.statc[0] == 0) {
			engine.playSE("stageclear");

			int effectiveLevel = engine.statistics.level;
			if (effectiveLevel >= LEVEL_FIELD_QUOTA.length) effectiveLevel = LEVEL_FIELD_QUOTA.length - 1;
			int fieldsFilled = LEVEL_FIELD_QUOTA[effectiveLevel];

			effectiveLevel = engine.statistics.level;
			if (effectiveLevel >= LEVEL_END_BASE_BONUS.length) effectiveLevel = LEVEL_END_BASE_BONUS.length - 1;
			int baseBonus = LEVEL_END_BASE_BONUS[effectiveLevel];

			endBonus = fieldsFilled * monominoesLeft * baseBonus;
			if (endBonus <= 0) endBonus = baseBonus;

			engine.statistics.score += endBonus;
		} else if (engine.statc[0] == 300) {
			localState = CUSTOMSTATE_INGAME;
			engine.playSE("go");
			levelUp(engine);

			engine.resetStatc();
			return false;
		} else if (engine.statc[0] == 240) {
			engine.playSE("ready");
		}

		return true;
	}

	@Override
	public void onLast(GameEngine engine, int playerID) {
		for (int i = 0; i < scorePopups.length; i++) {
			if (scorePopups[i] != null) {
				if (scorePopups[i].shouldNull()){
					scorePopups[i] = null;
					continue;
				}

				scorePopups[i].update();
			}
		}

		if( (engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (!owner.replayMode)) || engine.stat == GameEngine.STAT_CUSTOM ) {
			// Show rank
			if(engine.ctrl.isPush(Controller.BUTTON_F) && playerProperties.isLoggedIn() && engine.stat != GameEngine.STAT_CUSTOM) {
				showPlayerStats = !showPlayerStats;
				engine.playSE("change");
			}
		}

		if (scGetTime < 60) scGetTime++;
	}

	@Override
	public void renderLast(GameEngine engine, int playerID) {
		if(owner.menuOnly) return;

		if( (engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (!owner.replayMode)) ) {
			receiver.drawScoreFont(engine, playerID, 0, 0, getName(), EventReceiver.COLOR_PINK);
			if (!owner.replayMode) {
				float scale = (receiver.getNextDisplayType() == 2) ? 0.5f : 1.0f;
				int topY = (receiver.getNextDisplayType() == 2) ? 5 : 3;
				receiver.drawScoreFont(engine, playerID, 3, topY-1, "SCORE   LEVEL", EventReceiver.COLOR_PINK, scale);

				if (showPlayerStats) {
					for(int i = 0; i < RANKING_MAX; i++) {
						receiver.drawScoreFont(engine, playerID,  0, topY+i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW, scale);
						receiver.drawScoreFont(engine, playerID, 3, topY+i, String.valueOf(rankingScorePlayer[i]), (i == rankingRankPlayer), scale);
						receiver.drawScoreFont(engine, playerID, 11, topY+i, String.valueOf(rankingLevelPlayer[i] + 1), (i == rankingRankPlayer), scale);
					}

					receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 1, "PLAYER SCORES", EventReceiver.COLOR_BLUE);
					receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 2, playerProperties.getNameDisplay(), EventReceiver.COLOR_WHITE, 2f);

					receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 5, "F:SWITCH RANK SCREEN", EventReceiver.COLOR_GREEN);
				} else {
					for(int i = 0; i < RANKING_MAX; i++) {
						receiver.drawScoreFont(engine, playerID,  0, topY+i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW, scale);
						receiver.drawScoreFont(engine, playerID, 3, topY+i, String.valueOf(rankingScore[i]), (i == rankingRank), scale);
						receiver.drawScoreFont(engine, playerID, 11, topY+i, String.valueOf(rankingLevel[i] + 1), (i == rankingRank), scale);
					}

					receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 1, "LOCAL SCORES", EventReceiver.COLOR_BLUE);
					if (!playerProperties.isLoggedIn()) receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 2, "(NOT LOGGED IN)\n(E:LOG IN)");
					if (playerProperties.isLoggedIn()) receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 5, "F:SWITCH RANK SCREEN", EventReceiver.COLOR_GREEN);
				}
			}
		} else if (!engine.gameActive && engine.stat == GameEngine.STAT_CUSTOM) {
			playerProperties.loginScreen.renderScreen(receiver, engine, playerID);
		} else {
			receiver.drawMenuFont(engine, playerID, 0, 19, getName(), EventReceiver.COLOR_PINK);

			if (playerProperties.isLoggedIn()) {
				receiver.drawMenuFont(engine, playerID, 18, 18, "PLAYER", EventReceiver.COLOR_PINK);
				receiver.drawMenuFont(engine, playerID, 25, 18, playerProperties.getNameDisplay(), 2f);
			}

			receiver.drawMenuFont(engine, playerID, 0, 21, "SCORE", EventReceiver.COLOR_PINK);
			receiver.drawMenuFont(engine, playerID, 0, 22, String.valueOf(Interpolation.lerp(lastScore, engine.statistics.score, (double)scGetTime/60.0)));

			receiver.drawMenuFont(engine, playerID, 10, 21, "TIME", EventReceiver.COLOR_PINK);
			receiver.drawMenuFont(engine, playerID, 10, 22, GeneralUtil.getTime(engine.statistics.time));

			receiver.drawMenuFont(engine, playerID, 20, 21, "LEVEL", EventReceiver.COLOR_PINK);
			receiver.drawMenuFont(engine, playerID, 20, 22, String.valueOf(engine.statistics.level + 1));

			receiver.drawMenuFont(engine, playerID, 27, 21, "QUOTA", EventReceiver.COLOR_PINK);
			receiver.drawMenuFont(engine, playerID, 27, 22, String.valueOf(fieldsLeft));

			int length = String.valueOf(monominoesLeft).length();
			int offset = 0;
			if (length == 1) offset = 8;
			receiver.drawDirectFont(engine, playerID, 3 + offset, 286, String.valueOf(monominoesLeft));

			receiver.drawDirectFont(engine, playerID, 3, 362, scoreTowardsNewMonomino + " /", EventReceiver.COLOR_WHITE, 0.5f);
			receiver.drawDirectFont(engine, playerID, 3, 370, "3000", EventReceiver.COLOR_WHITE, 0.5f);

			drawConveyorBelt(engine);
			if (conveyorBelt != null && !conveyorBelt.isEmpty()) {
				for (GamePiece piece : conveyorBelt) {
					drawPiece(engine, playerID, piece);
				}
			}
			if (monominoConveyorBelt != null) {
				drawPiece(engine, playerID, monominoConveyorBelt);
			}
			if (selectedPiece != null) {
				drawMousePiece(engine, playerID);
			}

			for (Effect scorePopup : scorePopups) {
				if (scorePopup != null) {
					scorePopup.draw(engine, receiver, playerID, new int[]{0}, null);
				}
			}

			if (localState == CUSTOMSTATE_RESULTS) {
				receiver.drawMenuFont(engine, playerID, 0, 0, "STAGE CLEAR!", engine.statc[0] % 2 == 0, EventReceiver.COLOR_YELLOW, EventReceiver.COLOR_ORANGE);
				receiver.drawMenuFont(engine, playerID, 0, 2, "BONUS", engine.statc[0] % 2 == 0, EventReceiver.COLOR_YELLOW, EventReceiver.COLOR_ORANGE);
				receiver.drawMenuFont(engine, playerID, 6, 2, String.valueOf(endBonus));
			}
		}
	}

	@Override
	public void renderResult(GameEngine engine, int playerID) {
		receiver.drawMenuFont(engine, playerID, 0, 0, "SCORE", EventReceiver.COLOR_BLUE);
		receiver.drawMenuFont(engine, playerID, 0, 1, String.format("%12s", String.valueOf(engine.statistics.score)));

		receiver.drawMenuFont(engine, playerID, 0, 2, "LEVEL", EventReceiver.COLOR_BLUE);
		receiver.drawMenuFont(engine, playerID, 0, 3, String.format("%12s", String.valueOf(engine.statistics.level + 1)));

		receiver.drawMenuFont(engine, playerID, 0, 4, "TIME", EventReceiver.COLOR_BLUE);
		receiver.drawMenuFont(engine, playerID, 0, 5, String.format("%12s", GeneralUtil.getTime(engine.statistics.time)));
	}

	private void drawConveyorBelt(GameEngine engine) {
		int offset;
		int cOffset;

		if (cargoReturnCooldown > 0) {
			cOffset = 104;
		} else {
			cOffset = 0;
		}

		if (conveyorFrame < 3) {
			offset = 8 * conveyorFrame;
		} else {
			offset = 24;
		}

		customHolder.drawImage(engine, "cargoreturn", 40,288, cOffset,0, 104,72, 255, 255, 255, 255, 1f);

		for (int i = 0; i <= (32 / 8) + 1; i++) {
			customHolder.drawImage(engine,"conveyorreverse", 32 - (8 * i), 304, offset,0, 8,56,255, 255, 255, 255, 1f);
		}

		for (int i = 0; i <= ((640 - 144) / 8) + 1; i++) {
			customHolder.drawImage(engine,"conveyor", 144 + (8 * i), 304, offset,0, 8,56,255, 255, 255, 255, 1f);
		}
	}

	private void drawPiece(GameEngine engine, int playerID, GamePiece piece) {
		int[][] contents = piece.getContents();
		int x = piece.getX();
		int y = piece.getY();
		int colour = piece.getColour();
		int pwr = piece.getPowerup();

		for (int y2 = 0; y2 < contents.length; y2++) {
			for (int x2 = 0; x2 < contents[y2].length; x2++) {
				if (contents[y2][x2] != 0) {
					receiver.drawSingleBlock(engine, playerID, x + (x2 * 16), y + (y2 * 16), colour, engine.getSkin(), false, 0f, 1f, 1f);
				}
			}
		}

		// region POWER ACTIVE
		String str = "";
		int col = -1;
		switch (pwr) {
			case POWERUP_DESTROY_ONE:
				str = "D1";
				col = EventReceiver.COLOR_WHITE;
				break;
			case POWERUP_DESTROY_TWO:
				str = "D2";
				col = EventReceiver.COLOR_WHITE;
				break;
			case POWERUP_DESTROY_THREE:
				str = "D3";
				col = EventReceiver.COLOR_WHITE;
				break;
			case POWERUP_ADD_ONE_MONO:
				str = "+1";
				col = EventReceiver.COLOR_GREEN;
				break;
			case POWERUP_ADD_TWO_MONO:
				str = "+2";
				col = EventReceiver.COLOR_GREEN;
				break;
			case POWERUP_ADD_THREE_MONO:
				str = "+3";
				col = EventReceiver.COLOR_GREEN;
				break;
			case POWERUP_MULTIPLIER_TWO:
				str = "X2";
				col = EventReceiver.COLOR_CYAN;
				break;
			case POWERUP_MULTIPLIER_THREE:
				str = "X3";
				col = EventReceiver.COLOR_CYAN;
				break;
			case POWERUP_MULTIPLIER_FOUR:
				str = "X4";
				col = EventReceiver.COLOR_CYAN;
				break;
			case POWERUP_FILLROW:
				str = "FL";
				col = EventReceiver.COLOR_ORANGE;
				break;
			default:
				break;
		}
		if (col != -1) receiver.drawDirectFont(engine, playerID, x + (piece.getCursorOffset()[0] * 16) - 8, y + (piece.getCursorOffset()[1] * 16), str, col);
		// endregion POWER ACTIVE
	}

	private void drawMousePiece(GameEngine engine, int playerID) {
		int[][] contents = selectedPiece.getContents();
		int[] offset = selectedPiece.getCursorOffset();
		int x = mouseCoords[0] - (16 * offset[0]);
		int y = mouseCoords[1] - (16 * offset[1]);
		int colour = selectedPiece.getColour();
		int pwr = selectedPiece.getPowerup();

		for (int y2 = 0; y2 < contents.length; y2++) {
			for (int x2 = 0; x2 < contents[y2].length; x2++) {
				if (contents[y2][x2] != 0) {
					receiver.drawSingleBlock(engine, playerID, x + (x2 * 16) - 8, y + (y2 * 16) - 8, colour, engine.getSkin(), false, 0f, 1f, 1f);
				}
			}
		}

		// region POWER ACTIVE
		String str = "";
		int col = -1;
		switch (pwr) {
			case POWERUP_DESTROY_ONE:
				str = "D1";
				col = EventReceiver.COLOR_WHITE;
				break;
			case POWERUP_DESTROY_TWO:
				str = "D2";
				col = EventReceiver.COLOR_WHITE;
				break;
			case POWERUP_DESTROY_THREE:
				str = "D3";
				col = EventReceiver.COLOR_WHITE;
				break;
			case POWERUP_ADD_ONE_MONO:
				str = "+1";
				col = EventReceiver.COLOR_GREEN;
				break;
			case POWERUP_ADD_TWO_MONO:
				str = "+2";
				col = EventReceiver.COLOR_GREEN;
				break;
			case POWERUP_ADD_THREE_MONO:
				str = "+3";
				col = EventReceiver.COLOR_GREEN;
				break;
			case POWERUP_MULTIPLIER_TWO:
				str = "X2";
				col = EventReceiver.COLOR_CYAN;
				break;
			case POWERUP_MULTIPLIER_THREE:
				str = "X3";
				col = EventReceiver.COLOR_CYAN;
				break;
			case POWERUP_MULTIPLIER_FOUR:
				str = "X4";
				col = EventReceiver.COLOR_CYAN;
				break;
			case POWERUP_FILLROW:
				str = "FL";
				col = EventReceiver.COLOR_ORANGE;
				break;
			default:
				break;
		}
		if (col != -1) receiver.drawDirectFont(engine, playerID, x + (offset[0] * 16) - 16, y + (offset[1] * 16) - 8, str, col);
		// endregion POWER ACTIVE
	}

	private void addEffectToArray(Effect effect) {
		for (int i = 0; i < scorePopups.length; i++) {
			if (scorePopups[i] == null) {
				scorePopups[i] = effect;
				return;
			}
		}
	}

	/**
	 * Load settings from property file
	 * @param prop Property file
	 */
	private void loadSetting(CustomProperties prop) {
		bg = prop.getProperty("expressshipping.bg", -1);
		bgm = prop.getProperty("expressshipping.bgm", 0);
	}

	/**
	 * Save settings to property file
	 * @param prop Property file
	 */
	private void saveSetting(CustomProperties prop) {
		prop.setProperty("expressshipping.bg", bg);
		prop.setProperty("expressshipping.bgm", bgm);
	}

	/**
	 * Read rankings from property file
	 * @param prop Property file
	 */
	protected void loadRanking(CustomProperties prop) {
		for(int i = 0; i < RANKING_MAX; i++) {
			rankingScore[i] = prop.getProperty("expressshipping.ranking" + ".score." + i, 0);
			rankingLevel[i] = prop.getProperty("expressshipping.ranking" + ".level." + i, 0);
		}
	}

	/**
	 * Save rankings to property file
	 * @param prop Property file
	 */
	private void saveRanking(CustomProperties prop) {
		for(int i = 0; i < RANKING_MAX; i++) {
			prop.setProperty("expressshipping.ranking" + ".score." + i, rankingScore[i]);
			prop.setProperty("expressshipping.ranking" + ".level." + i, rankingLevel[i]);
		}
	}

	/**
	 * Load settings from property file
	 * @param prop Property file
	 */
	private void loadSettingPlayer(ProfileProperties prop) {
		if (!prop.isLoggedIn()) return;
		bg = prop.getProperty("expressshipping.bg", -1);
		bgm = prop.getProperty("expressshipping.bgm", 0);
	}

	/**
	 * Save settings to property file
	 * @param prop Property file
	 */
	private void saveSettingPlayer(ProfileProperties prop) {
		if (!prop.isLoggedIn()) return;
		prop.setProperty("expressshipping.bg", bg);
		prop.setProperty("expressshipping.bgm", bgm);
	}

	/**
	 * Read rankings from property file
	 * @param prop Property file
	 */
	protected void loadRankingPlayer(ProfileProperties prop) {
		if (!prop.isLoggedIn()) return;
		for(int i = 0; i < RANKING_MAX; i++) {
			rankingScorePlayer[i] = prop.getProperty("expressshipping.ranking" + ".score." + i, 0);
			rankingLevelPlayer[i] = prop.getProperty("expressshipping.ranking" + ".level." + i, 0);
		}
	}

	/**
	 * Save rankings to property file
	 * @param prop Property file
	 */
	private void saveRankingPlayer(ProfileProperties prop) {
		if (!prop.isLoggedIn()) return;
		for(int i = 0; i < RANKING_MAX; i++) {
			prop.setProperty("expressshipping.ranking" + ".score." + i, rankingScorePlayer[i]);
			prop.setProperty("expressshipping.ranking" + ".level." + i, rankingLevelPlayer[i]);
		}
	}

	/**
	 * Update rankings
	 * @param sc Score
	 * @param lv Level
	 */
	private void updateRanking(int sc, int lv) {
		rankingRank = checkRanking(sc, lv);

		if(rankingRank != -1) {
			// Shift down ranking entries
			for(int i = RANKING_MAX - 1; i > rankingRank; i--) {
				rankingScore[i] = rankingScore[i - 1];
				rankingLevel[i] = rankingLevel[i - 1];
			}

			// Add new data
			rankingScore[rankingRank] = sc;
			rankingLevel[rankingRank] = lv;
		}

		if (playerProperties.isLoggedIn()) {
			rankingRankPlayer = checkRankingPlayer(sc, lv);

			if(rankingRankPlayer != -1) {
				// Shift down ranking entries
				for(int i = RANKING_MAX - 1; i > rankingRankPlayer; i--) {
					rankingScorePlayer[i] = rankingScorePlayer[i - 1];
					rankingLevelPlayer[i] = rankingLevelPlayer[i - 1];
				}

				// Add new data
				rankingScorePlayer[rankingRankPlayer] = sc;
				rankingLevelPlayer[rankingRankPlayer] = lv;
			}
		}
	}

	/**
	 * Calculate ranking position
	 * @param sc Score
	 * @param lv Level
	 * @return Position (-1 if unranked)
	 */
	private int checkRanking(int sc, int lv) {
		for(int i = 0; i < RANKING_MAX; i++) {
			if(sc > rankingScore[i]) {
				return i;
			} else if (rankingScore[i] == sc && lv < rankingLevel[i]) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * Calculate ranking position
	 * @param sc Score
	 * @param lv Level
	 * @return Position (-1 if unranked)
	 */
	private int checkRankingPlayer(int sc, int lv) {
		for(int i = 0; i < RANKING_MAX; i++) {
			if(sc > rankingScorePlayer[i]) {
				return i;
			} else if (rankingScorePlayer[i] == sc && lv < rankingLevelPlayer[i]) {
				return i;
			}
		}

		return -1;
	}
}
