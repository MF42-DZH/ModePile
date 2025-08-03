package zeroxfc.nullpo.custom.modes;

import java.util.function.IntFunction;
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
import zeroxfc.nullpo.custom.libs.FieldManipulation;
import zeroxfc.nullpo.custom.libs.GameTextUtilities;
import zeroxfc.nullpo.custom.libs.LevelTableBuilder;
import zeroxfc.nullpo.custom.libs.ProfileProperties;
import zeroxfc.nullpo.custom.libs.RendererExtension;
import zeroxfc.nullpo.custom.libs.SpeedTableBuilder;
import zeroxfc.nullpo.custom.libs.mixins.HasCustomFieldDrawing;
import zeroxfc.nullpo.custom.libs.mixins.HasCustomOnMove;
import zeroxfc.nullpo.custom.libs.types.ObjectAlignment;
import zeroxfc.nullpo.custom.modes.objects.seasons.Badges;
import zeroxfc.nullpo.custom.modes.objects.seasons.SeasonPerk;
import zeroxfc.nullpo.custom.modes.objects.seasons.SeasonsSettings;

public class Seasons extends DummyMode implements HasCustomOnMove, HasCustomFieldDrawing {
    private static final Logger log = Logger.getLogger(Seasons.class);

    private static final int CURRENT_VERSION = 0;

    // TODO: This eventually will need to be changed.
    private static final IntFunction<SpeedParam> SPEED_TABLE = SpeedTableBuilder.createNew()
        .addTerminalGravity(4, 256)
        .addTerminalARE(25)
        .addTerminalLineARE(25)
        .addTerminalDAS(15)
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
        .addTerminalValue("INVALID")
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
        .addValue(Season.SUMMER, LEVELS_JUN)
        .addValue(Season.AUTUMN, LEVELS_OCT)
        .addTerminalValue(Season.WINTER)
        .buildLevelTable();

    private static final IntFunction<Integer> BGM_TABLE = LevelTableBuilder.<Integer>createNew()
        .addValue(BGMStatus.BGM_NORMAL1, LEVELS_APR)
        .addValue(BGMStatus.BGM_NORMAL2, LEVELS_JUN)
        .addValue(BGMStatus.BGM_NORMAL3, LEVELS_OCT)
        .addTerminalValue(BGMStatus.BGM_NORMAL4)
        .buildLevelTable();

    private static final IntFunction<Integer> BGM_FADE_LEVEL_TABLE = LevelTableBuilder.<Integer>createNew()
        .addValue(LEVELS_MAY - 72, LEVELS_APR)
        .addValue(LEVELS_AUG - 72, LEVELS_JUN)
        .addValue(LEVELS_NOV - 72, LEVELS_OCT)
        .addTerminalValue(-1)
        .buildLevelTable();

    private static String levelToString(int level) {
        final String month = MONTH_NAME_TABLE.apply(level);
        final int normLevel = level - LEVELS_SO_FAR.apply(level);
        return String.format("%02d:00 %02d %s YEAR %d", normLevel % 24, (normLevel / 24) + 1, month, level >= LEVELS_DEC ? 1 : 0);
    }

    // Game manager and current renderer.
    private GameManager owner;
    private EventReceiver receiver;
    private CustomResourceHolder customGraphics;
    private RendererExtension rendererExtension;

    // Settings
    private static final int HEADER_COLOUR = EventReceiver.COLOR_ORANGE;
    private SeasonsSettings settings;
    private ProfileProperties playerProperties;
    private boolean showPlayerStats;
    private RuleOptions ruleOptCopy;

    // Gameplay variables
    private boolean levelUpFlag;
    private int nextSectionLevel;
    private Season currentSeason;
    private int lastBackground, currentBackground, fadeProgress;
    private Badges badges;
    private int naturalLevelIncrement; // Is set to 6 during ending.
    private int currentEnergy;
    private int currentAbilityTimer;
    private boolean queuedFreefall; // Freefall only works between pieces so it doesn't screw you over.
    private boolean hasLandedBefore;
    private int lockedPieces;

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

        setupBackgrounds(engine);

        levelUpFlag = false;
        currentSeason = Season.SPRING;
        nextSectionLevel = 0;
        badges = new Badges();
        lockedPieces = 0;

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

