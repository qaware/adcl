package util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A util class for easy Map transformation
 * Use {@link MapTool#get()} to retrieve the resulting map
 * Other Methods are self-explanatory
 */
public class MapTool<K, V> {
    private final Map<K, V> map;

    public MapTool(Map<K, V> map) {
        this.map = map;
    }

    public MapTool<K, V> filterKeys(Predicate<K> filter) {
        return transform((m, k, v) -> {
            if (filter.test(k)) m.put(k, v);
        });
    }

    public MapTool<K, V> filterValues(Predicate<V> filter) {
        return transform((m, k, v) -> {
            if (filter.test(v)) m.put(k, v);
        });
    }

    public <K2> MapTool<K2, V> mapKeys(Function<K, K2> mapper) {
        return transform((m, k, v) -> m.put(mapper.apply(k), v));
    }

    public <V2> MapTool<K, V2> mapValues(Function<V, V2> mapper) {
        return transform((m, k, v) -> m.put(k, mapper.apply(v)));
    }

    public <K2> MapTool<K2, V> castKeys(Class<K2> keyClass) {
        return filterKeys(keyClass::isInstance).mapKeys(keyClass::cast);
    }

    public <V2> MapTool<K, V2> castValues(Class<V2> valueClass) {
        return filterValues(valueClass::isInstance).mapValues(valueClass::cast);
    }

    public Map<K, V> get() {
        return map;
    }

    private <K2, V2> MapTool<K2, V2> transform(TriConsumer<Map<K2, V2>, K, V> transformer) {
        Map<K2, V2> result = new HashMap<>();
        map.forEach((k, v) -> transformer.accept(result, k, v));
        return new MapTool<>(result);
    }

    interface TriConsumer<A, B, C> {
        void accept(A a, B b, C c);
    }
}