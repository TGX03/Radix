package de.tgx03.primitivelist;

import java.util.Arrays;

/**
 * A list using primitive shorts instead of object ones
 */
public class ShortList {

    private short[] arr;
    private int currentLength;

    /**
     * Creates a new list with an initial length of 10
     */
    public ShortList() {
        arr = new short[10];
    }

    /**
     * Creates a new list with the given initial length
     * @param length The initial length of this list
     */
    public ShortList(int length) {
        arr = new short[length];
    }

    /**
     * Creates a new list from a given short array
     * @param list The array this holds initially
     */
    public ShortList(short[] list) {
        int length = Math.round(list.length * 1.5f);
        arr = Arrays.copyOf(list, length);
        currentLength = list.length;
    }

    /**
     * Returns the element at a given position or throws an error if the position doesn't exist
     * @param position The target position
     * @return The element at this position
     */
    public short get(int position) {
        return arr[position];
    }

    /**
     * Adds a value to the end of this list
     * @param value The value to add
     */
    public void add(short value) {
        ensureCapacity(currentLength + 1);
        arr[currentLength] = value;
        currentLength++;
    }

    /**
     * Sets the value at a specific position to the given value
     * @param value The value to set
     * @param position The position to set
     */
    public void set(short value, int position) {
        ensureCapacity(position + 1);
        arr[position] = value;
        if (currentLength <= position) {
            currentLength = position + 1;
        }
    }

    /**
     * Clears a given position and shifts all following elements one plaace to the left
     * @param position The position to clear
     * @return The value at this position
     */
    public short remove(int position) {
        short result = arr[position];
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

    /**
     * @return The current size of this element
     */
    public int size() {
        return currentLength;
    }

    /**
     * Returns an array representing the current state of this list
     * As this is a primitive list, it's not needed to input an array
     * @return An array of the current state of this list
     */
    public short[] toArray() {
        return Arrays.copyOf(arr, currentLength);
    }

    /**
     * Removes all elements from this list
     */
    public void clear() {
        arr = new short[arr.length];
        currentLength = 0;
    }

    /**
     * If the array has less space than requested, its size is increased to 150% its current size
     * @param capacity The requested minimum capacity
     */
    private void ensureCapacity(int capacity) {
        if (arr.length < capacity) {
            int length = Math.round(capacity * 1.5f);
            arr = Arrays.copyOf(arr, length);
        }
    }
}
