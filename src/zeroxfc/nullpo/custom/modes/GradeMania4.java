package zeroxfc.nullpo.custom.modes;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.IntFunction;
import mu.nu.nullpo.game.component.BGMStatus;
import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.component.RuleOptions;
import mu.nu.nullpo.game.component.SpeedParam;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.game.subsystem.mode.DummyMode;
import mu.nu.nullpo.gui.slick.NormalFont;
import mu.nu.nullpo.util.CustomProperties;
import mu.nu.nullpo.util.GeneralUtil;
import org.apache.log4j.Logger;
import zeroxfc.nullpo.custom.libs.CustomResourceHolder;
import zeroxfc.nullpo.custom.libs.GameTextUtilities;
import zeroxfc.nullpo.custom.libs.ProfileProperties;
import zeroxfc.nullpo.custom.libs.RendererExtension;
import zeroxfc.nullpo.custom.libs.ScrollingMarqueeText;
import zeroxfc.nullpo.custom.libs.SoundLoader;
import zeroxfc.nullpo.custom.libs.SpeedTableBuilder;
import zeroxfc.nullpo.custom.libs.backgroundtypes.*;
import zeroxfc.nullpo.custom.libs.particles.Fireworks;
import zeroxfc.nullpo.custom.libs.particles.SurfaceSparks;

public class GradeMania4 extends DummyMode {
    private static final Logger log = Logger.getLogger(GradeMania4.class);

    private static final int CURRENT_VERSION = 0;

    private static final IntFunction<SpeedParam> SPEED_TABLE = new SpeedTableBuilder()
        .addGravity(4, 256, 30)
        .addGravity(6, 256, 35)
        .addGravity(8, 256, 40)
        .addGravity(10, 256, 50)
        .addGravity(12, 256, 60)
        .addGravity(16, 256, 70)
        .addGravity(32, 256, 80)
        .addGravity(48, 256, 90)
        .addGravity(64, 256, 100)
        .addGravity(80, 256, 120)
        .addGravity(96, 256, 140)
        .addGravity(112, 256, 160)
        .addGravity(128, 256, 170)
        .addGravity(144, 256, 200)
        .addGravity(4, 256, 220)
        .addGravity(32, 256, 230)
        .addGravity(64, 256, 233)
        .addGravity(96, 256, 236)
        .addGravity(128, 256, 239)
        .addGravity(160, 256, 243)
        .addGravity(192, 256, 247)
        .addGravity(224, 256, 251)
        .addGravity(256, 256, 300)
        .addGravity(512, 256, 330)
        .addGravity(768, 256, 360)
        .addGravity(1024, 256, 400)
        .addGravity(1280, 256, 420)
        .addGravity(1024, 256, 450)
        .addGravity(768, 256, 500)
        .addGravity(-1, 256, Integer.MAX_VALUE)
        .addARE(23, 700)
        .addARE(14, 800)
        .addARE(10, 900)
        .addARE(10, Integer.MAX_VALUE)
        .addLineARE(23, 600)
        .addLineARE(14, 700)
        .addLineARE(10, 800)
        .addLineARE(4, 900)
        .addLineARE(4, Integer.MAX_VALUE)
        .addLineDelay(40, 500)
        .addLineDelay(25, 600)
        .addLineDelay(16, 700)
        .addLineDelay(12, 800)
        .addLineDelay(6, 900)
        .addLineDelay(6, Integer.MAX_VALUE)
        .addLockDelay(31, Integer.MAX_VALUE)
        .addDAS(15, 500)
        .addDAS(9, 900)
        .addDAS(7, Integer.MAX_VALUE)
        .buildSpeedTable();

    private static final int[] TABLE_BGM_FADEOUT = { 280, 480, -1 };

    private static final int[] TABLE_BGM_CHANGE = { 300, 500, -1 };

    private static final String[] TABLE_CLASSIC_GRADE_NAME = {
        "", "9", "8", "7", "6", "5", "4", "3", "2", "1",
        "S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8", "S9",
        "M", "GM"
    };

    private static final String[] TABLE_SECRET_GRADE_NAME = {
        "9", "8", "7", "6", "5", "4", "3", "2", "1",
        "S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8", "S9",
        "GM"
    };

    private static final int HEADER_COLOUR = EventReceiver.COLOR_BLUE;

    private static String getAER(int left, int right) {
        return left + " OF " + right;
    }

    private static String getClassicGrade(int left, int right) {
        return TABLE_CLASSIC_GRADE_NAME[left + right];
    }

    private static final int LEVEL_LIMIT = 999;
    private static final int SECTION_LIMIT = 10;

    /**
     * Credits roll time. Thanks Nightshade!
     */
    private static final int ROLL_TIME_LIMIT = 3970;

    /**
     * Technically, this gives a bonus to the left-grade.
     */
    private static final int TIME_LIMIT_TEN_OF_TEN = (356 * 60);

    /**
     * In the modified ruleset, the point quota is global.
     */
    private static final int FULL_GAME_QUOTA_LIMIT = 9000;

    // NOTE: 0-199 is one section, not two!
    private static final int[] SECTION_COOL_TIMES = {
        (76 * 60),
        (39 * 60),
        (40 * 60),
        (40 * 60),
        (39 * 60),
        (35 * 60),
        (30 * 60),
        (29 * 60),
        (28 * 60)
    };

    private static final int RANKING_MAX = 10;

    private static final int SECTION_MAX = 10;

    // Credit headings
    private static final String[] CREDIT_HEADINGS = {
        "MODE CREATOR:",
        "BASIS:",
        "SPECIAL THANKS GOES TO",
        "CONGRATULATIONS!"
    };

    // Credit texts
    private static final String[] CREDIT_TEXTS = {
        "AZULLIA.",
        "AE-NORMAL.",
        "NIGHTSHADE, AKARI, MANDL27.",
        "YOU HAVE COMPLETED GRADE MANIA 4!"
    };

    private static final String[] headingAER = {
        "YOUR AER",
        "YOU ARE",
        "YOU AER"
    };

    private static final String headingClassic = "YOUR GRADE";

    private static int getFireworkLaunchCount(int totalGrade) {
        if (totalGrade >= 20) return 50;
        else if (totalGrade == 19) return 25;
        else return totalGrade;
    }

    // Settings
    private int version;
    private boolean useModepileRuleset;
    private int startLevel;
    private boolean showGrade;
    private boolean useClassicGrades;
    private boolean showSectionTime;
    private boolean always20g;
    private boolean alwaysExtra;
    private boolean hardDropEffect;
    private boolean animatedBackgrounds;
    private boolean sparkEffect;

    // Playtime Variables
    private int leftGrade;
    private int rightGrade;
    private int[] sectionTime;
    private boolean[] sectionAllClearAchieved;
    private int[] sectionPoints;
    private int fullGameQuota;
    private int gradeFlash;
    private int bgmLevel;
    private int secretGrade;
    private boolean rollStarted;
    private int rollTime;
    private boolean rollClear;
    private int nextSectionLevel;
    private boolean levelUpFlag;
    private RuleOptions engineBaseRules;
    private RuleOptions engineExtraRules;
    private int gradePresentTextIndex;

    private ScrollingMarqueeText creditText;

    private CustomResourceHolder customGraphics;
    private RendererExtension rendererExtension;
    private Fireworks fireworks;

    private List<int[]> pCoordList;
    private Piece cPiece;

    private List<int[]> sCoordList;

    private ProfileProperties playerProperties;
    private boolean showPlayerStats;
    private String playerName;

    private int rankingRank;
    private int[][] rankingGradeLeft, rankingGradeRight, rankingLevel, rankingTime;

    private int rankingRankPlayer;
    private int[][] rankingGradeLeftPlayer, rankingGradeRightPlayer, rankingLevelPlayer, rankingTimePlayer;

    private int rankingBoard() {
        return useModepileRuleset ? 1 : 0;
    }

    private AnimatedBackgroundHook[] animBgInstances;

    private Random fireworkRandomiser;
    private int fireworksLeft;

    private Random sparksRandomiser;
    private SurfaceSparks sparks;

    // Grade recognition system.
    private int getLeftGrade(GameEngine engine) {
        if (useModepileRuleset) {
            return Math.min(9, fullGameQuota / 1000) + (((engine.statistics.level >= LEVEL_LIMIT) && (engine.statistics.time <= TIME_LIMIT_TEN_OF_TEN)) ? 1 : 0);
        } else {
            int count = 0;

            for (int sectionPoint : sectionPoints) {
                if (sectionPoint >= 1000) ++count;
            }

            if ((engine.statistics.level >= LEVEL_LIMIT) && (engine.statistics.time <= TIME_LIMIT_TEN_OF_TEN)) ++count;

            return count;
        }
    }

