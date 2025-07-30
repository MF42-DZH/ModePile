package zeroxfc.nullpo.custom.modes;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.function.BiFunction;
import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.component.RuleOptions;
import mu.nu.nullpo.game.component.WallkickResult;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.util.CustomProperties;
import mu.nu.nullpo.util.GeneralUtil;
import net.omegaboshi.nullpomino.game.subsystem.randomizer.Randomizer;
import org.apache.log4j.Logger;
import zeroxfc.nullpo.custom.libs.CustomResourceHolder;
import zeroxfc.nullpo.custom.libs.GameTextUtilities;
import zeroxfc.nullpo.custom.libs.Interpolation;
import zeroxfc.nullpo.custom.libs.MathHelper;
import zeroxfc.nullpo.custom.libs.types.ObjectAlignment;
import zeroxfc.nullpo.custom.libs.ProfileProperties;
import zeroxfc.nullpo.custom.libs.RendererExtension;
import zeroxfc.nullpo.custom.libs.SoundLoader;
import zeroxfc.nullpo.custom.libs.particles.LandingParticles;

public class MultiNextMarathon extends MarathonModeBase {
    private static final Logger log = Logger.getLogger(MultiNextMarathon.class);

    private static final int HEADER_COLOUR = EventReceiver.COLOR_ORANGE;
    private int previousScore;
    private ProfileProperties playerProperties;
    private boolean showPlayerStats;
    private String playerName;
    private int rankingRankPlayer;
    private int[][] rankingScorePlayer;
    private int[][] rankingLinesPlayer;
    private int[][] rankingTimePlayer;
    private int currentBackground;

    private QueueHolder leftQueue;
    private QueueHolder rightQueue;
    private WhichQueue selectedNext;
    private WhichQueue lastUsedNext;
    private boolean canSwitchNext;

    private enum WhichQueue { LEFT, RIGHT }

    // Need this due to lambda jankery.
    private WhichQueue getSelectedNext() {
        return selectedNext;
    }

    private QueueHolder getNext() {
        if (selectedNext == WhichQueue.LEFT) {
            return leftQueue;
        } else {
            return rightQueue;
        }
    }

    private void switchNext(GameEngine engine) {
        if (selectedNext == WhichQueue.LEFT) {
            selectedNext = WhichQueue.RIGHT;
        } else {
            selectedNext = WhichQueue.LEFT;
        }
    }

    private static class QueueHolder {
        public final LinkedList<Piece> queue;
        public final int queueSize;

        private final Randomizer pieceRandom;
        private final GameEngine engine;
        private final Random pieceDataRand;

        public QueueHolder(GameEngine engine, Randomizer pieceRandom, int queueSize, long pieceDataRandSeed) {
            this.pieceRandom = pieceRandom;
            this.queue = new LinkedList<>();
            this.queueSize = queueSize;
            this.engine = engine;
            this.pieceDataRand = new Random(pieceDataRandSeed);
        }

        public void fillQueue() {
            while (queue.size() < queueSize) queue.addLast(turnIntoPiece(pieceRandom.next()));
        }

        public void returnPiece(Piece piece) {
            queue.addFirst(piece);
        }

        private Piece turnIntoPiece(int id) {
            final Piece piece = new Piece(id);

            piece.direction = engine.ruleopt.pieceDefaultDirection[piece.id];
            if(piece.direction >= Piece.DIRECTION_COUNT) {
                piece.direction = pieceDataRand.nextInt(Piece.DIRECTION_COUNT);
            }
            piece.connectBlocks = engine.connectBlocks;
            piece.setColor(engine.ruleopt.pieceColor[piece.id]);
            piece.setSkin(engine.getSkin());
            piece.updateConnectData();
            piece.setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
            piece.setAttribute(Block.BLOCK_ATTRIBUTE_BONE, engine.bone);

            if (engine.randomBlockColor) {
                if (engine.blockColors.length < engine.numColors || engine.numColors < 1) {
                    engine.numColors = engine.blockColors.length;
                }

                final int size = piece.getMaxBlock();
                final int[] colors = new int[size];
                for (int j = 0; j < size; j++) colors[j] = engine.blockColors[pieceDataRand.nextInt(engine.numColors)];
                piece.setColor(colors);
                piece.updateConnectData();
            }

            return piece;
        }
    }

    private static class ReturningPiece {
        private final Piece piece;
        private final int[] source;
        private final int[] target;

        private static final int MAX_LIFETIME = 12;
        private int lifetime;

        public ReturningPiece(Piece piece, int[] source, int[] target) {
            this.piece = piece;
            this.source = source;
            this.target = target;
            this.lifetime = 0;
        }

        public boolean update() {
            if (lifetime >= MAX_LIFETIME) return false;

            ++lifetime;
            return true;
        }

        public void draw(RendererExtension rendererExtension, EventReceiver receiver) {
            int cx = (int) Interpolation.smoothStep(source[0], target[0], (double) lifetime / MAX_LIFETIME);
            int cy = (int) Interpolation.smoothStep(source[1], target[1], (double) lifetime / MAX_LIFETIME);

            rendererExtension.drawAlignedScaledPiece(
                receiver,
                cx, cy, ObjectAlignment.TOP_LEFT,
                this.piece, 1.0f, Interpolation.lerp(0.0f, 0.333f, (double) lifetime / MAX_LIFETIME)
            );
        }
    }

    private ArrayList<ReturningPiece> returningPieces;

    /**
     * The good hard drop effect
     */
    private ArrayList<int[]> pCoordList;
    private Piece cPiece;

    private CustomResourceHolder customGraphics;
    private RendererExtension rendererExtension;
    private LandingParticles landingParticles;
    private boolean hardDropEffect;
    private boolean auditoryASMR;

    private RuleOptions oldRuleOpt;

    private BiFunction<Integer, Integer, int[]> frameColF;

    /*
     * Mode name
     */
    @Override
    public String getName() {
        return "MULTINEXT MARATHON";
    }

    @Override
    public void playerInit(GameEngine engine, int playerID) {
        owner = engine.owner;
        receiver = engine.owner.receiver;

        engine.isVisible = false;
        engine.owner.backgroundStatus.bg = -1;
        engine.owner.backgroundStatus.fadebg = -1;
        selectedNext = WhichQueue.LEFT;

        frameColF = new BiFunction<Integer, Integer, int[]>() {
            private final int[] tempColour = { 0, 0, 0 };

            @Override
            public int[] apply(Integer integer, Integer integer2) {
                final int width = engine.field != null ? engine.field.getWidth() : 10;
                final int height = engine.field != null ? engine.field.getHeight() : 20;

                final int usedX = getSelectedNext() == WhichQueue.LEFT ? integer : ((width * 4) - integer);

                tempColour[0] = 255;
                tempColour[1] = Interpolation.lerp(
                    255, 96,
                    MathHelper.clamp(
                        (Math.max(usedX, integer2) - 1d) / (height * 4),
                        0d, 1d
                    )
                );

                return tempColour;
            }
        };

        lastscore = 0;
        scgettime = 0;
        lastevent = EVENT_NONE;
        lastb2b = false;
        lastcombo = 0;
        lastpiece = 0;
        bgmlv = 0;
        previousScore = 0;
        big = false;

        rankingRank = -1;
        rankingScore = new int[RANKING_TYPE][RANKING_MAX];
        rankingLines = new int[RANKING_TYPE][RANKING_MAX];
        rankingTime = new int[RANKING_TYPE][RANKING_MAX];

        pCoordList = new ArrayList<>();
        cPiece = null;

        if (playerProperties == null) {
            playerProperties = new ProfileProperties(HEADER_COLOUR);
            showPlayerStats = false;
        }

        rankingRankPlayer = -1;
        rankingScorePlayer = new int[RANKING_TYPE][RANKING_MAX];
        rankingLinesPlayer = new int[RANKING_TYPE][RANKING_MAX];
        rankingTimePlayer = new int[RANKING_TYPE][RANKING_MAX];

        customGraphics = new CustomResourceHolder(1);
        rendererExtension = new RendererExtension(customGraphics);
        hardDropEffect = true;
        auditoryASMR = true;

        returningPieces = new ArrayList<>(2);

        netPlayerInit(engine, playerID);

        if (!owner.replayMode) {
            loadSetting(owner.modeConfig);
            loadRanking(owner.modeConfig, engine.ruleopt.strRuleName);

            if (playerProperties.isLoggedIn()) {
                loadSettingPlayer(playerProperties);
                loadRankingPlayer(playerProperties, engine.ruleopt.strRuleName);
            }

            playerName = "";
            version = BASE_VERSION;
        } else {
            loadSetting(owner.replayProp);
            if ((version == 0) && (owner.replayProp.getProperty("multinext.endless", false))) {
                goaltype = 2;
            }

            playerName = owner.replayProp.getProperty("multinext.playerName", "");

            // NET: Load name
            netPlayerName = engine.owner.replayProp.getProperty(playerID + ".net.netPlayerName", "");
        }

        currentBackground = startlevel;
        engine.bighalf = true;
        engine.bigmove = true;
        engine.framecolor = GameEngine.FRAME_COLOR_YELLOW;

        if (oldRuleOpt != null && engine.ruleopt.strRuleName.equals(oldRuleOpt.strRuleName)) engine.ruleopt = new RuleOptions(oldRuleOpt);
        oldRuleOpt = new RuleOptions(engine.ruleopt);
    }

