package zeroxfc.nullpo.custom.modes;

import java.util.Random;
import mu.nu.nullpo.game.component.BGMStatus;
import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.util.CustomProperties;
import mu.nu.nullpo.util.GeneralUtil;
import zeroxfc.nullpo.custom.libs.FlyInOutText;
import zeroxfc.nullpo.custom.libs.ProfileProperties;
import zeroxfc.nullpo.custom.libs.CustomResourceHolder;
import zeroxfc.nullpo.custom.libs.RendererExtension;
import zeroxfc.nullpo.custom.libs.backgroundtypes.AnimatedBackgroundHook;
import zeroxfc.nullpo.custom.libs.backgroundtypes.BackgroundDiagonalRipple;
import zeroxfc.nullpo.custom.libs.backgroundtypes.BackgroundVerticalBars;
import zeroxfc.nullpo.custom.libs.particles.BlockParticleCollection;

public class Joker extends MarathonModeBase {
    // Speed Tables
    private static final int[] ARE_TABLE = { 15, 15, 15, 15, 14, 14,
        13, 12, 11, 10, 9,
        8, 7, 6, 5, 15,
        13, 10, 10, 9, 9,
        8, 8, 7, 6, 5 };
    private static final int[] LOCK_TABLE = { 30, 29, 28, 27, 26, 25,
        24, 23, 22, 21, 20,
        19, 18, 17, 17, 30,
        27, 25, 23, 21, 20,
        19, 18, 17, 16, 15 };

    // Levels for speed changes
    private static final int[] LEVEL_ARE_LOCK_CHANGE = { 60, 70, 80, 90, 100,
        110, 120, 130, 140, 150,
        160, 170, 180, 190, 200,
        210, 220, 230, 240, 250,
        260, 270, 280, 290, 300,
        2000000000 };

    // Timer constants
    private static final int STARTING_TIMER = 7200;
    private static final int LEVEL_TIMEBONUS = 900;
    private static final int TIMER_MAX = 18000;

    private static final float[] GRADE_BOUNDARIES = {
        0f,      // F
        0.25f,   // E
        0.375f,  // D
        0.5f,    // C
        0.65f,   // B
        0.75f,   // A
        0.90f,   // S
        1.0f     // PF
    };
    private static final int headerColour = EventReceiver.COLOR_RED;
    // Ingame Timer
    private int mainTimer;
    // should use timer?
    private boolean shouldUseTimer;
    // Tetris line clear lifeline
    private int stock;
    // starting stock
    private int startingStock;
    // Ranking stuff
    private int[] rankingLevel;
    private int[] rankingTime;
    private int[] rankingLines;
    // time score
    private int timeScore;
    // Local randomiser
    private Random localRandom;
    // Particle stuff
    private BlockParticleCollection blockParticles;
    // ANIMATION TYPE
    private int lineClearAnimType;
    // Warning texts
    private FlyInOutText warningText;
    private FlyInOutText warningTextSecondLine;
    // Custom asset variables
    private int jevilTimer;
    private CustomResourceHolder customHolder;
    private RendererExtension rendererExtension;
    private boolean jSpeed;
    private boolean drawJevil;
    private float efficiency;
    private int efficiencyGrade;
    // PROFILE
    private ProfileProperties playerProperties;
    private boolean showPlayerStats;
    private int rankingRankPlayer;
    private int[] rankingLevelPlayer;
    private int[] rankingTimePlayer;
    private int[] rankingLinesPlayer;
    private String PLAYER_NAME;

    // Last amount of lines cleared;
    private int lastLine;

    // Animated backgrounds
    private boolean useAnimBG;
    private AnimatedBackgroundHook[] ANIMATED_BACKGROUNDS;

    @Override
    public String getName() {
        return "JOKER";
    }

    /*
     * Initialization
     */
    @Override
    public void playerInit(GameEngine engine, int playerID) {
        if (ANIMATED_BACKGROUNDS == null) {
            ANIMATED_BACKGROUNDS = new AnimatedBackgroundHook[] {
                new BackgroundVerticalBars(18, 60, 160, 1f, 4f, false),
                new BackgroundDiagonalRipple(19, 8, 8, 60, 1f, 2f, false, false)
            };
        }

        jevilTimer = 0;
        efficiency = 0f;
        efficiencyGrade = 0;
        jSpeed = false;
        drawJevil = false;
        owner = engine.owner;
        receiver = engine.owner.receiver;
        lastscore = 0;
        scgettime = 0;
        lastevent = EVENT_NONE;
        lastb2b = false;
        lastcombo = 0;
        lastpiece = 0;
        lineClearAnimType = 0;
        shouldUseTimer = false;
        stock = 0;
        startingStock = 0;
        lastLine = 0;
        useAnimBG = false;

        mainTimer = 0;
        warningText = null;
        warningTextSecondLine = null;

        rankingRank = -1;
        rankingLevel = new int[RANKING_MAX];
        rankingLines = new int[RANKING_MAX];
        rankingTime = new int[RANKING_MAX];

        if (playerProperties == null) {
            playerProperties = new ProfileProperties(headerColour);

            showPlayerStats = false;
        }

        rankingRankPlayer = -1;
        rankingLevelPlayer = new int[RANKING_MAX];
        rankingLinesPlayer = new int[RANKING_MAX];
        rankingTimePlayer = new int[RANKING_MAX];

        netPlayerInit(engine, playerID);

        if (!owner.replayMode) {
            loadSetting(owner.modeConfig);
            loadRanking(owner.modeConfig, engine.ruleopt.strRuleName);

            if (playerProperties.isLoggedIn()) {
                loadSettingPlayer(playerProperties);
                loadRankingPlayer(playerProperties, engine.ruleopt.strRuleName);
            }

            version = CURRENT_VERSION + 1;
            PLAYER_NAME = "";
        } else {
            loadSetting(owner.replayProp);
            if ((version == 0) && (owner.replayProp.getProperty("joker.endless", false))) goaltype = 2;

            // NET: Load name
            netPlayerName = engine.owner.replayProp.getProperty(playerID + ".net.netPlayerName", "");
            PLAYER_NAME = owner.replayProp.getProperty("joker.playerName", "");
        }

        engine.owner.backgroundStatus.bg = 18;
        engine.framecolor = GameEngine.FRAME_COLOR_PURPLE;
        engine.tspinEnable = false;
        engine.comboType = GameEngine.COMBO_TYPE_NORMAL;
        engine.b2b = false;

        engine.statistics.levelDispAdd = 0;

        engine.speed.gravity = -1;
        engine.speed.das = 6;
        engine.speed.denominator = 256;
        engine.speed.are = 15;
        engine.speed.areLine = 15;
        engine.speed.lineDelay = 0;
        engine.blockShowOutlineOnly = true;

        engine.bighalf = true;
        engine.bigmove = true;

        // 92 x 96
        customHolder = new CustomResourceHolder();
        customHolder.loadImage("res/graphics/jevil.png", "jevil");
        customHolder.loadImage("res/graphics/efficiency_grades.png", "grades");

        rendererExtension = new RendererExtension(customHolder);
    }