    private int getRightGrade(GameEngine engine) {
        return Math.min(9, engine.statistics.level / 111) + (rollClear ? 1 : 0);
    }

    private int getCombinedGrade(GameEngine engine) {
        return getLeftGrade(engine) + getRightGrade(engine);
    }

    private GameManager owner;
    private EventReceiver receiver;

    @Override
    public String getName() {
        return "GRADE MANIA 4";
    }

    @Override
    public void playerInit(GameEngine engine, int playerID) {
        owner = engine.owner;
        receiver = engine.owner.receiver;

        if (animBgInstances == null) {
            animBgInstances = new AnimatedBackgroundHook[SECTION_MAX];

            animBgInstances[0] = new BackgroundCircularRipple(0, BackgroundCircularRipple.DEF_FIELD_DIM, BackgroundCircularRipple.DEF_FIELD_DIM, null, null, BackgroundCircularRipple.DEF_WAVELENGTH, BackgroundCircularRipple.DEF_WAVESPEED, 180, BackgroundCircularRipple.DEF_BASE_SCALE, 0.5f);
            animBgInstances[1] = new BackgroundVerticalBars(1, 60, 160, 1f, 4f, false);
            animBgInstances[2] = new BackgroundDiagonalRipple(2, 8, 8, 60, 1f, 2f, false, false);
            animBgInstances[3] = new BackgroundHorizontalBars(3, 60, 120, 1f, 4f, false);
            animBgInstances[4] = new BackgroundDiagonalRipple(4, 8, 8, 60, 1f, 2f, false, true);
            animBgInstances[5] = new BackgroundVerticalBars(5, 60, 160, 1f, 4f, true);
            animBgInstances[6] = new BackgroundDiagonalRipple(6, 8, 8, 60, 1f, 2f, true, false);
            animBgInstances[7] = new BackgroundHorizontalBars(7, 60, 120, 1f, 4f, true);
            animBgInstances[8] = new BackgroundDiagonalRipple(8, 8, 8, 60, 1f, 2f, true, true);
            animBgInstances[9] = new BackgroundFakeScanlines(9);
        }

        SoundLoader.loadSoundset(SoundLoader.LOADTYPE_FIREWORKS);

        engineBaseRules = engine.ruleopt;
        engineExtraRules = new RuleOptions(engine.ruleopt);

        engineExtraRules.harddropEnable = true;
        engineExtraRules.harddropLock = true;
        engineExtraRules.harddropLimit = true;

        engineExtraRules.softdropEnable = true;
        engineExtraRules.softdropLock = false;
        engineExtraRules.softdropSurfaceLock = false;
        engineExtraRules.softdropLimit = true;

        useModepileRuleset = false;
        startLevel = 0;
        showGrade = false;
        useClassicGrades = false;
        showSectionTime = false;
        always20g = false;
        alwaysExtra = false;
        hardDropEffect = true;
        animatedBackgrounds = false;
        sparkEffect = true;

        leftGrade = 0;
        rightGrade = 0;
        sectionTime = new int[SECTION_LIMIT - 1];
        sectionAllClearAchieved = new boolean[SECTION_LIMIT - 1];
        sectionPoints = new int[SECTION_LIMIT - 1];
        fullGameQuota = 0;
        gradeFlash = 0;
        bgmLevel = 0;
        secretGrade = 0;
        rollStarted = false;
        rollTime = 0;
        rollClear = false;
        nextSectionLevel = 0;
        levelUpFlag = true;
        gradePresentTextIndex = 0;

        engine.ghost = true;

        creditText = new ScrollingMarqueeText(CREDIT_HEADINGS, CREDIT_TEXTS, EventReceiver.COLOR_ORANGE, EventReceiver.COLOR_WHITE);

        customGraphics = new CustomResourceHolder();
        rendererExtension = new RendererExtension(customGraphics);

        pCoordList = new LinkedList<>();
        cPiece = null;

        sCoordList = new LinkedList<>();

        engine.comboType = GameEngine.COMBO_TYPE_DISABLE;
        engine.framecolor = GameEngine.FRAME_COLOR_BLUE;

        owner.backgroundStatus.bg = 0;
        owner.backgroundStatus.fadebg = 0;

        engine.staffrollEnable = true;
        engine.staffrollNoDeath = false;

        if (playerProperties == null) {
            playerProperties = new ProfileProperties(HEADER_COLOUR);
            showPlayerStats = false;
        }

        rankingRank = -1;
        rankingGradeLeft = new int[2][RANKING_MAX];
        rankingGradeRight = new int[2][RANKING_MAX];
        rankingLevel = new int[2][RANKING_MAX];
        rankingTime = new int[2][RANKING_MAX];

        rankingRankPlayer = -1;
        rankingGradeLeftPlayer = new int[2][RANKING_MAX];
        rankingGradeRightPlayer = new int[2][RANKING_MAX];
        rankingLevelPlayer = new int[2][RANKING_MAX];
        rankingTimePlayer = new int[2][RANKING_MAX];

        if (!owner.replayMode) {
            loadSetting(owner.modeConfig);
            loadRanking(owner.modeConfig, engine.ruleopt.strRuleName);

            version = CURRENT_VERSION;

            if (playerProperties.isLoggedIn()) {
                loadSettingPlayer(playerProperties);
                loadRankingPlayer(playerProperties, engine.ruleopt.strRuleName);
            }

            playerName = "";
        } else {
            loadSetting(owner.replayProp);

            playerName = owner.replayProp.getProperty("grademania4.playerName", "");
            version = owner.replayProp.getProperty("grademania4.version", CURRENT_VERSION);
        }
    }

    private void loadSetting(CustomProperties prop) {
        useModepileRuleset = prop.getProperty("grademania4.customRule", false);
        startLevel = prop.getProperty("grademania4.startLevel", 0);
        showGrade = prop.getProperty("grademania4.showGrade", false);
        useClassicGrades = prop.getProperty("grademania4.useClassicGrades", false);
        showSectionTime = prop.getProperty("grademania4.showSectionTime", false);
        always20g = prop.getProperty("grademania4.always20g", false);
        alwaysExtra = prop.getProperty("grademania4.alwaysExtra", false);
        hardDropEffect = prop.getProperty("grademania4.hardDropEffect", true);
        animatedBackgrounds = prop.getProperty("grademania4.animatedBackgrounds", false);
        sparkEffect = prop.getProperty("grademania4.sparkEffect", true);
    }

    private void saveSetting(CustomProperties prop) {
        prop.setProperty("grademania4.customRule", useModepileRuleset);
        prop.setProperty("grademania4.startLevel", startLevel);
        prop.setProperty("grademania4.showGrade", showGrade);
        prop.setProperty("grademania4.useClassicGrades", useClassicGrades);
        prop.setProperty("grademania4.showSectionTime", showSectionTime);
        prop.setProperty("grademania4.always20g", always20g);
        prop.setProperty("grademania4.alwaysExtra", alwaysExtra);
        prop.setProperty("grademania4.hardDropEffect", hardDropEffect);
        prop.setProperty("grademania4.animatedBackgrounds", animatedBackgrounds);
        prop.setProperty("grademania4.sparkEffect", sparkEffect);
    }

    private void loadSettingPlayer(ProfileProperties prop) {
        if (!prop.isLoggedIn()) return;

        useModepileRuleset = prop.getProperty("grademania4.customRule", false);
        startLevel = prop.getProperty("grademania4.startLevel", 0);
        showGrade = prop.getProperty("grademania4.showGrade", false);
        useClassicGrades = prop.getProperty("grademania4.useClassicGrades", false);
        showSectionTime = prop.getProperty("grademania4.showSectionTime", false);
        always20g = prop.getProperty("grademania4.always20g", false);
        alwaysExtra = prop.getProperty("grademania4.alwaysExtra", false);
        hardDropEffect = prop.getProperty("grademania4.hardDropEffect", true);
        animatedBackgrounds = prop.getProperty("grademania4.animatedBackgrounds", false);
        sparkEffect = prop.getProperty("grademania4.sparkEffect", true);
    }

