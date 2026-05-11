package zeroxfc.nullpo.custom.libs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.util.CustomProperties;
import org.apache.log4j.Logger;

/** Representation of an object that holds the settings that a mode uses. */
public abstract class ModeSettings {
    protected static final Logger log = Logger.getLogger(ModeSettings.class);

    public final String propRoot;
    public final ProfileProperties playerProperties;

    protected ModeSettings(String propRoot, ProfileProperties playerProperties) {
        this.propRoot = propRoot;
        this.playerProperties = playerProperties;
    }

    // Construct a property path for settings and rankings.
    public final String propPath(Object... path) {
        return joinPropPath(propRoot, path);
    }

    public abstract void loadSetting(CustomProperties prop, boolean isReplay);
    public abstract void saveSetting(CustomProperties prop, boolean forReplay);

    public abstract void loadSettingPlayer(ProfileProperties prop);
    public abstract void saveSettingPlayer(ProfileProperties prop);

    public abstract void loadRanking(GameManager owner, String ruleName);
    public abstract void saveRanking(GameManager owner, String ruleName);

    public abstract void loadRankingPlayer(ProfileProperties prop, String ruleName);
    public abstract void saveRankingPlayer(ProfileProperties prop, String ruleName);

    public void commitSettingAndRank(EventReceiver receiver, GameManager owner) {
        receiver.saveModeConfig(owner.modeConfig);
    }

    public void commitPlayerSettingAndRank(ProfileProperties playerProperties) {
        if (!playerProperties.isLoggedIn()) return;
        playerProperties.saveProfileConfig();
    }

    // Settings properties handling annotation framework. Intricacies for version saving and other such things
    // need to be handled manually, as this is only for automating the simple properties.

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Property {
        String[] path();
        boolean isGlobal() default true;
        boolean isPlayer() default true;
    }

    // Only applicable to primitive and String fields.
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface PropertyDefault {
        byte byteValue() default 0;
        short shortValue() default 0;
        int intValue() default 0;
        long longValue() default 0;
        float floatValue() default 0.0f;
        double doubleValue() default 0.0;
        boolean booleanValue() default false;
        char charValue() default '\0';
        String stringValue() default "";
    }

    // Use these annotations for non-primitive properties.
    // Non-primitive properties need a codec.

    // XXX: PLEASE USE DISTINCT TYPES / CLASSES OR WRAPPERS THAT CANNOT BE DIRECTLY TYPECAST INTO EACH OTHER.
    //      MORE THAN ONE ANNOTATED CODEC OF THE SAME TYPE IN THE SAME CLASS WILL RESULT IN UNKNOWN BEHAVIOUR.

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface StaticCodec {}

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface InstanceCodec {}

    protected static String joinPropPath(String root, Object... path) {
        final StringBuilder sb = new StringBuilder(root);
        for (final Object segment : path) sb.append('.').append(segment);

        return sb.toString();
    }

    protected static String joinPropPaths(String root, Object[] paths) {
        return joinPropPath(root, paths);
    }

    /**
     * Generate a simple settings handler for a settings object.
     *
     * @param settingsObject Setting object holding mode settings.
     * @return Handler that automatically handles annotated fields.
     */
    @SuppressWarnings("unchecked")
    public static <S extends ModeSettings> SettingsHandler generateSettingsHandler(S settingsObject) {
        final Class<S> settingsClass = (Class<S>) settingsObject.getClass();
        final String propRoot = settingsObject.propRoot;

        final List<PropertyCodec<?>> possibleCodecs = Arrays
            .stream(settingsClass.getFields())
            .filter(f -> f.getAnnotation(StaticCodec.class) != null || f.getAnnotation(InstanceCodec.class) != null)
            .flatMap(
                f -> {
                    final Class<?> type = f.getType();

                    try {
                        if (!PropertyCodec.class.isAssignableFrom(type)) return Stream.empty();
                        else return Stream.of((PropertyCodec<?>) f.get(settingsObject));
                    } catch (IllegalAccessException | IllegalArgumentException | NullPointerException e) {
                        log.error(e);
                        return Stream.empty();
                    }
                }
            )
            .collect(Collectors.toList());

        final List<FieldHandler<S>> settingsHandlers = Arrays
            .stream(settingsClass.getFields())
            .filter(f -> f.getAnnotation(Property.class) != null)
            .map(f -> new FieldHandler<>(settingsObject, propRoot, f, possibleCodecs))
            .collect(Collectors.toList());

        return new SettingsHandler() {
            @Override
            public void loadSetting(CustomProperties prop) {
                for (final FieldHandler<S> fieldHandler : settingsHandlers) fieldHandler.readGlobal(prop);
            }

            @Override
            public void saveSetting(CustomProperties prop) {
                for (final FieldHandler<S> fieldHandler : settingsHandlers) fieldHandler.writeGlobal(prop);
            }

            @Override
            public void loadPlayerSetting(ProfileProperties prop) {
                for (final FieldHandler<S> fieldHandler : settingsHandlers) fieldHandler.readPlayer(prop);
            }

            @Override
            public void savePlayerSetting(ProfileProperties prop) {
                for (final FieldHandler<S> fieldHandler : settingsHandlers) fieldHandler.writePlayer(prop);
            }
        };
    }