    /**
     * Set the gravity rate
     *
     * @param engine GameEngine
     */
    public void setSpeed(GameEngine engine) {
        if ((engine.statistics.level) >= 60) {
            int ldAreIndex = 0;
            while ((engine.statistics.level) >= LEVEL_ARE_LOCK_CHANGE[ldAreIndex]) ldAreIndex++;

            engine.speed.are = ARE_TABLE[ldAreIndex];
            engine.speed.areLine = ARE_TABLE[ldAreIndex];
            engine.speed.lockDelay = LOCK_TABLE[ldAreIndex];
        }
    }

    private void calculateEfficiencyGrade(GameEngine engine) {
        efficiency = (float) engine.statistics.lines / ((engine.statistics.level - 50) * 4);
        for (int i = 0; i < GRADE_BOUNDARIES.length; i++) {
            if (efficiency >= GRADE_BOUNDARIES[i]) efficiencyGrade = i;
        }
    }

    /*
     * Called at settings screen
     */
    @Override
    public boolean onSetting(GameEngine engine, int playerID) {
        engine.owner.bgmStatus.bgm = -1;
        while (customHolder.getAmountManagedLoadedBGM() > 0) customHolder.removeBGMFromEnd(true);

        // NET: Net Ranking
        if (netIsNetRankingDisplayMode) {
            netOnUpdateNetPlayRanking(engine, goaltype);
        }
        // Menu
        else if (!engine.owner.replayMode) {
            // Configuration changes
            int change = updateCursor(engine, 4, playerID);

            if (change != 0) {
                engine.playSE("change");

                switch (engine.statc[2]) {
                    case 0:
                        startingStock += change;
                        if (startingStock > 25) startingStock = 0;
                        if (startingStock < 0) startingStock = 25;
                        break;
                    case 1:
                        big = !big;
                        break;
                    case 2:
                        lineClearAnimType += change;
                        if (lineClearAnimType > BlockParticleCollection.ANIMATION_TYPES - 1) lineClearAnimType = 0;
                        if (lineClearAnimType < 0) lineClearAnimType = BlockParticleCollection.ANIMATION_TYPES - 1;
                        break;
                    case 3:
                        drawJevil = !drawJevil;
                        break;
                    case 4:
                        useAnimBG = !useAnimBG;
                        break;
                }

                // NET: Signal options change
                if (netIsNetPlay && (netNumSpectators > 0)) {
                    netSendOptions(engine);
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


                // NET: Signal start of the game
                if (netIsNetPlay) netLobby.netPlayerClient.send("start1p\n");

                return false;
            }

            // Cancel
            if (engine.ctrl.isPush(Controller.BUTTON_B) && !netIsNetPlay) {
                engine.quitflag = true;
                playerProperties = new ProfileProperties(headerColour);
            }

            // New acc
            if (engine.ctrl.isPush(Controller.BUTTON_E) && engine.ai == null && !netIsNetPlay) {
                playerProperties = new ProfileProperties(headerColour);
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

    /*
     * Render the settings screen
     */
    @Override
    public void renderSetting(GameEngine engine, int playerID) {
        String lc = "";
        switch (lineClearAnimType) {
            case 0:
                lc = "DTET";
                break;
            case 1:
                lc = "TGM";
                break;
        }
        if (netIsNetRankingDisplayMode) {
            // NET: Netplay Ranking
            netOnRenderNetPlayRanking(engine, playerID, receiver);
        } else {
            drawMenu(engine, playerID, receiver, 0, EventReceiver.COLOR_RED, 0,
                "ST. STOCK", String.valueOf(startingStock));
            drawMenu(engine, playerID, receiver, 2, EventReceiver.COLOR_BLUE, 1,
                "BIG", GeneralUtil.getONorOFF(big),
                "L.C. ANIM.", lc,
                "DRAW JEVIL", GeneralUtil.getONorOFF(drawJevil),
                "ANIM. BGS.", GeneralUtil.getONorOFF(useAnimBG));
        }
    }

    @Override
    public boolean onReady(GameEngine engine, int playerID) {
        mainTimer = STARTING_TIMER;
        engine.statistics.level = 50;
        stock = startingStock;
        timeScore = 0;

        efficiencyGrade = 0;
        efficiency = 0f;

        engine.lives = 0;

        if (blockParticles != null) {
            blockParticles = null;
        }

        if (engine.statc[0] == 0 && useAnimBG) {
            engine.owner.backgroundStatus.bg = -2;
            for (AnimatedBackgroundHook bg : ANIMATED_BACKGROUNDS) {
                bg.reset();
            }
        }

        if (engine.statc[0] == 0 && drawJevil) {
            customHolder.loadNewBGMAppend("res/bgm/joker.ogg", false, true);
        }

        return false;
    }

    /*
     * Called for initialization during "Ready" screen
     */
    @Override
    public void startGame(GameEngine engine, int playerID) {
        engine.statistics.levelDispAdd = 0;

        engine.ruleopt.areCancelHold = true;
        engine.ruleopt.areCancelMove = true;
        engine.ruleopt.areCancelRotate = true;

        engine.ruleopt.harddropEnable = true;
        engine.ruleopt.softdropSurfaceLock = true;
        engine.ruleopt.softdropEnable = true;

        shouldUseTimer = true;
        engine.speed.lineDelay = 0;
        lastLine = 0;

        jevilTimer = 0;

        engine.ruleopt.rotateButtonAllowDouble = true;
        engine.ghost = false;
        engine.big = big;

        // scoreBeforeIncrease = 0;
        engine.speed.gravity = -1;
        engine.speed.das = 6;
        engine.speed.denominator = 256;
        engine.speed.are = 15;
        engine.speed.areLine = 15;
        engine.blockShowOutlineOnly = true;

        engine.blockOutlineType = GameEngine.BLOCK_OUTLINE_NORMAL;

        engine.bighalf = true;
        engine.bigmove = true;

        int total = (engine.field.getHeight() + engine.field.getHiddenHeight()) * engine.field.getWidth() * 2;
        blockParticles = new BlockParticleCollection(rendererExtension, total, lineClearAnimType);

        localRandom = new Random(engine.randSeed);

        // l = STARTING_LIVES + lifeOffset + 1;

        // o = false;

        setSpeed(engine);

        if (drawJevil) {
            owner.bgmStatus.bgm = 16;
            // customHolder.playCustomBGM(0, false);
        } else {
            owner.bgmStatus.bgm = 15;
        }
        owner.bgmStatus.fadesw = false;

        if (netIsWatch) {
            owner.bgmStatus.bgm = BGMStatus.BGM_NOTHING;
        }
    }

    @Override
    public void renderFirst(GameEngine engine, int playerID) {
        if (useAnimBG && engine.owner.backgroundStatus.bg < 0) {
            ANIMATED_BACKGROUNDS[engine.owner.backgroundStatus.bg + 2].draw(engine, playerID);
        }
    }

    /*
     * Render score
     */
    @Override
    public void renderLast(GameEngine engine, int playerID) {
        if (owner.menuOnly) return;

        receiver.drawScoreFont(engine, playerID, 0, 0, getName(), EventReceiver.COLOR_RED);
        receiver.drawScoreFont(engine, playerID, 0, 1, "(FINAL TIER)", EventReceiver.COLOR_RED);

        if ((engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (!owner.replayMode))) {
            if ((!owner.replayMode) && (!big) && (engine.ai == null) && (startingStock == 0)) {
                float scale = (receiver.getNextDisplayType() == 2) ? 0.5f : 1.0f;
                int topY = (receiver.getNextDisplayType() == 2) ? 6 : 4;
                receiver.drawScoreFont(engine, playerID, 3, topY - 1, "LEVEL  LINE TIME", EventReceiver.COLOR_BLUE, scale);

                if (showPlayerStats) {
                    for (int i = 0; i < RANKING_MAX; i++) {
                        receiver.drawScoreFont(engine, playerID, 0, topY + i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW, scale);
                        String s = String.valueOf(rankingLevelPlayer[i]);
                        receiver.drawScoreFont(engine, playerID, (s.length() > 6 && receiver.getNextDisplayType() != 2) ? 6 : 3, (s.length() > 6 && receiver.getNextDisplayType() != 2) ? (topY + i) * 2 : (topY + i), s, (i == rankingRankPlayer), (s.length() > 6 && receiver.getNextDisplayType() != 2) ? scale * 0.5f : scale);
                        receiver.drawScoreFont(engine, playerID, 10, topY + i, String.valueOf(rankingLinesPlayer[i]), (i == rankingRankPlayer), scale);
                        receiver.drawScoreFont(engine, playerID, 15, topY + i, GeneralUtil.getTime(rankingTimePlayer[i]), (i == rankingRankPlayer), scale);
                    }

                    receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 1, "PLAYER SCORES", EventReceiver.COLOR_BLUE);
                    receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 2, playerProperties.getNameDisplay(), EventReceiver.COLOR_WHITE, 2f);

                    receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 5, "F:SWITCH RANK SCREEN", EventReceiver.COLOR_GREEN);
                } else {
                    for (int i = 0; i < RANKING_MAX; i++) {
                        receiver.drawScoreFont(engine, playerID, 0, topY + i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW, scale);
                        String s = String.valueOf(rankingLevel[i]);
                        receiver.drawScoreFont(engine, playerID, (s.length() > 6 && receiver.getNextDisplayType() != 2) ? 6 : 3, (s.length() > 6 && receiver.getNextDisplayType() != 2) ? (topY + i) * 2 : (topY + i), s, (i == rankingRank), (s.length() > 6 && receiver.getNextDisplayType() != 2) ? scale * 0.5f : scale);
                        receiver.drawScoreFont(engine, playerID, 10, topY + i, String.valueOf(rankingLines[i]), (i == rankingRank), scale);
                        receiver.drawScoreFont(engine, playerID, 15, topY + i, GeneralUtil.getTime(rankingTime[i]), (i == rankingRank), scale);
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
            if (customHolder != null && drawJevil && engine.timerActive)
                customHolder.drawImage(engine, "jevil", 548, 0, 92 * (jSpeed ? (jevilTimer % 9) : (jevilTimer / 3)), 0, 92, 96, 255, 255, 255, 255, 1.0f);

            receiver.drawScoreFont(engine, playerID, 0, 3, "TIME", EventReceiver.COLOR_BLUE);
            String strScore;
            strScore = GeneralUtil.getTime(timeScore) + "(+" + GeneralUtil.getTime(engine.statistics.time - timeScore) + ")";
            receiver.drawScoreFont(engine, playerID, 0, 4, strScore);

            receiver.drawScoreFont(engine, playerID, 0, 6, "LINE", EventReceiver.COLOR_BLUE);
            receiver.drawScoreFont(engine, playerID, 0, 7, engine.statistics.lines + "");

            receiver.drawScoreFont(engine, playerID, 0, 9, "LEVEL", EventReceiver.COLOR_BLUE);
            receiver.drawScoreFont(engine, playerID, 0, 10, String.valueOf(engine.statistics.level));

            String b = "";
            if (scgettime < 120 && lastLine != 0) {
                if (lastLine >= 4 && engine.statistics.level <= 200) {
                    b = "(+1)";
                } else if (engine.statistics.level > 200 && lastLine < 4) {
                    b = "(-1)";
                }
            }
            receiver.drawScoreFont(engine, playerID, 0, 12, "STOCK", EventReceiver.COLOR_GREEN);
            receiver.drawScoreFont(engine, playerID, 0, 13, stock + b, stock <= 2);

            receiver.drawScoreFont(engine, playerID, 0, 15, "EFFICIENCY", EventReceiver.COLOR_GREEN);
            receiver.drawScoreFont(engine, playerID, 0, 16, String.format("%.2f", efficiency * 100) + "%", engine.statistics.level >= 300, EventReceiver.COLOR_WHITE, EventReceiver.COLOR_PINK);

            if (playerProperties.isLoggedIn() || PLAYER_NAME.length() > 0) {
                receiver.drawScoreFont(engine, playerID, 0, 18, "PLAYER", EventReceiver.COLOR_BLUE);
                receiver.drawScoreFont(engine, playerID, 0, 19, owner.replayMode ? PLAYER_NAME : playerProperties.getNameDisplay(), EventReceiver.COLOR_WHITE, 2f);
            }

            if (shouldUseTimer) {
                receiver.drawMenuFont(engine, playerID, 0, 21, "TIME LIMIT", EventReceiver.COLOR_RED);
                receiver.drawMenuFont(engine, playerID, 1, 22, GeneralUtil.getTime(mainTimer), (mainTimer <= 600 && ((mainTimer / 2) % 2 == 0)));
            }

            if (blockParticles != null) blockParticles.drawAll(engine, receiver, playerID);

            if (warningText != null) {
                warningText.draw(engine, receiver, playerID);
            }

            if (warningTextSecondLine != null) {
                warningTextSecondLine.draw(engine, receiver, playerID);
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
    }

    @Override
    public boolean onGameOver(GameEngine engine, int playerID) {
        shouldUseTimer = false;
        if (engine.statc[0] == 0 && drawJevil) {
            customHolder.stopCustomBGM();
            while (customHolder.getAmountManagedLoadedBGM() > 0) customHolder.removeBGMFromEnd(true);
        }

        return false;
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

    /*
     * Called after every frame
     */
    @Override
    public void onLast(GameEngine engine, int playerID) {
        scgettime++;
        jevilTimer = (jevilTimer + 1) % 27;

        if (useAnimBG && engine.owner.backgroundStatus.bg < 0) {
            ANIMATED_BACKGROUNDS[engine.owner.backgroundStatus.bg + 2].update();
        }

        if (engine.gameStarted && engine.ending == 0) {
            jSpeed = engine.field.getHighestBlockY() < 4;

            if (engine.stat == GameEngine.STAT_ARE) {
                engine.dasCount = engine.speed.das;
            }

            if (shouldUseTimer && engine.stat != GameEngine.STAT_GAMEOVER) {
                mainTimer--;

                if ((mainTimer <= 600) && (mainTimer % 60 == 0)) {
                    receiver.playSE("countdown");
                }
            }

            // Meter - use as timer.
            if (engine.statistics.level < 200) {

                engine.meterValue = (int) ((mainTimer / (double) TIMER_MAX) * receiver.getMeterMax(engine));
                engine.meterColor = GameEngine.METER_COLOR_GREEN;
                if (mainTimer > 3600 && mainTimer < 5400) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
                if (mainTimer <= 3600 && mainTimer > 1800) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
                if (mainTimer <= 1800) engine.meterColor = GameEngine.METER_COLOR_RED;
            } else {
                int meterMax = 20;

                engine.meterValue = (int) ((stock / (double) meterMax) * receiver.getMeterMax(engine));
                engine.meterColor = GameEngine.METER_COLOR_GREEN;
                if (stock <= 15 && stock > 10) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
                if (stock <= 10 && stock > 5) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
                if (stock <= 5) engine.meterColor = GameEngine.METER_COLOR_RED;
            }


            if (mainTimer <= 0) {
                if (engine.statistics.level < 200) {
                    engine.playSE("died");
                    engine.lives = 0;
                    engine.stat = GameEngine.STAT_GAMEOVER;
                    engine.resetStatc();
                    engine.gameEnded();
                }
            }
        }

        if (engine.gameStarted && blockParticles != null) {
            blockParticles.update();
        }

        if (engine.gameStarted && warningText != null) {
            warningText.update();
            if (warningText.shouldPurge()) {
                warningText = null;
            }
        }

        if (engine.gameStarted && warningTextSecondLine != null) {
            warningTextSecondLine.update();
            if (warningTextSecondLine.shouldPurge()) {
                warningTextSecondLine = null;
            }
        }

        if (stock < 0) {
            engine.playSE("died");
            stock = 0;
            engine.lives = 0;
            engine.stat = GameEngine.STAT_GAMEOVER;
            engine.resetStatc();
            engine.gameEnded();
        }

        if ((engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (!owner.replayMode)) || engine.stat == GameEngine.STAT_CUSTOM) {
            // Show rank
            if (engine.ctrl.isPush(Controller.BUTTON_F) && playerProperties.isLoggedIn() && engine.stat != GameEngine.STAT_CUSTOM) {
                showPlayerStats = !showPlayerStats;
                engine.playSE("change");
            }
        }

        if (engine.quitflag) {
            playerProperties = new ProfileProperties(headerColour);
            while (customHolder.getAmountManagedLoadedBGM() > 0) customHolder.removeBGMFromEnd(true);
        }
    }

    @Override
    public boolean onLineClear(GameEngine engine, int playerID) {
        //  event 発生
        owner.receiver.onLineClear(engine, playerID);

        engine.checkDropContinuousUse();

        // 横溜め
        if (engine.ruleopt.dasInLineClear) engine.padRepeat();
        else if (engine.ruleopt.dasRedirectInDelay) {
            engine.dasRedirect();
        }

        // 最初の frame
        if (engine.statc[0] == 0) {
            // Line clear flagを設定
            if (engine.clearMode == GameEngine.CLEAR_LINE)
                engine.lineClearing = engine.field.checkLine();
                // Set color clear flags
            else if (engine.clearMode == GameEngine.CLEAR_COLOR)
                engine.lineClearing = engine.field.checkColor(engine.colorClearSize, true, engine.garbageColorClear, engine.gemSameColor, engine.ignoreHidden);
                // Set line color clear flags
            else if (engine.clearMode == GameEngine.CLEAR_LINE_COLOR)
                engine.lineClearing = engine.field.checkLineColor(engine.colorClearSize, true, engine.lineColorDiagonals, engine.gemSameColor);
            else if (engine.clearMode == GameEngine.CLEAR_GEM_COLOR)
                engine.lineClearing = engine.field.gemColorCheck(engine.colorClearSize, true, engine.garbageColorClear, engine.ignoreHidden);

            // Linescountを決める
            int li = engine.lineClearing;
            if (big && engine.bighalf)
                li >>= 1;
            //if(li > 4) li = 4;

            if (engine.tspin) {
                engine.playSE("tspin" + li);

                if ((engine.ending == 0) || (engine.staffrollEnableStatistics)) {
                    if ((li == 1) && (engine.tspinmini)) engine.statistics.totalTSpinSingleMini++;
                    if ((li == 1) && (!engine.tspinmini)) engine.statistics.totalTSpinSingle++;
                    if ((li == 2) && (engine.tspinmini)) engine.statistics.totalTSpinDoubleMini++;
                    if ((li == 2) && (!engine.tspinmini)) engine.statistics.totalTSpinDouble++;
                    if (li == 3) engine.statistics.totalTSpinTriple++;
                }
            } else {
                if (engine.clearMode == GameEngine.CLEAR_LINE)
                    engine.playSE("erase" + li);

                if ((engine.ending == 0) || (engine.staffrollEnableStatistics)) {
                    if (li == 1) engine.statistics.totalSingle++;
                    if (li == 2) engine.statistics.totalDouble++;
                    if (li == 3) engine.statistics.totalTriple++;
                    if (li == 4) engine.statistics.totalFour++;
                }
            }

            // B2B bonus
            if (engine.b2bEnable) {
                if ((engine.tspin) || (li >= 4)) {
                    engine.b2bcount++;

                    if (engine.b2bcount == 1) {
                        engine.playSE("b2b_start");
                    } else {
                        engine.b2b = true;
                        engine.playSE("b2b_continue");

                        if ((engine.ending == 0) || (engine.staffrollEnableStatistics)) {
                            if (li == 4) engine.statistics.totalB2BFour++;
                            else engine.statistics.totalB2BTSpin++;
                        }
                    }
                } else if (engine.b2bcount != 0) {
                    engine.b2b = false;
                    engine.b2bcount = 0;
                    engine.playSE("b2b_end");
                }
            }

            // Combo
            if ((engine.comboType != GameEngine.COMBO_TYPE_DISABLE) && (engine.chain == 0)) {
                if ((engine.comboType == GameEngine.COMBO_TYPE_NORMAL) || ((engine.comboType == GameEngine.COMBO_TYPE_DOUBLE) && (li >= 2)))
                    engine.combo++;

                if (engine.combo >= 2) {
                    int cmbse = engine.combo - 1;
                    if (cmbse > 20) cmbse = 20;
                    engine.playSE("combo" + cmbse);
                }

                if ((engine.ending == 0) || (engine.staffrollEnableStatistics)) {
                    if (engine.combo > engine.statistics.maxCombo) engine.statistics.maxCombo = engine.combo;
                }
            }

            engine.lineGravityTotalLines += engine.lineClearing;

            if ((engine.ending == 0) || (engine.staffrollEnableStatistics)) engine.statistics.lines += li;

            if (engine.field.getHowManyGemClears() > 0) engine.playSE("gem");

            // Calculate score
            calcScore(engine, playerID, li);
            owner.receiver.calcScore(engine, playerID, li);

            // Blockを消す演出を出す (まだ実際には消えていない）
            if (engine.clearMode == GameEngine.CLEAR_LINE) {
                int cY = 0;
                for (int i = 0; i < engine.field.getHeight(); i++) {
                    if (engine.field.getLineFlag(i)) {
                        cY++;
                        for (int j = 0; j < engine.field.getWidth(); j++) {
                            Block blk = engine.field.getBlock(j, i);

                            if (blk != null) {
                                if (blockParticles != null) {
                                    switch (lineClearAnimType) {
                                        case 0:
                                            blockParticles.addBlock(engine, receiver, playerID, blk, j, i, 10, 90, (li >= 4), localRandom);
                                            break;
                                        case 1:
                                            blockParticles.addBlock(engine, receiver, playerID, blk, j, i, engine.field.getWidth(), cY, li, 120);
                                        default:
                                            break;
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (engine.clearMode == GameEngine.CLEAR_LINE_COLOR || engine.clearMode == GameEngine.CLEAR_COLOR || engine.clearMode == GameEngine.CLEAR_GEM_COLOR)
                for (int i = 0; i < engine.field.getHeight(); i++) {
                    for (int j = 0; j < engine.field.getWidth(); j++) {
                        Block blk = engine.field.getBlock(j, i);
                        if (blk == null)
                            continue;
                        if (blk.getAttribute(Block.BLOCK_ATTRIBUTE_ERASE)) {
                            //blockParticles.addBlock(engine, receiver, playerID, blk, j, i, 4, 60, localRandom);
                            if (engine.displaysize == 1) {
                                //owner.receiver.blockBreak(engine, playerID, 2*j, 2*i, blk);
                                //owner.receiver.blockBreak(engine, playerID, 2*j+1, 2*i, blk);
                                //owner.receiver.blockBreak(engine, playerID, 2*j, 2*i+1, blk);
                                //owner.receiver.blockBreak(engine, playerID, 2*j+1, 2*i+1, blk);

                                switch (lineClearAnimType) {
                                    case 0:
                                        blockParticles.addBlock(engine, receiver, playerID, blk, 2 * j, 2 * i, 10, 90, false, localRandom);
                                        blockParticles.addBlock(engine, receiver, playerID, blk, 2 * j + 1, 2 * i, 10, 90, false, localRandom);
                                        blockParticles.addBlock(engine, receiver, playerID, blk, 2 * j, 2 * i + 1, 10, 90, false, localRandom);
                                        blockParticles.addBlock(engine, receiver, playerID, blk, 2 * j + 1, 2 * i + 1, 10, 90, false, localRandom);
                                        break;
                                    case 1:
                                        blockParticles.addBlock(engine, receiver, playerID, blk, 2 * j, 2 * i, engine.field.getWidth(), 1, 1, 120);
                                        blockParticles.addBlock(engine, receiver, playerID, blk, 2 * j + 1, 2 * i, engine.field.getWidth(), 1, 1, 120);
                                        blockParticles.addBlock(engine, receiver, playerID, blk, 2 * j, 2 * i + 1, engine.field.getWidth(), 1, 1, 120);
                                        blockParticles.addBlock(engine, receiver, playerID, blk, 2 * j + 1, 2 * i + 1, engine.field.getWidth(), 1, 1, 120);
                                    default:
                                        break;
                                }
                            } else
                                //owner.receiver.blockBreak(engine, playerID, j, i, blk);
                                switch (lineClearAnimType) {
                                    case 0:
                                        blockParticles.addBlock(engine, receiver, playerID, blk, j, i, 10, 90, false, localRandom);
                                        break;
                                    case 1:
                                        blockParticles.addBlock(engine, receiver, playerID, blk, j, i, engine.field.getWidth(), 1, 1, 120);
                                    default:
                                        break;
                                }
                        }
                    }
                }

            // Blockを消す
            if (engine.clearMode == GameEngine.CLEAR_LINE)
                engine.field.clearLine();
            else if (engine.clearMode == GameEngine.CLEAR_COLOR)
                engine.field.clearColor(engine.colorClearSize, engine.garbageColorClear, engine.gemSameColor, engine.ignoreHidden);
            else if (engine.clearMode == GameEngine.CLEAR_LINE_COLOR)
                engine.field.clearLineColor(engine.colorClearSize, engine.lineColorDiagonals, engine.gemSameColor);
            else if (engine.clearMode == GameEngine.CLEAR_GEM_COLOR)
                engine.lineClearing = engine.field.gemClearColor(engine.colorClearSize, engine.garbageColorClear, engine.ignoreHidden);
        }

        // Linesを1段落とす
        if ((engine.lineGravityType == GameEngine.LINE_GRAVITY_NATIVE) &&
            (engine.getLineDelay() >= (engine.lineClearing - 1)) && (engine.statc[0] >= engine.getLineDelay() - (engine.lineClearing - 1)) && (engine.ruleopt.lineFallAnim)) {
            engine.field.downFloatingBlocksSingleLine();
        }

        // Line delay cancel check
        engine.delayCancelMoveLeft = engine.ctrl.isPush(Controller.BUTTON_LEFT);
        engine.delayCancelMoveRight = engine.ctrl.isPush(Controller.BUTTON_RIGHT);

        boolean moveCancel = engine.ruleopt.lineCancelMove && (engine.ctrl.isPush(engine.getUp()) ||
            engine.ctrl.isPush(engine.getDown()) || engine.delayCancelMoveLeft || engine.delayCancelMoveRight);
        boolean rotateCancel = engine.ruleopt.lineCancelRotate && (engine.ctrl.isPush(Controller.BUTTON_A) ||
            engine.ctrl.isPush(Controller.BUTTON_B) || engine.ctrl.isPush(Controller.BUTTON_C) ||
            engine.ctrl.isPush(Controller.BUTTON_E));
        boolean holdCancel = engine.ruleopt.lineCancelHold && engine.ctrl.isPush(Controller.BUTTON_D);

        engine.delayCancel = moveCancel || rotateCancel || holdCancel;

        if ((engine.statc[0] < engine.getLineDelay()) && engine.delayCancel) {
            engine.statc[0] = engine.getLineDelay();
        }

        // Next ステータス
        if (engine.statc[0] >= engine.getLineDelay()) {
            // Cascade
            if ((engine.lineGravityType == GameEngine.LINE_GRAVITY_CASCADE || engine.lineGravityType == GameEngine.LINE_GRAVITY_CASCADE_SLOW)) {
                if (engine.statc[6] < engine.getCascadeDelay()) {
                    engine.statc[6]++;
                    return true;
                } else if (engine.field.doCascadeGravity(engine.lineGravityType)) {
                    engine.statc[6] = 0;
                    return true;
                } else if (engine.statc[6] < engine.getCascadeClearDelay()) {
                    engine.statc[6]++;
                    return true;
                } else if (((engine.clearMode == GameEngine.CLEAR_LINE) && engine.field.checkLineNoFlag() > 0) ||
                    ((engine.clearMode == GameEngine.CLEAR_COLOR) && engine.field.checkColor(engine.colorClearSize, false, engine.garbageColorClear, engine.gemSameColor, engine.ignoreHidden) > 0) ||
                    ((engine.clearMode == GameEngine.CLEAR_LINE_COLOR) && engine.field.checkLineColor(engine.colorClearSize, false, engine.lineColorDiagonals, engine.gemSameColor) > 0) ||
                    ((engine.clearMode == GameEngine.CLEAR_GEM_COLOR) && engine.field.gemColorCheck(engine.colorClearSize, false, engine.garbageColorClear, engine.ignoreHidden) > 0)) {
                    engine.tspin = false;
                    engine.tspinmini = false;
                    engine.chain++;
                    if (engine.chain > engine.statistics.maxChain) engine.statistics.maxChain = engine.chain;
                    engine.statc[0] = 0;
                    engine.statc[6] = 0;
                    return true;
                }
            }

            boolean skip;
            skip = lineClearEnd(engine, playerID);
            owner.receiver.lineClearEnd(engine, playerID);

            if (!skip) {
                if (engine.lineGravityType == GameEngine.LINE_GRAVITY_NATIVE) engine.field.downFloatingBlocks();
                engine.playSE("linefall");

                engine.field.lineColorsCleared = null;

                if ((engine.stat == GameEngine.STAT_LINECLEAR) || (engine.versionMajor <= 6.3f)) {
                    engine.resetStatc();
                    if (engine.ending == 1) {
                        // Ending
                        engine.stat = GameEngine.STAT_ENDINGSTART;
                    } else if ((engine.getARELine() > 0) || (engine.lagARE)) {
                        // AREあり
                        engine.statc[0] = 0;
                        engine.statc[1] = engine.getARELine();
                        engine.statc[2] = 1;
                        engine.stat = GameEngine.STAT_ARE;
                    } else if (engine.interruptItemNumber != GameEngine.INTERRUPTITEM_NONE) {
                        // 中断効果のあるアイテム処理
                        engine.nowPieceObject = null;
                        engine.interruptItemPreviousStat = GameEngine.STAT_MOVE;
                        engine.stat = GameEngine.STAT_INTERRUPTITEM;
                    } else {
                        // AREなし
                        engine.nowPieceObject = null;
                        if (engine.versionMajor < 7.5f)
                            engine.initialRotate(); //XXX: Weird IRS thing on lines cleared but no ARE
                        engine.stat = GameEngine.STAT_MOVE;
                    }
                }
            }

            return true;
        }

        engine.statc[0]++;
        return true;
    }

    /*
     * Calculate score - PAIN
     */
    @Override
    public void calcScore(GameEngine engine, int playerID, int lines) {
        // Line clear bonus
        // if (linesUsed > 4) linesUsed = 4;

        if (lines > 0) {
            int pts = engine.statistics.time - timeScore;

            // Add to score
            if (pts > 0) {
                // scoreBeforeIncrease = engine.statistics.score;
                lastscore = pts;
                lastpiece = engine.nowPieceObject.id;
                scgettime = 0;
                timeScore += pts;
                lastLine = lines;
            }
        }

        if (lines > 0) {
            // Level up, unless at level 200+, where only Fours+ increment level.
            if (version < CURRENT_VERSION + 1 || engine.statistics.level < 200 || lines >= 4) {
                engine.statistics.level++;
                engine.playSE("levelup");
            }

            // owner.backgroundStatus.fadesw = true;
            // owner.backgroundStatus.fadecount = 0;
            // owner.backgroundStatus.fadebg = engine.statistics.level / 5;

            if (engine.statistics.level == 200) {
                shouldUseTimer = false;
                engine.playSE("medal");
                ++engine.owner.backgroundStatus.bg;

                int destinationX = receiver.getScoreDisplayPositionX(engine, playerID);
                int destinationY = receiver.getScoreDisplayPositionY(engine, playerID) + (18 * (engine.displaysize == 0 ? 16 : 32));

                int[] colors = new int[] { EventReceiver.COLOR_PINK, EventReceiver.COLOR_RED, EventReceiver.COLOR_PURPLE };

                warningText = new FlyInOutText("WARNING: NON-TETRISES", destinationX, destinationY, 9, 162, 9, colors, 1.0f, engine.randSeed + engine.statistics.time, true);
                warningTextSecondLine = new FlyInOutText("REDUCE STOCK!", destinationX + (4 * (engine.displaysize == 0 ? 16 : 32)), destinationY + (engine.displaysize == 0 ? 16 : 32), 9, 162, 9, colors, 1.0f, engine.randSeed + engine.statistics.time, true);
            }

            if (engine.statistics.level < 200) {
                mainTimer += LEVEL_TIMEBONUS;
                if (mainTimer > TIMER_MAX) mainTimer = TIMER_MAX;
            }

            if (engine.statistics.level == 300) {
                engine.playSE("endingstart");
                int destinationX = receiver.getScoreDisplayPositionX(engine, playerID);
                int destinationY = receiver.getScoreDisplayPositionY(engine, playerID) + (18 * (engine.displaysize == 0 ? 16 : 32));

                int[] colors = new int[] { EventReceiver.COLOR_YELLOW, EventReceiver.COLOR_ORANGE, EventReceiver.COLOR_RED };

                warningText = new FlyInOutText("CONGRATULATIONS!", destinationX, destinationY, 15, 120, 15, colors, 1.0f, engine.randSeed + engine.statistics.time, true);
            }

            if (lines < 4) {
                if (engine.statistics.level > 200) {
                    stock--;

                    if (stock <= 2) {
                        if (stock >= 0) engine.playSE("danger");

                        int destinationX = receiver.getScoreDisplayPositionX(engine, playerID);
                        int destinationY = receiver.getScoreDisplayPositionY(engine, playerID) + (18 * (engine.displaysize == 0 ? 16 : 32));

                        int[] colors = new int[] { EventReceiver.COLOR_RED, EventReceiver.COLOR_PURPLE };

                        warningText = new FlyInOutText(stock >= 0 ? "WARNING: STOCK LOW!" : "STOCK DEPLETED!", destinationX, destinationY, 15, 60, 15, colors, 1.0f, engine.randSeed + engine.statistics.time, stock >= 0);
                    }
                }
            } else {
                if (engine.statistics.level <= 200) stock++;
            }

            if (engine.statistics.level <= 300) {
                calculateEfficiencyGrade(engine);
            }

            setSpeed(engine);
        }
    }

    /*
     * Render results screen
     */
    @Override
    public void renderResult(GameEngine engine, int playerID) {
        drawResultStats(engine, playerID, receiver, 0, EventReceiver.COLOR_BLUE,
            STAT_LINES, STAT_LEVEL, STAT_LPM);
        drawResultRank(engine, playerID, receiver, 15, EventReceiver.COLOR_BLUE, rankingRank);

        receiver.drawMenuFont(engine, playerID, 0, 6, "TIME SCORE", EventReceiver.COLOR_BLUE);
        receiver.drawMenuFont(engine, playerID, 0, 7, String.format("%10s", GeneralUtil.getTime(timeScore)));

        receiver.drawMenuFont(engine, playerID, 0, 8, "EFFICIENCY", EventReceiver.COLOR_BLUE);
        receiver.drawMenuFont(engine, playerID, 0, 9, String.format("%10s", (String.format("%.2f", efficiency * 100) + "%")));

        if (engine.statistics.level >= 300) {
            receiver.drawMenuFont(engine, playerID, 0, 10, "GRADE", EventReceiver.COLOR_BLUE);

            int dX = 4 + receiver.getFieldDisplayPositionX(engine, playerID) + (3 * 16);
            int dY = 52 + receiver.getFieldDisplayPositionY(engine, playerID) + (int) (11.5 * 16);
            customHolder.drawImage(engine, "grades", dX, dY, (64 * efficiencyGrade), 0, 64, 48, 255, 255, 255, 255, 1.0f);
        }

        drawResultNetRank(engine, playerID, receiver, 10, EventReceiver.COLOR_BLUE, netRankingRank[0]);
        drawResultNetRankDaily(engine, playerID, receiver, 12, EventReceiver.COLOR_BLUE, netRankingRank[1]);

        if (netIsPB) {
            receiver.drawMenuFont(engine, playerID, 2, 21, "NEW PB", EventReceiver.COLOR_ORANGE);
        }

        if (netIsNetPlay && (netReplaySendStatus == 1)) {
            receiver.drawMenuFont(engine, playerID, 0, 22, "SENDING...", EventReceiver.COLOR_PINK);
        } else if (netIsNetPlay && !netIsWatch && (netReplaySendStatus == 2)) {
            receiver.drawMenuFont(engine, playerID, 1, 22, "A: RETRY", EventReceiver.COLOR_RED);
        }
    }

    /*
     * Called when saving replay
     */
    @Override
    public void saveReplay(GameEngine engine, int playerID, CustomProperties prop) {
        saveSetting(prop);

        // NET: Save name
        if ((netPlayerName != null) && (netPlayerName.length() > 0)) {
            prop.setProperty(playerID + ".net.netPlayerName", netPlayerName);
        }

        // Update rankings
        if ((!owner.replayMode) && (!big) && (engine.ai == null) && (startingStock == 0)) {
            updateRanking(engine.statistics.level, engine.statistics.lines, timeScore);

            if (playerProperties.isLoggedIn()) {
                prop.setProperty("joker.playerName", playerProperties.getNameDisplay());
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
        startlevel = prop.getProperty("joker.startlevel", 0);
        big = prop.getProperty("joker.big", false);
        startingStock = prop.getProperty("joker.extrastock", 0);
        version = prop.getProperty("joker.version", CURRENT_VERSION + 1);
        lineClearAnimType = prop.getProperty("joker.lcat", 0);
        drawJevil = prop.getProperty("joker.drawjevil", false);
        useAnimBG = prop.getProperty("joker.animatedBG", false);
    }

    /**
     * Save settings to property file
     *
     * @param prop Property file
     */
    private void saveSetting(CustomProperties prop) {
        prop.setProperty("joker.startlevel", startlevel);
        prop.setProperty("joker.big", big);
        prop.setProperty("joker.extrastock", startingStock);
        prop.setProperty("joker.version", version);
        prop.setProperty("joker.lcat", lineClearAnimType);
        prop.setProperty("joker.drawjevil", drawJevil);
        prop.setProperty("joker.animatedBG", useAnimBG);
    }

    /**
     * Load settings from property file
     *
     * @param prop Property file
     */
    private void loadSettingPlayer(ProfileProperties prop) {
        if (!prop.isLoggedIn()) return;
        startlevel = prop.getProperty("joker.startlevel", 0);
        big = prop.getProperty("joker.big", false);
        startingStock = prop.getProperty("joker.extrastock", 0);
        lineClearAnimType = prop.getProperty("joker.lcat", 0);
        drawJevil = prop.getProperty("joker.drawjevil", false);
        useAnimBG = prop.getProperty("joker.animatedBG", false);
    }

    /**
     * Save settings to property file
     *
     * @param prop Property file
     */
    private void saveSettingPlayer(ProfileProperties prop) {
        if (!prop.isLoggedIn()) return;
        prop.setProperty("joker.startlevel", startlevel);
        prop.setProperty("joker.big", big);
        prop.setProperty("joker.extrastock", startingStock);
        prop.setProperty("joker.lcat", lineClearAnimType);
        prop.setProperty("joker.drawjevil", drawJevil);
        prop.setProperty("joker.animatedBG", useAnimBG);
    }

    /**
     * Read rankings from property file
     *
     * @param prop     Property file
     * @param ruleName Rule name
     */
    @Override
    protected void loadRanking(CustomProperties prop, String ruleName) {
        for (int i = 0; i < RANKING_MAX; i++) {
            rankingLevel[i] = prop.getProperty("joker.ranking." + ruleName + ".level." + version + "." + i, 0);
            rankingLines[i] = prop.getProperty("joker.ranking." + ruleName + ".lines." + version + "." + i, 0);
            rankingTime[i] = prop.getProperty("joker.ranking." + ruleName + ".time." + version + "." + i, 0);
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
            prop.setProperty("joker.ranking." + ruleName + ".level." + version + "." + i, rankingLevel[i]);
            prop.setProperty("joker.ranking." + ruleName + ".lines." + version + "." + i, rankingLines[i]);
            prop.setProperty("joker.ranking." + ruleName + ".time." + version + "." + i, rankingTime[i]);
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
            rankingLevelPlayer[i] = prop.getProperty("joker.ranking." + ruleName + ".level." + version + "." + i, 0);
            rankingLinesPlayer[i] = prop.getProperty("joker.ranking." + ruleName + ".lines." + version + "." + i, 0);
            rankingTimePlayer[i] = prop.getProperty("joker.ranking." + ruleName + ".time." + version + "." + i, 0);
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
            prop.setProperty("joker.ranking." + ruleName + ".level." + version + "." + i, rankingLevelPlayer[i]);
            prop.setProperty("joker.ranking." + ruleName + ".lines." + version + "." + i, rankingLinesPlayer[i]);
            prop.setProperty("joker.ranking." + ruleName + ".time." + version + "." + i, rankingTimePlayer[i]);
        }
    }

    /**
     * Update rankings
     *
     * @param li   Lines
     * @param time Time
     */
    private void updateRanking(int lv, int li, int time) {
        rankingRank = checkRanking(lv, li, time);

        if (rankingRank != -1) {
            // Shift down ranking entries
            for (int i = RANKING_MAX - 1; i > rankingRank; i--) {
                rankingLevel[i] = rankingLevel[i - 1];
                rankingLines[i] = rankingLines[i - 1];
                rankingTime[i] = rankingTime[i - 1];
            }

            // Add new data
            rankingLevel[rankingRank] = lv;
            rankingLines[rankingRank] = li;
            rankingTime[rankingRank] = time;
        }

        if (playerProperties.isLoggedIn()) {
            rankingRankPlayer = checkRankingPlayer(lv, li, time);

            if (rankingRankPlayer != -1) {
                // Shift down ranking entries
                for (int i = RANKING_MAX - 1; i > rankingRankPlayer; i--) {
                    rankingLevelPlayer[i] = rankingLevelPlayer[i - 1];
                    rankingLinesPlayer[i] = rankingLinesPlayer[i - 1];
                    rankingTimePlayer[i] = rankingTimePlayer[i - 1];
                }

                // Add new data
                rankingLevelPlayer[rankingRankPlayer] = lv;
                rankingLinesPlayer[rankingRankPlayer] = li;
                rankingTimePlayer[rankingRankPlayer] = time;
            }
        }
    }

    /**
     * Calculate ranking position
     *
     * @param li   Lines
     * @param time Time
     * @return Position (-1 if unranked)
     */
    private int checkRanking(int lv, int li, int time) {
        for (int i = 0; i < RANKING_MAX; i++) {
            if (lv > rankingLevel[i]) {
                return i;
            } else if ((lv == rankingLevel[i]) && (li > rankingLines[i])) {
                return i;
            } else if ((lv == rankingLevel[i]) && (li == rankingLines[i]) && (time < rankingTime[i])) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Calculate ranking position
     *
     * @param li   Lines
     * @param time Time
     * @return Position (-1 if unranked)
     */
    private int checkRankingPlayer(int lv, int li, int time) {
        for (int i = 0; i < RANKING_MAX; i++) {
            if (lv > rankingLevelPlayer[i]) {
                return i;
            } else if ((lv == rankingLevelPlayer[i]) && (li > rankingLinesPlayer[i])) {
                return i;
            } else if ((lv == rankingLevelPlayer[i]) && (li == rankingLinesPlayer[i]) && (time < rankingTimePlayer[i])) {
                return i;
            }
        }

        return -1;
    }
}
