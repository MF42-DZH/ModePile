package zeroxfc.nullpo.custom.modes;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.TreeMap;
import java.util.function.BooleanSupplier;
import java.util.function.IntBinaryOperator;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;
import mu.nu.nullpo.game.component.BGMStatus;
import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.component.RuleOptions;
import mu.nu.nullpo.game.component.SpeedParam;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.game.subsystem.mode.DummyMode;
import mu.nu.nullpo.util.CustomProperties;
import mu.nu.nullpo.util.GeneralUtil;
import org.apache.log4j.Logger;
import zeroxfc.nullpo.custom.libs.CustomResourceHolder;
import zeroxfc.nullpo.custom.libs.DoubleVector;
import zeroxfc.nullpo.custom.libs.FieldManipulation;
import zeroxfc.nullpo.custom.libs.GameTextUtilities;
import zeroxfc.nullpo.custom.libs.Interpolation;
import zeroxfc.nullpo.custom.libs.LevelTableBuilder;
import zeroxfc.nullpo.custom.libs.MathHelper;
import zeroxfc.nullpo.custom.libs.MenuBuilder;
import zeroxfc.nullpo.custom.libs.MiscUtils;
import zeroxfc.nullpo.custom.libs.ModePileCredits;
import zeroxfc.nullpo.custom.libs.PrimitiveDrawingHook;
import zeroxfc.nullpo.custom.libs.ProfileProperties;
import zeroxfc.nullpo.custom.libs.RendererExtension;
import zeroxfc.nullpo.custom.libs.SoundLoader;
import zeroxfc.nullpo.custom.libs.SpeedTableBuilder;
import zeroxfc.nullpo.custom.libs.backgroundtypes.AnimatedBackgroundHook;
import zeroxfc.nullpo.custom.libs.backgroundtypes.BackgroundPieceMovement;
import zeroxfc.nullpo.custom.libs.mixins.HasCelebrationFireworks;
import zeroxfc.nullpo.custom.libs.mixins.HasCustomFieldDrawing;
import zeroxfc.nullpo.custom.libs.mixins.HasCustomGameOver;
import zeroxfc.nullpo.custom.libs.mixins.HasCustomLineClear;
import zeroxfc.nullpo.custom.libs.mixins.HasCustomMove;
import zeroxfc.nullpo.custom.libs.particles.Fireworks;
import zeroxfc.nullpo.custom.libs.particles.LandingParticles;
import zeroxfc.nullpo.custom.libs.particles.SurfaceSparks;
import zeroxfc.nullpo.custom.libs.particles.TextEmitter;
import zeroxfc.nullpo.custom.libs.types.ColourMixer;
import zeroxfc.nullpo.custom.libs.types.ObjectAlignment;
import zeroxfc.nullpo.custom.libs.types.tuples.IntPair;
import zeroxfc.nullpo.custom.libs.types.tuples.Pair;
import zeroxfc.nullpo.custom.modes.objects.seasons.*;

public class Seasons extends DummyMode implements HasCustomMove, HasCustomFieldDrawing, HasCustomLineClear, HasCustomGameOver, HasCelebrationFireworks {
    private static final Logger log = Logger.getLogger(Seasons.class);

    private static final Random FIREWORK_LAUNCHER_RANDOM = new Random();

    private static final int CURRENT_VERSION = 12;

    private enum FireworkLauncher implements BooleanSupplier {
        ONE(15), TWO(30), THREE(60);

        private final int maxCooldown;

        private int tick;
        private int currentCooldown;

        FireworkLauncher(int maxCooldown) {
            this.maxCooldown = maxCooldown;
            this.currentCooldown = FIREWORK_LAUNCHER_RANDOM.nextInt(maxCooldown + 1);

            tick = 0;
        }

        @Override
        public boolean getAsBoolean() {
            if (++tick < currentCooldown) return false;

            tick = 0;
            currentCooldown = FIREWORK_LAUNCHER_RANDOM.nextInt(maxCooldown + 1);

            return true;
        }
    }

    private enum Season {
        SPRING(GameEngine.FRAME_COLOR_GREEN, 1),
        SUMMER(GameEngine.FRAME_COLOR_RED, 2),
        AUTUMN(GameEngine.FRAME_COLOR_YELLOW, 4),
        WINTER(GameEngine.FRAME_COLOR_CYAN, 5);

        public final int defaultFrameColour;
        public final int performanceMultiplier;

        public static int defaultMenuFrameColour() {
            return GameEngine.FRAME_COLOR_GRAY;
        }

        Season(int defaultFrameColour, int performanceMultiplier) {
            this.defaultFrameColour = defaultFrameColour;
            this.performanceMultiplier = performanceMultiplier;
        }
    }

    private static final int HOURS_IN_DAY = 24;

    private static int addDays(int baseLevel, int days) {
        return baseLevel + HOURS_IN_DAY * days;
    }

    // The levels start on February instead of January because January is a little too early for Spring lmao.
    private static final int LEVELS_FEB = (28 * HOURS_IN_DAY);
    private static final int LEVELS_MAR = LEVELS_FEB + (31 * HOURS_IN_DAY);
    private static final int LEVELS_APR = LEVELS_MAR + (30 * HOURS_IN_DAY);
    private static final int LEVELS_MAY = LEVELS_APR + (31 * HOURS_IN_DAY);
    private static final int LEVELS_JUN = LEVELS_MAY + (30 * HOURS_IN_DAY);
    private static final int LEVELS_JUL = LEVELS_JUN + (31 * HOURS_IN_DAY);
    private static final int LEVELS_AUG = LEVELS_JUL + (31 * HOURS_IN_DAY);
    private static final int LEVELS_SEP = LEVELS_AUG + (30 * HOURS_IN_DAY);
    private static final int LEVELS_OCT = LEVELS_SEP + (31 * HOURS_IN_DAY);
    private static final int LEVELS_NOV = LEVELS_OCT + (30 * HOURS_IN_DAY);
    private static final int LEVELS_DEC = LEVELS_NOV + (31 * HOURS_IN_DAY);
    private static final int LEVELS_JAN = LEVELS_DEC + (31 * HOURS_IN_DAY); // Also the max level.

    // Base game July speed doesn't need to be set.
    private static final IntFunction<SpeedParam> SPEED_TABLE = SpeedTableBuilder.createNew()
        .addGravity(512, 65536, addDays(0, 1) + 12)
        .addGravity(1024, 65536, addDays(0, 3))
        .addGravity((1024 + 1536) / 2, 65536, addDays(0, 4) + 12)
        .addGravity(1536, 65536, addDays(0, 6))
        .addGravity((1536 + 2048) / 2, 65536, addDays(0, 7) + 12)
        .addGravity(2048, 65536, addDays(0, 9))
        .addGravity((2048 + 2560) / 2, 65536, addDays(0, 10) + 12)
        .addGravity(2560, 65536, addDays(0, 12))
        .addGravity((2560 + 3072) / 2, 65536, addDays(0, 13) + 12)
        .addGravity(3072, 65536, addDays(0, 15))
        .addGravity((3072 + 4096) / 2, 65536, addDays(0, 16) + 12)
        .addGravity(4096, 65536, addDays(0, 21))
        .addGravity((4096 + 8192) / 2, 65536, addDays(0, 22) + 12)
        .addGravity(8192, 65536, addDays(0, 24))
        .addGravity((8192 + 12288) / 2, 65536, addDays(0, 25) + 12)
        .addGravity(12288, 65536, addDays(0, 27))
        .addGravity((12288 + 16384) / 2, 65536, addDays(0, 28) + 12)
        .addGravity(16384, 65536, addDays(LEVELS_FEB, 1))
        .addGravity((16384 + 20480) / 2, 65536, addDays(LEVELS_FEB, 4) + 12)
        .addGravity(20480, 65536, addDays(LEVELS_FEB, 7))
        .addGravity((20480 + 24576) / 2, 65536, addDays(LEVELS_FEB, 10) + 12)
        .addGravity(24576, 65536, addDays(LEVELS_FEB, 13))
        .addGravity((24576 + 28672) / 2, 65536, addDays(LEVELS_FEB, 16) + 12)
        .addGravity(28672, 65536, addDays(LEVELS_FEB, 19))
        .addGravity((28672 + 32768) / 2, 65536, addDays(LEVELS_FEB, 22) + 12)
        .addGravity(32768, 65536, addDays(LEVELS_FEB, 25))
        .addGravity((32768 + 36864) / 2, 65536, addDays(LEVELS_FEB, 28) + 12)
        .addGravity(36864, 65536, addDays(LEVELS_MAR, 0))
        .addGravity(512, 65536, addDays(LEVELS_MAR, 1) + 12)
        .addGravity(1024, 65536, addDays(LEVELS_MAR, 3))
        .addGravity((1024 + 8192) / 2, 65536, addDays(LEVELS_MAR, 4) + 12)
        .addGravity(8192, 65536, addDays(LEVELS_MAR, 6))
        .addGravity((8192 + 16384) / 2, 65536, addDays(LEVELS_MAR, 7) + 12)
        .addGravity(16384, 65536, addDays(LEVELS_MAR, 9))
        .addGravity((16384 + 25476) / 2, 65536, addDays(LEVELS_MAR, 10) + 12)
        .addGravity(25476, 65536, addDays(LEVELS_MAR, 12))
        .addGravity((25476 + 32768) / 2, 65536, addDays(LEVELS_MAR, 13) + 12)
        .addGravity(32768, 65536, addDays(LEVELS_MAR, 15))
        .addGravity((32768 + 40960) / 2, 65536, addDays(LEVELS_MAR, 16) + 12)
        .addGravity(40960, 65536, addDays(LEVELS_MAR, 18))
        .addGravity((40960 + 49152) / 2, 65536, addDays(LEVELS_MAR, 19) + 12)
        .addGravity(49152, 65536, addDays(LEVELS_MAR, 21))
        .addGravity((49152 + 57344) / 2, 65536, addDays(LEVELS_MAR, 22) + 12)
        .addGravity(57344, 65536, addDays(LEVELS_MAR, 24))
        .addGravity((57344 + 65536) / 2, 65536, addDays(LEVELS_MAR, 25) + 12)
        .addGravity(1, 1, addDays(LEVELS_MAR, 27))
        .addGravity(2, 1, addDays(LEVELS_APR, 0))
        .addGravity(3, 2, addDays(LEVELS_APR, 7) + 12)
        .addGravity(3, 1, addDays(LEVELS_APR, 15))
        .addGravity(7, 2, addDays(LEVELS_APR, 23))
        .addGravity(4, 1, addDays(LEVELS_APR, 31))
        .addGravity(5, 1, addDays(LEVELS_MAY, 0))
        .addGravity(9, 2, addDays(LEVELS_MAY, 7) + 12)
        .addGravity(4, 1, addDays(LEVELS_MAY, 15))
        .addGravity(7, 2, addDays(LEVELS_MAY, 22) + 12)
        .addGravity(3, 1, addDays(LEVELS_MAY, 30))
        .addGravity(2, 1, LEVELS_NOV)
        .addGravity(3, 2, addDays(LEVELS_NOV, 15))
        .addGravity(1, 1, LEVELS_DEC)
        .addGravity(3, 4, addDays(LEVELS_DEC, 15))
        .addTerminalGravity(1, 2)
        .addARE(14, LEVELS_APR)
        .addARE(12, LEVELS_MAY)
        .addARE(10, LEVELS_JUN)
        .addARE(12, LEVELS_NOV)
        .addARE(14, LEVELS_DEC)
        .addTerminalARE(16)
        .addLineARE(23, LEVELS_APR)
        .addLineARE(19, LEVELS_MAY)
        .addLineARE(15, LEVELS_JUN)
        .addLineARE(11, LEVELS_NOV)
        .addLineARE(9, LEVELS_DEC)
        .addTerminalLineARE(7)
        .addDAS(11, LEVELS_JUL)
        .addTerminalDAS(8)
        .addLockDelay(30, LEVELS_JUL)
        .addLockDelay(18, LEVELS_OCT)
        .addLockDelay(30, LEVELS_DEC)
        .addTerminalLockDelay(18)
        .addLineDelay(40, LEVELS_JUL)
        .addLineDelay(20, LEVELS_OCT)
        .addLineDelay(15, LEVELS_DEC)
        .addTerminalLineDelay(8)
        .buildSpeedTable();

    // Roll Summer season speed doesn't need to be set.
    private static final IntFunction<SpeedParam> ROLL_SPEED_TABLE = SpeedTableBuilder.createNew()
        .addGravity(1, 2, LEVELS_FEB / 2)
        .addGravity(2, 2, LEVELS_FEB)
        .addGravity(3, 2, (LEVELS_FEB + LEVELS_MAR) / 2)
        .addGravity(4, 2, LEVELS_MAR)
        .addGravity(5, 2, (LEVELS_MAR + LEVELS_APR) / 2)
        .addGravity(6, 2, LEVELS_APR)
        .addGravity(4, 2, (LEVELS_JUL + LEVELS_AUG) / 2)
        .addGravity(4, 1, LEVELS_AUG)
        .addGravity(5, 2, (LEVELS_AUG + LEVELS_SEP) / 2)
        .addGravity(5, 1, LEVELS_SEP)
        .addGravity(6, 2, (LEVELS_SEP + LEVELS_OCT) / 2)
        .addGravity(6, 1, LEVELS_OCT)
        .addTerminalGravity(1, 2)
        .addARE(12, LEVELS_FEB)
        .addARE(11, LEVELS_MAR)
        .addARE(10, LEVELS_APR)
        .addARE(9, LEVELS_AUG)
        .addARE(8, LEVELS_SEP)
        .addARE(7, LEVELS_OCT)
        .addTerminalARE(12)
        .addLineARE(8, LEVELS_FEB)
        .addLineARE(7, LEVELS_MAR)
        .addLineARE(6, LEVELS_APR)
        .addLineARE(5, LEVELS_AUG)
        .addLineARE(4, LEVELS_SEP)
        .addLineARE(3, LEVELS_OCT)
        .addTerminalLineARE(6)
        .addTerminalDAS(8)
        .addLockDelay(20, LEVELS_APR)
        .addLockDelay(18, LEVELS_OCT)
        .addTerminalLockDelay(60)
        .addTerminalLineDelay(4)
        .buildSpeedTable();

    private static final int MAX_LEVEL = LEVELS_JAN;

    private static final IntFunction<Integer> NEXT_SECTION_LEVELS = LevelTableBuilder.<Integer>createNew()
        .addValue(LEVELS_FEB, LEVELS_FEB) // Spring
        .addValue(LEVELS_MAR, LEVELS_MAR)
        .addValue(LEVELS_APR, LEVELS_APR)
        .addValue(LEVELS_MAY, LEVELS_MAY) // Summer
        .addValue(LEVELS_JUN, LEVELS_JUN)
        .addValue(LEVELS_JUL, LEVELS_JUL)
        .addValue(LEVELS_AUG, LEVELS_AUG) // Autumn
        .addValue(LEVELS_SEP, LEVELS_SEP)
        .addValue(LEVELS_OCT, LEVELS_OCT)
        .addValue(LEVELS_NOV, LEVELS_NOV) // Winter
        .addValue(LEVELS_DEC, LEVELS_DEC)
        .addValue(LEVELS_JAN, LEVELS_JAN)
        .addTerminalValue(LEVELS_JAN)
        .buildLevelTable();

    private static final IntFunction<Integer> LEVELS_SO_FAR = LevelTableBuilder.<Integer>createNew()
        .addValue(0, LEVELS_FEB)
        .addValue(LEVELS_FEB, LEVELS_MAR)
        .addValue(LEVELS_MAR, LEVELS_APR)
        .addValue(LEVELS_APR, LEVELS_MAY)
        .addValue(LEVELS_MAY, LEVELS_JUN)
        .addValue(LEVELS_JUN, LEVELS_JUL)
        .addValue(LEVELS_JUL, LEVELS_AUG)
        .addValue(LEVELS_AUG, LEVELS_SEP)
        .addValue(LEVELS_SEP, LEVELS_OCT)
        .addValue(LEVELS_OCT, LEVELS_NOV)
        .addValue(LEVELS_NOV, LEVELS_DEC)
        .addValue(LEVELS_DEC, LEVELS_JAN)
        .addTerminalValue(LEVELS_JAN)
        .buildLevelTable();

    private static final IntFunction<String> MONTH_NAME_TABLE = LevelTableBuilder.<String>createNew()
        .addValue("FEB", LEVELS_FEB)
        .addValue("MAR", LEVELS_MAR)
        .addValue("APR", LEVELS_APR)
        .addValue("MAY", LEVELS_MAY)
        .addValue("JUN", LEVELS_JUN)
        .addValue("JUL", LEVELS_JUL)
        .addValue("AUG", LEVELS_AUG)
        .addValue("SEP", LEVELS_SEP)
        .addValue("OCT", LEVELS_OCT)
        .addValue("NOV", LEVELS_NOV)
        .addValue("DEC", LEVELS_DEC)
        .addValue("JAN", LEVELS_JAN)
        .addTerminalValue("FEB")
        .buildLevelTable();

    private static final IntFunction<Integer> MONTH_ORDINAL_TABLE = LevelTableBuilder.<Integer>createNew()
        .addValue(2, LEVELS_FEB)
        .addValue(3, LEVELS_MAR)
        .addValue(4, LEVELS_APR)
        .addValue(5, LEVELS_MAY)
        .addValue(6, LEVELS_JUN)
        .addValue(7, LEVELS_JUL)
        .addValue(8, LEVELS_AUG)
        .addValue(9, LEVELS_SEP)
        .addValue(10, LEVELS_OCT)
        .addValue(11, LEVELS_NOV)
        .addValue(12, LEVELS_DEC)
        .addValue(1, LEVELS_JAN)
        .addTerminalValue(2)
        .buildLevelTable();

    private static final IntFunction<Integer> BACKGROUND_TABLE = LevelTableBuilder.<Integer>createNew()
        .addValue(0, LEVELS_FEB)
        .addValue(1, LEVELS_MAR)
        .addValue(2, LEVELS_APR)
        .addValue(3, LEVELS_MAY)
        .addValue(4, LEVELS_JUN)
        .addValue(5, LEVELS_JUL)
        .addValue(6, LEVELS_AUG)
        .addValue(7, LEVELS_SEP)
        .addValue(8, LEVELS_OCT)
        .addValue(9, LEVELS_NOV)
        .addValue(10, LEVELS_DEC)
        .addValue(11, LEVELS_JAN)
        .addTerminalValue(12)
        .buildLevelTable();

    private static final IntFunction<Season> SEASON_TABLE = LevelTableBuilder.<Season>createNew()
        .addValue(Season.SPRING, LEVELS_APR)
        .addValue(Season.SUMMER, LEVELS_JUL)
        .addValue(Season.AUTUMN, LEVELS_OCT)
        .addTerminalValue(Season.WINTER)
        .buildLevelTable();

    private static final IntFunction<Integer> BGM_TABLE = LevelTableBuilder.<Integer>createNew()
        .addValue(BGMStatus.BGM_NORMAL1, LEVELS_APR)
        .addValue(BGMStatus.BGM_NORMAL2, LEVELS_JUL)
        .addValue(BGMStatus.BGM_NORMAL3, LEVELS_OCT)
        .addTerminalValue(BGMStatus.BGM_NORMAL4)
        .buildLevelTable();

    private static final IntFunction<Integer> BGM_FADE_LEVEL_TABLE = LevelTableBuilder.<Integer>createNew()
        .addValue(LEVELS_APR - 72, LEVELS_APR)
        .addValue(LEVELS_JUL - 72, LEVELS_JUL)
        .addValue(LEVELS_OCT - 72, LEVELS_OCT)
        .addValue(LEVELS_JAN - 72, LEVELS_JAN)
        .addTerminalValue(-1)
        .buildLevelTable();

    private static final ModePileCredits CREDITS = new ModePileCredits(
        GameTextUtilities.textElems(
            ModePileCredits.creditText("SEASONS", EventReceiver.COLOR_GREEN, (10f / 7f)),
            ModePileCredits.creditText("INSPIRED BY", EventReceiver.COLOR_WHITE, (10f / 12f)),
            ModePileCredits.creditText("HEBORIS", EventReceiver.COLOR_BLUE, 1f),
            ModePileCredits.creditText("AND", EventReceiver.COLOR_WHITE, 0.75f),
            ModePileCredits.creditText("AE-MASTER", EventReceiver.COLOR_RED, 1f),
            ModePileCredits.creditText("AND", EventReceiver.COLOR_WHITE, 0.75f),
            ModePileCredits.creditTextNoSp("TOUHOUMINO", EventReceiver.COLOR_PURPLE, 1f)
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
            ModePileCredits.creditText("NOW SURVIVE THE", EventReceiver.COLOR_YELLOW, 0.625f),
            ModePileCredits.creditText("GAUNTLET AND", EventReceiver.COLOR_YELLOW, 0.625f),
            ModePileCredits.creditTextNoSp("FINISH SEASONS!", EventReceiver.COLOR_YELLOW, 0.625f)
        ),
        0.875, 0.125, false
    );

    private String levelToString(int level) {
        if (level >= MAX_LEVEL) {
            return rollStarted ? "END" : "END?";
        }

        final String month = MONTH_NAME_TABLE.apply(level);
        final int normLevel = level - LEVELS_SO_FAR.apply(level);
        return String.format("%02d:00 %02d/%s/X%d", normLevel % 24, (normLevel / 24) + 1, month, level >= LEVELS_DEC ? 1 : 0);
    }

    private GameTextUtilities.TextBlock levelToResultBlock(int level) {
        if (level >= MAX_LEVEL) {
            return GameTextUtilities.TextBlock.of(GameTextUtilities.Text.of("END"));
        }

        final String month = MONTH_NAME_TABLE.apply(level);
        final int normLevel = level - LEVELS_SO_FAR.apply(level);

        final String time = String.format("%02d:00", normLevel % 24);
        final String date = String.format("%02d/%s/X%d", (normLevel / 24) + 1, month, level >= LEVELS_DEC ? 1 : 0);

        return GameTextUtilities.TextBlock.of(
            GameTextUtilities.TextJustification.CENTRE,
            GameTextUtilities.Text.of(time),
            GameTextUtilities.Text.newLine(),
            GameTextUtilities.Text.of(date)
        );
    }

