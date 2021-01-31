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
import zeroxfc.nullpo.custom.libs.Interpolation;
import zeroxfc.nullpo.custom.libs.ProfileProperties;
import zeroxfc.nullpo.custom.libs.RendererExtension;
import zeroxfc.nullpo.custom.libs.ShakingText;

public class Deltatris extends MarathonModeBase {
    /**
     * Gravity denominator for G calculation
     */
    private static final int GRAVITY_DENOMINATOR = 256;

    /**
     * Starting delays
     */
    private static final int START_ARE = 25,
        START_LINE_ARE = 25,
        START_LINE_DELAY = 40,
        START_DAS = 14,
        START_LOCK_DELAY = 30,
        START_GRAVITY = 4;

    /**
     * Delays after reaching max piece
     */
    private static final int[] END_ARE = { 8, 6, 4 },
        END_LINE_ARE = { 5, 4, 3 },
        END_LINE_DELAY = { 9, 6, 3 },
        END_DAS = { 6, 6, 4 },
        END_LOCK_DELAY = { 17, 15, 8 };

    /**
     * Score multipliers
     */
    private static final double MULTIPLIER_MINIMUM = 1.0,
        MULTIPLIER_SINGLE = 0.05,
        MULTIPLIER_DOUBLE = 0.125,
        MULTIPLIER_TRIPLE = 0.25,
        MULTIPLIER_TETRIS = 0.5,
        MULTIPLIER_B2B = 1.5,
        MULTIPLIER_COMBO = 0.025,
        MULTIPLIER_SPIN = 5.0,
        MULTIPLIER_MINI_SPIN = 3.0,
        MULTIPLIER_MAXIMUM = 20.0;

    /**
     * Pieces until max
     * Use interpolation to get the delays and gravity.
     * <p>
     * -- Planned --
     * GRAVITY, ARE, LINE ARE: Linear
     * DAS, LOCK DELAY, LINE DELAY: Ease-in-ease-out
     */
    private static final int[] PIECES_MAX = { 1000, 800, 600 };

    private static final double[] GRAVITY_MULTIPLIERS = { 1.014412098, 1.018047461, 1.024135373 };
    /**
     * Difficulties
     */
    private static final int DIFFICULTIES = 3;
    private static final int DIFFICULTY_EASY = 0;
    private static final int DIFFICULTY_NORMAL = 1;
    private static final int DIFFICULTY_HARD = 2;
    private static final int headerColour = EventReceiver.COLOR_RED;
    private ShakingText stext;
    private int difficulty;
    private double multiplier = 1.0;
    private double grav;
    private float mScale = 1.0f;
    private int scorebefore;
    // GENERIC
    private int rankingRank;
    private int[][] rankingScore;
    private int[][] rankingTime;
    private int[][] rankingLines;
    // PROFILE
    private ProfileProperties playerProperties = null;
    private boolean showPlayerStats;
    private int rankingRankPlayer;
    private int[][] rankingScorePlayer;
    private int[][] rankingTimePlayer;
    private int[][] rankingLinesPlayer;
    private String PLAYER_NAME;

    /**
     * The good hard drop effect
     */
    private ArrayList<int[]> pCoordList;
    private Piece cPiece;

    /**
     * Deltatris - How fast can you go in this ΔMAX-inspired gamemode?
     *
     * @return Mode name
     */
    @Override
    public String getName() {
        return "DELTATRIS";
    }

    // help me i forgot how to make modes

