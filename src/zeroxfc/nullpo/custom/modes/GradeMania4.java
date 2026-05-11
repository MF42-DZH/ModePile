package zeroxfc.nullpo.custom.modes;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.BooleanSupplier;
import java.util.function.IntBinaryOperator;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
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
import zeroxfc.nullpo.custom.libs.Interpolation;
import zeroxfc.nullpo.custom.libs.MathHelper;
import zeroxfc.nullpo.custom.libs.ModePileCredits;
import zeroxfc.nullpo.custom.libs.mixins.HasCustomFieldDrawing;
import zeroxfc.nullpo.custom.libs.types.ColourMixer;
import zeroxfc.nullpo.custom.libs.types.ObjectAlignment;
import zeroxfc.nullpo.custom.libs.ProfileProperties;
import zeroxfc.nullpo.custom.libs.RendererExtension;
import zeroxfc.nullpo.custom.libs.SoundLoader;
import zeroxfc.nullpo.custom.libs.SpeedTableBuilder;
import zeroxfc.nullpo.custom.libs.backgroundtypes.*;
import zeroxfc.nullpo.custom.libs.particles.Fireworks;
import zeroxfc.nullpo.custom.libs.particles.LandingParticles;
import zeroxfc.nullpo.custom.libs.particles.SurfaceSparks;

public class GradeMania4 extends DummyMode implements HasCustomFieldDrawing {
    private static final Logger log = Logger.getLogger(GradeMania4.class);

    private static final int CURRENT_VERSION = 2;

    private static SpeedTableBuilder.ModifiableARETable makeGravityTable() {
        return SpeedTableBuilder.createNew()
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
            .addTerminalGravity(-1, 256);
    }

    // region Speed Tables
    private static final IntFunction<SpeedParam> V1_SPEED_TABLE = makeGravityTable()
        .addARE(23, 700)
        .addARE(14, 800)
        .addARE(10, 900)
        .addTerminalARE(10)
        .addLineARE(23, 600)
        .addLineARE(14, 700)
        .addLineARE(10, 800)
        .addLineARE(4, 900)
        .addTerminalLineARE(4)
        .addDAS(15, 500)
        .addDAS(9, 900)
        .addTerminalDAS(7)
        .addTerminalLockDelay(31)
        .addLineDelay(40, 500)
        .addLineDelay(25, 600)
        .addLineDelay(16, 700)
        .addLineDelay(12, 800)
        .addLineDelay(6, 900)
        .addTerminalLineDelay(6)
        .buildSpeedTable();

    private static final IntFunction<SpeedParam> V2_SPEED_TABLE_SLOW = makeGravityTable()
        .addTerminalARE(26)
        .addTerminalLineARE(30)
        .addTerminalDAS(15)
        .addTerminalLockDelay(31)
        .addTerminalLineDelay(41)
        .buildSpeedTable();

    private static final IntFunction<SpeedParam> V2_SPEED_TABLE_FAST = makeGravityTable()
        .addARE(14, 700)
        .addARE(12, 800)
        .addTerminalARE(10)
        .addLineARE(23, 500)
        .addLineARE(14, 800)
        .addLineARE(8, 900)
        .addTerminalLineARE(6)
        .addDAS(11, 900)
        .addTerminalDAS(9)
        .addLockDelay(31, 900)
        .addTerminalLockDelay(27)
        .addLineDelay(40, 500)
        .addLineDelay(25, 600)
        .addLineDelay(16, 700)
        .addLineDelay(12, 800)
        .addLineDelay(10, 900)
        .addTerminalLineDelay(8)
        .buildSpeedTable();
    // endregion Speed Tables

    private IntFunction<SpeedParam> getSpeedTable() {
        if (version >= 2) return gameRuleset.speedTable;
        return V1_SPEED_TABLE;
    }

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

    private static GameTextUtilities.TextBlock getDisplayGradeBlock(int left, int right, int color) {
        return getDisplayGradeBlock(left, right, color, 1.0f);
    }

