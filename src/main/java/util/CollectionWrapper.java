package util;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;

public interface CollectionWrapper<E> extends Collection<E> {
    Collection<E> collectionToDisplay();

    @Override
    default int size() {
        return collectionToDisplay().size();
    }

    @Override
    default boolean isEmpty() {
        return collectionToDisplay().isEmpty();
    }

    @Override
    default boolean contains(Object o) {
        return collectionToDisplay().contains(o);
    }

    @NotNull
    @Override
    default Iterator<E> iterator() {
        return collectionToDisplay().iterator();
    }

    @NotNull
    @Override
    default Object[] toArray() {
        return collectionToDisplay().toArray();
    }

    @SuppressWarnings("SuspiciousToArrayCall" /* is wrapper */)
    @NotNull
    @Override
    default <T> T[] toArray(@NotNull T[] a) {
        return collectionToDisplay().toArray(a);
    }

    @Override
    default boolean add(E e) {
        return collectionToDisplay().add(e);
    }

    @Override
    default boolean remove(Object o) {
        return collectionToDisplay().remove(o);
    }

    @Override
    default boolean containsAll(@NotNull Collection<?> c) {
        return collectionToDisplay().containsAll(c);
    }

    @Override
    default boolean addAll(@NotNull Collection<? extends E> c) {
        return collectionToDisplay().addAll(c);
    }

    @Override
    default boolean removeAll(@NotNull Collection<?> c) {
        return collectionToDisplay().removeAll(c);
    }

    @Override
    default boolean retainAll(@NotNull Collection<?> c) {
        return collectionToDisplay().retainAll(c);
    }

    @Override
    default void clear() {
        collectionToDisplay().clear();
    }
}