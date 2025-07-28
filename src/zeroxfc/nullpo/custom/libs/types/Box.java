package zeroxfc.nullpo.custom.libs.types;

import java.util.function.Function;

/** A generic mutable box for one value. */
public class Box<V> {
    private V value;

    public Box(V value) {
        this.setValue(value);
    }

    public <T> Box<T> map(Function<V, T> func) {
        return new Box<>(func.apply(value));
    }

    public <T> Box<T> ap(Box<Function<V, T>> func) {
        return new Box<>(func.value.apply(value));
    }

    public <T> Box<T> flatMap(Function<V, Box<T>> func) {
        return func.apply(value);
    }

    public void modify(Function<V, V> func) {
        setValue(func.apply(getValue()));
    }

    public void share(Box<V> vw) {
        this.setValue(vw.getValue());
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }
}
