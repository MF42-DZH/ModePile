package zeroxfc.nullpo.custom.modes.objects.seasons;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.util.CustomProperties;
import mu.nu.nullpo.util.GeneralUtil;
import zeroxfc.nullpo.custom.libs.MenuBuilder;
import zeroxfc.nullpo.custom.libs.ModeLeaderboard;
import zeroxfc.nullpo.custom.libs.ProfileProperties;
import zeroxfc.nullpo.custom.libs.ModeSettings;
import zeroxfc.nullpo.custom.libs.PropertyCodec;
import zeroxfc.nullpo.custom.libs.types.Order;

public class SeasonsSettings extends ModeSettings {
    public static final int RANKING_MAX = 5;
    private static final String PROP_ROOT = "seasons";

    private final int currentVersion;
    public int version;
    private final String versionProp = propPath("version");

    @ModeSettings.PropertyPath(path = "fullGhost")
    @ModeSettings.DefaultValue(booleanValue = false)
    @ModeSettings.GlobalProperty
    @ModeSettings.PlayerProperty
    @MenuBuilder.SettingItem(id = 1, header = "FULL GHOST", headerColour = EventReceiver.COLOR_BLUE)
    public boolean fullGhost;

    @MenuBuilder.SettingChanger(id = 1)
    public void changeFullGhost(int ignored) {
        fullGhost = !fullGhost;
    }

    @MenuBuilder.SettingPrinter(id = 1)
    public String printFullGhost() {
        return GeneralUtil.getONorOFF(fullGhost);
    }

    @ModeSettings.PropertyPath(path = "perk")
    @ModeSettings.DefaultValue(intValue = 1)
    @ModeSettings.GlobalProperty
    @ModeSettings.PlayerProperty
    public int perkOrdinal;

    @MenuBuilder.SettingItem(id = 0, header = "PERK", headerColour = EventReceiver.COLOR_YELLOW)
    public SeasonPerk perk;

    @MenuBuilder.SettingChanger(id = 0)
    public void changePerk(int change) {
        int selectedPerk = perk.ordinal() + change;

        if (!hasCompletedGame) {
            if (selectedPerk < 1) selectedPerk = SeasonPerk.values().length - 1;
            else if (selectedPerk >= SeasonPerk.values().length) selectedPerk = 1;
        } else {
            if (selectedPerk < 0) selectedPerk = SeasonPerk.values().length - 1;
            else if (selectedPerk >= SeasonPerk.values().length) selectedPerk = 0;
        }

        perk = SeasonPerk.values()[selectedPerk];
    }

    @MenuBuilder.SettingPrinter(id = 0)
    public String printPerk() {
        String perkString = perk.name();
        if (perkString.contains("_")) {
            final String[] split = perkString.split("_");
            perkString = String.format("%s (%s)", split[0], split[1].charAt(0));
        }

        return perkString;
    }

    @ModeSettings.PropertyPath(path = "spinType")
    @ModeSettings.DefaultValue(intValue = GameEngine.SPINTYPE_4POINT)
    @ModeSettings.GlobalProperty
    @ModeSettings.PlayerProperty
    @MenuBuilder.SettingItem(id = 2, header = "SPIN TYPE", headerColour = EventReceiver.COLOR_GREEN)
    public int spinType;

    @MenuBuilder.SettingChanger(id = 2)
    public void changeSpinType(int change) {
        spinType += change;

        if (spinType < GameEngine.SPINTYPE_4POINT) spinType = GameEngine.SPINTYPE_IMMOBILE;
        else if (spinType > GameEngine.SPINTYPE_IMMOBILE) spinType = GameEngine.SPINTYPE_4POINT;
    }

    @MenuBuilder.SettingPrinter(id = 2)
    public String printSpinType() {
        String spinString = "DISABLED";
        if (spinType == GameEngine.SPINTYPE_4POINT) spinString = "4-POINT";
        if (spinType == GameEngine.SPINTYPE_IMMOBILE) spinString = "IMMOBILE";

        return spinString;
    }

    @ModeSettings.PropertyPath(path = "sparks")
    @ModeSettings.DefaultValue(booleanValue = true)
    @ModeSettings.GlobalProperty
    @ModeSettings.PlayerProperty
    @MenuBuilder.SettingItem(id = 3, header = "SPARKS", headerColour = EventReceiver.COLOR_PINK)
    public boolean sparkEffect;

    @MenuBuilder.SettingChanger(id = 3)
    public void changeSparkEffect(int ignored) {
        sparkEffect = !sparkEffect;
    }

    @MenuBuilder.SettingPrinter(id = 3)
    public String printSparkEffect() {
        return GeneralUtil.getONorOFF(sparkEffect);
    }