    /*
     * Initialization
     */
    @Override
    public void playerInit(GameEngine engine, int playerID) {
        owner = engine.owner;
        receiver = engine.owner.receiver;
        lastscore = 0;
        scgettime = 0;
        lastevent = EVENT_NONE;
        lastb2b = false;
        lastcombo = 0;
        lastpiece = 0;
        bgmlv = 0;
        multiplier = 1.0;
        mScale = 1.0f;
        grav = START_GRAVITY;
        scorebefore = 0;

        difficulty = 1;

        rankingRank = -1;
        rankingScore = new int[RANKING_TYPE][RANKING_MAX];
        rankingLines = new int[RANKING_TYPE][RANKING_MAX];
        rankingTime = new int[RANKING_TYPE][RANKING_MAX];

        while (playerProperties == null) {
            playerProperties = new ProfileProperties(headerColour);

            showPlayerStats = false;
        }

        rankingRankPlayer = -1;
        rankingScorePlayer = new int[RANKING_TYPE][RANKING_MAX];
        rankingLinesPlayer = new int[RANKING_TYPE][RANKING_MAX];
        rankingTimePlayer = new int[RANKING_TYPE][RANKING_MAX];

        pCoordList = new ArrayList<>();
        cPiece = null;

        netPlayerInit(engine, playerID);

        if (!owner.replayMode) {
            loadSetting(owner.modeConfig);
            loadRanking(owner.modeConfig, engine.ruleopt.strRuleName);

            if (playerProperties.isLoggedIn()) {
                loadSettingPlayer(playerProperties);
                loadRankingPlayer(playerProperties, engine.ruleopt.strRuleName);
            }

            version = CURRENT_VERSION;
            PLAYER_NAME = null;
        } else {
            loadSetting(owner.replayProp);
            if ((version == 0) && (owner.replayProp.getProperty("deltatris.endless", false))) goaltype = 2;

            PLAYER_NAME = owner.replayProp.getProperty("deltatris.playerName", "");

            // NET: Load name
            netPlayerName = engine.owner.replayProp.getProperty(playerID + ".net.netPlayerName", "");
        }

        engine.owner.backgroundStatus.bg = 0;
        engine.framecolor = GameEngine.FRAME_COLOR_GRAY;
    }

