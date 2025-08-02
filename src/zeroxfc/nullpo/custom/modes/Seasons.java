package zeroxfc.nullpo.custom.modes;

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
import zeroxfc.nullpo.custom.libs.CustomResourceHolder;
import zeroxfc.nullpo.custom.libs.GameTextUtilities;
import zeroxfc.nullpo.custom.libs.LevelTableBuilder;
import zeroxfc.nullpo.custom.libs.ProfileProperties;
import zeroxfc.nullpo.custom.libs.RendererExtension;
import zeroxfc.nullpo.custom.libs.SpeedTableBuilder;
import zeroxfc.nullpo.custom.libs.mixins.HasCustomFieldDrawing;
import zeroxfc.nullpo.custom.libs.mixins.HasCustomOnMove;
import zeroxfc.nullpo.custom.libs.types.ObjectAlignment;
import zeroxfc.nullpo.custom.modes.objects.seasons.Badges;
import zeroxfc.nullpo.custom.modes.objects.seasons.SeasonPerk;
import zeroxfc.nullpo.custom.modes.objects.seasons.SeasonsSettings;

public class Seasons extends DummyMode implements HasCustomOnMove, HasCustomFieldDrawing {
    private static final Logger log = Logger.getLogger(Seasons.class);

    private static final int CURRENT_VERSION = 0;

    // TODO: This eventually will need to be changed.
    private static final IntFunction<SpeedParam> SPEED_TABLE = SpeedTableBuilder.createNew()
        .addTerminalGravity(4, 256)
        .addTerminalARE(30)
        .addTerminalLineARE(30)
        .addTerminalDAS(15)
        .addTerminalLockDelay(30)
        .addTerminalLineDelay(40)
        .buildSpeedTable();

    private enum Season {
        SPRING(GameEngine.FRAME_COLOR_GREEN),
        SUMMER(GameEngine.FRAME_COLOR_RED),
        AUTUMN(GameEngine.FRAME_COLOR_YELLOW),
        WINTER(GameEngine.FRAME_COLOR_CYAN);

        public final int defaultFrameColour;

        public static int defaultMenuFrameColour() {
            return GameEngine.FRAME_COLOR_GRAY;
        }

        Season(int defaultFrameColour) {
            this.defaultFrameColour = defaultFrameColour;
        }
    }

    private static final int HOURS_IN_DAY = 24;

    private static final int LEVELS_FEB = (28 * HOURS_IN_DAY);
    private static final int LEVELS_MAR = LEVELS_FEB + (31 * HOURS_IN_DAY);
    private static final int LEVELS_APR = LEVELS_MAR + (30 * HOURS_IN_DAY);
    private static final int LEVELS_MAY = LEVELS_APR + (31 * HOURS_IN_DAY);
    private static final int LEVELS_JUN = LEVELS_MAY + (30 * HOURS_IN_DAY);
    private static final int LEVELS_JUL = LEVELS_JUN + (31 * HOURS_IN_DAY);
    private static final int LEVELS_AUG = LEVELS_JUL + (31 * HOURS_IN_DAY);
    private static final int LEVELS_SEP = LEVELS_AUG + (30 * HOURS_IN_DAY);
    private static final int LEVELS_OCT = LEVELS_SEP + (31 * HOURS_IN_DAY);
    private static final int LEVELS_NOV = LEVELS_OCT + (30 * HOURS_IN_DAY);
    private static final int LEVELS_DEC = LEVELS_NOV + (31 * HOURS_IN_DAY);
    private static final int LEVELS_JAN = LEVELS_DEC + (31 * HOURS_IN_DAY); // Also the max level.

