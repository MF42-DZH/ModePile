package zeroxfc.nullpo.custom.modes;

import java.util.ArrayList;
import java.util.Random;
import mu.nu.nullpo.game.component.BGMStatus;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.util.CustomProperties;
import mu.nu.nullpo.util.GeneralUtil;
import zeroxfc.nullpo.custom.libs.GameTextUtilities;
import zeroxfc.nullpo.custom.libs.ProfileProperties;
import zeroxfc.nullpo.custom.libs.RendererExtension;
import zeroxfc.nullpo.custom.libs.SoundLoader;

public class Constantris extends MarathonModeBase {
    private static final int DIFFICULTIES = 4;
    private static final int CURRENT_VERSION = 5;

    private static final int DIFFICULTY_EASY = 0,
        DIFFICULTY_NORMAL = 1,
        DIFFICULTY_HARD = 2,
        DIFFICULTY_VERY_HARD = 3;

    private static final String[] DIFFICULTY_NAMES = {
        "EASY", "NORMAL", "HARD", "VERY HARD"
    };

    private static final int[] GOAL_LINES = {
        // 5, 5, 5, 5, 5, 10, 10, 10, 10, 10,
        // 15, 15, 15, 15, 15, 20, 20, 20, 20, 20,
        // 30, 30, 30, 30, 30, 40, 40, 40, 40, 40,
        // 50, 50, 50, 50, 50, 60, 60, 60, 60, 60,
        // 75, 75, 75, 75, 75, 90, 90, 90, 90, 90,
        // 105, 105, 105, 105, 105, 120, 120, 120, 120, 120,
        // 140, 140, 140, 140, 140, 160, 160, 160, 160, 160,
        // 180, 180, 180, 180, 180, 200, 200, 200, 200, 200

        // 5, 5, 6, 6, 7, 7, 8, 8,
        // 9, 9, 10, 10, 11, 11, 12, 12, 13, 13,
        // 14, 14, 15, 15, 16, 16, 17, 17, 18, 18,
        // 19, 19, 20, 20, 20, 20, 20, 20, 20, 20,
        // 20, 20, 20, 20, 20, 20, 20, 20, 20, 20,
        // 20, 20, 20, 20, 20, 20, 20, 20, 20, 20,
        // 20, 20, 20, 20, 20, 20, 20, 20, 20, 20,
        // 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20

        5, 5, 5, 5, 6, 6, 6, 6,
        7, 7, 7, 7, 8, 8, 8, 8,
        9, 9, 9, 9, 10, 10, 10, 10,
        11, 11, 11, 11, 12, 12, 12, 12,
        13, 13, 13, 13, 14, 14, 14, 14,
        15, 15, 15, 15, 16, 16, 16, 16,
        17, 17, 17, 17, 18, 18, 18, 18,
        19, 19, 19, 19, 20, 20, 20, 20,
        20, 20, 20, 20, 20, 20, 20, 20,
        20, 20, 20, 20, 20, 20, 20, 20,
    };

    private static final int[] GOAL_LEVELS = { 20, 40, 60, 80 };
    private static final int[] GOAL_LINE_TOTALS = { sumOf(GOAL_LEVELS[0]), sumOf(GOAL_LEVELS[1]), sumOf(GOAL_LEVELS[2]), sumOf(GOAL_LEVELS[3]) };
    private static final int[] STARTING_SPARE_TIME = { 50, 30, 20, 10 };
    private static final int[] STALLING_LIMITS = { 150, 300 };
    private static final int[] PENALTY_MULTIPLIER = { 1, 1, 2, 4 };
    private static final int BASE_TOP_OUT_PENALTY = 10;
    private static final int RESTRICTION_NONE = 0,
        RESTRICTION_NO_HARD_DROP = 1,
        RESTRICTION_NO_NEXT_VIEW = 2,
        RESTRICTION_NO_STALLING = 3;
    private static final int TIME_QUEUE_TICK = 6;
    private static final int HEADER = EventReceiver.COLOR_YELLOW;
    private int difficulty;
    private int currentLineTarget;
    private int currentTimeTarget;
    private int timeReduceQueue, timeIncreaseQueue, timeReduceDelay, timeIncreaseDelay;
    private int currentStallRestriction;
    private int restrictionViolationFrame;
    private int bonusFrame;
    private int lastChange;
    private int changeFrame;
    private boolean hasPenalised;
    private boolean unfair;
    private int streak;
    private int spareTime;
    private int restriction;
    private Random localRandom;

    private int honkTimer;

    private ProfileProperties playerProperties;
    private int[][] rankingScorePlayer;
    private int[][] rankingLinesPlayer;
    private int[][] rankingTimePlayer;
    private int rankingRankPlayer;
    private boolean showPlayerStats;
    private String PLAYER_NAME;
    /**
     * The good hard drop effect
     */
    private ArrayList<int[]> pCoordList;
    private Piece cPiece;

    private RendererExtension rendererExtension;
    private boolean hardDropEffect;

    private static int sumOf(int max) {
        int sum = 0;

        for (int i = 0; i < max; i++) {
            sum += Constantris.GOAL_LINES[i];
        }

        return sum;
    }

    // region Mode Block

    /**
     * Returns the mode name.
     *
     * @return <code>String</code>
     */
    @Override
    public String getName() {
        return "CONSTANTRIS";
    }