    private void saveSettingPlayer(ProfileProperties prop) {
        if (!prop.isLoggedIn()) return;

        prop.setProperty("grademania4.customRule", useModepileRuleset);
        prop.setProperty("grademania4.startLevel", startLevel);
        prop.setProperty("grademania4.showGrade", showGrade);
        prop.setProperty("grademania4.useClassicGrades", useClassicGrades);
        prop.setProperty("grademania4.showSectionTime", showSectionTime);
        prop.setProperty("grademania4.always20g", always20g);
        prop.setProperty("grademania4.alwaysExtra", alwaysExtra);
        prop.setProperty("grademania4.hardDropEffect", hardDropEffect);
        prop.setProperty("grademania4.animatedBackgrounds", animatedBackgrounds);
        prop.setProperty("grademania4.sparkEffect", sparkEffect);
    }

    private void loadRanking(CustomProperties prop, String ruleName) {
        for (int i = 0; i < RANKING_MAX; ++i) {
            rankingGradeLeft[0][i] = prop.getProperty("grademania4.ranking.original." + ruleName + "." + version + ".gradeL." + i, 0);
            rankingGradeRight[0][i] = prop.getProperty("grademania4.ranking.original." + ruleName + "." + version + ".gradeR." + i, 0);
            rankingLevel[0][i] = prop.getProperty("grademania4.ranking.original." + ruleName + "." + version + ".level." + i, 0);
            rankingTime[0][i] = prop.getProperty("grademania4.ranking.original." + ruleName + "." + version + ".time." + i, 0);

            rankingGradeLeft[1][i] = prop.getProperty("grademania4.ranking.modepile." + ruleName + "." + version + ".gradeL." + i, 0);
            rankingGradeRight[1][i] = prop.getProperty("grademania4.ranking.modepile." + ruleName + "." + version + ".gradeR." + i, 0);
            rankingLevel[1][i] = prop.getProperty("grademania4.ranking.modepile." + ruleName + "." + version + ".level." + i, 0);
            rankingTime[1][i] = prop.getProperty("grademania4.ranking.modepile." + ruleName + "." + version + ".time." + i, 0);
        }
    }

    private void saveRanking(CustomProperties prop, String ruleName) {
        for (int i = 0; i < RANKING_MAX; ++i) {
            prop.setProperty("grademania4.ranking.original." + ruleName + "." + version + ".gradeL." + i, rankingGradeLeft[0][i]);
            prop.setProperty("grademania4.ranking.original." + ruleName + "." + version + ".gradeR." + i, rankingGradeRight[0][i]);
            prop.setProperty("grademania4.ranking.original." + ruleName + "." + version + ".level." + i, rankingLevel[0][i]);
            prop.setProperty("grademania4.ranking.original." + ruleName + "." + version + ".time." + i, rankingTime[0][i]);

            prop.setProperty("grademania4.ranking.modepile." + ruleName + "." + version + ".gradeL." + i, rankingGradeLeft[1][i]);
            prop.setProperty("grademania4.ranking.modepile." + ruleName + "." + version + ".gradeR." + i, rankingGradeRight[1][i]);
            prop.setProperty("grademania4.ranking.modepile." + ruleName + "." + version + ".level." + i, rankingLevel[1][i]);
            prop.setProperty("grademania4.ranking.modepile." + ruleName + "." + version + ".time." + i, rankingTime[1][i]);
        }
    }

    private void loadRankingPlayer(ProfileProperties prop, String ruleName) {
        if (!prop.isLoggedIn()) return;

        for (int i = 0; i < RANKING_MAX; ++i) {
            rankingGradeLeftPlayer[0][i] = prop.getProperty("grademania4.ranking.original." + ruleName + "." + version + ".gradeL." + i, 0);
            rankingGradeRightPlayer[0][i] = prop.getProperty("grademania4.ranking.original." + ruleName + "." + version + ".gradeR." + i, 0);
            rankingLevelPlayer[0][i] = prop.getProperty("grademania4.ranking.original." + ruleName + "." + version + ".level." + i, 0);
            rankingTimePlayer[0][i] = prop.getProperty("grademania4.ranking.original." + ruleName + "." + version + ".time." + i, 0);

            rankingGradeLeftPlayer[1][i] = prop.getProperty("grademania4.ranking.modepile." + ruleName + "." + version + ".gradeL." + i, 0);
            rankingGradeRightPlayer[1][i] = prop.getProperty("grademania4.ranking.modepile." + ruleName + "." + version + ".gradeR." + i, 0);
            rankingLevelPlayer[1][i] = prop.getProperty("grademania4.ranking.modepile." + ruleName + "." + version + ".level." + i, 0);
            rankingTimePlayer[1][i] = prop.getProperty("grademania4.ranking.modepile." + ruleName + "." + version + ".time." + i, 0);
        }
    }

    private void saveRankingPlayer(ProfileProperties prop, String ruleName) {
        if (!prop.isLoggedIn()) return;

        for (int i = 0; i < RANKING_MAX; ++i) {
            prop.setProperty("grademania4.ranking.original." + ruleName + "." + version + ".gradeL." + i, rankingGradeLeftPlayer[0][i]);
            prop.setProperty("grademania4.ranking.original." + ruleName + "." + version + ".gradeR." + i, rankingGradeRightPlayer[0][i]);
            prop.setProperty("grademania4.ranking.original." + ruleName + "." + version + ".level." + i, rankingLevelPlayer[0][i]);
            prop.setProperty("grademania4.ranking.original." + ruleName + "." + version + ".time." + i, rankingTimePlayer[0][i]);

            prop.setProperty("grademania4.ranking.modepile." + ruleName + "." + version + ".gradeL." + i, rankingGradeLeftPlayer[1][i]);
            prop.setProperty("grademania4.ranking.modepile." + ruleName + "." + version + ".gradeR." + i, rankingGradeRightPlayer[1][i]);
            prop.setProperty("grademania4.ranking.modepile." + ruleName + "." + version + ".level." + i, rankingLevelPlayer[1][i]);
            prop.setProperty("grademania4.ranking.modepile." + ruleName + "." + version + ".time." + i, rankingTimePlayer[1][i]);
        }
    }

    @Override
    public void saveReplay(GameEngine engine, int playerID, CustomProperties prop) {
        saveSetting(owner.replayProp);

        // Update rankings
        if ((!owner.replayMode) && (startLevel == 0) && (!alwaysExtra) && (!always20g) && (engine.ai == null)) {
            updateRanking(getLeftGrade(engine), getRightGrade(engine), engine.statistics.level, engine.statistics.time);

            if (playerProperties.isLoggedIn()) {
                prop.setProperty("grademania4.playerName", playerProperties.getNameDisplay());
            }

            if ((rankingRank != -1)) {
                saveRanking(owner.modeConfig, engine.ruleopt.strRuleName);
                receiver.saveModeConfig(owner.modeConfig);
            }

            if (rankingRankPlayer != -1 && playerProperties.isLoggedIn()) {
                saveRankingPlayer(playerProperties, engine.ruleopt.strRuleName);
                playerProperties.saveProfileConfig();
            }
        }
    }

    private void updateRanking(int gradeL, int gradeR, int level, int time) {
        rankingRank = checkRanking(gradeL, gradeR, level, time);

        if (rankingRank != -1) {
            for (int i = RANKING_MAX - 1; i > rankingRank; i--) {
                rankingGradeLeft[rankingBoard()][i] = rankingGradeLeft[rankingBoard()][i - 1];
                rankingGradeRight[rankingBoard()][i] = rankingGradeRight[rankingBoard()][i - 1];
                rankingLevel[rankingBoard()][i] = rankingLevel[rankingBoard()][i - 1];
                rankingTime[rankingBoard()][i] = rankingTime[rankingBoard()][i - 1];
            }

            rankingGradeLeft[rankingBoard()][rankingRank] = gradeL;
            rankingGradeRight[rankingBoard()][rankingRank] = gradeR;
            rankingLevel[rankingBoard()][rankingRank] = level;
            rankingTime[rankingBoard()][rankingRank] = time;
        }

        if (playerProperties.isLoggedIn()) {
            rankingRankPlayer = checkRankingPlayer(gradeL, gradeR, level, time);

            if (rankingRankPlayer != -1) {
                for (int i = RANKING_MAX - 1; i > rankingRankPlayer; i--) {
                    rankingGradeLeftPlayer[rankingBoard()][i] = rankingGradeLeftPlayer[rankingBoard()][i - 1];
                    rankingGradeRightPlayer[rankingBoard()][i] = rankingGradeRightPlayer[rankingBoard()][i - 1];
                    rankingLevelPlayer[rankingBoard()][i] = rankingLevelPlayer[rankingBoard()][i - 1];
                    rankingTimePlayer[rankingBoard()][i] = rankingTimePlayer[rankingBoard()][i - 1];
                }

                rankingGradeLeftPlayer[rankingBoard()][rankingRankPlayer] = gradeL;
                rankingGradeRightPlayer[rankingBoard()][rankingRankPlayer] = gradeR;
                rankingLevelPlayer[rankingBoard()][rankingRankPlayer] = level;
                rankingTimePlayer[rankingBoard()][rankingRankPlayer] = time;
            }
        }
    }

