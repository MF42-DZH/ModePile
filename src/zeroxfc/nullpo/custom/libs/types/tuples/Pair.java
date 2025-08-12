package zeroxfc.nullpo.custom.libs.types.tuples;

import java.util.Objects;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import zeroxfc.nullpo.custom.libs.types.ToFloatFunction;

/** Generic pair of objects. */
public final class Pair<L, R> {
    public static <L, R> Pair<L, R> of(L valL, R valR) {
        return new Pair<>(valL, valR);
    }

    // Instance data:
    public final L valL;
    public final R valR;

    private Pair(L valL, R valR) {
        this.valL = valL;
        this.valR = valR;
    }

    public IntPair mapToIntPair(ToIntFunction<L> funcL, ToIntFunction<R> funcR) {
        return IntPair.of(funcL.applyAsInt(valL), funcR.applyAsInt(valR));
    }

    public DoublePair mapToDoublePair(ToDoubleFunction<L> funcL, ToDoubleFunction<R> funcR) {
        return DoublePair.of(funcL.applyAsDouble(valL), funcR.applyAsDouble(valR));
    }

    public FloatPair mapToFloatPair(ToFloatFunction<L> funcL, ToFloatFunction<R> funcR) {
        return FloatPair.of(funcL.applyAsFloat(valL), funcR.applyAsFloat(valR));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(valL, pair.valL) && Objects.equals(valR, pair.valR);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valL, valR);
    }
}
