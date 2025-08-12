package zeroxfc.nullpo.custom.libs.types;

// Why isn't this in the Java 8 standard library?

@FunctionalInterface
public interface FloatFunction<R> {
    R apply(float arg);
}
