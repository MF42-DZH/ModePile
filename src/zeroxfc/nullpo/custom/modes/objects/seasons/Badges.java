package zeroxfc.nullpo.custom.modes.objects.seasons;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.IntUnaryOperator;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import zeroxfc.nullpo.custom.libs.GameTextUtilities;

// Badges for level bonuses and other stuff.
public class Badges {
    // Badges use 1dp internally, but are represented as ints to preserve precision.
    private int ac;     // AC -- 2
    private int fours;  // 4X -- 10
    private int spins;  // SP -- 10
    private int season; // SE -- 25

    public Badges() {
        ac = 0;
        fours = 0;
        spins = 0;
        season = 0;
    }

    // Call in mode calcScore. Every 10 season badges is an effective 1 badge.
    public void updateBadges(GameEngine engine, int lines, int seasonBadges, boolean minorBoost, boolean majorBoost) {
        final IntUnaryOperator mjBoost = (x) -> majorBoost ? (x + x) : x;

        // AC badge.
        if ((lines >= 1) && (engine.field.isEmpty())) {
            ac += mjBoost.applyAsInt(10);
            if (minorBoost) ++ac;
        }

        // Fours badge. We give partial credit here.
        if (lines >= 4) {
            fours += mjBoost.applyAsInt((int) (2.5 * lines));
            if (minorBoost) ++fours;
        } else if (lines == 3) {
            fours += mjBoost.applyAsInt(4);
            if (minorBoost) ++fours;
        } else if (lines >= 1) {
            fours += mjBoost.applyAsInt(lines);
            if (minorBoost) ++fours;
        }

        // Spin badge.
        if (engine.tspin) {
            if (lines >= 4) {
                spins += mjBoost.applyAsInt((int) (7.5 * lines));
                if (minorBoost) ++spins;
            } else if (lines == 3) {
                spins += mjBoost.applyAsInt(20);
                if (minorBoost) ++spins;
            } else if (lines == 2) {
                spins += mjBoost.applyAsInt(10);
                if (minorBoost) ++spins;
            } else if (lines == 1) {
                spins += mjBoost.applyAsInt((engine.tspinmini || engine.tspinez) ? 1 : 2);
                if (minorBoost) ++spins;
            }
        }

        // Season badges.
        season += mjBoost.applyAsInt(seasonBadges);
        if (seasonBadges > 0 && minorBoost) ++season;
    }

    public Map<String, Integer> getBadgesAsLevelBonuses() {
        final Map<String, Integer> map = new LinkedHashMap<>(4);

        map.put("AC", ac / 20);
        map.put("4X", fours / 100);
        map.put("SP", spins / 100);
        map.put("SE", season / 250);

        return map;
    }

    public int getLevelBonus() {
        int bonus = 0;

        bonus += ac / 20;
        bonus += fours / 100;
        bonus += spins / 100;
        bonus += season / 250;

        return bonus;
    }

    public GameTextUtilities.TextBlock getBadgeDisplay(boolean small) {
        final float baseScale = small ? 0.5f : 1f;

        return GameTextUtilities.TextBlock.of(
            GameTextUtilities.TextJustification.LEFT,
            GameTextUtilities.Text.custom("[AC]", EventReceiver.COLOR_GREEN, baseScale),
            GameTextUtilities.Text.custom(":", EventReceiver.COLOR_WHITE, baseScale),
            GameTextUtilities.Text.custom(String.format("%3d.%d", ac / 10, ac % 10), EventReceiver.COLOR_WHITE, baseScale),
            GameTextUtilities.Text.custom("/2", EventReceiver.COLOR_WHITE, 0.5f),
            GameTextUtilities.Text.newLine(),
            GameTextUtilities.Text.custom("[4X]", EventReceiver.COLOR_YELLOW, baseScale),
            GameTextUtilities.Text.custom(":", EventReceiver.COLOR_WHITE, baseScale),
            GameTextUtilities.Text.custom(String.format("%3d.%d", fours / 10, fours % 10), EventReceiver.COLOR_WHITE, baseScale),
            GameTextUtilities.Text.custom("/10", EventReceiver.COLOR_WHITE, 0.5f),
            GameTextUtilities.Text.newLine(),
            GameTextUtilities.Text.custom("[SP]", EventReceiver.COLOR_PURPLE, baseScale),
            GameTextUtilities.Text.custom(":", EventReceiver.COLOR_WHITE, baseScale),
            GameTextUtilities.Text.custom(String.format("%3d.%d", spins / 10, spins % 10), EventReceiver.COLOR_WHITE, baseScale),
            GameTextUtilities.Text.custom("/10", EventReceiver.COLOR_WHITE, 0.5f),
            GameTextUtilities.Text.newLine(),
            GameTextUtilities.Text.custom("[SE]", EventReceiver.COLOR_CYAN, baseScale),
            GameTextUtilities.Text.custom(":", EventReceiver.COLOR_WHITE, baseScale),
            GameTextUtilities.Text.custom(String.format("%3d.%d", season / 10, season % 10), EventReceiver.COLOR_WHITE, baseScale),
            GameTextUtilities.Text.custom("/25", EventReceiver.COLOR_WHITE, 0.5f)
        );
    }
}
