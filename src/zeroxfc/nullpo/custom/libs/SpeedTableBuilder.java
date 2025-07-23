package zeroxfc.nullpo.custom.libs;

import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.IntFunction;
import mu.nu.nullpo.game.component.SpeedParam;
import org.apache.log4j.Logger;

/**
 * Helper utility for building and using speed tables.
 * All values are in frames at 60FPS.
 * <br />
 * Gravity is expressed as numerator / denominator G (1G = 1 block per frame at 60FPS).
 * <br />
 * Side note: making fluent APIs in Java sucks!
 */
public final class SpeedTableBuilder {
    private static final Logger log = Logger.getLogger(SpeedTableBuilder.class);

    private final NavigableMap<Integer, Integer> gravityNumerators;
    private final NavigableMap<Integer, Integer> gravityDenominators;
    private final NavigableMap<Integer, Integer> ares;
    private final NavigableMap<Integer, Integer> lineAres;
    private final NavigableMap<Integer, Integer> dases;
    private final NavigableMap<Integer, Integer> lockDelays;
    private final NavigableMap<Integer, Integer> lineClearDelays;

    public static SpeedTableBuilder.ModifiableGravityTable createNew() {
        return new SpeedTableBuilder().new ModifiableGravityTable();
    }

    private SpeedTableBuilder() {
        gravityNumerators = new TreeMap<>();
        gravityDenominators = new TreeMap<>();
        ares = new TreeMap<>();
        lineAres = new TreeMap<>();
        dases = new TreeMap<>();
        lockDelays = new TreeMap<>();
        lineClearDelays = new TreeMap<>();
    }

    private static void verifyLevel(int changeLevel, NavigableMap<Integer, Integer> levelValues) {
        if (levelValues.isEmpty()) return;

        final int maxLevel = levelValues.descendingKeySet().first();
        if (maxLevel <= changeLevel) return;

        final RuntimeException exc = new IllegalArgumentException("Level change is lower than or equal to previous level change: " + maxLevel + " -> " + changeLevel);

        log.error(exc);
        throw exc;
    }

    public final class ModifiableGravityTable {
        private ModifiableGravityTable() {}

        public ModifiableGravityTable clear() {
            gravityNumerators.clear();
            gravityDenominators.clear();

            return this;
        }

        public ModifiableGravityTable addGravity(int num, int den, int changeLevel) {
            verifyLevel(changeLevel, gravityNumerators);
            verifyLevel(changeLevel, gravityDenominators);

            gravityNumerators.put(changeLevel, num);
            gravityDenominators.put(changeLevel, den);

            return this;
        }

        public ModifiableARETable addTerminalGravity(int num, int den) {
            addGravity(num, den, Integer.MAX_VALUE);
            return new ModifiableARETable();
        }
    }

    public final class ModifiableARETable {
        private ModifiableARETable() {}

        public ModifiableARETable clear() {
            ares.clear();

            return this;
        }

        public ModifiableARETable addARE(int are, int changeLevel) {
            verifyLevel(changeLevel, ares);

            ares.put(changeLevel, are);

            return this;
        }

        public ModifiableLineARETable addTerminalARE(int are) {
            addARE(are, Integer.MAX_VALUE);
            return new ModifiableLineARETable();
        }
    }

    public final class ModifiableLineARETable {
        private ModifiableLineARETable() {}

        public ModifiableLineARETable clear() {
            lineAres.clear();

            return this;
        }

        public ModifiableLineARETable addLineARE(int lineAre, int changeLevel) {
            verifyLevel(changeLevel, lineAres);

            lineAres.put(changeLevel, lineAre);

            return this;
        }

        public ModifiableDASTable addTerminalLineARE(int lineAre) {
            addLineARE(lineAre, Integer.MAX_VALUE);
            return new ModifiableDASTable();
        }
    }

    public final class ModifiableDASTable {
        private ModifiableDASTable() {}

        public ModifiableDASTable clear() {
            dases.clear();

            return this;
        }

        public ModifiableDASTable addDAS(int das, int changeLevel) {
            verifyLevel(changeLevel, dases);

            dases.put(changeLevel, das);

            return this;
        }

        public ModifiableLockDelayTable addTerminalDAS(int das) {
            addDAS(das, Integer.MAX_VALUE);
            return new ModifiableLockDelayTable();
        }
    }

    public final class ModifiableLockDelayTable {
        private ModifiableLockDelayTable() {}

        public ModifiableLockDelayTable clear() {
            lockDelays.clear();

            return this;
        }

        public ModifiableLockDelayTable addLockDelay(int lockDelay, int changeLevel) {
            verifyLevel(changeLevel, lockDelays);

            lockDelays.put(changeLevel, lockDelay);

            return this;
        }

        public ModifiableLineDelayTable addTerminalLockDelay(int lockDelay) {
            addLockDelay(lockDelay, Integer.MAX_VALUE);
            return new ModifiableLineDelayTable();
        }
    }

    public final class ModifiableLineDelayTable {
        private ModifiableLineDelayTable() {}

        public ModifiableLineDelayTable clear() {
            lineClearDelays.clear();

            return this;
        }

        public ModifiableLineDelayTable addLineDelay(int lineDelay, int changeLevel) {
            verifyLevel(changeLevel, lineClearDelays);

            lineClearDelays.put(changeLevel, lineDelay);

            return this;
        }

        public FinalizableTable addTerminalLineDelay(int lineDelay) {
            addLineDelay(lineDelay, Integer.MAX_VALUE);
            return new FinalizableTable();
        }
    }

    public final class FinalizableTable {
        private FinalizableTable() {}

        public IntFunction<SpeedParam> buildSpeedTable() {
            final TreeMap<Integer, Integer> localGravityNumerators = new TreeMap<>(gravityNumerators);
            final TreeMap<Integer, Integer> localGravityDenominators = new TreeMap<>(gravityDenominators);
            final TreeMap<Integer, Integer> localAres = new TreeMap<>(ares);
            final TreeMap<Integer, Integer> localLineAres = new TreeMap<>(lineAres);
            final TreeMap<Integer, Integer> localDases = new TreeMap<>(dases);
            final TreeMap<Integer, Integer> localLockDelays = new TreeMap<>(lockDelays);
            final TreeMap<Integer, Integer> localLineClearDelays = new TreeMap<>(lineClearDelays);

            return level -> {
                final SpeedParam currentSpeed = new SpeedParam();

                currentSpeed.gravity = localGravityNumerators.higherEntry(level).getValue();
                currentSpeed.denominator = localGravityDenominators.higherEntry(level).getValue();
                currentSpeed.are = localAres.higherEntry(level).getValue();
                currentSpeed.areLine = localLineAres.higherEntry(level).getValue();
                currentSpeed.das = localDases.higherEntry(level).getValue();
                currentSpeed.lockDelay = localLockDelays.higherEntry(level).getValue();
                currentSpeed.lineDelay = localLineClearDelays.higherEntry(level).getValue();

                return currentSpeed;
            };
        }
    }
}