    private static GameTextUtilities.TextBlock getDisplayGradeBlock(int left, int right, int color, float scale) {
        if (left + right == 19) {
            return GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.custom("M", color, scale),
                GameTextUtilities.Text.custom("ASTER", color, scale * 0.5f)
            );
        } else if (left + right == 20) {
            return GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.custom("G", color, scale),
                GameTextUtilities.Text.custom("RAND ", color, scale * 0.5f),
                GameTextUtilities.Text.custom("M", color, scale),
                GameTextUtilities.Text.custom("ASTER", color, scale * 0.5f)
            );
        }

        return GameTextUtilities.TextBlock.of(
            GameTextUtilities.Text.custom(TABLE_CLASSIC_GRADE_NAME[left + right], color, scale)
        );
    }

    private static GameTextUtilities.TextBlock getDisplayAERBlock(int left, int right, int color) {
        return getDisplayAERBlock(left, right, color, 1.0f);
    }

    private static GameTextUtilities.TextBlock getDisplayAERBlock(int left, int right, int color, float scale) {
        return GameTextUtilities.TextBlock.of(
            GameTextUtilities.Text.custom(String.valueOf(left), color, scale),
            GameTextUtilities.Text.custom(" OF ", color, scale * 0.5f),
            GameTextUtilities.Text.custom(String.valueOf(right), color, scale)
        );
    }

    private static GameTextUtilities.TextBlock getAERBlock(int left, int right) {
        int color = EventReceiver.COLOR_WHITE;
        if (left + right >= 20) color = EventReceiver.COLOR_YELLOW;
        else if (right >= 10) color = EventReceiver.COLOR_ORANGE;
        else if (right >= 9) color = EventReceiver.COLOR_GREEN;

        return GameTextUtilities.TextBlock.of(
            GameTextUtilities.Text.custom(String.valueOf(left), color, 2f),
            GameTextUtilities.Text.custom(" OF ", color, 1f),
            GameTextUtilities.Text.custom(String.valueOf(right), color, 2f)
        );
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

    // Is it actually this value?
    private static final int FOUR_GOAL_TEN_OF_TEN_ORIG = 63;
    private static final int FOUR_GOAL_TEN_OF_TEN_MPL = 50;

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

    private static final ModePileCredits CREDITS = new ModePileCredits(
        GameTextUtilities.textElems(
            ModePileCredits.creditText("GRADE", EventReceiver.COLOR_YELLOW, 2f),
            ModePileCredits.creditText("MANIA 4", EventReceiver.COLOR_YELLOW, (10f / 7f)),
            ModePileCredits.creditText("BASED ON", EventReceiver.COLOR_WHITE, 0.5f),
            ModePileCredits.creditText("AE-NORMAL", EventReceiver.COLOR_CYAN, 0.95f),
            ModePileCredits.creditTextNoSp("(WITH SPITE)", EventReceiver.COLOR_WHITE, 0.75f)
        ),
        GameTextUtilities.textElems(
            ModePileCredits.creditTextNoSp("CREATED BY", EventReceiver.COLOR_YELLOW, 0.75f),
            GameTextUtilities.Text.blankLine(1f),
            ModePileCredits.creditText("AZULLIA", EventReceiver.COLOR_CYAN, 1.2f),
            ModePileCredits.creditText("A.K.A.", EventReceiver.COLOR_WHITE, 0.5f),
            ModePileCredits.creditTextNoSp("0XFC963F18DC21", EventReceiver.COLOR_WHITE, 0.6f),
            GameTextUtilities.Text.blankLine(4f),
            ModePileCredits.creditTextNoSp("WITH HELP FROM", EventReceiver.COLOR_YELLOW, 0.7f),
            GameTextUtilities.Text.blankLine(1f),
            ModePileCredits.creditText("NIGHTSHADE", EventReceiver.COLOR_WHITE, 0.85f),
            ModePileCredits.creditText("MANDL27", EventReceiver.COLOR_WHITE, 0.85f),
            ModePileCredits.creditText("AKARI", EventReceiver.COLOR_WHITE, 0.85f),
            ModePileCredits.creditText("JAVA REFLECTION", EventReceiver.COLOR_RED, 0.65f),
            ModePileCredits.creditText("CODE CRIMES", EventReceiver.COLOR_RED, 0.65f),
            ModePileCredits.creditTextNoSp("A LOAD OF COFFEE", EventReceiver.COLOR_ORANGE, 0.625f)
        ),
        GameTextUtilities.textElems(
            ModePileCredits.creditText("CONGRATULATIONS!", EventReceiver.COLOR_YELLOW, 0.625f),
            ModePileCredits.creditText("YOU HAVE CLEARED", EventReceiver.COLOR_YELLOW, 0.625f),
            ModePileCredits.creditTextNoSp("GRADE MANIA 4!", EventReceiver.COLOR_YELLOW, 0.625f)
        ),
        0.775, 0.125, false
    );

    private static final String[] HEADING_AER = {
        "YOUR AER",
        "YOU ARE",
        "YOU AER"
    };

    private static final String HEADING_CLASSIC = "YOUR GRADE";

    private static GameTextUtilities.TextBlock secretGradeBlock(int secretGrade) {
        if (secretGrade < 19) {
            return GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.custom(TABLE_SECRET_GRADE_NAME[secretGrade - 1], EventReceiver.COLOR_WHITE, 1f)
            );
        } else {
            return GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.custom("G", EventReceiver.COLOR_YELLOW, 1f),
                GameTextUtilities.Text.custom("RAND ", EventReceiver.COLOR_YELLOW, 0.5f),
                GameTextUtilities.Text.custom("M", EventReceiver.COLOR_YELLOW, 1f),
                GameTextUtilities.Text.custom("ASTER", EventReceiver.COLOR_YELLOW, 0.5f)
            );
        }
    }

    private static GameTextUtilities.TextBlock secretAERBlock(int secretGrade, float scale) {
        final int right = Math.min(10, secretGrade + 1);
        final int left = Math.max(0, secretGrade - 9);

        int color = EventReceiver.COLOR_WHITE;
        if (secretGrade >= 19) color = EventReceiver.COLOR_YELLOW;

        return GameTextUtilities.TextBlock.of(
            GameTextUtilities.Text.custom(String.valueOf(left), color, scale * 2f),
            GameTextUtilities.Text.custom(" OF ", color, scale * 1f),
            GameTextUtilities.Text.custom(String.valueOf(right), color, scale * 2f)
        );
    }

    private static int getFireworkLaunchCount(int totalGrade) {
        if (totalGrade >= 20) return 50;
        else if (totalGrade == 19) return 25;
        else return totalGrade;
    }

    // Settings
    private int version;
    private int startLevel;
    private boolean showGrade;
    private boolean useClassicGrades;
    private boolean showSectionTime;
    private boolean always20g;
    private boolean alwaysExtra;
    private boolean hardDropEffect;
    private boolean animatedBackgrounds;
    private boolean sparkEffect;
    private boolean toggleExtra;

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
    private int nextTimeTextIndex;
    private boolean extraState;

    private boolean getExtraState() {
        return extraState;
    }

    private CustomResourceHolder customGraphics;
    private RendererExtension rendererExtension;
    private Fireworks fireworks;

    private List<int[]> pCoordList;
    private Piece cPiece;

    private ProfileProperties playerProperties;
    private boolean showPlayerStats;
    private String playerName;

    private int rankingRank;
    private int[][] rankingGradeLeft, rankingGradeRight, rankingLevel, rankingTime;

    private int rankingRankPlayer;
    private int[][] rankingGradeLeftPlayer, rankingGradeRightPlayer, rankingLevelPlayer, rankingTimePlayer;

    private AnimatedBackgroundHook[] animBgInstances;

    private Random fireworkRandomiser;
    private int fireworksLeft;

    private Random lpRandomiser;
    private LandingParticles landingParticles;

    private Random sparksRandomiser;
    private SurfaceSparks sparks;

    private int lastBackground;
    private int currentBackground;
    private int fadeProgress;

    private void setNewBackground(int newBg) {
        lastBackground = currentBackground;
        currentBackground = newBg;
        fadeProgress = 0;
    }

    @Override
    public int getLastBackground() {
        return lastBackground;
    }

    @Override
    public int getCurrentBackground() {
        return currentBackground;
    }

    @Override
    public float getFadeProgress() {
        return fadeProgress / 300f;
    }

    private FrameDrawingParameters frameDrawingParameters;

    @Override
    public FrameDrawingParameters getFrameDrawingParameters(GameEngine engine, int playerID) {
        return frameDrawingParameters;
    }

    // Grade recognition system.
    private int getLeftGrade(GameEngine engine) {
        switch (gameRuleset) {
            case ORIGINAL:
            case FOURS_ORIGINAL:
            case FOURS_FAST_ORIGINAL:
                {
                    int count = 0;

                    for (int sectionPoint : sectionPoints) {
                        if (sectionPoint >= 1000) ++count;
                    }

                    final boolean opc = !gameRuleset.isFoursRuleset() ? engine.statistics.time <= TIME_LIMIT_TEN_OF_TEN : engine.statistics.totalFour >= FOUR_GOAL_TEN_OF_TEN_ORIG;
                    if ((engine.statistics.level >= LEVEL_LIMIT) && opc) ++count;

                    return count;
                }
            case MODEPILE:
            case FOURS_MODEPILE:
            case FOURS_FAST_MODEPILE:
                {
                    final boolean mpc = !gameRuleset.isFoursRuleset() ? engine.statistics.time <= TIME_LIMIT_TEN_OF_TEN : engine.statistics.totalFour >= FOUR_GOAL_TEN_OF_TEN_MPL;
                    return Math.min(9, fullGameQuota / 1000) + (((engine.statistics.level >= LEVEL_LIMIT) && mpc) ? 1 : 0);
                }
            default:
                break;
        }

        return 0;
    }

    private int getRightGrade(GameEngine engine) {
        return Math.min(9, engine.statistics.level / 111) + (rollClear ? 1 : 0);
    }

    private int getCombinedGrade(GameEngine engine) {
        return getLeftGrade(engine) + getRightGrade(engine);
    }

    private GameManager owner;
    private EventReceiver receiver;

    private enum GameFlavour {
        ORIGINAL_TASTE, MODEPILE_REMIX
    }

    private enum Ruleset {
        ORIGINAL(0, "original", "ORIGINAL", V2_SPEED_TABLE_FAST),
        MODEPILE(1, "modepile", "MODEPILE", V2_SPEED_TABLE_FAST),
        FOURS_ORIGINAL(2, "foursoriginal", "FOURS ORIG.", V2_SPEED_TABLE_SLOW),
        FOURS_MODEPILE(3, "foursmodepile", "FOURS MPL.", V2_SPEED_TABLE_SLOW),
        FOURS_FAST_ORIGINAL(4, "foursfastorig", "FAST 4S ORIG.", V2_SPEED_TABLE_FAST),
        FOURS_FAST_MODEPILE(5, "foursfastmpl", "FAST 4S MPL.", V2_SPEED_TABLE_FAST);

        private final int leaderboard;
        private final String leaderboardString;
        private final String displayName;
        private final IntFunction<SpeedParam> speedTable;

        Ruleset(int leaderboard, String leaderboardString, String displayName, IntFunction<SpeedParam> speedTable) {
            this.leaderboard = leaderboard;
            this.leaderboardString = leaderboardString;
            this.displayName = displayName;
            this.speedTable = speedTable;
        }

        // All rules available
        private static final Ruleset[] RULES = {
            ORIGINAL, MODEPILE,
            FOURS_ORIGINAL, FOURS_MODEPILE,
            FOURS_FAST_ORIGINAL, FOURS_FAST_MODEPILE
        };

        private static final int LEADERBOARDS = RULES.length;

        public static Ruleset[] allRules() {
            return RULES;
        }

        public static Ruleset getRuleset(int rulesetId) {
            for (Ruleset rule : RULES) {
                if (rule.leaderboard == rulesetId) return rule;
            }

            return null;
        }

        public boolean isFoursRuleset() {
            return this.name().contains("FOURS");
        }

        public GameFlavour gameFlavour() {
            if (this.name().contains("ORIGINAL")) {
                return GameFlavour.ORIGINAL_TASTE;
            } else {
                return GameFlavour.MODEPILE_REMIX;
            }
        }
    }

    private Ruleset gameRuleset;

    // Stops some weird finangling with lambdas.
    private Ruleset getGameRuleset() {
        return gameRuleset;
    }

    @Override
    public String getName() {
        return "GRADE MANIA 4";
    }

    @Override
    public void playerInit(GameEngine engine, int playerID) {
        owner = engine.owner;
        receiver = engine.owner.receiver;

        setupBackgrounds(engine);

        if (animBgInstances == null) {
            animBgInstances = new AnimatedBackgroundHook[SECTION_MAX];

            animBgInstances[0] = new BackgroundCircularRipple(0, BackgroundCircularRipple.DEF_FIELD_DIM, BackgroundCircularRipple.DEF_FIELD_DIM, null, null, BackgroundCircularRipple.DEF_WAVELENGTH, BackgroundCircularRipple.DEF_WAVESPEED, 180, BackgroundCircularRipple.DEF_BASE_SCALE, 0.5f);
            animBgInstances[1] = new BackgroundVerticalBars(1, 60, 160, 1f, 4f, false);
            animBgInstances[2] = new BackgroundDiagonalRipple(2, 8, 8, 60, 1f, 2f, false, false);
            animBgInstances[3] = new BackgroundHorizontalBars(3, 60, 120, 1f, 4f, false);
            animBgInstances[4] = new BackgroundDiagonalRipple(4, 8, 8, 60, 1f, 2f, true, true);
            animBgInstances[5] = new BackgroundVerticalBars(5, 60, 160, 1f, 4f, true);
            animBgInstances[6] = new BackgroundDiagonalRipple(6, 8, 8, 60, 1f, 2f, true, false);
            animBgInstances[7] = new BackgroundHorizontalBars(7, 60, 120, 1f, 4f, true);
            animBgInstances[8] = new BackgroundDiagonalRipple(8, 8, 8, 60, 1f, 2f, false, true);
            animBgInstances[9] = new BackgroundFakeScanlines(9);
        }

        for (AnimatedBackgroundHook bg : animBgInstances) {
            bg.reset();
        }

        SoundLoader.Sounds.Fireworks.loadAllSounds();

        final ColourMixer usedMixer = ColourMixer.rgb24(0);
        final IntBinaryOperator outerFunc = new IntBinaryOperator() {
            private final ColourMixer mixer = ColourMixer.hslViaAngle(210, 0, 0.5);

            @Override
            public int applyAsInt(int x, int y) {
                final int width = engine.field != null ? engine.field.getWidth() : 10;
                final int height = engine.field != null ? engine.field.getHeight() : 20;
                final int maxX = RendererExtension.getShowMeter(receiver) ? width * 4 + 4 : width * 4 + 2;
                final int maxY = height * 4 + 2;

                final double distance =
                    Math.abs((maxY * x) - (maxX * y)) / Math.sqrt((double) (maxY * maxY) + (maxX * maxX));

                final double lMult = Interpolation.sineStep(
                    0.85, 0.5,
                    MathHelper.clamp(
                        distance / (maxX / 2d),
                        0d, 1d
                    )
                );

                if (getGameRuleset().gameFlavour() == GameFlavour.ORIGINAL_TASTE) {
                    mixer.setHueAngle(210).setSaturation(0.95).setLightness(lMult);

                    if (getExtraState()) {
                        mixer.setHueAngle(0).setSaturation(1).setLightness(lMult);
                    }
                } else {
                    mixer.setHueAngle(225).setSaturation(0.90).setLightness(lMult);

                    if (getExtraState()) {
                        mixer.setHueAngle(15).setSaturation(1).setLightness(lMult);
                    }
                }

                return mixer.getRGB24();
            }
        };

        frameDrawingParameters = new FrameDrawingParameters(
            outerFunc,
            (x, y) -> {
                usedMixer.setRGB24(outerFunc.applyAsInt(x, y));
                usedMixer.setValue(Math.max(usedMixer.getValue(), 0.8));
                usedMixer.setSaturation(usedMixer.getSaturation() * 0.5);

                return usedMixer.getRGB24();
            },
            null
        );

        engineBaseRules = engine.ruleopt;
        engineExtraRules = new RuleOptions(engine.ruleopt);

        engineExtraRules.harddropEnable = true;
        engineExtraRules.harddropLock = true;
        engineExtraRules.harddropLimit = true;

        engineExtraRules.softdropEnable = true;
        engineExtraRules.softdropLock = false;
        engineExtraRules.softdropSurfaceLock = false;
        engineExtraRules.softdropLimit = true;

        gameRuleset = Ruleset.ORIGINAL;
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
        nextTimeTextIndex = 0;
        extraState = false;

        engine.ghost = true;

        customGraphics = new CustomResourceHolder();
        rendererExtension = new RendererExtension(customGraphics);

        pCoordList = new LinkedList<>();
        cPiece = null;

        engine.comboType = GameEngine.COMBO_TYPE_NORMAL;
        engine.framecolor = GameEngine.FRAME_COLOR_BLUE;

        engine.staffrollEnable = true;
        engine.staffrollNoDeath = false;

        if (playerProperties == null) {
            playerProperties = new ProfileProperties(HEADER_COLOUR);
            showPlayerStats = false;
        }

        rankingRank = -1;
        rankingGradeLeft = new int[Ruleset.LEADERBOARDS][RANKING_MAX];
        rankingGradeRight = new int[Ruleset.LEADERBOARDS][RANKING_MAX];
        rankingLevel = new int[Ruleset.LEADERBOARDS][RANKING_MAX];
        rankingTime = new int[Ruleset.LEADERBOARDS][RANKING_MAX];

        rankingRankPlayer = -1;
        rankingGradeLeftPlayer = new int[Ruleset.LEADERBOARDS][RANKING_MAX];
        rankingGradeRightPlayer = new int[Ruleset.LEADERBOARDS][RANKING_MAX];
        rankingLevelPlayer = new int[Ruleset.LEADERBOARDS][RANKING_MAX];
        rankingTimePlayer = new int[Ruleset.LEADERBOARDS][RANKING_MAX];

        if (!owner.replayMode) {
            loadSetting(owner.modeConfig);
            loadRanking(owner.modeConfig, engine.ruleopt.strRuleName);

            version = CURRENT_VERSION;
            alwaysExtra = false;

            if (playerProperties.isLoggedIn()) {
                loadSettingPlayer(playerProperties);
                loadRankingPlayer(playerProperties, engine.ruleopt.strRuleName);
            }

            playerName = "";
        } else {
            loadSetting(owner.replayProp);

            playerName = owner.replayProp.getProperty("grademania4.playerName", "");
        }

        updateBGPulseFrames(engine, 60, 180, 1f);

        lastBackground = startLevel;
        currentBackground = startLevel;
        fadeProgress = 300;
    }

    private void loadRuleset(IntSupplier ruleGetter, BooleanSupplier legacyRuleGetter) {
        final Ruleset loaded = Ruleset.getRuleset(ruleGetter.getAsInt());
        if (loaded != null) {
            gameRuleset = loaded;
        } else {
            if (legacyRuleGetter.getAsBoolean()) gameRuleset = Ruleset.MODEPILE;
            else gameRuleset = Ruleset.ORIGINAL;
        }
    }

    private void loadSetting(CustomProperties prop) {
        loadRuleset(
            () -> prop.getProperty("grademania4.ruleset", -1),
            () -> prop.getProperty("grademania4.customRule", false)
        );

        startLevel = prop.getProperty("grademania4.startLevel", 0);
        showGrade = prop.getProperty("grademania4.showGrade", false);
        useClassicGrades = prop.getProperty("grademania4.useClassicGrades", false);
        showSectionTime = prop.getProperty("grademania4.showSectionTime", false);
        always20g = prop.getProperty("grademania4.always20g", false);
        alwaysExtra = prop.getProperty("grademania4.alwaysExtra", false); // We keep this for compatibility with old replays.
        hardDropEffect = prop.getProperty("grademania4.hardDropEffect", true);
        animatedBackgrounds = prop.getProperty("grademania4.animatedBackgrounds", false);
        sparkEffect = prop.getProperty("grademania4.sparkEffect", true);
        version = prop.getProperty("grademania4.version", 0);
        toggleExtra = prop.getProperty("grademania4.toggleExtra", false);
    }

    private void saveSetting(CustomProperties prop) {
        prop.setProperty("grademania4.ruleset", gameRuleset.leaderboard);
        prop.setProperty("grademania4.startLevel", startLevel);
        prop.setProperty("grademania4.showGrade", showGrade);
        prop.setProperty("grademania4.useClassicGrades", useClassicGrades);
        prop.setProperty("grademania4.showSectionTime", showSectionTime);
        prop.setProperty("grademania4.always20g", always20g);
        prop.setProperty("grademania4.alwaysExtra", false); // New plays / replays should never save this as true.
        prop.setProperty("grademania4.hardDropEffect", hardDropEffect);
        prop.setProperty("grademania4.animatedBackgrounds", animatedBackgrounds);
        prop.setProperty("grademania4.sparkEffect", sparkEffect);
        prop.setProperty("grademania4.version", version);
        prop.setProperty("grademania4.toggleExtra", toggleExtra);
    }

    private void loadSettingPlayer(ProfileProperties prop) {
        if (!prop.isLoggedIn()) return;

        loadRuleset(
            () -> prop.getProperty("grademania4.ruleset", -1),
            () -> prop.getProperty("grademania4.customRule", false)
        );
        startLevel = prop.getProperty("grademania4.startLevel", 0);
        showGrade = prop.getProperty("grademania4.showGrade", false);
        useClassicGrades = prop.getProperty("grademania4.useClassicGrades", false);
        showSectionTime = prop.getProperty("grademania4.showSectionTime", false);
        always20g = prop.getProperty("grademania4.always20g", false);
        alwaysExtra = false; // prop.getProperty("grademania4.alwaysExtra", false); -- Changing this setting has been superceded by toggle extra.
        hardDropEffect = prop.getProperty("grademania4.hardDropEffect", true);
        animatedBackgrounds = prop.getProperty("grademania4.animatedBackgrounds", false);
        sparkEffect = prop.getProperty("grademania4.sparkEffect", true);
        toggleExtra = prop.getProperty("grademania4.toggleExtra", false);
    }

    private void saveSettingPlayer(ProfileProperties prop) {
        if (!prop.isLoggedIn()) return;

        prop.setProperty("grademania4.ruleset", gameRuleset.leaderboard);
        prop.setProperty("grademania4.startLevel", startLevel);
        prop.setProperty("grademania4.showGrade", showGrade);
        prop.setProperty("grademania4.useClassicGrades", useClassicGrades);
        prop.setProperty("grademania4.showSectionTime", showSectionTime);
        prop.setProperty("grademania4.always20g", always20g);
        prop.setProperty("grademania4.alwaysExtra", false); // There is no reason to save this as true anymore due to toggle extra.
        prop.setProperty("grademania4.hardDropEffect", hardDropEffect);
        prop.setProperty("grademania4.animatedBackgrounds", animatedBackgrounds);
        prop.setProperty("grademania4.sparkEffect", sparkEffect);
        prop.setProperty("grademania4.toggleExtra", toggleExtra);
    }

    private void loadRanking(CustomProperties prop, String ruleName) {
        for (Ruleset ruleset : Ruleset.RULES) {
            for (int i = 0; i < RANKING_MAX; ++i) {
                rankingGradeLeft[ruleset.leaderboard][i] = prop.getProperty("grademania4.ranking." + ruleset.leaderboardString + "." + ruleName + "." + CURRENT_VERSION + ".gradeL." + i, 0);
                rankingGradeRight[ruleset.leaderboard][i] = prop.getProperty("grademania4.ranking." + ruleset.leaderboardString + "." + ruleName + "." + CURRENT_VERSION + ".gradeR." + i, 0);
                rankingLevel[ruleset.leaderboard][i] = prop.getProperty("grademania4.ranking." + ruleset.leaderboardString + "." + ruleName + "." + CURRENT_VERSION + ".level." + i, 0);
                rankingTime[ruleset.leaderboard][i] = prop.getProperty("grademania4.ranking." + ruleset.leaderboardString + "." + ruleName + "." + CURRENT_VERSION + ".time." + i, 0);
            }
        }
    }

    private void saveRanking(CustomProperties prop, String ruleName) {
        for (Ruleset ruleset : Ruleset.RULES) {
            for (int i = 0; i < RANKING_MAX; ++i) {
                prop.setProperty("grademania4.ranking." + ruleset.leaderboardString + "." + ruleName + "." + CURRENT_VERSION + ".gradeL." + i, rankingGradeLeft[ruleset.leaderboard][i]);
                prop.setProperty("grademania4.ranking." + ruleset.leaderboardString + "." + ruleName + "." + CURRENT_VERSION + ".gradeR." + i, rankingGradeRight[ruleset.leaderboard][i]);
                prop.setProperty("grademania4.ranking." + ruleset.leaderboardString + "." + ruleName + "." + CURRENT_VERSION + ".level." + i, rankingLevel[ruleset.leaderboard][i]);
                prop.setProperty("grademania4.ranking." + ruleset.leaderboardString + "." + ruleName + "." + CURRENT_VERSION + ".time." + i, rankingTime[ruleset.leaderboard][i]);
            }
        }
    }

    private void loadRankingPlayer(ProfileProperties prop, String ruleName) {
        if (!prop.isLoggedIn()) return;

        for (Ruleset ruleset : Ruleset.RULES) {
            for (int i = 0; i < RANKING_MAX; ++i) {
                rankingGradeLeftPlayer[ruleset.leaderboard][i] = prop.getProperty("grademania4.ranking." + ruleset.leaderboardString + "." + ruleName + "." + CURRENT_VERSION + ".gradeL." + i, 0);
                rankingGradeRightPlayer[ruleset.leaderboard][i] = prop.getProperty("grademania4.ranking." + ruleset.leaderboardString + "." + ruleName + "." + CURRENT_VERSION + ".gradeR." + i, 0);
                rankingLevelPlayer[ruleset.leaderboard][i] = prop.getProperty("grademania4.ranking." + ruleset.leaderboardString + "." + ruleName + "." + CURRENT_VERSION + ".level." + i, 0);
                rankingTimePlayer[ruleset.leaderboard][i] = prop.getProperty("grademania4.ranking." + ruleset.leaderboardString + "." + ruleName + "." + CURRENT_VERSION + ".time." + i, 0);
            }
        }
    }

    private void saveRankingPlayer(ProfileProperties prop, String ruleName) {
        if (!prop.isLoggedIn()) return;

        for (Ruleset ruleset : Ruleset.RULES) {
            for (int i = 0; i < RANKING_MAX; ++i) {
                prop.setProperty("grademania4.ranking." + ruleset.leaderboardString + "." + ruleName + "." + CURRENT_VERSION + ".gradeL." + i, rankingGradeLeftPlayer[ruleset.leaderboard][i]);
                prop.setProperty("grademania4.ranking." + ruleset.leaderboardString + "." + ruleName + "." + CURRENT_VERSION + ".gradeR." + i, rankingGradeRightPlayer[ruleset.leaderboard][i]);
                prop.setProperty("grademania4.ranking." + ruleset.leaderboardString + "." + ruleName + "." + CURRENT_VERSION + ".level." + i, rankingLevelPlayer[ruleset.leaderboard][i]);
                prop.setProperty("grademania4.ranking." + ruleset.leaderboardString + "." + ruleName + "." + CURRENT_VERSION + ".time." + i, rankingTime[ruleset.leaderboard][i]);
            }
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
                rankingGradeLeft[gameRuleset.leaderboard][i] = rankingGradeLeft[gameRuleset.leaderboard][i - 1];
                rankingGradeRight[gameRuleset.leaderboard][i] = rankingGradeRight[gameRuleset.leaderboard][i - 1];
                rankingLevel[gameRuleset.leaderboard][i] = rankingLevel[gameRuleset.leaderboard][i - 1];
                rankingTime[gameRuleset.leaderboard][i] = rankingTime[gameRuleset.leaderboard][i - 1];
            }

            rankingGradeLeft[gameRuleset.leaderboard][rankingRank] = gradeL;
            rankingGradeRight[gameRuleset.leaderboard][rankingRank] = gradeR;
            rankingLevel[gameRuleset.leaderboard][rankingRank] = level;
            rankingTime[gameRuleset.leaderboard][rankingRank] = time;
        }

        if (playerProperties.isLoggedIn()) {
            rankingRankPlayer = checkRankingPlayer(gradeL, gradeR, level, time);

            if (rankingRankPlayer != -1) {
                for (int i = RANKING_MAX - 1; i > rankingRankPlayer; i--) {
                    rankingGradeLeftPlayer[gameRuleset.leaderboard][i] = rankingGradeLeftPlayer[gameRuleset.leaderboard][i - 1];
                    rankingGradeRightPlayer[gameRuleset.leaderboard][i] = rankingGradeRightPlayer[gameRuleset.leaderboard][i - 1];
                    rankingLevelPlayer[gameRuleset.leaderboard][i] = rankingLevelPlayer[gameRuleset.leaderboard][i - 1];
                    rankingTimePlayer[gameRuleset.leaderboard][i] = rankingTimePlayer[gameRuleset.leaderboard][i - 1];
                }

                rankingGradeLeftPlayer[gameRuleset.leaderboard][rankingRankPlayer] = gradeL;
                rankingGradeRightPlayer[gameRuleset.leaderboard][rankingRankPlayer] = gradeR;
                rankingLevelPlayer[gameRuleset.leaderboard][rankingRankPlayer] = level;
                rankingTimePlayer[gameRuleset.leaderboard][rankingRankPlayer] = time;
            }
        }
    }

    private int checkRanking(int gradeL, int gradeR, int level, int time) {
        for (int i = 0; i < RANKING_MAX; ++i) {
            if (gradeL + gradeR > rankingGradeLeft[gameRuleset.leaderboard][i] + rankingGradeRight[gameRuleset.leaderboard][i]) return i;
            else if (gradeL + gradeR == rankingGradeLeft[gameRuleset.leaderboard][i] + rankingGradeRight[gameRuleset.leaderboard][i] && level > rankingLevel[gameRuleset.leaderboard][i]) return i;
            else if (gradeL + gradeR == rankingGradeLeft[gameRuleset.leaderboard][i] + rankingGradeRight[gameRuleset.leaderboard][i] && level == rankingLevel[gameRuleset.leaderboard][i] && time < rankingTime[gameRuleset.leaderboard][i]) return i;
        }

        return -1;
    }

    private int checkRankingPlayer(int gradeL, int gradeR, int level, int time) {
        for (int i = 0; i < RANKING_MAX; ++i) {
            if (gradeL + gradeR > rankingGradeLeftPlayer[gameRuleset.leaderboard][i] + rankingGradeRightPlayer[gameRuleset.leaderboard][i]) return i;
            else if (gradeL + gradeR == rankingGradeLeftPlayer[gameRuleset.leaderboard][i] + rankingGradeRightPlayer[gameRuleset.leaderboard][i] && level > rankingLevelPlayer[gameRuleset.leaderboard][i]) return i;
            else if (gradeL + gradeR == rankingGradeLeftPlayer[gameRuleset.leaderboard][i] + rankingGradeRightPlayer[gameRuleset.leaderboard][i] && level == rankingLevelPlayer[gameRuleset.leaderboard][i] && time < rankingTimePlayer[gameRuleset.leaderboard][i]) return i;
        }


        return -1;
    }

    private static final int EXTRA_ARE = 6;

    private void setSpeed(GameEngine engine) {
        final SpeedParam speed = getSpeedTable().apply(
            (version == 0) ? engine.statistics.level - 1 : engine.statistics.level
        );

        engine.speed = speed;

        if (always20g) engine.speed.gravity = -1;
    }

    private void setStartBGMLevel(GameEngine engine) {
        bgmLevel = 0;
        while (TABLE_BGM_CHANGE[bgmLevel] != -1 && engine.statistics.level >= TABLE_BGM_CHANGE[bgmLevel]) bgmLevel++;
    }

    @Override
    public boolean onSetting(GameEngine engine, int playerID) {
        lastBackground = startLevel;
        currentBackground = startLevel;
        fadeProgress = 300;

        if (!engine.owner.replayMode) {
            // Configuration changes
            int change = updateCursor(engine, 9);

            if (change != 0) {
                engine.playSE("change");

                switch (engine.statc[2]) {
                    case 0:
                        int newRule = gameRuleset.leaderboard + change;

                        if (newRule >= Ruleset.allRules().length) {
                            newRule = 0;
                        } else if (newRule < 0) {
                            newRule = Ruleset.allRules().length - 1;
                        }

                        gameRuleset = Ruleset.allRules()[newRule];
                        break;
                    case 1:
                        startLevel += change;
                        if (startLevel < 0) startLevel = 9;
                        if (startLevel > 9) startLevel = 0;

                        lastBackground = startLevel;
                        currentBackground = startLevel;
                        fadeProgress = 300;

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
                        toggleExtra = !toggleExtra;
                        break;
                    case 6:
                        always20g = !always20g;
                        break;
                    case 7:
                        hardDropEffect = !hardDropEffect;
                        updateBGPulseFrames(engine, 60, 180, 1f);
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

            if (engine.ctrl.isPush(Controller.BUTTON_E) && engine.ai == null && !playerProperties.isLoggedIn()) {
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
            "VARIANT", gameRuleset.displayName
        );
        drawMenu(engine, playerID, receiver, 2, EventReceiver.COLOR_RED, 1,
            "LEVEL", String.valueOf(startLevel * 100)
        );
        drawMenu(engine, playerID, receiver, 4, EventReceiver.COLOR_GREEN, 2,
            "SHOW GRADE", GeneralUtil.getONorOFF(showGrade),
            "CLASSIC GRS", GeneralUtil.getONorOFF(useClassicGrades),
            "SHOW STIME", GeneralUtil.getONorOFF(showSectionTime),
            "TOGGLE EXTRA", GeneralUtil.getONorOFF(toggleExtra)
        );
        drawMenu(engine, playerID, receiver, 12, EventReceiver.COLOR_BLUE, 6,
            "20G MODE", GeneralUtil.getONorOFF(always20g)
        );
        drawMenu(engine, playerID, receiver, 14, EventReceiver.COLOR_PINK, 7,
            "DROP EFF.", GeneralUtil.getONorOFF(hardDropEffect),
            "ANIM. BGS.", GeneralUtil.getONorOFF(animatedBackgrounds),
            "SPARKS", GeneralUtil.getONorOFF(sparkEffect)
        );
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

        // Preserve replay compatibility.
        final SpeedParam currentParam = getSpeedTable().apply(
            (version == 0) ? engine.statistics.level - 1 : engine.statistics.level
        );

        if (toggleExtra && engine.ctrl.isPush(Controller.BUTTON_F)) {
            extraState = !extraState;
        } else if (!toggleExtra) {
            extraState = engine.ctrl.isPress(Controller.BUTTON_F);
        }

        // Extra shortens ARE if it is longer than Extra's ARE.
        if (engine.gameActive && (extraState || alwaysExtra)) {
            engine.speed.are = Math.min(EXTRA_ARE, currentParam.are);
            engine.speed.areLine = Math.min(EXTRA_ARE, currentParam.areLine);
            engine.ruleopt = engineExtraRules;
            engine.speed.das = Math.min(EXTRA_ARE, currentParam.das);

            if (!alwaysExtra) engine.framecolor = GameEngine.FRAME_COLOR_RED;
        } else {
            engine.speed.are = currentParam.are;
            engine.speed.areLine = currentParam.areLine;
            engine.ruleopt = engineBaseRules;
            engine.speed.das = currentParam.das;

            engine.framecolor = GameEngine.FRAME_COLOR_BLUE;
        }
    }

    @Override
    public void drawBackgroundElements(RendererExtension rendererExtension, EventReceiver receiver, GameEngine engine, int playerID) {
        if (animatedBackgrounds) {
            rendererExtension.drawFadingAnimatedBackground(receiver, engine, playerID, animBgInstances[getLastBackground()], animBgInstances[getCurrentBackground()], getFadeProgress());
        } else {
            rendererExtension.drawFadingBackground(receiver, engine, getLastBackground(), getCurrentBackground(), getFadeProgress());
        }
    }

    @Override
    public void drawBetweenFrameAndField(RendererExtension rendererExtension, EventReceiver receiver, GameEngine engine, int playerID) {
        if ((engine.gameActive) && (engine.ending == 2)) {
            int time = ROLL_TIME_LIMIT - rollTime;
            if (time < 0) time = 0;
            receiver.drawScoreFont(engine, playerID, 0, 14, "ROLL TIME", EventReceiver.COLOR_BLUE);
            receiver.drawScoreFont(engine, playerID, 0, 15, GeneralUtil.getTime(time), ((time > 0) && (time < 10 * 60)));

            CREDITS.draw(receiver, engine, playerID, (double) rollTime / ROLL_TIME_LIMIT);
        }
    }

    @Override
    public void renderFirst(GameEngine engine, int playerID) {
        inRenderFirst(rendererExtension, receiver, engine, playerID);
    }

    @Override
    public void renderMove(GameEngine engine, int playerID) {
        inRenderMove(rendererExtension, receiver, engine, playerID);
    }

    @Override
    public boolean onReady(GameEngine engine, int playerID) {

        lastBackground = startLevel;
        currentBackground = startLevel;
        fadeProgress = 300;
        if (engine.statc[0] == 0 && animatedBackgrounds) {
            for (AnimatedBackgroundHook bg : animBgInstances) {
                bg.reset();
            }
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

        engine.tspinEnable = true;
        engine.tspinAllowKick = true;
        engine.spinCheckType = GameEngine.SPINTYPE_4POINT;
        engine.tspinminiType = GameEngine.TSPINMINI_TYPE_ROTATECHECK;

        engine.b2bEnable = true;

        engine.staffrollEnableStatistics = false;

        levelUpFlag = true;

        fireworkRandomiser = new Random(engine.randSeed);
        fireworks = new Fireworks(customGraphics, fireworkRandomiser);
        fireworksLeft = 0;

        sparksRandomiser = new Random(engine.randSeed * 2);
        sparks = new SurfaceSparks(customGraphics, sparksRandomiser);

        lpRandomiser = new Random(engine.randSeed * 3);
        landingParticles = new LandingParticles(customGraphics, lpRandomiser);

        setStartBGMLevel(engine);
        levelUp(engine);
        owner.bgmStatus.bgm = bgmLevel;
    }

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

        if (sparkEffect) {
            sparks.addNumber(engine, receiver, playerID, 12);
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
            updateBGPulseFrames(engine, 40, 120, 4f);
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
            } else if (lines == 3) {
                engine.statistics.level += 4;
            } else {
                engine.statistics.level += lines + (lines >>> 1);
            }

            if (lines > 4) engine.statistics.totalFour++;
            if (lines > 4 && engine.b2b) engine.statistics.totalB2BFour++;

            switch (gameRuleset) {
                case ORIGINAL:
                case FOURS_ORIGINAL:
                case FOURS_FAST_ORIGINAL:
                    if (lines >= 4) {
                        sectionPoints[currentSection] += 175;
                    }
                    break;
                case MODEPILE:
                case FOURS_MODEPILE:
                case FOURS_FAST_MODEPILE:
                    if (lines < 3) {
                        fullGameQuota += 10 * lines;
                    } else if (lines == 3) {
                        fullGameQuota += 50;
                    } else {
                         fullGameQuota += 175;
                    }
                    break;
                default:
                    break;
            }

            // AC bonus
            if (engine.field.isEmpty()) {
                engine.playSE("bravo");

                if (!sectionAllClearAchieved[currentSection]) {
                    sectionAllClearAchieved[currentSection] = true;

                    switch (gameRuleset) {
                        case ORIGINAL:
                        case FOURS_ORIGINAL:
                        case FOURS_FAST_ORIGINAL:
                            sectionPoints[currentSection] += 350;
                            break;
                        case MODEPILE:
                        case FOURS_MODEPILE:
                        case FOURS_FAST_MODEPILE:
                            fullGameQuota += 350;
                            break;
                        default:
                            break;
                    }
                }
            }

            levelUp(engine);

            if (engine.statistics.level >= LEVEL_LIMIT) {
                engine.statistics.level = LEVEL_LIMIT;
                engine.timerActive = false;
                engine.ending = 1;

                // Update Section "COOL"
                if (sectionTime[sectionTime.length - 1] <= SECTION_COOL_TIMES[SECTION_COOL_TIMES.length - 1]) {
                    sectionPoints[sectionPoints.length - 1] += 125;

                    if (showGrade) engine.playSE("cool");
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

                setNewBackground(nextSectionLevel / 100);

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
        if ((engine.gameActive) && (engine.ending == 2)) {
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

        if (fadeProgress < 300) {
            fadeProgress += 10;
        }

        if (animatedBackgrounds) {
            if (lastBackground != currentBackground && getFadeProgress() < 0.5f) {
                animBgInstances[lastBackground].update();
            }

            animBgInstances[currentBackground].update();
        }

        if ((engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (!owner.replayMode))) {
            // Show rank
            if (engine.ctrl.isPush(Controller.BUTTON_D) && playerProperties.isLoggedIn() && engine.stat != GameEngine.STAT_CUSTOM) {
                showPlayerStats = !showPlayerStats;
                engine.playSE("change");
            }
        }

        if (fireworks != null) fireworks.update();
        if (sparks != null) sparks.update();
        if (landingParticles != null) landingParticles.update();
    }

    private static final List<Integer> avgX = new LinkedList<>();
    private static final List<Integer> avgY = new LinkedList<>();

    @Override
    public void afterHardDropFall(GameEngine engine, int playerID, int fall) {
        int baseX = (16 * engine.nowPieceX) + 4 + receiver.getFieldDisplayPositionX(engine, playerID);
        int baseY = (16 * engine.nowPieceY) + 52 + receiver.getFieldDisplayPositionY(engine, playerID);

        if (hardDropEffect) {
            cPiece = new Piece(engine.nowPieceObject);
            for (int i = 1; i <= fall; i++) {
                pCoordList.add(
                    new int[] { engine.nowPieceX, engine.nowPieceY - i }
                );
            }

            int x2, y2;

            avgX.clear();
            avgY.clear();

            for (int i = 0; i < cPiece.getMaxBlock(); i++) {
                if (!cPiece.big) {
                    x2 = baseX + (cPiece.dataX[cPiece.direction][i] * 16);
                    y2 = baseY + (cPiece.dataY[cPiece.direction][i] * 16);

                    avgX.add(x2 + 8);
                    avgY.add(y2 + 8);
                } else {
                    x2 = baseX + (cPiece.dataX[cPiece.direction][i] * 32);
                    y2 = baseY + (cPiece.dataY[cPiece.direction][i] * 32);

                    avgX.add(x2 + 8);
                    avgY.add(y2 + 8);

                    avgX.add(x2 + 8);
                    avgY.add(y2 + 24);

                    avgX.add(x2 + 24);
                    avgY.add(y2 + 8);

                    avgX.add(x2 + 24);
                    avgY.add(y2 + 24);
                }
            }

            landingParticles.addNumber(receiver, engine, playerID, 32);

            if (animatedBackgrounds && engine.statistics.level < 100) {
                final int avgXVal = avgX.stream().mapToInt(Integer::intValue).sum() / avgX.size();
                final int avgYVal = avgY.stream().mapToInt(Integer::intValue).sum() / avgY.size();

                ((BackgroundCircularRipple) animBgInstances[0]).manualRipple(avgXVal, avgYVal);
            }
        }
    }

    private static int rankGradeColor(int left, int right) {
        if (left + right == 20) return EventReceiver.COLOR_YELLOW;
        else if (right == 10) return EventReceiver.COLOR_ORANGE;
        else if (right == 9) return EventReceiver.COLOR_GREEN;
        else return EventReceiver.COLOR_WHITE;
    }

    private void drawRankGrade(GameEngine engine, int playerID, int x, int y, int left, int right, int color, float scale) {
        GameTextUtilities.drawAlignedScoreTextBlock(
            receiver, engine, playerID, scale == 0.5f, x, y, false,
            useClassicGrades ? getDisplayGradeBlock(left, right, color, scale) : getDisplayAERBlock(left, right, color, scale),
            ObjectAlignment.TOP_LEFT
        );
    }

    @Override
    public void renderLast(GameEngine engine, int playerID) {
        receiver.drawScoreFont(engine, playerID, 0, 0, getName(), EventReceiver.COLOR_BLUE);
        receiver.drawScoreFont(engine, playerID, 0, 1, "(" + gameRuleset.displayName + " RULES)", EventReceiver.COLOR_BLUE);

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
                            drawRankGrade(
                                engine, playerID, 3, topY + i, rankingGradeLeftPlayer[gameRuleset.leaderboard][i], rankingGradeRightPlayer[gameRuleset.leaderboard][i],
                                (i == rankingRankPlayer) ? EventReceiver.COLOR_RED : rankGradeColor(rankingGradeLeftPlayer[gameRuleset.leaderboard][i], rankingGradeRightPlayer[gameRuleset.leaderboard][i]),
                                scale
                            );
                        } else {
                            drawRankGrade(
                                engine, playerID, 3, topY + i, rankingGradeLeftPlayer[gameRuleset.leaderboard][i], rankingGradeRightPlayer[gameRuleset.leaderboard][i],
                                rankGradeColor(rankingGradeLeftPlayer[gameRuleset.leaderboard][i], rankingGradeRightPlayer[gameRuleset.leaderboard][i]),
                                scale
                            );
                        }
                        receiver.drawScoreFont(engine, playerID, 10, topY + i, String.valueOf(rankingLevelPlayer[gameRuleset.leaderboard][i]), (i == rankingRankPlayer), scale);
                        receiver.drawScoreFont(engine, playerID, 16, topY + i, GeneralUtil.getTime(rankingTimePlayer[gameRuleset.leaderboard][i]), (i == rankingRankPlayer), scale);
                    }

                    receiver.drawScoreFont(engine, playerID, 0, 18, "PLAYER SCORES", EventReceiver.COLOR_BLUE);
                    GameTextUtilities.drawAlignedScoreText(
                        receiver, engine, playerID,
                        false, 0, 19,
                        GameTextUtilities.Text.ofBig(playerProperties.getNameDisplay()),
                        ObjectAlignment.TOP_LEFT
                    );

                    receiver.drawScoreFont(engine, playerID, 0, 22, "D:SWITCH RANK SCREEN", EventReceiver.COLOR_GREEN);

                } else {
                    for (int i = 0; i < RANKING_MAX; i++) {
                        receiver.drawScoreFont(engine, playerID, 0, topY + i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW, scale);
                        if (rankingRank != -1) {
                            drawRankGrade(
                                engine, playerID, 3, topY + i, rankingGradeLeft[gameRuleset.leaderboard][i], rankingGradeRight[gameRuleset.leaderboard][i],
                                (i == rankingRank) ? EventReceiver.COLOR_RED : rankGradeColor(rankingGradeLeft[gameRuleset.leaderboard][i], rankingGradeRight[gameRuleset.leaderboard][i]),
                                scale
                            );
                        } else {
                            drawRankGrade(
                                engine, playerID, 3, topY + i, rankingGradeLeft[gameRuleset.leaderboard][i], rankingGradeRight[gameRuleset.leaderboard][i],
                                rankGradeColor(rankingGradeLeft[gameRuleset.leaderboard][i], rankingGradeRight[gameRuleset.leaderboard][i]),
                                scale
                            );
                        }
                        receiver.drawScoreFont(engine, playerID, 10, topY + i, String.valueOf(rankingLevel[gameRuleset.leaderboard][i]), (i == rankingRank), scale);
                        receiver.drawScoreFont(engine, playerID, 16, topY + i, GeneralUtil.getTime(rankingTime[gameRuleset.leaderboard][i]), (i == rankingRank), scale);
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

                final int color = ((gradeFlash >>> 1) % 2) == 1 ? EventReceiver.COLOR_RED : EventReceiver.COLOR_WHITE;

                GameTextUtilities.drawAlignedScoreTextBlock(
                    receiver, engine, playerID, false, 0, 4, false,
                    useClassicGrades ? getDisplayGradeBlock(leftGrade, rightGrade, color) : getDisplayAERBlock(leftGrade, rightGrade, color),
                    ObjectAlignment.TOP_LEFT
                );
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
                    colorFront = new int[] { 255, 128, 0 };
                }

                if (speed >= 1.0f) speed = speed / 10;
                if (engine.speed.gravity < 0) speed = 1f;

                rendererExtension.drawAlignedSpeedMeter(receiver, ix, iy, ObjectAlignment.MIDDLE_LEFT, speed, 1.325f, 1.25f, colorBack, colorFront);
            }

            receiver.drawScoreFont(engine, playerID, 0, 9, String.format("%3d", nextSectionLevel));

            receiver.drawScoreFont(engine, playerID, 0, 11, "TIME", EventReceiver.COLOR_BLUE);
            receiver.drawScoreFont(engine, playerID, 0, 12, GeneralUtil.getTime(engine.statistics.time));

            if (showGrade && engine.ending < 2) {
                int ix, iy;
                ix = receiver.getScoreDisplayPositionX(engine, playerID);
                iy = receiver.getScoreDisplayPositionY(engine, playerID) + 18 * 14 + 8;

                receiver.drawScoreFont(engine, playerID, 0, 14, "QUOTA", EventReceiver.COLOR_GREEN);

                String foursText = "";

                final int expectedFours = (int) Math.ceil((engine.statistics.level / (float) LEVEL_LIMIT) * FOUR_GOAL_TEN_OF_TEN_ORIG);
                if (gameRuleset.isFoursRuleset() && gameRuleset.gameFlavour() == GameFlavour.MODEPILE_REMIX) foursText = String.format("(%2s/%2s)", engine.statistics.totalFour, FOUR_GOAL_TEN_OF_TEN_MPL);
                if (gameRuleset.isFoursRuleset() && gameRuleset.gameFlavour() == GameFlavour.ORIGINAL_TASTE) foursText = String.format("(%2s/%2s)", engine.statistics.totalFour, expectedFours);

                float qbarXScale = 4.2f;
                if (gameRuleset.isFoursRuleset()) qbarXScale = 7.325f;

                switch (gameRuleset) {
                    case ORIGINAL:
                    case FOURS_ORIGINAL:
                    case FOURS_FAST_ORIGINAL:
                        {
                            int section = engine.statistics.level / 100 - 1;
                            if (section < 0) section = 0;

                            final boolean yellow = sectionPoints[section] >= 1000;

                            receiver.drawScoreFont(
                                engine, playerID,
                                0, 15,
                                String.format("%4s", sectionPoints[section]) + " / " + 1000,
                                yellow ? EventReceiver.COLOR_YELLOW : EventReceiver.COLOR_WHITE
                            );

                            float value = Math.min(1f, sectionPoints[section] / 1000f);

                            rendererExtension.drawAlignedSpeedMeter(receiver, ix, iy,
                                ObjectAlignment.TOP_LEFT, value,
                                qbarXScale, 2f,
                                RendererExtension.SPEED_METER_RED, RendererExtension.SPEED_METER_GREEN
                            );

                            if (gameRuleset.isFoursRuleset()) {
                                float foursValue = Math.min(1f, expectedFours <= 0 ? 0f : (float) engine.statistics.totalFour / expectedFours);
                                final boolean yellowFours = engine.statistics.totalFour >= expectedFours;

                                receiver.drawScoreFont(
                                    engine, playerID,
                                    12, 15,
                                    foursText,
                                    yellowFours ? EventReceiver.COLOR_YELLOW : EventReceiver.COLOR_WHITE
                                );

                                rendererExtension.drawAlignedSpeedMeter(receiver, ix, iy + 12,
                                    ObjectAlignment.TOP_LEFT, foursValue,
                                    qbarXScale, 2f,
                                    RendererExtension.SPEED_METER_RED, RendererExtension.SPEED_METER_GREEN
                                );
                            }
                        }
                        break;
                    case MODEPILE:
                    case FOURS_MODEPILE:
                    case FOURS_FAST_MODEPILE:
                        {
                            final boolean yellow = fullGameQuota >= FULL_GAME_QUOTA_LIMIT;

                            receiver.drawScoreFont(
                                engine, playerID,
                                0, 15,
                                String.format("%4s", fullGameQuota) + " / " + FULL_GAME_QUOTA_LIMIT,
                                yellow ? EventReceiver.COLOR_YELLOW : EventReceiver.COLOR_WHITE
                            );

                            float value = Math.min(1f, fullGameQuota / (float) FULL_GAME_QUOTA_LIMIT);

                            rendererExtension.drawAlignedSpeedMeter(receiver, ix, iy,
                                ObjectAlignment.TOP_LEFT, value,
                                qbarXScale, 2f,
                                RendererExtension.SPEED_METER_RED, RendererExtension.SPEED_METER_GREEN
                            );

                            if (gameRuleset.isFoursRuleset()) {
                                float foursValue = Math.min(1f, (float) engine.statistics.totalFour / FOUR_GOAL_TEN_OF_TEN_MPL);
                                final boolean yellowFours = engine.statistics.totalFour >= FOUR_GOAL_TEN_OF_TEN_MPL;

                                receiver.drawScoreFont(
                                    engine, playerID,
                                    12, 15,
                                    foursText,
                                    yellowFours ? EventReceiver.COLOR_YELLOW : EventReceiver.COLOR_WHITE
                                );

                                rendererExtension.drawAlignedSpeedMeter(receiver, ix, iy + 12,
                                    ObjectAlignment.TOP_LEFT, foursValue,
                                    qbarXScale, 2f,
                                    RendererExtension.SPEED_METER_RED, RendererExtension.SPEED_METER_GREEN
                                );
                            }
                        }
                        break;
                    default:
                        break;
                }
            }

            if (playerProperties.isLoggedIn() || !playerName.isEmpty()) {
                if (showGrade) {
                    int basePlayerY = 18;
                    if (gameRuleset.isFoursRuleset()) basePlayerY = 19;

                    receiver.drawScoreFont(engine, playerID, 0, basePlayerY, "PLAYER", EventReceiver.COLOR_BLUE);
                    GameTextUtilities.drawAlignedScoreText(
                        receiver, engine, playerID,
                        false, 0, basePlayerY + 1,
                        GameTextUtilities.Text.ofBig(owner.replayMode ? playerName : playerProperties.getNameDisplay()),
                        ObjectAlignment.TOP_LEFT
                    );
                } else {
                    receiver.drawScoreFont(engine, playerID, 0, 15, "PLAYER", EventReceiver.COLOR_BLUE);
                    GameTextUtilities.drawAlignedScoreText(
                        receiver, engine, playerID,
                        false, 0, 16,
                        GameTextUtilities.Text.ofBig(owner.replayMode ? playerName : playerProperties.getNameDisplay()),
                        ObjectAlignment.TOP_LEFT
                    );
                }
            }

            int baseX = receiver.getFieldDisplayPositionX(engine, playerID) + 4;
            int baseY = receiver.getFieldDisplayPositionY(engine, playerID) + 52;
            if (!pCoordList.isEmpty() && cPiece != null && hardDropEffect) {
                for (int[] loc : pCoordList) {
                    int cx = baseX + (16 * loc[0]);
                    int cy = baseY + (16 * loc[1]);
                    rendererExtension.drawScaledPiece(receiver, engine, playerID, cx, cy, cPiece, 1f, 1f, 0f);
                }
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

        rendererExtension.drawPostHoldOutline(receiver, engine, playerID);

        if (fireworks != null) fireworks.draw(receiver);
        if (sparks != null) sparks.draw(receiver);
        if (landingParticles != null) landingParticles.draw(receiver);
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
    public void renderResult(GameEngine engine, int playerID ) {
        inRenderResult(rendererExtension, receiver, engine, playerID);

        receiver.drawMenuFont(engine, playerID, 0, 0, "kn PAGE" + (engine.statc[1] + 1) + "/3", EventReceiver.COLOR_RED);

        if (engine.statc[1] == 0) {
            final int lGrade = getLeftGrade(engine);
            final int rGrade = getRightGrade(engine);

            int gcolor = EventReceiver.COLOR_WHITE;
            if (getCombinedGrade(engine) >= 20) gcolor = EventReceiver.COLOR_YELLOW;
            else if (rGrade >= 10) gcolor = EventReceiver.COLOR_ORANGE;
            else if (rGrade >= 9) gcolor = EventReceiver.COLOR_GREEN;

            receiver.drawMenuFont(engine, playerID, 0, 2, useClassicGrades ? "GRADE" : "AER", EventReceiver.COLOR_BLUE);

            GameTextUtilities.drawAlignedMenuTextBlock(
                receiver, engine, playerID, false, 10, 3,false,
                useClassicGrades ? getDisplayGradeBlock(lGrade, rGrade, gcolor) : getDisplayAERBlock(lGrade, rGrade, gcolor),
                ObjectAlignment.TOP_RIGHT
            );

            drawResultStats(
                engine, playerID, receiver, 4, EventReceiver.COLOR_BLUE,
                STAT_SCORE, STAT_LINES, STAT_LEVEL_MANIA, STAT_TIME
            );
            drawResultRank(engine, playerID, receiver, 12, EventReceiver.COLOR_BLUE, rankingRank);
            if (secretGrade > 4) {
                if (useClassicGrades) {
                    receiver.drawMenuFont(engine, playerID, 0, 14, "S. GRADE", EventReceiver.COLOR_BLUE);
                    GameTextUtilities.drawAlignedMenuTextBlock(
                        receiver, engine, playerID, false, 10, 15, false,
                        secretGradeBlock(secretGrade),
                        ObjectAlignment.TOP_RIGHT
                    );
                } else {
                    receiver.drawMenuFont(engine, playerID, 0, 14, "S. AER", EventReceiver.COLOR_BLUE);
                    GameTextUtilities.drawAlignedMenuTextBlock(
                        receiver, engine, playerID, false, 10, 15, false,
                        secretAERBlock(secretGrade, 0.5f),
                        ObjectAlignment.TOP_RIGHT
                    );
                }
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

            gradePresentTextIndex = fireworkRandomiser.nextInt(10);
            nextTimeTextIndex = lpRandomiser.nextInt(5);

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

        if (engine.statc[0] == engine.field.getHeight() + 241) {
            final int finalGrade = getCombinedGrade(engine);

            if (finalGrade >= 20) engine.playSE("cool");
            else if (finalGrade == 19) engine.playSE("medal");

            log.info(String.format("AER: %s /// GRADE: %s", getLeftGrade(engine) + " OF " + getRightGrade(engine), TABLE_CLASSIC_GRADE_NAME[getCombinedGrade(engine)]));
        }

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
        } else if (engine.statc[0] < engine.field.getHeight() + 1 + 480) {
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

    private final GameTextUtilities.TextBlock grandMasterTextBlock = GameTextUtilities.TextBlock.of(
        GameTextUtilities.Text.custom("G", EventReceiver.COLOR_YELLOW, 2.5f),
        GameTextUtilities.Text.custom("RAND", EventReceiver.COLOR_YELLOW, 1.25f),
        GameTextUtilities.Text.newLine(),
        GameTextUtilities.Text.custom("M", EventReceiver.COLOR_YELLOW, 2.5f),
        GameTextUtilities.Text.custom("ASTER", EventReceiver.COLOR_YELLOW, 1.25f)
    );

    private GameTextUtilities.TextBlock masterTextBlock(int left, int right) {
        int color = right == 10 ? EventReceiver.COLOR_ORANGE : EventReceiver.COLOR_GREEN;

        return GameTextUtilities.TextBlock.of(
            GameTextUtilities.Text.custom("M", color, 2.5f),
            GameTextUtilities.Text.custom("ASTER", color, 1.25f)
        );
    }

    @Override
    public void renderGameOver(GameEngine engine, int playerID) {
        int offsetX = receiver.getFieldDisplayPositionX(engine, playerID);
        int offsetY = receiver.getFieldDisplayPositionY(engine, playerID);

        if (engine.statc[0] > engine.field.getHeight()) {
            NormalFont.printFont(offsetX + 12, offsetY + 204, "GAME OVER", EventReceiver.COLOR_WHITE, 1.0f);
        }

        final int finalGrade = getCombinedGrade(engine);
        int gradeColor = EventReceiver.COLOR_WHITE;
        if (finalGrade >= 20) gradeColor = EventReceiver.COLOR_YELLOW;
        else if (getRightGrade(engine) >= 10) gradeColor = EventReceiver.COLOR_ORANGE;
        else if (getRightGrade(engine) >= 9) gradeColor = EventReceiver.COLOR_GREEN;

        if (engine.statc[0] > engine.field.getHeight() + 150) {
            GameTextUtilities.drawDirectTextAlign(
                engine,
                offsetX + (16 * engine.field.getWidth() / 2) + 4,
                offsetY + 242,
                ObjectAlignment.TOP_MIDDLE,
                useClassicGrades ? HEADING_CLASSIC : HEADING_AER[gradePresentTextIndex],
                EventReceiver.COLOR_WHITE,
                0.75f
            );

            if (secretGrade > 4) {
                GameTextUtilities.drawDirectTextAlign(
                    engine,
                    offsetX + (16 * engine.field.getWidth() / 2) + 4,
                    offsetY + 86,
                    ObjectAlignment.TOP_MIDDLE,
                    useClassicGrades ? "SECRET GRADE" : "SECRET AER",
                    EventReceiver.COLOR_WHITE,
                    0.75f
                );
            }
        }

        if (engine.statc[0] > engine.field.getHeight() + 240) {
            if (useClassicGrades && getCombinedGrade(engine) < 19) {
                GameTextUtilities.drawDirectTextAlign(
                    engine,
                    offsetX + (16 * engine.field.getWidth() / 2) + 4,
                    offsetY + 264,
                    ObjectAlignment.TOP_MIDDLE,
                    TABLE_CLASSIC_GRADE_NAME[getCombinedGrade(engine)],
                    gradeColor,
                    2.5f
                );
            } else if (useClassicGrades && getCombinedGrade(engine) == 20) {
                GameTextUtilities.drawAlignedTextBlock(
                    engine,
                    offsetX + (16 * engine.field.getWidth() / 2) + 4,
                    offsetY + 264,
                    false,
                    grandMasterTextBlock,
                    ObjectAlignment.TOP_MIDDLE
                );
            } else if (useClassicGrades && getCombinedGrade(engine) == 19) {
                GameTextUtilities.drawAlignedTextBlock(
                    engine,
                    offsetX + (16 * engine.field.getWidth() / 2) + 4,
                    offsetY + 264,
                    false,
                    masterTextBlock(getLeftGrade(engine), getRightGrade(engine)),
                    ObjectAlignment.TOP_MIDDLE
                );
            } else {
                GameTextUtilities.drawAlignedTextBlock(
                    engine,
                    offsetX + (16 * engine.field.getWidth() / 2) + 4,
                    offsetY + 264,
                    false,
                    getAERBlock(getLeftGrade(engine), getRightGrade(engine)),
                    ObjectAlignment.TOP_MIDDLE
                );
            }

            if (secretGrade > 4) {
                if (useClassicGrades) {
                    if (secretGrade < 19) {
                        GameTextUtilities.drawDirectTextAlign(
                            engine,
                            offsetX + (16 * engine.field.getWidth() / 2) + 4,
                            offsetY + 106,
                            ObjectAlignment.TOP_MIDDLE,
                            TABLE_SECRET_GRADE_NAME[secretGrade - 1],
                            EventReceiver.COLOR_WHITE,
                            2.5f
                        );
                    } else {
                        GameTextUtilities.drawAlignedTextBlock(
                            engine,
                            offsetX + (16 * engine.field.getWidth() / 2) + 4,
                            offsetY + 106,
                            false,
                            grandMasterTextBlock,
                            ObjectAlignment.TOP_MIDDLE
                        );
                    }
                } else {
                    GameTextUtilities.drawAlignedTextBlock(
                        engine,
                        offsetX + (16 * engine.field.getWidth() / 2) + 4,
                        offsetY + 106,
                        false,
                        secretAERBlock(secretGrade, 1f),
                        ObjectAlignment.TOP_MIDDLE
                    );
                }
            }
        }

        if (engine.statc[0] > engine.field.getHeight() + 300 && getRightGrade(engine) == 10 && getLeftGrade(engine) < 10 && engine.statistics.level >= LEVEL_LIMIT) {
            if (nextTimeTextIndex == 4) {
                GameTextUtilities.drawAlignedTextBlock(
                    engine,
                    offsetX + (16 * engine.field.getWidth() / 2) + 4,
                    offsetY + 326,
                    false,
                    GameTextUtilities.TextBlock.of(
                        GameTextUtilities.Text.custom("BUT", EventReceiver.COLOR_ORANGE, 0.7f),
                        GameTextUtilities.Text.custom("... ", EventReceiver.COLOR_ORANGE, 0.35f),
                        GameTextUtilities.Text.custom("LET'S GO BETTER", EventReceiver.COLOR_ORANGE, 0.7f)
                    ),
                    ObjectAlignment.MIDDLE_MIDDLE
                );

                GameTextUtilities.drawDirectTextAlign(
                    engine,
                    offsetX + (16 * engine.field.getWidth() / 2) + 4,
                    offsetY + 342,
                    ObjectAlignment.MIDDLE_MIDDLE,
                    "NEXT TIME TO BE",
                    EventReceiver.COLOR_ORANGE,
                    0.7f
                );

                GameTextUtilities.drawDirectTextAlign(
                    engine,
                    offsetX + (16 * engine.field.getWidth() / 2) + 4,
                    offsetY + 358,
                    ObjectAlignment.MIDDLE_MIDDLE,
                    "A " + (useClassicGrades ? "GRAND MASTER" : "10 OF 10 PLAYER") + "!",
                    EventReceiver.COLOR_ORANGE,
                    0.7f
                );
            } else {
                GameTextUtilities.drawDirectTextAlign(
                    engine,
                    offsetX + (16 * engine.field.getWidth() / 2) + 4,
                    offsetY + 326,
                    ObjectAlignment.MIDDLE_MIDDLE,
                    "NOW TRY AGAIN TO BE",
                    EventReceiver.COLOR_ORANGE,
                    0.7f
                );

                GameTextUtilities.drawDirectTextAlign(
                    engine,
                    offsetX + (16 * engine.field.getWidth() / 2) + 4,
                    offsetY + 342,
                    ObjectAlignment.MIDDLE_MIDDLE,
                    "A " + (useClassicGrades ? "GRAND MASTER" : "10 OF 10 PLAYER") + "!",
                    EventReceiver.COLOR_ORANGE,
                    0.7f
                );
            }
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

    @Override
    public void renderExcellent(GameEngine engine, int playerID) {
        inRenderExcellent(rendererExtension, receiver, engine, playerID);
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