    private static GameTextUtilities.TextBlock levelToRankBlock(int gameLevel, int rollLevel) {
        if (rollLevel >= 0) {
            return GameTextUtilities.TextBlock.of(
                GameTextUtilities.TextJustification.LEFT,
                GameTextUtilities.Text.ofSmall(
                    levelToShortDate(gameLevel)
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.ofSmall(
                    levelToShortDate(rollLevel),
                    rollLevel >= MAX_LEVEL ? EventReceiver.COLOR_ORANGE : EventReceiver.COLOR_GREEN
                )
            );
        } else {
            return GameTextUtilities.TextBlock.of(
                GameTextUtilities.TextJustification.LEFT,
                GameTextUtilities.Text.ofSmall(
                    levelToShortDate(gameLevel)
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.ofSmall("UNFINISHED", EventReceiver.COLOR_WHITE)
            );
        }
    }

    private static String levelToShortDate(int level) {
        final int month = MONTH_ORDINAL_TABLE.apply(level);
        final int normLevel = level - LEVELS_SO_FAR.apply(level);

        return String.format(
            "%02d:00 %02d/%02d/X%d",
            normLevel % 24,
            (normLevel / 24) + 1,
            month,
            level >= LEVELS_DEC ? 1 : 0
        );
    }

    // Game manager and current renderer.
    private GameManager owner;
    private EventReceiver receiver;
    private CustomResourceHolder customGraphics;
    private RendererExtension rendererExtension;
    private PrimitiveDrawingHook drawing;

    // Settings
    private static final int HEADER_COLOUR = EventReceiver.COLOR_ORANGE;
    private SeasonsSettings settings;
    private MenuBuilder.Menu settingsMenu;
    private ProfileProperties playerProperties;


    private boolean showPlayerStats;
    private SubRanking showBoard;
    private RuleOptions ruleOptCopy;

    private boolean getGimmickPerkBoost() {
        return settings.perk == SeasonPerk.AUTUMN_PASSIVE;
    }

    private enum SubRanking {
        DATE, TIME, PERK
    }

    // Gameplay variables
    private boolean levelUpFlag;
    private int nextSectionLevel;
    private Season currentSeason;
    private int lastBackground, currentBackground, fadeProgress;
    private Badges badges;
    private TextEmitter textEmitter;
    private Collection<RewindBlock> rewindBlocks;
    private Random rewindBlockRandom;
    private Grading grading;
    private TotalGrades totalGrades;
    private int rollTime;
    private int rollElapsed;
    private int lastRank;
    private int lastRankPlayer;
    private int timeSpentInSeason;
    private int activeTimeSpentInSeason;
    private boolean lineClearAfterPiece;

    private static final int INCREMENT_IN_ROLL = 2;
    private int naturalLevelIncrement;

    private int currentEnergy;
    private int currentAbilityTimer;
    private boolean queuedFreefall; // Freefall only works between pieces so it doesn't screw you over.
    private boolean hasLandedBefore;
    private int lockedPieces;
    private NavigableMap<Integer, NextAndFieldState> statesAtTimes;
    private boolean rollStarted;
    private int rollLevelReached;
    private boolean fieldPurifyQueued;
    private int brokenBoneBlocks;
    private int brokenMinorBonusGems;
    private int brokenFlorets;
    private int brokenMajorBonusGems;
    private int brokenSnowBlocks;
    private int blocksUnderSnow;
    private int hardBlocksSeen;
    private FireAndSnow fireAndSnowEffect;

    private Random sparksRandomiser;
    private SurfaceSparks sparks;

    private Random lpRandomiser;
    private LandingParticles landingParticles;

    // Winter active stuff.
    private int waCurrentFreezeRow;
    private int waFrozenRows;

    private static class FireworkContainer {
        public static Fireworks fireworks;
        public static Random fireworkColourRandomizer;
        public static int fireworksLeft;
    }

    @Override
    public Fireworks getFireworkEmitter() {
        return FireworkContainer.fireworks;
    }

    @Override
    public Random getFireworkColourRandomizer() {
        return FireworkContainer.fireworkColourRandomizer;
    }

    @Override
    public int getFireworksLeft() {
        return FireworkContainer.fireworksLeft;
    }

    @Override
    public void setFireworksLeft(int count) {
        FireworkContainer.fireworksLeft = count;
    }

    @Override
    public void decrementFireworksLeft() {
        --FireworkContainer.fireworksLeft;
    }

    // Current Gimmicks
    private Gimmicks.Sproutlings gimmickSprMo2;
    private Gimmicks.FlourishingBlooms gimmickSprMo3;
    private Gimmicks.Dehydration gimmickSumMo1;
    private Gimmicks.Mirage gimmickSumMo2;
    private Gimmicks.IntoTheFire gimmickSumMo3;
    private Gimmicks.FallsCall gimmickAutMo1;
    private Gimmicks.FlowingWinds gimmickAutMo2;
    private Gimmicks.GhoulsAfoot gimmickAutMo3;
    private Gimmicks.Whiteout gimmickWinMo1;
    private Gimmicks.SnowMounds gimmickWinMo2;
    private Gimmicks.Icicles gimmickWinMo3;

    private Gimmicks.RisingEarth gimmickRollSpr;
    private Gimmicks.Conflagration gimmickRollSum;
    private Gimmicks.Haunting gimmickRollAut;
    private Gimmicks.AbsoluteZero gimmickRollWin;

    private static class DescriptionDraw {
        private static final int DURATION = 300;

        public final Gimmicks.HasDescription descObj;
        private final GameTextUtilities.TextBlock descBlk;

        private int counter;

        public DescriptionDraw(Gimmicks.HasDescription descObj) {
            this.descObj = descObj;
            this.descBlk = descObj.getDescription();

            this.counter = 0;
        }

        public int getDrawXOffset() {
            if (counter < 15) {
                return (int) Math.floor(Interpolation.tanStep((descBlk.getWidth() + 16) * 2d, 0d, (counter + 15) / 30d));
            } else {
                return 0;
            }
        }

        public int getDrawY() {
            if (counter < 240) {
                return 400;
            } else {
                return (int) Math.floor(Interpolation.tanStep(
                    400.0, 480.0 + (2.5 * descBlk.getHeight()),
                    (counter - 240.0) / 60.0
                ));
            }
        }

        public boolean update() {
            return (counter++ >= DURATION);
        }
    }

    private DescriptionDraw descriptionToDraw;

    private BlockVortex vortex;
    private Random bvr;

    private BackgroundPieceMovement[] animatedBackgrounds;

    private enum CustomState { PROFILE, FREEFALL, REWIND, FINAL_REWIND, FINAL_REWIND_RECOVERY }
    private CustomState customState;

    private static final int REWIND_TIME = 60 * 5;
    private static final int FINAL_REWIND_TIME = 60 * 40;

    private boolean abilityIsActive(GameEngine engine) {
        return settings.perk.isActive() && engine.gameStarted && engine.gameActive && (
            (currentAbilityTimer > 0)
                || (queuedFreefall || (engine.stat == GameEngine.STAT_CUSTOM && customState == CustomState.FREEFALL))
                || (HasCustomMove.getNextObject(engine, engine.nextPieceCount).block[0].item == -1)
        );
    }

    private static final ColourMixer GLOBAL_MIXER = ColourMixer.rgb(0, 0, 0);

    private class OuterFrame implements IntBinaryOperator {
        protected final GameEngine engine;
        protected final int playerID;

        public OuterFrame(GameEngine engine, int playerID) {
            this.engine = engine;
            this.playerID = playerID;
        }

        private static final int BASE_COLOUR_MENU = 0x00_404040;
        private static final int BASE_COLOUR_REWIND = 0x00_CC00FF;
        private static final int BASE_COLOUR_SPRING_1 = 0x00_00F000;
        private static final int BASE_COLOUR_SPRING_2 = 0x00_80F000;
        private static final int BASE_COLOUR_SUMMER_1 = 0x00_F0F000;
        private static final int BASE_COLOUR_SUMMER_2 = 0x00_F06400;
        private static final int BASE_COLOUR_AUTUMN_1 = 0x00_D85600;
        private static final int BASE_COLOUR_AUTUMN_2 = 0x00_F09200;
        private static final int BASE_COLOUR_WINTER_1 = 0x00_C0FFFF;
        private static final int BASE_COLOUR_WINTER_2 = 0x00_3272FF;

        protected final ColourMixer mixer = ColourMixer.rgb(0, 0, 0);
        protected final ColourMixer auxMixer = ColourMixer.rgb(0, 0, 0);

        @Override
        public int applyAsInt(int x, int y) {
            final int width = engine.field != null ? engine.field.getWidth() : 10;
            final int height = engine.field != null ? engine.field.getHeight() : 20;

            final int maxX = RendererExtension.getShowMeter(receiver) ? width * 4 + 4 : width * 4 + 2;
            final int maxY = height * 4 + 2;

            final double distance =
                Math.abs((maxY * x) - (maxX * y)) / Math.sqrt((double) (maxY * maxY) + (maxX * maxX));

            final double lMult = Interpolation.sineStep(
                1.125, 0.75,
                MathHelper.clamp(
                    distance / (maxX / 2d),
                    0d, 1d
                )
            );

            final double sMult = MathHelper.clamp(1.0 / lMult, 0.0, 1.0);

            final boolean isAbilityActive = abilityIsActive(engine);

            // Set the base colour.
            if (!engine.gameActive) {
                return mixer.setRGB24(BASE_COLOUR_MENU).setLightness(mixer.getLightness() * lMult).getRGB24();
            } else if (inRewind(engine)) {
                return mixer.setRGB24(BASE_COLOUR_REWIND).setLightness(mixer.getLightness() * lMult).getRGB24();
            } else if (!isAbilityActive) {
                switch (currentSeason) {
                    case SPRING: mixer.setRGB24(BASE_COLOUR_SPRING_1); break;
                    case SUMMER: mixer.setRGB24(BASE_COLOUR_SUMMER_1); break;
                    case AUTUMN: mixer.setRGB24(BASE_COLOUR_AUTUMN_1); break;
                    case WINTER: mixer.setRGB24(BASE_COLOUR_WINTER_1); break;
                }
            } else {
                switch (settings.perk) {
                    case SPRING_ACTIVE: mixer.setRGB24(BASE_COLOUR_SPRING_1); break;
                    case SUMMER_ACTIVE: mixer.setRGB24(BASE_COLOUR_SUMMER_1); break;
                    case AUTUMN_ACTIVE: mixer.setRGB24(BASE_COLOUR_AUTUMN_1); break;
                    case WINTER_ACTIVE: mixer.setRGB24(BASE_COLOUR_WINTER_1); break;
                    default: mixer.setRGB24(0x00_FFFFFF); break;
                }
            }

            final double phase = (Math.sin((((timeSpentInSeason + y) / 30d) % (2.0 * Math.PI))) + 1.0) / 2.0;

            // Specific effects for summer and winter.
            if (!isAbilityActive) {
                switch (currentSeason) {
                    case SPRING:
                        auxMixer
                            .setRGB24(BASE_COLOUR_SPRING_2)
                            .setHue(Interpolation.lerp(mixer.getHue(), auxMixer.getHue(), phase))
                            .setSaturation(Interpolation.lerp(mixer.getSaturation(), auxMixer.getSaturation(), phase))
                            .setValue(Interpolation.lerp(mixer.getValue(), auxMixer.getValue(), phase));

                        mixer
                            .setHue(auxMixer.getHue())
                            .setSaturation(auxMixer.getSaturation())
                            .setValue(auxMixer.getValue());
                        break;
                    case SUMMER: {
                        auxMixer
                            .setRGB24(BASE_COLOUR_SUMMER_2)
                            .setHue(Interpolation.lerp(mixer.getHue(), auxMixer.getHue(), phase))
                            .setSaturation(Interpolation.lerp(mixer.getSaturation(), auxMixer.getSaturation(), phase))
                            .setValue(Interpolation.lerp(mixer.getValue(), auxMixer.getValue(), phase));

                        mixer
                            .setHue(auxMixer.getHue())
                            .setSaturation(auxMixer.getSaturation())
                            .setValue(auxMixer.getValue());
                        break;
                    }
                    case AUTUMN:
                        auxMixer
                            .setRGB24(BASE_COLOUR_AUTUMN_2)
                            .setHue(Interpolation.lerp(mixer.getHue(), auxMixer.getHue(), phase))
                            .setSaturation(Interpolation.lerp(mixer.getSaturation(), auxMixer.getSaturation(), phase))
                            .setValue(Interpolation.lerp(mixer.getValue(), auxMixer.getValue(), phase));

                        mixer
                            .setHue(auxMixer.getHue())
                            .setSaturation(auxMixer.getSaturation())
                            .setValue(auxMixer.getValue());
                        break;
                    case WINTER: {
                        auxMixer
                            .setRGB24(BASE_COLOUR_WINTER_2)
                            .setHue(Interpolation.lerp(mixer.getHue(), auxMixer.getHue(), 1 - phase))
                            .setSaturation(Interpolation.lerp(mixer.getSaturation(), auxMixer.getSaturation(), 1 - phase))
                            .setValue(Interpolation.lerp(mixer.getValue(), auxMixer.getValue(), 1 - phase));

                        mixer
                            .setHue(auxMixer.getHue())
                            .setSaturation(auxMixer.getSaturation())
                            .setValue(auxMixer.getValue());
                        break;
                    }
                    default:
                        break;
                }
            }

            mixer.setLightness(mixer.getLightness() * lMult).setSaturation(sMult);

            // Roll Shimmer
            if (engine.ending != 0 && engine.gameActive && !isAbilityActive) {
                final double rollPhase = (Math.sin((-timeSpentInSeason + y) / 10d) + 1.0) / 2.0;

                mixer.setLightness(Interpolation.lerp(
                    mixer.getLightness(),
                    mixer.getLightness() * 2.25,
                    rollPhase
                ));
            }

            // Active ability glow
            if (isAbilityActive) {
                final double abilityPhase = (Math.cos((((timeSpentInSeason + y) / 6d) % (2.0 * Math.PI))) + 1.0) / 2.0;
                mixer.setLightness(Interpolation.lerp(mixer.getLightness(), 1.0, abilityPhase));
            }

            return mixer.getRGB24();
        }
    }

    private class InnerFrame extends OuterFrame {
        public InnerFrame(GameEngine engine, int playerID) {
            super(engine, playerID);
        }

        @Override
        public int applyAsInt(int x, int y) {
            return auxMixer.setRGB24(super.applyAsInt(x, y)).setValue(auxMixer.getValue() * 0.25).getRGB24();
        }
    }

    private class MeterFunction implements IntSupplier {
        private final GameEngine engine;
        private final ColourMixer mixer = ColourMixer.hsv(0, 1, 1);

        public MeterFunction(GameEngine engine) {
            this.engine = engine;
        }

        @Override
        public int getAsInt() {
            if (settings.perk.isActive()) {
                final double proportion = engine.meterValue / (double) receiver.getMeterMax(engine);

                if (currentAbilityTimer > 0) return mixer.setHueAngle(Interpolation.lerp(0.0, 120.0, proportion)).getRGB24();
                else if (currentEnergy == settings.perk.energyStore) return 0x00_00FF00;
                else return mixer.setHueAngle(Interpolation.lerp(0.0, 60.0, proportion)).getRGB24();
            } else {
                switch (currentSeason) {
                    case SPRING:
                        return mixer.setHueAngle(120).getRGB24();
                    case SUMMER:
                        return mixer.setHueAngle(60).getRGB24();
                    case AUTUMN:
                        return mixer.setHueAngle(30).getRGB24();
                    case WINTER:
                        return 0x00_30A8FF;
                    default:
                        return 0x00FFFFFF;
                }
            }
        }
    }

    private FrameDrawingParameters frameParams;

    @Override
    public FrameDrawingParameters getFrameDrawingParameters(GameEngine engine, int playerID) {
        return frameParams;
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

    private void setNewBackground(int newBg) {
        if (currentBackground == newBg) return;

        lastBackground = currentBackground;
        currentBackground = newBg;
        fadeProgress = 0;
    }

    private void updateFadeProgress() {
        fadeProgress = Math.min(300, fadeProgress + 15);
    }

    private void clearBaseGameGimmicks() {
        gimmickSprMo2 = null;
        gimmickSprMo3 = null;
        gimmickSumMo1 = null;
        gimmickSumMo2 = null;
        gimmickSumMo3 = null;
        gimmickAutMo1 = null;
        gimmickAutMo2 = null;
        gimmickAutMo3 = null;
        gimmickWinMo1 = null;
        gimmickWinMo2 = null;
        gimmickWinMo3 = null;
    }

    private void clearRollGimmicks() {
        gimmickRollSpr = null;
        gimmickRollSum = null;
        gimmickRollAut = null;
        gimmickRollWin = null;
    }

    @Override
    public String getName() {
        return "SEASONS";
    }

    @Override
    public void playerInit(GameEngine engine, int playerID) {
        owner = engine.owner;
        receiver = engine.owner.receiver;

        // Load all unique sound effects for this mode.
        SoundLoader.Sounds.Seasons.loadAllSounds();

        if (animatedBackgrounds == null) {
            animatedBackgrounds = new BackgroundPieceMovement[] {
                /* FEB */ new BackgroundPieceMovement(0, 1f),
                /* MAR */ new BackgroundPieceMovement(1, 1f),
                /* APR */ new BackgroundPieceMovement(2, 1f),
                /* MAY */ new BackgroundPieceMovement(3, 1f),
                /* JUN */ new BackgroundPieceMovement(4, 1f),
                /* JUL */ new BackgroundPieceMovement(5, 1f),
                /* AUG */ new BackgroundPieceMovement(6, 1f),
                /* SEP */ new BackgroundPieceMovement(7, 1f),
                /* OCT */ new BackgroundPieceMovement(8, 1f),
                /* NOV */ new BackgroundPieceMovement(9, 1f),
                /* DEC */ new BackgroundPieceMovement(10, 1f),
                /* JAN */ new BackgroundPieceMovement(11, 1f),
            };
        }

        customGraphics = new CustomResourceHolder();
        rendererExtension = new RendererExtension(customGraphics);
        drawing = new PrimitiveDrawingHook(customGraphics);
        statesAtTimes = new TreeMap<>();
        textEmitter = new TextEmitter();
        rewindBlocks = new LinkedList<>();

        FireworkContainer.fireworks = new Fireworks(customGraphics, engine.randSeed ^ 0x73556080);
        FireworkContainer.fireworks.enableSounds(engine);
        FireworkContainer.fireworkColourRandomizer = new Random(engine.randSeed ^ 0x07355608);
        FireworkContainer.fireworksLeft = 0;

        HasCelebrationFireworks.super.fireworksSetup();

        // For Zero Celsius and Absolute Zero
        customGraphics.loadImage("res/graphics/iceblock.png", "iceblock");

        setupBackgrounds(engine);
        frameParams = new FrameDrawingParameters(
            new OuterFrame(engine, playerID),
            new InnerFrame(engine, playerID),
            new MeterFunction(engine)
        );

        levelUpFlag = false;
        currentSeason = Season.SPRING;
        nextSectionLevel = 0;
        badges = new Badges();
        lockedPieces = 0;
        customState = CustomState.PROFILE;
        rollLevelReached = -1;
        fieldPurifyQueued = false;
        descriptionToDraw = null;
        timeSpentInSeason = 0;
        activeTimeSpentInSeason = 0;
        lineClearAfterPiece = true;
        waCurrentFreezeRow = 19;

        lastRank = -1;
        lastRankPlayer = -1;

        rollStarted = false;
        rollTime = 0;
        rollElapsed = 0;

        grading = new Grading();
        totalGrades = null;

        // Clear all gimmicks.
        clearBaseGameGimmicks();
        clearRollGimmicks();

        vortex = new BlockVortex();

        if (ruleOptCopy == null) {
            ruleOptCopy = new RuleOptions(engine.ruleopt);
        }

        engine.framecolor = Season.defaultMenuFrameColour();

        if (playerProperties == null) {
            playerProperties = new ProfileProperties(HEADER_COLOUR);
            showPlayerStats = false;
        }

        showBoard = SubRanking.DATE;

        settings = new SeasonsSettings(CURRENT_VERSION, playerProperties, () -> new SeasonsSettings.LeaderboardEntry(
            totalGrades.totalGradePoints,
            rollLevelReached,
            engine.statistics.level,
            engine.statistics.time,
            settings.perk
        ));

        settingsMenu = MenuBuilder.generateMenu(this, settings);

        if (!owner.replayMode) {
            settings.loadSetting(owner.modeConfig, false);
            settings.loadRanking(owner, engine.ruleopt.strRuleName);

            if (playerProperties.isLoggedIn()) {
                settings.loadSettingPlayer(playerProperties);
                settings.loadRankingPlayer(playerProperties, engine.ruleopt.strRuleName);
            }
        } else {
            settings.loadSetting(owner.replayProp, true);
        }

        lastBackground = 0;
        currentBackground = 0;
        fadeProgress = 300;

        engine.comboType = GameEngine.COMBO_TYPE_NORMAL;
    }

    // 0 1 2 3 4 (4.5) 5 6 7 8 9
    // 0 1 2 3 (4) 5 6 7 8
    private void addRewindBlockFromField(GameEngine engine, int playerID, int x, int y) {
        final int baseX = 4 + receiver.getFieldDisplayPositionX(engine, playerID);
        final int baseY = 52 + receiver.getFieldDisplayPositionY(engine, playerID);

        final Block blk = engine.field.getBlock(x, y);
        if (blk == null) return;

        final double midpoint = ((engine.field.getWidth()) / 2.0) - 0.5;
        final double difference = x - midpoint;

        final double yvr = (rewindBlockRandom.nextDouble() - 0.5) * 2 * 0.125;

        rewindBlocks.add(
            new RewindBlock(
                60, 0,
                new DoubleVector(baseX + (x * 16) + 8, baseY + (y * 16) + 8, false),
                new DoubleVector((difference * 4.0) / 30.0, -8.0 - ((engine.field.getHeightWithoutHurryupFloor() - y) * 0.25) + yvr, false),
                new DoubleVector((difference * -4.0) / (30.0 * 120.0), 9.80665 / 15.0, false),
                blk
            )
        );
    }

    private void addRewindBlocks(GameEngine engine, int playerID, Field olderField) {
        if (engine.field == null) return;

        if (FieldManipulation.getNumberOfBlocks(engine.field) > FieldManipulation.getNumberOfBlocks(olderField)) {
            for (int y = 0; y < engine.field.getHeightWithoutHurryupFloor(); ++y) {
                for (int x = 0; x < engine.field.getWidth(); ++x) {
                    if (engine.field.getBlockColor(x, y) != olderField.getBlockColor(x, y) && !engine.field.getBlockEmpty(x, y)) {
                        addRewindBlockFromField(engine, playerID, x, y);
                    }
                }
            }
        }
    }

    private int performRewindSteps(GameEngine engine, int playerID, int rewindTime, int rewindStartDelay, int oldestStateSeconds) {
        if (engine.statc[0] >= rewindStartDelay) {
            final NavigableSet<Integer> keys = statesAtTimes.navigableKeySet();
            final int maxTime = keys.last();
            final int minTime = Objects.requireNonNull(keys.ceiling(engine.statistics.time - (oldestStateSeconds * 60)));

            final List<Integer> validKeys = keys
                .stream()
                .filter(t -> t >= minTime && t <= maxTime)
                .collect(Collectors.toList());

            final int interpTime = (int) Math.ceil(Interpolation.tanStep(
                validKeys.size() - 1d, 0d,
                MathHelper.clamp(
                    (engine.statc[0] - (double) rewindStartDelay) / (rewindTime - (double) rewindStartDelay),
                    0.0, 1.0
                )
            ));

            final NextAndFieldState state = statesAtTimes.get(validKeys.get(interpTime));

            if (!FieldManipulation.fieldShapeEquals(engine.field, state.field)
                || engine.nextPieceCount != state.nextPosition) {
                if (!FieldManipulation.fieldShapeEquals(engine.field, state.field)) engine.playSE("step");
                else engine.playSE("move");

                addRewindBlocks(engine, playerID, state.field);
                engine.field = new Field(state.field);

                for (int i = state.nextPosition; i < engine.nextPieceCount + engine.ruleopt.nextDisplay; ++i) {
                    engine.nextPieceArrayObject[i] = HasCustomMove.initialisePiece(engine, engine.nextPieceArrayID[i]);
                }

                engine.nextPieceCount = state.nextPosition;

                if (state.holdPiece != null) {
                    engine.holdPieceObject = HasCustomMove.initialisePiece(engine, state.holdPiece.id);
                } else {
                    engine.holdPieceObject = null;
                }
            }

            return minTime;
        }

        return -1;
    }

    @Override
    public boolean onCustom(GameEngine engine, int playerID) {
        if (engine.gameStarted) {
            switch (customState) {
                case FREEFALL: {
                    if (engine.statc[0] == 0) {
                        engine.speed.lineDelay = Math.max(20, engine.speed.lineDelay);
                    }

                    // Arrived here from ARE, this is where the freefall happens.
                    if (engine.statc[0] > 0 && engine.statc[0] % 2 == 0) {
                        final Field oldField = new Field(engine.field);
                        final boolean landed = FieldManipulation.freeFallStep(engine.field);

                        if (!FieldManipulation.fieldShapeEquals(engine.field, oldField)) {
                            hasLandedBefore = landed;
                            if (hasLandedBefore) engine.playSE("linefall");
                        } else {
                            getCompleteLinesWithoutFlagging(engine);

                            if (engine.lineClearing == 0) {
                                engine.stat = GameEngine.STAT_ARE;
                                engine.resetStatc();
                            } else {
                                engine.stat = GameEngine.STAT_LINECLEAR;
                                lineClearAfterPiece = false;
                                engine.resetStatc();

                                engine.tspin = false;
                                engine.tspinmini = false;
                                engine.tspinez = false;

                                engine.statLineClear();
                            }
                        }
                    }
                }
                    break;
                case REWIND: {
                    engine.timerActive = false;

                    if (engine.statc[0] == 0) {
                        statesAtTimes.keySet().removeIf(k -> k < engine.statistics.time - (60 * 600));
                    }

                    final int boardTime = performRewindSteps(engine, playerID, REWIND_TIME, 30, 60);

                    if (engine.statc[0] >= REWIND_TIME) {
                        engine.speed.lineDelay = 60;

                        if (gimmickAutMo3 != null) {
                            engine.blockShowOutlineOnly = true;
                        }

                        if (gimmickWinMo1 != null && engine.statistics.level >= LEVELS_NOV) {
                            for (int i = engine.nextPieceCount; i <= engine.nextPieceCount + engine.ruleopt.nextDisplay; ++i) {
                                for (Block blk : engine.nextPieceArrayObject[i].block) {
                                    blk.color = Block.BLOCK_COLOR_GRAY;
                                    blk.bonusValue |= Gimmicks.Whiteout.SNOW_IDENTIFIER;
                                }
                            }

                            if (engine.holdPieceObject != null) {
                                for (Block blk : engine.holdPieceObject.block) {
                                    blk.color = Block.BLOCK_COLOR_GRAY;
                                    blk.bonusValue |= Gimmicks.Whiteout.SNOW_IDENTIFIER;
                                }
                            }
                        }

                        clearAddedSummerIPieces(engine);

                        // This *SHOULD* stop weird block mods from other seasons leaking in.
                        if (boardTime < (engine.statistics.time - activeTimeSpentInSeason)) {
                            purifyFieldAndNexts(engine);
                        }

                        engine.stat = GameEngine.STAT_LINECLEAR;
                        engine.resetStatc();
                    }
                }
                    break;
                case FINAL_REWIND: {
                    engine.timerActive = false;

                    if (engine.statc[0] == 0) {
                        statesAtTimes.keySet().removeIf(k -> k < engine.statistics.time - (60 * 600));
                    }

                    if (settings.hasSeenRollIntro && engine.ctrl.isPush(Controller.BUTTON_D) && engine.statc[0] < FINAL_REWIND_TIME) {
                        engine.statc[0] = FINAL_REWIND_TIME;
                    }

                    performRewindSteps(engine, playerID, FINAL_REWIND_TIME, 180, 600);

                    if (engine.statc[0] >= 180) {
                        engine.statistics.level = (int) Math.ceil(Interpolation.tanStep(
                            MAX_LEVEL, 0d,
                            MathHelper.clamp(
                                (engine.statc[0] - 180d) / (FINAL_REWIND_TIME - 180d),
                                0.0, 1.0
                            )
                        ));
                    }

                    if (engine.statc[0] >= FINAL_REWIND_TIME) {
                        clearAddedSummerIPieces(engine);

                        naturalLevelIncrement = INCREMENT_IN_ROLL;
                        levelUpFlag = false;
                        currentSeason = Season.SPRING;

                        customState = CustomState.FINAL_REWIND_RECOVERY;
                        engine.resetStatc();
                    }
                }
                    break;
                case FINAL_REWIND_RECOVERY: {
                    // Do another READY..GO! here.
                    if (engine.statc[0] == engine.readyStart) {
                        engine.playSE("ready");
                        purifyFieldAndNexts(engine);
                    }

                    if (engine.statc[0] == engine.goStart) {
                        engine.playSE("go");
                    }

                    if (engine.statc[0] >= engine.goEnd) {
                        engine.speed.lineDelay = 0;
                        engine.speed.areLine = 0;

                        engine.stat = GameEngine.STAT_LINECLEAR;

                        gimmickRollSpr = new Gimmicks.RisingEarth(new Random(engine.randSeed * 7355608), badges, getGimmickPerkBoost());
                        descriptionToDraw = new DescriptionDraw(gimmickRollSpr);

                        engine.resetStatc();
                    }
                }
                    break;
                default:
                    break;
            }

            engine.statc[0]++;
            return true;
        } else {
            showPlayerStats = false;
            engine.isInGame = true;

            final boolean s = playerProperties.loginScreen.updateScreen(engine, playerID);
            if (playerProperties.isLoggedIn()) {
                settings.loadRankingPlayer(playerProperties, engine.ruleopt.strRuleName);
                settings.loadSettingPlayer(playerProperties);
            }

            if (engine.stat == GameEngine.STAT_SETTING) engine.isInGame = false;

            return s;
        }
    }

    private void clearAddedSummerIPieces(GameEngine engine) {
        for (int i = engine.nextPieceArraySize - 1; i >= engine.nextPieceCount; --i) {
            if (engine.nextPieceArrayObject[i].block[0].item == -1) {
                HasCustomMove.removeFromNext(engine, i);
            }
        }
    }

    private static final GameTextUtilities.TextBlock ENDING_START_PASSAGE = GameTextUtilities.TextBlock.of(
        GameTextUtilities.TextJustification.CENTRE,
        GameTextUtilities.Text.custom("THE ", EventReceiver.COLOR_WHITE, (1f / 2f)),
        GameTextUtilities.Text.custom("WORDS", EventReceiver.COLOR_BLUE, 0.75f),
        GameTextUtilities.Text.custom(" ARE", EventReceiver.COLOR_WHITE, (1f / 2f)),
        GameTextUtilities.Text.newLine(),
        GameTextUtilities.Text.custom("DYING", EventReceiver.COLOR_RED, 0.75f),
        GameTextUtilities.Text.custom(" IN THE NIGHT", EventReceiver.COLOR_WHITE, (1f / 2f)),
        GameTextUtilities.Text.newLine(),
        GameTextUtilities.Text.custom("NO ", EventReceiver.COLOR_WHITE, (1f / 2f)),
        GameTextUtilities.Text.custom("WINTER", EventReceiver.COLOR_CYAN, 0.75f),
        GameTextUtilities.Text.custom(" LASTS FOREVER", EventReceiver.COLOR_WHITE, (1f / 2f)),
        GameTextUtilities.Text.newLine(),
        GameTextUtilities.Text.custom("THE ", EventReceiver.COLOR_WHITE, (1f / 2f)),
        GameTextUtilities.Text.custom("SEASONS", EventReceiver.COLOR_GREEN, 0.75f),
        GameTextUtilities.Text.custom(" PASS AND", EventReceiver.COLOR_WHITE, (1f / 2f)),
        GameTextUtilities.Text.newLine(),
        GameTextUtilities.Text.custom("SUNLIGHT", EventReceiver.COLOR_YELLOW, 0.75f),
        GameTextUtilities.Text.custom(" WILL SHINE", EventReceiver.COLOR_WHITE, (1f / 2f)),
        GameTextUtilities.Text.newLine(),
        GameTextUtilities.Text.custom("ON MY LIFE AGAIN", EventReceiver.COLOR_WHITE, (1f / 2f)),
        GameTextUtilities.Text.newLine(),
        GameTextUtilities.Text.custom("SO LET THE ", EventReceiver.COLOR_WHITE, (1f / 2f)),
        GameTextUtilities.Text.custom("PAST", EventReceiver.COLOR_PINK, 0.75f),
        GameTextUtilities.Text.newLine(),
        GameTextUtilities.Text.custom("NOW ", EventReceiver.COLOR_WHITE, (1f / 2f)),
        GameTextUtilities.Text.custom("BURN", EventReceiver.COLOR_ORANGE, 1f),
        GameTextUtilities.Text.newLine(),
        GameTextUtilities.Text.custom("DOWN IN ", EventReceiver.COLOR_WHITE, (1f / 2f)),
        GameTextUtilities.Text.custom("FLAMES", EventReceiver.COLOR_ORANGE, 1f)
    );

    @Override
    public void renderCustom(GameEngine engine, int playerID) {
        if (engine.field != null) {
            int baseX = (8 * engine.field.getWidth()) + 4 + receiver.getFieldDisplayPositionX(engine, playerID);
            int baseY = (8 * engine.field.getHeight()) + 52 + receiver.getFieldDisplayPositionY(engine, playerID);

            if (engine.gameStarted && customState == CustomState.FREEFALL) {
                // region Freefall
                int alpha = 255;
                if (engine.statc[0] >= 15) {
                    alpha = Interpolation.lerp(255, 0, (engine.statc[0] - 15d) / 30d);
                }

                GameTextUtilities.drawAlignedTextBlock(
                    engine,
                    baseX, baseY,
                    false,
                    GameTextUtilities.TextBlock.of(
                        GameTextUtilities.TextJustification.LEFT,
                        GameTextUtilities.Text.ofMixColorBig("FREE", (engine.statc[0] >>> 1) % 2 == 0 ? EventReceiver.COLOR_WHITE : EventReceiver.COLOR_YELLOW, 255, 255, 255, alpha),
                        GameTextUtilities.Text.newLine(),
                        GameTextUtilities.Text.ofMixColorBig("FALL", (engine.statc[0] >>> 1) % 2 == 0 ? EventReceiver.COLOR_WHITE : EventReceiver.COLOR_YELLOW, 255, 255, 255, alpha)
                    ),
                    ObjectAlignment.MIDDLE_MIDDLE
                );
                // endregion Freefall
            } else if (engine.gameStarted && customState == CustomState.REWIND) {
                // region Rewind
                int alpha = 255;
                if (engine.statc[0] >= 180) {
                    alpha = Interpolation.lerp(255, 0, (engine.statc[0] - 180d) / 120d);
                }

                GameTextUtilities.drawAlignedTextBlock(
                    engine,
                    baseX, baseY,
                    false,
                    GameTextUtilities.TextBlock.of(
                        GameTextUtilities.TextJustification.LEFT,
                        GameTextUtilities.Text.of(" "),
                        GameTextUtilities.Text.ofMixColorBig(
                            "TIME", (engine.statc[0] >>> 1) % 2 == 0 ? EventReceiver.COLOR_YELLOW : EventReceiver.COLOR_ORANGE, 255, 255, 255, alpha
                        ),
                        GameTextUtilities.Text.newLine(),
                        GameTextUtilities.Text.ofMixColorBig(
                            "WARP", (engine.statc[0] >>> 1) % 2 == 0 ? EventReceiver.COLOR_YELLOW : EventReceiver.COLOR_ORANGE, 255, 255, 255, alpha
                        )
                    ),
                    ObjectAlignment.MIDDLE_MIDDLE
                );
                // endregion Rewind
            } else if (engine.gameStarted && customState == CustomState.FINAL_REWIND) {
                // region Final Rewind
                if (settings.hasSeenRollIntro && (!owner.replayMode || owner.replayRerecord)) {
                    receiver.drawMenuFont(engine, playerID, 0, 21, "D:SKIP b", EventReceiver.COLOR_GREEN);
                }

                if (engine.statc[0] < 600) {
                    int alpha = 255;
                    if (engine.statc[0] >= 180) {
                        alpha = Interpolation.lerp(255, 0, (engine.statc[0] - 180d) / 420d);
                    }

                    GameTextUtilities.drawAlignedTextBlock(
                        engine,
                        baseX, baseY,
                        false,
                        GameTextUtilities.TextBlock.of(
                            GameTextUtilities.TextJustification.LEFT,
                            GameTextUtilities.Text.of(" "),
                            GameTextUtilities.Text.ofMixColorBig(
                                "TIME", (engine.statc[0] >>> 1) % 2 == 0 ? EventReceiver.COLOR_YELLOW : EventReceiver.COLOR_ORANGE, 255, 255, 255, alpha
                            ),
                            GameTextUtilities.Text.newLine(),
                            GameTextUtilities.Text.ofMixColorBig(
                                "WARP", (engine.statc[0] >>> 1) % 2 == 0 ? EventReceiver.COLOR_YELLOW : EventReceiver.COLOR_ORANGE, 255, 255, 255, alpha
                            ),
                            GameTextUtilities.Text.ofMixColor(
                                "?", (engine.statc[0] >>> 1) % 2 == 0 ? EventReceiver.COLOR_YELLOW : EventReceiver.COLOR_ORANGE, 255, 255, 255, alpha
                            )
                        ),
                        ObjectAlignment.MIDDLE_MIDDLE
                    );
                }

                final int basisTime = FINAL_REWIND_TIME - (60 * 21);

                // This is for gradually showing the ending passage.
                if (engine.statc[0] >= basisTime) {
                    drawing.drawRectangle(
                        receiver,
                        receiver.getFieldDisplayPositionX(engine, playerID) + 4 - 32, baseY - 68,
                        (engine.field.getWidth() + 4) * 16, 120,
                        0, 0, 0, 180,
                        true
                    );
                }

                int maxY = baseY - 64;
                if (engine.statc[0] >= basisTime) maxY += 12;
                if (engine.statc[0] >= (basisTime + ((60 * 1) + 30))) maxY += 12;
                if (engine.statc[0] >= (basisTime + ((60 * 4) + 48))) maxY += 12;
                if (engine.statc[0] >= (basisTime + ((60 * 9) + 48))) maxY += 12;
                if (engine.statc[0] >= (basisTime + ((60 * 12) + 0))) maxY += 12;
                if (engine.statc[0] >= (basisTime + ((60 * 14) + 24))) maxY += 8;
                if (engine.statc[0] >= (basisTime + ((60 * 16) + 48))) maxY += 12;
                if (engine.statc[0] >= (basisTime + ((60 * 18) + 24))) maxY += 16;
                if (engine.statc[0] >= (basisTime + ((60 * 19) + 12))) maxY = 480;

                GameTextUtilities.drawAlignedBoundedTextBlock(
                    engine,
                    baseX, baseY - 64,
                    0, 0, 640, maxY,
                    false,
                    ENDING_START_PASSAGE,
                    ObjectAlignment.TOP_MIDDLE
                );
                // endregion Final Rewind
            } else if (engine.gameStarted && customState == CustomState.FINAL_REWIND_RECOVERY) {
                receiver.renderReady(engine, playerID);
            }
        }
    }

    @Override
    public boolean onReady(GameEngine engine, int playerID) {
        if (engine.statc[0] == 0) {
            bvr = new Random(engine.randSeed);
            rewindBlockRandom = new Random(engine.randSeed);

            for (BackgroundPieceMovement bg : animatedBackgrounds) bg.setZoomFactor(settings.wobble ? 1.025f : 1f);
            for (AnimatedBackgroundHook bg : animatedBackgrounds) bg.reset();

            engine.statistics.level = 0;
            setSpeed(engine);

            // Setup active ability stuff.
            currentEnergy = 0;
            currentAbilityTimer = 0;

            // Setup freefall stuff
            hasLandedBefore = false;
            queuedFreefall = false;

            // Summer passive locked pieces counter.
            lockedPieces = 0;
            engine.ruleopt = new RuleOptions(ruleOptCopy);

            if (settings.perk == SeasonPerk.SUMMER_PASSIVE) {
                engine.ruleopt.nextDisplay += 3;
            }
        }

        return false;
    }

    @Override
    public boolean onSetting(GameEngine engine, int playerID) {
        if (!engine.owner.replayMode) {
            // Configuration changes
            settingsMenu.updateSettings(engine, playerID);

            if (engine.ctrl.isPush(Controller.BUTTON_A) && (engine.statc[3] >= 5)) {
                engine.playSE("decide");

                if (playerProperties.isLoggedIn()) {
                    settings.saveSettingPlayer(playerProperties);
                    settings.commitPlayerSettingAndRank(playerProperties);
                } else {
                    settings.saveSetting(owner.modeConfig, false);
                    settings.commitSettingAndRank(receiver, owner);
                }

                return false;
            }

            if (engine.ctrl.isPush(Controller.BUTTON_B)) {
                engine.quitflag = true;
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

            return engine.statc[3] < 120;
        }

        return true;
    }

    @Override
    public void renderSetting(GameEngine engine, int playerID) {
        settingsMenu.renderSettings(engine, playerID, receiver, 0);
    }

    private void setSpeed(GameEngine engine) {
        if (gimmickSumMo3 != null) {
            engine.speed = gimmickSumMo3.getSpeed(engine, badges, getGimmickPerkBoost());
        } else if (gimmickRollSum != null) {
            engine.speed = gimmickRollSum.getSpeed(engine, badges, getGimmickPerkBoost());
        } else if (engine.ending == 0) {
            engine.speed = SPEED_TABLE.apply(engine.statistics.level);
        } else {
            engine.speed = ROLL_SPEED_TABLE.apply(engine.statistics.level);
        }

        final boolean instantG = engine.speed.gravity < 0 || ((engine.speed.gravity / engine.speed.denominator) >= (engine.field == null ? 20 : engine.field.getHeight()));
        if (settings.perk == SeasonPerk.WINTER_PASSIVE) {
            engine.speed.lockDelay += 6;

            // Slow gravity to 80% speed.
            if (!instantG) {
                final double gravityProp = engine.speed.gravity / (double) engine.speed.denominator;
                final int newGravity = (int) Math.floor(gravityProp * 65536d * 0.8);

                engine.speed.gravity = newGravity;
                engine.speed.denominator = 65536;
            }
        }

        if (settings.perk == SeasonPerk.WINTER_ACTIVE && abilityIsActive(engine)) {
            engine.speed.gravity = 0;
        }

        if (gimmickAutMo1 != null) {
            // VERY LOW ARE:
            engine.speed.are = 8;
            engine.speed.areLine = 8;

            engine.speed.gravity = 0;
            engine.speed.denominator = 1;
        }
    }

    @Override
    public void startGame(GameEngine engine, int playerID) {
        engine.statistics.level = 0;

        nextSectionLevel = NEXT_SECTION_LEVELS.apply(engine.statistics.level);
        setNewBackground(BACKGROUND_TABLE.apply(engine.statistics.level));

        fireAndSnowEffect = new FireAndSnow(engine.randSeed * 23457968);

        owner.backgroundStatus.bg = -1;
        owner.backgroundStatus.fadebg = -1;

        engine.ghost = true;

        engine.big = false;
        engine.bigmove = true;
        engine.bighalf = true;

        engine.tspinEnable = settings.spinType != 2;
        engine.useAllSpinBonus = true;
        engine.tspinAllowKick = true;
        engine.tspinEnableEZ = true;
        engine.spinCheckType = settings.spinType == 2 ? 0 : settings.spinType;
        engine.tspinminiType = GameEngine.TSPINMINI_TYPE_ROTATECHECK;

        engine.b2bEnable = true;

        engine.staffrollEnableStatistics = false;
        engine.rainbowAnimate = true;

        naturalLevelIncrement = 1;

        sparksRandomiser = new Random(engine.randSeed * 2);
        sparks = new SurfaceSparks(customGraphics, sparksRandomiser);

        lpRandomiser = new Random(engine.randSeed * 3);
        landingParticles = new LandingParticles(customGraphics, lpRandomiser);

        setSpeed(engine);
        owner.bgmStatus.bgm = BGM_TABLE.apply(engine.statistics.level);
    }

    @Override
    public void inPieceSpawn(GameEngine engine, int playerID) {
        // 出現時の処理
        if (engine.statc[0] == 0) {
            // Store current field state.
            if (engine.statc[1] == 0) {
                if (gimmickSumMo2 != null) gimmickSumMo2.replaceQueue(engine);

                statesAtTimes.put(engine.statistics.time + rollElapsed, new NextAndFieldState(engine));
            }

            if ((engine.statc[1] == 0) && (!engine.initialHoldFlag)) {
                // 通常出現
                engine.nowPieceObject = HasCustomMove.getNextObjectCopy(engine, engine.nextPieceCount);
                engine.nextPieceCount++;
                if (engine.nextPieceCount < 0) engine.nextPieceCount = 0;
                engine.holdDisable = false;
            } else {
                // ホールド出現
                if (engine.initialHoldFlag) {
                    // 先行ホールド
                    if (engine.holdPieceObject == null) {
                        // 1回目
                        engine.holdPieceObject = HasCustomMove.getNextObjectCopy(engine, engine.nextPieceCount);
                        engine.holdPieceObject.applyOffsetArray(engine.ruleopt.pieceOffsetX[engine.holdPieceObject.id], engine.ruleopt.pieceOffsetY[engine.holdPieceObject.id]);
                        engine.nextPieceCount++;
                        if (engine.nextPieceCount < 0) engine.nextPieceCount = 0;

                        if (engine.bone)
                            engine.getNextObject(engine.nextPieceCount + engine.ruleopt.nextDisplay - 1).setAttribute(Block.BLOCK_ATTRIBUTE_BONE, true);

                        engine.nowPieceObject = HasCustomMove.getNextObjectCopy(engine, engine.nextPieceCount);
                        engine.nextPieceCount++;
                        if (engine.nextPieceCount < 0) engine.nextPieceCount = 0;
                    } else {
                        // 2回目以降
                        Piece pieceTemp = engine.holdPieceObject;
                        engine.holdPieceObject = HasCustomMove.getNextObjectCopy(engine, engine.nextPieceCount);
                        engine.holdPieceObject.applyOffsetArray(engine.ruleopt.pieceOffsetX[engine.holdPieceObject.id], engine.ruleopt.pieceOffsetY[engine.holdPieceObject.id]);
                        engine.nowPieceObject = pieceTemp;
                        engine.nextPieceCount++;
                        if (engine.nextPieceCount < 0) engine.nextPieceCount = 0;
                    }
                } else {
                    // 通常ホールド
                    if (engine.holdPieceObject == null) {
                        // 1回目
                        engine.nowPieceObject.big = false;
                        engine.holdPieceObject = engine.nowPieceObject;
                        engine.nowPieceObject = HasCustomMove.getNextObjectCopy(engine, engine.nextPieceCount);
                        engine.nextPieceCount++;
                        if (engine.nextPieceCount < 0) engine.nextPieceCount = 0;
                    } else {
                        // 2回目以降
                        engine.nowPieceObject.big = false;
                        Piece pieceTemp = engine.holdPieceObject;
                        engine.holdPieceObject = engine.nowPieceObject;
                        engine.nowPieceObject = pieceTemp;
                    }
                }

                // Directionを戻す
                if (engine.ruleopt.holdResetDirection && (engine.ruleopt.pieceDefaultDirection[engine.holdPieceObject.id] < Piece.DIRECTION_COUNT)) {
                    final Optional<Block> blk = Arrays.stream(engine.holdPieceObject.block).filter(b -> b.bonusValue > 0 && b.bonusValue < 16).findAny();
                    engine.holdPieceObject.direction = blk.map(b -> b.bonusValue >>> 1)
                        .orElseGet(() -> engine.ruleopt.pieceDefaultDirection[engine.holdPieceObject.id]);

                    engine.holdPieceObject.updateConnectData();
                } else if (engine.ruleopt.holdResetDirection) {
                    final Optional<Block> blk = Arrays.stream(engine.holdPieceObject.block).filter(b -> b.bonusValue > 0 && b.bonusValue < 16).findAny();
                    blk.ifPresent(b -> engine.holdPieceObject.direction = b.bonusValue >>> 1);
                }

                // 使用した count+1
                engine.holdUsedCount++;
                engine.statistics.totalHoldUsed++;

                // ホールド無効化
                engine.initialHoldFlag = false;
                engine.holdDisable = true;
            }

            // For connectivity.
            if (engine.nowPieceObject != null) {
                for (Block blk : engine.nowPieceObject.block) blk.pieceNum = lockedPieces;
            }

            engine.playSE("piece" + HasCustomMove.getNextObject(engine, engine.nextPieceCount).id);

            if (!engine.nowPieceObject.offsetApplied)
                engine.nowPieceObject.applyOffsetArray(engine.ruleopt.pieceOffsetX[engine.nowPieceObject.id], engine.ruleopt.pieceOffsetY[engine.nowPieceObject.id]);

            engine.nowPieceObject.big = engine.big;

            // 出現位置 (横）
            engine.nowPieceX = engine.getSpawnPosX(engine.field, engine.nowPieceObject);

            // 出現位置 (縦）
            engine.nowPieceY = engine.getSpawnPosY(engine.nowPieceObject);

            engine.nowPieceBottomY = engine.nowPieceObject.getBottom(engine.nowPieceX, engine.nowPieceY, engine.field);
            engine.nowPieceColorOverride = -1;

            if (engine.itemRollRollEnable) engine.nowPieceColorOverride = Block.BLOCK_COLOR_GRAY;

            // 先行rotation
            if (engine.versionMajor < 7.5f) engine.initialRotate(); //XXX: Weird active time IRS

            if ((engine.speed.gravity > engine.speed.denominator) && (engine.speed.denominator > 0))
                engine.gcount = engine.speed.gravity % engine.speed.denominator;
            else
                engine.gcount = 0;

            engine.lockDelayNow = 0;
            engine.dasSpeedCount = engine.getDASDelay();
            engine.dasRepeat = false;
            engine.dasInstant = false;
            engine.extendedMoveCount = 0;
            engine.extendedRotateCount = 0;
            engine.softdropFall = 0;
            engine.harddropFall = 0;
            engine.manualLock = false;
            engine.nowPieceMoveCount = 0;
            engine.nowPieceRotateCount = 0;
            engine.nowPieceRotateFailCount = 0;
            engine.nowWallkickCount = 0;
            engine.nowUpwardWallkickCount = 0;
            engine.lineClearing = 0;
            engine.lastmove = GameEngine.LASTMOVE_NONE;
            engine.kickused = false;
            engine.tspin = false;
            engine.tspinmini = false;
            engine.tspinez = false;

            // Don't clear bone blocks from queue if the summer gimmick is active.
            if (gimmickSumMo1 == null) {
                engine.getNextObject(engine.nextPieceCount + engine.ruleopt.nextDisplay - 1).setAttribute(Block.BLOCK_ATTRIBUTE_BONE, engine.bone);
            }

            if (gimmickWinMo3 != null || gimmickRollWin != null) {
                for (Block blk : engine.nowPieceObject.block) blk.bonusValue |= Gimmicks.AbsoluteZero.ZERO_IDENTIFIER;
            }

            if (engine.ending == 0) engine.timerActive = true;

            if ((engine.ai != null) && (!engine.owner.replayMode || engine.owner.replayRerecord))
                engine.ai.newPiece(engine, playerID);
        }
    }

    @Override
    public boolean doInfiniteLockDelayAbove99() {
        return false;
    }

    @Override
    public boolean inPostLockProcessing(GameEngine engine, int playerID, boolean instantlock) {
        if (pieceIsLocking(engine, instantlock)) {
            ++lockedPieces;

            // Add a new I-piece into the next queue if using summer passive perk.
            if (lockedPieces % 50 == 0 && settings.perk == SeasonPerk.SUMMER_PASSIVE) {
                HasCustomMove.insertIntoNexts(engine, engine.nextPieceCount + engine.ruleopt.nextDisplay, Piece.PIECE_I);
                for (Block blk : HasCustomMove.getNextObject(engine, engine.nextPieceCount + engine.ruleopt.nextDisplay).block) {
                    blk.item = -1;
                }
            }

            grading.resetDecayCounter();
        }

        // This needs to be performed before the piece is actually put in place.
        if (gimmickWinMo3 != null && pieceIsLocking(engine, instantlock) && engine.nowPieceObject.block[0].color == Block.BLOCK_COLOR_GEM_CYAN) {
            Gimmicks.Icicles.fragmentPieceAndDrillDown(engine);
        }

        final boolean ovr = HasCustomMove.super.inPostLockProcessing(engine, playerID, instantlock);

        if (gimmickWinMo3 != null) {
            FieldManipulation.updateAllBlockConnections(engine.field);
        }

        if (pieceIsLocking(engine, instantlock) && settings.perk == SeasonPerk.AUTUMN_ACTIVE && queuedFreefall && engine.gameStarted) {
            queuedFreefall = false;

            customState = CustomState.FREEFALL;
            engine.stat = GameEngine.STAT_CUSTOM;
            engine.resetStatc();

            return true;
        }

        lineClearAfterPiece = true;

        return ovr;
    }

    @Override
    public boolean onMove(GameEngine engine, int playerID) {
        if (engine.statc[0] == 0 && !engine.holdDisable && !(settings.perk == SeasonPerk.WINTER_ACTIVE && abilityIsActive(engine))) {
            if (gimmickSprMo2 != null) gimmickSprMo2.update(engine);
            if (gimmickSprMo3 != null) gimmickSprMo3.attemptPlacement(engine);
            if (gimmickRollSpr != null) gimmickRollSpr.update(engine);
        }

        if (engine.statc[0] == 0 && gimmickWinMo3 != null && !engine.lagStop && !engine.holdDisable) {
            gimmickWinMo3.updateField(engine);
        }

        if ((engine.statc[0] == 0) && (!engine.holdDisable) && (!levelUpFlag)) {
            if (engine.statistics.level < nextSectionLevel - 1) {
                engine.statistics.level = Math.min(engine.statistics.level + naturalLevelIncrement, nextSectionLevel - 1);

                if (engine.statistics.level == nextSectionLevel - 1)
                    engine.playSE("levelstop");
            }

            levelUp(engine);
        }

        if (engine.ending == 0 && (engine.statc[0] > 0 || engine.ruleopt.moveFirstFrame)) {
            grading.updatePerformanceDecay();
        }

        if (engine.gameActive && engine.ending == 2) {
            rollStarted = true;
        }

        if (engine.gameActive && !engine.timerActive && !rollStarted) {
            engine.timerActive = true;
        }

        if (engine.statc[0] > 0) {
            levelUpFlag = false;
        }

        if (gimmickAutMo1 != null && engine.statc[0] > gimmickAutMo1.getFallDelay(badges, getGimmickPerkBoost())) {
            if (settings.perk != SeasonPerk.WINTER_ACTIVE || currentAbilityTimer <= 0) {
                engine.speed.gravity = settings.perk == SeasonPerk.WINTER_PASSIVE ? 4 : 5;
            } else {
                engine.speed.gravity = 0;
            }
        } else if (gimmickAutMo1 != null) {
            engine.speed.gravity = 0;
        }

        if (settings.sparkEffect) {
            sparks.addNumber(engine, receiver, playerID, 12);
        }

        return inOnMove(engine, playerID);
    }

    @Override
    public boolean onARE(GameEngine engine, int playerID) {
        if (engine.statc[0] == 0 && !engine.holdDisable && !levelUpFlag && gimmickSprMo3 != null && !(settings.perk == SeasonPerk.WINTER_ACTIVE && abilityIsActive(engine))) {
            gimmickSprMo3.explode(engine);

            if (getCompleteLinesWithoutFlagging(engine) > 0) {
                engine.stat = GameEngine.STAT_LINECLEAR;
                lineClearAfterPiece = false;

                engine.resetStatc();
                return true;
            }
        }

        if (((engine.statc[0] >= engine.statc[1] - 1) && (!levelUpFlag))) {
            if (engine.statistics.level < nextSectionLevel - 1) {
                engine.statistics.level = Math.min(engine.statistics.level + naturalLevelIncrement, nextSectionLevel - 1);

                if (engine.statistics.level == nextSectionLevel - 1)
                    engine.playSE("levelstop");
            }

            levelUp(engine);
            levelUpFlag = true;
        }

        return false;
    }

    private static final int GRADE_TIME_OFFSET = 300;
    private static final int GRADE_BAR_TIME_MIN = 300;
    private static final int GRADE_BAR_TIME_MAX = 1200;
    private int selectedGradeBarTime;
    private boolean fireworksLaunched;

    private GameTextUtilities.TextBlock gradeName;
    private int nextRankLevel;
    private int nextTitleLevel;
    private int previousPoints;
    private int currentPoints;
    private static final int[] GBF = { 255, 255, 0 };
    private static final int[] GBB = { 0, 0, 0 };

    @Override
    public boolean shouldAdvanceGameOver(GameEngine engine, int playerID) {
        if (engine.statc[8] <= 180) return false;

        if (engine.ctrl.isPress(Controller.BUTTON_A) && engine.statc[9] < selectedGradeBarTime) {
            engine.statc[9] = Math.min(selectedGradeBarTime, engine.statc[9] + 60);
        } else if (engine.ctrl.isPush(Controller.BUTTON_A) && engine.statc[9] >= selectedGradeBarTime) {
            engine.statc[9] = selectedGradeBarTime + GRADE_TIME_OFFSET + 1;
        }

        if (areFireworksWaiting()) {
            engine.statc[9] = Math.min(engine.statc[9], selectedGradeBarTime);
        }

        return engine.statc[9] > (selectedGradeBarTime + GRADE_TIME_OFFSET);
    }

    @Override
    public void processPostGameOver(GameEngine engine) {
        engine.statc[0]++;
        engine.statc[8]++;

        if (engine.statc[8] <= 180) return;

        previousPoints = currentPoints;
        currentPoints = (int) Math.round(
            Interpolation.tanStep(
                0, totalGrades.totalGradePoints,
                MathHelper.clamp(engine.statc[9] / (double) selectedGradeBarTime, 0d, 1d)
            )
        );

        if (currentPoints > previousPoints) {
            engine.playSE("change");
        }

        final Pair<GameTextUtilities.TextBlock, IntPair> rankDisplay = TotalGrades.GRADE_NAMES.apply(currentPoints);

        if (nextTitleLevel < rankDisplay.valR.valR) {
            if (currentPoints == Grading.MAX_GRADE_POINTS) {
                engine.playSE("cool");
            } else {
                engine.playSE("medal");
            }
        } else if (nextRankLevel < rankDisplay.valR.valL) {
            if (rankDisplay.valL.get(2).getString().contains("1ST")) {
                engine.playSE("combo4");
            } else if (rankDisplay.valL.get(2).getString().contains("2ND")) {
                engine.playSE("combo3");
            } else if (rankDisplay.valL.get(2).getString().contains("3RD")) {
                engine.playSE("combo2");
            } else {
                engine.playSE("combo1");
            }
        }

        if (engine.statc[9] == selectedGradeBarTime && !fireworksLaunched) {
            fireworksLaunched = true;

            addFireworksLeft(currentPoints / 250);
            if (currentPoints == Grading.MAX_GRADE_POINTS) addFireworksLeft(30);
        }

        gradeName = rankDisplay.valL;
        nextRankLevel = rankDisplay.valR.valL;
        nextTitleLevel = rankDisplay.valR.valR;

        engine.statc[9]++;
    }

    @Override
    public boolean inGameOver(GameEngine engine, int playerID) {
        if (engine.lives <= 0) {
            inCustomAllLivesLost(engine, playerID);
        } else {
            inLifeLostAnimation(engine);
        }

        return true;
    }

    @Override
    public boolean onGameOver(GameEngine engine, int playerID) {
        if (settings.perk == SeasonPerk.WINTER_ACTIVE && engine.gameActive && abilityIsActive(engine) && waFrozenRows > 0) {
            currentAbilityTimer = 1;

            final NextAndFieldState state = statesAtTimes.lowerEntry(engine.statistics.time).getValue();

            engine.field = state.field;
            engine.nextPieceCount = state.nextPosition;
            engine.holdPieceObject = state.holdPiece;

            engine.stat = GameEngine.STAT_LINECLEAR;

            lineClearAfterPiece = false;
            engine.tspin = false;
            engine.tspinmini = false;
            engine.tspinez = false;

            waCurrentFreezeRow = engine.field.getHeightWithoutHurryupFloor() - 1;
            waFrozenRows = 0;

            engine.resetStatc();

            return true;
        }

        // Set the level back to max level if roll reached.
        if (engine.statc[0] == 0 && rollStarted && rollLevelReached < 0) {
            rollLevelReached = engine.statistics.level;
            engine.statistics.level = MAX_LEVEL;
        }

        if (engine.statc[0] == 0) {
            previousPoints = 0;
            currentPoints = 0;

            fireworksLaunched = false;

            totalGrades = grading.freeze(engine.statistics.level, rollLevelReached, badges, settings.perk);
            log.info(
                String.format(
                    "--- GRADING INFO ---\nGrade: %d\nPerf.: %d\nBadge: %d\nLevel: %d\nR.Lv.: %d\nClear: %d\nP.Ls.: %d",
                    totalGrades.totalGradePoints,
                    totalGrades.totalPerformancePoints,
                    totalGrades.totalBadgePoints,
                    totalGrades.totalLevelPoints,
                    totalGrades.totalRollLevelPoints,
                    totalGrades.allClearBonus,
                    totalGrades.perklessBonus
                )
            );

            engine.statistics.score = totalGrades.totalGradePoints;

            selectedGradeBarTime = Interpolation.lerp(GRADE_BAR_TIME_MIN, GRADE_BAR_TIME_MAX, totalGrades.totalGradePoints / (double) Grading.MAX_GRADE_POINTS);

            final Pair<GameTextUtilities.TextBlock, IntPair> rankDisplay = TotalGrades.GRADE_NAMES.apply(0);
            gradeName = rankDisplay.valL;
            nextRankLevel = rankDisplay.valR.valL;
            nextTitleLevel = rankDisplay.valR.valR;
        }

        return inGameOver(engine, playerID);
    }

    private void renderGrading(GameEngine engine, int baseX, int baseY) {
        if (engine.statc[8] <= 180) return;

        final int textColour = engine.statc[8] % 2 == 0 ? EventReceiver.COLOR_YELLOW : EventReceiver.COLOR_WHITE;

        // Title and Rank
        GameTextUtilities.drawAlignedTextBlock(
            engine,
            baseX + (engine.field.getWidth() * 8),
            baseY,
            false,
            gradeName,
            ObjectAlignment.TOP_MIDDLE
        );

        // Grade Bar
        rendererExtension.drawAlignedSpeedMeter(
            receiver,
            baseX + 2, baseY + 56,
            ObjectAlignment.MIDDLE_LEFT,
            currentPoints >= Grading.MAX_GRADE_POINTS ? 1f : (currentPoints % 100) / 100f,
            (engine.field.getWidth() * 16 - 4) / 42f,
            2f,
            GBB, GBF
        );

        // Left and right number text
        if (currentPoints < Grading.MAX_GRADE_POINTS) {
            GameTextUtilities.drawAlignedText(
                engine,
                baseX + 2,
                baseY + 60,
                GameTextUtilities.Text.ofSmall(String.valueOf(currentPoints - currentPoints % 100), textColour),
                ObjectAlignment.TOP_MIDDLE
            );

            GameTextUtilities.drawAlignedText(
                engine,
                baseX + (engine.field.getWidth() * 16) - 2,
                baseY + 60,
                GameTextUtilities.Text.ofSmall(String.valueOf(Math.min(nextRankLevel, Grading.MAX_GRADE_POINTS)), textColour),
                ObjectAlignment.TOP_MIDDLE
            );
        }

        int gradePointColour = EventReceiver.COLOR_WHITE;
        if (currentPoints >= Grading.MAX_GRADE_POINTS) gradePointColour = EventReceiver.COLOR_YELLOW;
        else if (currentPoints >= Grading.MAX_GRADE_POINTS - 100 && totalGrades.allClearBonus == 0) gradePointColour = EventReceiver.COLOR_RED;

        GameTextUtilities.drawAlignedText(
            engine,
            baseX + (engine.field.getWidth() * 8),
            baseY + 60,
            GameTextUtilities.Text.custom(
                String.valueOf(currentPoints),
                gradePointColour,
                Interpolation.lerp(0.5f, 1.25f, currentPoints / (double) Grading.MAX_GRADE_POINTS)
            ),
            ObjectAlignment.TOP_MIDDLE
        );

        if (engine.statc[9] < 60) return;

        GameTextUtilities.drawAlignedText(
            engine,
            baseX + 4,
            baseY + 96,
            GameTextUtilities.Text.of("PERF.", EventReceiver.COLOR_YELLOW),
            ObjectAlignment.TOP_LEFT
        );

        GameTextUtilities.drawAlignedTextBlock(
            engine,
            baseX + (engine.field.getWidth() * 16) - 4,
            baseY + 96,
            false,
            GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.ofSmall(String.valueOf(totalGrades.totalPerformancePoints), textColour),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.ofSmall("/" + Grading.MAX_PERFORMANCE_POINTS, textColour)
            ),
            ObjectAlignment.TOP_RIGHT
        );

        if (engine.statc[9] < 120) return;

        GameTextUtilities.drawAlignedText(
            engine,
            baseX + 4,
            baseY + 112,
            GameTextUtilities.Text.of("BADGE", EventReceiver.COLOR_YELLOW),
            ObjectAlignment.TOP_LEFT
        );

        GameTextUtilities.drawAlignedTextBlock(
            engine,
            baseX + (engine.field.getWidth() * 16) - 4,
            baseY + 112,
            false,
            GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.ofSmall(String.valueOf(totalGrades.totalBadgePoints), textColour),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.ofSmall("/" + Grading.MAX_BADGE_POINTS, textColour)
            ),
            ObjectAlignment.TOP_RIGHT
        );

        if (engine.statc[9] < 180) return;

        GameTextUtilities.drawAlignedText(
            engine,
            baseX + 4,
            baseY + 128,
            GameTextUtilities.Text.of("LEVEL", EventReceiver.COLOR_YELLOW),
            ObjectAlignment.TOP_LEFT
        );

        GameTextUtilities.drawAlignedTextBlock(
            engine,
            baseX + (engine.field.getWidth() * 16) - 4,
            baseY + 128,
            false,
            GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.ofSmall(String.valueOf(totalGrades.totalLevelPoints), textColour),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.ofSmall("/" + Grading.MAX_LEVEL_POINTS, textColour)
            ),
            ObjectAlignment.TOP_RIGHT
        );

        if (engine.statc[9] < 240) return;

        GameTextUtilities.drawAlignedText(
            engine,
            baseX + 4,
            baseY + 144,
            GameTextUtilities.Text.of("ROLL", EventReceiver.COLOR_BLUE),
            ObjectAlignment.TOP_LEFT
        );

        GameTextUtilities.drawAlignedTextBlock(
            engine,
            baseX + (engine.field.getWidth() * 16) - 4,
            baseY + 144,
            false,
            GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.ofSmall(String.valueOf(totalGrades.totalRollLevelPoints), textColour),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.ofSmall("/" + Grading.MAX_ROLL_LEVEL_POINTS, textColour)
            ),
            ObjectAlignment.TOP_RIGHT
        );

        if (engine.statc[9] < 300) return;

        GameTextUtilities.drawAlignedText(
            engine,
            baseX + 4,
            baseY + 160,
            GameTextUtilities.Text.of("CLEAR", EventReceiver.COLOR_GREEN),
            ObjectAlignment.TOP_LEFT
        );

        GameTextUtilities.drawAlignedText(
            engine,
            baseX + (engine.field.getWidth() * 16) - 4,
            baseY + 164,
            GameTextUtilities.Text.ofSmall(totalGrades.allClearBonus > 0 ? "+" + totalGrades.allClearBonus : "N/A", textColour),
            ObjectAlignment.TOP_RIGHT
        );

        if (engine.statc[9] < 360 || settings.perk != SeasonPerk.PERKLESS) return;

        GameTextUtilities.drawAlignedText(
            engine,
            baseX + 4,
            baseY + 176,
            GameTextUtilities.Text.of("P.LESS", EventReceiver.COLOR_ORANGE),
            ObjectAlignment.TOP_LEFT
        );

        GameTextUtilities.drawAlignedText(
            engine,
            baseX + (engine.field.getWidth() * 16) - 4,
            baseY + 180,
            GameTextUtilities.Text.ofSmall("+" + totalGrades.perklessBonus, textColour),
            ObjectAlignment.TOP_RIGHT
        );
    }

