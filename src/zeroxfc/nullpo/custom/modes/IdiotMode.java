/*
 * Idiot% Mode - 0xFC963F18DC21
 * 
 * Speed Mania 1/2... but with far more spins.
 */
package zeroxfc.nullpo.custom.modes;

import mu.nu.nullpo.game.component.BGMStatus;
import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.util.CustomProperties;
import mu.nu.nullpo.util.GeneralUtil;
import mu.nu.nullpo.game.subsystem.mode.DummyMode;
import zeroxfc.nullpo.custom.libs.ArrayRandomiser;
import zeroxfc.nullpo.custom.libs.ProfileProperties;
import zeroxfc.nullpo.custom.libs.RendererExtension;
import zeroxfc.nullpo.custom.libs.ScrollingMarqueeText;

import java.util.ArrayList;

public class IdiotMode extends DummyMode {
	/*
	 * DONE:
	 * 
	 * !important
	 * - Implement gravity that isn't 20g.
	 * 
	 * - Implement both 4-point and Immobile spin.
	 * 
	 * - Level counter does this:
	 *     - Mini/EZ Spin Single: 1
	 *     - Spin Single: 2
	 *     - Mini/EZ Spin Double: 3
	 *     - Spin Double: 4
	 *     - Spin Triple: 6
	 *     
	 *   Level stop requires spin clear.
	 * 
	 * - Spin chain: gain up to 3 bonus levels per B2B spin clear following this table
	 *     CHAIN|BONUS
	 *     -----------
	 *       0  |  0
	 *       1  |  0
	 *       2  |  1
	 *       3  |  1
	 *       4  |  2
	 *       5  |  2
	 *      6+  |  3
	 *      
	 *   B2B is counted for spin clears only, not for line clears.
	 *   
	 * - Skims: allow reduced line clear level advances (not past a level stop)
	 *     - SINGLE = 1
	 *     - DOUBLE = 1
	 *     - TRIPLE = 1
	 *     - FOUR   = 2
	 *     
	 * - Maybe more speed tables later?
	 * 
	 * - SECRET GRADE TO BE LABELLED AS "HIDDEN IDIOT"!
	 * 
	 * - Hidden difficulty called "DEVILSPIN"
	 */
	
	/*
	 * MISC:
	 * 
	 * There are two value tables for each game setting.
	 * tableName[0] is the table for Speed Mania.
	 * tableName[1] is the table for Speed Mania 2.
	 * Normal line clears increase score only by a fixed amount.
	 * Spin line clears use a different formula.
	 */
	
	// --------------------------------------------------------------------------------------
	
	/*
	 * [--- CONST BLOCK ---]
	 */
	
	// Current version
	private static final int CURRENT_VERSION = 2;
	
	// Default M1 torikan time. Same for all rule styles.
	private static final int[] DEFAULT_TORIKAN = { 14400, 12300, 25200 };
	
	// ARE Tables
	private static final int[][] tableARE = {
			{ 15, 11, 11, 5, 4, 3 },
			{ 8,  8,  8,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2 },
			{ 28 },
			{ 23, 23, 23, 23, 23, 23, 23, 14, 10, 10 },
			{ 23 }
	};
	
	// Line ARE Tables
	private static final int[][] tableARELine = {
			{ 11,  5,  5,  4,  4,  3 },
			{ 4,  3,  2,  2,  1,  1,  1,  1,  1,  1,  1,  1,  1,  2 },
			{ 28 },
			{ 23, 23, 23, 23, 23, 23, 14, 10,  4,  4 },
			{ 23 }
	};
	
	// Line Delay Tables
	private static final int[][] tableLineDelay = {
			{ 12,  6,  6,  7,  5,  4 },
			{ 6,  5,  4,  4,  3,  3,  3,  3,  3,  3,  3,  3,  3,  6 },
			{ 41 },
			{ 40, 40, 40, 40, 40, 25, 16, 12,  6,  6 },
			{ 40 }
	};
	
	// Lock Delay Tables
	private static final int[][] tableLockDelay = {
			{ 31, 27, 23, 19, 16, 16 },
			{ 19, 19, 18, 16, 16, 14, 13, 13, 13, 13, 13, 11,  9, 16 },
			{ 31 },
			{ 31, 31, 31, 31, 31, 31, 31, 31, 31, 18 },
			{ 31 }
	};
	
	// DAS Tables
	private static final int[][] tableDAS = {
			{ 11, 11, 10,  9,  7,  7 },
			{ 9,  7,  7,  7,  5,  5,  5,  5,  5,  5,  5,  5,  5,  5 }, 
			{ 15 },
			{ 15, 15, 15, 15, 15,  9,  9,  9,  9,  7 },
			{ 15 }
	};
	
