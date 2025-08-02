package zeroxfc.nullpo.custom.libs.types;

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

    public abstract void loadRanking(CustomProperties prop, String ruleName);
    public abstract void saveRanking(CustomProperties prop, String ruleName);

    public abstract void loadRankingPlayer(ProfileProperties prop, String ruleName);
    public abstract void saveRankingPlayer(ProfileProperties prop, String ruleName);
}
