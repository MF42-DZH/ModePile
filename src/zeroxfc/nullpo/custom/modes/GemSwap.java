package zeroxfc.nullpo.custom.modes;

import java.util.ArrayList;
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
import org.apache.log4j.Logger;
import zeroxfc.nullpo.custom.libs.MouseParser;
import zeroxfc.nullpo.custom.libs.ResourceHolderCustomAssetExtension;
import zeroxfc.nullpo.custom.modes.objects.gemswap.Effect;
import zeroxfc.nullpo.custom.modes.objects.gemswap.GemField;
import zeroxfc.nullpo.custom.modes.objects.gemswap.ScoreEvent;

/**
 * REMEMBER: Make the main routines work similarly to GameEngine, but centred around onCustom.
 * TODO: Override the ready/gameover states to remove next pieces/suppress replays respectively.
 */

public class GemSwap extends DummyMode {
    private static final Logger log = Logger.getLogger(GemSwap.class);

    private static final int FIELD_DIMENSION = 8;

    private static final int CUSTOMSTATE_IDLE = 0;
    private static final int CUSTOMSTATE_SWAP = 1;
    private static final int CUSTOMSTATE_CHECK = 2;
    private static final int CUSTOMSTATE_ACTION = 3;
    private static final int CUSTOMSTATE_ANIMATION = 4;
    private static final int CUSTOMSTATE_CLEAR = 5;
    private static final int CUSTOMSTATE_DROP = 6;

    private static final String[] stateNames = {
        "IDLE",
        "SWAP",
        "CHECK",
        "ACTION",
        "ANIMATION",
        "CLEAR",
        "DROP"
    };

    private GameManager owner;
    private EventReceiver receiver;
    private MouseParser mouseControl;
    private boolean selected;
    private int[] fieldSelection1;
    private int[] fieldSelection2;
    private GemField mainField;
    private GemField oldField;
    private int localState;
    // private Effect[] scorePopups;
    private Effect[] lightningChains;
    private ArrayList<ScoreEvent> events;
    private ResourceHolderCustomAssetExtension customHolder;
    private int bgm;
    private int combo;

    @Override
    public String getName() {
        return "GEM SWAP";
    }

    @Override
    public void playerInit(GameEngine engine, int playerID) {
        owner = engine.owner;
        receiver = engine.owner.receiver;

        reset(engine, false);
        events = new ArrayList<>(8);

        mouseControl = new MouseParser();
        engine.blockOutlineType = GameEngine.BLOCK_OUTLINE_NONE;
        engine.framecolor = GameEngine.FRAME_COLOR_GRAY;
        localState = CUSTOMSTATE_IDLE;

        customHolder = new ResourceHolderCustomAssetExtension();
        customHolder.loadImage("res/graphics/particle.png", "particle");

        loadSetting(owner.modeConfig);
    }

    /**
     * Resets the mode's variables.
     *
     * @param engine      Current GameEngine instance.
     * @param createField Should create gem field?
     */
    private void reset(GameEngine engine, boolean createField) {
        fieldSelection1 = new int[] { -1, -1 };
        fieldSelection2 = new int[] { -1, -1 };
        selected = false;

        localState = CUSTOMSTATE_IDLE;

        lightningChains = new Effect[96];
        // scorePopups = new Effect[128];

        if (createField) {
            mainField = new GemField(FIELD_DIMENSION, FIELD_DIMENSION, engine.randSeed);
            oldField = new GemField(mainField);

            do {
                mainField.generateField();
                mainField.matchCheck();
            } while (mainField.isMatchExist() || mainField.possibleMatches() <= 0);

            mainField.resetMatchArrays();
            mainField.resetFallStatus();
            mainField.resetSwapStatus();
        }
    }

    private void clearSelection() {
        fieldSelection1 = new int[] { -1, -1 };
        fieldSelection2 = new int[] { -1, -1 };
        selected = false;
    }

    private void clearEffects() {
        lightningChains = new Effect[96];
        // scorePopups = new Effect[128];
    }

