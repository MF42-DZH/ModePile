package zeroxfc.nullpo.custom.libs;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.IntFunction;

/**
 * Helper utility for building level-based value tables. Essentially a generified version of
 * <code>SpeedTableBuilder</code> but for things other than <code>SpeedParam</code> instances.
 * <br />
 * Use the static <code>createNew</code> method to start creating a table.
 */
public final class LevelTableBuilder<T> {
    private final List<T> values;
    private final LinkedList<Integer> levels;
    private final LevelTableBuilder<T> outer = this;

    public static <V> LevelTableBuilder<V>.ModifiableLevelTable createNew() {
        return new LevelTableBuilder<V>().new ModifiableLevelTable();
    }

    private LevelTableBuilder() {
        values = new LinkedList<>();
        levels = new LinkedList<>();
    }

    public final class ModifiableLevelTable {
        private ModifiableLevelTable() {}

        public ModifiableLevelTable clear() {
            values.clear();
            levels.clear();

            return this;
        }

        private void verifyLevel(int changeLevel) {
            if (levels.isEmpty() || levels.peekLast() <= changeLevel) return;
            throw new IllegalArgumentException("Level change is lower than or equal to previous level change: " + levels.peekLast() + " -> " + changeLevel);
        }

        public ModifiableLevelTable addValue(T value, int changeLevel) {
            verifyLevel(changeLevel);

            values.add(value);
            levels.add(changeLevel);

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
            if (levels.isEmpty() || values.isEmpty()) {
                throw new IllegalStateException("Value or level table is empty!");
            }

            if (levels.peekLast() < Integer.MAX_VALUE) {
                throw new IllegalStateException("Have not added terminal value yet!");
            }

            final List<T> localValues = new ArrayList<>(values);
            final List<Integer> localLevels = new ArrayList<>(levels);

            return (level) -> {
                for (int i = 0; i < localLevels.size(); ++i) {
                    if (localLevels.get(i) <= level) continue;
                    return localValues.get(i);
                }

                // This shouldn't happen unless you somehow have more than Integer.MAX_VALUE levels.
                return null;
            };
        }
    }
}
