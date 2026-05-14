package zeroxfc.nullpo.custom.libs;

import java.util.function.Supplier;
import mu.nu.nullpo.util.CustomProperties;
import zeroxfc.nullpo.custom.libs.types.tuples.DoublePair;
import zeroxfc.nullpo.custom.libs.types.tuples.FloatPair;
import zeroxfc.nullpo.custom.libs.types.tuples.IntPair;

// Encodes a value that can be saved to properties.
public interface PropertyCodec<V> {
    void save(CustomProperties properties, String propPath, V value);
    void savePlayer(ProfileProperties properties, String propPath, V value);

    // DefaultValue may be provided as null in derived codecs.
    V load(CustomProperties properties, String propPath, V defaultValue);
    V loadPlayer(ProfileProperties properties, String propPath, V defaultValue);

    default V load(CustomProperties properties, String propPath) {
        return load(properties, propPath, defaultLoadValue());
    }

    default V loadPlayer(ProfileProperties properties, String propPath) {
        return loadPlayer(properties, propPath, defaultLoadValue());
    }

    /** Override this with a non-null return value if you need a different default value in the codec. */
    default V defaultLoadValue() {
        return null;
    }

    default PropertyCodec<V> deriveWithNewDefault(Supplier<V> newDefault) {
        final PropertyCodec<V> old = this;

        return new PropertyCodec<V>() {
            @Override
            public void save(CustomProperties properties, String propPath, V value) {
                old.save(properties, propPath, value);
            }

            @Override
            public void savePlayer(ProfileProperties properties, String propPath, V value) {
                old.savePlayer(properties, propPath, value);
            }

            @Override
            public V load(CustomProperties properties, String propPath, V defaultValue) {
                return old.load(properties, propPath, defaultValue);
            }

            @Override
            public V loadPlayer(ProfileProperties properties, String propPath, V defaultValue) {
                return old.loadPlayer(properties, propPath, defaultValue);
            }

            @Override
            public V defaultLoadValue() {
                return newDefault.get();
            }

            @Override
            public Class<V> getValueClass() {
                return old.getValueClass();
            }
        };
    }

    Class<V> getValueClass();

    // Derivation helpers

    static <E extends Enum<?>> PropertyCodec<E> deriveEnumCodec(Class<E> clazz, E defaultValue) {
        if (!clazz.isEnum()) throw new IllegalArgumentException("Class is not an enum!");

        return new PropertyCodec<E>() {
            @Override
            public void save(CustomProperties properties, String propPath, E value) {
                IntegerCodec.INSTANCE.save(properties, propPath, value.ordinal());
            }

            @Override
            public void savePlayer(ProfileProperties properties, String propPath, E value) {
                IntegerCodec.INSTANCE.savePlayer(properties, propPath, value.ordinal());
            }

            @Override
            public E load(CustomProperties properties, String propPath, E defaultValue) {
                if (properties.getProperty(propPath, "").isEmpty()) return defaultValue;
                return clazz.getEnumConstants()[IntegerCodec.INSTANCE.load(properties, propPath, defaultValue.ordinal())];
            }

            @Override
            public E loadPlayer(ProfileProperties properties, String propPath, E defaultValue) {
                if (properties.getProperty(propPath, "").isEmpty()) return defaultValue;
                return clazz.getEnumConstants()[IntegerCodec.INSTANCE.loadPlayer(properties, propPath, defaultValue.ordinal())];
            }

            @Override
            public E defaultLoadValue() {
                return defaultValue;
            }

            @Override
            public Class<E> getValueClass() {
                return clazz;
            }
        };
    }

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

