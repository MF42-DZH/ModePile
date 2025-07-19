package zeroxfc.nullpo.custom.libs;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.IntFunction;
import mu.nu.nullpo.game.component.SpeedParam;

/**
 * Helper utility for building and using speed tables.
 * All values are in frames at 60FPS.
 *
 * Gravity is expressed as numerator / denominator G (1G = 1 block per frame at 60FPS).
 */
public class SpeedTableBuilder {
    private final List<Integer> gravityNumeratorValues;
    private final List<Integer> gravityDenominatorValues;
    private final LinkedList<Integer> gravityLevels;

    private final List<Integer> areValues;
    private final LinkedList<Integer> areLevels;

    private final List<Integer> lineAreValues;
    private final LinkedList<Integer> lineAreLevels;

    private final List<Integer> lineDelayValues;
    private final LinkedList<Integer> lineDelayLevels;

    private final List<Integer> lockDelayValues;
    private final LinkedList<Integer> lockDelayLevels;

    private final List<Integer> dasValues;
    private final LinkedList<Integer> dasLevels;

    public SpeedTableBuilder() {
        gravityNumeratorValues = new LinkedList<>();
        gravityDenominatorValues = new LinkedList<>();
        gravityLevels = new LinkedList<>();

        areValues = new LinkedList<>();
        areLevels = new LinkedList<>();

        lineAreValues = new LinkedList<>();
        lineAreLevels = new LinkedList<>();

        lineDelayValues = new LinkedList<>();
        lineDelayLevels = new LinkedList<>();

        lockDelayValues = new LinkedList<>();
        lockDelayLevels = new LinkedList<>();

        dasValues = new LinkedList<>();
        dasLevels = new LinkedList<>();
    }

    public SpeedTableBuilder clear() {
        gravityNumeratorValues.clear();
        gravityDenominatorValues.clear();
        gravityLevels.clear();

        areValues.clear();
        areLevels.clear();

        lineAreValues.clear();
        lineAreLevels.clear();

        lineDelayValues.clear();
        lineDelayLevels.clear();

        lockDelayValues.clear();
        lockDelayLevels.clear();

        dasValues.clear();
        dasLevels.clear();

        return this;
    }

    private void verifyLevel(int changeLevel, LinkedList<Integer> levelList) {
        if (levelList.isEmpty() || levelList.peekLast() <= changeLevel) return;
        throw new IllegalArgumentException("Level change is lower than or equal to previous level change: " + levelList.peekLast() + " -> " + changeLevel);
    }

    public SpeedTableBuilder addGravity(int num, int den, int changeLevel) {
        verifyLevel(changeLevel, gravityLevels);

        gravityNumeratorValues.add(num);
        gravityDenominatorValues.add(den);
        gravityLevels.add(changeLevel);

        return this;
    }

    public SpeedTableBuilder addTerminalGravity(int num, int den) {
        return addGravity(num, den, Integer.MAX_VALUE);
    }

    public SpeedTableBuilder addARE(int are, int changeLevel) {
        verifyLevel(changeLevel, areLevels);

        areValues.add(are);
        areLevels.add(changeLevel);

        return this;
    }

    public SpeedTableBuilder addTerminalARE(int are) {
        return addARE(are, Integer.MAX_VALUE);
    }

    public SpeedTableBuilder addLineARE(int are, int changeLevel) {
        verifyLevel(changeLevel, lineAreLevels);

        lineAreValues.add(are);
        lineAreLevels.add(changeLevel);

        return this;
    }

    public SpeedTableBuilder addTerminalLineARE(int are) {
        return addLineARE(are, Integer.MAX_VALUE);
    }

    public SpeedTableBuilder addLineDelay(int delay, int changeLevel) {
        verifyLevel(changeLevel, lineDelayLevels);

        lineDelayValues.add(delay);
        lineDelayLevels.add(changeLevel);

        return this;
    }

    public SpeedTableBuilder addTerminalLineDelay(int delay) {
        return addLineDelay(delay, Integer.MAX_VALUE);
    }

    public SpeedTableBuilder addLockDelay(int delay, int changeLevel) {
        verifyLevel(changeLevel, lockDelayLevels);

        lockDelayValues.add(delay);
        lockDelayLevels.add(changeLevel);

        return this;
    }

    public SpeedTableBuilder addTerminalLockDelay(int delay) {
        return addLockDelay(delay, Integer.MAX_VALUE);
    }