    /*
     * Initialization
     */
    @Override
    public void playerInit(GameEngine engine, int playerID) {
        owner = engine.owner;
        receiver = engine.owner.receiver;

        if (playerProperties == null) {
            showPlayerStats = false;
            playerProperties = new ProfileProperties(HEADER);
        }

        pCoordList = new ArrayList<>();
        cPiece = null;

        lastscore = 0;
        scgettime = 0;
        lastevent = EVENT_NONE;
        lastb2b = false;
        lastcombo = 0;
        lastpiece = 0;
        bgmlv = 0;

        currentLineTarget = 0;
        currentTimeTarget = 0;
        timeReduceQueue = 0;
        timeIncreaseQueue = 0;
        timeReduceDelay = 0;
        timeIncreaseDelay = 0;
        restrictionViolationFrame = 0;
        bonusFrame = 0;
        changeFrame = 0;
        spareTime = 0;
        streak = 0;
        restriction = RESTRICTION_NONE;
        lastChange = 0;
        hasPenalised = false;

        rankingRank = -1;
        rankingScore = new int[DIFFICULTIES][RANKING_MAX];
        rankingLines = new int[DIFFICULTIES][RANKING_MAX];
        rankingTime = new int[DIFFICULTIES][RANKING_MAX];

        rankingRankPlayer = -1;
        rankingScorePlayer = new int[DIFFICULTIES][RANKING_MAX];
        rankingLinesPlayer = new int[DIFFICULTIES][RANKING_MAX];
        rankingTimePlayer = new int[DIFFICULTIES][RANKING_MAX];

        unfair = false;
        honkTimer = 0;

        showPlayerStats = false;

        rendererExtension = new RendererExtension();
        hardDropEffect = true;

        netPlayerInit(engine, playerID);

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
            if ((version == 0) && (owner.replayProp.getProperty("constantris.endless", false))) goaltype = 2;

            PLAYER_NAME = owner.replayProp.getProperty("constantris.playerName", "");

            // NET: Load name
            netPlayerName = engine.owner.replayProp.getProperty(playerID + ".net.netPlayerName", "");
        }

        SoundLoader.loadSoundset(SoundLoader.LOADTYPE_CONSTANTRIS);

