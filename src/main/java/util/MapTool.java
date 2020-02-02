package util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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
    @NotNull
    private final Map<K, V> map;

    public MapTool(@NotNull Map<K, V> map) {
        this.map = map;
    }

    @NotNull
    public MapTool<K, V> filterKeys(@NotNull Predicate<K> filter) {
        return transform((m, k, v) -> {
            if (filter.test(k)) m.put(k, v);
        });
    }

    @NotNull
    public MapTool<K, V> filterValues(@NotNull Predicate<V> filter) {
        return transform((m, k, v) -> {
            if (filter.test(v)) m.put(k, v);
        });
    }

    @NotNull
    public <K2> MapTool<K2, V> mapKeys(@NotNull Function<K, K2> mapper) {
        return transform((m, k, v) -> m.put(mapper.apply(k), v));
    }

    @NotNull
    public <V2> MapTool<K, V2> mapValues(@NotNull Function<V, V2> mapper) {
        return transform((m, k, v) -> m.put(k, mapper.apply(v)));
    }

    @NotNull
    public <K2> MapTool<K2, V> castKeys(@NotNull Class<K2> keyClass) {
        return filterKeys(keyClass::isInstance).mapKeys(keyClass::cast);
    }

    @NotNull
    public <V2> MapTool<K, V2> castValues(@NotNull Class<V2> valueClass) {
        return filterValues(valueClass::isInstance).mapValues(valueClass::cast);
    }

    @NotNull
    public Map<K, V> get() {
        return map;
    }

    public void overrideTo(@NotNull Map<K, V> map) {
        map.clear();
        appendTo(map);
    }

    public void appendTo(@NotNull Map<K, V> map) {
        map.putAll(get());
    }

    @NotNull
    @Contract("_ -> new")
    private <K2, V2> MapTool<K2, V2> transform(@NotNull TriConsumer<Map<K2, V2>, K, V> transformer) {
        Map<K2, V2> result = new HashMap<>();
        map.forEach((k, v) -> transformer.accept(result, k, v));
        return new MapTool<>(result);
    }

    interface TriConsumer<A, B, C> {
        void accept(A a, B b, C c);
    }
}