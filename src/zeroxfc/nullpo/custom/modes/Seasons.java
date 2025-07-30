package zeroxfc.nullpo.custom.modes;

import java.util.function.IntFunction;
import mu.nu.nullpo.game.component.SpeedParam;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.subsystem.mode.DummyMode;
import org.apache.log4j.Logger;
import zeroxfc.nullpo.custom.libs.LevelTableBuilder;
import zeroxfc.nullpo.custom.libs.SpeedTableBuilder;

public class Seasons extends DummyMode {
    private static final Logger log = Logger.getLogger(Seasons.class);

    private static final int CURRENT_VERSION = 0;

    // TODO: This eventually will need to be changed.
    private static final IntFunction<SpeedParam> SPEED_TABLE = SpeedTableBuilder.createNew()
        .addTerminalGravity(4, 256)
        .addTerminalARE(30)
        .addTerminalLineARE(30)
        .addTerminalDAS(15)
        .addTerminalLockDelay(30)
        .addTerminalLineDelay(40)
        .buildSpeedTable();

    private static final int[] DAYS_IN_MONTH = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

    private static final String[] RAW_MONTH_NAMES = { "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC" };

    // This'll be quite the long mode.
    private static final IntFunction<Integer> NEXT_SECTION_LEVELS;
    private static final IntFunction<Integer> LEVELS_SO_FAR;
    private static final IntFunction<String> MONTH_NAMES;
    private static final int LEVEL_LIMIT;

    static {
        final LevelTableBuilder<Integer>.ModifiableLevelTable nslTable = LevelTableBuilder.createNew();
        final LevelTableBuilder<Integer>.ModifiableLevelTable lsfTable = LevelTableBuilder.createNew();
        final LevelTableBuilder<String>.ModifiableLevelTable monthTable = LevelTableBuilder.createNew();

        int levelsSoFar = 0;

        for (int i = 0; i < DAYS_IN_MONTH.length - 1; ++i) {
            lsfTable.addValue(levelsSoFar, levelsSoFar + (DAYS_IN_MONTH[i] * 24));
            levelsSoFar += DAYS_IN_MONTH[i] * 24;

            nslTable.addValue(levelsSoFar, levelsSoFar);
            monthTable.addValue(RAW_MONTH_NAMES[i], levelsSoFar);
        }

        NEXT_SECTION_LEVELS = nslTable.addTerminalValue(Integer.MAX_VALUE).buildLevelTable();
        LEVELS_SO_FAR = lsfTable.addTerminalValue(Integer.MAX_VALUE).buildLevelTable();
        MONTH_NAMES = monthTable.addTerminalValue("INVALID").buildLevelTable();
        LEVEL_LIMIT = levelsSoFar;
    }

    private static String levelToString(int level) {
        final String month = MONTH_NAMES.apply(level);
        final int levelsInMonth = level - LEVELS_SO_FAR.apply(level);
        return String.format("%d %s %d:00", levelsInMonth / 24, month, levelsInMonth % 24);
    }

    private static final int MENU_FRAME_COLOUR = GameEngine.FRAME_COLOR_GRAY;
    private static final int SPRING_FRAME_COLOUR = GameEngine.FRAME_COLOR_GREEN;
    private static final int SUMMER_FRAME_COLOUR = GameEngine.FRAME_COLOR_RED;
    private static final int AUTUMN_FRAME_COLOUR = GameEngine.FRAME_COLOR_YELLOW;
    private static final int WINTER_FRAME_COLOUR = GameEngine.FRAME_COLOR_CYAN;

    @Override
    public String getName() {
        return "SEASONS";
    }
}
