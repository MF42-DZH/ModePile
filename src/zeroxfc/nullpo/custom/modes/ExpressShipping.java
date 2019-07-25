package zeroxfc.nullpo.custom.modes;

import mu.nu.nullpo.game.component.BGMStatus;
import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.util.GeneralUtil;
import zeroxfc.nullpo.custom.libs.*;
import zeroxfc.nullpo.custom.modes.objects.expressshipping.*;

import java.util.ArrayList;
import java.util.Random;

public class ExpressShipping extends PuzzleGameEngine {
	// This is literally Puzzle Express.

	/** Local states for onCustom */
	private static final int CUSTOMSTATE_IDLE = 0,
	                         CUSTOMSTATE_INGAME = 1,
	                         CUSTOMSTATE_RESULTS = 2;

	private static final int CONVEYOR_SPEED = 1;
	private static final int CONVEYOR_TICK = 2;
	private static final int CONVEYOR_ANIMATION_FRAMES = 6;
	private static final int BOUNDING_BOX_PADDING = 2;
	private static final int SPAWN_TICK = 60;
	private static final double SPAWN_CHANCE = (2.0 / 3.0);

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

	private GameManager owner;
	private EventReceiver receiver;
	private MouseParser mouseControl;
	private Random boardRandomiser;
	private ResourceHolderCustomAssetExtension customHolder;
	private WeightedRandomiser pieceRandomiser;
	private ArrayList<GamePiece> conveyorBelt;
	private GamePiece selectedPiece;
	private GamePiece monominoConveyorBelt;
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
		selectedPiece = null;
		monominoConveyorBelt = null;
		engineTick = 0;
		conveyorFrame = 0;
		bg = 0;
		bgm = 0;
		spawnTime = 0;
		cargoReturnCooldown = 0;
		monominoesLeft = 0;

		customHolder = new ResourceHolderCustomAssetExtension(engine);
		customHolder.loadImage("res/graphics/conveyor.png", "conveyor");
		customHolder.loadImage("res/graphics/conveyorreverse.png", "conveyorreverse");
		customHolder.loadImage("res/graphics/cargoreturn.png", "cargoreturn");
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

			// Confirm
			if(engine.ctrl.isPush(Controller.BUTTON_A) && (engine.statc[3] >= 5)) {
				engine.playSE("decide");
				// saveSetting(owner.modeConfig);
				// receiver.saveModeConfig(owner.modeConfig);
				return false;
			}

			// Cancel
			if(engine.ctrl.isPush(Controller.BUTTON_B)) {
				engine.quitflag = true;
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
			boardRandomiser = new Random(engine.randSeed);
			pieceRandomiser = new WeightedRandomiser(PIECE_WEIGHTS[0], engine.randSeed);
			engineTick = 0;
			conveyorFrame = 0;
			cargoReturnCooldown = 0;
			spawnTime = 0;
			monominoesLeft = MONOMINO_START_AMOUNTS[0];
			fieldsLeft = LEVEL_FIELD_QUOTA[0];

			engine.field = Boards.getBoard(boardRandomiser.nextInt(Boards.Boards.length), engine.getSkin());
			engine.framecolor = boardRandomiser.nextInt(8);

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
			if(!engine.readyDone) engine.owner.bgmStatus.bgm = bgm;
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
		engine.fieldWidth = engine.field.getWidth();
		engine.fieldHeight = engine.field.getHeight();
		engine.fieldHiddenHeight = 0;
	}

