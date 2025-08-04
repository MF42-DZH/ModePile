package zeroxfc.nullpo.custom.modes.objects.seasons;

import java.util.Random;
import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import zeroxfc.nullpo.custom.libs.FieldManipulation;
import zeroxfc.nullpo.custom.libs.GameTextUtilities;
import zeroxfc.nullpo.custom.libs.PrimitiveDrawingHook;

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
    // APR - Moss Coating (Hurry-up floor, can be pushed down by badges)
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

    // Spring has simple gimmicks that shouldn't feel too unfamiliar.
    public static class Sproutlings implements HasDescription {
        // Basically Speed Mania 2's rising garbage with a slight twist.
        // No delaying garbage by clearing lines, and sometimes doesn't copy the bottom.

        private final Random random;

        private int counter;
        private int quota;

        public Sproutlings(Random random, Badges badges) {
            this.random = random;
            setQuota(badges);
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
                GameTextUtilities.Text.of(String.valueOf(quota), EventReceiver.COLOR_YELLOW),
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

        public void setQuota(Badges badges) {
            // Every 25 badges will increase the quota by 1.
            // The default quota is 8

            final int usedBadges = badges.getBadges() / 10;
            quota = 8 + (usedBadges / 25);
        }

        private static final int ATTRS = Block.BLOCK_ATTRIBUTE_GARBAGE | Block.BLOCK_ATTRIBUTE_VISIBLE | Block.BLOCK_ATTRIBUTE_OUTLINE;

        // Call on first frame of move.
        public void update(GameEngine engine) {
            ++counter;

            if (counter >= quota) {
                counter = 0;

                final double proc = random.nextDouble();

                if (proc < 0.8) {
                    engine.field.addBottomCopyGarbage(
                        Block.BLOCK_COLOR_GEM_GREEN,
                        engine.getSkin(),
                        ATTRS,
                        1
                    );
                } else if (proc < 0.9) {
                    for (int y = engine.field.getHeight() * -1; y < engine.field.getHeightWithoutHurryupFloor() - 1; ++y) {
                        for (int x = 0; x < engine.field.getWidth(); x += 2) {
                            if (engine.field.getBlockEmpty(x, engine.field.getHeightWithoutHurryupFloor() - 1)) continue;

                            Block blk = engine.field.getBlock(x, y + 1);
                            if (blk == null) blk = new Block();

                            engine.field.setBlock(x, y, blk);
                        }
                    }

                    for (int x = 0; x < engine.field.getWidth(); x += 2) {
                        if (engine.field.getBlockEmpty(x, engine.field.getHeightWithoutHurryupFloor() - 1)) continue;

                        final Block blk = new Block();
                        blk.color = Block.BLOCK_COLOR_GEM_GREEN;
                        blk.attribute = ATTRS;
                        blk.skin = engine.getSkin();

                        engine.field.setBlock(x, engine.field.getHeightWithoutHurryupFloor() - 1, blk);
                    }

                    FieldManipulation.updateAllBlockConnections(engine.field);
                } else {
                    for (int y = engine.field.getHeight() * -1; y < engine.field.getHeightWithoutHurryupFloor() - 1; ++y) {
                        for (int x = 1; x < engine.field.getWidth(); x += 2) {
                            if (engine.field.getBlockEmpty(x, engine.field.getHeightWithoutHurryupFloor() - 1)) continue;

                            Block blk = engine.field.getBlock(x, y + 1);
                            if (blk == null) blk = new Block();

                            engine.field.setBlock(x, y, blk);
                        }
                    }

                    for (int x = 1; x < engine.field.getWidth(); x += 2) {
                        if (engine.field.getBlockEmpty(x, engine.field.getHeightWithoutHurryupFloor() - 1)) continue;

                        final Block blk = new Block();
                        blk.color = Block.BLOCK_COLOR_GEM_GREEN;
                        blk.attribute = ATTRS;
                        blk.skin = engine.getSkin();

                        engine.field.setBlock(x, engine.field.getHeightWithoutHurryupFloor() - 1, blk);
                    }

                    FieldManipulation.updateAllBlockConnections(engine.field);
                }

                engine.playSE("garbage");
            }
        }
    }
}
