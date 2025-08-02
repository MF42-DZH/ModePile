package zeroxfc.nullpo.custom.modes.objects.seasons;

import java.util.LinkedHashMap;
import java.util.Map;
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
    public void updateBadges(GameEngine engine, int playerID, int lines, int seasonBadges) {
        // AC badge.
        if ((lines >= 1) && (engine.field.isEmpty())) {
            ac += 10;
        }

        // Fours badge. We give partial credit here.
        if (lines >= 4) {
            fours += 10;
        } else if (lines == 3) {
            fours += 4;
        } else if (lines >= 1) {
            fours += lines;
        }

        // Spin badge.
        if (engine.tspin) {
            if (lines >= 4) {
                spins += 30;
            } else if (lines == 3) {
                spins += 20;
            } else if (lines == 2) {
                spins += 10;
            } else if (lines == 1) {
                spins += (engine.tspinmini || engine.tspinez) ? 1 : 2;
            }
        }

        // Season badges.
        season += seasonBadges;
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
            GameTextUtilities.Text.custom(": ", EventReceiver.COLOR_WHITE, baseScale),
            GameTextUtilities.Text.custom(String.format("%3d.%d", ac / 10, ac % 10), EventReceiver.COLOR_WHITE, baseScale),
            GameTextUtilities.Text.custom("/2", EventReceiver.COLOR_WHITE, 0.5f),
            GameTextUtilities.Text.newLine(),
            GameTextUtilities.Text.custom("[4X]", EventReceiver.COLOR_YELLOW, baseScale),
            GameTextUtilities.Text.custom(": ", EventReceiver.COLOR_WHITE, baseScale),
            GameTextUtilities.Text.custom(String.format("%3d.%d", fours / 10, fours % 10), EventReceiver.COLOR_WHITE, baseScale),
            GameTextUtilities.Text.custom("/10", EventReceiver.COLOR_WHITE, 0.5f),
            GameTextUtilities.Text.newLine(),
            GameTextUtilities.Text.custom("[SP]", EventReceiver.COLOR_PURPLE, baseScale),
            GameTextUtilities.Text.custom(": ", EventReceiver.COLOR_WHITE, baseScale),
            GameTextUtilities.Text.custom(String.format("%3d.%d", spins / 10, spins % 10), EventReceiver.COLOR_WHITE, baseScale),
            GameTextUtilities.Text.custom("/10", EventReceiver.COLOR_WHITE, 0.5f),
            GameTextUtilities.Text.newLine(),
            GameTextUtilities.Text.custom("[SE]", EventReceiver.COLOR_CYAN, baseScale),
            GameTextUtilities.Text.custom(": ", EventReceiver.COLOR_WHITE, baseScale),
            GameTextUtilities.Text.custom(String.format("%3d.%d", season / 10, season % 10), EventReceiver.COLOR_WHITE, baseScale),
            GameTextUtilities.Text.custom("/25", EventReceiver.COLOR_WHITE, 0.5f)
        );
    }
}
