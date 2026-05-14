package zeroxfc.nullpo.custom.libs;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.util.CustomProperties;
import zeroxfc.nullpo.custom.libs.types.ObjectAlignment;

/** Achievements tracking system for logged in players. */
public abstract class PlayerAchievements extends ModeSettings {
    public static final int SUGGESTED_POPUP_WIDTH = 400;

    private final CustomResourceHolder customGraphics;
    private static final String TROPHY_IMAGE = "TROPHY";

    private final List<Achievement<?>> achievements = new LinkedList<>();

    // IMPORTANT: Call this constructor in your own public constructor.
    protected PlayerAchievements(String propRoot, ProfileProperties playerProperties) {
        super(propRoot + ".achievements", playerProperties);

        this.customGraphics = new CustomResourceHolder(1);
        customGraphics.loadImage("res/graphics/achievement-trophy.png", TROPHY_IMAGE);

        SoundLoader.Sounds.Achievements.loadAllSounds();
    }

    // V is a parameter being tracked by the achievement. Use Boolean for no-tracking achievements.
    public final class Achievement<V> {
        private static final float PROGRESS_TEXT_SIZE = 0.75f;

        private final GameTextUtilities.TextBlock name;
        private final GameTextUtilities.TextBlock description;
        private final boolean nonCounting;

        private final String path;
        private final PropertyCodec<V> codec;

        private V value;
        private final V defaultValue;
        private final V targetValue;
        private final BiFunction<V, V, Boolean> satisfyingPredicate;
        private final Function<V, Double> progress;
        private final BiFunction<V, V, String> stringifyProgress;

        public Achievement(
            GameTextUtilities.TextBlock name,
            GameTextUtilities.TextBlock description,
            boolean isNonCounting,
            String path,
            PropertyCodec<V> codec,
            V defaultValue,
            V targetValue,
            BiFunction<V, V, Boolean> satisfyingPredicate,
            Function<V, Double> progress,
            BiFunction<V, V, String> stringifyProgress
        ) {
            this.name = name;
            this.description = description;
            this.nonCounting = isNonCounting;

            this.path = path;
            this.codec = codec;

            this.value = defaultValue;
            this.defaultValue = defaultValue;
            this.targetValue = targetValue;
            this.satisfyingPredicate = satisfyingPredicate;
            this.progress = progress;
            this.stringifyProgress = stringifyProgress;
        }

        public V getValue() {
            if (!playerProperties.isLoggedIn()) return defaultValue;
            return value;
        }

        public V getTargetValue() {
            return targetValue;
        }

        public boolean isNonCounting() {
            return this.nonCounting;
        }

        public boolean hasBeenObtained() {
            if (!playerProperties.isLoggedIn()) return false;
            return satisfyingPredicate.apply(value, targetValue);
        }

        public boolean setValue(V value) {
            if (!playerProperties.isLoggedIn()) return false;
            final boolean obtainedBefore = hasBeenObtained();

            this.value = value;
            save();

            return hasBeenObtained() && !obtainedBefore;
        }

        public boolean modifyValue(UnaryOperator<V> modifier) {
            return setValue(modifier.apply(this.value));
        }

        public void save() {
            codec.savePlayer(
                playerProperties,
                propPath(path),
                value
            );
        }

        public void load() {
            this.value = codec.loadPlayer(
                playerProperties,
                propPath(path),
                defaultValue
            );
        }

        public int getAchievementWidth(int minWidth) {
            // 4px-TROPHY(64)-4px-DESC-4px
            return Math.max(minWidth, 4 + 64 + 4 + Math.max(name.getWidth(), description.getWidth()) + 4);
        }

        public int getAchievementHeight() {
            // 4px-name-4px-desc-4px-progress(12)-4px
            return Math.max(
                progress == null
                    ? 4 + name.getHeight() + 4 + description.getHeight() + 4
                    : 4 + name.getHeight() + 4 + description.getHeight() + 4 + 12 + 4,
                4 + 64 + 4 // if the trophy is larger than the name, desc and bar section, this will be used instead.
            );
        }