    @ModeSettings.PropertyPath(path = "landingEffect")
    @ModeSettings.DefaultValue(booleanValue = true)
    @ModeSettings.GlobalProperty
    @ModeSettings.PlayerProperty
    @MenuBuilder.SettingItem(id = 4, header = "DROP EFF.", headerColour = EventReceiver.COLOR_PINK)
    public boolean landingEffect;

    @MenuBuilder.SettingChanger(id = 4)
    public void changeLandingEffect(int ignored) {
        landingEffect = !landingEffect;
    }

    @MenuBuilder.SettingPrinter(id = 4)
    public String printLandingEffect() {
        return GeneralUtil.getONorOFF(landingEffect);
    }

    @ModeSettings.PropertyPath(path = "wobble")
    @ModeSettings.DefaultValue(booleanValue = true)
    @ModeSettings.GlobalProperty
    @ModeSettings.PlayerProperty
    @MenuBuilder.SettingItem(id = 5, header = "BG WOBBLE", headerColour = EventReceiver.COLOR_PINK)
    public boolean wobble;

    @MenuBuilder.SettingChanger(id = 5)
    public void changeWobble(int ignored) {
        wobble = !wobble;
    }

    @MenuBuilder.SettingPrinter(id = 5)
    public String printWobble() {
        return GeneralUtil.getONorOFF(wobble);
    }

    public String playerName;
    private final String playerNameProp = propPath("playerName");

    @ModeSettings.PropertyPath(path = "hasCompletedGame")
    @ModeSettings.DefaultValue(booleanValue = true)
    @ModeSettings.GlobalProperty
    @ModeSettings.PlayerProperty
    public boolean hasCompletedGame;

    @ModeSettings.PropertyPath(path = "hasSeenRollIntro")
    @ModeSettings.DefaultValue(booleanValue = true)
    @ModeSettings.GlobalProperty
    @ModeSettings.PlayerProperty
    public boolean hasSeenRollIntro;

    public final ModeLeaderboard<Integer, LeaderboardEntry> leaderboards;

    public static final class LeaderboardEntry {
        public final int gradePoints;
        public final int rollLevel;
        public final int level;
        public final int time;
        public final int perkOrdinal;

        public LeaderboardEntry(int gradePoints, int rollLevel, int level, int time, SeasonPerk perk) {
            this.gradePoints = gradePoints;
            this.rollLevel = rollLevel;
            this.level = level;
            this.time = time;
            this.perkOrdinal = perk.ordinal();
        }

        private LeaderboardEntry(int gradePoints, int rollLevel, int level, int time, int perkOrdinal) {
            this.gradePoints = gradePoints;
            this.rollLevel = rollLevel;
            this.level = level;
            this.time = time;
            this.perkOrdinal = perkOrdinal;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            LeaderboardEntry that = (LeaderboardEntry) o;
            return gradePoints == that.gradePoints && rollLevel == that.rollLevel && level == that.level && time == that.time && perkOrdinal == that.perkOrdinal;
        }

        @Override
        public int hashCode() {
            return Objects.hash(gradePoints, rollLevel, level, time, perkOrdinal);
        }

        public static final PropertyCodec<LeaderboardEntry> CODEC = new PropertyCodec<LeaderboardEntry>() {
            @Override
            public void save(CustomProperties properties, String propPath, LeaderboardEntry value) {
                IntegerCodec.INSTANCE.save(properties, propPath + ".gradePoint", value.gradePoints);
                IntegerCodec.INSTANCE.save(properties, propPath + ".rollDate", value.rollLevel);
                IntegerCodec.INSTANCE.save(properties, propPath + ".date", value.level);
                IntegerCodec.INSTANCE.save(properties, propPath + ".time", value.time);
                IntegerCodec.INSTANCE.save(properties, propPath + ".perk", value.perkOrdinal);
            }

            @Override
            public void savePlayer(ProfileProperties properties, String propPath, LeaderboardEntry value) {
                IntegerCodec.INSTANCE.savePlayer(properties, propPath + ".gradePoint", value.gradePoints);
                IntegerCodec.INSTANCE.savePlayer(properties, propPath + ".rollDate", value.rollLevel);
                IntegerCodec.INSTANCE.savePlayer(properties, propPath + ".date", value.level);
                IntegerCodec.INSTANCE.savePlayer(properties, propPath + ".time", value.time);
                IntegerCodec.INSTANCE.savePlayer(properties, propPath + ".perk", value.perkOrdinal);
            }

            @Override
            public LeaderboardEntry load(CustomProperties properties, String propPath, LeaderboardEntry defaultValue) {
                return new LeaderboardEntry(
                    IntegerCodec.INSTANCE.load(properties, propPath + ".gradePoint", defaultValue.gradePoints),
                    IntegerCodec.INSTANCE.load(properties, propPath + ".rollDate", defaultValue.rollLevel),
                    IntegerCodec.INSTANCE.load(properties, propPath + ".date", defaultValue.level),
                    IntegerCodec.INSTANCE.load(properties, propPath + ".time", defaultValue.time),
                    IntegerCodec.INSTANCE.load(properties, propPath + ".perk", defaultValue.perkOrdinal)
                );
            }

            @Override
            public LeaderboardEntry loadPlayer(ProfileProperties properties, String propPath, LeaderboardEntry defaultValue) {
                return new LeaderboardEntry(
                    IntegerCodec.INSTANCE.loadPlayer(properties, propPath + ".gradePoint", defaultValue.gradePoints),
                    IntegerCodec.INSTANCE.loadPlayer(properties, propPath + ".rollDate", defaultValue.rollLevel),
                    IntegerCodec.INSTANCE.loadPlayer(properties, propPath + ".date", defaultValue.level),
                    IntegerCodec.INSTANCE.loadPlayer(properties, propPath + ".time", defaultValue.time),
                    IntegerCodec.INSTANCE.loadPlayer(properties, propPath + ".perk", defaultValue.perkOrdinal)
                );
            }
        };

