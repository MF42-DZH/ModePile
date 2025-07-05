/*
 * COLOUR POWER mode
 * - Marathon, but with powers given by breaking enough blocks of a certain colour.
 * - Enjoy rainbows!
 */
package zeroxfc.nullpo.custom.modes;

import java.util.ArrayList;
import java.util.Random;
import mu.nu.nullpo.game.component.BGMStatus;
import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.util.CustomProperties;
import mu.nu.nullpo.util.GeneralUtil;
import zeroxfc.nullpo.custom.libs.CustomResourceHolder;
import zeroxfc.nullpo.custom.libs.FieldManipulation;
import zeroxfc.nullpo.custom.libs.GameTextUtilities;
import zeroxfc.nullpo.custom.libs.Interpolation;
import zeroxfc.nullpo.custom.libs.ProfileProperties;
import zeroxfc.nullpo.custom.libs.RendererExtension;
import zeroxfc.nullpo.custom.libs.particles.LandingParticles;

public class ColourPower extends MarathonModeBase {
    // Power meter max
    private int powerMeterMax;

    // Power-up amounts
    private static final int POWERUP_AMOUNT = 8;
    // Power-up names
    private static final String[] POWERUP_NAMES = {
        "EXTRA LIFE",
        "FREE-FALL",
        "DEL. LOWER-1/2",
        "DEL. UPPER-1/2",
        "MULTIPLIER UP",
        "FREEZE-TIME",
        "SIDE SHIFT",
        "SCORE BONUS"
    };

    private static final int POWERUP_EXTRALIFE = 0,
        POWERUP_FREEFALL = 1,
        POWERUP_BOTTOMCLEAR = 2,
        POWERUP_TOPCLEAR = 3,
        POWERUP_MULTIPLIER = 4,
        POWERUP_FREEZE = 5,
        POWERUP_SIDESHIFT = 6,
        POWERUP_SCOREBONUS = 7;

    // Power-up text colours
    private static final int[] POWERUP_TEXT_COLOURS = {
        EventReceiver.COLOR_WHITE,
        EventReceiver.COLOR_RED,
        EventReceiver.COLOR_ORANGE,
        EventReceiver.COLOR_YELLOW,
        EventReceiver.COLOR_GREEN,
        EventReceiver.COLOR_CYAN,
        EventReceiver.COLOR_DARKBLUE,
        EventReceiver.COLOR_PURPLE,
    };
    int l;
    // Power meter values
    private int[] meterValues;
    // Custom status timer
    private int customTimer;
    // Current active power
    private int currentActivePower;
    // Current score multiplier
    private int scoreMultiplier;
    // Current thing being modified
    private int currentModified;
    // HAS SET
    private boolean hasSet;
    // Rulebound mode: default = false.
    private boolean ruleboundMode;
    // Randomizer for non-rulebound mode
    private Random nonRuleboundRandomiser;
    // Colour history
    private int[] colourHistory;
    // engine dif
    private int[] defaultColours;
    // Hm
    private boolean preset;
    // LastScore
    private int scoreBeforeIncrease;

    private boolean hardDropEffect;

    /**
     * Rankings' scores
     */
    private int[][][] rankingScore;
    /**
     * Rankings' line counts
     */
    private int[][][] rankingLines;
    /**
     * Rankings' times
     */
    private int[][][] rankingTime;
    /**
     * Player profile
     */
    private ProfileProperties playerProperties;
    /**
     * Player rank
     */
    private int rankingRankPlayer;
    /**
     * Rankings' scores
     */
    private int[][][] rankingScorePlayer;
    /**
     * Rankings' line counts
     */
    private int[][][] rankingLinesPlayer;
    /**
     * Rankings' times
     */
    private int[][][] rankingTimePlayer;
    private boolean showPlayerStats;
    /**
     * The good hard drop effect
     */
    private ArrayList<int[]> pCoordList;
    private Piece cPiece;
    private String PLAYER_NAME;

    private CustomResourceHolder customGraphics;
    private RendererExtension rendererExtension;

    private LandingParticles landingParticles;

    // Mode Name
    @Override
    public String getName() {
        return "COLOUR POWER";
    }

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
        l = -1;
        defaultColours = null;

        pCoordList = new ArrayList<>();
        cPiece = null;

        customTimer = 0;
        meterValues = new int[POWERUP_AMOUNT];
        currentActivePower = -1;
        scoreMultiplier = 1;
        currentModified = -1;
        hasSet = false;
        ruleboundMode = false;
        preset = false;
        scoreBeforeIncrease = 0;
        colourHistory = new int[] { -1, -1, -1, -1 };

        if (playerProperties == null) {
            playerProperties = new ProfileProperties(EventReceiver.COLOR_GREEN);

            showPlayerStats = false;
        }

        rankingRankPlayer = -1;
        rankingScorePlayer = new int[2][RANKING_TYPE][RANKING_MAX];
        rankingLinesPlayer = new int[2][RANKING_TYPE][RANKING_MAX];
        rankingTimePlayer = new int[2][RANKING_TYPE][RANKING_MAX];

