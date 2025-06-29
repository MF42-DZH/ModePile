package zeroxfc.nullpo.custom.modes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Random;
import mu.nu.nullpo.game.component.BGMStatus;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.util.CustomProperties;
import mu.nu.nullpo.util.GeneralUtil;
import zeroxfc.nullpo.custom.libs.ProfileProperties;
import zeroxfc.nullpo.custom.libs.RendererExtension;

public class MissionMode extends MarathonModeBase {
    /**
     * Line counts when BGM changes occur
     */
    public static final int[] tableBGMChange = { 25, 50, 75, 100, -1 };
    /**
     * Line counts when game ending occurs
     */
    public static final int[] tableGameClearLines = { 50, 100, -1 };
    private static final int MISSION_GENERIC = EventReceiver.COLOR_PINK,
        MISSION_I = EventReceiver.COLOR_CYAN,
        MISSION_I_CLASSIC = EventReceiver.COLOR_RED,
        MISSION_J = EventReceiver.COLOR_DARKBLUE,
        MISSION_L = EventReceiver.COLOR_ORANGE,
        MISSION_O = EventReceiver.COLOR_YELLOW,
        MISSION_S = EventReceiver.COLOR_GREEN,
        MISSION_S_CLASSIC = EventReceiver.COLOR_PURPLE,
        MISSION_T = EventReceiver.COLOR_PURPLE,
        MISSION_T_CLASSIC = EventReceiver.COLOR_CYAN,
        MISSION_Z = EventReceiver.COLOR_RED,
        MISSION_Z_CLASSIC = EventReceiver.COLOR_GREEN,
        MISSION_NEUTRAL = EventReceiver.COLOR_WHITE;
    private static final int MISSION_CATEGORY_SPIN = 0,
        MISSION_CATEGORY_CLEAR = 1,
        MISSION_CATEGORY_SPIN_NONSPECIFIC = 2,
        MISSION_CATEGORY_CLEAR_NONSPECIFIC = 3,
        MISSION_CATEGORY_COMBO = 4;
    private static final String[] SPIN_MISSION_TABLE = {
        "-SPIN",
        "-SPIN SINGLE",
        "-SPIN DOUBLE",
        "-SPIN TRIPLE",
    };
    private static final String[] NON_SPECIFIC_SPIN_MISSION_TABLE = {
        "SPIN",
        "SPIN SINGLE",
        "SPIN DOUBLE",
        "SPIN TRIPLE",
    };
    private static final String[] LINECLEAR_MISSION_TABLE = {
        "SINGLE",
        "DOUBLE",
        "TRIPLE",
        "TETRIS"
    };
    private static final String[] VALID_SPIN_PIECES = {
        "J", "L", "S", "T", "Z"
    };
    private static final String[] VALID_LINECLEAR_PIECES = {
        "I", "J", "L", "O", "S", "T", "Z"
    };
    private static final int[] VALID_SPIN_MAX_LINES = {
        3, 3, 3, 3, 3
    };
    private static final int[] VALID_CLASSIC_SPIN_MAX_LINES = {
        2, 2, 1, 2, 1
    };
    private static final int[] VALID_MAX_LINECLEARS = {
        4, 3, 3, 2, 3, 3, 3
    };
    private static final int[] VALID_CLASSIC_MAX_LINECLEARS = {
        4, 3, 3, 2, 1, 2, 1
    };
    private static final int MAX_MISSION_GOAL = 4;
    private static final int MAX_MISSION_COMBO = 5;  // Add two. (3 because random...)
    // private static final int MAX_MISSION_TYPES = 5;
    private static final int headerColour = EventReceiver.COLOR_GREEN;
    // Mission randomiser.
    private Random missionRandomiser;
    // Mission text.
    private LinkedHashMap<String, Integer> currentMissionText;
    // Mission data.
    private int missionCategory;
    private int missionGoal;
    private int missionProgress;
    private int lineAmount;
    private int spinDetectionType;
    // private boolean isSpinMission;
    // private boolean isSpecific;
    private String specificPieceName;
    private boolean missionIsComplete;
    /**
     * The good hard drop effect
     */
    private ArrayList<int[]> pCoordList;
    private Piece cPiece;
    private ProfileProperties playerProperties;
    private String PLAYER_NAME;
    private boolean showPlayerStats;
    private int[][] rankingScorePlayer, rankingTimePlayer;
    private int rankingRankPlayer;

    private RendererExtension rendererExtension;

