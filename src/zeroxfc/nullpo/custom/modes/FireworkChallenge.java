/*
 * TGM 3 EASY mode recreation. It hopefully it is as close as possible to its source material.
 * - 0xFC963F18DC21
 */
package zeroxfc.nullpo.custom.modes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import mu.nu.nullpo.game.component.BGMStatus;
import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.game.subsystem.mode.DummyMode;
import mu.nu.nullpo.util.CustomProperties;
import mu.nu.nullpo.util.GeneralUtil;
import org.apache.log4j.Logger;
import zeroxfc.nullpo.custom.libs.ArrayRandomiser;
import zeroxfc.nullpo.custom.libs.CustomResourceHolder;
import zeroxfc.nullpo.custom.libs.FieldManipulation;
import zeroxfc.nullpo.custom.libs.ProfileProperties;
import zeroxfc.nullpo.custom.libs.RendererExtension;
import zeroxfc.nullpo.custom.libs.ScrollingMarqueeText;
import zeroxfc.nullpo.custom.libs.SoundLoader;
import zeroxfc.nullpo.custom.libs.particles.BlockParticleCollection;
import zeroxfc.nullpo.custom.libs.particles.Fireworks;

public class FireworkChallenge extends DummyMode {
    private static final Logger log = Logger.getLogger(FireworkChallenge.class);

    // Base Line clear values
    private static final double[] BASE_LINE_VALUES = { 1.0, 2.9, 3.8, 4.7 };

    // Combo multiplier table, non-credits
    private static final double[] ONMOVE_COMBO_MULTIPLIERS = { 1.0, 1.0, 1.5, 1.9, 2.2, 2.9, 3.5, 3.9, 4.2, 4.5 };

    // Combo multiplier table, credits
    private static final double[] CREDIT_COMBO_MULTIPLIERS = { 1.0, 1.5, 1.9, 2.2, 2.9 };

    // Split bonus
    private static final double SPLIT_BONUS = 1.4;

    // Lucky level bonus
    // given at 25, 50, 75, 125, 150, 175, or 199
    private static final double LUCKY_LEVEL_BONUS = 1.3;

    // Fixed speed combo bonus
    private static final double FIXED_SPEED_COMBO_BONUS = 1.3;

    // Lucky levels
    private static final int[] LUCKY_LEVELS = { 25, 50, 75, 125, 150, 175, 199 };

    // Combo credit conversion
    private static final int[] CREDIT_COMBO_CONVERSION = { 1, 1, 2, 3, 4, 4, 4, 4, 4, 0 };

    // 30s bonus
    private static final double THIRTY_BONUS = 1.4;

    // Max roll time - also used in max.
    private static final int MAX_ROLL_TIME = 3265;

    // BGM Fadeout level
    private static final int BGM_FADEOUT_LV = 190;

    /*
     * Spin Bonuses:
     *
     * 0 - no spin
     * 1 - Spin clear (not T, not O)
     * 2 - Spin clear (T)
     * 3 - TST (SRS only)
     *
     * [check engine.lastmove == 5 or 6 and the lines cleared value]
     */
    // private static final int[] SPIN_BONUSES = { 1, 2, 3, 4 };

    // Completion firework reward
    private static final int COMPLETION_FIREWORK_REWARD = 24;

    // Firework frame delays - note: bravos are not recognised during credits
    private static final int COMBO_BRAVO_DELAY = 10;
    private static final int COMPLETION_DELAY = 8;

    // Gravity table (Gravity speed value)
    private static final int[] GRAVITY_VALUES =
        {
            4, 5, 6, 8, 10, 12, 16, 32, 48, 64, 16, 48, 80, 112, 144, 176, 192, 208, 224, 240, -1, -1
        };

    // Gravity table (Gravity change level)
    private static final int[] GRAVITY_CHANGE_LEVELS =
        {
            8, 19, 35, 40, 50, 60, 70, 80, 90, 100, 112, 121, 132, 144, 156, 167, 177, 187, 195, 200, 10000
        };

    // Max. rankings
    private static final int RANKING_MAX = 10;

    // Max. sections
    private static final int SECTION_MAX = 2;

    // Default section times
    private static final int DEFAULT_SECTION_TIME = 6300;

    // Credit headings
    private static final String[] CREDIT_HEADINGS = {
        "MODE CREATOR:",
        "BASIS:",
        "SPECIAL THANKS GOES TO",
        "CONGRATULATIONS!"
    };

    // Credit texts
    private static final String[] CREDIT_TEXTS = {
        "0XFC963F18DC21.",
        "TI-EASY.",
        "GLITCHYPSI, OSHISAURE, RY00001, AKARI, THE DRAGON GOD NERROTH.",
        "YOU HAVE COMPLETED THE FIREWORK CHALLENGE!"
    };

    private static final int[] INPUT_SEQUENCE = {
        Controller.BUTTON_DOWN, Controller.BUTTON_DOWN,
        Controller.BUTTON_UP, Controller.BUTTON_UP,
        Controller.BUTTON_RIGHT, Controller.BUTTON_LEFT,
        Controller.BUTTON_RIGHT, Controller.BUTTON_LEFT,
    };
    private static final int headerColour = EventReceiver.COLOR_GREEN;
    private boolean[] presses;
    // Staff roll object
    private ScrollingMarqueeText creditObject;
    // ID of last piece
    private int lastPiece;
    // combo during credits
    private int comboDuringCredits;
    // Non-static firework timer
    private int creditsFreeFireworkDelay;
    // Firework timers
    private int comboFireworkTimer;
    private int bravoFireworkTimer;
    private int completionFireworkTimer;
    private int creditsFreeFireworkTimer;
    // Time since last firework
    private int lastFireworkTimer;
    // Time since last line clear
    private int lastLineClearTime;
    // firework queues
    private int comboFireworkQueue;
    private int bravoFireworkQueue;
    private int genericFireworkQueue;
    // is the line clear split
    private boolean isSplit;
    // total amount
    private int totalFireworkQueue;
    // Give the 30s 30Lv bonus?
    private boolean give30sBonus;
    // Varispeed Combo Bonus
    private double varispeedComboBonus;
    // Varispeed Finesse Bonus;
    private double varispeedFinesseBonus;
    // current firework score
    private int fireworksFired;
    // index of current gravity level
    private int gravityIndex;
    // level of next section
    private int nextSectionLevel;
    // color timer for score text
    private int scoreColorTimer;
    // has credit roll started?
    private boolean rollStarted;
    // current credit time
    private int rollTime;
    // section times
    private int[] sectionTime;
    // is section time new record?
    private boolean[] sectionIsPB;
    // is there a new section PB?
    private boolean sectionPBGet;
    // sections completed
    private int sectionsComplete;
    // average ST
    private int averageSectionTime;
    // show best STs?
    private boolean showBests;
    // 20g mode?
    private boolean maxGravMode;
    // big mode?
    private boolean big;
    // show section times?
    private boolean showST;
    // has added completionfireworks
    private boolean addedCompletionFireworks;
    // Current round's ranking rank
    private int rankingRank;
    // Rankings' 段位
    private int[] rankingFireworks;
    // Rankings'  level
    private int[] rankingLevel;
    // Rankings' times
    private int[] rankingTime;
    // Section Time記録
    private int[] bestSectionTime;
    // levels since last 30s split
    private int lvAtLastSplit;
    // level up flag
    private boolean levelUpFlag;
    // current GameManager
    private GameManager owner;
    // current EventReceiver;
    private EventReceiver receiver;
    // Firework system
    private Fireworks fireworkEmitter;
    // private randomiser for firework function - clone the game's randomiser to this.
    // engine.randSeed ???
    private Random fireworkRandomiser;
    // Particle stuff
    private BlockParticleCollection blockParticles;
    /**
     * The good hard drop effect
     */
    private ArrayList<int[]> pCoordList;
    private Piece cPiece;
    private ProfileProperties playerProperties;
    private boolean showPlayerStats;
    private String PLAYER_NAME;
    private int rankingRankPlayer;
    private int[] rankingFireworksPlayer, rankingLevelPlayer, rankingTimePlayer;