        public void drawAchievement(
            RendererExtension extension,
            PrimitiveDrawingHook drawing,
            EventReceiver receiver,
            GameEngine engine,
            int x,
            int y,
            int minWidth
        ) {
            final int rectWidth = getAchievementWidth(minWidth);
            final int rectHeight = getAchievementHeight();

            // Background
            drawing.drawRectangle(
                receiver,
                x, y,
                rectWidth, rectHeight,
                0, 0, 0, 192, true
            );

            // Trophy image.
            final int srcX;
            if (hasBeenObtained() && nonCounting) srcX = 128;
            else if (hasBeenObtained()) srcX = 64;
            else srcX = 0;

            final int trpOffsetY = (rectHeight - 64) / 2;

            final int colourComponent = (hasBeenObtained() || !nonCounting) ? 255 : 160;

            customGraphics.drawOffsetImage(
                engine, TROPHY_IMAGE,
                x + 4, y + trpOffsetY,
                64, 64,
                srcX, 0,
                64, 64,
                colourComponent, colourComponent, colourComponent, 255
            );

            // Name, Description and Progress Bar
            final int textAndBarX = x + 4 + 64 + 4;
            final int nameY = y + 4;
            final int descY = nameY + name.getHeight() + 4;

            GameTextUtilities.drawDirectTextBlock(
                engine,
                textAndBarX, nameY,
                false,
                name
            );

            GameTextUtilities.drawDirectTextBlock(
                engine,
                textAndBarX, descY,
                false,
                description
            );

            if (progress == null) return;

            final int barWidth = rectWidth - (4 + 64 + 4 + 4);
            final int barY = y + (rectHeight - (4 + 12));

            extension.drawAlignedSpeedMeter(
                receiver,
                textAndBarX, barY + 2,
                ObjectAlignment.TOP_LEFT,
                (float) progress.apply(value).doubleValue(),
                barWidth / 42f, 2f,
                RendererExtension.SPEED_METER_RED,
                RendererExtension.SPEED_METER_GREEN
            );

            final GameTextUtilities.Text progressText = GameTextUtilities.Text.custom(
                stringifyProgress != null ? stringifyProgress.apply(value, targetValue) : (value + "/" + targetValue),
                EventReceiver.COLOR_WHITE,
                PROGRESS_TEXT_SIZE
            );

            GameTextUtilities.drawAlignedText(
                engine,
                textAndBarX + (barWidth / 2), barY,
                progressText,
                ObjectAlignment.TOP_MIDDLE
            );
        }
    }

    public static final class AchievementPopup {
        private final Achievement<?> achievement;
        private final int totalLife;

        private int lifetime;

        public AchievementPopup(Achievement<?> achievement, int totalLife) {
            this.achievement = achievement;
            this.totalLife = totalLife;
            this.lifetime = 0;
        }

        public boolean update() {
            return lifetime++ >= totalLife;
        }

        public void draw(
            RendererExtension extension,
            PrimitiveDrawingHook drawing,
            EventReceiver receiver,
            GameEngine engine
        ) {
            final int minWidth = 400;
            final int x;

            if (lifetime < 30) x = (int) Interpolation.tanStep(640, 640 - achievement.getAchievementWidth(minWidth), lifetime / 30.0);
            else if (lifetime > (totalLife - 30)) x = (int) Interpolation.tanStep(640 - achievement.getAchievementWidth(minWidth), 640, (lifetime - (totalLife - 30)) / 30.0);
            else x = 640 - achievement.getAchievementWidth(minWidth);

            achievement.drawAchievement(extension, drawing, receiver, engine, x, 0, minWidth);
        }
    }

    // Register new achievements here:
    public final <V> Achievement<V> registerAchievement(
        GameTextUtilities.TextBlock name,
        GameTextUtilities.TextBlock description,
        boolean isNonCounting,
        String path,
        PropertyCodec<V> codec,
        V defaultValue,
        V targetValue,
        BiFunction<V, V, Boolean> satisfyingPredicate,
        Function<V, Double> progress,
        BiFunction<V, V, String> stringifyProgres
    ) {
        final Achievement<V> achievement = new Achievement<>(
            name, description, isNonCounting,
            path, codec,
            defaultValue, targetValue,
            satisfyingPredicate, progress, stringifyProgres
        );

        achievements.add(achievement);

        return achievement;
    }

    public final class AchievementMenu {
        private final int headerColour;
        private int selectedPage;

        private final List<Integer> achievementsInPage;

        public AchievementMenu(int headerColour) {
            this.headerColour = headerColour;
            this.selectedPage = 0;

            this.achievementsInPage = new LinkedList<>();

            // Reserving 48px of height for header, leaving 432px left.
            // We leave off another 8px at the bottom at minimum, so 424px is left.
            // We leave off another 24px for the counted achievement counter, so 400px left.
            final int fullBudget = 400;
            int yBudget = fullBudget;
            int achievementCount = 0;

            for (int i = 0; i < achievements.size();) {
                final Achievement<?> achievement = achievements.get(i);

                if (achievement.getAchievementHeight() + 4 <= yBudget) {
                    ++achievementCount;
                    ++i;

                    yBudget -= achievement.getAchievementHeight() + 4;
                } else if (achievement.getAchievementHeight() <= yBudget) {
                    achievementsInPage.add(achievementCount + 1);

                    achievementCount = 0;
                    yBudget = fullBudget;

                    ++i;
                } else {
                    achievementsInPage.add(achievementCount);
                    achievementCount = 0;
                    yBudget = fullBudget;
                }
            }

            if (achievementCount > 0) achievementsInPage.add(achievementCount);
        }

