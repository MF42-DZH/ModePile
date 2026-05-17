package zeroxfc.nullpo.custom.modes.objects.seasons;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.IntFunction;
import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.component.SpeedParam;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import zeroxfc.nullpo.custom.libs.CustomResourceHolder;
import zeroxfc.nullpo.custom.libs.FieldManipulation;
import zeroxfc.nullpo.custom.libs.GameTextUtilities;
import zeroxfc.nullpo.custom.libs.Interpolation;
import zeroxfc.nullpo.custom.libs.PrimitiveDrawingHook;
import zeroxfc.nullpo.custom.libs.SoundLoader;
import zeroxfc.nullpo.custom.libs.SpeedTableBuilder;
import zeroxfc.nullpo.custom.libs.mixins.HasCustomMove;
import zeroxfc.nullpo.custom.libs.types.ObjectAlignment;
import zeroxfc.nullpo.custom.libs.types.tuples.IntPair;

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
    // JAN - Icicles (Occasionally, blocks are replaced with icicle blocks that penetrate the stack.)

    // There will also be 4 gimmicks across the credits roll as you pass through the months.

    // SPRING - Rising Earth (Faster random hole garbage with brown blocks (slowed by badges))
    // SUMMER - Conflagration (VERY Fast Speed (~Shirase 3xx-12xx; lock delay increased by badges))
    // AUTUMN - Haunting (Outline Only + Bone Blocks + Black Overlay)
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
            // Every 30 badges will increase the countdown by 1.
            // The default countdown is 4

            countdown = 5 + getScoreMult(badges, perkBoost);
        }

        public int getScoreMult(Badges badges, boolean perkBoost) {
            final int usedBadges = badges.getBadges() / 10;
            final int denominator = perkBoost ? 15 : 30;

            return usedBadges / denominator;
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
            // Every 40 badges increases the countdown by 1.

            final int usedBadges = badges.getBadges() / 10;
            final int denominator = perkBoost ? 20 : 40;

            currentCountdown = 4 + getScoreMult(badges, perkBoost);
        }

        public int getScoreMult(Badges badges, boolean perkBoost) {
            final int usedBadges = badges.getBadges() / 10;
            final int denominator = perkBoost ? 20 : 40;

            return usedBadges / denominator;
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
                currentCountdown >= 10 ? GameTextUtilities.Text.of("(IN ", EventReceiver.COLOR_RED) : GameTextUtilities.Text.of(" (BLOOM IN ", EventReceiver.COLOR_RED),
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
            if (random.nextDouble() > (0.04 * (13 - currentCountdown) / 3d)) return;

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
        private final Random pieceRandom;
        private final Random fieldRandom;
        private final Random soundRandom;

        private final int seasonStartLv;
        private final int seasonEndLv;

        private double chance; // 0 - 1

        // Set the badge chance manually!
        public Dehydration(Random random, int seasonStartLv, int seasonEndLv) {
            this.pieceRandom = random;

            final long reseed = pieceRandom.nextLong();
            this.fieldRandom = new Random(reseed);
            this.soundRandom = new Random(~reseed);

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
            if (pieceRandom.nextDouble() >= chance) return;

            final Piece piece = HasCustomMove.getNextObject(engine, engine.nextPieceCount + engine.ruleopt.nextDisplay);
            if (piece == null) return;

            piece.setAttribute(Block.BLOCK_ATTRIBUTE_BONE, true);

            final int sound = soundRandom.nextInt(3);
            engine.playSE(SoundLoader.Sounds.Seasons.values()[(SoundLoader.Sounds.Seasons.GROUND_CRACKLE_1.ordinal()) + sound].sfx());
        }

        public void updateField(GameEngine engine) {
            if (engine.field == null) return;

            boolean playSound = false;

            for (int y = engine.field.getHighestBlockY(); y < engine.field.getHeight(); ++y) {
                for (int x = 0; x < engine.field.getWidth(); ++x) {
                    if (!engine.field.getBlockEmpty(x, y)) {
                        final Block blk = engine.field.getBlock(x, y);
                        if (!blk.getAttribute(Block.BLOCK_ATTRIBUTE_BONE) && fieldRandom.nextDouble() < (chance / 10d)) {
                            blk.setAttribute(Block.BLOCK_ATTRIBUTE_BONE, true);
                            playSound = true;
                        }
                    }
                }
            }

            if (playSound) {
                final int sound = soundRandom.nextInt(3);
                engine.playSE(SoundLoader.Sounds.Seasons.values()[(SoundLoader.Sounds.Seasons.GROUND_CRACKLE_1.ordinal()) + sound].sfx());
            }
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

        public boolean aboutToReplace() {
            return counter == currentAllowance;
        }

        // Call this right before a piece is spawned (but only if the piece did not come out of hold).
        public boolean replaceQueue(GameEngine engine) {
            // Only counts I-pieces if they're in the last position of the visible queue.
            final Piece current = HasCustomMove.getNextObject(engine, engine.nextPieceCount);

            if (current.id == Piece.PIECE_I && current.block[0].item >= 0) {
                ++counter;
            }

            if (counter > currentAllowance) {
                // Reset counter.
                counter = 0;

                // Skip I-piece.
                engine.nextPieceCount++;

                engine.playSE(SoundLoader.Sounds.Seasons.STEAM.sfx());
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
                aboutToReplace()
                    ? GameTextUtilities.Text.ofMixColor(getName(), EventReceiver.COLOR_YELLOW, 255, 180, 48, 255)
                    : GameTextUtilities.Text.of(getName(), EventReceiver.COLOR_YELLOW),
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
            // Every 60 badges, increase lock delay by 1f.
            final int usedBadges = badges.getBadges() / 10;
            final int denominator = perkBoost ? 30 : 60;

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
            // Increase every 30 badges.

            final int usedBadges = badges.getBadges();
            final int denominator = perkBoost ? 150 : 300;

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
        private final Random soundRandom;

        private final int seasonStartLv;
        private final int seasonEndLv;

        private double chance; // 0 - 1

        // Set the badge chance manually!
        public FlowingWinds(Random random, int seasonStartLv, int seasonEndLv) {
            this.random = random;
            this.soundRandom = new Random(seasonStartLv ^ seasonEndLv);

            this.seasonStartLv = seasonStartLv;
            this.seasonEndLv = seasonEndLv;
        }

        public void updateChance(GameEngine engine, Badges badges, boolean perkBoost) {
            final double progress = (engine.statistics.level - seasonStartLv) / (double) (seasonEndLv - seasonStartLv);
            final double baseChance = Interpolation.lerp(0.9935, 1.0, progress);

            final int denominator = perkBoost ? 10 : 20;

            chance = Math.pow(baseChance, badges.getBadges() / (double) denominator);
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

            final Piece piece = HasCustomMove.getNextObject(engine, engine.nextPieceCount + engine.ruleopt.nextDisplay);
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

            final int sound = soundRandom.nextInt(3);
            engine.playSE(SoundLoader.Sounds.Seasons.values()[(SoundLoader.Sounds.Seasons.WIND_1.ordinal()) + sound].sfx());
        }
    }

    public static class GhoulsAfoot implements HasDescription {
        private int bonusGap;
        private int currentBonusGap;

        public GhoulsAfoot(Badges badges, boolean perkBoost) {
            currentBonusGap = 0;
            updateBonusGap(badges, perkBoost);
        }

        public void updateBonusGap(Badges badges, boolean perkBoost) {
            final int usedBadges = badges.getBadges();
            final int denominator = perkBoost ? 100 : 200;

            bonusGap = usedBadges / denominator;
        }

        // Call in onLast.
        public void updateCurrentBonusGap(GameEngine engine) {
            if (engine.stat != GameEngine.STAT_MOVE || engine.statc[0] <= 1) currentBonusGap = 0;
            else currentBonusGap = Math.min(bonusGap, currentBonusGap + 4);
        }

        public void renderFlashlight(EventReceiver receiver, GameEngine engine, int playerID, PrimitiveDrawingHook drawing, boolean boo) {
            final int minX = receiver.getFieldDisplayPositionX(engine, playerID) + 4;
            final int maxX = minX + (engine.field.getWidth() * 16);
            final int minY = receiver.getFieldDisplayPositionY(engine, playerID) + 52;
            final int maxY = minY + (engine.field.getHeight() * 16);

            if (engine.stat != GameEngine.STAT_MOVE || engine.statc[0] <= 1 || engine.nowPieceObject == null) {
                drawing.drawRectangle(receiver, minX, minY, maxX - minX, maxY - minY, 0, 0, 0, 255, true);

                if (boo) GameTextUtilities.drawAlignedBoundedTextBlock(
                    engine,
                    minX + (engine.field.getWidth() * 8), minY + (engine.field.getHeight() * 8),
                    minX, minY,
                    maxX, maxY,
                    false,
                    GameTextUtilities.TextBlock.of(
                        GameTextUtilities.Text.ofMixColor(
                            "BOO!",
                            EventReceiver.COLOR_WHITE,
                            255, 255, 255, 32
                        )
                    ),
                    ObjectAlignment.MIDDLE_MIDDLE
                );
            } else {
                final int drawLeftX = minX + ((engine.nowPieceX + engine.nowPieceObject.getMinimumBlockX()) * 16) - currentBonusGap;
                final int drawRightX = minX + ((engine.nowPieceX + engine.nowPieceObject.getMaximumBlockX()) * 16) + 16 + currentBonusGap;

                if (drawLeftX > minX) {
                    drawing.drawRectangle(receiver, minX, minY, drawLeftX - minX, maxY - minY, 0, 0, 0, 255, true);

                    if (boo) GameTextUtilities.drawAlignedBoundedTextBlock(
                        engine,
                        minX + (engine.field.getWidth() * 8), minY + (engine.field.getHeight() * 8),
                        minX, minY,
                        drawLeftX, maxY,
                        false,
                        GameTextUtilities.TextBlock.of(
                            GameTextUtilities.Text.ofMixColor(
                                "BOO!",
                                EventReceiver.COLOR_WHITE,
                                255, 255, 255, 32
                            )
                        ),
                        ObjectAlignment.MIDDLE_MIDDLE
                    );
                }

                if (drawRightX < maxX) {
                    drawing.drawRectangle(receiver, drawRightX, minY, maxX - drawRightX, maxY - minY, 0, 0, 0, 255, true);

                    if (boo) GameTextUtilities.drawAlignedBoundedTextBlock(
                        engine,
                        minX + (engine.field.getWidth() * 8), minY + (engine.field.getHeight() * 8),
                        drawRightX, minY,
                        maxX, maxY,
                        false,
                        GameTextUtilities.TextBlock.of(
                            GameTextUtilities.Text.ofMixColor(
                                "BOO!",
                                EventReceiver.COLOR_WHITE,
                                255, 255, 255, 32
                            )
                        ),
                        ObjectAlignment.MIDDLE_MIDDLE
                    );
                }
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
                GameTextUtilities.Text.of(" (", EventReceiver.COLOR_RED),
                GameTextUtilities.Text.of("+" + bonusGap, EventReceiver.COLOR_YELLOW),
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

    // Winter's gimmicks are all about those visuals.
    public static class Whiteout implements HasDescription {
        public static final int ALPHA = 64;

        public static final int SNOW_IDENTIFIER = 0xABCD0000;
        public static final int SNOW_MASK = 0xFFFF0000;

        private static final double BASE_PROPORTION = 0.9995;
        private double proportion;

        public Whiteout(Badges badges, boolean perkBoost) {
            updateProportion(badges, perkBoost);
        }

        public void updateProportion(Badges badges, boolean perkBoost) {
            final int usedBadges = (perkBoost ? badges.getBadges() * 2 : badges.getBadges()) / 8;
            proportion = Math.pow(BASE_PROPORTION, usedBadges);
        }

        public void updateNext(GameEngine engine) {
            Piece piece = HasCustomMove.getNextObject(engine, engine.nextPieceCount + engine.ruleopt.nextDisplay);
            if (piece == null) return;

            piece.setColor(Block.BLOCK_COLOR_GRAY);
            for (Block blk : piece.block) blk.bonusValue = SNOW_IDENTIFIER;

            piece = HasCustomMove.getNextObject(engine, engine.nextPieceCount + engine.ruleopt.nextDisplay + 1);
            if (piece == null) return;

            piece.setColor(Block.BLOCK_COLOR_GRAY);
            for (Block blk : piece.block) blk.bonusValue = SNOW_IDENTIFIER;
        }

        public void drawInnerFog(EventReceiver receiver, GameEngine engine, int playerID, PrimitiveDrawingHook drawing) {
            if (engine.field == null) return;

            final int minX = receiver.getFieldDisplayPositionX(engine, playerID) + 4;
            final int minY = receiver.getFieldDisplayPositionY(engine, playerID) + 52;

            final int maxX = minX + (16 * engine.field.getWidth());
            final int maxY = minY + (16 * engine.field.getHeight());

            final int baseSizeX = maxX - minX;

            for (int i = 0; i < 8; ++i) {
                final double loopProp = Math.min(1.0, 1.5 * proportion * Math.pow(3d / 4d, i));

                // Field fog.
                final int fSizeX = (int) (baseSizeX * loopProp * 0.5);

                drawing.drawRectangle(
                    receiver,
                    minX, minY,
                    fSizeX, maxY - minY,
                    255, 255, 255, ALPHA,
                    true
                );

                drawing.drawRectangle(
                    receiver,
                    maxX - fSizeX, minY,
                    fSizeX, maxY - minY,
                    255, 255, 255, ALPHA,
                    true
                );
            }
        }

        public void drawOuterFog(EventReceiver receiver, PrimitiveDrawingHook drawing) {
            for (int i = 0; i < 8; ++i) {
                final double loopProp = Math.min(1.0, 1.5 * proportion * Math.pow(3d / 4d, i));

                // Background fog.
                final int outSizeX = (int) (320 * loopProp);

                drawing.drawRectangle(
                    receiver,
                    0, 0,
                    outSizeX, 480,
                    255, 255, 255, ALPHA,
                    true
                );

                drawing.drawRectangle(
                    receiver,
                    640 - outSizeX, 0,
                    outSizeX, 480,
                    255, 255, 255, ALPHA,
                    true
                );
            }
        }

        @Override
        public String getName() {
            return "WHITEOUT";
        }

        @Override
        public GameTextUtilities.TextBlock getSummary() {
            return GameTextUtilities.TextBlock.of(
                GameTextUtilities.TextJustification.LEFT,
                GameTextUtilities.Text.of(getName(), EventReceiver.COLOR_CYAN),
                GameTextUtilities.Text.of(" (", EventReceiver.COLOR_RED),
                GameTextUtilities.Text.of(String.format("%.02f", (1.0 - proportion) * 100d) + "%", EventReceiver.COLOR_YELLOW),
                GameTextUtilities.Text.of(" VIS.)", EventReceiver.COLOR_RED)
            );
        }

        @Override
        public GameTextUtilities.TextBlock getDescription() {
            return GameTextUtilities.TextBlock.of(
                GameTextUtilities.TextJustification.LEFT,
                GameTextUtilities.Text.of(getName(), EventReceiver.COLOR_CYAN),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.blankLine(0.5f),
                GameTextUtilities.Text.custom(
                    "THE DARK CLOUDS COVER THE SKY, AND SNOW BEGINS",
                    EventReceiver.COLOR_WHITE, 0.75f
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom(
                    "TO FALL. IT INTENSIFIES QUICKLY, FASTER THAN",
                    EventReceiver.COLOR_WHITE, 0.75f
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom(
                    "SENSE WOULD EXPECT. YOUR VISIBILITY IS SEVERELY",
                    EventReceiver.COLOR_WHITE, 0.75f
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom(
                    "REDUCED. WATCH YOUR STEP.",
                    EventReceiver.COLOR_WHITE, 0.75f
                )
            );
        }
    }

    public static class SnowMounds implements HasDescription {
        private int currentCounter;
        private int currentTickTime;
        private int height;

        public SnowMounds(Badges badges, boolean perkBoost) {
            height = 0;
            currentCounter = 0;

            setTickTime(badges, perkBoost);
        }

        public void setTickTime(Badges badges, boolean perkBoost) {
            final int usedBadges = badges.getBadges() / 10;
            currentTickTime = 48 + usedBadges / (perkBoost ? 5 : 10);
        }

        public boolean isYInSnow(GameEngine engine, int y) {
            final int topY = engine.field.getHeightWithoutHurryupFloor() - height;
            return y >= topY;
        }

        public void reduceHeight(int lines) {
            if (lines > height) {
                height = 0;
                currentCounter = 0;
            } else {
                height = Math.max(0, height - lines);
            }
        }

        public void update(GameEngine engine) {
            if (++currentCounter > currentTickTime) {
                currentCounter = 0;
                height = Math.min(engine.field.getHeight(), height + 1);
            }
        }

        public void draw(PrimitiveDrawingHook drawing, EventReceiver receiver, GameEngine engine, int playerID) {
            final int baseX = receiver.getFieldDisplayPositionX(engine, playerID) + 4;
            final int baseY = receiver.getFieldDisplayPositionY(engine, playerID) + 52;
            final int maxAlpha = 210;

            drawing.drawRectangle(
                receiver,
                baseX, baseY + (engine.field.getHeight() * 16) - (height * 16),
                engine.field.getWidth() * 16, (height * 16),
                255, 255, 255, maxAlpha,
                true
            );

            if (height >= engine.field.getHeight()) return;

            final double proportion = currentCounter / (double) currentTickTime;
            final int partialH = (int) Math.floor(16.0 * proportion);

            drawing.drawRectangle(
                receiver,
                baseX, baseY + (engine.field.getHeight() * 16) - (height * 16) - partialH,
                engine.field.getWidth() * 16, partialH,
                255, 255, 255, Interpolation.lerp(0, maxAlpha, proportion),
                true
            );
        }

        @Override
        public String getName() {
            return "SNOW MOUNDS";
        }

        @Override
        public GameTextUtilities.TextBlock getSummary() {
            return GameTextUtilities.TextBlock.of(
                GameTextUtilities.TextJustification.LEFT,
                GameTextUtilities.Text.of(getName(), EventReceiver.COLOR_CYAN),
                GameTextUtilities.Text.of(" (", EventReceiver.COLOR_RED),
                GameTextUtilities.Text.of(currentTickTime + "F", EventReceiver.COLOR_YELLOW),
                GameTextUtilities.Text.of(" FILL)", EventReceiver.COLOR_RED)
            );
        }

        @Override
        public GameTextUtilities.TextBlock getDescription() {
            return GameTextUtilities.TextBlock.of(
                GameTextUtilities.TextJustification.LEFT,
                GameTextUtilities.Text.of(getName(), EventReceiver.COLOR_CYAN),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.blankLine(0.5f),
                GameTextUtilities.Text.custom(
                    "THE INTENSIFYING SNOWSTORM CREATES LARGE MOUNDS",
                    EventReceiver.COLOR_WHITE, 0.75f
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom(
                    "OF FRESH SNOW. CLEAR THEM AWAY BEFORE YOU GET",
                    EventReceiver.COLOR_WHITE, 0.75f
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom(
                    "SNOWED IN PERMANENTLY.",
                    EventReceiver.COLOR_WHITE, 0.75f
                )
            );
        }
    }

    public static class Icicles implements HasDescription {
        public static final int ICICLE_IDENTIFIER = 0x00002738;
        public static final int ICICLE_MASK = 0x0000FFFF;

        private final Random random;
        private final int seasonStartLv;
        private final int seasonEndLv;

        private double chance; // 0 - 1

        public Icicles(Random random, int seasonStartLv, int seasonEndLv) {
            this.random = random;
            this.seasonStartLv = seasonStartLv;
            this.seasonEndLv = seasonEndLv;
        }

        public void updateChance(GameEngine engine, Badges badges, boolean perkBoost) {
            // 100% -> 10%, delayed by badges.
            final double progress = (engine.statistics.level - seasonStartLv) / (double) (seasonEndLv - seasonStartLv);
            final double baseChance = Interpolation.lerp(0.99, 1.0, progress);

            final int denominator = perkBoost ? 15 : 30;

            chance = 1.0 - (0.9 * Math.pow(baseChance, badges.getBadges() / (double) denominator));
        }

        public void updateNext(GameEngine engine) {
            if (random.nextDouble() > chance) return;

            final Piece piece = HasCustomMove.getNextObject(engine, engine.nextPieceCount + engine.ruleopt.nextDisplay);
            if (piece == null) return;

            for (Block blk : piece.block) {
                blk.bonusValue |= ICICLE_IDENTIFIER;
                blk.hard = Integer.MAX_VALUE;
            }

            piece.setColor(Block.BLOCK_COLOR_GEM_CYAN);
        }

        public void updateField(GameEngine engine) {
            for (int y = -engine.field.getHiddenHeight(); y < engine.field.getHeightWithoutHurryupFloor(); ++y) {
                for (int x = 0; x < engine.field.getWidth(); ++x) {
                    final Block blk = engine.field.getBlock(x, y);
                    if (blk == null || blk.isEmpty() || blk.hard <= 0) continue;
                    blk.hard = Integer.MAX_VALUE;
                }
            }
        }

        public static void fragmentPieceAndDrillDown(GameEngine engine) {
            final Set<IntPair> filled = new HashSet<>();

            for (int i = 0; i < engine.nowPieceObject.getMaxBlock(); ++i) {
                // First, we get the current position of the block.
                final int currentX = engine.nowPieceX + engine.nowPieceObject.dataX[engine.nowPieceObject.direction][i];
                final int currentY = engine.nowPieceY + engine.nowPieceObject.dataY[engine.nowPieceObject.direction][i];

                // Find the lowest hole, if it exists, and shove the block in there.
                for (int y = engine.field.getHeightWithoutHurryupFloor() - 1; y >= -engine.field.getHiddenHeight(); --y) {
                    if (!filled.contains(IntPair.of(currentX, y)) && engine.field.getBlockEmpty(currentX, y)) {
                        engine.nowPieceObject.dataY[engine.nowPieceObject.direction][i] = y - engine.nowPieceY;
                        filled.add(IntPair.of(currentX, y));

                        break;
                    }
                }
            }

            engine.playSE(SoundLoader.Sounds.Seasons.ICICLE.sfx());
        }

        @Override
        public String getName() {
            return "ICICLES";
        }

        @Override
        public GameTextUtilities.TextBlock getSummary() {
            return GameTextUtilities.TextBlock.of(
                GameTextUtilities.TextJustification.LEFT,
                GameTextUtilities.Text.of("ICICLES", EventReceiver.COLOR_CYAN),
                GameTextUtilities.Text.of(" (", EventReceiver.COLOR_RED),
                GameTextUtilities.Text.of(String.format("%.02f", chance * 100d) + "%", EventReceiver.COLOR_YELLOW),
                GameTextUtilities.Text.of(")", EventReceiver.COLOR_RED)
            );
        }

        @Override
        public GameTextUtilities.TextBlock getDescription() {
            return GameTextUtilities.TextBlock.of(
                GameTextUtilities.TextJustification.LEFT,
                GameTextUtilities.Text.of(getName(), EventReceiver.COLOR_CYAN),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.blankLine(0.5f),
                GameTextUtilities.Text.custom(
                    "THE AIR BECOMES FROSTBITTEN, AS ALL HEAT",
                    EventReceiver.COLOR_WHITE, 0.75f
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom(
                    "SEEMS TO LEAVE YOUR SURROUNDINGS, PULLED AWAY",
                    EventReceiver.COLOR_WHITE, 0.75f
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom(
                    "BY THIS WINTER'S UNRELENTING COLD. THE ICE IS",
                    EventReceiver.COLOR_WHITE, 0.75f
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom(
                    "TOUGH, BUT BRITTLE.",
                    EventReceiver.COLOR_WHITE, 0.75f
                )
            );
        }
    }

    // Spring's Roll Gimmick - Now Random
    public static class RisingEarth implements HasDescription {
        private final Random random;

        private int counter;
        private int countdown;

        public RisingEarth(Random random, Badges badges, boolean perkBoost) {
            setCountdown(badges, perkBoost);
            this.random = random;
        }

        @Override
        public String getName() {
            return "RISING EARTH";
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
                    "A RUMBLING IS HEARD BENEATH YOUR",
                    EventReceiver.COLOR_WHITE, 0.75f
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom(
                    "FEET. THE EARTH IS RISING, DON'T",
                    EventReceiver.COLOR_WHITE, 0.75f
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom(
                    "LET IT BURY YOU ALIVE.",
                    EventReceiver.COLOR_WHITE, 0.75f
                )
            );
        }

        public void setCountdown(Badges badges, boolean perkBoost) {
            // Every 75 badges will increase the countdown by 1.

            final int usedBadges = badges.getBadges();
            final int denominator = perkBoost ? 375 : 750;

            countdown = usedBadges / denominator;
        }

        // Call on first frame of move.
        public void update(GameEngine engine) {
            ++counter;

            if (counter >= countdown) {
                counter = 0;

                engine.field.addSingleHoleGarbage(
                    random.nextInt(engine.field.getWidth()),
                    Block.BLOCK_COLOR_GEM_ORANGE,
                    engine.getSkin(),
                    1
                );

                for (int x = 0; x < engine.field.getWidth(); ++x) {
                    engine.field.getBlock(x, engine.field.getHeightWithoutHurryupFloor() - 1).pieceNum = engine.statistics.time;
                }

                engine.playSE("garbage");
            }
        }
    }

    // Summer's Roll Gimmick - Much Faster
    public static class Conflagration implements HasDescription {
        private static final IntFunction<SpeedParam> SPEED_TABLE = SpeedTableBuilder.createNew()
            .addTerminalGravity(-1, 256)
            .addTerminalARE(6)
            .addLineARE(6, 2000)
            .addTerminalLineARE(5)
            .addDAS(8, 2000)
            .addTerminalDAS(6)
            .addLockDelay(15, 2000)
            .addLockDelay(13, 4000)
            .addLockDelay(12, 6000)
            .addLockDelay(10, 8000)
            .addTerminalLockDelay(8)
            .addLineDelay(4, 2000)
            .addTerminalLineDelay(3)
            .buildSpeedTable();

        private final int startLv;
        private final int endLv;
        private int currentLdBoost;

        public Conflagration(int startLv, int endLv) {
            this.startLv = startLv;
            this.endLv = endLv;
        }

        private int getLockDelayBoost(Badges badges, boolean perkBoost) {
            // Every 80 badges, increase lock delay by 1f.
            final int usedBadges = badges.getBadges() / 10;
            final int denominator = perkBoost ? 40 : 80;

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
            return "CONFLAGRATION";
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
                    "ANGRY FIRES ROAR TO LIFE AROUND",
                    EventReceiver.COLOR_WHITE, 0.75f
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom(
                    "YOU. ESCAPE AS FAST AS YOU CAN",
                    EventReceiver.COLOR_WHITE, 0.75f
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom(
                    "BEFORE IT CONSUMES YOUR VERY",
                    EventReceiver.COLOR_WHITE, 0.75f
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom(
                    "BEING WHOLE.",
                    EventReceiver.COLOR_WHITE, 0.75f
                )
            );
        }
    }

    // Autumn Roll Gimmick - Pure Visuals
    public static class Haunting implements HasDescription {
        private static final int DEFAULT_RADIUS = 24;
        private static final double MULT = 0.99925;
        private static final double CD_MAX = 2;

        private int currentRadius;
        private int countdown;
        private final Random random;
        private final LinkedList<Ghost> ghosts = new LinkedList<>();

        private class Ghost {
            private int posX;
            private final int posY;
            private final int speed;

            public Ghost(int posX, int posY, int speed) {
                this.posX = posX;
                this.posY = posY;
                this.speed = speed;
            }

            public boolean update(EventReceiver receiver, GameEngine engine, int playerID) {
                if (engine.field == null) return true;

                final int minX = receiver.getFieldDisplayPositionX(engine, playerID) + 4;
                final int maxX = minX + (engine.field.getWidth() * 16) + 16;

                posX += speed;

                if (speed < 0) {
                    return posX < (minX - currentRadius);
                } else {
                    return posX > (maxX + currentRadius);
                }
            }
        }

        public Haunting(Random random, Badges badges, boolean perkBoost) {
            this.countdown = 0;
            this.random = random;

            setCurrentRadius(badges, perkBoost);
        }

        public void setCurrentRadius(Badges badges, boolean perkBoost) {
            currentRadius = (int) Math.ceil(DEFAULT_RADIUS * Math.pow(MULT, badges.getBadges() / (perkBoost ? 5d : 10d)));
        }

        public void update(EventReceiver receiver, GameEngine engine, int playerID) {
            final int minX = receiver.getFieldDisplayPositionX(engine, playerID) + 4;
            final int maxX = minX + (engine.field.getWidth() * 16) + 16;
            final int minY = receiver.getFieldDisplayPositionY(engine, playerID) + 52 + currentRadius;
            final int maxY = minY + (engine.field.getHeight() * 16) - currentRadius;

            if (++countdown >= CD_MAX) {
                countdown = 0;
                ghosts.addFirst(
                    new Ghost(
                        random.nextInt(maxX - minX + 1) + minX,
                        random.nextInt(maxY - minY + 1) + minY,
                        random.nextDouble() < 0.5 ? (random.nextInt(6) + 1) : -(random.nextInt(6) + 1)
                    )
                );
            }

            ghosts.removeIf(g -> g.update(receiver, engine, playerID));
        }

        public void draw(PrimitiveDrawingHook drawing, EventReceiver receiver) {
            final CustomResourceHolder.Runtime runtime = CustomResourceHolder.getCurrentNullpominoRuntime();

            for (Ghost ghost : ghosts) {
                if (runtime == CustomResourceHolder.Runtime.SDL) {
                    drawing.drawRectangle(
                        receiver,
                        ghost.posX - currentRadius, ghost.posY - currentRadius,
                        currentRadius * 2, currentRadius * 2,
                        255, 255, 255, 200,
                        true
                    );
                } else if (runtime != CustomResourceHolder.Runtime.UNKNOWN) {
                    drawing.drawOval(
                        receiver,
                        ghost.posX - currentRadius, ghost.posY - currentRadius,
                        currentRadius * 2, currentRadius * 2,
                        255, 255, 255, 200,
                        true
                    );
                }
            }
        }

        @Override
        public String getName() {
            return "HAUNTING";
        }

        @Override
        public GameTextUtilities.TextBlock getSummary() {
            return GameTextUtilities.TextBlock.of(
                GameTextUtilities.TextJustification.LEFT,
                GameTextUtilities.Text.of(getName(), EventReceiver.COLOR_ORANGE),
                GameTextUtilities.Text.of(" (", EventReceiver.COLOR_RED),
                GameTextUtilities.Text.of(String.valueOf(currentRadius), EventReceiver.COLOR_YELLOW),
                GameTextUtilities.Text.of(" RADIUS)", EventReceiver.COLOR_RED)
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
                    "MALEVOLENT SPIRITS ENCIRCLE YOU,",
                    EventReceiver.COLOR_WHITE, 0.75f
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom(
                    "TRAPPING YOU WITHIN. DISPEL",
                    EventReceiver.COLOR_WHITE, 0.75f
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom(
                    "THEIR NEGATIVE ESSENCE BEFORE",
                    EventReceiver.COLOR_WHITE, 0.75f
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom(
                    "THEY SEIZE YOUR SOUL.",
                    EventReceiver.COLOR_WHITE, 0.75f
                )
            );
        }
    }

    // Winter Roll Gimmick - Familiar
    public static class AbsoluteZero implements HasDescription {
        public static int ZERO_IDENTIFIER = 0x0000DCBA;
        public static int ZERO_MASK = 0x0000FFFF;

        private final SeasonPerk perk;
        private int countdownMax;

        public AbsoluteZero(SeasonPerk perk, Badges badges, boolean perkBoost) {
            this.perk = perk;
            setCountdownMax(badges, perkBoost);
        }

        public void setCountdownMax(Badges badges, boolean perkBoost) {
            countdownMax = badges.getBadges() / (perkBoost ? 20 : 40);

            if (perk == SeasonPerk.SUMMER_PASSIVE) countdownMax += 15;
            else if (perk == SeasonPerk.SUMMER_ACTIVE) countdownMax += 30;
        }

        public void updateField(GameEngine engine) {
            if (engine.field == null) return;

            boolean playSound = false;

            for (int y = engine.field.getHighestBlockY(); y < engine.field.getHeight(); ++y) {
                for (int x = 0; x < engine.field.getWidth(); ++x) {
                    if (!engine.field.getBlockEmpty(x, y)) {
                        final Block blk = engine.field.getBlock(x, y);
                        if (((blk.bonusValue & ZERO_MASK) == ZERO_IDENTIFIER) && ++blk.countdown > countdownMax && blk.hard == 0) {
                            blk.countdown = 0;
                            blk.hard = Integer.MAX_VALUE;
                            blk.color = Block.BLOCK_COLOR_GRAY;

                            playSound = true;
                        }
                    }
                }
            }

            if (playSound) {
                engine.playSE(SoundLoader.Sounds.Seasons.ZERO_FREEZE.sfx());
            }
        }

        @Override
        public String getName() {
            return "ABSOLUTE ZERO";
        }

        @Override
        public GameTextUtilities.TextBlock getSummary() {
            return GameTextUtilities.TextBlock.of(
                GameTextUtilities.TextJustification.LEFT,
                GameTextUtilities.Text.of("ABS. ZERO", EventReceiver.COLOR_CYAN),
                GameTextUtilities.Text.of(" (", EventReceiver.COLOR_RED),
                GameTextUtilities.Text.of(countdownMax + "F", EventReceiver.COLOR_YELLOW),
                GameTextUtilities.Text.of(" DELAY)", EventReceiver.COLOR_RED)
            );
        }

        @Override
        public GameTextUtilities.TextBlock getDescription() {
            return GameTextUtilities.TextBlock.of(
                GameTextUtilities.TextJustification.LEFT,
                GameTextUtilities.Text.of(getName(), EventReceiver.COLOR_CYAN),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.blankLine(0.5f),
                GameTextUtilities.Text.custom(
                    "THE COLD RETURNS, STEALING ALL",
                    EventReceiver.COLOR_WHITE, 0.75f
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom(
                    "ENERGY FROM YOUR SURROUNDINGS.",
                    EventReceiver.COLOR_WHITE, 0.75f
                ),
                GameTextUtilities.Text.newLine(),
                GameTextUtilities.Text.custom(
                    "DO NOT LET IT TRAP YOU HERE.",
                    EventReceiver.COLOR_WHITE, 0.75f
                )
            );
        }
    }
}