    @Override
    public boolean onCustom(GameEngine engine, int playerID) {
        if (engine.gameStarted) {
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

    @Override
    public void renderCustom(GameEngine engine, int playerID) {
        if (engine.gameStarted) {
            int baseX = (8 * engine.field.getWidth()) + 4 + receiver.getFieldDisplayPositionX(engine, playerID);
            int baseY = (8 * engine.field.getHeight()) + 52 + receiver.getFieldDisplayPositionY(engine, playerID);

            GameTextUtilities.drawAlignedTextBlock(
                engine,
                baseX, baseY,
                false,
                GameTextUtilities.TextBlock.of(
                    GameTextUtilities.TextJustification.LEFT,
                    GameTextUtilities.Text.ofBig("FREE", (engine.statc[0] >>> 1) % 2 == 0 ? EventReceiver.COLOR_WHITE : EventReceiver.COLOR_YELLOW),
                    GameTextUtilities.Text.newLine(),
                    GameTextUtilities.Text.ofBig("FALL", (engine.statc[0] >>> 1) % 2 == 0 ? EventReceiver.COLOR_WHITE : EventReceiver.COLOR_YELLOW)
                ),
                ObjectAlignment.MIDDLE_MIDDLE
            );
        }
    }

    @Override
    public boolean onReady(GameEngine engine, int playerID) {
        if (engine.statc[0] == 0) {
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
            int change = updateCursor(engine, 1);

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

            if (engine.statc[3] >= 180) {
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

        drawMenu(engine, playerID, receiver, 0, EventReceiver.COLOR_YELLOW, 0,
            "PERK", perkString
        );
        drawMenu(engine, playerID, receiver, 2, EventReceiver.COLOR_BLUE, 1,
            "FULL GHOST", GeneralUtil.getONorOFF(settings.fullGhost)
        );
    }

    private void setSpeed(GameEngine engine) {
        // TODO: Populate this further later
        engine.speed = SPEED_TABLE.apply(engine.statistics.level);
        if (settings.perk == SeasonPerk.WINTER_PASSIVE) engine.speed.lockDelay += 6;
    }

    @Override
    public void startGame( GameEngine engine, int playerID ) {
        engine.statistics.level = 0;

        nextSectionLevel = NEXT_SECTION_LEVELS.apply(engine.statistics.level);
        setNewBackground(BACKGROUND_TABLE.apply(engine.statistics.level));

        owner.backgroundStatus.bg = -1;
        owner.backgroundStatus.fadebg = -1;

        engine.ghost = true;

        engine.big = false;
        engine.bigmove = true;
        engine.bighalf = true;

        engine.tspinEnable = true;
        engine.useAllSpinBonus = true;
        engine.tspinAllowKick = true;
        engine.spinCheckType = GameEngine.SPINTYPE_4POINT;
        engine.tspinminiType = GameEngine.TSPINMINI_TYPE_ROTATECHECK;

        engine.b2bEnable = true;

        engine.staffrollEnableStatistics = false;

        naturalLevelIncrement = 1;

        setSpeed(engine);
        owner.bgmStatus.bgm = BGM_TABLE.apply(engine.statistics.level);
    }

    @Override
    public void inPieceSpawn(GameEngine engine, int playerID) {
        // 出現時の処理
        if (engine.statc[0] == 0) {
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
                if ((engine.ruleopt.holdResetDirection) && (engine.ruleopt.pieceDefaultDirection[engine.holdPieceObject.id] < Piece.DIRECTION_COUNT)) {
                    engine.holdPieceObject.direction = engine.ruleopt.pieceDefaultDirection[engine.holdPieceObject.id];
                    engine.holdPieceObject.updateConnectData();
                }

                // 使用した count+1
                engine.holdUsedCount++;
                engine.statistics.totalHoldUsed++;

                // ホールド無効化
                engine.initialHoldFlag = false;
                engine.holdDisable = true;
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

            engine.getNextObject(engine.nextPieceCount + engine.ruleopt.nextDisplay - 1).setAttribute(Block.BLOCK_ATTRIBUTE_BONE, engine.bone);

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
            }

            return inPostLockProcessing(engine, playerID, instantlock);
        }
        return false;
    }

    @Override
    public boolean onMove(GameEngine engine, int playerID) {
        if ((engine.ending == 0) && (engine.statc[0] == 0) && (!engine.holdDisable) && (!levelUpFlag)) {
            if (engine.statistics.level < nextSectionLevel - 1) {
                engine.statistics.level = Math.min(engine.statistics.level + naturalLevelIncrement, nextSectionLevel - 1);

                if (engine.statistics.level == nextSectionLevel - 1)
                    engine.playSE("levelstop");
            }

            levelUp(engine);
        }

        if (engine.statc[0] > 0) {
            levelUpFlag = false;
        }

        return inOnMove(engine, playerID);
    }

    @Override
    public boolean onARE(GameEngine engine, int playerID) {
        if (engine.statc[0] == 0 && queuedFreefall && engine.gameStarted) {
            queuedFreefall = false;

            engine.stat = GameEngine.STAT_CUSTOM;
            engine.resetStatc();

            return true;
        }

        if ((engine.ending == 0 && (engine.statc[0] >= engine.statc[1] - 1) && (!levelUpFlag))) {
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

    private static int abilityCharge(GameEngine engine, int baseCharge) {
        return engine.tspin ? 5 * baseCharge : baseCharge;
    }

    @Override
    public void calcScore(GameEngine engine, int playerID, int lines) {
        if (lines >= 1) {
            if (engine.field.isEmpty()) {
                engine.playSE("bravo");
            }

            int levelIncrease = 0;

            if (lines >= 4) {
                levelIncrease += 3 * lines * naturalLevelIncrement;

                if (settings.perk.isActive()) {
                    currentEnergy = Math.min(settings.perk.energyStore, currentEnergy + abilityCharge(engine, settings.perk.restoredForFour));
                }

                if (lines > 4) {
                    engine.playSE("erase4");
                }
            } else if (lines == 3) {
                levelIncrease += 6 * naturalLevelIncrement;

                if (settings.perk.isActive()) {
                    currentEnergy = Math.min(settings.perk.energyStore, currentEnergy + abilityCharge(engine, settings.perk.restoredForTriple));
                }
            } else if (lines == 2) {
                levelIncrease += 3 * naturalLevelIncrement;

                if (settings.perk.isActive()) {
                    currentEnergy = Math.min(settings.perk.energyStore, currentEnergy + abilityCharge(engine, settings.perk.restoredForDouble));
                }
            } else {
                levelIncrease += naturalLevelIncrement;

                if (settings.perk.isActive()) {
                    currentEnergy = Math.min(settings.perk.energyStore, currentEnergy + abilityCharge(engine, settings.perk.restoredForSingle));
                }
            }

            levelIncrease += badges.getLevelBonus() * naturalLevelIncrement;

            if (settings.perk == SeasonPerk.SPRING_PASSIVE) {
                engine.statistics.level += levelIncrease + naturalLevelIncrement;
            } else if (settings.perk == SeasonPerk.SPRING_ACTIVE) {
                engine.statistics.level += levelIncrease * 2;
            } else {
                engine.statistics.level += levelIncrease;
            }

            levelUp(engine);
            badges.updateBadges(
                engine, lines,
                0,
                settings.perk == SeasonPerk.SPRING_PASSIVE,
                settings.perk == SeasonPerk.SPRING_ACTIVE && currentAbilityTimer > 0
            );

            // TODO: Temporary levelling. Add real ending later.
            if (engine.statistics.level >= LEVELS_JAN) {
                // ADD REAL ENDING LATER
                engine.ending = 1;
                engine.gameEnded();
            } else if (engine.statistics.level >= nextSectionLevel) {
                engine.playSE("levelup");

                setNewBackground(BACKGROUND_TABLE.apply(engine.statistics.level));

                currentSeason = SEASON_TABLE.apply(engine.statistics.level);

                if (owner.bgmStatus.bgm <= BGM_TABLE.apply(engine.statistics.level)) {
                    owner.bgmStatus.fadesw = false;
                    BGM_TABLE.apply(engine.statistics.level);

                    if (NEXT_SECTION_LEVELS.apply(engine.statistics.level) > nextSectionLevel) {
                        nextSectionLevel = NEXT_SECTION_LEVELS.apply(engine.statistics.level);
                    }
                }
            } else if (engine.statistics.level == nextSectionLevel - 1) {
                engine.playSE("levelstop");
            }
        }

    }

    private void levelUp(GameEngine engine) {
        if (!settings.perk.isActive()) {
            final double proportion = engine.statistics.level / (double) LEVELS_JAN;

            engine.meterValue = (int) Math.floor(receiver.getMeterMax(engine) * proportion);

            engine.meterColor = GameEngine.METER_COLOR_RED;
            if (proportion >= 0.75) engine.meterColor = GameEngine.METER_COLOR_GREEN;
            else if (proportion >= 0.5) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
            else if (proportion >= 0.25) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
        }

        setSpeed( engine );

        if ((engine.statistics.level >= LEVELS_APR) && (!settings.fullGhost)) engine.ghost = false;

        final int fadeout = BGM_FADE_LEVEL_TABLE.apply(engine.statistics.level);
        if ((fadeout > 0) && (engine.statistics.level >= fadeout)) {
            owner.bgmStatus.fadesw = true;
        }
    }

    @Override
    public void renderFirst(GameEngine engine, int playerID) {
        inRenderFirst(rendererExtension, receiver, engine, playerID);
    }

    @Override
    public void renderMove(GameEngine engine, int playerID) {
        inRenderMove(rendererExtension, receiver, engine, playerID);
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

    @Override
    public void onLast(GameEngine engine, int playerID) {
        updateFadeProgress();

        if (currentAbilityTimer > 0 && engine.stat == GameEngine.STAT_MOVE) {
            final int prevTimer = currentAbilityTimer--;

            if (settings.perk == SeasonPerk.WINTER_ACTIVE && prevTimer == 1) {
                setSpeed(engine);
                engine.lockDelayNow = (int) Math.floor((engine.lockDelayNow * engine.getLockDelay()) / 180d);
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
            if (proportion >= 0.75) engine.meterColor = GameEngine.METER_COLOR_GREEN;
            else if (proportion >= 0.5) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
            else if (proportion >= 0.25) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
        }

        if (engine.gameActive && settings.perk.isActive() && currentAbilityTimer > 0) {
            engine.framecolor = GameEngine.FRAME_COLOR_PINK;
        } else if (engine.gameActive) {
            engine.framecolor = currentSeason.defaultFrameColour;
        } else {
            engine.framecolor = Season.defaultMenuFrameColour();
        }

        if (!engine.lagStop && engine.gameStarted && engine.ctrl.isPush(Controller.BUTTON_F) && currentEnergy >= settings.perk.energyStore) {
            currentEnergy = 0;
            currentAbilityTimer = settings.perk.duration;

            if (settings.perk == SeasonPerk.AUTUMN_ACTIVE) queuedFreefall = true;
            else if (settings.perk == SeasonPerk.SUMMER_ACTIVE) {
                HasCustomOnMove.insertIntoNexts(engine, engine.nextPieceCount, Piece.PIECE_I, Piece.PIECE_I);
            }
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
        final int titlesColour = EventReceiver.COLOR_YELLOW;

        receiver.drawScoreFont(engine, playerID, 0, 0, getName(), titlesColour);

        if (engine.stat == GameEngine.STAT_SETTING || (engine.stat == GameEngine.STAT_RESULT && !owner.replayMode)) {
            final float scale = (receiver.getNextDisplayType() == 2) ? 0.5f : 1.0f;
            final int topY = (receiver.getNextDisplayType() == 2) ? 5 : 3;
            final boolean showRankings = !owner.replayMode && !settings.fullGhost;

            if (showRankings) {
                receiver.drawScoreFont(engine, playerID, 3, topY - 1, "RN&TI DATE  TIME", titlesColour, scale);
                for (int i = 0; i < SeasonsSettings.RANKING_MAX; ++i) {
                    receiver.drawScoreFont(engine, playerID, 0, topY + i, String.format("%2d N/A   N/A   N/A", i + 1));
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
            receiver.drawScoreFont(engine, playerID, 0, 2, "RANK & TITLE", titlesColour);
            receiver.drawScoreFont(engine, playerID, 0, 3, "N/A");

            receiver.drawScoreFont(engine, playerID, 0, 5, "SCORE", titlesColour);
            receiver.drawScoreFont(engine, playerID, 0, 6, "N/A");

            receiver.drawScoreFont(engine, playerID, 0, 8, "DATE", titlesColour);
            receiver.drawScoreFont(engine, playerID, 0, 9, levelToString(engine.statistics.level));

            receiver.drawScoreFont(engine, playerID, 0, 11, "TIME", titlesColour);
            receiver.drawScoreFont(engine, playerID, 0, 12, GeneralUtil.getTime(engine.statistics.time));

            receiver.drawScoreFont(engine, playerID, 0, 14, "PERK", titlesColour);
            receiver.drawScoreFont(engine, playerID, 0, 15, settings.perk.getName(), currentAbilityTimer > 0);

            receiver.drawScoreFont(engine, playerID, 0, 17, "BADGES", titlesColour);
            GameTextUtilities.drawAlignedScoreTextBlock(
                receiver, engine, playerID, false,
                0, 18, false,
                badges.getBadgeDisplay(false),
                ObjectAlignment.TOP_LEFT
            );
        }
    }

    @Override
    public void saveReplay(GameEngine engine, int playerID, CustomProperties prop) {
        settings.saveSetting(owner.replayProp, true);

        // If they have completed the game, set the completion status to true for their profile.
        if (playerProperties.isLoggedIn() && engine.ending > 0 && engine.statistics.level >= LEVELS_JAN) {
            settings.hasCompletedGame = true;
            settings.saveSettingPlayer(playerProperties);
        }

        // TODO: Rankings
    }
}
