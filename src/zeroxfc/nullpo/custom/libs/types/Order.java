package zeroxfc.nullpo.custom.libs.types;

import java.util.function.BiFunction;
import java.util.function.Supplier;

// Ranking order helper.
public enum Order {
    LT(-1), EQ(0), GT(1);
    public final int compareValue;

    public static Order fromCompare(int cmp) {
        if (cmp < 0) return LT;
        else if (cmp > 0) return GT;
        else return EQ;
    }

    public static <T extends Comparable<T>> BiFunction<T, T, Order> deriveComparator() {
        return (a, b) -> fromCompare(a.compareTo(b));
    }

    Order(int compareValue) {
        this.compareValue = compareValue;
    }

    public Order fold(Supplier<Order> other) {
        return fold(this, other.get());
    }

    public static Order fold(Order lhs, Order rhs) {
        if (lhs == EQ) return rhs;
        else return lhs;
    }
}
