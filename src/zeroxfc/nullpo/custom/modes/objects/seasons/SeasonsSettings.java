package zeroxfc.nullpo.custom.modes.objects.seasons;

import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.util.CustomProperties;
import zeroxfc.nullpo.custom.libs.ProfileProperties;
import zeroxfc.nullpo.custom.libs.types.ModeSettings;

public class SeasonsSettings extends ModeSettings {
    public static final int RANKING_MAX = 5;

    private static final String PROP_ROOT = "seasons";

    private final String versionProp = propPath("version");
    private final int currentVersion;
    public int version;

    private final String fullGhostProp = propPath("fullGhost");
    public boolean fullGhost;

    private final String perkProp = propPath("perk");
    public SeasonPerk perk;

    private final String spinTypeProp = propPath("spinType");
    public int spinType;

    private final String playerNameProp = propPath("playerName");
    public String playerName;

    // Only available for logged-in players.
    public final String hasCompletedGameProp = propPath("hasCompletedGame");
    public boolean hasCompletedGame;

    public SeasonsSettings(int currentVersion, ProfileProperties playerProperties) {
        super(PROP_ROOT, playerProperties);

        this.currentVersion = currentVersion;
        this.hasCompletedGame = false;
    }

    @Override
    public void loadSetting(CustomProperties prop, boolean isReplay) {
        perk = SeasonPerk.values()[prop.getProperty(perkProp, SeasonPerk.SPRING_PASSIVE.ordinal())];
        fullGhost = prop.getProperty(fullGhostProp, false);
        spinType = prop.getProperty(spinTypeProp, GameEngine.SPINTYPE_4POINT);

        // Version props are not saved on the player.
        version = isReplay ? prop.getProperty(versionProp, 0) : currentVersion;
        playerName = isReplay ? prop.getProperty(playerNameProp, "") : "";
    }

    @Override
    public void saveSetting(CustomProperties prop, boolean forReplay) {
        prop.setProperty(perkProp, perk.ordinal());
        prop.setProperty(fullGhostProp, fullGhost);
        prop.getProperty(spinTypeProp, spinType);

        // Version props are not saved on the player.
        prop.setProperty(versionProp, currentVersion);
        if (forReplay && playerProperties.isLoggedIn()) prop.setProperty(playerNameProp, playerName);
    }

    @Override
    public void loadSettingPlayer(ProfileProperties prop) {
        if (!prop.isLoggedIn()) return;

        perk = SeasonPerk.values()[prop.getProperty(perkProp, SeasonPerk.SPRING_PASSIVE.ordinal())];
        fullGhost = prop.getProperty(fullGhostProp, false);
        spinType = prop.getProperty(spinTypeProp, GameEngine.SPINTYPE_4POINT);

        hasCompletedGame = prop.getProperty(hasCompletedGameProp, false);
    }

    @Override
    public void saveSettingPlayer(ProfileProperties prop) {
        if (!prop.isLoggedIn()) return;

        prop.setProperty(perkProp, perk.ordinal());
        prop.setProperty(fullGhostProp, fullGhost);
        prop.getProperty(spinTypeProp, spinType);

        prop.setProperty(hasCompletedGameProp, hasCompletedGame);
    }

    @Override
    public void loadRanking(CustomProperties prop, String ruleName) {

    }

    @Override
    public void saveRanking(CustomProperties prop, String ruleName) {

    }

    @Override
    public void loadRankingPlayer(ProfileProperties prop, String ruleName) {
        if (!prop.isLoggedIn()) return;
    }

    @Override
    public void saveRankingPlayer(ProfileProperties prop, String ruleName) {
        if (!prop.isLoggedIn()) return;
    }
}
