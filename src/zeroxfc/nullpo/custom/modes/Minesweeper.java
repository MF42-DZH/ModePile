package zeroxfc.nullpo.custom.modes;

import java.util.Random;
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
import zeroxfc.nullpo.custom.libs.GameTextUtilities;
import zeroxfc.nullpo.custom.libs.ProfileProperties;
import zeroxfc.nullpo.custom.libs.SoundLoader;
import zeroxfc.nullpo.custom.modes.objects.minesweeper.GameGrid;

public class Minesweeper extends DummyMode {
    private static final int MAX_DIM = 20;

    private static final int TIMEBONUS_SQUARE = 9;
    private static final int headerColour = EventReceiver.COLOR_YELLOW;
    private GameGrid mainGrid;
    private int length, height, cursorX, cursorY, cursorScreenX, cursorScreenY, offsetX, offsetY;
    private Block[][] blockGrid;
    private float minePercentage;
    private boolean firstClick;
    private Random localRand;
    private int bgm;
    private int bg;
    private int timeLimit;
    private int currentLimit;
    private int numberOfCover;
    private ProfileProperties playerProperties;
    private String PLAYER_NAME;

    private EventReceiver receiver;
    private GameManager owner;

    @Override
    public String getName() {
        return "MINESWEEPER";
    }

    @Override
    public void playerInit(GameEngine engine, int playerID) {
        SoundLoader.loadSoundset(SoundLoader.LOADTYPE_MINESWEEPER);

        owner = engine.owner;
        receiver = engine.owner.receiver;

        length = 0;
        height = 0;
        cursorX = 0;
        cursorY = 0;
        offsetX = 0;
        offsetY = 0;
        cursorScreenX = 0;
        cursorScreenY = 0;
        firstClick = true;
        minePercentage = 0;
        bgm = 0;
        bg = 0;
        timeLimit = 0;
        currentLimit = 0;
        numberOfCover = 0;

        engine.framecolor = GameEngine.FRAME_COLOR_BLUE;
        engine.blockOutlineType = GameEngine.BLOCK_OUTLINE_SAMECOLOR;

        mainGrid = null;
        blockGrid = null;

        if (playerProperties == null) {
            playerProperties = new ProfileProperties(headerColour);
        }

        if (!owner.replayMode) {
            loadSetting(owner.modeConfig);

            PLAYER_NAME = "";
        } else {
            loadSetting(owner.replayProp);

            PLAYER_NAME = owner.replayProp.getProperty("minesweeper.playerName", "");
        }

        engine.owner.backgroundStatus.bg = bg;
    }

