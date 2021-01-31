package zeroxfc.nullpo.custom.modes;

import java.util.ArrayList;
import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.component.RuleOptions;
import mu.nu.nullpo.game.component.WallkickResult;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.game.subsystem.mode.DummyMode;
import mu.nu.nullpo.util.CustomProperties;
import mu.nu.nullpo.util.GeneralUtil;
import zeroxfc.nullpo.custom.libs.Interpolation;
import zeroxfc.nullpo.custom.libs.ProfileProperties;
import zeroxfc.nullpo.custom.libs.RendererExtension;
import zeroxfc.nullpo.custom.wallkick.TetrisEXWallkick;

public class EXReborn extends DummyMode {
    private static final int[] tableARE = {
        25, 25, 25, 25, 25, 20, 20, 20, 15, 15, 20, 15, 12, 10, 10, 8, 8, 8, 6, 6, 10
    };

    private static final int[] tableLineARE = {
        25, 25, 25, 25, 25, 20, 20, 20, 15, 15, 20, 15, 12, 10, 10, 8, 8, 8, 6, 6, 10
    };

    private static final int[] tableLineDelay = {
        35, 25, 25, 20, 20, 15, 15, 15, 15, 15, 12, 12, 12, 6, 6, 4, 4, 4, 4, 4, 4
    };

    private static final int[] tableGravity = {
        2, 4, 8, 16, 30, 60, 120, 360, 600, 1200, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
    };

    private static final int[] tableLockDelay = {
        40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 33, 28, 24, 20, 18, 15, 12, 10, 8, 20
    };

    private static final int[] tableDAS = {
        12, 12, 12, 12, 12, 10, 10, 10, 10, 10, 8, 8, 8, 8, 8, 6, 6, 6, 5, 5, 5
    };

    private static final int[] tableBGMFadeout = {
        490, 990, 1490, 1990
    };

    private static final int[] tableBGMChange = {
        500, 1000, 1500, 2000
    };

    private static final int gravityDenominator = 120;

    private static final int maxLevel = 2000;

    private static final int maxSection = 20;

    private static final int RANKING_MAX = 10;

    private static final int CURRENT_VERSION = 2;
    private static final int headerColour = EventReceiver.COLOR_BLUE;
    /**
     * Rankings' scores
     */
    private int[] rankingScore;
    /**
     * Rankings' level
     */
    private int[] rankingLevel;
    /**
     * Rankings' times
     */
    private int[] rankingTime;
    /**
     * The good hard drop effect
     */
    private ArrayList<int[]> pCoordList;
    private Piece cPiece;
    private GameManager owner;
    private EventReceiver receiver;
    private int rankingRank;
    private int comboValue;
    private boolean lvstopse, big, lvupflag, alwaysghost, greying;
    private int lastscore, previousscore, scgettime, bgmlv;
    private int nextseclv;
    // private int totalFall;
    private int version;
    private ProfileProperties playerProperties;
    private boolean showPlayerStats;
    private String PLAYER_NAME;
    private int rankingRankPlayer;

    /**
     * Rankings' scores
     */
    private int[] rankingScorePlayer;

    /**
     * Rankings' level
     */
    private int[] rankingLevelPlayer;

    /**
     * Rankings' times
     */
    private int[] rankingTimePlayer;

    /**
     * Mode name
     */
    @Override
    public String getName() {
        return "EX REBORN";
    }

    @Override
    public void playerInit(GameEngine engine, int playerID) {
        owner = engine.owner;
        receiver = engine.owner.receiver;

        if (playerProperties == null) {
            playerProperties = new ProfileProperties(headerColour);

            showPlayerStats = false;
        }

        rankingRankPlayer = -1;
        rankingScorePlayer = new int[RANKING_MAX];
        rankingLevelPlayer = new int[RANKING_MAX];
        rankingTimePlayer = new int[RANKING_MAX];

        rankingScore = new int[RANKING_MAX];
        rankingLevel = new int[RANKING_MAX];
        rankingTime = new int[RANKING_MAX];

        engine.speed.denominator = gravityDenominator;
        engine.speed.gravity = 2;
        rankingRank = -1;
        lvstopse = true;
        big = false;
        lvupflag = false;
        greying = false;
        nextseclv = 0;
        comboValue = 1;
        bgmlv = 0;
        // totalFall = 0;

        pCoordList = new ArrayList<>();
        cPiece = null;

        engine.bighalf = true;
        engine.bigmove = true;

        if (!owner.replayMode) {
            loadSetting(owner.modeConfig);
            loadRanking(owner.modeConfig, engine.ruleopt.strRuleName);

            if (playerProperties.isLoggedIn()) {
                loadSettingPlayer(playerProperties);
                loadRankingPlayer(playerProperties, engine.ruleopt.strRuleName);
            }

            version = CURRENT_VERSION;
            PLAYER_NAME = "";
        } else {
            loadSetting(owner.replayProp);
            PLAYER_NAME = owner.replayProp.getProperty("exreborn.playerName", "");
        }
    }

