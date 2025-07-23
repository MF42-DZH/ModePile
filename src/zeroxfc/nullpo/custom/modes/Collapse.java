package zeroxfc.nullpo.custom.modes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.IntFunction;
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
import zeroxfc.nullpo.custom.libs.FieldManipulation;
import zeroxfc.nullpo.custom.libs.GameTextUtilities;
import zeroxfc.nullpo.custom.libs.Interpolation;
import zeroxfc.nullpo.custom.libs.LevelTableBuilder;
import zeroxfc.nullpo.custom.libs.MathHelper;
import zeroxfc.nullpo.custom.libs.MouseParser;
import zeroxfc.nullpo.custom.libs.types.ObjectAlignment;
import zeroxfc.nullpo.custom.libs.ProfileProperties;
import zeroxfc.nullpo.custom.libs.CustomResourceHolder;
import zeroxfc.nullpo.custom.libs.SideWaveText;
import zeroxfc.nullpo.custom.libs.SoundLoader;
import zeroxfc.nullpo.custom.libs.WeightedRandomiser;

public class Collapse extends DummyMode {
    // Hey, have any of you played any of the Super Collapse games?
    // Field Dimensions: 12 x 16; 1 Hidden Height
    //
    // DONE: Create a weighted randomiser for the blocks.
    // DONE: Do the centre-direction column gravity thing... somehow.
    // DONE: Implement mouse control if you can.
    // DONE: Implement flying score popups.

    private static final int VERSION = 2;

    private static final int FLAG_COLOUR = Block.BLOCK_COLOR_CYAN;

    private static final int[] tableColors = {
        Block.BLOCK_COLOR_RED,
        Block.BLOCK_COLOR_BLUE,
        Block.BLOCK_COLOR_YELLOW,
        Block.BLOCK_COLOR_GREEN,
        Block.BLOCK_COLOR_PURPLE,
        Block.BLOCK_COLOR_GRAY  // SET AS BONE BLOCKS
    };  // Use bone blocks for indestructible, movable blocks.

    private static final int[] tableBombColors = {
        Block.BLOCK_COLOR_GEM_RED,
        Block.BLOCK_COLOR_GEM_BLUE,
        Block.BLOCK_COLOR_GEM_YELLOW,
        Block.BLOCK_COLOR_GEM_GREEN,
        Block.BLOCK_COLOR_GEM_PURPLE,
        Block.BLOCK_COLOR_GEM_RAINBOW
    };  // Use rainbow gems for super bombs.

