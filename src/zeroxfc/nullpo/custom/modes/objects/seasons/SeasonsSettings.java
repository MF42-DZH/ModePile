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
    public static final String PROP_ROOT = "seasons";

    private final int currentVersion;
    public int version;
    private final String versionProp = propPath("version");

    @ModeSettings.Property(path = "fullGhost")
    @PropertyDefault(booleanValue = false)
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

    @ModeSettings.Property(path = "perk")
    @MenuBuilder.SettingItem(id = 0, header = "PERK", headerColour = EventReceiver.COLOR_YELLOW)
    public SeasonPerk perk;

    @StaticCodec
    public static final PropertyCodec<SeasonPerk> PERK_CODEC = PropertyCodec.deriveEnumCodec(SeasonPerk.class, SeasonPerk.SPRING_PASSIVE);

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

    @ModeSettings.Property(path = "spinType")
    @PropertyDefault(intValue = GameEngine.SPINTYPE_4POINT)
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

    @ModeSettings.Property(path = "sparks")
    @PropertyDefault(booleanValue = true)
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

    @ModeSettings.Property(path = "landingEffect")
    @PropertyDefault(booleanValue = true)
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

    @ModeSettings.Property(path = "wobble")
    @PropertyDefault(booleanValue = true)
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

    @ModeSettings.Property(path = "hasCompletedGame")
    @PropertyDefault(booleanValue = false)
    public boolean hasCompletedGame;

    @ModeSettings.Property(path = "hasSeenRollIntro")
    @PropertyDefault(booleanValue = false)
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
            private static final String SUFFIX_GRADE_POINTS = "gradePoint";
            private static final String SUFFIX_ROLL_LEVEL = "rollDate";
            private static final String SUFFIX_LEVEL = "date";
            private static final String SUFFIX_TIME = "time";
            private static final String SUFFIX_PERK = "perk";

            @Override
            public void save(CustomProperties properties, String propPath, LeaderboardEntry value) {
                IntegerCodec.INSTANCE.save(properties, joinPropPath(propPath, SUFFIX_GRADE_POINTS), value.gradePoints);
                IntegerCodec.INSTANCE.save(properties, joinPropPath(propPath, SUFFIX_ROLL_LEVEL), value.rollLevel);
                IntegerCodec.INSTANCE.save(properties, joinPropPath(propPath, SUFFIX_LEVEL), value.level);
                IntegerCodec.INSTANCE.save(properties, joinPropPath(propPath, SUFFIX_TIME), value.time);
                IntegerCodec.INSTANCE.save(properties, joinPropPath(propPath, SUFFIX_PERK), value.perkOrdinal);
            }

            @Override
            public void savePlayer(ProfileProperties properties, String propPath, LeaderboardEntry value) {
                IntegerCodec.INSTANCE.savePlayer(properties, joinPropPath(propPath, SUFFIX_GRADE_POINTS), value.gradePoints);
                IntegerCodec.INSTANCE.savePlayer(properties, joinPropPath(propPath, SUFFIX_ROLL_LEVEL), value.rollLevel);
                IntegerCodec.INSTANCE.savePlayer(properties, joinPropPath(propPath, SUFFIX_LEVEL), value.level);
                IntegerCodec.INSTANCE.savePlayer(properties, joinPropPath(propPath, SUFFIX_TIME), value.time);
                IntegerCodec.INSTANCE.savePlayer(properties, joinPropPath(propPath, SUFFIX_PERK), value.perkOrdinal);
            }

            @Override
            public LeaderboardEntry load(CustomProperties properties, String propPath, LeaderboardEntry defaultValue) {
                return new LeaderboardEntry(
                    IntegerCodec.INSTANCE.load(properties, joinPropPath(propPath, SUFFIX_GRADE_POINTS), defaultValue.gradePoints),
                    IntegerCodec.INSTANCE.load(properties, joinPropPath(propPath, SUFFIX_ROLL_LEVEL), defaultValue.rollLevel),
                    IntegerCodec.INSTANCE.load(properties, joinPropPath(propPath, SUFFIX_LEVEL), defaultValue.level),
                    IntegerCodec.INSTANCE.load(properties, joinPropPath(propPath, SUFFIX_TIME), defaultValue.time),
                    IntegerCodec.INSTANCE.load(properties, joinPropPath(propPath, SUFFIX_PERK), defaultValue.perkOrdinal)
                );
            }

            @Override
            public LeaderboardEntry loadPlayer(ProfileProperties properties, String propPath, LeaderboardEntry defaultValue) {
                return new LeaderboardEntry(
                    IntegerCodec.INSTANCE.loadPlayer(properties, joinPropPath(propPath, SUFFIX_GRADE_POINTS), defaultValue.gradePoints),
                    IntegerCodec.INSTANCE.loadPlayer(properties, joinPropPath(propPath, SUFFIX_ROLL_LEVEL), defaultValue.rollLevel),
                    IntegerCodec.INSTANCE.loadPlayer(properties, joinPropPath(propPath, SUFFIX_LEVEL), defaultValue.level),
                    IntegerCodec.INSTANCE.loadPlayer(properties, joinPropPath(propPath, SUFFIX_TIME), defaultValue.time),
                    IntegerCodec.INSTANCE.loadPlayer(properties, joinPropPath(propPath, SUFFIX_PERK), defaultValue.perkOrdinal)
                );
            }

            @Override
            public LeaderboardEntry defaultLoadValue() {
                return new LeaderboardEntry(0, -1, 0, 0, SeasonPerk.PERKLESS);
            }

            @Override
            public Class<LeaderboardEntry> getValueClass() {
                return LeaderboardEntry.class;
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

    public int updateRankingPlayer() {
        if (!playerProperties.isLoggedIn()) return -1;
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
            currentVersion,
            RANKING_MAX,
            true
        );

        for (int i = 0; i < SeasonPerk.LEADERBOARDS; ++i) leaderboards.registerLeaderboard(i);
    }

    @Override
    public void loadSetting(CustomProperties prop, boolean isReplay) {
        simpleSettingsHandler.loadSetting(prop);

        // Version props are not saved on the player.
        version = isReplay ? prop.getProperty(versionProp, 0) : currentVersion;
        playerName = isReplay ? prop.getProperty(playerNameProp, "") : "";
    }

    @Override
    public void saveSetting(CustomProperties prop, boolean forReplay) {
        simpleSettingsHandler.saveSetting(prop);

        // Version props are not saved on the player.
        if (forReplay) prop.setProperty(versionProp, currentVersion);
        if (forReplay && playerProperties.isLoggedIn()) prop.setProperty(playerNameProp, playerProperties.getNameDisplay());
    }

    @Override
    public void loadSettingPlayer() {
        if (!playerProperties.isLoggedIn()) return;
        simpleSettingsHandler.loadPlayerSetting(playerProperties);
    }

    @Override
    public void saveSettingPlayer() {
        if (!playerProperties.isLoggedIn()) return;
        simpleSettingsHandler.savePlayerSetting(playerProperties);
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
    public void loadRankingPlayer(String ruleName) {
        if (!playerProperties.isLoggedIn()) return;
        leaderboards.loadPlayerRanking(playerProperties, ruleName);
    }

    @Override
    public void saveRankingPlayer(String ruleName) {
        if (!playerProperties.isLoggedIn()) return;
        leaderboards.savePlayerRanking(playerProperties, ruleName);
    }
}
