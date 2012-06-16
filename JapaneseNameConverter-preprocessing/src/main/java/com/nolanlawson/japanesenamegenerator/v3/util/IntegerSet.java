package com.nolanlawson.japanesenamegenerator.v3.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Fast implementation of a set of integers.  Doesn't box ints into Integers and stores the values in an array for quick lookups
 * and adds.
 * @author nolan
 */
public class IntegerSet implements Iterable<Integer> {

    private static final int DEFAULT_SIZE = 25;

    private boolean[] values;

    public IntegerSet() {
        values = new boolean[DEFAULT_SIZE];
    }

    /**
     * Initialize with the first value - i.e. a singleton set.  This also gives a default size to the underlying
     * array structure.
     * @param idx
     */
    public IntegerSet(int idx) {
        values = new boolean[idx + 1];
        values[idx] = true;
    }

    public IntegerSet(IntegerSet other) {
        values = ArrayUtil.copyOf(other.values, other.values.length);
    }

    public IntegerSet(Set<Character> other) {
        values = new boolean[DEFAULT_SIZE];
        for (Character ch : other) {
            this.add(ch);
        }
    }

    public void add(int i) {
        if (i >= values.length) {
            int newSize = Math.max(values.length * 2, i + 1);
            values = ArrayUtil.copyOf(values, newSize);
        }
        values[i] = true;
    }

    public boolean contains(int i) {
        return i < values.length && values[i];
    }

    public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {

            int idx = 0;

            public boolean hasNext() {
                while (idx < values.length && !values[idx]) {
                    idx++;
                }

                return (idx < values.length);
            }

            public Integer next() {

                if (!hasNext()) {
                    throw new NoSuchElementException();
                }

                idx++;
                return idx - 1;
            }

            public void remove() {
                throw new UnsupportedOperationException("Not supported.");
            }
        };
    }
}
