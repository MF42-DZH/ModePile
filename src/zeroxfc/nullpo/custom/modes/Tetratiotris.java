package zeroxfc.nullpo.custom.modes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.IntFunction;
import mu.nu.nullpo.game.component.BGMStatus;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.SpeedParam;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.game.subsystem.mode.DummyMode;
import mu.nu.nullpo.util.CustomProperties;
import mu.nu.nullpo.util.GeneralUtil;
import org.apache.log4j.Logger;
import zeroxfc.nullpo.custom.libs.GameTextUtilities;
import zeroxfc.nullpo.custom.libs.MathHelper;
import zeroxfc.nullpo.custom.libs.MenuBuilder;
import zeroxfc.nullpo.custom.libs.ProfileProperties;
import zeroxfc.nullpo.custom.libs.SpeedTableBuilder;
import zeroxfc.nullpo.custom.libs.types.ObjectAlignment;
import zeroxfc.nullpo.custom.modes.objects.marathonlike.TetratiotrisSettings;

public class Tetratiotris extends DummyMode {
    private static final Logger log = Logger.getLogger(Tetratiotris.class);

    private static final int CURRENT_VERSION = 1;
    private static final int HEADER_COLOUR = EventReceiver.COLOR_PINK;

    // Need to start with a non-zero score.
    private static final BigDecimal DEFAULT_SCORE = new BigDecimal(100);

    // Tetration power values.
    private static final BigDecimal POWER_SINGLE = new BigDecimal("0.001");
    private static final BigDecimal POWER_DOUBLE = new BigDecimal("0.002");
    private static final BigDecimal POWER_TRIPLE = new BigDecimal("0.004");
    private static final BigDecimal POWER_FOUR = new BigDecimal("0.008"); // 4+
    private static final BigDecimal POWER_SPIN_ZERO = new BigDecimal("0.004");
    private static final BigDecimal POWER_SPIN_ZERO_MINI = new BigDecimal("0.001");
    private static final BigDecimal POWER_SPIN_SINGLE = new BigDecimal("0.008");
    private static final BigDecimal POWER_SPIN_SINGLE_EZ = new BigDecimal("0.0012");
    private static final BigDecimal POWER_SPIN_SINGLE_MINI = new BigDecimal("0.002");
    private static final BigDecimal POWER_SPIN_DOUBLE = new BigDecimal("0.012");
    private static final BigDecimal POWER_SPIN_DOUBLE_MINI = new BigDecimal("0.004");
    private static final BigDecimal POWER_SPIN_TRIPLE = new BigDecimal("0.016"); // S3+
    private static final BigDecimal POWER_COMBO = new BigDecimal("0.0005");
    private static final BigDecimal POWER_BRAVO = new BigDecimal("0.018");
    private static final BigDecimal POWER_MULT_B2B = new BigDecimal("1.5");

    private static final IntFunction<SpeedParam> MODE_SPEED_TABLE = SpeedTableBuilder.createNew()
        .addGravity(1, 63, 1)
        .addGravity(1, 50, 2)
        .addGravity(1, 39, 3)
        .addGravity(1, 30, 4)
        .addGravity(1, 22, 5)
        .addGravity(1, 16, 6)
        .addGravity(1, 12, 7)
        .addGravity(1, 8, 8)
        .addGravity(1, 6, 9)
        .addGravity(1, 4, 10)
        .addGravity(1, 3, 11)
        .addGravity(1, 2, 12)
        .addGravity(1, 1, 13)
        .addGravity(465, 256, 14)
        .addGravity(731, 256, 15)
        .addGravity(1280, 256, 16)
        .addGravity(1707, 256, 17)
        .addTerminalGravity(-1, 256)
        .addTerminalARE(24)
        .addTerminalLineARE(24)
        .addTerminalDAS(14)
        .addTerminalLockDelay(30)
        .addTerminalLineDelay(40)
        .buildSpeedTable();

