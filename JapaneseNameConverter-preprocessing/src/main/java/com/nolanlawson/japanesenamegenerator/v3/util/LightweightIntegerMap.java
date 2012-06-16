package com.nolanlawson.japanesenamegenerator.v3.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author nolan
 */
public class LightweightIntegerMap implements Map<Integer,Integer> {

    private static final int INITIAL_SIZE = 32;

    private int startIdx;
    private int[] values;
    private boolean[] filled;

    public LightweightIntegerMap() {
    }
    
    public int size() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isEmpty() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean containsKey(Object key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Integer get(Object key) {

        Integer intKey = (Integer)key;

        if (filled == null) {
            return null;
        } else if ((intKey - startIdx) < filled.length && intKey >= startIdx && filled[intKey - startIdx]) {
            return values[intKey - startIdx];
        } else {
            return null;
        }
    }

    public Integer put(Integer key, Integer value) {

        if (values == null) {
            // first value
            values = new int[INITIAL_SIZE];
            filled = new boolean[INITIAL_SIZE];
            startIdx = key;
        } else if (key < startIdx) {
            int lengthToAdd = startIdx - key;
            int[] intArrayToAdd = new int[lengthToAdd];
            boolean[] boolArrayToAdd = new boolean[lengthToAdd];

            filled = ArrayUtil.concatenate(boolArrayToAdd, filled);
            values = ArrayUtil.concatenate(intArrayToAdd, values);

            startIdx = key;

        } else if ((key - startIdx) >= values.length) {
            int newSize = Math.max((key - startIdx) + 1,values.length * 2);
            values = ArrayUtil.copyOf(values, newSize);
            filled = ArrayUtil.copyOf(filled, newSize);
        }
        filled[key - startIdx] = true;
        values[key - startIdx] = value;

        return null; //  I don't give a shit
    }

    public Integer remove(Object key) {

        Integer intKey = (Integer)key;

        if (intKey >= startIdx && (intKey - startIdx) < filled.length) {
            filled[intKey - startIdx] = false;
        }

        return null; // I don't give a shit
    }

    public void putAll(Map<? extends Integer, ? extends Integer> m) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void clear() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Set<Integer> keySet() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<Integer> values() {

        List<Integer> result = new ArrayList<Integer>();

        for (int i = 0; i < values.length; i++) {
            if (filled[i]) {
                result.add(values[i]);
            }
        }

        return result;
    }

    public Set<Entry<Integer, Integer>> entrySet() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}
