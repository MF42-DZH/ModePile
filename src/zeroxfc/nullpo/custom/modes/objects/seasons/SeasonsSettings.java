package zeroxfc.nullpo.custom.modes.objects.seasons;

import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.util.CustomProperties;
import mu.nu.nullpo.util.GeneralUtil;
import zeroxfc.nullpo.custom.libs.MenuBuilder;
import zeroxfc.nullpo.custom.libs.ProfileProperties;
import zeroxfc.nullpo.custom.libs.types.ModeSettings;

@ModeSettings.PropertyRoot(root = "seasons")
public class SeasonsSettings extends ModeSettings {
    public static final int RANKING_MAX = 5;

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

    private String rankingGradePointProp(String ruleName, int leaderboard, int position) {
        return propPath("ranking", currentVersion, leaderboard, ruleName, "gradePoint", position);
    }
    public int[][] rankingGradePoint = new int[SeasonPerk.LEADERBOARDS][RANKING_MAX];
    public int[][] rankingGradePointPlayer = new int[SeasonPerk.LEADERBOARDS][RANKING_MAX];

    private String rankingDateProp(String ruleName, int leaderboard, int position) {
        return propPath("ranking", currentVersion, leaderboard, ruleName, "date", position);
    }
    public int[][] rankingDate = new int[SeasonPerk.LEADERBOARDS][RANKING_MAX];
    public int[][] rankingDatePlayer = new int[SeasonPerk.LEADERBOARDS][RANKING_MAX];

    private String rankingRollDateProp(String ruleName, int leaderboard, int position) {
        return propPath("ranking", currentVersion, leaderboard, ruleName, "rollDate", position);
    }
    public int[][] rankingRollDate = new int[SeasonPerk.LEADERBOARDS][RANKING_MAX];
    public int[][] rankingRollDatePlayer = new int[SeasonPerk.LEADERBOARDS][RANKING_MAX];

    private String rankingTimeProp(String ruleName, int leaderboard, int position) {
        return propPath("ranking", currentVersion, leaderboard, ruleName, "time", position);
    }
    public int[][] rankingTime = new int[SeasonPerk.LEADERBOARDS][RANKING_MAX];
    public int[][] rankingTimePlayer = new int[SeasonPerk.LEADERBOARDS][RANKING_MAX];

    private String rankingPerkProp(String ruleName, int leaderboard, int position) {
        return propPath("ranking", currentVersion, leaderboard, ruleName, "perk", position);
    }
    public int[][] rankingPerk = new int[SeasonPerk.LEADERBOARDS][RANKING_MAX];
    public int[][] rankingPerkPlayer = new int[SeasonPerk.LEADERBOARDS][RANKING_MAX];

    private Order compareRanking(boolean forPlayer, int leaderboard, int position, int gradePoints, int rollLevel, int level, int time) {
        final int[][] gp = forPlayer ? rankingGradePointPlayer : rankingGradePoint;
        final int[][] rd = forPlayer ? rankingRollDatePlayer : rankingRollDate;
        final int[][] d = forPlayer ? rankingDatePlayer : rankingDate;
        final int[][] t = forPlayer ? rankingTimePlayer : rankingTime;

        return Order.fromCompare(Integer.compare(gradePoints, gp[leaderboard][position]))
            .fold(() -> Order.fromCompare(Integer.compare(rollLevel, rd[leaderboard][position])))
            .fold(() -> Order.fromCompare(Integer.compare(level, d[leaderboard][position])))
            .fold(() -> Order.fromCompare(Integer.compare(t[leaderboard][position], time)));
    }

    private int getRanking(boolean forPlayer, int leaderboard, int gradePoints, int rollLevel, int level, int time) {
        for (int i = 0; i < RANKING_MAX; ++i) {
            final Order order = compareRanking(forPlayer, leaderboard, i, gradePoints, rollLevel, level, time);
            if (order == Order.GT || order == Order.EQ) return i;
        }

        return -1;
    }

    public int updateRanking(int gradePoints, int rollLevel, int level, int time) {
        final int leaderboard = perk.leaderboard;
        final int ranking = getRanking(false, leaderboard, gradePoints, rollLevel, level, time);

        if (ranking != -1) {
            for (int i = RANKING_MAX - 1; i > ranking; --i) {
                rankingGradePoint[leaderboard][i] = rankingGradePoint[leaderboard][i - 1];
                rankingRollDate[leaderboard][i] = rankingRollDate[leaderboard][i - 1];
                rankingDate[leaderboard][i] = rankingDate[leaderboard][i - 1];
                rankingTime[leaderboard][i] = rankingTime[leaderboard][i - 1];
                rankingPerk[leaderboard][i] = rankingPerk[leaderboard][i - 1];
            }

            rankingGradePoint[leaderboard][ranking] = gradePoints;
            rankingRollDate[leaderboard][ranking] = rollLevel;
            rankingDate[leaderboard][ranking] = level;
            rankingTime[leaderboard][ranking] = time;
            rankingPerk[leaderboard][ranking] = perk.ordinal();
        }

        return ranking;
    }