    private int checkRanking(int gradeL, int gradeR, int level, int time) {
        for (int i = 0; i < RANKING_MAX; ++i) {
            if (gradeL + gradeR > rankingGradeLeft[rankingBoard()][i] + rankingGradeRight[rankingBoard()][i]) return i;
            else if (gradeL + gradeR == rankingGradeLeft[rankingBoard()][i] + rankingGradeRight[rankingBoard()][i] && level > rankingLevel[rankingBoard()][i]) return i;
            else if (gradeL + gradeR == rankingGradeLeft[rankingBoard()][i] + rankingGradeRight[rankingBoard()][i] && level == rankingLevel[rankingBoard()][i] && time < rankingTime[rankingBoard()][i]) return i;
        }

        return -1;
    }

    private int checkRankingPlayer(int gradeL, int gradeR, int level, int time) {
        for (int i = 0; i < RANKING_MAX; ++i) {
            if (gradeL + gradeR > rankingGradeLeftPlayer[rankingBoard()][i] + rankingGradeRightPlayer[rankingBoard()][i]) return i;
            else if (gradeL + gradeR == rankingGradeLeftPlayer[rankingBoard()][i] + rankingGradeRightPlayer[rankingBoard()][i] && level > rankingLevelPlayer[rankingBoard()][i]) return i;
            else if (gradeL + gradeR == rankingGradeLeftPlayer[rankingBoard()][i] + rankingGradeRightPlayer[rankingBoard()][i] && level == rankingLevelPlayer[rankingBoard()][i] && time < rankingTimePlayer[rankingBoard()][i]) return i;
        }


        return -1;
    }

    private static final int EXTRA_ARE = 6;

    private void setSpeed(GameEngine engine) {
        final SpeedParam speed = SPEED_TABLE.apply(engine.statistics.level);

        engine.speed = speed;

        if (always20g) engine.speed.gravity = -1;

        // Extra shortens ARE.
        if (alwaysExtra || engine.ctrl.isPress(Controller.BUTTON_F)) {
            engine.speed.are = Math.min(EXTRA_ARE, speed.are);
            engine.speed.areLine = Math.min(EXTRA_ARE, speed.areLine);
        }
    }

    private void setStartBGMLevel(GameEngine engine) {
        bgmLevel = 0;
        while (TABLE_BGM_CHANGE[bgmLevel] != -1 && engine.statistics.level >= TABLE_BGM_CHANGE[bgmLevel]) bgmLevel++;
    }

    @Override
    public boolean onSetting(GameEngine engine, int playerID) {
        if (animatedBackgrounds) {
            owner.backgroundStatus.bg = -SECTION_MAX + startLevel;
            owner.backgroundStatus.fadebg = -SECTION_MAX + startLevel;
        } else {
            owner.backgroundStatus.bg = startLevel;
            owner.backgroundStatus.fadebg = startLevel;
        }

        if (!engine.owner.replayMode) {
            // Configuration changes
            int change = updateCursor(engine, 9);

            if (change != 0) {
                engine.playSE("change");

                switch (engine.statc[2]) {
                    case 0:
                        useModepileRuleset = !useModepileRuleset;
                        break;
                    case 1:
                        startLevel += change;
                        if (startLevel < 0) startLevel = 9;
                        if (startLevel > 9) startLevel = 0;
                        break;
                    case 2:
                        showGrade = !showGrade;
                        break;
                    case 3:
                        useClassicGrades = !useClassicGrades;
                        break;
                    case 4:
                        showSectionTime = !showSectionTime;
                        break;
                    case 5:
                        always20g = !always20g;
                        break;
                    case 6:
                        alwaysExtra = !alwaysExtra;
                        break;
                    case 7:
                        hardDropEffect = !hardDropEffect;
                        break;
                    case 8:
                        animatedBackgrounds = !animatedBackgrounds;
                        break;
                    case 9:
                        sparkEffect = !sparkEffect;
                        break;
                }
            }

            if (engine.ctrl.isPush(Controller.BUTTON_A) && (engine.statc[3] >= 5)) {
                engine.playSE("decide");

                if (playerProperties.isLoggedIn()) {
                    saveSettingPlayer(playerProperties);
                    playerProperties.saveProfileConfig();
                } else {
                    saveSetting(owner.modeConfig);
                    receiver.saveModeConfig(owner.modeConfig);
                }

                if (animatedBackgrounds) {
                    for (AnimatedBackgroundHook bg : animBgInstances) bg.reset();
                }

                return false;
            }

            if (engine.ctrl.isPush(Controller.BUTTON_B)) {
                engine.quitflag = true;
                playerProperties = new ProfileProperties(HEADER_COLOUR);
            }

            if (engine.ctrl.isPush(Controller.BUTTON_E) && engine.ai == null) {
                playerProperties = new ProfileProperties(HEADER_COLOUR);
                engine.playSE("decide");

                engine.stat = GameEngine.STAT_CUSTOM;
                engine.resetStatc();
                return true;
            }


            engine.statc[3]++;
        } else {
            engine.statc[3]++;
            engine.statc[2] = -1;

            if (engine.statc[3] >= 60) {
                if (animatedBackgrounds) {
                    for (AnimatedBackgroundHook bg : animBgInstances) bg.reset();
                }

                return false;
            }
        }

        return true;
    }

    @Override
    public void renderSetting(GameEngine engine, int playerID) {
        drawMenu(engine, playerID, receiver, 0, EventReceiver.COLOR_YELLOW, 0,
            "VARIANT", useModepileRuleset ? "MODEPILE" : "ORIGINAL"
        );
        drawMenu(engine, playerID, receiver, 2, EventReceiver.COLOR_RED, 1,
            "LEVEL", String.valueOf(startLevel * 100)
        );
        drawMenu(engine, playerID, receiver, 4, EventReceiver.COLOR_GREEN, 2,
            "SHOW GRADE", GeneralUtil.getONorOFF(showGrade),
            "CLASSIC GRS", GeneralUtil.getONorOFF(useClassicGrades),
            "SHOW STIME", GeneralUtil.getONorOFF(showSectionTime)
        );
        drawMenu(engine, playerID, receiver, 10, EventReceiver.COLOR_BLUE, 5,
            "20G MODE", GeneralUtil.getONorOFF(always20g),
            "EXTRA MODE", GeneralUtil.getONorOFF(alwaysExtra)
        );
        drawMenu(engine, playerID, receiver, 14, EventReceiver.COLOR_PINK, 7,
            "DROP EFF.", GeneralUtil.getONorOFF(hardDropEffect),
            "ANIM. BGS.", GeneralUtil.getONorOFF(animatedBackgrounds),
            "SPARKS", GeneralUtil.getONorOFF(sparkEffect)
        );
    }

    @Override
    public boolean onCustom(GameEngine engine, int playerID) {
        if (animatedBackgrounds) {
            owner.backgroundStatus.bg = -SECTION_MAX + startLevel;
            owner.backgroundStatus.fadebg = -SECTION_MAX + startLevel;
        } else {
            owner.backgroundStatus.bg = startLevel;
            owner.backgroundStatus.fadebg = startLevel;
        }

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

        final SpeedParam currentParam = SPEED_TABLE.apply(engine.statistics.level);

        if (engine.ctrl.isPress(Controller.BUTTON_F) || alwaysExtra) {
            engine.ruleopt = engineExtraRules;
            engine.speed.das = Math.min(EXTRA_ARE, currentParam.das);
        } else {
            engine.ruleopt = engineBaseRules;
            engine.speed.das = currentParam.das;
        }
    }

    @Override
    public void renderFirst(GameEngine engine, int playerID) {
        if (animatedBackgrounds && engine.owner.backgroundStatus.bg < 0) {
            animBgInstances[engine.owner.backgroundStatus.bg + SECTION_MAX].draw(engine, playerID);
        }
    }