    private RendererExtension rendererExtension;

    private boolean killed;
    // TODO: debug mode meme

    // Get mode name
    @Override
    public String getName() {
        return "FIREWORK CHALLENGE";
    }

    // Initialise everything.
    @Override
    public void playerInit(GameEngine engine, int playerID) {
        presses = new boolean[] { false, false, false, false, false, false, false, false, false, false };

        SoundLoader.loadSoundset(SoundLoader.LOADTYPE_FIREWORKS);

        creditObject = new ScrollingMarqueeText(CREDIT_HEADINGS, CREDIT_TEXTS, EventReceiver.COLOR_ORANGE, EventReceiver.COLOR_WHITE);

        owner = engine.owner;
        receiver = engine.owner.receiver;

        lastPiece = -1;
        creditsFreeFireworkDelay = 60;
        comboDuringCredits = 0;
        isSplit = false;

        comboFireworkTimer = 0;
        bravoFireworkTimer = 0;
        completionFireworkTimer = 0;
        creditsFreeFireworkTimer = 0;

        lastFireworkTimer = 0;

        lastLineClearTime = 0;

        comboFireworkQueue = 0;
        bravoFireworkQueue = 0;
        genericFireworkQueue = 0;

        totalFireworkQueue = 0;

        give30sBonus = false;

        varispeedComboBonus = 1.0;
        varispeedFinesseBonus = 1.0;

        pCoordList = new ArrayList<>();
        cPiece = null;

        fireworksFired = 0;

        gravityIndex = 0;
        nextSectionLevel = 0;

        scoreColorTimer = 0;

        rollStarted = false;
        rollTime = 0;

        sectionTime = new int[SECTION_MAX];
        sectionIsPB = new boolean[SECTION_MAX];
        bestSectionTime = new int[SECTION_MAX];
        sectionPBGet = false;

        sectionsComplete = 0;
        averageSectionTime = 0;
        lvAtLastSplit = 0;

        showBests = false;
        maxGravMode = false;
        big = false;
        showST = false;

        addedCompletionFireworks = false;

        rankingRank = -1;
        rankingFireworks = new int[RANKING_MAX];
        rankingLevel = new int[RANKING_MAX];
        rankingTime = new int[RANKING_MAX];

        if (playerProperties == null) {
            playerProperties = new ProfileProperties(headerColour);

            showPlayerStats = false;
        }

        killed = false;

        rankingRankPlayer = -1;
        rankingFireworksPlayer = new int[RANKING_MAX];
        rankingLevelPlayer = new int[RANKING_MAX];
        rankingTimePlayer = new int[RANKING_MAX];

        levelUpFlag = false;

        // DOUBLES+ ONLY!!!!
        engine.comboType = GameEngine.COMBO_TYPE_DOUBLE;
        engine.framecolor = GameEngine.FRAME_COLOR_GREEN;
        owner.backgroundStatus.bg = 0;

        // Constant non-gravity speeds.
        engine.speed.are = 25;
        engine.speed.areLine = 25;
        engine.speed.das = 14;
        engine.speed.lineDelay = 40;
        engine.speed.lockDelay = 30;

        engine.ghost = true;

        engine.tspinEnable = false;
        engine.b2bEnable = false;
        engine.bighalf = true;
        engine.bigmove = true;
        engine.staffrollEnable = true;
        engine.staffrollNoDeath = false;

        if (!owner.replayMode) {
            loadSetting(owner.modeConfig);
            loadRanking(owner.modeConfig, engine.ruleopt.strRuleName);

            if (playerProperties.isLoggedIn()) {
                loadSettingPlayer(playerProperties);
                loadRankingPlayer(playerProperties, engine.ruleopt.strRuleName);
            }

            PLAYER_NAME = "";
        } else {
            loadSetting(owner.replayProp);

            PLAYER_NAME = owner.replayProp.getProperty("fireworkchallenge.playerName", "");
        }
    }

    /**
     * Update falling speed
     *
     * @param engine GameEngine
     */
    private void setSpeed(GameEngine engine) {
        if (maxGravMode) {
            engine.speed.gravity = -1;
        } else {
            while (engine.statistics.level >= GRAVITY_CHANGE_LEVELS[gravityIndex]) gravityIndex++;
            engine.speed.gravity = GRAVITY_VALUES[gravityIndex];
        }
    }

    /**
     * Update average section time
     */
    private void setAverageSectionTime() {
        if (sectionsComplete > 0) {
            int temp = 0;
            for (int i = 0; i < sectionsComplete; i++) {
                if (i < sectionTime.length) temp += sectionTime[i];
            }
            averageSectionTime = temp / sectionsComplete;
        } else {
            averageSectionTime = 0;
        }
    }

    /**
     * Section Time更新処理
     *
     * @param sectionNumber Section number
     */
    private void stNewRecordCheck(int sectionNumber) {
        if ((sectionTime[sectionNumber] < bestSectionTime[sectionNumber]) && (!owner.replayMode)) {
            sectionIsPB[sectionNumber] = true;
            sectionPBGet = true;
        }
    }

