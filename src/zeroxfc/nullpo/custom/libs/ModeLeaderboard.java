package zeroxfc.nullpo.custom.libs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import mu.nu.nullpo.game.play.GameManager;
import zeroxfc.nullpo.custom.libs.types.Order;

// The parameter is a discriminator key.
// The key uniquely identifies a leaderboard of rankingMax entries.
// If your key is made up of multiple parts, it is suggested to put them together into a class that
// puts the parts together as a '.'-separated string, e.g.
//
// class ExampleKey {
//     int a;
//     int b;
//     ...
//     @Override
//     public String toString() {
//         return a + "." + b;
//     }
// }
//
// V must have an associated PropertyCodec.
public class ModeLeaderboard<K, V> {
    private final ModeSettings settings;
    private final boolean ruleDependent;

    private final int currentVersion;
    private final int rankingMax;

    private final Map<K, Leaderboard> leaderboards;
    private final PropertyCodec<V> codec;
    private final Supplier<V> newEntrySupplier;
    private final BiFunction<V, V, Order> orderFunction;

    public ModeLeaderboard(ModeSettings settings, PropertyCodec<V> codec, Supplier<V> defaultValue, Supplier<V> newEntrySupplier, BiFunction<V, V, Order> orderFunction, int currentVersion, int rankingMax, boolean ruleDependent) {
        this(settings, codec.deriveWithNewDefault(defaultValue), newEntrySupplier, orderFunction, currentVersion, rankingMax, ruleDependent);
    }

    public ModeLeaderboard(ModeSettings settings, PropertyCodec<V> codec, Supplier<V> newEntrySupplier, BiFunction<V, V, Order> orderFunction, int currentVersion, int rankingMax, boolean ruleDependent) {
        this.currentVersion = currentVersion;
        this.rankingMax = rankingMax;
        this.ruleDependent = ruleDependent;
        this.settings = settings;
        this.codec = codec;
        this.newEntrySupplier = newEntrySupplier;
        this.orderFunction = orderFunction;

        leaderboards = new LinkedHashMap<>();
    }

    public Leaderboard registerLeaderboard(K key) {
        final Leaderboard leaderboard = new Leaderboard(key);
        leaderboards.put(key, leaderboard);

        return leaderboard;
    }

    public V readLeaderboard(K key, int position) {
        return leaderboards.get(key).getEntry(position);
    }

    public V readPlayerLeaderboard(K key, int position) {
        return leaderboards.get(key).getPlayerEntry(position);
    }

    public int updateLeaderboard(K key) {
        final Leaderboard leaderboard = leaderboards.get(key);

        for (int i = 0; i < rankingMax; ++i) {
            final Order order = leaderboard.queryNewEntryAt(i);
            if (order == Order.GT) {
                leaderboard.insertNewEntryInto(i);
                return i;
            }
        }

        return -1;
    }

    public int updatePlayerLeaderboard(K key) {
        final Leaderboard leaderboard = leaderboards.get(key);

        for (int i = 0; i < rankingMax; ++i) {
            final Order order = leaderboard.queryNewPlayerEntryAt(i);
            if (order == Order.GT) {
                leaderboard.insertNewPlayerEntryInto(i);
                return i;
            }
        }

        return -1;
    }

    public void saveRanking(GameManager owner, String ruleName) {
        for (final Leaderboard leaderboard : leaderboards.values()) {
            leaderboard.saveRanking(owner, ruleName);
        }
    }

    public void loadRanking(GameManager owner, String ruleName) {
        for (final Leaderboard leaderboard : leaderboards.values()) {
            leaderboard.loadRanking(owner, ruleName);
        }
    }

    public void savePlayerRanking(ProfileProperties prop, String ruleName) {
        for (final Leaderboard leaderboard : leaderboards.values()) {
            leaderboard.saveRankingPlayer(prop, ruleName);
        }
    }

    public void loadPlayerRanking(ProfileProperties prop, String ruleName) {
        for (final Leaderboard leaderboard : leaderboards.values()) {
            leaderboard.loadRankingPlayer(prop, ruleName);
        }
    }

    public final class Leaderboard {
        private final K key;
        private final List<V> entries;
        private final List<V> playerEntries;

        public Leaderboard(K key) {
            this.key = key;

            entries = new ArrayList<>(rankingMax + 1);
            for (int i = 0; i < rankingMax; ++i) entries.add(codec.defaultLoadValue());

            playerEntries = new ArrayList<>(rankingMax + 1);
            for (int i = 0; i < rankingMax; ++i) playerEntries.add(codec.defaultLoadValue());
        }

        public Order queryNewEntryAt(int position) {
            return orderFunction.apply(newEntrySupplier.get(), getEntry(position));
        }

        public Order queryNewPlayerEntryAt(int position) {
            return orderFunction.apply(newEntrySupplier.get(), getPlayerEntry(position));
        }

        public void insertNewEntryInto(int position) {
            entries.add(position, newEntrySupplier.get());
            entries.remove(rankingMax);
        }

        public V getEntry(int position) {
            return entries.get(position);
        }

        public void insertNewPlayerEntryInto(int position) {
            playerEntries.add(position, newEntrySupplier.get());
            playerEntries.remove(rankingMax);
        }

        public V getPlayerEntry(int position) {
            return playerEntries.get(position);
        }

        public void saveRanking(GameManager owner, String ruleName) {
            for (int i = 0; i < rankingMax; ++i) {
                codec.save(owner.modeConfig, propPath(ruleName, i), getEntry(i));
            }
        }

        public void loadRanking(GameManager owner, String ruleName) {
            for (int i = 0; i < rankingMax; ++i) {
                entries.set(i, codec.load(owner.modeConfig, propPath(ruleName, i), codec.defaultLoadValue()));
            }
        }

        public void saveRankingPlayer(ProfileProperties prop, String ruleName) {
            if (!prop.isLoggedIn()) return;

            for (int i = 0; i < rankingMax; ++i) {
                codec.savePlayer(prop, propPath(ruleName, i), getPlayerEntry(i));
            }
        }

        public void loadRankingPlayer(ProfileProperties prop, String ruleName) {
            if (!prop.isLoggedIn()) return;

            for (int i = 0; i < rankingMax; ++i) {
                playerEntries.set(i, codec.loadPlayer(prop, propPath(ruleName, i), codec.defaultLoadValue()));
            }
        }

        private String propPath(String ruleName, int position) {
            if (ruleDependent) return settings.propPath("ranking", currentVersion, ruleName, key, position);
            else return settings.propPath("ranking", currentVersion, key, position);
        }
    }
}