        owner.backgroundStatus.bg = 0;
        engine.framecolor = GameEngine.FRAME_COLOR_YELLOW;
    }

    /*
     * Called at settings screen
     */
    @Override
    public boolean onSetting(GameEngine engine, int playerID) {
        // NET: Net Ranking
        if (netIsNetRankingDisplayMode) {
            netOnUpdateNetPlayRanking(engine, goaltype);
        }
        // Menu
        else if (!engine.owner.replayMode) {
            // Configuration changes
            int change = updateCursor(engine, 9, playerID);

            if (change != 0) {
                engine.playSE("change");

                switch (engine.statc[2]) {
                    case 0:
                        difficulty += change;
                        if (difficulty >= DIFFICULTIES) difficulty = 0;
                        else if (difficulty < 0) difficulty = DIFFICULTIES - 1;
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
                        big = !big;
                        break;
                    case 8:
                        unfair = !unfair;
                        break;
                    case 9:
                        hardDropEffect = !hardDropEffect;
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
                playerProperties = new ProfileProperties(HEADER);
            }

            // New acc
            if (engine.ctrl.isPush(Controller.BUTTON_E) && engine.ai == null) {
                playerProperties = new ProfileProperties(HEADER);
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
            drawMenu(engine, playerID, receiver, 0, EventReceiver.COLOR_BLUE, 0,
                "DIFFICULTY", DIFFICULTY_NAMES[difficulty],
                "SPIN BONUS", strTSpinEnable,
                "EZ SPIN", GeneralUtil.getONorOFF(enableTSpinKick),
                "SPIN TYPE", (spinCheckType == 0) ? "4POINT" : "IMMOBILE",
                "EZIMMOBILE", GeneralUtil.getONorOFF(tspinEnableEZ),
                "B2B", GeneralUtil.getONorOFF(enableB2B),
                "COMBO", GeneralUtil.getONorOFF(enableCombo),
                "BIG", GeneralUtil.getONorOFF(big));
            drawMenu(engine, playerID, receiver, 16, EventReceiver.COLOR_RED, 8,
                "MODIFIER", unfair ? "UNFAIR" : "FAIR");
            drawMenu(engine, playerID, receiver, 18, EventReceiver.COLOR_PINK, 9,
                "DROP EFF.", GeneralUtil.getONorOFF(hardDropEffect)
            );
        }
    }

    /*
     * Called for initialization during "Ready" screen
     */
    @Override
    public void startGame(GameEngine engine, int playerID) {
        localRandom = new Random(engine.randSeed + 1L);

        engine.statistics.level = 0;
        engine.statistics.levelDispAdd = 1;
        engine.b2bEnable = enableB2B;
        if (enableCombo) {
            engine.comboType = GameEngine.COMBO_TYPE_NORMAL;
        } else {
            engine.comboType = GameEngine.COMBO_TYPE_DISABLE;
        }
        engine.big = big;

        if (version >= 2) {
            engine.tspinAllowKick = enableTSpinKick;
            if (tspinEnableType == 0) {
                engine.tspinEnable = false;
            } else if (tspinEnableType == 1) {
                engine.tspinEnable = true;
            } else {
                engine.tspinEnable = true;
                engine.useAllSpinBonus = true;
            }
        } else {
            engine.tspinEnable = enableTSpin;
        }

        engine.spinCheckType = spinCheckType;
        engine.tspinEnableEZ = tspinEnableEZ;

        spareTime = STARTING_SPARE_TIME[difficulty];
        if (unfair && difficulty != DIFFICULTY_VERY_HARD) spareTime /= 2;
        setSpeed(engine);

        if (netIsWatch) {
            owner.bgmStatus.bgm = BGMStatus.BGM_NOTHING;
        }
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

    private void addTimeIncreaseQueue(int time) {
        timeIncreaseQueue += time;
        timeIncreaseDelay = 0;
        lastChange = time;
        changeFrame = 60;
    }

    private void addTimeReduceQueue(int time) {
        timeReduceQueue += ((unfair && difficulty != DIFFICULTY_VERY_HARD) ? time * 2 : time);
        timeReduceDelay = 0;
        lastChange = -time * ((unfair && difficulty != DIFFICULTY_VERY_HARD) ? 2 : 1);
        changeFrame = 60;
    }

    /**
     * Set the gravity rate
     *
     * @param engine GameEngine
     */
    public void setSpeed(GameEngine engine) {
        int lv = engine.statistics.level / 4;

        if (lv < 0) lv = 0;
        if (lv >= tableGravity.length) lv = tableGravity.length - 1;

        engine.speed.gravity = tableGravity[lv];
        engine.speed.denominator = tableDenominator[lv];

        currentLineTarget = sumOf(engine.statistics.level) + GOAL_LINES[engine.statistics.level];
        currentTimeTarget = currentTimeTarget + getTimeLimit(engine, GOAL_LINES[engine.statistics.level]);
    }

    @Override
    public boolean onGameOver(GameEngine engine, int playerID) {
        if (engine.statc[0] == 0) {
            int penalty = BASE_TOP_OUT_PENALTY * PENALTY_MULTIPLIER[difficulty];

            if (spareTime > (penalty * ((unfair && difficulty != DIFFICULTY_VERY_HARD) ? 2 : 1)) && engine.ending == 0) {
                engine.lives++;
                addTimeReduceQueue(penalty);
            } else if (spareTime > 0) {
                spareTime = 0;
                if (engine.ending == 0) engine.playSE("timereduce");
            }
        }

        return false;
    }

    @Override
    public boolean onExcellent(GameEngine engine, int playerID) {
        if (engine.statc[0] == 0) {
            engine.gameEnded();
            owner.bgmStatus.fadesw = true;

            engine.resetFieldVisible();

            engine.playSE("excellent");
        }

        if ((engine.statc[0] >= 120) && (engine.ctrl.isPush(Controller.BUTTON_A))) {
            engine.statc[0] = 600;
        }

        if ((engine.statc[0] >= 600) && (engine.statc[1] == 0) && (timeReduceQueue == 0 && timeIncreaseQueue == 0)) {
            if (unfair) spareTime *= 1.5;
            engine.statistics.score = spareTime;
            engine.resetStatc();
            engine.stat = GameEngine.STAT_GAMEOVER;
        } else {
            engine.statc[0]++;
        }

        return true;
    }

    @Override
    public void onFirst(GameEngine engine, int playerID) {
        int diff = currentTimeTarget - engine.statistics.time;

        if (engine.timerActive && engine.ctrl.isPush(Controller.BUTTON_F)) {
            engine.playSE("horn");
            if (diff > -getMaxDiff() && diff <= getMaxDiff()) honkTimer = getMaxDiff();
        }

        pCoordList.clear();
        cPiece = null;
    }

    @Override
    public void onLast(GameEngine engine, int playerID) {
        int diff = currentTimeTarget - engine.statistics.time;
        if (honkTimer > 0) --honkTimer;

        if (engine.statc[0] == 0 && engine.stat == GameEngine.STAT_MOVE) hasPenalised = false;
        if (restriction == RESTRICTION_NO_STALLING && engine.stat == GameEngine.STAT_MOVE && !hasPenalised && engine.statc[0] > currentStallRestriction) {
            addTimeReduceQueue(5 * PENALTY_MULTIPLIER[difficulty]);
            hasPenalised = true;
            restrictionViolationFrame = 60;
        }

        if (engine.statistics.time >= currentTimeTarget + getMaxDiff()) {
            if ((engine.statistics.time - (currentTimeTarget % 60)) % 60 == 0) {
                addTimeReduceQueue(PENALTY_MULTIPLIER[difficulty]);
            }
        }

        if (currentTimeTarget > 0 && diff <= getMaxDiff() && diff > -getMaxDiff() && (diff % 60 == 0))
            engine.playSE("countdown");

        if (restriction == RESTRICTION_NO_NEXT_VIEW) {
            engine.isNextVisible = false;
            engine.isHoldVisible = false;
        } else {
            engine.isNextVisible = true;
            engine.isHoldVisible = true;
        }

        if (restrictionViolationFrame > 0) --restrictionViolationFrame;
        if (bonusFrame > 0) --bonusFrame;

        if (timeIncreaseQueue > 0) ++timeIncreaseDelay;
        else timeIncreaseDelay = 0;

        if (timeReduceQueue > 0) ++timeReduceDelay;
        else timeReduceDelay = 0;

        if (timeIncreaseDelay >= TIME_QUEUE_TICK) {
            timeIncreaseDelay = 0;
            ++spareTime;
            --timeIncreaseQueue;
            engine.playSE("timeincrease");
        }

        if (timeReduceDelay >= TIME_QUEUE_TICK) {
            timeReduceDelay = 0;
            --spareTime;
            --timeReduceQueue;
            if (timeIncreaseQueue <= 0) engine.playSE("timereduce");
        }

        if (timeIncreaseQueue <= 0 && spareTime == 0 && engine.timerActive) {
            engine.stat = GameEngine.STAT_GAMEOVER;
            engine.resetStatc();
            engine.gameEnded();

            timeIncreaseQueue = 0;
            timeReduceQueue = 0;
        }

        if (changeFrame > 0) --changeFrame;
        else {
            if (lastChange != 0 && timeIncreaseQueue <= 0 && timeReduceQueue <= 0) lastChange = 0;
        }

        if ((engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (!owner.replayMode)) || engine.stat == GameEngine.STAT_CUSTOM) {
            // Show rank
            if (engine.ctrl.isPush(Controller.BUTTON_F) && playerProperties.isLoggedIn() && engine.stat != GameEngine.STAT_CUSTOM) {
                showPlayerStats = !showPlayerStats;
                engine.playSE("change");
            }
        }

        if (engine.quitflag) {
            playerProperties = new ProfileProperties(HEADER);
        }

        scgettime++;
    }

    /*
     * Render score
     */
    @Override
    public void renderLast(GameEngine engine, int playerID) {
        if (owner.menuOnly) return;

        receiver.drawScoreFont(engine, playerID, 0, 0, getName() + (unfair ? " (UNFAIR)" : ""), EventReceiver.COLOR_YELLOW);
        receiver.drawScoreFont(engine, playerID, 0, 1, "(" + DIFFICULTY_NAMES[difficulty] + " GAME)", difficulty == DIFFICULTY_VERY_HARD ? EventReceiver.COLOR_RED : EventReceiver.COLOR_YELLOW);

        if ((engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (!owner.replayMode))) {
            if ((!owner.replayMode) && (!big) && (engine.ai == null)) {
                if (showPlayerStats) {
                    float scale = (receiver.getNextDisplayType() == 2) ? 0.5f : 1.0f;
                    int topY = (receiver.getNextDisplayType() == 2) ? 6 : 4;
                    receiver.drawScoreFont(engine, playerID, 3, topY - 1, "SCORE  LINE TIME", EventReceiver.COLOR_BLUE, scale);

                    for (int i = 0; i < RANKING_MAX; i++) {
                        receiver.drawScoreFont(engine, playerID, 0, topY + i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW, scale);
                        receiver.drawScoreFont(engine, playerID, 3, topY + i, String.valueOf(rankingScorePlayer[difficulty][i]), (i == rankingRankPlayer), scale);
                        receiver.drawScoreFont(engine, playerID, 10, topY + i, String.valueOf(rankingLinesPlayer[difficulty][i]), (i == rankingRankPlayer), scale);
                        receiver.drawScoreFont(engine, playerID, 15, topY + i, GeneralUtil.getTime(rankingTimePlayer[difficulty][i]), (i == rankingRankPlayer), scale);
                    }

                    receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 1, "PLAYER SCORES", EventReceiver.COLOR_BLUE);
                    receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 2, playerProperties.getNameDisplay(), EventReceiver.COLOR_WHITE, 2f);

                    receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 5, "F:SWITCH RANK SCREEN", EventReceiver.COLOR_GREEN);
                } else {
                    float scale = (receiver.getNextDisplayType() == 2) ? 0.5f : 1.0f;
                    int topY = (receiver.getNextDisplayType() == 2) ? 6 : 4;
                    receiver.drawScoreFont(engine, playerID, 3, topY - 1, "SCORE  LINE TIME", EventReceiver.COLOR_BLUE, scale);

                    for (int i = 0; i < RANKING_MAX; i++) {
                        receiver.drawScoreFont(engine, playerID, 0, topY + i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW, scale);
                        receiver.drawScoreFont(engine, playerID, 3, topY + i, String.valueOf(rankingScore[difficulty][i]), (i == rankingRank), scale);
                        receiver.drawScoreFont(engine, playerID, 10, topY + i, String.valueOf(rankingLines[difficulty][i]), (i == rankingRank), scale);
                        receiver.drawScoreFont(engine, playerID, 15, topY + i, GeneralUtil.getTime(rankingTime[difficulty][i]), (i == rankingRank), scale);
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
            if (restrictionViolationFrame > 0 && bonusFrame <= 0) {
                int xx = receiver.getFieldDisplayPositionX(engine, playerID) + 4 + (engine.field.getWidth() / 2) * 16;
                int yy = receiver.getFieldDisplayPositionY(engine, playerID) + 52;
                GameTextUtilities.drawDirectTextAlign(engine, xx, yy + 8, GameTextUtilities.ALIGN_TOP_MIDDLE, "RESTRICTION", EventReceiver.COLOR_RED, 1f);
                GameTextUtilities.drawDirectTextAlign(engine, xx, yy + 24, GameTextUtilities.ALIGN_TOP_MIDDLE, "VIOLATION", EventReceiver.COLOR_RED, 1f);
            }

            if (bonusFrame > 0) {
                int xx = receiver.getFieldDisplayPositionX(engine, playerID) + 4 + (engine.field.getWidth() / 2) * 16;
                int yy = receiver.getFieldDisplayPositionY(engine, playerID) + 52;
                GameTextUtilities.drawDirectTextAlign(engine, xx, yy + 8, GameTextUtilities.ALIGN_TOP_MIDDLE, "TARGET", EventReceiver.COLOR_GREEN, 1f);
                GameTextUtilities.drawDirectTextAlign(engine, xx, yy + 24, GameTextUtilities.ALIGN_TOP_MIDDLE, "BONUS", EventReceiver.COLOR_GREEN, 1f);
            }

            int baseX = receiver.getFieldDisplayPositionX(engine, playerID) + 4;
            int baseY = receiver.getFieldDisplayPositionY(engine, playerID) + 52;
            if (pCoordList.size() > 0 && cPiece != null && hardDropEffect) {
                for (int[] loc : pCoordList) {
                    int cx = baseX + (16 * loc[0]);
                    int cy = baseY + (16 * loc[1]);
                    rendererExtension.drawScaledPiece(receiver, engine, playerID, cx, cy, cPiece, 1f, 0f);
                }
            }

            receiver.drawScoreFont(engine, playerID, 0, 3, "SPARE TIME", EventReceiver.COLOR_BLUE);
            String timeVal = (((spareTime == 0 && engine.stat == GameEngine.STAT_GAMEOVER) || engine.stat == GameEngine.STAT_RESULT) ? engine.statistics.score : spareTime) + " SECONDS ";
            if (timeReduceQueue > 0 || timeIncreaseQueue > 0 || changeFrame > 0) {
                timeVal += "(";
                if (lastChange > 0) timeVal += "+";
                timeVal += (lastChange + ")");
            }
            receiver.drawScoreFont(engine, playerID, 0, 4, timeVal, timeReduceQueue > 0, EventReceiver.COLOR_WHITE, EventReceiver.COLOR_RED);

            receiver.drawScoreFont(engine, playerID, 0, 6, "LINE", EventReceiver.COLOR_BLUE);
            if ((engine.statistics.level >= 19) && (tableGameClearLines[goaltype] < 0))
                receiver.drawScoreFont(engine, playerID, 0, 7, engine.statistics.lines + "");
            else
                receiver.drawScoreFont(engine, playerID, 0, 7, engine.statistics.lines + "/" + (currentLineTarget));

            receiver.drawScoreFont(engine, playerID, 0, 9, "LEVEL", EventReceiver.COLOR_BLUE);
            receiver.drawScoreFont(engine, playerID, 0, 10, String.valueOf(engine.statistics.level + 1));

            receiver.drawScoreFont(engine, playerID, 0, 12, "TIME / TARGET", EventReceiver.COLOR_BLUE);
            int diff = engine.statistics.time - currentTimeTarget;
            receiver.drawScoreFont(engine, playerID, 0, 13, GeneralUtil.getTime(engine.statistics.time), engine.statistics.time > currentTimeTarget + getMaxDiff(), (diff >= -getMaxDiff() && diff < getMaxDiff() && honkTimer > 0) ? EventReceiver.COLOR_CYAN : EventReceiver.COLOR_WHITE, EventReceiver.COLOR_RED);
            receiver.drawScoreFont(engine, playerID, 0, 14, GeneralUtil.getTime(currentTimeTarget), EventReceiver.COLOR_GREEN);

            receiver.drawScoreFont(engine, playerID, 0, 16, "RESTRICTION", EventReceiver.COLOR_RED);
            if (restriction == RESTRICTION_NONE) {
                receiver.drawScoreFont(engine, playerID, 0, 17, "NONE", EventReceiver.COLOR_GREEN);
            } else if (restriction == RESTRICTION_NO_HARD_DROP) {
                receiver.drawScoreFont(engine, playerID, 0, 17, "NO HARD DROPS", EventReceiver.COLOR_YELLOW);
            } else if (restriction == RESTRICTION_NO_STALLING) {
                if (currentStallRestriction < 300) {
                    receiver.drawScoreFont(engine, playerID, 0, 17, "STALL LIMIT 2.5S", EventReceiver.COLOR_ORANGE);
                } else {
                    receiver.drawScoreFont(engine, playerID, 0, 17, "STALL LIMIT 5.0S", EventReceiver.COLOR_YELLOW);
                }
            } else if (restriction == RESTRICTION_NO_NEXT_VIEW) {
                receiver.drawScoreFont(engine, playerID, 0, 17, "INVISIBLE PREVIEWS", EventReceiver.COLOR_ORANGE);
            }

            if (playerProperties.isLoggedIn() || PLAYER_NAME.length() > 0) {
                receiver.drawScoreFont(engine, playerID, 0, 19, "PLAYER", EventReceiver.COLOR_BLUE);
                GameTextUtilities.drawAlignedScoreText(receiver, engine, playerID, false, 0, 20, GameTextUtilities.Text.ofBig(owner.replayMode ? PLAYER_NAME : playerProperties.getNameDisplay()));
            }

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
    }

    /*
     * Render results screen
     */
    @Override
    public void renderResult(GameEngine engine, int playerID) {
        receiver.drawMenuFont(engine, playerID, 0, 0, "TIME SCORE", EventReceiver.COLOR_BLUE);
        String strGrade = String.format("%10d", engine.statistics.score);
        receiver.drawMenuFont(engine, playerID, 0, 1, strGrade);
        drawResultStats(engine, playerID, receiver, 2, EventReceiver.COLOR_BLUE,
            STAT_LINES, STAT_LEVEL, STAT_TIME, STAT_LPM);
        drawResultRank(engine, playerID, receiver, 10, EventReceiver.COLOR_BLUE, rankingRank);
        drawResultNetRank(engine, playerID, receiver, 12, EventReceiver.COLOR_BLUE, netRankingRank[0]);
        drawResultNetRankDaily(engine, playerID, receiver, 14, EventReceiver.COLOR_BLUE, netRankingRank[1]);

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
     * Calculate score
     */
    @Override
    public void calcScore(GameEngine engine, int playerID, int lines) {
        // Line clear bonus
        int pts = 0;

        lastevent = EVENT_NONE;
        if (engine.tspin) {
            // T-Spin 0 lines
            if ((lines == 0) && (!engine.tspinez)) {
                if (engine.tspinmini) {
                    lastevent = EVENT_TSPIN_ZERO_MINI;
                } else {
                    lastevent = EVENT_TSPIN_ZERO;
                }
            }
            // Immobile EZ Spin
            else if (engine.tspinez && (lines > 0)) {
                lastevent = EVENT_TSPIN_EZ;
            }
            // T-Spin 1 line
            else if (lines == 1) {
                if (engine.tspinmini) {
                    lastevent = EVENT_TSPIN_SINGLE_MINI;
                } else {
                    lastevent = EVENT_TSPIN_SINGLE;
                }
            }
            // T-Spin 2 lines
            else if (lines == 2) {
                if (engine.tspinmini && engine.useAllSpinBonus) {
                    pts = 1;
                    lastevent = EVENT_TSPIN_DOUBLE_MINI;
                } else {
                    pts = 1;
                    if (engine.b2b) pts = 2;
                    lastevent = EVENT_TSPIN_DOUBLE;
                }
            }
            // T-Spin 3 lines
            else if (lines >= 3) {
                pts = 4;
                if (engine.b2b) pts = 8;
                lastevent = EVENT_TSPIN_TRIPLE;
            }
        } else {
            if (lines == 1) {
                lastevent = EVENT_SINGLE;
            } else if (lines == 2) {
                lastevent = EVENT_DOUBLE;
            } else if (lines == 3) {
                lastevent = EVENT_TRIPLE;
            } else if (lines >= 4) {
                // 4 lines
                pts = 1;
                if (engine.b2b) pts = 2;
                lastevent = EVENT_FOUR;
            }
        }

        lastb2b = engine.b2b;

        // Combo
        if ((enableCombo) && (engine.combo >= 1) && (lines >= 1)) {
            pts += (engine.combo / 4);
            lastcombo = engine.combo;
        }

        // All clear
        if ((lines >= 1) && (engine.field.isEmpty())) {
            engine.playSE("bravo");
            pts += 10 * lines;
        }

        // Add to score
        if (pts > 0) {
            lastscore = pts;
            addTimeIncreaseQueue(pts);
        }

        if (lastevent != EVENT_NONE) {
            lastpiece = engine.nowPieceObject.id;
            scgettime = 0;
        }

        // BGM fade-out effects and BGM changes
        if (engine.statistics.lines >= ((bgmlv + 1) * GOAL_LINE_TOTALS[difficulty] / 6) - 5)
            owner.bgmStatus.fadesw = true;

        if ((engine.statistics.lines >= (bgmlv + 1) * GOAL_LINE_TOTALS[difficulty] / 6) &&
            ((engine.statistics.lines < GOAL_LINE_TOTALS[difficulty]) || (GOAL_LINE_TOTALS[difficulty] < 0))) {
            bgmlv++;
            owner.bgmStatus.bgm = bgmlv;
            owner.bgmStatus.fadesw = false;
        }

        if ((engine.statistics.lines >= GOAL_LINE_TOTALS[difficulty]) && (GOAL_LINE_TOTALS[difficulty] >= 0)) {
            // Ending
            int diff = engine.statistics.time - currentTimeTarget;
            int bonus = 0;
            if (diff >= -getMaxDiff() && diff < getMaxDiff()) {
                switch (difficulty) {
                    case DIFFICULTY_EASY:
                        bonus = 20;
                        break;
                    case DIFFICULTY_NORMAL:
                        bonus = 40;
                        break;
                    case DIFFICULTY_HARD:
                        bonus = 80;
                        break;
                    case DIFFICULTY_VERY_HARD:
                        bonus = 160;
                        break;
                    default:
                        break;
                }

                if (unfair) bonus *= 1.5;

                bonus += (unfair ? 1 : 3) + streak;
                bonusFrame = 120;
            }

            if (streak == engine.statistics.level)
                bonus += 20 * PENALTY_MULTIPLIER[difficulty] * ((unfair && difficulty != DIFFICULTY_VERY_HARD) ? 2 : 1);
            if (bonus != 0) addTimeIncreaseQueue(bonus);

            engine.ending = 1;
            engine.gameEnded();
        } else if ((engine.statistics.lines >= currentLineTarget) && (engine.statistics.level < GOAL_LEVELS[difficulty])) {
            // Level up
            engine.statistics.level++;

            owner.backgroundStatus.fadesw = true;
            owner.backgroundStatus.fadecount = 0;
            owner.backgroundStatus.fadebg = engine.statistics.level / 4;

            int diff = engine.statistics.time - currentTimeTarget;
            if (diff >= -getMaxDiff() && diff < getMaxDiff()) {
                int bonus = (honkTimer > 0 ? 3 : 0);
                if (honkTimer > 0) honkTimer = 0;
                addTimeIncreaseQueue((unfair ? 1 : 3) + streak + bonus);
                bonusFrame = 120;
                ++streak;
            } else {
                streak = 0;
            }

            if (diff <= -900) {
                restriction = RESTRICTION_NO_NEXT_VIEW;
            } else if (diff <= -450) {
                restriction = RESTRICTION_NO_HARD_DROP;
            } else if (diff >= 900) {
                restriction = RESTRICTION_NO_STALLING;
                currentStallRestriction = STALLING_LIMITS[0];
            } else if (diff >= 450) {
                restriction = RESTRICTION_NO_STALLING;
                currentStallRestriction = STALLING_LIMITS[1];
            } else {
                restriction = RESTRICTION_NONE;
            }

            setSpeed(engine);
            engine.playSE("levelup");
        }
    }

    /*
     * Soft drop
     */
    @Override
    public void afterSoftDropFall(GameEngine engine, int playerID, int fall) {
    }

    /*
     * Hard drop
     */
    @Override
    public void afterHardDropFall(GameEngine engine, int playerID, int fall) {
        if (restriction == RESTRICTION_NO_HARD_DROP && engine.stat == GameEngine.STAT_MOVE && fall > 0) {
            addTimeReduceQueue(2 * PENALTY_MULTIPLIER[difficulty]);
            restrictionViolationFrame = 60;
        }

        int baseX = (16 * engine.nowPieceX) + 4 + receiver.getFieldDisplayPositionX(engine, playerID);
        int baseY = (16 * engine.nowPieceY) + 52 + receiver.getFieldDisplayPositionY(engine, playerID);
        cPiece = new Piece(engine.nowPieceObject);
        for (int i = 1; i <= fall; i++) {
            pCoordList.add(
                new int[] { engine.nowPieceX, engine.nowPieceY - i }
            );
        }
        if (hardDropEffect) {
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
        if ((!owner.replayMode) && (!big) && (engine.ai == null)) {
            updateRanking(engine.statistics.score, engine.statistics.lines, engine.statistics.time, difficulty);

            if (playerProperties.isLoggedIn()) {
                prop.setProperty("constantris.playerName", playerProperties.getNameDisplay());
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
        tspinEnableType = prop.getProperty("constantris.tspinEnableType", 1);
        enableTSpin = prop.getProperty("constantris.enableTSpin", true);
        enableTSpinKick = prop.getProperty("constantris.enableTSpinKick", true);
        spinCheckType = prop.getProperty("constantris.spinCheckType", 0);
        tspinEnableEZ = prop.getProperty("constantris.tspinEnableEZ", false);
        enableB2B = prop.getProperty("constantris.enableB2B", true);
        enableCombo = prop.getProperty("constantris.enableCombo", true);
        goaltype = prop.getProperty("constantris.gametype", 0);
        big = prop.getProperty("constantris.big", false);
        version = prop.getProperty("constantris.version", 0);
        difficulty = prop.getProperty("constantris.difficulty", DIFFICULTY_EASY);
        unfair = prop.getProperty("constantris.unfair", false);
        hardDropEffect = prop.getProperty("constantris.hardDropEffect", true);
    }

    /**
     * Save settings to property file
     *
     * @param prop Property file
     */
    private void saveSetting(CustomProperties prop) {
        prop.setProperty("constantris.tspinEnableType", tspinEnableType);
        prop.setProperty("constantris.enableTSpin", enableTSpin);
        prop.setProperty("constantris.enableTSpinKick", enableTSpinKick);
        prop.setProperty("constantris.spinCheckType", spinCheckType);
        prop.setProperty("constantris.tspinEnableEZ", tspinEnableEZ);
        prop.setProperty("constantris.enableB2B", enableB2B);
        prop.setProperty("constantris.enableCombo", enableCombo);
        prop.setProperty("constantris.gametype", goaltype);
        prop.setProperty("constantris.big", big);
        prop.setProperty("constantris.version", version);
        prop.setProperty("constantris.difficulty", difficulty);
        prop.setProperty("constantris.unfair", unfair);
        prop.setProperty("constantris.hardDropEffect", hardDropEffect);
    }

    /**
     * Load settings from property file
     *
     * @param prop Property file
     */
    private void loadSettingPlayer(ProfileProperties prop) {
        tspinEnableType = prop.getProperty("constantris.tspinEnableType", 1);
        enableTSpin = prop.getProperty("constantris.enableTSpin", true);
        enableTSpinKick = prop.getProperty("constantris.enableTSpinKick", true);
        spinCheckType = prop.getProperty("constantris.spinCheckType", 0);
        tspinEnableEZ = prop.getProperty("constantris.tspinEnableEZ", false);
        enableB2B = prop.getProperty("constantris.enableB2B", true);
        enableCombo = prop.getProperty("constantris.enableCombo", true);
        goaltype = prop.getProperty("constantris.gametype", 0);
        big = prop.getProperty("constantris.big", false);
        difficulty = prop.getProperty("constantris.difficulty", DIFFICULTY_EASY);
        unfair = prop.getProperty("constantris.unfair", false);
        hardDropEffect = prop.getProperty("constantris.hardDropEffect", true);
    }

    /**
     * Save settings to property file
     *
     * @param prop Property file
     */
    private void saveSettingPlayer(ProfileProperties prop) {
        prop.setProperty("constantris.tspinEnableType", tspinEnableType);
        prop.setProperty("constantris.enableTSpin", enableTSpin);
        prop.setProperty("constantris.enableTSpinKick", enableTSpinKick);
        prop.setProperty("constantris.spinCheckType", spinCheckType);
        prop.setProperty("constantris.tspinEnableEZ", tspinEnableEZ);
        prop.setProperty("constantris.enableB2B", enableB2B);
        prop.setProperty("constantris.enableCombo", enableCombo);
        prop.setProperty("constantris.gametype", goaltype);
        prop.setProperty("constantris.big", big);
        prop.setProperty("constantris.difficulty", difficulty);
        prop.setProperty("constantris.unfair", unfair);
        prop.setProperty("constantris.hardDropEffect", hardDropEffect);
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
            for (int j = 0; j < DIFFICULTIES; j++) {
                rankingScore[j][i] = prop.getProperty("constantris.ranking." + CURRENT_VERSION + "." + ruleName + "." + j + ".score." + i, 0);
                rankingLines[j][i] = prop.getProperty("constantris.ranking." + CURRENT_VERSION + "." + ruleName + "." + j + ".lines." + i, 0);
                rankingTime[j][i] = prop.getProperty("constantris.ranking." + CURRENT_VERSION + "." + ruleName + "." + j + ".time." + i, 0);
            }
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
            for (int j = 0; j < DIFFICULTIES; j++) {
                prop.setProperty("constantris.ranking." + CURRENT_VERSION + "." + ruleName + "." + j + ".score." + i, rankingScore[j][i]);
                prop.setProperty("constantris.ranking." + CURRENT_VERSION + "." + ruleName + "." + j + ".lines." + i, rankingLines[j][i]);
                prop.setProperty("constantris.ranking." + CURRENT_VERSION + "." + ruleName + "." + j + ".time." + i, rankingTime[j][i]);
            }
        }
    }

    /**
     * Read rankings from property file
     *
     * @param prop     Property file
     * @param ruleName Rule name
     */
    protected void loadRankingPlayer(ProfileProperties prop, String ruleName) {
        for (int i = 0; i < RANKING_MAX; i++) {
            for (int j = 0; j < DIFFICULTIES; j++) {
                rankingScorePlayer[j][i] = prop.getProperty("constantris.ranking." + CURRENT_VERSION + "." + ruleName + "." + j + ".score." + i, 0);
                rankingLinesPlayer[j][i] = prop.getProperty("constantris.ranking." + CURRENT_VERSION + "." + ruleName + "." + j + ".lines." + i, 0);
                rankingTimePlayer[j][i] = prop.getProperty("constantris.ranking." + CURRENT_VERSION + "." + ruleName + "." + j + ".time." + i, 0);
            }
        }
    }

    /**
     * Save rankings to property file
     *
     * @param prop     Property file
     * @param ruleName Rule name
     */
    private void saveRankingPlayer(ProfileProperties prop, String ruleName) {
        for (int i = 0; i < RANKING_MAX; i++) {
            for (int j = 0; j < DIFFICULTIES; j++) {
                prop.setProperty("constantris.ranking." + CURRENT_VERSION + "." + ruleName + "." + j + ".score." + i, rankingScore[j][i]);
                prop.setProperty("constantris.ranking." + CURRENT_VERSION + "." + ruleName + "." + j + ".lines." + i, rankingLines[j][i]);
                prop.setProperty("constantris.ranking." + CURRENT_VERSION + "." + ruleName + "." + j + ".time." + i, rankingTime[j][i]);
            }
        }
    }

    /**
     * Update rankings
     *
     * @param sc   Score
     * @param li   Lines
     * @param time Time
     */
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

    /**
     * Calculate ranking position
     *
     * @param sc   Score
     * @param li   Lines
     * @param time Time
     * @return Position (-1 if unranked)
     */
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

    /**
     * Calculate ranking position
     *
     * @param sc   Score
     * @param li   Lines
     * @param time Time
     * @return Position (-1 if unranked)
     */
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

    // endregion

    // region Misc Block

    private int getMaxDiff() {
        switch (difficulty) {
            case DIFFICULTY_EASY:
                return 300;
            case DIFFICULTY_NORMAL:
                return 240;
            case DIFFICULTY_HARD:
                return 180;
            case DIFFICULTY_VERY_HARD:
                return 120;
            default:
                return 60;
        }
    }

    private int getTimeLimit(GameEngine engine, int nextLineGoal) {
        int timeLimit = (int) (60 * (unfair ? 20d : 17.5d));

        switch (difficulty) {
            case DIFFICULTY_EASY:
                timeLimit += 60 * 2;
                if (unfair) timeLimit -= localRandom.nextInt(2) * 60;
                break;
            case DIFFICULTY_NORMAL:
                if (unfair) timeLimit -= localRandom.nextInt(2) * 60;
                break;
            case DIFFICULTY_HARD:
                timeLimit -= 60 * 5;
                if (unfair) timeLimit -= localRandom.nextInt(2) * 60;
                break;
            case DIFFICULTY_VERY_HARD:
                timeLimit -= 60 * 7.5;
                if (!unfair) timeLimit += localRandom.nextInt(2) * 60;
                break;
            default:
                break;
        }

        double multiplier = (double) nextLineGoal / (unfair ? 10d : 8d);
        int t = (int) (timeLimit * multiplier);

        if (engine.statistics.level >= 20) {
            double extraMultiplier = 1 - ((unfair ? 0.25 : 0.18) * ((engine.statistics.level - 19d) / 60d));
            t *= extraMultiplier;
        }

        return t + (60 - (t % 60)) + (localRandom.nextInt(unfair ? 3 : 5) * 60);
    }

    // endregion
}