    private static final NavigableMap<Integer, Integer> BGM_INFO_TABLE;
    static {
        BGM_INFO_TABLE = new TreeMap<>();

        BGM_INFO_TABLE.put(50, BGMStatus.BGM_NORMAL2);
        BGM_INFO_TABLE.put(100, BGMStatus.BGM_NORMAL3);
        BGM_INFO_TABLE.put(150, BGMStatus.BGM_NORMAL4);
        BGM_INFO_TABLE.put(200, BGMStatus.BGM_NORMAL5);
    }

    private GameManager owner;
    private EventReceiver receiver;
    private TetratiotrisSettings settings;
    private MenuBuilder.Menu settingsMenu;
    private ProfileProperties playerProperties;
    private boolean showPlayerStats;

    private ExecutorService threadService;
    private AtomicReference<BigDecimal> realScore;
    private BigDecimal displayScore;
    private String lastTetration;
    private int timeSinceLastScore;

    private int lastRank;
    private int lastRankPlayer;

    @Override
    public String getName() {
        return "TETRATIOTRIS";
    }

    @Override
    public void modeInit(GameManager manager) {
        playerProperties = null;
    }

    @Override
    public void playerInit(GameEngine engine, int playerID) {
        owner = engine.owner;
        receiver = engine.owner.receiver;

        if (threadService != null) {
            threadService.shutdown();
            try {
                threadService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            } catch (InterruptedException e) {
                log.error(e);
                throw new RuntimeException(e);
            }

            threadService = null;
        }

        threadService = Executors.newSingleThreadExecutor();
        realScore = new AtomicReference<>(DEFAULT_SCORE);
        displayScore = DEFAULT_SCORE;

        lastRank = -1;
        lastRankPlayer = -1;

        if (playerProperties == null) {
            playerProperties = new ProfileProperties(HEADER_COLOUR);
            showPlayerStats = false;
        }

        settings = new TetratiotrisSettings(CURRENT_VERSION, playerProperties, () -> {
            threadService.shutdown();
            try {
                threadService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            } catch (InterruptedException e) {
                log.error(e);
                throw new RuntimeException(e);
            }

            return new TetratiotrisSettings.LeaderboardEntry(
                realScore.get(), engine.statistics.lines, engine.statistics.time
            );
        });
        settingsMenu = MenuBuilder.generateMenu(this, settings);

        if (!owner.replayMode) {
            settings.loadSetting(owner.modeConfig, false);
            settings.loadRanking(owner, engine.ruleopt.strRuleName);

            if (playerProperties.isLoggedIn()) {
                settings.loadSettingPlayer();
                settings.loadRankingPlayer(engine.ruleopt.strRuleName);
            }
        } else {
            settings.loadSetting(owner.replayProp, true);
        }

        engine.owner.backgroundStatus.bg = settings.startLevel;
        engine.framecolor = GameEngine.FRAME_COLOR_PINK;
    }

    public void setSpeed(GameEngine engine) {
        engine.speed = MODE_SPEED_TABLE.apply(engine.statistics.level);
    }

    @Override
    public boolean onSetting(GameEngine engine, int playerID) {
        if (!engine.owner.replayMode) {
            settingsMenu.updateSettings(engine, playerID);
            engine.owner.backgroundStatus.bg = settings.startLevel;

            if (engine.ctrl.isPush(Controller.BUTTON_A) && engine.statc[3] >= 5) {
                engine.playSE("decide");

                if (playerProperties.isLoggedIn()) {
                    settings.saveSettingPlayer();
                    settings.commitPlayerSettingAndRank();
                } else {
                    settings.saveSetting(owner.modeConfig, false);
                    settings.commitSettingAndRank(receiver, owner);
                }

                return false;
            }

            if (engine.ctrl.isPush(Controller.BUTTON_B)) {
                engine.quitflag = true;
            }

            if (engine.ctrl.isPush(Controller.BUTTON_E) && engine.ai == null) {
                engine.playSE("decide");

                engine.stat = GameEngine.STAT_CUSTOM;
                engine.resetStatc();

                return true;
            }

            engine.statc[3]++;
        } else {
            engine.statc[3]++;
            engine.statc[2] = -1;

            return engine.statc[3] < 60;
        }

        return true;
    }

