package zeroxfc.nullpo.custom.modes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.util.CustomProperties;
import org.apache.log4j.Logger;
import zeroxfc.nullpo.custom.libs.FieldManipulation;

public class DrawMode extends MarathonModeBase {
    /**
     * Debug logger
     */
    private static final Logger log = Logger.getLogger(DrawMode.class);

    /**
     * This mode always has 0G gravity
     */
    private static final int GRAVITY_CONSTANT = 0;

    /**
     * Time limit for placements per level:
     * Score starts decreasing when time is expired at a rate of
     * 8 * (level + 1) / frame until next placement
     */
    private static final int[] tablePieceTimeLimit = { 1800, 1680, 1620, 1560, 1500, 1440, 1380, 1320, 1260, 1200, 1140, 1080, 1020, 960, 900, 840, 780, 720, 660, 600 };

    /**
     * Line counts when game ending occurs
     */
    private static final int[] tableGameClearLines = { 50, 100, 150, 200, -1 };

    /**
     * Number of entries in rankings
     */
    private static final int RANKING_MAX = 10;

    /**
     * Number of ranking types
     */
    private static final int RANKING_TYPE = 5;

    /**
     * Number of game types
     */
    private static final int GAMETYPE_MAX = 5;

    /**
     * Maximum drawable squares in draw mode
     */
    private static final int MAX_SQUARES = 4;

    /**
     * Blocks for placement in field
     */
    private static final Block BLOCK_FILLED = new Block(Block.BLOCK_COLOR_GRAY, 0, Block.BLOCK_ATTRIBUTE_VISIBLE),
        BLOCK_EMPTY;
    private static final int[][][] SHAPES = {
        //region Shape Outlines
        new int[][] {
            new int[] { 1, 1, 1, 1 }
        },
        new int[][] {
            new int[] { 1 },
            new int[] { 1 },
            new int[] { 1 },
            new int[] { 1 }
        },  // I-PIECE
        new int[][] {
            new int[] { 0, 0, 1 },
            new int[] { 1, 1, 1 },
        },
        new int[][] {
            new int[] { 1, 0 },
            new int[] { 1, 0 },
            new int[] { 1, 1 }
        },
        new int[][] {
            new int[] { 1, 1, 1 },
            new int[] { 1, 0, 0 },
        },
        new int[][] {
            new int[] { 1, 1 },
            new int[] { 0, 1 },
            new int[] { 0, 1 }
        },  // L-PIECE
        new int[][] {
            new int[] { 1, 1 },
            new int[] { 1, 1 }
        },  // O-PIECE
        new int[][] {
            new int[] { 1, 1, 0 },
            new int[] { 0, 1, 1 },
        },
        new int[][] {
            new int[] { 0, 1 },
            new int[] { 1, 1 },
            new int[] { 1, 0 }
        },  // Z-PIECE
        new int[][] {
            new int[] { 0, 1, 0 },
            new int[] { 1, 1, 1 },
        },
        new int[][] {
            new int[] { 1, 0 },
            new int[] { 1, 1 },
            new int[] { 1, 0 }
        },
        new int[][] {
            new int[] { 1, 1, 1 },
            new int[] { 0, 1, 0 },
        },
        new int[][] {
            new int[] { 0, 1 },
            new int[] { 1, 1 },
            new int[] { 0, 1 }
        },  // T-PIECE
        new int[][] {
            new int[] { 1, 0, 0 },
            new int[] { 1, 1, 1 },
        },
        new int[][] {
            new int[] { 1, 1 },
            new int[] { 1, 0 },
            new int[] { 1, 0 }
        },
        new int[][] {
            new int[] { 1, 1, 1 },
            new int[] { 0, 0, 1 },
        },
        new int[][] {
            new int[] { 0, 1 },
            new int[] { 0, 1 },
            new int[] { 1, 1 }
        },  // J-PIECE
        new int[][] {
            new int[] { 0, 1, 1 },
            new int[] { 1, 1, 0 },
        },
        new int[][] {
            new int[] { 1, 0 },
            new int[] { 1, 1 },
            new int[] { 0, 1 }
        }   // S-PIECE
        //endregion
    };

    // region SHAPES
    // Aww s#/t! Here we go again...
    private static final ArrayList<Field> SHAPE_FIELDS = new ArrayList<>();
    private static final int[] SHAPE_TO_PIECE_ID = {
        0, 0, 1, 1, 1, 1, 2, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 6, 6
    };
    private static final int[] SHAPE_TO_PIECE_DIRECTION = {
        0, 1, 0, 1, 2, 3, 0, 2, 1, 0, 1, 2, 3, 0, 1, 2, 3, 2, 3
    };

    static {
        BLOCK_EMPTY = new Block(BLOCK_FILLED);
        BLOCK_EMPTY.setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, false);
    }

