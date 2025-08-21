package zeroxfc.nullpo.custom.modes.objects.seasons;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.IntUnaryOperator;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import zeroxfc.nullpo.custom.libs.GameTextUtilities;

// Badges for level bonuses and other stuff.
public class Badges {
    private static final int B2B_BONUS_MAX = 2;

    // Badges use 1dp internally, but are represented as ints to preserve precision.
    private int ac;     // AC -- 2
    private int fours;  // 4X -- 20
    private int spins;  // SP -- 15
    private int season; // SE -- 30

    private int acGreen;
    private int foursGreen;
    private int spinsGreen;
    private int seasonGreen;

    public Badges() {
        ac = 0;
        fours = 0;
        spins = 0;
        season = 0;

        acGreen = 0;
        foursGreen = 0;
        spinsGreen = 0;
        seasonGreen = 0;
    }

    // Call in mode calcScore. Every 10 season badges is an effective 1 badge.
    public void updateBadges(GameEngine engine, int lines, boolean minorBoost, boolean majorBoost) {
        final IntUnaryOperator mjBoost = (x) -> majorBoost ? (x * 2) + (x >>> 1) : x;

        // AC badge.
        if ((lines >= 1) && (engine.field.isEmpty())) {
            int gain = mjBoost.applyAsInt(10);
            if (minorBoost) gain += 2;

            ac += gain;
            acGreen += gain * 6;
        }

        // Fours badge. We give partial credit here.
        int foursGain = 0;

        if (lines >= 4) {
            foursGain += mjBoost.applyAsInt((int) (2.5 * lines));
            foursGain += mjBoost.applyAsInt(Math.min(B2B_BONUS_MAX, engine.b2bcount - 1));

            if (minorBoost) foursGain += 2;
        } else if (lines == 3) {
            foursGain += mjBoost.applyAsInt(4);
            if (minorBoost) foursGain += 2;
        } else if (lines >= 1) {
            foursGain += mjBoost.applyAsInt(lines);
            if (minorBoost) foursGain += 2;
        }

        fours += foursGain;
        foursGreen += foursGain * 3;

        // Spin badge.
        int spinsGain = 0;

        if (engine.tspin) {
            if (lines >= 4) {
                spinsGain += mjBoost.applyAsInt(10 * lines);
                if (minorBoost) spinsGain += 2;
            } else if (lines == 3) {
                spinsGain += mjBoost.applyAsInt(25);
                if (minorBoost) spinsGain += 2;
            } else if (lines == 2) {
                spinsGain += mjBoost.applyAsInt(10);
                if (minorBoost) spinsGain += 2;
            } else if (lines == 1) {
                spinsGain += mjBoost.applyAsInt((engine.tspinmini || engine.tspinez) ? 1 : 2);
            }

            if (lines >= 1) {
                if (engine.b2b) spinsGain += mjBoost.applyAsInt(Math.min(B2B_BONUS_MAX, engine.b2bcount - 1));
                if (minorBoost) spinsGain += 2;
            }
        }

        spins += spinsGain;
        spinsGreen += spinsGain * 3;
    }

    public void addSeasonBadges(int seasonBadges, boolean minorBoost, boolean majorBoost) {
        final IntUnaryOperator mjBoost = (x) -> majorBoost ? (x * 2) + (x >>> 1) : x;

        // Season badges.
        int gain = 0;

        gain += mjBoost.applyAsInt(seasonBadges);
        if (seasonBadges > 0 && minorBoost) gain += 2;

        season += gain;
        seasonGreen += gain * 3;
    }

    public Map<String, Integer> getBadgesAsLevelBonuses() {
        final Map<String, Integer> map = new LinkedHashMap<>(4);

        map.put("AC", ac / 20);
        map.put("4X", fours / 200);
        map.put("SP", spins / 150);
        map.put("SE", season / 300);

        return map;
    }

    // Divide by 10 to get whole count!
    // AC Badges count as 5 for gimmicks and titles.
    public int getBadges() {
        return (ac * 5) + fours + spins + season;
    }

    public int getLevelBonus() {
        int bonus = 0;

        bonus += ac / 20;
        bonus += fours / 200;
        bonus += spins / 150;
        bonus += season / 300;

        return bonus;
    }

    public void updateDrawTimers() {
        if (acGreen > 0) acGreen--;
        if (foursGreen > 0) foursGreen--;
        if (spinsGreen > 0) spinsGreen--;
        if (seasonGreen > 0) seasonGreen--;
    }

    public GameTextUtilities.TextBlock getBadgeDisplay(boolean small) {
        final float baseScale = small ? 0.5f : 1f;

        final int abc = acGreen > 0 ? EventReceiver.COLOR_GREEN : EventReceiver.COLOR_WHITE;
        final int fbc = foursGreen > 0 ? EventReceiver.COLOR_GREEN : EventReceiver.COLOR_WHITE;
        final int sbc = spinsGreen > 0 ? EventReceiver.COLOR_GREEN : EventReceiver.COLOR_WHITE;
        final int sec = seasonGreen > 0 ? EventReceiver.COLOR_GREEN : EventReceiver.COLOR_WHITE;

        return GameTextUtilities.TextBlock.of(
            GameTextUtilities.TextJustification.LEFT,
            GameTextUtilities.Text.custom("[AC]", EventReceiver.COLOR_GREEN, baseScale),
            GameTextUtilities.Text.custom(":", EventReceiver.COLOR_WHITE, baseScale),
            GameTextUtilities.Text.custom(String.format("%3d.%d", ac / 10, ac % 10), abc, baseScale),
            GameTextUtilities.Text.custom("/2", abc, 0.5f),
            GameTextUtilities.Text.newLine(),
            GameTextUtilities.Text.custom("[4X]", EventReceiver.COLOR_YELLOW, baseScale),
            GameTextUtilities.Text.custom(":", EventReceiver.COLOR_WHITE, baseScale),
            GameTextUtilities.Text.custom(String.format("%3d.%d", fours / 10, fours % 10), fbc, baseScale),
            GameTextUtilities.Text.custom("/20", fbc, 0.5f),
            GameTextUtilities.Text.newLine(),
            GameTextUtilities.Text.custom("[SP]", EventReceiver.COLOR_PURPLE, baseScale),
            GameTextUtilities.Text.custom(":", EventReceiver.COLOR_WHITE, baseScale),
            GameTextUtilities.Text.custom(String.format("%3d.%d", spins / 10, spins % 10), sbc, baseScale),
            GameTextUtilities.Text.custom("/15", sbc, 0.5f),
            GameTextUtilities.Text.newLine(),
            GameTextUtilities.Text.custom("[", EventReceiver.COLOR_GREEN, baseScale),
            GameTextUtilities.Text.custom("S", EventReceiver.COLOR_YELLOW, baseScale),
            GameTextUtilities.Text.custom("E", EventReceiver.COLOR_ORANGE, baseScale),
            GameTextUtilities.Text.custom("]", EventReceiver.COLOR_CYAN, baseScale),
            GameTextUtilities.Text.custom(":", EventReceiver.COLOR_WHITE, baseScale),
            GameTextUtilities.Text.custom(String.format("%3d.%d", season / 10, season % 10), sec, baseScale),
            GameTextUtilities.Text.custom("/30", sec, 0.5f)
        );
    }
}
