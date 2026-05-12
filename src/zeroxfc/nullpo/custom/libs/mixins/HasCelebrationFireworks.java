package zeroxfc.nullpo.custom.libs.mixins;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import zeroxfc.nullpo.custom.libs.SoundLoader;
import zeroxfc.nullpo.custom.libs.particles.Fireworks;
import zeroxfc.nullpo.custom.libs.types.tuples.Pair;

/** Helper for adding celebration fireworks to a gamemode. */
public interface HasCelebrationFireworks {
    // Add more streams as needed.
    enum Stream {
        STREAM_1,
        STREAM_2,
        STREAM_3,
        STREAM_4,
        STREAM_5;

        private static final AtomicLong IDs = new AtomicLong(0);

        public static void doToAllStreams(Consumer<? super Stream> action) {
            Arrays.stream(values()).forEach(action);
        }

        private final NavigableMap<Pair<Long, int[]>, Integer> queued = new TreeMap<>(Comparator.comparingLong(p -> p.valL));

        public boolean hasQueuedFireworks() {
            return !queued.isEmpty();
        }

        public void launch(final int[] colour) {
            queued.put(Pair.of(Stream.IDs.getAndIncrement(), colour), 0);
        }
    }

    // Return instances of these to use.
    Fireworks getFireworkEmitter();
    Random getFireworkColourRandomizer();

    // Firework amount controllers.
    int getFireworksLeft();
    void setFireworksLeft(int count);
    void decrementFireworksLeft();

    default void addFireworksLeft(int count) {
        setFireworksLeft(getFireworksLeft() + count);
    }

    // In case you're waiting on fireworks to finish for something.
    default boolean areFireworksWaiting() {
        return getFireworksLeft() > 0 || Arrays.stream(Stream.values()).anyMatch(Stream::hasQueuedFireworks);
    }

    // Parameters
    default int fireworkMinLife() {
        return 45;
    }

    default int fireworkMaxLife() {
        return 75;
    }

    default int fireworkDelay() {
        return 30;
    }

    default int fireworkMinX(EventReceiver receiver, GameEngine engine, int playerID) {
        return receiver.getFieldDisplayPositionX(engine, playerID) - 48;
    }

    default int fireworkMaxX(EventReceiver receiver, GameEngine engine, int playerID) {
        return receiver.getFieldDisplayPositionX(engine, playerID) + (engine.field.getWidth() * 16) + 48;
    }

    default int fireworkMinY(EventReceiver receiver, GameEngine engine, int playerID) {
        return receiver.getFieldDisplayPositionY(engine, playerID) - 48;
    }

    default int fireworkMaxY(EventReceiver receiver, GameEngine engine, int playerID) {
        return receiver.getFieldDisplayPositionY(engine, playerID) + (16 * 7);
    }

    // Return a more useful value if you want to do extra things when a firework explodes.
    default Runnable explodeExtraAction() {
        return () -> { };
    }

    // Setup code.
    default void fireworksSetup() {
        SoundLoader.Sounds.Fireworks.loadAllSounds();
        Stream.doToAllStreams(s -> s.queued.clear());
    }

    default void queueFireworkIf(GameEngine engine, BooleanSupplier when, Stream stream) {
        if (getFireworksLeft() <= 0 || !when.getAsBoolean()) return;
        queueFirework(engine, stream);
    }

    default void queueFirework(GameEngine engine, Stream stream) {
        final Random rnd = getFireworkColourRandomizer();
        if (rnd == null || getFireworksLeft() <= 0) return;

        decrementFireworksLeft();

        stream.launch(Fireworks.DEF_COLOURS[rnd.nextInt(Fireworks.DEF_COLOURS.length)]);
        engine.playSE(SoundLoader.Sounds.Fireworks.LAUNCH.sfx());
    }

    default void explodeFirework(EventReceiver receiver, GameEngine engine, int playerID, final int[] colour) {
        final Fireworks emitter = getFireworkEmitter();

        if (emitter != null) {
            emitter.addNumber(
                1,
                new Object[] {
                    fireworkMinX(receiver, engine, playerID), fireworkMaxX(receiver, engine, playerID),
                    fireworkMinY(receiver, engine, playerID), fireworkMaxY(receiver, engine, playerID),
                    colour[0], colour[1], colour[2], colour[3], colour[4],
                    Fireworks.DEF_MAX_VEL,
                    fireworkMinLife(), fireworkMaxLife()
                }
            );

            if (!emitter.areSoundsEnabled()) {
                engine.playSE(SoundLoader.Sounds.Fireworks.EXPLODE.sfx());
            }
        }
    }

    default void updateLaunchedFireworks(EventReceiver receiver, GameEngine engine, int playerID) {
        final Fireworks emitter = getFireworkEmitter();
        if (emitter != null) emitter.update();

        Stream.doToAllStreams(s -> {
            if (!s.hasQueuedFireworks()) return;

            final Set<Map.Entry<Pair<Long, int[]>, Integer>> entrySet = s.queued.entrySet();
            boolean exploded = false;

            for (Map.Entry<Pair<Long, int[]>, Integer> entry : entrySet) {
                entry.setValue(entry.getValue() + 1);

                if (entry.getValue() >= fireworkDelay()) {
                    explodeFirework(receiver, engine, playerID, entry.getKey().valR);
                    entry.setValue(Integer.MIN_VALUE);

                    explodeExtraAction().run();
                    exploded = true;
                }
            }

            if (exploded) entrySet.removeIf(e -> e.getValue() == Integer.MIN_VALUE);
        });
    }

    default void drawFireworks(EventReceiver receiver) {
        final Fireworks emitter = getFireworkEmitter();
        if (emitter != null) emitter.draw(receiver);
    }
}
