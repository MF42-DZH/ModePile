package zeroxfc.nullpo.custom.modes;

import mu.nu.nullpo.game.subsystem.mode.DummyMode;
import mu.nu.nullpo.gui.sdl.MouseInputSDL;
import mu.nu.nullpo.gui.sdl.ResourceHolderSDL;
import mu.nu.nullpo.gui.slick.MouseInput;
import mu.nu.nullpo.gui.slick.NullpoMinoSlick;
import mu.nu.nullpo.gui.slick.ResourceHolder;
import mu.nu.nullpo.gui.swing.ResourceHolderSwing;
import mu.nu.nullpo.util.CustomProperties;
import mu.nu.nullpo.util.GeneralUtil;

import java.util.Random;

import mu.nu.nullpo.game.component.*;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameManager;
import zeroxfc.nullpo.custom.libs.*;

public class Collapse extends DummyMode {
	// Hey, have any of you played any of the Super Collapse games?
	// Field Dimensions: 12 x 16; 1 Hidden Height
	//
	// TODO: Create a weighted randomiser for the blocks.
	// TODO: Do the centre-direction column gravity thing... somehow.
	// TODO: Implement mouse control if you can.
	// TODO: Implement flying score popups.
	
	private static final int[] tableColors = {
		Block.BLOCK_COLOR_RED,
		Block.BLOCK_COLOR_BLUE,
		Block.BLOCK_COLOR_YELLOW,
		Block.BLOCK_COLOR_GREEN,
		Block.BLOCK_COLOR_ORANGE,
		Block.BLOCK_COLOR_GRAY  // SET AS BONE BLOCKS
	};  // Use bone blocks for indestructible, movable blocks.
	
	private static final int[] tableBombColors = {
		Block.BLOCK_COLOR_GEM_RED,
		Block.BLOCK_COLOR_GEM_BLUE,
		Block.BLOCK_COLOR_GEM_YELLOW,
		Block.BLOCK_COLOR_GEM_GREEN,
		Block.BLOCK_COLOR_GEM_ORANGE,
		Block.BLOCK_COLOR_GEM_RAINBOW
	};  // Use rainbow gems for super bombs.
	
	private static final int[][] tableColorWeights = {
		{ 1, 1, 1, 0, 0, 0 },
		{ 160, 160, 160, 0, 0, 1 },
		{ 160, 160, 160, 80, 0, 2 },
		{ 160, 160, 160, 120, 0, 3 },
		{ 160, 160, 160, 160, 0, 4 },
		{ 192, 192, 192, 192, 96, 11 },
		{ 204, 204, 204, 204, 204, 18 }
	};
	
	private static final int[][] tableBombColorWeights = {
		{ 0, 0, 0, 0, 0, 1 },
		{ 5, 5, 5, 0, 0, 15 },
		{ 4, 4, 4, 4, 0, 16 },
		{ 4, 4, 4, 4, 0, 16 },
		{ 4, 4, 4, 4, 0, 16 },
		{ 8, 8, 8, 8, 8, 40 },
		{ 8, 8, 8, 8, 8, 40 }
	};
	
	private static final int[] tableLevelWeightShift = {
		0, 3, 6, 9, 12, 15, 18, 10000
	};
	
	private static final int[] tableLevelStartLines = {
		3, 4, 4, 5, 5, 5,
		6, 6, 6, 7, 7, 7,
		8, 8, 8, 9, 9, 9,
		10, 10, 10
	}; // MAX: 2.5 row/s
	
	private static final int[] tableSpawnSpeedNormal = {
		15, 15, 15, 14, 14, 13,
		13, 12, 12, 11, 11, 10,
		10, 9, 8, 7, 6, 5,
		4, 3, 2
	}; // MAX: 2.5 row/s
	
	private static final int[] tableSpawnSpeedEasy = {
		20, 19, 18, 17, 16, 15,
		14, 13, 12, 12, 11, 11,
		10, 10, 9, 9, 8, 8,
		7, 6, 5
	}; // MAX: 1 row/s
	
	private static final int[] tableSpawnSpeedHard = {
		12, 12, 12, 12, 11, 11,
		11, 10, 10, 9, 8, 7,
		6, 5, 4, 4, 3, 3,
		2, 2, 1
	}; // MAX: 5 row/s
	
	private static final int[] tableLevelLine = {
		20, 30, 40, 50, 60, 70,
		80, 90, 100, 120, 140, 160,
		180, 200, 240, 280, 320, 360,
		400, -1
	};
	
	private static final double bombChance = (0.01);
	
	private static final int MAX_RANKING = 10;
	
	private static final int MAX_DIFFICULTIES = 3;
	
	private static final String[] DIFFICULTY_NAMES = {
		"EASY",
		"NORMAL",
		"HARD"
	};
	
	private static final int maxSpeedLine = 8;
	
	private static final int HOLDER_SLICK = 0,
							 HOLDER_SWING = 1,
							 HOLDER_SDL = 2;
	
	private static final int LOCALSTATE_INGAME = 0,
							 LOCALSTATE_TRANSITION = 1;
	
	private GameManager owner;
	private EventReceiver receiver;
	private boolean enableBombs;
	private int[][] rankingScore;
	private int[][] rankingLevel;
	private int bScore;
	private int rankingRank;
	private int difficulty;
	private int linesLeft;
	private int bgm;
	private int cursorX, cursorY;
	private int fieldX, fieldY;
	private int holderType;
	private int spawnTimer, spawnTimerLimit;
	private WeightedRandomiser wRandomEngine;
	private WeightedRandomiser wRandomEngineBomb;
	private SideWaveText[] sTextArr;
	private Block[] nextBlocks;
	private int localState;
	private Random localRandom;
	private boolean force;
	private int lineSpawn;
	private int lastScore, scoreToDisplay, scGetTime;
	private double multiplier;
	private int acTime;
	
