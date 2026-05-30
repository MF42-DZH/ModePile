package zeroxfc.nullpo.custom.modes.objects.marathonlike;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.util.CustomProperties;
import mu.nu.nullpo.util.GeneralUtil;
import zeroxfc.nullpo.custom.libs.MathHelper;
import zeroxfc.nullpo.custom.libs.MenuBuilder;
import zeroxfc.nullpo.custom.libs.ModeLeaderboard;
import zeroxfc.nullpo.custom.libs.ModeSettings;
import zeroxfc.nullpo.custom.libs.ProfileProperties;
import zeroxfc.nullpo.custom.libs.PropertyCodec;
import zeroxfc.nullpo.custom.libs.types.Order;

public class TetratiotrisSettings extends ModeSettings {
    public static final int RANKING_MAX = 10;
    public static final String PROP_ROOT = "tetratiotris";

    public static final int[] TABLE_GAME_CLEAR_LINES = { 150, 200, -1 };

    private final int currentVersion;
    public int version;
    private final String versionProp = propPath("version");

    public String playerName;
    private final String playerNameProp = propPath("playerName");

    @ModeSettings.Property(path = "startLevel")
    @PropertyDefault(intValue = 0)
    @MenuBuilder.SettingItem(id = 0, header = "LEVEL", headerColour = EventReceiver.COLOR_PINK)
    public int startLevel;

    @MenuBuilder.SettingChanger(id = 0)
    public void changeStartLevel(int change) {
        startLevel += change;

        if (TABLE_GAME_CLEAR_LINES[goalType] >= 0) {
            if (startLevel < 0) startLevel = (TABLE_GAME_CLEAR_LINES[goalType] / 10) - 1;
            if (startLevel > (TABLE_GAME_CLEAR_LINES[goalType] / 10) - 1) startLevel = 0;
        } else {
            if (startLevel < 0) startLevel = 19;
            if (startLevel > 19) startLevel = 0;
        }
    }

    @MenuBuilder.SettingPrinter(id = 0)
    public String printStartLevel() {
        return Integer.toString(startLevel + 1);
    }

    @ModeSettings.Property(path = "spinBonus")
    @PropertyDefault(intValue = 1)
    @MenuBuilder.SettingItem(id = 1, header = "SPIN BONUS", headerColour = EventReceiver.COLOR_PINK)
    public int spinBonus;

    @MenuBuilder.SettingChanger(id = 1)
    public void changeSpinBonus(int change) {
        spinBonus += change;

        if (spinBonus < 0) spinBonus = 2;
        else if (spinBonus > 2) spinBonus = 0;
    }

    @MenuBuilder.SettingPrinter(id = 1)
    public String printSpinBonus() {
        switch (spinBonus) {
            case 1: return "T-ONLY";
            case 2: return "ALL";
            default: return "OFF";
        }
    }

    @ModeSettings.Property(path = "kickSpin")
    @PropertyDefault(booleanValue = true)
    @MenuBuilder.SettingItem(id = 2, header = "KICK SPIN", headerColour = EventReceiver.COLOR_PINK)
    public boolean kickSpin;

    @MenuBuilder.SettingChanger(id = 2)
    public void changeKickSpin(int ignored) {
        kickSpin = !kickSpin;
    }

    @MenuBuilder.SettingPrinter(id = 2)
    public String printKickSpin() {
        return GeneralUtil.getONorOFF(kickSpin);
    }

    @ModeSettings.Property(path = "spinCheck")
    @PropertyDefault(intValue = GameEngine.SPINTYPE_4POINT)
    @MenuBuilder.SettingItem(id = 3, header = "SPIN CHECK", headerColour = EventReceiver.COLOR_PINK)
    public int spinCheck;

    @MenuBuilder.SettingChanger(id = 3)
    public void changeSpinCheck(int change) {
        spinCheck += change;

        if (spinCheck < GameEngine.SPINTYPE_4POINT) spinCheck = GameEngine.SPINTYPE_IMMOBILE;
        if (spinCheck > GameEngine.SPINTYPE_IMMOBILE) spinCheck = GameEngine.SPINTYPE_4POINT;
    }