	// Garbage Rise Tables
	private static final int[][] tableGarbage = {
			{ 0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
			{ 0,  0,  0,  0,  0, 20, 18, 10,  9,  8,  0,  0,  0,  0},
			{ 0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
			{ 0,  0,  0,  0,  0,  0,  0,  0,  0,  0 },
			{ 0,  0,  0,  0 }
	};

	// REGRET Time
	private static final int[][] tableTimeRegret = {
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
			{ 4200, 4200, 3600, 3600, 3600, 3600, 3600, 3600, 3600, 3600, 3600, 3600, 3600, 3600 },
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
			{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
			{ -1, -1, -1, -1 }
	};
	
	// BGM Fade-out Tables
	private static final int[][] tableBGMFadeout = {
			{ 280, 480, -1 },
			{ 485, 685, 985, -1 },
			{ 480, -1 },
			{ 480, 680, 880, -1 },
			{ -1 }
	};
	
	// BGM Change Tables
	private static final int[][] tableBGMChange = {
			{ 300, 500, -1 },
			{ 500, 700, 1000, -1 },
			{ 500, -1 }, 
			{ 500, 700, 900, -1 },
			{ -1 }
	};
	
	// BGM Tables
	private static final int[][] tableBGMNumber = {
			{ BGMStatus.BGM_NORMAL2, BGMStatus.BGM_NORMAL3, BGMStatus.BGM_NORMAL4 },
			{ BGMStatus.BGM_NORMAL3, BGMStatus.BGM_NORMAL4, BGMStatus.BGM_NORMAL5, BGMStatus.BGM_NORMAL6 },
			{ BGMStatus.BGM_NORMAL1, BGMStatus.BGM_NORMAL2 },
			{ BGMStatus.BGM_NORMAL1, BGMStatus.BGM_NORMAL2, BGMStatus.BGM_NORMAL3, BGMStatus.BGM_NORMAL4 },
			{ BGMStatus.BGM_NORMAL1 }
	};
	
	// Short Grade Names
	private static final String[][] tableShortGradeName = {
			{ "SO", "BCELL", "IDIOT" },
			{ "SO", "AVG.", "<AVG.", "BCELL", "F-G", "IDIOT", "ACHE", "OB", "FO", "EH", "VM", "UM", "NULL", "T.ID." },
			{ "SO", "DOLT", "IDIOT" },
			{ "SO", "DW", "IDIOT" },
			{ "N/A", "AVG." }
	};
	
	// Grade Names
	private static final String[][] tableGradeName = {
			{ "SPUN OUT", "BRAINCELL", "IDIOT" },
			{ "SPUN OUT", "AVERAGE", "SUB-AVERAGE", "BRAINCELL", "F-GRADE", "IDIOT", "HEADACHED", "OBLIVIOUS", "FATALLY OBLIVIOUS", "EMPTY-HEADED", "VOID-MIND", "UNINITIALISED-MIND", "NULL-MIND", "TRUE IDIOT" },
			{ "SPUN OUT", "DOLT", "IDIOT" },
			{ "SPUN OUT", "DIMWIT", "IDIOT" },
			{ "UNRANKED", "AVERAGE" }
	};
	
	// Polite Short Grade Names
	private static final String[][] tableShortPoliteGradeName = {
			{ "SO", "S", "GS" },
			{ "1", "S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8", "S9", "S10", "S11", "S12", "S13" },
			{ "ZERO", "SPUN", "SP" },
			{ "ZERO", "SPUN", "SP" },
			{ "FAIL", "PASS" }
	};
		
	// Polite Grade Names
	private static final String[][] tablePoliteGradeName = {
			{ "SPUN OUT", "SPINNER", "GRAND SPINNER" },
			{ "1", "SPINNER 1", "SPINNER 2", "SPINNER 3", "SPINNER 4", "SPINNER 5", "SPINNER 6", "SPINNER 7", "SPINNER 8", "SPINNER 9", "SPINNER 10", "SPINNER 11", "SPINNER 12", "SPINNER 13" },
			{ "ZERO", "SPUN", "SPINNER" },
			{ "ZERO", "SPUN", "SPINNER" },
			{ "FAIL", "PASS" }
	};
	
	// Secret Grade Names
	private static final String[] tableSecretGradeName = {
		 "0%",  "12.5%",  "25%",  "37.5%",  "50%",  "62.5%",  "75%",  "87.5%",  "100%",
		"112.5%", "125%", "137.5%", "150%", "162.5%", "175%", "187.5%", "200%", "300%",	
		"420%"													
	};
	
	// Credit Time Limits
	private static final int ROLLTIMELIMIT[] = { 1982, 3238, 2968, 3694, 1982 };
	
	// Max Rank
	private static final int RANKING_MAX = 10;
	
	// Sections per game type
	private static final int[] SECTION_MAX = { 10, 13, 10, 10, 3 };
	
	// levelmax
	private static final int[] LEVEL_MAX = { 999, 1300, 999, 999, 300 };
	
	// Default section time if it hasn't been beaten
	private static final int DEFAULT_SECTION_TIME = 3600;
	private static final int DEFAULT_SLOW_ST = 5400;
	
	// Game types
	private static final int GAMETYPE_DEATH = 0,
							 GAMETYPE_OMEN = 1,
							 GAMETYPE_MASTERY = 2,
							 GAMETYPE_ABSOLUTE = 3,
							 GAMETYPE_NORMAL = 4;

	// Number of game types
	private static final int GAMETYPE_MAX = 5;
	
	// Game Type Names
	private static final String[] GAMETYPE_NAMES = { "DEATH", "OMEN", "MASTERY", "ABSOLUTE", "SHORT" };
	
	// Spin chain bonus values, for use with PositiveClamp()
	private static final int[] SPIN_CHAIN_LEVELBONUS = { 0, 0, 1, 1, 2, 2, 3 };
	
	// Scoring event constants
	private static final int EVENT_NONE = 0,
                             EVENT_SINGLE = 1,
                             EVENT_DOUBLE = 2,
                             EVENT_TRIPLE = 3,
                             EVENT_FOUR = 4,
                             EVENT_TSPIN_ZERO_MINI = 5,
                             EVENT_TSPIN_ZERO = 6,
                             EVENT_TSPIN_SINGLE_MINI = 7,
                             EVENT_TSPIN_SINGLE = 8,
                             EVENT_TSPIN_DOUBLE_MINI = 9,
                             EVENT_TSPIN_DOUBLE = 10,
                             EVENT_TSPIN_TRIPLE = 11,
                             EVENT_TSPIN_EZ = 12;
	
	// Spin alg. names
	private static final String[] SPIN_BONUS_NAMES = {
			"NONE", "T-ONLY", "ALL"
	};
	
	private static final String[] SPIN_CHECK_NAMES = {
			"4-POINT", "IMMOBILE"
	};
	
	// Gravity value table
	private static final int[] tableGravityValue =
	{
		4, 6, 8, 10, 12, 16, 32, 48, 64, 80,
		96, 112, 128, 144, 4, 32, 64, 96, 128, 160,
		192, 224, 256, 512, 768, 1024, 1280, 1024, 768, -1
	};

	// Levels for gravity change
	private static final int[] tableGravityChangeLevel =
	{
		30, 35, 40, 50, 60, 70, 80, 90, 100, 120,
		140, 160, 170, 200, 220, 230, 233, 236, 239, 243,
		247, 251, 300, 330, 360, 400, 420, 450, 500, 10000
	};
	
	// Credit headings
	private static final String[] CREDIT_HEADINGS = {
		"THIS MODE IS DEDICATED TO",
		"MODE CREATED BY",
		"SPECIAL THANKS GOES TO",
		"CONGRATULATIONS AND THANKS ALSO GOES TO"
	};
			
	// Credit texts
	private static final String[] CREDIT_TEXTS = {
		"AKARI AND HER VIDEO, \"SHIRASE IDIOT%\".",
		"0XFC963F18DC21.",
		"AKARI, OSHISAURE, RY00001, GLITCHYPSI AND THE DRAGON GOD NERROTH.",
		"YOU, FOR ENDURING AND REACHING THE ENDING!"
	};
	
	// Credit headings
	private static final String[] SHORT_CREDIT_HEADINGS = {
		"THANK YOU",
		"SINCE YOU ARE STILL HERE,"
	};
				
	// Credit texts
	private static final String[] SHORT_CREDIT_TEXTS = {
		"FOR TRYING IDIOT% MODE!",
		"NOW TRY AGAIN TO PREVAIL AT A HARDER DIFFICULTY!"
	};
	
	/*
	 * [--- VAR BLOCK ---]
	 */
	
	private ScrollingMarqueeText creditObjectDefault;  // Default credits
	private ScrollingMarqueeText creditObjectShort;    // Short credits
	private GameManager owner;        // Current owning GameManager
	private EventReceiver receiver;   // In-game event receiver
	private int nextSectionLv;        // Level number of next section
	private boolean levelUpFlag;      // Flag set if levelled up (?)
	private boolean condescension;    // Use condescending grade names?
	private int gameType;             // Which game type?
	private int grade;                // Current grade value
	private int gradeFlash;           // Frames of flashing for grade
	private int spinChain;            // Current chain value
	private int lastScore;            // Most recent score increase
	private int scoreGetTime;         // Time of the obtaining of the most recent grade (?)
	private int rollTime;             // Remaining roll time
	private boolean rollStarted;      // Has end roll started?
	private int rollCleared;          // Has end roll been cleared?
	private int garbageCount;         // Garbage counter
	private int regretDispFrame;      // How long to display REGRET!! message
	private int secretGrade;          // Tier of > shape.
	private int currentBGM;           // What BGM is playing?
	private int[] sectionTime;        // Section times
	private boolean[] isSectionPB;    // Was the time a new record?
	private int sectionCount;         // Number of cleared sections
	private int avgSectionTime;       // Average section time
	private int lastSectionTime;      // Previous section time
	private int medalAC;              // AllClear medal status
	private int medalST;              // SectionTime medal status
	private int medalSK;              // SKill medal status
	private int medalSC;              // SpinChain medal status
	private boolean showBestSTime;    // Should game show best section time?
	private int levelAtStart;         // What level to start on
	private boolean lvStopSE;         // Has lvStopSE been triggered?
	private int[] torikanTime;        // Time limit to lv500
	private boolean showST;           // Show ST list in-game?
	private boolean showGrade;        // Show grade in-game?
	private int version;              // Current mode version
	private int currentGameRank;      // Current attempt's rank
	private int[][] rankingGrade;     // Grade rankings
	private int[][] rankingLevel;     // Level rankings
	private int[][] rankingTime;      // Time rankings
	private int[][] rollClear;        // Has roll been cleared?
	private int[][] sectionBests;     // Best section times
	private int tspinEnableType;      // Which? 0 = disabled, 1 = t, 2 = all
	private boolean enableTSpinKick;  // Allow kick-spin?
	private int spinCheckType;        // Which? 0 = 4P (?), 1 = imm.
	private boolean tspinEnableEZ;    // Allow immobile EZ?
	private int lastevent;            // Last scoring event.
	private int lastpiece;            // Piece placed last.
	private int spins;                // Number of spins.
	private boolean shouldPlayLSSE;   // should it play the level stop SE?
	private boolean daredevil;        // Daredevil mode?
	private boolean torikaned;        // Was the player stopped?

	/** The good hard drop effect */
	private ArrayList<int[]> pCoordList;
	private Piece cPiece;

	private static final int headerColour = EventReceiver.COLOR_PURPLE;
	private ProfileProperties playerProperties;
	private String PLAYER_NAME;
	private boolean showPlayerStats;
	private int currentGameRankPlayer;      // Current attempt's rank
	private int[][] rankingGradePlayer;     // Grade rankings
	private int[][] rankingLevelPlayer;     // Level rankings
	private int[][] rankingTimePlayer;      // Time rankings
	private int[][] rollClearPlayer;        // Has roll been cleared?

	/*
	 * [--- OVERRIDE METHOD BLOCK ---]
	 */
	
	// Mode Name
	@Override
	public String getName() {
		return "IDIOT% MODE";
	}
	
	// Initialisation
	@Override
	public void playerInit(GameEngine engine, int playerID) {
		owner = engine.owner;
		receiver = engine.owner.receiver;

		pCoordList = new ArrayList<>();
		cPiece = null;
		
		creditObjectDefault = new ScrollingMarqueeText(CREDIT_HEADINGS, CREDIT_TEXTS, EventReceiver.COLOR_ORANGE, EventReceiver.COLOR_WHITE);
		creditObjectShort = new ScrollingMarqueeText(SHORT_CREDIT_HEADINGS, SHORT_CREDIT_TEXTS, EventReceiver.COLOR_ORANGE, EventReceiver.COLOR_WHITE);
		
		shouldPlayLSSE = false;
		condescension = true;  // VERY IMPORTANT.
		
		daredevil = false;
		
		tspinEnableType = 0;     
		enableTSpinKick = false; 
		spinCheckType = 0;       
		tspinEnableEZ = false;   
		lastevent = 0;           
		lastpiece = 0;           
		spins = 0;      
		
		nextSectionLv = 0;
		levelUpFlag = true;
		grade = 0;
		gradeFlash = 0;
		gameType = 0;
		spinChain = 1;
		lastScore = 0;
		scoreGetTime = 0;
		torikaned = false;
		
		rollTime = 0;
		rollStarted = false;
		rollCleared = 0;
		
		garbageCount = 0;
		regretDispFrame = 0;
		secretGrade = 0;
		
		currentBGM = 0;
		
		sectionTime = new int[SECTION_MAX[gameType]];
		isSectionPB = new boolean[SECTION_MAX[gameType]];
		sectionCount = 0;
		avgSectionTime = 0;
		lastSectionTime = 0;
		
		medalAC = 0;
		medalSC = 0;
		medalSK = 0;
		medalST = 0;
		
		showBestSTime = false;
		levelAtStart = 0;
		lvStopSE = false;
		torikanTime = new int[] { DEFAULT_TORIKAN[0], DEFAULT_TORIKAN[1], DEFAULT_TORIKAN[2], DEFAULT_TORIKAN[2], 0 };
		showST = false;
		showGrade = false;
		
		currentGameRank = -1;
		
		rankingGrade = new int[GAMETYPE_MAX][RANKING_MAX];
		rankingLevel = new int[GAMETYPE_MAX][RANKING_MAX];
		rankingTime = new int[GAMETYPE_MAX][RANKING_MAX];
		rollClear = new int[GAMETYPE_MAX][RANKING_MAX];

		if (playerProperties == null) {
			playerProperties = new ProfileProperties(headerColour);

			showPlayerStats = false;
		}

		currentGameRankPlayer = -1;
		rankingGradePlayer = new int[GAMETYPE_MAX][RANKING_MAX];
		rankingLevelPlayer = new int[GAMETYPE_MAX][RANKING_MAX];
		rankingTimePlayer = new int[GAMETYPE_MAX][RANKING_MAX];
		rollClearPlayer = new int[GAMETYPE_MAX][RANKING_MAX];
		
		sectionBests = new int[GAMETYPE_MAX][];
		sectionBests[0] = new int[SECTION_MAX[0]];
		sectionBests[1] = new int[SECTION_MAX[1]];
		sectionBests[2] = new int[SECTION_MAX[2]];
		sectionBests[3] = new int[SECTION_MAX[3]];
		sectionBests[4] = new int[SECTION_MAX[4]];
		
		// Engine options
		engine.tspinEnable = true;
		engine.b2bEnable = false;
		engine.bighalf = false;
		engine.bigmove = false;
		engine.staffrollEnable = true;
		engine.staffrollNoDeath = false;
		
		// Is replay?
		if(owner.replayMode == false) {
			version = CURRENT_VERSION;
			loadSetting(owner.modeConfig, engine.ruleopt.strRuleName);
			loadRanking(owner.modeConfig, engine.ruleopt.strRuleName);

			if (playerProperties.isLoggedIn()) {
				loadSettingPlayer(playerProperties, engine.ruleopt.strRuleName);
				loadRankingPlayer(playerProperties, engine.ruleopt.strRuleName);
			}

			PLAYER_NAME = "";
			
			switch (gameType) {
			case GAMETYPE_DEATH:
			case GAMETYPE_OMEN:
				engine.framecolor = GameEngine.FRAME_COLOR_RED;
				break;
			case GAMETYPE_MASTERY:
			case GAMETYPE_ABSOLUTE:
				engine.framecolor = GameEngine.FRAME_COLOR_BLUE;
				break;
			case GAMETYPE_NORMAL:
				engine.framecolor = GameEngine.FRAME_COLOR_GREEN;
				break;
			}
		} else {
			version = owner.replayProp.getProperty("idiotmode.version", 0);
			for (int j = 0; j < GAMETYPE_MAX; j++) {
				for(int i = 0; i < SECTION_MAX[j]; i++) {
					switch (gameType) {
					case GAMETYPE_MASTERY:
					case GAMETYPE_NORMAL:
					case GAMETYPE_ABSOLUTE:
						sectionBests[j][i] = DEFAULT_SLOW_ST;
						break;
					default:
						sectionBests[j][i] = DEFAULT_SECTION_TIME;
						break;
					}
				}
			}

			PLAYER_NAME = owner.replayProp.getProperty("idiotmode.playerName", "");

			loadSetting(owner.replayProp, engine.ruleopt.strRuleName);

			switch (gameType) {
			case GAMETYPE_DEATH:
			case GAMETYPE_OMEN:
				engine.framecolor = GameEngine.FRAME_COLOR_RED;
				break;
			case GAMETYPE_MASTERY:
			case GAMETYPE_ABSOLUTE:
				engine.framecolor = GameEngine.FRAME_COLOR_BLUE;
				break;
			case GAMETYPE_NORMAL:
				engine.framecolor = GameEngine.FRAME_COLOR_GREEN;
				break;
			}
		}
		
		owner.backgroundStatus.bg = levelAtStart;
	}

	private void setStartBgmlv(GameEngine engine, int gt) {
		currentBGM = 0;
		while((tableBGMChange[gt][currentBGM] != -1) && (engine.statistics.level >= tableBGMChange[gt][currentBGM])) currentBGM++;
	}
	
	private void setSpeed(GameEngine engine, int gt) {
		int gravityindex = 0;
		switch (gt) {
		case GAMETYPE_MASTERY:
		case GAMETYPE_ABSOLUTE:
			if(engine.statistics.level >= 500) {
				engine.speed.gravity = -1;
			} else {
				while(engine.statistics.level >= tableGravityChangeLevel[gravityindex]) gravityindex++;
				engine.speed.gravity = tableGravityValue[gravityindex];
			}
			break;
		case GAMETYPE_NORMAL:
			if(engine.statistics.level >= 300) {
				engine.speed.gravity = -1;
			} else {
				while(engine.statistics.level >= tableGravityChangeLevel[gravityindex]) gravityindex++;
				engine.speed.gravity = tableGravityValue[gravityindex];
			}
			break;
		default:
			engine.speed.gravity = -1;
			break;
		}
		
		int section = engine.statistics.level / 100;
		if(section > tableARE[gt].length - 1) section = tableARE[gt].length - 1;
		engine.speed.are = tableARE[gt][section];
		engine.speed.areLine = tableARELine[gt][section];
		engine.speed.lineDelay = tableLineDelay[gt][section];
		engine.speed.lockDelay = tableLockDelay[gt][section];
		engine.speed.das = tableDAS[gt][section];	
	}
	
	private void setAverageSectionTime() {
		if(sectionCount > 0) {
			int temp = 0;
			for(int i = levelAtStart; i < levelAtStart + sectionCount; i++) {
				if((i >= 0) && (i < sectionTime.length)) temp += sectionTime[i];
			}
			avgSectionTime = temp / sectionCount;
		} else {
			avgSectionTime = 0;
		}
	}
	
	private void stMedalCheck(GameEngine engine, int gt, int sectionNumber) {
		int best = daredevil ? (((gameType == GAMETYPE_MASTERY) || (gameType == GAMETYPE_ABSOLUTE) || (gameType == GAMETYPE_NORMAL)) ? DEFAULT_SLOW_ST : DEFAULT_SECTION_TIME) : sectionBests[gt][sectionNumber];

		if(lastSectionTime < best) {
			if(medalST < 3) {
				engine.playSE("medal");
				medalST = 3;
			}
			if(!owner.replayMode) {
				isSectionPB[sectionNumber] = true;
			}
		} else if((lastSectionTime < best + 300) && (medalST < 2)) {
			engine.playSE("medal");
			medalST = 2;
		} else if((lastSectionTime < best + 600) && (medalST < 1)) {
			engine.playSE("medal");
			medalST = 1;
		}
	}
	
	private int getMedalFontColor(int medalColor) {
		if(medalColor == 1) return EventReceiver.COLOR_RED;
		if(medalColor == 2) return EventReceiver.COLOR_WHITE;
		if(medalColor == 3) return EventReceiver.COLOR_YELLOW;
		return -1;
	}
	
	// Called on settings screen, allows setting changes
	@Override
	public boolean onSetting(GameEngine engine, int playerID) {
		// Menu
		// engine.framecolor = GameEngine.FRAME_COLOR_GRAY;
		if(engine.owner.replayMode == false) {
			// Configuration changes
			int change = updateCursor(engine, 11);

			if(change != 0) {
				engine.playSE("change");

				switch(engine.statc[2]) {
				case 0:
					gameType += change;
					if (gameType < 0) gameType = GAMETYPE_MAX - 1;
					if (gameType >= GAMETYPE_MAX) gameType = 0;
					if(levelAtStart < 0) levelAtStart = SECTION_MAX[gameType] - 1;
					if(levelAtStart >= SECTION_MAX[gameType]) levelAtStart = 0;
					
					switch (gameType) {
					case GAMETYPE_DEATH:
					case GAMETYPE_OMEN:
						engine.framecolor = GameEngine.FRAME_COLOR_RED;
						break;
					case GAMETYPE_MASTERY:
					case GAMETYPE_ABSOLUTE:
						engine.framecolor = GameEngine.FRAME_COLOR_BLUE;
						break;
					case GAMETYPE_NORMAL:
						engine.framecolor = GameEngine.FRAME_COLOR_GREEN;
						break;
					}
					
					break;
				case 1:
					spinCheckType += change;
					if(spinCheckType < 0) spinCheckType = 1;
					if(spinCheckType > 1) spinCheckType = 0;
					break;
				case 2:
					tspinEnableType += change;
					if(tspinEnableType < 0) tspinEnableType = 2;
					if(tspinEnableType > 2) tspinEnableType = 0;
					break;
				case 3:
					enableTSpinKick = !enableTSpinKick;
					break;
				case 4:
					tspinEnableEZ = !tspinEnableEZ;
					break;
				case 5:
					levelAtStart += change;
					if(levelAtStart < 0) levelAtStart = SECTION_MAX[gameType] - 1;
					if(levelAtStart >= SECTION_MAX[gameType]) levelAtStart = 0;
					owner.backgroundStatus.bg = levelAtStart;
					break;
				case 6:
					lvStopSE = !lvStopSE;
					break;
				case 7:
					showST = !showST;
					break;
				case 8:
					torikanTime[gameType] += 60 * change;
					if(torikanTime[gameType] < 0) torikanTime[gameType] = 72000;
					if(torikanTime[gameType] > 72000) torikanTime[gameType] = 0;
					break;
				case 9:
					showGrade = !showGrade;
					break;
				case 10:
					condescension = !condescension;
					break;
				case 11:
					daredevil = !daredevil;
					break;
				}
			}

			engine.owner.backgroundStatus.bg = levelAtStart;

			//  section time display切替
			if(engine.ctrl.isPush(Controller.BUTTON_F) && (engine.statc[3] >= 5)) {
				engine.playSE("change");
				if (!daredevil) {
					showBestSTime = !showBestSTime;
				}
			}

			// 決定
			if(engine.ctrl.isPush(Controller.BUTTON_A) && (engine.statc[3] >= 5)) {
				engine.playSE("decide");
				if (playerProperties.isLoggedIn()) {
					saveSettingPlayer(playerProperties, engine.ruleopt.strRuleName);
					playerProperties.saveProfileConfig();
				} else {
					saveSetting(owner.modeConfig, engine.ruleopt.strRuleName);
					receiver.saveModeConfig(owner.modeConfig);
				}

				sectionCount = 0;

				return false;
			}

			// Cancel
			if(engine.ctrl.isPush(Controller.BUTTON_B)) {
				engine.quitflag = true;
				playerProperties = new ProfileProperties(headerColour);
			}

			// New acc
			if(engine.ctrl.isPush(Controller.BUTTON_E) && engine.ai == null) {
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

			if(engine.statc[3] >= 60) {
				return false;
			}
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
			loadSettingPlayer(playerProperties, engine.ruleopt.strRuleName);
		}

		if (engine.stat == GameEngine.STAT_SETTING) engine.isInGame = false;

		return s;
	}
	
	// Draw Settings screen
	@Override
	public void renderSetting(GameEngine engine, int playerID) {
		if(engine.statc[2] < 10) {
			drawMenu(engine, playerID, receiver, 0, EventReceiver.COLOR_RED, 0,
					"DIFFICULTY", GAMETYPE_NAMES[gameType]);
			drawMenu(engine, playerID, receiver, 2, EventReceiver.COLOR_GREEN, 1,
					"SPIN TYPE", SPIN_CHECK_NAMES[spinCheckType],
					"SPIN BONUS", SPIN_BONUS_NAMES[tspinEnableType],
					"KICK SPINS", GeneralUtil.getONorOFF(enableTSpinKick),
					"EZIMMOBILE", GeneralUtil.getONorOFF(tspinEnableEZ));
			drawMenu(engine, playerID, receiver, 10, EventReceiver.COLOR_BLUE, 5,
					"LEVEL", String.valueOf(levelAtStart * 100),
					"LVSTOPSE", GeneralUtil.getONorOFF(lvStopSE),
					"SHOW STIME", GeneralUtil.getONorOFF(showST),
					"LV500LIMIT", (torikanTime[gameType] == 0) ? "NONE" : GeneralUtil.getTime(torikanTime[gameType]),
					"GRADE DISP", GeneralUtil.getONorOFF(showGrade));
		} else {
			drawMenu(engine, playerID, receiver, 0, EventReceiver.COLOR_PINK, 10,
					"CRUDENESS", GeneralUtil.getONorOFF(condescension),
					"DEVILSPIN", GeneralUtil.getONorOFF(daredevil));
		}
	}
	
	// START GAME!!!!
	@Override
	public void startGame(GameEngine engine, int playerID) {
		//saveSetting(owner.modeConfig, engine.ruleopt.strRuleName);
		engine.statistics.level = levelAtStart * 100;
		sectionTime = new int[SECTION_MAX[gameType]];
		isSectionPB = new boolean[SECTION_MAX[gameType]];
		
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
		
		creditObjectDefault = new ScrollingMarqueeText(ch, ct, EventReceiver.COLOR_ORANGE, EventReceiver.COLOR_WHITE);
		creditObjectShort = new ScrollingMarqueeText(SHORT_CREDIT_HEADINGS, SHORT_CREDIT_TEXTS, EventReceiver.COLOR_ORANGE, EventReceiver.COLOR_WHITE);
		// torikaned = false;

		// shouldPlayLSSE = lvStopSE;
		
		nextSectionLv = engine.statistics.level + 100;
		if(engine.statistics.level < 0) nextSectionLv = 100;
		
		switch (gameType) {
		case GAMETYPE_DEATH:
		case GAMETYPE_ABSOLUTE:
		case GAMETYPE_MASTERY:
			if(engine.statistics.level >= 900) nextSectionLv = 999;
			break;
		case GAMETYPE_NORMAL:
			if(engine.statistics.level >= 300) nextSectionLv = 300;
			break;
		case GAMETYPE_OMEN:
			if(engine.statistics.level >= 1300) nextSectionLv = 1300;
			break;
		}
		
		if(engine.statistics.level >= 1000) engine.bone = true;
		
		// The all-important: S P I N S
		engine.tspinAllowKick = enableTSpinKick;
		if(tspinEnableType == 0) {
			engine.tspinEnable = false;
		} else if(tspinEnableType == 1) {
			engine.tspinEnable = true;
		} else {
			engine.tspinEnable = true;
			engine.useAllSpinBonus = true;
		}
		engine.spinCheckType = spinCheckType;
		engine.tspinEnableEZ = tspinEnableEZ;
		
		owner.backgroundStatus.bg = engine.statistics.level / 100;
		
		lastevent = 0;           
		lastpiece = 0;           
		spins = 0;
		sectionCount = 0;

		setSpeed(engine, gameType);
		setStartBgmlv(engine, gameType);
		owner.bgmStatus.bgm = tableBGMNumber[gameType][currentBGM];
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

				RendererExtension.addBlockBreakEffect(receiver, x2, y2, cPiece.block[i]);
			} else {
				int x2 = baseX + (cPiece.dataX[cPiece.direction][i] * 32);
				int y2 = baseY + (cPiece.dataY[cPiece.direction][i] * 32);

				RendererExtension.addBlockBreakEffect(receiver, x2, y2, cPiece.block[i]);
				RendererExtension.addBlockBreakEffect(receiver, x2+16, y2, cPiece.block[i]);
				RendererExtension.addBlockBreakEffect(receiver, x2, y2+16, cPiece.block[i]);
				RendererExtension.addBlockBreakEffect(receiver, x2+16, y2+16, cPiece.block[i]);
			}
		}
	}

	// Render score
	@Override
	public void renderLast(GameEngine engine, int playerID) {
		receiver.drawScoreFont(engine, playerID, 0, 0, "IDIOT% " + GAMETYPE_NAMES[gameType], EventReceiver.COLOR_RED);
		String modeType = "(";
		switch (tspinEnableType) {
		case 0:
			modeType = modeType.concat("NO SPIN");
			break;
		case 1:
			modeType = modeType.concat("T-SPIN");
			break;
		case 2:
			modeType = modeType.concat("ALL SPIN");
			break;
		}
		if (daredevil) {
			modeType = modeType.concat(" DEVIL'S GAME");
		} else {
			modeType = modeType.concat(" GAME");
		}
		modeType = modeType.concat(")");
		receiver.drawScoreFont(engine, playerID, 0, 1, modeType, EventReceiver.COLOR_RED);

		if( (engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (owner.replayMode == false)) ) {
			if((owner.replayMode == false) && (levelAtStart == 0) && (engine.ai == null)) {
				if (!daredevil) {
					if(!showBestSTime) {
						// Rankings
						float scale = (receiver.getNextDisplayType() == 2) ? 0.5f : 1.0f;
						int topY = (receiver.getNextDisplayType() == 2) ? 6 : 4;
						receiver.drawScoreFont(engine, playerID, 3, topY-1, "GRADE LEVEL TIME", EventReceiver.COLOR_BLUE, scale);

						if (showPlayerStats) {
							for(int i = 0; i < RANKING_MAX; i++) {
								int gcolor = EventReceiver.COLOR_WHITE;
								if(rollClearPlayer[gameType][i] == 1) gcolor = EventReceiver.COLOR_GREEN;
								if(rollClearPlayer[gameType][i] == 2) gcolor = EventReceiver.COLOR_ORANGE;

								receiver.drawScoreFont(engine, playerID, 0, topY+i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW, scale);
								receiver.drawScoreFont(engine, playerID, 3, topY+i, getShortGradeNames()[gameType][rankingGradePlayer[gameType][i]], gcolor, scale);
								receiver.drawScoreFont(engine, playerID, 9, topY+i, String.valueOf(rankingLevelPlayer[gameType][i]), (i == currentGameRankPlayer), scale);
								receiver.drawScoreFont(engine, playerID, 15, topY+i, GeneralUtil.getTime(rankingTimePlayer[gameType][i]), (i == currentGameRankPlayer), scale);
							}

							receiver.drawScoreFont(engine, playerID, 0, 17, "F:VIEW SECTION TIME", EventReceiver.COLOR_GREEN);
							receiver.drawScoreFont(engine, playerID, 0, 18, "PLAYER SCORES", EventReceiver.COLOR_BLUE);
							receiver.drawScoreFont(engine, playerID, 0, 19, playerProperties.getNameDisplay(), EventReceiver.COLOR_WHITE, 2f);

							receiver.drawScoreFont(engine, playerID, 0, 22, "D:SWITCH RANK SCREEN", EventReceiver.COLOR_GREEN);
						} else {
							for(int i = 0; i < RANKING_MAX; i++) {
								int gcolor = EventReceiver.COLOR_WHITE;
								if(rollClear[gameType][i] == 1) gcolor = EventReceiver.COLOR_GREEN;
								if(rollClear[gameType][i] == 2) gcolor = EventReceiver.COLOR_ORANGE;

								receiver.drawScoreFont(engine, playerID, 0, topY+i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW, scale);
								receiver.drawScoreFont(engine, playerID, 3, topY+i, getShortGradeNames()[gameType][rankingGrade[gameType][i]], gcolor, scale);
								receiver.drawScoreFont(engine, playerID, 9, topY+i, String.valueOf(rankingLevel[gameType][i]), (i == currentGameRank), scale);
								receiver.drawScoreFont(engine, playerID, 15, topY+i, GeneralUtil.getTime(rankingTime[gameType][i]), (i == currentGameRank), scale);
							}

							receiver.drawScoreFont(engine, playerID, 0, 17, "F:VIEW SECTION TIME", EventReceiver.COLOR_GREEN);
							receiver.drawScoreFont(engine, playerID, 0, 18, "LOCAL SCORES", EventReceiver.COLOR_BLUE);
							if (!playerProperties.isLoggedIn()) receiver.drawScoreFont(engine, playerID, 0, 19, "(NOT LOGGED IN)\n(E:LOG IN)");
							if (playerProperties.isLoggedIn()) receiver.drawScoreFont(engine, playerID, 0, 22, "D:SWITCH RANK SCREEN", EventReceiver.COLOR_GREEN);
						}
					} else {
						// Section Time
						receiver.drawScoreFont(engine, playerID, 0, 2, "SECTION TIME", EventReceiver.COLOR_BLUE);
						
						/* This does not work
						if ((engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (owner.replayMode == false))) {
							sectionTime = new int[SECTION_MAX[gameType]];
							sectionTime = sectionBests[gameType].clone();
							isSectionPB = new boolean[SECTION_MAX[gameType]];
						}
						*/
						
						int totalTime = 0;
						for(int i = 0; i < SECTION_MAX[gameType]; i++) {
							int temp = i * 100;
							int temp2 = ((i + 1) * 100) - 1;

							String strSectionTime;
							strSectionTime = String.format("%4d-%4d %s", temp, temp2, GeneralUtil.getTime(sectionBests[gameType][i]));
							
							receiver.drawScoreFont(engine, playerID, 0, 3 + i, strSectionTime, (engine.stat == GameEngine.STAT_SETTING) ? false : isSectionPB[i]);

							totalTime += (engine.stat == GameEngine.STAT_SETTING) ? sectionBests[gameType][i] : sectionTime[i];
						}
						
						receiver.drawScoreFont(engine, playerID, 0, 17, "TOTAL", EventReceiver.COLOR_BLUE);
						receiver.drawScoreFont(engine, playerID, 0, 18, GeneralUtil.getTime(totalTime));
						receiver.drawScoreFont(engine, playerID, 9, 17, "AVERAGE", EventReceiver.COLOR_BLUE);
						receiver.drawScoreFont(engine, playerID, 9, 18, GeneralUtil.getTime(totalTime / SECTION_MAX[gameType]));

						receiver.drawScoreFont(engine, playerID, 0, 20, "F:VIEW RANKING", EventReceiver.COLOR_GREEN);
					}
				}
			}
		} else if (engine.stat == GameEngine.STAT_CUSTOM) {
			playerProperties.loginScreen.renderScreen(receiver, engine, playerID);
		} else {
			if(showGrade) {
				// 段位
				receiver.drawScoreFont(engine, playerID, 0, 3, "GRADE", EventReceiver.COLOR_BLUE);
				if((grade >= 0) && (grade < getGradeNames()[gameType].length))
					receiver.drawScoreFont(engine, playerID, 0, 4, getGradeNames()[gameType][grade], ((gradeFlash > 0) && (gradeFlash % 4 == 0)));

				// Score
				receiver.drawScoreFont(engine, playerID, 0, 6, "SCORE", EventReceiver.COLOR_BLUE);
				String strScore;
				if((lastScore == 0) || (scoreGetTime <= 0)) {
					strScore = String.valueOf(engine.statistics.score);
				} else {
					strScore = String.valueOf(engine.statistics.score) + "\n(+" + String.valueOf(lastScore) + ")";
				}
				receiver.drawScoreFont(engine, playerID, 0, 7, strScore);
			}

			//  level
			receiver.drawScoreFont(engine, playerID, 0, 10, "LEVEL", EventReceiver.COLOR_BLUE);
			int tempLevel = engine.statistics.level;
			if(tempLevel < 0) tempLevel = 0;
			String strLevel = String.format("%3d", tempLevel);
			receiver.drawScoreFont(engine, playerID, 0, 11, strLevel);

			int speed = engine.speed.gravity / 128;
			if(engine.speed.gravity < 0) speed = 40;
			receiver.drawSpeedMeter(engine, playerID, 0, 12, speed);

			receiver.drawScoreFont(engine, playerID, 0, 13, String.format("%3d", nextSectionLv));

			// Time
			receiver.drawScoreFont(engine, playerID, 0, 15, "TIME", EventReceiver.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 16, GeneralUtil.getTime(engine.statistics.time));

			int baseX = receiver.getFieldDisplayPositionX(engine, playerID) + 4;
			int baseY = receiver.getFieldDisplayPositionY(engine, playerID) + 52;
			if (pCoordList.size() > 0 && cPiece != null) {
				for (int[] loc : pCoordList) {
					int cx = baseX + (16 * loc[0]);
					int cy = baseY + (16 * loc[1]);
					RendererExtension.drawScaledPiece(receiver, engine, playerID, cx, cy, cPiece, 1f, 0f);
				}
			}

			// Roll 残り time
			if((engine.gameActive) && (engine.ending == 2)) {
				int time = ROLLTIMELIMIT[gameType] - rollTime;
				if(time < 0) time = 0;
				receiver.drawScoreFont(engine, playerID, 0, 18, "ROLL TIME", EventReceiver.COLOR_BLUE);
				receiver.drawScoreFont(engine, playerID, 0, 19, GeneralUtil.getTime(time), ((time > 0) && (time < 10 * 60)));
			} else {
				// Spin Chain
				receiver.drawScoreFont(engine, playerID, 0, 18, "SPIN CHAIN", EventReceiver.COLOR_BLUE);
				receiver.drawScoreFont(engine, playerID, 0, 19, Integer.toString(spinChain - 1));
			}

			// REGRET表示
			if(regretDispFrame > 0) {
				receiver.drawMenuFont(engine,playerID,2,21,"REGRET",(regretDispFrame % 4 == 0),EventReceiver.COLOR_WHITE,EventReceiver.COLOR_ORANGE);
			}

			//  medal
			if(medalAC >= 1) receiver.drawScoreFont(engine, playerID, 0, 21, "AC", getMedalFontColor(medalAC));
			if(medalST >= 1) receiver.drawScoreFont(engine, playerID, 3, 21, "ST", getMedalFontColor(medalST));
			if(medalSK >= 1) receiver.drawScoreFont(engine, playerID, 0, 22, "SK", getMedalFontColor(medalSK));
			if(medalSC >= 1) receiver.drawScoreFont(engine, playerID, 3, 22, "SC", getMedalFontColor(medalSC));

			if (playerProperties.isLoggedIn() || PLAYER_NAME.length() > 0) {
				receiver.drawScoreFont(engine, playerID, 6, 21, "PLAYER", EventReceiver.COLOR_BLUE);
				receiver.drawScoreFont(engine, playerID, 6, 22, owner.replayMode ? PLAYER_NAME : playerProperties.getNameDisplay(), EventReceiver.COLOR_WHITE, 2f);
			}

			// Section Time
			if((showST == true) && (sectionTime != null)) {
				int y = (receiver.getNextDisplayType() == 2) ? 5 : 3;
				int x = (receiver.getNextDisplayType() == 2) ? 20 : 12;
				int x2 = (receiver.getNextDisplayType() == 2) ? 9 : 12;
				float scale = (receiver.getNextDisplayType() == 2) ? 0.5f : 1.0f;

				receiver.drawScoreFont(engine, playerID, x, y, "SECTION TIME", EventReceiver.COLOR_BLUE, scale);

				for(int i = 0; i < sectionTime.length; i++) {
					if(sectionTime[i] > 0) {
						int temp = i * 100;

						int section = engine.statistics.level / 100;
						String strSeparator = " ";
						if((i == section) && (engine.ending == 0)) strSeparator = "b";

						String strSectionTime;
						strSectionTime = String.format("%4d%s%s", temp, strSeparator, GeneralUtil.getTime(sectionTime[i]));

						receiver.drawScoreFont(engine, playerID, x-1, y + 1 + i, strSectionTime, isSectionPB[i], scale);
					}
				}

				if(avgSectionTime > 0) {
					receiver.drawScoreFont(engine, playerID, x2, 18, "AVERAGE", EventReceiver.COLOR_BLUE);
					receiver.drawScoreFont(engine, playerID, x2, 19, GeneralUtil.getTime(avgSectionTime));
				}
			}
			
			if((lastevent != EVENT_NONE) && (scoreGetTime > 0) && (!rollStarted)) {
				String strPieceName = Piece.getPieceName(lastpiece);

				switch(lastevent) {
				case EVENT_TSPIN_ZERO_MINI:
					receiver.drawMenuFont(engine, playerID, 2, 21, strPieceName + "-SPIN", EventReceiver.COLOR_PURPLE);
					break;
				case EVENT_TSPIN_ZERO:
					receiver.drawMenuFont(engine, playerID, 2, 21, strPieceName + "-SPIN", EventReceiver.COLOR_PINK);
					break;
				case EVENT_TSPIN_SINGLE_MINI:
					receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-MINI-S", EventReceiver.COLOR_YELLOW);
					break;
				case EVENT_TSPIN_SINGLE:
					receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-SINGLE", EventReceiver.COLOR_ORANGE);
					break;
				case EVENT_TSPIN_DOUBLE_MINI:
					receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-MINI-D", EventReceiver.COLOR_ORANGE);
					break;
				case EVENT_TSPIN_DOUBLE:
					receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-DOUBLE", EventReceiver.COLOR_RED);
					break;
				case EVENT_TSPIN_TRIPLE:
					receiver.drawMenuFont(engine, playerID, 1, 21, strPieceName + "-TRIPLE", EventReceiver.COLOR_RED);
					break;
				case EVENT_TSPIN_EZ:
					receiver.drawMenuFont(engine, playerID, 3, 21, "EZ-" + strPieceName, EventReceiver.COLOR_ORANGE);
					break;
				default:
					break;
				}
			}
			
			if (rollStarted) {
				// go from -length to 40 + length
				/*
				int min = 39;
				String dedicationUsed = (gameType == GAMETYPE_NORMAL) ? DEDICATION_SHORT : DEDICATION;
				int max = -dedicationUsed.length() - 4;
				int currentX = (int)(min + (max - min) * ((float)rollTime / ROLLTIMELIMIT[gameType]));
				receiver.drawMenuFont(engine, playerID, currentX, 22, dedicationUsed, EventReceiver.COLOR_PINK);
				*/
				
				ScrollingMarqueeText usedText = (gameType == GAMETYPE_NORMAL) ? creditObjectShort : creditObjectDefault;
				usedText.drawAtY(engine, receiver, playerID, 27.25, engine.displaysize + 1, (double)rollTime / ROLLTIMELIMIT[gameType]);
			}
			
			torikaned = (engine.statistics.level == 500) && (engine.stat == GameEngine.STAT_EXCELLENT);
			if (torikaned) {
				int lineOne = 11;
				receiver.drawMenuFont(engine, playerID, 0, lineOne, "BUT...");
				receiver.drawMenuFont(engine, playerID, 0, lineOne + 1, "LET'S GO");
				receiver.drawMenuFont(engine, playerID, 0, lineOne + 2, "BETTER");
				receiver.drawMenuFont(engine, playerID, 0, lineOne + 3, "NEXT TIME!");
			}
		}
	}
	
	// Called on move (in-game?)
	@Override
	public boolean onMove(GameEngine engine, int playerID) {
		// 新規ピース出現時
		if((engine.ending == 0) && (engine.statc[0] == 0) && (engine.holdDisable == false) && (!levelUpFlag)) {
			// Level up
			if(engine.statistics.level < nextSectionLv - 1) {
				engine.statistics.level++;
				if((engine.statistics.level == nextSectionLv - 1) && (lvStopSE == true) && (shouldPlayLSSE == true)) {
					engine.playSE("levelstop");
					shouldPlayLSSE = false;
				}
			}
			levelUp(engine);
		}
		if( (engine.ending == 0) && (engine.statc[0] > 0) && ((version >= 1) || (engine.holdDisable == false)) ) {
			levelUpFlag = false;
		}

		if((engine.ending == 0) && (engine.statc[0] == 0) && (engine.holdDisable == false)) {
			// せり上がりカウント
			if(tableGarbage[gameType][engine.statistics.level / 100] != 0) garbageCount++;

			// せり上がり
			if((garbageCount >= tableGarbage[gameType][engine.statistics.level / 100]) && (tableGarbage[gameType][engine.statistics.level / 100] != 0)) {
				engine.playSE("garbage");
				engine.field.addBottomCopyGarbage(Block.BLOCK_COLOR_GRAY,
												  engine.getSkin(),
												  Block.BLOCK_ATTRIBUTE_GARBAGE | Block.BLOCK_ATTRIBUTE_VISIBLE | Block.BLOCK_ATTRIBUTE_OUTLINE,
												  1);
				garbageCount = 0;
			}
		}

		// Endingスタート
		switch (gameType) {
		case GAMETYPE_DEATH:
			if((engine.ending == 2) && (rollStarted == false)) {
				rollStarted = true;
				owner.bgmStatus.bgm = BGMStatus.BGM_ENDING1;
			}
			break;
		case GAMETYPE_ABSOLUTE:
			if((engine.ending == 2) && (rollStarted == false)) {
				rollStarted = true;
				owner.bgmStatus.bgm = BGMStatus.BGM_ENDING1;
				engine.blockHidden = 300;
				engine.blockHiddenAnim = true;
			}
			break;
		case GAMETYPE_OMEN:
			if((engine.ending == 2) && (rollStarted == false)) {
				rollStarted = true;
				engine.big = true;
				owner.bgmStatus.bgm = BGMStatus.BGM_ENDING1;
			}
			break;
		case GAMETYPE_MASTERY:
		case GAMETYPE_NORMAL:
			if((engine.ending == 2) && (rollStarted == false)) {
				rollStarted = true;
				owner.bgmStatus.bgm = BGMStatus.BGM_ENDING2;
			}
			break;
		}

		return false;
	}
	
	// executed during ARE
	@Override
	public boolean onARE(GameEngine engine, int playerID) {
		// 最後の frame
		if((engine.ending == 0) && (engine.statc[0] >= engine.statc[1] - 1) && (!levelUpFlag)) {
			if(engine.statistics.level < nextSectionLv - 1) {
				engine.statistics.level++;
				if((engine.statistics.level == nextSectionLv - 1) && (lvStopSE == true) && (shouldPlayLSSE == true)) {
					engine.playSE("levelstop");
					shouldPlayLSSE = false;
				}
			}
			levelUp(engine);
			levelUpFlag = true;
		}

		return false;
	}
	
	// called when level increases
	private void levelUp(GameEngine engine) {
		// Meter
		engine.meterValue = ((engine.statistics.level % 100) * receiver.getMeterMax(engine)) / 99;
		engine.meterColor = GameEngine.METER_COLOR_GREEN;
		if(engine.statistics.level % 100 >= 50) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
		if(engine.statistics.level % 100 >= 80) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
		if(engine.statistics.level == nextSectionLv - 1) engine.meterColor = GameEngine.METER_COLOR_RED;

		// 速度変更
		setSpeed(engine, gameType);

		// BGM fadeout
		if((tableBGMFadeout[gameType][currentBGM] != -1) && (engine.statistics.level >= tableBGMFadeout[gameType][currentBGM]))
			owner.bgmStatus.fadesw  = true;
	}
	
	// calculate score
	@Override
	public void calcScore(GameEngine engine, int playerID, int lines) {
		if(engine.tspin) {
			// T-Spin 0 lines
			if((lines == 0) && (!engine.tspinez)) {
				if(engine.tspinmini) {
					lastevent = EVENT_TSPIN_ZERO_MINI;
				} else {
					lastevent = EVENT_TSPIN_ZERO;
				}
			}
			// Immobile EZ Spin
			else if(engine.tspinez && (lines > 0)) {
				lastevent = EVENT_TSPIN_EZ;
				shouldPlayLSSE = lvStopSE;
			}
			// T-Spin 1 line
			else if(lines == 1) {
				if(engine.tspinmini) {
					lastevent = EVENT_TSPIN_SINGLE_MINI;
				} else {
					lastevent = EVENT_TSPIN_SINGLE;
				}
				shouldPlayLSSE = lvStopSE;
			}
			// T-Spin 2 lines
			else if(lines == 2) {
				if(engine.tspinmini && engine.useAllSpinBonus) {
					lastevent = EVENT_TSPIN_DOUBLE_MINI;
				} else {
					lastevent = EVENT_TSPIN_DOUBLE;
				}
				shouldPlayLSSE = lvStopSE;
			}
			// T-Spin 3 lines
			else if(lines >= 3) {
				lastevent = EVENT_TSPIN_TRIPLE;
				shouldPlayLSSE = lvStopSE;
			}
			lastpiece = engine.nowPieceObject.id;
		} else {
			if(lines == 1) {
				lastevent = EVENT_SINGLE;
			} else if(lines == 2) {
				lastevent = EVENT_DOUBLE;
			} else if(lines == 3) {
				lastevent = EVENT_TRIPLE;
			} else if(lines >= 4) {
				lastevent = EVENT_FOUR;
			}
			
			
		}
		
		// Combo
		if(lines >= 1 & !engine.tspin) {
			spinChain = 1;
		} else if (lines >= 1) {
			spinChain++;
			spins++;
			if(spinChain < 1) spinChain = 1;
		}

		if((lines >= 1) && (engine.ending == 0)) {
			// 4-line clearカウント
			if(engine.tspin) {
				// SK medal
				switch (gameType) {
				case GAMETYPE_OMEN:
				case GAMETYPE_DEATH:
					if((spins == 5) || (spins == 10) || (spins == 17)) {
						engine.playSE("medal");
						medalSK++;
					}
					break;
				case GAMETYPE_MASTERY:
				case GAMETYPE_ABSOLUTE:
					if((spins == 10) || (spins == 20) || (spins == 35)) {
						engine.playSE("medal");
						medalSK++;
					}
					break;
				default:
					break;
				}
				
			}

			// AC medal
			if(engine.field.isEmpty()) {
				engine.playSE("bravo");

				if(medalAC < 3) {
					engine.playSE("medal");
					medalAC++;
				}
			}

			// SC medal
			if((spinChain >= 4) && (medalSC < 1)) {
				engine.playSE("medal");
				medalSC = 1;
			} else if((spinChain >= 5) && (medalSC < 2)) {
				engine.playSE("medal");
				medalSC = 2;
			} else if((spinChain >= 7) && (medalSC < 3)) {
				engine.playSE("medal");
				medalSC = 3;
			}

			// せり上がりカウント減少
			if(tableGarbage[gameType][engine.statistics.level / 100] != 0) garbageCount -= lines;
			if(garbageCount < 0) garbageCount = 0;

			// Level up
			int levelb = engine.statistics.level;
			int levelplus = 0;
			switch (lastevent) {
				case EVENT_TSPIN_EZ:
					if (lines >= 2) {
						levelplus = 3;
					} else {
						levelplus = 1;
					}
					break;
				case EVENT_TSPIN_SINGLE_MINI:
					levelplus = 1;
					break;
				case EVENT_TSPIN_SINGLE:
					levelplus = 2;
					break;
				case EVENT_TSPIN_DOUBLE_MINI:
					levelplus = 3;
					if (gameType == GAMETYPE_OMEN && version >= 2) levelplus *= 1.5;
					break;
				case EVENT_TSPIN_DOUBLE:
					levelplus = 4;
					if (gameType == GAMETYPE_OMEN && version >= 2) levelplus *= 1.5;
					break;
				case EVENT_TSPIN_TRIPLE:
					levelplus = 6;
					if (gameType == GAMETYPE_OMEN && version >= 2) levelplus *= 1.5;
					break;
				case EVENT_SINGLE:
				case EVENT_DOUBLE:
				case EVENT_TRIPLE:
					if (engine.tspinEnable || version <= 1) {
						if (engine.statistics.level < nextSectionLv - 1) {
							levelplus = daredevil ? -1 : 1;
						} else {
							shouldPlayLSSE = false;
						}
					} else {
						levelplus = lines;
						if (gameType == GAMETYPE_OMEN && lines > 2) levelplus *= 1.5;
					}
					break;
				case EVENT_FOUR:
					if (engine.tspinEnable || version <= 1) {
						if (engine.statistics.level < nextSectionLv - 2) {
							levelplus = daredevil ? -2 : 2;
						} else if (engine.statistics.level == nextSectionLv - 2)  {
							levelplus = daredevil ? -1 : 1;
						} else {
							shouldPlayLSSE = false;
						}
					} else {
						levelplus = lines;
						if (gameType == GAMETYPE_OMEN) levelplus *= 1.5;
					}
					break;
				default:
					levelplus = 0;
					break;
			}
			
			// AC medal
			if(engine.field.isEmpty()) {
				engine.playSE("bravo");
				levelplus += 12;
				if(medalAC < 3) {
					engine.playSE("medal");
					medalAC++;
				}
			}
			
			levelplus += SPIN_CHAIN_LEVELBONUS[clamp(spinChain, 1, 6) - 1];
			levelplus *= daredevil ? 2 : 1;
					
			engine.statistics.level += levelplus;

			levelUp(engine);

			if(engine.statistics.level >= LEVEL_MAX[gameType]) {
				// Ending
				engine.playSE("endingstart");
				engine.statistics.level = LEVEL_MAX[gameType];
				engine.timerActive = false;
				engine.ending = 1;
				rollCleared = 1;
				torikaned = true;

				// Section Timeを記録
				lastSectionTime = sectionTime[levelb / 100];
				sectionCount++;
				setAverageSectionTime();

				// ST medal
				stMedalCheck(engine, gameType, levelb / 100);

				if(lastSectionTime > tableTimeRegret[gameType][levelb / 100] && tableTimeRegret[gameType][levelb / 100] != -1) {
					// REGRET判定
					regretDispFrame = 180;
					engine.playSE("regret");
				} else {
					// 段位上昇
					switch (gameType) {
					case GAMETYPE_MASTERY:
					case GAMETYPE_DEATH:
					case GAMETYPE_ABSOLUTE:
					case GAMETYPE_NORMAL:
						if(showGrade) engine.playSE("excellent");
						grade++;
						gradeFlash = 180;
						break;
					case GAMETYPE_OMEN:
						if(showGrade) engine.playSE("excellent");
						grade++;
						if(grade > 13) grade = 13;
						gradeFlash = 180;
						break;
					}
				}
			} else if( ((nextSectionLv ==  500) && (engine.statistics.level >=  500) && (torikanTime[gameType] > 0) && (engine.statistics.time > torikanTime[gameType])) ||
					   ((nextSectionLv == 1000) && (engine.statistics.level >= 1000) && (torikanTime[gameType] > 0) && (engine.statistics.time > torikanTime[gameType] * 2)) )
			{
				//  level500/1000とりカン
				engine.playSE("endingstart");
				torikaned = true;

				if(nextSectionLv == 500) engine.statistics.level = 500;
				if(nextSectionLv == 1000) engine.statistics.level = 1000;

				engine.gameEnded();
				engine.staffrollEnable = false;
				engine.ending = 1;

				secretGrade = engine.field.getSecretGrade();

				// Section Timeを記録
				lastSectionTime = sectionTime[levelb / 100];
				sectionCount++;
				setAverageSectionTime();

				// ST medal
				stMedalCheck(engine, gameType, levelb / 100);

				if(lastSectionTime > tableTimeRegret[gameType][levelb / 100] && tableTimeRegret[gameType][levelb / 100] != -1) {
					// REGRET判定
					regretDispFrame = 180;
					engine.playSE("regret");
				} else {
					// 段位上昇
					switch (gameType) {
					case GAMETYPE_MASTERY:
					case GAMETYPE_DEATH:
					case GAMETYPE_ABSOLUTE:
					case GAMETYPE_NORMAL:
						break;
					case GAMETYPE_OMEN:
						grade++;
						if(grade > 13) grade = 13;
						gradeFlash = 180;
						break;
					}
				}
			} else if(engine.statistics.level >= nextSectionLv) {
				// Next Section
				engine.playSE("levelup");

				// Background切り替え
				owner.backgroundStatus.fadesw = true;
				owner.backgroundStatus.fadecount = 0;
				owner.backgroundStatus.fadebg = nextSectionLv / 100;
				torikaned = true;

				// BGM切り替え
				if((tableBGMChange[gameType][currentBGM] != -1) && (engine.statistics.level >= tableBGMChange[gameType][currentBGM])) {
					currentBGM++;
					owner.bgmStatus.fadesw = false;
					owner.bgmStatus.bgm = tableBGMNumber[gameType][currentBGM];
				}

				// Section Timeを記録
				lastSectionTime = sectionTime[levelb / 100];
				sectionCount++;
				setAverageSectionTime();

				// ST medal
				stMedalCheck(engine, gameType, levelb / 100);

				// 骨Block出現開始
				if(engine.statistics.level >= 1000) engine.bone = true;

				// Update level for next section
				nextSectionLv += 100;
				if (nextSectionLv >= LEVEL_MAX[gameType]) {
					nextSectionLv = LEVEL_MAX[gameType];
				}

				if(lastSectionTime > tableTimeRegret[gameType][levelb / 100] && tableTimeRegret[gameType][levelb / 100] != -1) {
					// REGRET判定
					regretDispFrame = 180;
					engine.playSE("regret");
				} else {
					// 段位上昇
					switch (gameType) {
					case GAMETYPE_MASTERY:
					case GAMETYPE_DEATH:
					case GAMETYPE_ABSOLUTE:
						if ((levelb / 100) == 4) {
							if(showGrade) engine.playSE("gradeup");
							grade++;
							gradeFlash = 180;
						}
						break;
					case GAMETYPE_NORMAL:
						break;
					case GAMETYPE_OMEN:
						grade++;
						if(grade > 13) grade = 13;
						gradeFlash = 180;
						break;
					}
				}
			} else if((engine.statistics.level == nextSectionLv - 1) && (lvStopSE == true) && (shouldPlayLSSE == true)) {
				engine.playSE("levelstop");
			}

			// Calculate score
			int manuallock = 0;
			if(engine.manualLock == true) manuallock = 1;

			int bravo = 1;
			if(engine.field.isEmpty()) bravo = 2;

			int speedBonus = (engine.getLockDelay() * 6) - engine.statc[0];
			if(speedBonus < 0) speedBonus = 0;

			lastScore = ( ((levelb + lines) / 4 + engine.softdropFall + manuallock) * lines * spinChain + speedBonus +
						(engine.statistics.level / 2) ) * bravo;
			if (!engine.tspin) {
				lastScore = 0;
			}

			engine.statistics.score += lastScore;
			scoreGetTime = 120;
		}
	}
	
	// runs last each frame
	@Override
	public void onLast(GameEngine engine, int playerID) {
		// 段位上昇時のフラッシュ
		if(gradeFlash > 0) gradeFlash--;

		// 獲得Render score
		if(scoreGetTime > 0) scoreGetTime--;

		// REGRET表示
		if(regretDispFrame > 0) regretDispFrame--;

		// Section Time増加
		if((engine.timerActive) && (engine.ending == 0)) {
			int section = engine.statistics.level / 100;

			if((section >= 0) && (section < sectionTime.length)) {
				sectionTime[section]++;
			}
		}

		// Ending
		if((engine.gameActive) && (engine.ending == 2)) {
			rollTime++;

			// Time meter
			int remainRollTime = ROLLTIMELIMIT[gameType] - rollTime;
			engine.meterValue = (remainRollTime * receiver.getMeterMax(engine)) / ROLLTIMELIMIT[gameType];
			engine.meterColor = GameEngine.METER_COLOR_GREEN;
			if(remainRollTime <= 30*60) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
			if(remainRollTime <= 20*60) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
			if(remainRollTime <= 10*60) engine.meterColor = GameEngine.METER_COLOR_RED;

			// Roll 終了
			if(rollTime >= ROLLTIMELIMIT[gameType]) {
				secretGrade = engine.field.getSecretGrade();
				rollCleared = 2;
				engine.gameEnded();
				engine.resetStatc();
				engine.stat = GameEngine.STAT_EXCELLENT;
			}
		}

		if( (engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (!owner.replayMode)) || engine.stat == GameEngine.STAT_CUSTOM ) {
			// Show rank
			if(engine.ctrl.isPush(Controller.BUTTON_D) && playerProperties.isLoggedIn() && engine.stat != GameEngine.STAT_CUSTOM) {
				showPlayerStats = !showPlayerStats;
				engine.playSE("change");
			}
		}

		if (engine.quitflag) {
			playerProperties = new ProfileProperties(headerColour);
		}
	}

	// GAME OVER
	@Override
	public boolean onGameOver(GameEngine engine, int playerID) {
		if((engine.statc[0] == 0) && (engine.gameActive)) {
			secretGrade = engine.field.getSecretGrade();
		}
		return false;
	}
	
	// DRAW RESULTS
	@Override
	public void renderResult(GameEngine engine, int playerID) {
		receiver.drawMenuFont(engine, playerID, 0, 0, "kn PAGE" + (engine.statc[1] + 1) + "/3", EventReceiver.COLOR_RED);

		if(engine.statc[1] == 0) {
			int gcolor = EventReceiver.COLOR_WHITE;
			if((rollCleared == 1) || (rollCleared == 3)) gcolor = EventReceiver.COLOR_GREEN;
			if((rollCleared == 2) || (rollCleared == 4)) gcolor = EventReceiver.COLOR_ORANGE;
			receiver.drawMenuFont(engine, playerID, 0, 2, "GRADE", EventReceiver.COLOR_BLUE);
			String strGrade = String.format("%10s", getGradeNames()[gameType][grade]);
			receiver.drawMenuFont(engine, playerID, 0, 3, strGrade, gcolor);

			drawResultStats(engine, playerID, receiver, 4, EventReceiver.COLOR_BLUE,
					STAT_SCORE, STAT_LINES, STAT_LEVEL_MANIA, STAT_TIME);
			drawResultRank(engine, playerID, receiver, 12, EventReceiver.COLOR_BLUE, currentGameRank);
			if(secretGrade > 4) {
				drawResult(engine, playerID, receiver, 14, EventReceiver.COLOR_BLUE,
						"S. IDIOT%", String.format("%10s", tableSecretGradeName[secretGrade-1]));
			}
		} else if(engine.statc[1] == 1) {
			receiver.drawMenuFont(engine, playerID, 0, 2, "SECTION", EventReceiver.COLOR_BLUE);

			for(int i = 0; i < sectionTime.length; i++) {
				if(sectionTime[i] > 0) {
					receiver.drawMenuFont(engine, playerID, 2, 3 + i, GeneralUtil.getTime(sectionTime[i]), isSectionPB[i]);
				}
			}

			if(avgSectionTime > 0) {
				receiver.drawMenuFont(engine, playerID, 0, 16, "AVERAGE", EventReceiver.COLOR_BLUE);
				receiver.drawMenuFont(engine, playerID, 2, 17, GeneralUtil.getTime(avgSectionTime));
			}
		} else if(engine.statc[1] == 2) {
			receiver.drawMenuFont(engine, playerID, 0, 2, "MEDAL", EventReceiver.COLOR_BLUE);
			if(medalAC >= 1) receiver.drawMenuFont(engine, playerID, 5, 3, "AC", getMedalFontColor(medalAC));
			if(medalST >= 1) receiver.drawMenuFont(engine, playerID, 8, 3, "ST", getMedalFontColor(medalST));
			if(medalSK >= 1) receiver.drawMenuFont(engine, playerID, 5, 4, "SK", getMedalFontColor(medalSK));
			if(medalSC >= 1) receiver.drawMenuFont(engine, playerID, 8, 4, "SC", getMedalFontColor(medalSC));

			drawResultStats(engine, playerID, receiver, 6, EventReceiver.COLOR_BLUE,
					STAT_LPM, STAT_SPM, STAT_PIECE, STAT_PPS);
		}
	}
	
	// Call in results screen
	@Override
	public boolean onResult(GameEngine engine, int playerID) {
		// ページ切り替え
		if(engine.ctrl.isMenuRepeatKey(Controller.BUTTON_UP)) {
			engine.statc[1]--;
			if(engine.statc[1] < 0) engine.statc[1] = 2;
			engine.playSE("change");
		}
		if(engine.ctrl.isMenuRepeatKey(Controller.BUTTON_DOWN)) {
			engine.statc[1]++;
			if(engine.statc[1] > 2) engine.statc[1] = 0;
			engine.playSE("change");
		}
		//  section time display切替
		if(engine.ctrl.isPush(Controller.BUTTON_F)) {
			engine.playSE("change");
			showBestSTime = !showBestSTime;
		}

		return false;
	}
	
	/*
	 * [--- Settings, ranks and stuff ---]
	 */
	
	// Save Replay
	@Override
	public void saveReplay(GameEngine engine, int playerID, CustomProperties prop) {
		saveSetting(owner.replayProp, engine.ruleopt.strRuleName);
		owner.replayProp.setProperty("idiotmode.version", version);

		// Update rankings
		if((owner.replayMode == false) && (levelAtStart == 0) && (engine.ai == null) && (!daredevil)) {
			updateRanking(gameType, grade, engine.statistics.level, engine.statistics.time, rollCleared);
			if(medalST == 3) updateBestSectionTime(gameType);

			if (playerProperties.isLoggedIn()) {
				prop.setProperty("idiotmode.playerName", playerProperties.getNameDisplay());
			}

			if((currentGameRank != -1) || (medalST == 3)) {
				saveRanking(owner.modeConfig, engine.ruleopt.strRuleName);
				receiver.saveModeConfig(owner.modeConfig);
			}

			if(currentGameRankPlayer != -1 && playerProperties.isLoggedIn()) {
				saveRankingPlayer(playerProperties, engine.ruleopt.strRuleName);
				playerProperties.saveProfileConfig();
			}
		}
	}
	
	// Load last settings
	private void loadSetting(CustomProperties prop, String strRuleName) {
		gameType = prop.getProperty("idiotmode.gameType." + strRuleName, 0);
		levelAtStart = prop.getProperty("idiotmode.levelAtStart." + strRuleName, 0);
		lvStopSE = prop.getProperty("idiotmode.lvStopSE." + strRuleName, true);
		showST = prop.getProperty("idiotmode.showST." + strRuleName, false);
		torikanTime[0] = prop.getProperty("idiotmode.torikanTime.death." + strRuleName, DEFAULT_TORIKAN[0]);
		torikanTime[1] = prop.getProperty("idiotmode.torikanTime.omen." + strRuleName, DEFAULT_TORIKAN[1]);
		showGrade = prop.getProperty("idiotmode.showGrade." + strRuleName, false);
		condescension = prop.getProperty("idiotmode.condescension." + strRuleName, true);
		version = prop.getProperty("idiotmode.version." + strRuleName, CURRENT_VERSION);
		tspinEnableType = prop.getProperty("idiotmode.version.spinbonus." + strRuleName, 1);
		spinCheckType = prop.getProperty("idiotmode.version.spincheck." + strRuleName, 0);
		tspinEnableEZ = prop.getProperty("idiotmode.version.EZSpin." + strRuleName, true);
		enableTSpinKick = prop.getProperty("idiotmode.version.KickSpin." + strRuleName, true);
		daredevil = prop.getProperty("idiotmode.version.daredevil." + strRuleName, false);
	}
	
	// Save last settings
	private void saveSetting(CustomProperties prop, String strRuleName) {
		prop.setProperty("idiotmode.gameType." + strRuleName, gameType);
		prop.setProperty("idiotmode.levelAtStart." + strRuleName, levelAtStart);
		prop.setProperty("idiotmode.lvStopSE." + strRuleName, lvStopSE);
		prop.setProperty("idiotmode.showST." + strRuleName, showST);
		prop.setProperty("idiotmode.torikanTime.death." + strRuleName, torikanTime[0]);
		prop.setProperty("idiotmode.torikanTime.omen." + strRuleName, torikanTime[1]);
		prop.setProperty("idiotmode.showGrade." + strRuleName, showGrade);
		prop.setProperty("idiotmode.condescension." + strRuleName, condescension);
		prop.setProperty("idiotmode.version." + strRuleName, version);
		prop.setProperty("idiotmode.version.spinbonus." + strRuleName, tspinEnableType);
		prop.setProperty("idiotmode.version.spincheck." + strRuleName, spinCheckType);
		prop.setProperty("idiotmode.version.EZSpin." + strRuleName, tspinEnableEZ);
		prop.setProperty("idiotmode.version.KickSpin." + strRuleName, enableTSpinKick);
		prop.setProperty("idiotmode.version.daredevil." + strRuleName, daredevil);
	}

	// Load last settings
	private void loadSettingPlayer(ProfileProperties prop, String strRuleName) {
		if (!prop.isLoggedIn()) return;
		gameType = prop.getProperty("idiotmode.gameType." + strRuleName, 0);
		levelAtStart = prop.getProperty("idiotmode.levelAtStart." + strRuleName, 0);
		lvStopSE = prop.getProperty("idiotmode.lvStopSE." + strRuleName, true);
		showST = prop.getProperty("idiotmode.showST." + strRuleName, false);
		torikanTime[0] = prop.getProperty("idiotmode.torikanTime.death." + strRuleName, DEFAULT_TORIKAN[0]);
		torikanTime[1] = prop.getProperty("idiotmode.torikanTime.omen." + strRuleName, DEFAULT_TORIKAN[1]);
		showGrade = prop.getProperty("idiotmode.showGrade." + strRuleName, false);
		condescension = prop.getProperty("idiotmode.condescension." + strRuleName, true);
		tspinEnableType = prop.getProperty("idiotmode.version.spinbonus." + strRuleName, 1);
		spinCheckType = prop.getProperty("idiotmode.version.spincheck." + strRuleName, 0);
		tspinEnableEZ = prop.getProperty("idiotmode.version.EZSpin." + strRuleName, true);
		enableTSpinKick = prop.getProperty("idiotmode.version.KickSpin." + strRuleName, true);
		daredevil = prop.getProperty("idiotmode.version.daredevil." + strRuleName, false);
	}

	// Save last settings
	private void saveSettingPlayer(ProfileProperties prop, String strRuleName) {
		if (!prop.isLoggedIn()) return;
		prop.setProperty("idiotmode.gameType." + strRuleName, gameType);
		prop.setProperty("idiotmode.levelAtStart." + strRuleName, levelAtStart);
		prop.setProperty("idiotmode.lvStopSE." + strRuleName, lvStopSE);
		prop.setProperty("idiotmode.showST." + strRuleName, showST);
		prop.setProperty("idiotmode.torikanTime.death." + strRuleName, torikanTime[0]);
		prop.setProperty("idiotmode.torikanTime.omen." + strRuleName, torikanTime[1]);
		prop.setProperty("idiotmode.showGrade." + strRuleName, showGrade);
		prop.setProperty("idiotmode.condescension." + strRuleName, condescension);
		prop.setProperty("idiotmode.version.spinbonus." + strRuleName, tspinEnableType);
		prop.setProperty("idiotmode.version.spincheck." + strRuleName, spinCheckType);
		prop.setProperty("idiotmode.version.EZSpin." + strRuleName, tspinEnableEZ);
		prop.setProperty("idiotmode.version.KickSpin." + strRuleName, enableTSpinKick);
		prop.setProperty("idiotmode.version.daredevil." + strRuleName, daredevil);
	}
	
	// Load rankings
	private void loadRanking(CustomProperties prop, String ruleName) {
		for (int t = 0; t < GAMETYPE_MAX; t++) {
			for(int i = 0; i < RANKING_MAX; i++) {
				rankingGrade[t][i] = prop.getProperty("idiotmode.ranking." + ruleName + ".grade." + GAMETYPE_NAMES[t] + "." + i, 0);
				rankingLevel[t][i] = prop.getProperty("idiotmode.ranking." + ruleName + ".level." + GAMETYPE_NAMES[t] + "." + i, 0);
				rankingTime[t][i] = prop.getProperty("idiotmode.ranking." + ruleName + ".time." + GAMETYPE_NAMES[t] + "." + i, 0);
				rollClear[t][i] = prop.getProperty("idiotmode.ranking." + ruleName + ".rollclear." + GAMETYPE_NAMES[t] + "." + i, 0);
			}
			for(int i = 0; i < SECTION_MAX[t]; i++) {
				switch (t) {
				case GAMETYPE_ABSOLUTE:
				case GAMETYPE_MASTERY:
				case GAMETYPE_NORMAL:
					sectionBests[t][i] = prop.getProperty("idiotmode.bestSectionTime." + ruleName + "."  + GAMETYPE_NAMES[t] + "." + i, DEFAULT_SLOW_ST);
					break;

				case GAMETYPE_DEATH:
				case GAMETYPE_OMEN:
					sectionBests[t][i] = prop.getProperty("idiotmode.bestSectionTime." + ruleName + "."  + GAMETYPE_NAMES[t] + "." + i, DEFAULT_SECTION_TIME);
					break;
				}
				
			}
		}
	}
	
	// Save rankings
	private void saveRanking(CustomProperties prop, String ruleName) {
		for (int t = 0; t < GAMETYPE_MAX; t++) {
			for(int i = 0; i < RANKING_MAX; i++) {
				prop.setProperty("idiotmode.ranking." + ruleName + ".grade." + GAMETYPE_NAMES[t] + "." + i, rankingGrade[t][i]);
				prop.setProperty("idiotmode.ranking." + ruleName + ".level." + GAMETYPE_NAMES[t] + "." + i, rankingLevel[t][i]);
				prop.setProperty("idiotmode.ranking." + ruleName + ".time." + GAMETYPE_NAMES[t] + "." + i, rankingTime[t][i]);
				prop.setProperty("idiotmode.ranking." + ruleName + ".rollclear." + GAMETYPE_NAMES[t] + "." + i, rollClear[t][i]);
			}
			for(int i = 0; i < SECTION_MAX[t]; i++) {
				prop.setProperty("idiotmode.bestSectionTime." + ruleName + "." + GAMETYPE_NAMES[t] + "." + i, sectionBests[t][i]);
			}
		}
	}

	// Load rankings
	private void loadRankingPlayer(ProfileProperties prop, String ruleName) {
		if (!prop.isLoggedIn()) return;
		for (int t = 0; t < GAMETYPE_MAX; t++) {
			for(int i = 0; i < RANKING_MAX; i++) {
				rankingGradePlayer[t][i] = prop.getProperty("idiotmode.ranking." + ruleName + ".grade." + GAMETYPE_NAMES[t] + "." + i, 0);
				rankingLevelPlayer[t][i] = prop.getProperty("idiotmode.ranking." + ruleName + ".level." + GAMETYPE_NAMES[t] + "." + i, 0);
				rankingTimePlayer[t][i] = prop.getProperty("idiotmode.ranking." + ruleName + ".time." + GAMETYPE_NAMES[t] + "." + i, 0);
				rollClearPlayer[t][i] = prop.getProperty("idiotmode.ranking." + ruleName + ".rollclear." + GAMETYPE_NAMES[t] + "." + i, 0);
			}
		}
	}

	// Save rankings
	private void saveRankingPlayer(ProfileProperties prop, String ruleName) {
		if (!prop.isLoggedIn()) return;
		for (int t = 0; t < GAMETYPE_MAX; t++) {
			for(int i = 0; i < RANKING_MAX; i++) {
				prop.setProperty("idiotmode.ranking." + ruleName + ".grade." + GAMETYPE_NAMES[t] + "." + i, rankingGradePlayer[t][i]);
				prop.setProperty("idiotmode.ranking." + ruleName + ".level." + GAMETYPE_NAMES[t] + "." + i, rankingLevelPlayer[t][i]);
				prop.setProperty("idiotmode.ranking." + ruleName + ".time." + GAMETYPE_NAMES[t] + "." + i, rankingTimePlayer[t][i]);
				prop.setProperty("idiotmode.ranking." + ruleName + ".rollclear." + GAMETYPE_NAMES[t] + "." + i, rollClearPlayer[t][i]);
			}
		}
	}
	
	// Compare Rankings - gt: gameType
	private int checkRanking(int gt, int gr, int lv, int time, int clear) {
		for(int i = 0; i < RANKING_MAX; i++) {
			if(clear > rollClear[gt][i]) {
				return i;
			} else if((clear == rollClear[gt][i]) && (gr > rankingGrade[gt][i])) {
				return i;
			} else if((clear == rollClear[gt][i]) && (gr == rankingGrade[gt][i]) && (lv > rankingLevel[gt][i])) {
				return i;
			} else if((clear == rollClear[gt][i]) && (gr == rankingGrade[gt][i]) && (lv == rankingLevel[gt][i]) && (time < rankingTime[gt][i])) {
				return i;
			}
		}

		return -1;
	}

	// Compare Rankings - gt: gameType
	private int checkRankingPlayer(int gt, int gr, int lv, int time, int clear) {
		for(int i = 0; i < RANKING_MAX; i++) {
			if(clear > rollClearPlayer[gt][i]) {
				return i;
			} else if((clear == rollClearPlayer[gt][i]) && (gr > rankingGradePlayer[gt][i])) {
				return i;
			} else if((clear == rollClearPlayer[gt][i]) && (gr == rankingGradePlayer[gt][i]) && (lv > rankingLevelPlayer[gt][i])) {
				return i;
			} else if((clear == rollClearPlayer[gt][i]) && (gr == rankingGradePlayer[gt][i]) && (lv == rankingLevelPlayer[gt][i]) && (time < rankingTimePlayer[gt][i])) {
				return i;
			}
		}

		return -1;
	}
	
	// Update rankings - gt: gameType
	private void updateRanking(int gt, int gr, int lv, int time, int clear) {
		currentGameRank = checkRanking(gt, gr, lv, time, clear);

		if(currentGameRank != -1) {
			// Shift down ranking entries
			for(int i = RANKING_MAX - 1; i > currentGameRank; i--) {
				rankingGrade[gt][i] = rankingGrade[gt][i - 1];
				rankingLevel[gt][i] = rankingLevel[gt][i - 1];
				rankingTime[gt][i] = rankingTime[gt][i - 1];
				rollClear[gt][i] = rollClear[gt][i - 1];
			}

			// Add new data
			rankingGrade[gt][currentGameRank] = gr;
			rankingLevel[gt][currentGameRank] = lv;
			rankingTime[gt][currentGameRank] = time;
			rollClear[gt][currentGameRank] = clear;
		}

		if (playerProperties.isLoggedIn()) {
			currentGameRankPlayer = checkRankingPlayer(gt, gr, lv, time, clear);

			if(currentGameRankPlayer != -1) {
				// Shift down ranking entries
				for(int i = RANKING_MAX - 1; i > currentGameRankPlayer; i--) {
					rankingGradePlayer[gt][i] = rankingGradePlayer[gt][i - 1];
					rankingLevelPlayer[gt][i] = rankingLevelPlayer[gt][i - 1];
					rankingTimePlayer[gt][i] = rankingTimePlayer[gt][i - 1];
					rollClearPlayer[gt][i] = rollClearPlayer[gt][i - 1];
				}

				// Add new data
				rankingGradePlayer[gt][currentGameRankPlayer] = gr;
				rankingLevelPlayer[gt][currentGameRankPlayer] = lv;
				rankingTimePlayer[gt][currentGameRankPlayer] = time;
				rollClearPlayer[gt][currentGameRankPlayer] = clear;
			}
		}
	}
	
	// Update best STs
	private void updateBestSectionTime(int gt) {
		for(int i = 0; i < SECTION_MAX[gt]; i++) {
			if(isSectionPB[i]) {
				sectionBests[gt][i] = sectionTime[i];
			}
		}
	}
	
	/*
	 * [--- LOCALS BLOCK ---]
	 */
	
	// Returns value if value < max, else return max.
	private int clamp(int Value, int Min, int Max) {
		if (Value <= Max && Value >= Min) {
			return Value;
		} else if (Value > Max) {
			return Max;
		} else {
			return Min;
		}
	}
	
	private String[][] getGradeNames() {
		if (condescension) {
			return tableGradeName;
		} else {
			return tablePoliteGradeName;
		}
	}
	
	private String[][] getShortGradeNames() {
		if (condescension) {
			return tableShortGradeName;
		} else {
			return tableShortPoliteGradeName;
		}
	}
	
	// o7
	public static final String DEDICATION = "THIS MODE IS CREATED BY 0XFC963F18DC21 AND IS INSPIRED BY AND IS DEDICATED TO AKARI AND HER VIDEO, \"SHIRASE IDIOT%\". SPECIAL THANKS GOES TO AKARI, OSHISAURE, RY00001, GLITCHYPSI AND THE DRAGON GOD NERROTH. CONGRATULATIONS ON REACHING THE ENDING, AND THANK YOU TOO FOR PLAYING!";
	public static final String DEDICATION_SHORT = "THANK YOU FOR PLAYING IDIOT% MODE! NOW TRY A HARDER DIFFICULTY!";
}