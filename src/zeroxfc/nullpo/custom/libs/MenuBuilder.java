package zeroxfc.nullpo.custom.libs;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.subsystem.mode.DummyMode;

/**
 * Helper utility for building settings menus for modes.
 */
public final class MenuBuilder {
    private final DummyMode mode;
    private final List<Setting> settings;

    private MenuBuilder(DummyMode mode, List<Setting> settingList) {
        this.mode = mode;
        settings = settingList;
    }

    /**
     * Create a new settings menu builder.
     *
     * @return Setting menu builder with no settings.
     */
    public static MenuBuilder forMode(DummyMode mode) {
        final List<Setting> settings = new LinkedList<>();
        return new MenuBuilder(mode, settings);
    }

    /**
     * Add a setting to an existing setting menu builder.
     *
     * @param changer Settings modifier (will receive -1 or 1 from updateCursor)
     * @param asString Setting as String
     * @param header Setting header text
     * @param headerColour Setting header colour
     *
     * @return Setting menu builder with one setting.
     */
    public MenuBuilder addSetting(
        Consumer<Integer> changer,
        Supplier<String> asString,
        String header,
        int headerColour
    ) {
        settings.add(new Setting(changer, asString, header, headerColour));
        return this;
    }

    /** Finalize the settings menu. */
    public Menu build() {
        return new Menu();
    }

    private static final Mirror.MethodInvoker<DummyMode, Integer> updateCursorInvoker =
        Mirror.getMethodInvoker(DummyMode.class, "updateCursor", GameEngine.class, int.class, int.class);

    private static final Mirror.MethodInvoker<DummyMode, Object> drawMenuInvoker =
        Mirror.getMethodInvoker(DummyMode.class, "drawMenu", GameEngine.class, int.class, EventReceiver.class, int.class, int.class, int.class, String[].class);

    public final class Menu {
        /**
         * Update the current settings based on player action.
         */
        public void updateSettings(GameEngine engine, int playerID) {
            final int change = updateCursorInvoker.invoke(mode, engine, settings.size() - 1, playerID);
            if (change == 0) return;

            engine.playSE("change");
            settings.get(engine.statc[2]).changer.accept(change);
        }

        private final String[] varargsProxy = new String[2];

        /**
         * Draws the current settings menu.
         */
        public void renderSettings(GameEngine engine, int playerID, EventReceiver receiver, int startY) {
            for (int i = 0; i < settings.size(); ++i) {
                final int y = (2 * i) + startY;
                final Setting setting = settings.get(i);

                varargsProxy[0] = setting.header;
                varargsProxy[1] = setting.asString.get();

                drawMenuInvoker.invoke(
                    mode,
                    engine, playerID, receiver, y, setting.headerColour, i, varargsProxy
                );
            }
        }
    }

    // Setting container type.
    public static final class Setting {
        private final Consumer<Integer> changer;
        private final Supplier<String> asString;
        private final String header;
        private final int headerColour;

        public Setting(Consumer<Integer> changer, Supplier<String> asString, String header, int headerColour) {
            this.changer = changer;
            this.asString = asString;
            this.header = header;
            this.headerColour = headerColour;
        }
    }
}
