package zeroxfc.nullpo.custom.modes;

import java.util.function.IntFunction;
import mu.nu.nullpo.game.component.BGMStatus;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.SpeedParam;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.game.subsystem.mode.DummyMode;
import mu.nu.nullpo.util.GeneralUtil;
import org.apache.log4j.Logger;
import zeroxfc.nullpo.custom.libs.CustomResourceHolder;
import zeroxfc.nullpo.custom.libs.LevelTableBuilder;
import zeroxfc.nullpo.custom.libs.ProfileProperties;
import zeroxfc.nullpo.custom.libs.RendererExtension;
import zeroxfc.nullpo.custom.libs.SpeedTableBuilder;
import zeroxfc.nullpo.custom.libs.backgroundtypes.AnimatedBackgroundHook;
import zeroxfc.nullpo.custom.libs.mixins.HasCustomFieldDrawing;
import zeroxfc.nullpo.custom.libs.mixins.HasCustomOnMove;
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

    // TODO: Shuffle the levels so that February is the first month.
    private static final int LEVELS_JAN = 31 * HOURS_IN_DAY;
    private static final int LEVELS_FEB = LEVELS_JAN + (28 * HOURS_IN_DAY);
    private static final int LEVELS_MAR = LEVELS_FEB + (31 * HOURS_IN_DAY);
    private static final int LEVELS_APR = LEVELS_MAR + (30 * HOURS_IN_DAY);
    private static final int LEVELS_MAY = LEVELS_APR + (31 * HOURS_IN_DAY);
    private static final int LEVELS_JUN = LEVELS_MAY + (30 * HOURS_IN_DAY);
    private static final int LEVELS_JUL = LEVELS_JUN + (31 * HOURS_IN_DAY);
    private static final int LEVELS_AUG = LEVELS_JUL + (31 * HOURS_IN_DAY);
    private static final int LEVELS_SEP = LEVELS_AUG + (30 * HOURS_IN_DAY);
    private static final int LEVELS_OCT = LEVELS_SEP + (31 * HOURS_IN_DAY);
    private static final int LEVELS_NOV = LEVELS_OCT + (30 * HOURS_IN_DAY);
    private static final int LEVELS_DEC = LEVELS_NOV + (31 * HOURS_IN_DAY); // Also the max level.

    private static final IntFunction<Integer> NEXT_SECTION_LEVELS = LevelTableBuilder.<Integer>createNew()
        .addValue(LEVELS_JAN, LEVELS_JAN)
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
        .addTerminalValue(-1)
        .buildLevelTable();

    private static final IntFunction<String> MONTH_NAME_TABLE = LevelTableBuilder.<String>createNew()
        .addValue("JAN", LEVELS_JAN)
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
        .addTerminalValue("INVALID")
        .buildLevelTable();

    private static final IntFunction<Integer> BGM_TABLE = LevelTableBuilder.<Integer>createNew()
        .addValue(BGMStatus.BGM_NORMAL1, LEVELS_MAR)
        .addValue(BGMStatus.BGM_NORMAL1, LEVELS_JUL)
        .addValue(BGMStatus.BGM_NORMAL1, LEVELS_OCT)
        .addTerminalValue(BGMStatus.BGM_NORMAL4)
        .buildLevelTable();

    private static String levelToString(int level) {
        final String month = MONTH_NAME_TABLE.apply(level);
        final int levelsInMonth = level - NEXT_SECTION_LEVELS.apply(level);
        return String.format("%d %s %d:00", levelsInMonth / 24, month, levelsInMonth % 24);
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
    public boolean onSetting(GameEngine engine, int playerID) {
        if (!engine.owner.replayMode) {
            // Configuration changes
            int change = updateCursor(engine, 1);

            if (change != 0) {
                engine.playSE("change");

                switch (engine.statc[2]) {
                    case 0:
                        int selectedPerk = settings.perk.ordinal() + change;
                        if (selectedPerk < 0) selectedPerk = SeasonPerk.values().length - 1;
                        else if (selectedPerk >= SeasonPerk.values().length) selectedPerk = 0;

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
            perkString = String.format("%s (%s)", split[0], split[1].charAt(1));
        }

        drawMenu(engine, playerID, receiver, 0, EventReceiver.COLOR_YELLOW, 0,
            "PERK", perkString
        );
        drawMenu(engine, playerID, receiver, 2, EventReceiver.COLOR_BLUE, 1,
            "FULL GHOST", GeneralUtil.getONorOFF(settings.fullGhost)
        );
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
}