	private void levelUp(GameEngine engine) {
		engineTick = 0;
		spawnTime = 0;
		cargoReturnCooldown = 0;

		selectedPiece = null;
		monominoConveyorBelt = null;

		conveyorBelt.clear();
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
		boolean updateTime = false;

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

		if (updateTime) engine.statc[0]++;

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
	 */

	private boolean statIngame(GameEngine engine, int playerID) {
		if (!engine.timerActive) engine.timerActive = true;
		if (endBonus != 0) endBonus = 0;

		int[][][] boundingBoxesSpawn = new int[1][][];
		if (conveyorBelt.size() > 0) boundingBoxesSpawn = new int[conveyorBelt.size()][][];

		spawnTime++;
		if (spawnTime >= SPAWN_TICK) {
			spawnTime = 0;

			if (conveyorBelt.size() > 0) {
				int i = 0;
				for (GamePiece piece : conveyorBelt) {
					boundingBoxesSpawn[i] = piece.getConveyorBoundingBox();
					i++;
				}

				// TODO: Move this to the movement code.
				boolean lose = false;
				for (int[][] bbox : boundingBoxesSpawn) {
					int minX = bbox[0][0] - BOUNDING_BOX_PADDING;
					int maxX = bbox[1][0] + BOUNDING_BOX_PADDING;
					if (minX < 700 && 700 < maxX) {
						lose = true;
						break;
					}
				}

				if (lose) {
					localState = CUSTOMSTATE_IDLE;
					engine.stat = GameEngine.STAT_GAMEOVER;

					engine.gameEnded();
					engine.resetStatc();
					return false;
				} else {
					conveyorBelt.add(PieceFactory.getPiece(pieceRandomiser.nextInt(), -1, -1));
					conveyorBelt.get(conveyorBelt.size() - 1).setLocation(700, 324 - (int)(conveyorBelt.get(conveyorBelt.size() - 1).getConveyorYOffset() * 16));
				}

			} else {
				conveyorBelt.add(PieceFactory.getPiece(pieceRandomiser.nextInt(), -1, -1));
				conveyorBelt.get(conveyorBelt.size() - 1).setLocation(700, 324 - (int)(conveyorBelt.get(conveyorBelt.size() - 1).getConveyorYOffset() * 16));
			}
		}

		engineTick++;
		if (engineTick >= CONVEYOR_TICK) {
			engineTick = 0;
			if (conveyorBelt.size() > 0) {
				for (int i = 0; i < conveyorBelt.size(); i++) {
					int[][][] boundingBoxesMove = new int[conveyorBelt.size()][][];

					int i2 = 0;
					for (GamePiece piece : conveyorBelt) {
						if (i2 != i) boundingBoxesMove[i] = piece.getConveyorBoundingBox();
						i2++;
					}

					int pieceX = conveyorBelt.get(i).getLocation()[0];
					int pieceY = conveyorBelt.get(i).getLocation()[1];

					boolean canMove = false;
					for (int[][] bbox : boundingBoxesMove) {
						int minX = bbox[0][0] - BOUNDING_BOX_PADDING;
						int maxX = bbox[1][0] + BOUNDING_BOX_PADDING;
						if (minX < pieceX - CONVEYOR_SPEED ) {
							canMove = true;
							break;
						}
					}
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
					if (tY < 0 || tX >= height) {
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
	public void renderLast(GameEngine engine, int playerID) {
		if(owner.menuOnly) return;

		if( (engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (!owner.replayMode)) ) {
			receiver.drawScoreFont(engine, playerID, 0, 0, getName(), EventReceiver.COLOR_PINK);
			if (!owner.replayMode) {
				// DO NOTHING.
			}
		} else {
			receiver.drawMenuFont(engine, playerID, 0, 19, getName(), EventReceiver.COLOR_PINK);

			receiver.drawMenuFont(engine, playerID, 0, 21, "SCORE", EventReceiver.COLOR_PINK);
			receiver.drawMenuFont(engine, playerID, 0, 22, String.valueOf(engine.statistics.score));

			receiver.drawMenuFont(engine, playerID, 10, 21, "TIME", EventReceiver.COLOR_PINK);
			receiver.drawMenuFont(engine, playerID, 10, 22, GeneralUtil.getTime(engine.statistics.time));

			receiver.drawMenuFont(engine, playerID, 20, 21, "LEVEL", EventReceiver.COLOR_PINK);
			receiver.drawMenuFont(engine, playerID, 20, 22, String.valueOf(engine.statistics.level + 1));

			receiver.drawMenuFont(engine, playerID, 27, 21, "QUOTA", EventReceiver.COLOR_PINK);
			receiver.drawMenuFont(engine, playerID, 28, 22, String.valueOf(fieldsLeft));

			drawConveyorBelt(engine);
		}
	}

	private void drawConveyorBelt(GameEngine engine) {
		if (!engine.lagStop) {
			// TODO: Move this to onCustom.
			conveyorFrame = (conveyorFrame + 1) % CONVEYOR_ANIMATION_FRAMES;
		}

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

	private void moveAllPiecesInBelt() {
		engineTick++;

		if (engineTick >= CONVEYOR_TICK) {
			engineTick = 0;
			for (GamePiece piece : conveyorBelt) {
				if (piece != null) {
					// Sort this out when UI design is done.
					// piece.setLocation(piece.getX() + CONVEYOR_SPEED, piece.getY());
				}
			}
		}
	}
}
