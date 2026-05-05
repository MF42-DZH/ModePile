package zeroxfc.nullpo.custom.libs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.subsystem.mode.DummyMode;
import org.apache.log4j.Logger;

/**
 * Helper utility for building settings menus for modes.
 */
public final class MenuBuilder {
    private static final Logger log = Logger.getLogger(MenuBuilder.class);

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
     * @param header       Setting header text
     * @param headerColour Setting header colour
     * @param changer      Settings modifier (will receive -1 or 1 from updateCursor)
     * @param asString     Setting as String
     * @return Setting menu builder with one setting.
     */
    public MenuBuilder addSetting(
        String header, int headerColour, Consumer<Integer> changer,
        Supplier<String> asString
    ) {
        settings.add(new Setting(header, headerColour, changer, asString));
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
            // 10 settings per page.
            final int pages = settings.size() / 10;
            final int currentPage = engine.statc[2] / 10;

            if (pages >= 1) {
                drawMenuInvoker.invoke(
                    mode,
                    engine, playerID, receiver, 21, EventReceiver.COLOR_RED, "kn PAGE " + (currentPage + 1) / (pages + 1), -1
                );
            }

            for (int i = 0; i < Math.min(10, settings.size() - currentPage * 10); ++i) {
                final int y = (2 * i) + startY;
                final int statc = i + currentPage * 10;

                final Setting setting = settings.get(statc);

                varargsProxy[0] = setting.header;
                varargsProxy[1] = setting.asString.get();

                drawMenuInvoker.invoke(
                    mode,
                    engine, playerID, receiver, y, setting.headerColour, statc, varargsProxy
                );
            }
        }
    }

    // Setting container type.
    public static final class Setting {
        private final String header;
        private final int headerColour;
        private final Consumer<Integer> changer;
        private final Supplier<String> asString;

        public Setting(String header, int headerColour, Consumer<Integer> changer, Supplier<String> asString) {
            this.header = header;
            this.headerColour = headerColour;
            this.changer = changer;
            this.asString = asString;
        }

        public static final BiFunction<String, Integer, Function<Consumer<Integer>, Function<Supplier<String>, Setting>>> curryConstructor =
            (header, headerColour) -> changer -> asString -> new Setting(header, headerColour, changer, asString);
    }

    // Setting menu autogenerator framework.
    // Order values are used as the keys.

    // Placed on settings object PUBLIC modifiable fields.
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface SettingItem {
        String header();
        int headerColour();
        int order();
    }

    // Should be placed on PUBLIC methods with the signature "void NAME(int change)".
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface SettingChanger {
        int order();
    }

    // Should be placed on PUBLIC methods with the signature "String NAME()".
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface SettingPrinter {
        int order();
    }

    /**
     * Generate a menu from an annotated settings object.
     *
     * @param mode Game mode for which the settings are used in.
     * @param settingsObject Object holding settings and annotations.
     * @return Menu screen for settings.
     */
    @SuppressWarnings("unchecked")
    public static <S> Menu generateMenu(DummyMode mode, S settingsObject) {
        final Class<S> settingsClass = (Class<S>) settingsObject.getClass();

        final NavigableMap<Integer, SettingItem> settingFields = new TreeMap<>();
        final Map<Integer, Method> settingChangers = new HashMap<>();
        final Map<Integer, Method> settingPrinters = new HashMap<>();
        final List<Setting> foundSettings = new LinkedList<>();

        for (final Field field : settingsClass.getFields()) {
            for (final SettingItem itemAnnotation : field.getDeclaredAnnotationsByType(SettingItem.class)) {
                settingFields.put(itemAnnotation.order(), itemAnnotation);
                break;
            }
        }

        for (final Method method : settingsClass.getMethods()) {
            for (final SettingChanger changerAnnotation : method.getDeclaredAnnotationsByType(SettingChanger.class)) {
                settingChangers.put(changerAnnotation.order(), method);
                break;
            }
        }

        for (final Method method : settingsClass.getMethods()) {
            for (final SettingPrinter printerAnnotation : method.getDeclaredAnnotationsByType(SettingPrinter.class)) {
                settingPrinters.put(printerAnnotation.order(), method);
                break;
            }
        }

        for (final Map.Entry<Integer, SettingItem> settingField : settingFields.entrySet()) {
            final Optional<Method> changer = Optional.ofNullable(settingChangers.get(settingField.getKey()));
            final Optional<Method> printer = Optional.ofNullable(settingPrinters.get(settingField.getKey()));

            changer.ifPresent(c ->
                printer.ifPresent(p -> {
                    final Consumer<Integer> settingChanger = change -> {
                        try {
                            c.invoke(settingsObject, change);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            log.error(mode.getName() + ": Failed to change setting!");
                            log.error(e);
                        }
                    };

                    final Supplier<String> settingPrinter = () -> {
                        try {
                            return p.invoke(settingsObject).toString();
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            log.error(mode.getName() + ": Failed to stringify setting!");
                            log.error(e);

                            return "null";
                        }
                    };

                    foundSettings.add(
                        new Setting(
                            settingField.getValue().header(),
                            settingField.getValue().headerColour(),
                            settingChanger,
                            settingPrinter
                        )
                    );
                })
            );
        }

        return new MenuBuilder(mode, foundSettings).build();
    }
}