    @Override
    public void renderSetting(GameEngine engine, int playerID) {
        settingsMenu.renderSettings(engine, playerID, receiver, 0);
    }

    @Override
    public boolean onCustom(GameEngine engine, int playerID) {
        showPlayerStats = false;
        engine.isInGame = true;

        boolean s = playerProperties.loginScreen.updateScreen(engine, playerID);

        if (engine.stat == GameEngine.STAT_SETTING) {
            if (playerProperties.isLoggedIn()) {
                settings.loadRankingPlayer(engine.ruleopt.strRuleName);
                settings.loadSettingPlayer();
            }

            engine.isInGame = false;
        }

        return s;
    }

    @Override
    public void startGame(GameEngine engine, int playerID) {
        engine.statistics.level = settings.startLevel;
        engine.statistics.levelDispAdd = 1;

        engine.b2bEnable = settings.b2b;
        engine.comboType = settings.combo ? GameEngine.COMBO_TYPE_NORMAL : GameEngine.COMBO_TYPE_DISABLE;
        engine.big = settings.big;

        engine.bighalf = true;
        engine.bigmove = true;

        engine.tspinAllowKick = settings.kickSpin;
        engine.tspinEnable = settings.spinBonus > 0;
        engine.useAllSpinBonus = settings.spinBonus > 1;

        engine.spinCheckType = settings.spinCheck;
        engine.tspinEnableEZ = settings.ezSpin;

        realScore.set(DEFAULT_SCORE);
        displayScore = DEFAULT_SCORE;

        setSpeed(engine);
    }