        @Override
        public Class<Byte> getValueClass() {
            return Byte.class;
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

        @Override
        public Class<Short> getValueClass() {
            return Short.class;
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

        @Override
        public Class<Integer> getValueClass() {
            return Integer.class;
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

        @Override
        public Class<Long> getValueClass() {
            return Long.class;
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

        @Override
        public Class<Float> getValueClass() {
            return Float.class;
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

        @Override
        public Class<Double> getValueClass() {
            return Double.class;
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

        @Override
        public Class<Boolean> getValueClass() {
            return Boolean.class;
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

        @Override
        public Class<Character> getValueClass() {
            return Character.class;
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

        @Override
        public Class<String> getValueClass() {
            return String.class;
        }
    }

    final class IntPairCodec implements PropertyCodec<IntPair> {
        public static final IntPairCodec INSTANCE = new IntPairCodec();

        private IntPairCodec() { }

        @Override
        public void save(CustomProperties properties, String propPath, IntPair value) {
            IntegerCodec.INSTANCE.save(properties, propPath + ".valL", value.valL);
            IntegerCodec.INSTANCE.save(properties, propPath + ".valR", value.valR);
        }

        @Override
        public void savePlayer(ProfileProperties properties, String propPath, IntPair value) {
            IntegerCodec.INSTANCE.savePlayer(properties, propPath + ".valL", value.valL);
            IntegerCodec.INSTANCE.savePlayer(properties, propPath + ".valR", value.valR);
        }

        @Override
        public IntPair load(CustomProperties properties, String propPath, IntPair defaultValue) {
            return IntPair.of(
                IntegerCodec.INSTANCE.load(properties, propPath + ".valL", defaultValue.valL),
                IntegerCodec.INSTANCE.load(properties, propPath + ".valR", defaultValue.valR)
            );
        }

        @Override
        public IntPair loadPlayer(ProfileProperties properties, String propPath, IntPair defaultValue) {
            return IntPair.of(
                IntegerCodec.INSTANCE.loadPlayer(properties, propPath + ".valL", defaultValue.valL),
                IntegerCodec.INSTANCE.loadPlayer(properties, propPath + ".valR", defaultValue.valR)
            );
        }

        @Override
        public IntPair defaultLoadValue() {
            return IntPair.of(0, 0);
        }

        @Override
        public Class<IntPair> getValueClass() {
            return IntPair.class;
        }
    }

    final class DoublePairCodec implements PropertyCodec<DoublePair> {
        public static final DoublePairCodec INSTANCE = new DoublePairCodec();

        private DoublePairCodec() { }

        @Override
        public void save(CustomProperties properties, String propPath, DoublePair value) {
            DoubleCodec.INSTANCE.save(properties, propPath + ".valL", value.valL);
            DoubleCodec.INSTANCE.save(properties, propPath + ".valR", value.valR);
        }

        @Override
        public void savePlayer(ProfileProperties properties, String propPath, DoublePair value) {
            DoubleCodec.INSTANCE.savePlayer(properties, propPath + ".valL", value.valL);
            DoubleCodec.INSTANCE.savePlayer(properties, propPath + ".valR", value.valR);
        }

        @Override
        public DoublePair load(CustomProperties properties, String propPath, DoublePair defaultValue) {
            return DoublePair.of(
                DoubleCodec.INSTANCE.load(properties, propPath + ".valL", defaultValue.valL),
                DoubleCodec.INSTANCE.load(properties, propPath + ".valR", defaultValue.valR)
            );
        }

        @Override
        public DoublePair loadPlayer(ProfileProperties properties, String propPath, DoublePair defaultValue) {
            return DoublePair.of(
                DoubleCodec.INSTANCE.loadPlayer(properties, propPath + ".valL", defaultValue.valL),
                DoubleCodec.INSTANCE.loadPlayer(properties, propPath + ".valR", defaultValue.valR)
            );
        }

        @Override
        public DoublePair defaultLoadValue() {
            return DoublePair.of(0.0, 0.0);
        }

        @Override
        public Class<DoublePair> getValueClass() {
            return DoublePair.class;
        }
    }

    final class FloatPairCodec implements PropertyCodec<FloatPair> {
        public static final FloatPairCodec INSTANCE = new FloatPairCodec();

        private FloatPairCodec() { }

        @Override
        public void save(CustomProperties properties, String propPath, FloatPair value) {
            FloatCodec.INSTANCE.save(properties, propPath + ".valL", value.valL);
            FloatCodec.INSTANCE.save(properties, propPath + ".valR", value.valR);
        }

        @Override
        public void savePlayer(ProfileProperties properties, String propPath, FloatPair value) {
            FloatCodec.INSTANCE.savePlayer(properties, propPath + ".valL", value.valL);
            FloatCodec.INSTANCE.savePlayer(properties, propPath + ".valR", value.valR);
        }

        @Override
        public FloatPair load(CustomProperties properties, String propPath, FloatPair defaultValue) {
            return FloatPair.of(
                FloatCodec.INSTANCE.load(properties, propPath + ".valL", defaultValue.valL),
                FloatCodec.INSTANCE.load(properties, propPath + ".valR", defaultValue.valR)
            );
        }

        @Override
        public FloatPair loadPlayer(ProfileProperties properties, String propPath, FloatPair defaultValue) {
            return FloatPair.of(
                FloatCodec.INSTANCE.loadPlayer(properties, propPath + ".valL", defaultValue.valL),
                FloatCodec.INSTANCE.loadPlayer(properties, propPath + ".valR", defaultValue.valR)
            );
        }

        @Override
        public FloatPair defaultLoadValue() {
            return FloatPair.of(0.0f, 0.0f);
        }

        @Override
        public Class<FloatPair> getValueClass() {
            return FloatPair.class;
        }
    }
}

// Private unsafe codec for use with property codec generators.
// XXX: DO NOT USE THIS CLASS OUTSIDE THE LIBRARY CLASSES.
@SuppressWarnings({ "RawUseOfParameterized", "unchecked" })
final class UnsafePropertyCodec implements PropertyCodec<Object> {
    private final PropertyCodec codec;

    UnsafePropertyCodec(PropertyCodec codec) {
        this.codec = codec;
    }

    @Override
    public void save(CustomProperties properties, String propPath, Object value) {
        codec.save(properties, propPath, value);
    }

    @Override
    public void savePlayer(ProfileProperties properties, String propPath, Object value) {
        codec.savePlayer(properties, propPath, value);
    }

    @Override
    public Object load(CustomProperties properties, String propPath, Object defaultValue) {
        return codec.load(properties, propPath, defaultValue);
    }

    @Override
    public Object loadPlayer(ProfileProperties properties, String propPath, Object defaultValue) {
        return codec.loadPlayer(properties, propPath, defaultValue);
    }

    @Override
    public Object defaultLoadValue() {
        return codec.defaultLoadValue();
    }

    @Override
    public Class<Object> getValueClass() {
        return Object.class;
    }
}
