package util;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * A {@link Map} implementation which wraps another map but provides hooks on write actions
 */
@SuppressWarnings("java:S2160" /* No equals method needed, abstractMap#equals covers this */)
public class MapWithListeners<K, V> extends AbstractMap<K, V> {
    private final Map<K, V> map;
    private final BiConsumer<K, V> onPut;
    private final BiConsumer<K, V> onRemove;

    /**
     * @param map      the map to wrap
     * @param onPut    will be called with added key/value before a put operation
     * @param onRemove will be called with removed key/value after a remove operation
     */
    public MapWithListeners(Map<K, V> map, BiConsumer<K, V> onPut, BiConsumer<K, V> onRemove) {
        this.map = map;
        this.onPut = onPut;
        this.onRemove = onRemove;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return new AbstractSet<Entry<K, V>>() {
            private final Set<Entry<K, V>> es = map.entrySet();

            @NotNull
            @Override
            public Iterator<Entry<K, V>> iterator() {
                return new Iterator<Entry<K, V>>() {
                    private final Iterator<Entry<K, V>> it = es.iterator();
                    private Entry<K, V> last = null;

                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }

                    @Override
                    public Entry<K, V> next() {
                        last = it.next();
                        return last;
                    }

                    @Override
                    public void remove() {
                        it.remove();
                        onRemove.accept(last.getKey(), last.getValue());
                    }
                };
            }

            @Override
            public int size() {
                return es.size();
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V put(K key, V value) {
        onPut.accept(key, value);
        return map.put(key, value);
    }
}
