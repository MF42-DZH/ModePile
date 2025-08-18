package zeroxfc.nullpo.custom.modes.objects.seasons;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;
import mu.nu.nullpo.game.event.EventReceiver;
import zeroxfc.nullpo.custom.libs.GameTextUtilities;

public enum SeasonPerk {
    PERKLESS(0), // Only selectable if the player has beaten the mode in their current player profile.
    SPRING_PASSIVE(1), // +1 levels to level bonus, and gain +0.1 badges per badge gain, always.
    SPRING_ACTIVE(1, 600, 300, 5, 10, 25, 60), // 2x levels from line clears, and 2.5x badge gain while active.
    SUMMER_PASSIVE(1), // +3 to visible next queue length. Guarantees an I-piece every 50 pieces (pushes back the next queue).
    SUMMER_ACTIVE(1, 250, 0, 2, 5, 10, 25), // Get three I-pieces instantly, pushing the next queue back.
    AUTUMN_PASSIVE(1), // Badge benefits against gimmicks work 2x better.
    AUTUMN_ACTIVE(1, 800, 0, 5, 10, 20, 40), // Freefalls the field.
    WINTER_PASSIVE(1), // +6 frames of lock delay in 20G, Less gravity otherwise.
    WINTER_ACTIVE(1, 1000, 600, 5, 10, 20, 50); // Freezes gravity and gives increased lock delay (= 180) while active.

    private static class Descriptions {
        public final GameTextUtilities.TextBlock perkless;
        public final GameTextUtilities.TextBlock springPassive;
        public final GameTextUtilities.TextBlock springActive;
        public final GameTextUtilities.TextBlock summerPassive;
        public final GameTextUtilities.TextBlock summerActive;
        public final GameTextUtilities.TextBlock autumnPassive;
        public final GameTextUtilities.TextBlock autumnActive;
        public final GameTextUtilities.TextBlock winterPassive;
        public final GameTextUtilities.TextBlock winterActive;

        public static Descriptions get(float baseScale) {
            return new Descriptions(baseScale);
        }

