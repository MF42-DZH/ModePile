package zeroxfc.nullpo.custom.modes.objects.seasons;

import zeroxfc.nullpo.custom.libs.Interpolation;
import zeroxfc.nullpo.custom.modes.Seasons;

public class Grading {
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
        return (int) Math.floor((double) MAX_LEVEL_POINTS * level / (double) Seasons.MAX_LEVEL);
    }

    // 0->3000 (roll level)
    public static int getRollLevelPerformance(int level) {
        return (int) Math.floor((double) MAX_ROLL_LEVEL_POINTS * level / (double) Seasons.MAX_LEVEL);
    }

    public static final int ALL_CLEAR_BONUS = 500;

    public static final int PERKLESS_BONUS = 500;

    public TotalGrades freeze(int level, int rollLevel, Badges badges, SeasonPerk perk) {
        return new TotalGrades(
            getPerformance(),
            getBadgePerformance(badges),
            getLevelPerformance(level),
            rollLevel < 0 ? 0 : getRollLevelPerformance(rollLevel),
            (rollLevel >= level) && (level >= Seasons.MAX_LEVEL) ? ALL_CLEAR_BONUS : 0,
            perk == SeasonPerk.PERKLESS ? Interpolation.lerp(0, PERKLESS_BONUS, (level + Math.max(0, rollLevel)) / (Seasons.MAX_LEVEL * 2d)) : 0
        );
    }
}
