package de.tgx03;

public final class Radix {

    private enum run {FIRST, FIRSTSOURCE, SECONDSOURCE}

    /**
     * Sorts a given long array using radix sort
     * Make sure to correctly set how many bits are relevant, as lower values increase performance,
     * but if set too low the result is wrong
     * @param source The source array
     * @param numberLength How many bits are relevant for sorting
     * @return The sorted array
     */
    public static long[] sort(long[] source, int numberLength) {
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
                return mergeLists(first);
            }

            case SECONDSOURCE -> {
                return mergeLists(second);
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
    private static long[] mergeLists(LongList[] source) {
        long[] result = new long[source[0].size() + source[1].size()];
        long[] first = source[0].toArray();
        System.arraycopy(first, 0, result, 0, first.length);
        long[] second = source[1].toArray();
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

}