    @Override
    public void renderLast(GameEngine engine, int playerID) {
        if (owner.menuOnly) return;

        receiver.drawScoreFont(engine, playerID, 0, 0, getName(), HEADER_COLOUR);
        if (TetratiotrisSettings.TABLE_GAME_CLEAR_LINES[settings.goalType] < 0) {
            receiver.drawScoreFont(engine, playerID, 0, 1, "(ENDLESS GAME)", HEADER_COLOUR);
        } else {
            receiver.drawScoreFont(engine, playerID, 0, 1, "(" + TetratiotrisSettings.TABLE_GAME_CLEAR_LINES[settings.goalType] + " LINES GAME)", HEADER_COLOUR);
        }

        if ((engine.stat == GameEngine.STAT_SETTING || engine.stat == GameEngine.STAT_RESULT) && !owner.replayMode) {
            if (!settings.big && engine.ai == null) {
                final float scale = (receiver.getNextDisplayType() == 2) ? 0.5f : 1.0f;
                final int topY = (receiver.getNextDisplayType() == 2) ? 6 : 4;
                receiver.drawScoreFont(engine, playerID, 3, topY-1, "SCORE   LINE TIME", HEADER_COLOUR, scale);

                for (int i = 0; i < TetratiotrisSettings.RANKING_MAX; ++i) {
                    final TetratiotrisSettings.LeaderboardEntry entry = showPlayerStats
                        ? settings.leaderboards.readPlayerLeaderboard(TetratiotrisSettings.TABLE_GAME_CLEAR_LINES[settings.goalType], i)
                        : settings.leaderboards.readLeaderboard(TetratiotrisSettings.TABLE_GAME_CLEAR_LINES[settings.goalType], i);

                    final boolean rankingFlag = (showPlayerStats && i == lastRankPlayer) || (!showPlayerStats && i == lastRank);

                    receiver.drawScoreFont(engine, playerID, 0, topY+i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW, scale);
                    GameTextUtilities.drawAlignedScoreTextBlock(
                        receiver, engine, playerID,
                        receiver.getNextDisplayType() == 2,
                        3, topY + i,
                        false,
                        getStandardForm(entry.score, rankingFlag ? EventReceiver.COLOR_RED : EventReceiver.COLOR_WHITE, scale),
                        ObjectAlignment.TOP_LEFT
                    );
                    receiver.drawScoreFont(engine, playerID, 11, topY + i, String.valueOf(entry.lines), rankingFlag, scale);
                    receiver.drawScoreFont(engine, playerID, 16, topY + i, GeneralUtil.getTime(entry.time), rankingFlag, scale);
                }

                if (!showPlayerStats) {
                    receiver.drawScoreFont(engine, playerID, 0, topY + TetratiotrisSettings.RANKING_MAX + 1, "LOCAL SCORES", EventReceiver.COLOR_BLUE);
                    if (!playerProperties.isLoggedIn())
                        receiver.drawScoreFont(engine, playerID, 0, topY + TetratiotrisSettings.RANKING_MAX + 2, "(NOT LOGGED IN)\n(E:LOG IN)");
                    if (playerProperties.isLoggedIn())
                        receiver.drawScoreFont(engine, playerID, 0, topY + TetratiotrisSettings.RANKING_MAX + 5, "F:SWITCH RANK SCREEN", EventReceiver.COLOR_GREEN);
                } else {
                    receiver.drawScoreFont(engine, playerID, 0, topY + TetratiotrisSettings.RANKING_MAX + 1, "PLAYER SCORES", EventReceiver.COLOR_BLUE);
                    GameTextUtilities.drawAlignedScoreText(receiver, engine, playerID, false, 0, topY + TetratiotrisSettings.RANKING_MAX + 2, GameTextUtilities.Text.ofBig(owner.replayMode ? settings.playerName : playerProperties.getNameDisplay()));

                    receiver.drawScoreFont(engine, playerID, 0, topY + TetratiotrisSettings.RANKING_MAX + 5, "F:SWITCH RANK SCREEN", EventReceiver.COLOR_GREEN);
                }
            }
        } else if (engine.stat == GameEngine.STAT_CUSTOM) {
            playerProperties.loginScreen.renderScreen(receiver, engine, playerID);
        } else {
            receiver.drawScoreFont(engine, playerID, 0, 3, "SCORE", HEADER_COLOUR);
            GameTextUtilities.drawAlignedScoreTextBlock(
                receiver, engine,playerID,
                false,
                0, 4,
                false,
                getStandardForm(displayScore, EventReceiver.COLOR_WHITE, 1f),
                ObjectAlignment.TOP_LEFT
            );

            receiver.drawScoreFont(engine, playerID, 0, 6, "LINE", HEADER_COLOUR);
            if((engine.statistics.level >= 19) && (TetratiotrisSettings.TABLE_GAME_CLEAR_LINES[settings.goalType] < 0))
                receiver.drawScoreFont(engine, playerID, 0, 7, engine.statistics.lines + "");
            else
                receiver.drawScoreFont(engine, playerID, 0, 7, engine.statistics.lines + "/" + ((engine.statistics.level + 1) * 10));

            receiver.drawScoreFont(engine, playerID, 0, 9, "LEVEL", HEADER_COLOUR);
            receiver.drawScoreFont(engine, playerID, 0, 10, String.valueOf(engine.statistics.level + 1));

            receiver.drawScoreFont(engine, playerID, 0, 12, "TIME", HEADER_COLOUR);
            receiver.drawScoreFont(engine, playerID, 0, 13, GeneralUtil.getTime(engine.statistics.time));
        }
    }

    @Override
    public void onLast(GameEngine engine, int playerID) {
        displayScore = getDisplayScore(displayScore, realScore.get());

        if ((engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (!owner.replayMode) && (engine.ai == null))) {
            // Show rank
            if (engine.ctrl.isPush(Controller.BUTTON_D) && playerProperties.isLoggedIn() && engine.stat != GameEngine.STAT_CUSTOM) {
                showPlayerStats = !showPlayerStats;
                engine.playSE("change");
            }
        }
    }

