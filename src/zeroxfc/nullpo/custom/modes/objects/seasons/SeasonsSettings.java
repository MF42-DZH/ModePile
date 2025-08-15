package zeroxfc.nullpo.custom.modes.objects.seasons;

import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameManager;
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
    private final String hasCompletedGameProp = propPath("hasCompletedGame");
    public boolean hasCompletedGame;

    private final String hasSeenRollIntroProp = propPath("hasSeenRollIntro");
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
            }

            rankingGradePoint[leaderboard][ranking] = gradePoints;
            rankingRollDate[leaderboard][ranking] = rollLevel;
            rankingDate[leaderboard][ranking] = level;
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
            }

            rankingGradePointPlayer[leaderboard][ranking] = gradePoints;
            rankingRollDatePlayer[leaderboard][ranking] = rollLevel;
            rankingDatePlayer[leaderboard][ranking] = level;
        }

        return ranking;
    }

    // TODO: add a separate system for storing if players have seen the roll, to give them the ability to skip
    // TODO: achievements?

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

        hasCompletedGame = prop.getProperty(hasCompletedGameProp, false);
        hasSeenRollIntro = prop.getProperty(hasSeenRollIntroProp, false);

        // Version props are not saved on the player.
        version = isReplay ? prop.getProperty(versionProp, 0) : currentVersion;
        playerName = isReplay ? prop.getProperty(playerNameProp, "") : "";
    }

    @Override
    public void saveSetting(CustomProperties prop, boolean forReplay) {
        prop.setProperty(perkProp, perk.ordinal());
        prop.setProperty(fullGhostProp, fullGhost);
        prop.setProperty(spinTypeProp, spinType);

        prop.setProperty(hasCompletedGameProp, hasCompletedGame);
        prop.setProperty(hasSeenRollIntroProp, hasSeenRollIntro);

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
        hasSeenRollIntro = prop.getProperty(hasSeenRollIntroProp, false);
    }

    @Override
    public void saveSettingPlayer(ProfileProperties prop) {
        if (!prop.isLoggedIn()) return;

        prop.setProperty(perkProp, perk.ordinal());
        prop.setProperty(fullGhostProp, fullGhost);
        prop.setProperty(spinTypeProp, spinType);

        prop.setProperty(hasCompletedGameProp, hasCompletedGame);
        prop.setProperty(hasSeenRollIntroProp, hasSeenRollIntro);
    }

    @Override
    public void loadRanking(GameManager owner, String ruleName) {
        for (int b = 0; b < SeasonPerk.LEADERBOARDS; ++b) {
            for (int i = 0; i < RANKING_MAX; ++i) {
                rankingGradePoint[b][i] = owner.modeConfig.getProperty(rankingGradePointProp(ruleName, b, i), 0);
                rankingRollDate[b][i] = owner.modeConfig.getProperty(rankingRollDateProp(ruleName, b, i), -1);
                rankingDate[b][i] = owner.modeConfig.getProperty(rankingDateProp(ruleName, b, i), 0);
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
            }
        }
    }
}
