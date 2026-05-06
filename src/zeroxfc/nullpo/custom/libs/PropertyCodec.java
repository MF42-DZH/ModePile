package zeroxfc.nullpo.custom.libs;

import mu.nu.nullpo.util.CustomProperties;

// Encodes a value that can be saved to properties.
public interface PropertyCodec<V> {
    void save(CustomProperties properties, String propPath, V value);
    void savePlayer(ProfileProperties properties, String propPath, V value);

    // DefaultValue may be provided as null in derived codecs.
    V load(CustomProperties properties, String propPath, V defaultValue);
    V loadPlayer(ProfileProperties properties, String propPath, V defaultValue);

    // Predefined codec instances for simple things:

    final class ByteCodec implements PropertyCodec<Byte> {
        public static final ByteCodec INSTANCE = new ByteCodec();
        private ByteCodec() {}

        @Override
        public void save(CustomProperties properties, String propPath, Byte value) {
            properties.setProperty(propPath, (byte) value);
        }

        @Override
        public void savePlayer(ProfileProperties properties, String propPath, Byte value) {
            properties.setProperty(propPath, (byte) value);
        }

        @Override
        public Byte load(CustomProperties properties, String propPath, Byte defaultValue) {
            return properties.getProperty(propPath, (byte) defaultValue);
        }

        @Override
        public Byte loadPlayer(ProfileProperties properties, String propPath, Byte defaultValue) {
            return properties.getProperty(propPath, (byte) defaultValue);
        }
    }

    final class ShortCodec implements PropertyCodec<Short> {
        public static final ShortCodec INSTANCE = new ShortCodec();
        private ShortCodec() {}

        @Override
        public void save(CustomProperties properties, String propPath, Short value) {
            properties.setProperty(propPath, (short) value);
        }

        @Override
        public void savePlayer(ProfileProperties properties, String propPath, Short value) {
            properties.setProperty(propPath, (short) value);
        }

        @Override
        public Short load(CustomProperties properties, String propPath, Short defaultValue) {
            return properties.getProperty(propPath, (short) defaultValue);
        }

        @Override
        public Short loadPlayer(ProfileProperties properties, String propPath, Short defaultValue) {
            return properties.getProperty(propPath, (short) defaultValue);
        }
    }

    final class IntegerCodec implements PropertyCodec<Integer> {
        public static final IntegerCodec INSTANCE = new IntegerCodec();
        private IntegerCodec() {}

        @Override
        public void save(CustomProperties properties, String propPath, Integer value) {
            properties.setProperty(propPath, (int) value);
        }

        @Override
        public void savePlayer(ProfileProperties properties, String propPath, Integer value) {
            properties.setProperty(propPath, (int) value);
        }

        @Override
        public Integer load(CustomProperties properties, String propPath, Integer defaultValue) {
            return properties.getProperty(propPath, (int) defaultValue);
        }

        @Override
        public Integer loadPlayer(ProfileProperties properties, String propPath, Integer defaultValue) {
            return properties.getProperty(propPath, (int) defaultValue);
        }
    }

    final class LongCodec implements PropertyCodec<Long> {
        public static final LongCodec INSTANCE = new LongCodec();
        private LongCodec() {}

        @Override
        public void save(CustomProperties properties, String propPath, Long value) {
            properties.setProperty(propPath, (long) value);
        }

        @Override
        public void savePlayer(ProfileProperties properties, String propPath, Long value) {
            properties.setProperty(propPath, (long) value);
        }

        @Override
        public Long load(CustomProperties properties, String propPath, Long defaultValue) {
            return properties.getProperty(propPath, (long) defaultValue);
        }

        @Override
        public Long loadPlayer(ProfileProperties properties, String propPath, Long defaultValue) {
            return properties.getProperty(propPath, (long) defaultValue);
        }
    }

    final class FloatCodec implements PropertyCodec<Float> {
        public static final FloatCodec INSTANCE = new FloatCodec();
        private FloatCodec() {}

        @Override
        public void save(CustomProperties properties, String propPath, Float value) {
            properties.setProperty(propPath, (float) value);
        }

        @Override
        public void savePlayer(ProfileProperties properties, String propPath, Float value) {
            properties.setProperty(propPath, (float) value);
        }

        @Override
        public Float load(CustomProperties properties, String propPath, Float defaultValue) {
            return properties.getProperty(propPath, (float) defaultValue);
        }

        @Override
        public Float loadPlayer(ProfileProperties properties, String propPath, Float defaultValue) {
            return properties.getProperty(propPath, (float) defaultValue);
        }
    }

    final class DoubleCodec implements PropertyCodec<Double> {
        public static final DoubleCodec INSTANCE = new DoubleCodec();
        private DoubleCodec() {}

        @Override
        public void save(CustomProperties properties, String propPath, Double value) {
            properties.setProperty(propPath, (double) value);
        }

        @Override
        public void savePlayer(ProfileProperties properties, String propPath, Double value) {
            properties.setProperty(propPath, (double) value);
        }

        @Override
        public Double load(CustomProperties properties, String propPath, Double defaultValue) {
            return properties.getProperty(propPath, (double) defaultValue);
        }

        @Override
        public Double loadPlayer(ProfileProperties properties, String propPath, Double defaultValue) {
            return properties.getProperty(propPath, (double) defaultValue);
        }
    }

    final class BooleanCodec implements PropertyCodec<Boolean> {
        public static final BooleanCodec INSTANCE = new BooleanCodec();
        private BooleanCodec() {}

        @Override
        public void save(CustomProperties properties, String propPath, Boolean value) {
            properties.setProperty(propPath, (boolean) value);
        }

        @Override
        public void savePlayer(ProfileProperties properties, String propPath, Boolean value) {
            properties.setProperty(propPath, (boolean) value);
        }

        @Override
        public Boolean load(CustomProperties properties, String propPath, Boolean defaultValue) {
            return properties.getProperty(propPath, (boolean) defaultValue);
        }

        @Override
        public Boolean loadPlayer(ProfileProperties properties, String propPath, Boolean defaultValue) {
            return properties.getProperty(propPath, (boolean) defaultValue);
        }
    }

    final class CharacterCodec implements PropertyCodec<Character> {
        public static final CharacterCodec INSTANCE = new CharacterCodec();
        private CharacterCodec() {}

        @Override
        public void save(CustomProperties properties, String propPath, Character value) {
            properties.setProperty(propPath, (char) value);
        }

        @Override
        public void savePlayer(ProfileProperties properties, String propPath, Character value) {
            properties.setProperty(propPath, (char) value);
        }

        @Override
        public Character load(CustomProperties properties, String propPath, Character defaultValue) {
            return properties.getProperty(propPath, (char) defaultValue);
        }

        @Override
        public Character loadPlayer(ProfileProperties properties, String propPath, Character defaultValue) {
            return properties.getProperty(propPath, (char) defaultValue);
        }
    }

    final class StringCodec implements PropertyCodec<String> {
        public static final StringCodec INSTANCE = new StringCodec();
        private StringCodec() {}

        @Override
        public void save(CustomProperties properties, String propPath, String value) {
            properties.setProperty(propPath, value);
        }

        @Override
        public void savePlayer(ProfileProperties properties, String propPath, String value) {
            properties.setProperty(propPath, value);
        }

        @Override
        public String load(CustomProperties properties, String propPath, String defaultValue) {
            return properties.getProperty(propPath, defaultValue);
        }

        @Override
        public String loadPlayer(ProfileProperties properties, String propPath, String defaultValue) {
            return properties.getProperty(propPath, defaultValue);
        }
    }
}
