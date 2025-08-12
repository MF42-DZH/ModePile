package zeroxfc.nullpo.custom.libs.types;

// Why isn't this in the Java 8 standard library?

@FunctionalInterface
public interface ToFloatFunction<T> {
    float applyAsFloat(T arg);
}