    /**
     * Set the overall game speed
     *
     * @param engine GameEngine
     */
    public void setSpeed(GameEngine engine) {
        double percentage = engine.statistics.totalPieceLocked / (double) PIECES_MAX[difficulty];
        if (percentage > 1d) percentage = 1d;

        if (engine.speed.gravity < GRAVITY_DENOMINATOR * 20) {
            grav *= GRAVITY_MULTIPLIERS[difficulty];
            grav = Math.min(grav, GRAVITY_DENOMINATOR * 20);
        }
        engine.speed.gravity = (int) grav;
        engine.speed.are = (int) Math.ceil(Interpolation.lerp((double) START_ARE, END_ARE[difficulty], percentage));
        engine.speed.areLine = (int) Math.ceil(Interpolation.lerp((double) START_LINE_ARE, END_LINE_ARE[difficulty], percentage));
        engine.speed.lineDelay = (int) Math.ceil(Interpolation.smoothStep(START_LINE_DELAY, END_LINE_DELAY[difficulty], percentage));
        engine.speed.das = (int) Math.floor(Interpolation.smoothStep(START_DAS, END_DAS[difficulty], percentage));
        engine.speed.lockDelay = (int) Math.ceil(Interpolation.smoothStep(START_LOCK_DELAY, END_LOCK_DELAY[difficulty], percentage));
        engine.speed.denominator = GRAVITY_DENOMINATOR;
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
            int change = updateCursor(engine, 7, playerID);

            if (change != 0) {
                engine.playSE("change");

                switch (engine.statc[2]) {
                    case 0:
                        difficulty += change;
                        if (difficulty < 0) difficulty = DIFFICULTIES - 1;
                        if (difficulty >= DIFFICULTIES) difficulty = 0;
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

    private String getDifficultyName() {
        switch (difficulty) {
            case DIFFICULTY_EASY:
                return "EASY";
            case DIFFICULTY_NORMAL:
                return "NORMAL";
            case DIFFICULTY_HARD:
                return "HARD";
            default:
                return "N/A";
        }
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
                "DIFFICULTY", getDifficultyName(),
                "SPIN BONUS", strTSpinEnable,
                "EZ SPIN", GeneralUtil.getONorOFF(enableTSpinKick),
                "SPIN TYPE", (spinCheckType == 0) ? "4POINT" : "IMMOBILE",
                "EZIMMOBILE", GeneralUtil.getONorOFF(tspinEnableEZ),
                "B2B", GeneralUtil.getONorOFF(enableB2B),
                "COMBO", GeneralUtil.getONorOFF(enableCombo),
                "BIG", GeneralUtil.getONorOFF(big));
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

    @Override
    public void onFirst(GameEngine engine, int playerID) {
        pCoordList.clear();
        cPiece = null;
    }

    /*
     * Called for initialization during "Ready" screen
     */
    @Override
    public void startGame(GameEngine engine, int playerID) {
        engine.statistics.level = 0;
        engine.statistics.levelDispAdd = 1;
        engine.b2bEnable = enableB2B;
        scorebefore = 0;

        stext = new ShakingText(new Random(engine.randSeed));

        multiplier = MULTIPLIER_MINIMUM;

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

        setSpeed(engine);
        grav = START_GRAVITY;

        if (netIsWatch) {
            owner.bgmStatus.bgm = BGMStatus.BGM_NOTHING;
        }
    }

    @Override
    public boolean onMove(GameEngine engine, int playerID) {
        if (engine.statc[0] > engine.speed.lockDelay * 3) {
            multiplier = Math.max(1, multiplier * 0.99225);
        }
        return false;
    }

    /*
     * Render score
     */
    @Override
    public void renderLast(GameEngine engine, int playerID) {
        if (owner.menuOnly) return;

        receiver.drawScoreFont(engine, playerID, 0, 0, getName(), EventReceiver.COLOR_RED);
        receiver.drawScoreFont(engine, playerID, 0, 1, "(" + getDifficultyName() + " DIFFICULTY)", EventReceiver.COLOR_RED);

        if ((engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (!owner.replayMode))) {
            if ((!owner.replayMode) && (!big) && (engine.ai == null)) {
                float scale = (receiver.getNextDisplayType() == 2) ? 0.5f : 1.0f;
                int topY = (receiver.getNextDisplayType() == 2) ? 6 : 4;
                receiver.drawScoreFont(engine, playerID, 3, topY - 1, "SCORE  LINE TIME", EventReceiver.COLOR_BLUE, scale);

                if (showPlayerStats) {
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
            int baseX = receiver.getFieldDisplayPositionX(engine, playerID) + 4;
            int baseY = receiver.getFieldDisplayPositionY(engine, playerID) + 52;
            if (pCoordList.size() > 0 && cPiece != null) {
                for (int[] loc : pCoordList) {
                    int cx = baseX + (16 * loc[0]);
                    int cy = baseY + (16 * loc[1]);
                    RendererExtension.drawScaledPiece(receiver, engine, playerID, cx, cy, cPiece, 1f, 0f);
                }
            }

            int s = engine.statistics.score;
            if (scgettime < 120)
                s = (int) Interpolation.sineStep(scorebefore, engine.statistics.score, (double) scgettime / 120);

            receiver.drawScoreFont(engine, playerID, 0, 3, "SCORE", EventReceiver.COLOR_BLUE);
            String strScore;
            if ((lastscore == 0) || (scgettime >= 120)) {
                strScore = String.valueOf(s);
            } else {
                strScore = s + "(+" + lastscore + ")";
            }
            receiver.drawScoreFont(engine, playerID, 0, 4, strScore);

            int rix, riy;
            rix = receiver.getScoreDisplayPositionX(engine, playerID);
            riy = receiver.getScoreDisplayPositionY(engine, playerID) + 13 * 16;

            GameTextUtilities.drawDirectTextAlign(receiver, engine, playerID, rix, riy,
                GameTextUtilities.ALIGN_TOP_LEFT,
                String.format("%.2f", multiplier) + "X",
                (engine.stat == GameEngine.STAT_MOVE && engine.statc[0] > engine.speed.lockDelay * 3) ? EventReceiver.COLOR_RED : (mScale > 1 ? EventReceiver.COLOR_ORANGE : EventReceiver.COLOR_WHITE),
                mScale);

            receiver.drawScoreFont(engine, playerID, 0, 6, "LINE", EventReceiver.COLOR_BLUE);
            receiver.drawScoreFont(engine, playerID, 0, 7, engine.statistics.lines + "");

            receiver.drawScoreFont(engine, playerID, 0, 9, "TIME", EventReceiver.COLOR_BLUE);
            receiver.drawScoreFont(engine, playerID, 0, 10, GeneralUtil.getTime(engine.statistics.time));

            receiver.drawScoreFont(engine, playerID, 0, 12, "MULTIPLIER", EventReceiver.COLOR_GREEN);


            receiver.drawScoreFont(engine, playerID, 0, 17, "SPEED", EventReceiver.COLOR_RED);

            if ((playerProperties != null && playerProperties.isLoggedIn()) || (PLAYER_NAME != null && PLAYER_NAME.length() > 0)) {
                receiver.drawScoreFont(engine, playerID, 8, 17, "PLAYER", EventReceiver.COLOR_BLUE);
                receiver.drawScoreFont(engine, playerID, 8, 18, owner.replayMode ? PLAYER_NAME : playerProperties.getNameDisplay(), EventReceiver.COLOR_WHITE, 2f);
            }

            int ix, iy;
            ix = receiver.getScoreDisplayPositionX(engine, playerID);
            iy = receiver.getScoreDisplayPositionY(engine, playerID) + 18 * 16 + 4;
            RendererExtension.drawAlignedSpeedMeter(receiver, ix, iy,
                RendererExtension.ALIGN_TOP_LEFT,
                engine.statistics.totalPieceLocked < PIECES_MAX[difficulty] ? (float) Math.min(1, engine.statistics.totalPieceLocked / (double) PIECES_MAX[difficulty]) : (engine.statistics.time / 12 % 2 == 0 ? 1f : 0f),
                2f);

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

            if (engine.statistics.totalPieceLocked >= PIECES_MAX[difficulty]) {
                stext.drawScoreText(receiver, engine, playerID, 0, 20, 4, 2, "MAXIMUM VELOCITY", (engine.statistics.time / 3 % 3 == 0) ? EventReceiver.COLOR_ORANGE : EventReceiver.COLOR_YELLOW, 1.25f);
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
     * Called after every frame
     */
    @Override
    public void onLast(GameEngine engine, int playerID) {
        scgettime++;
        mScale = Math.max(1, mScale * 0.98f);

        // Meter
        engine.meterValue = (int) ((multiplier / 20d) * receiver.getMeterMax(engine));
        engine.meterColor = GameEngine.METER_COLOR_GREEN;
        if (multiplier < 15) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
        if (multiplier < 10) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
        if (multiplier < 5) engine.meterColor = GameEngine.METER_COLOR_RED;

        if ((engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (!owner.replayMode)) || engine.stat == GameEngine.STAT_CUSTOM) {
            // Show rank
            if (engine.ctrl.isPush(Controller.BUTTON_F) && playerProperties.isLoggedIn() && engine.stat != GameEngine.STAT_CUSTOM) {
                showPlayerStats = !showPlayerStats;
                engine.playSE("change");
            }
        }
    }

    /*
     * Calculate score
     */
    @Override
    public void calcScore(GameEngine engine, int playerID, int lines) {
        // Line clear bonus
        int pts = 0;

        if (engine.tspin) {
            // T-Spin 0 lines
            if ((lines == 0) && (!engine.tspinez)) {
                if (engine.tspinmini) {
                    pts += 100 * (engine.statistics.level + 1) * multiplier;
                    lastevent = EVENT_TSPIN_ZERO_MINI;
                } else {
                    pts += 400 * (engine.statistics.level + 1) * multiplier;
                    lastevent = EVENT_TSPIN_ZERO;
                }
            }
            // Immobile EZ Spin
            else if (engine.tspinez && (lines > 0)) {
                if (engine.b2b) {
                    pts += 180 * (engine.statistics.level + 1) * multiplier;

                    multiplier += MULTIPLIER_SINGLE * MULTIPLIER_MINI_SPIN * MULTIPLIER_B2B;
                } else {
                    pts += 120 * (engine.statistics.level + 1) * multiplier;

                    multiplier += MULTIPLIER_SINGLE * MULTIPLIER_MINI_SPIN;
                }
                lastevent = EVENT_TSPIN_EZ;
            }
            // T-Spin 1 line
            else if (lines == 1) {
                if (engine.tspinmini) {
                    if (engine.b2b) {
                        pts += 300 * (engine.statistics.level + 1);

                        multiplier += MULTIPLIER_SINGLE * MULTIPLIER_MINI_SPIN * MULTIPLIER_B2B;
                    } else {
                        pts += 200 * (engine.statistics.level + 1);

                        multiplier += MULTIPLIER_SINGLE * MULTIPLIER_MINI_SPIN;
                    }
                    lastevent = EVENT_TSPIN_SINGLE_MINI;
                } else {
                    if (engine.b2b) {
                        pts += 1200 * (engine.statistics.level + 1);

                        multiplier += MULTIPLIER_SINGLE * MULTIPLIER_SPIN * MULTIPLIER_B2B;
                    } else {
                        pts += 800 * (engine.statistics.level + 1);

                        multiplier += MULTIPLIER_SINGLE * MULTIPLIER_SPIN;
                    }
                    lastevent = EVENT_TSPIN_SINGLE;
                }
            }
            // T-Spin 2 lines
            else if (lines == 2) {
                if (engine.tspinmini && engine.useAllSpinBonus) {
                    if (engine.b2b) {
                        pts += 600 * (engine.statistics.level + 1);

                        multiplier += MULTIPLIER_DOUBLE * MULTIPLIER_MINI_SPIN * MULTIPLIER_B2B;
                    } else {
                        pts += 400 * (engine.statistics.level + 1);

                        multiplier += MULTIPLIER_DOUBLE * MULTIPLIER_MINI_SPIN;
                    }
                    lastevent = EVENT_TSPIN_DOUBLE_MINI;
                } else {
                    if (engine.b2b) {
                        pts += 1800 * (engine.statistics.level + 1);

                        multiplier += MULTIPLIER_DOUBLE * MULTIPLIER_SPIN * MULTIPLIER_B2B;
                    } else {
                        pts += 1200 * (engine.statistics.level + 1);

                        multiplier += MULTIPLIER_DOUBLE * MULTIPLIER_SPIN;
                    }
                    lastevent = EVENT_TSPIN_DOUBLE;
                }
            }
            // T-Spin 3 lines
            else if (lines >= 3) {
                if (engine.b2b) {
                    pts += 2400 * (engine.statistics.level + 1);

                    multiplier += MULTIPLIER_TRIPLE * MULTIPLIER_SPIN * MULTIPLIER_B2B;
                } else {
                    pts += 1600 * (engine.statistics.level + 1);

                    multiplier += MULTIPLIER_TRIPLE * MULTIPLIER_SPIN;
                }
                lastevent = EVENT_TSPIN_TRIPLE;
            }

            if (lines > 0) mScale += (1f / 3f) * lines;
        } else {
            if (lines == 1) {
                pts += 100 * (engine.statistics.level + 1); // 1列
                lastevent = EVENT_SINGLE;

                multiplier += MULTIPLIER_SINGLE;
            } else if (lines == 2) {
                pts += 300 * (engine.statistics.level + 1); // 2列
                lastevent = EVENT_DOUBLE;

                multiplier += MULTIPLIER_DOUBLE;
            } else if (lines == 3) {
                pts += 500 * (engine.statistics.level + 1); // 3列
                lastevent = EVENT_TRIPLE;

                multiplier += MULTIPLIER_TRIPLE;
            } else if (lines >= 4) {
                // 4 lines
                if (engine.b2b) {
                    pts += 1200 * (engine.statistics.level + 1);

                    multiplier += MULTIPLIER_TETRIS * MULTIPLIER_B2B;
                } else {
                    pts += 800 * (engine.statistics.level + 1);

                    multiplier += MULTIPLIER_TETRIS;
                }
                lastevent = EVENT_FOUR;
            }

            if (lines > 0) mScale += 0.25f * lines;
        }

        lastb2b = engine.b2b;

        // Combo
        if ((enableCombo) && (engine.combo >= 1) && (lines >= 1)) {
            pts += ((engine.combo - 1) * 50) * (engine.statistics.level + 1);
            lastcombo = engine.combo;

            multiplier += engine.combo * MULTIPLIER_COMBO;
            mScale += engine.combo * 0.1;
        }

        // All clear
        if ((lines >= 1) && (engine.field.isEmpty())) {
            engine.playSE("bravo");
            pts += 1800 * (engine.statistics.level + 1);

            multiplier += 2.0;
            mScale += 0.5;
        }

        multiplier = Math.min(MULTIPLIER_MAXIMUM, multiplier);
        pts *= multiplier;

        // Add to score
        if (pts > 0) {
            scorebefore = engine.statistics.score;

            lastscore = pts;
            lastpiece = engine.nowPieceObject.id;
            scgettime = 0;
            if (lines >= 1) engine.statistics.scoreFromLineClear += pts;
            else engine.statistics.scoreFromOtherBonus += pts;
            engine.statistics.score += pts;
        }

        // BGM fade-out effects and BGM changes
        int pieces = Math.min(PIECES_MAX[difficulty] + (PIECES_MAX[difficulty] / 20), engine.statistics.totalPieceLocked);
        int lastLevel = engine.statistics.level;

//		if ((pieces - (PIECES_MAX[difficulty] / 20)) % (PIECES_MAX[difficulty] / 5) >= ((PIECES_MAX[difficulty] / 5) - 10) && engine.statistics.totalPieceLocked - (PIECES_MAX[difficulty] / 20) <= PIECES_MAX[difficulty]) {
//			owner.bgmStatus.fadesw = true;
//		} else if ((0 == pieces % (PIECES_MAX[difficulty] / 5)) && engine.statistics.totalPieceLocked - (PIECES_MAX[difficulty] / 20) <= PIECES_MAX[difficulty] && (pieces - (PIECES_MAX[difficulty] / 20)) > 0) {
//			bgmlv++;
//			owner.bgmStatus.bgm = bgmlv;
//			owner.bgmStatus.fadesw = false;
//		}

        // Level up
        engine.statistics.level = Math.min(19, pieces / (PIECES_MAX[difficulty] / 20));
        double levelDec = (double) pieces / ((double) PIECES_MAX[difficulty] / 20d);

        if ((levelDec - (int) levelDec) >= 0.8 && pieces < PIECES_MAX[difficulty]) {
            if (
                engine.statistics.level == 3 ||
                    engine.statistics.level == 7 ||
                    engine.statistics.level == 11 ||
                    engine.statistics.level == 15 ||
                    engine.statistics.level == 19
            ) owner.bgmStatus.fadesw = true;
        }

        if (engine.statistics.level > lastLevel) {
            owner.backgroundStatus.fadesw = true;
            owner.backgroundStatus.fadecount = 0;
            owner.backgroundStatus.fadebg = engine.statistics.level;

            if (
                engine.statistics.level == 4 ||
                    engine.statistics.level == 8 ||
                    engine.statistics.level == 12 ||
                    engine.statistics.level == 16
            ) {
                bgmlv++;
                owner.bgmStatus.bgm = bgmlv;
                owner.bgmStatus.fadesw = false;
            }

            engine.playSE("levelup");
        }

        if (engine.statistics.totalPieceLocked == PIECES_MAX[difficulty]) {
            engine.playSE("hurryup");

            bgmlv++;
            owner.bgmStatus.bgm = bgmlv;
            owner.bgmStatus.fadesw = false;
        }

        setSpeed(engine);
    }

    /*
     * Soft drop
     */
    @Override
    public void afterSoftDropFall(GameEngine engine, int playerID, int fall) {
        engine.statistics.scoreFromSoftDrop += fall * multiplier;
        engine.statistics.score += fall * multiplier;
    }

    /*
     * Hard drop
     */
    @Override
    public void afterHardDropFall(GameEngine engine, int playerID, int fall) {
        engine.statistics.scoreFromHardDrop += fall * 2 * multiplier;
        engine.statistics.score += fall * 2 * multiplier;

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
     * Render results screen
     */
    @Override
    public void renderResult(GameEngine engine, int playerID) {
        drawResultStats(engine, playerID, receiver, 0, EventReceiver.COLOR_BLUE,
            STAT_SCORE, STAT_LINES, STAT_TIME, STAT_SPL, STAT_LPM);
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
                prop.setProperty("deltatris.playerName", playerProperties.getNameDisplay());
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
        tspinEnableType = prop.getProperty("deltatris.tspinEnableType", 1);
        enableTSpin = prop.getProperty("deltatris.enableTSpin", true);
        enableTSpinKick = prop.getProperty("deltatris.enableTSpinKick", true);
        spinCheckType = prop.getProperty("deltatris.spinCheckType", 0);
        tspinEnableEZ = prop.getProperty("deltatris.tspinEnableEZ", false);
        enableB2B = prop.getProperty("deltatris.enableB2B", true);
        enableCombo = prop.getProperty("deltatris.enableCombo", true);
        difficulty = prop.getProperty("deltatris.difficulty", 1);
        big = prop.getProperty("deltatris.big", false);
        version = prop.getProperty("deltatris.version", 0);
    }

    /**
     * Save settings to property file
     *
     * @param prop Property file
     */
    private void saveSetting(CustomProperties prop) {
        prop.setProperty("deltatris.tspinEnableType", tspinEnableType);
        prop.setProperty("deltatris.enableTSpin", enableTSpin);
        prop.setProperty("deltatris.enableTSpinKick", enableTSpinKick);
        prop.setProperty("deltatris.spinCheckType", spinCheckType);
        prop.setProperty("deltatris.tspinEnableEZ", tspinEnableEZ);
        prop.setProperty("deltatris.enableB2B", enableB2B);
        prop.setProperty("deltatris.enableCombo", enableCombo);
        prop.setProperty("deltatris.difficulty", difficulty);
        prop.setProperty("deltatris.big", big);
        prop.setProperty("deltatris.version", version);
    }

    /**
     * Load settings from property file
     *
     * @param prop Property file
     */
    private void loadSettingPlayer(ProfileProperties prop) {
        tspinEnableType = prop.getProperty("deltatris.tspinEnableType", 1);
        enableTSpin = prop.getProperty("deltatris.enableTSpin", true);
        enableTSpinKick = prop.getProperty("deltatris.enableTSpinKick", true);
        spinCheckType = prop.getProperty("deltatris.spinCheckType", 0);
        tspinEnableEZ = prop.getProperty("deltatris.tspinEnableEZ", false);
        enableB2B = prop.getProperty("deltatris.enableB2B", true);
        enableCombo = prop.getProperty("deltatris.enableCombo", true);
        difficulty = prop.getProperty("deltatris.difficulty", 1);
        big = prop.getProperty("deltatris.big", false);
    }

    /**
     * Save settings to property file
     *
     * @param prop Property file
     */
    private void saveSettingPlayer(ProfileProperties prop) {
        prop.setProperty("deltatris.tspinEnableType", tspinEnableType);
        prop.setProperty("deltatris.enableTSpin", enableTSpin);
        prop.setProperty("deltatris.enableTSpinKick", enableTSpinKick);
        prop.setProperty("deltatris.spinCheckType", spinCheckType);
        prop.setProperty("deltatris.tspinEnableEZ", tspinEnableEZ);
        prop.setProperty("deltatris.enableB2B", enableB2B);
        prop.setProperty("deltatris.enableCombo", enableCombo);
        prop.setProperty("deltatris.difficulty", difficulty);
        prop.setProperty("deltatris.big", big);
        prop.setProperty("deltatris.version", version);
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
            for (int j = 0; j < GAMETYPE_MAX; j++) {
                rankingScore[j][i] = prop.getProperty("deltatris.ranking." + ruleName + "." + j + ".score." + i, 0);
                rankingLines[j][i] = prop.getProperty("deltatris.ranking." + ruleName + "." + j + ".lines." + i, 0);
                rankingTime[j][i] = prop.getProperty("deltatris.ranking." + ruleName + "." + j + ".time." + i, 0);
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
            for (int j = 0; j < GAMETYPE_MAX; j++) {
                prop.setProperty("deltatris.ranking." + ruleName + "." + j + ".score." + i, rankingScore[j][i]);
                prop.setProperty("deltatris.ranking." + ruleName + "." + j + ".lines." + i, rankingLines[j][i]);
                prop.setProperty("deltatris.ranking." + ruleName + "." + j + ".time." + i, rankingTime[j][i]);
            }
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
            for (int j = 0; j < GAMETYPE_MAX; j++) {
                rankingScorePlayer[j][i] = prop.getProperty("deltatris.ranking." + ruleName + "." + j + ".score." + i, 0);
                rankingLinesPlayer[j][i] = prop.getProperty("deltatris.ranking." + ruleName + "." + j + ".lines." + i, 0);
                rankingTimePlayer[j][i] = prop.getProperty("deltatris.ranking." + ruleName + "." + j + ".time." + i, 0);
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
        if (!prop.isLoggedIn()) return;
        for (int i = 0; i < RANKING_MAX; i++) {
            for (int j = 0; j < GAMETYPE_MAX; j++) {
                prop.setProperty("deltatris.ranking." + ruleName + "." + j + ".score." + i, rankingScorePlayer[j][i]);
                prop.setProperty("deltatris.ranking." + ruleName + "." + j + ".lines." + i, rankingLinesPlayer[j][i]);
                prop.setProperty("deltatris.ranking." + ruleName + "." + j + ".time." + i, rankingTimePlayer[j][i]);
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
}
