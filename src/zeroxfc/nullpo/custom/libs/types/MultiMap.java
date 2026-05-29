package zeroxfc.nullpo.custom.libs.types;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/** A map that can hold multiple values per key. */
public class MultiMap<K, V> {
    private final Map<K, Set<V>> backingMap;
    private final Supplier<Set<V>> setConstructor;

    public MultiMap(Map<K, Set<V>> backingMap, Supplier<Set<V>> setConstructor) {
        this.backingMap = backingMap;
        this.setConstructor = setConstructor;
    }

    public int size() {
        return backingMap
            .values()
            .stream()
            .mapToInt(Set::size)
            .sum();
    }

    public void clear() {
        backingMap.clear();
    }

    public void put(K key, V value) {
        backingMap.compute(
            key,
            (k, s) -> {
                if (s == null) {
                    final Set<V> newSet = setConstructor.get();
                    newSet.add(value);

                    return newSet;
                } else {
                    s.add(value);
                    return s;
                }
            }
        );
    }

    public Set<V> get(K key) {
        return backingMap.get(key);
    }

    public boolean containsKey(K key) {
        return backingMap.containsKey(key);
    }

    public boolean containsValueAt(K key, V value) {
        return get(key).contains(value);
    }

    public Set<K> keySet() {
        return backingMap.keySet();
    }

    public Set<Map.Entry<K, V>> entrySet() {
        return backingMap
            .entrySet()
            .stream()
            .flatMap(e -> e
                .getValue()
                .stream()
                .map(v -> new Map.Entry<K, V>() {
                    @Override
                    public K getKey() {
                        return e.getKey();
                    }

                    @Override
                    public V getValue() {
                        return v;
                    }

                    @Override
                    public V setValue(V value) {
                        e.getValue().remove(v);
                        e.getValue().add(value);

                        return v;
                    }
                })
            )
            .collect(Collectors.toSet());
    }
}
