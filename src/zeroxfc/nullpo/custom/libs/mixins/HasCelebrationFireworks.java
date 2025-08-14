package zeroxfc.nullpo.custom.libs.mixins;

import java.util.Comparator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import zeroxfc.nullpo.custom.libs.SoundLoader;
import zeroxfc.nullpo.custom.libs.particles.Fireworks;
import zeroxfc.nullpo.custom.libs.types.tuples.Pair;

/** Helper for adding celebration fireworks to a gamemode. */
public interface HasCelebrationFireworks {
    // This is fine, right?
    NavigableMap<Pair<Integer, int[]>, Integer> LAUNCHED = new TreeMap<>(Comparator.comparingInt(p -> p.valL));
    AtomicInteger IDs = new AtomicInteger(0);

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
        return getFireworksLeft() > 0 || !LAUNCHED.isEmpty();
    }

    // Parameters
    default int fireworkMinLife() {
        return 45;
    }

    default int fireworkMaxLife() {
        return 75;
    }

    // Setup code.
    default void fireworksSetup() {
        SoundLoader.Sounds.Fireworks.loadAllSounds();

        setFireworksLeft(0);
        LAUNCHED.clear();
    }

    default void queueFireworkIf(GameEngine engine, BooleanSupplier when) {
        if (!when.getAsBoolean() || getFireworksLeft() <= 0) return;
        queueFirework(engine);
    }

    default void queueFirework(GameEngine engine) {
        final Random rnd = getFireworkColourRandomizer();
        if (getFireworksLeft() <= 0 || rnd == null) return;

        decrementFireworksLeft();

        final int[] colour = Fireworks.DEF_COLOURS[rnd.nextInt(Fireworks.DEF_COLOURS.length)];
        LAUNCHED.put(Pair.of(IDs.getAndIncrement(), colour), 0);

        engine.playSE(SoundLoader.Sounds.Fireworks.LAUNCH.sfx());
    }

    default void explodeFirework(EventReceiver receiver, GameEngine engine, int playerID, final int[] colour) {
        final Fireworks emitter = getFireworkEmitter();

        if (emitter != null) {
            int minX = receiver.getFieldDisplayPositionX(engine, playerID) - 48;
            int maxX = receiver.getFieldDisplayPositionX(engine, playerID) + (engine.field.getWidth() * 16) + 48;
            int minY = receiver.getFieldDisplayPositionY(engine, playerID) - 48;
            int maxY = receiver.getFieldDisplayPositionY(engine, playerID) + (16 * 7);

            emitter.addNumber(
                1,
                new Object[] {
                    minX, maxX, minY, maxY,
                    colour[0], colour[1], colour[2], colour[3], colour[4],
                    Fireworks.DEF_MAX_VEL,
                    fireworkMinLife(), fireworkMaxLife()
                }
            );

            engine.playSE(SoundLoader.Sounds.Fireworks.EXPLODE.sfx());
        }
    }

    default void updateLaunchedFireworks(EventReceiver receiver, GameEngine engine, int playerID) {
        final Fireworks emitter = getFireworkEmitter();
        if (emitter != null) emitter.update();

        final Set<Map.Entry<Pair<Integer, int[]>, Integer>> entrySet = LAUNCHED.entrySet();

        for (Map.Entry<Pair<Integer, int[]>, Integer> entry : entrySet) {
            entry.setValue(entry.getValue() + 1);

            if (entry.getValue() >= 30) {
                explodeFirework(receiver, engine, playerID, entry.getKey().valR);
                entry.setValue(Integer.MIN_VALUE);
            }
        }

        entrySet.removeIf(e -> e.getValue() == Integer.MIN_VALUE);
    }

    default void drawFireworks(EventReceiver receiver) {
        final Fireworks emitter = getFireworkEmitter();
        if (emitter != null) emitter.draw(receiver);
    }
}