    public int updateRankingPlayer(ProfileProperties prop, int gradePoints, int rollLevel, int level, int time) {
        if (!prop.isLoggedIn()) return -1;

        final int leaderboard = perk.leaderboard;
        final int ranking = getRanking(true, leaderboard, gradePoints, rollLevel, level, time);

        if (ranking != -1) {
            for (int i = RANKING_MAX - 1; i > ranking; --i) {
                rankingGradePointPlayer[leaderboard][i] = rankingGradePointPlayer[leaderboard][i - 1];
                rankingRollDate[leaderboard][i] = rankingRollDatePlayer[leaderboard][i - 1];
                rankingDatePlayer[leaderboard][i] = rankingDatePlayer[leaderboard][i - 1];
                rankingTimePlayer[leaderboard][i] = rankingTimePlayer[leaderboard][i - 1];
                rankingPerkPlayer[leaderboard][i] = rankingPerkPlayer[leaderboard][i - 1];
            }

            rankingGradePointPlayer[leaderboard][ranking] = gradePoints;
            rankingRollDatePlayer[leaderboard][ranking] = rollLevel;
            rankingDatePlayer[leaderboard][ranking] = level;
            rankingTimePlayer[leaderboard][ranking] = time;
            rankingPerkPlayer[leaderboard][ranking] = perk.ordinal();
        }

        return ranking;
    }

    private final SettingsHandler simpleSettingsHandler;

    public SeasonsSettings(int currentVersion, ProfileProperties playerProperties) {
        super(playerProperties);

        this.simpleSettingsHandler = ModeSettings.generateSettingsHandler(this);
        this.currentVersion = currentVersion;
        this.hasCompletedGame = false;
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
        for (int b = 0; b < SeasonPerk.LEADERBOARDS; ++b) {
            for (int i = 0; i < RANKING_MAX; ++i) {
                rankingGradePoint[b][i] = owner.modeConfig.getProperty(rankingGradePointProp(ruleName, b, i), 0);
                rankingRollDate[b][i] = owner.modeConfig.getProperty(rankingRollDateProp(ruleName, b, i), -1);
                rankingDate[b][i] = owner.modeConfig.getProperty(rankingDateProp(ruleName, b, i), 0);
                rankingTime[b][i] = owner.modeConfig.getProperty(rankingTimeProp(ruleName, b, i), 0);
                rankingPerk[b][i] = owner.modeConfig.getProperty(rankingPerkProp(ruleName, b, i), SeasonPerk.PERKLESS.ordinal());
            }
        }
    }

    @Override
    public void saveRanking(GameManager owner, String ruleName) {
        for (int b = 0; b < SeasonPerk.LEADERBOARDS; ++b) {
            for (int i = 0; i < RANKING_MAX; ++i) {
                owner.modeConfig.setProperty(rankingGradePointProp(ruleName, b, i), rankingGradePoint[b][i]);
                owner.modeConfig.setProperty(rankingDateProp(ruleName, b, i), rankingDate[b][i]);
                owner.modeConfig.setProperty(rankingRollDateProp(ruleName, b, i), rankingRollDate[b][i]);
                owner.modeConfig.setProperty(rankingTimeProp(ruleName, b, i), rankingTime[b][i]);
                owner.modeConfig.setProperty(rankingPerkProp(ruleName, b, i), rankingPerk[b][i]);
            }
        }
    }

    @Override
    public void loadRankingPlayer(ProfileProperties prop, String ruleName) {
        if (!prop.isLoggedIn()) return;

        for (int b = 0; b < SeasonPerk.LEADERBOARDS; ++b) {
            for (int i = 0; i < RANKING_MAX; ++i) {
                rankingGradePointPlayer[b][i] = prop.getProperty(rankingGradePointProp(ruleName, b, i), 0);
                rankingRollDatePlayer[b][i] = prop.getProperty(rankingRollDateProp(ruleName, b, i), -1);
                rankingDatePlayer[b][i] = prop.getProperty(rankingDateProp(ruleName, b, i), 0);
                rankingTimePlayer[b][i] = prop.getProperty(rankingTimeProp(ruleName, b, i), 0);
                rankingPerkPlayer[b][i] = prop.getProperty(rankingPerkProp(ruleName, b, i), SeasonPerk.PERKLESS.ordinal());
            }
        }
    }

    @Override
    public void saveRankingPlayer(ProfileProperties prop, String ruleName) {
        if (!prop.isLoggedIn()) return;

        for (int b = 0; b < SeasonPerk.LEADERBOARDS; ++b) {
            for (int i = 0; i < RANKING_MAX; ++i) {
                prop.setProperty(rankingGradePointProp(ruleName, b, i), rankingGradePointPlayer[b][i]);
                prop.setProperty(rankingDateProp(ruleName, b, i), rankingDatePlayer[b][i]);
                prop.setProperty(rankingRollDateProp(ruleName, b, i), rankingRollDatePlayer[b][i]);
                prop.setProperty(rankingTimeProp(ruleName, b, i), rankingTimePlayer[b][i]);
                prop.setProperty(rankingPerkProp(ruleName, b, i), rankingPerkPlayer[b][i]);
            }
        }
    }
}