    public SpeedTableBuilder addDAS(int delay, int changeLevel) {
        verifyLevel(changeLevel, dasLevels);

        dasValues.add(delay);
        dasLevels.add(changeLevel);

        return this;
    }

    public SpeedTableBuilder addTerminalDAS(int delay) {
        return addDAS(delay, Integer.MAX_VALUE);
    }

    public IntFunction<SpeedParam> buildSpeedTable() {
        if (gravityLevels.isEmpty()
            || areLevels.isEmpty()
            || lineAreLevels.isEmpty()
            || lineDelayLevels.isEmpty()
            || lockDelayLevels.isEmpty()
            || dasLevels.isEmpty()) {
            throw new IllegalStateException("One or more value tables are empty!");
        }

        if (gravityLevels.peekLast() < Integer.MAX_VALUE
            || areLevels.peekLast() < Integer.MAX_VALUE
            || lineAreLevels.peekLast() < Integer.MAX_VALUE
            || lineDelayLevels.peekLast() < Integer.MAX_VALUE
            || lockDelayLevels.peekLast() < Integer.MAX_VALUE
            || dasLevels.peekLast() < Integer.MAX_VALUE) {
            throw new IllegalStateException("One or more tables not capped with a terminal value!");
        }

        final List<Integer> localGravityNumeratorValues = new ArrayList<>(this.gravityNumeratorValues);
        final List<Integer> localGravityDenominatorValues = new ArrayList<>(this.gravityDenominatorValues);
        final List<Integer> localGravityLevels = new ArrayList<>(this.gravityLevels);

        final List<Integer> localAreValues = new ArrayList<>(this.areValues);
        final List<Integer> localAreLevels = new ArrayList<>(this.areLevels);

        final List<Integer> localLineAreValues = new ArrayList<>(this.lineAreValues);
        final List<Integer> localLineAreLevels = new ArrayList<>(this.lineAreLevels);

        final List<Integer> localLineDelayValues = new ArrayList<>(this.lineDelayValues);
        final List<Integer> localLineDelayLevels = new ArrayList<>(this.lineDelayLevels);

        final List<Integer> localLockDelayValues = new ArrayList<>(this.lockDelayValues);
        final List<Integer> localLockDelayLevels = new ArrayList<>(this.lockDelayLevels);

        final List<Integer> localDasValues = new ArrayList<>(this.dasValues);
        final List<Integer> localDasLevels = new ArrayList<>(this.dasLevels);

        return (level) -> {
            final SpeedParam speed = new SpeedParam();

            speed.gravity = localGravityNumeratorValues.get(localGravityNumeratorValues.size() - 1);
            speed.denominator = localGravityDenominatorValues.get(localGravityDenominatorValues.size() - 1);
            for (int i = 0; i < localGravityLevels.size(); i++) {
                if (localGravityLevels.get(i) <= level) continue;

                speed.gravity = localGravityNumeratorValues.get(i);
                speed.denominator = localGravityDenominatorValues.get(i);
                break;
            }

            speed.are = localAreValues.get(localAreValues.size() - 1);
            for (int i = 0; i < localAreLevels.size(); i++) {
                if (localAreLevels.get(i) <= level) continue;

                speed.are = localAreValues.get(i);
                break;
            }

            speed.areLine = localLineAreValues.get(localLineAreValues.size() - 1);
            for (int i = 0; i < localLineAreLevels.size(); i++) {
                if (localLineAreLevels.get(i) <= level) continue;

                speed.areLine = localLineAreValues.get(i);
                break;
            }

            speed.lineDelay = localLineDelayValues.get(localLineDelayValues.size() - 1);
            for (int i = 0; i < localLineDelayLevels.size(); i++) {
                if (localLineDelayLevels.get(i) <= level) continue;

                speed.lineDelay = localLineDelayValues.get(i);
                break;
            }

            speed.lockDelay = localLockDelayValues.get(localLockDelayValues.size() - 1);
            for (int i = 0; i < localLockDelayLevels.size(); i++) {
                if (localLockDelayLevels.get(i) <= level) continue;

                speed.lockDelay = localLockDelayValues.get(i);
                break;
            }

            speed.das = localDasValues.get(localDasValues.size() - 1);
            for (int i = 0; i < localDasLevels.size(); i++) {
                if (localDasLevels.get(i) <= level) continue;

                speed.das = localDasValues.get(i);
                break;
            }

            return speed;
        };
    }
}