    @Override
    public void renderGameOver(GameEngine engine, int playerID) {
        int baseX = receiver.getFieldDisplayPositionX(engine, playerID) + 4;
        int baseY = receiver.getFieldDisplayPositionY(engine, playerID) + 52;

        if (engine.statc[0] > engine.field.getHeight()) {
            GameTextUtilities.drawAlignedText(
                engine,
                baseX + (engine.field.getWidth() * 8),
                engine.statc[8] <= 135 ? baseY + 152 : baseY + (int) Interpolation.tanStep(152, 64, MathHelper.clamp((engine.statc[8] - 135) / 45d, 0d, 1d)),
                GameTextUtilities.Text.of("GAME OVER"),
                ObjectAlignment.TOP_MIDDLE
            );

            // Render the grading system.
            renderGrading(engine, baseX, baseY + 96);
        }
    }

    private void addEventText(GameEngine engine, int playerID, int lines, int combo) {
        final int baseX = receiver.getFieldDisplayPositionX(engine, playerID) + 4;
        final int baseY = receiver.getFieldDisplayPositionX(engine, playerID) + 52;

        final DoubleVector basePosition;
        final boolean reverse;

        if (lineClearAfterPiece) {
            final int pieceX = (16 * engine.nowPieceX) + baseX;
            final int pieceY = (16 * engine.nowPieceY) + baseY;

            int pieceXs = 0;
            int pieceYs = 0;

            for (int i = 0; i < engine.nowPieceObject.getMaxBlock(); ++i) {
                pieceXs += pieceX + (engine.nowPieceObject.dataX[engine.nowPieceObject.direction][i] * 16);
                pieceYs += pieceY + (engine.nowPieceObject.dataY[engine.nowPieceObject.direction][i] * 16) + 8;
            }

            pieceXs /= engine.nowPieceObject.getMaxBlock();
            pieceYs /= engine.nowPieceObject.getMaxBlock();

            final double upX = pieceXs < (baseX + (engine.field.getWidth() * 8))
                ? (baseX + ((engine.nowPieceX + engine.nowPieceObject.getMaximumBlockX()) * 16))
                : (baseX + ((engine.nowPieceX + engine.nowPieceObject.getMinimumBlockX()) * 16));

            basePosition = new DoubleVector(upX, pieceYs, false);
            reverse = pieceXs < (receiver.getFieldDisplayPositionX(engine, playerID) + 4 + (8 * engine.field.getWidth()));
        } else {
            basePosition = new DoubleVector(baseX + (engine.field.getWidth() * 8), baseY + (engine.field.getHeight() * 4), false);
            reverse = false;
        }

        final String pieceName = (engine.nowPieceObject != null && lineClearAfterPiece) ? Piece.getPieceName(engine.nowPieceObject.id) : "";
        final DoubleVector baseVelocity = new DoubleVector(0.0, -8, false);
        final DoubleVector baseAcceleration = new DoubleVector(0.0, 9.80665 / 15.0, false);
        final float startScale = 1f;
        final float endScale = 2f;
        final int maxLife = 60;
        final int lifeOffset = 2;
        final int colour = 0x00FFFFFF;

        final StringBuilder sb = new StringBuilder();

        if (engine.field.isEmpty()) {
            textEmitter.addString(
                "BRAVO!",
                new DoubleVector(
                    baseX + (8d * engine.field.getWidth()) + 8d,
                    baseY + 16d,
                    false
                ), baseVelocity, baseAcceleration,
                ObjectAlignment.MIDDLE_MIDDLE,
                0, lifeOffset, maxLife,
                EventReceiver.COLOR_YELLOW,
                startScale, endScale * 1.5f,
                colour, 255,
                colour, 255,
                reverse
            );
        }

        if (lines == 0) {
            if (engine.tspinez) {
                sb.append("EZ-").append(pieceName);
                textEmitter.addString(
                    sb.toString(),
                    basePosition, baseVelocity, baseAcceleration,
                    ObjectAlignment.MIDDLE_MIDDLE,
                    0, lifeOffset, maxLife,
                    EventReceiver.COLOR_GREEN,
                    startScale, endScale,
                    colour, 255,
                    colour, 255,
                    reverse
                );
            } else if (engine.tspinmini) {
                sb.append(pieceName).append("-MINI");
                textEmitter.addString(
                    sb.toString(),
                    basePosition, baseVelocity, baseAcceleration,
                    ObjectAlignment.MIDDLE_MIDDLE,
                    0, lifeOffset, maxLife,
                    EventReceiver.COLOR_GREEN,
                    startScale, endScale,
                    colour, 255,
                    colour, 255,
                    reverse
                );
            } else if (engine.tspin) {
                sb.append(pieceName).append("-SPIN");
                textEmitter.addString(
                    sb.toString(),
                    basePosition, baseVelocity, baseAcceleration,
                    ObjectAlignment.MIDDLE_MIDDLE,
                    0, lifeOffset, maxLife,
                    EventReceiver.COLOR_PURPLE,
                    startScale, endScale,
                    colour, 255,
                    colour, 255,
                    reverse
                );
            }
        } else {
            String lineName = "SINGLE";
            if (lines == 2) lineName = "DOUBLE";
            else if (lines == 3) lineName = "TRIPLE";
            else if (lines == 4) lineName = "FOUR";
            else if (lines > 4) lineName = MiscUtils.Numerics.nameOfNumber(BigInteger.valueOf(lines));

            if (engine.tspinez) {
                sb.append("EZ-").append(pieceName).append("-").append(lineName);
                textEmitter.addString(
                    sb.toString(),
                    basePosition, baseVelocity, baseAcceleration,
                    ObjectAlignment.MIDDLE_MIDDLE,
                    0, lifeOffset, maxLife,
                    EventReceiver.COLOR_ORANGE,
                    startScale, endScale,
                    colour, 255,
                    colour, 255,
                    reverse
                );
            } else if (engine.tspinmini) {
                sb.append(pieceName).append("-").append(lineName).append("-M");
                textEmitter.addString(
                    sb.toString(),
                    basePosition, baseVelocity, baseAcceleration,
                    ObjectAlignment.MIDDLE_MIDDLE,
                    0, lifeOffset, maxLife,
                    EventReceiver.COLOR_ORANGE,
                    startScale, endScale,
                    colour, 255,
                    colour, 255,
                    reverse
                );
            } else if (engine.tspin) {
                sb.append(pieceName).append("-").append(lineName);
                textEmitter.addString(
                    sb.toString(),
                    basePosition, baseVelocity, baseAcceleration,
                    ObjectAlignment.MIDDLE_MIDDLE,
                    0, lifeOffset, maxLife,
                    EventReceiver.COLOR_PINK,
                    startScale, endScale,
                    colour, 255,
                    colour, 255,
                    reverse
                );
            } else {
                sb.append(lineName);
                textEmitter.addString(
                    lineName,
                    basePosition, baseVelocity, baseAcceleration,
                    ObjectAlignment.MIDDLE_MIDDLE,
                    0, lifeOffset, maxLife,
                    lines >= 4 ? EventReceiver.COLOR_YELLOW : EventReceiver.COLOR_WHITE,
                    startScale, endScale,
                    colour, 255,
                    colour, 255,
                    reverse
                );
            }
        }

        if (engine.b2b && engine.b2bcount > 1 && lines > 0) {
            final int b2bc = engine.b2bcount == 2 ? EventReceiver.COLOR_ORANGE : EventReceiver.COLOR_RED;

            textEmitter.addString(
                "B2B",
                DoubleVector.sub(basePosition, new DoubleVector(0, 24, false)),
                baseVelocity,
                baseAcceleration,
                ObjectAlignment.MIDDLE_MIDDLE,
                -1 * ((lifeOffset * (sb.length() - 3)) / 2), lifeOffset, maxLife,
                b2bc,
                startScale, endScale,
                colour, 255,
                colour, 255,
                reverse
            );
        }

        if (combo >= 2 && lines > 0) {
            final int startColour = GLOBAL_MIXER
                .setRGB24(colour)
                .setBlue(1.0 - Math.min(1.0, combo / 11d))
                .getRGB24();

            final String str = (combo - 1) + " COMBO";

            textEmitter.addString(
                str,
                DoubleVector.add(basePosition, new DoubleVector(0, 32, false)),
                baseVelocity,
                baseAcceleration,
                ObjectAlignment.MIDDLE_MIDDLE,
                ((lifeOffset * (str.length() - 3)) / 2), lifeOffset, maxLife,
                EventReceiver.COLOR_WHITE,
                startScale, endScale,
                startColour, 255,
                colour, 255,
                reverse
            );
        }
    }