    @Override
    public boolean onSetting(GameEngine engine, int playerID) {
        // NET: Net Ranking
        if (netIsNetRankingDisplayMode) {
            netOnUpdateNetPlayRanking(engine, goaltype);
        }

        // Menu
        else if (!engine.owner.replayMode) {
            // Configuration changes
            int change = updateCursor(engine, 10, playerID);

            if (change != 0) {
                engine.playSE("change");

                switch (engine.statc[2]) {
                    case 0:
                        startlevel += change;
                        if (tableGameClearLines[goaltype] >= 0) {
                            if (startlevel < 0) startlevel = (tableGameClearLines[goaltype] - 1) / 10;
                            if (startlevel > (tableGameClearLines[goaltype] - 1) / 10) startlevel = 0;
                        } else {
                            if (startlevel < 0) startlevel = 19;
                            if (startlevel > 19) startlevel = 0;
                        }
                        currentBackground = startlevel;
                        break;
                    case 1:
                        //enableTSpin = !enableTSpin;
                        tspinEnableType += change;
                        if (tspinEnableType < 0) tspinEnableType = 2;
                        if (tspinEnableType > 2) tspinEnableType = 0;
                        break;
                    case 2:
                        enableTSpinKick = !enableTSpinKick;
                        break;
                    case 3:
                        spinCheckType += change;
                        if (spinCheckType < 0) spinCheckType = 1;
                        if (spinCheckType > 1) spinCheckType = 0;
                        break;
                    case 4:
                        tspinEnableEZ = !tspinEnableEZ;
                        break;
                    case 5:
                        enableB2B = !enableB2B;
                        break;
                    case 6:
                        enableCombo = !enableCombo;
                        break;
                    case 7:
                        goaltype += change;
                        if (goaltype < 0) goaltype = GAMETYPE_MAX - 1;
                        if (goaltype > GAMETYPE_MAX - 1) goaltype = 0;

                        if ((startlevel > (tableGameClearLines[goaltype] - 1) / 10) && (tableGameClearLines[goaltype] >= 0)) {
                            startlevel = (tableGameClearLines[goaltype] - 1) / 10;
                            currentBackground = startlevel;
                        }
                        break;
                    case 8:
                        big = !big;
                        break;
                    case 9:
                        hardDropEffect = !hardDropEffect;
                        break;
                    case 10:
                        auditoryASMR = !auditoryASMR;
                        break;
                }

                // NET: Signal options change
                if (netIsNetPlay && (netNumSpectators > 0)) {
                    netSendOptions(engine);
                }
            }

            currentBackground = startlevel;

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

                // NET: Signal start of the game
                if (netIsNetPlay) netLobby.netPlayerClient.send("start1p\n");

                return false;
            }

            // Cancel
            if (engine.ctrl.isPush(Controller.BUTTON_B) && !netIsNetPlay) {
                engine.quitflag = true;
                playerProperties = new ProfileProperties(HEADER_COLOUR);
            }

            // New acc
            if (engine.ctrl.isPush(Controller.BUTTON_E) && engine.ai == null && !netIsNetPlay) {
                playerProperties = new ProfileProperties(HEADER_COLOUR);
                engine.playSE("decide");

                engine.stat = GameEngine.STAT_CUSTOM;
                engine.resetStatc();
                return true;
            }

            // NET: Netplay Ranking
            if (engine.ctrl.isPush(Controller.BUTTON_D) && netIsNetPlay && startlevel == 0 && !big &&
                engine.ai == null) {
                netEnterNetPlayRankingScreen(engine, playerID, goaltype);
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
        if (netIsNetRankingDisplayMode) {
            // NET: Netplay Ranking
            netOnRenderNetPlayRanking(engine, playerID, receiver);
        } else {
            String strTSpinEnable = "";
            if (version >= 2) {
                if (tspinEnableType == 0) strTSpinEnable = "OFF";
                if (tspinEnableType == 1) strTSpinEnable = "T-ONLY";
                if (tspinEnableType == 2) strTSpinEnable = "ALL";
            } else {
                strTSpinEnable = GeneralUtil.getONorOFF(enableTSpin);
            }

            if (engine.statc[2] < 10) {
                drawMenu(engine, playerID, receiver, 0, EventReceiver.COLOR_BLUE, 0,
                    "LEVEL", String.valueOf(startlevel + 1),
                    "SPIN BONUS", strTSpinEnable,
                    "EZ SPIN", GeneralUtil.getONorOFF(enableTSpinKick),
                    "SPIN TYPE", (spinCheckType == 0) ? "4POINT" : "IMMOBILE",
                    "EZIMMOBILE", GeneralUtil.getONorOFF(tspinEnableEZ),
                    "B2B", GeneralUtil.getONorOFF(enableB2B),
                    "COMBO", GeneralUtil.getONorOFF(enableCombo),
                    "GOAL", (goaltype == 2) ? "ENDLESS" : tableGameClearLines[goaltype] + " LINES",
                    "BIG", GeneralUtil.getONorOFF(big)
                );
                drawMenu(engine, playerID, receiver, 18, EventReceiver.COLOR_PINK, 9,
                    "DROP EFF.", GeneralUtil.getONorOFF(hardDropEffect)
                );
            } else {
                drawMenu(engine, playerID, receiver, 0, EventReceiver.COLOR_ORANGE, 10,
                    "ASMR", GeneralUtil.getONorOFF(auditoryASMR)
                );
            }
        }
    }

    @Override
    public boolean onReady(GameEngine engine, int playerID) {
        if (engine.statc[0] == 0) {
            returningPieces.clear();
            previousScore = 0;

            final Randomizer leftRand = GeneralUtil.loadRandomizer(engine.ruleopt.strRandomizer);
            final Randomizer rightRand = GeneralUtil.loadRandomizer(engine.ruleopt.strRandomizer);

            leftRand.setState(engine.nextPieceEnable, engine.randSeed);
            rightRand.setState(engine.nextPieceEnable, (engine.randSeed << 1) + engine.randSeed);

            leftQueue = new QueueHolder(
                engine,
                leftRand,
                Math.max(1, engine.ruleopt.nextDisplay),
                engine.randSeed >>> 1
            );

            rightQueue = new QueueHolder(
                engine,
                rightRand,
                Math.max(1, engine.ruleopt.nextDisplay),
                (engine.randSeed << 1) + 1
            );

            leftQueue.fillQueue();
            rightQueue.fillQueue();

            final RuleOptions newRules = new RuleOptions(engine.ruleopt);
            newRules.nextDisplay = 0;
            newRules.holdEnable = false;

            engine.ruleopt = newRules;

            selectedNext = WhichQueue.LEFT;
            lastUsedNext = WhichQueue.LEFT;
            canSwitchNext = true;

            landingParticles = new LandingParticles(customGraphics, engine.randSeed);
        }

        return false;
    }

    @Override
    public void onFirst(GameEngine engine, int playerID) {
        pCoordList.clear();
        cPiece = null;
    }

    @Override
    public void renderFirst(GameEngine engine, int playerID) {
        rendererExtension.drawDefaultBackground(engine, currentBackground);

        int offsetX = receiver.getFieldDisplayPositionX(engine, playerID);
        int offsetY = receiver.getFieldDisplayPositionY(engine, playerID);

        if (engine.displaysize != -1 && frameColF != null) {
            rendererExtension.drawNext(receiver, engine, offsetX, offsetY);

            rendererExtension.drawCustomFrame(
                receiver, engine,
                offsetX, offsetY + 48, engine.displaysize,
                frameColF
            );

            rendererExtension.drawField(receiver, engine, offsetX + 4, offsetY + 52, engine.displaysize);
        } else {
            rendererExtension.drawFrame(receiver, engine, offsetX, offsetY, -1);

            rendererExtension.drawCustomFrame(
                receiver, engine,
                offsetX, offsetY + 48, -1,
                frameColF
            );

            rendererExtension.drawField(receiver, engine, offsetX + 4, offsetY + 4, -1);
        }
    }

    @Override
    public void renderMove(GameEngine engine, int playerID) {
        engine.isVisible = true;
        receiver.renderMove(engine, playerID);
        engine.isVisible = false;
    }

    @Override
    public void afterHardDropFall(GameEngine engine, int playerID, int fall) {
        engine.statistics.scoreFromHardDrop += fall * 2;
        engine.statistics.score += fall * 2;

        cPiece = new Piece(engine.nowPieceObject);
        for (int i = 1; i <= fall; i++) {
            pCoordList.add(
                new int[] { engine.nowPieceX, engine.nowPieceY - i }
            );
        }

        if (hardDropEffect) {
            landingParticles.addNumber(receiver, engine, playerID, 32);
        }
    }

    private void setSideNexts(boolean side, boolean big) {
        final Class<EventReceiver> erc = EventReceiver.class;

        try {
            final Field localFieldSN = erc.getDeclaredField("sidenext");
            final Field localFieldSNB = erc.getDeclaredField("bigsidenext");

            localFieldSN.setAccessible(true);
            localFieldSNB.setAccessible(true);

            localFieldSN.setBoolean(receiver, side);
            localFieldSNB.setBoolean(receiver, big);
        } catch (Exception e) {
            log.error("Failed to set next data.");
            log.error(e);
        }
    }

    @Override
    public void onLast(GameEngine engine, int playerID) {
        scgettime++;

        if ((engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (!owner.replayMode)) || engine.stat == GameEngine.STAT_CUSTOM) {
            // Show rank
            if (engine.ctrl.isPush(Controller.BUTTON_F) && playerProperties.isLoggedIn() && engine.stat != GameEngine.STAT_CUSTOM) {
                showPlayerStats = !showPlayerStats;
                engine.playSE("change");
            }
        }

        // Force top next.
        setSideNexts(false, false);

        // Next shuffling
        if (engine.ctrl.isPush(Controller.BUTTON_F) && engine.isInGame && engine.stat != GameEngine.STAT_CUSTOM) {
            switchNext(engine);
            SoundLoader.playPannedSound(engine, "initialhold", selectedNext == WhichQueue.LEFT ? -1f : 1f);
        }

        if (engine.quitflag) {
            playerProperties = new ProfileProperties(HEADER_COLOUR);
        }

        if (!returningPieces.isEmpty()) {
            returningPieces.removeIf(rp -> !rp.update());
        }

        if (landingParticles != null) landingParticles.update();
    }

    @Override
    public boolean onMove(GameEngine engine, int playerID) {
        engine.dasRepeat = false;

        // 横溜めInitialization
        int moveDirection = engine.getMoveDirection();

        if((engine.statc[0] > 0) || (engine.ruleopt.dasInMoveFirstFrame)) {
            if(engine.dasDirection != moveDirection) {
                engine.dasDirection = moveDirection;
                if(!(engine.dasDirection == 0 && engine.ruleopt.dasStoreChargeOnNeutral)){
                    engine.dasCount = 0;
                }
            }
        }

        // 出現時の処理
        if(engine.statc[0] == 0) {
            if (!engine.initialHoldFlag) {
                engine.nowPieceObject = getNext().queue.poll();
                lastUsedNext = selectedNext;
            }

            leftQueue.fillQueue();
            rightQueue.fillQueue();

            // Initial Return
            if (canSwitchNext && engine.ctrl.isPress(Controller.BUTTON_D) && engine.nowPieceObject != null) {
                canSwitchNext = false;

                engine.nowPieceObject.direction = engine.ruleopt.pieceDefaultDirection[engine.nowPieceObject.id];
                engine.nowPieceObject.updateConnectData();

                getNext().returnPiece(engine.nowPieceObject);

                switchNext(engine);
                SoundLoader.playPannedSound(engine, "initialhold", selectedNext == WhichQueue.LEFT ? -1f : 1f);

                engine.statc[0] = 0;
                engine.statc[1] = 1;

                engine.initialHoldFlag = true;

                engine.nowPieceObject = getNext().queue.poll();
                lastUsedNext = selectedNext;

                return true;
            } else {
                engine.initialHoldFlag = false;
            }

            if (auditoryASMR || lastUsedNext == WhichQueue.LEFT) {
                SoundLoader.playPannedSound(engine, "piece" + leftQueue.queue.peek().id, -1f);
            }

            if (auditoryASMR || lastUsedNext == WhichQueue.RIGHT) {
                SoundLoader.playPannedSound(engine, "piece" + rightQueue.queue.peek().id, 1f);
            }

            if(!engine.nowPieceObject.offsetApplied)
                engine.nowPieceObject.applyOffsetArray(engine.ruleopt.pieceOffsetX[engine.nowPieceObject.id], engine.ruleopt.pieceOffsetY[engine.nowPieceObject.id]);

            engine.nowPieceObject.big = engine.big;

            // 出現位置 (横）
            engine.nowPieceX = engine.getSpawnPosX(engine.field, engine.nowPieceObject);

            // 出現位置 (縦）
            engine.nowPieceY = engine.getSpawnPosY(engine.nowPieceObject);

            engine.nowPieceBottomY = engine.nowPieceObject.getBottom(engine.nowPieceX, engine.nowPieceY, engine.field);
            engine.nowPieceColorOverride = -1;

            if(engine.itemRollRollEnable) engine.nowPieceColorOverride = Block.BLOCK_COLOR_GRAY;

            // 先行rotation
            if(engine.versionMajor < 7.5f) engine.initialRotate();

            if((engine.speed.gravity > engine.speed.denominator) && (engine.speed.denominator > 0))
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

            engine.nowPieceObject.updateConnectData();

            if(engine.ending == 0) engine.timerActive = true;

            if((engine.ai != null) && (!owner.replayMode || owner.replayRerecord)) engine.ai.newPiece(engine, playerID);
        }

        engine.checkDropContinuousUse();

        boolean softdropUsed = false; // この frame にSoft dropを使ったらtrue
        int softdropFallNow = 0; // この frame のSoft dropで落下した段count

        boolean updown = false; // Up下同時押し flag
        if(engine.ctrl.isPress(engine.getUp()) && engine.ctrl.isPress(engine.getDown())) updown = true;

        if(!engine.dasInstant) {
            // Return
            if (engine.statc[0] > 0 && canSwitchNext && engine.ctrl.isPush(Controller.BUTTON_D) && engine.nowPieceObject != null) {
                canSwitchNext = false;

                engine.nowPieceObject.direction = engine.ruleopt.pieceDefaultDirection[engine.nowPieceObject.id];
                engine.nowPieceObject.updateConnectData();

                (lastUsedNext == WhichQueue.LEFT ? leftQueue : rightQueue).returnPiece(engine.nowPieceObject);
                addReturningPiece(engine, playerID, lastUsedNext);

                selectedNext = (lastUsedNext == WhichQueue.LEFT ? WhichQueue.RIGHT : WhichQueue.LEFT);

                SoundLoader.playPannedSound(engine, "hold", selectedNext == WhichQueue.LEFT ? -1f : 1f);

                engine.statc[0] = 0;
                engine.statc[1] = 1;
                return true;
            } else if (engine.statc[0] > 0 && engine.ctrl.isPush(Controller.BUTTON_D)) {
                engine.playSE("holdfail");
            }

            // rotation
            boolean onGroundBeforeRotate = engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY + 1, engine.field);
            int move = 0;
            boolean rotated = false;

            if(engine.initialRotateDirection != 0) {
                move = engine.initialRotateDirection;
                engine.initialRotateLastDirection = engine.initialRotateDirection;
                engine.initialRotateContinuousUse = true;
                engine.playSE("initialrotate");
            } else if((engine.statc[0] > 0) || (engine.ruleopt.moveFirstFrame)) {
                if((engine.itemRollRollEnable) && (engine.replayTimer % engine.itemRollRollInterval == 0)) move = 1;	// Roll Roll

                //  button input
                if(engine.ctrl.isPush(Controller.BUTTON_A) || engine.ctrl.isPush(Controller.BUTTON_C)) move = -1;
                else if(engine.ctrl.isPush(Controller.BUTTON_B)) move = 1;
                else if(engine.ctrl.isPush(Controller.BUTTON_E)) move = 2;

                if(move != 0) {
                    engine.initialRotateLastDirection = move;
                    engine.initialRotateContinuousUse = true;
                }
            }

            if((!engine.ruleopt.rotateButtonAllowDouble) && (move == 2)) move = -1;
            if((!engine.ruleopt.rotateButtonAllowReverse) && (move == 1)) move = -1;
            if(engine.isRotateButtonDefaultRight() && (move != 2)) move = move * -1;

            if(move != 0) {
                // Direction after rotationを決める
                int rt = engine.getRotateDirection(move);

                // rotationできるか判定
                if(!engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY, rt, engine.field))
                {
                    // Wallkickなしでrotationできるとき
                    rotated = true;
                    engine.kickused = false;
                    engine.nowPieceObject.direction = rt;
                    engine.nowPieceObject.updateConnectData();
                } else if( (engine.ruleopt.rotateWallkick) &&
                    (engine.wallkick != null) &&
                    ((engine.initialRotateDirection == 0) || (engine.ruleopt.rotateInitialWallkick)) &&
                    ((engine.ruleopt.lockresetLimitOver != RuleOptions.LOCKRESET_LIMIT_OVER_NOWALLKICK) || (!engine.isRotateCountExceed())) )
                {
                    // Wallkickを試みる
                    boolean allowUpward = (engine.ruleopt.rotateMaxUpwardWallkick < 0) || (engine.nowUpwardWallkickCount < engine.ruleopt.rotateMaxUpwardWallkick);
                    WallkickResult kick = engine.wallkick.executeWallkick(engine.nowPieceX, engine.nowPieceY, move, engine.nowPieceObject.direction, rt,
                        allowUpward, engine.nowPieceObject, engine.field, engine.ctrl);

                    if(kick != null) {
                        rotated = true;
                        engine.kickused = true;
                        engine.nowWallkickCount++;
                        if(kick.isUpward()) engine.nowUpwardWallkickCount++;
                        engine.nowPieceObject.direction = kick.direction;
                        engine.nowPieceObject.updateConnectData();
                        engine.nowPieceX += kick.offsetX;
                        engine.nowPieceY += kick.offsetY;

                        if(engine.ruleopt.lockresetWallkick && !engine.isRotateCountExceed()) {
                            engine.lockDelayNow = 0;
                            engine.nowPieceObject.setDarkness(0f);
                        }
                    }
                }

                // Domino Quick Turn
                if(!rotated && engine.dominoQuickTurn && (engine.nowPieceObject.id == Piece.PIECE_I2) && (engine.nowPieceRotateFailCount >= 1)) {
                    rt = engine.getRotateDirection(2);
                    rotated = true;
                    engine.nowPieceObject.direction = rt;
                    engine.nowPieceObject.updateConnectData();
                    engine.nowPieceRotateFailCount = 0;

                    if(engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY, rt, engine.field)) {
                        engine.nowPieceY--;
                    } else if(onGroundBeforeRotate) {
                        engine.nowPieceY++;
                    }
                }

                if(rotated) {
                    // rotation成功
                    engine.nowPieceBottomY = engine.nowPieceObject.getBottom(engine.nowPieceX, engine.nowPieceY, engine.field);

                    if((engine.ruleopt.lockresetRotate) && (!engine.isRotateCountExceed())) {
                        engine.lockDelayNow = 0;
                        engine.nowPieceObject.setDarkness(0f);
                    }

                    if(onGroundBeforeRotate) {
                        engine.extendedRotateCount++;
                        engine.lastmove = GameEngine.LASTMOVE_ROTATE_GROUND;
                    } else {
                        engine.lastmove = GameEngine.LASTMOVE_ROTATE_AIR;
                    }

                    if(engine.initialRotateDirection == 0) {
                        engine.playSE("rotate");
                    }

                    engine.nowPieceRotateCount++;
                    if((engine.ending == 0) || (engine.staffrollEnableStatistics)) engine.statistics.totalPieceRotate++;
                } else {
                    // rotation失敗
                    engine.playSE("rotfail");
                    engine.nowPieceRotateFailCount++;
                }
            }
            engine.initialRotateDirection = 0;