    static final class FieldHandler<S extends ModeSettings> {
        private final S modeSettings;
        private final String path;
        private final Field field;
        private final List<PropertyCodec<?>> possibleCodecs;

        private final Property property;
        private final Optional<PropertyDefault> defaults;

        FieldHandler(S modeSettings, String root, Field field, List<PropertyCodec<?>> possibleCodecs) {
            this.modeSettings = modeSettings;
            this.field = field;

            property = Optional
                .ofNullable(field.getAnnotation(Property.class))
                .orElseThrow(() -> new IllegalStateException("No property annotation defined on field: " + field.getName()));
            defaults = Optional.ofNullable(field.getAnnotation(PropertyDefault.class));

            this.possibleCodecs = possibleCodecs;
            this.path = joinPropPaths(root, property.path());
        }

        private boolean handlePrimitiveRead(CustomProperties prop) throws IllegalAccessException {
            final Class<?> fieldType = field.getType();

            if (!defaults.isPresent()) return false;

            if (fieldType.isAssignableFrom(byte.class)) field.setByte(modeSettings, prop.getProperty(path, defaults.get().byteValue()));
            else if (fieldType.isAssignableFrom(short.class)) field.setShort(modeSettings, prop.getProperty(path, defaults.get().shortValue()));
            else if (fieldType.isAssignableFrom(int.class)) field.setInt(modeSettings, prop.getProperty(path, defaults.get().intValue()));
            else if (fieldType.isAssignableFrom(long.class)) field.setLong(modeSettings, prop.getProperty(path, defaults.get().longValue()));
            else if (fieldType.isAssignableFrom(float.class)) field.setFloat(modeSettings, prop.getProperty(path, defaults.get().floatValue()));
            else if (fieldType.isAssignableFrom(double.class)) field.setDouble(modeSettings, prop.getProperty(path, defaults.get().doubleValue()));
            else if (fieldType.isAssignableFrom(boolean.class)) field.setBoolean(modeSettings, prop.getProperty(path, defaults.get().booleanValue()));
            else if (fieldType.isAssignableFrom(char.class)) field.setChar(modeSettings, prop.getProperty(path, defaults.get().charValue()));
            else if (fieldType.isAssignableFrom(String.class)) field.set(modeSettings, prop.getProperty(path, defaults.get().stringValue()));
            else return false;

            return true;
        }

        private boolean handlePrimitiveRead(ProfileProperties prop) throws IllegalAccessException {
            final Class<?> fieldType = field.getType();

            if (!defaults.isPresent()) return false;

            if (fieldType.isAssignableFrom(byte.class)) field.setByte(modeSettings, prop.getProperty(path, defaults.get().byteValue()));
            else if (fieldType.isAssignableFrom(short.class)) field.setShort(modeSettings, prop.getProperty(path, defaults.get().shortValue()));
            else if (fieldType.isAssignableFrom(int.class)) field.setInt(modeSettings, prop.getProperty(path, defaults.get().intValue()));
            else if (fieldType.isAssignableFrom(long.class)) field.setLong(modeSettings, prop.getProperty(path, defaults.get().longValue()));
            else if (fieldType.isAssignableFrom(float.class)) field.setFloat(modeSettings, prop.getProperty(path, defaults.get().floatValue()));
            else if (fieldType.isAssignableFrom(double.class)) field.setDouble(modeSettings, prop.getProperty(path, defaults.get().doubleValue()));
            else if (fieldType.isAssignableFrom(boolean.class)) field.setBoolean(modeSettings, prop.getProperty(path, defaults.get().booleanValue()));
            else if (fieldType.isAssignableFrom(char.class)) field.setChar(modeSettings, prop.getProperty(path, defaults.get().charValue()));
            else if (fieldType.isAssignableFrom(String.class)) field.set(modeSettings, prop.getProperty(path, defaults.get().stringValue()));
            else return false;

            return true;
        }

        private boolean handlePrimitiveWrite(CustomProperties prop) throws IllegalAccessException {
            final Class<?> fieldType = field.getType();

            if (byte.class.isAssignableFrom(fieldType)) prop.setProperty(path, field.getByte(modeSettings));
            else if (short.class.isAssignableFrom(fieldType)) prop.setProperty(path, field.getShort(modeSettings));
            else if (int.class.isAssignableFrom(fieldType)) prop.setProperty(path, field.getInt(modeSettings));
            else if (long.class.isAssignableFrom(fieldType)) prop.setProperty(path, field.getLong(modeSettings));
            else if (float.class.isAssignableFrom(fieldType)) prop.setProperty(path, field.getFloat(modeSettings));
            else if (double.class.isAssignableFrom(fieldType)) prop.setProperty(path, field.getDouble(modeSettings));
            else if (boolean.class.isAssignableFrom(fieldType)) prop.setProperty(path, field.getBoolean(modeSettings));
            else if (char.class.isAssignableFrom(fieldType)) prop.setProperty(path, field.getChar(modeSettings));
            else if (String.class.isAssignableFrom(fieldType)) prop.setProperty(path, field.get(modeSettings).toString());
            else return false;

            return true;
        }

