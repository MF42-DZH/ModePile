package zeroxfc.nullpo.custom.modes.objects.seasons;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import zeroxfc.nullpo.custom.libs.FieldManipulation;
import zeroxfc.nullpo.custom.libs.GameTextUtilities;
import zeroxfc.nullpo.custom.libs.Interpolation;
import zeroxfc.nullpo.custom.libs.PrimitiveDrawingHook;
import zeroxfc.nullpo.custom.libs.mixins.HasCustomOnMove;

public class Gimmicks {
    // This class just holds other classes. We don't need to instantiate it.
    private Gimmicks() {}

    public interface HasDescription {
        String getName();

        GameTextUtilities.TextBlock getSummary();

        GameTextUtilities.TextBlock getDescription();

        default void drawDescription(PrimitiveDrawingHook drawing, EventReceiver receiver, GameEngine engine, int xOffset, int y) {
            final GameTextUtilities.TextBlock desc = getDescription();
            final int textW = desc.getWidth();
            final int textH = desc.getHeight();
            final int rectW = textW + 16;
            final int rectH = textH + 16;

            drawing.drawRectangle(
                receiver,
                640 - rectW + xOffset, y - (rectH / 2),
                rectW, rectH,
                0, 0, 0, 192, true
            );

            GameTextUtilities.drawDirectTextBlock(
                engine,
                640 - rectW + 8 + xOffset, y - (rectH / 2) + 8,
                false,
                desc
            );
        }
    }

    // There aer [sic] only 11 main game gimmicks because Spring 1st month (February) has no gimmick.

    // MAR - Sproutlings (Gem Garbage, can be delayed by badges)
    // APR - Flourishing Blooms (Gems bombs occasionally populate the field, exploding into non-exploding gems, countdowns decreased by badges)
    // MAY - Dehydration (Bone blocks, you get less with more badges, but more as you approach the end of July)
    // JUN - Shortage (every nth I-piece is replaced with another piece, increase n with more badges)
    // JUL - Into The Fire (Fast Speed (~Death 200-500), lock delay increased with badges)
    // AUG - Fall's Call (Kiwamemichi Gravity (5G once it kicks in) + VERY low ARE, 0G -> 5G can be delayed with badges)
    // SEP - Flowing Winds (a player-affectable version of a certain other gimmick spinning people around)
    // OCT - Ghouls Afoot (Stack Outline Only + Flashlight around piece and a scrolling light around the stack (more badges = bigger light))
    // NOV - Packed Ice (Lines only clear every 2 instances of complete lines being formed, clearing > 4 lines grants massive bonuses)
    // DEC - Whiteout (Pieces all turn white, and a haze obscures the screen)
    // JAN - Zero Celsius (1G, an easier version of a certain gimmick ABSOLUTEly terrorising people, interval can be delayed with badges)

    // There will also be 4 gimmicks across the credits roll as you pass through the months.

    // SPRING - Rising Earth (Faster full line copy Sproutlings with brown blocks (slowed by badges))
    // SUMMER - Conflagration (VERY Fast Speed (~Shirase 3xx-8xx; lock delay increased by badges))
    // AUTUMN - Haunting (Outline Only + Bone Blocks)
    // WINTER - Absolute Zero (Zero Celsius but harder (blocks have infinite hardness once frozen, can only clear bottom row with Fours, slowed by badges))

    // Spring has relatively relaxed gimmicks.
    public static class Sproutlings implements HasDescription {
        // Basically Speed Mania 2's rising garbage with a slight twist.
        // No delaying garbage by clearing lines, and sometimes doesn't copy the bottom.

        private int counter;
        private int countdown;

        public Sproutlings(Badges badges) {
            setCountdown(badges);
        }

        @Override
        public String getName() {
            return "SPROUTLINGS";
        }

        @Override
        public GameTextUtilities.TextBlock getSummary() {
            return GameTextUtilities.TextBlock.of(
                GameTextUtilities.TextJustification.LEFT,
                GameTextUtilities.Text.of(getName(), EventReceiver.COLOR_GREEN),
                GameTextUtilities.Text.of(" (", EventReceiver.COLOR_RED),
                GameTextUtilities.Text.of(String.valueOf(countdown), EventReceiver.COLOR_YELLOW),
                GameTextUtilities.Text.of("P)", EventReceiver.COLOR_RED)
            );
        }