    private int abilityCharge(GameEngine engine, int baseCharge) {
        if (currentAbilityTimer > 0) return 0;
        else return engine.tspin ? 5 * baseCharge : baseCharge;
    }

    @Override
    public void afterSoftDropFall(GameEngine engine, int playerID, int fall) {
        animatedBackgrounds[getLastBackground()].updateDrop(engine, fall);
        animatedBackgrounds[getCurrentBackground()].updateDrop(engine, fall);
    }

    @Override
    public void afterHardDropFall(GameEngine engine, int playerID, int fall) {
        if (settings.landingEffect) {
            landingParticles.addNumber(receiver, engine, playerID, 32);
        }

        animatedBackgrounds[getLastBackground()].updateDrop(engine, fall * 2);
        animatedBackgrounds[getCurrentBackground()].updateDrop(engine, fall * 2);
    }

    private static final Map<IntPair, Block> wm3HardBlocksClearing = new HashMap<>(40);

    @Override
    public void calcScore(GameEngine engine, int playerID, int lines) {
        addEventText(engine, playerID, lines, engine.combo);

        if (lines >= 1) {
            if (engine.field.isEmpty()) {
                engine.playSE("bravo");
            }

            int levelIncrease = 0;
            final int oldEnergy = currentEnergy;

            // region Lines
            if (lines > 4) {
                levelIncrease += (lines * 2) * naturalLevelIncrement;

                if (settings.perk.isActive()) {
                    currentEnergy = Math.min(
                        settings.perk.energyStore,
                        currentEnergy + (int) (abilityCharge(engine, settings.perk.restoredForFour) * (lines / 4d))
                    );

                    if (engine.b2bcount > 1 && !engine.tspin) currentEnergy = Math.min(settings.perk.energyStore, currentEnergy + ((currentAbilityTimer > 0) ? 0 : settings.perk.restoredForSingle));
                }
            } else if (lines == 4) {
                levelIncrease += 8 * naturalLevelIncrement;

                if (settings.perk.isActive()) {
                    currentEnergy = Math.min(settings.perk.energyStore, currentEnergy + abilityCharge(engine, settings.perk.restoredForFour));
                }
            } else if (lines == 3) {
                levelIncrease += 5 * naturalLevelIncrement;

                if (settings.perk.isActive()) {
                    currentEnergy = Math.min(settings.perk.energyStore, currentEnergy + abilityCharge(engine, settings.perk.restoredForTriple));
                }
            } else if (lines == 2) {
                levelIncrease += 2 * naturalLevelIncrement;

                if (settings.perk.isActive()) {
                    currentEnergy = Math.min(settings.perk.energyStore, currentEnergy + abilityCharge(engine, settings.perk.restoredForDouble));
                }
            } else {
                levelIncrease += naturalLevelIncrement;

                if (settings.perk.isActive()) {
                    currentEnergy = Math.min(settings.perk.energyStore, currentEnergy + abilityCharge(engine, settings.perk.restoredForSingle));
                }
            }
            // endregion Lines

            if (engine.tspin) {
                if (engine.b2bcount > 1) currentEnergy = Math.min(settings.perk.energyStore, currentEnergy + ((currentAbilityTimer > 0) ? 0 : settings.perk.restoredForSingle));
            }

            if (settings.perk.isActive() && oldEnergy < settings.perk.energyStore && currentEnergy >= settings.perk.energyStore) {
                engine.playSE("cool");
            }

            levelIncrease += badges.getLevelBonus() * naturalLevelIncrement;

            if (settings.perk == SeasonPerk.SPRING_PASSIVE) {
                engine.statistics.level += levelIncrease + naturalLevelIncrement;
            } else if (settings.perk == SeasonPerk.SPRING_ACTIVE && currentAbilityTimer > 0) {
                engine.statistics.level += levelIncrease * 2;
            } else {
                engine.statistics.level += levelIncrease;
            }

            if (engine.statistics.level > MAX_LEVEL) engine.statistics.level = MAX_LEVEL;

            levelUp(engine);
            badges.updateBadges(
                engine, lines,
                settings.perk == SeasonPerk.SPRING_PASSIVE,
                settings.perk == SeasonPerk.SPRING_ACTIVE && currentAbilityTimer > 0
            );

            if (engine.ending == 0) {
                final int performance = performanceForLines(engine, lines);
                grading.addPerformancePoints(performance);
            } else {
                // Add a small amount of bonus time for line clears.
                if (lines >= 4) {
                    rollTime += 5 * lines;
                } else if (lines == 3) {
                    rollTime += 9;
                } else {
                    rollTime += 2 * lines;
                }
            }

            if (engine.statistics.level >= MAX_LEVEL && !rollStarted) {
                engine.playSE("endingstart");

                nextSectionLevel = LEVELS_FEB;
                engine.ending = 2;

                owner.bgmStatus.fadesw = false;
                owner.bgmStatus.bgm = BGMStatus.BGM_NORMAL5;

                setNewBackground(0);
                fieldPurifyQueued = true;

                clearBaseGameGimmicks();

                rollTime = (150 * 60) + badges.getBadges() + (badges.getBadges() >>> 2); // 2.5 minutes + 1 frame per 0.1 badges (and extra frame per 4 badges).

                customState = CustomState.FINAL_REWIND;
                engine.stat = GameEngine.STAT_CUSTOM;
                engine.resetStatc();
            } else if (engine.statistics.level >= MAX_LEVEL) {
                engine.resetFieldVisible();
                clearRollGimmicks();

                owner.bgmStatus.fadesw = false;

                engine.ending = 1;
                engine.gameEnded();
            } else if (engine.statistics.level >= nextSectionLevel && rollStarted) {
                engine.playSE("levelup");

                final Season oldSeason = currentSeason;
                currentSeason = SEASON_TABLE.apply(engine.statistics.level);

                if (currentSeason != oldSeason) {
                    timeSpentInSeason = 0;
                    activeTimeSpentInSeason = 0;

                    switch (currentSeason) {
                        case SUMMER:
                            gimmickRollSpr = null;

                            gimmickRollSum = new Gimmicks.Conflagration(LEVELS_APR, LEVELS_JUL);
                            gimmickRollSum.getSpeed(engine, badges, getGimmickPerkBoost());

                            descriptionToDraw = new DescriptionDraw(gimmickRollSum);
                            break;
                        case AUTUMN:
                            gimmickRollSum = null;

                            gimmickRollAut = new Gimmicks.Haunting(new Random(engine.randSeed - 31), badges, getGimmickPerkBoost());
                            descriptionToDraw = new DescriptionDraw(gimmickRollAut);

                            engine.bone = true;
                            engine.blockShowOutlineOnly = true;
                            break;
                        case WINTER:
                            gimmickRollAut = null;

                            engine.bone = false;
                            engine.blockShowOutlineOnly = false;
                            engine.resetFieldVisible();

                            gimmickRollWin = new Gimmicks.AbsoluteZero(badges, getGimmickPerkBoost());
                            descriptionToDraw = new DescriptionDraw(gimmickRollWin);
                            break;
                        default:
                            clearRollGimmicks();
                    }

                    purifyFieldAndNexts(engine);
                }

                if (NEXT_SECTION_LEVELS.apply(engine.statistics.level) > nextSectionLevel) {
                    nextSectionLevel = NEXT_SECTION_LEVELS.apply(engine.statistics.level);
                }

                setNewBackground(BACKGROUND_TABLE.apply(engine.statistics.level));
            } else if (engine.statistics.level >= nextSectionLevel) {
                engine.playSE("levelup");

                setNewBackground(BACKGROUND_TABLE.apply(engine.statistics.level));

                if (engine.statistics.level >= LEVELS_FEB && engine.statistics.level < LEVELS_MAR) {
                    gimmickSprMo2 = new Gimmicks.Sproutlings(badges, getGimmickPerkBoost());
                    descriptionToDraw = new DescriptionDraw(gimmickSprMo2);
                } else if (engine.statistics.level >= LEVELS_MAR && engine.statistics.level < LEVELS_APR) {
                    gimmickSprMo3 = new Gimmicks.FlourishingBlooms(new Random(engine.randSeed + 2), badges, getGimmickPerkBoost());
                    descriptionToDraw = new DescriptionDraw(gimmickSprMo3);
                } else if (engine.statistics.level >= LEVELS_APR && engine.statistics.level < LEVELS_MAY) {
                    gimmickSprMo2 = null;
                    gimmickSprMo3 = null;

                    gimmickSumMo1 = new Gimmicks.Dehydration(new Random(engine.randSeed + 3), LEVELS_APR, LEVELS_JUL);
                    descriptionToDraw = new DescriptionDraw(gimmickSumMo1);
                } else if (engine.statistics.level >= LEVELS_MAY && engine.statistics.level < LEVELS_JUN) {
                    gimmickSumMo2 = new Gimmicks.Mirage(badges, getGimmickPerkBoost());
                    descriptionToDraw = new DescriptionDraw(gimmickSumMo2);
                } else if (engine.statistics.level >= LEVELS_JUN && engine.statistics.level < LEVELS_JUL) {
                    gimmickSumMo3 = new Gimmicks.IntoTheFire(LEVELS_JUN, LEVELS_JUL);
                    gimmickSumMo3.getSpeed(engine, badges, getGimmickPerkBoost()); // Initialisation. Do not actually feed it into the speed yet.

                    descriptionToDraw = new DescriptionDraw(gimmickSumMo3);
                } else if (engine.statistics.level >= LEVELS_JUL && engine.statistics.level < LEVELS_AUG) {
                    gimmickSumMo1 = null;
                    gimmickSumMo2 = null;
                    gimmickSumMo3 = null;

                    gimmickAutMo1 = new Gimmicks.FallsCall();
                    descriptionToDraw = new DescriptionDraw(gimmickAutMo1);
                } else if (engine.statistics.level >= LEVELS_AUG && engine.statistics.level < LEVELS_SEP) {
                    gimmickAutMo2 = new Gimmicks.FlowingWinds(new Random(engine.randSeed + 4), LEVELS_AUG, LEVELS_OCT);
                    descriptionToDraw = new DescriptionDraw(gimmickAutMo2);
                } else if (engine.statistics.level >= LEVELS_SEP && engine.statistics.level < LEVELS_OCT) {
                    gimmickAutMo3 = new Gimmicks.GhoulsAfoot(badges, getGimmickPerkBoost());
                    descriptionToDraw = new DescriptionDraw(gimmickAutMo3);
                } else if (engine.statistics.level >= LEVELS_OCT && engine.statistics.level < LEVELS_NOV) {
                    gimmickAutMo1 = null;
                    gimmickAutMo2 = null;
                    gimmickAutMo3 = null;

                    gimmickWinMo1 = new Gimmicks.Whiteout(badges, getGimmickPerkBoost());
                    descriptionToDraw = new DescriptionDraw(gimmickWinMo1);
                } else if (engine.statistics.level >= LEVELS_NOV && engine.statistics.level < LEVELS_DEC) {
                    gimmickWinMo2 = new Gimmicks.SnowMounds(badges, getGimmickPerkBoost());
                    descriptionToDraw = new DescriptionDraw(gimmickWinMo2);
                } else if (engine.statistics.level >= LEVELS_DEC && engine.statistics.level < LEVELS_JAN) {
                    gimmickWinMo3 = new Gimmicks.Icicles(new Random(engine.randSeed * 2738), LEVELS_DEC, LEVELS_JAN);
                    gimmickWinMo3.updateChance(engine, badges, getGimmickPerkBoost());
                    descriptionToDraw = new DescriptionDraw(gimmickWinMo3);
                }

                final Season oldSeason = currentSeason;
                currentSeason = SEASON_TABLE.apply(engine.statistics.level);

                if (currentSeason != oldSeason) {
                    timeSpentInSeason = 0;
                    activeTimeSpentInSeason = 0;
                    fieldPurifyQueued = true;
                }

                if (owner.bgmStatus.bgm <= BGM_TABLE.apply(engine.statistics.level)) {
                    owner.bgmStatus.fadesw = false;
                    owner.bgmStatus.bgm = BGM_TABLE.apply(engine.statistics.level);

                    if (NEXT_SECTION_LEVELS.apply(engine.statistics.level) > nextSectionLevel) {
                        nextSectionLevel = NEXT_SECTION_LEVELS.apply(engine.statistics.level);
                    }
                }

                customState = CustomState.REWIND;
                engine.stat = GameEngine.STAT_CUSTOM;
                engine.resetStatc();
            } else if (engine.statistics.level == nextSectionLevel - 1) {
                engine.playSE("levelstop");
            }
        }
    }

