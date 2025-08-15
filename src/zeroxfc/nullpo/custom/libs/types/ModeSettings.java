package zeroxfc.nullpo.custom.libs.types;

import java.util.function.Supplier;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.util.CustomProperties;
import zeroxfc.nullpo.custom.libs.ProfileProperties;

/** Representation of an object that holds the settings that a mode uses. */
public abstract class ModeSettings {
    private final String propRoot;
    public final ProfileProperties playerProperties;

    protected ModeSettings(String propRoot, ProfileProperties playerProperties) {
        this.propRoot = propRoot;
        this.playerProperties = playerProperties;
    }

    // Construct a property path for settings and rankings.
    protected final String propPath(Object... path) {
        final StringBuilder sb = new StringBuilder(propRoot);
        for (Object obj : path) sb.append('.').append(obj.toString());

        return sb.toString();
    }

    public abstract void loadSetting(CustomProperties prop, boolean isReplay);
    public abstract void saveSetting(CustomProperties prop, boolean forReplay);

    public abstract void loadSettingPlayer(ProfileProperties prop);
    public abstract void saveSettingPlayer(ProfileProperties prop);

    public abstract void loadRanking(GameManager owner, String ruleName);
    public abstract void saveRanking(GameManager owner, String ruleName);

    public abstract void loadRankingPlayer(ProfileProperties prop, String ruleName);
    public abstract void saveRankingPlayer(ProfileProperties prop, String ruleName);

    public void commitSettingAndRank(EventReceiver receiver, GameManager owner) {
        receiver.saveModeConfig(owner.modeConfig);
    }

    public void commitPlayerSettingAndRank(ProfileProperties playerProperties) {
        if (!playerProperties.isLoggedIn()) return;
        playerProperties.saveProfileConfig();
    }

    // Ranking order helper.
    protected enum Order {
        LT(-1), EQ(0), GT(1);
        public final int compareValue;

        public static Order fromCompare(int cmp) {
            if (cmp < 0) return LT;
            else if (cmp > 0) return GT;
            else return EQ;
        }

        Order(int compareValue) {
            this.compareValue = compareValue;
        }

        public Order fold(Supplier<Order> other) {
            if (this == EQ) return other.get();
            else return this;
        }
    }
}