        rankingRank = -1;
        rankingScore = new int[2][RANKING_TYPE][RANKING_MAX];
        rankingLines = new int[2][RANKING_TYPE][RANKING_MAX];
        rankingTime = new int[2][RANKING_TYPE][RANKING_MAX];

        customGraphics = new CustomResourceHolder(1);
        rendererExtension = new RendererExtension(customGraphics);
        hardDropEffect = true;

        netPlayerInit(engine, playerID);

        if (owner.replayMode == false) {
            loadSetting(owner.modeConfig);
            loadRanking(owner.modeConfig, engine.ruleopt.strRuleName);

            if (playerProperties.isLoggedIn()) {
                loadSettingPlayer(playerProperties);
                loadRankingPlayer(playerProperties, engine.ruleopt.strRuleName);
            }

            PLAYER_NAME = "";
            version = BASE_VERSION + 1;
        } else {
            loadSetting(owner.replayProp);
            if ((version == 0) && (owner.replayProp.getProperty("colourpower.endless", false) == true))
                goaltype = 2;

            PLAYER_NAME = owner.replayProp.getProperty("colourpower.playerName", "");

            // NET: Load name
            netPlayerName = engine.owner.replayProp.getProperty(playerID + ".net.netPlayerName", "");
        }

        engine.owner.backgroundStatus.bg = startlevel;
        engine.framecolor = GameEngine.FRAME_COLOR_GREEN;
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
        else if (engine.owner.replayMode == false) {
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
                        engine.owner.backgroundStatus.bg = startlevel;
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
                            engine.owner.backgroundStatus.bg = startlevel;
                        }
                        break;
                    case 8:
                        big = !big;
                        break;
                    case 9:
                        ruleboundMode = !ruleboundMode;
                        break;
                    case 10:
                        hardDropEffect = !hardDropEffect;
                        break;
                }

                // NET: Signal options change
                if (netIsNetPlay && (netNumSpectators > 0)) {
                    netSendOptions(engine);
                }
            }

            engine.owner.backgroundStatus.bg = startlevel;

            // Confirm
            if (engine.ctrl.isPush(Controller.BUTTON_A) && (engine.statc[3] >= 5)) {
                engine.playSE("decide");
                saveSetting(owner.modeConfig);
                receiver.saveModeConfig(owner.modeConfig);

                // NET: Signal start of the game
                if (netIsNetPlay) netLobby.netPlayerClient.send("start1p\n");

                return false;
            }

            // Cancel
            if (engine.ctrl.isPush(Controller.BUTTON_B) && !netIsNetPlay) {
                engine.quitflag = true;
                playerProperties = new ProfileProperties(EventReceiver.COLOR_GREEN);
            }

            // New acc
            if (engine.ctrl.isPush(Controller.BUTTON_E) && engine.ai == null && !netIsNetPlay) {
                playerProperties = new ProfileProperties(EventReceiver.COLOR_GREEN);
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

    private void randomizeColours(GameEngine engine, boolean singlePiece) {
        if (singlePiece) {
            int v = -1;
            for (int j = 0; j < 8; j++) {
                boolean flag = false;

                v = nonRuleboundRandomiser.nextInt(8) + 1;
                for (int elem : colourHistory) {
                    if (elem == v) {
                        flag = true;
                        break;
                    }
                }
                if (!flag) break;
            }
            appendToHistory(v);

            engine.getNextObject(engine.nextPieceCount + engine.ruleopt.nextDisplay - 1).setColor(v);
        } else {
            for (int i = 0; i < engine.nextPieceArrayObject.length; i++) {
                int v = -1;
                for (int j = 0; j < 8; j++) {
                    boolean flag = false;

                    v = nonRuleboundRandomiser.nextInt(8) + 1;
                    for (int elem : colourHistory) {
                        if (elem == v) {
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) break;
                }
                appendToHistory(v);

                engine.nextPieceArrayObject[i].setColor(v);
            }
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
                    "BIG", GeneralUtil.getONorOFF(big));
                drawMenu(engine, playerID, receiver, 18, EventReceiver.COLOR_RED, 9,
                    "RULEBOUND", GeneralUtil.getONorOFF(ruleboundMode));
            } else {
                drawMenu(engine, playerID, receiver, 0, EventReceiver.COLOR_PINK, 10,
                    "DROP EFF.", GeneralUtil.getONorOFF(hardDropEffect)
                );
            }
        }
    }

    public boolean onReady(GameEngine engine, int playerID) {
        powerMeterMax = version < BASE_VERSION + 1 ? 10 : 5;

        if (engine.statc[0] == 1) {
            if (defaultColours == null) defaultColours = engine.ruleopt.pieceColor;
            nonRuleboundRandomiser = new Random(engine.randSeed);
            colourHistory = new int[] { 621, 621, 621, 621 };

            if (!ruleboundMode) {
                randomizeColours(engine, false);
            } else {
                engine.ruleopt.pieceColor = defaultColours;
            }
        }
        return false;
    }

    /*
     * Called for initialization during "Ready" screen
     */
    @Override
    public void startGame(GameEngine engine, int playerID) {
        engine.statistics.level = startlevel;
        engine.statistics.levelDispAdd = 1;
        engine.b2bEnable = enableB2B;
        if (enableCombo == true) {
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

        customTimer = 0;
        meterValues = new int[POWERUP_AMOUNT];
        currentActivePower = -1;
        scoreMultiplier = 1;
        currentModified = -1;
        hasSet = false;
        scoreBeforeIncrease = 0;

        preset = true;

        landingParticles = new LandingParticles(customGraphics, engine.randSeed);

        if (netIsWatch) {
            owner.bgmStatus.bgm = BGMStatus.BGM_NOTHING;
        }

        l = -1;
    }

    /**
     * Set the gravity rate
     *
     * @param engine GameEngine
     */
    public void setSpeed(GameEngine engine) {
        int lv = engine.statistics.level;

        if (lv < 0) lv = 0;
        if (lv >= tableGravity.length) lv = tableGravity.length - 1;

        engine.speed.gravity = tableGravity[lv];
        engine.speed.denominator = tableDenominator[lv];
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
        engine.statistics.scoreFromHardDrop += fall * 2;
        engine.statistics.score += fall * 2;

        //int x = (16 * engine.nowPieceX) + 4 + receiver.getFieldDisplayPositionX(engine, playerID) + (16 * engine.nowPieceObject.getWidth() / 2);
        //int y = (16 * engine.nowPieceY) + 52 + receiver.getFieldDisplayPositionY(engine, playerID) + (16 * engine.nowPieceObject.getHeight() / 2);

        int baseX = (16 * engine.nowPieceX) + 4 + receiver.getFieldDisplayPositionX(engine, playerID);
        int baseY = (16 * engine.nowPieceY) + 52 + receiver.getFieldDisplayPositionY(engine, playerID);
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

        if (engine.quitflag) {
            playerProperties = new ProfileProperties(EventReceiver.COLOR_GREEN);
        }

        if (landingParticles != null) landingParticles.update();
    }

    /*
     * Render score
     */
    @Override
    public void renderLast(GameEngine engine, int playerID) {
        if (owner.menuOnly) return;

        receiver.drawScoreFont(engine, playerID, 0, 0, (ruleboundMode ? "RULEBOUND " : "") + getName(), EventReceiver.COLOR_GREEN);

        if (tableGameClearLines[goaltype] == -1) {
            receiver.drawScoreFont(engine, playerID, 0, 1, "(ENDLESS GAME)", EventReceiver.COLOR_GREEN);
        } else {
            receiver.drawScoreFont(engine, playerID, 0, 1, "(" + tableGameClearLines[goaltype] + " LINES GAME)", EventReceiver.COLOR_GREEN);
        }

        if ((engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (owner.replayMode == false))) {
            if ((owner.replayMode == false) && (big == false) && (engine.ai == null)) {
                float scale = (receiver.getNextDisplayType() == 2) ? 0.5f : 1.0f;
                int topY = (receiver.getNextDisplayType() == 2) ? 6 : 4;
                receiver.drawScoreFont(engine, playerID, 3, topY - 1, "SCORE  LINE TIME", EventReceiver.COLOR_BLUE, scale);

                if (showPlayerStats) {
                    for (int i = 0; i < RANKING_MAX; i++) {
                        receiver.drawScoreFont(engine, playerID, 0, topY + i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW, scale);
                        String s = String.valueOf(rankingScorePlayer[ruleboundMode ? 1 : 0][goaltype][i]);
                        receiver.drawScoreFont(engine, playerID, (s.length() > 6 && receiver.getNextDisplayType() != 2) ? 6 : 3, (s.length() > 6 && receiver.getNextDisplayType() != 2) ? (topY + i) * 2 : (topY + i), s, (i == rankingRankPlayer), (s.length() > 6 && receiver.getNextDisplayType() != 2) ? scale * 0.5f : scale);
                        receiver.drawScoreFont(engine, playerID, 10, topY + i, String.valueOf(rankingLinesPlayer[ruleboundMode ? 1 : 0][goaltype][i]), (i == rankingRankPlayer), scale);
                        receiver.drawScoreFont(engine, playerID, 15, topY + i, GeneralUtil.getTime(rankingTimePlayer[ruleboundMode ? 1 : 0][goaltype][i]), (i == rankingRankPlayer), scale);
                    }

                    receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 1, "PLAYER SCORES", EventReceiver.COLOR_BLUE);
                    receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 2, playerProperties.getNameDisplay(), EventReceiver.COLOR_WHITE, 2f);

                    receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 5, "F:SWITCH RANK SCREEN", EventReceiver.COLOR_GREEN);
                } else {
                    for (int i = 0; i < RANKING_MAX; i++) {
                        receiver.drawScoreFont(engine, playerID, 0, topY + i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW, scale);
                        String s = String.valueOf(rankingScore[ruleboundMode ? 1 : 0][goaltype][i]);
                        receiver.drawScoreFont(engine, playerID, (s.length() > 6 && receiver.getNextDisplayType() != 2) ? 6 : 3, (s.length() > 6 && receiver.getNextDisplayType() != 2) ? (topY + i) * 2 : (topY + i), s, (i == rankingRank), (s.length() > 6 && receiver.getNextDisplayType() != 2) ? scale * 0.5f : scale);
                        receiver.drawScoreFont(engine, playerID, 10, topY + i, String.valueOf(rankingLines[ruleboundMode ? 1 : 0][goaltype][i]), (i == rankingRank), scale);
                        receiver.drawScoreFont(engine, playerID, 15, topY + i, GeneralUtil.getTime(rankingTime[ruleboundMode ? 1 : 0][goaltype][i]), (i == rankingRank), scale);
                    }

                    receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 1, "LOCAL SCORES", EventReceiver.COLOR_BLUE);
                    if (!playerProperties.isLoggedIn())
                        receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 2, "(NOT LOGGED IN)\n(E:LOG IN)");
                    if (playerProperties.isLoggedIn())
                        receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 5, "F:SWITCH RANK SCREEN", EventReceiver.COLOR_GREEN);
                }
            }
        } else if (engine.stat == GameEngine.STAT_CUSTOM && !engine.gameActive) {
            playerProperties.loginScreen.renderScreen(receiver, engine, playerID);
        } else {
            receiver.drawScoreFont(engine, playerID, 0, 3, "SCORE", EventReceiver.COLOR_BLUE);
            String strScore;
            if ((lastscore == 0) || (scgettime >= 120)) {
                strScore = String.valueOf(engine.statistics.score);
            } else {
                strScore = (int) Interpolation.sineStep(scoreBeforeIncrease, engine.statistics.score, (scgettime / 120.0)) + "(+" + lastscore + ")";
            }
            receiver.drawScoreFont(engine, playerID, 0, 4, strScore);

            receiver.drawScoreFont(engine, playerID, 0, 6, "LINE", EventReceiver.COLOR_BLUE);
            if ((engine.statistics.level >= 19) && (tableGameClearLines[goaltype] < 0))
                receiver.drawScoreFont(engine, playerID, 0, 7, engine.statistics.lines + "");
            else
                receiver.drawScoreFont(engine, playerID, 0, 7, engine.statistics.lines + "/" + ((engine.statistics.level + 1) * 10));

            receiver.drawScoreFont(engine, playerID, 0, 9, "LEVEL", EventReceiver.COLOR_BLUE);
            receiver.drawScoreFont(engine, playerID, 0, 10, String.valueOf(engine.statistics.level + 1));

            receiver.drawScoreFont(engine, playerID, 0, 12, "TIME", EventReceiver.COLOR_BLUE);
            receiver.drawScoreFont(engine, playerID, 0, 13, GeneralUtil.getTime(engine.statistics.time));

            // Power-up progess
            float scale = 1f;
            int base = 12;
            if (receiver.getNextDisplayType() >= 1) {
                scale = 0.5f;
                base = 24;
            }
            receiver.drawScoreFont(engine, playerID, (int) (10 / scale), (int) (base / scale), "POWER-UPS", EventReceiver.COLOR_PINK, scale);
            receiver.drawScoreFont(engine, playerID, (int) (10 / scale), (int) (base + 1 / scale), "  GREY:" + String.format("%d/%d", meterValues[0], powerMeterMax), POWERUP_TEXT_COLOURS[0], scale);
            receiver.drawScoreFont(engine, playerID, (int) (10 / scale), (int) (base + 2 / scale), "   RED:" + String.format("%d/%d", meterValues[1], powerMeterMax), POWERUP_TEXT_COLOURS[1], scale);
            receiver.drawScoreFont(engine, playerID, (int) (10 / scale), (int) (base + 3 / scale), "ORANGE:" + String.format("%d/%d", meterValues[2], powerMeterMax), POWERUP_TEXT_COLOURS[2], scale);
            receiver.drawScoreFont(engine, playerID, (int) (10 / scale), (int) (base + 4 / scale), "YELLOW:" + String.format("%d/%d", meterValues[3], powerMeterMax), POWERUP_TEXT_COLOURS[3], scale);
            receiver.drawScoreFont(engine, playerID, (int) (10 / scale), (int) (base + 5 / scale), " GREEN:" + String.format("%d/%d", meterValues[4], powerMeterMax), POWERUP_TEXT_COLOURS[4], scale);
            receiver.drawScoreFont(engine, playerID, (int) (10 / scale), (int) (base + 6 / scale), "  CYAN:" + String.format("%d/%d", meterValues[5], powerMeterMax), POWERUP_TEXT_COLOURS[5], scale);
            receiver.drawScoreFont(engine, playerID, (int) (10 / scale), (int) (base + 7 / scale), "  BLUE:" + String.format("%d/%d", meterValues[6], powerMeterMax), POWERUP_TEXT_COLOURS[6], scale);
            receiver.drawScoreFont(engine, playerID, (int) (10 / scale), (int) (base + 8 / scale), "PURPLE:" + String.format("%d/%d", meterValues[7], powerMeterMax), POWERUP_TEXT_COLOURS[7], scale);

            receiver.drawScoreFont(engine, playerID, 0, 15, "MULTI.", EventReceiver.COLOR_BLUE);
            receiver.drawScoreFont(engine, playerID, 0, 16, scoreMultiplier + "X");

            receiver.drawScoreFont(engine, playerID, 0, 18, "LIVES", EventReceiver.COLOR_GREEN);
            receiver.drawScoreFont(engine, playerID, 0, 19, (engine.stat != GameEngine.STAT_GAMEOVER) ? (engine.lives + 1) + "/5" : l + "/5");

            if (playerProperties.isLoggedIn() || PLAYER_NAME.length() > 0) {
                receiver.drawScoreFont(engine, playerID, 0, 21, "PLAYER", EventReceiver.COLOR_BLUE);
                GameTextUtilities.drawAlignedScoreText(receiver, engine, playerID, false, 0, 22, GameTextUtilities.Text.ofBig(owner.replayMode ? PLAYER_NAME : playerProperties.getNameDisplay()));
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

            if (engine.stat == GameEngine.STAT_CUSTOM && customTimer < 120 && !(currentActivePower == 0 && engine.lives >= 4)) {
                int offset = (10 - POWERUP_NAMES[currentActivePower].length()) / 2;
                receiver.drawMenuFont(engine, playerID, offset, engine.field.getHeight() / 2, POWERUP_NAMES[currentActivePower], POWERUP_TEXT_COLOURS[currentActivePower]);
                receiver.drawMenuFont(engine, playerID, 0, (engine.field.getHeight() / 2) + 1, "ACTIVATED!");
            } else if (currentActivePower == 0 && customTimer < 120 && engine.stat == GameEngine.STAT_CUSTOM && engine.lives >= 4) {
                int offset = (10 - "SMALL SCORE BONUS".length()) / 2;
                receiver.drawMenuFont(engine, playerID, 0, engine.field.getHeight() / 2 - 1, "LIVES FULL!", EventReceiver.COLOR_PINK);
                receiver.drawMenuFont(engine, playerID, offset, engine.field.getHeight() / 2 + 1, "SMALL SCORE BONUS", EventReceiver.COLOR_PINK);
                receiver.drawMenuFont(engine, playerID, 0, (engine.field.getHeight() / 2) + 2, "ACTIVATED!");
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
    public boolean onLineClear(GameEngine engine, int playerID) {
        if (!hasSet) {
            currentModified = addPowerToIndex(engine.nowPieceObject.getColors()[0] - 1);
            hasSet = true;
        }

        return false;
    }

    @Override
    public boolean onGameOver(GameEngine engine, int playerID) {
        if (l == -1) l = 0;
        return false;
    }

    @Override
    public boolean onExcellent(GameEngine engine, int playerID) {
        if (engine.lives > 0) {
            int bonus = engine.lives * 50000 * scoreMultiplier;

            lastscore = bonus;
            lastevent = EVENT_NONE;
            scgettime = 0;
            scoreBeforeIncrease = engine.statistics.score;
            engine.statistics.score += bonus;

            engine.lives = 0;
        }
        return false;
    }

    @Override
    public boolean onMove(GameEngine engine, int playerID) {
        if (currentModified != -1) {
            currentActivePower = currentModified;
            engine.resetStatc();
            engine.stat = GameEngine.STAT_CUSTOM;
            customTimer = 0;
            return true;
        }

//		if (engine.statc[0] > 0 && !ruleboundMode && !preset) {
//			int v = -1;
//			for (int j = 0; j < 8; j++) {
//				boolean flag = false;
//				
//				 v = nonRuleboundRandomiser.nextInt(POWERUP_NAMES.length);
//				 for (int elem : colourHistory) {
//					if (elem == v) {
//						flag = true;
//						break;
//					}
//				 }
//				 if (!flag) break;
//			}
//			appendToHistory(v);
//			
//			try {
//				engine.nextPieceArrayObject[engine.nextPieceCount + engine.ruleopt.nextDisplay - 1].setColor(v + 1);
//			} catch (IndexOutOfBoundsException e) {
//				// DO NOTHING
//			}
//			
//			preset = true;
//		}
        if (engine.statc[0] == 0 && !ruleboundMode && preset && !engine.holdDisable) {
            preset = false;
        }

        hasSet = false;

        return false;
    }

    // Where the magic happens
    @Override
    public boolean onCustom(GameEngine engine, int playerID) {
        if (engine.gameActive) {
            customTimer++;
            if (customTimer >= 150) {
				/*
				switch (currentActivePower) {
				case POWERUP_BOTTOMCLEAR:
				case POWERUP_TOPCLEAR:
				case POWERUP_FREEFALL:
					engine.stat = GameEngine.STAT_LINECLEAR;
					// engine.stat = GameEngine.STAT_MOVE;
					break;
				default:
					engine.stat = GameEngine.STAT_MOVE;
					break;
				} */
                engine.resetStatc();
                engine.stat = GameEngine.STAT_MOVE;
                currentModified = -1;
                currentActivePower = 1;
                return true;
            } else if (customTimer == 120) {
                switch (currentActivePower) {
                    case POWERUP_EXTRALIFE:
                        if (engine.lives < 4) {
                            engine.lives++;
                            engine.playSE("cool");
                        } else {
                            scgettime = 0;
                            lastevent = EVENT_NONE;
                            scoreBeforeIncrease = engine.statistics.score;
                            engine.statistics.score += (3200 * scoreMultiplier * (engine.statistics.level + 1));
                            lastscore = (3200 * scoreMultiplier * (engine.statistics.level + 1));
                            engine.playSE("medal");
                        }
                        break;
                    case POWERUP_FREEFALL:
                        if (engine.field.freeFall()) {
                            engine.field.checkLine();
                            engine.field.clearLine();

                            for (int i = 0; i < engine.field.getHeight(); i++) {
                                if (engine.field.getLineFlag(i)) {
                                    for (int j = 0; j < engine.field.getWidth(); j++) {
                                        Block blk = engine.field.getBlock(j, i);

                                        if (blk != null) {
                                            // blockBreak(engine, playerID, j, i, blk);
                                            receiver.blockBreak(engine, playerID, j, i, blk);
                                        }
                                    }
                                }
                            }

                            engine.field.downFloatingBlocks();
                            engine.playSE("linefall");
                        }
                        break;
                    case POWERUP_BOTTOMCLEAR:
                        if (!engine.field.isEmpty()) {
                            engine.field.delLower();
                            engine.field.clearLine();

                            for (int i = 0; i < engine.field.getHeight(); i++) {
                                if (engine.field.getLineFlag(i)) {
                                    for (int j = 0; j < engine.field.getWidth(); j++) {
                                        Block blk = engine.field.getBlock(j, i);

                                        if (blk != null) {
                                            // blockBreak(engine, playerID, j, i, blk);
                                            receiver.blockBreak(engine, playerID, j, i, blk);
                                        }
                                    }
                                }
                            }

                            engine.field.downFloatingBlocks();

                            engine.playSE("linefall");
                        }
                        break;
                    case POWERUP_TOPCLEAR:
                        if (!engine.field.isEmpty()) {
                            FieldManipulation.delUpperFix(engine.field);
                            engine.field.clearLine();

                            for (int i = 0; i < engine.field.getHeight(); i++) {
                                if (engine.field.getLineFlag(i)) {
                                    for (int j = 0; j < engine.field.getWidth(); j++) {
                                        Block blk = engine.field.getBlock(j, i);

                                        if (blk != null) {
                                            // blockBreak(engine, playerID, j, i, blk);
                                            receiver.blockBreak(engine, playerID, j, i, blk);
                                        }
                                    }
                                }
                            }

                            engine.field.downFloatingBlocks();

                            engine.playSE("linefall");
                        }
                        break;
                    case POWERUP_MULTIPLIER:
                        scoreMultiplier++;
                        engine.playSE("cool");
                        break;
                    case POWERUP_FREEZE:
                        engine.speed.gravity = 0;
                        break;
                    case POWERUP_SIDESHIFT:
                        engine.field.moveLeft();
                        engine.playSE("linefall");
                        break;
                    case POWERUP_SCOREBONUS:
                        scgettime = 0;
                        lastevent = EVENT_NONE;
                        scoreBeforeIncrease = engine.statistics.score;
                        engine.statistics.score += (6400 * scoreMultiplier * (engine.statistics.level + 1));
                        lastscore = (6400 * scoreMultiplier * (engine.statistics.level + 1));
                        engine.playSE("medal");
                        break;
                }
            }
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

        return false;
    }

    /*
     * Calculate score
     */
    @Override
    public void calcScore(GameEngine engine, int playerID, int lines) {
        setSpeed(engine);

        if (!ruleboundMode) {
            randomizeColours(engine, true);
        } else {
            engine.ruleopt.pieceColor = defaultColours;
        }

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
                if (engine.b2b) {
                    pts += 2400 * (engine.statistics.level + 1);
                } else {
                    pts += 1600 * (engine.statistics.level + 1);
                }
                lastevent = EVENT_TSPIN_TRIPLE;
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
                if (engine.b2b) {
                    pts += 1200 * (engine.statistics.level + 1);
                } else {
                    pts += 800 * (engine.statistics.level + 1);
                }
                lastevent = EVENT_FOUR;
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
            if (lines > 0 && engine.lives > 0 && (engine.statistics.lines % 100 == 0) && (tableGameClearLines[goaltype] < 0) && (engine.statistics.lines >= 200)) {
                int bonus = engine.lives * 50000 * scoreMultiplier;
                pts += bonus;
            }

            pts *= scoreMultiplier;
            lastscore = pts;
            lastpiece = engine.nowPieceObject.id;
            scgettime = 0;
            scoreBeforeIncrease = engine.statistics.score;
            if (lines >= 1) engine.statistics.scoreFromLineClear += pts;
            else engine.statistics.scoreFromOtherBonus += pts;
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
            l = engine.lives;
            engine.ending = 1;
            engine.gameEnded();
        } else if ((engine.statistics.lines >= (engine.statistics.level + 1) * 10) && (engine.statistics.level < 19)) {
            // Level up
            engine.statistics.level++;

            owner.backgroundStatus.fadesw = true;
            owner.backgroundStatus.fadecount = 0;
            owner.backgroundStatus.fadebg = engine.statistics.level;

            setSpeed(engine);
            engine.playSE("levelup");
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
            updateRanking(engine.statistics.score, engine.statistics.lines, engine.statistics.time, goaltype);

            if (playerProperties.isLoggedIn()) {
                prop.setProperty("colourpower.playerName", playerProperties.getNameDisplay());
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
        startlevel = prop.getProperty("colourpower.startlevel", 0);
        tspinEnableType = prop.getProperty("colourpower.tspinEnableType", 1);
        enableTSpin = prop.getProperty("colourpower.enableTSpin", true);
        enableTSpinKick = prop.getProperty("colourpower.enableTSpinKick", true);
        spinCheckType = prop.getProperty("colourpower.spinCheckType", 0);
        tspinEnableEZ = prop.getProperty("colourpower.tspinEnableEZ", false);
        enableB2B = prop.getProperty("colourpower.enableB2B", true);
        enableCombo = prop.getProperty("colourpower.enableCombo", true);
        goaltype = prop.getProperty("colourpower.gametype", 0);
        big = prop.getProperty("colourpower.big", false);
        version = prop.getProperty("colourpower.version", BASE_VERSION + 1);
        ruleboundMode = prop.getProperty("colourpower.rulebound", false);
        hardDropEffect = prop.getProperty("colourpower.hardDropEffect", true);
    }

    /**
     * Save settings to property file
     *
     * @param prop Property file
     */
    private void saveSetting(CustomProperties prop) {
        prop.setProperty("colourpower.startlevel", startlevel);
        prop.setProperty("colourpower.tspinEnableType", tspinEnableType);
        prop.setProperty("colourpower.enableTSpin", enableTSpin);
        prop.setProperty("colourpower.enableTSpinKick", enableTSpinKick);
        prop.setProperty("colourpower.spinCheckType", spinCheckType);
        prop.setProperty("colourpower.tspinEnableEZ", tspinEnableEZ);
        prop.setProperty("colourpower.enableB2B", enableB2B);
        prop.setProperty("colourpower.enableCombo", enableCombo);
        prop.setProperty("colourpower.gametype", goaltype);
        prop.setProperty("colourpower.big", big);
        prop.setProperty("colourpower.version", version);
        prop.setProperty("colourpower.rulebound", ruleboundMode);
        prop.setProperty("colourpower.hardDropEffect", hardDropEffect);
    }

    /**
     * Load settings from property file
     *
     * @param prop Property file
     */
    private void loadSettingPlayer(ProfileProperties prop) {
        if (!prop.isLoggedIn()) return;
        startlevel = prop.getProperty("colourpower.startlevel", 0);
        tspinEnableType = prop.getProperty("colourpower.tspinEnableType", 1);
        enableTSpin = prop.getProperty("colourpower.enableTSpin", true);
        enableTSpinKick = prop.getProperty("colourpower.enableTSpinKick", true);
        spinCheckType = prop.getProperty("colourpower.spinCheckType", 0);
        tspinEnableEZ = prop.getProperty("colourpower.tspinEnableEZ", false);
        enableB2B = prop.getProperty("colourpower.enableB2B", true);
        enableCombo = prop.getProperty("colourpower.enableCombo", true);
        goaltype = prop.getProperty("colourpower.gametype", 0);
        big = prop.getProperty("colourpower.big", false);
        ruleboundMode = prop.getProperty("colourpower.rulebound", false);
        hardDropEffect = prop.getProperty("colourpower.hardDropEffect", true);
    }

    /**
     * Save settings to property file
     *
     * @param prop Property file
     */
    private void saveSettingPlayer(ProfileProperties prop) {
        if (!prop.isLoggedIn()) return;
        prop.setProperty("colourpower.startlevel", startlevel);
        prop.setProperty("colourpower.tspinEnableType", tspinEnableType);
        prop.setProperty("colourpower.enableTSpin", enableTSpin);
        prop.setProperty("colourpower.enableTSpinKick", enableTSpinKick);
        prop.setProperty("colourpower.spinCheckType", spinCheckType);
        prop.setProperty("colourpower.tspinEnableEZ", tspinEnableEZ);
        prop.setProperty("colourpower.enableB2B", enableB2B);
        prop.setProperty("colourpower.enableCombo", enableCombo);
        prop.setProperty("colourpower.gametype", goaltype);
        prop.setProperty("colourpower.big", big);
        prop.setProperty("colourpower.rulebound", ruleboundMode);
        prop.setProperty("colourpower.hardDropEffect", hardDropEffect);
    }

    /**
     * Read rankings from property file
     *
     * @param prop     Property file
     * @param ruleName Rule name
     */
    @Override
    protected void loadRanking(CustomProperties prop, String ruleName) {
        for (int h = 0; h < 2; h++) {
            for (int i = 0; i < RANKING_MAX; i++) {
                for (int j = 0; j < GAMETYPE_MAX; j++) {
                    rankingScore[h][j][i] = prop.getProperty("colourpower.ranking." + ruleName + "." + h + "." + j + ".score." + i, 0);
                    rankingLines[h][j][i] = prop.getProperty("colourpower.ranking." + ruleName + "." + h + "." + j + ".lines." + i, 0);
                    rankingTime[h][j][i] = prop.getProperty("colourpower.ranking." + ruleName + "." + h + "." + j + ".time." + i, 0);
                }
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
        for (int h = 0; h < 2; h++) {
            for (int i = 0; i < RANKING_MAX; i++) {
                for (int j = 0; j < GAMETYPE_MAX; j++) {
                    prop.setProperty("colourpower.ranking." + ruleName + "." + h + "." + j + ".score." + i, rankingScore[h][j][i]);
                    prop.setProperty("colourpower.ranking." + ruleName + "." + h + "." + j + ".lines." + i, rankingLines[h][j][i]);
                    prop.setProperty("colourpower.ranking." + ruleName + "." + h + "." + j + ".time." + i, rankingTime[h][j][i]);
                }
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
        for (int h = 0; h < 2; h++) {
            for (int i = 0; i < RANKING_MAX; i++) {
                for (int j = 0; j < GAMETYPE_MAX; j++) {
                    rankingScorePlayer[h][j][i] = prop.getProperty("colourpower.ranking." + ruleName + "." + h + "." + j + ".score." + i, 0);
                    rankingLinesPlayer[h][j][i] = prop.getProperty("colourpower.ranking." + ruleName + "." + h + "." + j + ".lines." + i, 0);
                    rankingTimePlayer[h][j][i] = prop.getProperty("colourpower.ranking." + ruleName + "." + h + "." + j + ".time." + i, 0);
                }
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
        for (int h = 0; h < 2; h++) {
            for (int i = 0; i < RANKING_MAX; i++) {
                for (int j = 0; j < GAMETYPE_MAX; j++) {
                    prop.setProperty("colourpower.ranking." + ruleName + "." + h + "." + j + ".score." + i, rankingScorePlayer[h][j][i]);
                    prop.setProperty("colourpower.ranking." + ruleName + "." + h + "." + j + ".lines." + i, rankingLinesPlayer[h][j][i]);
                    prop.setProperty("colourpower.ranking." + ruleName + "." + h + "." + j + ".time." + i, rankingTimePlayer[h][j][i]);
                }
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
                rankingScore[ruleboundMode ? 1 : 0][type][i] = rankingScore[ruleboundMode ? 1 : 0][type][i - 1];
                rankingLines[ruleboundMode ? 1 : 0][type][i] = rankingLines[ruleboundMode ? 1 : 0][type][i - 1];
                rankingTime[ruleboundMode ? 1 : 0][type][i] = rankingTime[ruleboundMode ? 1 : 0][type][i - 1];
            }

            // Add new data
            rankingScore[ruleboundMode ? 1 : 0][type][rankingRank] = sc;
            rankingLines[ruleboundMode ? 1 : 0][type][rankingRank] = li;
            rankingTime[ruleboundMode ? 1 : 0][type][rankingRank] = time;
        }

        if (playerProperties.isLoggedIn()) {
            rankingRankPlayer = checkRankingPlayer(sc, li, time, type);

            if (rankingRankPlayer != -1) {
                // Shift down ranking entries
                for (int i = RANKING_MAX - 1; i > rankingRankPlayer; i--) {
                    rankingScorePlayer[ruleboundMode ? 1 : 0][type][i] = rankingScorePlayer[ruleboundMode ? 1 : 0][type][i - 1];
                    rankingLinesPlayer[ruleboundMode ? 1 : 0][type][i] = rankingLinesPlayer[ruleboundMode ? 1 : 0][type][i - 1];
                    rankingTimePlayer[ruleboundMode ? 1 : 0][type][i] = rankingTimePlayer[ruleboundMode ? 1 : 0][type][i - 1];
                }

                // Add new data
                rankingScorePlayer[ruleboundMode ? 1 : 0][type][rankingRankPlayer] = sc;
                rankingLinesPlayer[ruleboundMode ? 1 : 0][type][rankingRankPlayer] = li;
                rankingTimePlayer[ruleboundMode ? 1 : 0][type][rankingRankPlayer] = time;
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
            if (sc > rankingScore[ruleboundMode ? 1 : 0][type][i]) {
                return i;
            } else if ((sc == rankingScore[ruleboundMode ? 1 : 0][type][i]) && (li > rankingLines[ruleboundMode ? 1 : 0][type][i])) {
                return i;
            } else if ((sc == rankingScore[ruleboundMode ? 1 : 0][type][i]) && (li == rankingLines[ruleboundMode ? 1 : 0][type][i]) && (time < rankingTime[ruleboundMode ? 1 : 0][type][i])) {
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
            if (sc > rankingScorePlayer[ruleboundMode ? 1 : 0][type][i]) {
                return i;
            } else if ((sc == rankingScorePlayer[ruleboundMode ? 1 : 0][type][i]) && (li > rankingLinesPlayer[ruleboundMode ? 1 : 0][type][i])) {
                return i;
            } else if ((sc == rankingScorePlayer[ruleboundMode ? 1 : 0][type][i]) && (li == rankingLinesPlayer[ruleboundMode ? 1 : 0][type][i]) && (time < rankingTimePlayer[ruleboundMode ? 1 : 0][type][i])) {
                return i;
            }
        }

        return -1;
    }

    // Add to meter at specific index
    private int addPowerToIndex(int index) {
        meterValues[index]++;

        if (meterValues[index] >= powerMeterMax) {
            meterValues[index] = 0;
            return index;
        }

        return -1;
    }

    // Append to history
    private void appendToHistory(int val) {
        for (int i = colourHistory.length - 1; i > 0; i--) {
            colourHistory[i - 1] = colourHistory[i];
        }
        colourHistory[colourHistory.length - 1] = val;
    }
}