        public void updateMenu(GameEngine engine) {
            if (engine.ctrl.isPress(Controller.BUTTON_DOWN)) {
                if (engine.statc[1]++ % 15 == 0) advancePage(engine);
            } else if (engine.ctrl.isPress(Controller.BUTTON_UP)) {
                if (engine.statc[1]++ % 15 == 0) rewindPage(engine);
            } else {
                engine.statc[1] = 0;
            }

            if (engine.ctrl.isPush(Controller.BUTTON_B)) {
                engine.playSE("decide");

                engine.stat = GameEngine.STAT_SETTING;
                engine.resetStatc();
            }
        }

        private void advancePage(GameEngine engine) {
            selectedPage += 1;
            if (selectedPage >= achievementsInPage.size()) selectedPage = 0;

            engine.playSE("change");
        }

        private void rewindPage(GameEngine engine) {
            selectedPage -= 1;
            if (selectedPage < 0) selectedPage = achievementsInPage.size() - 1;

            engine.playSE("change");
        }

        // The screen is 640x480
        public void drawMenu(RendererExtension extension, PrimitiveDrawingHook drawing, EventReceiver receiver, GameEngine engine) {
            // Header
            GameTextUtilities.drawAlignedText(
                engine,
                320, 24,
                GameTextUtilities.Text.custom("ACHIEVEMENTS " + (selectedPage + 1) + "/" + achievementsInPage.size(), headerColour, 2f),
                ObjectAlignment.MIDDLE_MIDDLE
            );

            // Achievements
            int startOffset = 0;
            for (int page = 0; page < selectedPage; ++page) startOffset += achievementsInPage.get(page);

            int y = 48;
            for (int ix = startOffset; ix < startOffset + achievementsInPage.get(selectedPage); ++ix) {
                final Achievement<?> achievement = achievements.get(ix);

                achievement.drawAchievement(
                    extension, drawing, receiver, engine,
                    32, y,
                    640 - 64
                );

                y += achievement.getAchievementHeight() + 4;
            }

            // Counter
            final long countedAchievements = achievements.stream().filter(a -> !a.isNonCounting()).count();
            final long obtainedCounted = achievements.stream().filter(a -> !a.isNonCounting() && a.hasBeenObtained()).count();

            final long uncountedAchievements = achievements.stream().filter(a -> a.isNonCounting()).count();
            final long obtainedUncounted = achievements.stream().filter(a -> a.isNonCounting() && a.hasBeenObtained()).count();

            final int counterY = 64 + 384 + 12;
            GameTextUtilities.drawAlignedTextBlock(
                engine,
                320, counterY,
                false,
                GameTextUtilities.TextBlock.of(
                    GameTextUtilities.TextJustification.CENTRE,
                    GameTextUtilities.Text.custom("OBTAINED (NON-?): " + obtainedCounted + "/" + countedAchievements, EventReceiver.COLOR_WHITE, 1f),
                    GameTextUtilities.Text.newLine(),
                    GameTextUtilities.Text.customMixColor(
                        "OBTAINED (?): " + obtainedUncounted + "/" + uncountedAchievements,
                        EventReceiver.COLOR_WHITE,
                        160, 160, 160, 160,
                        0.5f
                    )
                ),
                ObjectAlignment.MIDDLE_MIDDLE
            );
        }
    }

    public AchievementMenu getMenu() {
        return new AchievementMenu(playerProperties.headerColour);
    }

    // Do not override these methods below in your own class.

    public final void loadAchievements() {
        loadSettingPlayer();
    }

    public final void saveAchievements() {
        saveSettingPlayer();
    }

    @Override
    public void loadSettingPlayer() {
        if (!playerProperties.isLoggedIn()) return;
        for (final Achievement<?> achievement : achievements) achievement.load();
    }

    @Override
    public void saveSettingPlayer() {
        if (!playerProperties.isLoggedIn()) return;
        for (final Achievement<?> achievement : achievements) achievement.save();
    }

    // Do not use these methods below for player achievements, they don't do anything.

    @Override
    public void loadSetting(CustomProperties prop, boolean isReplay) {
        return;
    }

    @Override
    public void saveSetting(CustomProperties prop, boolean forReplay) {
        return;
    }

    @Override
    public void loadRanking(GameManager owner, String ruleName) {
        return;
    }

    @Override
    public void saveRanking(GameManager owner, String ruleName) {
        return;
    }

    @Override
    public void loadRankingPlayer(String ruleName) {
        return;
    }

    @Override
    public void saveRankingPlayer(String ruleName) {
        return;
    }
}