    @Override
    public void calcScore(GameEngine engine, int playerID, int lines) {
        // Lines
        BigDecimal chosenPow = BigDecimal.ZERO;
        final BigDecimal multiplier = BigDecimal.ONE.add(new BigDecimal("0.5").multiply(BigDecimal.valueOf(engine.statistics.level)));

        if (lines == 0) {
            if (engine.tspin && engine.tspinmini) chosenPow = POWER_SPIN_ZERO_MINI;
            else if (engine.tspin) chosenPow = POWER_SPIN_ZERO;
        } else if (lines == 1) {
            if (engine.tspin && engine.tspinez) chosenPow = POWER_SPIN_SINGLE_EZ;
            else if (engine.tspin && engine.tspinmini) chosenPow = POWER_SPIN_SINGLE_MINI;
            else if (engine.tspin) chosenPow = POWER_SPIN_SINGLE;
            else chosenPow = POWER_SINGLE;
        } else if (lines == 2) {
            if (engine.tspin && engine.tspinmini && engine.useAllSpinBonus) chosenPow = POWER_SPIN_DOUBLE_MINI;
            else if (engine.tspin) chosenPow = POWER_SPIN_DOUBLE;
            else chosenPow = POWER_DOUBLE;
        } else if (lines == 3) {
            if (engine.tspin) chosenPow = POWER_SPIN_TRIPLE;
            else chosenPow = POWER_TRIPLE;
        } else if (lines >= 4) {
            if (engine.tspin) {
                chosenPow = POWER_SPIN_TRIPLE;
                engine.playSE("tspin3");
            } else {
                chosenPow = POWER_FOUR;
                if (lines > 4) engine.playSE("erase4");
            }
        }

        if (engine.b2b) chosenPow = chosenPow.multiply(POWER_MULT_B2B);

        chosenPow = chosenPow.multiply(multiplier).add(BigDecimal.ONE);

        if (chosenPow.compareTo(BigDecimal.ONE) > 0) {
            powRealScore(chosenPow);
        }

        // Ren
        if (settings.combo && engine.combo >= 2 && lines >= 1) {
            powRealScore(POWER_COMBO.multiply(new BigDecimal(engine.combo - 1)).multiply(multiplier).add(BigDecimal.ONE));
        }

        // Bravo
        if (lines > 0 && engine.field.isEmpty()) {
            engine.playSE("bravo");
            powRealScore(POWER_BRAVO.multiply(multiplier).add(BigDecimal.ONE));
        }

        // BGM
        final int linesBefore = engine.statistics.lines - lines;
        final Map.Entry<Integer, Integer> bgmEntry = BGM_INFO_TABLE.ceilingEntry(linesBefore);

        if (bgmEntry != null) {
            if (engine.statistics.lines >= bgmEntry.getKey() - 5) owner.bgmStatus.fadesw = true;
            if (engine.statistics.lines >= bgmEntry.getKey() && TetratiotrisSettings.TABLE_GAME_CLEAR_LINES[settings.goalType] > 0 && engine.statistics.lines < TetratiotrisSettings.TABLE_GAME_CLEAR_LINES[settings.goalType]) {
                owner.bgmStatus.bgm = bgmEntry.getValue();
                owner.bgmStatus.fadesw = false;
            }
        }

        // Meter
        engine.meterValue = ((engine.statistics.lines % 10) * receiver.getMeterMax(engine)) / 9;
        engine.meterColor = GameEngine.METER_COLOR_GREEN;
        if(engine.statistics.lines % 10 >= 4) engine.meterColor = GameEngine.METER_COLOR_YELLOW;
        if(engine.statistics.lines % 10 >= 6) engine.meterColor = GameEngine.METER_COLOR_ORANGE;
        if(engine.statistics.lines % 10 >= 8) engine.meterColor = GameEngine.METER_COLOR_RED;

        if((engine.statistics.lines >= TetratiotrisSettings.TABLE_GAME_CLEAR_LINES[settings.goalType]) && (TetratiotrisSettings.TABLE_GAME_CLEAR_LINES[settings.goalType] >= 0)) {
            // Ending
            engine.ending = 1;
            engine.gameEnded();
        } else if((engine.statistics.lines >= (engine.statistics.level + 1) * 10) && (engine.statistics.level < 19)) {
            // Level up
            engine.statistics.level++;

            owner.backgroundStatus.fadesw = true;
            owner.backgroundStatus.fadecount = 0;
            owner.backgroundStatus.fadebg = engine.statistics.level;

            setSpeed(engine);
            engine.playSE("levelup");
        }
    }

