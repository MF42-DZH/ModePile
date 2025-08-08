package zeroxfc.nullpo.custom.modes.objects.seasons;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.IntFunction;
import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.component.SpeedParam;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import zeroxfc.nullpo.custom.libs.FieldManipulation;
import zeroxfc.nullpo.custom.libs.GameTextUtilities;
import zeroxfc.nullpo.custom.libs.Interpolation;
import zeroxfc.nullpo.custom.libs.PrimitiveDrawingHook;
import zeroxfc.nullpo.custom.libs.SpeedTableBuilder;
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

    // MAR - Sproutlings (Gem Garbage, sped up by badges for more badge gain)
    // APR - Flourishing Blooms (Gems bombs occasionally populate the field, exploding into non-exploding gems, countdowns decreased by badges)
    // MAY - Dehydration (Bone blocks, you get less with more badges, but more as you approach the end of July)
    // JUN - Mirage (every nth I-piece is skipped, increase n with more badges)
    // JUL - Into The Fire (Fast Speed (~Death 200-500), lock delay increased with badges)
    // AUG - Fall's Call (Kiwamemichi Gravity (5G once it kicks in) + VERY low ARE, 0G -> 5G can be delayed with badges)
    // SEP - Flowing Winds (a player-affectable version of a certain other gimmick spinning people around)
    // OCT - Ghouls Afoot (Stack Outline Only + Flashlight around piece and a scrolling light around the stack (more badges = bigger light))
    // NOV - Whiteout (Pieces all turn white, and a haze obscures the screen)
    // DEC - Snow Mounds (HEBO HIDDEN, slowed with badges)
    // JAN - Zero Celsius (1G, an easier version of a certain gimmick ABSOLUTEly terrorising people, interval can be delayed with badges)

    // There will also be 4 gimmicks across the credits roll as you pass through the months.

    // SPRING - Rising Earth (Faster full line copy Sproutlings with brown blocks (slowed by badges))
    // SUMMER - Conflagration (VERY Fast Speed (~Shirase 3xx-8xx; lock delay increased by badges))
    // AUTUMN - Haunting (Outline Only + Bone Blocks)
    // WINTER - Absolute Zero (Zero Celsius but harder (blocks have infinite hardness once frozen, can only clear bottom row with Fours, slowed by badges))

    // Spring has relatively relaxed gimmicks.
    public static class Sproutlings implements HasDescription {
        // Basically Speed Mania 2's rising garbage with a slight twist.

        private int counter;
        private int countdown;

        public Sproutlings(Badges badges, boolean perkBoost) {
            setCountdown(badges, perkBoost);
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
                GameTextUtilities.Text.of(" PIECES)", EventReceiver.COLOR_RED)
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

        public void setCountdown(Badges badges, boolean perkBoost) {
            // Every 30 badges will decrease the countdown by 1.
            // The default countdown is 12

            final int usedBadges = badges.getBadges() / 10;
            final int denominator = perkBoost ? 15 : 30;

            countdown = Math.max(4, 12 - (usedBadges / denominator));
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

                for (int x = 0; x < engine.field.getWidth(); ++x) {
                    engine.field.getBlock(x, engine.field.getHeightWithoutHurryupFloor() - 1).pieceNum = engine.statistics.time;
                }

                engine.playSE("garbage");
            }
        }
    }

    public static class FlourishingBlooms implements HasDescription {
        private final Random random;
        private int currentCountdown;

        public FlourishingBlooms(Random random, Badges badges, boolean perkBoost) {
            this.random = random;
            setCountdown(badges, perkBoost);
        }

        public int getCurrentCountdown() {
            return currentCountdown;
        }

        public void setCountdown(Badges badges, boolean perkBoost) {
            // Every 40 badges decreases the countdown by 1.

            final int usedBadges = badges.getBadges() / 10;
            final int denominator = perkBoost ? 20 : 40;

            currentCountdown = Math.max(4, 12 - (usedBadges / denominator));
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
                    if (!blk.isEmpty() && blk.color != Block.BLOCK_COLOR_GEM_ORANGE && blk.color != Block.BLOCK_COLOR_GEM_GREEN) {
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
            final List<BlockInfo> toFill = new LinkedList<>();

            for (int y = (-1 * engine.field.getHiddenHeight()); y < engine.field.getHeightWithoutHurryupFloor(); ++y) {
                for (int x = 0; x < engine.field.getWidth(); ++x) {
                    final Block blk = engine.field.getBlock(x, y);
                    if (blk.color == Block.BLOCK_COLOR_GEM_ORANGE && (++blk.countdown >= currentCountdown)) {
                        blocks.add(new BlockInfo(x, y, blk));
                        blk.countdown = 0;
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
                toFill.clear();

                bi.blk.color = bi.blk.secondaryColor;
                bi.blk.secondaryColor = 0;

                for (int y = bi.y + size; y >= bi.y - size; --y) {
                    for (int x = bi.x - size; x <= bi.x + size; ++x) {
                        if (x < 0 || x >= engine.field.getWidth() || y < (-1 * engine.field.getHiddenHeight()) || y >= engine.field.getHeightWithoutHurryupFloor()) continue;

                        final int dist = Math.abs(y - bi.y) + Math.abs(x - bi.x);
                        if (dist > size || engine.field.getBlockEmpty(x, y)) continue;

                        FieldManipulation.pushColumnUpFrom(engine.field, x, y);

                        final Block selected = dist <= 1 ? innerGem : outerGem;
                        toFill.add(new BlockInfo(x, y, new Block(selected)));
                    }
                }

                for (BlockInfo fillBi : toFill) {
                    engine.field.setBlock(fillBi.x, fillBi.y, fillBi.blk);
                }
            }

            engine.playSE("square_g");
        }
    }

    // Summer's gimmicks come hot and fast.
    // N.B. the speed of the mode is also pretty fast in summer
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

        public void updateChance(GameEngine engine, Badges badges, boolean perkBoost) {
            final double progress = (engine.statistics.level - seasonStartLv) / (double) (seasonEndLv - seasonStartLv);
            final double baseChance = Interpolation.lerp(0.975, 1.0, progress);

            final int denominator = perkBoost ? 10 : 20;
            chance = Math.pow(baseChance, badges.getBadges() / (double) denominator);
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

    public static class Mirage implements HasDescription {
        private int counter = 0;
        private int currentAllowance;

        public Mirage(Badges badges, boolean perkBoost) {
            setAllowance(badges, perkBoost);
        }

        public void setAllowance(Badges badges, boolean perkBoost) {
            // Start at 2, +1 for every 80 badges.

            final int usedBadges = badges.getBadges() / 10;
            final int denominator = perkBoost ? 40 : 80;

            currentAllowance = 2 + (usedBadges / denominator);
        }

        // Call this right before a piece is spawned (but only if the piece did not come out of hold).
        public boolean replaceQueue(GameEngine engine) {
            // Only counts I-pieces if they're in the last position of the visible queue.
            final Piece current = HasCustomOnMove.getNextObject(engine, engine.nextPieceCount);

            if (current.id == Piece.PIECE_I && current.block[0].item >= 0) {
                ++counter;
            }

            if (counter > currentAllowance) {
                // Reset counter.
                counter = 0;

                // Skip I-piece.
                engine.nextPieceCount++;

                engine.playSE("movefail");
                return true;
            }

            return false;
        }

        @Override
        public String getName() {
            return "MIRAGE";
        }

        @Override
        public GameTextUtilities.TextBlock getSummary() {
            return GameTextUtilities.TextBlock.of(
                GameTextUtilities.TextJustification.LEFT,
                GameTextUtilities.Text.of(getName(), EventReceiver.COLOR_YELLOW),
                GameTextUtilities.Text.of(" (", EventReceiver.COLOR_RED),
                GameTextUtilities.Text.of(String.valueOf(currentAllowance), EventReceiver.COLOR_YELLOW),
                GameTextUtilities.Text.of(" REAL)", EventReceiver.COLOR_RED)
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
                    "THE INTERTWINING OF THE HOT, ARID SURFACE AIR",
                    EventReceiver.COLOR_WHITE, 0.75f
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom(
                    "AND THE COOL, MILD AIR ABOVE CREATES DECEPTIVE",
                    EventReceiver.COLOR_WHITE, 0.75f
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom(
                    "ILLUSIONS. KEEP ON YOUR TOES AND DO NOT GET",
                    EventReceiver.COLOR_WHITE, 0.75f
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom(
                    "TOO COMPLACENT. MIND YOUR WELLS.",
                    EventReceiver.COLOR_WHITE, 0.75f
                )
            );
        }
    }

    public static class IntoTheFire implements HasDescription {
        private static final IntFunction<SpeedParam> SPEED_TABLE = SpeedTableBuilder.createNew()
            .addTerminalGravity(-1, 256)
            .addARE(14, 2500)
            .addARE(8, 5000)
            .addARE(7, 7500)
            .addTerminalARE(6)
            .addLineARE(8, 5000)
            .addLineARE(7, 7500)
            .addTerminalLineARE(6)
            .addDAS(11, 2500)
            .addDAS(10, 5000)
            .addTerminalDAS(8)
            .addLockDelay(22, 2500)
            .addLockDelay(18, 5000)
            .addTerminalLockDelay(15)
            .addLineDelay(6, 5000)
            .addLineDelay(5, 7500)
            .addTerminalLineDelay(4)
            .buildSpeedTable();

        private final int startLv;
        private final int endLv;
        private int currentLdBoost;

        public IntoTheFire(int startLv, int endLv) {
            this.startLv = startLv;
            this.endLv = endLv;
        }

        private int getLockDelayBoost(Badges badges, boolean perkBoost) {
            // Every 50 badges, increase lock delay by 1f.
            final int usedBadges = badges.getBadges() / 10;
            final int denominator = perkBoost ? 25 : 50;

            currentLdBoost = usedBadges / denominator;
            return currentLdBoost;
        }

        public SpeedParam getSpeed(GameEngine engine, Badges badges, boolean perkBoost) {
            final double usedProp = (engine.statistics.level - startLv) / (double) (endLv - startLv);
            final int usedLv = (int) Math.floor(usedProp * 10000d);

            final SpeedParam baseParam = SPEED_TABLE.apply(usedLv);
            baseParam.lockDelay += getLockDelayBoost(badges, perkBoost);

            return baseParam;
        }

        @Override
        public String getName() {
            return "INTO THE FIRE";
        }

        @Override
        public GameTextUtilities.TextBlock getSummary() {
            return GameTextUtilities.TextBlock.of(
                GameTextUtilities.TextJustification.LEFT,
                GameTextUtilities.Text.of(getName(), EventReceiver.COLOR_YELLOW),
                GameTextUtilities.Text.of(" (", EventReceiver.COLOR_RED),
                GameTextUtilities.Text.of("+" + currentLdBoost + "F", EventReceiver.COLOR_YELLOW),
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
                    "WILDFIRES IGNITE AND BURN AROUND YOU.",
                    EventReceiver.COLOR_WHITE, 0.75f
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom(
                    "DON'T GET CAUGHT IN THE HEAT OF THE MOMENT.",
                    EventReceiver.COLOR_WHITE, 0.75f
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom(
                    "FIND YOUR STRENGTH TO ESCAPE THIS INFERNO!",
                    EventReceiver.COLOR_WHITE, 0.75f
                )
            );
        }
    }

    // Autumn's gimmicks range from movement, to spooks.
    public static class FallsCall implements HasDescription {
        private int currentDelay;

        public int getFallDelay(Badges badges, boolean perkBoost) {
            // Default delay is 6.
            // Increase every 25 badges.

            final int usedBadges = badges.getBadges();
            final int denominator = perkBoost ? 125 : 250;

            currentDelay = 6 + (usedBadges / denominator);
            return currentDelay;
        }

        @Override
        public String getName() {
            return "FALL'S CALL";
        }

        @Override
        public GameTextUtilities.TextBlock getSummary() {
            return GameTextUtilities.TextBlock.of(
                GameTextUtilities.TextJustification.LEFT,
                GameTextUtilities.Text.of(getName(), EventReceiver.COLOR_ORANGE),
                GameTextUtilities.Text.of(" (", EventReceiver.COLOR_RED),
                GameTextUtilities.Text.of(currentDelay + "F", EventReceiver.COLOR_YELLOW),
                GameTextUtilities.Text.of(" DELAY)", EventReceiver.COLOR_RED)
            );
        }

        @Override
        public GameTextUtilities.TextBlock getDescription() {
            return GameTextUtilities.TextBlock.of(
                GameTextUtilities.TextJustification.LEFT,
                GameTextUtilities.Text.of(getName(), EventReceiver.COLOR_ORANGE),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.blankLine(0.5f),
                GameTextUtilities.Text.custom(
                    "WITHIN THE DIFFICULT ROADS, THE CALL OF THE",
                    EventReceiver.COLOR_WHITE, 0.75f
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom(
                    "AUTUMN SEASON RESONATES HAUNTINGLY. GRAVITY",
                    EventReceiver.COLOR_WHITE, 0.75f
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom(
                    "TWISTS AND TURNS IN UNEXPECTED WAYS.",
                    EventReceiver.COLOR_WHITE, 0.75f
                )
            );
        }
    }

    public static class FlowingWinds implements HasDescription {
        private final Random random;
        private final int seasonStartLv;
        private final int seasonEndLv;

        private double chance; // 0 - 1

        // Set the badge chance manually!
        public FlowingWinds(Random random, int seasonStartLv, int seasonEndLv) {
            this.random = random;
            this.seasonStartLv = seasonStartLv;
            this.seasonEndLv = seasonEndLv;
        }

        public void updateChance(GameEngine engine, Badges badges, boolean perkBoost) {
            final double progress = (engine.statistics.level - seasonStartLv) / (double) (seasonEndLv - seasonStartLv);
            final double baseChance = Interpolation.lerp(0.9925, 1.0, progress);

            final int denominator = perkBoost ? 10 : 20;

            // We don't want the max chance to be 1. That's too annoying.
            chance = Math.pow(baseChance, badges.getBadges() / (double) denominator) * 0.8;
        }

        @Override
        public String getName() {
            return "FLOWING WINDS";
        }

        @Override
        public GameTextUtilities.TextBlock getSummary() {
            return GameTextUtilities.TextBlock.of(
                GameTextUtilities.TextJustification.LEFT,
                GameTextUtilities.Text.of(getName(), EventReceiver.COLOR_ORANGE),
                GameTextUtilities.Text.of(" (", EventReceiver.COLOR_RED),
                GameTextUtilities.Text.of(String.format("%.02f", chance * 100d) + "%", EventReceiver.COLOR_YELLOW),
                GameTextUtilities.Text.of(")", EventReceiver.COLOR_RED)
            );
        }

        @Override
        public GameTextUtilities.TextBlock getDescription() {
            return GameTextUtilities.TextBlock.of(
                GameTextUtilities.TextJustification.LEFT,
                GameTextUtilities.Text.of(getName(), EventReceiver.COLOR_ORANGE),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.blankLine(0.5f),
                GameTextUtilities.Text.custom(
                    "THE WINDS BEGIN TO PICK UP, THE LEAVES ON THE",
                    EventReceiver.COLOR_WHITE, 0.75f
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom(
                    "TREES YIELDING TO ITS STRONG, YET FLOWING NATURE.",
                    EventReceiver.COLOR_WHITE, 0.75f
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom(
                    "BE CAREFUL, AS YOU WILL ALSO NEED TO ADJUST",
                    EventReceiver.COLOR_WHITE, 0.75f
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom(
                    "WHEN THE WINDS BLOW IN UNFAVOURABLE DIRECTIONS.",
                    EventReceiver.COLOR_WHITE, 0.75f
                )
            );
        }

        public void updateNext(GameEngine engine) {
            if (random.nextDouble() > chance) return;

            final Piece piece = HasCustomOnMove.getNextObject(engine, engine.nextPieceCount + engine.ruleopt.nextDisplay);
            if (piece == null) return;

            int newRotation = engine.ruleopt.pieceDefaultDirection[piece.id];
            while (newRotation == engine.ruleopt.pieceDefaultDirection[piece.id]) newRotation = random.nextInt(Piece.DIRECTION_COUNT);

            // This uniquely identifies that this piece has been rotated.
            for (Block blk : piece.block) blk.bonusValue = (newRotation << 1) + 1;

            piece.direction = newRotation;
            piece.setColor(new int[] {
                Block.BLOCK_COLOR_GEM_RAINBOW,
                engine.ruleopt.pieceColor[piece.id],
                engine.ruleopt.pieceColor[piece.id],
                engine.ruleopt.pieceColor[piece.id]
            });

            engine.playSE("rotate");
        }
    }

    public static class GhoulsAfoot implements HasDescription {
        private int bonusGap;

        public GhoulsAfoot(Badges badges, boolean perkBoost) {
            updateBonusGap(badges, perkBoost);
        }

        public void updateBonusGap(Badges badges, boolean perkBoost) {
            final int usedBadges = badges.getBadges();
            final int denominator = perkBoost ? 80 : 160;

            bonusGap = usedBadges / denominator;
        }

        public void renderFlashlight(EventReceiver receiver, GameEngine engine, int playerID, PrimitiveDrawingHook drawing) {
            final int minX = receiver.getFieldDisplayPositionX(engine, playerID) + 4;
            final int maxX = minX + (engine.field.getWidth() * 16);
            final int minY = receiver.getFieldDisplayPositionY(engine, playerID) + 52;
            final int maxY = minY + (engine.field.getHeight() * 16);

            if (engine.stat != GameEngine.STAT_MOVE) {
                drawing.drawRectangle(receiver, minX, minY, maxX - minX, maxY - minY, 0, 0, 0, 255, true);
            } else if (engine.nowPieceObject != null) {
                final int drawLeftX = minX + ((engine.nowPieceX + engine.nowPieceObject.getMinimumBlockX()) * 16) - bonusGap;
                final int drawRightX = minX + ((engine.nowPieceX + engine.nowPieceObject.getMaximumBlockX()) * 16) + 16 + bonusGap;

                if (drawLeftX > minX) drawing.drawRectangle(receiver, minX, minY, drawLeftX - minX, maxY - minY, 0, 0, 0, 255, true);
                if (drawRightX < maxX) drawing.drawRectangle(receiver, drawRightX, minY, maxX - drawRightX, maxY - minY, 0, 0, 0, 255, true);
            }
        }

        @Override
        public String getName() {
            return "GHOULS AFOOT";
        }

        @Override
        public GameTextUtilities.TextBlock getSummary() {
            return GameTextUtilities.TextBlock.of(
                GameTextUtilities.TextJustification.LEFT,
                GameTextUtilities.Text.of(getName(), EventReceiver.COLOR_ORANGE),
                GameTextUtilities.Text.of(" (+", EventReceiver.COLOR_RED),
                GameTextUtilities.Text.of(String.valueOf(bonusGap), EventReceiver.COLOR_YELLOW),
                GameTextUtilities.Text.of(" WIDTH)", EventReceiver.COLOR_RED)
            );
        }

        @Override
        public GameTextUtilities.TextBlock getDescription() {
            return GameTextUtilities.TextBlock.of(
                GameTextUtilities.TextJustification.LEFT,
                GameTextUtilities.Text.of(getName(), EventReceiver.COLOR_ORANGE),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.blankLine(0.5f),
                GameTextUtilities.Text.custom(
                    "ON THE MONTH WHERE THE BOUNDARY BETWEEN",
                    EventReceiver.COLOR_WHITE, 0.75f
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom(
                    "THE LIVING AND THE DEAD BEGINS TO BLUR,",
                    EventReceiver.COLOR_WHITE, 0.75f
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom(
                    "THE DARKNESS ENCROACHES EVER CLOSER.",
                    EventReceiver.COLOR_WHITE, 0.75f
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom(
                    "PRAY YOU'VE BROUGHT A FLASHLIGHT.",
                    EventReceiver.COLOR_WHITE, 0.75f
                )
            );
        }
    }
}
