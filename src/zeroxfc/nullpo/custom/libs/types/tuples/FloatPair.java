package zeroxfc.nullpo.custom.libs.types.tuples;

import java.util.Objects;
import zeroxfc.nullpo.custom.libs.types.FloatFunction;

/** Specialized instance of pairs for primitive floats. */
public class FloatPair {
    public static FloatPair of(float valL, float valR) {
        return new FloatPair(valL, valR);
    }

    // Instance data:
    public final float valL;
    public final float valR;

    private FloatPair(float valL, float valR) {
        this.valL = valL;
        this.valR = valR;
    }

    public Pair<Float, Float> boxed() {
        return Pair.of(valL, valR);
    }

    public <L, R> Pair<L, R> mapToPair(FloatFunction<L> funcL, FloatFunction<R> funcR) {
        return Pair.of(funcL.apply(valL), funcR.apply(valR));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FloatPair floatPair = (FloatPair) o;
        return valL == floatPair.valL && valR == floatPair.valR;
    }

    @Override
    public int hashCode() {
        return Objects.hash(valL, valR);
    }
}