    private int performanceForLines(GameEngine engine, int lines) {
        int performance = 2;
        if (lines == 2) performance = 5;
        else if (lines == 3) performance = 10;
        else if (lines >= 4) performance = 5 * lines;

        if (engine.tspin && engine.tspinez) {
            performance += performance >>> 2;
        } else if (engine.tspin && engine.tspinmini) {
            performance += performance >>> 1;
        } else if (engine.tspin) {
            performance *= 2;
        }

        if (engine.b2b) {
            performance += 5;
        }

        performance *= currentSeason.performanceMultiplier;
        return performance;
    }

    @Override
    public void blockBreak(GameEngine engine, int playerID, int x, int y, Block blk) {
        // Add 0.1 badge per sproutling gem cleared.
        if ((gimmickSprMo2 != null && blk.color == Block.BLOCK_COLOR_GEM_GREEN) || (gimmickRollSpr != null && blk.color == Block.BLOCK_COLOR_GEM_ORANGE)) {
            ++brokenMinorBonusGems;
        }

        if (gimmickSprMo3 != null) {
            if (blk.color == Block.BLOCK_COLOR_GEM_ORANGE) {
                badges.addSeasonBadges(
                    1,
                    settings.perk == SeasonPerk.SPRING_PASSIVE,
                    settings.perk == SeasonPerk.SPRING_ACTIVE && currentAbilityTimer > 0
                );
            } else if (blk.color == Block.BLOCK_COLOR_GEM_PURPLE || blk.color == Block.BLOCK_COLOR_GEM_YELLOW) {
                ++brokenFlorets;
            }
        }

        if (gimmickSumMo1 != null && blk.getAttribute(Block.BLOCK_ATTRIBUTE_BONE)) {
            ++brokenBoneBlocks;
        }

        if (gimmickAutMo2 != null && blk.color == Block.BLOCK_COLOR_GEM_RAINBOW) {
            ++brokenMajorBonusGems;
        }

        if (gimmickWinMo1 != null && (blk.bonusValue & Gimmicks.Whiteout.SNOW_MASK) == Gimmicks.Whiteout.SNOW_IDENTIFIER) {
            ++brokenSnowBlocks;
        }

        if (gimmickWinMo2 != null && gimmickWinMo2.isYInSnow(engine, y)) {
            ++blocksUnderSnow;
        }

        if ((gimmickWinMo3 != null || gimmickRollWin != null) && blk.hard > 0) {
            ++hardBlocksSeen;
        }

        if (gimmickWinMo3 != null && blk.hard > 0) {
            wm3HardBlocksClearing.put(IntPair.of(x, y), blk);

            // Only clear the line if the row is full of ice blocks.
            if (x == engine.field.getWidth() - 1) {
                final List<Map.Entry<IntPair, Block>> hardInRow = wm3HardBlocksClearing
                    .entrySet()
                    .stream()
                    .filter(e -> e.getKey().valR == y)
                    .collect(Collectors.toList());

                if (hardInRow.size() == engine.field.getWidth()) {
                    hardInRow.forEach(e -> e.getValue().hard = 0);
                }
            }
        }
    }