        private boolean handlePrimitiveWrite(ProfileProperties prop) throws IllegalAccessException {
            final Class<?> fieldType = field.getType();

            if (byte.class.isAssignableFrom(fieldType)) prop.setProperty(path, field.getByte(modeSettings));
            else if (short.class.isAssignableFrom(fieldType)) prop.setProperty(path, field.getShort(modeSettings));
            else if (int.class.isAssignableFrom(fieldType)) prop.setProperty(path, field.getInt(modeSettings));
            else if (long.class.isAssignableFrom(fieldType)) prop.setProperty(path, field.getLong(modeSettings));
            else if (float.class.isAssignableFrom(fieldType)) prop.setProperty(path, field.getFloat(modeSettings));
            else if (double.class.isAssignableFrom(fieldType)) prop.setProperty(path, field.getDouble(modeSettings));
            else if (boolean.class.isAssignableFrom(fieldType)) prop.setProperty(path, field.getBoolean(modeSettings));
            else if (char.class.isAssignableFrom(fieldType)) prop.setProperty(path, field.getChar(modeSettings));
            else if (String.class.isAssignableFrom(fieldType)) prop.setProperty(path, field.get(modeSettings).toString());
            else return false;

            return true;
        }

        private void handleObjectRead(CustomProperties prop) throws IllegalAccessException, IllegalArgumentException {
            final Class<?> fieldType = field.getType();

            for (final PropertyCodec<?> codec : possibleCodecs) {
                if (fieldType.isAssignableFrom(codec.getValueClass())) {
                    field.set(modeSettings, codec.load(prop, path));
                    break;
                }
            }
        }

        private void handleObjectRead(ProfileProperties prop) throws IllegalAccessException, IllegalArgumentException {
            final Class<?> fieldType = field.getType();

            for (final PropertyCodec<?> codec : possibleCodecs) {
                if (fieldType.isAssignableFrom(codec.getValueClass())) {
                    field.set(modeSettings, codec.loadPlayer(prop, path));
                    break;
                }
            }
        }

        private void handleObjectWrite(CustomProperties prop) throws IllegalAccessException, IllegalArgumentException {
            final Class<?> fieldType = field.getType();

            for (final PropertyCodec<?> codec : possibleCodecs) {
                if (codec.getValueClass().isAssignableFrom(fieldType)) {
                    new UnsafePropertyCodec(codec).save(prop, path, field.get(modeSettings));
                    break;
                }
            }
        }

        private void handleObjectWrite(ProfileProperties prop) throws IllegalAccessException, IllegalArgumentException {
            final Class<?> fieldType = field.getType();

            for (final PropertyCodec<?> codec : possibleCodecs) {
                if (codec.getValueClass().isAssignableFrom(fieldType)) {
                    new UnsafePropertyCodec(codec).savePlayer(prop, path, field.get(modeSettings));
                    break;
                }
            }
        }

        public void readGlobal(CustomProperties prop) {
            if (!property.isGlobal()) return;

            try {
                if (!handlePrimitiveRead(prop)) handleObjectRead(prop);
            } catch (IllegalAccessException | IllegalArgumentException e) {
                log.error("Failed to assign global field (" + field.getName() + "): ");
                log.error(e);
            }
        }

        public void readPlayer(ProfileProperties prop) {
            if (!property.isPlayer() || !prop.isLoggedIn()) return;

            try {
                if (!handlePrimitiveRead(prop)) handleObjectRead(prop);
            } catch (IllegalAccessException | IllegalArgumentException e) {
                log.error("Failed to assign player field (" + field.getName() + "): ");
                log.error(e);
            }
        }

        public void writeGlobal(CustomProperties prop) {
            if (!property.isGlobal()) return;

            try {
                if (!handlePrimitiveWrite(prop)) handleObjectWrite(prop);
            } catch (IllegalAccessException | IllegalArgumentException e) {
                log.error("Failed to read global field (" + field.getName() + "): ");
                log.error(e);
            }
        }

        public void writePlayer(ProfileProperties prop) {
            if (!property.isPlayer() || !prop.isLoggedIn()) return;

            try {
                if (!handlePrimitiveWrite(prop)) handleObjectWrite(prop);
            } catch (IllegalAccessException | IllegalArgumentException e) {
                log.error("Failed to read player field (" + field.getName() + "): ");
                log.error(e);
            }
        }
    }

    // Settings handler object, generated using above method. Used for simple properties only.
    public static abstract class SettingsHandler {
        public abstract void loadSetting(CustomProperties prop);
        public abstract void saveSetting(CustomProperties prop);
        public abstract void loadPlayerSetting(ProfileProperties prop);
        public abstract void savePlayerSetting(ProfileProperties prop);
    }
}