    @MenuBuilder.SettingPrinter(id = 3)
    public String printSpinCheck() {
        if (spinCheck == GameEngine.SPINTYPE_4POINT) return "4-POINT";
        if (spinCheck == GameEngine.SPINTYPE_IMMOBILE) return "IMMOBILE";

        return "UNKNOWN";
    }

    @ModeSettings.Property(path = "ezSpin")
    @PropertyDefault(booleanValue = false)
    @MenuBuilder.SettingItem(id = 4, header = "EZ-IMM.", headerColour = EventReceiver.COLOR_PINK)
    public boolean ezSpin;

    @MenuBuilder.SettingChanger(id = 4)
    public void changeEzSpin(int ignored) {
        ezSpin = !ezSpin;
    }

    @MenuBuilder.SettingPrinter(id = 4)
    public String printEzSpin() {
        return GeneralUtil.getONorOFF(ezSpin);
    }

    @ModeSettings.Property(path = "b2b")
    @PropertyDefault(booleanValue = true)
    @MenuBuilder.SettingItem(id = 5, header = "B2B", headerColour = EventReceiver.COLOR_PINK)
    public boolean b2b;

    @MenuBuilder.SettingChanger(id = 5)
    public void changeB2B(int ignored) {
        b2b = !b2b;
    }

    @MenuBuilder.SettingPrinter(id = 5)
    public String printB2B() {
        return GeneralUtil.getONorOFF(b2b);
    }

    @ModeSettings.Property(path = "combo")
    @PropertyDefault(booleanValue = true)
    @MenuBuilder.SettingItem(id = 6, header = "COMBO", headerColour = EventReceiver.COLOR_PINK)
    public boolean combo;

    @MenuBuilder.SettingChanger(id = 6)
    public void changeCombo(int ignored) {
        combo = !combo;
    }

    @MenuBuilder.SettingPrinter(id = 6)
    public String printCombo() {
        return GeneralUtil.getONorOFF(combo);
    }

    @ModeSettings.Property(path = "goalType")
    @PropertyDefault(intValue = 0)
    @MenuBuilder.SettingItem(id = 7, header = "GOAL", headerColour = EventReceiver.COLOR_PINK)
    public int goalType;

    @MenuBuilder.SettingChanger(id = 7)
    public void changeGoalType(int change) {
        goalType += change;

        if (goalType < 0) goalType = TABLE_GAME_CLEAR_LINES.length - 1;
        if (goalType > TABLE_GAME_CLEAR_LINES.length - 1) goalType = 0;

        if ((TABLE_GAME_CLEAR_LINES[goalType] >= 0) && (startLevel > (TABLE_GAME_CLEAR_LINES[goalType] / 10) - 1)) {
            startLevel = (TABLE_GAME_CLEAR_LINES[goalType] / 10) - 1;
        }
    }

    @MenuBuilder.SettingPrinter(id = 7)
    public String printGoalType() {
        if (TABLE_GAME_CLEAR_LINES[goalType] < 0) return "ENDLESS";
        else return TABLE_GAME_CLEAR_LINES[goalType] + " LINES";
    }

    @ModeSettings.Property(path = "big")
    @PropertyDefault(booleanValue = false)
    @MenuBuilder.SettingItem(id = 8, header = "BIG", headerColour = EventReceiver.COLOR_PINK)
    public boolean big;

    @MenuBuilder.SettingChanger(id = 8)
    public void changeBig(int ignored) {
        big = !big;
    }

    @MenuBuilder.SettingPrinter(id = 8)
    public String printBig() {
        return GeneralUtil.getONorOFF(big);
    }

    private final SettingsHandler simpleSettingsHandler;

    public static final class BigDecimalImpreciseInfo {
        private static final int DECIMAL_PRECISION = 6;

        public final int mantissaNoSep;
        public final int exponent;