    @Override
    public void callCalcScore(GameEngine engine, int playerID, int li) {
        HasCustomLineClear.super.callCalcScore(engine, playerID, li);

        // Update season badges based on lines.
        if (gimmickSumMo2 != null) {
            if (engine.nowPieceObject != null && engine.nowPieceObject.id == Piece.PIECE_I && li > 0) {
                badges.addSeasonBadges(
                    li > 2 ? (li + (li >>> 1)) : li,
                    settings.perk == SeasonPerk.SPRING_PASSIVE,
                    settings.perk == SeasonPerk.SPRING_ACTIVE && currentAbilityTimer > 0
                );
            }
        }
        if (gimmickSumMo3 != null || gimmickRollSum != null) {
            if (li > 0) {
                badges.addSeasonBadges(
                    li > 2 ? (li + (li >>> 1)) : li,
                    settings.perk == SeasonPerk.SPRING_PASSIVE,
                    settings.perk == SeasonPerk.SPRING_ACTIVE && currentAbilityTimer > 0
                );
            }
        }
        if (gimmickAutMo1 != null) {
            if (li > 0) {
                final int bonus = engine.speed.gravity == 0 ? (li * 2) : (li > 2 ? (li + (li >>> 1)) : li);
                badges.addSeasonBadges(
                    bonus,
                    settings.perk == SeasonPerk.SPRING_PASSIVE,
                    settings.perk == SeasonPerk.SPRING_ACTIVE && currentAbilityTimer > 0
                );
            }
        }
        if (gimmickAutMo3 != null || gimmickRollAut != null) {
            if (li > 0) {
                badges.addSeasonBadges(
                    li > 2 ? (li + (li >>> 1)) : li,
                    settings.perk == SeasonPerk.SPRING_PASSIVE,
                    settings.perk == SeasonPerk.SPRING_ACTIVE && currentAbilityTimer > 0
                );
            }
        }

        // Update gimmicks, and perform next queue modification here.
        if (gimmickSprMo2 != null) {
            gimmickSprMo2.setCountdown(badges, getGimmickPerkBoost());
        }
        if (gimmickRollSpr != null) {
            gimmickRollSpr.setCountdown(badges, getGimmickPerkBoost());
        }
        if (gimmickSprMo3 != null) {
            gimmickSprMo3.setCountdown(badges, getGimmickPerkBoost());
        }
        if (gimmickSumMo1 != null) {
            gimmickSumMo1.updateChance(engine, badges, getGimmickPerkBoost());
            gimmickSumMo1.updateNext(engine);
            gimmickSumMo1.updateField(engine);
        }
        if (gimmickSumMo2 != null) {
            gimmickSumMo2.setAllowance(badges, getGimmickPerkBoost());
        }
        if (gimmickSumMo3 != null) {
            gimmickSumMo3.getSpeed(engine, badges, getGimmickPerkBoost());
        }
        if (gimmickRollSum != null) {
            gimmickRollSum.getSpeed(engine, badges, getGimmickPerkBoost());
        }
        if (gimmickAutMo1 != null) {
            gimmickAutMo1.getFallDelay(badges, getGimmickPerkBoost());
        }
        if (gimmickAutMo2 != null) {
            gimmickAutMo2.updateChance(engine, badges, getGimmickPerkBoost());
            gimmickAutMo2.updateNext(engine);
        }
        if (gimmickAutMo3 != null) {
            gimmickAutMo3.updateBonusGap(badges, getGimmickPerkBoost());
        }
        if (gimmickRollAut != null) {
            gimmickRollAut.setCurrentRadius(badges, getGimmickPerkBoost());
        }
        if (gimmickWinMo1 != null) {
            gimmickWinMo1.updateProportion(badges, getGimmickPerkBoost());
            gimmickWinMo1.updateNext(engine);
        }
        if (gimmickWinMo2 != null) {
            gimmickWinMo2.setTickTime(badges, getGimmickPerkBoost());
        }
        if (gimmickWinMo3 != null) {
            gimmickWinMo3.updateChance(engine, badges, getGimmickPerkBoost());
            gimmickWinMo3.updateNext(engine);
        }
        if (gimmickRollWin != null) {
            gimmickRollWin.setCountdownMax(badges, getGimmickPerkBoost());
        }
    }

    @Override
    public void inLineClearing(GameEngine engine, int playerID) {
        if(engine.statc[0] == 0) {
            final int li = Math.max(0, flagLinesForClearing(engine) - waFrozenRows);
            engine.lineClearing = li;

            if (settings.perk != SeasonPerk.WINTER_ACTIVE || !abilityIsActive(engine)) {
                processLineClearStatistics(engine, li);

                processB2B(engine, li);

                processCombo(engine, li);

                engine.lineGravityTotalLines += engine.lineClearing;

                processTotalLineStatistics(engine, li);

                if(engine.field.getHowManyGemClears() > 0) engine.playSE("gem");

                callCalcScore(engine, playerID, li);

                callAndDrawBrokenBlocks(engine, playerID, li);

                eraseFlaggedBlocks(engine, li);
            } else if (li > 0) {
                if (engine.clearMode == GameEngine.CLEAR_LINE)
                    engine.playSE("erase" + Math.min(4, li));

                callCalcScore(engine, playerID, 0);

                // Move lines to the bottom.
                boolean moved = false;

                for (int y = waCurrentFreezeRow; y >= -engine.field.getHiddenHeight(); --y) {
                    if (engine.field.getLineFlag(y)) {
                        engine.field.setLineFlag(y, false);
                        FieldManipulation.rotateLinesUp(engine.field, y, waCurrentFreezeRow);

                        --waCurrentFreezeRow;
                        ++waFrozenRows;
                        moved = true;

                        FieldManipulation.updateAllBlockConnections(engine.field);
                    }
                }

                if (moved) {
                    engine.playSE("combo" + Math.min(20, waFrozenRows));
                }

                for (int y = -engine.field.getHiddenHeight(); y < engine.field.getHeightWithoutHurryupFloor(); ++y) {
                    engine.field.setLineFlag(y, false);
                }

                engine.stat = GameEngine.STAT_ARE;

                engine.resetStatc();
                engine.statc[1] = engine.getARELine();
            } else {
                callCalcScore(engine, playerID, 0);

                for (int y = -engine.field.getHiddenHeight(); y < engine.field.getHeightWithoutHurryupFloor(); ++y) {
                    engine.field.setLineFlag(y, false);
                }

                engine.stat = GameEngine.STAT_ARE;

                engine.resetStatc();
                engine.statc[1] = engine.getARE();
            }
        }
    }

    @Override
    public int getCompleteLinesWithoutFlagging(GameEngine engine) {
        final int result = HasCustomMove.super.getCompleteLinesWithoutFlagging(engine) - waFrozenRows;
        engine.lineClearing = Math.max(0, engine.lineClearing - waFrozenRows);

        return result;
    }

    @Override
    public void eraseFlaggedBlocks(GameEngine engine, int li) {
        if (li >= 4 && gimmickRollWin != null) {
            FieldManipulation.pushDown(engine.field);
        }

        HasCustomLineClear.super.eraseFlaggedBlocks(engine, li);
    }

    @Override
    public void callAndDrawBrokenBlocks(GameEngine engine, int playerID, int li) {
        brokenMinorBonusGems = 0;
        brokenBoneBlocks = 0;
        brokenFlorets = 0;
        brokenMajorBonusGems = 0;
        brokenSnowBlocks = 0;
        blocksUnderSnow = 0;
        hardBlocksSeen = 0;
        if (gimmickWinMo3 != null) wm3HardBlocksClearing.clear();

        HasCustomLineClear.super.callAndDrawBrokenBlocks(engine, playerID, li);

        // Janky... lmao. Update season badges based on single blocks.
        if (brokenMinorBonusGems > 0) {
            int bonus = brokenMinorBonusGems + brokenMinorBonusGems / 9;
            if (gimmickSprMo2 != null) {
                bonus += gimmickSprMo2.getScoreMult(badges, getGimmickPerkBoost()) * li;
                bonus >>>= 1;
            }

            badges.addSeasonBadges(
                bonus,
                settings.perk == SeasonPerk.SPRING_PASSIVE,
                settings.perk == SeasonPerk.SPRING_ACTIVE && currentAbilityTimer > 0
            );
        }

        if (brokenFlorets > 0) {
            badges.addSeasonBadges(
                brokenFlorets + (gimmickSprMo3.getScoreMult(badges, getGimmickPerkBoost()) * li),
                settings.perk == SeasonPerk.SPRING_PASSIVE,
                settings.perk == SeasonPerk.SPRING_ACTIVE && currentAbilityTimer > 0
            );
        }

        if (brokenBoneBlocks >= 2) {
            int bonus = brokenBoneBlocks / 2;
            if (bonus > 10) bonus += (bonus >>> 1);

            badges.addSeasonBadges(
                bonus,
                settings.perk == SeasonPerk.SPRING_PASSIVE,
                settings.perk == SeasonPerk.SPRING_ACTIVE && currentAbilityTimer > 0
            );
        }

        if (brokenMajorBonusGems > 0) {
            badges.addSeasonBadges(
                brokenMajorBonusGems * 5,
                settings.perk == SeasonPerk.SPRING_PASSIVE,
                settings.perk == SeasonPerk.SPRING_ACTIVE && currentAbilityTimer > 0
            );
        }

        if (brokenSnowBlocks >= 10) {
            int bonus = brokenSnowBlocks / 10;
            if (bonus > 2) bonus += (bonus >>> 1);

            badges.addSeasonBadges(
                bonus,
                settings.perk == SeasonPerk.SPRING_PASSIVE,
                settings.perk == SeasonPerk.SPRING_ACTIVE && currentAbilityTimer > 0
            );
        }

        if (blocksUnderSnow >= 5) {
            int bonus = blocksUnderSnow / 5;
            if (bonus > 4) bonus += (bonus >>> 1);

            badges.addSeasonBadges(
                bonus,
                settings.perk == SeasonPerk.SPRING_PASSIVE,
                settings.perk == SeasonPerk.SPRING_ACTIVE && currentAbilityTimer > 0
            );
        }

        if (hardBlocksSeen >= 4) {
            int bonus = hardBlocksSeen / 4;
            if (bonus > 5) bonus += (bonus >>> 1);

            badges.addSeasonBadges(
                bonus,
                settings.perk == SeasonPerk.SPRING_PASSIVE,
                settings.perk == SeasonPerk.SPRING_ACTIVE && currentAbilityTimer > 0
            );
        }

        // Update the season gimmicks, but do not process other things.
        if (gimmickSprMo2 != null) {
            gimmickSprMo2.setCountdown(badges, getGimmickPerkBoost());
        }
        if (gimmickRollSpr != null) {
            gimmickRollSpr.setCountdown(badges, getGimmickPerkBoost());
        }
        if (gimmickSprMo3 != null) {
            gimmickSprMo3.setCountdown(badges, getGimmickPerkBoost());
        }
        if (gimmickSumMo1 != null) {
            gimmickSumMo1.updateChance(engine, badges, getGimmickPerkBoost());
        }
        if (gimmickSumMo2 != null) {
            gimmickSumMo2.setAllowance(badges, getGimmickPerkBoost());
        }
        if (gimmickSumMo3 != null) {
            gimmickSumMo3.getSpeed(engine, badges, getGimmickPerkBoost());
        }
        if (gimmickRollSum != null) {
            gimmickRollSum.getSpeed(engine, badges, getGimmickPerkBoost());
        }
        if (gimmickAutMo1 != null) {
            gimmickAutMo1.getFallDelay(badges, getGimmickPerkBoost());
        }
        if (gimmickAutMo2 != null) {
            gimmickAutMo2.updateChance(engine, badges, getGimmickPerkBoost());
        }
        if (gimmickAutMo3 != null) {
            gimmickAutMo3.updateBonusGap(badges, getGimmickPerkBoost());
        }
        if (gimmickRollAut != null) {
            gimmickRollAut.setCurrentRadius(badges, getGimmickPerkBoost());
        }
        if (gimmickWinMo1 != null) {
            gimmickWinMo1.updateProportion(badges, getGimmickPerkBoost());
        }
        if (gimmickWinMo2 != null) {
            gimmickWinMo2.setTickTime(badges, getGimmickPerkBoost());
            gimmickWinMo2.reduceHeight(li);
        }
        if (gimmickWinMo3 != null) {
            gimmickWinMo3.updateChance(engine, badges, getGimmickPerkBoost());
        }
        if (gimmickRollWin != null) {
            gimmickRollWin.setCountdownMax(badges, getGimmickPerkBoost());
        }
    }

    private void purifyFieldAndNexts(GameEngine engine) {
        FieldManipulation.clearFieldEffects(engine.field, blk -> {
            blk.countdown = 0;
            blk.color = Block.gemToNormalColor(blk.color);
            blk.bonusValue = 0;
            blk.secondaryColor = 0;
        });

        FieldManipulation.updateAllBlockConnections(engine.field);

        if (engine.holdPieceObject != null) {
            engine.holdPieceObject.setAttribute(Block.BLOCK_ATTRIBUTE_BONE, false);
        }

        for (int i = 0; i < engine.nextPieceArraySize; ++i) {
            engine.nextPieceArrayObject[i] = HasCustomMove.initialisePiece(engine, engine.nextPieceArrayID[i]);
        }
    }

    @Override
    public boolean inOnLineClear(GameEngine engine, int playerID) {
        inInputRepeatCheck(engine);

        inLineClearing(engine, playerID);

        if (engine.stat != GameEngine.STAT_LINECLEAR) {
            return true;
        }

        inLineClearAnimation(engine);

        inLineDelayCancelCheck(engine);

        if (inAfterLineDelay(engine, playerID)) return true;

        engine.statc[0]++;
        return true;
    }

    @Override
    public boolean onLineClear(GameEngine engine, int playerID) {
        // If field purify queued, clear field effects.
        // It will be true when seasons change, or when the ending starts.
        if (fieldPurifyQueued) {
            fieldPurifyQueued = false;
            purifyFieldAndNexts(engine);
        }

        // Fixed line delay for non-piece line clears.
        if (!lineClearAfterPiece) {
            engine.speed.lineDelay = Math.max(20, engine.speed.lineDelay);
        }

        final boolean olc = inOnLineClear(engine, playerID);
        if (engine.stat != GameEngine.STAT_LINECLEAR) {
            return true;
        }

        if (isLineDelayFinished(engine) && getCompleteLinesWithoutFlagging(engine) > 0 && engine.gameActive) {
            lineClearAfterPiece = false;
            engine.resetStatc();

            return true;
        }

        return olc;
    }

    @Override
    public void renderLineClear(GameEngine engine, int playerID) {
        // When the infinite line clear loop.
        if ((settings.perk == SeasonPerk.WINTER_ACTIVE || settings.perk == SeasonPerk.AUTUMN_ACTIVE) && !lineClearAfterPiece && gimmickRollWin != null) {
            GameTextUtilities.drawAlignedTextBlock(
                engine,
                receiver.getFieldDisplayPositionX(engine, playerID) + 4 + (engine.field.getWidth() * 8),
                receiver.getFieldDisplayPositionY(engine, playerID) + 52 + (engine.field.getHeight() * 8),
                false,
                GameTextUtilities.TextBlock.of(
                    GameTextUtilities.TextJustification.LEFT,
                    GameTextUtilities.Text.ofBig("NICE :)", EventReceiver.COLOR_GREEN)
                ),
                ObjectAlignment.MIDDLE_MIDDLE
            );
        }
    }

    private void levelUp(GameEngine engine) {
        if (!settings.perk.isActive()) {
            final double proportion = engine.statistics.level / (double) MAX_LEVEL;

            engine.meterValue = (int) Math.floor(receiver.getMeterMax(engine) * proportion);

            engine.meterColor = GameEngine.METER_COLOR_RED;
            if (proportion >= 0.75) engine.meterColor = GameEngine.METER_COLOR_GREEN;
            else if (proportion >= 0.5) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
            else if (proportion >= 0.25) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
        }

        setSpeed(engine);

        // Autumn having the Flowing Winds gimmick makes alternative solutions with a partial ghost less fun.
        engine.ghost = engine.ending == 0 && (currentSeason == Season.SPRING || currentSeason == Season.AUTUMN);

        final int fadeout = engine.ending == 0 ? BGM_FADE_LEVEL_TABLE.apply(engine.statistics.level) : -1;
        if ((fadeout > 0) && (engine.statistics.level >= fadeout)) {
            owner.bgmStatus.fadesw = true;
        }
    }

    private boolean inRewind(GameEngine engine) {
        return engine.stat == GameEngine.STAT_CUSTOM && (customState == CustomState.REWIND || customState == CustomState.FINAL_REWIND);
    }

    @Override
    public void drawBackgroundElements(RendererExtension rendererExtension, EventReceiver receiver, GameEngine engine, int playerID) {
        if (engine.gameStarted) {
           rendererExtension.drawFadingAnimatedBackground(receiver, engine, playerID, animatedBackgrounds[getLastBackground()], animatedBackgrounds[getCurrentBackground()], getFadeProgress());
        } else {
            HasCustomFieldDrawing.super.drawBackgroundElements(rendererExtension, receiver, engine, playerID);
        }

        if (!RendererExtension.hasUserEnabledFadeEffect(receiver) && inRewind(engine)) {
            drawing.drawRectangle(
                receiver,
                0, 0,
                640, 480,
                0, 0, 0, 255,
                true
            );
        }

        if (gimmickWinMo1 != null && !inRewind(engine)) {
            gimmickWinMo1.drawOuterFog(receiver, drawing);
        }

        if (vortex != null) {
            vortex.draw(rendererExtension, receiver);
        }

        if (fireAndSnowEffect != null && engine.gameStarted) {
            fireAndSnowEffect.draw(drawing, receiver);
        }
    }

    @Override
    public void drawBetweenFrameAndField(RendererExtension rendererExtension, EventReceiver receiver, GameEngine engine, int playerID) {
        if (rollElapsed <= 120 * 60 && rollStarted && rollTime > 0) {
            CREDITS.drawNoStop(receiver, engine, playerID, rollElapsed / (120d * 60d));
        }
    }

    // Overlays for countdowns or hardness meters.
    private void drawBlockTextOverlays(GameEngine engine, int playerID) {
        if (engine.field == null) return;

        // Draw countdowns for Flourishing Bloom
        final int baseX = receiver.getFieldDisplayPositionX(engine, playerID) + 4;
        final int baseY = receiver.getFieldDisplayPositionY(engine, playerID) + 52;

        for (int y = 0; y < engine.field.getHeightWithoutHurryupFloor(); ++y) {
            for (int x = 0; x < engine.field.getWidth(); ++x) {
                // Flourishing Bloom
                if (gimmickSprMo3 != null) {
                    final Block blk = engine.field.getBlock(x, y);

                    if (blk != null && blk.color == Block.BLOCK_COLOR_GEM_ORANGE) {
                        GameTextUtilities.drawAlignedText(
                            engine,
                            baseX + (16 * x) + 8,
                            baseY + (16 * y) + 8,
                            GameTextUtilities.Text.ofSmall(
                                String.valueOf(blk.countdown),
                                blk.countdown == gimmickSprMo3.getCurrentCountdown() - 1 ? EventReceiver.COLOR_YELLOW : EventReceiver.COLOR_WHITE
                            ),
                            ObjectAlignment.MIDDLE_MIDDLE
                        );
                    }
                }

                // Absolute Zero & Icicles
                {
                    final Block blk = engine.field.getBlock(x, y);

                    if (blk != null && !blk.isEmpty() && blk.color != Block.BLOCK_COLOR_GEM_CYAN && blk.hard > 0) {
                        customGraphics.drawImage(
                            engine, "iceblock",
                            baseX + (16 * x), baseY + (16 * y),
                            0, 0, 32, 32,
                            200, 255, 255, 255,
                            0.5f
                        );
                    }
                }
            }
        }
    }

