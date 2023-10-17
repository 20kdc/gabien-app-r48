/*
 * gabien-common - Cross-platform game and UI framework
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */
package r48.io;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Created on 12th August 2022.
 */
public final class IntArrayIterable implements Iterable<Integer> {
    private final int[] source;

    public IntArrayIterable(int[] array) {
        source = array;
    }

    @Override
    public Iterator<Integer> iterator() {
        return new ArrayIterator(source);
    }

    public static final class ArrayIterator implements Iterator<Integer> {
        private final int[] source;
        private int index = 0;

        public ArrayIterator(int[] src) {
            source = src;
        }

        @Override
        public boolean hasNext() {
            return index < source.length;
        }

        @Override
        public Integer next() {
            if (index >= source.length)
                throw new NoSuchElementException("Read off of end of IntArrayIterable");
            return source[index++];
        }
    }
}