        public BigDecimalImpreciseInfo(BigDecimal num) {
            final BigDecimal stripped = num.stripTrailingZeros();
            final int nonDecimalDigits = num.precision() - num.scale();

            mantissaNoSep = stripped
                .movePointRight(DECIMAL_PRECISION - nonDecimalDigits)
                .setScale(0, RoundingMode.FLOOR)
                .intValueExact();

            exponent = nonDecimalDigits - 1;
        }

        public BigDecimalImpreciseInfo(int mantissaNoSep, int exponent) {
            this.mantissaNoSep = mantissaNoSep;
            this.exponent = exponent;
        }
    }

    private static final PropertyCodec<BigDecimalImpreciseInfo> IMPRECISE_BD_CODEC = new PropertyCodec<BigDecimalImpreciseInfo>() {
        private static final String SUFFIX_MANTISSA = "mantissaInexact";
        private static final String SUFFIX_EXPONENT = "exponent";

        @Override
        public void save(CustomProperties properties, String propPath, BigDecimalImpreciseInfo value) {
            IntegerCodec.INSTANCE.save(properties, joinPropPath(propPath, SUFFIX_MANTISSA), value.mantissaNoSep);
            IntegerCodec.INSTANCE.save(properties, joinPropPath(propPath, SUFFIX_EXPONENT), value.exponent);
        }

        @Override
        public void savePlayer(ProfileProperties properties, String propPath, BigDecimalImpreciseInfo value) {
            IntegerCodec.INSTANCE.savePlayer(properties, joinPropPath(propPath, SUFFIX_MANTISSA), value.mantissaNoSep);
            IntegerCodec.INSTANCE.savePlayer(properties, joinPropPath(propPath, SUFFIX_EXPONENT), value.exponent);
        }

        @Override
        public BigDecimalImpreciseInfo load(CustomProperties properties, String propPath, BigDecimalImpreciseInfo defaultValue) {
            final int mantissa = IntegerCodec.INSTANCE.load(properties, joinPropPath(propPath, SUFFIX_MANTISSA), defaultValue.mantissaNoSep);
            final int exponent = IntegerCodec.INSTANCE.load(properties, joinPropPath(propPath, SUFFIX_EXPONENT), defaultValue.exponent);

            return new BigDecimalImpreciseInfo(mantissa, exponent);
        }

        @Override
        public BigDecimalImpreciseInfo loadPlayer(ProfileProperties properties, String propPath, BigDecimalImpreciseInfo defaultValue) {
            final int mantissa = IntegerCodec.INSTANCE.loadPlayer(properties, joinPropPath(propPath, SUFFIX_MANTISSA), defaultValue.mantissaNoSep);
            final int exponent = IntegerCodec.INSTANCE.loadPlayer(properties, joinPropPath(propPath, SUFFIX_EXPONENT), defaultValue.exponent);

            return new BigDecimalImpreciseInfo(mantissa, exponent);
        }

        @Override
        public Class<BigDecimalImpreciseInfo> getValueClass() {
            return BigDecimalImpreciseInfo.class;
        }

        @Override
        public BigDecimalImpreciseInfo defaultLoadValue() {
            return new BigDecimalImpreciseInfo(BigDecimal.ZERO);
        }
    };

    public static final class LeaderboardEntry {
        public final BigDecimalImpreciseInfo score;
        public final int lines;
        public final int time;

        public LeaderboardEntry(BigDecimalImpreciseInfo score, int lines, int time) {
            this.score = score;
            this.lines = lines;
            this.time = time;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            LeaderboardEntry that = (LeaderboardEntry) o;
            return lines == that.lines && time == that.time && Objects.equals(score, that.score);
        }

        @Override
        public int hashCode() {
            return Objects.hash(score, lines, time);
        }