//	private static final int[][] PIECE_ID_TO_OFFSET = {
//			null,
//			new int[] { 1, 1 },
//			null,
//
//	};

    static {
        for (int i = 0; i < SHAPES.length; i++) {
            int[][] shape = SHAPES[i];
            int dimX = shape[0].length;
            int dimY = shape.length;

            SHAPE_FIELDS.add(new Field(dimX, dimY, 0));

            for (int y = 0; y < dimY; y++) {
                for (int x = 0; x < dimX; x++) {
                    SHAPE_FIELDS.get(i).getBlock(x, y).copy(new Block(shape[y][x], 0));
                }
            }
        }
    }

    // endregion SHAPES

    /**
     * The match confidence of the field to each piece.
     */
    private ArrayList<Double> matchConfidences;

    /**
     * Default Pieces
     */
    private ArrayList<Piece> pieceSet;

    /**
     * Current location in draw field
     */
    private int locationX, locationY;

    /**
     * Current location of placement block
     */
    private int pieceStartX;

    /**
     * Drawing field:<br/>
     * Compare this to the shape fields (non-exact, non-colourmatch).
     */
    private Field drawField;

    /**
     * Best match values
     */
    private int currentMaxIndex;
    private int currentPieceID;
    private int[][] currentOffset;

    /**
     * Random place generator
     */
    private Random placementRandomiser;

    /**
     * Mode Name
     */
    @Override
    public String getName() {
        return "DRAW MODE";
    }

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

        matchConfidences = new ArrayList<>();

        pieceSet = new ArrayList<>();

        locationX = 0;
        locationY = 0;
        pieceStartX = 0;

        rankingRank = -1;
        rankingScore = new int[RANKING_TYPE][RANKING_MAX];
        rankingLines = new int[RANKING_TYPE][RANKING_MAX];
        rankingTime = new int[RANKING_TYPE][RANKING_MAX];

        placementRandomiser = null;

        netPlayerInit(engine, playerID);

        if (!owner.replayMode) {
            loadSetting(owner.modeConfig);
            loadRanking(owner.modeConfig, engine.ruleopt.strRuleName);
            version = BASE_VERSION;
        } else {
            loadSetting(owner.replayProp);
            if ((version == 0) && owner.replayProp.getProperty("drawMode.endless", false)) goaltype = 2;

            // NET: Load name
            netPlayerName = engine.owner.replayProp.getProperty(playerID + ".net.netPlayerName", "");
        }

        engine.owner.backgroundStatus.bg = startlevel;
        engine.framecolor = GameEngine.FRAME_COLOR_GREEN;
    }

    @Override
    public void onFirst(GameEngine engine, int playerID) {
        engine.holdPieceObject = null;
        engine.holdDisable = true;
    }

    private void generateNewPlacementX(GameEngine engine) {
        if (placementRandomiser != null) {
            pieceStartX = placementRandomiser.nextInt(engine.field.getWidth());

            int widthExtraLeft = 3;
            int widthExtraRight = 3;

            if (pieceStartX < 3) widthExtraLeft = pieceStartX;
            if (pieceStartX >= engine.field.getWidth() - 3)
                widthExtraLeft = engine.field.getWidth() - pieceStartX - 1;

            int height = 7;
            if (engine.field.getHeight() < 4 || engine.field.getHiddenHeight() < 3) {
                // Field size is too small, log error into log file and end game.
                engine.gameEnded();
                engine.stat = GameEngine.STAT_GAMEOVER;

                log.error("The field is not large enough to play this mode.\nPlease use a field with height >= 4 and hidden height >= 3.", new IllegalArgumentException("Field size is too small."));

                engine.resetStatc();
            }
        }
    }

    @Override
    public boolean onReady(GameEngine engine, int playerID) {
        // 横溜め
        if (engine.ruleopt.dasInReady && engine.gameActive) engine.padRepeat();
        else if (engine.ruleopt.dasRedirectInDelay) {
            engine.dasRedirect();
        }

        // Initialization
        if (engine.statc[0] == 0) {
            // fieldInitialization
            BLOCK_FILLED.skin = engine.getSkin();
            BLOCK_EMPTY.skin = engine.getSkin();

            engine.ruleopt.fieldWidth = 8;
            engine.ruleopt.fieldHeight = 8;
            engine.ruleopt.fieldHiddenHeight = 0;
            engine.displaysize = 1;

            engine.ruleopt.nextDisplay = 0;
            engine.ruleopt.holdEnable = false;

            engine.fieldWidth = engine.ruleopt.fieldWidth;
            engine.fieldHeight = engine.ruleopt.fieldHeight;
            engine.fieldHiddenHeight = engine.ruleopt.fieldHiddenHeight;
            engine.field = new Field(engine.fieldWidth, engine.fieldHeight, engine.fieldHiddenHeight, true);

            placementRandomiser = new Random(engine.randSeed);
            generateNewPlacementX(engine);

            pieceSet.clear();

            // Generate new piece set from the stored data.
            for (int i = 0; i <= SHAPE_TO_PIECE_ID.length; i++) {
                pieceSet.add(new Piece(SHAPE_TO_PIECE_ID[i]));
                pieceSet.get(i).direction = SHAPE_TO_PIECE_DIRECTION[i];
                pieceSet.get(i).connectBlocks = engine.connectBlocks;
                pieceSet.get(i).setColor(engine.ruleopt.pieceColor[pieceSet.get(i).id]);
                pieceSet.get(i).setSkin(engine.getSkin());
                pieceSet.get(i).updateConnectData();
                pieceSet.get(i).setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
                pieceSet.get(i).setAttribute(Block.BLOCK_ATTRIBUTE_BONE, engine.bone);
            }

            if (!engine.readyDone) {
                //  button input状態リセット
                engine.ctrl.reset();
                // ゲーム中 flagON
                engine.gameActive = true;
                engine.gameStarted = true;
                engine.isInGame = true;
            }

            rankingRank = -1;
        }

        // READY音
        if (engine.statc[0] == engine.readyStart) engine.playSE("ready");

        // GO音
        if (engine.statc[0] == engine.goStart) engine.playSE("go");

        // 開始
        if (engine.statc[0] >= engine.goEnd) {
            if (!engine.readyDone) engine.owner.bgmStatus.bgm = 0;
            if (engine.owner.mode != null) engine.owner.mode.startGame(engine, playerID);
            engine.owner.receiver.startGame(engine, playerID);
            engine.stat = GameEngine.STAT_MOVE;
            // localState = 0;
            engine.timerActive = true;
            engine.resetStatc();
            if (!engine.readyDone) {
                engine.startTime = System.nanoTime();
                //startTime = System.nanoTime()/1000000L;
            }
            engine.readyDone = true;
            return true;
        }

        engine.statc[0]++;

        return true;
    }

    // region Setting and Ranking

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
            updateRanking(engine.statistics.score, engine.statistics.lines, engine.statistics.time, goaltype);

            if (rankingRank != -1) {
                saveRanking(owner.modeConfig, engine.ruleopt.strRuleName);
                receiver.saveModeConfig(owner.modeConfig);
            }
        }
    }

    /**
     * Load settings from property file
     *
     * @param prop Property file
     */
    private void loadSetting(CustomProperties prop) {
        startlevel = prop.getProperty("drawMode.startlevel", 0);
        tspinEnableType = prop.getProperty("drawMode.tspinEnableType", 1);
        enableTSpin = prop.getProperty("drawMode.enableTSpin", true);
        enableTSpinKick = prop.getProperty("drawMode.enableTSpinKick", true);
        spinCheckType = prop.getProperty("drawMode.spinCheckType", 0);
        tspinEnableEZ = prop.getProperty("drawMode.tspinEnableEZ", false);
        enableB2B = prop.getProperty("drawMode.enableB2B", true);
        enableCombo = prop.getProperty("drawMode.enableCombo", true);
        goaltype = prop.getProperty("drawMode.gametype", 0);
        big = prop.getProperty("drawMode.big", false);
        version = prop.getProperty("drawMode.version", 0);
    }

    /**
     * Save settings to property file
     *
     * @param prop Property file
     */
    private void saveSetting(CustomProperties prop) {
        prop.setProperty("drawMode.startlevel", startlevel);
        prop.setProperty("drawMode.tspinEnableType", tspinEnableType);
        prop.setProperty("drawMode.enableTSpin", enableTSpin);
        prop.setProperty("drawMode.enableTSpinKick", enableTSpinKick);
        prop.setProperty("drawMode.spinCheckType", spinCheckType);
        prop.setProperty("drawMode.tspinEnableEZ", tspinEnableEZ);
        prop.setProperty("drawMode.enableB2B", enableB2B);
        prop.setProperty("drawMode.enableCombo", enableCombo);
        prop.setProperty("drawMode.gametype", goaltype);
        prop.setProperty("drawMode.big", big);
        prop.setProperty("drawMode.version", version);
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
                rankingScore[j][i] = prop.getProperty("drawMode.ranking." + ruleName + "." + j + ".score." + i, 0);
                rankingLines[j][i] = prop.getProperty("drawMode.ranking." + ruleName + "." + j + ".lines." + i, 0);
                rankingTime[j][i] = prop.getProperty("drawMode.ranking." + ruleName + "." + j + ".time." + i, 0);
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
                prop.setProperty("drawMode.ranking." + ruleName + "." + j + ".score." + i, rankingScore[j][i]);
                prop.setProperty("drawMode.ranking." + ruleName + "." + j + ".lines." + i, rankingLines[j][i]);
                prop.setProperty("drawMode.ranking." + ruleName + "." + j + ".time." + i, rankingTime[j][i]);
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

    // endregion

    /**
     * Parse matches and set closest match values.
     *
     * @param engine
     */
    private void parseMatches(GameEngine engine) {
        matchConfidences.clear();
        int index = 0;
        for (Field field : SHAPE_FIELDS) {
            // log.debug("INDEX " + index + ":");
            matchConfidences.add(FieldManipulation.fieldCompare(engine.field, field));
            index++;
        }

        double currentMaxMatchValue = Collections.max(matchConfidences);
        currentMaxIndex = matchConfidences.indexOf(currentMaxMatchValue);
        currentPieceID = SHAPE_TO_PIECE_ID[currentMaxIndex];
    }
}