    /*
     * Called at settings screen
     */
    @Override
    public boolean onSetting(GameEngine engine, int playerID) {
        engine.framecolor = GameEngine.FRAME_COLOR_GRAY;
        // Menu
        if (engine.owner.replayMode == false) {
            // Configuration changes
            int change = updateCursor(engine, 2, playerID);

            if (change != 0) {
                engine.playSE("change");

                switch (engine.statc[2]) {
                    case 0:
                        lvstopse = !lvstopse;
                        break;
                    case 1:
                        alwaysghost = !alwaysghost;
                        break;
                    case 2:
                        big = !big;
                        break;
                }
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

    /*
     * Render the settings screen
     */
    @Override
    public void renderSetting(GameEngine engine, int playerID) {
        drawMenu(engine, playerID, receiver, 0, EventReceiver.COLOR_BLUE, 0,
            "LVSTOPSE", GeneralUtil.getONorOFF(lvstopse),
            "FULL GHOST", GeneralUtil.getONorOFF(alwaysghost),
            "BIG", GeneralUtil.getONorOFF(big));
    }

    /*
     * Called at game start
     */
    @Override
    public void startGame(GameEngine engine, int playerID) {
        nextseclv = engine.statistics.level + 100;
        greying = false;

        owner.backgroundStatus.bg = engine.statistics.level / 100;

        bgmlv = 0;

        engine.big = big;

        setSpeed(engine);
        owner.bgmStatus.bgm = bgmlv;
    }

    /**
     * Set the gravity rate
     *
     * @param engine GameEngine
     */
    public void setSpeed(GameEngine engine) {
        int lv = engine.statistics.level / 100;

        if (lv < 0) lv = 0;
        if (lv > maxSection) lv = maxSection;

        engine.speed.gravity = tableGravity[lv];
        engine.speed.are = tableARE[lv];
        engine.speed.areLine = tableLineARE[lv];
        engine.speed.das = tableDAS[lv];
        engine.speed.lockDelay = tableLockDelay[lv];
        engine.speed.lineDelay = tableLineDelay[lv];
    }

    // Wallkick algorithm is modifed such that if the rulename contains "T-EX", it enables Tetris-EX's special movement-based wallkick.
    // !! IMPORTANT: Make sure rule contains T-EX in its name.
    /*
     * Rule names:
     *
     * T-EX-CLASSIC
     * T-EX-MODERN
     */

    // NOTE: above comment is void as a replacement detection will be made using the wallkick class name.
    @Override
    public boolean onMove(GameEngine engine, int playerID) {
        if ((engine.ending == 0) && (engine.statc[0] == 0) && (engine.holdDisable == false) && (!lvupflag)) {
            if (engine.statistics.level < nextseclv - 1) {
                engine.statistics.level++;
                if ((engine.statistics.level == nextseclv - 1) && (lvstopse == true))
                    engine.playSE("levelstop");
            }
            levelUp(engine);
        }
        if ((engine.ending == 0) && (engine.statc[0] > 0) && ((engine.holdDisable == false))) {
            lvupflag = false;
        }

        // 横溜めInitialization
        int moveDirection = engine.getMoveDirection();

        if ((engine.statc[0] > 0) || (engine.ruleopt.dasInMoveFirstFrame)) {
            if (engine.dasDirection != moveDirection) {
                engine.dasDirection = moveDirection;
                if (!(engine.dasDirection == 0 && engine.ruleopt.dasStoreChargeOnNeutral)) {
                    engine.dasCount = 0;
                }
            }
        }

        // 出現時の処理
        if (engine.statc[0] == 0) {
            if ((engine.statc[1] == 0) && (engine.initialHoldFlag == false)) {
                // 通常出現
                engine.nowPieceObject = engine.getNextObjectCopy(engine.nextPieceCount);
                engine.nextPieceCount++;
                if (engine.nextPieceCount < 0) engine.nextPieceCount = 0;
                engine.holdDisable = false;
            } else {
                // ホールド出現
                if (engine.initialHoldFlag) {
                    // 先行ホールド
                    if (engine.holdPieceObject == null) {
                        // 1回目
                        engine.holdPieceObject = engine.getNextObjectCopy(engine.nextPieceCount);
                        engine.holdPieceObject.applyOffsetArray(engine.ruleopt.pieceOffsetX[engine.holdPieceObject.id], engine.ruleopt.pieceOffsetY[engine.holdPieceObject.id]);
                        engine.nextPieceCount++;
                        if (engine.nextPieceCount < 0) engine.nextPieceCount = 0;

                        if (engine.bone)
                            engine.getNextObject(engine.nextPieceCount + engine.ruleopt.nextDisplay - 1).setAttribute(Block.BLOCK_ATTRIBUTE_BONE, true);

                        // New code to replace [] blocks
                        if (greying)
                            engine.getNextObject(engine.nextPieceCount + engine.ruleopt.nextDisplay - 1).setColor(Block.BLOCK_COLOR_GRAY);

                        engine.nowPieceObject = engine.getNextObjectCopy(engine.nextPieceCount);
                        engine.nextPieceCount++;
                        if (engine.nextPieceCount < 0) engine.nextPieceCount = 0;
                    } else {
                        // 2回目以降
                        Piece pieceTemp = engine.holdPieceObject;
                        engine.holdPieceObject = engine.getNextObjectCopy(engine.nextPieceCount);
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
                        engine.nowPieceObject = engine.getNextObjectCopy(engine.nextPieceCount);
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
            engine.playSE("piece" + engine.getNextObject(engine.nextPieceCount).id);

            if (engine.nowPieceObject.offsetApplied == false)
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
            //if( (getARE() != 0) && ((getARELine() != 0) || (version < 6.3f)) ) initialRotate();

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
            // New code to replace [] blocks
            if (greying)
                engine.getNextObject(engine.nextPieceCount + engine.ruleopt.nextDisplay - 1).setColor(Block.BLOCK_COLOR_GRAY);

            if (engine.ending == 0) engine.timerActive = true;

            if ((engine.ai != null) && (!engine.owner.replayMode || engine.owner.replayRerecord))
                engine.ai.newPiece(engine, playerID);
        }

        engine.checkDropContinuousUse();

        boolean softdropUsed = false; // この frame にSoft dropを使ったらtrue
        int softdropFallNow = 0; // この frame のSoft dropで落下した段count

        boolean updown = false; // Up下同時押し flag
        if (engine.ctrl.isPress(engine.getUp()) && engine.ctrl.isPress(engine.getDown())) updown = true;

        if (!engine.dasInstant) {

            // ホールド
            if (engine.ctrl.isPush(Controller.BUTTON_D) || engine.initialHoldFlag) {
                if (engine.isHoldOK()) {
                    engine.statc[0] = 0;
                    engine.statc[1] = 1;
                    if (!engine.initialHoldFlag) engine.playSE("hold");
                    engine.initialHoldContinuousUse = true;
                    engine.initialHoldFlag = false;
                    engine.holdDisable = true;
                    engine.initialRotate(); //Hold swap triggered IRS
                    engine.statMove();
                    return true;
                } else if ((engine.statc[0] > 0) && (!engine.initialHoldFlag)) {
                    engine.playSE("holdfail");
                }
            }

            // rotation
            boolean onGroundBeforeRotate = engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY + 1, engine.field);
            int move = 0;
            boolean rotated = false;

            if (engine.initialRotateDirection != 0) {
                move = engine.initialRotateDirection;
                engine.initialRotateLastDirection = engine.initialRotateDirection;
                engine.initialRotateContinuousUse = true;
                engine.playSE("initialrotate");
            } else if ((engine.statc[0] > 0) || (engine.ruleopt.moveFirstFrame == true)) {
                if ((engine.itemRollRollEnable) && (engine.replayTimer % engine.itemRollRollInterval == 0))
                    move = 1;    // Roll Roll

                //  button input
                if (engine.ctrl.isPush(Controller.BUTTON_A) || engine.ctrl.isPush(Controller.BUTTON_C)) move = -1;
                else if (engine.ctrl.isPush(Controller.BUTTON_B)) move = 1;
                else if (engine.ctrl.isPush(Controller.BUTTON_E)) move = 2;

                if (move != 0) {
                    engine.initialRotateLastDirection = move;
                    engine.initialRotateContinuousUse = true;
                }
            }

            if ((engine.ruleopt.rotateButtonAllowDouble == false) && (move == 2)) move = -1;
            if ((engine.ruleopt.rotateButtonAllowReverse == false) && (move == 1)) move = -1;
            if (engine.isRotateButtonDefaultRight() && (move != 2)) move = move * -1;

            if (move != 0) {
                // Direction after rotationを決める
                int rt = engine.getRotateDirection(move);

                // rotationできるか判定
                if ((!(engine.wallkick.getClass().getCanonicalName().contains("TetrisEXWallkick")) || engine.initialRotateDirection != 0) && engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY, rt, engine.field) == false) {
                    // Wallkickなしでrotationできるとき
                    rotated = true;
                    engine.kickused = false;
                    engine.nowPieceObject.direction = rt;
                    engine.nowPieceObject.updateConnectData();
                } else if ((engine.ruleopt.rotateWallkick == true) &&
                    (engine.wallkick != null) &&
                    ((engine.initialRotateDirection == 0) || (engine.ruleopt.rotateInitialWallkick == true)) &&
                    ((engine.ruleopt.lockresetLimitOver != RuleOptions.LOCKRESET_LIMIT_OVER_NOWALLKICK) || (engine.isRotateCountExceed() == false))) {
                    if (engine.wallkick instanceof TetrisEXWallkick) {
                        ((TetrisEXWallkick) engine.wallkick).currentDASCharge = engine.dasCount;
                        ((TetrisEXWallkick) engine.wallkick).maxDASCharge = engine.getDAS();
                        ((TetrisEXWallkick) engine.wallkick).dasDirection = engine.dasDirection;
                    }

                    // Wallkickを試みる
                    boolean allowUpward = (engine.ruleopt.rotateMaxUpwardWallkick < 0) || (engine.nowUpwardWallkickCount < engine.ruleopt.rotateMaxUpwardWallkick);
                    WallkickResult kick = engine.wallkick.executeWallkick(engine.nowPieceX, engine.nowPieceY, move, engine.nowPieceObject.direction, rt,
                        allowUpward, engine.nowPieceObject, engine.field, engine.ctrl);

                    if (kick != null) {
                        rotated = true;
                        engine.kickused = true;
                        engine.nowWallkickCount++;
                        if (kick.isUpward()) engine.nowUpwardWallkickCount++;
                        engine.nowPieceObject.direction = kick.direction;
                        engine.nowPieceObject.updateConnectData();
                        engine.nowPieceX += kick.offsetX;
                        engine.nowPieceY += kick.offsetY;

                        if (engine.ruleopt.lockresetWallkick && !engine.isRotateCountExceed()) {
                            engine.lockDelayNow = 0;
                            engine.nowPieceObject.setDarkness(0f);
                        }
                    }
                }

                // Domino Quick Turn
                if (!rotated && engine.dominoQuickTurn && (engine.nowPieceObject.id == Piece.PIECE_I2) && (engine.nowPieceRotateFailCount >= 1)) {
                    rt = engine.getRotateDirection(2);
                    rotated = true;
                    engine.nowPieceObject.direction = rt;
                    engine.nowPieceObject.updateConnectData();
                    engine.nowPieceRotateFailCount = 0;

                    if (engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY, rt, engine.field) == true) {
                        engine.nowPieceY--;
                    } else if (onGroundBeforeRotate) {
                        engine.nowPieceY++;
                    }
                }

                if (rotated == true) {
                    // rotation成功
                    engine.nowPieceBottomY = engine.nowPieceObject.getBottom(engine.nowPieceX, engine.nowPieceY, engine.field);

                    if ((engine.ruleopt.lockresetRotate == true) && (engine.isRotateCountExceed() == false)) {
                        engine.lockDelayNow = 0;
                        engine.nowPieceObject.setDarkness(0f);
                    }

                    if (onGroundBeforeRotate) {
                        engine.extendedRotateCount++;
                        engine.lastmove = GameEngine.LASTMOVE_ROTATE_GROUND;
                    } else {
                        engine.lastmove = GameEngine.LASTMOVE_ROTATE_AIR;
                    }

                    if (engine.initialRotateDirection == 0) {
                        engine.playSE("rotate");
                    }

                    engine.nowPieceRotateCount++;
                    if ((engine.ending == 0) || (engine.staffrollEnableStatistics))
                        engine.statistics.totalPieceRotate++;
                } else {
                    // rotation失敗
                    engine.playSE("rotfail");
                    engine.nowPieceRotateFailCount++;
                }
            }
            engine.initialRotateDirection = 0;

            // game over check
            if ((engine.statc[0] == 0) && (engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY, engine.field) == true)) {
                // Blockの出現位置を上にずらすことができる場合はそうする
                for (int i = 0; i < engine.ruleopt.pieceEnterMaxDistanceY; i++) {
                    if (engine.nowPieceObject.big) engine.nowPieceY -= 2;
                    else engine.nowPieceY--;

                    if (engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY, engine.field) == false) {
                        engine.nowPieceBottomY = engine.nowPieceObject.getBottom(engine.nowPieceX, engine.nowPieceY, engine.field);
                        break;
                    }
                }

                // 死亡
                if (engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY, engine.field) == true) {
                    engine.nowPieceObject.placeToField(engine.nowPieceX, engine.nowPieceY, engine.field);
                    engine.nowPieceObject = null;

                    if (engine.statistics.level >= 2000) {
                        engine.stat = GameEngine.STAT_EXCELLENT;
                    } else {
                        engine.stat = GameEngine.STAT_GAMEOVER;
                    }
                    if ((engine.ending == 2) && (engine.staffrollNoDeath)) engine.stat = GameEngine.STAT_NOTHING;
                    engine.resetStatc();
                    return true;
                }
            }

        }

        int move = 0;
        boolean sidemoveflag = false;    // この frame に横移動したらtrue

        if ((engine.statc[0] > 0) || (engine.ruleopt.moveFirstFrame == true)) {
            // 横移動
            boolean onGroundBeforeMove = engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY + 1, engine.field);

            move = moveDirection;

            if (engine.statc[0] == 0 && engine.delayCancel) {
                if (engine.delayCancelMoveLeft) move = -1;
                if (engine.delayCancelMoveRight) move = 1;
                engine.dasCount = 0;
                // delayCancel = false;
                engine.delayCancelMoveLeft = false;
                engine.delayCancelMoveRight = false;
            } else if (engine.statc[0] == 1 && engine.delayCancel && (engine.dasCount < engine.getDAS())) {
                move = 0;
                engine.delayCancel = false;
            }

            if (move != 0) sidemoveflag = true;

            if (engine.big && engine.bigmove) move *= 2;

            if ((move != 0) && (engine.dasCount == 0)) engine.shiftLock = 0;

            if ((move != 0) && ((engine.dasCount == 0) || (engine.dasCount >= engine.getDAS()))) {
                engine.shiftLock &= engine.ctrl.getButtonBit();

                if (engine.shiftLock == 0) {
                    if ((engine.dasSpeedCount >= engine.getDASDelay()) || (engine.dasCount == 0)) {
                        if (engine.dasCount > 0) engine.dasSpeedCount = 1;

                        if (engine.nowPieceObject.checkCollision(engine.nowPieceX + move, engine.nowPieceY, engine.field) == false) {
                            engine.nowPieceX += move;

                            if ((engine.getDASDelay() == 0) && (engine.dasCount > 0) && (engine.nowPieceObject.checkCollision(engine.nowPieceX + move, engine.nowPieceY, engine.field) == false)) {
                                if (!engine.dasInstant) engine.playSE("move");
                                engine.dasRepeat = true;
                                engine.dasInstant = true;
                            }

                            //log.debug("Successful movement: move="+move);

                            if ((engine.ruleopt.lockresetMove == true) && (engine.isMoveCountExceed() == false)) {
                                engine.lockDelayNow = 0;
                                engine.nowPieceObject.setDarkness(0f);
                            }

                            engine.nowPieceMoveCount++;
                            if ((engine.ending == 0) || (engine.staffrollEnableStatistics))
                                engine.statistics.totalPieceMove++;
                            engine.nowPieceBottomY = engine.nowPieceObject.getBottom(engine.nowPieceX, engine.nowPieceY, engine.field);

                            if (onGroundBeforeMove) {
                                engine.extendedMoveCount++;
                                engine.lastmove = GameEngine.LASTMOVE_SLIDE_GROUND;
                            } else {
                                engine.lastmove = GameEngine.LASTMOVE_SLIDE_AIR;
                            }

                            if (!engine.dasInstant) engine.playSE("move");

                        } else if (engine.ruleopt.dasChargeOnBlockedMove) {
                            engine.dasCount = engine.getDAS();
                            engine.dasSpeedCount = engine.getDASDelay();
                        }
                    } else {
                        engine.dasSpeedCount++;
                    }
                }
            }

            // Hard drop
            if ((engine.ctrl.isPress(engine.getUp()) == true) &&
                (engine.harddropContinuousUse == false) &&
                (engine.ruleopt.harddropEnable == true) &&
                ((engine.isDiagonalMoveEnabled() == true) || (sidemoveflag == false)) &&
                ((engine.ruleopt.moveUpAndDown == true) || (updown == false)) &&
                (engine.nowPieceY < engine.nowPieceBottomY)) {
                engine.harddropFall += engine.nowPieceBottomY - engine.nowPieceY;

                if (engine.nowPieceY != engine.nowPieceBottomY) {
                    engine.nowPieceY = engine.nowPieceBottomY;
                    engine.playSE("harddrop");
                }

                if (engine.owner.mode != null)
                    engine.owner.mode.afterHardDropFall(engine, playerID, engine.harddropFall);
                engine.owner.receiver.afterHardDropFall(engine, playerID, engine.harddropFall);

                engine.lastmove = GameEngine.LASTMOVE_FALL_SELF;
                if (engine.ruleopt.lockresetFall == true) {
                    engine.lockDelayNow = 0;
                    engine.nowPieceObject.setDarkness(0f);
                    engine.extendedMoveCount = 0;
                    engine.extendedRotateCount = 0;
                }
            }

            if (!engine.ruleopt.softdropGravitySpeedLimit || (engine.ruleopt.softdropSpeed < 1.0f)) {
                // Old Soft Drop codes
                if ((engine.ctrl.isPress(engine.getDown()) == true) &&
                    (engine.softdropContinuousUse == false) &&
                    (engine.ruleopt.softdropEnable == true) &&
                    ((engine.isDiagonalMoveEnabled() == true) || (sidemoveflag == false)) &&
                    ((engine.ruleopt.moveUpAndDown == true) || (updown == false))) {
                    if ((engine.ruleopt.softdropMultiplyNativeSpeed == true) || (engine.speed.denominator <= 0))
                        engine.gcount += (int) (engine.speed.gravity * engine.ruleopt.softdropSpeed);
                    else
                        engine.gcount += (int) (engine.speed.denominator * engine.ruleopt.softdropSpeed);

                    softdropUsed = true;
                }
            } else {
                // New Soft Drop codes
                if (engine.ctrl.isPress(engine.getDown()) && !engine.softdropContinuousUse &&
                    engine.ruleopt.softdropEnable && (engine.isDiagonalMoveEnabled() || !sidemoveflag) &&
                    (engine.ruleopt.moveUpAndDown || !updown) &&
                    (engine.ruleopt.softdropMultiplyNativeSpeed || (engine.speed.gravity < (int) (engine.speed.denominator * engine.ruleopt.softdropSpeed)))) {
                    if ((engine.ruleopt.softdropMultiplyNativeSpeed == true) || (engine.speed.denominator <= 0)) {
                        // gcount += (int)(speed.gravity * ruleopt.softdropSpeed);
                        engine.gcount = (int) (engine.speed.gravity * engine.ruleopt.softdropSpeed);
                    } else {
                        // gcount += (int)(speed.denominator * ruleopt.softdropSpeed);
                        engine.gcount = (int) (engine.speed.denominator * engine.ruleopt.softdropSpeed);
                    }

                    softdropUsed = true;
                } else {
                    // 落下
                    // This prevents soft drop from adding to the gravity speed.
                    engine.gcount += engine.speed.gravity;
                }
            }

            if ((engine.ending == 0) || (engine.staffrollEnableStatistics))
                engine.statistics.totalPieceActiveTime++;
        }