    @Override
    public void renderResult(GameEngine engine, int playerID) {
        receiver.drawMenuFont(engine, playerID, 0, 0, "SCORE", EventReceiver.COLOR_BLUE);
        GameTextUtilities.drawAlignedMenuTextBlock(
            receiver, engine, playerID,
            false,
            0, 1,
            false,
            getStandardForm(realScore.get(), EventReceiver.COLOR_WHITE, 1f),
            ObjectAlignment.TOP_LEFT
        );

        drawResultStats(
            engine, playerID, receiver, 2, EventReceiver.COLOR_BLUE,
            STAT_LINES, STAT_LEVEL, STAT_TIME, STAT_LPM
        );
    }

    @Override
    public void saveReplay(GameEngine engine, int playerID, CustomProperties prop) {
        settings.saveSetting(prop, true);

        if (!owner.replayMode && !settings.big && engine.ai == null) {
            lastRank = settings.leaderboards.updateLeaderboard(TetratiotrisSettings.TABLE_GAME_CLEAR_LINES[settings.goalType]);
            lastRankPlayer = settings.leaderboards.updatePlayerLeaderboard(TetratiotrisSettings.TABLE_GAME_CLEAR_LINES[settings.goalType]);

            if (playerProperties.isLoggedIn()) {
                prop.setProperty("accelerator.playerName", playerProperties.getNameDisplay());
            }

            if (lastRank != -1) {
                settings.leaderboards.saveRanking(owner, engine.ruleopt.strRuleName);
                settings.commitSettingAndRank(receiver, owner);
            }

            if (lastRankPlayer != -1 && playerProperties.isLoggedIn()) {
                settings.leaderboards.savePlayerRanking(playerProperties, engine.ruleopt.strRuleName);
                settings.commitPlayerSettingAndRank();
            }
        }
    }

    private static final DecimalFormat SCI_FORM_FORMATTER = new DecimalFormat("0.00E0");

    private static GameTextUtilities.TextBlock getStandardForm(BigDecimal num, int colour, float scale) {
        final String rawFormat = SCI_FORM_FORMATTER.format(num);
        final String[] parts = rawFormat.split("E");

        return GameTextUtilities.TextBlock.of(
            GameTextUtilities.TextJustification.LEFT,
            GameTextUtilities.Text.custom(parts[0], colour, scale),
            GameTextUtilities.Text.custom("E", colour, scale / 2f),
            GameTextUtilities.Text.custom(parts[1], colour, scale)
        );
    }

    private void powRealScore(final BigDecimal exp) {
        final AtomicReference<BigDecimal> scoreRef = realScore;
//        scoreRef.updateAndGet(s -> pow(s, exp));

        threadService.submit(() -> {
            scoreRef.updateAndGet(s -> pow(s, exp));
        });
    }

    private static BigDecimal pow(BigDecimal base, BigDecimal exp) {
        // Tweak latter two params as appropriate.
        return roundToAppropriate(
            MathHelper.bigPow(
                base, exp,
                8,
                512
            )
        );
    }

    // Run this after score calculation
    private static BigDecimal roundToAppropriate(BigDecimal num) {
        return num.setScale(1, RoundingMode.HALF_UP);
    }

    private static BigDecimal getDisplayScore(BigDecimal currentDisplay, BigDecimal targetScore) {
        if (currentDisplay.compareTo(targetScore) >= 0) return targetScore;

        final BigDecimal difference = targetScore.subtract(currentDisplay);
        final BigDecimal add = difference.movePointLeft(2).multiply(BigDecimal.valueOf(2));

        final BigDecimal newScore = currentDisplay.add(add);

        if (newScore.compareTo(targetScore) >= 0) return targetScore;
        return roundToAppropriate(newScore.stripTrailingZeros());
    }
}