        public static final PropertyCodec<LeaderboardEntry> CODEC = new PropertyCodec<LeaderboardEntry>() {
            private static final String SUFFIX_SCORE = "score";
            private static final String SUFFIX_LINES = "level";
            private static final String SUFFIX_TIME = "time";

            @Override
            public void save(CustomProperties properties, String propPath, LeaderboardEntry value) {
                IMPRECISE_BD_CODEC.save(properties, joinPropPath(propPath, SUFFIX_SCORE), value.score);
                PropertyCodec.IntegerCodec.INSTANCE.save(properties, joinPropPath(propPath, SUFFIX_LINES), value.lines);
                PropertyCodec.IntegerCodec.INSTANCE.save(properties, joinPropPath(propPath, SUFFIX_TIME), value.time);
            }

            @Override
            public void savePlayer(ProfileProperties properties, String propPath, LeaderboardEntry value) {
                IMPRECISE_BD_CODEC.savePlayer(properties, joinPropPath(propPath, SUFFIX_SCORE), value.score);
                PropertyCodec.IntegerCodec.INSTANCE.savePlayer(properties, joinPropPath(propPath, SUFFIX_LINES), value.lines);
                PropertyCodec.IntegerCodec.INSTANCE.savePlayer(properties, joinPropPath(propPath, SUFFIX_TIME), value.time);
            }

            @Override
            public LeaderboardEntry load(CustomProperties properties, String propPath, LeaderboardEntry defaultValue) {
                return new LeaderboardEntry(
                    IMPRECISE_BD_CODEC.load(properties, joinPropPath(propPath, SUFFIX_SCORE), defaultValue.score),
                    PropertyCodec.IntegerCodec.INSTANCE.load(properties, joinPropPath(propPath, SUFFIX_LINES), defaultValue.lines),
                    PropertyCodec.IntegerCodec.INSTANCE.load(properties, joinPropPath(propPath, SUFFIX_TIME), defaultValue.time)
                );
            }

            @Override
            public LeaderboardEntry loadPlayer(ProfileProperties properties, String propPath, LeaderboardEntry defaultValue) {
                return new LeaderboardEntry(
                    IMPRECISE_BD_CODEC.loadPlayer(properties, joinPropPath(propPath, SUFFIX_SCORE), defaultValue.score),
                    PropertyCodec.IntegerCodec.INSTANCE.loadPlayer(properties, joinPropPath(propPath, SUFFIX_LINES), defaultValue.lines),
                    PropertyCodec.IntegerCodec.INSTANCE.loadPlayer(properties, joinPropPath(propPath, SUFFIX_TIME), defaultValue.time)
                );
            }

            @Override
            public Class<LeaderboardEntry> getValueClass() {
                return LeaderboardEntry.class;
            }

            @Override
            public LeaderboardEntry defaultLoadValue() {
                return new LeaderboardEntry(new BigDecimalImpreciseInfo(BigDecimal.valueOf(100)), 0, 0);
            }
        };

        public static final BiFunction<LeaderboardEntry, LeaderboardEntry, Order> ORDER = (newEntry, existing) -> Order
            .fromCompare(Integer.compare(newEntry.score.exponent, existing.score.exponent))
            .fold(() -> Order.fromCompare(Integer.compare(newEntry.score.mantissaNoSep, existing.score.mantissaNoSep)))
            .fold(() -> Order.fromCompare(Integer.compare(newEntry.lines, existing.lines)))
            .fold(() -> Order.fromCompare(Integer.compare(existing.time, newEntry.time)));
    }

    public final ModeLeaderboard<Integer, LeaderboardEntry> leaderboards;

    public TetratiotrisSettings(int currentVersion, ProfileProperties playerProperties, Supplier<LeaderboardEntry> newEntrySupplier) {
        super(PROP_ROOT, playerProperties);

        this.simpleSettingsHandler = ModeSettings.generateSettingsHandler(this);
        this.currentVersion = currentVersion;

        leaderboards = new ModeLeaderboard<>(
            this,
            LeaderboardEntry.CODEC,
            newEntrySupplier,
            LeaderboardEntry.ORDER,
            currentVersion,
            RANKING_MAX,
            true,
            lines -> {
                if (lines < 0) return "ENDLESS";
                else return lines + "L";
            }
        );

        leaderboards.registerLeaderboard(150);
        leaderboards.registerLeaderboard(200);
        leaderboards.registerLeaderboard(-1);
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