    private void renderExtraBoardInfo(GameEngine engine, int playerID) {
        drawBlockTextOverlays(engine, playerID);

        if (sparks != null) sparks.draw(receiver);
        if (landingParticles != null) landingParticles.draw(receiver);

        if (gimmickAutMo3 != null && engine.stat != GameEngine.STAT_CUSTOM && engine.gameActive) {
            gimmickAutMo3.renderFlashlight(receiver, engine, playerID, drawing);
        }

        if (gimmickWinMo2 != null && engine.gameActive) {
            gimmickWinMo2.draw(drawing, receiver, engine, playerID);
        }

        if (gimmickWinMo1 != null && engine.stat != GameEngine.STAT_MOVE && engine.stat != GameEngine.STAT_CUSTOM && engine.gameActive) {
            gimmickWinMo1.drawInnerFog(receiver, engine, playerID, drawing);
        }

        if (gimmickRollAut != null && engine.gameActive) {
            gimmickRollAut.draw(drawing, receiver);
        }

        if (settings.perk == SeasonPerk.WINTER_ACTIVE && abilityIsActive(engine) && waFrozenRows > 0) {
            final int baseX = receiver.getFieldDisplayPositionX(engine, playerID) + 4;
            final int baseY = receiver.getFieldDisplayPositionY(engine, playerID) + 52;

            final double phase1 = Math.sin(timeSpentInSeason / (2d * Math.PI)) * 0.75;
            final double phase2 = Math.sin((timeSpentInSeason / (2d * Math.PI)) + Math.PI) * 0.75;
            final double phase3 = Math.cos(timeSpentInSeason / (2d * Math.PI)) * 0.75;
            final double phase4 = Math.cos((timeSpentInSeason / (2d * Math.PI)) + Math.PI) * 0.75;

            drawing.drawRectangle(
                receiver,
                (int) Math.round(baseX - ((phase1 / 2) * 16)),
                (int) Math.round(baseY + (16 * (waCurrentFreezeRow + 1)) - ((phase1 / 2) * 16d)),
                (int) Math.round((16d * engine.field.getWidth()) + (phase1 * 16d)),
                (int) Math.round((16d * waFrozenRows) + (phase1 * 16d)),
                63, 255, 255, 64,
                true
            );

            drawing.drawRectangle(
                receiver,
                (int) Math.round(baseX - ((phase2 / 2) * 16)),
                (int) Math.round(baseY + (16 * (waCurrentFreezeRow + 1)) - ((phase2 / 2) * 16d)),
                (int) Math.round((16d * engine.field.getWidth()) + (phase2 * 16d)),
                (int) Math.round((16d * waFrozenRows) + (phase2 * 16d)),
                63, 255, 255, 64,
                true
            );

            drawing.drawRectangle(
                receiver,
                (int) Math.round(baseX - ((phase3 / 2) * 16)),
                (int) Math.round(baseY + (16 * (waCurrentFreezeRow + 1)) - ((phase3 / 2) * 16d)),
                (int) Math.round((16d * engine.field.getWidth()) + (phase3 * 16d)),
                (int) Math.round((16d * waFrozenRows) + (phase3 * 16d)),
                255, 255, 255, 64,
                true
            );

            drawing.drawRectangle(
                receiver,
                (int) Math.round(baseX - ((phase4 / 2) * 16)),
                (int) Math.round(baseY + (16 * (waCurrentFreezeRow + 1)) - ((phase4 / 2) * 16d)),
                (int) Math.round((16d * engine.field.getWidth()) + (phase4 * 16d)),
                (int) Math.round((16d * waFrozenRows) + (phase4 * 16d)),
                255, 255, 255, 64,
                true
            );
        }

        rewindBlocks.forEach(rb -> rb.draw(rendererExtension, receiver));
        textEmitter.drawAll(engine);
    }

    @Override
    public void renderFirst(GameEngine engine, int playerID) {
        inRenderFirst(rendererExtension, receiver, engine, playerID);
        renderExtraBoardInfo(engine, playerID);
    }

    @Override
    public void renderReady(GameEngine engine, int playerID) {
        final int baseX = receiver.getFieldDisplayPositionX(engine, playerID) + 4;
        final int baseY = receiver.getFieldDisplayPositionY(engine, playerID) + 52;

        if (settings.perk.isActive()) {
            GameTextUtilities.drawAlignedTextBlock(
                engine,
                baseX + 80, baseY + (13 * 16),
                false,
                GameTextUtilities.TextBlock.of(
                    GameTextUtilities.TextJustification.CENTRE,
                    GameTextUtilities.Text.custom("YOU HAVE SELECTED AN", EventReceiver.COLOR_WHITE, 0.625f), GameTextUtilities.Text.newLine(),
                    GameTextUtilities.Text.custom("ACTIVE ABILITY. PRESS", EventReceiver.COLOR_WHITE, 0.625f), GameTextUtilities.Text.newLine(),
                    GameTextUtilities.Text.custom("F", EventReceiver.COLOR_YELLOW, 0.625f), GameTextUtilities.Text.custom(" TO ACTIVATE IT WHEN", EventReceiver.COLOR_WHITE, 0.625f), GameTextUtilities.Text.newLine(),
                    GameTextUtilities.Text.custom("ITS ENERGY IS FULL!", EventReceiver.COLOR_WHITE, 0.625f), GameTextUtilities.Text.newLine()
                ),
                ObjectAlignment.TOP_MIDDLE
            );
        }
    }

    @Override
    public void renderMove(GameEngine engine, int playerID) {
        inRenderMove(rendererExtension, receiver, engine, playerID);
        rendererExtension.drawPostHoldOutline(receiver, engine, playerID);

        if (gimmickWinMo1 != null) {
            gimmickWinMo1.drawInnerFog(receiver, engine, playerID, drawing);
        }

        if (gimmickAutMo3 != null && engine.stat != GameEngine.STAT_CUSTOM && engine.gameActive) {
            gimmickAutMo3.renderFlashlight(receiver, engine, playerID, drawing);
        }
    }

    @Override
    public void renderExcellent(GameEngine engine, int playerID) {
        inRenderExcellent(rendererExtension, receiver, engine, playerID);
    }

    private enum ResultScreenPage {
        GRADE_AND_BADGE, LEVELS_AND_TIME, SECONDARY_STATS, BREAKDOWN
    }

    @Override
    public void renderResult(GameEngine engine, int playerID) {
        inRenderResult(rendererExtension, receiver, engine, playerID);

        int baseX = receiver.getFieldDisplayPositionX(engine, playerID) + 4;
        int baseY = receiver.getFieldDisplayPositionY(engine, playerID) + 52;

        receiver.drawMenuFont(engine, playerID, 0, 0, "kn PAGE" + (engine.statc[1] + 1) + "/" + ResultScreenPage.values().length, EventReceiver.COLOR_RED);

        switch (ResultScreenPage.values()[engine.statc[1]]) {
            case GRADE_AND_BADGE: {
                receiver.drawMenuFont(engine, playerID, 0, 2, "TITLE&RANK", EventReceiver.COLOR_BLUE);
                GameTextUtilities.drawAlignedTextBlock(
                    engine,
                    baseX + (engine.field.getWidth() * 8),
                    baseY + (16 * 4),
                    false,
                    gradeName,
                    ObjectAlignment.TOP_MIDDLE
                );

                receiver.drawMenuFont(engine, playerID, 0, 9, "BADGES", EventReceiver.COLOR_BLUE);
                GameTextUtilities.drawAlignedMenuTextBlock(
                    receiver, engine, playerID, false,
                    0, 10, false,
                    badges.getBadgeDisplay(false),
                    ObjectAlignment.TOP_LEFT
                );
                break;
            }
            case LEVELS_AND_TIME: {
                receiver.drawMenuFont(engine, playerID, 0, 2, "DATE", EventReceiver.COLOR_BLUE);
                GameTextUtilities.drawAlignedMenuTextBlock(
                    receiver, engine, playerID, false,
                    engine.field.getWidth() / 2, 3, false,
                    levelToResultBlock(engine.statistics.level),
                    ObjectAlignment.TOP_MIDDLE
                );

                if (rollStarted) {
                    receiver.drawMenuFont(engine, playerID, 0, 6, "ROLL DATE", EventReceiver.COLOR_BLUE);
                    GameTextUtilities.drawAlignedMenuTextBlock(
                        receiver, engine, playerID, false,
                        engine.field.getWidth() / 2, 7, false,
                        levelToResultBlock(rollLevelReached),
                        ObjectAlignment.TOP_MIDDLE
                    );

                }

                drawResultStats(
                    engine, playerID, receiver, 11, EventReceiver.COLOR_BLUE,
                    STAT_TIME
                );

                if (rollStarted) {
                    receiver.drawMenuFont(engine, playerID, 0, 13, "ROLL TIME", EventReceiver.COLOR_BLUE);
                    receiver.drawMenuFont(engine, playerID, 0, 14, String.format("%10s", GeneralUtil.getTime(rollElapsed)));
                }
                break;
            }
            case SECONDARY_STATS: {
                drawResultStats(
                    engine, playerID, receiver, 2, EventReceiver.COLOR_BLUE,
                    STAT_LINES, // 2 - 3
                    STAT_LPM,   // 4 - 5
                    STAT_PIECE, // 6 - 7
                    STAT_PPS    // 8 - 9
                );
                break;
            }
            case BREAKDOWN: {
                if (totalGrades != null) {
                    receiver.drawMenuFont(
                        engine, playerID, 0, 2,
                        "PERFORMANCE",
                        EventReceiver.COLOR_YELLOW
                    );

                    receiver.drawMenuFont(
                        engine, playerID, 0, 2 + 1,
                        String.format("%10s", totalGrades.totalPerformancePoints + " / " + Grading.MAX_PERFORMANCE_POINTS)
                    );

                    receiver.drawMenuFont(
                        engine, playerID, 0, 4,
                        "BADGE",
                        EventReceiver.COLOR_YELLOW
                    );

                    receiver.drawMenuFont(
                        engine, playerID, 0, 4 + 1,
                        String.format("%10s", totalGrades.totalBadgePoints + " / " + Grading.MAX_BADGE_POINTS)
                    );

                    receiver.drawMenuFont(
                        engine, playerID, 0, 6,
                        "LEVEL",
                        EventReceiver.COLOR_YELLOW
                    );

                    receiver.drawMenuFont(
                        engine, playerID, 0, 6 + 1,
                        String.format("%10s", totalGrades.totalLevelPoints + " / " + Grading.MAX_LEVEL_POINTS)
                    );

                    if (rollStarted) {
                        receiver.drawMenuFont(
                            engine, playerID, 0, 8,
                            "ROLL",
                            EventReceiver.COLOR_BLUE
                        );

                        receiver.drawMenuFont(
                            engine, playerID, 0, 8 + 1,
                            String.format("%10s", totalGrades.totalRollLevelPoints + " / " + Grading.MAX_ROLL_LEVEL_POINTS)
                        );

                        receiver.drawMenuFont(
                            engine, playerID, 0, 10,
                            "CLEAR",
                            EventReceiver.COLOR_GREEN
                        );

                        receiver.drawMenuFont(
                            engine, playerID, 0, 10 + 1,
                            String.format("%10s", totalGrades.allClearBonus > 0 ? "+" + totalGrades.allClearBonus : "N/A")
                        );
                    }

                    if (settings.perk == SeasonPerk.PERKLESS) {
                        receiver.drawMenuFont(
                            engine, playerID, 0, 12,
                            "PERKLESS",
                            EventReceiver.COLOR_ORANGE
                        );

                        receiver.drawMenuFont(
                            engine, playerID, 0, 12 + 1,
                            String.format("%10s", totalGrades.perklessBonus > 0 ? "+" + totalGrades.perklessBonus : "N/A")
                        );
                    }
                }
                break;
            }
        }
    }

    @Override
    public boolean onResult(GameEngine engine, int playerID) {
        if (engine.ctrl.isMenuRepeatKey( Controller.BUTTON_UP)) {
            engine.statc[1]--;
            if (engine.statc[1] < 0) engine.statc[1] = ResultScreenPage.values().length - 1;
            engine.playSE("change");
        }

        if (engine.ctrl.isMenuRepeatKey(Controller.BUTTON_DOWN)) {
            engine.statc[1]++;
            if (engine.statc[1] >= ResultScreenPage.values().length) engine.statc[1] = 0;
            engine.playSE("change");
        }

        return false;
    }

    @Override
    public void onFirst(GameEngine engine, int playerID) {
        if (settings.perk == SeasonPerk.WINTER_ACTIVE) {
            if (currentAbilityTimer > 0) {
                setSpeed(engine);
                engine.speed.lockDelay = 180;
            }
        }
    }

    private void processGimmicks(GameEngine engine, int playerID) {
        if (gimmickAutMo3 == null && gimmickRollAut == null) {
            engine.blockShowOutlineOnly = false;
        } else if (gimmickAutMo3 != null) {
            gimmickAutMo3.updateCurrentBonusGap(engine);
        }

        if (gimmickWinMo2 != null && engine.timerActive) {
            gimmickWinMo2.update(engine);
        }

        if (gimmickRollAut != null && rollStarted && engine.gameActive) {
            gimmickRollAut.update(receiver, engine, playerID);
        }

        if (gimmickRollWin != null && rollStarted && engine.gameActive) {
            gimmickRollWin.updateField(engine);
        }
    }

    @Override
    public void onLast(GameEngine engine, int playerID) {
        if (!(engine.stat == GameEngine.STAT_CUSTOM
            && (customState == CustomState.REWIND || customState == CustomState.FINAL_REWIND)
            && fadeProgress == 150)) {
            updateFadeProgress();
        }

        if (!engine.lagStop && engine.gameActive) {
            ++timeSpentInSeason;
            if (engine.timerActive) ++activeTimeSpentInSeason;

            if (settings.perk == SeasonPerk.WINTER_ACTIVE && !abilityIsActive(engine) && engine.field != null) {
                waCurrentFreezeRow = engine.field.getHeightWithoutHurryupFloor() - 1;
                waFrozenRows = 0;
            }

            if (engine.timerActive) {
                if (gimmickSumMo3 != null && engine.statistics.time % 4 == 1) fireAndSnowEffect.addFire(2, false);

                if (gimmickWinMo3 != null && engine.statistics.time % 4 == 1) fireAndSnowEffect.addSnow(5);
                else if (gimmickWinMo2 != null && engine.statistics.time % 4 == 1) fireAndSnowEffect.addSnow(3);
                else if (gimmickWinMo1 != null && engine.statistics.time % 4 == 1) fireAndSnowEffect.addSnow(2);
            }

            engine.ghost |= settings.perk == SeasonPerk.WINTER_ACTIVE && abilityIsActive(engine);
        }

        if (engine.quitflag) {
            ruleOptCopy = null;
            playerProperties = new ProfileProperties(HEADER_COLOUR);
        }

        if (engine.gameActive) {
            animatedBackgrounds[getLastBackground()].updateMove(engine);
            animatedBackgrounds[getCurrentBackground()].updateMove(engine);

            animatedBackgrounds[getLastBackground()].updateDrop(engine, 0);
            animatedBackgrounds[getCurrentBackground()].updateDrop(engine, 0);
        }

        if (rollStarted && engine.gameActive) {
            ++rollElapsed;
            if (--rollTime <= 0) {
                engine.playSE("died");

                engine.stat = GameEngine.STAT_GAMEOVER;
                engine.resetStatc();
            }

            if (rollTime <= 600 && rollTime > 0 && rollTime % 60 == 0) engine.playSE("countdown");

            if (rollTime % 4 == 1) fireAndSnowEffect.addFire(currentSeason == Season.SUMMER ? 8 : 4, currentSeason == Season.WINTER);
        }

        if (fireAndSnowEffect != null) fireAndSnowEffect.update();

        queueFireworkIf(engine, FireworkLauncher.ONE, Stream.STREAM_1);
        queueFireworkIf(engine, FireworkLauncher.TWO, Stream.STREAM_2);
        queueFireworkIf(engine, FireworkLauncher.THREE, Stream.STREAM_3);

        updateLaunchedFireworks(receiver, engine, playerID);

        if (descriptionToDraw != null && descriptionToDraw.update()) {
            descriptionToDraw = null;
        }

        if (badges != null) {
            badges.updateDrawTimers();
        }

        if (!engine.lagStop) {
            if (textEmitter != null) textEmitter.updateAll();
            if (rewindBlocks != null) rewindBlocks.removeIf(RewindBlock::update);

            if (engine.stat == GameEngine.STAT_CUSTOM && engine.gameStarted && fadeProgress < 240) {
                vortex.add(bvr, bvr.nextInt(7) + 2, engine.getSkin());
                vortex.add(bvr, bvr.nextInt(7) + 2, engine.getSkin());
                vortex.add(bvr, bvr.nextInt(7) + 2, engine.getSkin());
                vortex.add(bvr, bvr.nextInt(7) + 2, engine.getSkin());
                vortex.add(bvr, Block.BLOCK_COLOR_RAINBOW, engine.getSkin());
                vortex.add(bvr, Block.BLOCK_COLOR_RAINBOW, engine.getSkin());
            }

            if (vortex != null) {
                vortex.update();
            }

            processGimmicks(engine, playerID);
        }

        if (currentAbilityTimer > 0 && engine.stat == GameEngine.STAT_MOVE) {
            final int prevTimer = currentAbilityTimer--;

            if (settings.perk == SeasonPerk.WINTER_ACTIVE && prevTimer == 1) {
                setSpeed(engine);

                if (engine.stat == GameEngine.STAT_MOVE) {
                    final NextAndFieldState state = statesAtTimes.lastEntry().getValue();

                    engine.field = state.field;
                    engine.nextPieceCount = state.nextPosition;
                    engine.holdPieceObject = state.holdPiece;
                }

                engine.stat = GameEngine.STAT_LINECLEAR;

                lineClearAfterPiece = false;
                engine.tspin = false;
                engine.tspinmini = false;
                engine.tspinez = false;

                waCurrentFreezeRow = engine.field.getHeightWithoutHurryupFloor() - 1;
                waFrozenRows = 0;

                engine.resetStatc();
            }

            if (prevTimer == 1) {
                engine.playSE("stageclear");
            }
        }

        if (currentAbilityTimer > 0) {
            final double proportion = currentAbilityTimer / (double) settings.perk.duration;

            engine.meterValue = (int) Math.floor(receiver.getMeterMax(engine) * proportion);

            engine.meterColor = GameEngine.METER_COLOR_RED;
            if (proportion >= 0.75) engine.meterColor = GameEngine.METER_COLOR_GREEN;
            else if (proportion >= 0.5) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
            else if (proportion >= 0.25) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
        } else if (settings.perk.isActive()) {
            final double proportion = currentEnergy / (double) settings.perk.energyStore;

            engine.meterValue = (int) Math.floor(receiver.getMeterMax(engine) * proportion);

            engine.meterColor = GameEngine.METER_COLOR_RED;
            if (proportion >= 1.0) engine.meterColor = GameEngine.METER_COLOR_GREEN;
            else if (proportion >= (2d / 3d)) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
            else if (proportion >= (1d / 3d)) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
        }

        if (engine.gameActive && engine.stat == GameEngine.STAT_CUSTOM && (customState == CustomState.REWIND || customState == CustomState.FINAL_REWIND)) {
            engine.framecolor = GameEngine.FRAME_COLOR_PURPLE;
        } else if (engine.gameActive && settings.perk.isActive() && currentAbilityTimer > 0) {
            engine.framecolor = GameEngine.FRAME_COLOR_PINK;
        } else if (engine.gameStarted) {
            engine.framecolor = currentSeason.defaultFrameColour;
        } else {
            engine.framecolor = Season.defaultMenuFrameColour();
        }

        if (!engine.lagStop && engine.gameStarted && engine.gameActive && engine.ctrl.isPush(Controller.BUTTON_F) && currentEnergy >= settings.perk.energyStore && settings.perk.isActive()) {
            currentEnergy = 0;
            currentAbilityTimer = settings.perk.duration;

            if (settings.perk == SeasonPerk.AUTUMN_ACTIVE) queuedFreefall = true;
            else if (settings.perk == SeasonPerk.SUMMER_ACTIVE) {
                HasCustomMove.insertIntoNexts(engine, engine.nextPieceCount, Piece.PIECE_I, Piece.PIECE_I, Piece.PIECE_I);
                for (int i = engine.nextPieceCount; i < engine.nextPieceCount + 3; ++i) {
                    for (Block blk : HasCustomMove.getNextObject(engine, i).block) blk.item = -1;
                }
            }

            engine.playSE("medal");
        }

        if (engine.stat == GameEngine.STAT_SETTING || (engine.stat == GameEngine.STAT_RESULT && !owner.replayMode)) {
            // Show player rank
            if (engine.ctrl.isPush(Controller.BUTTON_D) && playerProperties.isLoggedIn() && engine.stat != GameEngine.STAT_CUSTOM) {
                showPlayerStats = !showPlayerStats;
                engine.playSE("change");
            }

            // Show time
            if (engine.ctrl.isPush(Controller.BUTTON_F) && engine.stat != GameEngine.STAT_CUSTOM) {
                showBoard = SubRanking.values()[(showBoard.ordinal() + 1) % SubRanking.values().length];
                engine.playSE("change");
            }
        }

        if (sparks != null) sparks.update();
        if (landingParticles != null) landingParticles.update();
    }

    private static final int[] METER_WHITE = { 255, 255, 255 };
    private static final int[] METER_ORANGE = { 255, 100, 0 };