    @Override
    public boolean onSetting(GameEngine engine, int playerID) {
        if (mainGrid != null) mainGrid = null;

        // Menu
        if (engine.owner.replayMode == false) {
            // Configuration changes
            int change = updateCursor(engine, 5, playerID);

            if (change != 0) {
                engine.playSE("change");

                switch (engine.statc[2]) {
                    case 0:
                        length += change;
                        if (length > 256) length = 4;
                        if (length < 4) length = 256;
                        break;
                    case 1:
                        height += change;
                        if (height > 256) height = 5;
                        if (height < 4) height = 256;
                        break;
                    case 2:
                        minePercentage += change * 0.1f;
                        if (minePercentage > 99.9f) minePercentage = 0.1f;
                        if (minePercentage < 0.1f || ((int) (length * height * (minePercentage / 100f)) == 0))
                            minePercentage = 99.9f;
                        break;
                    case 3:
                        timeLimit += 60 * change;
                        if (timeLimit > 36000) timeLimit = 0;
                        if (timeLimit < 0) timeLimit = 36000;
                        break;
                    case 4:
                        bgm += change;
                        if (bgm > 15) bgm = 0;
                        if (bgm < 0) bgm = 15;
                        break;
                    case 5:
                        bg += change;
                        if (bg > 19) bg = 0;
                        if (bg < 0) bg = 19;

                        engine.owner.backgroundStatus.bg = bg;
                        break;
                }
            }

            engine.owner.backgroundStatus.bg = bg;

            // Confirm
            if (engine.ctrl.isPush(Controller.BUTTON_A) && (engine.statc[3] >= 5)) {
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
            if (engine.ctrl.isPush(Controller.BUTTON_B)) {
                engine.quitflag = true;
                playerProperties = new ProfileProperties(headerColour);
            }

            // New acc
            if (engine.ctrl.isPush(Controller.BUTTON_E) && engine.ai == null) {
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

            return engine.statc[3] < 60;
        }

        return true;
    }

    @Override
    public void renderSetting(GameEngine engine, int playerID) {
        drawMenu(engine, playerID, receiver, 0, EventReceiver.COLOR_BLUE, 0,
            "WIDTH", String.valueOf(length),
            "HEIGHT", String.valueOf(height));
        drawMenu(engine, playerID, receiver, 4, EventReceiver.COLOR_RED, 2,
            "MINE%", String.format("%.2f", minePercentage),
            "TIME LIMIT", ((timeLimit > 0) ? GeneralUtil.getTime(timeLimit) : "DISABLED"));
        drawMenu(engine, playerID, receiver, 8, EventReceiver.COLOR_GREEN, 4,
            "BGM", String.valueOf(bgm),
            "BACKGROUND", String.valueOf(bg));
    }

    @Override
    public void onLast(GameEngine engine, int playerID) {
        if (engine.quitflag) {
            playerProperties = new ProfileProperties(EventReceiver.COLOR_GREEN);
        }
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
            // fieldInitialization
            blockGrid = new Block[height][length];

            for (int y = 0; y < blockGrid.length; y++) {
                for (int x = 0; x < blockGrid[y].length; x++) {
                    blockGrid[y][x] = new Block();
                }
            }

            cursorX = 0;
            cursorY = 0;
            offsetX = 0;
            offsetY = 0;
            cursorScreenX = 0;
            cursorScreenY = 0;

            currentLimit = timeLimit;

            firstClick = true;
            localRand = new Random(engine.randSeed);
            mainGrid = new GameGrid(length, height, minePercentage, engine.randSeed);

            engine.ruleopt.fieldWidth = Math.min(length, MAX_DIM);
            engine.ruleopt.fieldHeight = Math.min(height, MAX_DIM);
            engine.ruleopt.fieldHiddenHeight = 0;

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
                        Block covered = new Block(Block.BLOCK_COLOR_BLUE, engine.getSkin(), Block.BLOCK_ATTRIBUTE_VISIBLE | Block.BLOCK_ATTRIBUTE_OUTLINE);

                        if (blk != null) {
                            if (blk.blockToChar() == covered.blockToChar()) {
                                if (!blk.getAttribute(Block.BLOCK_ATTRIBUTE_GARBAGE)) {
                                    blk.color = Block.BLOCK_COLOR_GRAY;
                                    blk.setAttribute(Block.BLOCK_ATTRIBUTE_GARBAGE, true);
                                }
                                blk.darkness = 0.9f;
                                blk.elapsedFrames = -1;
                            }
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
                if (!owner.replayMode || owner.replayRerecord) owner.saveReplay();

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

    @Override
    public boolean onCustom(GameEngine engine, int playerID) {
        if (engine.gameActive) {
            // Block mine = new Block(Block.BLOCK_COLOR_GEM_RED, engine.getSkin(), Block.BLOCK_ATTRIBUTE_VISIBLE);
            // Block open = new Block(Block.BLOCK_COLOR_GRAY, engine.getSkin(), Block.BLOCK_ATTRIBUTE_VISIBLE);
            Block dummyBlock = new Block(Block.BLOCK_COLOR_GEM_RED);

            int changeX = 0;
            int changeY = 0;

            if (engine.ctrl.isPress(Controller.BUTTON_LEFT)) {
                if (engine.ctrl.isPush(Controller.BUTTON_LEFT) || engine.ctrl.isPress(Controller.BUTTON_E)) {
                    cursorX -= 1;
                    changeX += -1;
                    if (cursorX < 0) cursorX = length - 1;
                    engine.playSE("change");
                }
            }
            if (engine.ctrl.isPress(Controller.BUTTON_RIGHT)) {
                if (engine.ctrl.isPush(Controller.BUTTON_RIGHT) || engine.ctrl.isPress(Controller.BUTTON_E)) {
                    cursorX += 1;
                    changeX += 1;
                    if (cursorX >= length) cursorX = 0;
                    engine.playSE("change");
                }
            }
            if (engine.ctrl.isPress(Controller.BUTTON_UP)) {
                if (engine.ctrl.isPush(Controller.BUTTON_UP) || engine.ctrl.isPress(Controller.BUTTON_E)) {
                    cursorY -= 1;
                    changeY += -1;
                    if (cursorY < 0) cursorY = height - 1;
                    engine.playSE("change");
                }
            }
            if (engine.ctrl.isPress(Controller.BUTTON_DOWN)) {
                if (engine.ctrl.isPush(Controller.BUTTON_DOWN) || engine.ctrl.isPress(Controller.BUTTON_E)) {
                    cursorY += 1;
                    changeY += 1;
                    if (cursorY >= height) cursorY = 0;
                    engine.playSE("change");
                }
            }

            cursorScreenX += changeX;
            cursorScreenY += changeY;

            if (length > 20) {
                if (cursorX == 0) {
                    cursorScreenX = 0;
                    offsetX = 0;
                } else if (cursorX == length - 1) {
                    cursorScreenX = MAX_DIM - 1;
                    offsetX = length - MAX_DIM;
                } else if (cursorScreenX < 0) {
                    cursorScreenX = 0;
                    offsetX--;
                } else if (cursorScreenX >= MAX_DIM) {
                    cursorScreenX = MAX_DIM - 1;
                    offsetX++;
                }
            } else {
                cursorScreenX = cursorX;
            }

            if (height > 20) {
                if (cursorY == 0) {
                    cursorScreenY = 0;
                    offsetY = 0;
                } else if (cursorY == height - 1) {
                    cursorScreenY = MAX_DIM - 1;
                    offsetY = height - MAX_DIM;
                } else if (cursorScreenY < 0) {
                    cursorScreenY = 0;
                    offsetY--;
                } else if (cursorScreenY >= MAX_DIM) {
                    cursorScreenY = MAX_DIM - 1;
                    offsetY++;
                }
            } else {
                cursorScreenY = cursorY;
            }

            // IN ORDER: cycle state, uncover, chord uncover, chord flag

            int res;
            if (engine.ctrl.isPush(Controller.BUTTON_B)) {
                res = mainGrid.cycleState(cursorX, cursorY);
                if (res == 0) {
                    engine.playSE("hold");
                } else {
                    engine.playSE("holdfail");
                }
            } else if (engine.ctrl.isPush(Controller.BUTTON_A)) {
                if (firstClick) {
                    firstClick = false;
                    mainGrid.generateMines(cursorX, cursorY);
                }

                res = mainGrid.uncoverAt(cursorX, cursorY);
                switch (res) {
                    case GameGrid.STATE_ALREADY_OPEN:
                        engine.playSE("holdfail");
                        break;
                    case GameGrid.STATE_MINE:
                        engine.playSE("explosion" + (localRand.nextInt(4) + 1));
                        // receiver.blockBreak(engine, playerID, cursorX, cursorY, dummyBlock);
                        mainGrid.uncoverAllMines();

                        updateBlockGrid(engine);
                        updateEngineGrid(engine);

                        for (int y = 0; y < (height > 20 ? MAX_DIM : height); y++) {
                            for (int x = 0; x < (length > 20 ? MAX_DIM : length); x++) {
                                if (engine.field.getBlock(x, y).color == Block.BLOCK_COLOR_GEM_RED) {
                                    receiver.blockBreak(engine, playerID, x, y, dummyBlock);
                                }
                            }
                        }

                        //				for (int y = 0; y < height; y++) {
                        //					for (int x = 0; x < length; x++) {
                        //						if (mainGrid.getSquareAt(x, y).isMine) engine.field.getBlock(x, y).copy(mine);
                        //					}
                        //				}

                        engine.gameEnded();
                        engine.resetStatc();
                        engine.stat = GameEngine.STAT_GAMEOVER;

                        return true;
                    case GameGrid.STATE_SAFE:
                        if (numberOfCover > mainGrid.getCoveredSquares() && timeLimit > 0) {
                            int bonusCount = numberOfCover - mainGrid.getCoveredSquares();
                            currentLimit += TIMEBONUS_SQUARE * bonusCount;
                            numberOfCover = mainGrid.getCoveredSquares();
                        }

                        engine.playSE("lock");
                        break;
                    default:
                        engine.playSE("holdfail");
                        break;
                }
            } else if (engine.ctrl.isPush(Controller.BUTTON_C)) {
                if (!firstClick && mainGrid.getSurroundingMines(cursorX, cursorY) > 0 && mainGrid.getSquareAt(cursorX, cursorY).uncovered && (mainGrid.getSurroundingFlags(cursorX, cursorY) == mainGrid.getSurroundingMines(cursorX, cursorY))) {
                    int[][] testLocations = { { -1, -1 }, { 0, -1 }, { 1, -1 },
                        { -1, 0 }, { 1, 0 },
                        { -1, 1 }, { 0, 1 }, { 1, 1 } };

                    for (int[] loc : testLocations) {
                        int px = cursorX + loc[0];
                        int py = cursorY + loc[1];

                        if (px < 0 || px >= length) continue;
                        if (py < 0 || py >= height) continue;

                        res = mainGrid.uncoverAt(px, py);
                        switch (res) {
                            case GameGrid.STATE_MINE:
                                engine.playSE("explosion" + (localRand.nextInt(4) + 1));
                                mainGrid.uncoverAllMines();

                                updateBlockGrid(engine);
                                updateEngineGrid(engine);

                                for (int y = 0; y < (height > 20 ? MAX_DIM : height); y++) {
                                    for (int x = 0; x < (length > 20 ? MAX_DIM : length); x++) {
                                        if (engine.field.getBlock(x, y).color == Block.BLOCK_COLOR_GEM_RED) {
                                            receiver.blockBreak(engine, playerID, x, y, dummyBlock);
                                        }
                                    }
                                }

                                //						for (int y = 0; y < height; y++) {
                                //							for (int x = 0; x < length; x++) {
                                //								if (mainGrid.getSquareAt(x, y).isMine) engine.field.getBlock(x, y).copy(mine);
                                //							}
                                //						}

                                engine.gameEnded();
                                engine.resetStatc();
                                engine.stat = GameEngine.STAT_GAMEOVER;

                                return true;
                            default:
                                break;
                        }
                    }
                    if (numberOfCover > mainGrid.getCoveredSquares() && timeLimit > 0) {
                        int bonusCount = numberOfCover - mainGrid.getCoveredSquares();
                        currentLimit += TIMEBONUS_SQUARE * bonusCount;
                        numberOfCover = mainGrid.getCoveredSquares();
                    }

                    engine.playSE("lock");
                } else {
                    engine.playSE("holdfail");
                }
            } else if (engine.ctrl.isPush(Controller.BUTTON_D)) {
                if (!firstClick && mainGrid.getSquareAt(cursorX, cursorY).uncovered && (mainGrid.getSurroundingCovered(cursorX, cursorY) == mainGrid.getSurroundingMines(cursorX, cursorY))) {
                    int[][] testLocations = { { -1, -1 }, { 0, -1 }, { 1, -1 },
                        { -1, 0 }, { 1, 0 },
                        { -1, 1 }, { 0, 1 }, { 1, 1 } };

                    for (int[] loc : testLocations) {
                        int px = cursorX + loc[0];
                        int py = cursorY + loc[1];

                        if (px < 0 || px >= length) continue;
                        if (py < 0 || py >= height) continue;

                        if (!mainGrid.getSquareAt(px, py).uncovered) {
                            mainGrid.getSquareAt(px, py).flagged = true;
                            mainGrid.getSquareAt(px, py).question = false;
                        }
                    }
                    engine.playSE("hold");
                } else {
                    engine.playSE("holdfail");
                }
            }

            updateBlockGrid(engine);
            updateEngineGrid(engine);
            numberOfCover = mainGrid.getCoveredSquares();
            if (currentLimit > 0 && timeLimit > 0) {
                currentLimit--;
                if (currentLimit % 15 == 0 && currentLimit <= 600) engine.playSE("countdown");
                else if (currentLimit % 60 == 0 && currentLimit <= 1800) engine.playSE("countdown");
            } else if (timeLimit > 0) {
                engine.playSE("explosion" + (localRand.nextInt(4) + 1));
                mainGrid.uncoverAllMines();

                updateBlockGrid(engine);
                updateEngineGrid(engine);

                for (int y = 0; y < (height > 20 ? MAX_DIM : height); y++) {
                    for (int x = 0; x < (length > 20 ? MAX_DIM : length); x++) {
                        if (engine.field.getBlock(x, y).color == Block.BLOCK_COLOR_GEM_RED) {
                            receiver.blockBreak(engine, playerID, x, y, dummyBlock);
                        }
                    }
                }

                engine.gameEnded();
                engine.resetStatc();
                engine.stat = GameEngine.STAT_GAMEOVER;

                return true;
            }

            if (mainGrid.getCoveredSquares() == mainGrid.getMines() && mainGrid.getMines() > 0) {
                mainGrid.uncoverNonMines();
                mainGrid.flagAllCovered();

                updateBlockGrid(engine);
                updateEngineGrid(engine);

                //			for (int y = 0; y < height; y++) {
                //				for (int x = 0; x < length; x++) {
                //					if (mainGrid.getSquareAt(x, y).uncovered) engine.field.getBlock(x, y).copy(open);
                //				}
                //			}

                engine.gameEnded();
                engine.stat = GameEngine.STAT_EXCELLENT;
                engine.ending = 1;
                engine.resetStatc();
                return true;
            }

            if (engine.ending == 0) engine.timerActive = true;

            engine.statc[0]++;
        } else {
            engine.isInGame = true;

            boolean s = playerProperties.loginScreen.updateScreen(engine, playerID);
            if (playerProperties.isLoggedIn()) {
                loadSettingPlayer(playerProperties);
            }

            if (engine.stat == GameEngine.STAT_SETTING) engine.isInGame = false;
        }
        return true;
    }

    private void updateBlockGrid(GameEngine engine) {
        Block covered = new Block(Block.BLOCK_COLOR_BLUE, engine.getSkin(), Block.BLOCK_ATTRIBUTE_VISIBLE | Block.BLOCK_ATTRIBUTE_OUTLINE);
        Block open = new Block(Block.BLOCK_COLOR_GRAY, engine.getSkin(), Block.BLOCK_ATTRIBUTE_VISIBLE);
        Block mine = new Block(Block.BLOCK_COLOR_GEM_RED, engine.getSkin(), Block.BLOCK_ATTRIBUTE_VISIBLE);

        for (int y = 0; y < blockGrid.length; y++) {
            for (int x = 0; x < blockGrid[y].length; x++) {
                if (mainGrid.getSquareAt(x, y).uncovered) {
                    if (!mainGrid.getSquareAt(x, y).isMine) {
                        if (blockGrid[y][x].blockToChar() != open.blockToChar()) blockGrid[y][x].copy(open);
                    } else {
                        if (blockGrid[y][x].blockToChar() != mine.blockToChar()) blockGrid[y][x].copy(mine);
                    }
                } else {
                    if (blockGrid[y][x].blockToChar() != covered.blockToChar()) {
                        blockGrid[y][x].copy(covered);
                    }
                }
            }
        }
    }

    private void updateEngineGrid(GameEngine engine) {
        for (int y = 0; y < (height > 20 ? MAX_DIM : height); y++) {
            for (int x = 0; x < (length > 20 ? MAX_DIM : length); x++) {
                int px = x + offsetX;
                int py = y + offsetY;

                if (engine.field.getBlock(x, y).blockToChar() != blockGrid[py][px].blockToChar()) {
                    engine.field.getBlock(x, y).copy(blockGrid[py][px]);
                }
            }
        }
    }

    @Override
    public void renderLast(GameEngine engine, int playerID) {
        if (owner.menuOnly) return;

        int ix = (((length < MAX_DIM) ? length : MAX_DIM) - 10);
        if (ix < 0) ix = 0;

        receiver.drawScoreFont(engine, playerID, ix, 0, getName(), EventReceiver.COLOR_BLUE);
        receiver.drawScoreFont(engine, playerID, ix, 1, "(" + (int) (length * height * (minePercentage / 100f)) + " MINES)", EventReceiver.COLOR_BLUE);


        if ((engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (owner.replayMode == false))) {
            if (playerProperties.isLoggedIn()) {
                receiver.drawScoreFont(engine, playerID, ix, 3, "PLAYER", EventReceiver.COLOR_BLUE);
                receiver.drawScoreFont(engine, playerID, ix, 4, playerProperties.getNameDisplay(), EventReceiver.COLOR_WHITE, 2f);
            } else {
                receiver.drawScoreFont(engine, playerID, ix, 3, "LOGIN STATUS", EventReceiver.COLOR_BLUE);
                if (!playerProperties.isLoggedIn())
                    receiver.drawScoreFont(engine, playerID, ix, 4, "(NOT LOGGED IN)\n(E:LOG IN)");
            }
        } else if (engine.stat == GameEngine.STAT_CUSTOM && !engine.gameActive) {
            playerProperties.loginScreen.renderScreen(receiver, engine, playerID);
        } else {
            receiver.drawScoreFont(engine, playerID, ix, 3, "TIME", EventReceiver.COLOR_BLUE);
            receiver.drawScoreFont(engine, playerID, ix, 4, GeneralUtil.getTime(engine.statistics.time));

            if (timeLimit > 0) {
                receiver.drawScoreFont(engine, playerID, ix, 9, "TIME LIMIT", EventReceiver.COLOR_RED);
                receiver.drawScoreFont(engine, playerID, ix, 10, GeneralUtil.getTime(currentLimit), (currentLimit <= 1800 && (engine.statistics.time / 2) % 2 == 0));
            }

            if (mainGrid != null) {
                receiver.drawScoreFont(engine, playerID, ix, 6, "FLAGS LEFT", EventReceiver.COLOR_BLUE);
                receiver.drawScoreFont(engine, playerID, ix, 7, String.valueOf(mainGrid.getMines() - mainGrid.getFlaggedSquares()));

                int m = mainGrid.getMines() - mainGrid.getFlaggedSquares();
                float proportion = (float) m / mainGrid.getMines();
                engine.meterValue = (m * receiver.getMeterMax(engine)) / mainGrid.getMines();
                engine.meterColor = GameEngine.METER_COLOR_RED;
                if (proportion <= 0.75f) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
                if (proportion <= 0.5f) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
                if (proportion <= 0.25f) engine.meterColor = GameEngine.METER_COLOR_GREEN;

                // Block covered = new Block(Block.BLOCK_COLOR_BLUE, engine.getSkin(), Block.BLOCK_ATTRIBUTE_VISIBLE | Block.BLOCK_ATTRIBUTE_OUTLINE);
                // Block open = new Block(Block.BLOCK_COLOR_GRAY, engine.getSkin(), Block.BLOCK_ATTRIBUTE_VISIBLE);
                // Block mine = new Block(Block.BLOCK_COLOR_GEM_RED, engine.getSkin(), Block.BLOCK_ATTRIBUTE_VISIBLE);

                if (engine.stat == GameEngine.STAT_CUSTOM) {
                    if (height <= MAX_DIM && length <= MAX_DIM) {
                        for (int y = 0; y < height; y++) {
                            for (int x = 0; x < length; x++) {
                                if (mainGrid.getSquareAt(x, y).uncovered) {
                                    if (!mainGrid.getSquareAt(x, y).isMine) {
                                        int s = mainGrid.getSurroundingMines(x, y);
                                        if (s > 0) {
                                            int c = EventReceiver.COLOR_PINK;
                                            if (s == 2) c = EventReceiver.COLOR_GREEN;
                                            else if (s == 3) c = EventReceiver.COLOR_DARKBLUE;
                                            else if (s == 4) c = EventReceiver.COLOR_RED;
                                            else if (s == 5) c = EventReceiver.COLOR_CYAN;
                                            else if (s == 6) c = EventReceiver.COLOR_YELLOW;
                                            else if (s == 7) c = EventReceiver.COLOR_PURPLE;
                                            else if (s == 8) c = EventReceiver.COLOR_ORANGE;

                                            receiver.drawMenuFont(engine, playerID, x, y, String.valueOf(s), c);
                                        }
                                    }
                                } else {
                                    if (mainGrid.getSquareAt(x, y).flagged) {
                                        receiver.drawMenuFont(engine, playerID, x, y, "b", EventReceiver.COLOR_RED);
                                    } else if (mainGrid.getSquareAt(x, y).question) {
                                        receiver.drawMenuFont(engine, playerID, x, y, "?");
                                    }
                                }
                            }
                        }
                    } else {
                        for (int y = 0; y < ((height <= MAX_DIM) ? height : MAX_DIM); y++) {
                            for (int x = 0; x < ((length <= MAX_DIM) ? length : MAX_DIM); x++) {
                                int px, py;
                                px = x + offsetX;
                                py = y + offsetY;
                                if (mainGrid.getSquareAt(px, py).uncovered) {
                                    if (!mainGrid.getSquareAt(px, py).isMine) {
                                        int s = mainGrid.getSurroundingMines(px, py);
                                        if (s > 0) {
                                            int c = EventReceiver.COLOR_PINK;
                                            if (s == 2) c = EventReceiver.COLOR_GREEN;
                                            else if (s == 3) c = EventReceiver.COLOR_DARKBLUE;
                                            else if (s == 4) c = EventReceiver.COLOR_RED;
                                            else if (s == 5) c = EventReceiver.COLOR_CYAN;
                                            else if (s == 6) c = EventReceiver.COLOR_YELLOW;
                                            else if (s == 7) c = EventReceiver.COLOR_PURPLE;
                                            else if (s == 8) c = EventReceiver.COLOR_ORANGE;

                                            receiver.drawMenuFont(engine, playerID, x, y, String.valueOf(s), c);
                                        }
                                    }
                                } else {
                                    if (mainGrid.getSquareAt(px, py).flagged) {
                                        receiver.drawMenuFont(engine, playerID, x, y, "b", EventReceiver.COLOR_RED);
                                    } else if (mainGrid.getSquareAt(px, py).question) {
                                        receiver.drawMenuFont(engine, playerID, x, y, "?");
                                    }
                                }
                            }
                        }
                    }

                    receiver.drawMenuFont(engine, playerID, cursorScreenX, cursorScreenY, "f", EventReceiver.COLOR_YELLOW);
                    int foffsetX = receiver.getFieldDisplayPositionX(engine, playerID) + 4;
                    int foffsetY = receiver.getFieldDisplayPositionY(engine, playerID) + 52;
                    int size = 16;

                    if (length > MAX_DIM) {

                        if (offsetX != 0)
                            receiver.drawDirectFont(engine, playerID, foffsetX - (2 * size), foffsetY + (int) (9.5 * size), "<", EventReceiver.COLOR_YELLOW);
                        if (offsetX != length - MAX_DIM)
                            receiver.drawDirectFont(engine, playerID, foffsetX + (21 * size), foffsetY + (int) (9.5 * size), ">", EventReceiver.COLOR_YELLOW);
                    }

                    if (height > MAX_DIM) {
                        if (offsetY != 0)
                            receiver.drawDirectFont(engine, playerID, foffsetX + (int) (9.5 * size), foffsetY - (2 * size), "k", EventReceiver.COLOR_YELLOW);
                        if (offsetY != length - MAX_DIM)
                            receiver.drawDirectFont(engine, playerID, foffsetX + (int) (9.5 * size), foffsetY + (21 * size), "n", EventReceiver.COLOR_YELLOW);
                    }
                }
            }

            if (playerProperties.isLoggedIn() || PLAYER_NAME.length() > 0) {
                receiver.drawScoreFont(engine, playerID, 0, 20, "PLAYER", EventReceiver.COLOR_BLUE);
                GameTextUtilities.drawAlignedScoreText(receiver, engine, playerID, false, 0, 21, GameTextUtilities.Text.ofBig(owner.replayMode ? PLAYER_NAME : playerProperties.getNameDisplay()));
            }
        }
    }

    /*
     * Render results screen
     */
    @Override
    public void renderResult(GameEngine engine, int playerID) {
        receiver.drawMenuFont(engine, playerID, 0, 0, "TIME", EventReceiver.COLOR_BLUE);
        receiver.drawMenuFont(engine, playerID, 0, 1, String.format("%10s", GeneralUtil.getTime(engine.statistics.time)));

        receiver.drawMenuFont(engine, playerID, 0, 2, "MINES", EventReceiver.COLOR_RED);
        receiver.drawMenuFont(engine, playerID, 0, 3, String.format("%10s", mainGrid.getMines()));

        receiver.drawMenuFont(engine, playerID, 0, 4, "REMAINING", EventReceiver.COLOR_GREEN);
        receiver.drawMenuFont(engine, playerID, 0, 5, String.format("%10s", mainGrid.getMines() - mainGrid.getFlaggedSquares()));
    }

    /*
     * Called when saving replay
     */
    @Override
    public void saveReplay(GameEngine engine, int playerID, CustomProperties prop) {
        saveSetting(prop);

        if ((owner.replayMode == false)) {
            receiver.saveModeConfig(owner.modeConfig);
        }
    }

    /**
     * Load settings from property file
     *
     * @param prop Property file
     */
    private void loadSetting(CustomProperties prop) {
        length = prop.getProperty("minesweeper.length", 15);
        height = prop.getProperty("minesweeper.height", 15);
        minePercentage = prop.getProperty("minesweeper.minePercentage", 15f);
        bgm = prop.getProperty("minesweeper.bgm", 0);
        bg = prop.getProperty("minesweeper.bg", 0);
        timeLimit = prop.getProperty("minesweeper.timeLimit", 0);
    }

    /**
     * Save settings to property file
     *
     * @param prop Property file
     */
    private void saveSetting(CustomProperties prop) {
        prop.setProperty("minesweeper.length", length);
        prop.setProperty("minesweeper.height", height);
        prop.setProperty("minesweeper.minePercentage", minePercentage);
        prop.setProperty("minesweeper.bgm", bgm);
        prop.setProperty("minesweeper.bg", bg);
        prop.setProperty("minesweeper.timeLimit", timeLimit);
    }

    /**
     * Load settings from property file
     *
     * @param prop Property file
     */
    private void loadSettingPlayer(ProfileProperties prop) {
        length = prop.getProperty("minesweeper.length", 15);
        height = prop.getProperty("minesweeper.height", 15);
        minePercentage = prop.getProperty("minesweeper.minePercentage", 15f);
        bgm = prop.getProperty("minesweeper.bgm", 0);
        bg = prop.getProperty("minesweeper.bg", 0);
        timeLimit = prop.getProperty("minesweeper.timeLimit", 0);
    }

    /**
     * Save settings to property file
     *
     * @param prop Property file
     */
    private void saveSettingPlayer(ProfileProperties prop) {
        prop.setProperty("minesweeper.length", length);
        prop.setProperty("minesweeper.height", height);
        prop.setProperty("minesweeper.minePercentage", minePercentage);
        prop.setProperty("minesweeper.bgm", bgm);
        prop.setProperty("minesweeper.bg", bg);
        prop.setProperty("minesweeper.timeLimit", timeLimit);
    }
}