        @Override
        public GameTextUtilities.TextBlock getDescription() {
            return GameTextUtilities.TextBlock.of(
                GameTextUtilities.TextJustification.LEFT,
                GameTextUtilities.Text.of(getName(), EventReceiver.COLOR_GREEN),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.blankLine(0.5f),
                GameTextUtilities.Text.custom(
                    "THE EARTH STIRS WITH NEW LIFE.",
                    EventReceiver.COLOR_WHITE, 0.75f
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom(
                    "GEMS WILL SPROUT FROM THE GROUND,",
                    EventReceiver.COLOR_WHITE, 0.75f
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom(
                    "CLEAR THEM TO CLAIM EXTRA STRENGTH!",
                    EventReceiver.COLOR_WHITE, 0.75f
                )
            );
        }

        public void setCountdown(Badges badges) {
            // Every 30 badges will decrease the countdown by 1.
            // The default countdown is 12

            final int usedBadges = badges.getBadges() / 10;
            countdown = Math.max(4, 12 - (usedBadges / 30));
        }

        private static final int ATTRS = Block.BLOCK_ATTRIBUTE_GARBAGE | Block.BLOCK_ATTRIBUTE_VISIBLE | Block.BLOCK_ATTRIBUTE_OUTLINE;

        // Call on first frame of move.
        public void update(GameEngine engine) {
            ++counter;

            if (counter >= countdown) {
                counter = 0;

                engine.field.addBottomCopyGarbage(
                    Block.BLOCK_COLOR_GEM_GREEN,
                    engine.getSkin(),
                    ATTRS,
                    1
                );

                engine.playSE("garbage");
            }
        }
    }

    public static class FlourishingBlooms implements HasDescription {
        private final Random random;
        private int currentCountdown;

        public FlourishingBlooms(Random random, Badges badges) {
            this.random = random;
            setCountdown(badges);
        }

        public int getCurrentCountdown() {
            return currentCountdown;
        }

        public void setCountdown(Badges badges) {
            // Every 40 badges decreases the countdown by 1.

            final int usedBadges = badges.getBadges() / 10;
            currentCountdown = Math.max(4, 12 - (usedBadges / 40));
        }

        @Override
        public String getName() {
            return "FLOURISHING BLOOMS";
        }

        @Override
        public GameTextUtilities.TextBlock getSummary() {
            return GameTextUtilities.TextBlock.of(
                GameTextUtilities.TextJustification.LEFT,
                GameTextUtilities.Text.of("FL. BLOOMS", EventReceiver.COLOR_GREEN),
                GameTextUtilities.Text.of(" (BLOOM IN ", EventReceiver.COLOR_RED),
                GameTextUtilities.Text.of(String.valueOf(currentCountdown), EventReceiver.COLOR_YELLOW),
                GameTextUtilities.Text.of("P)", EventReceiver.COLOR_RED)
            );
        }

        @Override
        public GameTextUtilities.TextBlock getDescription() {
            return GameTextUtilities.TextBlock.of(
                GameTextUtilities.TextJustification.LEFT,
                GameTextUtilities.Text.of(getName(), EventReceiver.COLOR_GREEN),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.blankLine(0.5f),
                GameTextUtilities.Text.custom(
                    "THE SEEDS OF THE NEW BLOOMS HAVE BEGUN",
                    EventReceiver.COLOR_WHITE, 0.75f
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom(
                    "TO GERMINATE AND BLOOM. CLEAR THEM TO",
                    EventReceiver.COLOR_WHITE, 0.75f
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom(
                    "GET A BOOST, OR WAIT UNTIL THEY FULLY",
                    EventReceiver.COLOR_WHITE, 0.75f
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom(
                    "BLOOM TO REAP THE REWARDS!",
                    EventReceiver.COLOR_WHITE, 0.75f
                )
            );
        }

        private static class BlockInfo {
            public final int x;
            public final int y;
            public final Block blk;

            public BlockInfo(int x, int y, Block blk) {
                this.x = x;
                this.y = y;
                this.blk = blk;
            }
        }

        // Call at first move frame.
        public void attemptPlacement(GameEngine engine) {
            if (random.nextDouble() > (0.025 * (13 - currentCountdown) / 3d)) return;

            final List<BlockInfo> blocks = new LinkedList<>();
            for (int y = (-1 * engine.field.getHiddenHeight()); y < engine.field.getHeightWithoutHurryupFloor(); ++y) {
                for (int x = 0; x < engine.field.getWidth(); ++x) {
                    final Block blk = engine.field.getBlock(x, y);
                    if (!blk.isEmpty() && blk.color != Block.BLOCK_COLOR_GEM_ORANGE) {
                        blocks.add(new BlockInfo(x, y, engine.field.getBlock(x, y)));
                    }
                }
            }

            final BlockInfo selected = blocks.get(random.nextInt(blocks.size()));
            selected.blk.attribute &= (~ Block.BLOCK_ATTRIBUTE_BONE);
            selected.blk.secondaryColor = selected.blk.color;
            selected.blk.color = Block.BLOCK_COLOR_GEM_ORANGE;

            engine.playSE("square_s");
        }

        private static final int ATTRS = Block.BLOCK_ATTRIBUTE_GARBAGE | Block.BLOCK_ATTRIBUTE_VISIBLE | Block.BLOCK_ATTRIBUTE_OUTLINE;

        // Call in first frame of ARE.
        public void explode(GameEngine engine) {
            final List<BlockInfo> blocks = new LinkedList<>();

            for (int y = (-1 * engine.field.getHiddenHeight()); y < engine.field.getHeightWithoutHurryupFloor(); ++y) {
                for (int x = 0; x < engine.field.getWidth(); ++x) {
                    final Block blk = engine.field.getBlock(x, y);
                    if (blk.color == Block.BLOCK_COLOR_GEM_ORANGE && (++blk.countdown >= currentCountdown)) {
                        blocks.add(new BlockInfo(x, y, blk));
                    }
                }
            }

            if (blocks.isEmpty()) return;

            final Block outerGem = new Block(Block.BLOCK_COLOR_GEM_PURPLE, engine.getSkin(), ATTRS);
            final Block innerGem = new Block(Block.BLOCK_COLOR_GEM_YELLOW, engine.getSkin(), ATTRS);

            outerGem.pieceNum = engine.statistics.time;
            innerGem.pieceNum = engine.statistics.time;

            final int size = 2;

            for (BlockInfo bi : blocks) {
                bi.blk.color = bi.blk.secondaryColor;
                bi.blk.secondaryColor = 0;

                for (int y = bi.y + size; y >= bi.y - size; --y) {
                    for (int x = bi.x - size; x <= bi.x + size; ++x) {
                        if (x < 0 || x >= engine.field.getWidth() || y < (-1 * engine.field.getHiddenHeight()) || y >= engine.field.getHeightWithoutHurryupFloor()) continue;

                        final int dist = Math.abs(y - bi.y) + Math.abs(x - bi.x);
                        if (dist > size) continue;

                        FieldManipulation.pushColumnUpFrom(engine.field, x, y);
                    }
                }

                for (int y = bi.y - size; y <= bi.y + size; ++y) {
                    for (int x = bi.x - size; x <= bi.x + size; ++x) {
                        if (x < 0 || x >= engine.field.getWidth() || y < (-1 * engine.field.getHiddenHeight()) || y >= engine.field.getHeightWithoutHurryupFloor()) continue;

                        final int dist = Math.abs(y - bi.y) + Math.abs(x - bi.x);
                        if (dist > size) continue;

                        final Block selected = dist <= 1 ? innerGem : outerGem;

                        engine.field.setBlock(x, y, new Block(selected));
                    }
                }
            }

            engine.playSE("square_g");
        }
    }

    public static class Dehydration implements HasDescription {
        private final Random random;
        private final int seasonStartLv;
        private final int seasonEndLv;

        private double chance; // 0 - 1

        // Set the badge chance manually!
        public Dehydration(Random random, int seasonStartLv, int seasonEndLv) {
            this.random = random;
            this.seasonStartLv = seasonStartLv;
            this.seasonEndLv = seasonEndLv;
        }

        public void updateChance(GameEngine engine, Badges badges) {
            final double progress = (engine.statistics.level - seasonStartLv) / (double) (seasonEndLv - seasonStartLv);
            final double baseChance = Interpolation.lerp(0.975, 1.0, progress);

            chance = Math.pow(baseChance, Math.floor(badges.getBadges() / 20d));
        }

        @Override
        public String getName() {
            return "DEHYDRATION";
        }

        @Override
        public GameTextUtilities.TextBlock getSummary() {
            return GameTextUtilities.TextBlock.of(
                GameTextUtilities.TextJustification.LEFT,
                GameTextUtilities.Text.of(getName(), EventReceiver.COLOR_YELLOW),
                GameTextUtilities.Text.of(" (", EventReceiver.COLOR_RED),
                GameTextUtilities.Text.of(String.format("%.02f", chance * 100d) + "%", EventReceiver.COLOR_YELLOW),
                GameTextUtilities.Text.of(")", EventReceiver.COLOR_RED)
            );
        }

        @Override
        public GameTextUtilities.TextBlock getDescription() {
            return GameTextUtilities.TextBlock.of(
                GameTextUtilities.TextJustification.LEFT,
                GameTextUtilities.Text.of(getName(), EventReceiver.COLOR_YELLOW),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.blankLine(0.5f),
                GameTextUtilities.Text.custom(
                    "A SWELTERING HEAT BEGINS TO PARCH",
                    EventReceiver.COLOR_WHITE, 0.75f
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom(
                    "AND DRY OUT THE ENVIRONMENT AROUND",
                    EventReceiver.COLOR_WHITE, 0.75f
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom(
                    "YOU. SALVAGE THOSE BONES!",
                    EventReceiver.COLOR_WHITE, 0.75f
                )
            );
        }

        public void updateNext(GameEngine engine) {
            if (random.nextDouble() > chance) return;

            final Piece piece = HasCustomOnMove.getNextObject(engine, engine.nextPieceCount + engine.ruleopt.nextDisplay);
            if (piece == null) return;

            piece.setAttribute(Block.BLOCK_ATTRIBUTE_BONE, true);
            engine.playSE("movefail");
        }
    }
}
