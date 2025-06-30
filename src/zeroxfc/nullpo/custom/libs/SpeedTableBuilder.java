package zeroxfc.nullpo.custom.libs;

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
        if (levelList.isEmpty() || levelList.peekLast() < changeLevel) return;
        throw new IllegalArgumentException("Level change is lower than previous level change: " + levelList.peekLast() + " -> " + changeLevel);
    }

    public SpeedTableBuilder addGravity(int num, int den, int changeLevel) {
        verifyLevel(changeLevel, gravityLevels);

        gravityNumeratorValues.add(num);
        gravityDenominatorValues.add(den);
        gravityLevels.add(changeLevel);

        return this;
    }

    public SpeedTableBuilder addARE(int are, int changeLevel) {
        verifyLevel(changeLevel, areLevels);

        areValues.add(are);
        areLevels.add(changeLevel);

        return this;
    }

    public SpeedTableBuilder addLineARE(int are, int changeLevel) {
        verifyLevel(changeLevel, lineAreLevels);

        lineAreValues.add(are);
        lineAreLevels.add(changeLevel);

        return this;
    }

    public SpeedTableBuilder addLineDelay(int delay, int changeLevel) {
        verifyLevel(changeLevel, lineDelayLevels);

        lineDelayValues.add(delay);
        lineDelayLevels.add(changeLevel);

        return this;
    }

    public SpeedTableBuilder addLockDelay(int delay, int changeLevel) {
        verifyLevel(changeLevel, lockDelayLevels);

        lockDelayValues.add(delay);
        lockDelayLevels.add(changeLevel);

        return this;
    }

    public SpeedTableBuilder addDAS(int delay, int changeLevel) {
        verifyLevel(changeLevel, dasLevels);

        dasValues.add(delay);
        dasLevels.add(changeLevel);

        return this;
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

        final List<Integer> localGravityNumeratorValues = new LinkedList<>(this.gravityNumeratorValues);
        final List<Integer> localGravityDenominatorValues = new LinkedList<>(this.gravityDenominatorValues);
        final List<Integer> localGravityLevels = new LinkedList<>(this.gravityLevels);

        final List<Integer> localAreValues = new LinkedList<>(this.areValues);
        final List<Integer> localAreLevels = new LinkedList<>(this.areLevels);

        final List<Integer> localLineAreValues = new LinkedList<>(this.lineAreValues);
        final List<Integer> localLineAreLevels = new LinkedList<>(this.lineAreLevels);

        final List<Integer> localLineDelayValues = new LinkedList<>(this.lineDelayValues);
        final List<Integer> localLineDelayLevels = new LinkedList<>(this.lineDelayLevels);

        final List<Integer> localLockDelayValues = new LinkedList<>(this.lockDelayValues);
        final List<Integer> localLockDelayLevels = new LinkedList<>(this.lockDelayLevels);

        final List<Integer> localDasValues = new LinkedList<>(this.dasValues);
        final List<Integer> localDasLevels = new LinkedList<>(this.dasLevels);

        return (level) -> {
            final SpeedParam speed = new SpeedParam();

            for (int i = 0; i < localGravityLevels.size(); i++) {
                if (localGravityLevels.get(i) < level) continue;

                speed.gravity = localGravityNumeratorValues.get(i);
                speed.denominator = localGravityDenominatorValues.get(i);
                break;
            }

            for (int i = 0; i < localAreLevels.size(); i++) {
                if (localAreLevels.get(i) < level) continue;

                speed.are = localAreValues.get(i);
                break;
            }

            for (int i = 0; i < localLineAreLevels.size(); i++) {
                if (localLineAreLevels.get(i) < level) continue;

                speed.areLine = localLineAreValues.get(i);
                break;
            }

            for (int i = 0; i < localLineDelayLevels.size(); i++) {
                if (localLineDelayLevels.get(i) < level) continue;

                speed.lineDelay = localLineDelayValues.get(i);
                break;
            }

            for (int i = 0; i < localLockDelayLevels.size(); i++) {
                if (localLockDelayLevels.get(i) < level) continue;

                speed.lockDelay = localLockDelayValues.get(i);
                break;
            }

            for (int i = 0; i < localDasLevels.size(); i++) {
                if (localDasLevels.get(i) < level) continue;

                speed.das = localDasValues.get(i);
                break;
            }

            return speed;
        };
    }
}
