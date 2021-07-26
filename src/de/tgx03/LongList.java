package de.tgx03;

import java.util.Arrays;

public class LongList {

    private long[] arr;
    private int currentLength;

    public LongList() {
        arr = new long[10];
    }

    public LongList(int length) {
        arr = new long[length];
    }

    public LongList(long[] list) {
        int length = Math.round(list.length * 1.5f);
        arr = Arrays.copyOf(list, length);
        currentLength = list.length;
    }

    public long get(int position) {
        return arr[position];
    }

    public void add(long value) {
        ensureCapacity(currentLength + 1);
        arr[currentLength] = value;
        currentLength++;
    }

    public void set(long value, int position) {
        ensureCapacity(position + 1);
        arr[position] = value;
        if (currentLength <= position) {
            currentLength = position + 1;
        }
    }

    public long remove(int position) {
        long result = arr[position];
        for (int i = position; position < arr.length; i++) {
            if (i < arr.length - 1) {
                arr[i] = arr[i + 1];
            } else {
                arr[i] = 0;
            }
        }
        currentLength--;
        return result;
    }

    public int size() {
        return currentLength;
    }

    public long[] toArray() {
        return Arrays.copyOf(arr, currentLength);
    }

    public void clear() {
        arr = new long[arr.length];
        currentLength = 0;
    }

    private void ensureCapacity(int capacity) {
        if (arr.length < capacity) {
            int length = Math.round(capacity * 1.5f);
            arr = Arrays.copyOf(arr, length);
        }
    }
}
