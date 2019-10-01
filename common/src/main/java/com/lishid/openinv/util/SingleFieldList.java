/*
 * Copyright (C) 2011-2019 lishid. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.lishid.openinv.util;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

/**
 * A List implementation intended for wrapping a single field.
 *
 * @param <V> the type of the field
 *
 * @author Jikoo
 */
public class SingleFieldList<V> extends AbstractCollection<V> implements List<V> {

    private final Supplier<V> fieldGetter;
    private final Consumer<V> fieldSetter;

    public SingleFieldList(@NotNull Supplier<V> fieldGetter, @NotNull Consumer<V> fieldSetter) {
        this.fieldGetter = fieldGetter;
        this.fieldSetter = fieldSetter;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean contains(Object o) {
        return Objects.equals(o, fieldGetter.get());
    }

    @NotNull
    @Override
    public Iterator<V> iterator() {
        return listIterator();
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends V> c) {
        return super.addAll(c);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof SingleFieldList
                && fieldGetter.equals(((SingleFieldList) o).fieldGetter)
                && fieldSetter.equals(((SingleFieldList) o).fieldSetter);
    }

    @Override
    public int hashCode() {
        return fieldSetter.hashCode() * 17 * fieldGetter.hashCode();
    }

    @Override
    public V get(int index) {
        if (index != 0) {
            throw new IndexOutOfBoundsException();
        }
        return fieldGetter.get();
    }

    @Override
    public V set(int index, V element) {
        if (index != 0) {
            throw new IndexOutOfBoundsException();
        }

        V old = fieldGetter.get();
        fieldSetter.accept(element);

        return old;
    }

    @Override
    public void add(int index, V element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
        return fieldGetter.get().equals(o) ? 0 : -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        return indexOf(o);
    }

    @NotNull
    @Override
    public ListIterator<V> listIterator() {
        return new ListIterator<V>() {
            private boolean hasNext = true;

            @Override
            public boolean hasNext() {
                return hasNext;
            }

            @Override
            public V next() {
                if (!hasNext) {
                    throw new NoSuchElementException();
                }
                return fieldGetter.get();
            }

            @Override
            public boolean hasPrevious() {
                return !hasNext;
            }

            @Override
            public V previous() {
                if (hasNext) {
                    throw new NoSuchElementException();
                }
                return fieldGetter.get();
            }

            @Override
            public int nextIndex() {
                return hasNext ? 0 : 1;
            }

            @Override
            public int previousIndex() {
                return hasNext ? -1 : 0;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void set(V v) {
                fieldSetter.accept(v);
            }

            @Override
            public void add(V v) {
                throw new UnsupportedOperationException();
            }
        };
    }

    @NotNull
    @Override
    public ListIterator<V> listIterator(int index) {
        if (index != 0) {
            throw new IndexOutOfBoundsException();
        }
        return listIterator();
    }

    @NotNull
    @Override
    public List<V> subList(int fromIndex, int toIndex) {
        if (fromIndex != 0 || toIndex != 1) {
            throw new IndexOutOfBoundsException();
        }

        return this;
    }

    @Override
    public void clear() {}

}
