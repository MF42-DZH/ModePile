package zeroxfc.nullpo.custom.modes.objects.seasons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import zeroxfc.nullpo.custom.libs.GameTextUtilities;

// Badges for level bonuses and other stuff.
// Parameter is for a type that extends custom badges.
public class Badges<T extends Badges.Custom> {
    // Badges use 1dp internally, but are represented as ints to preserve precision.
    // Custom badges also use 1dp-rep integers.
    // Except AC. AC uses whole numbers.
    private int ac;    // AC -- 2
    private int fours; // 4X -- 10
    private int spins; // SP -- 10
    private final List<T> customBadges;

    public Badges(T... customBadges) {
        ac = 0;
        fours = 0;
        spins = 0;

        this.customBadges = new ArrayList<>(Arrays.asList(customBadges));
    }

    // Call in mode calcScore.
    public void updateBadges(GameEngine engine, int playerID, int lines, Consumer<? super T> customBadgeUpdater) {
        // AC badge.
        if ((lines >= 1) && (engine.field.isEmpty())) {
            ++ac;
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

        // Update custom badges.
        for (final T badge : customBadges) {
            customBadgeUpdater.accept(badge);
        }
    }

    public Map<String, Integer> getAllBadges() {
        final Map<String, Integer> map = new LinkedHashMap<>(3 + customBadges.size());

        map.put("AC", ac);
        map.put("4X", fours);
        map.put("SP", spins);

        for (final T badge : customBadges) {
            map.put(badge.display, badge.count);
        }

        return map;
    }

    public int getLevelBonus() {
        int bonus = 0;
        bonus += ac / 2;
        bonus += fours / 100;
        bonus += spins / 100;
        return bonus + customBadges.stream().mapToInt(Custom::getLevelBonus).sum();
    }

    public GameTextUtilities.TextBlock getBadgeDisplay() {
        final GameTextUtilities.TextBlockElement acText = GameTextUtilities.texts(
            GameTextUtilities.Text.custom("AC", EventReceiver.COLOR_GREEN, 1f),
            GameTextUtilities.Text.custom(": ", EventReceiver.COLOR_WHITE, 1f),
            GameTextUtilities.Text.custom(String.valueOf(ac), EventReceiver.COLOR_WHITE, 1f),
            GameTextUtilities.Text.custom("/2", EventReceiver.COLOR_WHITE, 0.5f)
        );

        final GameTextUtilities.TextBlockElement foursText = GameTextUtilities.texts(
            GameTextUtilities.Text.custom("4X", EventReceiver.COLOR_YELLOW, 1f),
            GameTextUtilities.Text.custom(": ", EventReceiver.COLOR_WHITE, 1f),
            GameTextUtilities.Text.custom(String.format("%3d.%d", fours / 10, fours % 10), EventReceiver.COLOR_WHITE, 1f),
            GameTextUtilities.Text.custom("/10", EventReceiver.COLOR_WHITE, 0.5f)
        );

        final GameTextUtilities.TextBlockElement spinsText = GameTextUtilities.texts(
            GameTextUtilities.Text.custom("SP", EventReceiver.COLOR_PURPLE, 1f),
            GameTextUtilities.Text.custom(": ", EventReceiver.COLOR_WHITE, 1f),
            GameTextUtilities.Text.custom(String.format("%3d.%d", spins / 10, spins % 10), EventReceiver.COLOR_WHITE, 1f),
            GameTextUtilities.Text.custom("/10", EventReceiver.COLOR_WHITE, 0.5f)
        );

        final List<GameTextUtilities.Text> elements = new LinkedList<>();

        for (final T badge : customBadges) {
            elements.add(GameTextUtilities.Text.custom(badge.display, EventReceiver.COLOR_PURPLE, 1f));
            elements.add(GameTextUtilities.Text.custom(": ", EventReceiver.COLOR_WHITE, 1f));
            elements.add(GameTextUtilities.Text.custom(String.format("%3d.%d", badge.count / 10, badge.count % 10), EventReceiver.COLOR_WHITE, 1f));
            elements.add(GameTextUtilities.Text.custom("/" + (badge.threshold / 10), EventReceiver.COLOR_WHITE, 0.5f));
            elements.add(GameTextUtilities.Text.newLine());
        }

        return GameTextUtilities.TextBlock.of(
            GameTextUtilities.TextJustification.LEFT,
            acText, GameTextUtilities.Text.newLine(),
            foursText, GameTextUtilities.Text.newLine(),
            spinsText, GameTextUtilities.Text.newLine(),
            () -> elements
        );
    }

    public abstract static class Custom {
        public final String display;        // Must be 2 characters.
        public final int displayColour;
        public final int threshold;         // Level bonus threshold.
        public final boolean onceOnlyBonus; // Is bonus capped to 1?

        public int count;

        public Custom(String display, int displayColour, int threshold, boolean onceOnlyBonus) {
            this.display = display;
            this.displayColour = displayColour;
            this.threshold = threshold;
            this.onceOnlyBonus = onceOnlyBonus;

            count = 0;
        }

        public int getLevelBonus() {
            if (onceOnlyBonus) return (count > threshold) ? 1 : 0;
            else return count / threshold;
        }
    }
}
