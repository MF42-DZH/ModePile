package zeroxfc.nullpo.custom.libs.types.tuples;

import java.util.Objects;
import java.util.function.IntFunction;

/** Specialized instance of pairs for primitive integers. */
public class IntPair {
    public static IntPair of(int valL, int valR) {
        return new IntPair(valL, valR);
    }

    // Instance data:
    public final int valL;
    public final int valR;

    private IntPair(int valL, int valR) {
        this.valL = valL;
        this.valR = valR;
    }

    public Pair<Integer, Integer> boxed() {
        return Pair.of(valL, valR);
    }

    public <L, R> Pair<L, R> mapToPair(IntFunction<L> funcL, IntFunction<R> funcR) {
        return Pair.of(funcL.apply(valL), funcR.apply(valR));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        IntPair intPair = (IntPair) o;
        return valL == intPair.valL && valR == intPair.valR;
    }

    @Override
    public int hashCode() {
        return Objects.hash(valL, valR);
    }
}