    @Override
    public boolean onSetting(GameEngine engine, int playerID) {
        // Menu
        if (!engine.owner.replayMode) {
            // Configuration changes
            int change = updateCursor(engine, 0, playerID);

            if (change != 0) {
                engine.playSE("change");

                // TODO: more settings
                switch (engine.statc[2]) {
                    case 0:
                        bgm += change;
                        if (bgm < 0) bgm = 15;
                        if (bgm > 15) bgm = 0;
                        break;
                }
            }

            // Confirm
            if (engine.ctrl.isPush(Controller.BUTTON_A) && (engine.statc[3] >= 5)) {
                engine.playSE("decide");
                saveSetting(owner.modeConfig);
                receiver.saveModeConfig(owner.modeConfig);
                return false;
            }

            // Cancel
            if (engine.ctrl.isPush(Controller.BUTTON_B)) {
                engine.quitflag = true;
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

    @Override
    public void renderSetting(GameEngine engine, int playerID) {
        drawMenu(engine, playerID, receiver, 0, EventReceiver.COLOR_BLUE, 0,
            "BGM", String.valueOf(bgm));
    }

    @Override
    public boolean onReady(GameEngine engine, int playerID) {
        // 横溜め
        if (engine.ruleopt.dasInReady && engine.gameActive) engine.padRepeat();
        else if (engine.ruleopt.dasRedirectInDelay) {
            engine.dasRedirect();
        }

        // Initialization
        if (engine.statc[0] == 0) {
            engine.ruleopt.nextDisplay = 0;
            engine.ruleopt.holdEnable = false;

            // fieldInitialization
            engine.displaysize = 1;

            reset(engine, true);
            localState = CUSTOMSTATE_SWAP;

            engine.ruleopt.fieldWidth = FIELD_DIMENSION;
            engine.ruleopt.fieldHeight = FIELD_DIMENSION;
            engine.ruleopt.fieldHiddenHeight = 0;
            engine.ruleopt.fieldCeiling = true;

            engine.fieldWidth = engine.ruleopt.fieldWidth;
            engine.fieldHeight = engine.ruleopt.fieldHeight;
            engine.fieldHiddenHeight = engine.ruleopt.fieldHiddenHeight;
            engine.field = new Field(engine.fieldWidth, engine.fieldHeight, engine.fieldHiddenHeight, engine.ruleopt.fieldCeiling);

            if (!engine.readyDone) {
                //  button input状態リセット
                engine.ctrl.reset();
                // ゲーム中 flagON
                engine.gameActive = true;
                engine.gameStarted = true;
                engine.isInGame = true;
            }
        }

        // READY音
        if (engine.statc[0] == engine.readyStart) engine.playSE("ready");

        // GO音
        if (engine.statc[0] == engine.goStart) engine.playSE("go");

        // 開始
        if (engine.statc[0] >= engine.goEnd) {
            if (!engine.readyDone) engine.owner.bgmStatus.bgm = bgm;
            if (engine.owner.mode != null) engine.owner.mode.startGame(engine, playerID);
            engine.owner.receiver.startGame(engine, playerID);
            engine.stat = GameEngine.STAT_CUSTOM;
            engine.resetStatc();
            if (!engine.readyDone) {
                engine.startTime = System.nanoTime();
                //startTime = System.nanoTime()/1000000L;
            }
            engine.readyDone = true;
            return true;
        }

        engine.statc[0]++;

        return true;
    }

    // TODO: Add in rankings later. For now focus on the onCustom mechanics.
    @Override
    public boolean onGameOver(GameEngine engine, int playerID) {
        if (engine.lives <= 0) {
            // もう復活できないとき
            if (engine.statc[0] == 0) {
                engine.gameEnded();
                engine.blockShowOutlineOnly = false;
                if (owner.getPlayers() < 2) owner.bgmStatus.bgm = BGMStatus.BGM_NOTHING;

                if (engine.field.isEmpty()) {
                    engine.statc[0] = engine.field.getHeight() + 1;
                } else {
                    engine.resetFieldVisible();
                }
            }

            if (engine.statc[0] < engine.field.getHeight() + 1) {
                for (int i = 0; i < engine.field.getWidth(); i++) {
                    if (engine.field.getBlockColor(i, engine.field.getHeight() - engine.statc[0]) != Block.BLOCK_COLOR_NONE) {
                        Block blk = engine.field.getBlock(i, engine.field.getHeight() - engine.statc[0]);

                        if (blk != null && blk.color > Block.BLOCK_COLOR_NONE) {
                            if (!blk.getAttribute(Block.BLOCK_ATTRIBUTE_GARBAGE)) {
                                blk.color = Block.BLOCK_COLOR_GRAY;
                                blk.setAttribute(Block.BLOCK_ATTRIBUTE_GARBAGE, true);
                            }
                            blk.darkness = 0.3f;
                            blk.elapsedFrames = -1;
                        }
                    }
                }
                engine.statc[0]++;
            } else if (engine.statc[0] == engine.field.getHeight() + 1) {
                engine.playSE("gameover");
                engine.statc[0]++;
            } else if (engine.statc[0] < engine.field.getHeight() + 1 + 180) {
                if ((engine.statc[0] >= engine.field.getHeight() + 1 + 60) && (engine.ctrl.isPush(Controller.BUTTON_A))) {
                    engine.statc[0] = engine.field.getHeight() + 1 + 180;
                }

                engine.statc[0]++;
            } else {
                // if (enableBombs) updateRanking(engine.statistics.score, difficulty, engine.statistics.level + 1);
                // if (rankingRank != -1) saveRanking(owner.modeConfig);
                // receiver.saveModeConfig(owner.modeConfig);

                for (int i = 0; i < owner.getPlayers(); i++) {
                    if ((i == playerID) || (engine.gameoverAll)) {
                        if (owner.engine[i].field != null) {
                            owner.engine[i].field.reset();
                        }
                        owner.engine[i].resetStatc();
                        owner.engine[i].stat = GameEngine.STAT_RESULT;
                    }
                }
            }
        } else {
            // 復活できるとき
            if (engine.statc[0] == 0) {
                engine.blockShowOutlineOnly = false;
                engine.playSE("died");

                engine.resetFieldVisible();

                for (int i = (engine.field.getHiddenHeight() * -1); i < engine.field.getHeight(); i++) {
                    for (int j = 0; j < engine.field.getWidth(); j++) {
                        if (engine.field.getBlockColor(j, i) != Block.BLOCK_COLOR_NONE) {
                            engine.field.setBlockColor(j, i, Block.BLOCK_COLOR_GRAY);
                        }
                    }
                }

                engine.statc[0] = 1;
            }

            if (!engine.field.isEmpty()) {
                engine.field.pushDown();
            } else if (engine.statc[1] < engine.getARE()) {
                engine.statc[1]++;
            } else {
                engine.lives--;
                engine.resetStatc();
                engine.stat = GameEngine.STAT_CUSTOM;
            }
        }
        return true;
    }

    // TODO: Fill in all the state cases by making the appropriate methods.
    @Override
    public boolean onCustom(GameEngine engine, int playerID) {
        mouseControl.update();
        boolean updateTime = false;

        switch (localState) {
            case CUSTOMSTATE_SWAP:
                updateTime = statSwap(engine, playerID);
                break;
            case CUSTOMSTATE_CHECK:
                updateTime = statCheck(engine, playerID);
                break;
            case CUSTOMSTATE_ACTION:
                updateTime = statAction(engine, playerID);
                break;
            case CUSTOMSTATE_ANIMATION:
                updateTime = statAnimation(engine, playerID);
                break;
            case CUSTOMSTATE_CLEAR:
                updateTime = statClear(engine, playerID);
                break;
            case CUSTOMSTATE_DROP:
                updateTime = statDrop(engine, playerID);
                break;
            default:
                break;
        }

        updateEffectArrays();

        if (updateTime) engine.statc[0]++;
        return true;
    }

    private boolean statSwap(GameEngine engine, int playerID) {
        if (engine.statc[0] == 0) {
            combo = 0;
            mainField.resetFallStatus();
            mainField.resetSwapStatus();
            mainField.resetMatchArrays();
            clearSelection();
        }

        if (mainField.possibleMatches() <= 0) {
            engine.resetStatc();
            engine.stat = GameEngine.STAT_GAMEOVER;

            clearEffects();
            localState = CUSTOMSTATE_IDLE;
            return false;
        }

        if (mouseControl.getMouseClick(MouseParser.BUTTON_LEFT)) {
            int[] fieldLocationSelected = parseMouse(engine, playerID);

            if (fieldLocationSelected[0] >= 0 && fieldLocationSelected[0] < FIELD_DIMENSION &&
                fieldLocationSelected[1] >= 0 && fieldLocationSelected[1] < FIELD_DIMENSION) {
                if (!selected) {
                    fieldSelection1 = fieldLocationSelected;
                    selected = true;
                } else {
                    if ((fieldLocationSelected[0] == (fieldSelection1[0] + 1) && fieldLocationSelected[1] == (fieldSelection1[1])) ||
                        (fieldLocationSelected[0] == (fieldSelection1[0] - 1) && fieldLocationSelected[1] == (fieldSelection1[1])) ||
                        (fieldLocationSelected[0] == (fieldSelection1[0]) && fieldLocationSelected[1] == (fieldSelection1[1] + 1)) ||
                        (fieldLocationSelected[0] == (fieldSelection1[0]) && fieldLocationSelected[1] == (fieldSelection1[1] - 1))) {
                        fieldSelection2 = fieldLocationSelected;

                        localState = CUSTOMSTATE_CHECK;
                        engine.resetStatc();
                        return false;
                    } else {
                        if (fieldLocationSelected[0] != fieldSelection1[0] && fieldLocationSelected[1] != fieldSelection1[1]) {
                            fieldSelection1 = fieldLocationSelected;
                            selected = true;
                        } else {
                            clearSelection();
                        }
                    }
                }
            } else {
                clearSelection();
            }
        }

        return true;
    }

    private boolean statCheck(GameEngine engine, int playerID) {
        if (engine.statc[0] == 0) {
            oldField = new GemField(mainField);

            if (fieldSelection1[0] != -1) {
                mainField.swapGems(fieldSelection1[0], fieldSelection1[1], fieldSelection2[0], fieldSelection2[1]);
            }
            mainField.matchCheck();
			
			/* else {
				int c1 = mainField.getCell(fieldSelection1[0], fieldSelection1[1]).getColour();
				int c2 = mainField.getCell(fieldSelection2[0], fieldSelection2[1]).getColour();
				int cSelected = 0;
				
				if (c1 == Block.BLOCK_COLOR_GEM_RAINBOW && c2 != Block.BLOCK_COLOR_GEM_RAINBOW) {
					cSelected = c2;
				} else if (c2 == Block.BLOCK_COLOR_GEM_RAINBOW && c1 != Block.BLOCK_COLOR_GEM_RAINBOW) {
					cSelected = c1;
				} else if (c1 == Block.BLOCK_COLOR_GEM_RAINBOW && c2 == Block.BLOCK_COLOR_GEM_RAINBOW) {
					cSelected = c1;
				}
				
				IntWrapper counter = new IntWrapper(0);  // Use this later for gems cleared statistics later.
				
				for (int y = 0; y < mainField.getHeight(); y++) {
					for (int x = 0; x < mainField.getWidth(); x++) {
						if (mainField.getCell(x, y).getID() == GemField.GEMID_HYPERCUBE && mainField.getCell(x, y).getRecentSwap()) {
							if (!mainField.getCell(x, y).getActionConducted()) {
								mainField.getCell(x, y).conductAction(mainField, new int[] { cSelected }, counter);
							}
						}
					}
				}
			} */

        } else if (engine.statc[0] == 9) {
            if (mainField.isMatchExist()) {

                clearSelection();

                engine.resetStatc();
                localState = CUSTOMSTATE_ACTION;
                return false;
            }
        } else if (engine.statc[0] == 15) {
            mainField = new GemField(oldField);
            mainField.refreshLocations();

            engine.resetStatc();
            localState = CUSTOMSTATE_SWAP;
            return false;
        }

        return true;
    }

    private boolean statAction(GameEngine engine, int playerID) {
        events = new ArrayList<ScoreEvent>(8);

        for (int y = 0; y < mainField.getHeight(); y++) {
            for (int x = 0; x < mainField.getWidth(); x++) {
                if (mainField.getCellAttribute(x, y, GemField.ATTRIBUTE_DESTROY) && mainField.getCellAttribute(x, y, GemField.ATTRIBUTE_SPECIAL)) {
                    if (!mainField.getCellAttribute(x, y, GemField.ATTRIBUTE_ACTIONCONDUCTED)) {
                        mainField.getCell(x, y).conductAction(mainField, new int[] { mainField.getCell(x, y).getColour() }, events);
                    }
                } else if (mainField.getCellAttribute(x, y, GemField.ATTRIBUTE_DESTROY) && mainField.getCellAttribute(x, y, GemField.ATTRIBUTE_RECENTSWAP)) {
                    // events.add(new ScoreEvent(getScore(mainField.getFieldHorizontalMatchValues()[y][x] + mainField.getFieldVerticalMatchValues()[y][x], engine),
                    // 		   x, y, mainField.getCell(x, y).getID(), EventReceiver.COLOR_ORANGE));

                    // SPECIAL GEM PARSING GOES HERE LATER.
                }
            }
        }

        for (int y = mainField.getHeight() - 1; y >= 0; y--) {
            for (int x = 0; x < mainField.getWidth(); x++) {
                if (mainField.getCellAttribute(x, y, GemField.ATTRIBUTE_DESTROY) && mainField.getCellAttribute(x, y, GemField.ATTRIBUTE_RECENTFALL)) {
                    // events.add(new ScoreEvent(getScore(mainField.getFieldHorizontalMatchValues()[y][x] + mainField.getFieldVerticalMatchValues()[y][x], engine),
                    // 		   x, y, mainField.getCell(x, y).getID(), EventReceiver.COLOR_YELLOW));

                    int colour = mainField.getCell(x, y).getColour();

                    int xOffset = -1;
                    while (x + xOffset >= 0) {
                        if (mainField.getCell(x + xOffset, y).getColour() == colour) {
                            mainField.setCellAttribute(x + xOffset, y, GemField.ATTRIBUTE_RECENTFALL, false);
                        } else {
                            break;
                        }

                        xOffset--;
                    }
                    xOffset = 1;
                    while (x + xOffset < mainField.getWidth()) {
                        if (mainField.getCell(x + xOffset, y).getColour() == colour) {
                            mainField.setCellAttribute(x + xOffset, y, GemField.ATTRIBUTE_RECENTFALL, false);
                        } else {
                            break;
                        }

                        xOffset++;
                    }

                    int yOffset = -1;
                    while (y + yOffset >= 0) {
                        if (mainField.getCell(x, y + yOffset).getColour() == colour) {
                            mainField.setCellAttribute(x, y + yOffset, GemField.ATTRIBUTE_RECENTFALL, false);
                        } else {
                            break;
                        }

                        yOffset--;
                    }
                    yOffset = 1;
                    while (y + yOffset < mainField.getHeight()) {
                        if (mainField.getCell(x, y + yOffset).getColour() == colour) {
                            mainField.setCellAttribute(x, y + yOffset, GemField.ATTRIBUTE_RECENTFALL, false);
                        } else {
                            break;
                        }

                        yOffset++;
                    }
                }
            }
        }

        engine.resetStatc();
        localState = CUSTOMSTATE_ANIMATION;
        return false;
    }

    private boolean statAnimation(GameEngine engine, int playerID) {
        if (engine.statc[0] == 0) {
//			for (ScoreEvent event : events) {
//				int locationX = receiver.getFieldDisplayPositionX(engine, playerID) + 4 + (event.getX() * 32) + 16;
//				int locationY = receiver.getFieldDisplayPositionY(engine, playerID) + 52 + (event.getY() * 32) + 16;
//				
//				if (event.getOrigin() == GemField.GEMID_HYPERCUBE ||
//					event.getOrigin() == GemField.GEMID_STAR ||
//					event.getOrigin() == GemField.GEMID_SUPERNOVA) {
//					switch (event.getOrigin()) {
//					case GemField.GEMID_HYPERCUBE:
//						
//						break;
//					case GemField.GEMID_STAR:
//						locationX = receiver.getFieldDisplayPositionX(engine, playerID) + 4 + (event.getX() * 32) + 16;
//						locationY = receiver.getFieldDisplayPositionY(engine, playerID) + 52 + (event.getY() * 32) + 16;
//						break;
//					case GemField.GEMID_SUPERNOVA:
//						
//						break;
//					default:
//						break;
//					}
//				}
//			}
            log.info("NO ANIMS YET.");
        } else if (engine.statc[0] > 0) {
            engine.resetStatc();
            localState = CUSTOMSTATE_CLEAR;
        }

        return true;
    }

    private boolean statClear(GameEngine engine, int playerID) {
        // for (ScoreEvent scoreEvent : events) {
        // 	int locationX = receiver.getFieldDisplayPositionX(engine, playerID) + 4 + (scoreEvent.getX() * 32) + 16;
        // 	int locationY = receiver.getFieldDisplayPositionY(engine, playerID) + 52 + (scoreEvent.getY() * 32) + 16;
        //
        // 	// addEffectToArray(new ScorePopup(getScore(scoreEvent.getScoreValue(), engine), new int[] { locationX, locationY }, scoreEvent.getColour(), 1f), 1);
        // 	// TODO: -- MOVE THIS -- engine.statistics.score += getScore(scoreEvent.getScoreValue(), engine);
        // }

        engine.statistics.score += getScore(engine.field.checkLineColor(3, false, false, true), combo, engine);

        mainField.destroyGems();
        combo += 1;

        engine.resetStatc();
        localState = CUSTOMSTATE_DROP;
        return false;
    }

    private boolean statDrop(GameEngine engine, int playerID) {
        if (engine.statc[0] % 5 == 0) {
            boolean dropped = mainField.dropAllGemsOnce();
            mainField.createGemsOnTopRow();
            if (!dropped) {
                engine.resetStatc();
                localState = CUSTOMSTATE_CHECK;
                return false;
            }
        }

        return true;
    }

    private int[] parseMouse(GameEngine engine, int playerID) {
        int[] fieldLocationClicked = new int[] { -1, -1 };
        int[] mouseCoordinates = mouseControl.getMouseCoordinates();

        if (mouseControl.getMouseClick(MouseParser.BUTTON_LEFT)) {
            // TERMINAL LUMBAGO
            int fX = -1, fY = -1;

            fX = (mouseCoordinates[0] - 4 - receiver.getFieldDisplayPositionX(engine, playerID)) / 32;
            fY = (mouseCoordinates[1] - 52 - receiver.getFieldDisplayPositionY(engine, playerID)) / 32;

            fieldLocationClicked = new int[] { fX, fY };
        }

        return fieldLocationClicked;
    }

    private void updateEffectArrays() {
        for (int i = 0; i < lightningChains.length; i++) {
            if (lightningChains[i] != null) {
                if (lightningChains[i].shouldNull()) {
                    lightningChains[i] = null;
                    continue;
                }

                lightningChains[i].update();
            }
        }

        // for (int i = 0; i < scorePopups.length; i++) {
        // 	if (scorePopups[i] != null) {
        // 		if (scorePopups[i].shouldNull()) {
        // 			scorePopups[i] = null;
        // 			continue;
        // 		}
        //
        // 		scorePopups[i].update();
        // 	}
        // }
    }

    /**
     * Adds a new effect to one of the effect arrays.
     *
     * @param effect      Effect object to add.
     * @param destination Which array. 0 = lightning, 1 = score.
     */
    private void addEffectToArray(Effect effect, int destination) {
        switch (destination) {
            case 0:
                for (int i = 0; i < lightningChains.length; i++) {
                    if (lightningChains[i] == null) {
                        lightningChains[i] = effect;
                        return;
                    }
                }
                break;
            // case 1:
            // 	for (int i = 0; i < scorePopups.length; i++) {
            // 		if (scorePopups[i] == null) {
            // 			scorePopups[i] = effect;
            // 			return;
            // 		}
            // 	}
            // 	break;
            default:
                log.info("ARRAY ID NOT FOUND " + destination);
                break;
        }
    }

    private int getScore(int gems, int combo, GameEngine engine) {
        int h = gems - 2;

        int x = 1;
        for (int i = 0; i < h; i++) {
            x++;
        }

        return (int) (x * 5 * (engine.statistics.level + 1) * Math.pow(1.1, combo));
    }

    @Override
    public void onLast(GameEngine engine, int playerID) {
        if (mainField != null && engine.field != null) engine.field.copy(mainField.getFieldToDraw(engine));
    }

    @Override
    public void renderLast(GameEngine engine, int playerID) {
        if (owner.menuOnly) return;

        receiver.drawScoreFont(engine, playerID, 3, 0, getName(), EventReceiver.COLOR_WHITE);
        receiver.drawScoreFont(engine, playerID, 3, 1, "(MATCH-3)", EventReceiver.COLOR_WHITE);

        if ((engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (owner.replayMode == false))) {
//			if((owner.replayMode == false) && (enableBombs == true) && (engine.ai == null)) {
//				float scale = (receiver.getNextDisplayType() == 2) ? 0.5f : 1.0f;
//				int topY = (receiver.getNextDisplayType() == 2) ? 6 : 4;
//				receiver.drawScoreFont(engine, playerID, 3, topY-1, "SCORE    LEVEL", EventReceiver.COLOR_BLUE, scale);
//
//				for(int i = 0; i < MAX_RANKING; i++) {
//					receiver.drawScoreFont(engine, playerID,  0, topY+i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW, scale);
//					receiver.drawScoreFont(engine, playerID,  3, topY+i, String.valueOf(rankingScore[difficulty][i]), (i == rankingRank), scale);
//					receiver.drawScoreFont(engine, playerID,  12, topY+i, String.valueOf(rankingLevel[difficulty][i]), (i == rankingRank), scale);
//				}
//			}
        } else {
            if (localState == CUSTOMSTATE_CHECK || localState == CUSTOMSTATE_SWAP) {
                if (fieldSelection1[0] != -1) {
                    int locationX = receiver.getFieldDisplayPositionX(engine, playerID) + 4 + (fieldSelection1[0] * 32) + 8;
                    int locationY = receiver.getFieldDisplayPositionY(engine, playerID) + 52 + (fieldSelection1[1] * 32) + 8;

                    receiver.drawDirectFont(engine, playerID, locationX, locationY, "X", EventReceiver.COLOR_ORANGE);
                }

                if (fieldSelection2[0] != -1) {
                    int locationX = receiver.getFieldDisplayPositionX(engine, playerID) + 4 + (fieldSelection2[0] * 32) + 8;
                    int locationY = receiver.getFieldDisplayPositionY(engine, playerID) + 52 + (fieldSelection2[1] * 32) + 8;

                    receiver.drawDirectFont(engine, playerID, locationX, locationY, "X", EventReceiver.COLOR_ORANGE);
                }
            }

            receiver.drawScoreFont(engine, playerID, 3, 3, "SCORE", EventReceiver.COLOR_BLUE);
            receiver.drawScoreFont(engine, playerID, 3, 4, String.valueOf(engine.statistics.score));

            receiver.drawScoreFont(engine, playerID, 3, 6, "MOUSE COORDS", EventReceiver.COLOR_BLUE);
            receiver.drawScoreFont(engine, playerID, 3, 7, "(" + mouseControl.getMouseX() + ", " + mouseControl.getMouseY() + ")");

            receiver.drawScoreFont(engine, playerID, 3, 9, "FIELD CELL CLICKED (1)", EventReceiver.COLOR_BLUE);
            receiver.drawScoreFont(engine, playerID, 3, 10, "(" + fieldSelection1[0] + ", " + fieldSelection1[1] + ")");

            receiver.drawScoreFont(engine, playerID, 3, 12, "FIELD CELL CLICKED (2)", EventReceiver.COLOR_BLUE);
            receiver.drawScoreFont(engine, playerID, 3, 13, "(" + fieldSelection2[0] + ", " + fieldSelection2[1] + ")");

            receiver.drawScoreFont(engine, playerID, 3, 15, "CUSTOM STATE", EventReceiver.COLOR_BLUE);
            receiver.drawScoreFont(engine, playerID, 3, 16, stateNames[localState]);

            // for (Effect effect : scorePopups) {
            // 	if (effect != null) effect.draw(engine, receiver, playerID, new int[] { 0 }, null);
            // }

            for (Effect effect : lightningChains) {
                if (effect != null) effect.draw(engine, receiver, playerID, new int[] { 4 }, customHolder);
            }
        }
    }

    @Override
    public void renderResult(GameEngine engine, int playerID) {
        receiver.drawMenuFont(engine, playerID, 0, 0, "SCORE", EventReceiver.COLOR_BLUE);
        receiver.drawMenuFont(engine, playerID, 0, 1, String.format("%12s", engine.statistics.score));

        receiver.drawMenuFont(engine, playerID, 0, 2, "LEVEL", EventReceiver.COLOR_BLUE);
        receiver.drawMenuFont(engine, playerID, 0, 3, String.format("%12s", (engine.statistics.level + 1)));

        receiver.drawMenuFont(engine, playerID, 0, 4, "TIME", EventReceiver.COLOR_BLUE);
        receiver.drawMenuFont(engine, playerID, 0, 5, String.format("%12s", GeneralUtil.getTime(engine.statistics.time)));
    }

    /**
     * Load settings from property file
     *
     * @param prop Property file
     */
    private void loadSetting(CustomProperties prop) {
        bgm = prop.getProperty("gemswap.bgm", 0);
    }

    /**
     * Save settings to property file
     *
     * @param prop Property file
     */
    private void saveSetting(CustomProperties prop) {
        prop.setProperty("gemswap.bgm", bgm);
    }
}