    @Override
    public void renderLast(GameEngine engine, int playerID) {
        final int titlesColour = settings.perk == SeasonPerk.PERKLESS ? EventReceiver.COLOR_ORANGE : EventReceiver.COLOR_YELLOW;
        final float scale = (receiver.getNextDisplayType() == 2) ? 0.5f : 1.0f;
        final boolean smallGrid = receiver.getNextDisplayType() == 2;

        receiver.drawScoreFont(engine, playerID, 0, 0, getName(), titlesColour);

        if (engine.stat == GameEngine.STAT_SETTING || (engine.stat == GameEngine.STAT_RESULT && !owner.replayMode)) {
            final int topY = smallGrid ? 5 : 3;
            final int leftX = smallGrid ? 18 : 11;
            final boolean showRankings = !owner.replayMode && !settings.fullGhost;

            if (showRankings) {
                final String spaces = smallGrid ? "          " : "   ";

                receiver.drawScoreFont(engine, playerID, 3, topY - 1, "TI&RN" + spaces + showBoard.toString(), titlesColour, scale);
                for (int i = 0; i < SeasonsSettings.RANKING_MAX; ++i) {
                    final boolean rankFlag = (lastRank == i && !showPlayerStats) || (lastRankPlayer == i && showPlayerStats);
                    final SeasonsSettings.LeaderboardEntry entry = showPlayerStats
                        ? settings.leaderboards.readPlayerLeaderboard(settings.perk.leaderboard, i)
                        : settings.leaderboards.readLeaderboard(settings.perk.leaderboard, i);

                    receiver.drawScoreFont(
                        engine, playerID,
                        0,  smallGrid ? topY + i + i : topY + i,
                        String.format("%2d", i + 1),
                        rankFlag,
                        scale
                    );
                    GameTextUtilities.drawAlignedScoreTextBlock(
                        receiver, engine, playerID,
                        smallGrid,
                        3, smallGrid ? topY + i + i : topY + i,
                        false,
                        TotalGrades.gradeForRank(entry.gradePoints),
                        ObjectAlignment.TOP_LEFT
                    );

                    final GameTextUtilities.TextBlock subRankText;
                    switch (showBoard) {
                        case DATE:
                            subRankText = levelToRankBlock(entry.level, entry.rollLevel);
                            break;
                        case TIME:
                            subRankText = GameTextUtilities.TextBlock.of(GameTextUtilities.Text.of(GeneralUtil.getTime(entry.time), rankFlag ? EventReceiver.COLOR_RED : EventReceiver.COLOR_WHITE));
                            break;
                        case PERK:
                            subRankText = GameTextUtilities.TextBlock.of(
                                GameTextUtilities.Text.blankLine(0.125f),
                                GameTextUtilities.Text.custom(
                                    SeasonPerk.values()[entry.perkOrdinal].getName(),
                                    rankFlag ? EventReceiver.COLOR_RED : EventReceiver.COLOR_WHITE,
                                    0.75f
                                )
                            );
                            break;
                        default:
                            // Shouldn't happen, but Java can't figure it out.
                            subRankText = GameTextUtilities.TextBlock.of(GameTextUtilities.Text.of("N/A"));
                    }

                    GameTextUtilities.drawAlignedScoreTextBlock(
                        receiver, engine, playerID,
                        smallGrid,
                        leftX, smallGrid ? topY + i + i : topY + i,
                        false,
                        subRankText,
                        ObjectAlignment.TOP_LEFT
                    );
                }

                if (!playerProperties.isLoggedIn() || !showPlayerStats) {
                    receiver.drawScoreFont(engine, playerID, 0, topY + SeasonsSettings.RANKING_MAX + 7, "LOCAL SCORES", EventReceiver.COLOR_BLUE);
                } else {
                    receiver.drawScoreFont(engine, playerID, 0, topY + SeasonsSettings.RANKING_MAX + 7, "PLAYER SCORES", EventReceiver.COLOR_BLUE);
                }

                if (!playerProperties.isLoggedIn()) {
                    receiver.drawScoreFont(engine, playerID, 0, topY + SeasonsSettings.RANKING_MAX + 8, "(NOT LOGGED IN)\n(E:LOG IN)");
                } else {
                    if (showPlayerStats) {
                        GameTextUtilities.drawAlignedScoreText(
                            receiver, engine, playerID, smallGrid,
                            0, topY + SeasonsSettings.RANKING_MAX + 8,
                            GameTextUtilities.Text.ofBig(owner.replayMode ? settings.playerName : playerProperties.getNameDisplay())
                        );
                    }

                    receiver.drawScoreFont(engine, playerID, 0, topY + SeasonsSettings.RANKING_MAX + 11, "D:SWITCH LOC./PLY. RANK", EventReceiver.COLOR_GREEN);
                }

                receiver.drawScoreFont(engine, playerID, 0, topY + SeasonsSettings.RANKING_MAX + 12, "F:SWITCH DATE/TIME/PERK", EventReceiver.COLOR_GREEN);
            }

            GameTextUtilities.drawAlignedScoreTextBlock(
                receiver, engine, playerID, receiver.getNextDisplayType() == 2,
                0, smallGrid ? topY + (2 * (showRankings ? 1 + SeasonsSettings.RANKING_MAX : (-1))) : topY + (showRankings ? 1 + SeasonsSettings.RANKING_MAX : (-1)), false,
                settings.perk.getDescription(scale),
                ObjectAlignment.TOP_LEFT
            );
        } else if (engine.stat == GameEngine.STAT_CUSTOM && !engine.gameStarted) {
            playerProperties.loginScreen.renderScreen(receiver, engine, playerID);
        } else {
            receiver.drawScoreFont(engine, playerID, 0, 2, "DATE", titlesColour);
            if (engine.stat == GameEngine.STAT_GAMEOVER && rollLevelReached >= 0) {
                receiver.drawScoreFont(engine, playerID, 0, 3, levelToString(rollLevelReached));
            } else {
                receiver.drawScoreFont(engine, playerID, 0, 3, levelToString(engine.statistics.level));
            }

            if (engine.ending == 0 || (engine.gameStarted && !engine.gameActive)) {
                receiver.drawScoreFont(engine, playerID, 0, 5, "TIME", titlesColour);
                receiver.drawScoreFont(engine, playerID, 0, 6, GeneralUtil.getTime(engine.statistics.time));
            } else if (engine.ending > 0 && !rollStarted && engine.stat == GameEngine.STAT_CUSTOM && customState == CustomState.FINAL_REWIND) {
                final int timeDisplay = (int) Math.ceil(Interpolation.tanStep(
                    engine.statistics.time,
                    rollTime,
                    MathHelper.clamp((engine.statc[0] - 180d) / (FINAL_REWIND_TIME - 180d), 0d, 1d)
                ));

                receiver.drawScoreFont(engine, playerID, 0, 5, "TIME...?", titlesColour);
                receiver.drawScoreFont(engine, playerID, 0, 6, GeneralUtil.getTime(timeDisplay));
            } else if (engine.ending > 0) {
                receiver.drawScoreFont(engine, playerID, 0, 5, "ROLL LIMIT", EventReceiver.COLOR_DARKBLUE);
                receiver.drawScoreFont(engine, playerID, 0, 6, GeneralUtil.getTime(rollTime), rollTime <= 600 && rollTime % 2 == 0 && engine.gameActive);
            }

            {
                final int height = engine.field == null ? 20 : engine.field.getHeight();

                final float speedProp;

                if (engine.field == null || engine.speed.gravity == 0 || (settings.perk == SeasonPerk.WINTER_ACTIVE && abilityIsActive(engine))) {
                    speedProp = 0.0f;
                } else {
                    speedProp = engine.speed.gravity == -1 ? ((float) height) : MathHelper.clamp(
                        engine.speed.gravity / (float) engine.speed.denominator,
                        0.0f, (float) height
                    );
                }

                final float meterValue = speedProp < 1.0 ? speedProp : MathHelper.clamp(speedProp / 10.0f, 0.0f, 1.0f);

                final int gridX = (smallGrid && (engine.ending == 0 || (engine.gameStarted && !engine.gameActive))) ? 9 : 12;
                final float barScaleX = (smallGrid && gridX == 12) ? 112f / 42f : 160f / 42f;

                rendererExtension.drawAlignedSpeedMeter(
                    receiver,
                    receiver.getScoreDisplayPositionX(engine, playerID) + (16 * gridX),
                    receiver.getScoreDisplayPositionY(engine, playerID) + (16 * 5) + 8,
                    ObjectAlignment.MIDDLE_LEFT,
                    meterValue,
                    barScaleX,
                    2f,
                    speedProp < 1.0 ? METER_WHITE : RendererExtension.SPEED_METER_GREEN,
                    speedProp < 1.0 ? METER_ORANGE : RendererExtension.SPEED_METER_RED
                );
                receiver.drawScoreFont(
                    engine, playerID,
                    gridX, 6,
                    speedProp < 1.0
                        ? (speedProp == 0.0f ? "0.00G" : String.format("1/%d.%02dG", (int) Math.floor(1f / speedProp), (int) Math.floor(((1f / speedProp) % 1.0f) * 100.0f)))
                        : String.format("%d.%02dG", (int) Math.floor(speedProp), (int) Math.floor((speedProp % 1.0f) * 100.0f)),
                    EventReceiver.COLOR_WHITE
                );
            }

            receiver.drawScoreFont(engine, playerID, 0, 8, "PERK", titlesColour);
            receiver.drawScoreFont(engine, playerID, 0, 9, settings.perk.getName(), abilityIsActive(engine));

            receiver.drawScoreFont(engine, playerID, 0, 11, "BADGES", titlesColour);
            GameTextUtilities.drawAlignedScoreTextBlock(
                receiver, engine, playerID, false,
                0, 12, false,
                badges.getBadgeDisplay(true),
                ObjectAlignment.TOP_LEFT
            );

            if (playerProperties.isLoggedIn() || !settings.playerName.isEmpty()) {
                final String name = playerProperties.isLoggedIn() ? playerProperties.getNameDisplay() : settings.playerName;

                receiver.drawScoreFont(engine, playerID, 13, 11, "PLAYER", titlesColour);
                GameTextUtilities.drawAlignedScoreText(
                    receiver, engine, playerID, false,
                    13, 12,
                    GameTextUtilities.Text.ofBig(name),
                    ObjectAlignment.TOP_LEFT
                );
            }

            if (engine.gameActive) {
                if (engine.statistics.level >= LEVELS_FEB || rollStarted) {
                    receiver.drawScoreFont(engine, playerID, 0, 17, "EFFECTS", titlesColour);
                }

                // region Gimmicks
                final Gimmicks.HasDescription gimmickSlot1;
                final Gimmicks.HasDescription gimmickSlot2;
                final Gimmicks.HasDescription gimmickSlot3;

                if (engine.ending == 0) {
                    switch (currentSeason) {
                        case SPRING:
                            gimmickSlot1 = gimmickSprMo2;
                            gimmickSlot2 = gimmickSprMo3;
                            gimmickSlot3 = null;
                            break;
                        case SUMMER:
                            gimmickSlot1 = gimmickSumMo1;
                            gimmickSlot2 = gimmickSumMo2;
                            gimmickSlot3 = gimmickSumMo3;
                            break;
                        case AUTUMN:
                            gimmickSlot1 = gimmickAutMo1;
                            gimmickSlot2 = gimmickAutMo2;
                            gimmickSlot3 = gimmickAutMo3;
                            break;
                        case WINTER:
                            gimmickSlot1 = gimmickWinMo1;
                            gimmickSlot2 = gimmickWinMo2;
                            gimmickSlot3 = gimmickWinMo3;
                            break;
                        default:
                            gimmickSlot1 = null;
                            gimmickSlot2 = null;
                            gimmickSlot3 = null;
                            break;
                    }
                } else if (rollStarted) {
                    if (gimmickRollSpr != null) gimmickSlot1 = gimmickRollSpr;
                    else if (gimmickRollSum != null) gimmickSlot1 = gimmickRollSum;
                    else if (gimmickRollAut != null) gimmickSlot1 = gimmickRollAut;
                    else if (gimmickRollWin != null) gimmickSlot1 = gimmickRollWin;
                    else gimmickSlot1 = null;

                    gimmickSlot2 = null;
                    gimmickSlot3 = null;
                } else {
                    gimmickSlot1 = null;
                    gimmickSlot2 = null;
                    gimmickSlot3 = null;
                }

                if (gimmickSlot1 != null) {
                    GameTextUtilities.drawAlignedScoreTextBlock(
                        receiver, engine, playerID, false,
                        0, 18,
                        false,
                        gimmickSlot1.getSummary(),
                        ObjectAlignment.TOP_LEFT
                    );
                }

                if (gimmickSlot2 != null) {
                    GameTextUtilities.drawAlignedScoreTextBlock(
                        receiver, engine, playerID, false,
                        0, 19,
                        false,
                        gimmickSlot2.getSummary(),
                        ObjectAlignment.TOP_LEFT
                    );
                }

                if (gimmickSlot3 != null) {
                    GameTextUtilities.drawAlignedScoreTextBlock(
                        receiver, engine, playerID, false,
                        0, 20,
                        false,
                        gimmickSlot3.getSummary(),
                        ObjectAlignment.TOP_LEFT
                    );
                }
                // endregion Gimmicks

                if (descriptionToDraw != null) {
                    descriptionToDraw.descObj.drawDescription(drawing, receiver, engine, descriptionToDraw.getDrawXOffset(), descriptionToDraw.getDrawY());
                }
            }
        }

        rendererExtension.drawPostHoldOutline(receiver, engine, playerID);

        drawFireworks(receiver);
    }

    @Override
    public void saveReplay(GameEngine engine, int playerID, CustomProperties prop) {
        if (!owner.replayMode && !owner.replayRerecord) {
            if (!settings.hasSeenRollIntro) settings.hasSeenRollIntro = engine.statistics.level >= MAX_LEVEL;
            if (!settings.hasCompletedGame)
                settings.hasCompletedGame = engine.statistics.level >= MAX_LEVEL && rollLevelReached >= MAX_LEVEL;

            // Forcefully save settings if roll completed or game completed.
            if (settings.hasSeenRollIntro || settings.hasCompletedGame) {
                settings.saveSetting(owner.modeConfig, false);
                if (playerProperties.isLoggedIn()) settings.saveSettingPlayer(playerProperties);
            }

            // If they have completed the game, set the completion status to true for their profile.
            if (playerProperties.isLoggedIn() && engine.ending > 0 && engine.statistics.level >= MAX_LEVEL) {
                settings.hasCompletedGame = true;
                settings.saveSettingPlayer(playerProperties);
            }
        }

        // Save Replay Properties
        settings.saveSetting(owner.replayProp, true);

        if (!owner.replayMode && !settings.fullGhost && engine.ai == null) {
            lastRank = settings.updateRanking();
            lastRankPlayer = settings.updateRankingPlayer(playerProperties);

            if (lastRank != -1) {
                settings.saveRanking(owner, engine.ruleopt.strRuleName);
                settings.commitSettingAndRank(receiver, owner);
            }

            if (lastRankPlayer != -1) {
                settings.saveRankingPlayer(playerProperties, engine.ruleopt.strRuleName);
                settings.commitPlayerSettingAndRank(playerProperties);
            }
        }
    }

    private static class Grading {
        public static final int MAX_GRADE_POINTS = 12000;
        public static final int MAX_PERFORMANCE_POINTS = 2500;
        public static final int MAX_BADGE_POINTS = 4000;
        public static final int MAX_LEVEL_POINTS = 3000;
        public static final int MAX_ROLL_LEVEL_POINTS = 2500;

        private static final int BASE_PERF_DECAY = 45;
        private static final double PERF_DECAY_POW = 343d / 400d;

        private int performance; // 0->2500
        private int performanceGainDebt;
        private int performanceDecay;
        private int currentPerformanceDecayRate;

        public Grading() {
            performance = 10;
            performanceDecay = 0;
            performanceGainDebt = 0;
            currentPerformanceDecayRate = BASE_PERF_DECAY;
        }

        public void addPerformancePoints(int performancePoints) {
            if (performanceGainDebt > 0) {
                final int oldDebt = performanceGainDebt;

                performanceGainDebt = Math.max(0, performanceGainDebt - performancePoints);
                performancePoints -= oldDebt;
            }

            if (performancePoints <= 0) return;

            performance = Math.min(2500, performance + performancePoints);
            currentPerformanceDecayRate = (int) Math.ceil(BASE_PERF_DECAY * Math.pow(PERF_DECAY_POW, Math.floor(performance / 100d)));
        }

        public void resetDecayCounter() {
            performanceDecay = 0;
        }

        public void updatePerformanceDecay() {
            if (++performanceDecay >= currentPerformanceDecayRate) {
                final int oldPerf = performance;
                performance = Math.max(performance - 1, performance - (performance % 100));

                if (oldPerf == performance && performanceGainDebt < 100) ++performanceGainDebt;

                performanceDecay = 0;
            }
        }

        public int getPerformance() {
            return Math.min(MAX_PERFORMANCE_POINTS, performance);
        }

        // 0->4000 (Maximum at 640 badges (6400 internal badges))
        public static int getBadgePerformance(Badges badges) {
            return Math.min(MAX_BADGE_POINTS, Interpolation.lerp(0, 4000, badges.getBadges() / 6400d));
        }

        // 0->3000 (level)
        public static int getLevelPerformance(int level) {
            return (int) Math.floor((double) MAX_LEVEL_POINTS * level / (double) MAX_LEVEL);
        }

        // 0->3000 (roll level)
        public static int getRollLevelPerformance(int level) {
            return (int) Math.floor((double) MAX_ROLL_LEVEL_POINTS * level / (double) MAX_LEVEL);
        }

        public static final int ALL_CLEAR_BONUS = 500;

        public static final int PERKLESS_BONUS = 500;

        public TotalGrades freeze(int level, int rollLevel, Badges badges, SeasonPerk perk) {
            return new TotalGrades(
                getPerformance(),
                getBadgePerformance(badges),
                getLevelPerformance(level),
                rollLevel < 0 ? 0 : getRollLevelPerformance(rollLevel),
                (rollLevel >= level) && (level >= MAX_LEVEL) ? ALL_CLEAR_BONUS : 0,
                perk == SeasonPerk.PERKLESS ? Interpolation.lerp(0, PERKLESS_BONUS, (level + Math.max(0, rollLevel)) / (MAX_LEVEL * 2d)) : 0
            );
        }
    }

    private static class TotalGrades {
        public final int totalGradePoints;
        public final int totalPerformancePoints;
        public final int totalBadgePoints;
        public final int totalLevelPoints;
        public final int totalRollLevelPoints;
        public final int allClearBonus;
        public final int perklessBonus;

        public TotalGrades(int totalPerformancePoints, int totalBadgePoints, int totalLevelPoints, int totalRollLevelPoints, int allClearBonus, int perklessBonus) {
            this.totalPerformancePoints = totalPerformancePoints;
            this.totalBadgePoints = totalBadgePoints;
            this.totalLevelPoints = totalLevelPoints;
            this.totalRollLevelPoints = totalRollLevelPoints;
            this.allClearBonus = allClearBonus;
            this.perklessBonus = perklessBonus;

            this.totalGradePoints = Math.min(
                Grading.MAX_GRADE_POINTS,
                Math.min(
                    Grading.MAX_GRADE_POINTS - 100,
                    totalPerformancePoints + totalBadgePoints + totalLevelPoints + totalRollLevelPoints + perklessBonus
                ) + allClearBonus
            );
        }

        private static void addRanks(LevelTableBuilder<Pair<GameTextUtilities.TextBlock, IntPair>>.ModifiableLevelTable table, String name, int basePoints, int count, int titleColour) {
            for (int i = 1; i <= count; ++i) {
                // this assumes count <= 20
                final int rank = count - (i - 1);

                String suffix = "TH";
                if (rank == 3) suffix = "RD";
                else if (rank == 2) suffix = "ND";
                else if (rank == 1) suffix = "ST";

                int rankColour = EventReceiver.COLOR_BLUE;
                if (rank == 3) rankColour = EventReceiver.COLOR_ORANGE;
                else if (rank == 2) rankColour = EventReceiver.COLOR_WHITE;
                else if (rank == 1) rankColour = EventReceiver.COLOR_YELLOW;

                table.addValue(
                    Pair.of(
                        GameTextUtilities.TextBlock.of(
                            GameTextUtilities.TextJustification.CENTRE,
                            GameTextUtilities.Text.of(name, titleColour),
                            GameTextUtilities.Text.newLine(),
                            GameTextUtilities.Text.custom(rank + suffix + " RANK", rankColour, 0.75f)
                        ),
                        IntPair.of(
                            basePoints + (i * 100),
                            basePoints + (count * 100)
                        )
                    ),
                    basePoints + (i * 100)
                );
            }
        }

        public static GameTextUtilities.TextBlock gradeForRank(int gradePoints) {
            if (gradePoints < 100) {
                return GameTextUtilities.TextBlock.of(
                    GameTextUtilities.Text.of("SETTLER")
                );
            } else if (gradePoints >= Grading.MAX_GRADE_POINTS) {
                return GameTextUtilities.TextBlock.of(
                    GameTextUtilities.Text.ofSmall("SEASONS", EventReceiver.COLOR_ORANGE),
                    GameTextUtilities.Text.newLine(),
                    GameTextUtilities.Text.ofSmall("GRAND MASTER", EventReceiver.COLOR_YELLOW)
                );
            }

            final Pair<GameTextUtilities.TextBlock, IntPair> info = GRADE_NAMES.apply(gradePoints);
            return GameTextUtilities.TextBlock.of(
                GameTextUtilities.Text.ofSmall(info.valL.get(0).getString(), info.valL.get(0).getColour()),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.ofSmall(info.valL.get(2).getString(), info.valL.get(2).getColour())
            );
        }

        // (Name, (Next Rank at Points, Next Title at Points))
        public static final IntFunction<Pair<GameTextUtilities.TextBlock, IntPair>> GRADE_NAMES;
        static {
            final LevelTableBuilder<Pair<GameTextUtilities.TextBlock, IntPair>>.ModifiableLevelTable table = LevelTableBuilder.createNew();

            // Settler
            table.addValue(
                Pair.of(
                    GameTextUtilities.TextBlock.of(GameTextUtilities.Text.of("SETTLER", EventReceiver.COLOR_WHITE)),
                    IntPair.of(100, 100)
                ),
                100
            );

            // Traveller
            addRanks(table, "TRAVELLER", 100, 7, EventReceiver.COLOR_GREEN);

            // Wanderer
            addRanks(table, "WANDERER", 800, 7, EventReceiver.COLOR_GREEN);

            // Pilgrim
            addRanks(table, "PILGRIM", 1500, 7, EventReceiver.COLOR_GREEN);

            // Nomad
            addRanks(table, "NOMAD", 2200, 7, EventReceiver.COLOR_GREEN);

            // Trainee
            addRanks(table, "TRAINEE", 2900, 7, EventReceiver.COLOR_YELLOW);

            // Warrior
            addRanks(table, "WARRIOR", 3600, 7, EventReceiver.COLOR_YELLOW);

            // Noble
            addRanks(table, "NOBLE", 4300, 7, EventReceiver.COLOR_YELLOW);

            // Hero
            addRanks(table, "HERO", 5000, 7, EventReceiver.COLOR_YELLOW);

            // Attuned
            addRanks(table, "ATTUNED", 5700, 7, EventReceiver.COLOR_ORANGE);

            // Symbiotic
            addRanks(table, "SYMBIOTIC", 6400, 7, EventReceiver.COLOR_ORANGE);

            // Elemental
            addRanks(table, "ELEMENTAL", 7100, 7, EventReceiver.COLOR_ORANGE);

            // Embodiment
            addRanks(table, "EMBODIMENT", 7800, 7, EventReceiver.COLOR_ORANGE);

            // Overseer
            addRanks(table, "OVERSEER", 8500, 7, EventReceiver.COLOR_CYAN);

            // Archon
            addRanks(table, "ARCHON", 9200, 7, EventReceiver.COLOR_CYAN);

            // High Ruler
            addRanks(table, "HIGH RULER", 9900, 7, EventReceiver.COLOR_CYAN);

            // Dominator
            addRanks(table, "DOMINATOR", 10600, 7, EventReceiver.COLOR_CYAN);

            // Master
            addRanks(table, "MASTER", 11300, 7, EventReceiver.COLOR_PINK);

            // SGM
            GRADE_NAMES = table
                .addTerminalValue(
                    Pair.of(
                        GameTextUtilities.TextBlock.of(
                            GameTextUtilities.TextJustification.CENTRE,
                            GameTextUtilities.Text.custom("SEASONS", EventReceiver.COLOR_ORANGE, 1.5f),
                            GameTextUtilities.Text.newLine(),
                            GameTextUtilities.Text.custom("GRAND MASTER", EventReceiver.COLOR_YELLOW, 0.75f)
                        ),
                        IntPair.of(Integer.MAX_VALUE, Integer.MAX_VALUE)
                    )
                ).buildLevelTable();
        }
    }
}