        if (!engine.ruleopt.softdropGravitySpeedLimit || (engine.ruleopt.softdropSpeed < 1.0f))
            engine.gcount += engine.speed.gravity;    // Part of Old Soft Drop

        while ((engine.gcount >= engine.speed.denominator) || (engine.speed.gravity < 0)) {
            if (engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY + 1, engine.field) == false) {
                if (engine.speed.gravity >= 0) engine.gcount -= engine.speed.denominator;
                engine.nowPieceY++;

                if (engine.ruleopt.lockresetFall == true) {
                    engine.lockDelayNow = 0;
                    engine.nowPieceObject.setDarkness(0f);
                }

                if ((engine.lastmove != GameEngine.LASTMOVE_ROTATE_GROUND) && (engine.lastmove != GameEngine.LASTMOVE_SLIDE_GROUND) && (engine.lastmove != GameEngine.LASTMOVE_FALL_SELF)) {
                    engine.extendedMoveCount = 0;
                    engine.extendedRotateCount = 0;
                }

                if (softdropUsed == true) {
                    engine.lastmove = GameEngine.LASTMOVE_FALL_SELF;
                    engine.softdropFall++;
                    softdropFallNow++;
                    engine.playSE("softdrop");
                } else {
                    engine.lastmove = GameEngine.LASTMOVE_FALL_AUTO;
                }
            } else {
                break;
            }
        }

        if (softdropFallNow > 0) {
            if (engine.owner.mode != null) engine.owner.mode.afterSoftDropFall(engine, playerID, softdropFallNow);
            engine.owner.receiver.afterSoftDropFall(engine, playerID, softdropFallNow);
        }

        // 接地と固定
        if ((engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY + 1, engine.field) == true) &&
            ((engine.statc[0] > 0) || (engine.ruleopt.moveFirstFrame == true))) {
            if ((engine.lockDelayNow == 0) && (engine.getLockDelay() > 0))
                engine.playSE("step");

            if (engine.lockDelayNow < engine.getLockDelay())
                engine.lockDelayNow++;

            if ((engine.getLockDelay() >= 99) && (engine.lockDelayNow > 98))
                engine.lockDelayNow = 98;

            if (engine.lockDelayNow < engine.getLockDelay()) {
                if (engine.lockDelayNow >= engine.getLockDelay() - 1)
                    engine.nowPieceObject.setDarkness(0.5f);
                else
                    engine.nowPieceObject.setDarkness((engine.lockDelayNow * 7 / engine.getLockDelay()) * 0.05f);
            }

            if (engine.getLockDelay() != 0)
                engine.gcount = engine.speed.gravity;

            // trueになると即固定
            boolean instantlock = false;

            // Hard drop固定
            if ((engine.ctrl.isPress(engine.getUp()) == true) &&
                (engine.harddropContinuousUse == false) &&
                (engine.ruleopt.harddropEnable == true) &&
                ((engine.isDiagonalMoveEnabled() == true) || (sidemoveflag == false)) &&
                ((engine.ruleopt.moveUpAndDown == true) || (updown == false)) &&
                (engine.ruleopt.harddropLock == true)) {
                engine.harddropContinuousUse = true;
                engine.manualLock = true;
                instantlock = true;
            }

            // Soft drop固定
            if ((engine.ctrl.isPress(engine.getDown()) == true) &&
                (engine.softdropContinuousUse == false) &&
                (engine.ruleopt.softdropEnable == true) &&
                ((engine.isDiagonalMoveEnabled() == true) || (sidemoveflag == false)) &&
                ((engine.ruleopt.moveUpAndDown == true) || (updown == false)) &&
                (engine.ruleopt.softdropLock == true)) {
                engine.softdropContinuousUse = true;
                engine.manualLock = true;
                instantlock = true;
            }

            // 接地状態でソフドドロップ固定
            if ((engine.ctrl.isPush(engine.getDown()) == true) &&
                (engine.ruleopt.softdropEnable == true) &&
                ((engine.isDiagonalMoveEnabled() == true) || (sidemoveflag == false)) &&
                ((engine.ruleopt.moveUpAndDown == true) || (updown == false)) &&
                (engine.ruleopt.softdropSurfaceLock == true)) {
                engine.softdropContinuousUse = true;
                engine.manualLock = true;
                instantlock = true;
            }

            if ((engine.manualLock == true) && (engine.ruleopt.shiftLockEnable)) {
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

            // 固定
            if (((engine.lockDelayNow >= engine.getLockDelay()) && (engine.getLockDelay() > 0)) || (instantlock == true)) {
                if (engine.ruleopt.lockflash > 0) engine.nowPieceObject.setDarkness(-0.8f);

				/*if((lastmove == LASTMOVE_ROTATE_GROUND) && (tspinEnable == true)) {

					tspinmini = false;

					// T-Spin Mini判定

					if(!useAllSpinBonus) {
						if(spinCheckType == SPINTYPE_4POINT) {
							if(tspinminiType == TSPINMINI_TYPE_ROTATECHECK) {
								if(nowPieceObject.checkCollision(nowPieceX, nowPieceY, getRotateDirection(-1), field) &&
								   nowPieceObject.checkCollision(nowPieceX, nowPieceY, getRotateDirection( 1), field))
									tspinmini = true;
							} else if(tspinminiType == TSPINMINI_TYPE_WALLKICKFLAG) {
								tspinmini = kickused;
							}
						} else if(spinCheckType == SPINTYPE_IMMOBILE) {
							Field copyField = new Field(field);
							nowPieceObject.placeToField(nowPieceX, nowPieceY, copyField);
							if((copyField.checkLineNoFlag() == 1) && (kickused == true)) tspinmini = true;
						}
					}
				}*/

                // T-Spin判定
                if ((engine.lastmove == GameEngine.LASTMOVE_ROTATE_GROUND) && (engine.tspinEnable == true)) {
                    if (engine.useAllSpinBonus)
                        engine.setAllSpin(engine.nowPieceX, engine.nowPieceY, engine.nowPieceObject, engine.field);
                    else
                        engine.setTSpin(engine.nowPieceX, engine.nowPieceY, engine.nowPieceObject, engine.field);
                }

                engine.nowPieceObject.setAttribute(Block.BLOCK_ATTRIBUTE_SELFPLACED, true);

                boolean partialLockOut = engine.nowPieceObject.isPartialLockOut(engine.nowPieceX, engine.nowPieceY, engine.field);
                boolean put = engine.nowPieceObject.placeToField(engine.nowPieceX, engine.nowPieceY, engine.field);

                engine.playSE("lock");

                engine.holdDisable = false;

                if ((engine.ending == 0) || (engine.staffrollEnableStatistics))
                    engine.statistics.totalPieceLocked++;

                if (engine.clearMode == GameEngine.CLEAR_LINE)
                    engine.lineClearing = engine.field.checkLineNoFlag();
                else if (engine.clearMode == GameEngine.CLEAR_COLOR)
                    engine.lineClearing = engine.field.checkColor(engine.colorClearSize, false, engine.garbageColorClear, engine.gemSameColor, engine.ignoreHidden);
                else if (engine.clearMode == GameEngine.CLEAR_LINE_COLOR)
                    engine.lineClearing = engine.field.checkLineColor(engine.colorClearSize, false, engine.lineColorDiagonals, engine.gemSameColor);
                else if (engine.clearMode == GameEngine.CLEAR_GEM_COLOR)
                    engine.lineClearing = engine.field.gemColorCheck(engine.colorClearSize, false, engine.garbageColorClear, engine.ignoreHidden);
                engine.chain = 0;
                engine.lineGravityTotalLines = 0;

                if (engine.lineClearing == 0) {
                    engine.combo = 0;

                    if (engine.tspin) {
                        engine.playSE("tspin0");

                        if ((engine.ending == 0) || (engine.staffrollEnableStatistics)) {
                            if (engine.tspinmini) engine.statistics.totalTSpinZeroMini++;
                            else engine.statistics.totalTSpinZero++;
                        }
                    }

                    if (engine.owner.mode != null)
                        engine.owner.mode.calcScore(engine, playerID, engine.lineClearing);
                    engine.owner.receiver.calcScore(engine, playerID, engine.lineClearing);
                }

                if (engine.owner.mode != null) engine.owner.mode.pieceLocked(engine, playerID, engine.lineClearing);
                engine.owner.receiver.pieceLocked(engine, playerID, engine.lineClearing);

                engine.dasRepeat = false;
                engine.dasInstant = false;

                // Next 処理を決める(Mode 側でステータスを弄っている場合は何もしない)
                if ((engine.stat == GameEngine.STAT_MOVE) || (engine.versionMajor <= 6.3f)) {
                    engine.resetStatc();

                    if ((engine.ending == 1) && (engine.versionMajor >= 6.6f) && (engine.versionMinorOld >= 0.1f)) {
                        // Ending
                        engine.stat = GameEngine.STAT_ENDINGSTART;
                    } else if ((!put && engine.ruleopt.fieldLockoutDeath) || (partialLockOut && engine.ruleopt.fieldPartialLockoutDeath)) {
                        // 画面外に置いて死亡
                        engine.stat = GameEngine.STAT_GAMEOVER;
                        if ((engine.ending == 2) && (engine.staffrollNoDeath))
                            engine.stat = GameEngine.STAT_NOTHING;
                    } else if ((engine.lineGravityType == GameEngine.LINE_GRAVITY_CASCADE || engine.lineGravityType == GameEngine.LINE_GRAVITY_CASCADE_SLOW)
                        && !engine.connectBlocks) {
                        engine.stat = GameEngine.STAT_LINECLEAR;
                        engine.statc[0] = engine.getLineDelay();
                        engine.statLineClear();
                    } else if ((engine.lineClearing > 0) && ((engine.ruleopt.lockflash <= 0) || (!engine.ruleopt.lockflashBeforeLineClear))) {
                        // Line clear
                        engine.stat = GameEngine.STAT_LINECLEAR;
                        engine.statLineClear();
                    } else if (((engine.getARE() > 0) || (engine.lagARE) || (engine.ruleopt.lockflashBeforeLineClear)) &&
                        (engine.ruleopt.lockflash > 0) && (engine.ruleopt.lockflashOnlyFrame)) {
                        // AREあり (光あり）
                        engine.stat = GameEngine.STAT_LOCKFLASH;
                    } else if ((engine.getARE() > 0) || (engine.lagARE)) {
                        // AREあり (光なし）
                        engine.statc[1] = engine.getARE();
                        engine.stat = GameEngine.STAT_ARE;
                    } else if (engine.interruptItemNumber != GameEngine.INTERRUPTITEM_NONE) {
                        // 中断効果のあるアイテム処理
                        engine.nowPieceObject = null;
                        engine.interruptItemPreviousStat = GameEngine.STAT_MOVE;
                        engine.stat = GameEngine.STAT_INTERRUPTITEM;
                    } else {
                        // AREなし
                        engine.stat = GameEngine.STAT_MOVE;
                        if (engine.ruleopt.moveFirstFrame == false) engine.statMove();
                    }
                }
                return true;
            }
        }

        // 横溜め
        if ((engine.statc[0] > 0) || (engine.ruleopt.dasInMoveFirstFrame)) {
            if ((moveDirection != 0) && (moveDirection == engine.dasDirection) && ((engine.dasCount < engine.getDAS()) || (engine.getDAS() <= 0))) {
                engine.dasCount++;
            }
        }

        engine.statc[0]++;
        return true;
    }

    /**
     * levelが上がったときの共通処理
     */
    private void levelUp(GameEngine engine) {
        // 速度変更
        setSpeed(engine);

        // LV100到達でghost を消す
        if ((engine.statistics.level >= 500) && (!alwaysghost)) engine.ghost = false;

        // BGM fadeout
        if (bgmlv < 4) {
            if (engine.statistics.level >= tableBGMFadeout[bgmlv]) {
                owner.bgmStatus.fadesw = true;
            }
        }
    }

    /*
     * Calculate score
     */
    @Override
    public void calcScore(GameEngine engine, int playerID, int lines) {
        if (engine.ending != 0) return;

        double effectiveSection = (engine.statistics.level / 100d);

        // Combo
        if (lines == 0) {
            comboValue = 1;
        } else {
            comboValue = comboValue + (2 * lines) - 2;
            if (comboValue < 1) comboValue = 1;
        }

        int s = (int) (Math.pow(2, (20d - engine.nowPieceY) / 20d) * Math.pow(2, effectiveSection / 5d) * 25);
        if (version >= 2) {
            // if (totalFall <= 0) s = 0;
            engine.statistics.score += s;
            engine.statistics.scoreFromOtherBonus += s;
            // totalFall = 0;
        }

        if (lines >= 1) {
            // Calculate score
            int manuallock = 0;
            if (engine.manualLock) manuallock = 1;

            int bravo = 1;
            if (engine.field.isEmpty()) {
                bravo = 4;
                engine.playSE("bravo");
            }

            if (version < 2)
                lastscore = (((engine.statistics.level + lines) / 4) + engine.softdropFall + engine.harddropFall + manuallock) * lines * comboValue * bravo;  // TGM1 scoring
            else {
                // T-EX scoring: refer to https://github.com/farteryhr/labs/blob/master/t-ex_core.as in the killline() function
                int scoreBase = (int) (lines * Math.pow(2, effectiveSection / 5d) * 150);
                lastscore = scoreBase * lines;
            }

            previousscore = engine.statistics.score;
            engine.statistics.score += lastscore;
            lastscore += s;
            scgettime = 120;

            int lvbefore = engine.statistics.level;
            // Level up
            engine.statistics.level += lines;
            levelUp(engine);

            if (engine.statistics.level >= maxLevel) {
                // Ending
                engine.statistics.level = maxLevel;
                if (lvbefore < maxLevel) {
                    engine.resetStatc();
                    engine.playSE("endingstart");
                    engine.statc[0] = -1;
                    engine.stat = GameEngine.STAT_CUSTOM;
                }
            } else if (engine.statistics.level >= nextseclv) {
                // Next Section
                owner.backgroundStatus.bg = engine.statistics.level / 100;
                engine.playSE("levelup");

                owner.backgroundStatus.fadesw = true;
                owner.backgroundStatus.fadecount = 0;
                owner.backgroundStatus.fadebg = nextseclv / 100;

                nextseclv += 100;
                if (nextseclv > maxLevel) nextseclv = maxLevel;

                if (engine.statistics.level >= 1000 && engine.statistics.level < 1004) {
                    engine.resetStatc();
                    engine.playSE("endingstart");
                    engine.statc[0] = -1;
                    engine.stat = GameEngine.STAT_CUSTOM;
                } else {
                    if (bgmlv < 4) {
                        if (engine.statistics.level >= tableBGMChange[bgmlv]) {
                            bgmlv++;
                            owner.bgmStatus.fadesw = false;
                            owner.bgmStatus.bgm = bgmlv;
                        }
                    }
                }

                if (engine.statistics.level >= 1500) {
                    if (version < 2) engine.bone = true;
                    else greying = true;
                }
            } else if ((engine.statistics.level == nextseclv - 1) && (lvstopse)) {
                engine.playSE("levelstop");
            }
        }
    }

    @Override
    public boolean onCustom(GameEngine engine, int playerID) {
        if (engine.gameActive) {
            if (engine.statc[0] > (180 + (engine.field.getHiddenHeight() + engine.field.getHeight()) * 3)) {
                engine.resetStatc();
                engine.stat = GameEngine.STAT_MOVE;
                engine.initialRotate();
                engine.playSE("go");

                if (!big) engine.big = (engine.statistics.level >= maxLevel);
                engine.timerActive = true;

                if (bgmlv < 4) {
                    if (engine.statistics.level >= tableBGMChange[bgmlv]) {
                        bgmlv++;
                        owner.bgmStatus.fadesw = false;
                        owner.bgmStatus.bgm = bgmlv;
                    }
                }
                return true;
            } else if (engine.statc[0] == (120 + engine.field.getHeight() * 3)) {
                if (engine.framecolor == GameEngine.FRAME_COLOR_RED) engine.framecolor = GameEngine.FRAME_COLOR_BLUE;
                if (engine.framecolor == GameEngine.FRAME_COLOR_GRAY) engine.framecolor = GameEngine.FRAME_COLOR_RED;
                engine.playSE("ready");
            } else if (engine.statc[0] > 0) {
                int f = engine.statc[0] - 60;

                if (f % 3 == 0 && f >= 0 && ((engine.field.getHeight() - 1) - (f / 3)) >= (-1 * engine.field.getHiddenHeight())) {
                    for (int x = 0; x < engine.field.getWidth(); x++) {
                        int y = engine.field.getHeight() - 1 - (f / 3);
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
            }

            engine.timerActive = false;

            engine.statc[0]++;
        } else {
            showPlayerStats = false;

            engine.isInGame = true;

            boolean s = playerProperties.loginScreen.updateScreen(engine, playerID);
            if (playerProperties.isLoggedIn()) {
                loadRankingPlayer(playerProperties, engine.ruleopt.strRuleName);
                loadSettingPlayer(playerProperties);
            }

            if (engine.stat == GameEngine.STAT_SETTING) engine.isInGame = false;
        }

        return true;
    }

    @Override
    public void onFirst(GameEngine engine, int playerID) {
        pCoordList.clear();
        cPiece = null;
    }

    /*
     * 各 frame の終わりの処理
     */
    @Override
    public void onLast(GameEngine engine, int playerID) {
        // 獲得Render score
        if (scgettime > 0) scgettime--;

        double proportion = ((double) (engine.getLockDelay() - engine.lockDelayNow) / engine.getLockDelay());
        // Meter
        engine.meterValue = (int) (proportion * receiver.getMeterMax(engine));
        engine.meterColor = GameEngine.METER_COLOR_GREEN;
        if (proportion <= 0.75) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
        if (proportion <= 0.5) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
        if (proportion <= 0.25) engine.meterColor = GameEngine.METER_COLOR_RED;

        if ((engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (!owner.replayMode)) || engine.stat == GameEngine.STAT_CUSTOM) {
            // Show rank
            if (engine.ctrl.isPush(Controller.BUTTON_F) && playerProperties.isLoggedIn() && engine.stat != GameEngine.STAT_CUSTOM) {
                showPlayerStats = !showPlayerStats;
                engine.playSE("change");
            }
        }

        if (engine.quitflag) {
            playerProperties = new ProfileProperties(headerColour);
        }
    }

    /*
     * Render score
     */
    @Override
    public void renderLast(GameEngine engine, int playerID) {
        if (owner.menuOnly) return;

        receiver.drawScoreFont(engine, playerID, 0, 0, getName(), EventReceiver.COLOR_BLUE);

        if ((engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (owner.replayMode == false))) {
            if ((owner.replayMode == false) && (big == false) && (engine.ai == null)) {
                float scale = (receiver.getNextDisplayType() == 2) ? 0.5f : 1.0f;
                int topY = (receiver.getNextDisplayType() == 2) ? 4 : 3;
                receiver.drawScoreFont(engine, playerID, 3, topY - 1, "SCORE  LEVEL TIME", EventReceiver.COLOR_BLUE, scale);

                if (showPlayerStats) {
                    for (int i = 0; i < RANKING_MAX; i++) {
                        receiver.drawScoreFont(engine, playerID, 0, topY + i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW, scale);
                        receiver.drawScoreFont(engine, playerID, 3, topY + i, String.valueOf(rankingScorePlayer[i]), (i == rankingRankPlayer), scale);
                        receiver.drawScoreFont(engine, playerID, 10, topY + i, getLevelName(rankingLevelPlayer[i]), (i == rankingRankPlayer), scale);
                        receiver.drawScoreFont(engine, playerID, 16, topY + i, GeneralUtil.getTime(rankingTimePlayer[i]), (i == rankingRankPlayer), scale);
                    }

                    receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 1, "PLAYER SCORES", EventReceiver.COLOR_BLUE);
                    receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 2, playerProperties.getNameDisplay(), EventReceiver.COLOR_WHITE, 2f);

                    receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 5, "F:SWITCH RANK SCREEN", EventReceiver.COLOR_GREEN);
                } else {
                    for (int i = 0; i < RANKING_MAX; i++) {
                        receiver.drawScoreFont(engine, playerID, 0, topY + i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW, scale);
                        receiver.drawScoreFont(engine, playerID, 3, topY + i, String.valueOf(rankingScore[i]), (i == rankingRank), scale);
                        receiver.drawScoreFont(engine, playerID, 10, topY + i, getLevelName(rankingLevel[i]), (i == rankingRank), scale);
                        receiver.drawScoreFont(engine, playerID, 16, topY + i, GeneralUtil.getTime(rankingTime[i]), (i == rankingRank), scale);
                    }

                    receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 1, "LOCAL SCORES", EventReceiver.COLOR_BLUE);
                    if (!playerProperties.isLoggedIn())
                        receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 2, "(NOT LOGGED IN)\n(E:LOG IN)");
                    if (playerProperties.isLoggedIn())
                        receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 5, "F:SWITCH RANK SCREEN", EventReceiver.COLOR_GREEN);

                }
            }
        } else if (!engine.gameActive && engine.stat == GameEngine.STAT_CUSTOM) {
            playerProperties.loginScreen.renderScreen(receiver, engine, playerID);
        } else {
            receiver.drawScoreFont(engine, playerID, 0, 2, "SCORE", EventReceiver.COLOR_BLUE);
            String strScore;
            int sc = (int) Interpolation.sineStep(previousscore, engine.statistics.score, (double) (120 - scgettime) / 120.0);
            if ((lastscore == 0) || (scgettime <= 0)) {
                strScore = String.valueOf(sc);
            } else {
                strScore = sc + "(+" + lastscore + ")";
            }
            receiver.drawScoreFont(engine, playerID, 0, 3, strScore);

            receiver.drawScoreFont(engine, playerID, 0, 5, "LEVEL", EventReceiver.COLOR_BLUE);
            receiver.drawScoreFont(engine, playerID, 0, 6, getLevelName(engine));

            receiver.drawScoreFont(engine, playerID, 0, 8, "TIME", EventReceiver.COLOR_BLUE);
            receiver.drawScoreFont(engine, playerID, 0, 9, GeneralUtil.getTime(engine.statistics.time));

            if (playerProperties.isLoggedIn() || PLAYER_NAME.length() > 0) {
                receiver.drawScoreFont(engine, playerID, 0, 11, "PLAYER", EventReceiver.COLOR_BLUE);
                receiver.drawScoreFont(engine, playerID, 0, 12, owner.replayMode ? PLAYER_NAME : playerProperties.getNameDisplay(), EventReceiver.COLOR_WHITE, 2f);
            }

            int baseX = receiver.getFieldDisplayPositionX(engine, playerID) + 4;
            int baseY = receiver.getFieldDisplayPositionY(engine, playerID) + 52;
            if (pCoordList.size() > 0 && cPiece != null) {
                for (int[] loc : pCoordList) {
                    int cx = baseX + (16 * loc[0]);
                    int cy = baseY + (16 * loc[1]);
                    RendererExtension.drawScaledPiece(receiver, engine, playerID, cx, cy, cPiece, 1f, 0f);
                }
            }
        }
    }

    /*
     * Soft drop
     */
    @Override
    public void afterSoftDropFall(GameEngine engine, int playerID, int fall) {
        if (version < 2) {
            engine.statistics.scoreFromSoftDrop += fall;
            engine.statistics.score += fall;
        }
    }

    /*
     * Hard drop
     */
    @Override
    public void afterHardDropFall(GameEngine engine, int playerID, int fall) {
        if (version < 2) {
            engine.statistics.scoreFromHardDrop += fall * 2;
            engine.statistics.score += fall * 2;
        }

        int baseX = (16 * engine.nowPieceX) + 4 + receiver.getFieldDisplayPositionX(engine, playerID);
        int baseY = (16 * engine.nowPieceY) + 52 + receiver.getFieldDisplayPositionY(engine, playerID);
        cPiece = new Piece(engine.nowPieceObject);
        for (int i = 1; i <= fall; i++) {
            pCoordList.add(
                new int[] { engine.nowPieceX, engine.nowPieceY - i }
            );
        }
        for (int i = 0; i < cPiece.getMaxBlock(); i++) {
            if (!cPiece.big) {
                int x2 = baseX + (cPiece.dataX[cPiece.direction][i] * 16);
                int y2 = baseY + (cPiece.dataY[cPiece.direction][i] * 16);

                RendererExtension.addBlockBreakEffect(receiver, x2, y2, cPiece.block[i]);
            } else {
                int x2 = baseX + (cPiece.dataX[cPiece.direction][i] * 32);
                int y2 = baseY + (cPiece.dataY[cPiece.direction][i] * 32);

                RendererExtension.addBlockBreakEffect(receiver, x2, y2, cPiece.block[i]);
                RendererExtension.addBlockBreakEffect(receiver, x2 + 16, y2, cPiece.block[i]);
                RendererExtension.addBlockBreakEffect(receiver, x2, y2 + 16, cPiece.block[i]);
                RendererExtension.addBlockBreakEffect(receiver, x2 + 16, y2 + 16, cPiece.block[i]);
            }
        }
    }

    /*
     * 結果画面の処理
     */
    @Override
    public boolean onResult(GameEngine engine, int playerID) {
        // ページ切り替え
        if (engine.ctrl.isMenuRepeatKey(Controller.BUTTON_UP)) {
            engine.statc[1]--;
            if (engine.statc[1] < 0) engine.statc[1] = 1;
            engine.playSE("change");
        }
        if (engine.ctrl.isMenuRepeatKey(Controller.BUTTON_DOWN)) {
            engine.statc[1]++;
            if (engine.statc[1] > 1) engine.statc[1] = 0;
            engine.playSE("change");
        }

        return false;
    }

    /*
     * 結果画面
     */
    @Override
    public void renderResult(GameEngine engine, int playerID) {
        receiver.drawMenuFont(engine, playerID, 0, 0, "kn PAGE" + (engine.statc[1] + 1) + "/2", EventReceiver.COLOR_RED);

        if (engine.statc[1] == 0) {
            drawResultStats(engine, playerID, receiver, 2, EventReceiver.COLOR_BLUE,
                STAT_SCORE, STAT_LINES, STAT_TIME);
            receiver.drawMenuFont(engine, playerID, 0, 8, "LEVEL", EventReceiver.COLOR_BLUE);
            receiver.drawMenuFont(engine, playerID, 0, 9, String.format("%10s", getLevelName(engine)));
            drawResultRank(engine, playerID, receiver, 10, EventReceiver.COLOR_BLUE, rankingRank);
        } else if (engine.statc[1] == 1) {
            drawResultStats(engine, playerID, receiver, 2, EventReceiver.COLOR_BLUE,
                STAT_LPM, STAT_SPM, STAT_PIECE, STAT_PPS);
        }
    }

    /*
     * Called when saving replay
     */
    @Override
    public void saveReplay(GameEngine engine, int playerID, CustomProperties prop) {
        saveSetting(prop);

        // Update rankings
        if ((!owner.replayMode) && (!big) && (engine.ai == null)) {
            updateRanking(engine.statistics.score, engine.statistics.level, engine.statistics.time);

            if (playerProperties.isLoggedIn()) {
                prop.setProperty("exreborn.playerName", playerProperties.getNameDisplay());
            }

            if (rankingRank != -1) {
                saveRanking(owner.modeConfig, engine.ruleopt.strRuleName);
                receiver.saveModeConfig(owner.modeConfig);
            }

            if (rankingRankPlayer != -1 && playerProperties.isLoggedIn()) {
                saveRankingPlayer(playerProperties, engine.ruleopt.strRuleName);
                playerProperties.saveProfileConfig();
            }
        }
    }

    /**
     * Load settings from property file
     *
     * @param prop Property file
     */
    private void loadSetting(CustomProperties prop) {
        lvstopse = prop.getProperty("exreborn.lvstopse", true);
        big = prop.getProperty("exreborn.big", false);
        alwaysghost = prop.getProperty("exreborn.alwaysghost", false);
        version = prop.getProperty("exreborn.version", 1);
    }

    /**
     * Save settings to property file
     *
     * @param prop Property file
     */
    private void saveSetting(CustomProperties prop) {
        prop.setProperty("exreborn.lvstopse", lvstopse);
        prop.setProperty("exreborn.big", big);
        prop.setProperty("exreborn.alwaysghost", alwaysghost);
        prop.setProperty("exreborn.version", version);
    }

    /**
     * Load settings from property file
     *
     * @param prop Property file
     */
    private void loadSettingPlayer(ProfileProperties prop) {
        if (!prop.isLoggedIn()) return;
        lvstopse = prop.getProperty("exreborn.lvstopse", true);
        big = prop.getProperty("exreborn.big", false);
        alwaysghost = prop.getProperty("exreborn.alwaysghost", false);
    }

    /**
     * Save settings to property file
     *
     * @param prop Property file
     */
    private void saveSettingPlayer(ProfileProperties prop) {
        if (!prop.isLoggedIn()) return;
        prop.setProperty("exreborn.lvstopse", lvstopse);
        prop.setProperty("exreborn.big", big);
        prop.setProperty("exreborn.alwaysghost", alwaysghost);
    }

    /**
     * Read rankings from property file
     *
     * @param prop     Property file
     * @param ruleName Rule name
     */
    protected void loadRanking(CustomProperties prop, String ruleName) {
        for (int i = 0; i < RANKING_MAX; i++) {
            rankingScore[i] = prop.getProperty("exreborn.ranking." + CURRENT_VERSION + "." + ruleName + ".score." + i, 0);
            rankingLevel[i] = prop.getProperty("exreborn.ranking." + CURRENT_VERSION + "." + ruleName + ".lines." + i, 0);
            rankingTime[i] = prop.getProperty("exreborn.ranking." + CURRENT_VERSION + "." + ruleName + ".time." + i, 0);
        }
    }

    /**
     * Save rankings to property file
     *
     * @param prop     Property file
     * @param ruleName Rule name
     */
    private void saveRanking(CustomProperties prop, String ruleName) {
        for (int i = 0; i < RANKING_MAX; i++) {
            prop.setProperty("exreborn.ranking." + CURRENT_VERSION + "." + ruleName + ".score." + i, rankingScore[i]);
            prop.setProperty("exreborn.ranking." + CURRENT_VERSION + "." + ruleName + ".lines." + i, rankingLevel[i]);
            prop.setProperty("exreborn.ranking." + CURRENT_VERSION + "." + ruleName + ".time." + i, rankingTime[i]);
        }
    }

    /**
     * Read rankings from property file
     *
     * @param prop     Property file
     * @param ruleName Rule name
     */
    private void loadRankingPlayer(ProfileProperties prop, String ruleName) {
        if (!prop.isLoggedIn()) return;
        for (int i = 0; i < RANKING_MAX; i++) {
            rankingScorePlayer[i] = prop.getProperty("exreborn.ranking." + CURRENT_VERSION + "." + ruleName + ".score." + i, 0);
            rankingLevelPlayer[i] = prop.getProperty("exreborn.ranking." + CURRENT_VERSION + "." + ruleName + ".lines." + i, 0);
            rankingTimePlayer[i] = prop.getProperty("exreborn.ranking." + CURRENT_VERSION + "." + ruleName + ".time." + i, 0);
        }
    }

    /**
     * Save rankings to property file
     *
     * @param prop     Property file
     * @param ruleName Rule name
     */
    private void saveRankingPlayer(ProfileProperties prop, String ruleName) {
        if (!prop.isLoggedIn()) return;
        for (int i = 0; i < RANKING_MAX; i++) {
            prop.setProperty("exreborn.ranking." + CURRENT_VERSION + "." + ruleName + ".score." + i, rankingScorePlayer[i]);
            prop.setProperty("exreborn.ranking." + CURRENT_VERSION + "." + ruleName + ".lines." + i, rankingLevelPlayer[i]);
            prop.setProperty("exreborn.ranking." + CURRENT_VERSION + "." + ruleName + ".time." + i, rankingTimePlayer[i]);
        }
    }

    /**
     * Update rankings
     *
     * @param sc   Score
     * @param lv   Level
     * @param time Time
     */
    private void updateRanking(int sc, int lv, int time) {
        rankingRank = checkRanking(sc, lv, time);

        if (rankingRank != -1) {
            // Shift down ranking entries
            for (int i = RANKING_MAX - 1; i > rankingRank; i--) {
                rankingScore[i] = rankingScore[i - 1];
                rankingLevel[i] = rankingLevel[i - 1];
                rankingTime[i] = rankingTime[i - 1];
            }

            // Add new data
            rankingScore[rankingRank] = sc;
            rankingLevel[rankingRank] = lv;
            rankingTime[rankingRank] = time;
        }

        if (playerProperties.isLoggedIn()) {
            rankingRankPlayer = checkRankingPlayer(sc, lv, time);

            if (rankingRankPlayer != -1) {
                // Shift down ranking entries
                for (int i = RANKING_MAX - 1; i > rankingRankPlayer; i--) {
                    rankingScorePlayer[i] = rankingScorePlayer[i - 1];
                    rankingLevelPlayer[i] = rankingLevelPlayer[i - 1];
                    rankingTimePlayer[i] = rankingTimePlayer[i - 1];
                }

                // Add new data
                rankingScorePlayer[rankingRankPlayer] = sc;
                rankingLevelPlayer[rankingRankPlayer] = lv;
                rankingTimePlayer[rankingRankPlayer] = time;
            }
        }
    }

    /**
     * Calculate ranking position
     *
     * @param sc   Score
     * @param lv   Level
     * @param time Time
     * @return Position (-1 if unranked)
     */
    private int checkRanking(int sc, int lv, int time) {
        for (int i = 0; i < RANKING_MAX; i++) {
            if (sc > rankingScore[i]) {
                return i;
            } else if ((sc == rankingScore[i]) && (lv > rankingLevel[i])) {
                return i;
            } else if ((sc == rankingScore[i]) && (lv == rankingLevel[i]) && (time < rankingTime[i])) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Calculate ranking position
     *
     * @param sc   Score
     * @param lv   Level
     * @param time Time
     * @return Position (-1 if unranked)
     */
    private int checkRankingPlayer(int sc, int lv, int time) {
        for (int i = 0; i < RANKING_MAX; i++) {
            if (sc > rankingScorePlayer[i]) {
                return i;
            } else if ((sc == rankingScorePlayer[i]) && (lv > rankingLevelPlayer[i])) {
                return i;
            } else if ((sc == rankingScorePlayer[i]) && (lv == rankingLevelPlayer[i]) && (time < rankingTimePlayer[i])) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Gets the level string.
     *
     * @param engine Current GameEngine.
     * @return String that gives the current level.
     */
    private String getLevelName(GameEngine engine) {
        int lvRaw = engine.statistics.level;
        int lv100 = lvRaw / 100;
        int lvUnit = lvRaw % 100;
        String str = "";

        if (lv100 < 10) {
            str += "H" + lv100 + "-" + lvUnit;
        } else if (lv100 < 20) {
            str += "M" + (lv100 - 10) + "-" + lvUnit;
        } else {
            str += "WTF";
        }

        return str;
    }

    private String getLevelName(int lvRaw) {
        int lv100 = lvRaw / 100;
        int lvUnit = lvRaw % 100;
        String str = "";

        if (lv100 < 10) {
            str += "H" + lv100 + "-" + lvUnit;
        } else if (lv100 < 20) {
            str += "M" + (lv100 - 10) + "-" + lvUnit;
        } else {
            str += "WTF";
        }

        return str;
    }
}