        private Descriptions(float baseScale) {
            perkless = GameTextUtilities.TextBlock.of(
                GameTextUtilities.TextJustification.LEFT,
                GameTextUtilities.Text.custom("\"PERKLESS\"", EventReceiver.COLOR_RED, baseScale), GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.blankLine(1f),
                GameTextUtilities.Text.custom("VENTURE FORTH WITH", EventReceiver.COLOR_WHITE, baseScale * 0.75f), GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("NO AID FROM THE", EventReceiver.COLOR_WHITE, baseScale * 0.75f), GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("ESSENCES OF SEASONS", EventReceiver.COLOR_WHITE, baseScale * 0.75f), GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("FOR AN EXTRA CHALLENGE.", EventReceiver.COLOR_WHITE, baseScale * 0.75f)
            );

            springPassive = GameTextUtilities.TextBlock.of(
                GameTextUtilities.TextJustification.LEFT,
                GameTextUtilities.Text.custom("\"SEED OF GROWTH\"", EventReceiver.COLOR_GREEN, baseScale), GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.blankLine(1f),
                GameTextUtilities.Text.custom("SPRING GIFTS YOU A BOON", EventReceiver.COLOR_WHITE, baseScale * 0.75f), GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("TO AID IN THE GROWTH OF", EventReceiver.COLOR_WHITE, baseScale * 0.75f), GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("A STRONG FOUNDATION LATER.", EventReceiver.COLOR_WHITE, baseScale * 0.75f), GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("(+1 LV/CLEAR, +0.1 BADGE/GAIN)", EventReceiver.COLOR_YELLOW, baseScale * 0.6f)
            );

            springActive = GameTextUtilities.TextBlock.of(
                GameTextUtilities.TextJustification.LEFT,
                GameTextUtilities.Text.custom("\"UNBOUNDED NATURE\" (b)", EventReceiver.COLOR_GREEN, baseScale), GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.blankLine(1f),
                GameTextUtilities.Text.custom("SPRING EMPOWERS YOU WITH", EventReceiver.COLOR_WHITE, baseScale * 0.75f), GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("A TEMPORARY SURGE OF", EventReceiver.COLOR_WHITE, baseScale * 0.75f), GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("FERTILIZING POWER.", EventReceiver.COLOR_WHITE, baseScale * 0.75f), GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("(2X LV & 2.5X BADGES/CLEAR WHEN ACTIVE)", EventReceiver.COLOR_YELLOW, baseScale * 0.6f)
            );

            summerPassive = GameTextUtilities.TextBlock.of(
                GameTextUtilities.TextJustification.LEFT,
                GameTextUtilities.Text.custom("\"BRIGHT FORESIGHT\"", EventReceiver.COLOR_YELLOW, baseScale), GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.blankLine(1f),
                GameTextUtilities.Text.custom("SUMMER GIFTS YOU A BOON", EventReceiver.COLOR_WHITE, baseScale * 0.75f), GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("TO AID IN THE PREDICTION", EventReceiver.COLOR_WHITE, baseScale * 0.75f), GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("OF ALL YOUR OUTCOMES.", EventReceiver.COLOR_WHITE, baseScale * 0.75f), GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("(+3 NEXTS, GET EXTRA I-PIECE REGULARLY)", EventReceiver.COLOR_YELLOW, baseScale * 0.6f)
            );

            summerActive = GameTextUtilities.TextBlock.of(
                GameTextUtilities.TextJustification.LEFT,
                GameTextUtilities.Text.custom("\"DROUGHT QUENCHER\" (b)", EventReceiver.COLOR_YELLOW, baseScale), GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.blankLine(1f),
                GameTextUtilities.Text.custom("SUMMER GIVES YOU THE ABILITY", EventReceiver.COLOR_WHITE, baseScale * 0.75f), GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("TO END VIOLENT DROUGHTS", EventReceiver.COLOR_WHITE, baseScale * 0.75f), GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("WHENEVER YOU DESIRE.", EventReceiver.COLOR_WHITE, baseScale * 0.75f), GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("(GET 3 I-PIECES WHEN ACTIVATED)", EventReceiver.COLOR_YELLOW, baseScale * 0.6f)
            );

            autumnPassive = GameTextUtilities.TextBlock.of(
                GameTextUtilities.TextJustification.LEFT,
                GameTextUtilities.Text.custom("\"BOUNTIFUL HARVEST\"", EventReceiver.COLOR_ORANGE, baseScale), GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.blankLine(1f),
                GameTextUtilities.Text.custom("AUTUMN GIFTS YOU A BOON", EventReceiver.COLOR_WHITE, baseScale * 0.75f), GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("TO LET YOUR HARVEST", EventReceiver.COLOR_WHITE, baseScale * 0.75f), GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("BE DOUBLY RETURNING.", EventReceiver.COLOR_WHITE, baseScale * 0.75f), GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("(BADGE EFFECTS 2X UPON GIMMICKS)", EventReceiver.COLOR_YELLOW, baseScale * 0.6f)
            );

            autumnActive = GameTextUtilities.TextBlock.of(
                GameTextUtilities.TextJustification.LEFT,
                GameTextUtilities.Text.custom("\"INTENSE GRAVITAS\" (b)", EventReceiver.COLOR_ORANGE, baseScale), GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.blankLine(1f),
                GameTextUtilities.Text.custom("AUTUMN BLESSES YOU WITH", EventReceiver.COLOR_WHITE, baseScale * 0.75f), GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("THE STRENGTH TO CAVE IN", EventReceiver.COLOR_WHITE, baseScale * 0.75f), GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("ANY CREVICES IN YOUR ATTACK.", EventReceiver.COLOR_WHITE, baseScale * 0.75f), GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("(FREEFALL STACK WHEN ACTIVATED)", EventReceiver.COLOR_YELLOW, baseScale * 0.6f)
            );

            winterPassive = GameTextUtilities.TextBlock.of(
                GameTextUtilities.TextJustification.LEFT,
                GameTextUtilities.Text.custom("\"STOIC'S WILL\"", EventReceiver.COLOR_CYAN, baseScale), GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.blankLine(1f),
                GameTextUtilities.Text.custom("WINTER GIFTS YOU A BOON", EventReceiver.COLOR_WHITE, baseScale * 0.75f), GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("TO LET YOU FOCUS EVEN", EventReceiver.COLOR_WHITE, baseScale * 0.75f), GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("IN THE TOUGHEST TIMES.", EventReceiver.COLOR_WHITE, baseScale * 0.75f), GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("(+6F LOCK DLY., SLOWER NON-INSTANT GRV.)", EventReceiver.COLOR_YELLOW, baseScale * 0.6f)
            );

            winterActive = GameTextUtilities.TextBlock.of(
                GameTextUtilities.TextJustification.LEFT,
                GameTextUtilities.Text.custom("\"CHILL OUT\" (b)", EventReceiver.COLOR_CYAN, baseScale), GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.blankLine(1f),
                GameTextUtilities.Text.custom("WINTER GIVES YOU THE", EventReceiver.COLOR_WHITE, baseScale * 0.75f), GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("ENCOURAGEMENT TO LET YOU", EventReceiver.COLOR_WHITE, baseScale * 0.75f), GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("STEP BACK AND ASSESS.", EventReceiver.COLOR_WHITE, baseScale * 0.75f), GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom("(0G, 180F LOCK DELAY WHILE ACTIVE)", EventReceiver.COLOR_YELLOW, baseScale * 0.6f)
            );
        }
    }