            // game over check
            if((engine.statc[0] == 0) && (engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY, engine.field))) {
                // Blockの出現位置を上にずらすことができる場合はそうする
                for(int i = 0; i < engine.ruleopt.pieceEnterMaxDistanceY; i++) {
                    if(engine.nowPieceObject.big) engine.nowPieceY -= 2;
                    else engine.nowPieceY--;

                    if(!engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY, engine.field)) {
                        engine.nowPieceBottomY = engine.nowPieceObject.getBottom(engine.nowPieceX, engine.nowPieceY, engine.field);
                        break;
                    }
                }

                // 死亡
                if(engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY, engine.field)) {
                    engine.nowPieceObject.placeToField(engine.nowPieceX, engine.nowPieceY, engine.field);
                    engine.nowPieceObject = null;
                    engine.stat = GameEngine.STAT_GAMEOVER;
                    if((engine.ending == 2) && (engine.staffrollNoDeath)) engine.stat = GameEngine.STAT_NOTHING;
                    engine.resetStatc();
                    return true;
                }
            }

        }

        int move = 0;
        boolean sidemoveflag = false;	// この frame に横移動したらtrue

        if((engine.statc[0] > 0) || (engine.ruleopt.moveFirstFrame)) {
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

            if(move != 0) sidemoveflag = true;

            if(engine.big && engine.bigmove) move *= 2;

            if((move != 0) && (engine.dasCount == 0)) engine.shiftLock = 0;

            if( (move != 0) && ((engine.dasCount == 0) || (engine.dasCount >= engine.getDAS())) ) {
                engine.shiftLock &= engine.ctrl.getButtonBit();

                if(engine.shiftLock == 0) {
                    if( (engine.dasSpeedCount >= engine.getDASDelay()) || (engine.dasCount == 0) ) {
                        if(engine.dasCount > 0) engine.dasSpeedCount = 1;

                        if(!engine.nowPieceObject.checkCollision(engine.nowPieceX + move, engine.nowPieceY, engine.field)) {
                            engine.nowPieceX += move;

                            if((engine.getDASDelay() == 0) && (engine.dasCount > 0) && (!engine.nowPieceObject.checkCollision(engine.nowPieceX + move, engine.nowPieceY, engine.field))) {
                                if(!engine.dasInstant) engine.playSE("move");
                                engine.dasRepeat = true;
                                engine.dasInstant = true;
                            }

                            //log.debug("Successful movement: move="+move);

                            if((engine.ruleopt.lockresetMove) && (!engine.isMoveCountExceed())) {
                                engine.lockDelayNow = 0;
                                engine.nowPieceObject.setDarkness(0f);
                            }

                            engine.nowPieceMoveCount++;
                            if((engine.ending == 0) || (engine.staffrollEnableStatistics)) engine.statistics.totalPieceMove++;
                            engine.nowPieceBottomY = engine.nowPieceObject.getBottom(engine.nowPieceX, engine.nowPieceY, engine.field);

                            if(onGroundBeforeMove) {
                                engine.extendedMoveCount++;
                                engine.lastmove = GameEngine.LASTMOVE_SLIDE_GROUND;
                            } else {
                                engine.lastmove = GameEngine.LASTMOVE_SLIDE_AIR;
                            }

                            if(!engine.dasInstant) engine.playSE("move");

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
            if( (engine.ctrl.isPress(engine.getUp())) &&
                (!engine.harddropContinuousUse) &&
                (engine.ruleopt.harddropEnable) &&
                ((engine.isDiagonalMoveEnabled()) || (!sidemoveflag)) &&
                ((engine.ruleopt.moveUpAndDown) || (!updown)) &&
                (engine.nowPieceY < engine.nowPieceBottomY) )
            {
                engine.harddropFall += engine.nowPieceBottomY - engine.nowPieceY;

                if(engine.nowPieceY != engine.nowPieceBottomY) {
                    engine.nowPieceY = engine.nowPieceBottomY;
                    engine.playSE("harddrop");
                }

                if(owner.mode != null) owner.mode.afterHardDropFall(engine, playerID, engine.harddropFall);
                owner.receiver.afterHardDropFall(engine, playerID, engine.harddropFall);

                engine.lastmove = GameEngine.LASTMOVE_FALL_SELF;
                if(engine.ruleopt.lockresetFall) {
                    engine.lockDelayNow = 0;
                    engine.nowPieceObject.setDarkness(0f);
                    engine.extendedMoveCount = 0;
                    engine.extendedRotateCount = 0;
                }
            }

            if(!engine.ruleopt.softdropGravitySpeedLimit || (engine.ruleopt.softdropSpeed < 1.0f)) {
                // Old Soft Drop codes
                if( (engine.ctrl.isPress(engine.getDown())) &&
                    (!engine.softdropContinuousUse) &&
                    (engine.ruleopt.softdropEnable) &&
                    ((engine.isDiagonalMoveEnabled()) || (!sidemoveflag)) &&
                    ((engine.ruleopt.moveUpAndDown) || (!updown)) )
                {
                    if((engine.ruleopt.softdropMultiplyNativeSpeed) || (engine.speed.denominator <= 0))
                        engine.gcount += (int)(engine.speed.gravity * engine.ruleopt.softdropSpeed);
                    else
                        engine.gcount += (int)(engine.speed.denominator * engine.ruleopt.softdropSpeed);

                    softdropUsed = true;
                }
            } else {
                // New Soft Drop codes
                if( engine.ctrl.isPress(engine.getDown()) && !engine.softdropContinuousUse &&
                    engine.ruleopt.softdropEnable && (engine.isDiagonalMoveEnabled() || !sidemoveflag) &&
                    (engine.ruleopt.moveUpAndDown || !updown) &&
                    (engine.ruleopt.softdropMultiplyNativeSpeed || (engine.speed.gravity < (int)(engine.speed.denominator * engine.ruleopt.softdropSpeed))) )
                {
                    if((engine.ruleopt.softdropMultiplyNativeSpeed) || (engine.speed.denominator <= 0)) {
                        // gcount += (int)(speed.gravity * ruleopt.softdropSpeed);
                        engine.gcount = (int)(engine.speed.gravity * engine.ruleopt.softdropSpeed);
                    } else {
                        // gcount += (int)(speed.denominator * ruleopt.softdropSpeed);
                        engine.gcount = (int)(engine.speed.denominator * engine.ruleopt.softdropSpeed);
                    }

                    softdropUsed = true;
                } else {
                    // 落下
                    // This prevents soft drop from adding to the gravity speed.
                    engine.gcount += engine.speed.gravity;
                }
            }

            if((engine.ending == 0) || (engine.staffrollEnableStatistics)) engine.statistics.totalPieceActiveTime++;
        }

        if(!engine.ruleopt.softdropGravitySpeedLimit || (engine.ruleopt.softdropSpeed < 1.0f))
            engine.gcount += engine.speed.gravity;	// Part of Old Soft Drop

        while((engine.gcount >= engine.speed.denominator) || (engine.speed.gravity < 0)) {
            if(!engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY + 1, engine.field)) {
                if(engine.speed.gravity >= 0) engine.gcount -= engine.speed.denominator;
                engine.nowPieceY++;

                if(engine.ruleopt.lockresetFall) {
                    engine.lockDelayNow = 0;
                    engine.nowPieceObject.setDarkness(0f);
                }

                if((engine.lastmove != GameEngine.LASTMOVE_ROTATE_GROUND) && (engine.lastmove != GameEngine.LASTMOVE_SLIDE_GROUND) && (engine.lastmove != GameEngine.LASTMOVE_FALL_SELF)) {
                    engine.extendedMoveCount = 0;
                    engine.extendedRotateCount = 0;
                }

                if(softdropUsed) {
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

        if(softdropFallNow > 0) {
            if(owner.mode != null) owner.mode.afterSoftDropFall(engine, playerID, softdropFallNow);
            owner.receiver.afterSoftDropFall(engine, playerID, softdropFallNow);
        }

        // 接地と固定
        if( (engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY + 1, engine.field)) &&
            ((engine.statc[0] > 0) || (engine.ruleopt.moveFirstFrame)) )
        {
            if((engine.lockDelayNow == 0) && (engine.getLockDelay() > 0))
                engine.playSE("step");

            if(engine.lockDelayNow < engine.getLockDelay())
                engine.lockDelayNow++;

            if((engine.getLockDelay() >= 99) && (engine.lockDelayNow > 98))
                engine.lockDelayNow = 98;

            if(engine.lockDelayNow < engine.getLockDelay()) {
                if(engine.lockDelayNow >= engine.getLockDelay() - 1)
                    engine.nowPieceObject.setDarkness(0.5f);
                else
                    engine.nowPieceObject.setDarkness((engine.lockDelayNow * 7f / engine.getLockDelay()) * 0.05f);
            }

            if(engine.getLockDelay() != 0)
                engine.gcount = engine.speed.gravity;

            // trueになると即固定
            boolean instantlock = false;

            // Hard drop固定
            if( (engine.ctrl.isPress(engine.getUp())) &&
                (!engine.harddropContinuousUse) &&
                (engine.ruleopt.harddropEnable) &&
                ((engine.isDiagonalMoveEnabled()) || (!sidemoveflag)) &&
                ((engine.ruleopt.moveUpAndDown) || (!updown)) &&
                (engine.ruleopt.harddropLock) )
            {
                engine.harddropContinuousUse = true;
                engine.manualLock = true;
                instantlock = true;
            }

            // Soft drop固定
            if( (engine.ctrl.isPress(engine.getDown())) &&
                (!engine.softdropContinuousUse) &&
                (engine.ruleopt.softdropEnable) &&
                ((engine.isDiagonalMoveEnabled()) || (!sidemoveflag)) &&
                ((engine.ruleopt.moveUpAndDown) || (!updown)) &&
                (engine.ruleopt.softdropLock) )
            {
                engine.softdropContinuousUse = true;
                engine.manualLock = true;
                instantlock = true;
            }

            // 接地状態でソフドドロップ固定
            if( (engine.ctrl.isPush(engine.getDown())) &&
                (engine.ruleopt.softdropEnable) &&
                ((engine.isDiagonalMoveEnabled()) || (!sidemoveflag)) &&
                ((engine.ruleopt.moveUpAndDown) || (!updown)) &&
                (engine.ruleopt.softdropSurfaceLock) )
            {
                engine.softdropContinuousUse = true;
                engine.manualLock = true;
                instantlock = true;
            }

            if((engine.manualLock) && (engine.ruleopt.shiftLockEnable)) {
                // bit 1 and 2 are button_up and button_down currently
                engine.shiftLock = engine.ctrl.getButtonBit() & 3;
            }

            // 移動＆rotationcount制限超過
            if( (engine.ruleopt.lockresetLimitOver == RuleOptions.LOCKRESET_LIMIT_OVER_INSTANT) && (engine.isMoveCountExceed() || engine.isRotateCountExceed()) ) {
                instantlock = true;
            }

            // 接地即固定
            if( (engine.getLockDelay() == 0) && ((engine.gcount >= engine.speed.denominator) || (engine.speed.gravity < 0)) ) {
                instantlock = true;
            }

            // 固定
            if( ((engine.lockDelayNow >= engine.getLockDelay()) && (engine.getLockDelay() > 0)) || (instantlock) ) {
                if(engine.ruleopt.lockflash > 0) engine.nowPieceObject.setDarkness(-0.8f);

                // T-Spin判定
                if(((engine.lastmove == GameEngine.LASTMOVE_ROTATE_GROUND) || (engine.lastmove == GameEngine.LASTMOVE_ROTATE_AIR)) && (engine.tspinEnable)) {
                    if(engine.useAllSpinBonus)
                        engine.setAllSpin(engine.nowPieceX, engine.nowPieceY, engine.nowPieceObject, engine.field);
                    else
                        engine.setTSpin(engine.nowPieceX, engine.nowPieceY, engine.nowPieceObject, engine.field);
                }

                engine.nowPieceObject.setAttribute(Block.BLOCK_ATTRIBUTE_SELFPLACED, true);

                boolean partialLockOut = engine.nowPieceObject.isPartialLockOut(engine.nowPieceX, engine.nowPieceY, engine.field);
                boolean put = engine.nowPieceObject.placeToField(engine.nowPieceX, engine.nowPieceY, engine.field);

                engine.playSE("lock");

                engine.holdDisable = false;
                canSwitchNext = true;

                if((engine.ending == 0) || (engine.staffrollEnableStatistics)) engine.statistics.totalPieceLocked++;

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

                if(engine.lineClearing == 0) {
                    engine.combo = 0;

                    if(engine.tspin) {
                        engine.playSE("tspin0");

                        if((engine.ending == 0) || (engine.staffrollEnableStatistics)) {
                            if(engine.tspinmini) engine.statistics.totalTSpinZeroMini++;
                            else engine.statistics.totalTSpinZero++;
                        }
                    }

                    if(owner.mode != null) owner.mode.calcScore(engine, playerID, engine.lineClearing);
                    owner.receiver.calcScore(engine, playerID, engine.lineClearing);
                }

                if(owner.mode != null) owner.mode.pieceLocked(engine, playerID, engine.lineClearing);
                owner.receiver.pieceLocked(engine, playerID, engine.lineClearing);

                engine.dasRepeat = false;
                engine.dasInstant = false;

                // Next 処理を決める(Mode 側でステータスを弄っている場合は何もしない)
                if((engine.stat == GameEngine.STAT_MOVE) || (engine.versionMajor <= 6.3f)) {
                    engine.resetStatc();

                    if((engine.ending == 1) && (engine.versionMajor >= 6.6f) && (engine.versionMinorOld >= 0.1f)) {
                        // Ending
                        engine.stat = GameEngine.STAT_ENDINGSTART;
                    } else if( (!put && engine.ruleopt.fieldLockoutDeath) || (partialLockOut && engine.ruleopt.fieldPartialLockoutDeath) ) {
                        // 画面外に置いて死亡
                        engine.stat = GameEngine.STAT_GAMEOVER;
                        if((engine.ending == 2) && (engine.staffrollNoDeath)) engine.stat = GameEngine.STAT_NOTHING;
                    } else if ((engine.lineGravityType == GameEngine.LINE_GRAVITY_CASCADE || engine.lineGravityType == GameEngine.LINE_GRAVITY_CASCADE_SLOW)
                        && !engine.connectBlocks) {
                        engine.stat = GameEngine.STAT_LINECLEAR;
                        engine.statc[0] = engine.getLineDelay();
                        engine.statLineClear();
                    } else if( (engine.lineClearing > 0) && ((engine.ruleopt.lockflash <= 0) || (!engine.ruleopt.lockflashBeforeLineClear)) ) {
                        // Line clear
                        engine.stat = GameEngine.STAT_LINECLEAR;
                        engine.statLineClear();
                    } else if( ((engine.getARE() > 0) || (engine.lagARE) || (engine.ruleopt.lockflashBeforeLineClear)) &&
                        (engine.ruleopt.lockflash > 0) && (engine.ruleopt.lockflashOnlyFrame) )
                    {
                        // AREあり (光あり）
                        engine.stat = GameEngine.STAT_LOCKFLASH;
                    } else if((engine.getARE() > 0) || (engine.lagARE)) {
                        // AREあり (光なし）
                        engine.statc[1] = engine.getARE();
                        engine.stat = GameEngine.STAT_ARE;
                    } else if(engine.interruptItemNumber != GameEngine.INTERRUPTITEM_NONE) {
                        // 中断効果のあるアイテム処理
                        engine.nowPieceObject = null;
                        engine.interruptItemPreviousStat = GameEngine.STAT_MOVE;
                        engine.stat = GameEngine.STAT_INTERRUPTITEM;
                    } else {
                        // AREなし
                        engine.stat = GameEngine.STAT_MOVE;
                        if(!engine.ruleopt.moveFirstFrame) engine.statMove();
                    }
                }
                return true;
            }
        }

        // 横溜め
        if((engine.statc[0] > 0) || (engine.ruleopt.dasInMoveFirstFrame)) {
            if( (moveDirection != 0) && (moveDirection == engine.dasDirection) && ((engine.dasCount < engine.getDAS()) || (engine.getDAS() <= 0)) ) {
                engine.dasCount++;
            }
        }

        engine.statc[0]++;

        return true;
    }

    @Override
    public boolean onCustom(GameEngine engine, int playerID) {
        showPlayerStats = false;

        engine.isInGame = true;

        boolean s = playerProperties.loginScreen.updateScreen(engine, playerID);
        if (playerProperties.isLoggedIn()) {
            loadRankingPlayer(playerProperties, engine.ruleopt.strRuleName);
            loadSettingPlayer(playerProperties);
        }

        if (engine.stat == GameEngine.STAT_SETTING) engine.isInGame = false;

        return s;
    }

    private void addReturningPiece(GameEngine engine, int playerID, WhichQueue queue) {
        final Piece copy = new Piece(engine.nowPieceObject);
        copy.resetOffsetArray();
        copy.direction = engine.ruleopt.pieceDefaultDirection[copy.id];
        copy.updateConnectData();

        final int baseX = receiver.getFieldDisplayPositionX(engine, playerID) + 4;
        final int baseY = receiver.getFieldDisplayPositionY(engine, playerID) + 52;

        final int[] source = new int[] {
            baseX + (engine.nowPieceX * 16),
            baseY + (engine.nowPieceY * 16)
        };

        final int offsetX = (queue == WhichQueue.LEFT) ? 0 : (16 * Math.max(copy.getWidth(), copy.getHeight()));

        final int[] target = new int[] {
            ((queue == WhichQueue.LEFT) ? ((baseX - 4) + 8) : ((baseX - 4) - 8 + (engine.field.getWidth() * 16))) - offsetX,
            (baseY - 52) + 40 - ((copy.getMaximumBlockY() + 1) * 16)
        };

        returningPieces.add(new ReturningPiece(copy, source, target));
    }

    private static float pieceSize(int index) {
        if (index <= 0) return 1.0f;
        else return 0.5f;
    }

    @Override
    public void renderLast(GameEngine engine, int playerID) {
        if (owner.menuOnly) return;

        receiver.drawScoreFont(engine, playerID, 0, 0, getName(), EventReceiver.COLOR_ORANGE);

        if (tableGameClearLines[goaltype] == -1) {
            receiver.drawScoreFont(engine, playerID, 0, 1, "(ENDLESS GAME)", EventReceiver.COLOR_ORANGE);
        } else {
            receiver.drawScoreFont(engine, playerID, 0, 1, "(" + tableGameClearLines[goaltype] + " LINES GAME)", EventReceiver.COLOR_ORANGE);
        }

        if ((engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (!owner.replayMode))) {
            if ((!owner.replayMode) && (!big) && (engine.ai == null)) {
                float scale = (receiver.getNextDisplayType() == 2) ? 0.5f : 1.0f;
                int topY = (receiver.getNextDisplayType() == 2) ? 6 : 4;
                receiver.drawScoreFont(engine, playerID, 3, topY - 1, "SCORE  LINE TIME", EventReceiver.COLOR_BLUE, scale);

                if (showPlayerStats) {
                    for (int i = 0; i < RANKING_MAX; i++) {
                        receiver.drawScoreFont(engine, playerID, 0, topY + i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW, scale);
                        receiver.drawScoreFont(engine, playerID, 3, topY + i, String.valueOf(rankingScorePlayer[goaltype][i]), (i == rankingRankPlayer), scale);
                        receiver.drawScoreFont(engine, playerID, 10, topY + i, String.valueOf(rankingLinesPlayer[goaltype][i]), (i == rankingRankPlayer), scale);
                        receiver.drawScoreFont(engine, playerID, 15, topY + i, GeneralUtil.getTime(rankingTimePlayer[goaltype][i]), (i == rankingRankPlayer), scale);
                    }

                    receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 1, "PLAYER SCORES", EventReceiver.COLOR_BLUE);
                    GameTextUtilities.drawAlignedScoreText(receiver, engine, playerID, false, 0, topY + RANKING_MAX + 2, GameTextUtilities.Text.ofBig(owner.replayMode ? playerName : playerProperties.getNameDisplay()));

                    receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 5, "F:SWITCH RANK SCREEN", EventReceiver.COLOR_GREEN);
                } else {
                    for (int i = 0; i < RANKING_MAX; i++) {
                        receiver.drawScoreFont(engine, playerID, 0, topY + i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW, scale);
                        receiver.drawScoreFont(engine, playerID, 3, topY + i, String.valueOf(rankingScore[goaltype][i]), (i == rankingRank), scale);
                        receiver.drawScoreFont(engine, playerID, 10, topY + i, String.valueOf(rankingLines[goaltype][i]), (i == rankingRank), scale);
                        receiver.drawScoreFont(engine, playerID, 15, topY + i, GeneralUtil.getTime(rankingTime[goaltype][i]), (i == rankingRank), scale);
                    }

                    receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 1, "LOCAL SCORES", EventReceiver.COLOR_BLUE);
                    if (!playerProperties.isLoggedIn())
                        receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 2, "(NOT LOGGED IN)\n(E:LOG IN)");
                    if (playerProperties.isLoggedIn())
                        receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 5, "F:SWITCH RANK SCREEN", EventReceiver.COLOR_GREEN);
                }
            }
        } else if (engine.stat == GameEngine.STAT_CUSTOM) {
            playerProperties.loginScreen.renderScreen(receiver, engine, playerID);
        } else {
            receiver.drawScoreFont(engine, playerID, 0, 3, "SCORE", EventReceiver.COLOR_BLUE);
            String strScore;
            if ((lastscore == 0) || (scgettime >= 120)) {
                strScore = String.valueOf(engine.statistics.score);
            } else {
                int cScore = (int) Interpolation.sineStep(previousScore, engine.statistics.score, (double) scgettime / 120.0);
                strScore = cScore + "(+" + lastscore + ")";
            }
            receiver.drawScoreFont(engine, playerID, 0, 4, strScore);

            if (playerProperties.isLoggedIn() || !playerName.isEmpty()) {
                receiver.drawScoreFont(engine, playerID, 0, 15, "PLAYER", EventReceiver.COLOR_BLUE);
                GameTextUtilities.drawAlignedScoreText(receiver, engine, playerID, false, 0, 16, GameTextUtilities.Text.ofBig(owner.replayMode ? playerName : playerProperties.getNameDisplay()));
            }

            receiver.drawScoreFont(engine, playerID, 0, 6, "LINE", EventReceiver.COLOR_BLUE);
            if ((engine.statistics.level >= 19) && (tableGameClearLines[goaltype] < 0))
                receiver.drawScoreFont(engine, playerID, 0, 7, engine.statistics.lines + "");
            else
                receiver.drawScoreFont(engine, playerID, 0, 7, engine.statistics.lines + "/" + ((engine.statistics.level + 1) * 10));

            receiver.drawScoreFont(engine, playerID, 0, 9, "LEVEL", EventReceiver.COLOR_BLUE);
            receiver.drawScoreFont(engine, playerID, 0, 10, String.valueOf(engine.statistics.level + 1));

            receiver.drawScoreFont(engine, playerID, 0, 12, "TIME", EventReceiver.COLOR_BLUE);
            receiver.drawScoreFont(engine, playerID, 0, 13, GeneralUtil.getTime(engine.statistics.time));

            int baseX = receiver.getFieldDisplayPositionX(engine, playerID) + 4;
            int baseY = receiver.getFieldDisplayPositionY(engine, playerID) + 52;
            if (!pCoordList.isEmpty() && cPiece != null && hardDropEffect) {
                for (int[] loc : pCoordList) {
                    int cx = baseX + (16 * loc[0]);
                    int cy = baseY + (16 * loc[1]);
                    rendererExtension.drawScaledPiece(receiver, engine, playerID, cx, cy, cPiece, 1f, 0f);
                }
            }

            // region NEXT

            if (engine.gameStarted) {
                if (selectedNext == WhichQueue.LEFT) {
                    GameTextUtilities.drawAlignedMenuText(
                        receiver, engine, playerID, true,
                        0, -8, GameTextUtilities.Text.ofSmall("NEXT", EventReceiver.COLOR_ORANGE),
                        ObjectAlignment.TOP_LEFT
                    );
                } else {
                    GameTextUtilities.drawAlignedMenuText(
                        receiver, engine, playerID, true,
                        (engine.field.getWidth() * 2), -8, GameTextUtilities.Text.ofSmall("NEXT", EventReceiver.COLOR_ORANGE),
                        ObjectAlignment.TOP_RIGHT
                    );
                }

                final int lastColor = canSwitchNext ? EventReceiver.COLOR_GREEN : EventReceiver.COLOR_WHITE;

                if (lastUsedNext == WhichQueue.LEFT) {
                    GameTextUtilities.drawAlignedMenuText(
                        receiver, engine, playerID, true,
                        0, -9, GameTextUtilities.Text.ofSmall("LAST", lastColor),
                        ObjectAlignment.TOP_LEFT
                    );
                } else {
                    GameTextUtilities.drawAlignedMenuText(
                        receiver, engine, playerID, true,
                        (engine.field.getWidth() * 2), -9, GameTextUtilities.Text.ofSmall("LAST", lastColor),
                        ObjectAlignment.TOP_RIGHT
                    );
                }

                if (!returningPieces.isEmpty()) returningPieces.forEach(rp -> rp.draw(rendererExtension, receiver));

                // Left Next
                if (leftQueue != null) {
                    final float extraDarkness = selectedNext == WhichQueue.LEFT ? 0f : 0.333f;

                    for (int i = 0; i < leftQueue.queue.size(); i++) {
                        final Piece piece = new Piece(leftQueue.queue.get(i));
                        piece.resetOffsetArray();
                        piece.direction = engine.ruleopt.pieceDefaultDirection[piece.id];
                        piece.updateConnectData();

                        int x2, y2;

                        if (i == 0) {
                            x2 = (baseX - 4) + 8;
                            y2 = (baseY - 52) + 40 - ((piece.getMaximumBlockY() + 1) * 16);
                        } else {
                            x2 = (baseX - 4) - 8;
                            y2 = (baseY - 52) + 40 + (40 * (i - 1)) - ((piece.getMaximumBlockY() + 1) * 8);
                        }

                        final ObjectAlignment alignment = i == 0 ? ObjectAlignment.TOP_LEFT : ObjectAlignment.TOP_RIGHT;

                        rendererExtension.drawAlignedScaledPiece(
                            receiver,
                            x2, y2, alignment,
                            piece,
                            pieceSize(i),
                            extraDarkness
                        );
                    }
                }

                // Right Next
                if (rightQueue != null) {
                    final float extraDarkness = selectedNext == WhichQueue.RIGHT ? 0f : 0.333f;

                    for (int i = 0; i < rightQueue.queue.size(); i++) {
                        final Piece piece = new Piece(rightQueue.queue.get(i));
                        piece.resetOffsetArray();
                        piece.direction = engine.ruleopt.pieceDefaultDirection[piece.id];
                        piece.updateConnectData();

                        int x2, y2;

                        if (i == 0) {
                            x2 = (baseX - 4) + (engine.field.getWidth() * 16) - 8;
                            y2 = (baseY - 52) + 40 - ((piece.getMaximumBlockY() + 1) * 16);
                        } else {
                            x2 = (baseX - 4) + (engine.field.getWidth() * 16) + 16;
                            y2 = (baseY - 52) + 40 + (40 * (i - 1)) - ((piece.getMaximumBlockY() + 1) * 8);
                        }

                        final ObjectAlignment alignment = i == 0 ? ObjectAlignment.TOP_RIGHT : ObjectAlignment.TOP_LEFT;

                        rendererExtension.drawAlignedScaledPiece(
                            receiver,
                            x2, y2, alignment,
                            piece,
                            pieceSize(i),
                            extraDarkness
                        );
                    }
                }
            }

            // endregion NEXT

            if ((lastevent != EVENT_NONE) && (scgettime < 120)) {
                String strPieceName = Piece.getPieceName(lastpiece);

                switch (lastevent) {
                    case EVENT_SINGLE:
                        receiver.drawMenuFont(engine, playerID, 2, 21, "SINGLE", EventReceiver.COLOR_DARKBLUE);
                        break;
                    case EVENT_DOUBLE:
                        receiver.drawMenuFont(engine, playerID, 2, 21, "DOUBLE", EventReceiver.COLOR_BLUE);
                        break;
                    case EVENT_TRIPLE:
                        receiver.drawMenuFont(engine, playerID, 2, 21, "TRIPLE", EventReceiver.COLOR_GREEN);
                        break;
                    case EVENT_FOUR:
                        if (lastb2b)
                            receiver.drawMenuFont(engine, playerID, 3, 21, "FOUR", EventReceiver.COLOR_RED);
                        else receiver.drawMenuFont(engine, playerID, 3, 21, "FOUR", EventReceiver.COLOR_ORANGE);
                        break;
                    case EVENT_TSPIN_ZERO_MINI:
                        receiver.drawMenuFont(engine, playerID, 2, 21, strPieceName + "-SPIN", EventReceiver.COLOR_PURPLE);
                        break;
                    case EVENT_TSPIN_ZERO:
                        receiver.drawMenuFont(engine, playerID, 2, 21, strPieceName + "-SPIN", EventReceiver.COLOR_PINK);
                        break;
                    case EVENT_TSPIN_SINGLE_MINI:
                        if (lastb2b)
                            receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-MINI-S", EventReceiver.COLOR_RED);
                        else
                            receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-MINI-S", EventReceiver.COLOR_ORANGE);
                        break;
                    case EVENT_TSPIN_SINGLE:
                        if (lastb2b)
                            receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-SINGLE", EventReceiver.COLOR_RED);
                        else
                            receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-SINGLE", EventReceiver.COLOR_ORANGE);
                        break;
                    case EVENT_TSPIN_DOUBLE_MINI:
                        if (lastb2b)
                            receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-MINI-D", EventReceiver.COLOR_RED);
                        else
                            receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-MINI-D", EventReceiver.COLOR_ORANGE);
                        break;
                    case EVENT_TSPIN_DOUBLE:
                        if (lastb2b)
                            receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-DOUBLE", EventReceiver.COLOR_RED);
                        else
                            receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-DOUBLE", EventReceiver.COLOR_ORANGE);
                        break;
                    case EVENT_TSPIN_TRIPLE:
                        if (lastb2b)
                            receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-TRIPLE", EventReceiver.COLOR_RED);
                        else
                            receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-TRIPLE", EventReceiver.COLOR_ORANGE);
                        break;
                    case EVENT_TSPIN_EZ:
                        if (lastb2b)
                            receiver.drawMenuFont(engine, playerID, 3, 21, "EZ-" + strPieceName, EventReceiver.COLOR_RED);
                        else
                            receiver.drawMenuFont(engine, playerID, 3, 21, "EZ-" + strPieceName, EventReceiver.COLOR_ORANGE);
                        break;
                }

                if ((lastcombo >= 2) && (lastevent != EVENT_TSPIN_ZERO_MINI) && (lastevent != EVENT_TSPIN_ZERO))
                    receiver.drawMenuFont(engine, playerID, 2, 22, (lastcombo - 1) + "COMBO", EventReceiver.COLOR_CYAN);
            }
        }

        // NET: Number of spectators
        netDrawSpectatorsCount(engine, 0, 18);
        // NET: All number of players
        if (playerID == getPlayers() - 1) {
            netDrawAllPlayersCount(engine);
            netDrawGameRate(engine);
        }
        // NET: Player name (It may also appear in offline replay)
        netDrawPlayerName(engine);

        if (landingParticles != null) landingParticles.draw(receiver);
    }

    @Override
    public void calcScore(GameEngine engine, int playerID, int lines) {
        // Line clear bonus
        int pts = 0;

        if (engine.tspin) {
            // T-Spin 0 lines
            if ((lines == 0) && (!engine.tspinez)) {
                if (engine.tspinmini) {
                    pts += 100 * (engine.statistics.level + 1);
                    lastevent = EVENT_TSPIN_ZERO_MINI;
                } else {
                    pts += 400 * (engine.statistics.level + 1);
                    lastevent = EVENT_TSPIN_ZERO;
                }
            }
            // Immobile EZ Spin
            else if (engine.tspinez && (lines > 0)) {
                if (engine.b2b) {
                    pts += 180 * (engine.statistics.level + 1);
                } else {
                    pts += 120 * (engine.statistics.level + 1);
                }
                lastevent = EVENT_TSPIN_EZ;
            }
            // T-Spin 1 line
            else if (lines == 1) {
                if (engine.tspinmini) {
                    if (engine.b2b) {
                        pts += 300 * (engine.statistics.level + 1);
                    } else {
                        pts += 200 * (engine.statistics.level + 1);
                    }
                    lastevent = EVENT_TSPIN_SINGLE_MINI;
                } else {
                    if (engine.b2b) {
                        pts += 1200 * (engine.statistics.level + 1);
                    } else {
                        pts += 800 * (engine.statistics.level + 1);
                    }
                    lastevent = EVENT_TSPIN_SINGLE;
                }
            }
            // T-Spin 2 lines
            else if (lines == 2) {
                if (engine.tspinmini && engine.useAllSpinBonus) {
                    if (engine.b2b) {
                        pts += 600 * (engine.statistics.level + 1);
                    } else {
                        pts += 400 * (engine.statistics.level + 1);
                    }
                    lastevent = EVENT_TSPIN_DOUBLE_MINI;
                } else {
                    if (engine.b2b) {
                        pts += 1800 * (engine.statistics.level + 1);
                    } else {
                        pts += 1200 * (engine.statistics.level + 1);
                    }
                    lastevent = EVENT_TSPIN_DOUBLE;
                }
            }
            // T-Spin 3 lines
            else if (lines >= 3) {
                if (lines == 3) {
                    if (engine.b2b) {
                        pts += 2400 * (engine.statistics.level + 1);
                    } else {
                        pts += 1600 * (engine.statistics.level + 1);
                    }
                } else {
                    pts = getLineScore(lines * 2);
                    if (engine.b2b) pts = (int) (pts * 1.5);
                }

                lastevent = EVENT_TSPIN_TRIPLE;
                if (lines >= 4) engine.playSE("tspin3");
            }
        } else {
            if (lines == 1) {
                pts += 100 * (engine.statistics.level + 1); // 1列
                lastevent = EVENT_SINGLE;
            } else if (lines == 2) {
                pts += 300 * (engine.statistics.level + 1); // 2列
                lastevent = EVENT_DOUBLE;
            } else if (lines == 3) {
                pts += 500 * (engine.statistics.level + 1); // 3列
                lastevent = EVENT_TRIPLE;
            } else if (lines >= 4) {
                // 4 lines
                if (lines == 4) {
                    if (engine.b2b) {
                        pts += 1200 * (engine.statistics.level + 1);
                    } else {
                        pts += 800 * (engine.statistics.level + 1);
                    }
                } else {
                    pts = getLineScore(lines);
                    if (engine.b2b) pts = (int) (pts * 1.5);
                }

                lastevent = EVENT_FOUR;

                if (lines >= 5) engine.playSE("erase4");
            }
        }

        lastb2b = engine.b2b;

        // Combo
        if ((enableCombo) && (engine.combo >= 1) && (lines >= 1)) {
            pts += ((engine.combo - 1) * 50) * (engine.statistics.level + 1);
            lastcombo = engine.combo;
        }

        // All clear
        if ((lines >= 1) && (engine.field.isEmpty())) {
            engine.playSE("bravo");
            pts += 1800 * (engine.statistics.level + 1);
        }

        // Add to score
        if (pts > 0) {
            lastscore = pts;
            lastpiece = engine.nowPieceObject.id;
            scgettime = 0;
            if (lines >= 1) engine.statistics.scoreFromLineClear += pts;
            else engine.statistics.scoreFromOtherBonus += pts;
            previousScore = engine.statistics.score;
            engine.statistics.score += pts;
        }

        // BGM fade-out effects and BGM changes
        if (tableBGMChange[bgmlv] != -1) {
            if (engine.statistics.lines >= tableBGMChange[bgmlv] - 5) owner.bgmStatus.fadesw = true;

            if ((engine.statistics.lines >= tableBGMChange[bgmlv]) &&
                ((engine.statistics.lines < tableGameClearLines[goaltype]) || (tableGameClearLines[goaltype] < 0))) {
                bgmlv++;
                owner.bgmStatus.bgm = bgmlv;
                owner.bgmStatus.fadesw = false;
            }
        }

        // Meter
        engine.meterValue = ((engine.statistics.lines % 10) * receiver.getMeterMax(engine)) / 9;
        engine.meterColor = GameEngine.METER_COLOR_GREEN;
        if (engine.statistics.lines % 10 >= 4) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
        if (engine.statistics.lines % 10 >= 6) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
        if (engine.statistics.lines % 10 >= 8) engine.meterColor = GameEngine.METER_COLOR_RED;

        if ((engine.statistics.lines >= tableGameClearLines[goaltype]) && (tableGameClearLines[goaltype] >= 0)) {
            // Ending
            engine.ending = 1;
            engine.gameEnded();
        } else if ((engine.statistics.lines >= (engine.statistics.level + 1) * 10) && (engine.statistics.level < 19)) {
            // Level up
            engine.statistics.level++;

            currentBackground = engine.statistics.level;

            setSpeed(engine);
            engine.playSE("levelup");
        }
    }

    private void loadSetting(CustomProperties prop) {
        startlevel = prop.getProperty("multinext.startlevel", 0);
        tspinEnableType = prop.getProperty("multinext.tspinEnableType", 1);
        enableTSpin = prop.getProperty("multinext.enableTSpin", true);
        enableTSpinKick = prop.getProperty("multinext.enableTSpinKick", true);
        spinCheckType = prop.getProperty("multinext.spinCheckType", 0);
        tspinEnableEZ = prop.getProperty("multinext.tspinEnableEZ", false);
        enableB2B = prop.getProperty("multinext.enableB2B", true);
        enableCombo = prop.getProperty("multinext.enableCombo", true);
        goaltype = prop.getProperty("multinext.gametype", 0);
        big = prop.getProperty("multinext.big", false);
        version = prop.getProperty("multinext.version", 0);
        hardDropEffect = prop.getProperty("multinext.hardDropEffect", true);
        auditoryASMR = prop.getProperty("multinext.asmr", true);
    }

    private void saveSetting(CustomProperties prop) {
        prop.setProperty("multinext.startlevel", startlevel);
        prop.setProperty("multinext.tspinEnableType", tspinEnableType);
        prop.setProperty("multinext.enableTSpin", enableTSpin);
        prop.setProperty("multinext.enableTSpinKick", enableTSpinKick);
        prop.setProperty("multinext.spinCheckType", spinCheckType);
        prop.setProperty("multinext.tspinEnableEZ", tspinEnableEZ);
        prop.setProperty("multinext.enableB2B", enableB2B);
        prop.setProperty("multinext.enableCombo", enableCombo);
        prop.setProperty("multinext.gametype", goaltype);
        prop.setProperty("multinext.big", big);
        prop.setProperty("multinext.version", version);
        prop.setProperty("multinext.hardDropEffect", hardDropEffect);
        prop.setProperty("multinext.asmr", auditoryASMR);
    }

    private void loadSettingPlayer(ProfileProperties prop) {
        if (!prop.isLoggedIn()) return;

        startlevel = prop.getProperty("multinext.startlevel", 0);
        tspinEnableType = prop.getProperty("multinext.tspinEnableType", 1);
        enableTSpin = prop.getProperty("multinext.enableTSpin", true);
        enableTSpinKick = prop.getProperty("multinext.enableTSpinKick", true);
        spinCheckType = prop.getProperty("multinext.spinCheckType", 0);
        tspinEnableEZ = prop.getProperty("multinext.tspinEnableEZ", false);
        enableB2B = prop.getProperty("multinext.enableB2B", true);
        enableCombo = prop.getProperty("multinext.enableCombo", true);
        goaltype = prop.getProperty("multinext.gametype", 0);
        big = prop.getProperty("multinext.big", false);
        hardDropEffect = prop.getProperty("multinext.hardDropEffect", true);
        auditoryASMR = prop.getProperty("multinext.asmr", true);
    }

    private void saveSettingPlayer(ProfileProperties prop) {
        if (!prop.isLoggedIn()) return;

        prop.setProperty("multinext.startlevel", startlevel);
        prop.setProperty("multinext.tspinEnableType", tspinEnableType);
        prop.setProperty("multinext.enableTSpin", enableTSpin);
        prop.setProperty("multinext.enableTSpinKick", enableTSpinKick);
        prop.setProperty("multinext.spinCheckType", spinCheckType);
        prop.setProperty("multinext.tspinEnableEZ", tspinEnableEZ);
        prop.setProperty("multinext.enableB2B", enableB2B);
        prop.setProperty("multinext.enableCombo", enableCombo);
        prop.setProperty("multinext.gametype", goaltype);
        prop.setProperty("multinext.big", big);
        prop.setProperty("multinext.hardDropEffect", hardDropEffect);
        prop.setProperty("multinext.asmr", auditoryASMR);
    }

    @Override
    protected void loadRanking(CustomProperties prop, String ruleName) {
        for (int i = 0; i < RANKING_MAX; i++) {
            for (int j = 0; j < GAMETYPE_MAX; j++) {
                rankingScore[j][i] = prop.getProperty("multinext.ranking." + ruleName + "." + j + ".score." + i, 0);
                rankingLines[j][i] = prop.getProperty("multinext.ranking." + ruleName + "." + j + ".lines." + i, 0);
                rankingTime[j][i] = prop.getProperty("multinext.ranking." + ruleName + "." + j + ".time." + i, 0);
            }
        }
    }

    private void saveRanking(CustomProperties prop, String ruleName) {
        for (int i = 0; i < RANKING_MAX; i++) {
            for (int j = 0; j < GAMETYPE_MAX; j++) {
                prop.setProperty("multinext.ranking." + ruleName + "." + j + ".score." + i, rankingScore[j][i]);
                prop.setProperty("multinext.ranking." + ruleName + "." + j + ".lines." + i, rankingLines[j][i]);
                prop.setProperty("multinext.ranking." + ruleName + "." + j + ".time." + i, rankingTime[j][i]);
            }
        }
    }

    private void loadRankingPlayer(ProfileProperties prop, String ruleName) {
        if (!prop.isLoggedIn()) return;
        for (int i = 0; i < RANKING_MAX; i++) {
            for (int j = 0; j < GAMETYPE_MAX; j++) {
                rankingScorePlayer[j][i] = prop.getProperty("multinext.ranking." + ruleName + "." + j + ".score." + i, 0);
                rankingLinesPlayer[j][i] = prop.getProperty("multinext.ranking." + ruleName + "." + j + ".lines." + i, 0);
                rankingTimePlayer[j][i] = prop.getProperty("multinext.ranking." + ruleName + "." + j + ".time." + i, 0);
            }
        }
    }

    private void saveRankingPlayer(ProfileProperties prop, String ruleName) {
        if (!prop.isLoggedIn()) return;
        for (int i = 0; i < RANKING_MAX; i++) {
            for (int j = 0; j < GAMETYPE_MAX; j++) {
                prop.setProperty("multinext.ranking." + ruleName + "." + j + ".score." + i, rankingScorePlayer[j][i]);
                prop.setProperty("multinext.ranking." + ruleName + "." + j + ".lines." + i, rankingLinesPlayer[j][i]);
                prop.setProperty("multinext.ranking." + ruleName + "." + j + ".time." + i, rankingTimePlayer[j][i]);
            }
        }
    }

    private void updateRanking(int sc, int li, int time, int type) {
        rankingRank = checkRanking(sc, li, time, type);

        if (rankingRank != -1) {
            // Shift down ranking entries
            for (int i = RANKING_MAX - 1; i > rankingRank; i--) {
                rankingScore[type][i] = rankingScore[type][i - 1];
                rankingLines[type][i] = rankingLines[type][i - 1];
                rankingTime[type][i] = rankingTime[type][i - 1];
            }

            // Add new data
            rankingScore[type][rankingRank] = sc;
            rankingLines[type][rankingRank] = li;
            rankingTime[type][rankingRank] = time;
        }

        if (playerProperties.isLoggedIn()) {
            rankingRankPlayer = checkRankingPlayer(sc, li, time, type);

            if (rankingRankPlayer != -1) {
                // Shift down ranking entries
                for (int i = RANKING_MAX - 1; i > rankingRankPlayer; i--) {
                    rankingScorePlayer[type][i] = rankingScorePlayer[type][i - 1];
                    rankingLinesPlayer[type][i] = rankingLinesPlayer[type][i - 1];
                    rankingTimePlayer[type][i] = rankingTimePlayer[type][i - 1];
                }

                // Add new data
                rankingScorePlayer[type][rankingRankPlayer] = sc;
                rankingLinesPlayer[type][rankingRankPlayer] = li;
                rankingTimePlayer[type][rankingRankPlayer] = time;
            }
        }
    }

    private int checkRanking(int sc, int li, int time, int type) {
        for (int i = 0; i < RANKING_MAX; i++) {
            if (sc > rankingScore[type][i]) {
                return i;
            } else if ((sc == rankingScore[type][i]) && (li > rankingLines[type][i])) {
                return i;
            } else if ((sc == rankingScore[type][i]) && (li == rankingLines[type][i]) && (time < rankingTime[type][i])) {
                return i;
            }
        }

        return -1;
    }

    private int checkRankingPlayer(int sc, int li, int time, int type) {
        for (int i = 0; i < RANKING_MAX; i++) {
            if (sc > rankingScorePlayer[type][i]) {
                return i;
            } else if ((sc == rankingScorePlayer[type][i]) && (li > rankingLinesPlayer[type][i])) {
                return i;
            } else if ((sc == rankingScorePlayer[type][i]) && (li == rankingLinesPlayer[type][i]) && (time < rankingTimePlayer[type][i])) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public void saveReplay(GameEngine engine, int playerID, CustomProperties prop) {
        saveSetting(prop);

        // NET: Save name
        if ((netPlayerName != null) && (!netPlayerName.isEmpty())) {
            prop.setProperty(playerID + ".net.netPlayerName", netPlayerName);
        }

        // Update rankings
        if ((!owner.replayMode) && (!big) && (engine.ai == null)) {
            updateRanking(engine.statistics.score, engine.statistics.lines, engine.statistics.time, goaltype);

            if (playerProperties.isLoggedIn()) {
                prop.setProperty("multinext.playerName", playerProperties.getNameDisplay());
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

    private int getLineScore(int lines) {
        int x = 1;
        if (lines == 0) x = 0;

        if (lines >= 2) {
            for (int i = 2; i <= lines; i++) {
                x += (i / 2) + 1;
            }
        }

        return x * 100;
    }
}