        public static final BiFunction<LeaderboardEntry, LeaderboardEntry, Order> ORDER = (newEntry, existing) -> Order
            .fromCompare(Integer.compare(newEntry.gradePoints, existing.gradePoints))
            .fold(() -> Order.fromCompare(Integer.compare(newEntry.rollLevel, existing.rollLevel)))
            .fold(() -> Order.fromCompare(Integer.compare(newEntry.level, existing.level)))
            .fold(() -> Order.fromCompare(Integer.compare(existing.time, newEntry.time)));
    }

    public int updateRanking() {
        return leaderboards.updateLeaderboard(perk.leaderboard);
    }

    public int updateRankingPlayer(ProfileProperties prop) {
        if (!prop.isLoggedIn()) return -1;
        return leaderboards.updatePlayerLeaderboard(perk.leaderboard);
    }

    private final SettingsHandler simpleSettingsHandler;

    public SeasonsSettings(int currentVersion, ProfileProperties playerProperties, Supplier<LeaderboardEntry> newEntrySupplier) {
        super(PROP_ROOT, playerProperties);

        this.simpleSettingsHandler = ModeSettings.generateSettingsHandler(this);
        this.currentVersion = currentVersion;
        this.hasCompletedGame = false;

        leaderboards = new ModeLeaderboard<>(
            this,
            LeaderboardEntry.CODEC,
            newEntrySupplier,
            LeaderboardEntry.ORDER,
            new LeaderboardEntry(0, -1, 0, 0, SeasonPerk.PERKLESS),
            currentVersion,
            RANKING_MAX,
            true
        );

        for (int i = 0; i < SeasonPerk.LEADERBOARDS; ++i) leaderboards.registerLeaderboard(i);
    }

    @Override
    public void loadSetting(CustomProperties prop, boolean isReplay) {
        simpleSettingsHandler.loadSetting(prop);
        perk = SeasonPerk.values()[perkOrdinal];

        // Version props are not saved on the player.
        version = isReplay ? prop.getProperty(versionProp, 0) : currentVersion;
        playerName = isReplay ? prop.getProperty(playerNameProp, "") : "";
    }

    @Override
    public void saveSetting(CustomProperties prop, boolean forReplay) {
        perkOrdinal = perk.ordinal();
        simpleSettingsHandler.saveSetting(prop);

        // Version props are not saved on the player.
        prop.setProperty(versionProp, currentVersion);
        if (forReplay && playerProperties.isLoggedIn()) prop.setProperty(playerNameProp, playerName);
    }

    @Override
    public void loadSettingPlayer(ProfileProperties prop) {
        if (!prop.isLoggedIn()) return;

        simpleSettingsHandler.loadPlayerSetting(prop);
        perk = SeasonPerk.values()[perkOrdinal];
    }

    @Override
    public void saveSettingPlayer(ProfileProperties prop) {
        if (!prop.isLoggedIn()) return;

        perkOrdinal = perk.ordinal();
        simpleSettingsHandler.savePlayerSetting(prop);
    }

    @Override
    public void loadRanking(GameManager owner, String ruleName) {
        leaderboards.loadRanking(owner, ruleName);
    }

    @Override
    public void saveRanking(GameManager owner, String ruleName) {
        leaderboards.saveRanking(owner, ruleName);
    }

    @Override
    public void loadRankingPlayer(ProfileProperties prop, String ruleName) {
        if (!prop.isLoggedIn()) return;
        leaderboards.loadPlayerRanking(prop, ruleName);
    }

    @Override
    public void saveRankingPlayer(ProfileProperties prop, String ruleName) {
        if (!prop.isLoggedIn()) return;
        leaderboards.savePlayerRanking(prop, ruleName);
    }
}