    // Perk leaderboard (Perkless vs. with Perks).
    public final int leaderboard;
    public static final int LEADERBOARDS = 2;

    public final int energyStore; // Active perks require the correct amount of energy to activate.
    public final int duration;    // Active duration in frames (where applicable).

    // Spin = 5x restoration.
    public final int restoredForSingle;
    public final int restoredForDouble;
    public final int restoredForTriple;
    public final int restoredForFour;

    public boolean isActive() {
        return energyStore > 0;
    }

    public String getName() {
        switch (this) {
            case PERKLESS:
                return "PERKLESS";
            case SPRING_PASSIVE:
                return "SEED OF GROWTH";
            case SPRING_ACTIVE:
                return "UNBOUNDED NATURE";
            case SUMMER_PASSIVE:
                return "BRIGHT FORESIGHT";
            case SUMMER_ACTIVE:
                return "DROUGHT QUENCHER";
            case AUTUMN_PASSIVE:
                return "BOUNTIFUL HARVEST";
            case AUTUMN_ACTIVE:
                return "INTENSE GRAVITAS";
            case WINTER_PASSIVE:
                return "STOIC'S WILL";
            case WINTER_ACTIVE:
                return "CHILL OUT";
            default:
                // XXX: Shouldn't happen, but Java's not smart enough to figure this out.
                return "";
        }
    }

    public GameTextUtilities.TextBlock getDescription(float baseScale) {
        final Descriptions descriptions = Descriptions.get(baseScale);

        switch (this) {
            case PERKLESS:
                return descriptions.perkless;
            case SPRING_PASSIVE:
                return descriptions.springPassive;
            case SPRING_ACTIVE:
                return descriptions.springActive;
            case SUMMER_PASSIVE:
                return descriptions.summerPassive;
            case SUMMER_ACTIVE:
                return descriptions.summerActive;
            case AUTUMN_PASSIVE:
                return descriptions.autumnPassive;
            case AUTUMN_ACTIVE:
                return descriptions.autumnActive;
            case WINTER_PASSIVE:
                return descriptions.winterPassive;
            case WINTER_ACTIVE:
                return descriptions.winterActive;
            default:
                // XXX: Shouldn't happen, but Java's not smart enough to figure this out.
                return null;
        }
    }

    SeasonPerk(int leaderboard, int energyStore, int duration, int restoredForSingle, int restoredForDouble, int restoredForTriple, int restoredForFour) {
        this.leaderboard = leaderboard;
        this.energyStore = energyStore;
        this.duration = duration;
        this.restoredForSingle = restoredForSingle;
        this.restoredForDouble = restoredForDouble;
        this.restoredForTriple = restoredForTriple;
        this.restoredForFour = restoredForFour;
    }

    SeasonPerk(int leaderboard) {
        this(leaderboard, 0, 0, 0, 0, 0, 0);
    }
}
