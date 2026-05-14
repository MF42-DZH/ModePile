package zeroxfc.nullpo.custom.modes.objects.seasons;

import java.util.function.IntFunction;
import mu.nu.nullpo.game.event.EventReceiver;
import zeroxfc.nullpo.custom.libs.GameTextUtilities;
import zeroxfc.nullpo.custom.libs.LevelTableBuilder;
import zeroxfc.nullpo.custom.libs.types.tuples.IntPair;
import zeroxfc.nullpo.custom.libs.types.tuples.Pair;

public class TotalGrades {
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