    private static final int[][] tableColorWeights = {
        { 1, 1, 1, 0, 0, 0 },
        { 160, 160, 160, 0, 0, 1 },
        { 160, 160, 160, 160, 0, 3 },
        { 160, 160, 160, 160, 0, 4 },
        { 160, 160, 160, 160, 0, 5 },
        { 192, 192, 192, 192, 192, 12 },
        { 204, 204, 204, 204, 204, 20 }
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

    // Gets a colour count for a level.
    private static int getColourCount(int level) {
        int result = 0;
        int ix = 0;
        while (true) {
            if (tableLevelWeightShift[ix + 1] >= level) {
                result = ix;
                break;
            } else {
                ix++;
            }
        }

        int count = 0;

        for (int i = 0; i < tableColorWeights[result].length - 1; ++i) {
            if (tableColorWeights[result][i] > 0) ++count;
        }

        return count;
    }

    // Class representing score multipliers and big clear thresholds.
    private static class Multipliers {
        public final int bigSquareClear;
        public final int bigThreshold;
        public final int bombClear;

        private Multipliers(int bigSquareClear, int bigThreshold, int bombClear) {
            this.bigSquareClear = bigSquareClear;
            this.bigThreshold = bigThreshold;
            this.bombClear = bombClear;
        }

        // Pass in engine.statistics.level directly.
        public static Multipliers getForLevel(int level) {
            int bigSquareClear = 2 + level + 1;
            int bigThreshold = 15;
            int bombClear = 1;

            // 5c here is so much harder than 5c puyo
            if (getColourCount(level) > 4) {
                bigSquareClear *= 25;
                bigThreshold = 6;
                bombClear = 5;
            } else if (getColourCount(level) > 3) {
                bigSquareClear *= 10;
                bigThreshold = 10;
            }

            return new Multipliers(bigSquareClear, bigThreshold, bombClear);
        }
    }

    private static final int[] tableLevelWeightShift = {
        0, 3, 6, 9, 12, 15, 18, Integer.MAX_VALUE
    };

    private static final int[] tableLevelStartLines = {
        3, 4, 4, 5, 5, 5,
        6, 6, 6, 7, 7, 7,
        8, 8, 8, 9, 9, 9,
        10, 10, 10
    };

    private static final int[] tableSpawnSpeedEasy = {
        20, 19, 18, 17, 16, 15,
        14, 13, 12, 12, 11, 11,
        10, 10, 9, 9, 8, 8,
        7, 6, 5
    }; // MAX: 1 row/s

    private static final IntFunction<Integer> endlessSpeedTableEasy;

    private static final int[] tableSpawnSpeedNormal = {
        15, 15, 15, 14, 14, 13,
        13, 12, 12, 11, 11, 10,
        10, 9, 8, 7, 6, 5,
        4, 3, 2
    }; // MAX: 2.5 row/s

    private static final IntFunction<Integer> endlessSpeedTableNormal;

    private static final int[] tableSpawnSpeedHard = {
        12, 12, 12, 12, 11, 11,
        11, 10, 10, 9, 8, 7,
        6, 5, 4, 4, 3, 3,
        2, 2, 1
    }; // MAX: 5 row/s

    private static final IntFunction<Integer> endlessSpeedTableHard;

    static {
        // Easy
        LevelTableBuilder<Integer>.ModifiableLevelTable currentTable = LevelTableBuilder.createNew();
        for (int i = 0; i < tableSpawnSpeedEasy.length - 1; ++i) {
            currentTable.addValue(tableSpawnSpeedEasy[i], 60 * (i + 1));
        }

        endlessSpeedTableEasy = currentTable
            .addTerminalValue(tableSpawnSpeedEasy[tableSpawnSpeedEasy.length - 1])
            .buildLevelTable();

        // Normal
        currentTable = LevelTableBuilder.createNew();
        for (int i = 0; i < tableSpawnSpeedNormal.length - 1; ++i) {
            currentTable.addValue(tableSpawnSpeedNormal[i], 50 * (i + 1));
        }

        endlessSpeedTableNormal = currentTable
            .addTerminalValue(tableSpawnSpeedNormal[tableSpawnSpeedNormal.length - 1])
            .buildLevelTable();

        // Hard
        currentTable = LevelTableBuilder.createNew();
        for (int i = 0; i < tableSpawnSpeedHard.length - 1; ++i) {
            currentTable.addValue(tableSpawnSpeedHard[i], 40 * (i + 1));
        }

        endlessSpeedTableHard = currentTable
            .addTerminalValue(tableSpawnSpeedHard[tableSpawnSpeedHard.length - 1])
            .buildLevelTable();
    }

    private static final int[] tableLevelLine = {
        20, 30, 40, 50, 60, 70,
        80, 90, 100, 120, 140, 160,
        180, 200, 240, 280, 320, 360,
        400, -1
    };

    private static int levelLinesForPast19(int level) {
        // Levels are zero-indexed, so the minimum here is 19.
        // Start at 450 onwards. 450 = 50 * 9.
        return 50 * (level - 10);
    }

    private static final int[] endlessThresholds = { 180, 150, 120 };

    private static final double BOMB_CHANCE = (0.01);

    private static final int BONUS_LEVEL_TIME_LIMIT = (15 * 60);

    private static final int GAMETYPES = 2;

    private int gameTypeRanking() {
        return endless ? 1 : 0;
    }

    private static final int MAX_RANKING = 10;

    private static final int MAX_DIFFICULTIES = 3;

    private static final String[] DIFFICULTY_NAMES = {
        "EASY",
        "NORMAL",
        "HARD"
    };

    private static final int MAX_SPEED_LINE = 8;

    private static final int HOLDER_SLICK = 0,
        HOLDER_SWING = 1,
        HOLDER_SDL = 2;

    private static final int LOCALSTATE_INGAME = 0,
        LOCALSTATE_TRANSITION = 1;

    private GameManager owner;
    private EventReceiver receiver;
    private boolean enableBombs;
    private int[][][] rankingScore;
    private int[][][] rankingLevel;
    private int bScore;
    private int rankingRank;
    private int difficulty;
    private int linesSoFar;
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
    private int scoreToDisplay, scGetTime, sinceLastClear;
    private int acTime;
    private MouseParser mouseInput;
    private Multipliers multipliers;

    private boolean isBonusLevel;
    private boolean bonusSuccess;
    private int bonusLevelTimer;

    private static boolean nextIsBonus(int level) {
        // 2, 4, 10, 12, 14, 20, 22, 24, etc.
        // Levels are zero-indexed.
        return (level % 10 == 9 || level % 10 == 1 || level % 10 == 3);
    }

    private ProfileProperties playerProperties;
    private int[][][] rankingScorePlayer;
    private int[][][] rankingLevelPlayer;
    private int rankingRankPlayer;
    private boolean showPlayerStats;
    private int outline;
    private boolean shrinkPopups;
    private int startLevel;
    private boolean endless;

    /*
     * ------ MAIN METHODS ------
     */

    @Override
    public String getName() {
        return "COLLAPSE";
    }

    @Override
    public void playerInit(GameEngine engine, int playerID) {
        SoundLoader.loadSoundset(SoundLoader.SoundSet.COLLAPSE);

        owner = engine.owner;
        receiver = engine.owner.receiver;

        if (playerProperties == null) {
            showPlayerStats = false;
            playerProperties = new ProfileProperties(EventReceiver.COLOR_ORANGE);
        }

        rankingScorePlayer = new int[GAMETYPES][MAX_DIFFICULTIES][MAX_RANKING];
        rankingLevelPlayer = new int[GAMETYPES][MAX_DIFFICULTIES][MAX_RANKING];
        rankingRankPlayer = -1;

        enableBombs = false;
        rankingScore = new int[GAMETYPES][MAX_DIFFICULTIES][MAX_RANKING];
        rankingLevel = new int[GAMETYPES][MAX_DIFFICULTIES][MAX_RANKING];
        rankingRank = -1;
        difficulty = 0;
        linesSoFar = 0;
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
        scoreToDisplay = 0;
        scGetTime = 0;
        sinceLastClear = 0;
        acTime = -1;
        outline = GameEngine.BLOCK_OUTLINE_NONE;
        shrinkPopups = true;
        mouseInput = new MouseParser();
        startLevel = 0;
        multipliers = null;
        endless = false;

        isBonusLevel = false;
        bonusSuccess = false;
        bonusLevelTimer = 0;

        resetSTextArr();

        spawnTimer = 0;
        spawnTimerLimit = 0;

        nextBlocks = new Block[12];
        resetBlockArray();

        String mainClass = CustomResourceHolder.getMainClassName();

        holderType = -1;
        if (mainClass.contains("Slick")) holderType = HOLDER_SLICK;
        else if (mainClass.contains("Swing")) holderType = HOLDER_SWING;
        else if (mainClass.contains("SDL")) holderType = HOLDER_SDL;

        engine.framecolor = GameEngine.FRAME_COLOR_YELLOW;

        loadSetting(owner.modeConfig);
        loadRanking(owner.modeConfig);

        if (playerProperties.isLoggedIn()) {
            loadSettingPlayer(playerProperties);
            loadRankingPlayer(playerProperties);
        }

        // Stops an annoying flicker when retrying when start level is > 0 (shows as > 1 for display)
        if (endless) {
            engine.owner.backgroundStatus.bg = 0;
        } else {
            engine.owner.backgroundStatus.bg = startLevel % 20;
        }
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

    private void addSText(GameEngine engine, int playerID, int score, boolean big, boolean largeclear) {
        if (holderType != HOLDER_SWING) {
            SideWaveText s = new SideWaveText(cursorX, cursorY, 1.5, !largeclear ? 0 : (big ? 24 : 16), String.valueOf(score), big, largeclear);

            for (int i = 0; i < sTextArr.length; i++) {
                if (sTextArr[i] == null) {
                    sTextArr[i] = s;
                    return;
                }
            }
        } else {
            addSText(engine, playerID, fieldX, fieldY, score, big, largeclear);
        }
    }

    private void addSText(GameEngine engine, int playerID, int flX, int flY, int score, boolean big, boolean largeclear) {
        final int baseX = receiver.getFieldDisplayPositionX(engine, playerID) + 4;
        final int baseY = receiver.getFieldDisplayPositionX(engine, playerID) + 52;

        SideWaveText s = new SideWaveText(baseX + (flX * 16) + 8, baseY + (flY * 16) + 8, 1.5, !largeclear ? 0 : (big ? 24 : 16), String.valueOf(score), big, largeclear);

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
        if (!engine.owner.replayMode) {
            // Configuration changes
            int change = updateCursor(engine, 6, playerID);

            if (change != 0) {
                engine.playSE("change");

                switch (engine.statc[2]) {
                    case 0:
                        difficulty += change;
                        if (difficulty < 0) difficulty = MAX_DIFFICULTIES - 1;
                        if (difficulty >= MAX_DIFFICULTIES) difficulty = 0;
                        break;
                    case 1:
                        endless = !endless;
                        break;
                    case 2:
                        if (!endless) {
                            startLevel += change;
                            if (startLevel < 0) startLevel = 39;
                            if (startLevel > 39) startLevel = 0;
                        }
                        break;
                    case 3:
                        enableBombs = !enableBombs;
                        break;
                    case 4:
                        bgm += change;
                        if (bgm < 0) bgm = 15;
                        if (bgm > 15) bgm = 0;
                        break;
                    case 5:
                        outline += change;
                        if (outline < GameEngine.BLOCK_OUTLINE_NONE) outline = GameEngine.BLOCK_OUTLINE_SAMECOLOR;
                        if (outline > GameEngine.BLOCK_OUTLINE_SAMECOLOR) outline = GameEngine.BLOCK_OUTLINE_NONE;
                        break;
                    case 6:
                        shrinkPopups = !shrinkPopups;
                        break;
                }
            }

            if (endless) {
                engine.owner.backgroundStatus.bg = 0;
            } else {
                engine.owner.backgroundStatus.bg = startLevel % 20;
            }

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
                playerProperties = new ProfileProperties(EventReceiver.COLOR_ORANGE);
            }

            // New acc
            if (engine.ctrl.isPush(Controller.BUTTON_E) && engine.ai == null) {
                playerProperties = new ProfileProperties(EventReceiver.COLOR_ORANGE);
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
        String outlineStr;
        switch (outline) {
            case GameEngine.BLOCK_OUTLINE_NONE:
                outlineStr = "NONE";
                break;
            case GameEngine.BLOCK_OUTLINE_NORMAL:
                outlineStr = "NORMAL";
                break;
            case GameEngine.BLOCK_OUTLINE_CONNECT:
                outlineStr = "CONNECT";
                break;
            case GameEngine.BLOCK_OUTLINE_SAMECOLOR:
                outlineStr = "COLOR";
                break;
            default:
                outlineStr = "";
                break;
        }

        drawMenu(engine, playerID, receiver, 0, EventReceiver.COLOR_RED, 0,
            "DIFFICULTY", DIFFICULTY_NAMES[difficulty],
            "ENDLESS", GeneralUtil.getONorOFF(endless));
        drawMenu(engine, playerID, receiver, 4, EventReceiver.COLOR_BLUE, 2,
            "LEVEL", endless ? "N/A" : String.valueOf(startLevel + 1));
        drawMenu(engine, playerID, receiver, 6, EventReceiver.COLOR_BLUE, 3,
            "BOMBS", GeneralUtil.getONorOFF(enableBombs),
            "BGM", String.valueOf(bgm));
        drawMenu(engine, playerID, receiver, 10, EventReceiver.COLOR_PINK, 5,
            "OUTLINE", outlineStr,
            "POPUP SHRINK", GeneralUtil.getONorOFF(shrinkPopups));
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
            engine.blockOutlineType = outline;

            engine.ruleopt.fieldWidth = 12;
            engine.ruleopt.fieldHeight = 16;
            engine.ruleopt.fieldHiddenHeight = 1;
            engine.ruleopt.nextDisplay = 0;
            engine.ruleopt.holdEnable = false;

            engine.statistics.level = startLevel;

            resetSTextArr();

            cursorX = 0;
            cursorY = 0;
            fieldX = -1;
            fieldY = -1;
            localState = -1;
            force = false;
            scoreToDisplay = 0;
            sinceLastClear = 0;
            lastLanding = false;
            lastMoved = false;

            localRandom = new Random(engine.randSeed - 1);
            wRandomEngine = new WeightedRandomiser(tableColorWeights[0], engine.randSeed);
            wRandomEngineBomb = new WeightedRandomiser(tableBombColorWeights[0], engine.randSeed + 1);

            levelUp(engine, true);
            if (endless) {
                engine.owner.backgroundStatus.bg = 0;
                engine.statistics.level = 0;
                linesLeft = -1;
            }

            engine.fieldWidth = engine.ruleopt.fieldWidth;
            engine.fieldHeight = engine.ruleopt.fieldHeight;
            engine.fieldHiddenHeight = engine.ruleopt.fieldHiddenHeight;
            engine.field = new Field(engine.fieldWidth, engine.fieldHeight, engine.fieldHiddenHeight, engine.ruleopt.fieldCeiling);
            engine.field.setAllAttribute(Block.BLOCK_ATTRIBUTE_OUTLINE, true);
            engine.field.setAllAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);

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
            if (!engine.readyDone) engine.owner.bgmStatus.bgm = -1;
            if (engine.owner.mode != null) engine.owner.mode.startGame(engine, playerID);
            engine.owner.receiver.startGame(engine, playerID);
            engine.stat = GameEngine.STAT_CUSTOM;

            for (int i = 0; i < lineSpawn; i++) {
                while (getNextEmpty() != -1) {
                    int index = getNextEmpty();
                    int temp = -1;

                    temp = wRandomEngine.nextInt();
                    if (temp == 5) continue;

                    if (linesLeft == 1) {
                        temp = Block.BLOCK_COLOR_GRAY;
                    }
                    nextBlocks[index] = new Block(tableColors[temp], engine.getSkin());
                    nextBlocks[index].setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
                    nextBlocks[index].setAttribute(Block.BLOCK_ATTRIBUTE_OUTLINE, true);
                }

                incrementField(engine);
                resetBlockArray();
            }

            engine.playSE("rise");

            localState = LOCALSTATE_INGAME;
            engine.resetStatc();

            if (!engine.readyDone) {
                engine.startTime = System.nanoTime();
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
//            linesLeft = 2;

//			engine.stat = GameEngine.STAT_EXCELLENT;
//			engine.ending = 1;
//			engine.rainbowAnimate = false;
//			return false;
//		}  // DEBUG CODE.

        if (engine.gameActive) {
            parseMouse(engine, playerID);
            if (!engine.rainbowAnimate) engine.rainbowAnimate = true;

            boolean incrementTime = false;
            switch (localState) {
                case LOCALSTATE_INGAME:
                    if (!engine.timerActive && !isBonusLevel) engine.timerActive = true;
                    incrementTime = stateInGame(engine, playerID);
                    break;
                case LOCALSTATE_TRANSITION:
                    if (engine.timerActive) engine.timerActive = false;
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
        } else {
            showPlayerStats = false;

            engine.isInGame = true;

            playerProperties.loginScreen.updateScreen(engine, playerID);
            if (playerProperties.isLoggedIn()) {
                loadRankingPlayer(playerProperties);
                loadSettingPlayer(playerProperties);
            }

            if (engine.stat == GameEngine.STAT_SETTING) engine.isInGame = false;

            return true;
        }
    }

    // DONE: Make the rest of the gamemode work.

    private void clearSquares(GameEngine engine, int playerID) {
        int score = 0;
        int squares = 0;
        boolean fromBomb = false;

        if (engine.field.getBlock(fieldX, fieldY).color <= 8 && engine.field.getBlock(fieldX, fieldY).color != Block.BLOCK_COLOR_GRAY) {
            squares = getSquares(engine, fieldX, fieldY);

            if (squares >= 3) {
                score = getBaseScore(squares);

                final boolean bigClear = squares >= multipliers.bigThreshold;
                if (bigClear) score *= multipliers.bigSquareClear;

                for (int y = 0; y < engine.field.getHeight(); y++) {
                    for (int x = 0; x < engine.field.getWidth(); x++) {
                        if (engine.field.getBlock(x, y).color == FLAG_COLOUR) {
                            engine.field.getBlock(x, y).setAttribute(Block.BLOCK_ATTRIBUTE_ERASE, true);
                        }
                    }
                }

                engine.playSE(bigClear ? "bigclear" : "normalclear");
            } else {
                engine.playSE("noclear");
                for (int y = 0; y < engine.field.getHeight(); y++) {
                    for (int x = 0; x < engine.field.getWidth(); x++) {
                        if (engine.field.getBlock(x, y).color == FLAG_COLOUR) {
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

                score = getBaseScore(squares);
            } else {
                squares = 0;

                for (int y = 0; y < engine.field.getHeight(); y++) {
                    for (int x = 0; x < engine.field.getWidth(); x++) {
                        if (isCoordWithinRadius(fieldX, fieldY, x, y, 5) && !engine.field.getBlockEmpty(x, y)) {
                            engine.field.getBlock(x, y).secondaryColor = engine.field.getBlock(x, y).color;
                            engine.field.getBlock(x, y).setAttribute(Block.BLOCK_ATTRIBUTE_ERASE, true);

                            squares++;
                        }
                    }
                }

                score = getBaseScore(squares);
            }

            score *= multipliers.bombClear;
            explode(engine);
        } else {
            engine.playSE("noclear");
        }

        if (score > 0) {
            final boolean bigClear = squares >= multipliers.bigThreshold;

            if (squares >= 6) addSText(engine, playerID, score, fromBomb, bigClear);

            if (engine.field.isEmpty()) {
                acTime = 0;
                engine.playSE("bonus");

                if (isBonusLevel) {
                    engine.statistics.score += 50000 + (40000 * (engine.statistics.level + 1));

                    bonusSuccess = true;

                    engine.resetStatc();
                    localState = LOCALSTATE_TRANSITION;
                } else {
                    engine.statistics.score += 10000 * (engine.statistics.level + 1);
                }
            }
            engine.statistics.score += score;
        }
    }

    private boolean lastLanding;
    private boolean lastMoved;

    private boolean stateInGame(GameEngine engine, int playerID) {
        updateMeter(engine);
        endLevelEmptyCounter = 0;
        endLevelEmptyRowCounter = 0;

        if (sinceLastClear > 0 && sinceLastClear % 2 == 0) {
            final Field checkField = new Field(engine.field);
            final boolean detectedLanding = FieldManipulation.freeFallStep(engine.field);

            if (!FieldManipulation.fieldEquals(engine.field, checkField)) {
                lastLanding = detectedLanding;
                lastMoved = false;
            } else {
                if (lastLanding) engine.playSE("landing");
                lastLanding = false;

                if (bringColumnsCloser(engine)) {
                    lastMoved = true;
                } else {
                    if (lastMoved) engine.playSE("nolanding");
                    lastMoved = false;
                }
            }
        }

        // Store the animated copy of the field at the moment.
        final Field tempFieldCopy = new Field(engine.field);

        // Set up the field for clear checks.
        engine.field.freeFall();
        for (int i = 0; i < 6; i++) {
            bringColumnsCloser(engine);
        }

        if (holderType != HOLDER_SWING) {
            if (fieldX != -1) {
                if (!engine.field.getBlockEmpty(fieldX, fieldY)) {
                    clearSquares(engine, playerID);
                }
            }
        } else {
            if (engine.ctrl.isPush(Controller.BUTTON_A)) {
                if (!engine.field.getBlockEmpty(fieldX, fieldY)) {
                    clearSquares(engine, playerID);
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
            sinceLastClear = 0;
        } else {
            // If no clear, put the animated field back in.
            engine.field.copy(tempFieldCopy);
        }

        if (!isBonusLevel) {
            spawnTimer++;

            if (spawnTimer >= spawnTimerLimit) {
                spawnTimer = 0;
                int index = getNextEmpty();
                if (index != -1) {
                    double coeff = localRandom.nextDouble();
                    int temp = -1;

                    // this probably lets you bag for bombs but it should make spires less annoying to deal with
                    final int chanceMultiplier = Math.max(1, engine.field.getHeight() - engine.field.getHighestBlockY() - 10);

                    if (coeff <= BOMB_CHANCE * chanceMultiplier && enableBombs && linesLeft != 1) {
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

                    engine.playSE("move");
                } else {
                    if (linesLeft > 0) linesLeft--;
                    updateMeter(engine);

                    if (linesLeft != 0) {
                        engine.playSE("rise");
                        incrementField(engine);
                    }

                    resetBlockArray();

                    if (linesLeft == 3) {
                        engine.playSE("gradeup");
                    }

                    midgameSpeedSet(engine, playerID);

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

                    if (coeff <= BOMB_CHANCE * (engine.field.getHighestBlockY() < 4 ? 3 : 1) && enableBombs && linesLeft != 1) {
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
                // Set up the field for level end checks.
                engine.field.freeFall();
                for (int i = 0; i < 6; i++) {
                    bringColumnsCloser(engine);
                }

                engine.resetStatc();
                localState = LOCALSTATE_TRANSITION;
                return false;
            }
        } else {
            if (--bonusLevelTimer <= 0 && localState != LOCALSTATE_TRANSITION) {
                engine.playSE("regret");

                bonusSuccess = false;

                engine.resetStatc();
                localState = LOCALSTATE_TRANSITION;
                return false;
            }

            if (bonusLevelTimer <= 180 && bonusLevelTimer > 0 && bonusLevelTimer % 60 == 0 && localState == LOCALSTATE_INGAME) {
                engine.playSE("countdown");
            }
        }

        if (engine.field.getHighestBlockY() < 0) {
            if (linesLeft >= 0) {
                engine.stat = GameEngine.STAT_GAMEOVER;
                engine.resetStatc();
                engine.gameEnded();
                engine.rainbowAnimate = false;
            } else {
                // Only show excellent for endless sections.
                engine.stat = GameEngine.STAT_EXCELLENT;
                engine.resetStatc();

                engine.gameEnded();
                engine.ending = 1;
                engine.rainbowAnimate = false;
            }
        }

        return false;
    }

    private void updateMeter(GameEngine engine) {
        if (isBonusLevel) {
            final double proportion = (double) bonusLevelTimer / BONUS_LEVEL_TIME_LIMIT;

            engine.meterValue = (int) (proportion * receiver.getMeterMax(engine));
            engine.meterColor = GameEngine.METER_COLOR_GREEN;
            if (proportion <= 0.75f) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
            if (proportion <= 0.5f) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
            if (proportion <= 0.25f) engine.meterColor = GameEngine.METER_COLOR_RED;
        } else {
            if (linesLeft >= 0) {
                final double proportion = (double) linesLeft / (engine.statistics.level <= 19 ? tableLevelLine[engine.statistics.level] : levelLinesForPast19(engine.statistics.level));

                engine.meterValue = (int) (proportion * receiver.getMeterMax(engine));
                engine.meterColor = GameEngine.METER_COLOR_RED;
                if (proportion <= 0.75f) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
                if (proportion <= 0.5f) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
                if (proportion <= 0.25f) engine.meterColor = GameEngine.METER_COLOR_GREEN;
            } else if (!endless) {
                engine.meterValue = receiver.getMeterMax(engine);
                engine.meterColor = GameEngine.METER_COLOR_GREEN;
            } else {
                final int threshold = endlessThresholds[difficulty];
                final double proportion = (double) (threshold - (linesSoFar % threshold)) / threshold;

                engine.meterValue = (int) (proportion * receiver.getMeterMax(engine));
                engine.meterColor = GameEngine.METER_COLOR_RED;
                if (proportion <= 0.75f) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
                if (proportion <= 0.5f) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
                if (proportion <= 0.25f) engine.meterColor = GameEngine.METER_COLOR_GREEN;
            }
        }
    }

    private int endLevelEmpties;
    private int endLevelEmptyRowCounter;
    private int endLevelEmptyCounter;

    private boolean stateTransition(GameEngine engine, int playerID) {
        if (!isBonusLevel) {
            final int fillDuration = endLevelEmpties * 3;

            if (engine.statc[0] > fillDuration + 180 && (scoreToDisplay == engine.statistics.score)) {
                resetBlockArray();
                engine.resetStatc();

                if (nextIsBonus(engine.statistics.level)) {
                    BonusFields.setBonusField(engine, engine.statistics.score);

                    isBonusLevel = true;
                    bonusLevelTimer = BONUS_LEVEL_TIME_LIMIT;
                } else {
                    levelUp(engine, false);

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
                        resetBlockArray();
                    }
                }

                engine.playSE("go");
                engine.playSE("rise");

                localState = LOCALSTATE_INGAME;
                return false;
            } else if (engine.statc[0] > 0) {
                final Block nblk = new Block(Block.BLOCK_COLOR_NONE);
                nblk.setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
                nblk.setAttribute(Block.BLOCK_ATTRIBUTE_OUTLINE, true);
                nblk.setAttribute(Block.BLOCK_ATTRIBUTE_ERASE, false);

                final Block gblk = new Block(Block.BLOCK_COLOR_GRAY);
                gblk.skin = engine.getSkin();
                gblk.setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
                gblk.setAttribute(Block.BLOCK_ATTRIBUTE_OUTLINE, true);
                gblk.setAttribute(Block.BLOCK_ATTRIBUTE_ERASE, false);

                if (engine.statc[0] >= fillDuration) {
                    int f = (engine.statc[0] - fillDuration) - 60;

                    if (f % 3 == 0 && f >= 0 && (15 - (f / 3)) >= -1) {
                        for (int x = 0; x < engine.field.getWidth(); x++) {
                            int y = 15 - (f / 3);
                            Block blk = engine.field.getBlock(x, y);


                            if (blk != null) {
                                if (blk.color > Block.BLOCK_COLOR_NONE) {
                                    receiver.blockBreak(engine, playerID, x, y, blk);
                                    engine.field.getBlock(x, y).copy(nblk);
                                }
                            }
                        }
                    }
                } else if (engine.statc[0] % 3 == 0) {
                    boolean breakOut = false;

                    for (int y = 0; y < engine.field.getHeight() && !breakOut; ++y) {
                        for (int x = 0; x < engine.field.getWidth(); ++x) {
                            final Block blk = engine.field.getBlock(x, y);
                            if (blk != null && blk.isEmpty()) {
                                breakOut = true;
                                blk.copy(gblk);

                                engine.playSE("bonuspop");
                                endLevelEmptyCounter++;

                                if (x == engine.field.getWidth() - 1) {
                                    for (int x2 = 0; x2 < engine.field.getWidth(); ++x2) {
                                        if (engine.field.getBlock(x2, y).color != Block.BLOCK_COLOR_GRAY || engine.field.getBlock(x2, y).getAttribute(Block.BLOCK_ATTRIBUTE_BONE))
                                            break;
                                        else if (x2 == x) {
                                            engine.playSE("gem");
                                            endLevelEmptyRowCounter++;

                                            int shownScore = getRawRowLevelClearBonus(engine, endLevelEmptyRowCounter);
                                            if (endLevelEmptyRowCounter > 1)
                                                shownScore = getRawRowLevelClearBonus(engine, endLevelEmptyRowCounter) - getRawRowLevelClearBonus(engine, endLevelEmptyRowCounter - 1);

                                            addSText(
                                                engine, playerID,
                                                (engine.field.getWidth() / 2) - 1, y,
                                                shownScore,
                                                false, true
                                            );

                                            for (int x3 = 0; x3 < engine.field.getWidth(); ++x3) {
                                                engine.field.getBlock(x3, y).elapsedFrames = 0;
                                            }
                                        }
                                    }
                                }

                                break;
                            }
                        }
                    }
                }

                if (engine.statc[0] == fillDuration + 120) engine.playSE("ready");
            } else {
                endLevelEmpties = FieldManipulation.getNumberOfEmptySpaces(engine.field);

                engine.playSE("stageclear");
                bScore = getLevelClearBonus(engine, false);
                engine.statistics.score += bScore;
            }
        } else {
            if (engine.statc[0] == 240) {
                engine.playSE("ready");
            } else if (engine.statc[0] >= 300 && (scoreToDisplay == engine.statistics.score)) {
                resetBlockArray();
                engine.resetStatc();
                levelUp(engine, false);

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
                    resetBlockArray();
                }

                engine.playSE("go");
                engine.playSE("rise");

                isBonusLevel = false;
                localState = LOCALSTATE_INGAME;
                return false;
            } else if (engine.statc[0] >= 60) {
                final int f = engine.statc[0] - 60;

                final Block nblk = new Block(Block.BLOCK_COLOR_NONE);
                nblk.setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
                nblk.setAttribute(Block.BLOCK_ATTRIBUTE_OUTLINE, true);
                nblk.setAttribute(Block.BLOCK_ATTRIBUTE_ERASE, false);

                if (f % 3 == 0 && 15 - f / 3 >= -1) {
                    for (int x = 0; x < engine.field.getWidth(); x++) {
                        int y = 15 - (f / 3);
                        Block blk = engine.field.getBlock(x, y);


                        if (blk != null) {
                            if (blk.color > Block.BLOCK_COLOR_NONE) {
                                receiver.blockBreak(engine, playerID, x, y, blk);
                                engine.field.getBlock(x, y).copy(nblk);
                            }
                        }
                    }
                }
            }
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
        return flagSquares(engine, x, y, engine.field.getBlockColor(x, y));
    }

    private int flagSquares(GameEngine engine, int x, int y, int color) {
        if (x >= 0 && x < 12 && y >= 0 && y < 16) {
            if (engine.field.getBlockColor(x, y) == color || engine.field.getBlockColor(x, y) == 35 || engine.field.getBlockColor(x, y) == (7 + color)) {
                engine.field.getBlock(x, y).secondaryColor = engine.field.getBlock(x, y).color;
                engine.field.getBlock(x, y).color = FLAG_COLOUR;

                return 1 + flagSquares(engine, x + 1, y, color) + flagSquares(engine, x - 1, y, color) + flagSquares(engine, x, y + 1, color) + flagSquares(engine, x, y - 1, color);
            }
        }

        return 0;
    }

    private void resetBlockArray() {
        for (int i = 0; i < nextBlocks.length; i++) {
            nextBlocks[i] = new Block(Block.BLOCK_COLOR_NONE);
            nextBlocks[i].setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
            nextBlocks[i].setAttribute(Block.BLOCK_ATTRIBUTE_OUTLINE, true);
        }
    }

    private void incrementField(GameEngine engine) {
        ++linesSoFar;

        for (int y = -1; y < engine.field.getHeight() - 1; y++) {
            for (int x = 0; x < engine.field.getWidth(); x++) {
                engine.field.getBlock(x, y).copy(engine.field.getBlock(x, y + 1));
            }
        }

        for (int x = 0; x < engine.field.getWidth(); x++) {
            engine.field.getBlock(x, engine.field.getHeight() - 1).copy(nextBlocks[x]);
        }
    }

    private boolean bringColumnsCloser(GameEngine engine) {
        final Block nblk = new Block(Block.BLOCK_COLOR_NONE);
        nblk.setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
        nblk.setAttribute(Block.BLOCK_ATTRIBUTE_OUTLINE, true);

        boolean moved = false;

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
                        moved |= !engine.field.getBlock(x2 - 1, y).isEmpty();
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
                        moved |= !engine.field.getBlock(x2 + 1, y).isEmpty();
                    }
                }

                for (int y = 0; y < engine.field.getHeight(); y++) {
                    engine.field.getBlock(11, y).copy(nblk);
                }
            }
        }

        return moved;
    }

    private void levelUp(GameEngine engine, boolean beginning) {
        if (!beginning) engine.statistics.level++;
        owner.backgroundStatus.bg = engine.statistics.level % 20;

        int effectiveLevel = Math.min(engine.statistics.level, 19);

        multipliers = Multipliers.getForLevel(engine.statistics.level);

        spawnTimer = 0;
        resetBlockArray();

        lineSpawn = tableLevelStartLines[effectiveLevel];

        linesLeft = tableLevelLine[effectiveLevel];
        if (engine.statistics.level >= 19) linesLeft = levelLinesForPast19(engine.statistics.level);

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

        wRandomEngine.setWeights(tableColorWeights[result]);
        wRandomEngineBomb.setWeights(tableBombColorWeights[result]);

        double mult = 1.5;
        if (linesLeft < 0) mult = 1.0;

        switch (difficulty) {
            case 0:
                spawnTimerLimit = (int) (tableSpawnSpeedEasy[effectiveLevel] * mult);
                break;
            case 1:
                spawnTimerLimit = (int) (tableSpawnSpeedNormal[effectiveLevel] * mult);
                break;
            case 2:
                spawnTimerLimit = (int) (tableSpawnSpeedHard[effectiveLevel] * mult);
                break;
        }
    }

    private void midgameSpeedSet(GameEngine engine, int playerID) {
        if (endless) {
            final int oldBg = engine.owner.backgroundStatus.bg;
            final int threshold = endlessThresholds[difficulty];

            switch (difficulty) {
                case 0:
                    spawnTimerLimit = endlessSpeedTableEasy.apply(linesSoFar);
                    break;
                case 1:
                    spawnTimerLimit = endlessSpeedTableNormal.apply(linesSoFar);
                    break;
                case 2:
                    spawnTimerLimit = endlessSpeedTableHard.apply(linesSoFar);
                    break;
                default:
                    break;
            }

            engine.owner.backgroundStatus.bg = (linesSoFar / threshold) % 20;
            engine.statistics.level = linesSoFar / threshold;

            if (engine.statistics.level >= 20) spawnTimerLimit = (int) Math.ceil(spawnTimerLimit * Math.pow(((threshold - 5) / (double) threshold), linesSoFar - (threshold * 20d)));
            else {
               spawnTimerLimit = (int) (spawnTimerLimit * Interpolation.smoothStep(1.5, 0.75, (linesSoFar % threshold) / (double) threshold));
            }

            int effectiveLevel = Math.min(engine.statistics.level, 19);
            multipliers = Multipliers.getForLevel(engine.statistics.level);

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

            wRandomEngine.setWeights(tableColorWeights[result]);
            wRandomEngineBomb.setWeights(tableBombColorWeights[result]);

            if (oldBg != engine.owner.backgroundStatus.bg && engine.stat != GameEngine.STAT_EXCELLENT) {
                engine.playSE("levelup");

                final Field copy = new Field(engine.field);

                // Give level end bonus.
                engine.field.freeFall();
                for (int i = 0; i < 6; i++) {
                    bringColumnsCloser(engine);
                }

                engine.statistics.level--;
                final int bonusScore = getLevelClearBonus(engine, true);
                engine.statistics.level++;

                engine.statistics.score += bonusScore;

                addSText(
                    engine, playerID,
                    (engine.field.getWidth() / 2) - 1, 2,
                    bonusScore,
                    true, true
                );

                engine.field.copy(copy);
            }

            if (spawnTimerLimit <= 0) spawnTimerLimit = 1;
        } else {
            final int effectiveLevel = Math.min(engine.statistics.level, 19);
            final int lineLimit = engine.statistics.level <= 19 ? tableLevelLine[effectiveLevel] : levelLinesForPast19(engine.statistics.level);

            if (linesLeft >= MAX_SPEED_LINE) {
                double fraction = 0.75 + (0.75 * ((double) (linesLeft - MAX_SPEED_LINE) / (lineLimit - MAX_SPEED_LINE)));
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
                if (mod > 0) spawnTimerLimit = (int) (rawSpeed + 1);
                else spawnTimerLimit = (int) rawSpeed;
            } else {
                if (linesLeft == 0) {
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
                    if (mod > 0) spawnTimerLimit = (int) (rawSpeed + 1);
                    else spawnTimerLimit = (int) rawSpeed;
                }
            }
        }
    }

    private void parseMouse(GameEngine engine, int playerID) {
        if (holderType == HOLDER_SWING) {
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
        } else {
            mouseInput.update();

            cursorX = mouseInput.getMouseX();
            cursorY = mouseInput.getMouseY();

            if (mouseInput.getMouseClick(MouseParser.MouseButton.LEFT)) {
                fieldX = mouseInput.getMouseFieldX(receiver, engine, playerID);
                fieldY = mouseInput.getMouseFieldY(receiver, engine, playerID);
            } else {
                fieldX = -1;
                fieldY = -1;
            }
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
                if (enableBombs && startLevel == 0) updateRanking(engine.statistics.score, gameTypeRanking(), difficulty, engine.statistics.level + 1);
                if (rankingRank != -1) saveRanking(owner.modeConfig);
                if (rankingRankPlayer != -1 && playerProperties.isLoggedIn()) {
                    saveRankingPlayer(playerProperties);
                    playerProperties.saveProfileConfig();
                }
                receiver.saveModeConfig(owner.modeConfig);

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

    // idk why it sometimes reaches int overflow
    private static int pointGainForFrame(int frame) {
        if (frame < 2) return 1;

        int gain = 0;

        for (int i = frame; i > 0; --i) {
            if (i < 2) gain++;
            else gain += (int) (Math.floor(Math.pow(1.2, i - 2d) + (i >= 48 ? 1 : 0)));
        }

        return gain;
    }

    private void incrementScore(GameEngine engine) {
        if (localState == LOCALSTATE_INGAME || isBonusLevel) {
            if (scGetTime % 2 == 0) {
                scoreToDisplay += (int) Math.min(Math.max(Math.pow((scGetTime >>> 1) - 6.0, 2.0), 1), Math.ceil((engine.statistics.score - scoreToDisplay) * 0.5));
//                scoreToDisplay += pointGainForFrame(scGetTime >>> 1);
                if (engine.statistics.score < scoreToDisplay) scoreToDisplay = engine.statistics.score;
            }

            if (engine.statistics.score <= scoreToDisplay) scGetTime = 0;
        } else if (localState == LOCALSTATE_TRANSITION && engine.stat == GameEngine.STAT_CUSTOM) {
            final int currentBonusDisplay = getRawLevelClearBonus(engine, endLevelEmptyCounter, endLevelEmptyRowCounter);
            final int display = engine.statistics.score - bScore + currentBonusDisplay;

            final int fillDuration = engine.field.getWidth() * (Math.min(endLevelEmptyRowCounter, engine.field.getHeight() - 1)) * 3;

            int usedTime = engine.statc[0] % (engine.field.getWidth() * 3);
            if (engine.statc[0] >= fillDuration) usedTime = engine.statc[0] - fillDuration;

            if (usedTime % 2 == 0) {
                scoreToDisplay += (int) Math.min(Math.max(Math.pow((usedTime >>> 1) - 6.0, 2.0), 1), Math.ceil((display - scoreToDisplay) * 0.5));
//                scoreToDisplay += pointGainForFrame(usedTime >>> 1);
                if (engine.statistics.score < scoreToDisplay) scoreToDisplay = engine.statistics.score;
            }
        }
    }

    @Override
    public void onLast(GameEngine engine, int playerID) {
        if (!engine.lagStop) {
            scGetTime++;
            incrementScore(engine);
            sinceLastClear++;
        }

        if (!engine.lagStop) {
            updateSTextArr();
            if (acTime < 120 && acTime >= 0) acTime++;
            else acTime = -1;
        }

        if ((engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (!owner.replayMode)) || engine.stat == GameEngine.STAT_CUSTOM) {
            // Show rank
            if (engine.ctrl.isPush(Controller.BUTTON_F) && playerProperties.isLoggedIn() && engine.stat != GameEngine.STAT_CUSTOM) {
                showPlayerStats = !showPlayerStats;
                engine.playSE("change");
            }
        }

        if (localState == LOCALSTATE_INGAME && engine.field != null && engine.field.getHighestBlockY() <= 0 && !isBonusLevel) {
            engine.framecolor = GameEngine.FRAME_COLOR_RED;
        } else {
            engine.framecolor = GameEngine.FRAME_COLOR_YELLOW;
        }

        if (engine.field != null) {
            FieldManipulation.updateAllBlockConnections(engine.field);
        }

        if (engine.quitflag) {
            playerProperties = new ProfileProperties(EventReceiver.COLOR_ORANGE);
        }
    }

    @Override
    public void renderLast(GameEngine engine, int playerID) {
        if (owner.menuOnly) return;

        receiver.drawScoreFont(engine, playerID, 0, 0, getName(), EventReceiver.COLOR_ORANGE);

        final String ruleString = String.format("%s %s", DIFFICULTY_NAMES[difficulty], endless ? "ENDLESS" : "DIFFICULTY");
        receiver.drawScoreFont(engine, playerID, 0, 1, "(" + ruleString + ")", EventReceiver.COLOR_ORANGE);

        if ((engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (!owner.replayMode))) {
            if ((!owner.replayMode) && (enableBombs) && (startLevel == 0) && (engine.ai == null)) {
                float scale = (receiver.getNextDisplayType() == 2) ? 0.5f : 1.0f;
                int topY = (receiver.getNextDisplayType() == 2) ? 6 : 4;
                receiver.drawScoreFont(engine, playerID, 3, topY - 1, "SCORE    LEVEL", EventReceiver.COLOR_BLUE, scale);

                if (showPlayerStats) {
                    for (int i = 0; i < MAX_RANKING; i++) {
                        receiver.drawScoreFont(engine, playerID, 0, topY + i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW, scale);
                        receiver.drawScoreFont(engine, playerID, 3, topY + i, String.valueOf(rankingScorePlayer[gameTypeRanking()][difficulty][i]), (i == rankingRankPlayer), scale);
                        receiver.drawScoreFont(engine, playerID, 12, topY + i, String.valueOf(rankingLevelPlayer[gameTypeRanking()][difficulty][i]), (i == rankingRankPlayer), scale);
                    }

                    receiver.drawScoreFont(engine, playerID, 0, topY + MAX_RANKING + 1, "PLAYER SCORES", EventReceiver.COLOR_BLUE);
                    GameTextUtilities.drawAlignedScoreText(receiver, engine, playerID, false, 0, topY + MAX_RANKING + 2, GameTextUtilities.Text.ofBig(playerProperties.getNameDisplay()));

                    receiver.drawScoreFont(engine, playerID, 0, topY + MAX_RANKING + 5, "F:SWITCH RANK SCREEN", EventReceiver.COLOR_GREEN);
                } else {
                    for (int i = 0; i < MAX_RANKING; i++) {
                        receiver.drawScoreFont(engine, playerID, 0, topY + i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW, scale);
                        receiver.drawScoreFont(engine, playerID, 3, topY + i, String.valueOf(rankingScore[gameTypeRanking()][difficulty][i]), (i == rankingRank), scale);
                        receiver.drawScoreFont(engine, playerID, 12, topY + i, String.valueOf(rankingLevel[gameTypeRanking()][difficulty][i]), (i == rankingRank), scale);
                    }

                    receiver.drawScoreFont(engine, playerID, 0, topY + MAX_RANKING + 1, "LOCAL SCORES", EventReceiver.COLOR_BLUE);
                    if (!playerProperties.isLoggedIn())
                        receiver.drawScoreFont(engine, playerID, 0, topY + MAX_RANKING + 2, "(NOT LOGGED IN)\n(E:LOG IN)");
                    if (playerProperties.isLoggedIn())
                        receiver.drawScoreFont(engine, playerID, 0, topY + MAX_RANKING + 5, "F:SWITCH RANK SCREEN", EventReceiver.COLOR_GREEN);
                }
            }
        } else if (!engine.gameActive && engine.stat == GameEngine.STAT_CUSTOM) {
            playerProperties.loginScreen.renderScreen(receiver, engine, playerID);
        } else {
            receiver.drawScoreFont(engine, playerID, 0, 3, "SCORE", EventReceiver.COLOR_BLUE);
            receiver.drawScoreFont(engine, playerID, 0, 4, String.valueOf(scoreToDisplay));

            receiver.drawScoreFont(engine, playerID, 0, 6, "LEVEL", EventReceiver.COLOR_BLUE);
            if (!isBonusLevel) receiver.drawScoreFont(engine, playerID, 0, 7, String.valueOf(engine.statistics.level + 1));
            else receiver.drawScoreFont(engine, playerID, 0, 7, "BONUS", EventReceiver.COLOR_YELLOW);

            int lineCountColor = EventReceiver.COLOR_WHITE;
            if (linesLeft <= 0) lineCountColor = EventReceiver.COLOR_GREEN;
            else if (linesLeft <= 3) lineCountColor = EventReceiver.COLOR_YELLOW;

            receiver.drawScoreFont(engine, playerID, 0, 9, "LINES LEFT", EventReceiver.COLOR_BLUE);
            receiver.drawScoreFont(engine, playerID, 0, 10, linesLeft >= 0 ? String.valueOf(linesLeft) : "INFINITE", lineCountColor);

            if (isBonusLevel) {
                receiver.drawScoreFont(engine, playerID, 0, 12, "TIME LIMIT", EventReceiver.COLOR_RED);
                receiver.drawScoreFont(engine, playerID, 0, 13, GeneralUtil.getTime(bonusLevelTimer), bonusLevelTimer <= 180 && bonusLevelTimer % 2 == 1);
            } else {
                receiver.drawScoreFont(engine, playerID, 0, 12, "TIME", EventReceiver.COLOR_BLUE);
                receiver.drawScoreFont(engine, playerID, 0, 13, GeneralUtil.getTime(engine.statistics.time));
            }

            if (playerProperties.isLoggedIn()) {
                receiver.drawScoreFont(engine, playerID, 0, 15, "PLAYER", EventReceiver.COLOR_BLUE);
                GameTextUtilities.drawAlignedScoreText(receiver, engine, playerID, false, 0, 16, GameTextUtilities.Text.ofBig(playerProperties.getNameDisplay()));
            }

            if (holderType == HOLDER_SWING) {
                receiver.drawMenuFont(engine, playerID, fieldX, fieldY, "f", EventReceiver.COLOR_YELLOW);
            }

            if (localState == LOCALSTATE_TRANSITION) {
                if (!isBonusLevel || bonusSuccess) {
                    final String s = isBonusLevel ? String.valueOf(50000 + (40000 * (engine.statistics.level + 1))) : String.valueOf(
                        getRawLevelClearBonus(engine, endLevelEmptyCounter, endLevelEmptyRowCounter)
                    );

                    final int l = s.length();
                    final int offset = (12 - l) / 2;
                    receiver.drawMenuFont(engine, playerID, 2, 6, isBonusLevel ? " GREAT! " : "LEVEL UP", (engine.statc[0] / 2) % 2 == 0, EventReceiver.COLOR_YELLOW, EventReceiver.COLOR_ORANGE);
                    receiver.drawMenuFont(engine, playerID, 0, 8, "BONUS POINTS", EventReceiver.COLOR_YELLOW);
                    receiver.drawMenuFont(engine, playerID, offset, 9, s);
                } else {
                    receiver.drawMenuFont(engine, playerID, 0, 7, "  TOO BAD!  ", (engine.statc[0] / 2) % 2 == 0);
                    receiver.drawMenuFont(engine, playerID, 0, 8, "BONUS FAILED", EventReceiver.COLOR_RED);
                }

                if (nextIsBonus(engine.statistics.level) && !isBonusLevel) {
                    receiver.drawMenuFont(engine, playerID, 0, 11, "  INCOMING  ", EventReceiver.COLOR_GREEN);
                    receiver.drawMenuFont(engine, playerID, 0, 12, "BONUS LEVEL!", EventReceiver.COLOR_GREEN);
                }
            }

            if (localState == LOCALSTATE_INGAME) {
                int fx = receiver.getFieldDisplayPositionX(engine, playerID) + 4;
                int fy = receiver.getFieldDisplayPositionY(engine, playerID) + 52 + (17 * 16);
                for (int i = 0; i < nextBlocks.length; i++) {
                    if (nextBlocks[i].color > Block.BLOCK_COLOR_NONE)
                        receiver.drawSingleBlock(engine, playerID, fx + (i * 16), fy, nextBlocks[i].getDrawColor(), engine.getSkin(), nextBlocks[i].getAttribute(Block.BLOCK_ATTRIBUTE_BONE), 0f, 1f, 1f);
                }

                String s = String.valueOf(10000 * (engine.statistics.level + 1));
                int l = s.length();
                int offset = (12 - l) / 2;

                if (acTime >= 0 && acTime < 120) {
                    receiver.drawMenuFont(engine, playerID, 1, 6, "ALL CLEAR!", (engine.statistics.time / 2) % 2 == 0, EventReceiver.COLOR_YELLOW, EventReceiver.COLOR_ORANGE);
                    receiver.drawMenuFont(engine, playerID, 0, 8, "BONUS POINTS", EventReceiver.COLOR_YELLOW);
                    receiver.drawMenuFont(engine, playerID, offset, 9, s);
                }
            }

            for (SideWaveText sideWaveText : sTextArr) {
                if (sideWaveText != null) {
                    int x = 0, y = 0;
                    int[] h = sideWaveText.getLocation();
                    x = h[0];
                    y = h[1];

                    float scale = 0;
                    float baseScale = sideWaveText.getBig() ? 1.5f : 1f;

                    int limit = 90;
                    if (sideWaveText.getLifeTime() < limit) {
                        scale = baseScale;
                    } else {
                        scale = baseScale - (baseScale * ((float) (sideWaveText.getLifeTime() - limit) / (120 - limit)));
                    }

                    int alpha = (int) Interpolation.smoothStep(255.0, 0.0, MathHelper.clamp((sideWaveText.getLifeTime() - 60) / 60.0, 0.0, 1.0));

                    if ((sideWaveText.getLifeTime() / 2) % 2 == 0 && sideWaveText.getLargeClear()) {
                        GameTextUtilities.drawAlignedText(
                            engine, x, y,
                            GameTextUtilities.Text.customMixColor(sideWaveText.getText(), EventReceiver.COLOR_YELLOW, 255, 255, 255, alpha, scale),
                            ObjectAlignment.MIDDLE_MIDDLE
                        );
                    } else {
                        GameTextUtilities.drawAlignedText(
                            engine, x, y,
                            GameTextUtilities.Text.customMixColor(sideWaveText.getText(), EventReceiver.COLOR_ORANGE, 255, 255, 255, alpha, scale),
                            ObjectAlignment.MIDDLE_MIDDLE
                        );
                    }
                }
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
        enableBombs = prop.getProperty("collapse.enableBombs", true);
        difficulty = prop.getProperty("collapse.difficulty", 1);
        bgm = prop.getProperty("collapse.bgm", 0);
        outline = prop.getProperty("collapse.outline", GameEngine.BLOCK_OUTLINE_NORMAL);
        shrinkPopups = prop.getProperty("collapse.shrinkscorepopups", true);
        startLevel = prop.getProperty("collapse.startlevel", 0);
        endless = prop.getProperty("collapse.endless", false);
    }

    /**
     * Save settings to property file
     *
     * @param prop Property file
     */
    private void saveSetting(CustomProperties prop) {
        prop.setProperty("collapse.enableBombs", enableBombs);
        prop.setProperty("collapse.difficulty", difficulty);
        prop.setProperty("collapse.bgm", bgm);
        prop.setProperty("collapse.outline", outline);
        prop.setProperty("collapse.shrinkscorepopups", shrinkPopups);
        prop.setProperty("collapse.startlevel", startLevel);
        prop.setProperty("collapse.endless", endless);
    }

    /**
     * Load settings from property file
     *
     * @param prop Property file
     */
    private void loadSettingPlayer(ProfileProperties prop) {
        if (!prop.isLoggedIn()) return;

        enableBombs = prop.getProperty("collapse.enableBombs", true);
        difficulty = prop.getProperty("collapse.difficulty", 1);
        bgm = prop.getProperty("collapse.bgm", 0);
        outline = prop.getProperty("collapse.outline", GameEngine.BLOCK_OUTLINE_NORMAL);
        shrinkPopups = prop.getProperty("collapse.shrinkscorepopups", true);
        startLevel = prop.getProperty("collapse.startlevel", 0);
        endless = prop.getProperty("collapse.endless", false);
    }

    /**
     * Save settings to property file
     *
     * @param prop Property file
     */
    private void saveSettingPlayer(ProfileProperties prop) {
        if (!prop.isLoggedIn()) return;

        prop.setProperty("collapse.enableBombs", enableBombs);
        prop.setProperty("collapse.difficulty", difficulty);
        prop.setProperty("collapse.bgm", bgm);
        prop.setProperty("collapse.outline", outline);
        prop.setProperty("collapse.shrinkscorepopups", shrinkPopups);
        prop.setProperty("collapse.startlevel", startLevel);
        prop.setProperty("collapse.endless", endless);
    }

    /**
     * Read rankings from property file
     *
     * @param prop Property file
     */
    protected void loadRanking(CustomProperties prop) {
        for (int m = 0; m < GAMETYPES; m++) {
            for (int i = 0; i < MAX_RANKING; i++) {
                for (int j = 0; j < MAX_DIFFICULTIES; j++) {
                    rankingScore[m][j][i] = prop.getProperty("collapse." + VERSION + ".ranking." + m + "." + j + ".score." + i, 0);
                    rankingLevel[m][j][i] = prop.getProperty("collapse." + VERSION + ".ranking." + m + "." + j + ".level." + i, 0);
                }
            }
        }
    }

    /**
     * Save rankings to property file
     *
     * @param prop Property file
     */
    private void saveRanking(CustomProperties prop) {
        for (int m = 0; m < GAMETYPES; m++) {
            for (int i = 0; i < MAX_RANKING; i++) {
                for (int j = 0; j < MAX_DIFFICULTIES; j++) {
                    prop.setProperty("collapse." + VERSION + ".ranking." + m + "." + j + ".score." + i, rankingScore[m][j][i]);
                    prop.setProperty("collapse." + VERSION + ".ranking." + m + "." + j + ".level." + i, rankingLevel[m][j][i]);
                }
            }
        }
    }

    /**
     * Read rankings from property file
     *
     * @param prop Property file
     */
    private void loadRankingPlayer(ProfileProperties prop) {
        if (!prop.isLoggedIn()) return;

        for (int m = 0; m < GAMETYPES; m++) {
            for (int i = 0; i < MAX_RANKING; i++) {
                for (int j = 0; j < MAX_DIFFICULTIES; j++) {
                    rankingScorePlayer[m][j][i] = prop.getProperty("collapse." + VERSION + ".ranking." + m + "." + j + ".score." + i, 0);
                    rankingLevelPlayer[m][j][i] = prop.getProperty("collapse." + VERSION + ".ranking." + m + "." + j + ".level." + i, 0);
                }
            }
        }
    }

    /**
     * Save rankings to property file
     *
     * @param prop Property file
     */
    private void saveRankingPlayer(ProfileProperties prop) {
        if (!prop.isLoggedIn()) return;

        for (int m = 0; m < GAMETYPES; m++) {
            for (int i = 0; i < MAX_RANKING; i++) {
                for (int j = 0; j < MAX_DIFFICULTIES; j++) {
                    prop.setProperty("collapse." + VERSION + ".ranking." + m + "." + j + ".score." + i, rankingScorePlayer[m][j][i]);
                    prop.setProperty("collapse." + VERSION + ".ranking." + m + "." + j + ".level." + i, rankingLevelPlayer[m][j][i]);
                }
            }
        }
    }

    /**
     * Update rankings
     *
     * @param sc Score
     */
    private void updateRanking(int sc, int gametype, int difficulty, int lv) {
        rankingRank = checkRanking(sc, lv, gametype, difficulty);

        if (rankingRank != -1) {
            // Shift down ranking entries
            for (int i = MAX_RANKING - 1; i > rankingRank; i--) {
                rankingScore[gametype][difficulty][i] = rankingScore[gametype][difficulty][i - 1];
                rankingLevel[gametype][difficulty][i] = rankingLevel[gametype][difficulty][i - 1];
            }

            // Add new data
            rankingScore[gametype][difficulty][rankingRank] = sc;
            rankingLevel[gametype][difficulty][rankingRank] = lv;
        }

        if (playerProperties.isLoggedIn()) {
            rankingRankPlayer = checkRankingPlayer(sc, lv, gametype, difficulty);

            if (rankingRankPlayer != -1) {
                // Shift down ranking entries
                for (int i = MAX_RANKING - 1; i > rankingRankPlayer; i--) {
                    rankingScorePlayer[gametype][difficulty][i] = rankingScorePlayer[gametype][difficulty][i - 1];
                    rankingLevelPlayer[gametype][difficulty][i] = rankingLevelPlayer[gametype][difficulty][i - 1];
                }

                // Add new data
                rankingScorePlayer[gametype][difficulty][rankingRankPlayer] = sc;
                rankingLevelPlayer[gametype][difficulty][rankingRankPlayer] = lv;
            }
        }
    }

    /**
     * Calculate ranking position
     *
     * @param sc Score
     * @return Position (-1 if unranked)
     */
    private int checkRanking(int sc, int lv, int gametype, int difficulty) {
        for (int i = 0; i < MAX_RANKING; i++) {
            if (sc > rankingScore[gametype][difficulty][i]) {
                return i;
            } else if (sc == rankingScore[gametype][difficulty][i] && lv < rankingLevel[gametype][difficulty][i]) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Calculate ranking position
     *
     * @param sc Score
     * @return Position (-1 if unranked)
     */
    private int checkRankingPlayer(int sc, int lv, int gametype, int difficulty) {
        for (int i = 0; i < MAX_RANKING; i++) {
            if (sc > rankingScorePlayer[gametype][difficulty][i]) {
                return i;
            } else if (sc == rankingScorePlayer[gametype][difficulty][i] && lv < rankingLevelPlayer[gametype][difficulty][i]) {
                return i;
            }
        }

        return -1;
    }

    /*
     * ------ MODE-SPECIFIC PRIVATE METHODS ------
     */

    /*
     * Which one of the devs (or if it's a solo dev, them) thought this was a good idea???
     *
     * (3*min(n-2;4))
     * + f(7;6;3)
     * + f(13;9;5)
     * + f(23;14;5)
     * + f(38;19;10)
     * + f(68;28;4)
     * + f(102;32;1)
     * + f(34;33;3)
     * + (68
     *    * (
     *        (floor(max(n-37;0)/8)
     *        * (floor(max(n-37;0)/8)+1)*4)
     *        + ceil(max(n-37;0)/8)
     *        * mod(max(n-37;0);8)
     *      )
     *   )
     *
     * f(x;y;z) = max(x*min(n-y;z);0)
     * n: number of squares
     *
     * End of level:
     * (((100*min(n;3))*max(n-3;1))*(x*(x+1))/2)+y
     *
     * y: empties
     * x: empty rows
     * n: level (1-indexed)
     *
     * SC1-2's scoring, provided by leikaisho
     */

    private static int scoreF(int squares, int x, int y, int z) {
        return Math.max(0, x * Math.min(squares - y, z));
    }

    private int getBaseScore(int squares) {
        final List<Integer> scores = new ArrayList<>(9);

        scores.add(3 * Math.min(squares - 2, 4));
        scores.add(scoreF(squares, 7, 6, 3));
        scores.add(scoreF(squares, 13, 9, 5));
        scores.add(scoreF(squares, 23, 14, 5));
        scores.add(scoreF(squares, 38, 19, 10));
        scores.add(scoreF(squares, 68, 28, 4));
        scores.add(scoreF(squares, 102, 32, 1));
        scores.add(scoreF(squares, 34, 33, 3));
        scores.add((int)
            (68.0 *
                (
                    ((Math.floor(Math.max(squares - 37, 0) / 8.0)) * ((Math.floor(Math.max(squares - 37, 0) / 8.0 + 1.0) * 4.0)))
                        + ((Math.ceil(Math.max(squares - 37, 0) / 8.0)) * (Math.max(squares - 37, 0) % 8))
                )
            )
        );

        return scores.stream().mapToInt(Integer::valueOf).sum();
    }

    private int getRawRowLevelClearBonus(GameEngine engine, int emptyRows) {
        final int usedLevel = engine.statistics.level + 1;
        return (((100 * Math.min(usedLevel, 3)) * Math.max(usedLevel - 3, 1)) * (emptyRows * (emptyRows + 1)) / 2);
    }

    private int getRawLevelClearBonus(GameEngine engine, int empties, int emptyRows) {
        return getRawRowLevelClearBonus(engine, emptyRows) + empties;
    }

    private int getLevelClearBonus(GameEngine engine, boolean ignoreAddedLine) {
        int h = engine.field.getHeight();
        int w = engine.field.getWidth();

        int empties = ignoreAddedLine ? w : 0;
        int emptyRows = ignoreAddedLine ? 1 : 0;

        for (int y = 0; y < h; y++) {
            int s = 0;

            // Get empty blocks in current row.
            for (int x = 0; x < w; x++) {
                Block blk = engine.field.getBlock(x, y);
                if ((blk != null) && (blk.color == Block.BLOCK_COLOR_NONE)) {
                    ++s;
                    ++empties;
                }
            }

            if (s == w) ++emptyRows;
        }

        return getRawLevelClearBonus(engine, empties, emptyRows);
    }

    /**
     * Checks if a coordinate is within a certain radius.
     *
     * @param x      X-coordinate of circle's centre.
     * @param y      Y-coordinate of circle's centre.
     * @param xTest  X-coordinate of test square.
     * @param yTest  Y-coordinate of test square.
     * @param radius The testing radius
     * @return The result of the check. true: within. false: not within.
     */
    private boolean isCoordWithinRadius(int x, int y, int xTest, int yTest, double radius) {
        int dX = xTest - x;
        int dY = yTest - y;

        final double distance = (double) (dX * dX) + (double) (dY * dY);
        return (distance <= (radius * radius));
    }

    // Is this really all this does lmao?
    private void explode(GameEngine engine) {
        engine.playSE("bombexplode");
    }

    private static class BonusFields {
        // Note: 12w x 16h fields

        public static final int[][] BONUS_FIELD_1 = {
            { 0, 0, 0, 4, 4, 3, 4, 4, 0, 4, 4, 3 },
            { 0, 0, 0, 4, 3, 3, 4, 0, 0, 4, 3, 3 },
            { 0, 0, 0, 3, 3, 3, 0, 0, 0, 3, 3, 3 },
            { 0, 0, 4, 3, 3, 4, 0, 0, 4, 3, 3, 3 },
            { 1, 1, 1, 2, 2, 2, 1, 1, 1, 2, 2, 2 },
            { 1, 1, 1, 2, 2, 2, 1, 1, 1, 2, 2, 2 },
            { 1, 1, 1, 2, 2, 2, 1, 1, 1, 2, 2, 2 },
            { 1, 1, 1, 2, 2, 2, 1, 1, 1, 2, 2, 2 },
            { 0, 0, 0, 4, 4, 3, 4, 4, 0, 4, 4, 3 },
            { 0, 0, 0, 4, 3, 3, 4, 0, 0, 4, 3, 3 },
            { 0, 0, 0, 3, 3, 3, 0, 0, 0, 3, 3, 3 },
            { 0, 0, 4, 3, 3, 4, 0, 0, 4, 3, 3, 3 },
            { 1, 1, 1, 2, 2, 2, 1, 1, 1, 2, 2, 2 },
            { 1, 1, 1, 2, 2, 2, 1, 1, 1, 2, 2, 2 },
            { 1, 1, 1, 2, 2, 2, 1, 1, 1, 2, 2, 2 },
            { 1, 1, 1, 2, 2, 2, 1, 1, 1, 2, 2, 2 },
        };

        public static final int[][] BONUS_FIELD_2 = {
            { 0, 0, 0, 3, 3, 3, 0, 0, 0, 3, 3, 3 },
            { 0, 0, 0, 3, 3, 3, 0, 0, 0, 3, 3, 3 },
            { 0, 0, 0, 3, 3, 3, 0, 0, 0, 3, 3, 3 },
            { 0, 0, 0, 3, 3, 3, 0, 0, 0, 3, 3, 3 },
            { 1, 1, 1, 4, 4, 2, 4, 4, 1, 4, 4, 2 },
            { 1, 1, 1, 4, 2, 2, 4, 1, 1, 4, 2, 2 },
            { 1, 1, 1, 2, 2, 2, 1, 1, 1, 2, 2, 2 },
            { 1, 1, 4, 2, 2, 4, 1, 1, 4, 2, 2, 2 },
            { 0, 0, 0, 3, 3, 3, 0, 0, 0, 3, 3, 3 },
            { 0, 0, 0, 3, 3, 3, 0, 0, 0, 3, 3, 3 },
            { 0, 0, 0, 3, 3, 3, 0, 0, 0, 3, 3, 3 },
            { 0, 0, 0, 3, 3, 3, 0, 0, 0, 3, 3, 3 },
            { 1, 1, 1, 4, 4, 2, 4, 4, 1, 4, 4, 2 },
            { 1, 1, 1, 4, 2, 2, 4, 1, 1, 4, 2, 2 },
            { 1, 1, 1, 2, 2, 2, 1, 1, 1, 2, 2, 2 },
            { 1, 1, 4, 2, 2, 4, 1, 1, 4, 2, 2, 2 },
        };

        public static final int[][] BONUS_FIELD_3 = {
            { 0, 0, 0, 3, 3, 3, 0, 0, 0, 3, 3, 3 },
            { 0, 0, 2, 3, 3, 3, 0, 0, 2, 3, 3, 3 },
            { 0, 2, 2, 3, 3, 3, 0, 2, 2, 3, 3, 3 },
            { 2, 2, 2, 3, 3, 3, 2, 2, 2, 3, 3, 3 },
            { 1, 1, 1, 4, 2, 2, 4, 1, 1, 4, 2, 2 },
            { 1, 1, 1, 2, 2, 2, 1, 1, 1, 2, 2, 2 },
            { 1, 1, 4, 2, 2, 4, 1, 1, 4, 2, 2, 2 },
            { 1, 4, 4, 2, 4, 4, 1, 4, 4, 2, 2, 2 },
            { 0, 0, 0, 3, 3, 3, 0, 0, 0, 3, 3, 3 },
            { 0, 0, 2, 3, 3, 3, 0, 0, 2, 3, 3, 3 },
            { 0, 2, 2, 3, 3, 3, 0, 2, 2, 3, 3, 3 },
            { 2, 2, 2, 3, 3, 3, 2, 2, 2, 3, 3, 3 },
            { 1, 1, 1, 4, 2, 2, 4, 1, 1, 4, 2, 2 },
            { 1, 1, 1, 2, 2, 2, 1, 1, 1, 2, 2, 2 },
            { 1, 1, 4, 2, 2, 4, 1, 1, 4, 2, 2, 2 },
            { 1, 4, 4, 2, 4, 4, 1, 4, 4, 2, 2, 2 },
        };


        public static void setBonusField(GameEngine engine, int scoreSeed) {
            final int[][] selected = new int[][][] { BONUS_FIELD_1, BONUS_FIELD_2, BONUS_FIELD_3 }[scoreSeed % 3];

            for (int y = 0; y < 16; ++y) {
                for (int x = 0; x < 12; ++x) {
                    final Block blk = new Block(0, engine.getSkin());
                    blk.setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
                    blk.setAttribute(Block.BLOCK_ATTRIBUTE_OUTLINE, true);

                    blk.color = tableColors[(selected[y][x] + scoreSeed) % 5];
                    engine.field.setBlock(x, y, blk);
                }
            }
        }
    }
}