	/*
	 * ------ MAIN METHODS ------
	 */
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "COLLAPSE";
	}
	
	@Override
	public void playerInit(GameEngine engine, int playerID) {
		SoundLoader.loadSoundset(SoundLoader.LOADTYPE_COLLAPSE);
		
		owner = engine.owner;
		receiver = engine.owner.receiver;
		
		enableBombs = false;
		rankingScore = new int[MAX_DIFFICULTIES][MAX_RANKING];
		rankingLevel = new int[MAX_DIFFICULTIES][MAX_RANKING];
		rankingRank = -1;
		difficulty = 0;
		linesLeft = 0;
		bgm = 0;
		cursorX = 0;
		cursorY = 0;
		fieldX = -1;
		fieldY = -1;
		wRandomEngine = null;
		wRandomEngineBomb = null;
		localRandom = null;
		localState = -1;
		force = false;
		bScore = 0;
		lineSpawn = 0;
		lastScore = 0;
		scoreToDisplay = 0;
		scGetTime = 0;
		multiplier = 1;
		acTime = -1;
		
		resetSTextArr();
		
		spawnTimer = 0;
		spawnTimerLimit = 0;
		
		nextBlocks = new Block[12];
		resetBlockArray(engine);
		
		holderType = -1;
		if (ResourceHolder.imgNormalBlockList != null) holderType = HOLDER_SLICK;
		else if (ResourceHolderSwing.imgNormalBlockList != null) holderType = HOLDER_SWING;
		else if (ResourceHolderSDL.imgNormalBlockList != null) holderType = HOLDER_SDL;
		
		engine.framecolor = GameEngine.FRAME_COLOR_YELLOW;
		
		loadSetting(owner.modeConfig);
		loadRanking(owner.modeConfig);
	}
	
	private void resetSTextArr() {
		sTextArr = new SideWaveText[30];
	}
	
	private void updateSTextArr() {
		for (int i = 0; i < sTextArr.length; i++) {
			if (sTextArr[i] != null) {
				if (sTextArr[i].getLifeTime() >= SideWaveText.MaxLifeTime) {
					sTextArr[i] = null;
					continue;
				}
				
				sTextArr[i].update();
			}
		}
	}
	
	private void addSText(int score, boolean big, boolean largeclear) {
		int offsetX = -1 * ((String.valueOf(score).length() * ( big ? 32 : 16 )) / 2);
		int offsetY = -8;
		
		if (big) {
			offsetY *= 2;
		}
		
		SideWaveText s = new SideWaveText(cursorX + offsetX, cursorY + offsetY, 1.5, !largeclear ? 0 : (big ? 24 : 16), String.valueOf(score), big, largeclear);
		
		for (int i = 0; i < sTextArr.length; i++) {
			if (sTextArr[i] == null) {
				sTextArr[i] = s;
				return;
			}
		}
	}
	
	@Override
	public boolean onSetting(GameEngine engine, int playerID) {
		// Menu
		if(engine.owner.replayMode == false) {
			// Configuration changes
			int change = updateCursor(engine, 2, playerID);

			if(change != 0) {
				engine.playSE("change");

				switch(engine.statc[2]) {
				case 0:
					difficulty += change;
					if (difficulty < 0) difficulty = MAX_DIFFICULTIES - 1;
					if (difficulty >= MAX_DIFFICULTIES) difficulty = 0;
					break;
				case 1:
					enableBombs = !enableBombs;
					break;
				case 2:
					bgm += change;
					if (bgm < 0) bgm = 15;
					if (bgm > 15) bgm = 0;
					break;
				}
			}

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
		drawMenu(engine, playerID, receiver, 0, EventReceiver.COLOR_RED, 0,
				"DIFFICULTY", DIFFICULTY_NAMES[difficulty]);
		drawMenu(engine, playerID, receiver, 2, EventReceiver.COLOR_BLUE, 1,
				"BOMBS", GeneralUtil.getONorOFF(enableBombs),
				"BGM", String.valueOf(bgm));
	}
	
	@Override
	public boolean onReady(GameEngine engine, int playerID) {
		// 横溜め
		if(engine.ruleopt.dasInReady && engine.gameActive) engine.padRepeat();
		else if(engine.ruleopt.dasRedirectInDelay) { engine.dasRedirect(); }

		// Initialization
		if(engine.statc[0] == 0) {
			engine.ruleopt.fieldWidth = 12;
			engine.ruleopt.fieldHeight = 16;
			engine.ruleopt.fieldHiddenHeight = 1;
			engine.statistics.level = 0;
			
			resetSTextArr();
			
			cursorX = 0;
			cursorY = 0;
			fieldX = -1;
			fieldY = -1;
			localState = -1;
			force = false;
			lastScore = 0;
			scoreToDisplay = 0;
			
			localRandom = new Random(engine.randSeed - 1);
			wRandomEngine = new WeightedRandomiser(tableColorWeights[0], engine.randSeed);
			wRandomEngineBomb = new WeightedRandomiser(tableBombColorWeights[0], engine.randSeed + 1);
				
			levelUp(engine, playerID, true);
			
			engine.fieldWidth = engine.ruleopt.fieldWidth;
			engine.fieldHeight = engine.ruleopt.fieldHeight;
			engine.fieldHiddenHeight = engine.ruleopt.fieldHiddenHeight;
			engine.field = new Field(engine.fieldWidth, engine.fieldHeight, engine.fieldHiddenHeight, engine.ruleopt.fieldCeiling);
			engine.field.setAllAttribute(Block.BLOCK_ATTRIBUTE_OUTLINE, true);
			engine.field.setAllAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
			
			if(!engine.readyDone) {
				//  button input状態リセット
				engine.ctrl.reset();
				// ゲーム中 flagON
				engine.gameActive = true;
				engine.gameStarted = true;
				engine.isInGame = true;
			}
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
			
			for (int i = 0; i < lineSpawn; i++) {
				while (getNextEmpty() != -1) {
					int index = getNextEmpty();
					int temp = -1;

					temp = wRandomEngine.nextInt();
					if (linesLeft == 1) {
						temp = Block.BLOCK_COLOR_GRAY;
					}
					nextBlocks[index] = new Block(tableColors[temp], engine.getSkin());
					nextBlocks[index].setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
					nextBlocks[index].setAttribute(Block.BLOCK_ATTRIBUTE_OUTLINE, true);
				}
				
				incrementField(engine);
				resetBlockArray(engine);
			}
			
			engine.playSE("rise");
			
			localState = LOCALSTATE_INGAME;
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
	public boolean onCustom(GameEngine engine, int playerID) {
//		if (engine.ctrl.isPush(Controller.BUTTON_D)) {
//			engine.resetStatc();
//			engine.gameEnded();
//			
//			engine.stat = GameEngine.STAT_EXCELLENT;
//			engine.ending = 1;
//			engine.rainbowAnimate = false;
//			return false;
//		}  // DEBUG CODE.
		
		parseMouse(engine, playerID);
		if (!engine.rainbowAnimate) engine.rainbowAnimate = true;
		
		boolean incrementTime = false;
		switch (localState) {
		case LOCALSTATE_INGAME:
			if (engine.timerActive == false) engine.timerActive = true;
			incrementTime = stateInGame(engine, playerID);
			break;
		case LOCALSTATE_TRANSITION:
			if (engine.timerActive == true) engine.timerActive = false;
			incrementTime = stateTransition(engine, playerID);
			break;
		default:
			break;
		}
		
		if (engine.ending != 0) {
			engine.timerActive = false;
		}
		
		if (incrementTime) engine.statc[0]++;
		
		return true;
	}
	
	// TODO: Make the rest of the gamemode work.

	private void clearSquares(GameEngine engine) {
		int score = 0;
		int squares = 0;
		boolean fromBomb = false;
		
		if (engine.field.getBlock(fieldX, fieldY).color < 8 && engine.field.getBlock(fieldX, fieldY).color != Block.BLOCK_COLOR_GRAY) {
			squares = getSquares(engine, fieldX, fieldY);
			
			if (squares >= 3) {
			    score = getClearScore(engine, squares);
			    
			    for (int y = 0; y < engine.field.getHeight(); y++) {
					for (int x = 0; x < engine.field.getWidth(); x++) {
						if (engine.field.getBlock(x, y).color == Block.BLOCK_COLOR_PURPLE) {
							engine.field.getBlock(x, y).setAttribute(Block.BLOCK_ATTRIBUTE_ERASE, true);
						}
					}
			    }
			    
			    int u = 0;
			    if (squares >= 12) u = 1;
			    engine.playSE(u == 1 ? "bigclear" : "normalclear");
			} else {
				engine.playSE("noclear");
				for (int y = 0; y < engine.field.getHeight(); y++) {
					for (int x = 0; x < engine.field.getWidth(); x++) {
						if (engine.field.getBlock(x, y).color == Block.BLOCK_COLOR_PURPLE) {
							engine.field.getBlock(x, y).color = engine.field.getBlock(x, y).secondaryColor;
						}
					}
				}
			}
		} else if (engine.field.getBlock(fieldX, fieldY).color != Block.BLOCK_COLOR_GRAY) {
			fromBomb = true;
			if (engine.field.getBlock(fieldX, fieldY).color != 35) {
				
				int c = engine.field.getBlock(fieldX, fieldY).color - 7;
				
				for (int y = 0; y < engine.field.getHeight(); y++) {
					for (int x = 0; x < engine.field.getWidth(); x++) {
						if (engine.field.getBlock(x, y).color == c || engine.field.getBlock(x, y).color == (c + 7)) {
							engine.field.getBlock(x, y).secondaryColor = engine.field.getBlock(x, y).color;
							engine.field.getBlock(x, y).setAttribute(Block.BLOCK_ATTRIBUTE_ERASE, true);
							
							squares++;
						}
					}
				}
				
				score = (int)(getClearScore(engine, squares) * 0.75);
			} else {
				squares = 0;
			
				for (int y = 0; y < engine.field.getHeight(); y++) {
					for (int x = 0; x < engine.field.getWidth(); x++) {
						if (isCoordWithinRadius(fieldX, fieldY, x, y, 4.5) && !engine.field.getBlockEmpty(x, y)) {
							engine.field.getBlock(x, y).secondaryColor = engine.field.getBlock(x, y).color;
							engine.field.getBlock(x, y).setAttribute(Block.BLOCK_ATTRIBUTE_ERASE, true);
							
							squares++;
						}
					}
				}
				
				score = (int)(getClearScore(engine, squares) * 0.5);
			}
			
			explode(engine);
		} else {
			engine.playSE("noclear");
		}
		
		if (score > 0) {
			score *= multiplier;
			
			if (squares >= 6) addSText(score, fromBomb, (squares >= 12));
			setNewLowerScore(engine);
			
			if (engine.field.isEmpty()) {
				acTime = 0;
				engine.playSE("bonus");
				engine.statistics.score += 100000 * Math.pow(1.025, engine.statistics.level);
			}
			engine.statistics.score += score;
		}
	}
	
	private boolean stateInGame(GameEngine engine, int playerID) {		
		if (holderType != HOLDER_SWING) {
			if (fieldX != -1) {
				if (!engine.field.getBlockEmpty(fieldX, fieldY)) {
					clearSquares(engine);
				}
			}
		} else {
			if (engine.ctrl.isPush(Controller.BUTTON_A)) {
				if (!engine.field.getBlockEmpty(fieldX, fieldY)) {
					clearSquares(engine);
				}
			}
		}
		
		int brk = 0;
		if (bScore > 0) bScore = 0;
		
		for (int y = 0; y < engine.field.getHeight(); y++) {
			for (int x = 0; x < engine.field.getWidth(); x++) {
				if (engine.field.getBlock(x, y).getAttribute(Block.BLOCK_ATTRIBUTE_ERASE)) {
					Block blk = new Block(engine.field.getBlock(x, y));
					blk.color = blk.secondaryColor;
					
					Block nblk = new Block(Block.BLOCK_COLOR_NONE);
					nblk.setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
					nblk.setAttribute(Block.BLOCK_ATTRIBUTE_OUTLINE, true);
					nblk.setAttribute(Block.BLOCK_ATTRIBUTE_ERASE, false);
					
					engine.field.getBlock(x, y).copy(nblk);
					
					receiver.blockBreak(engine, playerID, x, y, blk);
					brk++;
				}
			}
		}
		
		if (brk > 0) {
			if (engine.field.freeFall()) engine.playSE("landing");
			else engine.playSE("nolanding");
			for (int i = 0; i < 6; i++) {
				bringColumnsCloser(engine);
			}
		}
		
		spawnTimer++;
		
		if (spawnTimer >= spawnTimerLimit) {
			spawnTimer = 0;
			int index = getNextEmpty();
			if (index != -1) {
				double coeff = localRandom.nextDouble();
				int temp = -1;
				
				if (coeff <= bombChance * ( engine.field.getHighestBlockY() < 4 ? 3 : 1 ) && enableBombs && linesLeft != 1) {
					temp = wRandomEngineBomb.nextInt();
					
					nextBlocks[index] = new Block(tableBombColors[temp], engine.getSkin());
					nextBlocks[index].setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
					nextBlocks[index].setAttribute(Block.BLOCK_ATTRIBUTE_OUTLINE, true);
				} else {
					temp = wRandomEngine.nextInt();
					
					boolean bone = false;
					if (temp == wRandomEngine.getMax() && wRandomEngine.getMax() == 5) bone = true;
					
					if (linesLeft == 1) {
						temp = 5;
					}
					
					nextBlocks[index] = new Block(tableColors[temp], engine.getSkin());
					nextBlocks[index].setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
					nextBlocks[index].setAttribute(Block.BLOCK_ATTRIBUTE_OUTLINE, true);
					nextBlocks[index].setAttribute(Block.BLOCK_ATTRIBUTE_BONE, bone);
				}
			} else {
				if (linesLeft > 0) linesLeft--;
				
				double proportion = (double)linesLeft / tableLevelLine[engine.statistics.level];
				if (linesLeft >= 0) {
					engine.meterValue = (int)(proportion * receiver.getMeterMax(engine));
					engine.meterColor = GameEngine.METER_COLOR_RED;
					if(proportion <= 0.75f) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
					if(proportion <= 0.5f) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
					if(proportion <= 0.25f) engine.meterColor = GameEngine.METER_COLOR_GREEN;
				} else {
					engine.meterValue = receiver.getMeterMax(engine);
					engine.meterColor = GameEngine.METER_COLOR_GREEN;
				}
				
				if (linesLeft != 0) {
					engine.playSE("rise");
					incrementField(engine);
				}
				
				resetBlockArray(engine);
				
				if (linesLeft == 3) {
					engine.playSE("gradeup");
				}
				
				multiplier = midgameSpeedSet(engine);
				
				if (spawnTimer == 0 && engine.field.getHighestBlockY() <= 2 && engine.field.getHighestBlockY() >= 0) {
					engine.playSE("danger");
				}
			}
		} else if (force && getNextEmpty() != -1) {
			spawnTimer = 0;
			force = false;
			while (getNextEmpty() != -1) {
				int index = getNextEmpty();
				double coeff = localRandom.nextDouble();
				int temp = -1;
				
				if (coeff <= bombChance * ( engine.field.getHighestBlockY() < 4 ? 3 : 1 ) && enableBombs && linesLeft != 1) {
					temp = wRandomEngineBomb.nextInt();
					
					if (linesLeft == 1) {
						temp = 5;
					}
					
					nextBlocks[index] = new Block(tableBombColors[temp], engine.getSkin());
					nextBlocks[index].setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
					nextBlocks[index].setAttribute(Block.BLOCK_ATTRIBUTE_OUTLINE, true);
				} else {
					temp = wRandomEngine.nextInt();
					
					boolean bone = false;
					if (temp == wRandomEngine.getMax() && wRandomEngine.getMax() == 5) bone = true;
					
					if (linesLeft == 1) {
						temp = 5;
					}
					
					nextBlocks[index] = new Block(tableColors[temp], engine.getSkin());
					nextBlocks[index].setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
					nextBlocks[index].setAttribute(Block.BLOCK_ATTRIBUTE_OUTLINE, true);
					nextBlocks[index].setAttribute(Block.BLOCK_ATTRIBUTE_BONE, bone);
				}
			}
		}
		
		if (linesLeft == 0) {
			engine.resetStatc();
			localState = LOCALSTATE_TRANSITION;
			return false;
		}
		
		if (engine.field.getHighestBlockY() < 0) {
			if (engine.statistics.level < 19) {
				engine.stat = GameEngine.STAT_GAMEOVER;
				engine.resetStatc();
				engine.gameEnded();
				engine.rainbowAnimate = false;
			} else {
				engine.stat = GameEngine.STAT_EXCELLENT;
				engine.resetStatc();
				
				engine.gameEnded();
				engine.ending = 1;
				engine.rainbowAnimate = false;
			}
		}
		
		return false;
	}
	
	private boolean stateTransition(GameEngine engine, int playerID) {
		if (engine.statc[0] > (180 + 16 * 3)) {
			levelUp(engine, playerID, false);
			resetBlockArray(engine);
			engine.resetStatc();
			
			for (int i = 0; i < lineSpawn; i++) {
				while (getNextEmpty() != -1) {
					int index = getNextEmpty();
					int temp = -1;

					temp = wRandomEngine.nextInt();
					
					boolean bone = false;
					if (temp == wRandomEngine.getMax() && wRandomEngine.getMax() == 5) bone = true;
					
					if (linesLeft == 1) {
						temp = Block.BLOCK_COLOR_GRAY;
					}
					
					nextBlocks[index] = new Block(tableColors[temp], engine.getSkin());
					nextBlocks[index].setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
					nextBlocks[index].setAttribute(Block.BLOCK_ATTRIBUTE_OUTLINE, true);
					nextBlocks[index].setAttribute(Block.BLOCK_ATTRIBUTE_BONE, bone);
				}
				
				incrementField(engine);
				resetBlockArray(engine);
			}
			
			engine.playSE("go");
			engine.playSE("rise");
			
			localState = LOCALSTATE_INGAME;
			return false;
		} else if (engine.statc[0] > 0) {
			int f = engine.statc[0] - 60;
			
			if (f % 3 == 0 && f >= 0 && (15 - (f / 3)) >= -1) {
				for (int x = 0; x < engine.field.getWidth(); x++) {
					int y = 15 - (f / 3);
					Block blk = engine.field.getBlock(x, y);
					
					Block nblk = new Block(Block.BLOCK_COLOR_NONE);
					nblk.setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
					nblk.setAttribute(Block.BLOCK_ATTRIBUTE_OUTLINE, true);
					nblk.setAttribute(Block.BLOCK_ATTRIBUTE_ERASE, false);
					
					if (blk != null) {
						if (blk.color > Block.BLOCK_COLOR_NONE) {
							receiver.blockBreak(engine, playerID, x, y, blk);
							engine.field.getBlock(x, y).copy(nblk);
						}
					}
				}
			}
			
			if (engine.statc[0] == (120 + 16 * 3)) engine.playSE("ready");
		} else {
			engine.playSE("stageclear");
			bScore = getLevelClearBonus(engine);
			setNewLowerScore(engine);
			engine.statistics.score += bScore;
		}
		
		return true;
	}
	
	private int getNextEmpty() {
		int index = -1;
		
		for (int i = 0; i < nextBlocks.length; i++) {
			 if (nextBlocks[i].color == Block.BLOCK_COLOR_NONE) {
				 index = i;
				 break;
			 }
		}
		
		return index;
	}
	
	private int getSquares(GameEngine engine, int x, int y) {
		IntWrapper wrapper = new IntWrapper();
		
		flagSquares(engine, wrapper, x, y, engine.field.getBlockColor(x, y));
		
		return wrapper.value;
	}
	
	private void flagSquares(GameEngine engine, IntWrapper counter, int x, int y, int color) {
		if (x >= 0 && x < 12 && y >= 0 && y < 16) {
			if (engine.field.getBlockColor(x, y) == color || engine.field.getBlockColor(x, y) == 35 || engine.field.getBlockColor(x, y) == (7 + color)) {
					counter.value++;
					engine.field.getBlock(x, y).secondaryColor = engine.field.getBlock(x, y).color;
					engine.field.getBlock(x, y).color = Block.BLOCK_COLOR_PURPLE;
					
					flagSquares(engine, counter, x + 1, y, color);
					flagSquares(engine, counter, x - 1, y, color);
					flagSquares(engine, counter, x, y + 1, color);
					flagSquares(engine, counter, x, y - 1, color);
				} else {
					return;
				}
		} else {
			return;
		}
	}
	
	private void resetBlockArray(GameEngine engine) {
		for (int i = 0; i < nextBlocks.length; i++) {
			nextBlocks[i] = new Block(Block.BLOCK_COLOR_NONE);
			nextBlocks[i].setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
			nextBlocks[i].setAttribute(Block.BLOCK_ATTRIBUTE_OUTLINE, true);
		}
	}
	
	private void incrementField(GameEngine engine) {
		for (int y = -1; y < engine.field.getHeight() - 1; y++) {
			for (int x = 0; x < engine.field.getWidth(); x++) {
				engine.field.getBlock(x, y).copy(engine.field.getBlock(x, y + 1));
			}
		}
		
		for (int x = 0; x < engine.field.getWidth(); x++) {
			engine.field.getBlock(x, engine.field.getHeight() - 1).copy(nextBlocks[x]);
		}
	}
	
	private void bringColumnsCloser(GameEngine engine) {
		Block nblk = new Block(Block.BLOCK_COLOR_NONE);
		nblk.setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
		nblk.setAttribute(Block.BLOCK_ATTRIBUTE_OUTLINE, true);
		
		for (int x = 5; x > 0; x--) {
			boolean empty = true;
			for (int y = 0; y < engine.field.getHeight(); y++) {
				if (!engine.field.getBlockEmpty(x, y)) {
					empty = false;
					break;
				}
			}
			
			if (empty) {
				for (int x2 = x; x2 > 0; x2--) {
					for (int y = 0; y < engine.field.getHeight(); y++) {
						engine.field.getBlock(x2, y).copy(engine.field.getBlock(x2 - 1, y));
					}
				}
				
				for (int y = 0; y < engine.field.getHeight(); y++) {
					engine.field.getBlock(0, y).copy(nblk);
				}
			}
		}
		
		for (int x = 6; x < 11; x++) {
			boolean empty = true;
			for (int y = 0; y < engine.field.getHeight(); y++) {
				if (!engine.field.getBlockEmpty(x, y)) {
					empty = false;
					break;
				}
			}
			
			if (empty) {
				for (int x2 = x; x2 < 11; x2++) {
					for (int y = 0; y < engine.field.getHeight(); y++) {
						engine.field.getBlock(x2, y).copy(engine.field.getBlock(x2 + 1, y));
					}
				}
				
				for (int y = 0; y < engine.field.getHeight(); y++) {
					engine.field.getBlock(11, y).copy(nblk);
				}
			}
		}
	}
	
	private void levelUp(GameEngine engine, int playerID, boolean beginning) {
		if (!beginning) engine.statistics.level++;
		owner.backgroundStatus.bg = engine.statistics.level % 20;

		int effectiveLevel = engine.statistics.level;
		
		spawnTimer = 0;
		resetBlockArray(engine);
		
		lineSpawn = tableLevelStartLines[effectiveLevel];
		linesLeft = tableLevelLine[effectiveLevel];
		
		if (holderType == HOLDER_SWING) {
			fieldX = 0;
			fieldY = 15;
		}
		
		force = false;
		
		int result = 0;
		int is = 0;
		while (true) {
			if (tableLevelWeightShift[is + 1] >= effectiveLevel) {
				result = is;
				break;
			} else {
				is++;
			}
		}
		
		
		multiplier = 0.75;
		
		wRandomEngine.setWeights(tableColorWeights[result]);
		wRandomEngineBomb.setWeights(tableBombColorWeights[result]);
		
		switch (difficulty) {
		case 0:
			spawnTimerLimit = (int)(tableSpawnSpeedEasy[effectiveLevel] * 1.5);
			break;
		case 1:
			spawnTimerLimit = (int)(tableSpawnSpeedNormal[effectiveLevel] * 1.5);
			break;
		case 2:
			spawnTimerLimit = (int)(tableSpawnSpeedHard[effectiveLevel] * 1.5);
			break;
		}
	}
	
	private double midgameSpeedSet(GameEngine engine) {
		if (linesLeft >= maxSpeedLine) {
			int effectiveLevel = engine.statistics.level;
			double fraction = 0.75 + (0.75 * ((double)(linesLeft - maxSpeedLine) / (tableLevelLine[effectiveLevel] - maxSpeedLine)));
			double rawSpeed = 1;
			
			switch (difficulty) {
			case 0:
				rawSpeed = fraction * tableSpawnSpeedEasy[effectiveLevel];
				break;
			case 1:
				rawSpeed = fraction * tableSpawnSpeedNormal[effectiveLevel];
				break;
			case 2:
				rawSpeed = fraction * tableSpawnSpeedHard[effectiveLevel];
				break;
			}
			
			double mod = rawSpeed % 1;
			if (mod > 0) spawnTimerLimit = (int)(rawSpeed + 1);
			else spawnTimerLimit = (int)rawSpeed;
			
			return 0.75 - (fraction - 1.5);
		} else if (linesLeft >= 0) {
			return 1.5;
		} else {
			int effectiveLevel = engine.statistics.level;
			double rawSpeed = 1;
			
			switch (difficulty) {
			case 0:
				rawSpeed = 0.75 * tableSpawnSpeedEasy[effectiveLevel];
				break;
			case 1:
				rawSpeed = 0.75 * tableSpawnSpeedNormal[effectiveLevel];
				break;
			case 2:
				rawSpeed = 0.75 * tableSpawnSpeedHard[effectiveLevel];
				break;
			}
			
			double mod = rawSpeed % 1;
			if (mod > 0) spawnTimerLimit = (int)(rawSpeed + 1);
			else spawnTimerLimit = (int)rawSpeed;
			
			return 1.5;
		}
	}
	
	private void parseMouse(GameEngine engine, int playerID) {
		switch (holderType) {
		case HOLDER_SLICK:
			MouseInput.mouseInput.update(NullpoMinoSlick.appGameContainer.getInput());
			
			cursorX = MouseInput.mouseInput.getMouseX();
			cursorY = MouseInput.mouseInput.getMouseY();
			
			if (MouseInput.mouseInput.isMouseClicked()) {
				fieldX = (cursorX - 4 - receiver.getFieldDisplayPositionX(engine, playerID)) / 16;
				fieldY = (cursorY - 52 - receiver.getFieldDisplayPositionY(engine, playerID)) / 16;
			} else {
				fieldX = -1;
				fieldY = -1;
			}
			
			break;
		case HOLDER_SWING:
			// XXX: SWING DOES NOT SUPPORT MOUSE INPUT. FALL BACK TO KEYBOARD INPUT.
			int changeX = 0, changeY = 0;
			if (engine.ctrl.isPush(Controller.BUTTON_LEFT)) {
				changeX += -1;
			}
			if (engine.ctrl.isPush(Controller.BUTTON_RIGHT)) {
				changeX += 1;
			}
			if (engine.ctrl.isPush(Controller.BUTTON_UP)) {
				changeY += -1;
			}
			if (engine.ctrl.isPush(Controller.BUTTON_DOWN)) {
				changeY += 1;
			}
			
			fieldX += changeX;
			fieldY += changeY;
			
			if (changeX != 0 || changeY != 0) engine.playSE("change");
			
			if (fieldX < 0) fieldX = 11;
			if (fieldX > 11) fieldX = 0;
			if (fieldY < 0) fieldY = 15;
			if (fieldY > 15) fieldY = 0;
			
			if (engine.ctrl.isPush(Controller.BUTTON_B) && localState == LOCALSTATE_INGAME) force = true;
			
			break;
		case HOLDER_SDL:
			try {
				MouseInputSDL.mouseInput.update();
			} catch (Exception e) {
				// DO NOTHING
			}
			
			cursorX = MouseInputSDL.mouseInput.getMouseX();
			cursorY = MouseInputSDL.mouseInput.getMouseY();
			
			if (MouseInputSDL.mouseInput.isMouseClicked()) {
				fieldX = (cursorX - 4 - receiver.getFieldDisplayPositionX(engine, playerID)) / 16;
				fieldY = (cursorY - 52 - receiver.getFieldDisplayPositionY(engine, playerID)) / 16;
			} else {
				fieldX = -1;
				fieldY = -1;
			}
			
			break;
		default:
			break;
		}
		
		if ((fieldX < 0 || fieldX > 11) ||
			(fieldY < 0 || fieldY > 15)) {
			if (holderType != HOLDER_SWING) {
				if (fieldY == 17 && localState == LOCALSTATE_INGAME) force = true;
				
				fieldX = -1;
				fieldY = -1;
			}
		} else {
			if (holderType != HOLDER_SWING) {
				force = false;
			}
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
						
						if(blk != null && blk.color > Block.BLOCK_COLOR_NONE) {
							if(!blk.getAttribute(Block.BLOCK_ATTRIBUTE_GARBAGE)) {
								blk.color = Block.BLOCK_COLOR_GRAY;
								blk.setAttribute(Block.BLOCK_ATTRIBUTE_GARBAGE, true);
							}
							blk.darkness = 0.3f;
							blk.elapsedFrames = -1;
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
				if (enableBombs) updateRanking(engine.statistics.score, difficulty, engine.statistics.level + 1);
				if (rankingRank != -1) saveRanking(owner.modeConfig);
				receiver.saveModeConfig(owner.modeConfig);
				
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
	
	private void incrementScore(GameEngine engine) {
		scoreToDisplay = Interpolation.lerp(lastScore, engine.statistics.score, (float)scGetTime / 60);
	}
	
	private void setNewLowerScore(GameEngine engine) {
		lastScore = engine.statistics.score;
		scGetTime = 0;
	}
	
	@Override
	public void onLast(GameEngine engine, int playerID) {
		if (scGetTime < 60 && !engine.lagStop) scGetTime++;
		if (!engine.lagStop) {
			updateSTextArr();
			if (acTime < 120 && acTime >= 0) acTime++;
			else acTime = -1;
		}
	}
	
	@Override
	public void renderLast(GameEngine engine, int playerID) {
		if(owner.menuOnly) return;
		
		receiver.drawScoreFont(engine, playerID, 0, 0, getName(), EventReceiver.COLOR_ORANGE);
		receiver.drawScoreFont(engine, playerID, 0, 1, "(" + DIFFICULTY_NAMES[difficulty] + " DIFFICULTY)", EventReceiver.COLOR_ORANGE);

		if( (engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (owner.replayMode == false)) ) {
			if((owner.replayMode == false) && (enableBombs == true) && (engine.ai == null)) {
				float scale = (receiver.getNextDisplayType() == 2) ? 0.5f : 1.0f;
				int topY = (receiver.getNextDisplayType() == 2) ? 6 : 4;
				receiver.drawScoreFont(engine, playerID, 3, topY-1, "SCORE    LEVEL", EventReceiver.COLOR_BLUE, scale);

				for(int i = 0; i < MAX_RANKING; i++) {
					receiver.drawScoreFont(engine, playerID,  0, topY+i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW, scale);
					receiver.drawScoreFont(engine, playerID,  3, topY+i, String.valueOf(rankingScore[difficulty][i]), (i == rankingRank), scale);
					receiver.drawScoreFont(engine, playerID,  12, topY+i, String.valueOf(rankingLevel[difficulty][i]), (i == rankingRank), scale);
				}
			}
		} else {
			if (!engine.lagStop) incrementScore(engine);
			receiver.drawScoreFont(engine, playerID, 0, 3, "SCORE", EventReceiver.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 4, String.valueOf(scoreToDisplay));
			
			receiver.drawScoreFont(engine, playerID, 0, 6, "LEVEL", EventReceiver.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 7, String.valueOf(engine.statistics.level + 1));
			
			receiver.drawScoreFont(engine, playerID, 0, 9, "LINES LEFT", EventReceiver.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 10, linesLeft >= 0 ? String.valueOf(linesLeft) : "INFINITY");
			
			receiver.drawScoreFont(engine, playerID, 0, 12, "TIME", EventReceiver.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 13, GeneralUtil.getTime(engine.statistics.time));
			
			for (int i = 0; i < sTextArr.length; i++) {
				if (sTextArr[i] != null) {
					int x = 0, y = 0;
					int[] h = sTextArr[i].getLocation();
					x = h[0];
					y = h[1];
					
					float scale = 0;
					float rs = 0;
					float baseScale = sTextArr[i].getBig() ? 2 : 1;
					double baseDim = sTextArr[i].getBig() ? 32.0 : 16.0;
					if (sTextArr[i].getLifeTime() < 24) {
						scale = baseScale;
						rs = 1;
					} else {
						scale = baseScale - (baseScale * ((float)(sTextArr[i].getLifeTime() - 24) / 96));
						rs = 1 - (((float)(sTextArr[i].getLifeTime() - 24) / 96));
					}
					int nOffX = 0, nOffY = 0;
					if (rs < 1) {
						nOffX = (int)( (( baseDim * sTextArr[i].getText().length() ) - ( (baseDim * sTextArr[i].getText().length()) * rs )) / 2 );
						nOffY = (int)( (baseDim - (baseDim * rs)) / 2 );
					}
					
					if ((sTextArr[i].getLifeTime() / 2) % 2 == 0 && sTextArr[i].getLargeClear()) {
						receiver.drawDirectFont(engine, playerID, x + nOffX, y + nOffY, sTextArr[i].getText(), EventReceiver.COLOR_YELLOW, scale);
					} else {
						receiver.drawDirectFont(engine, playerID, x + nOffX, y + nOffY, sTextArr[i].getText(), EventReceiver.COLOR_ORANGE, scale);
					}
				}
			}
			
			if (holderType == HOLDER_SWING) {
				receiver.drawMenuFont(engine, playerID, fieldX, fieldY, "f", EventReceiver.COLOR_YELLOW);
			}
			
			if (localState == LOCALSTATE_TRANSITION) {
				String s = String.valueOf(bScore);
				int l = s.length();
				int offset = (12 - l) / 2;
				receiver.drawMenuFont(engine, playerID, 2, 6, "LEVEL UP", (engine.statc[0] / 2) % 2 == 0, EventReceiver.COLOR_YELLOW, EventReceiver.COLOR_ORANGE);
				receiver.drawMenuFont(engine, playerID, 0, 8, "BONUS POINTS", EventReceiver.COLOR_YELLOW);
				receiver.drawMenuFont(engine, playerID, offset, 9, s);
			}
			
//			receiver.drawScoreFont(engine, playerID, 0, 15, "MOUSE COORDS", EventReceiver.COLOR_BLUE);
//			receiver.drawScoreFont(engine, playerID, 0, 16, "(" + cursorX + ", " + cursorY + ")");
//			
//			receiver.drawScoreFont(engine, playerID, 0, 18, "FIELD CELL CLICKED", EventReceiver.COLOR_BLUE);
//			receiver.drawScoreFont(engine, playerID, 0, 19, "(" + fieldX + ", " + fieldY + ")");
			
			if (localState == LOCALSTATE_INGAME) {
				int fx = receiver.getFieldDisplayPositionX(engine, playerID) + 4;
				int fy = receiver.getFieldDisplayPositionY(engine, playerID) + 52 + (17 * 16);
				for (int i = 0; i < nextBlocks.length; i++) {
					if (nextBlocks[i].color > Block.BLOCK_COLOR_NONE) receiver.drawSingleBlock(engine, playerID, fx + (i * 16), fy, nextBlocks[i].color, engine.getSkin(), nextBlocks[i].getAttribute(Block.BLOCK_ATTRIBUTE_BONE), 0f, 1f, 1f);
				}
				
				String s = String.valueOf((int)(100000 * Math.pow(1.025, engine.statistics.level)));
				int l = s.length();
				int offset = (12 - l) / 2;
				
				if (acTime >= 0 && acTime < 120) {
					receiver.drawMenuFont(engine, playerID, 1, 6, "ALL CLEAR!", (engine.statistics.time / 2) % 2 == 0, EventReceiver.COLOR_YELLOW, EventReceiver.COLOR_ORANGE);
					receiver.drawMenuFont(engine, playerID, 0, 8, "BONUS POINTS", EventReceiver.COLOR_YELLOW);
					receiver.drawMenuFont(engine, playerID, offset, 9, s);
				}
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
	
	/**
	 * Load settings from property file
	 * @param prop Property file
	 */
	private void loadSetting(CustomProperties prop) {
		enableBombs = prop.getProperty("collapse.enableBombs", true);
		difficulty = prop.getProperty("collapse.difficulty", 1);
		bgm = prop.getProperty("collapse.bgm", 0);
	}

	/**
	 * Save settings to property file
	 * @param prop Property file
	 */
	private void saveSetting(CustomProperties prop) {
		prop.setProperty("collapse.enableBombs", enableBombs);
		prop.setProperty("collapse.difficulty", difficulty);
		prop.setProperty("collapse.bgm", bgm);
	}
	
	/**
	 * Read rankings from property file
	 * @param prop Property file
	 * @param ruleName Rule name
	 */
	protected void loadRanking(CustomProperties prop) {
		for(int i = 0; i < MAX_RANKING; i++) {
			for(int j = 0; j < MAX_DIFFICULTIES; j++) {
				rankingScore[j][i] = prop.getProperty("collapse.ranking." + j + ".score." + i, 0);
				rankingLevel[j][i] = prop.getProperty("collapse.ranking." + j + ".level." + i, 0);
			}
		}
	}

	/**
	 * Save rankings to property file
	 * @param prop Property file
	 * @param ruleName Rule name
	 */
	private void saveRanking(CustomProperties prop) {
		for(int i = 0; i < MAX_RANKING; i++) {
			for(int j = 0; j < MAX_DIFFICULTIES; j++) {
				prop.setProperty("collapse.ranking." + j + ".score." + i, rankingScore[j][i]);
				prop.setProperty("collapse.ranking." + j + ".level." + i, rankingLevel[j][i]);
			}
		}
	}

	/**
	 * Update rankings
	 * @param sc Score
	 * @param li Lines
	 * @param time Time
	 */
	private void updateRanking(int sc, int type, int lv) {
		rankingRank = checkRanking(sc, type);

		if(rankingRank != -1) {
			// Shift down ranking entries
			for(int i = MAX_RANKING - 1; i > rankingRank; i--) {
				rankingScore[type][i] = rankingScore[type][i - 1];
				rankingLevel[type][i] = rankingLevel[type][i - 1];
			}

			// Add new data
			rankingScore[type][rankingRank] = sc;
			rankingLevel[type][rankingRank] = lv;
		}
	}

	/**
	 * Calculate ranking position
	 * @param sc Score
	 * @param li Lines
	 * @param time Time
	 * @return Position (-1 if unranked)
	 */
	private int checkRanking(int sc, int type) {
		for(int i = 0; i < MAX_RANKING; i++) {
			if(sc > rankingScore[type][i]) {
				return i;
			}
		}
		
		return -1;
	}
	
	/*
	 * ------ MODE-SPECIFIC PRIVATE METHODS ------
	 */
	
	private int getClearScore(GameEngine engine, int squares) {
		return (int)((18 * squares * ((double)squares / 4) * ((double)(engine.statistics.level + 1) / 3)) * Math.pow(1.015, squares));
	}
	
	private int getLevelClearBonus(GameEngine engine) {
		int h = engine.field.getHeight();
		int w = engine.field.getWidth();
		int s = 0;
		
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				Block blk = engine.field.getBlock(x, y);
				if ((blk != null) && (blk.color == Block.BLOCK_COLOR_NONE)) {
					s++;
				}
			}
		}
		
		return getClearScore(engine, s) / 48;
	}
	
	/**
	 * Checks if a coordinate is within a certain radius.
	 * @param x X-coordinate of circle's centre.
	 * @param y Y-coordinate of circle's centre.
	 * @param xTest X-coordinate of test square.
	 * @param yTest Y-coordinate of test square.
	 * @param radius The testing radius
	 * @return The result of the check. true: within. false: not within.
	 */
	private boolean isCoordWithinRadius(int x, int y, int xTest, int yTest, double radius) {
		int dX = xTest - x;
		int dY = yTest - y;
		
		double distance = Math.sqrt((dX * dX) + (dY * dY));
		return (distance <= radius);
	}
	
	private void explode(GameEngine engine) {
		engine.playSE("bombexplode");
	}
}