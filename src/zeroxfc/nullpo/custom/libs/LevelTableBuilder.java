package zeroxfc.nullpo.custom.libs;

import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.IntFunction;
import org.apache.log4j.Logger;

/**
 * Helper utility for building level-based value tables. Essentially a generified version of
 * <code>SpeedTableBuilder</code> but for things other than <code>SpeedParam</code> instances.
 * <br />
 * Use the static <code>createNew</code> method to start creating a table.
 */
public final class LevelTableBuilder<T> {
    private static final Logger log = Logger.getLogger(LevelTableBuilder.class);

    private final NavigableMap<Integer, T> levelValues;
    private final LevelTableBuilder<T> outer = this;

    public static <V> LevelTableBuilder<V>.ModifiableLevelTable createNew() {
        return new LevelTableBuilder<V>().new ModifiableLevelTable();
    }

    private LevelTableBuilder() {
        levelValues = new TreeMap<>();
    }

    private void verifyLevel(int changeLevel) {
        if (levelValues.isEmpty()) return;

        final int maxLevel = levelValues.descendingKeySet().first();
        if (maxLevel <= changeLevel) return;

        final RuntimeException exc = new IllegalArgumentException("Level change is lower than or equal to previous level change: " + maxLevel + " -> " + changeLevel);

        log.error(exc);
        throw exc;
    }

    public final class ModifiableLevelTable {
        private ModifiableLevelTable() {}

        public ModifiableLevelTable clear() {
            levelValues.clear();
            return this;
        }

        public ModifiableLevelTable addValue(T value, int changeLevel) {
            verifyLevel(changeLevel);
            levelValues.put(changeLevel, value);

            return this;
        }

        public FinalizedLevelTable addTerminalValue(T value) {
            addValue(value, Integer.MAX_VALUE);
            return outer.new FinalizedLevelTable();
        }
    }

    public final class FinalizedLevelTable {
        private FinalizedLevelTable() {}

        public IntFunction<T> buildLevelTable() {
            final TreeMap<Integer, T> finalizedTable = new TreeMap<>(levelValues);
            return level -> finalizedTable.higherEntry(level).getValue();
        }
    }
}