    @Override
    public boolean onReady(GameEngine engine, int playerID) {
        if (engine.statc[0] == 0 && animatedBackgrounds) {
            engine.owner.backgroundStatus.bg = -SECTION_MAX + startLevel;
            for (AnimatedBackgroundHook bg : animBgInstances) {
                bg.reset();
            }
        } else if (!animatedBackgrounds) {
            engine.owner.backgroundStatus.bg = startLevel;
            engine.owner.backgroundStatus.fadebg = startLevel;
        }

        if (engine.statc[0] == 0) {
            leftGrade = 0;
            rightGrade = 0;
            fullGameQuota = 0;

            for (int i = 0; i < sectionPoints.length; ++i) {
                sectionPoints[i] = 0;
                sectionTime[i] = 0;
                sectionAllClearAchieved[i] = false;
            }
        }

        secretGrade = -1;

        setSpeed(engine);
        return false;
    }

    @Override
    public void startGame(GameEngine engine, int playerID) {
        super.startGame(engine, playerID);
        engine.statistics.level = startLevel * 100;

        nextSectionLevel = startLevel * 100 + 100;
        if (engine.statistics.level <= 0) nextSectionLevel = 100;
        if (engine.statistics.level >= 900) nextSectionLevel = LEVEL_LIMIT;

        levelUpFlag = true;

        fireworkRandomiser = new Random(engine.randSeed);
        fireworks = new Fireworks(customGraphics, fireworkRandomiser);
        fireworksLeft = 0;

        sparksRandomiser = new Random(engine.randSeed * 2);
        sparks = new SurfaceSparks(customGraphics, sparksRandomiser);

        setStartBGMLevel(engine);
        levelUp(engine);
        owner.bgmStatus.bgm = bgmLevel;
    }

    private boolean hasMovedFrame = false;

    @Override
    public boolean onMove(GameEngine engine, int playerID) {
        if ((engine.ending == 0) && (engine.statc[0] == 0) && (!engine.holdDisable) && (!levelUpFlag)) {
            if (engine.statistics.level < nextSectionLevel - 1) {
                engine.statistics.level++;
                if (engine.statistics.level == nextSectionLevel - 1)
                    engine.playSE("levelstop");
            }

            levelUp(engine);
        }

        if (sparkEffect &&
            engine.nowPieceObject != null &&
            engine.getMoveDirection() != 0 &&
            !hasMovedFrame &&
            (engine.dasCount == 0 || engine.dasCount >= engine.getDAS())) {
            hasMovedFrame = true;

            int baseX = (16 * engine.nowPieceX) + 4 + receiver.getFieldDisplayPositionX(engine, playerID);
            int baseY = (16 * engine.nowPieceY) + 52 + receiver.getFieldDisplayPositionY(engine, playerID);

            if (engine.nowPieceObject.checkCollision(engine.nowPieceX, engine.nowPieceY + 1, engine.field) &&
                !engine.nowPieceObject.checkCollision(engine.nowPieceX + engine.getMoveDirection(), engine.nowPieceY, engine.field)) {
                final int[] pieceDataY = engine.nowPieceObject.dataY[engine.nowPieceObject.direction];
                final int lowY = Arrays.stream(pieceDataY).max().getAsInt();

                sCoordList.clear();
                for (int i = 0; i < engine.nowPieceObject.getMaxBlock(); ++i) {
                    if (engine.nowPieceObject.dataY[engine.nowPieceObject.direction][i] == lowY) {
                        int realX = engine.nowPieceX + engine.nowPieceObject.dataX[engine.nowPieceObject.direction][i];
                        int realY = engine.nowPieceY + engine.nowPieceObject.dataY[engine.nowPieceObject.direction][i];

                        if (!engine.field.getBlockEmptyF(realX, realY + 1)) {
                            sparks.addNumber(16, new SurfaceSparks.Parameters(
                                baseX + 16 * engine.nowPieceObject.dataX[engine.nowPieceObject.direction][i],
                                baseX + 16 * (engine.nowPieceObject.dataX[engine.nowPieceObject.direction][i] + 1),
                                baseY + 16 * (lowY + 1),
                                engine.getMoveDirection() * -1)
                            );
                        }
                    }
                }
            }
        } else {
            hasMovedFrame = false;
        }

        if ((engine.ending == 0) && (engine.statc[0] > 0)) {
            levelUpFlag = false;
        }

        if ((engine.ending == 2) && (!rollStarted)) {
            rollStarted = true;

            engine.blockHidden = 300;
            engine.blockHiddenAnim = true;
            engine.blockOutlineType = GameEngine.BLOCK_OUTLINE_NONE;

            owner.bgmStatus.bgm = BGMStatus.BGM_ENDING1;
        }

        return false;
    }

    @Override
    public boolean onARE(GameEngine engine, int playerID) {
        if ((engine.ending == 0  && (engine.statc[ 0 ]>= engine.statc[1] - 1) && (!levelUpFlag))) {
            if (engine.statistics.level < nextSectionLevel - 1) {
                engine.statistics.level++;
                if (engine.statistics.level == nextSectionLevel - 1)
                    engine.playSE("levelstop");
            }

            levelUp(engine);
            levelUpFlag = true;
        }

        return false;
    }

    private void updateBGPulseFrames(GameEngine engine, int barFrames, int rippleFrames, float scPeriodMult) {
        for (AnimatedBackgroundHook bg : animBgInstances) {
            if (bg instanceof BackgroundHorizontalBars) {
                ((BackgroundHorizontalBars) bg).modifyValues(barFrames, null, null, null);
            } else if (bg instanceof BackgroundVerticalBars) {
                ((BackgroundVerticalBars) bg).modifyValues(barFrames, null, null, null);
            } else if (bg instanceof BackgroundDiagonalRipple) {
                ((BackgroundDiagonalRipple) bg).modifyValues(barFrames, null, null, null, null);
            } else if (bg instanceof BackgroundCircularRipple) {
                ((BackgroundCircularRipple) bg).modifyValues(null, (hardDropEffect && engine.ending == 0) ? 0 : rippleFrames, null, null, null, null, null);
            } else if (bg instanceof BackgroundFakeScanlines) {
                ((BackgroundFakeScanlines) bg).updatePhaseMult(scPeriodMult);
            }
        }
    }

    private void levelUp(GameEngine engine) {
        if (engine.statistics.level < 900) {
            engine.meterValue = ((engine.statistics.level % 100) * receiver.getMeterMax(engine)) / 99;
        } else {
            engine.meterValue = ((engine.statistics.level % 100) * receiver.getMeterMax(engine)) / 98;
        }

        if (animatedBackgrounds && (engine.statistics.level % 100 > 50) && engine.ending <= 0) {
            updateBGPulseFrames(engine, 40, 120, 2f);
        } else if (animatedBackgrounds) {
            updateBGPulseFrames(engine, 60, 180, 1f);
        }

        engine.meterColor = GameEngine.METER_COLOR_GREEN;
        if (engine.statistics.level % 100 >= 50) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
        if (engine.statistics.level % 100 >= 80) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
        if (engine.statistics.level == nextSectionLevel - 1) engine.meterColor = GameEngine.METER_COLOR_RED;

        setSpeed(engine);

        if (TABLE_BGM_FADEOUT[bgmLevel] != -1 && engine.statistics.level >= TABLE_BGM_FADEOUT[bgmLevel]) {
            owner.bgmStatus.fadesw = true;
        }
    }

