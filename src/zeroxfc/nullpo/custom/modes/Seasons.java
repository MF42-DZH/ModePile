package zeroxfc.nullpo.custom.modes;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.TreeMap;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import mu.nu.nullpo.game.component.BGMStatus;
import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.component.RuleOptions;
import mu.nu.nullpo.game.component.SpeedParam;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.game.subsystem.mode.DummyMode;
import mu.nu.nullpo.util.CustomProperties;
import mu.nu.nullpo.util.GeneralUtil;
import org.apache.log4j.Logger;
import zeroxfc.nullpo.custom.libs.CustomResourceHolder;
import zeroxfc.nullpo.custom.libs.DoubleVector;
import zeroxfc.nullpo.custom.libs.FieldManipulation;
import zeroxfc.nullpo.custom.libs.GameTextUtilities;
import zeroxfc.nullpo.custom.libs.Interpolation;
import zeroxfc.nullpo.custom.libs.LevelTableBuilder;
import zeroxfc.nullpo.custom.libs.MathHelper;
import zeroxfc.nullpo.custom.libs.PrimitiveDrawingHook;
import zeroxfc.nullpo.custom.libs.ProfileProperties;
import zeroxfc.nullpo.custom.libs.RendererExtension;
import zeroxfc.nullpo.custom.libs.SpeedTableBuilder;
import zeroxfc.nullpo.custom.libs.mixins.HasCustomFieldDrawing;
import zeroxfc.nullpo.custom.libs.mixins.HasCustomLineClear;
import zeroxfc.nullpo.custom.libs.mixins.HasCustomOnMove;
import zeroxfc.nullpo.custom.libs.particles.TextEmitter;
import zeroxfc.nullpo.custom.libs.types.ObjectAlignment;
import zeroxfc.nullpo.custom.libs.types.tuples.IntPair;
import zeroxfc.nullpo.custom.libs.types.tuples.Pair;
import zeroxfc.nullpo.custom.modes.objects.seasons.*;

public class Seasons extends DummyMode implements HasCustomOnMove, HasCustomFieldDrawing, HasCustomLineClear {
    private static final Logger log = Logger.getLogger(Seasons.class);

    private static final int CURRENT_VERSION = 0;

    // TODO: This eventually will need to be changed.
    private static final IntFunction<SpeedParam> SPEED_TABLE = SpeedTableBuilder.createNew()
        .addTerminalGravity(4, 256)
        .addTerminalARE(14)
        .addTerminalLineARE(23)
        .addTerminalDAS(11)
        .addTerminalLockDelay(30)
        .addTerminalLineDelay(40)
        .buildSpeedTable();

    private enum Season {
        SPRING(GameEngine.FRAME_COLOR_GREEN),
        SUMMER(GameEngine.FRAME_COLOR_RED),
        AUTUMN(GameEngine.FRAME_COLOR_YELLOW),
        WINTER(GameEngine.FRAME_COLOR_CYAN);

        public final int defaultFrameColour;

        public static int defaultMenuFrameColour() {
            return GameEngine.FRAME_COLOR_GRAY;
        }

        Season(int defaultFrameColour) {
            this.defaultFrameColour = defaultFrameColour;
        }
    }

    private static final int HOURS_IN_DAY = 24;

    private static final int LEVELS_FEB = (28 * HOURS_IN_DAY);
    private static final int LEVELS_MAR = LEVELS_FEB + (31 * HOURS_IN_DAY);
    private static final int LEVELS_APR = LEVELS_MAR + (30 * HOURS_IN_DAY);
    private static final int LEVELS_MAY = LEVELS_APR + (31 * HOURS_IN_DAY);
    private static final int LEVELS_JUN = LEVELS_MAY + (30 * HOURS_IN_DAY);
    private static final int LEVELS_JUL = LEVELS_JUN + (31 * HOURS_IN_DAY);
    private static final int LEVELS_AUG = LEVELS_JUL + (31 * HOURS_IN_DAY);
    private static final int LEVELS_SEP = LEVELS_AUG + (30 * HOURS_IN_DAY);
    private static final int LEVELS_OCT = LEVELS_SEP + (31 * HOURS_IN_DAY);
    private static final int LEVELS_NOV = LEVELS_OCT + (30 * HOURS_IN_DAY);
    private static final int LEVELS_DEC = LEVELS_NOV + (31 * HOURS_IN_DAY);
    private static final int LEVELS_JAN = LEVELS_DEC + (31 * HOURS_IN_DAY); // Also the max level.

    // TODO: SET THIS TO LEVELS_JAN WHEN READY!!!
    private static final int MAX_LEVEL = LEVELS_JAN;

    private static final IntFunction<Integer> NEXT_SECTION_LEVELS = LevelTableBuilder.<Integer>createNew()
        .addValue(LEVELS_FEB, LEVELS_FEB)
        .addValue(LEVELS_MAR, LEVELS_MAR)
        .addValue(LEVELS_APR, LEVELS_APR)
        .addValue(LEVELS_MAY, LEVELS_MAY)
        .addValue(LEVELS_JUN, LEVELS_JUN)
        .addValue(LEVELS_JUL, LEVELS_JUL)
        .addValue(LEVELS_AUG, LEVELS_AUG)
        .addValue(LEVELS_SEP, LEVELS_SEP)
        .addValue(LEVELS_OCT, LEVELS_OCT)
        .addValue(LEVELS_NOV, LEVELS_NOV)
        .addValue(LEVELS_DEC, LEVELS_DEC)
        .addValue(LEVELS_JAN, LEVELS_JAN)
        .addTerminalValue(LEVELS_JAN)
        .buildLevelTable();

    private static final IntFunction<Integer> LEVELS_SO_FAR = LevelTableBuilder.<Integer>createNew()
        .addValue(0, LEVELS_FEB)
        .addValue(LEVELS_FEB, LEVELS_MAR)
        .addValue(LEVELS_MAR, LEVELS_APR)
        .addValue(LEVELS_APR, LEVELS_MAY)
        .addValue(LEVELS_MAY, LEVELS_JUN)
        .addValue(LEVELS_JUN, LEVELS_JUL)
        .addValue(LEVELS_JUL, LEVELS_AUG)
        .addValue(LEVELS_AUG, LEVELS_SEP)
        .addValue(LEVELS_SEP, LEVELS_OCT)
        .addValue(LEVELS_OCT, LEVELS_NOV)
        .addValue(LEVELS_NOV, LEVELS_DEC)
        .addValue(LEVELS_DEC, LEVELS_JAN)
        .addTerminalValue(LEVELS_JAN)
        .buildLevelTable();

    private static final IntFunction<String> MONTH_NAME_TABLE = LevelTableBuilder.<String>createNew()
        .addValue("FEB", LEVELS_FEB)
        .addValue("MAR", LEVELS_MAR)
        .addValue("APR", LEVELS_APR)
        .addValue("MAY", LEVELS_MAY)
        .addValue("JUN", LEVELS_JUN)
        .addValue("JUL", LEVELS_JUL)
        .addValue("AUG", LEVELS_AUG)
        .addValue("SEP", LEVELS_SEP)
        .addValue("OCT", LEVELS_OCT)
        .addValue("NOV", LEVELS_NOV)
        .addValue("DEC", LEVELS_DEC)
        .addValue("JAN", LEVELS_JAN)
        .addTerminalValue("FEB")
        .buildLevelTable();

    private static final IntFunction<Integer> MONTH_ORDINAL_TABLE = LevelTableBuilder.<Integer>createNew()
        .addValue(2, LEVELS_FEB)
        .addValue(3, LEVELS_MAR)
        .addValue(4, LEVELS_APR)
        .addValue(5, LEVELS_MAY)
        .addValue(6, LEVELS_JUN)
        .addValue(7, LEVELS_JUL)
        .addValue(8, LEVELS_AUG)
        .addValue(9, LEVELS_SEP)
        .addValue(10, LEVELS_OCT)
        .addValue(11, LEVELS_NOV)
        .addValue(12, LEVELS_DEC)
        .addValue(1, LEVELS_JAN)
        .addTerminalValue(2)
        .buildLevelTable();

    private static final IntFunction<Integer> BACKGROUND_TABLE = LevelTableBuilder.<Integer>createNew()
        .addValue(0, LEVELS_FEB)
        .addValue(1, LEVELS_MAR)
        .addValue(2, LEVELS_APR)
        .addValue(3, LEVELS_MAY)
        .addValue(4, LEVELS_JUN)
        .addValue(5, LEVELS_JUL)
        .addValue(6, LEVELS_AUG)
        .addValue(7, LEVELS_SEP)
        .addValue(8, LEVELS_OCT)
        .addValue(9, LEVELS_NOV)
        .addValue(10, LEVELS_DEC)
        .addValue(11, LEVELS_JAN)
        .addTerminalValue(12)
        .buildLevelTable();

    private static final IntFunction<Season> SEASON_TABLE = LevelTableBuilder.<Season>createNew()
        .addValue(Season.SPRING, LEVELS_APR)
        .addValue(Season.SUMMER, LEVELS_JUL)
        .addValue(Season.AUTUMN, LEVELS_OCT)
        .addTerminalValue(Season.WINTER)
        .buildLevelTable();

    private static final IntFunction<Integer> BGM_TABLE = LevelTableBuilder.<Integer>createNew()
        .addValue(BGMStatus.BGM_NORMAL1, LEVELS_APR)
        .addValue(BGMStatus.BGM_NORMAL2, LEVELS_JUL)
        .addValue(BGMStatus.BGM_NORMAL3, LEVELS_OCT)
        .addTerminalValue(BGMStatus.BGM_NORMAL4)
        .buildLevelTable();

    private static final IntFunction<Integer> BGM_FADE_LEVEL_TABLE = LevelTableBuilder.<Integer>createNew()
        .addValue(LEVELS_APR - 72, LEVELS_APR)
        .addValue(LEVELS_JUL - 72, LEVELS_JUL)
        .addValue(LEVELS_OCT - 72, LEVELS_OCT)
        .addTerminalValue(-1)
        .buildLevelTable();

    private static String levelToString(int level) {
        final String month = MONTH_NAME_TABLE.apply(level);
        final int normLevel = level - LEVELS_SO_FAR.apply(level);
        return String.format("%02d:00 %02d %s YEAR %d", normLevel % 24, (normLevel / 24) + 1, month, level >= LEVELS_DEC ? 1 : 0);
    }

    private static int getRankColour(int gameLevel, int rollLevel) {
        int colour = gameLevel >= MAX_LEVEL ? EventReceiver.COLOR_GREEN : EventReceiver.COLOR_WHITE;
        if (rollLevel >= gameLevel && gameLevel >= MAX_LEVEL) colour = EventReceiver.COLOR_ORANGE;

        return colour;
    }

