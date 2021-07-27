package de.tgx03;

import de.tgx03.primitivelist.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public final class Radix {

    private enum run {FIRST, FIRSTSOURCE, SECONDSOURCE}

    /**
     * Sorts a given object array using radix sort
     * @param source The source array
     * @param calculator How to determine the absolute value of an object
     * @param <E> The type of object to sort
     * @return The sorted array
     */
    public static <E> E[] sort(E[] source, ObjectValue<E> calculator) {
        return sort(source, 64, calculator);
    }

    /**
     * Sorts a given object array using radix sort
     * Only uses how many bits are set to sort. Meaning lower numberlengths increase performance,
     * however setting it too low results in wrong results
     * @param source The source array
     * @param numberLength How many bits of the absolute values are relevant
     * @param calculator How to determine the absolute value of an object
     * @param <E> The type of object to sort
     * @return The sorted array
     */
    public static <E> E[] sort(E[] source, int numberLength, ObjectValue<E> calculator) {
        if (source.length == 0) {
            return source;
        }
        if (numberLength > 64) numberLength = 64;
        List<ValuedObject<E>> first = new ArrayList<>();
        List<ValuedObject<E>> second = new ArrayList<>();
        long position = 1;
        for (int i = 0; i < numberLength; i++) {
            if (i == 0) {
                for (E current : source) {
                    ValuedObject<E> entry = new ValuedObject<>(current, calculator.value(current));
                    long list = entry.value & position;
                    if (list == 0) {
                        first.add(entry);
                    } else {
                        second.add(entry);
                    }
                }
            } else {
                Tupel<List<ValuedObject<E>>> set = sort(first, second, position);
                first = set.first;
                second = set.second;
            }
            position = position<<1;
        }
        return mergeLists(first, second, numberLength == 64, source[0].getClass());
    }

    private static <E> Tupel<List<ValuedObject<E>>> sort(List<ValuedObject<E>> first, List<ValuedObject<E>> second, final long position) {
        int size = Math.max(first.size(), second.size());
        List<ValuedObject<E>> firstBucket = new ArrayList<>(size);
        List<ValuedObject<E>> secondBucket = new ArrayList<>(size);
        for (ValuedObject<E> current : first) {
            long list = current.value & position;
            if (list == 0) {
                firstBucket.add(current);
            } else {
                secondBucket.add(current);
            }
        }
        for (ValuedObject<E> current : second) {
            long list = current.value & position;
            if (list == 0) {
                firstBucket.add(current);
            } else {
                secondBucket.add(current);
            }
        }
        return new Tupel<List<ValuedObject<E>>>(firstBucket, secondBucket);
    }

    private static <E> E[] mergeLists(List<ValuedObject<E>> first, List<ValuedObject<E>> second, boolean negative, Class<?> EClass) {
        try {
            E[] result = (E[]) Array.newInstance(EClass, first.size() + second.size());
            if (negative) {
                Thread copier1 = new Thread(new Copier<>(second, result, 0));
                copier1.start();
                Thread copier2 = new Thread(new Copier<>(first, result, second.size()));
                copier2.start();
                copier1.join();
                copier2.join();
            } else {
                Thread copier1 = new Thread(new Copier<>(first, result, 0));
                copier1.start();
                Thread copier2 = new Thread(new Copier<>(second, result, first.size()));
                copier2.start();
                copier1.join();
                copier2.join();
            }
            return result;
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
            return null;
        }
    }

    /**
     * Sorts a given byte array using radix sort
     * This method assumes that all 8 bits are relevant
     * @param source The source array
     * @return The sorted array
     */
    public static byte[] sort(byte[] source) {
        return sort(source, 8);
    }

    /**
     * Sorts a given byte array using radix sort
     * Make sure to correctly set how many bits are relevant, as lower values increase performance,
     * but if set too low the result is wrong
     * @param source The source array
     * @param numberLength How many bits are relevant for sorting
     * @return The sorted array
     */
    public static byte[] sort(byte[] source, int numberLength) {
        if (numberLength > 8) numberLength = 8;
        ByteList[] first = new ByteList[2];
        ByteList[] second = new ByteList[2];
        for (int i = 0; i < first.length; i++) {
            first[i] = new ByteList();
            second[i] = new ByteList();
        }
        run current = run.FIRST;
        byte position = 1;
        for (int i = 0; i < numberLength; i++) {
            switch (current) {
                case FIRST -> {
                    for (byte element : source) {
                        int list = element & position;
                        if (list == 0) {
                            first[0].add(element);
                        } else {
                            first[1].add(element);
                        }
                    }
                    current = run.FIRSTSOURCE;
                }
                case FIRSTSOURCE -> {
                    second = sortPosition(first, position);
                    current = run.SECONDSOURCE;
                }
                case SECONDSOURCE -> {
                    first = sortPosition(second, position);
                    current = run.FIRSTSOURCE;
                }
            }
            position = (byte) (position<<1);
        }
        switch (current) {

            case FIRSTSOURCE -> {
                return mergeLists(first, numberLength == 8);
            }

            case SECONDSOURCE -> {
                return mergeLists(second, numberLength == 8);
            }

            default -> throw new Error("Don't know what happened");
        }
    }

    /**
     * Sorts 2 given ByteLists at the given position
     * Keep in mind that the position argument isn't a count of the current position,
     * but an int where every place is 0 except the one bit to be sorted by
     * @param source The source lists
     * @param position The current position to sort by
     * @return Two ByteLists which were sorted at the given position
     */
    private static ByteList[] sortPosition(ByteList[] source, byte position) {
        ByteList[] result = new ByteList[2];
        int size = Math.max(source[0].size(), source[1].size());
        result[0] = new ByteList(size);
        result[1] = new ByteList(size);
        for (int i = 0; i <= 1; i++) {
            for (int x = 0; x < source[i].size(); x++) {
                byte element = source[i].get(x);
                int list = element & position;
                if (list == 0) {
                    result[0].add(element);
                } else {
                    result[1].add(element);
                }
            }
        }
        return result;
    }

    /**
     * Merges 2 ByteLists from an array into one byte array
     * @param source The source
     * @return A long array holding the elements from the source
     */
    private static byte[] mergeLists(ByteList[] source, boolean negative) {
        byte[] result = new byte[source[0].size() + source[1].size()];
        byte[] first = source[0].toArray();
        byte[] second = source[1].toArray();
        mergeArrays(first, first.length, second, second.length, result, negative);
        return result;
    }

    /**
     * Sorts a given char array using radix sort
     * This method assumes that all 16 bits are relevant
     * @param source The source array
     * @return The sorted array
     */
    public static char[] sort(char[] source) {
        return sort(source, 16);
    }

    /**
     * Sorts a given int array using radix sort
     * Make sure to correctly set how many bits are relevant, as lower values increase performance,
     * but if set too low the result is wrong
     * @param source The source array
     * @param numberLength How many bits are relevant for sorting
     * @return The sorted array
     */
    public static char[] sort(char[] source, int numberLength) {
        if (numberLength > 16) numberLength = 16;
        CharList[] first = new CharList[2];
        CharList[] second = new CharList[2];
        for (int i = 0; i < first.length; i++) {
            first[i] = new CharList();
            second[i] = new CharList();
        }
        run current = run.FIRST;
        char position = 1;
        for (int i = 0; i < numberLength; i++) {
            switch (current) {
                case FIRST -> {
                    for (char element : source) {
                        char list = (char) (element & position);
                        if (list == 0) {
                            first[0].add(element);
                        } else {
                            first[1].add(element);
                        }
                    }
                    current = run.FIRSTSOURCE;
                }
                case FIRSTSOURCE -> {
                    second = sortPosition(first, position);
                    current = run.SECONDSOURCE;
                }
                case SECONDSOURCE -> {
                    first = sortPosition(second, position);
                    current = run.FIRSTSOURCE;
                }
            }
            position = (char) (position<<1);
        }
        switch (current) {

            case FIRSTSOURCE -> {
                return mergeLists(first, numberLength == 16);
            }

            case SECONDSOURCE -> {
                return mergeLists(second, numberLength == 16);
            }

            default -> throw new Error("Don't know what happened");
        }
    }

    /**
     * Sorts 2 given IntLists at the given position
     * Keep in mind that the position argument isn't a count of the current position,
     * but an int where every place is 0 except the one bit to be sorted by
     * @param source The source lists
     * @param position The current position to sort by
     * @return Two IntLists which were sorted at the given position
     */
    private static CharList[] sortPosition(CharList[] source, char position) {
        CharList[] result = new CharList[2];
        int size = Math.max(source[0].size(), source[1].size());
        result[0] = new CharList(size);
        result[1] = new CharList(size);
        for (int i = 0; i <= 1; i++) {
            for (int x = 0; x < source[i].size(); x++) {
                char element = source[i].get(x);
                int list = element & position;
                if (list == 0) {
                    result[0].add(element);
                } else {
                    result[1].add(element);
                }
            }
        }
        return result;
    }

    /**
     * Merges 2 IntLists from an array into one long array
     * @param source The source
     * @return A long array holding the elements from the source
     */
    private static char[] mergeLists(CharList[] source, boolean negative) {
        char[] result = new char[source[0].size() + source[1].size()];
        char[] first = source[0].toArray();
        char[] second = source[1].toArray();
        mergeArrays(first, first.length, second, second.length, result, negative);
        return result;
    }

    /**
     * Sorts a given short array using radix sort
     * This method assumes that all 16 bits are relevant
     * @param source The source array
     * @return The sorted array
     */
    public static short[] sort(short[] source) {
        return sort(source, 16);
    }

    /**
     * Sorts a given short array using radix sort
     * Make sure to correctly set how many bits are relevant, as lower values increase performance,
     * but if set too low the result is wrong
     * @param source The source array
     * @param numberLength How many bits are relevant for sorting
     * @return The sorted array
     */
    public static short[] sort(short[] source, int numberLength) {
        if (numberLength > 16) numberLength = 16;
        ShortList[] first = new ShortList[2];
        ShortList[] second = new ShortList[2];
        for (int i = 0; i < first.length; i++) {
            first[i] = new ShortList();
            second[i] = new ShortList();
        }
        run current = run.FIRST;
        short position = 1;
        for (int i = 0; i < numberLength; i++) {
            switch (current) {
                case FIRST -> {
                    for (short element : source) {
                        int list = element & position;
                        if (list == 0) {
                            first[0].add(element);
                        } else {
                            first[1].add(element);
                        }
                    }
                    current = run.FIRSTSOURCE;
                }
                case FIRSTSOURCE -> {
                    second = sortPosition(first, position);
                    current = run.SECONDSOURCE;
                }
                case SECONDSOURCE -> {
                    first = sortPosition(second, position);
                    current = run.FIRSTSOURCE;
                }
            }
            position = (short) (position<<1);
        }
        switch (current) {

            case FIRSTSOURCE -> {
                return mergeLists(first, numberLength == 16);
            }

            case SECONDSOURCE -> {
                return mergeLists(second, numberLength == 16);
            }

            default -> throw new Error("Don't know what happened");
        }
    }

    /**
     * Sorts 2 given ShortLists at the given position
     * Keep in mind that the position argument isn't a count of the current position,
     * but a short where every place is 0 except the one bit to be sorted by
     * @param source The source lists
     * @param position The current position to sort by
     * @return Two IntLists which were sorted at the given position
     */
    private static ShortList[] sortPosition(ShortList[] source, short position) {
        ShortList[] result = new ShortList[2];
        int size = Math.max(source[0].size(), source[1].size());
        result[0] = new ShortList(size);
        result[1] = new ShortList(size);
        for (int i = 0; i <= 1; i++) {
            for (int x = 0; x < source[i].size(); x++) {
                short element = source[i].get(x);
                int list = (element & position);
                if (list == 0) {
                    result[0].add(element);
                } else {
                    result[1].add(element);
                }
            }
        }
        return result;
    }

    /**
     * Merges 2 ShortLists from an array into one long array
     * @param source The source
     * @return A long array holding the elements from the source
     */
    private static short[] mergeLists(ShortList[] source, boolean negative) {
        short[] result = new short[source[0].size() + source[1].size()];
        short[] first = source[0].toArray();
        short[] second = source[1].toArray();
        mergeArrays(first, first.length, second, second.length, result, negative);
        return result;
    }

    /**
     * Sorts a given int array using radix sort
     * This method assumes that all 32 bits are relevant
     * @param source The source array
     * @return The sorted array
     */
    public static int[] sort(int[] source) {
        return sort(source, 32);
    }

    /**
     * Sorts a given int array using radix sort
     * Make sure to correctly set how many bits are relevant, as lower values increase performance,
     * but if set too low the result is wrong
     * @param source The source array
     * @param numberLength How many bits are relevant for sorting
     * @return The sorted array
     */
    public static int[] sort(int[] source, int numberLength) {
        if (numberLength > 32) numberLength = 32;
        IntList[] first = new IntList[2];
        IntList[] second = new IntList[2];
        for (int i = 0; i < first.length; i++) {
            first[i] = new IntList();
            second[i] = new IntList();
        }
        run current = run.FIRST;
       int position = 1;
        for (int i = 0; i < numberLength; i++) {
            switch (current) {
                case FIRST -> {
                    for (int element : source) {
                        int list = element & position;
                        if (list == 0) {
                            first[0].add(element);
                        } else {
                            first[1].add(element);
                        }
                    }
                    current = run.FIRSTSOURCE;
                }
                case FIRSTSOURCE -> {
                    second = sortPosition(first, position);
                    current = run.SECONDSOURCE;
                }
                case SECONDSOURCE -> {
                    first = sortPosition(second, position);
                    current = run.FIRSTSOURCE;
                }
            }
            position = position<<1;
        }
        switch (current) {

            case FIRSTSOURCE -> {
                return mergeLists(first, numberLength == 32);
            }

            case SECONDSOURCE -> {
                return mergeLists(second, numberLength == 32);
            }

            default -> throw new Error("Don't know what happened");
        }
    }

    /**
     * Sorts 2 given IntLists at the given position
     * Keep in mind that the position argument isn't a count of the current position,
     * but an int where every place is 0 except the one bit to be sorted by
     * @param source The source lists
     * @param position The current position to sort by
     * @return Two IntLists which were sorted at the given position
     */
    private static IntList[] sortPosition(IntList[] source, int position) {
        IntList[] result = new IntList[2];
        int size = Math.max(source[0].size(), source[1].size());
        result[0] = new IntList(size);
        result[1] = new IntList(size);
        for (int i = 0; i <= 1; i++) {
            for (int x = 0; x < source[i].size(); x++) {
                int element = source[i].get(x);
               int list = element & position;
                if (list == 0) {
                    result[0].add(element);
                } else {
                    result[1].add(element);
                }
            }
        }
        return result;
    }

    /**
     * Merges 2 IntLists from an array into one long array
     * @param source The source
     * @return A long array holding the elements from the source
     */
    private static int[] mergeLists(IntList[] source, boolean negative) {
        int[] result = new int[source[0].size() + source[1].size()];
        int[] first = source[0].toArray();
        int[] second = source[1].toArray();
        mergeArrays(first, first.length, second, second.length, result, negative);
        return result;
    }

    /**
     * Sorts a given long array using radix sort
     * This method assumes that all 64 bits are relevant
     * @param source The source array
     * @return The sorted array
     */
    public static long[] sort(long[] source) {
        return sort(source, 64);
    }

    /**
     * Sorts a given long array using radix sort
     * Make sure to correctly set how many bits are relevant, as lower values increase performance,
     * but if set too low the result is wrong
     * @param source The source array
     * @param numberLength How many bits are relevant for sorting
     * @return The sorted array
     */
    public static long[] sort(long[] source, int numberLength) {
        if (numberLength > 64) numberLength = 64;
        LongList[] first = new LongList[2];
        LongList[] second = new LongList[2];
        for (int i = 0; i < first.length; i++) {
            first[i] = new LongList();
            second[i] = new LongList();
        }
        run current = run.FIRST;
        long position = 1;
        for (int i = 0; i < numberLength; i++) {
            switch (current) {
                case FIRST -> {
                    for (long element : source) {
                        long list = element & position;
                        if (list == 0) {
                            first[0].add(element);
                        } else {
                            first[1].add(element);
                        }
                    }
                    current = run.FIRSTSOURCE;
                }
                case FIRSTSOURCE -> {
                    second = sortPosition(first, position);
                    current = run.SECONDSOURCE;
                }
                case SECONDSOURCE -> {
                    first = sortPosition(second, position);
                    current = run.FIRSTSOURCE;
                }
            }
            position = position<<1;
        }
        switch (current) {

            case FIRSTSOURCE -> {
                return mergeLists(first, numberLength == 64);
            }

            case SECONDSOURCE -> {
                return mergeLists(second, numberLength == 64 );
            }

            default -> throw new Error("Don't know what happened");
        }
    }

    /**
     * Sorts 2 given LongLists at the given position
     * Keep in mind that the position argument isn't a count of the current position,
     * but a long where every place is 0 except the one bit to be sorted by
     * @param source The source lists
     * @param position The current position to sort by
     * @return Two LongLists which were sorted at the given position
     */
    private static LongList[] sortPosition(LongList[] source, long position) {
        LongList[] result = new LongList[2];
        int size = Math.max(source[0].size(), source[1].size());
        result[0] = new LongList(size);
        result[1] = new LongList(size);
        for (int i = 0; i <= 1; i++) {
            for (int x = 0; x < source[i].size(); x++) {
                long element = source[i].get(x);
                long list = element & position;
                if (list == 0) {
                    result[0].add(element);
                } else {
                    result[1].add(element);
                }
            }
        }
        return result;
    }

    /**
     * Merges 2 LongLists from an array into one long array
     * @param source The source
     * @return A long array holding the elements from the source
     */
    private static long[] mergeLists(LongList[] source, boolean negative) {
        long[] result = new long[source[0].size() + source[1].size()];
        long[] first = source[0].toArray();
        long[] second = source[1].toArray();
        mergeArrays(first, first.length, second, second.length, result, negative);
        return result;
    }

    /**
     * Merges 2 given arrays together.
     * Also orders them correctly if the values can be negative
     * @param firstArr The first source array
     * @param firstArrLength How long the first source array is
     * @param secondArr The second source array
     * @param secondArrLength How long the second array is
     * @param targetArr The target array
     * @param negative Whether the values may be negative
     */
    private static void mergeArrays(Object firstArr, int firstArrLength, Object secondArr, int secondArrLength, Object targetArr, boolean negative) {
        if (negative) {
            System.arraycopy(secondArr, 0, targetArr, 0, secondArrLength);
            System.arraycopy(firstArr, 0, targetArr, secondArrLength, firstArrLength);
        } else {
            System.arraycopy(firstArr, 0, targetArr, 0, firstArrLength);
            System.arraycopy(secondArr, 0, targetArr, firstArrLength, secondArrLength);
        }
    }

    /**
     * This interface is used to determine the absolute value of an object,
     * as comparisons often don't work for radix sort
     * @param <E> The type of object to determine the value of
     */
    @FunctionalInterface
    public interface ObjectValue<E> {

        /**
         * Determines the absolute value of an object
         * @param object The object to determine the value of
         * @return The value of the object
         */
        long value(E object);

    }

    /**
     * Unboxes the contents of a list and copies them into an array
     * @param <E> The type of value this copier copies
     */
    private static class Copier<E> implements Runnable {

        private final List<ValuedObject<E>> source;
        private final E[] target;
        private final int targetPosition;

        /**
         * @param source The source list consisting of valued objects
         * @param target The target array which holds the pure objects without their values
         * @param targetPosition Where to start in the target array
         */
        public Copier(List<ValuedObject<E>> source, E[] target, int targetPosition) {
            this.source = source;
            this.target = target;
            this.targetPosition = targetPosition;
        }

        public void run() {
            for (int i = 0; i < source.size(); i++) {
                target[i + targetPosition] = source.get(i).object;
            }
        }
    }

    private record ValuedObject<T>(T object, long value) {}

    private record Tupel<K>(K first, K second) {}
}