    @Override
    public void calcScore(GameEngine engine, int playerID, int lines) {
        if (lines >= 1 && engine.ending == 0) {
            int currentSection = engine.statistics.level / 100 - 1;
            if (currentSection < 0) currentSection = 0;

            if (lines < 3) {
                engine.statistics.level += lines;
                if (useModepileRuleset) fullGameQuota += 10 * lines;
            } else if (lines == 3) {
                engine.statistics.level += 4;
                if (useModepileRuleset) fullGameQuota += 50;
            } else {
                engine.statistics.level += lines + (lines >>> 1);

                if (useModepileRuleset) fullGameQuota += 175;
                else sectionPoints[currentSection] += 175;
            }

            if (fullGameQuota > FULL_GAME_QUOTA_LIMIT) fullGameQuota = FULL_GAME_QUOTA_LIMIT;

            // AC bonus
            if (engine.field.isEmpty()) {
                engine.playSE("bravo");

                if (!sectionAllClearAchieved[currentSection]) {
                    sectionAllClearAchieved[currentSection] = true;
                    sectionPoints[currentSection] += 350;
                }
            }

            levelUp(engine);

            if (engine.statistics.level >= LEVEL_LIMIT) {
                engine.statistics.level = LEVEL_LIMIT;
                engine.timerActive = false;
                engine.ending = 1;

                if (sectionTime[8] <= SECTION_COOL_TIMES[8]) {
                    sectionAllClearAchieved[8] = true;
                }
            } else if (engine.statistics.level >= nextSectionLevel) {
                engine.playSE("levelup");

                // Update Section "COOL"
                if (engine.statistics.level >= 200) {
                    int section = engine.statistics.level / 100 - 2;
                    if (sectionTime[section] <= SECTION_COOL_TIMES[section]) {
                        sectionPoints[section] += 125;

                        if (showGrade) engine.playSE("cool");
                    }
                }

                owner.backgroundStatus.fadesw = true;
                owner.backgroundStatus.fadecount = 0;

                owner.backgroundStatus.fadebg = nextSectionLevel / 100;

                if (animatedBackgrounds) {
                    owner.backgroundStatus.bg = owner.backgroundStatus.fadebg - SECTION_MAX;
                    owner.backgroundStatus.fadebg = owner.backgroundStatus.fadebg - SECTION_MAX;
                }

                if ((TABLE_BGM_FADEOUT[bgmLevel] != -1) && (engine.statistics.level >= TABLE_BGM_CHANGE[bgmLevel])) {
                    bgmLevel++;
                    owner.bgmStatus.fadesw = false;
                    owner.bgmStatus.bgm = bgmLevel;
                }

                // Update level for next section
                nextSectionLevel += 100;
                if (nextSectionLevel > LEVEL_LIMIT) nextSectionLevel = LEVEL_LIMIT;
            } else if ((engine.statistics.level == nextSectionLevel - 1)) {
                engine.playSE("levelstop");
            }
        }
    }

    @Override
    public void onLast(GameEngine engine, int playerID) {
        final int oldGrade = leftGrade + rightGrade;

        leftGrade = getLeftGrade(engine);
        rightGrade = getRightGrade(engine);

        final int newGrade = leftGrade + rightGrade;

        if (newGrade > oldGrade && showGrade) {
            gradeFlash = 180;
            engine.playSE("gradeup");
        }

        if (gradeFlash > 0) gradeFlash--;

        if ((engine.timerActive) && (engine.ending == 0) ) {
            int section = engine.statistics.level / 100 - 1;
            if (section < 0) section = 0;

            if (section < sectionTime.length) {
                sectionTime[section]++;
            }
        }

        // Ending
        if ((engine.gameActive) && (engine.ending == 2) ) {
            rollTime++;

            int remainRollTime = ROLL_TIME_LIMIT - rollTime;
            engine.meterValue = (remainRollTime * receiver.getMeterMax(engine)) / ROLL_TIME_LIMIT;
            engine.meterColor = GameEngine.METER_COLOR_RED;
            if (remainRollTime <= 30 * 60) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
            if (remainRollTime <= 20 * 60) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
            if (remainRollTime <= 10 * 60) engine.meterColor = GameEngine.METER_COLOR_GREEN;

            if (rollTime >= ROLL_TIME_LIMIT) {
                rollClear = true;

                engine.blockOutlineType = GameEngine.BLOCK_OUTLINE_NORMAL;

                engine.gameEnded();
                engine.resetStatc();

                engine.stat = GameEngine.STAT_EXCELLENT;
                fireworksLeft = getFireworkLaunchCount(getCombinedGrade(engine));
            }
        }

        if (animatedBackgrounds && (owner.backgroundStatus.bg + SECTION_MAX < 10)) {
            animBgInstances[owner.backgroundStatus.bg + SECTION_MAX].update();
        }

        if ((engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (!owner.replayMode))) {
            // Show rank
            if (engine.ctrl.isPush(Controller.BUTTON_F) && playerProperties.isLoggedIn() && engine.stat != GameEngine.STAT_CUSTOM) {
                showPlayerStats = !showPlayerStats;
                engine.playSE("change");
            }
        }

        if (fireworks != null) fireworks.update();
        if (sparks != null) sparks.update();
    }

    private static final List<Integer> avgX = new LinkedList<>();
    private static final List<Integer> avgY = new LinkedList<>();

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