    private static GameTextUtilities.TextBlock levelToRankBlock(int gameLevel, int rollLevel) {
        final int gameMonth = MONTH_ORDINAL_TABLE.apply(gameLevel);
        final int rollMonth = MONTH_ORDINAL_TABLE.apply(rollLevel);

        final int normGameLevel = gameLevel - LEVELS_SO_FAR.apply(gameLevel);
        final int normRollLevel = rollLevel - LEVELS_SO_FAR.apply(rollLevel);

        if (rollLevel >= 0) {
            return GameTextUtilities.TextBlock.of(
                GameTextUtilities.TextJustification.LEFT,
                GameTextUtilities.Text.ofSmall(
                    String.format(
                        "%02d:00 %02d/%02d/%02d",
                        normGameLevel % 24,
                        (normGameLevel % 24) + 1,
                        gameMonth,
                        gameLevel >= LEVELS_DEC ? 1 : 0
                    )
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.ofSmall(
                    String.format(
                        "%02d:00 %02d/%02d/%02d",
                        normRollLevel % 24,
                        (normRollLevel % 24) + 1,
                        rollMonth,
                        rollLevel >= LEVELS_DEC ? 1 : 0
                    )
                )
            );
        } else {
            return GameTextUtilities.TextBlock.of(
                GameTextUtilities.TextJustification.LEFT,
                GameTextUtilities.Text.ofSmall(
                    String.format(
                        "%02d:00 %02d/%02d/%02d",
                        normGameLevel % 24,
                        (normGameLevel % 24) + 1,
                        gameMonth,
                        gameLevel >= LEVELS_DEC ? 1 : 0
                    )
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.ofSmall("TBC", EventReceiver.COLOR_CYAN)
            );
        }
    }

    // Game manager and current renderer.
    private GameManager owner;
    private EventReceiver receiver;
    private CustomResourceHolder customGraphics;
    private RendererExtension rendererExtension;
    private PrimitiveDrawingHook drawing;

    // Settings
    private static final int HEADER_COLOUR = EventReceiver.COLOR_ORANGE;
    private SeasonsSettings settings;
    private ProfileProperties playerProperties;
    private boolean showPlayerStats;
    private RuleOptions ruleOptCopy;

    private boolean getGimmickPerkBoost() {
        return settings.perk == SeasonPerk.AUTUMN_PASSIVE;
    }

    // Gameplay variables
    private boolean levelUpFlag;
    private int nextSectionLevel;
    private Season currentSeason;
    private int lastBackground, currentBackground, fadeProgress;
    private Badges badges;
    private final TextEmitter textEmitter = new TextEmitter();
    private final Collection<RewindBlock> rewindBlocks = new LinkedList<>();
    private Random rewindBlockRandom;

    private static final int INCREMENT_IN_ROLL = 1; // TODO: Change only if roll is too slow.
    private int naturalLevelIncrement;

    private int currentEnergy;
    private int currentAbilityTimer;
    private boolean queuedFreefall; // Freefall only works between pieces so it doesn't screw you over.
    private boolean hasLandedBefore;
    private int lockedPieces;
    private NavigableMap<Integer, NextAndFieldState> statesAtTimes;
    private boolean rollStarted;
    private int rollLevelReached;
    private boolean fieldPurifyQueued;
    private int brokenBoneBlocks;
    private int brokenSproutlings;
    private int brokenFlorets;
    private int brokenRainbowBlocks;
    private int brokenSnowBlocks;
    private int blocksUnderSnow;
    private int hardBlocksSeen;
    private boolean canPushdown;

    // Current Gimmicks
    private Gimmicks.Sproutlings gimmickSprMo2;
    private Gimmicks.FlourishingBlooms gimmickSprMo3;
    private Gimmicks.Dehydration gimmickSumMo1;
    private Gimmicks.Mirage gimmickSumMo2;
    private Gimmicks.IntoTheFire gimmickSumMo3;
    private Gimmicks.FallsCall gimmickAutMo1;
    private Gimmicks.FlowingWinds gimmickAutMo2;
    private Gimmicks.GhoulsAfoot gimmickAutMo3;
    private Gimmicks.Whiteout gimmickWinMo1;
    private Gimmicks.SnowMounds gimmickWinMo2;
    private Gimmicks.ZeroCelsius gimmickWinMo3;

    private Object gimmickRollSpr; // TODO
    private Object gimmickRollSum; // TODO
    private Object gimmickRollAut; // TODO
    private Object gimmickRollWin; // TODO
    // TODO: ADD THE REST OF THEM

    private static class DescriptionDraw {
        private static final int DURATION = 300;

        public final Gimmicks.HasDescription descObj;
        private final GameTextUtilities.TextBlock descBlk;

        private int counter;

        public DescriptionDraw(Gimmicks.HasDescription descObj) {
            this.descObj = descObj;
            this.descBlk = descObj.getDescription();

            this.counter = 0;
        }

        public int getDrawXOffset() {
            if (counter < 15) {
                return (int) Math.floor(Interpolation.tanStep((descBlk.getWidth() + 16) * 2d, 0d, (counter + 15) / 30d));
            } else {
                return 0;
            }
        }

        public int getDrawY() {
            if (counter < 240) {
                return 400;
            } else {
                return (int) Math.floor(Interpolation.tanStep(
                    400.0, 480.0 + (2.5 * descBlk.getHeight()),
                    (counter - 240.0) / 60.0
                ));
            }
        }

        public boolean update() {
            return (counter++ >= DURATION);
        }
    }

    private DescriptionDraw descriptionToDraw;

    private BlockVortex vortex;
    private Random bvr;

    private enum CustomState { PROFILE, FREEFALL, REWIND, FINAL_REWIND, FINAL_REWIND_RECOVERY }
    private CustomState customState;

    private static final int REWIND_TIME = 60 * 5;
    private static final int FINAL_REWIND_TIME = 60 * 40;

    @Override
    public FrameDrawingParameters getFrameDrawingParameters(GameEngine engine, int playerID) {
        return null;
    }

    @Override
    public int getLastBackground() {
        return lastBackground;
    }

    @Override
    public int getCurrentBackground() {
        return currentBackground;
    }

    @Override
    public float getFadeProgress() {
        return fadeProgress / 300f;
    }

    private void setNewBackground(int newBg) {
        if (currentBackground == newBg) return;

        lastBackground = currentBackground;
        currentBackground = newBg;
        fadeProgress = 0;
    }

    private void updateFadeProgress() {
        fadeProgress = Math.min(300, fadeProgress + 15);
    }

    private void clearBaseGameGimmicks() {
        gimmickSprMo2 = null;
        gimmickSprMo3 = null;
        gimmickSumMo1 = null;
        gimmickSumMo2 = null;
        gimmickSumMo3 = null;
        gimmickAutMo1 = null;
        gimmickAutMo2 = null;
        gimmickAutMo3 = null;
        gimmickWinMo1 = null;
        gimmickWinMo2 = null;
        gimmickWinMo3 = null;
    }

    @Override
    public String getName() {
        return "SEASONS";
    }

    @Override
    public void playerInit(GameEngine engine, int playerID) {
        owner = engine.owner;
        receiver = engine.owner.receiver;

        customGraphics = new CustomResourceHolder();
        rendererExtension = new RendererExtension(customGraphics);
        drawing = new PrimitiveDrawingHook(customGraphics);
        statesAtTimes = new TreeMap<>();
        textEmitter.clear();
        rewindBlocks.clear();

        // For Zero Celsius and Absolute Zero
        customGraphics.loadImage("res/graphics/iceblock.png", "iceblock");

        setupBackgrounds(engine);

        levelUpFlag = false;
        currentSeason = Season.SPRING;
        nextSectionLevel = 0;
        badges = new Badges();
        lockedPieces = 0;
        customState = CustomState.PROFILE;
        rollLevelReached = -1;
        fieldPurifyQueued = false;
        descriptionToDraw = null;
        canPushdown = true;

        // Clear all gimmicks.
        clearBaseGameGimmicks();

        vortex = new BlockVortex();

        if (ruleOptCopy == null) {
            ruleOptCopy = new RuleOptions(engine.ruleopt);
        }

        engine.framecolor = Season.defaultMenuFrameColour();

        if (playerProperties == null) {
            playerProperties = new ProfileProperties(HEADER_COLOUR);
            showPlayerStats = false;
        }

        settings = new SeasonsSettings(CURRENT_VERSION, playerProperties);

        if (!owner.replayMode) {
            settings.loadSetting(owner.modeConfig, false);
            settings.loadRanking(owner.modeConfig, engine.ruleopt.strRuleName);

            if (playerProperties.isLoggedIn()) {
                settings.loadSettingPlayer(playerProperties);
                settings.loadRankingPlayer(playerProperties, engine.ruleopt.strRuleName);
            }
        } else {
            settings.loadSetting(owner.replayProp, true);
        }

        lastBackground = 0;
        currentBackground = 0;
        fadeProgress = 300;
    }

    // 0 1 2 3 4 (4.5) 5 6 7 8 9
    // 0 1 2 3 (4) 5 6 7 8
    private void addRewindBlockFromField(GameEngine engine, int playerID, int x, int y) {
        final int baseX = 4 + receiver.getFieldDisplayPositionX(engine, playerID);
        final int baseY = 52 + receiver.getFieldDisplayPositionY(engine, playerID);

        final Block blk = engine.field.getBlock(x, y);
        if (blk == null) return;

        final double midpoint = ((engine.field.getWidth()) / 2.0) - 0.5;
        final double difference = x - midpoint;

        final double yvr = (rewindBlockRandom.nextDouble() - 0.5) * 2 * 0.125;

        rewindBlocks.add(
            new RewindBlock(
                60, 0,
                new DoubleVector(baseX + (x * 16) + 8, baseY + (y * 16) + 8, false),
                new DoubleVector((difference * 4.0) / 30.0, -8.0 - ((engine.field.getHeightWithoutHurryupFloor() - y) * 0.25) + yvr, false),
                new DoubleVector((difference * -4.0) / (30.0 * 120.0), 9.80665 / 15.0, false),
                blk
            )
        );
    }

    private void addRewindBlocks(GameEngine engine, int playerID, Field olderField) {
        if (engine.field == null) return;

        if (FieldManipulation.getNumberOfBlocks(engine.field) > FieldManipulation.getNumberOfBlocks(olderField)) {
            for (int y = 0; y < engine.field.getHeightWithoutHurryupFloor(); ++y) {
                for (int x = 0; x < engine.field.getWidth(); ++x) {
                    if (engine.field.getBlockColor(x, y) != olderField.getBlockColor(x, y) && !engine.field.getBlockEmpty(x, y)) {
                        addRewindBlockFromField(engine, playerID, x, y);
                    }
                }
            }
        }
    }

    private void performRewindSteps(GameEngine engine, int playerID, int rewindTime, int rewindStartDelay, int oldestStateSeconds) {
        if (engine.statc[0] >= rewindStartDelay) {
            final NavigableSet<Integer> keys = statesAtTimes.navigableKeySet();
            final int maxTime = keys.last();
            final int minTime = Objects.requireNonNull(keys.ceiling(engine.statistics.time - (oldestStateSeconds * 60)));

            final List<Integer> validKeys = keys
                .stream()
                .filter(t -> t >= minTime && t <= maxTime)
                .collect(Collectors.toList());

            final int interpTime = (int) Math.ceil(Interpolation.tanStep(
                validKeys.size() - 1d, 0d,
                MathHelper.clamp(
                    (engine.statc[0] - (double) rewindStartDelay) / (rewindTime - (double) rewindStartDelay),
                    0.0, 1.0
                )
            ));

            final NextAndFieldState state = statesAtTimes.get(validKeys.get(interpTime));

            if (!FieldManipulation.fieldEquals(engine.field, state.field)
                || engine.nextPieceCount != state.nextPosition) {
                engine.playSE("step");

                addRewindBlocks(engine, playerID, state.field);
                engine.field = new Field(state.field);

                for (int i = state.nextPosition; i < engine.nextPieceCount + engine.ruleopt.nextDisplay; ++i) {
                    engine.nextPieceArrayObject[i] = HasCustomOnMove.initialisePiece(engine, engine.nextPieceArrayID[i]);
                }

                engine.nextPieceCount = state.nextPosition;

                if (state.holdPiece != null) {
                    engine.holdPieceObject = HasCustomOnMove.initialisePiece(engine, state.holdPiece.id);
                } else {
                    engine.holdPieceObject = null;
                }
            }
        }
    }

    @Override
    public boolean onCustom(GameEngine engine, int playerID) {
        if (engine.gameStarted) {
            switch (customState) {
                case FREEFALL: {
                    // Arrived here from ARE, this is where the freefall happens.
                    if (engine.statc[0] > 0 && engine.statc[0] % 3 == 0) {
                        final Field oldField = new Field(engine.field);
                        final boolean landed = FieldManipulation.freeFallStep(engine.field);

                        if (!FieldManipulation.fieldEquals(engine.field, oldField)) {
                            hasLandedBefore = landed;
                            if (hasLandedBefore) engine.playSE("linefall");
                        } else {
                            engine.lineClearing = engine.field.checkLineNoFlag();

                            if (engine.lineClearing == 0) {
                                engine.stat = GameEngine.STAT_ARE;
                                engine.resetStatc();
                            } else {
                                engine.stat = GameEngine.STAT_LINECLEAR;
                                engine.resetStatc();

                                engine.tspin = false;
                                engine.tspinmini = false;
                                engine.tspinez = false;

                                engine.statLineClear();
                            }
                        }
                    }
                }
                    break;
                case REWIND: {
                    engine.timerActive = false;

                    if (engine.statc[0] == 0) {
                        // Not guaranteed to run the GC, but it's nice to run it here when there's no player input expected.
                        statesAtTimes.keySet().removeIf(k -> k < engine.statistics.time - (60 * 600));
                        System.gc();
                    }

                    performRewindSteps(engine, playerID, REWIND_TIME, 30, 60);

                    if (engine.statc[0] >= REWIND_TIME) {
                        engine.speed.lineDelay = 60;

                        if (gimmickAutMo3 != null) {
                            engine.blockShowOutlineOnly = true;
                        }

                        if (gimmickWinMo1 != null && engine.statistics.level >= LEVELS_NOV) {
                            for (int i = engine.nextPieceCount; i <= engine.nextPieceCount + engine.ruleopt.nextDisplay; ++i) {
                                for (Block blk : engine.nextPieceArrayObject[i].block) {
                                    blk.color = Block.BLOCK_COLOR_GRAY;
                                    blk.bonusValue |= Gimmicks.Whiteout.SNOW_IDENTIFIER;
                                }
                            }

                            if (engine.holdPieceObject != null) {
                                for (Block blk : engine.holdPieceObject.block) {
                                    blk.color = Block.BLOCK_COLOR_GRAY;
                                    blk.bonusValue |= Gimmicks.Whiteout.SNOW_IDENTIFIER;
                                }
                            }
                        }

                        clearAddedSummerIPieces(engine);

                        engine.stat = GameEngine.STAT_LINECLEAR;
                        engine.resetStatc();
                    }
                }
                    break;
                case FINAL_REWIND: {
                    // TODO: Add a system to skip the final rewind if the player has seen it whole before.
                    engine.timerActive = false;

                    if (engine.statc[0] == 0) {
                        // Not guaranteed to run the GC, but it's nice to run it here when there's no player input expected.
                        statesAtTimes.keySet().removeIf(k -> k < engine.statistics.time - (60 * 600));
                        System.gc();
                    }

                    performRewindSteps(engine, playerID, FINAL_REWIND_TIME, 180, 600);

                    if (engine.statc[0] >= 180) {
                        engine.statistics.level = (int) Math.ceil(Interpolation.tanStep(
                            MAX_LEVEL, 0d,
                            MathHelper.clamp(
                                (engine.statc[0] - 180d) / (FINAL_REWIND_TIME - 180d),
                                0.0, 1.0
                            )
                        ));
                    }

                    if (engine.statc[0] >= FINAL_REWIND_TIME) {
                        clearAddedSummerIPieces(engine);

                        engine.ending = 2;
                        naturalLevelIncrement = INCREMENT_IN_ROLL;
                        levelUpFlag = false;
                        currentSeason = Season.SPRING;

                        customState = CustomState.FINAL_REWIND_RECOVERY;
                        engine.resetStatc();
                    }
                }
                    break;
                case FINAL_REWIND_RECOVERY: {
                    // Do another READY..GO! here.
                    if (engine.statc[0] == engine.readyStart) {
                        engine.playSE("ready");
                        purifyFieldAndNexts(engine);
                    }

                    if (engine.statc[0] == engine.goStart) {
                        engine.playSE("go");
                    }

                    if (engine.statc[0] >= engine.goEnd) {
                        engine.speed.lineDelay = 0;
                        engine.speed.areLine = 0;

                        engine.stat = GameEngine.STAT_LINECLEAR;
                        engine.resetStatc();
                    }
                }
                    break;
                default:
                    break;
            }

            engine.statc[0]++;
            return true;
        } else {
            showPlayerStats = false;
            engine.isInGame = true;

            final boolean s = playerProperties.loginScreen.updateScreen(engine, playerID);
            if (playerProperties.isLoggedIn()) {
                settings.loadRankingPlayer(playerProperties, engine.ruleopt.strRuleName);
                settings.loadSettingPlayer(playerProperties);
            }

            if (engine.stat == GameEngine.STAT_SETTING) engine.isInGame = false;

            return s;
        }
    }

    private void clearAddedSummerIPieces(GameEngine engine) {
        for (int i = engine.nextPieceArraySize - 1; i >= engine.nextPieceCount; --i) {
            if (engine.nextPieceArrayObject[i].block[0].item == -1) {
                HasCustomOnMove.removeFromNext(engine, i);
            }
        }
    }

    private static final GameTextUtilities.TextBlock ENDING_START_PASSAGE = GameTextUtilities.TextBlock.of(
        GameTextUtilities.TextJustification.CENTRE,
        GameTextUtilities.Text.custom("THE ", EventReceiver.COLOR_WHITE, (1f / 2f)),
        GameTextUtilities.Text.custom("WORDS", EventReceiver.COLOR_BLUE, 0.75f),
        GameTextUtilities.Text.custom(" ARE", EventReceiver.COLOR_WHITE, (1f / 2f)),
        GameTextUtilities.Text.newLine(),
        GameTextUtilities.Text.custom("DYING", EventReceiver.COLOR_RED, 0.75f),
        GameTextUtilities.Text.custom(" IN THE NIGHT", EventReceiver.COLOR_WHITE, (1f / 2f)),
        GameTextUtilities.Text.newLine(),
        GameTextUtilities.Text.custom("NO ", EventReceiver.COLOR_WHITE, (1f / 2f)),
        GameTextUtilities.Text.custom("WINTER", EventReceiver.COLOR_CYAN, 0.75f),
        GameTextUtilities.Text.custom(" LASTS FOREVER", EventReceiver.COLOR_WHITE, (1f / 2f)),
        GameTextUtilities.Text.newLine(),
        GameTextUtilities.Text.custom("THE ", EventReceiver.COLOR_WHITE, (1f / 2f)),
        GameTextUtilities.Text.custom("SEASONS", EventReceiver.COLOR_GREEN, 0.75f),
        GameTextUtilities.Text.custom(" PASS AND", EventReceiver.COLOR_WHITE, (1f / 2f)),
        GameTextUtilities.Text.newLine(),
        GameTextUtilities.Text.custom("SUNLIGHT", EventReceiver.COLOR_YELLOW, 0.75f),
        GameTextUtilities.Text.custom(" WILL SHINE", EventReceiver.COLOR_WHITE, (1f / 2f)),
        GameTextUtilities.Text.newLine(),
        GameTextUtilities.Text.custom("ON MY LIFE AGAIN", EventReceiver.COLOR_WHITE, (1f / 2f)),
        GameTextUtilities.Text.newLine(),
        GameTextUtilities.Text.custom("SO LET THE ", EventReceiver.COLOR_WHITE, (1f / 2f)),
        GameTextUtilities.Text.custom("PAST", EventReceiver.COLOR_PINK, 0.75f),
        GameTextUtilities.Text.newLine(),
        GameTextUtilities.Text.custom("NOW ", EventReceiver.COLOR_WHITE, (1f / 2f)),
        GameTextUtilities.Text.custom("BURN", EventReceiver.COLOR_ORANGE, 1f),
        GameTextUtilities.Text.newLine(),
        GameTextUtilities.Text.custom("DOWN IN ", EventReceiver.COLOR_WHITE, (1f / 2f)),
        GameTextUtilities.Text.custom("FLAMES", EventReceiver.COLOR_ORANGE, 1f)
    );

    @Override
    public void renderCustom(GameEngine engine, int playerID) {
        if (engine.field != null) {
            int baseX = (8 * engine.field.getWidth()) + 4 + receiver.getFieldDisplayPositionX(engine, playerID);
            int baseY = (8 * engine.field.getHeight()) + 52 + receiver.getFieldDisplayPositionY(engine, playerID);

            if (engine.gameStarted && customState == CustomState.FREEFALL) {
                // region Freefall
                int alpha = 255;
                if (engine.statc[0] >= 15) {
                    alpha = Interpolation.lerp(255, 0, (engine.statc[0] - 15d) / 30d);
                }

                GameTextUtilities.drawAlignedTextBlock(
                    engine,
                    baseX, baseY,
                    false,
                    GameTextUtilities.TextBlock.of(
                        GameTextUtilities.TextJustification.LEFT,
                        GameTextUtilities.Text.ofMixColorBig("FREE", (engine.statc[0] >>> 1) % 2 == 0 ? EventReceiver.COLOR_WHITE : EventReceiver.COLOR_YELLOW, 255, 255, 255, alpha),
                        GameTextUtilities.Text.newLine(),
                        GameTextUtilities.Text.ofMixColorBig("FALL", (engine.statc[0] >>> 1) % 2 == 0 ? EventReceiver.COLOR_WHITE : EventReceiver.COLOR_YELLOW, 255, 255, 255, alpha)
                    ),
                    ObjectAlignment.MIDDLE_MIDDLE
                );
                // endregion Freefall
            } else if (engine.gameStarted && customState == CustomState.REWIND) {
                // region Rewind
                int alpha = 255;
                if (engine.statc[0] >= 180) {
                    alpha = Interpolation.lerp(255, 0, (engine.statc[0] - 180d) / 120d);
                }

                GameTextUtilities.drawAlignedTextBlock(
                    engine,
                    baseX, baseY,
                    false,
                    GameTextUtilities.TextBlock.of(
                        GameTextUtilities.TextJustification.LEFT,
                        GameTextUtilities.Text.of(" "),
                        GameTextUtilities.Text.ofMixColorBig(
                            "TIME", (engine.statc[0] >>> 1) % 2 == 0 ? EventReceiver.COLOR_YELLOW : EventReceiver.COLOR_ORANGE, 255, 255, 255, alpha
                        ),
                        GameTextUtilities.Text.newLine(),
                        GameTextUtilities.Text.ofMixColorBig(
                            "WARP", (engine.statc[0] >>> 1) % 2 == 0 ? EventReceiver.COLOR_YELLOW : EventReceiver.COLOR_ORANGE, 255, 255, 255, alpha
                        )
                    ),
                    ObjectAlignment.MIDDLE_MIDDLE
                );
                // endregion Rewind
            } else if (engine.gameStarted && customState == CustomState.FINAL_REWIND) {
                // region Final Rewind
                if (engine.statc[0] < 600) {
                    int alpha = 255;
                    if (engine.statc[0] >= 180) {
                        alpha = Interpolation.lerp(255, 0, (engine.statc[0] - 180d) / 420d);
                    }

                    GameTextUtilities.drawAlignedTextBlock(
                        engine,
                        baseX, baseY,
                        false,
                        GameTextUtilities.TextBlock.of(
                            GameTextUtilities.TextJustification.LEFT,
                            GameTextUtilities.Text.of(" "),
                            GameTextUtilities.Text.ofMixColorBig(
                                "TIME", (engine.statc[0] >>> 1) % 2 == 0 ? EventReceiver.COLOR_YELLOW : EventReceiver.COLOR_ORANGE, 255, 255, 255, alpha
                            ),
                            GameTextUtilities.Text.newLine(),
                            GameTextUtilities.Text.ofMixColorBig(
                                "WARP", (engine.statc[0] >>> 1) % 2 == 0 ? EventReceiver.COLOR_YELLOW : EventReceiver.COLOR_ORANGE, 255, 255, 255, alpha
                            ),
                            GameTextUtilities.Text.ofMixColor(
                                "?", (engine.statc[0] >>> 1) % 2 == 0 ? EventReceiver.COLOR_YELLOW : EventReceiver.COLOR_ORANGE, 255, 255, 255, alpha
                            )
                        ),
                        ObjectAlignment.MIDDLE_MIDDLE
                    );
                }

                final int basisTime = FINAL_REWIND_TIME - (60 * 21);

                int maxY = baseY - 64;
                if (engine.statc[0] >= basisTime) maxY += 12;
                if (engine.statc[0] >= (basisTime + ((60 * 1) + 30))) maxY += 12;
                if (engine.statc[0] >= (basisTime + ((60 * 4) + 48))) maxY += 12;
                if (engine.statc[0] >= (basisTime + ((60 * 9) + 48))) maxY += 12;
                if (engine.statc[0] >= (basisTime + ((60 * 12) + 0))) maxY += 12;
                if (engine.statc[0] >= (basisTime + ((60 * 14) + 24))) maxY += 8;
                if (engine.statc[0] >= (basisTime + ((60 * 16) + 48))) maxY += 12;
                if (engine.statc[0] >= (basisTime + ((60 * 18) + 24))) maxY += 16;
                if (engine.statc[0] >= (basisTime + ((60 * 19) + 12))) maxY = 480;

                GameTextUtilities.drawAlignedBoundedTextBlock(
                    engine,
                    baseX, baseY - 64,
                    0, 0, 640, maxY,
                    false,
                    ENDING_START_PASSAGE,
                    ObjectAlignment.TOP_MIDDLE
                );
                // endregion Final Rewind
            } else if (engine.gameStarted && customState == CustomState.FINAL_REWIND_RECOVERY) {
                receiver.renderReady(engine, playerID);
            }
        }
    }

    @Override
    public boolean onReady(GameEngine engine, int playerID) {
        if (engine.statc[0] == 0) {
            bvr = new Random(engine.randSeed);
            rewindBlockRandom = new Random(engine.randSeed);

            rollStarted = false;

            // Setup active ability stuff.
            currentEnergy = 0;
            currentAbilityTimer = 0;

            // Setup freefall stuff
            hasLandedBefore = false;
            queuedFreefall = false;

            // Summer passive locked pieces counter.
            lockedPieces = 0;
            engine.ruleopt = new RuleOptions(ruleOptCopy);

            if (settings.perk == SeasonPerk.SUMMER_PASSIVE) {
                engine.ruleopt.nextDisplay += 3;
            }
        }

        return false;
    }

    @Override
    public boolean onSetting(GameEngine engine, int playerID) {
        if (!engine.owner.replayMode) {
            // Configuration changes
            int change = updateCursor(engine, 2);

            if (change != 0) {
                engine.playSE("change");

                switch (engine.statc[2]) {
                    case 0:
                        int selectedPerk = settings.perk.ordinal() + change;

                        if (!settings.hasCompletedGame) {
                            if (selectedPerk < 1) selectedPerk = SeasonPerk.values().length - 1;
                            else if (selectedPerk >= SeasonPerk.values().length) selectedPerk = 1;
                        } else {
                            if (selectedPerk < 0) selectedPerk = SeasonPerk.values().length - 1;
                            else if (selectedPerk >= SeasonPerk.values().length) selectedPerk = 0;
                        }

                        settings.perk = SeasonPerk.values()[selectedPerk];
                        break;
                    case 1:
                        settings.fullGhost = !settings.fullGhost;
                        break;
                    case 2:
                        settings.spinType += change;
                        if (settings.spinType < GameEngine.SPINTYPE_4POINT) settings.spinType = 2;
                        else if (settings.spinType > 2) settings.spinType = GameEngine.SPINTYPE_4POINT;
                        break;
                    default:
                        break;
                }
            }

            if (engine.ctrl.isPush(Controller.BUTTON_A) && (engine.statc[3] >= 5)) {
                engine.playSE("decide");

                if (playerProperties.isLoggedIn()) {
                    settings.saveSettingPlayer(playerProperties);
                    playerProperties.saveProfileConfig();
                } else {
                    settings.saveSetting(owner.modeConfig, false);
                    receiver.saveModeConfig(owner.modeConfig);
                }

                return false;
            }

            if (engine.ctrl.isPush(Controller.BUTTON_B)) {
                engine.quitflag = true;
                ruleOptCopy = null;

                playerProperties = new ProfileProperties(HEADER_COLOUR);
            }

            if (engine.ctrl.isPush(Controller.BUTTON_E) && engine.ai == null) {
                playerProperties = new ProfileProperties(HEADER_COLOUR);
                engine.playSE("decide");

                engine.stat = GameEngine.STAT_CUSTOM;
                engine.resetStatc();
                return true;
            }

            engine.statc[3]++;
        } else {
            engine.statc[3]++;
            engine.statc[2] = -1;

            if (engine.statc[3] >= 120) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void renderSetting(GameEngine engine, int playerID) {
        String perkString = settings.perk.name();
        if (perkString.contains("_")) {
            final String[] split = perkString.split("_");
            perkString = String.format("%s (%s)", split[0], split[1].charAt(0));
        }

        String spinString = "DISABLED";
        if (settings.spinType == GameEngine.SPINTYPE_4POINT) spinString = "4-POINT";
        if (settings.spinType == GameEngine.SPINTYPE_IMMOBILE) spinString = "IMMOBILE";

        drawMenu(engine, playerID, receiver, 0, EventReceiver.COLOR_YELLOW, 0,
            "PERK", perkString
        );
        drawMenu(engine, playerID, receiver, 2, EventReceiver.COLOR_BLUE, 1,
            "FULL GHOST", GeneralUtil.getONorOFF(settings.fullGhost)
        );
        drawMenu(engine, playerID, receiver, 4, EventReceiver.COLOR_GREEN, 2,
            "SPIN TYPE", spinString
        );
    }

    private void setSpeed(GameEngine engine) {
        // TODO: Populate this further later
        if (gimmickSumMo3 != null) {
            engine.speed = gimmickSumMo3.getSpeed(engine, badges, getGimmickPerkBoost());
        } else {
            engine.speed = SPEED_TABLE.apply(engine.statistics.level);
        }

        final boolean instantG = engine.speed.gravity < 0 || ((engine.speed.gravity / engine.speed.denominator) >= engine.field.getHeight());
        if (settings.perk == SeasonPerk.WINTER_PASSIVE && instantG) engine.speed.lockDelay += 6;
        else if (settings.perk == SeasonPerk.WINTER_PASSIVE) engine.speed.gravity = Math.max(1, engine.speed.gravity >>> 1);

        if (gimmickAutMo1 != null) {
            // VERY LOW ARE:
            engine.speed.are = 6;
            engine.speed.areLine = 6;

            engine.speed.gravity = 0;
            engine.speed.denominator = 1;
        }
    }

    @Override
    public void startGame(GameEngine engine, int playerID) {
        engine.statistics.level = 0;

        nextSectionLevel = NEXT_SECTION_LEVELS.apply(engine.statistics.level);
        setNewBackground(BACKGROUND_TABLE.apply(engine.statistics.level));

        owner.backgroundStatus.bg = -1;
        owner.backgroundStatus.fadebg = -1;

        engine.ghost = true;

        engine.big = false;
        engine.bigmove = true;
        engine.bighalf = true;

        engine.tspinEnable = settings.spinType != 2;
        engine.useAllSpinBonus = true;
        engine.tspinAllowKick = true;
        engine.tspinEnableEZ = true;
        engine.spinCheckType = settings.spinType == 2 ? 0 : settings.spinType;
        engine.tspinminiType = GameEngine.TSPINMINI_TYPE_ROTATECHECK;

        engine.b2bEnable = true;

        engine.staffrollEnableStatistics = false;
        engine.rainbowAnimate = true;

        naturalLevelIncrement = 1;

        setSpeed(engine);
        owner.bgmStatus.bgm = BGM_TABLE.apply(engine.statistics.level);
    }

    @Override
    public void inPieceSpawn(GameEngine engine, int playerID) {
        // 出現時の処理
        if (engine.statc[0] == 0) {
            // Store current field state.
            if (engine.statc[1] == 0 && engine.ending == 0) {
                if (gimmickSumMo2 != null) gimmickSumMo2.replaceQueue(engine);

                statesAtTimes.put(engine.statistics.time, new NextAndFieldState(engine));
            }

            if ((engine.statc[1] == 0) && (!engine.initialHoldFlag)) {
                // 通常出現
                engine.nowPieceObject = HasCustomOnMove.getNextObjectCopy(engine, engine.nextPieceCount);
                engine.nextPieceCount++;
                if (engine.nextPieceCount < 0) engine.nextPieceCount = 0;
                engine.holdDisable = false;
            } else {
                // ホールド出現
                if (engine.initialHoldFlag) {
                    // 先行ホールド
                    if (engine.holdPieceObject == null) {
                        // 1回目
                        engine.holdPieceObject = HasCustomOnMove.getNextObjectCopy(engine, engine.nextPieceCount);
                        engine.holdPieceObject.applyOffsetArray(engine.ruleopt.pieceOffsetX[engine.holdPieceObject.id], engine.ruleopt.pieceOffsetY[engine.holdPieceObject.id]);
                        engine.nextPieceCount++;
                        if (engine.nextPieceCount < 0) engine.nextPieceCount = 0;

                        if (engine.bone)
                            engine.getNextObject(engine.nextPieceCount + engine.ruleopt.nextDisplay - 1).setAttribute(Block.BLOCK_ATTRIBUTE_BONE, true);

                        engine.nowPieceObject = HasCustomOnMove.getNextObjectCopy(engine, engine.nextPieceCount);
                        engine.nextPieceCount++;
                        if (engine.nextPieceCount < 0) engine.nextPieceCount = 0;
                    } else {
                        // 2回目以降
                        Piece pieceTemp = engine.holdPieceObject;
                        engine.holdPieceObject = HasCustomOnMove.getNextObjectCopy(engine, engine.nextPieceCount);
                        engine.holdPieceObject.applyOffsetArray(engine.ruleopt.pieceOffsetX[engine.holdPieceObject.id], engine.ruleopt.pieceOffsetY[engine.holdPieceObject.id]);
                        engine.nowPieceObject = pieceTemp;
                        engine.nextPieceCount++;
                        if (engine.nextPieceCount < 0) engine.nextPieceCount = 0;
                    }
                } else {
                    // 通常ホールド
                    if (engine.holdPieceObject == null) {
                        // 1回目
                        engine.nowPieceObject.big = false;
                        engine.holdPieceObject = engine.nowPieceObject;
                        engine.nowPieceObject = HasCustomOnMove.getNextObjectCopy(engine, engine.nextPieceCount);
                        engine.nextPieceCount++;
                        if (engine.nextPieceCount < 0) engine.nextPieceCount = 0;
                    } else {
                        // 2回目以降
                        engine.nowPieceObject.big = false;
                        Piece pieceTemp = engine.holdPieceObject;
                        engine.holdPieceObject = engine.nowPieceObject;
                        engine.nowPieceObject = pieceTemp;
                    }
                }

                // Directionを戻す
                if (engine.ruleopt.holdResetDirection && (engine.ruleopt.pieceDefaultDirection[engine.holdPieceObject.id] < Piece.DIRECTION_COUNT)) {
                    final Optional<Block> blk = Arrays.stream(engine.holdPieceObject.block).filter(b -> b.bonusValue > 0 && b.bonusValue < 16).findAny();
                    engine.holdPieceObject.direction = blk.map(b -> b.bonusValue >>> 1)
                        .orElseGet(() -> engine.ruleopt.pieceDefaultDirection[engine.holdPieceObject.id]);

                    engine.holdPieceObject.updateConnectData();
                } else if (engine.ruleopt.holdResetDirection) {
                    final Optional<Block> blk = Arrays.stream(engine.holdPieceObject.block).filter(b -> b.bonusValue > 0 && b.bonusValue < 16).findAny();
                    blk.ifPresent(b -> engine.holdPieceObject.direction = b.bonusValue >>> 1);
                }

                // 使用した count+1
                engine.holdUsedCount++;
                engine.statistics.totalHoldUsed++;

                // ホールド無効化
                engine.initialHoldFlag = false;
                engine.holdDisable = true;
            }

            // For connectivity.
            if (engine.nowPieceObject != null) {
                for (Block blk : engine.nowPieceObject.block) blk.pieceNum = lockedPieces;
            }

            engine.playSE("piece" + HasCustomOnMove.getNextObject(engine, engine.nextPieceCount).id);

            if (!engine.nowPieceObject.offsetApplied)
                engine.nowPieceObject.applyOffsetArray(engine.ruleopt.pieceOffsetX[engine.nowPieceObject.id], engine.ruleopt.pieceOffsetY[engine.nowPieceObject.id]);

            engine.nowPieceObject.big = engine.big;

            // 出現位置 (横）
            engine.nowPieceX = engine.getSpawnPosX(engine.field, engine.nowPieceObject);

            // 出現位置 (縦）
            engine.nowPieceY = engine.getSpawnPosY(engine.nowPieceObject);

            engine.nowPieceBottomY = engine.nowPieceObject.getBottom(engine.nowPieceX, engine.nowPieceY, engine.field);
            engine.nowPieceColorOverride = -1;

            if (engine.itemRollRollEnable) engine.nowPieceColorOverride = Block.BLOCK_COLOR_GRAY;

            // 先行rotation
            if (engine.versionMajor < 7.5f) engine.initialRotate(); //XXX: Weird active time IRS

            if ((engine.speed.gravity > engine.speed.denominator) && (engine.speed.denominator > 0))
                engine.gcount = engine.speed.gravity % engine.speed.denominator;
            else
                engine.gcount = 0;

            engine.lockDelayNow = 0;
            engine.dasSpeedCount = engine.getDASDelay();
            engine.dasRepeat = false;
            engine.dasInstant = false;
            engine.extendedMoveCount = 0;
            engine.extendedRotateCount = 0;
            engine.softdropFall = 0;
            engine.harddropFall = 0;
            engine.manualLock = false;
            engine.nowPieceMoveCount = 0;
            engine.nowPieceRotateCount = 0;
            engine.nowPieceRotateFailCount = 0;
            engine.nowWallkickCount = 0;
            engine.nowUpwardWallkickCount = 0;
            engine.lineClearing = 0;
            engine.lastmove = GameEngine.LASTMOVE_NONE;
            engine.kickused = false;
            engine.tspin = false;
            engine.tspinmini = false;
            engine.tspinez = false;

            // Don't clear bone blocks from queue if the summer gimmick is active.
            if (gimmickSumMo1 == null) {
                engine.getNextObject(engine.nextPieceCount + engine.ruleopt.nextDisplay - 1).setAttribute(Block.BLOCK_ATTRIBUTE_BONE, engine.bone);
            }

            if (gimmickWinMo3 != null) {
                for (Block blk : engine.nowPieceObject.block) blk.bonusValue |= Gimmicks.ZeroCelsius.ZERO_IDENTIFIER;
            }

            if (engine.ending == 0) engine.timerActive = true;

            if ((engine.ai != null) && (!engine.owner.replayMode || engine.owner.replayRerecord))
                engine.ai.newPiece(engine, playerID);
        }
    }

    @Override
    public boolean inLockDelayProcessing(GameEngine engine, int playerID, PlayerMoveResult result, boolean updown) {
        // 接地と固定
        if ((engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY + 1, engine.field)) &&
            ((engine.statc[0] > 0) || (engine.ruleopt.moveFirstFrame))) {
            if ((engine.lockDelayNow == 0) && (engine.getLockDelay() > 0))
                engine.playSE("step");

            if (engine.lockDelayNow < engine.getLockDelay())
                engine.lockDelayNow++;

            // Prevents lock delay > 98f.
//            if ((engine.getLockDelay() >= 99) && (engine.lockDelayNow > 98))
//                engine.lockDelayNow = 98;

            if (engine.lockDelayNow < engine.getLockDelay()) {
                if (engine.lockDelayNow >= engine.getLockDelay() - 1)
                    engine.nowPieceObject.setDarkness(0.5f);
                else
                    engine.nowPieceObject.setDarkness((engine.lockDelayNow * 7f / engine.getLockDelay()) * 0.05f);
            }

            if (engine.getLockDelay() != 0)
                engine.gcount = engine.speed.gravity;

            // trueになると即固定
            boolean instantlock = false;

            // Hard drop固定
            if ((engine.ctrl.isPress(engine.getUp())) &&
                (!engine.harddropContinuousUse) &&
                (engine.ruleopt.harddropEnable) &&
                ((engine.isDiagonalMoveEnabled()) || (!result.sidemoveflag)) &&
                ((engine.ruleopt.moveUpAndDown) || (!updown)) &&
                (engine.ruleopt.harddropLock)) {
                engine.harddropContinuousUse = true;
                engine.manualLock = true;
                instantlock = true;
            }

            // Soft drop固定
            if ((engine.ctrl.isPress(engine.getDown())) &&
                (!engine.softdropContinuousUse) &&
                (engine.ruleopt.softdropEnable) &&
                ((engine.isDiagonalMoveEnabled()) || (!result.sidemoveflag)) &&
                ((engine.ruleopt.moveUpAndDown) || (!updown)) &&
                (engine.ruleopt.softdropLock)) {
                engine.softdropContinuousUse = true;
                engine.manualLock = true;
                instantlock = true;
            }

            // 接地状態でソフドドロップ固定
            if ((engine.ctrl.isPush(engine.getDown())) &&
                (engine.ruleopt.softdropEnable) &&
                ((engine.isDiagonalMoveEnabled()) || (!result.sidemoveflag)) &&
                ((engine.ruleopt.moveUpAndDown) || (!updown)) &&
                (engine.ruleopt.softdropSurfaceLock)) {
                engine.softdropContinuousUse = true;
                engine.manualLock = true;
                instantlock = true;
            }

            if ((engine.manualLock) && (engine.ruleopt.shiftLockEnable)) {
                // bit 1 and 2 are button_up and button_down currently
                engine.shiftLock = engine.ctrl.getButtonBit() & 3;
            }

            // 移動＆rotationcount制限超過
            if ((engine.ruleopt.lockresetLimitOver == RuleOptions.LOCKRESET_LIMIT_OVER_INSTANT) && (engine.isMoveCountExceed() || engine.isRotateCountExceed())) {
                instantlock = true;
            }

            // 接地即固定
            if ((engine.getLockDelay() == 0) && ((engine.gcount >= engine.speed.denominator) || (engine.speed.gravity < 0))) {
                instantlock = true;
            }

            // Count a piece as locked.
            if (instantlock) {
                ++lockedPieces;
            }

            // Add a new I-piece into the next queue if using summer passive perk.
            if (lockedPieces % 50 == 0 && settings.perk == SeasonPerk.SUMMER_PASSIVE) {
                HasCustomOnMove.insertIntoNexts(engine, engine.nextPieceCount + engine.ruleopt.nextDisplay, Piece.PIECE_I);
                for (Block blk : HasCustomOnMove.getNextObject(engine, engine.nextPieceCount + engine.ruleopt.nextDisplay).block) {
                    blk.item = -1;
                }
            }

            return inPostLockProcessing(engine, playerID, instantlock);
        }
        return false;
    }

    @Override
    public boolean onMove(GameEngine engine, int playerID) {
        if (engine.statc[0] == 0 && !engine.holdDisable) {
            if (gimmickSprMo2 != null) gimmickSprMo2.update(engine);
            if (gimmickSprMo3 != null) gimmickSprMo3.attemptPlacement(engine);
        }

        if ((engine.statc[0] == 0) && (!engine.holdDisable) && (!levelUpFlag)) {
            if (engine.statistics.level < nextSectionLevel - 1) {
                engine.statistics.level = Math.min(engine.statistics.level + naturalLevelIncrement, nextSectionLevel - 1);

                if (engine.statistics.level == nextSectionLevel - 1)
                    engine.playSE("levelstop");
            }

            levelUp(engine);
        }

        if (engine.gameActive && engine.ending == 2) {
            rollStarted = true;
        }

        if (engine.gameActive && !engine.timerActive && !rollStarted) {
            engine.timerActive = true;
        }

        if (engine.statc[0] > 0) {
            levelUpFlag = false;
        }

        if (gimmickAutMo1 != null && engine.statc[0] > gimmickAutMo1.getFallDelay(badges, getGimmickPerkBoost())) {
            if (settings.perk != SeasonPerk.WINTER_ACTIVE || currentAbilityTimer <= 0) {
                engine.speed.gravity = settings.perk == SeasonPerk.WINTER_PASSIVE ? 4 : 5;
            } else {
                engine.speed.gravity = 0;
            }
        } else if (gimmickAutMo1 != null) {
            engine.speed.gravity = 0;
        }

        return inOnMove(engine, playerID);
    }

    @Override
    public boolean onARE(GameEngine engine, int playerID) {
        if (engine.statc[0] == 0 && queuedFreefall && engine.gameStarted) {
            queuedFreefall = false;

            customState = CustomState.FREEFALL;
            engine.stat = GameEngine.STAT_CUSTOM;
            engine.resetStatc();

            return true;
        }

        if (engine.statc[0] == 0 && !engine.holdDisable && !levelUpFlag) {
            if (gimmickSprMo3 != null) gimmickSprMo3.explode(engine);
        }

        if (((engine.statc[0] >= engine.statc[1] - 1) && (!levelUpFlag))) {
            if (engine.statistics.level < nextSectionLevel - 1) {
                engine.statistics.level = Math.min(engine.statistics.level + naturalLevelIncrement, nextSectionLevel - 1);

                if (engine.statistics.level == nextSectionLevel - 1)
                    engine.playSE("levelstop");
            }

            levelUp(engine);
            levelUpFlag = true;
        }

        return false;
    }

    @Override
    public boolean onGameOver(GameEngine engine, int playerID) {
        // Set the level back to max level.
        if (rollStarted && rollLevelReached < 0) {
            rollLevelReached = engine.statistics.level;
            engine.statistics.level = MAX_LEVEL;
        }

        return false;
    }

    private void addEventText(GameEngine engine, int playerID, int lines) {
        final int baseX = receiver.getFieldDisplayPositionX(engine, playerID) + 4;
        final int baseY = receiver.getFieldDisplayPositionX(engine, playerID) + 52;
        final int pieceX = (16 * engine.nowPieceX) + baseX;
        final int pieceY = (16 * engine.nowPieceY) + baseY;

        int pieceXs = 0;
        int pieceYs = 0;

        for (int i = 0; i < engine.nowPieceObject.getMaxBlock(); ++i) {
            pieceXs += pieceX + (engine.nowPieceObject.dataX[engine.nowPieceObject.direction][i] * 16) + 8;
            pieceYs += pieceY + (engine.nowPieceObject.dataY[engine.nowPieceObject.direction][i] * 16) + 8;
        }

        pieceXs /= engine.nowPieceObject.getMaxBlock();
        pieceYs /= engine.nowPieceObject.getMaxBlock();

        final double upX = pieceXs < (baseX + (engine.field.getWidth() * 8))
            ? (baseX + ((engine.nowPieceX + engine.nowPieceObject.getMaximumBlockX()) * 16))
            : (baseX + ((engine.nowPieceX + engine.nowPieceObject.getMinimumBlockX()) * 16));

        final DoubleVector basePosition = new DoubleVector(upX, pieceYs, false);

        final String pieceName = Piece.getPieceName(engine.nowPieceObject.id);
        final DoubleVector baseVelocity = new DoubleVector(0.0, -6.4, false);
        final DoubleVector baseAcceleration = new DoubleVector(0.0, 9.80665 / 15.0, false);
        final float startScale = 1f;
        final float endScale = 2f;
        final int maxLife = 60;
        final int lifeOffset = 2;
        final int colour = 0x00FFFFFF;
        final boolean reverse = pieceXs < (receiver.getFieldDisplayPositionX(engine, playerID) + 4 + (8 * engine.field.getWidth()));

        final StringBuilder sb = new StringBuilder();

        if (engine.field.isEmpty()) {
            textEmitter.addString(
                "BRAVO!",
                new DoubleVector(
                    receiver.getFieldDisplayPositionX(engine, playerID) + 4d + (8 * engine.field.getWidth()),
                    receiver.getFieldDisplayPositionY(engine, playerID) + 52d + 32d,
                    false
                ), baseVelocity, baseAcceleration,
                ObjectAlignment.MIDDLE_MIDDLE,
                0, lifeOffset, maxLife,
                EventReceiver.COLOR_YELLOW,
                startScale, endScale * 1.5f,
                colour, 255,
                colour, 64,
                reverse
            );
        }

        if (lines == 0) {
            if (engine.tspinez) {
                sb.append("EZ-").append(pieceName);
                textEmitter.addString(
                    sb.toString(),
                    basePosition, baseVelocity, baseAcceleration,
                    ObjectAlignment.MIDDLE_MIDDLE,
                    0, lifeOffset, maxLife,
                    EventReceiver.COLOR_GREEN,
                    startScale, endScale,
                    colour, 255,
                    colour, 64,
                    reverse
                );
            } else if (engine.tspinmini) {
                sb.append(pieceName).append("-MINI");
                textEmitter.addString(
                    sb.toString(),
                    basePosition, baseVelocity, baseAcceleration,
                    ObjectAlignment.MIDDLE_MIDDLE,
                    0, lifeOffset, maxLife,
                    EventReceiver.COLOR_GREEN,
                    startScale, endScale,
                    colour, 255,
                    colour, 64,
                    reverse
                );
            } else if (engine.tspin) {
                sb.append(pieceName).append("-SPIN");
                textEmitter.addString(
                    sb.toString(),
                    basePosition, baseVelocity, baseAcceleration,
                    ObjectAlignment.MIDDLE_MIDDLE,
                    0, lifeOffset, maxLife,
                    EventReceiver.COLOR_PURPLE,
                    startScale, endScale,
                    colour, 255,
                    colour, 64,
                    reverse
                );
            }
        } else {
            String lineName = "SINGLE";
            if (lines == 2) lineName = "DOUBLE";
            else if (lines == 3) lineName = "TRIPLE";
            else if (lines >= 4) lineName = "FOUR";

            if (engine.tspinez) {
                sb.append("EZ-").append(pieceName).append("-").append(lineName);
                textEmitter.addString(
                    sb.toString(),
                    basePosition, baseVelocity, baseAcceleration,
                    ObjectAlignment.MIDDLE_MIDDLE,
                    0, lifeOffset, maxLife,
                    EventReceiver.COLOR_ORANGE,
                    startScale, endScale,
                    colour, 255,
                    colour, 64,
                    reverse
                );
            } else if (engine.tspinmini) {
                sb.append(pieceName).append("-").append(lineName).append("-M");
                textEmitter.addString(
                    sb.toString(),
                    basePosition, baseVelocity, baseAcceleration,
                    ObjectAlignment.MIDDLE_MIDDLE,
                    0, lifeOffset, maxLife,
                    EventReceiver.COLOR_ORANGE,
                    startScale, endScale,
                    colour, 255,
                    colour, 64,
                    reverse
                );
            } else if (engine.tspin) {
                sb.append(pieceName).append("-").append(lineName);
                textEmitter.addString(
                    sb.toString(),
                    basePosition, baseVelocity, baseAcceleration,
                    ObjectAlignment.MIDDLE_MIDDLE,
                    0, lifeOffset, maxLife,
                    EventReceiver.COLOR_PINK,
                    startScale, endScale,
                    colour, 255,
                    colour, 64,
                    reverse
                );
            } else {
                sb.append(lineName);
                textEmitter.addString(
                    lineName,
                    basePosition, baseVelocity, baseAcceleration,
                    ObjectAlignment.MIDDLE_MIDDLE,
                    0, lifeOffset, maxLife,
                    lines >= 4 ? EventReceiver.COLOR_YELLOW : EventReceiver.COLOR_WHITE,
                    startScale, endScale,
                    colour, 255,
                    colour, 64,
                    reverse
                );
            }
        }

        if (engine.b2b && engine.b2bcount > 1 && lines > 0) {
            textEmitter.addString(
                "B2B",
                DoubleVector.sub(basePosition, new DoubleVector(0, 24, false)),
                baseVelocity,
                baseAcceleration,
                ObjectAlignment.MIDDLE_MIDDLE,
                -1 * ((lifeOffset * (sb.length() - 3)) / 2), lifeOffset, maxLife,
                EventReceiver.COLOR_RED,
                startScale, endScale,
                colour, 255,
                colour, 64,
                reverse
            );
        }
    }

    private int abilityCharge(GameEngine engine, int baseCharge) {
        if (currentAbilityTimer > 0) return 0;
        else return engine.tspin ? 5 * baseCharge : baseCharge;
    }

    @Override
    public void calcScore(GameEngine engine, int playerID, int lines) {
        addEventText(engine, playerID, lines);

        if (lines >= 1) {
            if (engine.field.isEmpty()) {
                engine.playSE("bravo");
            }

            int levelIncrease = 0;

            // region Lines
            if (lines > 4) {
                levelIncrease += (lines * 2) * naturalLevelIncrement;

                if (settings.perk.isActive()) {
                    currentEnergy = Math.min(
                        settings.perk.energyStore,
                        currentEnergy + (int) (abilityCharge(engine, settings.perk.restoredForFour) * (lines / 4d))
                    );
                }
            } else if (lines == 4) {
                levelIncrease += 8 * naturalLevelIncrement;

                if (settings.perk.isActive()) {
                    currentEnergy = Math.min(settings.perk.energyStore, currentEnergy + abilityCharge(engine, settings.perk.restoredForFour));
                }
            } else if (lines == 3) {
                levelIncrease += 5 * naturalLevelIncrement;

                if (settings.perk.isActive()) {
                    currentEnergy = Math.min(settings.perk.energyStore, currentEnergy + abilityCharge(engine, settings.perk.restoredForTriple));
                }
            } else if (lines == 2) {
                levelIncrease += 2 * naturalLevelIncrement;

                if (settings.perk.isActive()) {
                    currentEnergy = Math.min(settings.perk.energyStore, currentEnergy + abilityCharge(engine, settings.perk.restoredForDouble));
                }
            } else {
                levelIncrease += naturalLevelIncrement;

                if (settings.perk.isActive()) {
                    currentEnergy = Math.min(settings.perk.energyStore, currentEnergy + abilityCharge(engine, settings.perk.restoredForSingle));
                }
            }
            // endregion Lines

            levelIncrease += badges.getLevelBonus() * naturalLevelIncrement;

            if (settings.perk == SeasonPerk.SPRING_PASSIVE) {
                engine.statistics.level += levelIncrease + naturalLevelIncrement;
            } else if (settings.perk == SeasonPerk.SPRING_ACTIVE && currentAbilityTimer > 0) {
                engine.statistics.level += levelIncrease * 2;
            } else {
                engine.statistics.level += levelIncrease;
            }

            if (engine.statistics.level > MAX_LEVEL) engine.statistics.level = MAX_LEVEL;

            levelUp(engine);
            badges.updateBadges(
                engine, lines,
                settings.perk == SeasonPerk.SPRING_PASSIVE,
                settings.perk == SeasonPerk.SPRING_ACTIVE && currentAbilityTimer > 0
            );

            // TODO: Temporary levelling. Add real ending later.
            if (engine.statistics.level >= MAX_LEVEL && !rollStarted) {
                engine.playSE("endingstart");

                nextSectionLevel = LEVELS_FEB;

                setNewBackground(0);
                fieldPurifyQueued = true;

                clearBaseGameGimmicks();

                customState = CustomState.FINAL_REWIND;
                engine.stat = GameEngine.STAT_CUSTOM;
                engine.resetStatc();
            } else if (engine.statistics.level >= MAX_LEVEL) {
                engine.resetFieldVisible();

                engine.ending = 1;
                engine.gameEnded();
            } else if (engine.statistics.level >= nextSectionLevel && rollStarted) {
                engine.playSE("levelup");

                final Season oldSeason = currentSeason;
                currentSeason = SEASON_TABLE.apply(engine.statistics.level);

                if (currentSeason != oldSeason) purifyFieldAndNexts(engine);

                setNewBackground(BACKGROUND_TABLE.apply(engine.statistics.level));
            } else if (engine.statistics.level >= nextSectionLevel) {
                engine.playSE("levelup");

                setNewBackground(BACKGROUND_TABLE.apply(engine.statistics.level));

                if (engine.statistics.level >= LEVELS_FEB && engine.statistics.level < LEVELS_MAR) {
                    gimmickSprMo2 = new Gimmicks.Sproutlings(badges, getGimmickPerkBoost());
                    descriptionToDraw = new DescriptionDraw(gimmickSprMo2);
                } else if (engine.statistics.level >= LEVELS_MAR && engine.statistics.level < LEVELS_APR) {
                    gimmickSprMo3 = new Gimmicks.FlourishingBlooms(new Random(engine.randSeed + 2), badges, getGimmickPerkBoost());
                    descriptionToDraw = new DescriptionDraw(gimmickSprMo3);
                } else if (engine.statistics.level >= LEVELS_APR && engine.statistics.level < LEVELS_MAY) {
                    gimmickSprMo2 = null;
                    gimmickSprMo3 = null;

                    gimmickSumMo1 = new Gimmicks.Dehydration(new Random(engine.randSeed + 3), LEVELS_APR, LEVELS_JUL);
                    descriptionToDraw = new DescriptionDraw(gimmickSumMo1);
                } else if (engine.statistics.level >= LEVELS_MAY && engine.statistics.level < LEVELS_JUN) {
                    gimmickSumMo2 = new Gimmicks.Mirage(badges, getGimmickPerkBoost());
                    descriptionToDraw = new DescriptionDraw(gimmickSumMo2);
                } else if (engine.statistics.level >= LEVELS_JUN && engine.statistics.level < LEVELS_JUL) {
                    gimmickSumMo3 = new Gimmicks.IntoTheFire(LEVELS_JUN, LEVELS_JUL);
                    gimmickSumMo3.getSpeed(engine, badges, getGimmickPerkBoost()); // Initialisation. Do not actually feed it into the speed yet.

                    descriptionToDraw = new DescriptionDraw(gimmickSumMo3);
                } else if (engine.statistics.level >= LEVELS_JUL && engine.statistics.level < LEVELS_AUG) {
                    gimmickSumMo1 = null;
                    gimmickSumMo2 = null;
                    gimmickSumMo3 = null;

                    gimmickAutMo1 = new Gimmicks.FallsCall();
                    descriptionToDraw = new DescriptionDraw(gimmickAutMo1);
                } else if (engine.statistics.level >= LEVELS_AUG && engine.statistics.level < LEVELS_SEP) {
                    gimmickAutMo2 = new Gimmicks.FlowingWinds(new Random(engine.randSeed + 4), LEVELS_AUG, LEVELS_OCT);
                    descriptionToDraw = new DescriptionDraw(gimmickAutMo2);
                } else if (engine.statistics.level >= LEVELS_SEP && engine.statistics.level < LEVELS_OCT) {
                    gimmickAutMo3 = new Gimmicks.GhoulsAfoot(badges, getGimmickPerkBoost());
                    descriptionToDraw = new DescriptionDraw(gimmickAutMo3);
                } else if (engine.statistics.level >= LEVELS_OCT && engine.statistics.level < LEVELS_NOV) {
                    gimmickAutMo1 = null;
                    gimmickAutMo2 = null;
                    gimmickAutMo3 = null;

                    gimmickWinMo1 = new Gimmicks.Whiteout(badges, getGimmickPerkBoost());
                    descriptionToDraw = new DescriptionDraw(gimmickWinMo1);
                } else if (engine.statistics.level >= LEVELS_NOV && engine.statistics.level < LEVELS_DEC) {
                    gimmickWinMo2 = new Gimmicks.SnowMounds(badges, getGimmickPerkBoost());
                    descriptionToDraw = new DescriptionDraw(gimmickWinMo2);
                } else if (engine.statistics.level >= LEVELS_DEC && engine.statistics.level < LEVELS_JAN) {
                    gimmickWinMo3 = new Gimmicks.ZeroCelsius(badges, getGimmickPerkBoost());
                    descriptionToDraw = new DescriptionDraw(gimmickWinMo3);
                }

                final Season oldSeason = currentSeason;
                currentSeason = SEASON_TABLE.apply(engine.statistics.level);

                if (currentSeason != oldSeason) fieldPurifyQueued = true;

                if (owner.bgmStatus.bgm <= BGM_TABLE.apply(engine.statistics.level)) {
                    owner.bgmStatus.fadesw = false;
                    owner.bgmStatus.bgm = BGM_TABLE.apply(engine.statistics.level);

                    if (NEXT_SECTION_LEVELS.apply(engine.statistics.level) > nextSectionLevel) {
                        nextSectionLevel = NEXT_SECTION_LEVELS.apply(engine.statistics.level);
                    }
                }

                customState = CustomState.REWIND;
                engine.stat = GameEngine.STAT_CUSTOM;
                engine.resetStatc();
            } else if (engine.statistics.level == nextSectionLevel - 1) {
                engine.playSE("levelstop");
            }
        }
    }

    @Override
    public void blockBreak(GameEngine engine, int playerID, int x, int y, Block blk) {
        // Add 0.1 badge per sproutling gem cleared.
        if (gimmickSprMo2 != null && blk.color == Block.BLOCK_COLOR_GEM_GREEN) {
            ++brokenSproutlings;
        }

        if (gimmickSprMo3 != null) {
            if (blk.color == Block.BLOCK_COLOR_GEM_ORANGE) {
                badges.addSeasonBadges(
                    1,
                    settings.perk == SeasonPerk.SPRING_PASSIVE,
                    settings.perk == SeasonPerk.SPRING_ACTIVE && currentAbilityTimer > 0
                );
            } else if (blk.color == Block.BLOCK_COLOR_GEM_PURPLE || blk.color == Block.BLOCK_COLOR_GEM_YELLOW) {
                ++brokenFlorets;
            }
        }

        if (gimmickSumMo1 != null && blk.getAttribute(Block.BLOCK_ATTRIBUTE_BONE)) {
            ++brokenBoneBlocks;
        }

        if (gimmickAutMo2 != null && blk.color == Block.BLOCK_COLOR_GEM_RAINBOW) {
            ++brokenRainbowBlocks;
        }

        if (gimmickWinMo1 != null && (blk.bonusValue & Gimmicks.Whiteout.SNOW_MASK) == Gimmicks.Whiteout.SNOW_IDENTIFIER) {
            ++brokenSnowBlocks;
        }

        if (gimmickWinMo2 != null && gimmickWinMo2.isYInSnow(engine, y)) {
            ++blocksUnderSnow;
        }

        if (gimmickWinMo3 != null && blk.hard > 0) {
            ++hardBlocksSeen;
        }
    }

    @Override
    public void callCalcScore(GameEngine engine, int playerID, int li) {
        HasCustomLineClear.super.callCalcScore(engine, playerID, li);

        // Update season badges based on lines.
        if (gimmickSumMo2 != null) {
            if (engine.nowPieceObject != null && engine.nowPieceObject.id == Piece.PIECE_I && li > 0) {
                badges.addSeasonBadges(
                    li > 2 ? (li + (li >>> 1)) : li,
                    settings.perk == SeasonPerk.SPRING_PASSIVE,
                    settings.perk == SeasonPerk.SPRING_ACTIVE && currentAbilityTimer > 0
                );
            }
        }
        if (gimmickSumMo3 != null) {
            if (li > 0) {
                badges.addSeasonBadges(
                    li > 2 ? (li + (li >>> 1)) : li,
                    settings.perk == SeasonPerk.SPRING_PASSIVE,
                    settings.perk == SeasonPerk.SPRING_ACTIVE && currentAbilityTimer > 0
                );
            }
        }
        if (gimmickAutMo1 != null) {
            if (li > 0) {
                final int bonus = engine.speed.gravity == 0 ? (li * 2) : (li > 2 ? (li + (li >>> 1)) : li);
                badges.addSeasonBadges(
                    bonus,
                    settings.perk == SeasonPerk.SPRING_PASSIVE,
                    settings.perk == SeasonPerk.SPRING_ACTIVE && currentAbilityTimer > 0
                );
            }
        }
        if (gimmickAutMo3 != null) {
            if (li > 0) {
                badges.addSeasonBadges(
                    li > 2 ? (li + (li >>> 1)) : li,
                    settings.perk == SeasonPerk.SPRING_PASSIVE,
                    settings.perk == SeasonPerk.SPRING_ACTIVE && currentAbilityTimer > 0
                );
            }
        }

        // Update gimmicks, and perform next queue modification here.
        if (gimmickSprMo2 != null) {
            gimmickSprMo2.setCountdown(badges, getGimmickPerkBoost());
        }
        if (gimmickSprMo3 != null) {
            gimmickSprMo3.setCountdown(badges, getGimmickPerkBoost());
        }
        if (gimmickSumMo1 != null) {
            gimmickSumMo1.updateChance(engine, badges, getGimmickPerkBoost());
            gimmickSumMo1.updateNext(engine);
            gimmickSumMo1.updateField(engine);
        }
        if (gimmickSumMo2 != null) {
            gimmickSumMo2.setAllowance(badges, getGimmickPerkBoost());
        }
        if (gimmickSumMo3 != null) {
            gimmickSumMo3.getSpeed(engine, badges, getGimmickPerkBoost());
        }
        if (gimmickAutMo1 != null) {
            gimmickAutMo1.getFallDelay(badges, getGimmickPerkBoost());
        }
        if (gimmickAutMo2 != null) {
            gimmickAutMo2.updateChance(engine, badges, getGimmickPerkBoost());
            gimmickAutMo2.updateNext(engine);
        }
        if (gimmickAutMo3 != null) {
            gimmickAutMo3.updateBonusGap(badges, getGimmickPerkBoost());
        }
        if (gimmickWinMo1 != null) {
            gimmickWinMo1.updateProportion(badges, getGimmickPerkBoost());
            gimmickWinMo1.updateNext(engine);
        }
        if (gimmickWinMo2 != null) {
            gimmickWinMo2.setTickTime(badges, getGimmickPerkBoost());
        }
        if (gimmickWinMo3 != null) {
            gimmickWinMo3.setCountdownMax(badges, getGimmickPerkBoost());
        }
    }

    @Override
    public void eraseFlaggedBlocks(GameEngine engine, int li) {
        if (li >= 4 && gimmickWinMo3 != null) {
            FieldManipulation.pushDown(engine.field);
        }

        HasCustomLineClear.super.eraseFlaggedBlocks(engine, li);
    }

    @Override
    public void callAndDrawBrokenBlocks(GameEngine engine, int playerID, int li) {
        brokenSproutlings = 0;
        brokenBoneBlocks = 0;
        brokenFlorets = 0;
        brokenRainbowBlocks = 0;
        brokenSnowBlocks = 0;
        blocksUnderSnow = 0;
        hardBlocksSeen = 0;

        HasCustomLineClear.super.callAndDrawBrokenBlocks(engine, playerID, li);

        // Janky... lmao. Update season badges based on single blocks.
        if (brokenSproutlings > 0) {
            badges.addSeasonBadges(
                brokenSproutlings + brokenSproutlings / 9,
                settings.perk == SeasonPerk.SPRING_PASSIVE,
                settings.perk == SeasonPerk.SPRING_ACTIVE && currentAbilityTimer > 0
            );
        }

        if (brokenFlorets > 0) {
            badges.addSeasonBadges(
                brokenFlorets,
                settings.perk == SeasonPerk.SPRING_PASSIVE,
                settings.perk == SeasonPerk.SPRING_ACTIVE && currentAbilityTimer > 0
            );
        }

        if (brokenBoneBlocks >= 2) {
            int bonus = brokenBoneBlocks / 2;
            if (bonus > 10) bonus += (bonus >>> 1);

            badges.addSeasonBadges(
                bonus,
                settings.perk == SeasonPerk.SPRING_PASSIVE,
                settings.perk == SeasonPerk.SPRING_ACTIVE && currentAbilityTimer > 0
            );
        }

        if (brokenRainbowBlocks > 0) {
            badges.addSeasonBadges(
                brokenRainbowBlocks * 5,
                settings.perk == SeasonPerk.SPRING_PASSIVE,
                settings.perk == SeasonPerk.SPRING_ACTIVE && currentAbilityTimer > 0
            );
        }

        if (brokenSnowBlocks >= 10) {
            int bonus = brokenSnowBlocks / 10;
            if (bonus > 2) bonus += (bonus >>> 1);

            badges.addSeasonBadges(
                bonus,
                settings.perk == SeasonPerk.SPRING_PASSIVE,
                settings.perk == SeasonPerk.SPRING_ACTIVE && currentAbilityTimer > 0
            );
        }

        if (blocksUnderSnow >= 5) {
            int bonus = blocksUnderSnow / 5;
            if (bonus > 4) bonus += (bonus >>> 1);

            badges.addSeasonBadges(
                bonus,
                settings.perk == SeasonPerk.SPRING_PASSIVE,
                settings.perk == SeasonPerk.SPRING_ACTIVE && currentAbilityTimer > 0
            );
        }

        if (hardBlocksSeen >= 4) {
            int bonus = hardBlocksSeen / 4;
            if (bonus > 5) bonus += (bonus >>> 1);

            badges.addSeasonBadges(
                bonus,
                settings.perk == SeasonPerk.SPRING_PASSIVE,
                settings.perk == SeasonPerk.SPRING_ACTIVE && currentAbilityTimer > 0
            );
        }

        // Update the season gimmicks, but do not process other things.
        if (gimmickSprMo2 != null) {
            gimmickSprMo2.setCountdown(badges, getGimmickPerkBoost());
        }
        if (gimmickSprMo3 != null) {
            gimmickSprMo3.setCountdown(badges, getGimmickPerkBoost());
        }
        if (gimmickSumMo1 != null) {
            gimmickSumMo1.updateChance(engine, badges, getGimmickPerkBoost());
        }
        if (gimmickSumMo2 != null) {
            gimmickSumMo2.setAllowance(badges, getGimmickPerkBoost());
        }
        if (gimmickSumMo3 != null) {
            gimmickSumMo3.getSpeed(engine, badges, getGimmickPerkBoost());
        }
        if (gimmickAutMo1 != null) {
            gimmickAutMo1.getFallDelay(badges, getGimmickPerkBoost());
        }
        if (gimmickAutMo2 != null) {
            gimmickAutMo2.updateChance(engine, badges, getGimmickPerkBoost());
        }
        if (gimmickAutMo3 != null) {
            gimmickAutMo3.updateBonusGap(badges, getGimmickPerkBoost());
        }
        if (gimmickWinMo1 != null) {
            gimmickWinMo1.updateProportion(badges, getGimmickPerkBoost());
        }
        if (gimmickWinMo2 != null) {
            gimmickWinMo2.setTickTime(badges, getGimmickPerkBoost());
            gimmickWinMo2.reduceHeight(li);
        }
        if (gimmickWinMo3 != null) {
            gimmickWinMo3.setCountdownMax(badges, getGimmickPerkBoost());
        }
    }

    private void purifyFieldAndNexts(GameEngine engine) {
        // TODO: Might need to do some extra purification steps here.
        FieldManipulation.clearFieldEffects(engine.field, blk -> {
            blk.countdown = 0;
            blk.color = Block.gemToNormalColor(blk.color);
            blk.bonusValue = 0;
            blk.secondaryColor = 0;
        });

        FieldManipulation.updateAllBlockConnections(engine.field);

        if (engine.holdPieceObject != null) {
            engine.holdPieceObject.setAttribute(Block.BLOCK_ATTRIBUTE_BONE, false);
        }

        for (int i = 0; i < engine.nextPieceArraySize; ++i) {
            engine.nextPieceArrayObject[i] = HasCustomOnMove.initialisePiece(engine, engine.nextPieceArrayID[i]);
        }
    }

    @Override
    public boolean onLineClear(GameEngine engine, int playerID) {
        // If field purify queued, clear field effects.
        // It will be true when seasons change, or when the ending starts.
        if (fieldPurifyQueued) {
            fieldPurifyQueued = false;
            purifyFieldAndNexts(engine);
        }

        return HasCustomLineClear.super.inOnLineClear(engine, playerID);
    }

    private void levelUp(GameEngine engine) {
        if (!settings.perk.isActive()) {
            final double proportion = engine.statistics.level / (double) MAX_LEVEL;

            engine.meterValue = (int) Math.floor(receiver.getMeterMax(engine) * proportion);

            engine.meterColor = GameEngine.METER_COLOR_RED;
            if (proportion >= 0.75) engine.meterColor = GameEngine.METER_COLOR_GREEN;
            else if (proportion >= 0.5) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
            else if (proportion >= 0.25) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
        }

        setSpeed(engine);

        if ((engine.statistics.level >= LEVELS_APR) && (!settings.fullGhost)) engine.ghost = false;

        final int fadeout = BGM_FADE_LEVEL_TABLE.apply(engine.statistics.level);
        if ((fadeout > 0) && (engine.statistics.level >= fadeout)) {
            owner.bgmStatus.fadesw = true;
        }
    }

    @Override
    public void drawBackgroundElements(RendererExtension rendererExtension, EventReceiver receiver, GameEngine engine, int playerID) {
        HasCustomFieldDrawing.super.drawBackgroundElements(rendererExtension, receiver, engine, playerID);

        if (gimmickWinMo1 != null) {
            gimmickWinMo1.drawOuterFog(receiver, drawing);
        }

        if (vortex != null) {
            vortex.draw(rendererExtension, receiver);
        }
    }

    // Overlays for countdowns or hardness meters.
    private void drawBlockTextOverlays(GameEngine engine, int playerID) {
        if (engine.field == null) return;

        // Draw countdowns for Flourishing Bloom, or Hard counter for Zero Celsius
        final int baseX = receiver.getFieldDisplayPositionX(engine, playerID) + 4;
        final int baseY = receiver.getFieldDisplayPositionY(engine, playerID) + 52;

        for (int y = 0; y < engine.field.getHeightWithoutHurryupFloor(); ++y) {
            for (int x = 0; x < engine.field.getWidth(); ++x) {
                // Flourishing Bloom
                if (gimmickSprMo3 != null) {
                    final Block blk = engine.field.getBlock(x, y);

                    if (blk != null && blk.color == Block.BLOCK_COLOR_GEM_ORANGE) {
                        GameTextUtilities.drawAlignedText(
                            engine,
                            baseX + (16 * x) + 8,
                            baseY + (16 * y) + 8,
                            GameTextUtilities.Text.ofSmall(
                                String.valueOf(blk.countdown),
                                blk.countdown == gimmickSprMo3.getCurrentCountdown() - 1 ? EventReceiver.COLOR_YELLOW : EventReceiver.COLOR_WHITE
                            ),
                            ObjectAlignment.MIDDLE_MIDDLE
                        );
                    }
                }

                // Zero Celsius
                if (gimmickWinMo3 != null) {
                    final Block blk = engine.field.getBlock(x, y);

                    if (blk != null && blk.hard > 0) {
                        customGraphics.drawImage(
                            engine, "iceblock",
                            baseX + (16 * x), baseY + (16 * y),
                            0, 0, 32, 32,
                            200, 255, 255, 255,
                            0.5f
                        );

                        GameTextUtilities.drawAlignedText(
                            engine,
                            baseX + (16 * x) + 8,
                            baseY + (16 * y) + 8,
                            GameTextUtilities.Text.ofSmall(
                                String.valueOf(blk.hard),
                                EventReceiver.COLOR_WHITE
                            ),
                            ObjectAlignment.MIDDLE_MIDDLE
                        );
                    }
                }
            }
        }
    }

    private void renderExtraBoardInfo(GameEngine engine, int playerID) {
        drawBlockTextOverlays(engine, playerID);

        if (gimmickAutMo3 != null && engine.stat != GameEngine.STAT_CUSTOM) {
            gimmickAutMo3.renderFlashlight(receiver, engine, playerID, drawing);
        }

        if (gimmickWinMo2 != null) {
            gimmickWinMo2.draw(drawing, receiver, engine, playerID);
        }

        if (gimmickWinMo1 != null && engine.stat != GameEngine.STAT_MOVE && engine.stat != GameEngine.STAT_CUSTOM) {
            gimmickWinMo1.drawInnerFog(receiver, engine, playerID, drawing);
        }

        rewindBlocks.forEach(rb -> rb.draw(rendererExtension, receiver));
        textEmitter.drawAll(engine);
    }

    @Override
    public void renderFirst(GameEngine engine, int playerID) {
        inRenderFirst(rendererExtension, receiver, engine, playerID);
        renderExtraBoardInfo(engine, playerID);
    }

    @Override
    public void renderMove(GameEngine engine, int playerID) {
        inRenderMove(rendererExtension, receiver, engine, playerID);
        rendererExtension.drawPostHoldOutline(receiver, engine, playerID);

        if (gimmickWinMo1 != null) {
            gimmickWinMo1.drawInnerFog(receiver, engine, playerID, drawing);
        }
    }

    @Override
    public void renderExcellent(GameEngine engine, int playerID) {
        inRenderExcellent(rendererExtension, receiver, engine, playerID);
    }

    @Override
    public void renderGameOver(GameEngine engine, int playerID) {
        inRenderGameOver(rendererExtension, receiver, engine, playerID);
    }

    @Override
    public void renderResult(GameEngine engine, int playerID) {
        inRenderResult(rendererExtension, receiver, engine, playerID);
    }

    @Override
    public void onFirst(GameEngine engine, int playerID) {
        if (settings.perk == SeasonPerk.WINTER_ACTIVE) {
            if (currentAbilityTimer > 0) {
                setSpeed(engine);
                engine.speed.lockDelay = 180;
            }
        }
    }

    private void processGimmicks(GameEngine engine, int playerID) {
        if (gimmickAutMo3 == null) {
            engine.blockShowOutlineOnly = false;
        } else {
            gimmickAutMo3.updateCurrentBonusGap(engine);
        }

        if (gimmickWinMo2 != null && engine.timerActive) {
            gimmickWinMo2.update(engine);
        }

        if (gimmickWinMo3 != null && engine.timerActive) {
            gimmickWinMo3.updateField(engine);
        }
    }

    @Override
    public void onLast(GameEngine engine, int playerID) {
        if (!(engine.stat == GameEngine.STAT_CUSTOM
            && (customState == CustomState.REWIND || customState == CustomState.FINAL_REWIND)
            && fadeProgress == 150)) {
            updateFadeProgress();
        }

        if (descriptionToDraw != null && descriptionToDraw.update()) {
            descriptionToDraw = null;
        }

        if (badges != null) {
            badges.updateDrawTimers();
        }

        if (!engine.lagStop) {
            textEmitter.updateAll();
            rewindBlocks.removeIf(RewindBlock::update);

            if (engine.stat == GameEngine.STAT_CUSTOM && engine.gameStarted && fadeProgress < 240) {
                vortex.add(bvr, bvr.nextInt(7) + 2, engine.getSkin());
                vortex.add(bvr, bvr.nextInt(7) + 2, engine.getSkin());
                vortex.add(bvr, bvr.nextInt(7) + 2, engine.getSkin());
                vortex.add(bvr, bvr.nextInt(7) + 2, engine.getSkin());
                vortex.add(bvr, Block.BLOCK_COLOR_RAINBOW, engine.getSkin());
                vortex.add(bvr, Block.BLOCK_COLOR_RAINBOW, engine.getSkin());
            }

            if (vortex != null) {
                vortex.update();
            }

            processGimmicks(engine, playerID);
        }

        if (currentAbilityTimer > 0 && engine.stat == GameEngine.STAT_MOVE) {
            final int prevTimer = currentAbilityTimer--;

            if (settings.perk == SeasonPerk.WINTER_ACTIVE && prevTimer == 1) {
                setSpeed(engine);
                engine.lockDelayNow = (int) Math.floor((engine.lockDelayNow * engine.getLockDelay()) / 180d);
            }

            if (prevTimer == 1) {
                engine.playSE("stageclear");
            }
        }

        if (currentAbilityTimer > 0) {
            final double proportion = currentAbilityTimer / (double) settings.perk.duration;

            engine.meterValue = (int) Math.floor(receiver.getMeterMax(engine) * proportion);

            engine.meterColor = GameEngine.METER_COLOR_RED;
            if (proportion >= 0.75) engine.meterColor = GameEngine.METER_COLOR_GREEN;
            else if (proportion >= 0.5) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
            else if (proportion >= 0.25) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
        } else if (settings.perk.isActive()) {
            final double proportion = currentEnergy / (double) settings.perk.energyStore;

            engine.meterValue = (int) Math.floor(receiver.getMeterMax(engine) * proportion);

            engine.meterColor = GameEngine.METER_COLOR_RED;
            if (proportion >= 1.0) engine.meterColor = GameEngine.METER_COLOR_GREEN;
            else if (proportion >= (2d / 3d)) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
            else if (proportion >= (1d / 3d)) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
        }

        if (engine.gameActive && engine.stat == GameEngine.STAT_CUSTOM && (customState == CustomState.REWIND || customState == CustomState.FINAL_REWIND)) {
            engine.framecolor = GameEngine.FRAME_COLOR_PURPLE;
        } else if (engine.gameActive && settings.perk.isActive() && currentAbilityTimer > 0) {
            engine.framecolor = GameEngine.FRAME_COLOR_PINK;
        } else if (engine.gameStarted) {
            engine.framecolor = currentSeason.defaultFrameColour;
        } else {
            engine.framecolor = Season.defaultMenuFrameColour();
        }

        if (!engine.lagStop && engine.gameStarted && engine.ctrl.isPush(Controller.BUTTON_F) && currentEnergy >= settings.perk.energyStore && settings.perk.isActive()) {
            currentEnergy = 0;
            currentAbilityTimer = settings.perk.duration;

            if (settings.perk == SeasonPerk.AUTUMN_ACTIVE) queuedFreefall = true;
            else if (settings.perk == SeasonPerk.SUMMER_ACTIVE) {
                HasCustomOnMove.insertIntoNexts(engine, engine.nextPieceCount, Piece.PIECE_I, Piece.PIECE_I, Piece.PIECE_I);
                for (int i = engine.nextPieceCount; i < engine.nextPieceCount + 3; ++i) {
                    for (Block blk : HasCustomOnMove.getNextObject(engine, i).block) blk.item = -1;
                }
            }

            engine.playSE("medal");
        }

        if (engine.stat == GameEngine.STAT_SETTING || (engine.stat == GameEngine.STAT_RESULT && !owner.replayMode)) {
            // Show player rank
            if (engine.ctrl.isPush(Controller.BUTTON_D) && playerProperties.isLoggedIn() && engine.stat != GameEngine.STAT_CUSTOM) {
                showPlayerStats = !showPlayerStats;
                engine.playSE("change");
            }
        }
    }

    @Override
    public void renderLast(GameEngine engine, int playerID) {
        final int titlesColour = settings.perk == SeasonPerk.PERKLESS ? EventReceiver.COLOR_ORANGE : EventReceiver.COLOR_YELLOW;

        receiver.drawScoreFont(engine, playerID, 0, 0, getName(), titlesColour);

        if (engine.stat == GameEngine.STAT_SETTING || (engine.stat == GameEngine.STAT_RESULT && !owner.replayMode)) {
            final float scale = (receiver.getNextDisplayType() == 2) ? 0.5f : 1.0f;
            final int topY = (receiver.getNextDisplayType() == 2) ? 5 : 3;
            final boolean showRankings = !owner.replayMode && !settings.fullGhost;

            if (showRankings) {
                receiver.drawScoreFont(engine, playerID, 3, topY - 1, "RANK/TITLE  DATE", titlesColour, scale);
                for (int i = 0; i < SeasonsSettings.RANKING_MAX; ++i) {
                    receiver.drawScoreFont(engine, playerID, 0, topY + i, String.format("%2d N/A", i + 1));
                    GameTextUtilities.drawAlignedScoreTextBlock(
                        receiver, engine, playerID,
                        receiver.getNextDisplayType() == 2,
                        15, topY + i,
                        false,
                        levelToRankBlock(0, -1), // TODO: RANKING
                        ObjectAlignment.TOP_LEFT
                    );
                }

                receiver.drawScoreFont(engine, playerID, 0, topY + 1 + SeasonsSettings.RANKING_MAX, "D:SWITCH RANK SCREEN", EventReceiver.COLOR_GREEN, scale);
            }

            GameTextUtilities.drawAlignedScoreTextBlock(
                receiver, engine, playerID, receiver.getNextDisplayType() == 2,
                0, topY + (showRankings ? 3 + SeasonsSettings.RANKING_MAX : (-1)), false,
                settings.perk.getDescription(scale),
                ObjectAlignment.TOP_LEFT
            );
        } else if (engine.stat == GameEngine.STAT_CUSTOM && !engine.gameStarted) {
            playerProperties.loginScreen.renderScreen(receiver, engine, playerID);
        } else {
            receiver.drawScoreFont(engine, playerID, 0, 2, "DATE", titlesColour);
            if (engine.stat == GameEngine.STAT_GAMEOVER && rollLevelReached >= 0) {
                receiver.drawScoreFont(engine, playerID, 0, 3, levelToString(rollLevelReached));
            } else {
                receiver.drawScoreFont(engine, playerID, 0, 3, levelToString(engine.statistics.level));
            }

            receiver.drawScoreFont(engine, playerID, 0, 5, "TIME", titlesColour);
            receiver.drawScoreFont(engine, playerID, 0, 6, GeneralUtil.getTime(engine.statistics.time));

            receiver.drawScoreFont(engine, playerID, 0, 8, "PERK", titlesColour);
            receiver.drawScoreFont(engine, playerID, 0, 9, settings.perk.getName(), currentAbilityTimer > 0);

            receiver.drawScoreFont(engine, playerID, 0, 11, "BADGES", titlesColour);
            GameTextUtilities.drawAlignedScoreTextBlock(
                receiver, engine, playerID, false,
                0, 12, false,
                badges.getBadgeDisplay(false),
                ObjectAlignment.TOP_LEFT
            );

            if (playerProperties.isLoggedIn() || !settings.playerName.isEmpty()) {
                final String name = playerProperties.isLoggedIn() ? playerProperties.getNameDisplay() : settings.playerName;

                receiver.drawScoreFont(engine, playerID, 13, 12, "PLAYER", titlesColour);
                GameTextUtilities.drawAlignedScoreText(
                    receiver, engine, playerID, false,
                    13, 12,
                    GameTextUtilities.Text.ofBig(name),
                    ObjectAlignment.TOP_LEFT
                );
            }

            if (engine.statistics.level >= LEVELS_FEB || rollStarted) {
                receiver.drawScoreFont(engine, playerID, 0, 17, "EFFECTS", titlesColour);
            }

            if (gimmickSprMo2 != null) {
                GameTextUtilities.drawAlignedScoreTextBlock(
                    receiver, engine, playerID, false,
                    0, 18,
                    false,
                    gimmickSprMo2.getSummary(),
                    ObjectAlignment.TOP_LEFT
                );
            }

            if (gimmickSprMo3 != null) {
                GameTextUtilities.drawAlignedScoreTextBlock(
                    receiver, engine, playerID, false,
                    0, 19,
                    false,
                    gimmickSprMo3.getSummary(),
                    ObjectAlignment.TOP_LEFT
                );
            }

            if (gimmickSumMo1 != null) {
                GameTextUtilities.drawAlignedScoreTextBlock(
                    receiver, engine, playerID, false,
                    0, 18,
                    false,
                    gimmickSumMo1.getSummary(),
                    ObjectAlignment.TOP_LEFT
                );
            }

            if (gimmickSumMo2 != null) {
                GameTextUtilities.drawAlignedScoreTextBlock(
                    receiver, engine, playerID, false,
                    0, 19,
                    false,
                    gimmickSumMo2.getSummary(),
                    ObjectAlignment.TOP_LEFT
                );
            }

            if (gimmickSumMo3 != null) {
                GameTextUtilities.drawAlignedScoreTextBlock(
                    receiver, engine, playerID, false,
                    0, 20,
                    false,
                    gimmickSumMo3.getSummary(),
                    ObjectAlignment.TOP_LEFT
                );
            }

            if (gimmickAutMo1 != null) {
                GameTextUtilities.drawAlignedScoreTextBlock(
                    receiver, engine, playerID, false,
                    0, 18,
                    false,
                    gimmickAutMo1.getSummary(),
                    ObjectAlignment.TOP_LEFT
                );
            }

            if (gimmickAutMo2 != null) {
                GameTextUtilities.drawAlignedScoreTextBlock(
                    receiver, engine, playerID, false,
                    0, 19,
                    false,
                    gimmickAutMo2.getSummary(),
                    ObjectAlignment.TOP_LEFT
                );
            }

            if (gimmickAutMo3 != null) {
                GameTextUtilities.drawAlignedScoreTextBlock(
                    receiver, engine, playerID, false,
                    0, 20,
                    false,
                    gimmickAutMo3.getSummary(),
                    ObjectAlignment.TOP_LEFT
                );
            }

            if (gimmickWinMo1 != null) {
                GameTextUtilities.drawAlignedScoreTextBlock(
                    receiver, engine, playerID, false,
                    0, 18,
                    false,
                    gimmickWinMo1.getSummary(),
                    ObjectAlignment.TOP_LEFT
                );
            }

            if (gimmickWinMo2 != null) {
                GameTextUtilities.drawAlignedScoreTextBlock(
                    receiver, engine, playerID, false,
                    0, 19,
                    false,
                    gimmickWinMo2.getSummary(),
                    ObjectAlignment.TOP_LEFT
                );
            }

            if (gimmickWinMo3 != null) {
                GameTextUtilities.drawAlignedScoreTextBlock(
                    receiver, engine, playerID, false,
                    0, 20,
                    false,
                    gimmickWinMo3.getSummary(),
                    ObjectAlignment.TOP_LEFT
                );
            }

            if (descriptionToDraw != null) {
                descriptionToDraw.descObj.drawDescription(drawing, receiver, engine, descriptionToDraw.getDrawXOffset(), descriptionToDraw.getDrawY());
            }
        }
    }

    @Override
    public void saveReplay(GameEngine engine, int playerID, CustomProperties prop) {
        settings.saveSetting(owner.replayProp, true);

        // If they have completed the game, set the completion status to true for their profile.
        if (playerProperties.isLoggedIn() && engine.ending > 0 && engine.statistics.level >= MAX_LEVEL) {
            settings.hasCompletedGame = true;
            settings.saveSettingPlayer(playerProperties);
        }

        // TODO: Rankings
    }

    private static class Grading {
        public static final int MAX_GRADE_POINTS = 12000;

        private static final int BASE_PERF_DECAY = 120;
        private static final double PERF_DECAY_POW = 6d / 7d;

        private int performance; // 0->2500
        private int performanceDecay;
        private int currentPerformanceDecayRate;

        public Grading() {
            performance = 00;
            performanceDecay = 0;
            currentPerformanceDecayRate = BASE_PERF_DECAY;
        }

        public void addPerformancePoints(int performancePoints) {
            performance = Math.min(2500, performance + performancePoints);
            currentPerformanceDecayRate = (int) Math.floor(BASE_PERF_DECAY * Math.pow(PERF_DECAY_POW, Math.floor(performance / 100d)));
        }

        public void updatePerformanceDecay() {
            if (++performanceDecay >= currentPerformanceDecayRate) {
                performance = Math.max(performance - 1, performance - (performance % 100));
                performanceDecay = 0;
            }
        }

        public int getPerformance() {
            return Math.min(2500, performance);
        }

        // 0->4000
        public static int getBadgePerformance(Badges badges) {
            return Math.min(4000, badges.getBadges() / 2);
        }

        // 0->3000 (level + roll level each)
        public static int getLevelPerformance(int level) {
            return (int) Math.floor(3000d * level / (double) MAX_LEVEL);
        }

        public static final int ALL_CLEAR_BONUS = 400;

        public static final int PERKLESS_BONUS = 600;

        public TotalGrades freeze(int level, int rollLevel, Badges badges, SeasonPerk perk) {
            return new TotalGrades(
                getPerformance(),
                getBadgePerformance(badges),
                getLevelPerformance(level),
                getLevelPerformance(rollLevel),
                (rollLevel >= level) && (level >= MAX_LEVEL) ? ALL_CLEAR_BONUS : 0,
                perk == SeasonPerk.PERKLESS ? PERKLESS_BONUS : 0
            );
        }
    }

    private static class TotalGrades {
        public final int totalGradePoints;
        public final int totalPerformancePoints;
        public final int totalBadgePoints;
        public final int totalLevelPoints;
        public final int totalRollLevelPoints;
        public final int allClearBonus;
        public final int perklessBonus;

        public TotalGrades(int totalPerformancePoints, int totalBadgePoints, int totalLevelPoints, int totalRollLevelPoints, int allClearBonus, int perklessBonus) {
            this.totalPerformancePoints = totalPerformancePoints;
            this.totalBadgePoints = totalBadgePoints;
            this.totalLevelPoints = totalLevelPoints;
            this.totalRollLevelPoints = totalRollLevelPoints;
            this.allClearBonus = allClearBonus;
            this.perklessBonus = perklessBonus;

            this.totalGradePoints = Math.min(
                Grading.MAX_GRADE_POINTS,
                totalPerformancePoints + totalBadgePoints + totalLevelPoints + totalRollLevelPoints + allClearBonus + perklessBonus
            );
        }

        private static void addRanks(LevelTableBuilder<Pair<GameTextUtilities.TextBlock, IntPair>>.ModifiableLevelTable table, String name, int basePoints, int count, int titleColour) {
            for (int i = 1; i <= count; ++i) {
                // this assumes count <= 20
                final int rank = count - (i - 1);

                String suffix = "TH";
                if (rank == 3) suffix = "RD";
                else if (rank == 2) suffix = "ND";
                else if (rank == 1) suffix = "ST";

                table.addValue(
                    Pair.of(
                        GameTextUtilities.TextBlock.of(
                            GameTextUtilities.TextJustification.CENTRE,
                            GameTextUtilities.Text.of(name, titleColour),
                            GameTextUtilities.Text.newLine(),
                            GameTextUtilities.Text.ofSmall(rank + suffix + " RANK")
                        ),
                        IntPair.of(
                            basePoints + (i * 100),
                            basePoints + (count * 100)
                        )
                    ),
                    basePoints + (i * 100)
                );
            }
        }

        // (Name, (Next Rank at Points, Next Title at Points))
        public static final IntFunction<Pair<GameTextUtilities.TextBlock, IntPair>> GRADE_NAMES;
        static {
            final LevelTableBuilder<Pair<GameTextUtilities.TextBlock, IntPair>>.ModifiableLevelTable table = LevelTableBuilder.createNew();

            // Settler
            table.addValue(
                Pair.of(
                    GameTextUtilities.TextBlock.of(GameTextUtilities.Text.of("SETTLER", EventReceiver.COLOR_WHITE)),
                    IntPair.of(100, 100)
                ),
                100
            );

            // Traveller
            addRanks(table, "TRAVELLER", 100, 7, EventReceiver.COLOR_GREEN);

            // Wanderer
            addRanks(table, "WANDERER", 800, 7, EventReceiver.COLOR_GREEN);

            // Adept
            addRanks(table, "PILGRIM", 1500, 7, EventReceiver.COLOR_GREEN);

            // Nomad
            addRanks(table, "NOMAD", 2200, 7, EventReceiver.COLOR_GREEN);

            // Trainee
            addRanks(table, "TRAINEE", 2900, 7, EventReceiver.COLOR_YELLOW);

            // Warrior
            addRanks(table, "WARRIOR", 3600, 7, EventReceiver.COLOR_YELLOW);

            // Noble
            addRanks(table, "NOBLE", 4300, 7, EventReceiver.COLOR_YELLOW);

            // Hero
            addRanks(table, "HERO", 5000, 7, EventReceiver.COLOR_YELLOW);

            // Attuned
            addRanks(table, "ATTUNED", 5700, 7, EventReceiver.COLOR_ORANGE);

            // Symbiotic
            addRanks(table, "SYMBIOTIC", 6400, 7, EventReceiver.COLOR_ORANGE);

            // Elemental
            addRanks(table, "ELEMENTAL", 7100, 7, EventReceiver.COLOR_ORANGE);

            // Embodiment
            addRanks(table, "EMBODIMENT", 7800, 7, EventReceiver.COLOR_ORANGE);

            // Overseer
            addRanks(table, "OVERSEER", 8500, 7, EventReceiver.COLOR_CYAN);

            // Archon
            addRanks(table, "ARCHON", 9200, 7, EventReceiver.COLOR_CYAN);

            // High Ruler
            addRanks(table, "HIGH RULER", 9900, 7, EventReceiver.COLOR_CYAN);

            // Dominator
            addRanks(table, "DOMINATOR", 10600, 7, EventReceiver.COLOR_CYAN);

            // Master
            addRanks(table, "MASTER", 11300, 7, EventReceiver.COLOR_YELLOW);

            // SGM
            GRADE_NAMES = table
                .addTerminalValue(
                    Pair.of(
                        GameTextUtilities.TextBlock.of(
                            GameTextUtilities.TextJustification.CENTRE,
                            GameTextUtilities.Text.ofBig("SEASONS", EventReceiver.COLOR_GREEN),
                            GameTextUtilities.Text.newLine(),
                            GameTextUtilities.Text.of("GRAND MASTER", EventReceiver.COLOR_YELLOW)
                        ),
                        IntPair.of(Integer.MAX_VALUE, Integer.MAX_VALUE)
                    )
                ).buildLevelTable();
        }
    }
}
