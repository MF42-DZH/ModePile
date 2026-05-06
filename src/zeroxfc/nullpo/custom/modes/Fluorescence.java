package zeroxfc.nullpo.custom.modes;

import java.util.Random;
import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.game.subsystem.mode.DummyMode;
import zeroxfc.nullpo.custom.libs.CustomResourceHolder;
import zeroxfc.nullpo.custom.libs.FieldManipulation;
import zeroxfc.nullpo.custom.libs.PrimitiveDrawingHook;
import zeroxfc.nullpo.custom.libs.RendererExtension;
import zeroxfc.nullpo.custom.libs.mixins.HasCustomFieldDrawing;
import zeroxfc.nullpo.custom.libs.mixins.HasCustomMove;
import zeroxfc.nullpo.custom.modes.objects.fluorescence.FluorescenceRandomizer;

public class Fluorescence extends DummyMode implements HasCustomFieldDrawing, HasCustomMove {
    // NullpoMino base mode values.
    private GameManager owner;
    private EventReceiver receiver;

    // Custom graphics values.
    private CustomResourceHolder customGraphics;
    private RendererExtension rendererExtension;
    private PrimitiveDrawingHook drawing;

    // Game variables.
    private FluorescenceRandomizer randomizer;

    @Override
    public String getName() {
        return "FLUORESCENCE";
    }

    @Override
    public int getGameStyle() {
        return GameEngine.GAMESTYLE_AVALANCHE;
    }

    @Override
    public FrameDrawingParameters getFrameDrawingParameters(GameEngine engine, int playerID) {
        return null;
    }

    @Override
    public int getLastBackground() {
        return 0;
    }

    @Override
    public int getCurrentBackground() {
        return 0;
    }

    @Override
    public float getFadeProgress() {
        return 0;
    }

    @Override
    public void playerInit(GameEngine engine, int playerID) {
        owner = engine.owner;
        receiver = engine.owner.receiver;
        customGraphics = new CustomResourceHolder();
        rendererExtension = new RendererExtension(customGraphics);
        drawing = new PrimitiveDrawingHook(customGraphics);

        setupBackgrounds(engine);

        // We only want O-pieces generating.
        for (int i = 0; i < Piece.PIECE_COUNT; ++i) {
            engine.nextPieceEnable[i] = i == Piece.PIECE_O;
        }
    }

    @Override
    public boolean onReady(GameEngine engine, int playerID) {
        if (engine.statc[0] == 0) {
            engine.statistics.level = 0;
        } else if (engine.statc[0] == 1) {
            randomizer = new FluorescenceRandomizer(new Random(engine.randSeed), Block.BLOCK_COLOR_GRAY, Block.BLOCK_COLOR_ORANGE);

            for (int i = 0; i < engine.ruleopt.nextDisplay; ++i) {
                randomizer.initPiece(HasCustomMove.getNextObject(engine, engine.nextPieceCount + i));
            }
        }

        return false;
    }

    @Override
    public void startGame(GameEngine engine, int playerID) {
        // TODO
    }

    @Override
    public boolean onMove(GameEngine engine, int playerID) {
        return inOnMove(engine, playerID);
    }

    @Override
    public void inPieceSpawn(GameEngine engine, int playerID) {
        // 出現時の処理
        if (engine.statc[0] == 0) {
            if ((engine.statc[1] == 0) && (!engine.initialHoldFlag)) {
                // 通常出現
                engine.nowPieceObject = HasCustomMove.getNextObjectCopy(engine, engine.nextPieceCount);
                engine.nextPieceCount++;
                if (engine.nextPieceCount < 0) engine.nextPieceCount = 0;
                engine.holdDisable = false;
            } else {
                // ホールド出現
                if (engine.initialHoldFlag) {
                    // 先行ホールド
                    if (engine.holdPieceObject == null) {
                        // 1回目
                        engine.holdPieceObject = HasCustomMove.getNextObjectCopy(engine, engine.nextPieceCount);
                        engine.holdPieceObject.applyOffsetArray(engine.ruleopt.pieceOffsetX[engine.holdPieceObject.id], engine.ruleopt.pieceOffsetY[engine.holdPieceObject.id]);
                        engine.nextPieceCount++;
                        if (engine.nextPieceCount < 0) engine.nextPieceCount = 0;

                        if (engine.bone)
                            HasCustomMove.getNextObject(engine, engine.nextPieceCount + engine.ruleopt.nextDisplay - 1).setAttribute(Block.BLOCK_ATTRIBUTE_BONE, true);

                        {
                            final Piece justIn = HasCustomMove.getNextObject(engine, engine.nextPieceCount + engine.ruleopt.nextDisplay - 1);
                            if (justIn.block[0].bonusValue != 10) randomizer.initPiece(justIn);
                        }

                        engine.nowPieceObject = HasCustomMove.getNextObjectCopy(engine, engine.nextPieceCount);
                        engine.nextPieceCount++;
                        if (engine.nextPieceCount < 0) engine.nextPieceCount = 0;
                    } else {
                        // 2回目以降
                        Piece pieceTemp = engine.holdPieceObject;
                        engine.holdPieceObject = HasCustomMove.getNextObjectCopy(engine, engine.nextPieceCount);
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
                        engine.nowPieceObject = HasCustomMove.getNextObjectCopy(engine, engine.nextPieceCount);
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
            engine.playSE("piece" + HasCustomMove.getNextObject(engine, engine.nextPieceCount).id);

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

            HasCustomMove.getNextObject(engine, engine.nextPieceCount + engine.ruleopt.nextDisplay - 1).setAttribute(Block.BLOCK_ATTRIBUTE_BONE, engine.bone);
            {
                final Piece justIn = HasCustomMove.getNextObject(engine, engine.nextPieceCount + engine.ruleopt.nextDisplay - 1);
                if (justIn.block[0].bonusValue != 10) randomizer.initPiece(justIn);
            }

            if (engine.ending == 0) engine.timerActive = true;

            if ((engine.ai != null) && (!engine.owner.replayMode || engine.owner.replayRerecord))
                engine.ai.newPiece(engine, playerID);
        }
    }

    @Override
    public void onLast(GameEngine engine, int playerID) {
        if (engine.gameActive && !engine.lagStop) FieldManipulation.freeFallStep(engine.field);
    }

    @Override
    public void renderFirst(GameEngine engine, int playerID) {
        inRenderFirst(rendererExtension, receiver, engine, playerID);
    }

    @Override
    public void renderMove(GameEngine engine, int playerID) {
        inRenderMove(rendererExtension, receiver, engine, playerID);
        rendererExtension.drawPostHoldOutline(receiver, engine, playerID);
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
}