    @Override
    public String getName() {
        return "MISSION MODE";
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

        pCoordList = new ArrayList<>();
        cPiece = null;

        currentMissionText = new LinkedHashMap<String, Integer>();
        missionCategory = -1;
        missionGoal = -1;
        missionProgress = 0;
        lineAmount = 0;
        spinDetectionType = 0;
        // isSpinMission = false;
        // isSpecific = false;
        specificPieceName = "";

        rankingRank = -1;
        rankingScore = new int[RANKING_TYPE][RANKING_MAX];
        rankingTime = new int[RANKING_TYPE][RANKING_MAX];

        if (playerProperties == null) {
            playerProperties = new ProfileProperties(headerColour);

            showPlayerStats = false;
        }

        rankingRank = -1;
        rankingScore = new int[RANKING_TYPE][RANKING_MAX];
        rankingTime = new int[RANKING_TYPE][RANKING_MAX];

        rendererExtension = new RendererExtension();

        netPlayerInit(engine, playerID);

        if (owner.replayMode == false) {
            loadSetting(owner.modeConfig);
            loadRanking(owner.modeConfig, engine.ruleopt.strRuleName);

            if (playerProperties.isLoggedIn()) {
                loadSettingPlayer(playerProperties);
                loadRankingPlayer(playerProperties, engine.ruleopt.strRuleName);
            }

            PLAYER_NAME = "";
            version = BASE_VERSION;
        } else {
            loadSetting(owner.replayProp);
            if ((version == 0) && (owner.replayProp.getProperty("missionmode.endless", false) == true))
                goaltype = 2;

            PLAYER_NAME = owner.replayProp.getProperty("missionmode.playerName", "");

            // NET: Load name
            netPlayerName = engine.owner.replayProp.getProperty(playerID + ".net.netPlayerName", "");
        }

        engine.owner.backgroundStatus.bg = startlevel;
        engine.framecolor = GameEngine.FRAME_COLOR_GREEN;
        missionIsComplete = false;
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
            int change = updateCursor(engine, 0, playerID);

            if (change != 0) {
                engine.playSE("change");

                switch (engine.statc[2]) {
                    case 0:
                        goaltype += change;
                        if (goaltype < 0) goaltype = GAMETYPE_MAX - 1;
                        if (goaltype > GAMETYPE_MAX - 1) goaltype = 0;

                        if ((startlevel > (tableGameClearLines[goaltype] - 1) / 10) && (tableGameClearLines[goaltype] >= 0)) {
                            startlevel = (tableGameClearLines[goaltype] - 1) / 10;
                            engine.owner.backgroundStatus.bg = startlevel;
                        }
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
        if (netIsNetRankingDisplayMode) {
            // NET: Netplay Ranking
            netOnRenderNetPlayRanking(engine, playerID, receiver);
        } else {
            drawMenu(engine, playerID, receiver, 0, EventReceiver.COLOR_BLUE, 0,
                "GOAL", (goaltype == 2) ? "ENDLESS" : tableGameClearLines[goaltype] + " MISS.");
        }
    }

    public boolean onReady(GameEngine engine, int playerID) {
        if (engine.statc[0] == 0) {
            missionRandomiser = new Random(engine.randSeed);
            generateNewMission(engine, false);
        }

        return false;
    }

    /*
     * Called after every frame
     */
    @Override
    public void onLast(GameEngine engine, int playerID) {
        scgettime++;

        if ((scgettime >= 120) && (missionIsComplete) && (engine.timerActive)) {
            generateNewMission(engine, engine.statistics.score == (tableGameClearLines[goaltype] - 1));
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
        }
    }


    /*
     * Render score
     */
    @Override
    public void renderLast(GameEngine engine, int playerID) {
        if (owner.menuOnly) return;

        receiver.drawScoreFont(engine, playerID, 0, 0, getName(), EventReceiver.COLOR_GREEN);

        if (tableGameClearLines[goaltype] == -1) {
            receiver.drawScoreFont(engine, playerID, 0, 1, "(ENDLESS GAME)", EventReceiver.COLOR_GREEN);
        } else {
            receiver.drawScoreFont(engine, playerID, 0, 1, "(" + tableGameClearLines[goaltype] + " MISSIONS GAME)", EventReceiver.COLOR_GREEN);
        }

        if ((engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (owner.replayMode == false))) {
            if ((owner.replayMode == false) && (big == false) && (engine.ai == null)) {
                float scale = (receiver.getNextDisplayType() == 2) ? 0.5f : 1.0f;
                int topY = (receiver.getNextDisplayType() == 2) ? 6 : 4;
                receiver.drawScoreFont(engine, playerID, 3, topY - 1, "SCORE TIME", EventReceiver.COLOR_BLUE, scale);

                if (showPlayerStats) {
                    for (int i = 0; i < RANKING_MAX; i++) {
                        receiver.drawScoreFont(engine, playerID, 0, topY + i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW, scale);
                        receiver.drawScoreFont(engine, playerID, 3, topY + i, String.valueOf(rankingScorePlayer[goaltype][i]), (i == rankingRankPlayer), scale);
                        receiver.drawScoreFont(engine, playerID, 9, topY + i, GeneralUtil.getTime(rankingTimePlayer[goaltype][i]), (i == rankingRankPlayer), scale);
                    }

                    receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 1, "PLAYER SCORES", EventReceiver.COLOR_BLUE);
                    receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 2, playerProperties.getNameDisplay(), EventReceiver.COLOR_WHITE, 2f);

                    receiver.drawScoreFont(engine, playerID, 0, topY + RANKING_MAX + 5, "F:SWITCH RANK SCREEN", EventReceiver.COLOR_GREEN);
                } else {
                    for (int i = 0; i < RANKING_MAX; i++) {
                        receiver.drawScoreFont(engine, playerID, 0, topY + i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW, scale);
                        receiver.drawScoreFont(engine, playerID, 3, topY + i, String.valueOf(rankingScore[goaltype][i]), (i == rankingRank), scale);
                        receiver.drawScoreFont(engine, playerID, 9, topY + i, GeneralUtil.getTime(rankingTime[goaltype][i]), (i == rankingRank), scale);
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
            receiver.drawScoreFont(engine, playerID, 0, 3, "COMPLETED", EventReceiver.COLOR_BLUE);
            String strScore;
            if ((lastscore == 0) || (scgettime >= 120)) {
                strScore = engine.statistics.score + "/" + ((engine.statistics.level + 1) * 5);
            } else {
                strScore = engine.statistics.score + "(+" + lastscore + ")";
            }
            receiver.drawScoreFont(engine, playerID, 0, 4, strScore);

            receiver.drawScoreFont(engine, playerID, 0, 6, "LEVEL", EventReceiver.COLOR_BLUE);
            receiver.drawScoreFont(engine, playerID, 0, 7, String.valueOf(engine.statistics.level + 1));

            receiver.drawScoreFont(engine, playerID, 0, 9, "TIME", EventReceiver.COLOR_BLUE);
            receiver.drawScoreFont(engine, playerID, 0, 10, GeneralUtil.getTime(engine.statistics.time));

            receiver.drawScoreFont(engine, playerID, 0, 12, "CURRENT MISSION", EventReceiver.COLOR_GREEN);
            receiver.drawScoreFont(engine, playerID, 0, 13, missionProgress + "/" + missionGoal, missionIsComplete);
            if (!missionIsComplete) {
                drawMissionStrings(engine, playerID, currentMissionText, 0, 15, 1.0f);
            }

            if (playerProperties.isLoggedIn() || PLAYER_NAME.length() > 0) {
                receiver.drawScoreFont(engine, playerID, 0, 20, "PLAYER", EventReceiver.COLOR_BLUE);
                receiver.drawScoreFont(engine, playerID, 0, 21, owner.replayMode ? PLAYER_NAME : playerProperties.getNameDisplay(), EventReceiver.COLOR_WHITE, 2f);
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

            if ((lastevent != EVENT_NONE)) {
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
     * Calculate score
     */
    @Override
    public void calcScore(GameEngine engine, int playerID, int lines) {
        // Line clear bonus
        int pts = 0;
        boolean incremented = false;
        lastpiece = engine.nowPieceObject.id;

        if (engine.tspin) {
            // T-Spin 0 lines
            if ((lines == 0) && (!engine.tspinez)) {
                if (engine.tspinmini) {
//					pts += 100 * (engine.statistics.level + 1);
                    lastevent = EVENT_TSPIN_ZERO_MINI;
                } else {
//					pts += 400 * (engine.statistics.level + 1);
                    lastevent = EVENT_TSPIN_ZERO;
                }
            }
            // Immobile EZ Spin
            else if (engine.tspinez && (lines > 0)) {
                if (engine.b2b) {
//					pts += 180 * (engine.statistics.level + 1);
                } else {
//					pts += 120 * (engine.statistics.level + 1);
                }
                lastevent = EVENT_TSPIN_EZ;
            }
            // T-Spin 1 line
            else if (lines == 1) {
                if (engine.tspinmini) {
                    if (engine.b2b) {
//						pts += 300 * (engine.statistics.level + 1);
                    } else {
//						pts += 200 * (engine.statistics.level + 1);
                    }
                    lastevent = EVENT_TSPIN_SINGLE_MINI;
                } else {
                    if (engine.b2b) {
//						pts += 1200 * (engine.statistics.level + 1);
                    } else {
//						pts += 800 * (engine.statistics.level + 1);
                    }
                    lastevent = EVENT_TSPIN_SINGLE;
                }
            }
            // T-Spin 2 lines
            else if (lines == 2) {
                if (engine.tspinmini && engine.useAllSpinBonus) {
                    if (engine.b2b) {
//						pts += 600 * (engine.statistics.level + 1);
                    } else {
//						pts += 400 * (engine.statistics.level + 1);
                    }
                    lastevent = EVENT_TSPIN_DOUBLE_MINI;
                } else {
                    if (engine.b2b) {
//						pts += 1800 * (engine.statistics.level + 1);
                    } else {
//						pts += 1200 * (engine.statistics.level + 1);
                    }
                    lastevent = EVENT_TSPIN_DOUBLE;
                }
            }
            // T-Spin 3 lines
            else if (lines >= 3) {
                if (engine.b2b) {
//					pts += 2400 * (engine.statistics.level + 1);
                } else {
//					pts += 1600 * (engine.statistics.level + 1);
                }
                lastevent = EVENT_TSPIN_TRIPLE;
            }
        } else {
            if (lines == 1) {
//				pts += 100 * (engine.statistics.level + 1); // 1列
                lastevent = EVENT_SINGLE;
            } else if (lines == 2) {
//				pts += 300 * (engine.statistics.level + 1); // 2列
                lastevent = EVENT_DOUBLE;
            } else if (lines == 3) {
//				pts += 500 * (engine.statistics.level + 1); // 3列
                lastevent = EVENT_TRIPLE;
            } else if (lines >= 4) {
                // 4 lines
                if (engine.b2b) {
//					pts += 1200 * (engine.statistics.level + 1);
                } else {
//					pts += 800 * (engine.statistics.level + 1);
                }
                lastevent = EVENT_FOUR;
            } else if (lines == 0) {
                lastevent = EVENT_NONE;
            }
        }

        if (!missionIsComplete) {
            switch (missionCategory) {
                case MISSION_CATEGORY_SPIN:
                    if (engine.tspin) {
                        if (lines == lineAmount && Piece.getPieceName(lastpiece) == specificPieceName) {
                            missionProgress++;
                            incremented = true;
                        }
                    }
                    break;
                case MISSION_CATEGORY_SPIN_NONSPECIFIC:
                    if (engine.tspin) {
                        if (lines == lineAmount) {
                            missionProgress++;
                            incremented = true;
                        }
                    }
                    break;
                case MISSION_CATEGORY_CLEAR:
                    if (lines == lineAmount && Piece.getPieceName(lastpiece) == specificPieceName) {
                        missionProgress++;
                        incremented = true;
                    }
                    break;
                case MISSION_CATEGORY_CLEAR_NONSPECIFIC:
                    if (lines == lineAmount) {
                        missionProgress++;
                        incremented = true;
                    }
                    break;
                default:
                    break;
            }
        }

        lastb2b = engine.b2b;

        if (!missionIsComplete) {
            if (missionCategory == MISSION_CATEGORY_COMBO) {
                missionProgress = engine.combo;
            }
            if ((missionCategory == MISSION_CATEGORY_COMBO) &&
                (missionProgress == missionGoal)) {
                incremented = true;
            }
        }

        // Combo
        if ((engine.combo >= 1) && (lines >= 1)) {
//			pts += ((engine.combo - 1) * 50) * (engine.statistics.level + 1);
            lastcombo = engine.combo;
        }

        if (missionProgress == missionGoal && incremented) {
            pts = 1;
            missionIsComplete = true;
            engine.playSE("cool");
        }

        // Add to score
        if (pts > 0) {
            lastscore = pts;
            scgettime = 0;
            if (lines >= 1) engine.statistics.scoreFromLineClear += pts;
            else engine.statistics.scoreFromOtherBonus += pts;
            engine.statistics.score += pts;
        }

        // BGM fade-out effects and BGM changes
        if (tableBGMChange[bgmlv] != -1) {
            if (engine.statistics.score >= tableBGMChange[bgmlv] - 1) owner.bgmStatus.fadesw = true;

            if ((engine.statistics.score >= tableBGMChange[bgmlv]) &&
                ((engine.statistics.score < tableGameClearLines[goaltype]) || (tableGameClearLines[goaltype] < 0))) {
                bgmlv++;
                owner.bgmStatus.bgm = bgmlv;
                owner.bgmStatus.fadesw = false;
            }
        }

        // Meter
        engine.meterValue = ((engine.statistics.score % 10) * receiver.getMeterMax(engine)) / 9;
        engine.meterColor = GameEngine.METER_COLOR_GREEN;
        if (engine.statistics.score % 10 >= 4) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
        if (engine.statistics.score % 10 >= 6) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
        if (engine.statistics.score % 10 >= 8) engine.meterColor = GameEngine.METER_COLOR_RED;

        if ((engine.statistics.score >= tableGameClearLines[goaltype]) && (tableGameClearLines[goaltype] >= 0)) {
            // Ending
            engine.ending = 1;
            engine.gameEnded();
        } else if ((engine.statistics.score >= (engine.statistics.level + 1) * 5) && (engine.statistics.level < 19)) {
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
     * Soft drop
     */
    @Override
    public void afterSoftDropFall(GameEngine engine, int playerID, int fall) {
        return;
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


    // ------------------------------------------------------------------------------------------
    // Ranking/Setting Saving/Loading
    // ------------------------------------------------------------------------------------------

    /*
     * Called for initialization during "Ready" screen
     */
    @Override
    public void startGame(GameEngine engine, int playerID) {
        engine.statistics.level = startlevel;
        engine.statistics.levelDispAdd = 1;
        engine.b2bEnable = true;
        engine.tspinEnable = true;
        engine.useAllSpinBonus = true;
        engine.tspinAllowKick = true;
        engine.tspinEnableEZ = false;

        setSpeed(engine);

        if (netIsWatch) {
            owner.bgmStatus.bgm = BGMStatus.BGM_NOTHING;
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
        if ((owner.replayMode == false) && (big == false) && (engine.ai == null)) {
            updateRanking(engine.statistics.score, engine.statistics.time, goaltype);

            if (playerProperties.isLoggedIn()) {
                prop.setProperty("missionmode.playerName", playerProperties.getNameDisplay());
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
        goaltype = prop.getProperty("missionmode.gametype", 0);
        version = prop.getProperty("missionmode.version", 0);
    }


    /**
     * Save settings to property file
     *
     * @param prop Property file
     */
    private void saveSetting(CustomProperties prop) {
        prop.setProperty("missionmode.gametype", goaltype);
        prop.setProperty("missionmode.version", version);
    }

    /**
     * Load settings from property file
     *
     * @param prop Property file
     */
    private void loadSettingPlayer(ProfileProperties prop) {
        if (!prop.isLoggedIn()) return;
        goaltype = prop.getProperty("missionmode.gametype", 0);
    }


    /**
     * Save settings to property file
     *
     * @param prop Property file
     */
    private void saveSettingPlayer(ProfileProperties prop) {
        if (!prop.isLoggedIn()) return;
        prop.setProperty("missionmode.gametype", goaltype);
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
                rankingScore[j][i] = prop.getProperty("missionmode.ranking." + ruleName + "." + j + ".score." + i, 0);
                rankingTime[j][i] = prop.getProperty("missionmode.ranking." + ruleName + "." + j + ".time." + i, 0);
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
                prop.setProperty("missionmode.ranking." + ruleName + "." + j + ".score." + i, rankingScore[j][i]);
                prop.setProperty("missionmode.ranking." + ruleName + "." + j + ".time." + i, rankingTime[j][i]);
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
                rankingScorePlayer[j][i] = prop.getProperty("missionmode.ranking." + ruleName + "." + j + ".score." + i, 0);
                rankingTimePlayer[j][i] = prop.getProperty("missionmode.ranking." + ruleName + "." + j + ".time." + i, 0);
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
                prop.setProperty("missionmode.ranking." + ruleName + "." + j + ".score." + i, rankingScorePlayer[j][i]);
                prop.setProperty("missionmode.ranking." + ruleName + "." + j + ".time." + i, rankingTimePlayer[j][i]);
            }
        }
    }


    /**
     * Update rankings
     *
     * @param sc   Score
     * @param time Time
     */
    private void updateRanking(int sc, int time, int type) {
        rankingRank = checkRanking(sc, time, type);

        if (rankingRank != -1) {
            // Shift down ranking entries
            for (int i = RANKING_MAX - 1; i > rankingRank; i--) {
                rankingScore[type][i] = rankingScore[type][i - 1];
                rankingTime[type][i] = rankingTime[type][i - 1];
            }

            // Add new data
            rankingScore[type][rankingRank] = sc;
            rankingTime[type][rankingRank] = time;
        }

        if (playerProperties.isLoggedIn()) {
            rankingRankPlayer = checkRankingPlayer(sc, time, type);

            if (rankingRankPlayer != -1) {
                // Shift down ranking entries
                for (int i = RANKING_MAX - 1; i > rankingRankPlayer; i--) {
                    rankingScorePlayer[type][i] = rankingScorePlayer[type][i - 1];
                    rankingTimePlayer[type][i] = rankingTimePlayer[type][i - 1];
                }

                // Add new data
                rankingScorePlayer[type][rankingRankPlayer] = sc;
                rankingTimePlayer[type][rankingRankPlayer] = time;
            }
        }
    }


    /**
     * Calculate ranking position
     *
     * @param sc   Score
     * @param time Time
     * @return Position (-1 if unranked)
     */
    private int checkRanking(int sc, int time, int type) {
        for (int i = 0; i < RANKING_MAX; i++) {
            if (sc > rankingScore[type][i]) {
                return i;
            } else if ((sc == rankingScore[type][i]) && (time < rankingTime[type][i])) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Calculate ranking position
     *
     * @param sc   Score
     * @param time Time
     * @return Position (-1 if unranked)
     */
    private int checkRankingPlayer(int sc, int time, int type) {
        for (int i = 0; i < RANKING_MAX; i++) {
            if (sc > rankingScorePlayer[type][i]) {
                return i;
            } else if ((sc == rankingScorePlayer[type][i]) && (time < rankingTimePlayer[type][i])) {
                return i;
            }
        }

        return -1;
    }

    /*
     * Render results screen
     */
    @Override
    public void renderResult(GameEngine engine, int playerID) {
        drawResultStats(engine, playerID, receiver, 0, EventReceiver.COLOR_BLUE,
            STAT_SCORE, STAT_LEVEL, STAT_TIME, STAT_LPM);
        drawResultRank(engine, playerID, receiver, 8, EventReceiver.COLOR_BLUE, rankingRank);
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

    // ------------------------------------------------------------------------------------------
    // CRAPPY MISSION GENERATION METHODS >_<
    // ------------------------------------------------------------------------------------------

    /*
     * I don't even know how these even are supposed to integrate into the rest of the mode but we'll get there later.
     */


    private void generateNewMission(GameEngine engine, boolean lastMission) {
        boolean classic = engine.ruleopt.strRuleName.contains("CLASSIC");
        int missionTypeRd = missionRandomiser.nextInt(100);
        int missionType = 0;
        if (missionTypeRd < 40) {
            missionType = MISSION_CATEGORY_CLEAR_NONSPECIFIC;
        } else if (missionTypeRd < 60) {
            missionType = MISSION_CATEGORY_COMBO;
        } else if (missionTypeRd < 80) {
            missionType = MISSION_CATEGORY_SPIN_NONSPECIFIC;
        } else if (missionTypeRd < 90) {
            missionType = MISSION_CATEGORY_CLEAR;
        } else {
            missionType = MISSION_CATEGORY_SPIN;
        }
        missionCategory = missionType;
        missionIsComplete = false;

        switch (missionType) {
            case MISSION_CATEGORY_CLEAR:
                int pieceNum = missionRandomiser.nextInt(VALID_LINECLEAR_PIECES.length);
                String pieceName = VALID_LINECLEAR_PIECES[pieceNum];
                int bodyColour = missionColor(pieceName, classic);
                int lineClear = missionRandomiser.nextInt(classic ? VALID_CLASSIC_MAX_LINECLEARS[pieceNum] : VALID_MAX_LINECLEARS[pieceNum]) + 1;
                int amount = missionRandomiser.nextInt(MAX_MISSION_GOAL) + 1;

                missionGoal = amount;
                missionProgress = 0;
                lineAmount = lineClear;
                //isSpecific = true;
                //isSpinMission = false;
                specificPieceName = pieceName;
                spinDetectionType = 0;

                currentMissionText = generateSpecificLineMissionData(pieceName, bodyColour, lineClear, amount, lastMission);
                break;
            case MISSION_CATEGORY_CLEAR_NONSPECIFIC:
                int lineClear1 = missionRandomiser.nextInt(4) + 1;
                int amount1 = missionRandomiser.nextInt(MAX_MISSION_GOAL) + 1;

                missionGoal = amount1;
                missionProgress = 0;
                lineAmount = lineClear1;
                //isSpecific = false;
                //isSpinMission = false;
                specificPieceName = "NULL";
                spinDetectionType = 0;

                currentMissionText = generateLineMissionData(lineClear1, amount1, lastMission);
                break;
            case MISSION_CATEGORY_SPIN:
                int pieceNum2 = missionRandomiser.nextInt(VALID_SPIN_PIECES.length);
                String pieceName2 = VALID_SPIN_PIECES[pieceNum2];
                int bodyColour2 = missionColor(pieceName2, classic);
                int lineClear2 = missionRandomiser.nextInt(classic ? VALID_CLASSIC_SPIN_MAX_LINES[pieceNum2] : VALID_SPIN_MAX_LINES[pieceNum2]);
                int amount2 = missionRandomiser.nextInt(MAX_MISSION_GOAL) + 1;

                missionGoal = amount2;
                missionProgress = 0;
                lineAmount = lineClear2;
                //isSpecific = true;
                //isSpinMission = true;
                specificPieceName = pieceName2;
                spinDetectionType = missionRandomiser.nextInt(2);

                currentMissionText = generateSpinMissionData(pieceName2, bodyColour2, spinDetectionType, lineClear2, amount2, lastMission);
                break;
            case MISSION_CATEGORY_SPIN_NONSPECIFIC:
                int lineClear3 = missionRandomiser.nextInt(classic ? 2 : 3);
                int amount3 = missionRandomiser.nextInt(MAX_MISSION_GOAL) + 1;

                missionGoal = amount3;
                missionProgress = 0;
                lineAmount = lineClear3;
                //isSpecific = false;
                //isSpinMission = true;
                specificPieceName = "NULL";
                spinDetectionType = missionRandomiser.nextInt(2);

                currentMissionText = generateNonSpecificSpinMissionData(spinDetectionType, lineClear3, amount3, lastMission);
                break;
            case MISSION_CATEGORY_COMBO:
                int comboLimit = missionRandomiser.nextInt(MAX_MISSION_COMBO) + 3;

                missionGoal = comboLimit;
                missionProgress = 0;
                lineAmount = 0;
                //isSpecific = false;
                //isSpinMission = false;
                specificPieceName = "NULL";
                spinDetectionType = 0;

                currentMissionText = generateComboMissionData(comboLimit, lastMission);
                break;
        }

        engine.spinCheckType = spinDetectionType;
        engine.comboType = (missionType == MISSION_CATEGORY_COMBO) ? GameEngine.COMBO_TYPE_NORMAL : 0;
        engine.combo = 0;
        lastcombo = 0;
    }

    private int missionColor(String pieceName, boolean classic) {
        switch (pieceName) {
            case "I":
                return classic ? MISSION_I_CLASSIC : MISSION_I;
            case "J":
                return MISSION_J;
            case "L":
                return MISSION_L;
            case "O":
                return MISSION_O;
            case "S":
                return classic ? MISSION_S_CLASSIC : MISSION_S;
            case "T":
                return classic ? MISSION_T_CLASSIC : MISSION_T;
            case "Z":
                return classic ? MISSION_Z_CLASSIC : MISSION_Z;
            default:
                return MISSION_GENERIC;
        }
    }


    private LinkedHashMap<String, Integer> generateSpinMissionData(String pieceName, int bodyColour, int spinType, int spinLine, int amount, boolean lastMission) {
        String header = "PERFORM";
        String footer = lastMission ? "TO WIN!" : "TO ADVANCE!";
        String missionBody = amount + "X " + pieceName + SPIN_MISSION_TABLE[spinLine] + (amount > 1 ? "S" : "");

        LinkedHashMap<String, Integer> result = new LinkedHashMap<String, Integer>();

        result.put(header, MISSION_NEUTRAL);  // Key is text to draw, value is colour.
        result.put("[NEWLINE]", -1);  // Tells the text-drawing method to move one line down.
        result.put(missionBody, bodyColour);
        result.put("[NEWLINE1]", -1);
        result.put("USING THE ", MISSION_NEUTRAL);
        result.put("[NEWLINE3]", -1);
        result.put(spinType == 0 ? "4-POINT" : "IMMOBILE", MISSION_GENERIC);
        result.put(" SPIN TYPE", MISSION_NEUTRAL);
        result.put("[NEWLINE2]", -1);
        result.put(footer, MISSION_NEUTRAL);

        return result;
    }


    private LinkedHashMap<String, Integer> generateNonSpecificSpinMissionData(int spinType, int spinLine, int amount, boolean lastMission) {
        String header = "PERFORM";
        String footer = lastMission ? "TO WIN!" : "TO ADVANCE!";
        String missionBody = amount + "X " + NON_SPECIFIC_SPIN_MISSION_TABLE[spinLine] + (amount > 1 ? "S" : "");

        LinkedHashMap<String, Integer> result = new LinkedHashMap<String, Integer>();

        result.put(header, MISSION_NEUTRAL);  // Key is text to draw, value is colour.
        result.put("[NEWLINE]", -1);  // Tells the text-drawing method to move one line down.
        result.put(missionBody, MISSION_GENERIC);
        result.put("[NEWLINE1]", -1);
        result.put("USING THE ", MISSION_NEUTRAL);
        result.put("[NEWLINE3]", -1);
        result.put(spinType == 0 ? "4-POINT" : "IMMOBILE", MISSION_GENERIC);
        result.put(" SPIN TYPE", MISSION_NEUTRAL);
        result.put("[NEWLINE2]", -1);
        result.put(footer, MISSION_NEUTRAL);

        return result;
    }


    private LinkedHashMap<String, Integer> generateLineMissionData(int lineClear, int amount, boolean lastMission) {
        String header = "PERFORM";
        String footer = lastMission ? "TO WIN!" : "TO ADVANCE!";
        String missionBody = amount + "X " + LINECLEAR_MISSION_TABLE[lineClear - 1] + (amount > 1 ? (lineClear != 4 ? "S" : "ES") : "");

        LinkedHashMap<String, Integer> result = new LinkedHashMap<String, Integer>();

        result.put(header, MISSION_NEUTRAL);
        result.put("[NEWLINE]", -1);
        result.put(missionBody, MISSION_GENERIC);
        result.put("[NEWLINE1]", -1);
        result.put(footer, MISSION_NEUTRAL);

        return result;
    }


    private LinkedHashMap<String, Integer> generateSpecificLineMissionData(String pieceName, int bodyColour, int lineClear, int amount, boolean lastMission) {
        String header = "PERFORM";
        String footer = lastMission ? "TO WIN!" : "TO ADVANCE!";
        String missionBody = amount + "X " + LINECLEAR_MISSION_TABLE[lineClear - 1] + (amount > 1 ? (lineClear != 4 ? "S" : "ES") : "");

        LinkedHashMap<String, Integer> result = new LinkedHashMap<String, Integer>();

        result.put(header, MISSION_NEUTRAL);
        result.put("[NEWLINE]", -1);
        result.put(missionBody, MISSION_GENERIC);
        result.put("[NEWLINE1]", -1);
        result.put("USING ", MISSION_NEUTRAL);
        result.put(pieceName + "-PIECES", bodyColour);
        result.put("[NEWLINE2]", -1);
        result.put(footer, MISSION_NEUTRAL);

        return result;
    }


    private LinkedHashMap<String, Integer> generateComboMissionData(int maxCombo, boolean lastMission) {
        String header = "PERFORM";
        String footer = lastMission ? "TO WIN!" : "TO ADVANCE!";
        String missionBody = "A " + maxCombo + "X " + "COMBO";

        LinkedHashMap<String, Integer> result = new LinkedHashMap<String, Integer>();

        result.put(header, MISSION_NEUTRAL);
        result.put("[NEWLINE]", -1);
        result.put(missionBody, MISSION_GENERIC);
        result.put("[NEWLINE1]", -1);
        result.put(footer, MISSION_NEUTRAL);

        return result;
    }


    /**
     * Draws the mission texts to a location.
     *
     * @param engine       GameEngine to draw with.
     * @param playerID     Player to draw next to (0 = 1P).
     * @param missionData  LinkedHashMap containing mission text and colour data.
     * @param destinationX X of destination (uses drawScoreFont(...)).
     * @param destinationY Y of destination (uses drawScoreFont(...)).
     * @param scale        Text scale (0.5f, 1.0f, 2.0f).
     */
    private void drawMissionStrings(GameEngine engine, int playerID, LinkedHashMap<String, Integer> missionData, int destinationX, int destinationY, float scale) {
        int counterX = 0, counterY = 0;
        for (String i : currentMissionText.keySet()) {
            if (currentMissionText.get(i) != -1) {
                receiver.drawScoreFont(engine, playerID, destinationX + counterX, destinationY + counterY, i, currentMissionText.get(i), scale);
                counterX += i.length();
            } else {
                counterX = 0;
                counterY++;
            }
        }
    }
} 