    private static final IntFunction<Integer> NEXT_SECTION_LEVELS = LevelTableBuilder.<Integer>createNew()
        .addValue(LEVELS_FEB, LEVELS_FEB)
        .addValue(LEVELS_MAR, LEVELS_MAR)
        .addValue(LEVELS_APR, LEVELS_APR)
        .addValue(LEVELS_MAY, LEVELS_MAY)
        .addValue(LEVELS_JUN, LEVELS_JUN)
        .addValue(LEVELS_JUL, LEVELS_JUL)
        .addValue(LEVELS_AUG, LEVELS_AUG)
        .addValue(LEVELS_SEP, LEVELS_SEP)
        .addValue(LEVELS_OCT, LEVELS_OCT)
        .addValue(LEVELS_NOV, LEVELS_NOV)
        .addValue(LEVELS_DEC, LEVELS_DEC)
        .addValue(LEVELS_JAN, LEVELS_JAN)
        .addTerminalValue(LEVELS_JAN)
        .buildLevelTable();

    private static final IntFunction<Integer> LEVELS_SO_FAR = LevelTableBuilder.<Integer>createNew()
        .addValue(0, LEVELS_FEB)
        .addValue(LEVELS_FEB, LEVELS_MAR)
        .addValue(LEVELS_MAR, LEVELS_APR)
        .addValue(LEVELS_APR, LEVELS_MAY)
        .addValue(LEVELS_MAY, LEVELS_JUN)
        .addValue(LEVELS_JUN, LEVELS_JUL)
        .addValue(LEVELS_JUL, LEVELS_AUG)
        .addValue(LEVELS_AUG, LEVELS_SEP)
        .addValue(LEVELS_SEP, LEVELS_OCT)
        .addValue(LEVELS_OCT, LEVELS_NOV)
        .addValue(LEVELS_NOV, LEVELS_DEC)
        .addValue(LEVELS_DEC, LEVELS_JAN)
        .addTerminalValue(LEVELS_JAN)
        .buildLevelTable();

    private static final IntFunction<String> MONTH_NAME_TABLE = LevelTableBuilder.<String>createNew()
        .addValue("FEB", LEVELS_FEB)
        .addValue("MAR", LEVELS_MAR)
        .addValue("APR", LEVELS_APR)
        .addValue("MAY", LEVELS_MAY)
        .addValue("JUN", LEVELS_JUN)
        .addValue("JUL", LEVELS_JUL)
        .addValue("AUG", LEVELS_AUG)
        .addValue("SEP", LEVELS_SEP)
        .addValue("OCT", LEVELS_OCT)
        .addValue("NOV", LEVELS_NOV)
        .addValue("DEC", LEVELS_DEC)
        .addValue("JAN", LEVELS_JAN)
        .addTerminalValue("INVALID")
        .buildLevelTable();

    private static final IntFunction<Integer> BACKGROUND_TABLE = LevelTableBuilder.<Integer>createNew()
        .addValue(0, LEVELS_FEB)
        .addValue(1, LEVELS_MAR)
        .addValue(2, LEVELS_APR)
        .addValue(3, LEVELS_MAY)
        .addValue(4, LEVELS_JUN)
        .addValue(5, LEVELS_JUL)
        .addValue(6, LEVELS_AUG)
        .addValue(7, LEVELS_SEP)
        .addValue(8, LEVELS_OCT)
        .addValue(9, LEVELS_NOV)
        .addValue(10, LEVELS_DEC)
        .addValue(11, LEVELS_JAN)
        .addTerminalValue(12)
        .buildLevelTable();

    private static final IntFunction<Season> SEASON_TABLE = LevelTableBuilder.<Season>createNew()
        .addValue(Season.SPRING, LEVELS_APR)
        .addValue(Season.SUMMER, LEVELS_JUN)
        .addValue(Season.AUTUMN, LEVELS_OCT)
        .addTerminalValue(Season.WINTER)
        .buildLevelTable();

    private static final IntFunction<Integer> BGM_TABLE = LevelTableBuilder.<Integer>createNew()
        .addValue(BGMStatus.BGM_NORMAL1, LEVELS_APR)
        .addValue(BGMStatus.BGM_NORMAL2, LEVELS_JUN)
        .addValue(BGMStatus.BGM_NORMAL3, LEVELS_OCT)
        .addTerminalValue(BGMStatus.BGM_NORMAL4)
        .buildLevelTable();

