package zeroxfc.nullpo.custom.modes;

import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.util.GeneralUtil;
import zeroxfc.nullpo.custom.libs.*;
import zeroxfc.nullpo.custom.modes.objects.expressshipping.*;

import java.util.Random;

public class ExpressShipping extends PuzzleGameEngine {
	// This is literally Puzzle Express.

	// TODO: add field border colour randomiser.

	/** Local states for onCustom */
	private static final int CUSTOMSTATE_IDLE = 0,
	                         CUSTOMSTATE_INGAME = 1,
	                         CUSTOMSTATE_RESULTS = 2;

	private static final int CONVEYOR_SPEED = 1;
	private static final int CONVEYOR_TICK = 2;
	private static final int CONVEYOR_ANIMATION_FRAMES = 6;
	private static final int BOUNDING_BOX_PADDING = 4;

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

	private GameManager owner;
	private EventReceiver receiver;
	private MouseParser mouseControl;
	private Random boardRandomiser;
	private ResourceHolderCustomAssetExtension customHolder;
	private WeightedRandomiser pieceRandomiser;
	private GamePiece[] conveyorBelt;
	private GamePiece selectedPiece;
	private int localState;
	private int engineTick;
	private int conveyorFrame;
	private int bgm;
	private int cargoReturnCooldown;

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
		conveyorBelt = new GamePiece[48];
		selectedPiece = null;
		engineTick = 0;
		conveyorFrame = 0;
		bgm = 0;
		cargoReturnCooldown = 0;

		customHolder = new ResourceHolderCustomAssetExtension(engine);
		customHolder.loadImage("res/graphics/conveyor.png", "conveyor");
		customHolder.loadImage("res/graphics/conveyorreverse.png", "conveyorreverse");
		customHolder.loadImage("res/graphics/cargoreturn.png", "cargoreturn");
	}

	@Override
	public boolean onSetting(GameEngine engine, int playerID) {
		// Menu
		if(engine.owner.replayMode == false) {
			// Configuration changes
			int change = updateCursor(engine, 0, playerID);

			if(change != 0) {
				engine.playSE("change");

				switch(engine.statc[2]) {
					case 0:
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

			if(engine.statc[3] >= 60) {
				return false;
			}
		}

		return true;
	}

	@Override
	public void renderSetting(GameEngine engine, int playerID) {
		drawMenu(engine, playerID, receiver, 0, EventReceiver.COLOR_BLUE, 0,
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

			engine.field = Boards.getBoard(boardRandomiser.nextInt(Boards.Boards.length), engine.getSkin());

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
	public void renderLast(GameEngine engine, int playerID) {
		if(owner.menuOnly) return;

		if( (engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (owner.replayMode == false)) ) {
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