    /*
     * Called at settings screen
     */
    @Override
    public boolean onSetting(GameEngine engine, int playerID) {
        // Menu
        if (engine.owner.replayMode == false) {
            // Configuration changes
            int change = updateCursor(engine, 2);

            if (change != 0) {
                engine.playSE("change");

                switch (engine.statc[2]) {
                    case 0:
                        maxGravMode = !maxGravMode;
                        break;
                    case 1:
                        showST = !showST;
                        break;
                    case 2:
                        big = !big;
                        break;
                }
            }

            //  section time display切替
            if (engine.ctrl.isPush(Controller.BUTTON_F) && (engine.statc[3] >= 5)) {
                engine.playSE("change");
                showBests = !showBests;
            }

            // 決定
            if (engine.ctrl.isPush(Controller.BUTTON_A) && (engine.statc[3] >= 5)) {
                engine.playSE("decide");
                if (playerProperties.isLoggedIn()) {
                    saveSettingPlayer(playerProperties);
                    playerProperties.saveProfileConfig();
                } else {
                    saveSetting(owner.modeConfig);
                    receiver.saveModeConfig(owner.modeConfig);
                }
                showBests = false;
                sectionsComplete = 0;
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
        } else {
            engine.statc[3]++;
            engine.statc[2] = -1;

            return engine.statc[3] < 60;
        }

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

    /*
     * Render the settings screen
     */
    @Override
    public void renderSetting(GameEngine engine, int playerID) {
        drawMenu(engine, playerID, receiver, 0, EventReceiver.COLOR_BLUE, 0,
            "20G MODE", GeneralUtil.getONorOFF(maxGravMode),
            "SHOW STIME", GeneralUtil.getONorOFF(showST),
            "BIG", GeneralUtil.getONorOFF(big));
    }

    @Override
    public boolean onReady(GameEngine engine, int playerID) {
        if (blockParticles != null) {
            blockParticles = null;
        }

        return false;
    }

    // called onStart
    @Override
    public void startGame(GameEngine engine, int playerID) {
        final CustomResourceHolder usedCRH = new CustomResourceHolder(1);

        killed = false;
        addedCompletionFireworks = false;
        fireworkEmitter = new Fireworks(usedCRH, engine.randSeed + 31415L);
        fireworkRandomiser = new Random(engine.randSeed);

        ArrayRandomiser creditScrambler = new ArrayRandomiser(engine.randSeed);
        int[] arr = new int[CREDIT_HEADINGS.length - 1];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = i;
        }
        int[] t = creditScrambler.permute(arr);
        int[] newArr = new int[CREDIT_HEADINGS.length];
        for (int i = 0; i < t.length; i++) {
            newArr[i] = t[i];
        }
        newArr[CREDIT_HEADINGS.length - 1] = CREDIT_HEADINGS.length - 1;

        String[] ch = new String[CREDIT_HEADINGS.length];
        String[] ct = new String[CREDIT_HEADINGS.length];

        for (int i = 0; i < newArr.length; i++) {
            ch[i] = CREDIT_HEADINGS[newArr[i]];
            ct[i] = CREDIT_TEXTS[newArr[i]];
        }

        creditObject = new ScrollingMarqueeText(ch, ct, EventReceiver.COLOR_ORANGE, EventReceiver.COLOR_WHITE);

        sectionTime = new int[SECTION_MAX];
        sectionIsPB = new boolean[SECTION_MAX];
        sectionsComplete = 0;
        isSplit = false;

        rollTime = 0;
        rollStarted = false;

        lvAtLastSplit = 0;
        fireworksFired = 0;
        comboDuringCredits = 0;
        creditsFreeFireworkDelay = 60;
        addedCompletionFireworks = false;
        scoreColorTimer = 0;

        comboFireworkTimer = 0;
        bravoFireworkTimer = 0;
        completionFireworkTimer = 0;
        creditsFreeFireworkTimer = 0;

        lastFireworkTimer = 0;

        lastLineClearTime = 0;

        comboFireworkQueue = 0;
        bravoFireworkQueue = 0;
        genericFireworkQueue = 0;

        totalFireworkQueue = 0;

        nextSectionLevel = engine.statistics.level + 100;
        if (engine.statistics.level < 0) nextSectionLevel = 100;
        if (engine.statistics.level > 200) nextSectionLevel = 200;

        owner.backgroundStatus.bg = engine.statistics.level / 100;
        lastPiece = -1;
        engine.big = big;

        rendererExtension = new RendererExtension(usedCRH);

        int total = (engine.field.getHeight() + engine.field.getHiddenHeight()) * engine.field.getWidth() * 2;
        blockParticles = new BlockParticleCollection(rendererExtension, total, 1);

        setSpeed(engine);
        owner.bgmStatus.bgm = 0;
    }

    @Override
    public void onFirst(GameEngine engine, int playerID) {
        pCoordList.clear();
        cPiece = null;
    }

    /*
     * Hard drop
     */
    @Override
    public void afterHardDropFall(GameEngine engine, int playerID, int fall) {
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

                rendererExtension.addBlockBreakEffect(receiver, x2, y2, cPiece.block[i]);
            } else {
                int x2 = baseX + (cPiece.dataX[cPiece.direction][i] * 32);
                int y2 = baseY + (cPiece.dataY[cPiece.direction][i] * 32);

                rendererExtension.addBlockBreakEffect(receiver, x2, y2, cPiece.block[i]);
                rendererExtension.addBlockBreakEffect(receiver, x2 + 16, y2, cPiece.block[i]);
                rendererExtension.addBlockBreakEffect(receiver, x2, y2 + 16, cPiece.block[i]);
                rendererExtension.addBlockBreakEffect(receiver, x2 + 16, y2 + 16, cPiece.block[i]);
            }
        }
    }

    /*
     * Render score
     */
    @Override
    public void renderLast(GameEngine engine, int playerID) {
        receiver.drawScoreFont(engine, playerID, 0, 0, getName(), EventReceiver.COLOR_GREEN);

        if ((engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (owner.replayMode == false))) {
            if ((owner.replayMode == false) && (big == false) && (maxGravMode == false) && (engine.ai == null)) {
                if (!showBests) {
                    // Rankings
                    float scale = (receiver.getNextDisplayType() == 2) ? 0.5f : 1.0f;
                    int topY = (receiver.getNextDisplayType() == 2) ? 5 : 3;
                    receiver.drawScoreFont(engine, playerID, 3, topY - 1, "F.WORK LEVEL TIME", EventReceiver.COLOR_BLUE, scale);

                    if (showPlayerStats) {
                        for (int i = 0; i < RANKING_MAX; i++) {
                            receiver.drawScoreFont(engine, playerID, 0, topY + i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW, scale);
                            receiver.drawScoreFont(engine, playerID, 3, topY + i, String.valueOf(rankingFireworksPlayer[i]), (i == rankingRankPlayer), scale);
                            receiver.drawScoreFont(engine, playerID, 10, topY + i, String.valueOf(rankingLevelPlayer[i]), (i == rankingRankPlayer), scale);
                            receiver.drawScoreFont(engine, playerID, 16, topY + i, GeneralUtil.getTime(rankingTimePlayer[i]), (i == rankingRankPlayer), scale);
                        }

                        receiver.drawScoreFont(engine, playerID, 0, 18, "PLAYER SCORES", EventReceiver.COLOR_BLUE);
                        receiver.drawScoreFont(engine, playerID, 0, 19, playerProperties.getNameDisplay(), EventReceiver.COLOR_WHITE, 2f);

                        receiver.drawScoreFont(engine, playerID, 0, 22, "D:SWITCH RANK SCREEN", EventReceiver.COLOR_GREEN);

                    } else {
                        for (int i = 0; i < RANKING_MAX; i++) {
                            receiver.drawScoreFont(engine, playerID, 0, topY + i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW, scale);
                            receiver.drawScoreFont(engine, playerID, 3, topY + i, String.valueOf(rankingFireworks[i]), (i == rankingRank), scale);
                            receiver.drawScoreFont(engine, playerID, 10, topY + i, String.valueOf(rankingLevel[i]), (i == rankingRank), scale);
                            receiver.drawScoreFont(engine, playerID, 16, topY + i, GeneralUtil.getTime(rankingTime[i]), (i == rankingRank), scale);
                        }

                        receiver.drawScoreFont(engine, playerID, 0, 18, "LOCAL SCORES", EventReceiver.COLOR_BLUE);
                        if (!playerProperties.isLoggedIn())
                            receiver.drawScoreFont(engine, playerID, 0, 19, "(NOT LOGGED IN)\n(E:LOG IN)");
                        if (playerProperties.isLoggedIn())
                            receiver.drawScoreFont(engine, playerID, 0, 22, "D:SWITCH RANK SCREEN", EventReceiver.COLOR_GREEN);

                    }

                    receiver.drawScoreFont(engine, playerID, 0, 17, "F:VIEW SECTION TIME", EventReceiver.COLOR_GREEN);
                } else {
                    // Section Time
                    receiver.drawScoreFont(engine, playerID, 0, 2, "SECTION TIME", EventReceiver.COLOR_BLUE);

                    int totalTime = 0;
                    for (int i = 0; i < SECTION_MAX; i++) {
                        int temp = Math.min(i * 100, 200);
                        int temp2 = Math.min(((i + 1) * 100) - 1, 200);

                        String strSectionTime;
                        strSectionTime = String.format("%3d-%3d %s", temp, temp2, GeneralUtil.getTime(bestSectionTime[i]));

                        receiver.drawScoreFont(engine, playerID, 0, 3 + i, strSectionTime, sectionIsPB[i]);

                        totalTime += bestSectionTime[i];
                    }

                    receiver.drawScoreFont(engine, playerID, 0, 14, "TOTAL", EventReceiver.COLOR_BLUE);
                    receiver.drawScoreFont(engine, playerID, 0, 15, GeneralUtil.getTime(totalTime));
                    receiver.drawScoreFont(engine, playerID, 9, 14, "AVERAGE", EventReceiver.COLOR_BLUE);
                    receiver.drawScoreFont(engine, playerID, 9, 15, GeneralUtil.getTime(totalTime / SECTION_MAX));

                    receiver.drawScoreFont(engine, playerID, 0, 17, "F:VIEW RANKING", EventReceiver.COLOR_GREEN);
                }
            }
        } else if (engine.stat == GameEngine.STAT_CUSTOM) {
            playerProperties.loginScreen.renderScreen(receiver, engine, playerID);
        } else {
            receiver.drawScoreFont(engine, playerID, 0, 2, "FIREWORKS", EventReceiver.COLOR_BLUE);
            receiver.drawScoreFont(engine, playerID, 0, 3, String.valueOf(fireworksFired), (scoreColorTimer > 0));

            receiver.drawScoreFont(engine, playerID, 0, 5, "LEVEL", EventReceiver.COLOR_BLUE);
            int tempLevel = engine.statistics.level;
            if (tempLevel < 0) tempLevel = 0;
            String strLevel = String.format("%3d", tempLevel);
            receiver.drawScoreFont(engine, playerID, 0, 6, strLevel);

            int speed = engine.speed.gravity / 128;
            if (engine.speed.gravity < 0) speed = 40;
            receiver.drawSpeedMeter(engine, playerID, 0, 7, speed);

            receiver.drawScoreFont(engine, playerID, 0, 8, String.format("%3d", nextSectionLevel));

            receiver.drawScoreFont(engine, playerID, 0, 10, "TIME", EventReceiver.COLOR_BLUE);
            receiver.drawScoreFont(engine, playerID, 0, 11, GeneralUtil.getTime(engine.statistics.time));

            if (playerProperties.isLoggedIn() || PLAYER_NAME.length() > 0) {
                receiver.drawScoreFont(engine, playerID, 0, 16, "PLAYER", EventReceiver.COLOR_BLUE);
                receiver.drawScoreFont(engine, playerID, 0, 17, owner.replayMode ? PLAYER_NAME : playerProperties.getNameDisplay(), EventReceiver.COLOR_WHITE, 2f);
            }

            int baseX = receiver.getFieldDisplayPositionX(engine, playerID) + 4;
            int baseY = receiver.getFieldDisplayPositionY(engine, playerID) + 52;
            if (pCoordList.size() > 0 && cPiece != null) {
                for (int[] loc : pCoordList) {
                    int cx = baseX + (16 * loc[0]);
                    int cy = baseY + (16 * loc[1]);
                    rendererExtension.drawScaledPiece(receiver, engine, playerID, cx, cy, cPiece, 1f, 0f);
                }
            }

            if ((engine.gameActive) && (engine.ending == 2)) {
                int time = MAX_ROLL_TIME - rollTime;
                if (time < 0) time = 0;
                receiver.drawScoreFont(engine, playerID, 0, 13, "ROLL TIME", EventReceiver.COLOR_BLUE);
                receiver.drawScoreFont(engine, playerID, 0, 14, GeneralUtil.getTime(time), ((time > 0) && (time < 10 * 60)));
                creditObject.drawAtY(engine, receiver, playerID, 27.25, engine.displaysize + 1, (double) rollTime / MAX_ROLL_TIME);
            } /* else {
				receiver.drawScoreFont(engine, playerID, 0, 13, "PENDING FIREWORKS", EventReceiver.COLOR_BLUE);
				receiver.drawScoreFont(engine, playerID, 0, 14, String.valueOf(totalFireworkQueue));
			} */

            // Section Time
            if ((showST == true) && (sectionTime != null)) {
                int x = (receiver.getNextDisplayType() == 2) ? 8 : 12;
                int x2 = (receiver.getNextDisplayType() == 2) ? 9 : 12;
                receiver.drawScoreFont(engine, playerID, x, 2, "SECTION TIME", EventReceiver.COLOR_BLUE);

                for (int i = 0; i < sectionTime.length; i++) {
                    if (sectionTime[i] > 0) {
                        int temp = i * 100;
                        if (temp > 200) temp = 200;

                        int section = engine.statistics.level / 100;
                        String strSeparator = " ";
                        if ((i == section) && (engine.ending == 0)) strSeparator = "b";

                        String strSectionTime;
                        strSectionTime = String.format("%3d%s%s", temp, strSeparator, GeneralUtil.getTime(sectionTime[i]));

                        receiver.drawScoreFont(engine, playerID, x, 3 + i, strSectionTime, sectionIsPB[i]);
                    }
                }

                if (averageSectionTime > 0) {
                    receiver.drawScoreFont(engine, playerID, x2, 14, "AVERAGE", EventReceiver.COLOR_BLUE);
                    receiver.drawScoreFont(engine, playerID, x2, 15, GeneralUtil.getTime(averageSectionTime));
                }
            }

            if (blockParticles != null) blockParticles.drawAll(engine, receiver, playerID);
        }

        if (fireworkEmitter != null) fireworkEmitter.draw(receiver);
    }

    /*
     * 移動中の処理
     */
    @Override
    public boolean onMove(GameEngine engine, int playerID) {
        // 新規ピース出現時
        if ((engine.ending == 0) && (engine.statc[0] == 0) && (engine.holdDisable == false) && (!levelUpFlag)) {
            // Level up
            if (engine.statistics.level < nextSectionLevel - 1) {
                engine.statistics.level++;
                if (engine.statistics.level == nextSectionLevel - 1) engine.playSE("levelstop");
            }
            levelUp(engine);
        }
        if ((engine.ending == 0) && (engine.statc[0] > 0)) {
            levelUpFlag = false;
        }

        // "Killscreen" at 15 minutes
        if (engine.ending == 0 && engine.statistics.time >= (15 * 3600) && !killed) {
            engine.speed.lineDelay = 4;
            engine.speed.are = 6;
            engine.speed.areLine = 6;
            engine.speed.lockDelay = 15;
            killed = true;
        }

        // Endingスタート
        if ((engine.ending == 2) && (!rollStarted)) {
            rollStarted = true;
            owner.bgmStatus.fadesw = false;
            owner.bgmStatus.bgm = BGMStatus.BGM_ENDING2;
        }

        return false;
    }

    /**
     * levelが上がったときの共通処理
     */
    private void levelUp(GameEngine engine) {
        // Meter
        engine.meterValue = ((engine.statistics.level % 100) * receiver.getMeterMax(engine)) / 99;
        engine.meterColor = GameEngine.METER_COLOR_GREEN;
        if (engine.statistics.level % 100 >= 50) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
        if (engine.statistics.level % 100 >= 80) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
        if (engine.statistics.level == nextSectionLevel - 1) engine.meterColor = GameEngine.METER_COLOR_RED;

        // 速度変更
        setSpeed(engine);

        // LV100到達でghost を消す
        // if((engine.statistics.level >= 100) && (!tlsMode)) engine.ghost = false;

        if (engine.statistics.level >= BGM_FADEOUT_LV) {
            owner.bgmStatus.fadesw = true;
        }
    }

    /*
     * ARE中の処理
     */
    @Override
    public boolean onARE(GameEngine engine, int playerID) {
        // 最後の frame
        if ((engine.ending == 0) && (engine.statc[0] >= engine.statc[1] - 1) && (!levelUpFlag)) {
            if (engine.statistics.level < nextSectionLevel - 1) {
                engine.statistics.level++;
                if (engine.statistics.level == nextSectionLevel - 1) engine.playSE("levelstop");
            }
            levelUp(engine);
            levelUpFlag = true;
        }

        return false;
    }

    /*
     * Calculate score
     */
    @Override
    public void calcScore(GameEngine engine, int playerID, int lines) {
        if ((lines >= 1) && (engine.ending == 0)) {
            if (lines >= 2) isSplit = FieldManipulation.checkLineForSplit(engine.field);

            if (engine.field.isEmpty()) {
                engine.playSE("bravo");
                bravoFireworkQueue += 6;
            }

            // firework calculation
            calculateVarispeedCombo(engine, engine.statistics.level + lines);
            calculateVarispeedFinesse(engine, engine.statistics.level + lines);

            int usedLines = lines - 1;
            if (usedLines > 3) usedLines = 3;

            int usedCombo = engine.combo;
            if (usedCombo > 9) usedCombo = 9;

            double fireworkTotal = BASE_LINE_VALUES[usedLines];
            if (lastFireworkTimer > 0) fireworkTotal *= FIXED_SPEED_COMBO_BONUS;
            fireworkTotal *= varispeedComboBonus;
            fireworkTotal *= varispeedFinesseBonus;
            if (lines > 1) fireworkTotal *= ONMOVE_COMBO_MULTIPLIERS[usedCombo];
            if (Arrays.stream(LUCKY_LEVELS).anyMatch(i -> i == engine.statistics.level))
                fireworkTotal *= LUCKY_LEVEL_BONUS;
            if (give30sBonus) {
                fireworkTotal *= THIRTY_BONUS;
                give30sBonus = false;
            }
            if (isSplit) fireworkTotal *= SPLIT_BONUS;

            if (engine.lastmove == 5 || engine.lastmove == 6) {
                lastPiece = engine.nowPieceObject.id;
                if (!Piece.getPieceName(lastPiece).equals("O")) {
                    if (Piece.getPieceName(lastPiece).equals("T")) {
                        if (lines >= 3) {
                            fireworkTotal *= 4;
                        } else {
                            fireworkTotal *= 3;
                        }
                    } else {
                        fireworkTotal *= 2;
                    }
                    engine.playSE("tspin0");
                }
            }

            comboFireworkQueue += (int) fireworkTotal;

            // Level up
            int levelplus = lines;
            engine.statistics.level += levelplus;

            levelUp(engine);

            if (engine.statistics.level >= 200) {
                // Ending
                if (engine.ending == 0) {
                    comboDuringCredits = engine.combo;
                    if (comboDuringCredits > 9) comboDuringCredits = 9;
                    comboDuringCredits = CREDIT_COMBO_CONVERSION[comboDuringCredits];

                    creditsFreeFireworkDelay = MAX_ROLL_TIME / fireworksFired;
                }

                engine.statistics.level = 200;
                engine.timerActive = false;
                engine.ending = 1;

                // Section Timeを記録
                stNewRecordCheck(sectionsComplete);
                sectionsComplete++;
                setAverageSectionTime();
            } else if (engine.statistics.level >= nextSectionLevel) {
                // Next Section
                engine.playSE("levelup");

                // Background切り替え
                owner.backgroundStatus.fadesw = true;
                owner.backgroundStatus.fadecount = 0;
                owner.backgroundStatus.fadebg = 1;

                // Section Timeを記録
                stNewRecordCheck(sectionsComplete);
                sectionsComplete++;
                setAverageSectionTime();

                // Update level for next section
                nextSectionLevel += 100;
                if (nextSectionLevel > 200) nextSectionLevel = 200;
            } else if (engine.statistics.level == nextSectionLevel - 1) {
                engine.playSE("levelstop");
            }
        } else if (lines >= 1) {
            if (lines >= 2) isSplit = FieldManipulation.checkLineForSplit(engine.field);

            // firework calculation
            calculateVarispeedCombo(engine, engine.statistics.level);
            calculateVarispeedFinesse(engine, engine.statistics.level);

            int usedLines = lines - 1;
            if (usedLines > 3) usedLines = 3;

            double fireworkTotal = BASE_LINE_VALUES[usedLines];
            if (lastFireworkTimer > 0) fireworkTotal *= FIXED_SPEED_COMBO_BONUS;
            fireworkTotal *= varispeedComboBonus;
            fireworkTotal *= varispeedFinesseBonus;
            if (lines > 1) fireworkTotal *= CREDIT_COMBO_MULTIPLIERS[comboDuringCredits];
            if (engine.statistics.level < 200)
                if (Arrays.stream(LUCKY_LEVELS).anyMatch(i -> i == engine.statistics.level))
                    fireworkTotal *= LUCKY_LEVEL_BONUS;
            if (give30sBonus) {
                fireworkTotal *= THIRTY_BONUS;
                give30sBonus = false;
            }
            if (isSplit) fireworkTotal *= SPLIT_BONUS;

            if (engine.lastmove == 5 || engine.lastmove == 6) {
                lastPiece = engine.nowPieceObject.id;
                if (!Piece.getPieceName(lastPiece).equals("O")) {
                    if (Piece.getPieceName(lastPiece) == "T") {
                        if (lines >= 3) {
                            fireworkTotal *= 4;
                        } else {
                            fireworkTotal *= 3;
                        }
                    } else {
                        fireworkTotal *= 2;
                    }
                    engine.playSE("tspin0");
                }
            }

            comboFireworkQueue += (int) fireworkTotal;
        }
        isSplit = false;
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
                                    blockParticles.addBlock(engine, receiver, playerID, blk, j, i, engine.field.getWidth(), cY, li, 120);
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
                            // blockParticles.addBlock(engine, receiver, playerID, blk, j, i, 4, 60, localRandom);
                            if (engine.displaysize == 1) {
                                //owner.receiver.blockBreak(engine, playerID, 2*j, 2*i, blk);
                                //owner.receiver.blockBreak(engine, playerID, 2*j+1, 2*i, blk);
                                //owner.receiver.blockBreak(engine, playerID, 2*j, 2*i+1, blk);
                                //owner.receiver.blockBreak(engine, playerID, 2*j+1, 2*i+1, blk);

                                blockParticles.addBlock(engine, receiver, playerID, blk, 2 * j, 2 * i, engine.field.getWidth(), 1, 1, 120);
                                blockParticles.addBlock(engine, receiver, playerID, blk, 2 * j + 1, 2 * i, engine.field.getWidth(), 1, 1, 120);
                                blockParticles.addBlock(engine, receiver, playerID, blk, 2 * j, 2 * i + 1, engine.field.getWidth(), 1, 1, 120);
                                blockParticles.addBlock(engine, receiver, playerID, blk, 2 * j + 1, 2 * i + 1, engine.field.getWidth(), 1, 1, 120);
                            } else
                                //owner.receiver.blockBreak(engine, playerID, j, i, blk);
                                blockParticles.addBlock(engine, receiver, playerID, blk, j, i, engine.field.getWidth(), 1, 1, 120);
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

            boolean skip = false;
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

        return false;
    }

    @Override
    public void onLast(GameEngine engine, int playerID) {
        if (scoreColorTimer > 0) scoreColorTimer--;

        // Section Time増加
        if ((engine.timerActive) && (engine.ending == 0)) {
            int section = engine.statistics.level / 100;

            if ((section >= 0) && (section < sectionTime.length)) {
                sectionTime[section]++;
            }

            if (engine.statistics.time % 1800 == 0) {
                give30sBonus = engine.statistics.level - lvAtLastSplit >= 30;

                lvAtLastSplit = engine.statistics.level;
            }

            if (lastFireworkTimer > 0) lastFireworkTimer--;
        }

        if (engine.gameActive) {
            totalFireworkQueue = bravoFireworkQueue + comboFireworkQueue + genericFireworkQueue;
            if (totalFireworkQueue > 0 || engine.ending == 2) {
                updateFireworkTimers(engine, playerID);
            }

            lastLineClearTime++;
        }

        // Ending
        if ((engine.gameActive) && (engine.ending == 2)) {
            rollTime++;

            // Time meter
            int remainRollTime = MAX_ROLL_TIME - rollTime;
            engine.meterValue = (remainRollTime * receiver.getMeterMax(engine)) / MAX_ROLL_TIME;
            engine.meterColor = GameEngine.METER_COLOR_GREEN;
            if (remainRollTime <= 30 * 60) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
            if (remainRollTime <= 20 * 60) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
            if (remainRollTime <= 10 * 60) engine.meterColor = GameEngine.METER_COLOR_RED;

            // Roll 終了
            if (rollTime >= MAX_ROLL_TIME) {

                engine.blockOutlineType = GameEngine.BLOCK_OUTLINE_NORMAL;
                engine.blockShowOutlineOnly = false;

                engine.gameEnded();
                engine.resetStatc();
                engine.stat = GameEngine.STAT_EXCELLENT;
            }
        }

        if (blockParticles != null) {
            blockParticles.update();
        }

        if ((engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (!owner.replayMode)) || engine.stat == GameEngine.STAT_CUSTOM) {
            // Show rank
            if (engine.ctrl.isPush(Controller.BUTTON_D) && playerProperties.isLoggedIn() && engine.stat != GameEngine.STAT_CUSTOM) {
                showPlayerStats = !showPlayerStats;
                engine.playSE("change");
            }
        }

        if (engine.quitflag) {
            playerProperties = new ProfileProperties(headerColour);
        }

        if (fireworkEmitter != null) fireworkEmitter.update();
    }

    /*
     * 結果画面
     */
    @Override
    public void renderResult(GameEngine engine, int playerID) {
        receiver.drawMenuFont(engine, playerID, 0, 0, "kn PAGE" + (engine.statc[1] + 1) + "/3", EventReceiver.COLOR_RED);

        if (engine.statc[1] == 0) {
            drawResult(engine, playerID, receiver, 2, EventReceiver.COLOR_BLUE,
                "FIREWORKS", String.format("%10d", fireworksFired));
            drawResultStats(engine, playerID, receiver, 4, EventReceiver.COLOR_BLUE,
                STAT_LINES, STAT_LEVEL_MANIA, STAT_TIME);
            drawResultRank(engine, playerID, receiver, 10, EventReceiver.COLOR_BLUE, rankingRank);
        } else if (engine.statc[1] == 1) {
            receiver.drawMenuFont(engine, playerID, 0, 2, "SECTION", EventReceiver.COLOR_BLUE);

            for (int i = 0; i < sectionTime.length; i++) {
                if (sectionTime[i] > 0) {
                    receiver.drawMenuFont(engine, playerID, 2, 3 + i, GeneralUtil.getTime(sectionTime[i]), sectionIsPB[i]);
                }
            }

            if (averageSectionTime > 0) {
                receiver.drawMenuFont(engine, playerID, 0, 14, "AVERAGE", EventReceiver.COLOR_BLUE);
                receiver.drawMenuFont(engine, playerID, 2, 15, GeneralUtil.getTime(averageSectionTime));
            }
        } else if (engine.statc[1] == 2) {
            drawResultStats(engine, playerID, receiver, 2, EventReceiver.COLOR_BLUE,
                STAT_LPM, STAT_PIECE, STAT_PPS);
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
            if (engine.statc[1] < 0) engine.statc[1] = 2;
            engine.playSE("change");
        }
        if (engine.ctrl.isMenuRepeatKey(Controller.BUTTON_DOWN)) {
            engine.statc[1]++;
            if (engine.statc[1] > 2) engine.statc[1] = 0;
            engine.playSE("change");
        }
        //  section time display切替
        if (engine.ctrl.isPush(Controller.BUTTON_F)) {
            engine.playSE("change");
            showBests = !showBests;
        }

        return false;
    }

    @Override
    public boolean onExcellent(GameEngine engine, int playerID) {
        if (!addedCompletionFireworks) {
            genericFireworkQueue += COMPLETION_FIREWORK_REWARD;
            if (totalFireworkQueue == 0) totalFireworkQueue += COMPLETION_FIREWORK_REWARD;
            addedCompletionFireworks = true;
        }

        if (totalFireworkQueue > 0) {
            updateFireworkTimers(engine, playerID);
            return true;
        } else {
            return false;
        }
    }

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

                        if (blk != null) {
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
            } else if (engine.statc[0] < engine.field.getHeight() + 1 + 300 || totalFireworkQueue > 0) {
                updateFireworkTimers(engine, playerID);

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
                engine.stat = GameEngine.STAT_MOVE;
            }
        }

        return false;
    }

    /*
     * Maybe it works???
     */
    private void launchFirework(GameEngine engine, int playerID) {
        int[] colour = Fireworks.DEF_COLOURS[fireworkRandomiser.nextInt(Fireworks.DEF_COLOURS.length)];
//		Block dummyBlock = new Block(blockCol);

//		int x = fireworkRandomiser.nextInt(17) - 3;
//		int y = 3 + (fireworkRandomiser.nextInt(7) - 3);
//
//		receiver.blockBreak(engine, playerID, x, y, dummyBlock);

        int minx = receiver.getFieldDisplayPositionX(engine, playerID) - 48;
        int maxx = receiver.getFieldDisplayPositionX(engine, playerID) + (engine.field.getWidth() * 16) + 48;
        int miny = receiver.getFieldDisplayPositionY(engine, playerID) - 48;
        int maxy = receiver.getFieldDisplayPositionY(engine, playerID) + (16 * 7);

        fireworkEmitter.addNumber(1,
            new Object[] {
                minx, maxx, miny, maxy,
                colour[0], colour[1], colour[2], colour[3], colour[4],
                Fireworks.DEF_MAX_VEL,
                45, 75
            });

        lastFireworkTimer = 100;
        scoreColorTimer = 2;
        fireworksFired++;
    }

    /**
     * Load settings from property file
     *
     * @param prop Property file
     */
    private void loadSetting(CustomProperties prop) {
        // tlsMode = prop.getProperty("fireworkchallenge.alwaysghost", false);
        maxGravMode = prop.getProperty("fireworkchallenge.always20g", false);
        showST = prop.getProperty("fireworkchallenge.showsectiontime", false);
        big = prop.getProperty("fireworkchallenge.big", false);
    }

    /**
     * Save settings to property file
     *
     * @param prop Property file
     */
    private void saveSettingPlayer(ProfileProperties prop) {
        if (!prop.isLoggedIn()) return;
        // prop.setProperty("fireworkchallenge.alwaysghost", tlsMode);
        prop.setProperty("fireworkchallenge.always20g", maxGravMode);
        prop.setProperty("fireworkchallenge.big", big);
        prop.setProperty("fireworkchallenge.showsectiontime", showST);
    }

    /**
     * Load settings from property file
     *
     * @param prop Property file
     */
    private void loadSettingPlayer(ProfileProperties prop) {
        if (!prop.isLoggedIn()) return;
        // tlsMode = prop.getProperty("fireworkchallenge.alwaysghost", false);
        maxGravMode = prop.getProperty("fireworkchallenge.always20g", false);
        showST = prop.getProperty("fireworkchallenge.showsectiontime", false);
        big = prop.getProperty("fireworkchallenge.big", false);
    }

    /**
     * Save settings to property file
     *
     * @param prop Property file
     */
    private void saveSetting(CustomProperties prop) {
        // prop.setProperty("fireworkchallenge.alwaysghost", tlsMode);
        prop.setProperty("fireworkchallenge.always20g", maxGravMode);
        prop.setProperty("fireworkchallenge.big", big);
        prop.setProperty("fireworkchallenge.showsectiontime", showST);
    }

    /*
     * リプレイ保存
     */
    @Override
    public void saveReplay(GameEngine engine, int playerID, CustomProperties prop) {
        saveSetting(owner.replayProp);
        owner.replayProp.setProperty("result.firework", fireworksFired);

        // Update rankings
        if ((owner.replayMode == false) && (maxGravMode == false) && (big == false) && (engine.ai == null)) {
            updateRanking(fireworksFired, engine.statistics.level, engine.statistics.time);
            if (sectionPBGet) updateBestSectionTime();

            if (playerProperties.isLoggedIn()) {
                prop.setProperty("fireworkchallenge.playerName", playerProperties.getNameDisplay());
            }

            if ((rankingRank != -1) || (sectionPBGet)) {
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
     * Read rankings from property file
     *
     * @param prop     Property file
     * @param ruleName Rule name
     */
    private void loadRanking(CustomProperties prop, String ruleName) {
        for (int i = 0; i < RANKING_MAX; i++) {
            rankingFireworks[i] = prop.getProperty("fireworkchallenge.ranking." + ruleName + ".fireworks." + i, 0);
            rankingLevel[i] = prop.getProperty("fireworkchallenge.ranking." + ruleName + ".level." + i, 0);
            rankingTime[i] = prop.getProperty("fireworkchallenge.ranking." + ruleName + ".time." + i, 0);
        }
        for (int i = 0; i < SECTION_MAX; i++) {
            bestSectionTime[i] = prop.getProperty("fireworkchallenge.bestSectionTime." + ruleName + "." + i, DEFAULT_SECTION_TIME);
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
            prop.setProperty("fireworkchallenge.ranking." + ruleName + ".fireworks." + i, rankingFireworks[i]);
            prop.setProperty("fireworkchallenge.ranking." + ruleName + ".level." + i, rankingLevel[i]);
            prop.setProperty("fireworkchallenge.ranking." + ruleName + ".time." + i, rankingTime[i]);
        }
        for (int i = 0; i < SECTION_MAX; i++) {
            prop.setProperty("fireworkchallenge.bestSectionTime." + ruleName + "." + i, bestSectionTime[i]);
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
            rankingFireworksPlayer[i] = prop.getProperty("fireworkchallenge.ranking." + ruleName + ".fireworks." + i, 0);
            rankingLevelPlayer[i] = prop.getProperty("fireworkchallenge.ranking." + ruleName + ".level." + i, 0);
            rankingTimePlayer[i] = prop.getProperty("fireworkchallenge.ranking." + ruleName + ".time." + i, 0);
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
            prop.setProperty("fireworkchallenge.ranking." + ruleName + ".fireworks." + i, rankingFireworksPlayer[i]);
            prop.setProperty("fireworkchallenge.ranking." + ruleName + ".level." + i, rankingLevelPlayer[i]);
            prop.setProperty("fireworkchallenge.ranking." + ruleName + ".time." + i, rankingTimePlayer[i]);
        }
    }

    /**
     * Update rankings
     *
     * @param gr   段位
     * @param lv   level
     * @param time Time
     */
    private void updateRanking(int gr, int lv, int time) {
        rankingRank = checkRanking(gr, lv, time);

        if (rankingRank != -1) {
            // Shift down ranking entries
            for (int i = RANKING_MAX - 1; i > rankingRank; i--) {
                rankingFireworks[i] = rankingFireworks[i - 1];
                rankingLevel[i] = rankingLevel[i - 1];
                rankingTime[i] = rankingTime[i - 1];
            }

            // Add new data
            rankingFireworks[rankingRank] = gr;
            rankingLevel[rankingRank] = lv;
            rankingTime[rankingRank] = time;
        }

        if (playerProperties.isLoggedIn()) {
            rankingRankPlayer = checkRankingPlayer(gr, lv, time);

            if (rankingRankPlayer != -1) {
                // Shift down ranking entries
                for (int i = RANKING_MAX - 1; i > rankingRankPlayer; i--) {
                    rankingFireworksPlayer[i] = rankingFireworksPlayer[i - 1];
                    rankingLevelPlayer[i] = rankingLevelPlayer[i - 1];
                    rankingTimePlayer[i] = rankingTimePlayer[i - 1];
                }

                // Add new data
                rankingFireworksPlayer[rankingRankPlayer] = gr;
                rankingLevelPlayer[rankingRankPlayer] = lv;
                rankingTimePlayer[rankingRankPlayer] = time;
            }
        }
    }

    /**
     * Calculate ranking position
     *
     * @param gr   段位
     * @param lv   level
     * @param time Time
     * @return Position (-1 if unranked)
     */
    private int checkRanking(int gr, int lv, int time) {
        for (int i = 0; i < RANKING_MAX; i++) {
            if (gr > rankingFireworks[i]) {
                return i;
            } else if ((gr == rankingFireworks[i]) && (lv > rankingLevel[i])) {
                return i;
            } else if ((gr == rankingFireworks[i]) && (lv == rankingLevel[i]) && (time < rankingTime[i])) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Calculate ranking position
     *
     * @param gr   段位
     * @param lv   level
     * @param time Time
     * @return Position (-1 if unranked)
     */
    private int checkRankingPlayer(int gr, int lv, int time) {
        for (int i = 0; i < RANKING_MAX; i++) {
            if (gr > rankingFireworksPlayer[i]) {
                return i;
            } else if ((gr == rankingFireworksPlayer[i]) && (lv > rankingLevelPlayer[i])) {
                return i;
            } else if ((gr == rankingFireworksPlayer[i]) && (lv == rankingLevelPlayer[i]) && (time < rankingTimePlayer[i])) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Update best section time records
     */
    private void updateBestSectionTime() {
        for (int i = 0; i < SECTION_MAX; i++) {
            if (sectionIsPB[i]) {
                bestSectionTime[i] = sectionTime[i];
            }
        }
    }

    // Update all the firework timers. Just call this.
    private void updateFireworkTimers(GameEngine engine, int playerID) {
        boolean sp = false;
        if (rollStarted && (engine.stat != GameEngine.STAT_EXCELLENT) && (engine.stat != GameEngine.STAT_GAMEOVER)) {
            creditsFreeFireworkTimer++;
        }

        if (bravoFireworkQueue > 0) bravoFireworkTimer++;
        if (comboFireworkQueue > 0) comboFireworkTimer++;

        if (engine.stat == GameEngine.STAT_EXCELLENT) {
            if (genericFireworkQueue > 0) completionFireworkTimer++;
        }

        if (bravoFireworkTimer >= COMBO_BRAVO_DELAY) {
            bravoFireworkTimer = 0;
            launchFirework(engine, playerID);
            bravoFireworkQueue--;

            if (!sp) {
                engine.playSE("fireworklaunch");
                engine.playSE("fireworkexplode");
                sp = true;
            }
        }
        if (comboFireworkTimer >= COMBO_BRAVO_DELAY) {
            comboFireworkTimer = 0;
            launchFirework(engine, playerID);
            comboFireworkQueue--;

            if (!sp) {
                engine.playSE("fireworklaunch");
                engine.playSE("fireworkexplode");
                sp = true;
            }
        }
        if (completionFireworkTimer >= COMPLETION_DELAY) {
            completionFireworkTimer = 0;
            launchFirework(engine, playerID);
            genericFireworkQueue--;

            if (!sp) {
                engine.playSE("fireworklaunch");
                engine.playSE("fireworkexplode");
                sp = true;
            }
        }
        if (creditsFreeFireworkTimer >= creditsFreeFireworkDelay && rollStarted && (engine.stat != GameEngine.STAT_EXCELLENT) && (engine.stat != GameEngine.STAT_GAMEOVER)) {
            creditsFreeFireworkTimer = 0;
            launchFirework(engine, playerID);

            if (!sp) {
                engine.playSE("fireworklaunch");
                engine.playSE("fireworkexplode");
                sp = true;
            }
        }

        totalFireworkQueue = bravoFireworkQueue + comboFireworkQueue + genericFireworkQueue;

        // log.debug(String.format("T: %d, BR: %d, CB: %d, CL: %d, CFD: %d, CFT: %d", totalFireworkQueue, bravoFireworkQueue, comboFireworkQueue, genericFireworkQueue, creditsFreeFireworkDelay, creditsFreeFireworkTimer));
    }

    private void calculateVarispeedCombo(GameEngine engine, int level) {
        int frame = level - lastLineClearTime;
        if (frame < 100) frame = 100;

        varispeedComboBonus = (frame * 0.01);
    }

    private void calculateVarispeedFinesse(GameEngine engine, int level) {
        int frame = level - engine.statc[0];
        if (frame < 120) frame = 120;

        varispeedFinesseBonus = (frame / 120.0);
    }
}