    private static final IntFunction<Integer> BGM_FADE_LEVEL_TABLE = LevelTableBuilder.<Integer>createNew()
        .addValue(LEVELS_MAY - 72, LEVELS_APR)
        .addValue(LEVELS_AUG - 72, LEVELS_JUN)
        .addValue(LEVELS_NOV - 72, LEVELS_OCT)
        .addTerminalValue(-1)
        .buildLevelTable();

    private static String levelToString(int level) {
        final String month = MONTH_NAME_TABLE.apply(level);
        final int normLevel = level - LEVELS_SO_FAR.apply(level);
        return String.format("%02d %s %02d:00 XXX%d", (normLevel / 24) + 1, month, normLevel % 24, level >= LEVELS_DEC ? 1 : 0);
    }

    // Game manager and current renderer.
    private GameManager owner;
    private EventReceiver receiver;
    private CustomResourceHolder customGraphics;
    private RendererExtension rendererExtension;

    // Settings
    private static final int HEADER_COLOUR = EventReceiver.COLOR_ORANGE;
    private SeasonsSettings settings;
    private ProfileProperties playerProperties;
    private boolean showPlayerStats;

    // Gameplay variables
    private boolean levelUpFlag;
    private int nextSectionLevel;
    private Season currentSeason;
    private int lastBackground, currentBackground, fadeProgress;
    private Badges badges;
    private int naturalLevelIncrement;

    @Override
    public FrameDrawingParameters getFrameDrawingParameters(GameEngine engine, int playerID) {
        return null;
    }

    @Override
    public int getLastBackground() {
        return lastBackground;
    }

    @Override
    public int getCurrentBackground() {
        return currentBackground;
    }

    @Override
    public float getFadeProgress() {
        return fadeProgress / 300f;
    }

    private void setNewBackground(int newBg) {
        if (currentBackground == newBg) return;

        lastBackground = currentBackground;
        currentBackground = newBg;
        fadeProgress = 0;
    }

    private void updateFadeProgress() {
        fadeProgress = Math.min(300, fadeProgress + 15);
    }

    @Override
    public String getName() {
        return "SEASONS";
    }

    @Override
    public void playerInit(GameEngine engine, int playerID) {
        owner = engine.owner;
        receiver = engine.owner.receiver;

        customGraphics = new CustomResourceHolder();
        rendererExtension = new RendererExtension(customGraphics);

        setupBackgrounds(engine);

        levelUpFlag = false;
        currentSeason = Season.SPRING;
        nextSectionLevel = 0;
        badges = new Badges();

        engine.framecolor = Season.defaultMenuFrameColour();

        if (playerProperties == null) {
            playerProperties = new ProfileProperties(HEADER_COLOUR);
            showPlayerStats = false;
        }

        settings = new SeasonsSettings(CURRENT_VERSION, playerProperties);

        if (!owner.replayMode) {
            settings.loadSetting(owner.modeConfig, false);
            settings.loadRanking(owner.modeConfig, engine.ruleopt.strRuleName);

            if (playerProperties.isLoggedIn()) {
                settings.loadSettingPlayer(playerProperties);
                settings.loadRankingPlayer(playerProperties, engine.ruleopt.strRuleName);
            }
        } else {
            settings.loadSetting(owner.replayProp, true);
        }

        lastBackground = 0;
        currentBackground = 0;
        fadeProgress = 300;
    }

    @Override
    public boolean onCustom(GameEngine engine, int playerID) {
        showPlayerStats = false;
        engine.isInGame = true;

        final boolean s = playerProperties.loginScreen.updateScreen(engine, playerID);
        if (playerProperties.isLoggedIn()) {
            settings.loadRankingPlayer(playerProperties, engine.ruleopt.strRuleName);
            settings.loadSettingPlayer(playerProperties);
        }

        if (engine.stat == GameEngine.STAT_SETTING) engine.isInGame = false;

        return s;
    }