        if (hardDropEffect) {
            int x2, y2;

            avgX.clear();
            avgY.clear();

            for (int i = 0; i < cPiece.getMaxBlock(); i++) {
                if (!cPiece.big) {
                    x2 = baseX + (cPiece.dataX[cPiece.direction][i] * 16);
                    y2 = baseY + (cPiece.dataY[cPiece.direction][i] * 16);

                    rendererExtension.addBlockBreakEffect(receiver, x2, y2, cPiece.block[i]);
                } else {
                    x2 = baseX + (cPiece.dataX[cPiece.direction][i] * 32);
                    y2 = baseY + (cPiece.dataY[cPiece.direction][i] * 32);

                    rendererExtension.addBlockBreakEffect(receiver, x2, y2, cPiece.block[i]);
                    rendererExtension.addBlockBreakEffect(receiver, x2 + 16, y2, cPiece.block[i]);
                    rendererExtension.addBlockBreakEffect(receiver, x2, y2 + 16, cPiece.block[i]);
                    rendererExtension.addBlockBreakEffect(receiver, x2 + 16, y2 + 16, cPiece.block[i]);
                }

                avgX.add(x2 + 8);
                avgY.add(y2 + 8);
            }

            if (animatedBackgrounds && engine.statistics.level < 100) {
                final int avgXVal = avgX.stream().mapToInt(Integer::intValue).sum() / avgX.size();
                final int avgYVal = avgY.stream().mapToInt(Integer::intValue).sum() / avgY.size();

                ((BackgroundCircularRipple) animBgInstances[0]).manualRipple(avgXVal, avgYVal);
            }
        }
    }

    private String gradeString(int left, int right) {
        if (useClassicGrades) return TABLE_CLASSIC_GRADE_NAME[left + right];
        else return getAER(left, right).replace(" OF ", "/");
    }

    private static int rightGradeColor(int right) {
        if (right == 10) return EventReceiver.COLOR_ORANGE;
        else if (right == 9) return EventReceiver.COLOR_GREEN;
        else return EventReceiver.COLOR_WHITE;
    }

    @Override
    public void renderLast(GameEngine engine, int playerID) {
        receiver.drawScoreFont(engine, playerID, 0, 0, getName(), EventReceiver.COLOR_BLUE);
        receiver.drawScoreFont(engine, playerID, 0, 1, "(" + (useModepileRuleset ? "MODEPILE" : "ORIGINAL") + " RULES)", EventReceiver.COLOR_BLUE);

        if ((engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (!owner.replayMode))) {
            if ((!owner.replayMode) && (startLevel == 0) && (!alwaysExtra) && (!always20g) && (engine.ai == null)) {
                // Rankings
                float scale = (receiver.getNextDisplayType() == 2) ? 0.5f : 1.0f;
                int topY = (receiver.getNextDisplayType() == 2) ? 6 : 4;

                if (useClassicGrades) {
                    receiver.drawScoreFont(engine, playerID, 3, topY - 1, "GRADE  LEVEL TIME", EventReceiver.COLOR_BLUE, scale);
                } else {
                    receiver.drawScoreFont(engine, playerID, 3, topY - 1, "AER    LEVEL TIME", EventReceiver.COLOR_BLUE, scale);
                }

                if (showPlayerStats) {
                    for (int i = 0; i < RANKING_MAX; i++) {
                        receiver.drawScoreFont(engine, playerID, 0, topY + i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW, scale);
                        if (rankingRankPlayer != -1) {
                            receiver.drawScoreFont(engine, playerID, 3, topY + i, gradeString(rankingGradeLeftPlayer[rankingBoard()][i], rankingGradeRightPlayer[rankingBoard()][i]), (i == rankingRankPlayer), scale);
                        } else {
                            receiver.drawScoreFont(engine, playerID, 3, topY + i, gradeString(rankingGradeLeftPlayer[rankingBoard()][i], rankingGradeRightPlayer[rankingBoard()][i]), rightGradeColor(rankingGradeRightPlayer[rankingBoard()][i]), scale);
                        }
                        receiver.drawScoreFont(engine, playerID, 10, topY + i, String.valueOf(rankingLevelPlayer[rankingBoard()][i]), (i == rankingRankPlayer), scale);
                        receiver.drawScoreFont(engine, playerID, 16, topY + i, GeneralUtil.getTime(rankingTimePlayer[rankingBoard()][i]), (i == rankingRankPlayer), scale);
                    }

                    receiver.drawScoreFont(engine, playerID, 0, 18, "PLAYER SCORES", EventReceiver.COLOR_BLUE);
                    receiver.drawScoreFont(engine, playerID, 0, 19, playerProperties.getNameDisplay(), EventReceiver.COLOR_WHITE, 2f);

                    receiver.drawScoreFont(engine, playerID, 0, 22, "D:SWITCH RANK SCREEN", EventReceiver.COLOR_GREEN);

                } else {
                    for (int i = 0; i < RANKING_MAX; i++) {
                        receiver.drawScoreFont(engine, playerID, 0, topY + i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW, scale);
                        if (rankingRank != -1) {
                            receiver.drawScoreFont(engine, playerID, 3, topY + i, gradeString(rankingGradeLeft[rankingBoard()][i], rankingGradeRight[rankingBoard()][i]), (i == rankingRank), scale);
                        } else {
                            receiver.drawScoreFont(engine, playerID, 3, topY + i, gradeString(rankingGradeLeft[rankingBoard()][i], rankingGradeRight[rankingBoard()][i]), rightGradeColor(rankingGradeRight[rankingBoard()][i]), scale);
                        }
                        receiver.drawScoreFont(engine, playerID, 10, topY + i, String.valueOf(rankingLevel[rankingBoard()][i]), (i == rankingRank), scale);
                        receiver.drawScoreFont(engine, playerID, 16, topY + i, GeneralUtil.getTime(rankingTime[rankingBoard()][i]), (i == rankingRank), scale);
                    }

                    receiver.drawScoreFont(engine, playerID, 0, 18, "LOCAL SCORES", EventReceiver.COLOR_BLUE);
                    if (!playerProperties.isLoggedIn())
                        receiver.drawScoreFont(engine, playerID, 0, 19, "(NOT LOGGED IN)\n(E:LOG IN)");
                    if (playerProperties.isLoggedIn())
                        receiver.drawScoreFont(engine, playerID, 0, 22, "D:SWITCH RANK SCREEN", EventReceiver.COLOR_GREEN);

                }
            }
        } else if (engine.stat == GameEngine.STAT_CUSTOM) {
            playerProperties.loginScreen.renderScreen(receiver, engine, playerID);
        } else {
            if (showGrade) {
                receiver.drawScoreFont(engine, playerID, 0, 3, useClassicGrades ? "GRADE" : "AER", EventReceiver.COLOR_BLUE);

                if (useClassicGrades) {
                    receiver.drawScoreFont(engine, playerID, 0, 4, TABLE_CLASSIC_GRADE_NAME[getCombinedGrade(engine)], (((gradeFlash >>> 1) % 2) == 1));
                } else {
                    receiver.drawScoreFont(engine, playerID, 0, 4, getAER(leftGrade, rightGrade), (((gradeFlash >>> 1) % 2) == 1));
                }
            }

            receiver.drawScoreFont(engine, playerID, 0, 6, "LEVEL", EventReceiver.COLOR_BLUE);
            int tempLevel = engine.statistics.level;
            if (tempLevel < 0) tempLevel = 0;
            String strLevel = String.format("%3d", tempLevel);
            receiver.drawScoreFont(engine, playerID, 0, 7, strLevel);

            {
                int ix, iy;
                ix = receiver.getScoreDisplayPositionX(engine, playerID) - 3;
                iy = receiver.getScoreDisplayPositionY(engine, playerID) + 16 * 8 + 8;

                float speed = engine.speed.gravity / (float) engine.speed.denominator;

                int[] colorFront, colorBack;
                if (speed >= 1.0f || engine.speed.gravity < 0) {
                    colorBack = RendererExtension.SPEED_METER_GREEN;
                    colorFront = RendererExtension.SPEED_METER_RED;
                } else {
                    colorBack = new int[] { 255, 255, 255 };
                    colorFront = new int[] { 225, 225, 0 };
                }

                if (speed >= 1.0f) speed = speed / 20;
                if (engine.speed.gravity < 0) speed = 1f;

                rendererExtension.drawAlignedSpeedMeter(receiver, ix, iy, RendererExtension.ALIGN_MIDDLE_LEFT, speed, 1.26f, 1.25f, colorBack, colorFront);
            }

            receiver.drawScoreFont(engine, playerID, 0, 9, String.format("%3d", nextSectionLevel));

            receiver.drawScoreFont(engine, playerID, 0, 11, "TIME", EventReceiver.COLOR_BLUE);
            receiver.drawScoreFont(engine, playerID, 0, 12, GeneralUtil.getTime(engine.statistics.time));

            int ix, iy;
            ix = receiver.getScoreDisplayPositionX(engine, playerID);
            iy = receiver.getScoreDisplayPositionY(engine, playerID) + 18 * 14 + 8;

            if (showGrade && engine.ending < 2) {
                receiver.drawScoreFont(engine, playerID, 0, 14, "QUOTA", EventReceiver.COLOR_GREEN);

                if (useModepileRuleset) {
                    receiver.drawScoreFont(engine, playerID, 0, 15, fullGameQuota + " / " + 9000, fullGameQuota >= FULL_GAME_QUOTA_LIMIT ? EventReceiver.COLOR_YELLOW : EventReceiver.COLOR_WHITE);

                    float value = Math.min(1f, fullGameQuota / (float) FULL_GAME_QUOTA_LIMIT);

                    rendererExtension.drawAlignedSpeedMeter(receiver, ix, iy,
                        RendererExtension.ALIGN_TOP_LEFT, value,
                        4f, 2f,
                        RendererExtension.SPEED_METER_RED, RendererExtension.SPEED_METER_GREEN
                    );
                } else {
                    int section = engine.statistics.level / 100 - 1;
                    if (section < 0) section = 0;

                    receiver.drawScoreFont(engine, playerID, 0, 15, sectionPoints[section] + " / " + 1000, sectionPoints[section] >= 1000 ? EventReceiver.COLOR_YELLOW : EventReceiver.COLOR_WHITE);

                    float value = Math.min(1f, sectionPoints[section] / 1000f);

                    rendererExtension.drawAlignedSpeedMeter(receiver, ix, iy,
                        RendererExtension.ALIGN_TOP_LEFT, value,
                        4f, 2f,
                        RendererExtension.SPEED_METER_RED, RendererExtension.SPEED_METER_GREEN
                    );
                }
            }

            if (playerProperties.isLoggedIn() || !playerName.isEmpty()) {
                if (showGrade) {
                    receiver.drawScoreFont(engine, playerID, 0, 18, "PLAYER", EventReceiver.COLOR_BLUE);
                    receiver.drawScoreFont(engine, playerID, 0, 19, owner.replayMode ? playerName : playerProperties.getNameDisplay(), EventReceiver.COLOR_WHITE, 2f);
                } else {
                    receiver.drawScoreFont(engine, playerID, 0, 15, "PLAYER", EventReceiver.COLOR_BLUE);
                    receiver.drawScoreFont(engine, playerID, 0, 16, owner.replayMode ? playerName : playerProperties.getNameDisplay(), EventReceiver.COLOR_WHITE, 2f);
                }
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

            if ((engine.gameActive) && (engine.ending == 2)) {
                int time = ROLL_TIME_LIMIT - rollTime;
                if (time < 0) time = 0;
                receiver.drawScoreFont(engine, playerID, 0, 14, "ROLL TIME", EventReceiver.COLOR_BLUE);
                receiver.drawScoreFont(engine, playerID, 0, 15, GeneralUtil.getTime(time), ((time > 0) && (time < 10 * 60)));
                creditText.drawAtY(engine, receiver, playerID, 27.25, engine.displaysize + 1, (double) rollTime / ROLL_TIME_LIMIT);
            }

            // Section Time
            if ((showSectionTime) && (sectionTime != null)) {
                int x = (receiver.getNextDisplayType() == 2) ? 8 : 12;

                receiver.drawScoreFont(engine, playerID, x, 3, "SECTION TIME", EventReceiver.COLOR_BLUE);

                for (int i = 0; i < sectionTime.length; i++) {
                    if (sectionTime[i] > 0) {
                        int temp = i * 100;
                        if (i > 0) temp += 100;

                        int section = engine.statistics.level / 100 - 1;
                        if (section < 0) section = 0;

                        String strSeparator = " ";
                        if ((i == section) && (engine.ending == 0)) strSeparator = "b";

                        String strSectionTime;
                        strSectionTime = String.format("%3d%s%s", temp, strSeparator, GeneralUtil.getTime(sectionTime[i]));

                        receiver.drawScoreFont(engine, playerID, x, 5 + i, strSectionTime, sectionTime[i] <= SECTION_COOL_TIMES[i] ? EventReceiver.COLOR_GREEN : EventReceiver.COLOR_WHITE);
                    }
                }
            }
        }

        if (fireworks != null) fireworks.draw(receiver);
        if (sparks != null) sparks.draw(receiver);
    }

    @Override
    public boolean onResult(GameEngine engine, int playerID) {
        if (engine.ctrl.isMenuRepeatKey( Controller.BUTTON_UP)) {
            engine.statc[1]--;
            if (engine.statc[1] < 0) engine.statc[1] = 2;
            engine.playSE("change");
        }

        if (engine.ctrl.isMenuRepeatKey(Controller.BUTTON_DOWN)) {
            engine.statc[1]++;
            if (engine.statc[1] > 2) engine.statc[1] = 0;
            engine.playSE("change");
        }

        return false;
    }

    @Override
    public void renderResult( GameEngine engine, int playerID ) {
        receiver.drawMenuFont(engine, playerID, 0, 0, "kn PAGE" + (engine.statc[1] + 1) + "/3", EventReceiver.COLOR_RED);

        if (engine.statc[1] == 0) {
            int gcolor = EventReceiver.COLOR_WHITE;
            if (getCombinedGrade(engine) >= 20) gcolor = EventReceiver.COLOR_YELLOW;

            receiver.drawMenuFont(engine, playerID, 0, 2, useClassicGrades ? "GRADE" : "AER", EventReceiver.COLOR_BLUE);
            String strGrade = useClassicGrades ? String.format("%10s", TABLE_CLASSIC_GRADE_NAME[getCombinedGrade(engine)]) : String.format("%10s", getAER(getLeftGrade(engine), getRightGrade(engine)));

            receiver.drawMenuFont(engine, playerID, 0, 3, strGrade, gcolor);

            drawResultStats(
                engine, playerID, receiver, 4, EventReceiver.COLOR_BLUE,
                STAT_SCORE, STAT_LINES, STAT_LEVEL_MANIA, STAT_TIME
            );
            drawResultRank(engine, playerID, receiver, 12, EventReceiver.COLOR_BLUE, rankingRank);
            if (secretGrade > 4) {
                drawResult(engine, playerID, receiver, 14, EventReceiver.COLOR_BLUE,
                    "S. GRADE", String.format("%10s", TABLE_SECRET_GRADE_NAME[secretGrade - 1])
                );
            }
        } else if (engine.statc[1] == 1) {
            receiver.drawMenuFont(engine, playerID, 0, 2, "SECTION", EventReceiver.COLOR_BLUE);

            for (int i = 0; i < sectionTime.length; i++) {
                if (sectionTime[i] > 0) {
                    receiver.drawMenuFont(engine, playerID, 2, 3 + i, GeneralUtil.getTime(sectionTime[i]), sectionTime[i] < SECTION_COOL_TIMES[i] ? EventReceiver.COLOR_GREEN : EventReceiver.COLOR_WHITE);
                }
            }
        } else if (engine.statc[1] == 2) {
            drawResultStats( engine, playerID, receiver, 6, EventReceiver.COLOR_BLUE,
                STAT_LPM, STAT_SPM, STAT_PIECE, STAT_PPS);
        }
    }

    @Override
    public boolean onGameOver(GameEngine engine, int playerID) {
        if (engine.statc[0] == 0) {
            engine.blockOutlineType = GameEngine.BLOCK_OUTLINE_NORMAL;
            secretGrade = engine.field.getSecretGrade();

            gradePresentTextIndex = fireworkRandomiser.nextInt(6) + sparksRandomiser.nextInt(4);

            if (gradePresentTextIndex >= 9) gradePresentTextIndex = 2;
            else if (gradePresentTextIndex >= 7) gradePresentTextIndex = 1;
            else gradePresentTextIndex = 0;
        }

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

        if (engine.statc[0] == engine.field.getHeight() + 151 && getCombinedGrade(engine) == 20) engine.playSE("cool");

        if (engine.statc[0] < engine.field.getHeight() + 1) {
            for (int i = 0; i < engine.field.getWidth(); i++) {
                if (engine.field.getBlockColor(i, engine.field.getHeight() - engine.statc[0]) != Block.BLOCK_COLOR_NONE) {
                    final Block blk = engine.field.getBlock(i, engine.field.getHeight() - engine.statc[0]);

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
            ++engine.statc[0];
        } else if (engine.statc[0] == engine.field.getHeight() + 1) {
            engine.playSE("gameover");
            engine.statc[0]++;
        } else if (engine.statc[0] < engine.field.getHeight() + 1 + 420) {
            engine.statc[0]++;
        } else {
            if(!owner.replayMode || owner.replayRerecord) owner.saveReplay();

            for(int i = 0; i < owner.getPlayers(); i++) {
                if((i == playerID) || (engine.gameoverAll)) {
                    if(owner.engine[i].field != null) {
                        owner.engine[i].field.reset();
                    }
                    owner.engine[i].resetStatc();
                    owner.engine[i].stat = GameEngine.STAT_RESULT;
                }
            }
        }

        owner.receiver.onGameOver(engine, playerID);

        return true;
    }

    @Override
    public void renderGameOver(GameEngine engine, int playerID) {
        int offsetX = receiver.getFieldDisplayPositionX(engine, playerID);
        int offsetY = receiver.getFieldDisplayPositionY(engine, playerID);

        NormalFont.printFont(offsetX + 12, offsetY + 204, "GAME OVER", EventReceiver.COLOR_WHITE, 1.0f);

        if (engine.statc[0] > engine.field.getHeight() + 90) {
            GameTextUtilities.drawDirectTextAlign(
                receiver,
                engine,
                playerID,
                offsetX + (16 * engine.field.getWidth() / 2) + 4,
                offsetY + 252,
                GameTextUtilities.ALIGN_MIDDLE_MIDDLE,
                useClassicGrades ? headingClassic : headingAER[gradePresentTextIndex],
                EventReceiver.COLOR_WHITE,
                0.75f
            );
        }

        if (engine.statc[0] > engine.field.getHeight() + 150) {
            GameTextUtilities.drawDirectTextAlign(
                receiver,
                engine,
                playerID,
                offsetX + (16 * engine.field.getWidth() / 2) + 4,
                offsetY + 284,
                GameTextUtilities.ALIGN_MIDDLE_MIDDLE,
                    useClassicGrades ? TABLE_CLASSIC_GRADE_NAME[getCombinedGrade(engine)] : getAER(getLeftGrade(engine), getRightGrade(engine)),
                getCombinedGrade(engine) >= 20 ? EventReceiver.COLOR_YELLOW : EventReceiver.COLOR_WHITE,
                useClassicGrades ? 2.5f : 1.5f
            );
        }
    }

    @Override
    public boolean onExcellent(GameEngine engine, int playerID) {
        if (engine.statc[0] == 0) {
            engine.gameEnded();
            owner.bgmStatus.fadesw = true;

            engine.resetFieldVisible();

            engine.playSE("excellent");
        }

        if (fireworksLeft > 0) {
            if (engine.statc[0] % 12 == 0) {
                --fireworksLeft;

                launchFirework(engine, playerID);

                engine.playSE("fireworklaunch");
                engine.playSE("fireworkexplode");
            }

            ++engine.statc[0];
        } else {
            if ((engine.statc[0] >= 120) && (engine.ctrl.isPush(Controller.BUTTON_A))) {
                engine.statc[0] = 600;
            }

            if ((engine.statc[0] >= 600) && (engine.statc[1] == 0)) {
                engine.resetStatc();
                engine.stat = GameEngine.STAT_GAMEOVER;
            } else {
                ++engine.statc[0];
            }
        }

        owner.receiver.onExcellent(engine, playerID);

        return true;
    }

    private void launchFirework(GameEngine engine, int playerID) {
        if (fireworks != null) {
            final int[] colour = Fireworks.DEF_COLOURS[fireworkRandomiser.nextInt(Fireworks.DEF_COLOURS.length)];

            int minx = receiver.getFieldDisplayPositionX(engine, playerID) - 48;
            int maxx = receiver.getFieldDisplayPositionX(engine, playerID) + (engine.field.getWidth() * 16) + 48;
            int miny = receiver.getFieldDisplayPositionY(engine, playerID) - 48;
            int maxy = receiver.getFieldDisplayPositionY(engine, playerID) + (16 * 7);

            fireworks.addNumber(
                1,
                new Object[] {
                    minx, maxx, miny, maxy,
                    colour[0], colour[1], colour[2], colour[3], colour[4],
                    Fireworks.DEF_MAX_VEL,
                    45, 75
                }
            );
        }
    }
}
