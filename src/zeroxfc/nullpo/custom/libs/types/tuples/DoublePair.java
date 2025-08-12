package zeroxfc.nullpo.custom.libs.types.tuples;

import java.util.Objects;
import java.util.function.DoubleFunction;

/** Specialized instance of pairs for primitive doubles. */
public class DoublePair {
    public static DoublePair of(double valL, double valR) {
        return new DoublePair(valL, valR);
    }

    // Instance data:
    public final double valL;
    public final double valR;

    private DoublePair(double valL, double valR) {
        this.valL = valL;
        this.valR = valR;
    }

    public Pair<Double, Double> boxed() {
        return Pair.of(valL, valR);
    }

    public <L, R> Pair<L, R> mapToPair(DoubleFunction<L> funcL, DoubleFunction<R> funcR) {
        return Pair.of(funcL.apply(valL), funcR.apply(valR));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DoublePair doublePair = (DoublePair) o;
        return valL == doublePair.valL && valR == doublePair.valR;
    }

    @Override
    public int hashCode() {
        return Objects.hash(valL, valR);
    }
}