    @Override
    public boolean onSetting(GameEngine engine, int playerID) {
        if (!engine.owner.replayMode) {
            // Configuration changes
            int change = updateCursor(engine, 1);

            if (change != 0) {
                engine.playSE("change");

                switch (engine.statc[2]) {
                    case 0:
                        int selectedPerk = settings.perk.ordinal() + change;

                        if (!settings.hasCompletedGame) {
                            if (selectedPerk < 1) selectedPerk = SeasonPerk.values().length - 1;
                            else if (selectedPerk >= SeasonPerk.values().length) selectedPerk = 1;
                        } else {
                            if (selectedPerk < 0) selectedPerk = SeasonPerk.values().length - 1;
                            else if (selectedPerk >= SeasonPerk.values().length) selectedPerk = 0;
                        }

                        settings.perk = SeasonPerk.values()[selectedPerk];
                        break;
                    case 1:
                        settings.fullGhost = !settings.fullGhost;
                        break;
                }
            }

            if (engine.ctrl.isPush(Controller.BUTTON_A) && (engine.statc[3] >= 5)) {
                engine.playSE("decide");

                if (playerProperties.isLoggedIn()) {
                    settings.saveSettingPlayer(playerProperties);
                    playerProperties.saveProfileConfig();
                } else {
                    settings.saveSetting(owner.modeConfig, false);
                    receiver.saveModeConfig(owner.modeConfig);
                }

                return false;
            }

            if (engine.ctrl.isPush(Controller.BUTTON_B)) {
                engine.quitflag = true;
                playerProperties = new ProfileProperties(HEADER_COLOUR);
            }

            if (engine.ctrl.isPush(Controller.BUTTON_E) && engine.ai == null) {
                playerProperties = new ProfileProperties(HEADER_COLOUR);
                engine.playSE("decide");

                engine.stat = GameEngine.STAT_CUSTOM;
                engine.resetStatc();
                return true;
            }


            engine.statc[3]++;
        } else {
            engine.statc[3]++;
            engine.statc[2] = -1;

            if (engine.statc[3] >= 60) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void renderSetting(GameEngine engine, int playerID) {
        String perkString = settings.perk.name();
        if (perkString.contains("_")) {
            final String[] split = perkString.split("_");
            perkString = String.format("%s (%s)", split[0], split[1].charAt(0));
        }

        drawMenu(engine, playerID, receiver, 0, EventReceiver.COLOR_YELLOW, 0,
            "PERK", perkString
        );
        drawMenu(engine, playerID, receiver, 2, EventReceiver.COLOR_BLUE, 1,
            "FULL GHOST", GeneralUtil.getONorOFF(settings.fullGhost)
        );
    }

    private void setSpeed(GameEngine engine) {
        // TODO: Populate this further later
        engine.speed = SPEED_TABLE.apply(engine.statistics.level);
    }

    @Override
    public void startGame( GameEngine engine, int playerID ) {
        engine.statistics.level = 0;

        nextSectionLevel = NEXT_SECTION_LEVELS.apply(engine.statistics.level);
        setNewBackground(BACKGROUND_TABLE.apply(engine.statistics.level));

        owner.backgroundStatus.bg = -1;
        owner.backgroundStatus.fadebg = -1;

        engine.ghost = true;

        engine.big = false;
        engine.bigmove = true;
        engine.bighalf = true;

        engine.tspinEnable = true;
        engine.useAllSpinBonus = true;
        engine.tspinAllowKick = true;
        engine.spinCheckType = GameEngine.SPINTYPE_4POINT;
        engine.tspinminiType = GameEngine.TSPINMINI_TYPE_ROTATECHECK;

        engine.b2bEnable = true;

        engine.staffrollEnableStatistics = false;

        naturalLevelIncrement = 1;

        setSpeed(engine);
        owner.bgmStatus.bgm = BGM_TABLE.apply(engine.statistics.level);
    }

    @Override
    public boolean onMove(GameEngine engine, int playerID) {
        if ((engine.ending == 0) && (engine.statc[0] == 0) && (!engine.holdDisable) && (!levelUpFlag)) {
            if (engine.statistics.level < nextSectionLevel - 1) {
                engine.statistics.level = Math.min(engine.statistics.level + naturalLevelIncrement, nextSectionLevel - 1);

                if (engine.statistics.level == nextSectionLevel - 1)
                    engine.playSE("levelstop");
            }

            levelUp(engine);
        }

        if (engine.statc[0] > 0) {
            levelUpFlag = false;
        }

        return inOnMove(engine, playerID);
    }

    @Override
    public boolean onARE(GameEngine engine, int playerID) {
        if ((engine.ending == 0 && (engine.statc[0]>= engine.statc[1] - 1) && (!levelUpFlag))) {
            if (engine.statistics.level < nextSectionLevel - 1) {
                engine.statistics.level = Math.min(engine.statistics.level + naturalLevelIncrement, nextSectionLevel - 1);

                if (engine.statistics.level == nextSectionLevel - 1)
                    engine.playSE("levelstop");
            }

            levelUp(engine);
            levelUpFlag = true;
        }

        return false;
    }

    // FOR CALCSCORE: THE CLEAR THAT BRINGS YOU ABOVE A BADGE THRESHOLD DOESN'T BENEFIT FROM THE LEVEL BONUS!


    @Override
    public void calcScore(GameEngine engine, int playerID, int lines) {
        if (lines >= 1) {
            if (engine.field.isEmpty()) {
                engine.playSE("bravo");
            }

            int levelBefore = engine.statistics.level;

            if (lines >= 4) {
                engine.statistics.level += 6 * naturalLevelIncrement;
            } else if (lines == 3) {
                engine.statistics.level += 4 * naturalLevelIncrement;
            } else {
                engine.statistics.level += lines * naturalLevelIncrement;
            }

            engine.statistics.level += badges.getLevelBonus() * naturalLevelIncrement;

            levelUp(engine);
            badges.updateBadges(engine, lines, 0);

            // TODO: Temporary levelling. Add real ending later.
            if (engine.statistics.level >= LEVELS_JAN) {
                // ADD REAL ENDING LATER
                engine.ending = 1;
                engine.gameEnded();
            } else if (engine.statistics.level >= nextSectionLevel) {
                engine.playSE("levelup");

                setNewBackground(BACKGROUND_TABLE.apply(engine.statistics.level));

                currentSeason = SEASON_TABLE.apply(engine.statistics.level);

                if (owner.bgmStatus.bgm <= BGM_TABLE.apply(engine.statistics.level)) {
                    owner.bgmStatus.fadesw = false;
                    BGM_TABLE.apply(engine.statistics.level);

                    if (NEXT_SECTION_LEVELS.apply(engine.statistics.level) > nextSectionLevel) {
                        nextSectionLevel = NEXT_SECTION_LEVELS.apply(engine.statistics.level);
                    }
                }
            } else if(engine.statistics.level == nextSectionLevel - 1) {
                engine.playSE("levelstop");
            }
        }
    }

    private void levelUp(GameEngine engine ) {
        // TODO: Meter
        engine.meterValue = 0;
        engine.meterColor = GameEngine.METER_COLOR_GREEN;

        setSpeed( engine );

        if ((engine.statistics.level >= LEVELS_APR) && (!settings.fullGhost)) engine.ghost = false;

        final int fadeout = BGM_FADE_LEVEL_TABLE.apply(engine.statistics.level);
        if ((fadeout > 0) && (engine.statistics.level >= fadeout)) {
            owner.bgmStatus.fadesw = true;
        }
    }

    @Override
    public void renderFirst(GameEngine engine, int playerID) {
        inRenderFirst(rendererExtension, receiver, engine, playerID);
    }

    @Override
    public void renderMove(GameEngine engine, int playerID) {
        inRenderMove(rendererExtension, receiver, engine, playerID);
    }

    @Override
    public void renderExcellent(GameEngine engine, int playerID) {
        inRenderExcellent(rendererExtension, receiver, engine, playerID);
    }

    @Override
    public void renderGameOver(GameEngine engine, int playerID) {
        inRenderGameOver(rendererExtension, receiver, engine, playerID);
    }

    @Override
    public void renderResult(GameEngine engine, int playerID) {
        inRenderResult(rendererExtension, receiver, engine, playerID);
    }

    @Override
    public void onLast(GameEngine engine, int playerID) {
        updateFadeProgress();

        if (engine.gameActive) {
            engine.framecolor = currentSeason.defaultFrameColour;
        } else {
            engine.framecolor = Season.defaultMenuFrameColour();
        }
    }

    @Override
    public void renderLast(GameEngine engine, int playerID) {
        final int titlesColour = EventReceiver.COLOR_YELLOW;

        receiver.drawScoreFont(engine, playerID, 0, 0, getName(), titlesColour);

        if (engine.stat == GameEngine.STAT_SETTING || (engine.stat == GameEngine.STAT_RESULT && !owner.replayMode)) {
            final float scale = (receiver.getNextDisplayType() == 2) ? 0.5f : 1.0f;
            final int topY = (receiver.getNextDisplayType() == 2) ? 5 : 3;
            final boolean showRankings = !owner.replayMode && !settings.fullGhost;

            if (showRankings) {
                receiver.drawScoreFont(engine, playerID, 3, topY - 1, "RN&TI DATE  TIME", titlesColour, scale);
                for (int i = 0; i < SeasonsSettings.RANKING_MAX; ++i) {
                    receiver.drawScoreFont(engine, playerID, 0, topY + i, String.format("%2d N/A   N/A   N/A", i + 1));
                }

                receiver.drawScoreFont(engine, playerID, 0, topY + 1 + SeasonsSettings.RANKING_MAX, "D:SWITCH RANK SCREEN", EventReceiver.COLOR_GREEN, scale);
            }

            GameTextUtilities.drawAlignedScoreTextBlock(
                receiver, engine, playerID, receiver.getNextDisplayType() == 2,
                0, topY + (showRankings ? 3 + SeasonsSettings.RANKING_MAX : (-1)), false,
                settings.perk.getDescription(scale),
                ObjectAlignment.TOP_LEFT
            );
        } else if (engine.stat == GameEngine.STAT_CUSTOM && !engine.gameStarted) {
            playerProperties.loginScreen.renderScreen(receiver, engine, playerID);
        } else {
            receiver.drawScoreFont(engine, playerID, 0, 2, "RANK & TITLE", titlesColour);
            receiver.drawScoreFont(engine, playerID, 0, 3, "N/A");

            receiver.drawScoreFont(engine, playerID, 0, 5, "SCORE", titlesColour);
            receiver.drawScoreFont(engine, playerID, 0, 6, "N/A");

            receiver.drawScoreFont(engine, playerID, 0, 8, "DATE", titlesColour);
            receiver.drawScoreFont(engine, playerID, 0, 9, levelToString(engine.statistics.level));

            receiver.drawScoreFont(engine, playerID, 0, 11, "TIME", titlesColour);
            receiver.drawScoreFont(engine, playerID, 0, 12, GeneralUtil.getTime(engine.statistics.time));

            receiver.drawScoreFont(engine, playerID, 0, 14, "PERK", titlesColour);
            receiver.drawScoreFont(engine, playerID, 0, 15, settings.perk.getName());

            receiver.drawScoreFont(engine, playerID, 0, 17, "BADGES", titlesColour);
            GameTextUtilities.drawAlignedScoreTextBlock(
                receiver, engine, playerID, false,
                0, 18, false,
                badges.getBadgeDisplay(false),
                ObjectAlignment.TOP_LEFT
            );
        }
    }

    @Override
    public void saveReplay(GameEngine engine, int playerID, CustomProperties prop) {
        settings.saveSetting(owner.replayProp, true);

        // If they have completed the game, set the completion status to true for their profile.
        if (playerProperties.isLoggedIn() && engine.ending > 0 && engine.statistics.level >= LEVELS_JAN) {
            settings.hasCompletedGame = true;
            settings.saveSettingPlayer(playerProperties);
        }

        // TODO: Rankings
    }
}
